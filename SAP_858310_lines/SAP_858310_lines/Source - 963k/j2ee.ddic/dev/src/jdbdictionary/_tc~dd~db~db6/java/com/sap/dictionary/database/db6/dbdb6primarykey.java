package com.sap.dictionary.database.db6;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import com.sap.dictionary.database.dbs.DbColumn;
import com.sap.dictionary.database.dbs.DbColumns;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbIndexColumnInfo;
import com.sap.dictionary.database.dbs.DbObjectSqlStatements;
import com.sap.dictionary.database.dbs.DbPrimaryKey;
import com.sap.dictionary.database.dbs.DbSchema;
import com.sap.dictionary.database.dbs.DbSqlStatement;
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

public class DbDb6PrimaryKey extends DbPrimaryKey
{
	private String indName = null;
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);
	private static Location loc = Logger.getLocation("db6.DbDb6PrimaryKey");

	private static final String GET_PK_NAME = "SELECT a.INDNAME, USER_DEFINED, colname, colorder FROM SYSCAT.INDEXES a, syscat.indexcoluse b"
			+ " WHERE a.TABSCHEMA = CURRENT SCHEMA AND a.indschema = b.indschema AND a.indname = b.indname AND a.TABNAME = ? AND a.UNIQUERULE = 'P' "
			+ " AND b.colorder <> 'I' ORDER by b.colseq";
	private static final String GEN_PK_NAME = "SELECT GENERATE_UNIQUE() FROM SYSIBM.SYSDUMMY1";
	private static final String GET_PK_TYPE = "SELECT indname, indextype FROM syscat.indexes WHERE tabschema = CURRENT SCHEMA AND tabname = ? AND uniquerule = 'P'";

	private boolean isClustered = false;
	private DbDb6IncludeColumns includeColumns;

	public DbDb6PrimaryKey()
	{
		super();
	}

	public DbDb6PrimaryKey(DbFactory factory, DbPrimaryKey other)
	{
		super(factory, other);
	}

	public DbDb6PrimaryKey(DbFactory factory)
	{
		super(factory);
	}

	public DbDb6PrimaryKey(DbFactory factory, DbSchema schema, String tableName)
	{
		super(factory, schema, tableName);
	}

	public DbDb6PrimaryKey(DbFactory factory, String tableName)
	{
		super(factory, tableName);
	}

	public void setSpecificContentViaXml(XmlMap xmlMap) throws JddException
	{
		loc.entering("setSpecificContentViaXml");

		XmlMap xmlIndexProperties = xmlMap.getXmlMap("index-properties");
		if (!xmlIndexProperties.isEmpty())
		{
			//
			// clustered index ?
			//
			this.isClustered = xmlIndexProperties.getBoolean("is-clustered");
			if (isClustered)
				this.setSpecificIsSet(true);

			//
			// contains the index include columns ?
			//
			XmlMap xmlIncludeColumns = xmlIndexProperties.getXmlMap("include-columns");
			if (!xmlIncludeColumns.isEmpty())
			{
				this.includeColumns = new DbDb6IncludeColumns(xmlIncludeColumns);
				this.setSpecificIsSet(true);
			}
		}
		loc.exiting();
	}

	public void setCommonContentViaDb() throws JddException
	{
		loc.entering("setCommonContentViaDb");

		Connection con = getDbFactory().getConnection();
		boolean isDescending = false;
		ArrayList<DbIndexColumnInfo> columnsInfo = new ArrayList<DbIndexColumnInfo>();
		String colname;
		String colorder;

		try
		{
			PreparedStatement pst1 = NativeSQLAccess.prepareNativeStatement(con, GET_PK_NAME);
			pst1.setString(1, this.getTableName().toUpperCase());
			ResultSet rs1 = pst1.executeQuery();

			while (rs1.next())
			{
				if (rs1.getInt(2) == 1)
					this.indName = rs1.getString(1);

				colname = rs1.getString(3);
				colorder = rs1.getString(4);
				isDescending = "D".equals(colorder);
				columnsInfo.add(new DbIndexColumnInfo(colname, isDescending));
			}
			rs1.close();
			pst1.close();
		} catch (SQLException ex)
		{
			Object[] arguments = {ex.getMessage()};
			cat.errorT(loc, "setCommonContentViaDb failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		setContent(columnsInfo);
		loc.exiting();
	}

	public void setSpecificContentViaDb() throws JddException
	{
		loc.entering("setSpecificContentViaDb");

		Connection con = getDbFactory().getConnection();
		try
		{
			PreparedStatement ps = NativeSQLAccess.prepareNativeStatement(con, GET_PK_TYPE);
			ps.setString(1, this.getTableName().toUpperCase());
			ResultSet rset = ps.executeQuery();

			if (rset.next())
			{
				this.indName = rset.getString(1).toUpperCase();
				this.isClustered = rset.getString(2).equals("CLUS");
				includeColumns = new DbDb6IncludeColumns();
				includeColumns.getIncludeColumns(con, this.indName);
			}
			rset.close();
			ps.close();
		} catch (SQLException ex)
		{
			Object[] arguments = {ex.getMessage()};
			cat.errorT(loc, "setSpecificContentViaDb failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
		if (isClustered || includeColumns.getIncludeColumnList().size() > 0)
			this.setSpecificIsSet(true);
		loc.exiting();
	}

	public void writeSpecificContentToXmlFile(PrintWriter file, String offset0) throws JddException
	{
		String offset1 = offset0 + XmlHelper.tabulate();
		String offset2 = offset1 + XmlHelper.tabulate();

		file.println(offset0 + "<db6>");
		file.println(offset1 + "<index-properties>");
		file.println(offset2 + "<is-clustered>" + this.isClustered + "</is-clustered>");
		if (this.includeColumns != null && this.includeColumns.getIncludeColumnList().size() > 0)
			this.includeColumns.writeContentToXmlFile(file, offset2);
		file.println(offset1 + "</index-properties>");
		file.println(offset0 + "</db6>");
	}

	public DbObjectSqlStatements getDdlStatementsForCreate()
	{
		DbObjectSqlStatements pkDef = new DbObjectSqlStatements("PK/CREATE/" + this.getTableName());
		DbSqlStatement createStatement = new DbSqlStatement();
		DbSqlStatement alterStatement = new DbSqlStatement();
		String tabName = this.getTableName();
		String pkName = this.genereatePKname();

		createStatement.addLine("CREATE UNIQUE INDEX \"" + pkName + "\" ON \"" + tabName.toUpperCase() + "\"");
		createStatement.merge(getDdlColumnsClause());

		// Consider DB-sepcific settings: CLUSTER and INCLUDE COLUMNS
		if ((includeColumns != null) && (includeColumns.hasIncludeColumns()))
		{
			createStatement.addLine(" INCLUDE ");
			createStatement.merge(includeColumns.getDdlColumnsClause());
		}
		if (isClustered)
			createStatement.addLine(" CLUSTER");

		createStatement.addLine(" ALLOW REVERSE SCANS");

		alterStatement.addLine("ALTER TABLE \"" + tabName.toUpperCase() + "\" ADD CONSTRAINT \"" + pkName
				+ "\" PRIMARY KEY");
		alterStatement.merge(getDdlColumnsClause());

		pkDef.add(createStatement);
		pkDef.add(alterStatement);
		return pkDef;
	}

	public DbObjectSqlStatements getDdlStatementsForDrop()
	{
		DbObjectSqlStatements pkDrop = new DbObjectSqlStatements("PK/DROP/" + this.getTableName());
		DbSqlStatement dropStatement = new DbSqlStatement(true);
		String tabName = this.getTableName();

		//
		// drop primary key constraint
		//
		/*
		 * if (this.getDbSchema() != null) { tabName =
		 * this.getDbSchema().getSchemaName() + "." + this.getTableName(); } else {
		 * tabName = this.getTableName(); }
		 */

		dropStatement.addLine("ALTER TABLE " + '"' + tabName.toUpperCase() + '"' + " DROP PRIMARY KEY");

		pkDrop.add(dropStatement);

		//
		// drop underlying unique index if neccessary
		//
		if (this.indName != null)
		{
			DbSqlStatement indexStatement = new DbSqlStatement(true);
			String indName = this.indName;

			/*
			 * if (this.getDbSchema() != null) { indName =
			 * this.getDbSchema().getSchemaName() + "." + this.indName; } else {
			 * indName = this.indName; }
			 */

			indexStatement.addLine("DROP INDEX " + '"' + indName.toUpperCase() + '"');
			pkDrop.add(indexStatement);
		}

		return pkDrop;
	}

	//
	// Check the index's-width
	// return true - if index-width is o.k
	//
	public boolean checkWidth()
	{
		loc.entering("checkWidth");

		Iterator iter = this.getColumnNames().iterator();
		String colName = null;
		DbColumns columns = this.getTable().getColumns();
		DbColumn column;
		boolean check = true;
		int total = 0;

		while (iter.hasNext())
		{
			colName = ((DbIndexColumnInfo) iter.next()).getName();
			column = columns.getColumn(colName);
			if (column == null)
			{
				Object[] arguments = {this.getTableName(), colName};
				cat.errorT(loc, "checkWidth for primary key on table {0}: no column named {1}.", arguments);
				check = false;

				continue;
			}

			//
			// calculate index size with overhead for table with VALUE COMPRESSION
			//
			switch (column.getJavaSqlType())
			{
				case java.sql.Types.BLOB :
				case java.sql.Types.CLOB :
					Object[] arguments = {this.getTableName(), colName};
					cat.errorT(loc,
							"checkWidth for primary key on table {0}: column type for column {1} is not allowed in an index.",
							arguments);
					check = false; // not allowed in primary key
					break;

				case java.sql.Types.BIGINT :
					total += 10;
					break;

				case java.sql.Types.BINARY :
				case java.sql.Types.VARBINARY :
				case java.sql.Types.LONGVARBINARY :
					total += column.getLength() + 2;
					break;

				case java.sql.Types.CHAR :
				case java.sql.Types.VARCHAR :
				case java.sql.Types.LONGVARCHAR :
					total += (column.getLength() * 3) + 2;
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
					total += 10;
					break;

				case java.sql.Types.REAL :
					total += 6;
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
		// add row overhead
		//
		total += 2;

		if (total >= 1024)
		{
			check = false;

			Object[] arguments = {this.getTableName(), new Integer(total)};
			cat
					.errorT(
							loc,
							"checkWidth for primary key on table {0}: total width of all columns in primary key ({1}) including row overhead is greater than the allowed maximum (1023 byte) .",
							arguments);
		}

		loc.exiting();
		return check;
	}

	//
	// Checks if number of index-columns maintained is allowed
	// @return true if number of index-columns is correct, false otherwise
	//
	public boolean checkNumberOfColumns()
	{
		int numCols = this.getColumnNames().size();
		boolean check = (numCols > 0 && numCols <= 16);

		loc.entering("checkNumberOfColumns");
		if (check == false)
		{
			Object[] arguments = {this.getTableName()};
			cat.errorT(loc, "checkNumberOfColumns for primary key on table {0}: only 16 primary key columns are allowed.",
					arguments);
		}
		loc.exiting();

		return check;
	}

	//
	// Checks if primary key-columns are not null
	// return true - if number of primary-columns are all not null, false
	// otherwise
	//
	public boolean checkColumnsNotNull()
	{
		loc.entering("checkColumnsNotNull");

		Iterator iter = this.getColumnNames().iterator();
		String colName = null;
		DbColumns columns = this.getTable().getColumns();
		DbColumn column;
		boolean check = true;

		while (iter.hasNext())
		{
			colName = ((DbIndexColumnInfo) iter.next()).getName();
			column = columns.getColumn(colName);
			if (column == null || column.isNotNull() == false)
			{
				check = false;
				Object[] arguments = {this.getTableName(), colName};
				cat.errorT(loc, "checkColumnsNotNull for primary key on table {0}: column {1} must not be nullable",
						arguments);
			}
		}

		loc.exiting();
		return check;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.dictionary.database.dbs.DbPrimaryKey#replaceSpecificContent(com.sap.dictionary.database.dbs.DbPrimaryKey)
	 */
	@Override
	public void replaceSpecificContent(DbPrimaryKey pkey)
	{
		if (pkey != null)
		{
			this.isClustered = ((DbDb6PrimaryKey) pkey).isClustered();
			this.includeColumns = ((DbDb6PrimaryKey) pkey).getIncludeColumns();
		}
	}

	/**
	 * @return the includeColumns
	 */
	public DbDb6IncludeColumns getIncludeColumns()
	{
		return includeColumns;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.dictionary.database.dbs.DbPrimaryKey#checkSpecificContent()
	 */
	@Override
	public boolean checkSpecificContent()
	{
		boolean includeOk;

		if ((this.includeColumns != null) && (!this.includeColumns.isEmpty()))
		{
			// Check for uniqueness not needed: PK is always unique
			// INCLUDE columns must exist in the table
			includeOk = this.includeColumns.checkColumns(this.getTable().getColumns());
			if (!includeOk)
				return false;
		}
		// If a partitiong key exists,
		// check if all columns of the partitioning key are also
		// included in the primary key.
		DbDb6Table table = (DbDb6Table) this.getTable();
		if ((table.getPartitioningKey() != null) && (!table.getPartitioningKey().isEmpty()))
			if (!table.getPartitioningKey().checkColumns(this.getColumns()))
				return false;
		// A clustered primary key is not allowed if the table has MDC columns
		if (this.isClustered)
			if ((table.getMdcColumns() != null) && (!table.getMdcColumns().isEmpty()))
				return false;
		// only one clustered index allowed
		// check in DbDb6Indexes.checkSpecificContent()

		return true;
	}

	/**
	 * @return the isClustered
	 */
	public boolean isClustered()
	{
		return isClustered;
	}

	/**
	 * Create a name for a primary key. This is needed for DB6 as only in a
	 * CREATE INDEX statement we can specify DB-specific parameters like CLUSTER
	 * or include columns. The index must have a name and via this name it is
	 * afterwards promoted to the primary key with an ALTER TABLE statement (see
	 * getDdlStatementForCreate()). The name is created similar to how DB6
	 * creates names for constraints ("SQL" + a timestamp, 18 characters) with
	 * the help of the GENERATE_UNIQUE() function. As a fallback
	 * System.currentTimeMillis() is used. Note that this will result in shorter
	 * names (16 characters).
	 * 
	 * @return a unique name for the primary key
	 */
	private String genereatePKname()
	{
		Connection con = getDbFactory().getConnection();
		String pkname = "SQL" + System.currentTimeMillis();

		if (con != null)
		{
			try
			{
				PreparedStatement pst1 = NativeSQLAccess.prepareNativeStatement(con, GEN_PK_NAME);
				ResultSet rs1 = pst1.executeQuery();
				rs1.next();
				pkname = rs1.getString(1);
				pkname = pkname.substring(2, 17);
				pkname = "SQL" + pkname;

				rs1.close();
				pst1.close();
			} catch (SQLException ex)
			{
				Object[] arguments = { ex.getMessage() };
				cat.warningT(loc, "generatePKname failed: {0}", arguments);
				loc.exiting();
			}
		}

		return pkname;
	}

	/**
	 * 
	 * Help function. Returns all columns of the primary key as strings (instead
	 * of DbIndexColumnInfo as in getColumnNames()) in an ArrayList. Needed in
	 * DbDb6Table.checkSpecificContent().
	 * 
	 * @return ArrayList with the column names of the index as string.
	 */
	public ArrayList<String> getColumns()
	{
		ArrayList<String> columnList = new ArrayList<String>();
		DbIndexColumnInfo colInfo;

		for (int i = 0; i < this.getColumnNames().size(); i++)
		{
			colInfo = (DbIndexColumnInfo) this.getColumnNames().get(i);
			columnList.add(colInfo.getName());
		}
		return columnList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.dictionary.database.dbs.DbPrimaryKey#equalsSpecificContent(com.sap.dictionary.database.dbs.DbPrimaryKey)
	 */
	@Override
	public boolean equalsSpecificContent(DbPrimaryKey other)
	{
		if (other == null)
			return false;
		
		DbDb6PrimaryKey otherDb6PK = (DbDb6PrimaryKey) other;
		if (this.isClustered != otherDb6PK.isClustered)
			return false;
		
		// Comparing includeColumns:
		// includeColumns can be null, empty or filled
		//
		//  case  this.includeColumns  other.inlcudeColumns  action
		//  --------------------------------------------------------------------
		//  (1)   null                 null                  return true;
		//  (2)   !null                !null                 return this.includeColumns.equals(other.includeColumns)
		//  (3)   !null                null                  return this.includeColumns.equals(other.includeColumns)
		//          --> equals is required here because this.includeColumns can be empty (and result is true)
		//  (4)   null                 !null                 return other.includeColumns.equals(this.includeColumns)
		//          --> equals is required here because other.includeColumns can be empty (and result is true)
		// 
		// case (4)
		if ((this.includeColumns == null) && (otherDb6PK.includeColumns != null))
			return (!otherDb6PK.includeColumns.equals(this.includeColumns));
		// case (2) and (3)
		if (this.includeColumns != null)
			if (!this.includeColumns.equals(otherDb6PK.includeColumns))
				return false;

		// case (1)
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.dictionary.database.dbs.DbPrimaryKey#toString()
	 */
	@Override
	public String toString()
	{
		String superString = super.toString();
		String includeString = "";

		String clusterString = "Clustered Index       : " + this.isClustered;
		if (includeColumns != null)
			includeString = includeColumns.toString();

		return (superString + clusterString + "\n" + includeString);
	}
}