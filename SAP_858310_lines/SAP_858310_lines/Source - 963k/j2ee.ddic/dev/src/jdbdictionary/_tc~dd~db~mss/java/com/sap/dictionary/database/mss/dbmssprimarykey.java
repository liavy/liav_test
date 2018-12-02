package com.sap.dictionary.database.mss;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
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
import com.sap.dictionary.database.dbs.ExType;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlHelper;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.dictionary.database.dbs.DbPrimaryKeyDifference;
import com.sap.dictionary.database.dbs.Action;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;

import java.io.FileWriter;

/**
 * @author d000312
 *
 * Primary key handling
 */
public class DbMssPrimaryKey extends DbPrimaryKey {

	public boolean isNonClustered = false;
	// public boolean disallowPageLocks = false;
	
	private static Location loc = Logger.getLocation("mss.DbMssPrimaryKey");
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);


	public DbMssPrimaryKey() {
		super();
	}

	public DbMssPrimaryKey(DbFactory factory, DbPrimaryKey other) {
		super(factory, other);
	}

	public DbMssPrimaryKey(DbFactory factory) {
		super(factory);
	}

	public DbMssPrimaryKey(
		DbFactory factory,
		DbSchema schema,
		String tableName) {
		super(factory, schema, tableName);
	}

	public DbMssPrimaryKey(DbFactory factory, String tableName) {
		super(factory, tableName);
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbPrimaryKey#setSpecificContentViaXml(com.sap.dictionary.database.dbs.XmlMap)
	 * Definition of Signatures
	 */
	public void setSpecificContentViaXml(XmlMap xmlMap) throws JddException {
		loc.entering("setSpecificContentViaXml");

		// myTrace("DbMssPrimaryKey setSpecificContentViaXml");

		try {
			if (xmlMap.isEmpty() == false) {
				isNonClustered = xmlMap.getBoolean("is-nonclustered");
				// disallowPageLocks = xmlMap.getBoolean("disallow-pagelocks");
				// myTrace("isNonClustered " + isNonClustered);
			}
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "setSpecificContentViaXml failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		loc.exiting();
		return;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbPrimaryKey#setCommonContentViaDb()
	 */
	public void setCommonContentViaDb() throws JddException {
		loc.entering("setSpecificContentViaXml");

		Connection con = getDbFactory().getConnection();

		String name = " ";
		String tabname = " ";
		boolean isUnique = false;
		boolean isPrimaryKey = false;
		ArrayList columnList = new ArrayList();
		DatabaseMetaData dbmd = null;
		
		int status = 0;
		int indid = 0;
		int id = 0;
		
		tabname = this.getTableName(); // ???

		String schemaName = null;
		String prefix = null;
		schemaName = retrieveSchemaName(con);
		if (schemaName == null)
			prefix = "user";
		else
			prefix = "'" + schemaName + "'";

		// set status, indid, id
		try {
			Statement dstmt = NativeSQLAccess.createNativeStatement(con);
			// gd 170303 con.createStatement();

			java.sql.ResultSet drs =
				dstmt.executeQuery(
					"select si.indid, si.id from sysindexes si, sysobjects sopk "
						+ "where "
						+ "si.id = object_id(" + prefix + " + '.' + '" +  tabname + "') and "
						+ "si.id = sopk.parent_obj and sopk.xtype = 'PK' and "
						+ "si.name = sopk.name");

			if (drs.next()) {
				indid = drs.getInt(1);
				id = drs.getInt(2);
			} else {
				// what now?
				// throw exception "index doesn't exist"?
				drs.close();
				dstmt.close();

				throw new JddException(
					ExType.NOT_ON_DB,
					"primary key to " + tabname + " doesn't exist on database");
			}
			drs.close();
			dstmt.close();
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "setCommonContentViaDb failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		// columns
		StringBuffer queryBuffer =
			new StringBuffer(
				"select index_col(" + prefix + " + '.' + '" + tabname + "', " + indid + ", 1)");
		for (int col_i = 2; col_i <= 16; col_i++) {
			queryBuffer.append(
				", index_col(" + prefix + " + '.' + '" + tabname + "', " + indid + ", " + col_i + ")");
		}

		String query = queryBuffer.toString();
		String colName = null;

		try {
			Statement dstmt = NativeSQLAccess.createNativeStatement(con);
			// gd 170303 con.createStatement();
			java.sql.ResultSet drs = dstmt.executeQuery(query);

			if (drs.next()) {
				for (int col_i = 1; col_i <= 16; col_i++) {
					colName = drs.getString(col_i);
					if (colName == null)
						break;

					columnList.add(new DbIndexColumnInfo(colName, false));
				}
			}

			drs.close();
			dstmt.close();
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "setCommonContentViaDb failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		//The implemetation calls method setContent to set the index-definition
		setContent(columnList);

		loc.exiting();
		return;
	}
	
	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbPrimaryKey#setSpecificContentViaDb()
	 * Retrieve all necessary information from database to fill the specific content
	 */
	public void setSpecificContentViaDb() throws JddException {
		loc.entering("setSpecificContentViaDb");

		Connection con = getDbFactory().getConnection();

		String schemaName = null;
		String prefix = null;
		schemaName = retrieveSchemaName(con);
                if (schemaName == null)
			prefix = "user";
		else
			prefix = "'" + schemaName + "'";

		try {
			Statement dstmt = NativeSQLAccess.createNativeStatement(con);
			
			String pkName = null;
			
			java.sql.ResultSet drs = dstmt.executeQuery("select so1.name from sysobjects so1 "
				+ "where "
                                + "so1.parent_obj = object_id(" + prefix + " + '.' + '" + this.getTableName() + "') and "
				+ "so1.xtype = 'PK'");
			if (drs.next()) {
				pkName = drs.getString(1);	
			}
			drs.close();

			drs =	dstmt.executeQuery(
					"select INDEXPROPERTY(object_id(" + prefix + " + '.' + '"
						+ this.getTableName()
						+ "'), '"
						+ pkName
						+ "', 'isClustered') "
                        /*
						+ ", INDEXPROPERTY(object_id(" + prefix + " + '.' + '"
						+ this.getTableName()
						+ "'), '"
						+ pkName
						+ "', 'isPageLockDisallowed')"
						*/
						);

			if (drs.next()) {
				isNonClustered = (drs.getInt(1) == 0);
				// disallowPageLocks = (drs.getInt(2) == 1);
			}
			drs.close();
			dstmt.close();
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "setSpecificContentViaDb failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		super.setSpecificIsSet(true);

		loc.exiting();
		return;
	}

	/**
	 * Write specific content to XML file
	 * @param file     XML file in creation
	 * @param offset0  intent
	 * @throws JddException
	 */
	public void writeSpecificContentToXmlFile(PrintWriter file, String offset0)
		throws JddException {
		loc.entering("writeSpecificContentToXmlFile");

		/* omit writing of defaults */
		if (this.isNonClustered == true)
		{
			try {
				file.println(offset0 + "<mss>");

				String offset2 = offset0 + XmlHelper.tabulate();

				file.println(
				offset2
					+ "<is-nonclustered>"
					+ this.isNonClustered
					+ "</is-nonclustered>");
			
				file.println(offset0 + "</mss>");
			} catch (Exception ex) {
				Object[] arguments = { ex.getMessage()};
				cat.errorT(loc, "writeSpecificContentToXmlFile failed: {0}", arguments);
				loc.exiting();
				throw JddException.createInstance(ex);
			}
		}

		loc.exiting();
		return;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbPrimaryKey#getDdlStatementsForCreate()
	 * Build DDL statement for creation of primary key
	 */
	public DbObjectSqlStatements getDdlStatementsForCreate() {
		loc.entering("getDdlStatementsForCreate");

		DbObjectSqlStatements pkDef =
			new DbObjectSqlStatements(this.getTableName() + "$PK");
		// ???
		DbSqlStatement createStatement = new DbSqlStatement();
		String clusteredStr;

		if (isNonClustered)
			clusteredStr = " NONCLUSTERED ";
		else
			clusteredStr = " CLUSTERED ";

		String schemaName = null;
		String prefix = null;
		String userdot = null;
		if (this.getDbSchema() != null) {
			schemaName = this.getDbSchema().getSchemaName();
		}
		if (schemaName == null) {
			prefix = "user";
			schemaName = "";
			userdot = "";
		}
		else {
			prefix = "'" + schemaName + "'";
			userdot = schemaName + ".";
		}
		
		createStatement.addLine(
			"ALTER TABLE "
				+ userdot
				+ "["
				+ this.getTableName()
				+ "] "
				+ "ADD PRIMARY KEY"
				+ clusteredStr);
		createStatement.merge(getDdlColumnsClause());
		pkDef.add(createStatement);

		/* if (disallowPageLocks == true) {
			DbSqlStatement indexoptionStatement = new DbSqlStatement();
			indexoptionStatement.addLine("declare @pkName nvarchar(128)");
			indexoptionStatement.addLine(
			"select @pkName = '" + this.getTableName() + "' + '.' + so1.name from sysobjects so1 "
				+ "where "
				+ "so1.parent_obj = object_id(" + prefix + " + '.' + '" + this.getTableName() + "') and "
				+ "so1.xtype = 'PK'");
			indexoptionStatement.addLine(
			"if @pkName is not null "
				+ "exec sp_indexoption @pkName, 'DisAllowPageLocks', 'TRUE'");
			pkDef.add(indexoptionStatement);
		} */

		loc.exiting();
		return pkDef;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbPrimaryKey#getDdlColumnsClause()
	 * Build column clause for DDL creation statement
	 */
	public DbSqlStatement getDdlColumnsClause() {
		loc.entering("getDdlColumnsClause");

		DbSqlStatement colDef = new DbSqlStatement();

		colDef.addLine("(");

		ArrayList indexColumns = this.getColumnNames();
		Iterator icIterator = indexColumns.iterator();
		DbIndexColumnInfo indColumn = null;
		String sep = ",";
		while (icIterator.hasNext()) {
			indColumn = (DbIndexColumnInfo) icIterator.next();
			if (icIterator.hasNext() == false)
				sep = "";
			if (indColumn.isDescending())
				colDef.addLine("  [" + indColumn.getName() + "] " + sep);
			else
				colDef.addLine("  [" + indColumn.getName() + "]" + sep);
		}

		colDef.addLine(")");

		loc.exiting();
		return colDef;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbPrimaryKey#getDdlStatementsForDrop()
	 * Build DDL statement for dropping the primary key
	 */
	public DbObjectSqlStatements getDdlStatementsForDrop() {
		loc.entering("getDdlStatementsForDrop");

		DbObjectSqlStatements dropDef =
			new DbObjectSqlStatements(this.getTableName() + "$PK");
		DbSqlStatement dropLine = new DbSqlStatement(true);

		String schemaName = null;
		String prefix = null;
		String userdot = null;
		if (this.getDbSchema() != null) {
			schemaName = this.getDbSchema().getSchemaName();
		}
                if (schemaName == null) {
			prefix = "user";
			schemaName = "";
			userdot = "";
		}
		else {
			prefix = "'" + schemaName + "'";
			userdot = schemaName + ".";
		}

		// primary key has a name, either specified at create time or chosen by SQL Server
		// this name must be specified on drop
		dropLine.addLine("declare @pkName varchar(128)");
		dropLine.addLine(
			"select @pkName = so1.name from sysobjects so1 "
				+ "where "
				+ "so1.parent_obj = object_id(" + prefix + " + '.' + '" + this.getTableName() + "') and "
				+ "so1.xtype = 'PK'");
		dropLine.addLine(
			"if @pkName is not null "
				+ "exec ('ALTER TABLE "
				+ userdot
				+ "["
				+ this.getTableName()
				+ "] DROP CONSTRAINT [' + @pkName + ']')");

		dropDef.add(dropLine);

		return dropDef;
	}


		/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbPrimaryKey#compareTo()
	 * Compares this index to its target version
	 */
	public DbPrimaryKeyDifference compareTo(DbPrimaryKey target) throws JddException {
		DbPrimaryKeyDifference difference = super.compareTo(target);

		if (difference != null)
			return difference;
			
		// check of specific part
		
		loc.entering("compareTo");
		
		// clustered property

		try {
			if(this.isNonClustered != ((DbMssPrimaryKey) target).isNonClustered)
				difference = super.getDbFactory().makeDbPrimaryKeyDifference(this, target,
								 Action.DROP_CREATE);
		} catch (Exception ex) {
			throw JddException.createInstance(ex);
		}  	
		
		if (difference != null) {
			loc.exiting();
			return difference;
		}
		
		// disallowPageLocks property
		/*
		try {
			if(this.disallowPageLocks != ((DbMssPrimaryKey) target).disallowPageLocks)
				difference = super.getDbFactory().makeDbPrimaryKeyDifference(this, target,
									 Action.DROP_CREATE);
		} catch (Exception ex) {
			throw JddException.createInstance(ex);
		}
		*/  

		loc.exiting();
		return difference;
	}


	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbPrimaryKey#checkWidth()
	 * Check the primaryKeys's-width
	 * @return true  if primary Key-width is o.k
	 *         false if it exceeds the database limit
	 * 
	 */
	public boolean checkWidth() {
		loc.entering("checkWidth");

		// compute length of one entry, compare against maximum (900)
		Iterator iter = this.getColumnNames().iterator();
		String colName = null;
		DbColumns columns = this.getTable().getColumns();
		DbColumn column;
		boolean check = true;
		int total = 0;

		while (iter.hasNext()) {
			colName = ((DbIndexColumnInfo) iter.next()).getName();
			column = columns.getColumn(colName);
			if (column == null) {
				check = false;
				Object[] arguments = { this.getTableName(), colName };
				cat.errorT(loc, 
					"checkWidth (PK to {0}, column {1}): no such column in table",
					arguments);

				continue;
			}
			switch (column.getJavaSqlType()) {
				case java.sql.Types.BLOB :
				case java.sql.Types.CLOB :
				case java.sql.Types.LONGVARBINARY :
				case java.sql.Types.LONGVARCHAR :
					check = false; // not allowed in index/key

					Object[] arguments =
						{
							this.getTableName(),
							colName,
							column.getJavaSqlTypeName()};
					cat.errorT(loc, 
						"checkWidth (PK to {0}, column {1}): type {2} not allowed for primary key",
						arguments);
					break;

				case java.sql.Types.BIGINT :
					total += 8;
					break;
				case java.sql.Types.BINARY :
				case java.sql.Types.VARBINARY :
					total += column.getLength();
					break;
				case java.sql.Types.CHAR :
				case java.sql.Types.VARCHAR :
					total += (column.getLength() * 2);
					break;
				case java.sql.Types.DATE :
				case java.sql.Types.TIME :
				case java.sql.Types.TIMESTAMP :
					total += 8;
					break;
				case java.sql.Types.DECIMAL :
				case java.sql.Types.NUMERIC :
					long prec = column.getLength();
					if (prec < 10)
						total += 5;
					else if (prec < 20)
						total += 9;
					else if (prec < 29)
						total += 13;
					else
						total += 17;
					break;
				case java.sql.Types.DOUBLE :
				case java.sql.Types.FLOAT :
				case java.sql.Types.REAL :
					total += 8;
					break;
				case java.sql.Types.INTEGER :
					total += 4;
					break;
				case java.sql.Types.SMALLINT :
					total += 4;
					break;
				case java.sql.Types.TINYINT :
					total += 8;
					break;
			}
		}

		if (total > 900) {
			check = false;

			Object[] arguments = { this.getTableName(), new Integer(total)};
			cat.errorT(loc, 
				"checkWidth (PK to {0}): total width of primary key ({1}) greater than allowed maximum (900)",
				arguments);
		}

		loc.exiting();
		return check;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbPrimaryKey#checkNumberOfColumns()
	 * Check if number of primary key-columns maintained is allowed
	 * @return true  if number of primary-columns is correct
	 *         false if number of columns exceeds the database limit
	 */
	public boolean checkNumberOfColumns() {
		loc.entering("checkNumberOfColumns");

		int numCols = this.getColumnNames().size();
		boolean check = (numCols > 0 && numCols <= 16);

		if (check == false) {
			Object[] arguments = { this.getTableName(), new Integer(numCols)};
			cat.errorT(loc, 
				"checkNumberOfColumns (PK to {0}): column count {1} not in allowed range [1..16]",
				arguments);
		}
		loc.exiting();
		return check;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbPrimaryKey#checkColumnsNotNull()
	 * Check if primary key-columns are not null
	 * @return true  if number of primary-columns are all not null
	 *         false otherwise
	 */
	public boolean checkColumnsNotNull() {
		loc.entering("checkColumnsNotNull");

		Iterator iter = this.getColumnNames().iterator();
		String colName = null;
		DbColumns columns = this.getTable().getColumns();
		DbColumn column;
		boolean check = true;

		while (iter.hasNext()) {
			colName = ((DbIndexColumnInfo) iter.next()).getName();
			column = columns.getColumn(colName);
			if (column == null || column.isNotNull() == false) {
				check = false;
				Object[] arguments = { this.getTableName(), colName };
				cat.errorT(loc, 
					"checkColumnsNotNull (PK to {0}): column {1} must not be nullable",
					arguments);
			}
		}
		loc.exiting();
		return check;
	}


	/**
	 *  Checks the db-specific parameters of the primary key 
    	 *  @return true db specific parameters are o.k., false otherwise
    	 **/
    	public boolean checkSpecificContent() {
       		return true;
    	}


	/**
    	 *  Replaces the Db specific parameters of this primary key
    	 *  with those of other primary key
    	 *  @param other   an instance of DbPrimaryKey
    	 **/   
     	public void mergeDbSpecificContent(DbPrimaryKey other) {     

		this.isNonClustered = ((DbMssPrimaryKey)other).isNonClustered;
		// this.disallowPageLocks = ((DbMssPrimaryKey)other).disallowPageLocks;

		return;
 
     	}	

	public void replaceSpecificContent(DbPrimaryKey other) {     

		this.isNonClustered = ((DbMssPrimaryKey)other).isNonClustered;
		// this.disallowPageLocks = ((DbMssPrimaryKey)other).disallowPageLocks;

		return;
 
     	}	

	
        /**
         *  Checks if Database Specific Parameters of this Primary Key and another 
         *  Primary Key are the same. 
         *  True should be delivered if both Primary Key instances have no Database Specific 
         *  Parameters or if they are the same or differ in local parameters only. 
         *  In all other cases false should be the return value.
         *  Local parameters mean those which can not be maintained in xml but internally
         *  only to preserve properties on database (such as the tablespace where the table 
         *  is located) when drop/create or a conversion takes place.
         *  @param other   an instance of DbPrimaryKey
         *  @return true - if both key instances have no Database Specific Parameters or if they are 
         *  the same or differ in local parameters only.  
         **/      
         public boolean equalsSpecificContent(DbPrimaryKey other) {
                 return (this.isNonClustered == ((DbMssPrimaryKey)other).isNonClustered);
         }     


	public String toString() {
		return super.toString() + "is NonClustered       : " + this.isNonClustered + "\n";		
	 }


	/**
	 * Retrieve the SQL Server release of the connected database
	 * @param con  current connection
	 * @return     SQL Server release (major)
	 * @throws JddException
	 */
	private long retrieveSQLServerRelease(Connection con) {
		long rel = 8;

		try {
			Statement schStmt = NativeSQLAccess.createNativeStatement(con);
			
			java.sql.ResultSet rs = schStmt.executeQuery("select (@@microsoftversion / 65536) / 256");
			rs.next();
			rel = rs.getInt(1);
			rs.close();
			schStmt.close();
		} catch (Exception ex) {
                        loc.entering("retrieveSQLServerRelease");
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "retrieveSQLServerRelease failed: {0}", arguments);
			loc.exiting();
		}
		
		return rel;
	}


	/**
	 * Retrieve current schema's name (== user)
	 * @param con  current connection
	 * @return     schema name
	 * @throws JddException
	 */
	private String retrieveSchemaName(Connection con) throws JddException {
		String schemaName = null;

		if (this.getDbSchema() != null) {
			schemaName = this.getDbSchema().getSchemaName();
		} else {
			// determine schema name i'm in
			try {
				Statement schStmt = NativeSQLAccess.createNativeStatement(con);
				// gd 170303 con.createStatement();

				java.sql.ResultSet rs = schStmt.executeQuery("select user");
				rs.next();
				schemaName = rs.getString(1);
				rs.close();
				schStmt.close();
			} catch (Exception ex) {
				Object[] arguments = { ex.getMessage()};
				cat.errorT(loc, "retrieveSchemaName failed: {0}", arguments);
				loc.exiting();
				throw JddException.createInstance(ex);
			}
		}
		return schemaName;
	}

        /*
	private void myTrace(String str) {
		FileWriter out = null;
		
		System.out.println(str);
		try {
			out = new FileWriter("c:/GD_DbIndex.txt", true);
			out.write(str + "\n");
			out.close();
		}
		catch (Exception ex) {
		}
   	}
        */
}