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
import com.sap.dictionary.database.dbs.DbIndex;
import com.sap.dictionary.database.dbs.DbIndexColumnInfo;
import com.sap.dictionary.database.dbs.DbObjectSqlStatements;
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

public class DbDb6Index extends DbIndex
{

	private static final Category cat = Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);
	private static Location loc = Logger.getLocation("db6.DbDb6Index");

	private static final int V9_INDEX_NAME_LENGTH = 128;
	
	private static final String GET_UNIQUE_RULE = "SELECT UNIQUERULE FROM SYSCAT.INDEXES "
			+ "WHERE TABSCHEMA = CURRENT SCHEMA AND INDNAME = ? AND UNIQUERULE <> 'P'";
	private static final String GET_INDEX_COLS = "SELECT colname, colorder FROM syscat.indexcoluse "
			+ "WHERE indschema = CURRENT SCHEMA AND indNAME = ? AND colorder <> 'I' ORDER BY colseq";

	private boolean isClustered = false;
	private DbDb6IncludeColumns includeColumns;

	public DbDb6Index()
	{
		super();
	}

	public DbDb6Index(DbFactory factory)
	{
		super(factory);
	}

	public DbDb6Index(DbFactory factory, DbIndex other)
	{
		super(factory, other);
	}

	public DbDb6Index(DbFactory factory, String name, String tabname)
	{
		super(factory, name, tabname);
	}

	/**
	 * @return the includeColumns
	 */
	public DbDb6IncludeColumns getIncludeColumns()
	{
		return includeColumns;
	}

	/**
	 * @return the isClustered
	 */
	public boolean isClustered()
	{
		return isClustered;
	}

	public DbDb6Index(DbFactory factory, DbSchema schema, String name, String tabname)
	{
		super(factory, schema, name, tabname);
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

	public DbObjectSqlStatements getDdlStatementsForCreate()
	{
		DbObjectSqlStatements indexDef = new DbObjectSqlStatements(this.getName());
		DbSqlStatement createStatement = new DbSqlStatement();

		String unique = this.isUnique() ? "UNIQUE " : "";
		createStatement.addLine("CREATE" + " " + unique + "INDEX" + " " + '"' + this.getName().toUpperCase() + '"'
				+ " ON " + this.getTableName().toUpperCase() + " ");
		createStatement.merge(getDdlColumnsClause());
		if ((includeColumns != null) && (includeColumns.hasIncludeColumns()))
		{
			createStatement.addLine(" INCLUDE ");
			createStatement.merge(includeColumns.getDdlColumnsClause());
		}
		if (isClustered)
			createStatement.addLine(" CLUSTER");
		createStatement.addLine(" ALLOW REVERSE SCANS");
		indexDef.add(createStatement);

		return indexDef;
	}

	public void setCommonContentViaDb() throws JddException
	{
		loc.entering("setCommonContentViaDb");

		Connection con = getDbFactory().getConnection();
		boolean isUnique = false;
		boolean isDescending = false;
		ArrayList<DbIndexColumnInfo> columnsInfo = new ArrayList<DbIndexColumnInfo>();
		String colname;
		String colorder;

		try
		{
			PreparedStatement pst1 = NativeSQLAccess.prepareNativeStatement(con, GET_UNIQUE_RULE);
			PreparedStatement pst2 = NativeSQLAccess.prepareNativeStatement(con, GET_INDEX_COLS);
			pst1.setString(1, this.getName().toUpperCase());
			pst2.setString(1, this.getName().toUpperCase());
			ResultSet rs1 = pst1.executeQuery();

			if (rs1.next())
			{
				isUnique = "U".equals(rs1.getString(1));
				ResultSet rs2 = pst2.executeQuery();
				while (rs2.next())
				{
					colname = rs2.getString(1);
					colorder = rs2.getString(2);
					isDescending = "D".equals(colorder);
					columnsInfo.add(new DbIndexColumnInfo(colname, isDescending));
				}
				rs2.close();
			}

			rs1.close();
			pst1.close();
			pst2.close();
		} catch (SQLException ex)
		{
			Object[] arguments = {ex.getMessage()};
			cat.errorT(loc, "setCommonContentViaDb failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		setContent(isUnique, columnsInfo);
		loc.exiting();

	}

	public void setSpecificContentViaDb() throws JddException
	{
		loc.entering("setSpecificContentViaDb");

		Connection con = getDbFactory().getConnection();

		try
		{

			PreparedStatement ps = NativeSQLAccess.prepareNativeStatement(con, "SELECT INDEXTYPE FROM SYSCAT.INDEXES "
					+ "WHERE TABSCHEMA = CURRENT SCHEMA AND INDNAME = ? AND UNIQUERULE <> 'P' ");
			ps.setString(1, this.getName().toUpperCase());
			ResultSet rset = ps.executeQuery();

			if (rset.next())
			{
				this.isClustered = rset.getString(1).equals("CLUS");
			}
			rset.close();
			ps.close();

			includeColumns = new DbDb6IncludeColumns();
			includeColumns.getIncludeColumns(con, this.getName().toUpperCase());

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

	public boolean existsOnDb() throws JddException
	{
		loc.entering("existsOnDb");

		boolean exists = false;
		Connection con = getDbFactory().getConnection();

		try
		{
			PreparedStatement existsStatement = NativeSQLAccess.prepareNativeStatement(con,
					"SELECT 1 FROM SYSCAT.INDEXES WHERE INDSCHEMA = CURRENT SCHEMA AND INDNAME = ?");
			existsStatement.setString(1, this.getName().toUpperCase());
			ResultSet rs = existsStatement.executeQuery();
			exists = (rs.next() == true);
			rs.close();
			existsStatement.close();
		} catch (Exception ex)
		{
			Object[] arguments = {this.getName(), ex.getMessage()};
			cat.errorT(loc, "existence check for index {0} failed: {1}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		Object[] arguments = {this.getName(), exists ? "exits" : "doesn't exist"};
		cat.infoT(loc, "index {0} {1} on database", arguments);
		loc.exiting();
		return exists;
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
		DbColumns columns = this.getIndexes().getTable().getColumns();
		DbColumn column;
		boolean check = true;
		int total = 0;

		while (iter.hasNext())
		{
			colName = ((DbIndexColumnInfo) iter.next()).getName();
			column = columns.getColumn(colName);
			if (column == null)
			{
				check = false;

				Object[] arguments = {this.getName(), colName, this.getIndexes().getTable().getName()};
				cat.errorT(loc, "checkWidth for index {0}: no column named {1} in table {2}.", arguments);
				continue;
			}

			//
			// calculate index size with overhead for table with VALUE COMPRESSION
			//
			switch (column.getJavaSqlType())
			{
				case java.sql.Types.BLOB :
				case java.sql.Types.CLOB :
				{
					Object[] arguments = {this.getName(), colName};
					cat.errorT(loc, "checkWidth for index {0}: column type for column {1} is not allowed in an index.",
							arguments);
					check = false;
				}
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

			Object[] arguments = {this.getName(), new Integer(total)};
			cat
					.errorT(
							loc,
							"checkWidth for index {0}: total width of all columns in index ({1}) including row overhead is greater than the allowed maximum (1023) .",
							arguments);
		}

		loc.exiting();
		return check;
	}

	//
	// Check the index's name according to its length
	// return true - if name-length is o.k
	//
	public boolean checkNameLength()
	{
		int nameLen = this.getName().length();
		boolean check = (nameLen > 0 && nameLen <= V9_INDEX_NAME_LENGTH);

		loc.entering("checkNameLength");
		if (check == false)
		{
			Object[] arguments = {this.getName()};
			cat.errorT(loc, "checkNameLength for index {0}: index name length must range from 0 to " + V9_INDEX_NAME_LENGTH, arguments);
		}
		loc.exiting();

		return check;
	}

	//
	// Checks if indexname is a reserved word
	// return true - if index-name has no conflict with reserved words,
	// false otherwise
	//
	public boolean checkNameForReservedWord()
	{
		// no DB specific check as decided in JDBC meeting
		boolean check = true;

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
			cat.errorT(loc, "checkNumberOfColumns: only 16 index columns are allowed.");
		}
		loc.exiting();

		return check;
	}

	public void writeSpecificContentToXmlFile(PrintWriter file, String offset0)
	{
		String offset1 = offset0 + XmlHelper.tabulate();
		String offset2 = offset1 + XmlHelper.tabulate();

		file.println(offset0 + "<db6>");
		file.println(offset1 + "<index-properties>");
		file.println(offset2 + "<is-clustered>" + this.isClustered + "</is-clustered>");
		if (this.includeColumns != null && this.includeColumns.getIncludeColumnList().size() > 0)
		{
			this.includeColumns.writeContentToXmlFile(file, offset2);
		}
		file.println(offset1 + "</index-properties>");
		file.println(offset0 + "</db6>");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.dictionary.database.dbs.DbIndex#replaceDbSpecificContent(com.sap.dictionary.database.dbs.DbIndex)
	 */
	@Override
	public void replaceSpecificContent(DbIndex index)
	{
		if (index != null)
		{
			this.isClustered = ((DbDb6Index) index).isClustered();
			this.includeColumns = ((DbDb6Index) index).getIncludeColumns();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.dictionary.database.dbs.DbIndex#checkSpecificContent()
	 */
	@Override
	public boolean checkSpecificContent()
	{
		boolean includeOk;

		// INCLUDE columns can only be specified if the
		// index is unique.
		if ((this.includeColumns != null) && (!this.includeColumns.isEmpty()))
		{
			if (!isUnique())
				return false;

			// INCLUDE columns must exist in the table
			includeOk = this.includeColumns.checkColumns(this.getIndexes().getTable().getColumns());
			if (!includeOk)
				return false;
		}
		// If this is an unique index and a partitioning key exists,
		// check if all columns of the partitioning key are also
		// included in the unique index.
		DbDb6Table table = (DbDb6Table) this.getIndexes().getTable();		
		if (this.isUnique())
		{
			if ((table.getPartitioningKey() != null) && (!table.getPartitioningKey().isEmpty()))
				if (!table.getPartitioningKey().checkColumns(this.getColumns()))
					return false;
		}
		// A clustered index is not allowed if the table has MDC columns
		if (this.isClustered)
			if ((table.getMdcColumns() != null) && (!table.getMdcColumns().isEmpty()))
				return false;
		// only one clustered index allowed
		// check in DbDb6Indexes.checkSpecificContent()
		return true;
	}

	/**
	 * 
	 * Help function. Returns all columns of the index as strings (instead of
	 * DbIndexColumnInfo as in getColumnNames()) in an ArrayList. Needed in
	 * DbDb6Table.checkSpecificContent().
	 * 
	 * @return ArrayList with the column names of the index as strings.
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
	 * @see com.sap.dictionary.database.dbs.DbIndex#equalsSpecificContent(com.sap.dictionary.database.dbs.DbIndex)
	 */
	@Override
	public boolean equalsSpecificContent(DbIndex other)
	{
		if (other == null)
			return false;
		
		DbDb6Index otherDb6index = (DbDb6Index) other;
		if (this.isClustered != otherDb6index.isClustered)
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
		if ((this.includeColumns == null) && (otherDb6index.includeColumns != null))
			return (!otherDb6index.includeColumns.equals(this.includeColumns));
		// case (2) and (3)
		if (this.includeColumns != null)
			if (!this.includeColumns.equals(otherDb6index.includeColumns))
				return false;

		// case (1)
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.dictionary.database.dbs.DbIndex#toString()
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