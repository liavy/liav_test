package com.sap.dictionary.database.db6;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.sap.dictionary.database.dbs.Action;
import com.sap.dictionary.database.dbs.DbColumn;
import com.sap.dictionary.database.dbs.DbColumnIterator;
import com.sap.dictionary.database.dbs.DbColumns;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbIndexIterator;
import com.sap.dictionary.database.dbs.DbIndexes;
import com.sap.dictionary.database.dbs.DbObjectSqlStatements;
import com.sap.dictionary.database.dbs.DbSchema;
import com.sap.dictionary.database.dbs.DbSqlStatement;
import com.sap.dictionary.database.dbs.DbTable;
import com.sap.dictionary.database.dbs.DbTableDifference;
import com.sap.dictionary.database.dbs.ExType;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlHelper;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * Title: Analysis of table and view changes: DB6 specific classes Description:
 * DB6 specific analysis of table and view changes. Tool to deliver DB6 specific
 * database information. Copyright: Copyright (c) 2001 Company: SAP AG
 * 
 * @author Frank-Martin Haas , Frank-Herwig Walter
 * @version 1.0
 */

/**
 * @author d025792
 * 
 */
public class DbDb6Table extends DbTable
{

	private static final Category cat = Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);
	private static Location loc = Logger.getLocation("db6.DbDb6Table");

	//
	// DB6 specific table attributes
	//
	private DbDb6Tablespaces tableSpaces = null;
	private DbDb6PartitioningKey partitioningKey = null;
	private DbDb6MDC mdcColumns = null;
	private boolean isVolatile = false;
	private boolean isReorgPending = false;
	private boolean isCompressed = false;

	public DbDb6Table()
	{
		super();
	}

	public DbDb6Table(DbFactory factory)
	{
		super(factory);
	}

	public DbDb6Table(DbFactory factory, String name)
	{
		super(factory, name);
	}

	public DbDb6Table(DbFactory factory, DbSchema schema, String name)
	{
		super(factory, schema, name);
	}

	public DbDb6Table(DbFactory factory, DbTable other)
	{
		super(factory, other);
	}

	//
	// overwrite setCommonContentViaDb()
	// to check if table is in reorg pending state
	//
	public void setCommonContentViaDb(DbFactory factory) throws JddException
	{
		checkReorgPending(factory);
		super.setCommonContentViaDb(factory);
	}

	//
	// overwrite setColumnsViaDb() to call db specific setContentViaDb
	// super method does not call DB specific methods
	//
	public void setColumnsViaDb(DbFactory factory) throws JddException
	{
		loc.entering("setColumnsViaDb");
		try
		{
			DbDb6Columns cols = new DbDb6Columns(factory);
			cols.setTable(this);
			cols.setContentViaDb(factory);
			setColumns(cols);
		} catch (Exception ex)
		{
			Object[] arguments = {getName(), ex.getMessage()};
			cat.errorT(loc, "setColumnsViaDb ({0}) failed: {1}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	public void setTableSpecificContentViaXml(XmlMap xmlMap) throws JddException
	{
		loc.entering("setTableSpecificContentViaXml");

		//
		// read table properties
		//

		XmlMap xmlTableProperties = xmlMap.getXmlMap("table-properties");
		if (!xmlTableProperties.isEmpty())
		{
			XmlMap xmlMDCmap = xmlTableProperties.getXmlMap("mdc-dimensions");
			if ((!xmlMDCmap.isEmpty()) && (xmlMDCmap.get("columns") != null))
			{
				this.mdcColumns = new DbDb6MDC(xmlMDCmap);
				this.setSpecificIsSet(true);
			}

			XmlMap xmlPartitioning = xmlTableProperties.getXmlMap("partitioning-key");
			if ((!xmlPartitioning.isEmpty()) && (xmlPartitioning.get("columns") != null))
			{
				this.partitioningKey = new DbDb6PartitioningKey(xmlPartitioning);
				this.setSpecificIsSet(true);
			}

			this.isVolatile = xmlTableProperties.getBoolean("volatile");
			this.isCompressed = xmlTableProperties.getBoolean("rowcompression");
			if (this.isVolatile || this.isCompressed)
				this.setSpecificIsSet(true);
		}

		loc.exiting();
	}

	public void setTableSpecificContentViaDb() throws JddException
	{
		loc.entering("setTableSpecificContentViaDb");

		Connection con = getDbFactory().getConnection();
		boolean isPartitioned = false;
		boolean isClustered = false;

		try
		{
			// Tablespace handling:
			// Tablespaces are considered as an 'internal' attribute which must
			// survive a drop/create in one system. They are not 'transported'
			// via the xml description to other systems. Tablespace information
			// is therefore considered only in replaceSpecificContent() (for the
			// drop/create)
			// and in getDdlStatementsForCreate().
			// Tablespace information is read from the DB in this method.
			//
			// get volatile, isPartitioned, tablespace information
			//
			PreparedStatement tableSpaceStatement = NativeSQLAccess.prepareNativeStatement(con,
					"SELECT VOLATILE, CLUSTERED, COMPRESSION, PARTITION_MODE, TBSPACE, INDEX_TBSPACE, LONG_TBSPACE FROM SYSCAT.TABLES "
							+ "WHERE TABSCHEMA = CURRENT SCHEMA AND TABNAME = ?");
			tableSpaceStatement.setString(1, this.getName().toUpperCase());

			ResultSet rset = tableSpaceStatement.executeQuery();
			tableSpaces = new DbDb6Tablespaces();
			while (rset.next())
			{
				this.isVolatile = "C".equals(rset.getString(1));
				isClustered = "Y".equals(rset.getString(2));
				this.isCompressed = "R".equals(rset.getString(3)) || "B".equals(rset.getString(3));
				isPartitioned = "H".equals(rset.getString(4));
				this.tableSpaces.setTableSpaces(rset.getString(5), rset.getString(6), rset.getString(7));
			}
			rset.close();
			tableSpaceStatement.close();

			//
			// get partitioning info
			//
			if (isPartitioned)
			{
				partitioningKey = new DbDb6PartitioningKey();
				partitioningKey.getPartitioningColumns(con, getName().toUpperCase());
			}

			// 
			// get MDC columns
			// 
			if (isClustered)
			{
				mdcColumns = new DbDb6MDC();
				mdcColumns.getMDCDimensions(con, getName().toUpperCase());
			}

		} catch (SQLException ex)
		{
			Object[] arguments = {ex.getMessage()};
			cat.errorT(loc, "setTableSpecificContentViaDb failed: {0}", arguments);
			loc.exiting();

			throw JddException.createInstance(ex);
		}

		// Decide if specific parameters are found
		// The tablespace information is only kept internally and
		// not written to the xml. It therefore does not contribute
		// to the db specific parameters here.
		if (isClustered || isPartitioned || this.isVolatile || this.isCompressed)
			this.setSpecificIsSet(true);

		loc.exiting();
	}

	public DbObjectSqlStatements getDdlStatementsForCreate() throws JddException
	{
		loc.entering("getDdlStatementsForCreate");

		DbObjectSqlStatements tableDefStatements = new DbObjectSqlStatements(this.getName());
		DbSqlStatement createStatement = new DbSqlStatement();
		String tabName = this.getName();
		DbDb6Environment db6Env = (DbDb6Environment) getDbFactory().getEnvironment();

		//
		// build CREATE TABLE statement
		//
		try
		{
			createStatement.addLine("CREATE TABLE " + '"' + tabName.toUpperCase() + '"');
			createStatement.merge(this.getColumns().getDdlClause());

			//
			// add UNICODE attribute
			//
			createStatement.addLine(" CCSID UNICODE ");

			//
			// add tablespace attributes
			// (1) use specific attributes if available
			// (2) use default tablespace clause if available
			//
			if (tableSpaces != null)
			{
				if (tableSpaces.getDataTableSpace() != null)
				{
					createStatement.addLine(" IN " + tableSpaces.getDataTableSpace());
				}
				if (tableSpaces.getIndexTableSpace() != null)
				{
					createStatement.addLine(" INDEX IN " + tableSpaces.getIndexTableSpace());
				}
				if (tableSpaces.getLongTableSpace() != null)
				{
					createStatement.addLine(" LONG IN " + tableSpaces.getLongTableSpace());
				}
			} else if (db6Env.getDefaultTablespaceClause() == null)
			{
				//
				// try to determine default tablespaces if connection exists
				//
				Connection con = getDbFactory().getConnection();

				if (con != null && getDbFactory().getDatabase().getAbbreviation().equals("Db6"))
				{
					db6Env.retrieveDefaultTablespace(con);
					if (db6Env.getDefaultTablespaceClause() != null)
					{
						createStatement.addLine(db6Env.getDefaultTablespaceClause());
					}
				}

			} else
			{
				createStatement.addLine(db6Env.getDefaultTablespaceClause());
			}

			//
			// add MDC DIMENSION columns
			//
			if (mdcColumns != null)
			{
				ArrayList<String> mdcCols = mdcColumns.getMdcColumnList();
				String mdcClause = "( " + mdcCols.get(0);

				for (int ii = 1; ii < mdcCols.size(); ii++)
				{
					mdcClause = mdcClause + ", " + mdcCols.get(ii);
				}
				mdcClause = mdcClause + " )";
				createStatement.addLine(" ORGANIZE BY DIMENSIONS " + mdcClause);
			}

			//
			// add partitioning key clause
			//
			if (partitioningKey != null)
			{
				ArrayList<String> partKeyCols = partitioningKey.getPartitioningKey();
				String partKeyClause = "( " + partKeyCols.get(0);

				for (int ii = 1; ii < partKeyCols.size(); ii++)
				{
					partKeyClause = partKeyClause + ", " + partKeyCols.get(ii);
				}
				partKeyClause = partKeyClause + " )";
				createStatement.addLine(" DISTRIBUTE BY HASH " + partKeyClause);
			}

			//
			// add VALUE COMPRESSION clause
			//
			createStatement.addLine(" VALUE COMPRESSION ");
			tableDefStatements.add(createStatement);

			//
			// add ALTER TABLE VOLATILE statement
			//
			if (this.isVolatile)
			{
				DbSqlStatement volatileStatement = new DbSqlStatement();

				volatileStatement.addLine("ALTER TABLE " + '"' + tabName.toUpperCase() + '"' + " VOLATILE");
				tableDefStatements.add(volatileStatement);
			}

			//
			// add ALTER TABLE COMPRESS YES statement
			//
			if (this.isCompressed)
			{
				DbSqlStatement compressStatement = new DbSqlStatement();

				compressStatement.addLine("ALTER TABLE " + '"' + tabName.toUpperCase() + '"' + " COMPRESS YES");
				tableDefStatements.add(compressStatement);
			}

			//
			// add CREATE PRIMARY KEY statements
			//
			if (this.getPrimaryKey() != null)
			{
				tableDefStatements.merge(this.getPrimaryKey().getDdlStatementsForCreate());
			}

			//
			// add CREATE INDEX statements
			//
			if (this.getIndexes() != null)
			{
				tableDefStatements.merge(this.getIndexes().getDdlStatementsForCreate());
			}
		} catch (Exception ex)
		{
			Object[] arguments = {ex.getMessage()};
			cat.infoT(loc, "getDdlStatementsForCreate failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		loc.exiting();
		return tableDefStatements;
	}

	public void writeTableSpecificContentToXmlFile(PrintWriter file, String offset0) throws JddException
	{
		//
		// Write header
		//
		String offset1 = offset0 + XmlHelper.tabulate();
		String offset2 = offset1 + XmlHelper.tabulate();
		file.println(offset0 + "<db6>");

		//
		// Write table properties
		//
		file.println(offset1 + "<table-properties>");
		if (partitioningKey != null)
		{
			partitioningKey.writeContentToXmlFile(file, offset2);
		}
		if (mdcColumns != null)
		{
			mdcColumns.writeContentToXmlFile(file, offset2);
		}
		file.println(offset2 + "<volatile>" + this.isVolatile + "</volatile>");
		file.println(offset2 + "<rowcompression>" + this.isCompressed + "</rowcompression>");
		file.println(offset1 + "</table-properties>");
		file.println(offset0 + "</db6>");

		return;
	}

	public void setPrimaryKeyViaDb() throws JddException
	{
		DbFactory factory = getDbFactory();
		DbDb6PrimaryKey primaryKey = new DbDb6PrimaryKey(factory, this.getName());

		primaryKey.setCommonContentViaDb();

		if (primaryKey.getColumnNames().isEmpty())
		{
			super.setPrimaryKey(null);
		} else
		{
			super.setPrimaryKey(primaryKey);
		}

	}

	public void setIndexesViaDb() throws JddException
	{
		loc.entering("setIndexesViaDb");

		DbFactory factory = getDbFactory();
		Connection con = factory.getConnection();
		DbIndexes dbIndexes = new DbIndexes(factory);

		try
		{
			PreparedStatement indexStatement = NativeSQLAccess
					.prepareNativeStatement(
							con,
							"SELECT INDNAME FROM SYSCAT.INDEXES "
									+ " WHERE TABSCHEMA = CURRENT SCHEMA AND TABNAME = ? AND UNIQUERULE <> 'P' AND INDEXTYPE NOT IN ('BLOK', 'DIM')");
			indexStatement.setString(1, getName().toUpperCase());

			ResultSet rset = indexStatement.executeQuery();

			while (rset.next())
			{
				DbDb6Index dbIndex = new DbDb6Index(factory, getSchema(), getName(), rset.getString(1));

				dbIndex.setCommonContentViaDb();
				dbIndexes.add(dbIndex);
			}

			rset.close();
			indexStatement.close();
		} catch (Exception ex)
		{
			Object[] arguments = {ex.getMessage()};
			cat.errorT(loc, "setIndexesViaDb failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		super.setIndexes(dbIndexes);
		loc.exiting();
	}

	public boolean existsOnDb() throws JddException
	{
		loc.entering("existsOnDb");

		boolean exists = false;
		Connection con = getDbFactory().getConnection();

		try
		{
			PreparedStatement existsStatement = NativeSQLAccess.prepareNativeStatement(con,
					"SELECT 1 FROM SYSCAT.TABLES WHERE TABSCHEMA = CURRENT SCHEMA AND TABNAME = ? AND TYPE ='T' ");
			existsStatement.setString(1, this.getName().toUpperCase());
			ResultSet rs = existsStatement.executeQuery();
			exists = (rs.next() == true);
			rs.close();
			existsStatement.close();
		} catch (Exception ex)
		{
			Object[] arguments = {this.getName(), ex.getMessage()};
			cat.errorT(loc, "existence check for table {0} failed: {1}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		Object[] arguments = {this.getName(), exists ? "exits" : "doesn't exist"};
		cat.infoT(loc, "table {0} {1} on database", arguments);
		loc.exiting();
		return exists;
	}

	// 
	// returns true if table is in reorg pending state
	//
	public boolean isReorgPending()
	{
		return isReorgPending;
	}

	/**
	 * Delivers the names of views using this table as basetable
	 * 
	 * @return The names of dependent views as ArrayList
	 * @exception JddException
	 *               error during selection detected
	 */
	public ArrayList<String> getDependentViews() throws JddException
	{
		loc.entering("getDependentViews");

		ArrayList<String> names = new ArrayList<String>();
		Connection con = getDbFactory().getConnection();
		DbDb6Environment db6Env = (DbDb6Environment) getDbFactory().getEnvironment();
		String schemaName = db6Env.getCurrentSchema();
		String viewSchema;

		//
		// get view names from SYSCAT.VIEWDEP
		//
		try
		{
			PreparedStatement getViewStatement = NativeSQLAccess.prepareNativeStatement(con,
					"SELECT VIEWSCHEMA, VIEWNAME FROM SYSCAT.VIEWDEP " + "WHERE BSCHEMA = CURRENT SCHEMA AND BNAME = ? ");
			getViewStatement.setString(1, this.getName().toUpperCase());
			ResultSet rs = getViewStatement.executeQuery();

			while (rs.next())
			{
				viewSchema = rs.getString(1).trim();

				if (viewSchema.compareTo(schemaName) != 0)
				{
					//
					// views from other schema reference this table
					//
					String message;
					message = "view " + rs.getString(2) + " in foreign schema " + viewSchema + " references table "
							+ this.getName().toUpperCase();
					cat.errorT(loc, message);
					loc.exiting();
					rs.close();
					getViewStatement.close();
					throw new JddException(ExType.OTHER, message);
				}

				names.add(rs.getString(2));
			}

			rs.close();
			getViewStatement.close();

		} catch (Exception ex)
		{
			Object[] arguments = {this.getName(), ex.getMessage()};
			cat.errorT(loc, "retrieval of view names referencing table {0} failed: {1}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		loc.exiting();
		return (names);
	}

	public boolean existsData() throws JddException
	{
		loc.entering("existsData");

		boolean exists = false;
		Connection con = getDbFactory().getConnection();
		String tabName = this.getName();

		/*
		 * if ( this.getSchema() != null ) { tabName =
		 * this.getSchema().getSchemaName() + "." + this.getName(); } else {
		 * tabName = this.getName(); }
		 */

		try
		{
			Statement dstmt = NativeSQLAccess.createNativeStatement(con);
			ResultSet drs = dstmt.executeQuery("SELECT 1 FROM " + '"' + tabName.toUpperCase() + '"'
					+ " FETCH FIRST 1 ROWS ONLY OPTIMIZE FOR 1 ROWS");
			exists = (drs.next() == true);
			drs.close();
			dstmt.close();
		} catch (Exception ex)
		{
			Object[] arguments = {this.getName(), ex.getMessage()};
			cat.errorT(loc, "existsData failed for table {0} : {1}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		Object[] arguments = {this.getName(), exists ? "" : "no"};
		cat.infoT(loc, "table {0} contains {1} data", arguments);
		loc.exiting();
		return exists;
	}

	//
	// Check the table's name according to its length
	// return true - if name-length is o.k
	//
	public boolean checkNameLength()
	{
		int nameLen = this.getName().length();
		boolean check = (nameLen > 0 && nameLen <= 128);

		loc.entering("checkNameLength");
		if (check == false)
		{
			cat.errorT(loc, "checkNameLength: index name length must range from 0 to 128 .");
		}
		loc.exiting();

		return check;
	}

	//
	// Checks if tablename is a reserved word
	// return true - if table-name has no conflict with reserved words,
	// false otherwise
	//
	public boolean checkNameForReservedWord()
	{
		boolean isReserved = DbDb6Environment.isReservedWord(this.getName());

		return (!isReserved);
	}

	//
	// Check the table-width
	// @return true - if table-width is o.
	//
	public boolean checkWidth()
	{
		loc.entering("checkWidth");

		DbColumns columns = this.getColumns();
		DbColumnIterator iter = columns.iterator();
		DbColumn column;
		boolean check = true;
		int total = 0;
		int colCnt = 0;
		int varCnt = 0;

		while (iter.hasNext())
		{
			column = (DbColumn) iter.next();
			colCnt++;

			switch (column.getJavaSqlType())
			{
				case java.sql.Types.BLOB :
				case java.sql.Types.CLOB :
					long length = column.getLength();
					if (column.getJavaSqlType() == java.sql.Types.CLOB)
						length *= 3;
					if (length >= 1070000000)
						total += 278;
					else if (length >= 536000000)
						total += 254;
					else if (length >= 134000000)
						total += 222;
					else if (length >= 4190000)
						total += 198;
					else if (length >= 524000)
						total += 166;
					else if (length >= 65536)
						total += 142;
					else if (length >= 8192)
						total += 118;
					else if (length >= 1024)
						total += 94;
					else
						total += 70;
					break;

				/*
				 * for native DB2 LONG types case java.sql.Types.LONGVARBINARY: case
				 * java.sql.Types.LONGVARCHAR: total += 22; break;
				 */

				case java.sql.Types.BIGINT :
					total += 10;
					break;

				case java.sql.Types.BINARY :
					total += column.getLength() + 2;
					break;

				case java.sql.Types.LONGVARBINARY : // open SQL uses LONGVARBINARY
					// as VARBINARY
				case java.sql.Types.VARBINARY :
					total += column.getLength() + 2;
					varCnt++;
					break;

				case java.sql.Types.CHAR :
					total += (column.getLength() * 3) + 2;
					break;

				case java.sql.Types.LONGVARCHAR : // open SQL uses LONGVARCHAR as
					// VARCHAR
				case java.sql.Types.VARCHAR :
					total += (column.getLength() * 3) + 2;
					varCnt++;
					break;

				case java.sql.Types.DATE :
					total += 6;
					break;

				case java.sql.Types.TIME :
					total += 5;
					break;

				case java.sql.Types.TIMESTAMP :
					total += 12;
					break;

				case java.sql.Types.DECIMAL :
				case java.sql.Types.NUMERIC :
					total += column.getLength() / 2 + 3;
					break;

				case java.sql.Types.DOUBLE :
				case java.sql.Types.FLOAT :
				case java.sql.Types.REAL :
					total += 10;
					break;

				case java.sql.Types.INTEGER :
					total += 6;
					break;

				case java.sql.Types.SMALLINT :
				case java.sql.Types.TINYINT :
					total += 4;
					break;
			}
		}

		//
		// 2 byte row overhead
		//
		total += 2;

		//
		// we assume that tables are created on at least 16K tablespaces
		//
		if (total > 16293)
		{
			check = false;

			Object[] arguments = {this.getName(), new Integer(total)};
			cat
					.errorT(
							loc,
							"checkWidth {0}: total width of table ({1}) including row overhead is greater than the allowed maximum for 16K tablespaces .",
							arguments);
		}

		loc.exiting();
		return check;
	}

	public String toString()
	{
		String superString = super.toString();
		String volatileString = "Volatile              : " + this.isVolatile;
		String compressionString = "Compression           : " + this.isCompressed;
		String partString = "";
		String mdcString = "";

		// Do not consider tablespaces - 'local' parameters
		if (partitioningKey != null)
			partString = partitioningKey.toString();
		if (mdcColumns != null)
			mdcString = mdcColumns.toString();

		return (superString + volatileString + "\n" + compressionString + "\n" + partString + mdcString);
	}

	//
	// checks if table is reorg pending
	//
	private void checkReorgPending(DbFactory factory) throws JddException
	{
		loc.entering("checkReorgPending");

		Connection con = getDbFactory().getConnection();
		DbDb6Environment db6Env = (DbDb6Environment) getDbFactory().getEnvironment();
		String dbVersion = db6Env.getDatabaseVersion();

		if (dbVersion == null || dbVersion.compareTo("SQL09") < 0)
			return;

		try
		{
			//
			// need to include SYSIBM.SYSDUMMY1 in the statement to avoid SQL0873
			// errors
			//
			Statement reorg_stmt = NativeSQLAccess.createNativeStatement(con);
			ResultSet reorg_rs = reorg_stmt.executeQuery("SELECT REORG_PENDING FROM SYSIBM.SYSDUMMY1, "
					+ "TABLE ( ADMIN_GET_TAB_INFO( CURRENT SCHEMA , '" + this.getName().toUpperCase() + "' ) ) AS X");
			if (reorg_rs.next())
				isReorgPending = reorg_rs.getString(1).equals("Y");
			reorg_rs.close();
			reorg_stmt.close();

			if (isReorgPending)
			{
				Object[] arguments = {this.getName()};
				cat.infoT(loc, "checkReorgPending: table {0} is reorg pending and needs to be reorged.", arguments);
			}
		} catch (Exception ex)
		{
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		loc.exiting();
	}

	public DbTableDifference compareTo(DbTable target) throws Exception
	{
		DbDb6TableDifference tableDiff = (DbDb6TableDifference) super.compareTo(target);

		//
		// set Action to ALTER
		// if no Action exists for other reasons and the table is reorg pending
		//
		if (isReorgPending && (tableDiff == null || tableDiff.getAction() == Action.NOTHING))
		{
			if (tableDiff == null)
				tableDiff = new DbDb6TableDifference(this, target);
			tableDiff.setAction(Action.ALTER);
		}
		return tableDiff;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.dictionary.database.dbs.DbTable#replaceTableSpecificContent(com.sap.dictionary.database.dbs.DbTable)
	 */
	@Override
	public void replaceTableSpecificContent(DbTable table)
	{
		if (table != null)
		{
			this.isCompressed = ((DbDb6Table) table).isCompressed();
			this.isVolatile = ((DbDb6Table) table).isVolatile();
			this.mdcColumns = ((DbDb6Table) table).getMdcColumns();
			this.partitioningKey = ((DbDb6Table) table).getPartitioningKey();
			this.tableSpaces = ((DbDb6Table) table).getTableSpaces();
		}
	}

	/**
	 * @return the isCompressed
	 */
	public boolean isCompressed()
	{
		return isCompressed;
	}

	/**
	 * @return the isVolatile
	 */
	public boolean isVolatile()
	{
		return isVolatile;
	}

	/**
	 * @return the mdcColumns
	 */
	public DbDb6MDC getMdcColumns()
	{
		return mdcColumns;
	}

	/**
	 * @return the partitioningKey
	 */
	public DbDb6PartitioningKey getPartitioningKey()
	{
		return partitioningKey;
	}

	/**
	 * @return the tableSpaces
	 */
	public DbDb6Tablespaces getTableSpaces()
	{
		return tableSpaces;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.dictionary.database.dbs.DbTable#checkSpecificContent()
	 */
	@Override
	public boolean checkTableSpecificContent()
	{
		boolean mdcOk = true, mdcOk1 = true, mdcOk2 = true, mdcOk3 = true;
		boolean partitioningOk = true;
		boolean partitioningOk1 = true, partitioningOk2 = true, partitioningOk3 = true;

		// The check should be possible without a DB connection.
		// 
		// For MDC and partitioning key columns we check if
		// every column exists in the table.
		// For the partitioning key we also check if the
		// columns are contained in every unique index.
		// For MDC columns we also check if a clustered index exists.
		if ((mdcColumns != null) && (!mdcColumns.isEmpty()))
		{
			mdcOk1 = this.mdcColumns.checkColumns(this.getColumns());
         // check if a clustered secondary index exists
			if (this.getIndexes() != null)
			{
				DbIndexIterator indexIter = this.getIndexes().iterator();
				DbDb6Index index = null;
				while (indexIter.hasNext())
				{
					index = (DbDb6Index) indexIter.next();
					if (index.isClustered())
					{
						mdcOk2 = false;
						break;
					}
				}
			}
			// check if the primary key is clustered
			if (this.getPrimaryKey() != null)
			{
				DbDb6PrimaryKey db6pkey = (DbDb6PrimaryKey) this.getPrimaryKey();
				if (db6pkey.isClustered())
					mdcOk3 = false;
			}
		}
		mdcOk = (mdcOk1 && mdcOk2 && mdcOk3);

		if ((partitioningKey != null) && (!partitioningKey.isEmpty()))
		{
			partitioningOk1 = this.partitioningKey.checkColumns(this.getColumns());
			// check if columns are contained in secondary indexes
			if (this.getIndexes() != null)
			{
				DbIndexIterator indexIter = this.getIndexes().iterator();
				DbDb6Index index = null;
				while (indexIter.hasNext())
				{
					index = (DbDb6Index) indexIter.next();
					if (index.isUnique())
						partitioningOk2 = this.partitioningKey.checkColumns(index.getColumns());
					if (!partitioningOk2)
						break;
				}
			}
			// check if columns are contained in primary key
			DbDb6PrimaryKey pkey = (DbDb6PrimaryKey) this.getPrimaryKey();
			if (pkey != null)
				partitioningOk3 = this.partitioningKey.checkColumns(pkey.getColumns());

		}
		partitioningOk = (partitioningOk1 && partitioningOk2 && partitioningOk3);

		if (mdcOk && partitioningOk)
			return true;
		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.dictionary.database.dbs.DbTable#equalsSpecificContent(com.sap.dictionary.database.dbs.DbTable)
	 */
	@Override
	public boolean equalsTableSpecificContent(DbTable other)
	{
		
		if (other == null)
			return false;
		DbDb6Table otherDb6table = (DbDb6Table) other;

		if (this.isCompressed != otherDb6table.isCompressed)
			return false;
		if (this.isVolatile != otherDb6table.isVolatile)
			return false;
		
		// Comparing mdcColumns and partitioningKey:
		// both can be null, empty or filled
		//
		// decision matrix for mdcColumns:
		//
		//  case  this.mdcColumns  other.mdcColumns  action
		//  --------------------------------------------------------------------
		//  (1)   null             null              return true;
		//  (2)   !null            !null             return this.mdcColumns.equals(other.mdcColumns)
		//  (3)   !null            null              return this.mdcColumns.equals(other.mdcColumns)
		//          --> equals is required here because this.mdcColumns can be empty (and result is true)
		//  (4)   null             !null             return other.mdcColumns.equals(this.mdcColumns)
		//          --> equals is required here because other.mdcColumns can be empty (and result is true)
		// 		

		// case (4)
		if ((this.mdcColumns == null) && (otherDb6table.mdcColumns != null))
			return (!otherDb6table.mdcColumns.equals(this.mdcColumns));
		// case (2) and (3)
		if (this.mdcColumns != null)
			if (!this.mdcColumns.equals(otherDb6table.mdcColumns))
				return false;		

		// case (4)
		if ((this.partitioningKey == null) && (otherDb6table.partitioningKey != null))
			return (!otherDb6table.partitioningKey.equals(this.partitioningKey));
		// case (2) and (3)
		if (this.partitioningKey != null)
			if (!this.partitioningKey.equals(otherDb6table.partitioningKey))
				return false;			

		// case (1)
		return true;
	}

}