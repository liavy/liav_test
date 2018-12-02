package com.sap.dictionary.database.mss;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
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
import com.sap.dictionary.database.dbs.ExType;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlHelper;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.dictionary.database.dbs.DbIndexDifference;
import com.sap.dictionary.database.dbs.Action;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;

import java.io.FileWriter;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and Xml-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public class DbMssIndex extends DbIndex {

	public boolean isClustered = false;
	// public boolean disallowPageLocks = false; // not supported currently

	private static Location loc = Logger.getLocation("mss.DbMssIndex");
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);


	public DbMssIndex() {
		super();
	}

	public DbMssIndex(DbFactory factory) {
		super(factory);
	}

	public DbMssIndex(DbFactory factory, DbIndex other) {
		super(factory, other);
	}

	public DbMssIndex(DbFactory factory, String tableName, String indexName) {
		super(factory, tableName, indexName);
	}

	public DbMssIndex(
		DbFactory factory,
		DbSchema schema,
		String tableName,
		String indexName) {
		super(factory, schema, tableName, indexName);
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbIndex#setSpecificContentViaXml(com.sap.dictionary.database.dbs.XmlMap)
	 */
	public void setSpecificContentViaXml(XmlMap xmlMap) throws JddException {
		loc.entering("setSpecificContentViaXml");

		//myTrace("setSpecificContentViaXml");
		//myTrace("this.getTableName() " + this.getTableName() + ", " + 
                //        "this.getName() " + this.getName());

		try {
			if (xmlMap.isEmpty() == false) {
				isClustered = xmlMap.getBoolean("is-clustered");
				// disallowPageLocks = xmlMap.getBoolean("disallow-pagelocks");
                        }
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "setSpecificContentViaXml failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		//myTrace("isClustered " + isClustered);
		//myTrace("disallowPageLocks " + disallowPageLocks); 
		loc.exiting();
		return;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbIndex#setCommonContentViaDb()
	 * Database-catalog-method to get index-information
	 */
	public void setCommonContentViaDb() throws JddException {
		loc.entering("setCommonContentViaDb");

		Connection con = getDbFactory().getConnection();
		String name = this.getName();
		String tabname = this.getTableName();
		boolean isUnique = false;
		ArrayList columnList = new ArrayList();
		DatabaseMetaData dbmd = null;
		long sqlServerVersion = 7;
		
		int status = 0;
		int indid = 0;
		int id = 0;
		
		sqlServerVersion = retrieveSQLServerRelease(con);

		String schemaName = null;
		String prefix = null;
		schemaName = retrieveSchemaName(con);			
		if (schemaName == null) {
			prefix = "user";
		} else {
			prefix = "'" + schemaName + "'";
		}
			
		// set status, indid, id
		try {
			Statement dstmt = NativeSQLAccess.createNativeStatement(con);
			// gd 170303 con.createStatement();

			java.sql.ResultSet drs =
				dstmt.executeQuery(
					"select si.status, si.indid, si.id from sysindexes si "
						+ "where "
                                                + "si.id = object_id(" + prefix + " + '.' + '" + tabname + "') and " 
						+ "si.name = '"	+ name + "'");

			if (drs.next()) {
				status = drs.getInt(1);
				indid = drs.getInt(2);
				id = drs.getInt(3);
			} else {
				// what now?
				// throw exception "index doesn't exist"?
				Object[] arguments = { tabname, name };

				drs.close();
				dstmt.close();

				cat.errorT(loc,
					"index {0}.{1} doesn't exist on database",
					arguments);
				loc.exiting();
				throw new JddException(
					ExType.NOT_ON_DB,
					"index " + tabname + "." + name + " doesn't exist on db");
			}
			drs.close();
			dstmt.close();
		} catch (SQLException ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "setCommonContentViaDb failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		// uniqueness, primary key
		isUnique = ((status & 2) > 0);

		// columns
		StringBuffer queryBuffer =
			new StringBuffer(
				"select index_col(" + prefix + " + '.' + '" + tabname + "', " + indid + ", 1)");
		if (sqlServerVersion > 7)
			queryBuffer.append(
				", indexkey_property("
					+ id
					+ ", "
					+ indid
					+ ", 1, 'IsDescending')");

		for (int col_i = 2; col_i <= 16; col_i++) {
			queryBuffer.append(
				", index_col(" + prefix + " + '.' + '" + tabname + "', " + indid + ", " + col_i + ")");
			if (sqlServerVersion > 7)
				queryBuffer.append(
					", indexkey_property("
						+ id
						+ ", "
						+ indid
						+ ", "
						+ col_i
						+ ", 'IsDescending')");
		}

		String query = queryBuffer.toString();
		int factor = (sqlServerVersion > 7) ? 2 : 1;
		String colName = null;
		boolean isDescending = false;

		try {
			Statement dstmt = NativeSQLAccess.createNativeStatement(con);
			// gd 170303 con.createStatement();
			java.sql.ResultSet drs = dstmt.executeQuery(query);

			if (drs.next()) {
				for (int col_i = 0; col_i < 16; col_i++) {
					colName = drs.getString(col_i * factor + 1);
					if (colName == null)
						break;

					if (sqlServerVersion > 7)
						isDescending = drs.getInt(col_i * factor + 2) == 1;
					columnList.add(
						new DbIndexColumnInfo(colName, isDescending));
				}
			}

			drs.close();
			dstmt.close();
		} catch (SQLException ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "setCommonContentViaDb failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		setContent(isUnique, columnList);

		loc.exiting();

		return;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbIndex#setSpecificContentViaDb()
	 * Retrieve all necessary inforamtion from database to set specific content
	 */
	public void setSpecificContentViaDb() throws JddException {
		loc.entering("setSpecificContentViaDb");

		Connection con = getDbFactory().getConnection();

		//myTrace("setSpecificContentViaDb");
		//myTrace("this.getTableName() " + this.getTableName() + ", " + 
                //        "this.getName() " + this.getName());

		String schemaName = retrieveSchemaName(con);
		String prefix = null;
		if (schemaName == null)
			prefix = "user";
		else
			prefix = "'" + schemaName + "'";

		// gd2108 determine clustered property
		try {
			Statement dstmt = NativeSQLAccess.createNativeStatement(con);
			
			java.sql.ResultSet drs =
				dstmt.executeQuery(
					"select INDEXPROPERTY(object_id(" + prefix + " + '.' + '"
						+ this.getTableName()
						+ "'), '"
						+ this.getName()
						+ "', 'isClustered') "
			/*
                        + ", INDEXPROPERTY(object_id(" + prefix + " + '.' + '"
						+ this.getTableName()
						+ "'), '"
						+ this.getName()
						+ "', 'isPageLockDisallowed')"*/
						);

			if (drs.next()) {
				isClustered = (drs.getInt(1) == 1);
				// disallowPageLocks = (drs.getInt(2) == 1);
			}
			drs.close();
			dstmt.close();

			super.setSpecificIsSet(true);
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "setSpecificContentViaDb failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		//myTrace("this.isClustered " + isClustered + ", " + 
                //        "this.disallowPageLocks " + disallowPageLocks);


		loc.exiting();
		return;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbIndex#existsOnDb()
	 * Check existence of index on database
	 */
	public boolean existsOnDb() throws JddException {
		loc.entering("existsOnDb");
		boolean exists = false;
		Connection con = getDbFactory().getConnection();

		String schemaName = retrieveSchemaName(con);
		String prefix = null;
		if (schemaName == null)
			prefix = "user";
		else
			prefix = "'" + schemaName + "'";
	
		try {
			Statement dstmt = NativeSQLAccess.createNativeStatement(con);
			java.sql.ResultSet drs =
				dstmt.executeQuery(
					"select 1 from sysobjects so, sysindexes si "
						+ "where so.id = object_id(" + prefix + " + '.' + '" + this.getTableName() + "') and "
						+ "so.type = 'U' and "
						+ "si.id = so.id and si.name = '" + this.getName() + "'");
			exists = (drs.next() == true);
			drs.close();
			dstmt.close();
		} catch (SQLException sqlex) {
			Object[] arguments =
				{ this.getTableName(), this.getName(), sqlex.getMessage()};
			cat.errorT(loc,
				"existence check for index {0}.{1} failed: {2}",
				arguments);
			loc.exiting();

			throw new JddException(ExType.SQL_ERROR, sqlex.getMessage());
		} catch (Exception ex) {
			Object[] arguments =
				{ this.getTableName(), this.getName(), ex.getMessage()};
			cat.errorT(loc,
				"existence check for index {0}.{1} failed: {2}",
				arguments);
			loc.exiting();

			throw new JddException(ExType.OTHER, ex.getMessage());
		}

		Object[] arguments =
			{
				this.getTableName(),
				this.getName(),
				exists ? "exits " : "doesn't exist" };
		cat.infoT(loc, "index {0}.{1} {2} on db", arguments);
		loc.exiting();
		return exists;
	}

	/**
	 * write specific content to XML file
	 * @param file           XML file in creation
	 * @param offset0        intent
	 * @throws JddException  
	 */
	public void writeSpecificContentToXmlFile(PrintWriter file, String offset0)
		throws JddException {
		loc.entering("writeSpecificContentToXmlFile");

		/* omit writing of defaults */
		if (this.isClustered == true)
		{
			try {
				String offset2 = offset0 + XmlHelper.tabulate();

				file.println(offset0 + "<mss>");

				file.println(
				offset2
					+ "<is-clustered>"
					+ this.isClustered
					+ "</is-clustered>");
				
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
	 * @see com.sap.dictionary.database.dbs.DbIndex#getDdlStatementsForCreate()
	 * build DDL statement for index creation
	 */
	public DbObjectSqlStatements getDdlStatementsForCreate() {
		loc.entering("getDdlStatementsForCreate");

		// gd2208 not yet part of DbIndex/definition?
		DbObjectSqlStatements indexDef =
			new DbObjectSqlStatements(this.getName());
		DbSqlStatement createStatement = new DbSqlStatement();
		String clusteredStr;
		String uniqueStr;

		if (this.isUnique())
			uniqueStr = " UNIQUE ";
		else
			uniqueStr = " ";
			
		// gd030106 special handling of J2EE_CONFIGENTRY-index
		if ((this.getName().compareToIgnoreCase("J2EE_ENTRY_I0") == 0) ||
                    (this.getName().compareToIgnoreCase("SHD_ENTRY_I0") == 0))
			isClustered = true;
			
		if (isClustered)
			clusteredStr = " CLUSTERED ";
		else
			clusteredStr = " ";

		String schemaName = null;
		if (this.getSchema() != null) {
			schemaName = this.getSchema().getSchemaName();
		}
		String userdot = "";
		if (schemaName != null) {
			userdot = schemaName + ".";
		}

		createStatement.addLine(
			"CREATE"
				+ uniqueStr
				+ clusteredStr
				+ " INDEX ["
				+ this.getName()
				+ "]  ON "
				+ userdot
				+ "["
				+ this.getTableName()
				+ "]");
		createStatement.merge(getDdlColumnsClause());
		indexDef.add(createStatement);

		/*if (this.disallowPageLocks == true) {
			DbSqlStatement indexoptionStatement = new DbSqlStatement();
			indexoptionStatement.addLine(
				"exec sp_indexoption '"
                                + this.getTableName() + "." + this.getName()
				+ "', 'DisAllowPageLocks', 'TRUE'"); 
			indexDef.add(indexoptionStatement);
                }*/

		loc.exiting();
		return indexDef;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbIndex#getDdlColumnsClause()
	 * build columns clause for DDL creation statement
	 */
	public DbSqlStatement getDdlColumnsClause() {
		loc.entering("getDdlColumnsClause");

		DbSqlStatement colDef = new DbSqlStatement();

		colDef.addLine("(");

		Iterator icIterator = this.getColumnNames().iterator();
		DbIndexColumnInfo indColumn = null;
		String sep = ",";
		while (icIterator.hasNext()) {
			indColumn = (DbIndexColumnInfo) icIterator.next();
			if (icIterator.hasNext() == false)
				sep = "";
			if (indColumn.isDescending())
				colDef.addLine("  [" + indColumn.getName() + "] DESC" + sep);
			else
				colDef.addLine("  [" + indColumn.getName() + "]" + sep);
		}

		colDef.addLine(")");

		return colDef;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbIndex#getDdlStatementsForDrop()
	 * build DDL statement for dropping index
	 */
	public DbObjectSqlStatements getDdlStatementsForDrop() {
		loc.entering("getDdlStatementsForDrop");

		// gd2208 not yet part of DbIndex/definition?
		DbObjectSqlStatements dropDef =
			new DbObjectSqlStatements(this.getName());
		DbSqlStatement dropLine = new DbSqlStatement(true);

		String schemaName = null;
		if (this.getSchema() != null) {
			schemaName = this.getSchema().getSchemaName();
		}
		String userdot = "";
		if (schemaName != null) {
			userdot = schemaName + ".";
		}

		dropLine.addLine(
			"DROP INDEX "
				+ userdot
				+ "["
				+ this.getTableName()
				+ "].["
				+ this.getName()
				+ "]");
		dropDef.add(dropLine);

		loc.exiting();
		return dropDef;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbIndex#compareTo()
	 * Compares this index to its target version
	 */
	public DbIndexDifference compareTo(DbIndex target) throws JddException {
		DbIndexDifference difference = super.compareTo(target);

		if (difference != null)
			return difference;
			
		// check of specific part
		
		loc.entering("compareTo");
		
		// clustered property
		
		// for J2EE_CONFIGENTRY to enforce clustered index
		if ((this.getName().compareToIgnoreCase("J2EE_ENTRY_I0") == 0) ||
                    (this.getName().compareToIgnoreCase("SHD_ENTRY_I0") == 0)) {
			try {
				if(isClustered == false) 	
					difference = super.getDbFactory().makeDbIndexDifference(this, target,
									 Action.DROP_CREATE);
			} catch (Exception ex) {
				throw JddException.createInstance(ex);
			}  	 
		}
		else {
			try {
				if(this.isClustered != ((DbMssIndex) target).isClustered)	
					difference = super.getDbFactory().makeDbIndexDifference(this, target,
									 Action.DROP_CREATE);
			} catch (Exception ex) {
				throw JddException.createInstance(ex);
			}  	
		}

		if (difference != null) {
			loc.exiting();
			return difference;
		}
		
		
		// disallowPageLocks property
		/*
		try {		
			if(this.disallowPageLocks != ((DbMssIndex) target).disallowPageLocks)
				difference = super.getDbFactory().makeDbIndexDifference(this, target,
									 Action.DROP_CREATE);
		} catch (Exception ex) {
			throw JddException.createInstance(ex);
		} 
		*/

		loc.exiting();
		return difference;
	}
		
	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbIndex#checkWidth()
	 * Check the index's-width
	 * @return true - if index-width is o.k
	 */
	public boolean checkWidth() {
		loc.entering("checkWidth");

		// compute length of one entry, compare against maximum (900)
		Iterator iter = this.getColumnNames().iterator();
		String colName = null;
		DbColumns columns = this.getIndexes().getTable().getColumns();
		DbColumn column;
		boolean check = true;
		int total = 0;

		while (iter.hasNext()) {
			colName = ((DbIndexColumnInfo) iter.next()).getName();
			column = columns.getColumn(colName);
			if (column == null) {
				check = false;

				Object[] arguments =
					{ this.getName(), colName, this.getTableName()};
				cat.errorT(loc, 
					"checkWidth {0}: no column {1} in table {2} ",
					arguments);
				continue;
			}
			switch (column.getJavaSqlType()) {
				case java.sql.Types.BLOB :
				case java.sql.Types.CLOB :
					{
						check = false; // not allowed in index/key

						Object[] arguments = { this.getName(), colName };
						cat.errorT(loc, 
							"checkWidth {0}: column of type LOB ({1}) not allowed in index",
							arguments);
					}
					break;

				case java.sql.Types.BIGINT :
					total += 8;
					break;
				case java.sql.Types.BINARY :
				case java.sql.Types.VARBINARY :
				case java.sql.Types.LONGVARBINARY :
					total += column.getLength();
					break;
				case java.sql.Types.CHAR :
				case java.sql.Types.VARCHAR :
				case java.sql.Types.LONGVARCHAR :
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

			Object[] arguments = { this.getName(), new Integer(total)};
			cat.errorT(loc, 
				"checkWidth {0}: total width of index ({1}) greater than allowed maximum (900)",
				arguments);
		}

		loc.exiting();
		return check;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbIndex#checkNameLength()
	 * Check the index's name according to its length
	 * @return true - if name-length is o.k
	 */
	public boolean checkNameLength() {
		loc.entering("checkNameLength");

		int nameLen = this.getName().length();
		boolean check = (nameLen > 0 && nameLen <= 128);

		if (check == false) {
			Object[] arguments = { this.getName(), new Integer(nameLen)};
			cat.errorT(loc, 
				"checkNameLength {0}: length {1} invalid (valid range [1..128])",
				arguments);
		}
		loc.exiting();
		return check;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbIndex#checkNameForReservedWord()
	 * Checks if indexname is a reserved word
	 * @return true - if index-name has no conflict with reserved words,
	 *                   false otherwise
	 */
	public boolean checkNameForReservedWord() {
		loc.entering("checkNameForReservedWord");

		boolean check =
			(DbMssEnvironment.isReservedWord(this.getName()) == false);

		if (check == false) {
			Object[] arguments = { this.getName()};
			cat.errorT(loc, "{0} is a reserved word", arguments);
		}
		loc.exiting();
		return check;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbIndex#checkNumberOfColumns()
	 * Checks if number of index-columns maintained is allowed
	 * @return true if number of index-columns is correct
	 *         false otherwise
	 */
	public boolean checkNumberOfColumns() {
		loc.entering("checkNumberOfColumns");

		int numCols = this.getColumnNames().size();
		boolean check = (numCols > 0 && numCols <= 16);

		if (check == false) {
			Object[] arguments = { this.getName(), new Integer(numCols)};
			cat.errorT(loc, 
				"checkNumberOfColumns{0}: column count {1} not in allowed range [1..16]",
				arguments);
		}
		loc.exiting();
		return check;
	}


	/**
    	 *  Checks the db-specific parameters of this index 
    	 *  @return true db specific parameters are o.k., false otherwise
   	 **/
    	public boolean checkSpecificContent() {
      		return true;
    	}


	/**
    	 *  Replaces the Db specific parameters of this index
    	 *  with those of other index
    	 *  @param other   an instance of DbIndex
    	 **/   
     	public void mergeDbSpecificContent(DbIndex other) { 

		/*myTrace("mergeDbSpecificContent");
		myTrace("this.getTableName() " + this.getTableName() + ", " + 
                        "this.getName() " + this.getName());
		if (other != null)
			myTrace("other.getTableName() " + other.getTableName() + ", " + 
                        "other.getName() " + other.getName());
		else
			myTrace("other null");
                */

		if (other == null)
			return;
		
		this.isClustered = ((DbMssIndex) other).isClustered;
		// this.disallowPageLocks = ((DbMssIndex) other).disallowPageLocks;
 		return;

     	}

	public void replaceSpecificContent(DbIndex other) { 

		/*myTrace("mergeDbSpecificContent");
		myTrace("this.getTableName() " + this.getTableName() + ", " + 
                        "this.getName() " + this.getName());
		if (other != null)
			myTrace("other.getTableName() " + other.getTableName() + ", " + 
                        "other.getName() " + other.getName());
		else
			myTrace("other null");
                */

		if (other == null)
			return;
		
		this.isClustered = ((DbMssIndex) other).isClustered;
		// this.disallowPageLocks = ((DbMssIndex) other).disallowPageLocks;
 		return;

     	}


        /**
         *  Checks if Database Specific Parameters of this index and another index 
         *  are the same. 
         *  True should be delivered if both index instances have no Database Specific 
         *  Parameters or if they are the same or differ in local parameters only. 
         *  In all other cases false should be the return value.
         *  Local parameters mean those which can not be maintained in xml but internally
         *  only to preserve properties on database (such as tablespaces where the table 
         *  is located) when drop/create or a conversion takes place.
         *  @param other   an instance of DbIndex
         *  @return true - if both index instances have no Database Specific Parameters or if they are 
         *  the same or differ in local parameters only.  
         **/      
         public boolean equalsSpecificContent(DbIndex other) {
                 return (this.isClustered == ((DbMssIndex) other).isClustered);
         }     


         public String toString() {
		return super.toString() + "is Clustered          : " + this.isClustered + "\n";		
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
	 * Retrieve the current schema's name (== user)
	 * @param con  current connection
	 * @return     user's name
	 * @throws JddException
	 */
	private String retrieveSchemaName(Connection con) {
		String schemaName = null;

		if (this.getSchema() != null) {
			schemaName = this.getSchema().getSchemaName();
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
                                loc.entering("retrieveSchemaName");
				Object[] arguments = { ex.getMessage()};
				cat.errorT(loc, "retrieveSchemaName failed: {0}", arguments);
				loc.exiting();
			}
		}
		return schemaName;
	}

   
   /*
   private void myTrace(String str) {
	FileWriter out = null;
		
	System.out.println(str);
	try {
		out = new FileWriter("c:/GDTrace.txt", true);
		out.write(str + "\n");
		out.close();
	}
	catch (Exception ex) {
	}
   }
   */
   
}