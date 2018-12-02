package com.sap.dictionary.database.db4;

import com.sap.dictionary.database.dbs.DbColumnIterator;
import com.sap.dictionary.database.dbs.DbColumns;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbIndexes;
import com.sap.dictionary.database.dbs.DbPrimaryKey;
import com.sap.dictionary.database.dbs.DbObjectSqlStatements;
import com.sap.dictionary.database.dbs.DbTable;
import com.sap.dictionary.database.dbs.DbSchema;
import com.sap.dictionary.database.dbs.DbSqlStatement;
import com.sap.dictionary.database.dbs.ExType;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Title:        DbDb4Table
 * Copyright:    Copyright (c) 2002
 * Company:      SAP AG
 * @author       Michael Tsesis & Kerstin Hoeft, Dorothea Rink
 */

public class DbDb4Table extends DbTable {

	private static final Location loc = Logger.getLocation("db4.DbDb4Table");
	private static final Category cat =
		Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);

	//----------------
	//  constructors  -------------------------------------------------------------------
	//----------------

	public DbDb4Table() {
		super();
	}

	public DbDb4Table(DbFactory factory) {
		super(factory);
	}

	public DbDb4Table(DbFactory factory, DbTable other) {
		super(factory, other); // other currently not used
	}

	public DbDb4Table(
		DbFactory factory,
		DbSchema schema,
		String name // table name
	) {
		super(factory, schema, name);
	}

	public DbDb4Table(DbFactory factory, String name) {
		super(factory, name);
	}

	//------------------
	// public methods  ---------------------------------------------------------
	//------------------

	// Taken from .dbs to call DbDb4Indexes() and DbDb4Columns()
	public void setCommonContentViaXml(XmlMap xmlMap) throws JddException {
		loc.entering(cat, "setCommonContent(XmlMap)");
		super.setCommonContentViaXml(xmlMap);

		/*
		 * Override DbColumns and DbIndexes with DbDb4 objects.
		 * Don't throw special exceptions. This should be done by 
		 * parent class already.
		 */
		try {
			DbFactory factory = this.getDbFactory();
			XmlMap table = xmlMap.getXmlMap("Dbtable");
			loc.debugT(
				cat,
				"DbDb4Table.setCommonContentViaXml(): "
					+ "Replace DbColumns with DbDb4Columns.");
			setColumns(new DbDb4Columns(factory, table.getXmlMap("columns")));
			loc.debugT(
				cat,
				"DbDb4Table.setCommonContentViaXml(): "
					+ "Replace DbIndexes with DbDb4Indexes.");
			XmlMap indexesMap = table.getXmlMap("indexes");
			if (indexesMap != null) {
				setIndexes(new DbDb4Indexes(factory, indexesMap));
			}
		} catch (Exception ex) { //$JL-EXC$
			throw JddException.createInstance(ex);
		} finally {
			loc.exiting();
		}

	}

	/**
	 *  Reads the table specific parameters out of the XmlMap and fills the
	 *  corresponding database-dependend variables 
	 *  @param xmlMap               the table-XmlMap containing the values
	 *                                for the specific properties    
	 * */
	public void setTableSpecificContentViaXml(XmlMap xmlMap) {
		/*
		 * Currently not needed. 
		 * Maybe add still support for volatile flag, though not provided by db yet
		 */
		this.setSpecificIsSet(true);
	}

	/**
	 *  Writes the table specific variables to an xml-document
	 *  @param file              the destination file
	 *  @param offset0           the base-offset for the outermost tag
	 * */
	public void writeTableSpecificContentToXmlFile(
		PrintWriter file,
		String offset0) {
		/*
		 * Currently not needed. 
		 * Maybe add still support for volatile flag, though not provided by db yet
		 */
	}

	/**
	 *  Reads the table's specific information from the database and filles the 
	 *  corresponding table variables which are database dependent. 
	 * */
	public void setTableSpecificContentViaDb() {
		/*
		 * Currently not needed. 
		 * Maybe add still support for volatile flag, though not provided by db yet
		 */
		this.setSpecificIsSet(true);
	}

	// Like father class but uses Db4 specific classes
	public void setCommonContentViaDb(DbFactory factory) throws JddException {
		loc.entering(cat, "setCommonContentViaDb(DbFactory)");
		try {
			setColumnsViaDb(factory);
			setPrimaryKeyViaDb();
			setIndexesViaDb();
		} catch (Exception ex) { //$JL-EXC$
			cat.errorT(
				loc,
				"Exception caught setting table contents: {0}.",
				new Object[] { ex.getMessage()});
			throw JddException.createInstance(ex);
		} finally {
			loc.exiting();
		}
	}

	// Like father class but uses Db4 specific classes
	public void setColumnsViaDb(DbFactory factory) throws JddException {
		loc.entering(cat, "setColumnsViaDb(DbFactory)");
		DbDb4Columns cols = new DbDb4Columns(factory);
		cols.setTable(this);
		cols.setContentViaDb(factory);
		setColumns(cols);
		loc.exiting();
	}

	/**
	 *  Retrieves a list of indexes for this table from the database, 
	 *  and sets all attributes by calling DbDb4Index.setCommonContentsViaDb()
	 *  and DbDb4Index.setSpecificContentsViaDb().
	 **/
	public void setIndexesViaDb() throws JddException {
		loc.entering(cat, "setIndexesViaDb()");
		ArrayList indexNames = null;
		Iterator iterator = null;
		DbFactory factory = this.getDbFactory();
		Connection con = null;

		DbIndexes indexes = new DbIndexes(factory);

		// Fetch index names from db.
		indexNames = getIndexNamesViaDb(factory);

		// Fetch index information from db
		iterator = indexNames.iterator();
		while (iterator.hasNext()) {
			indexes.add(setIndexViaDb((String) iterator.next()));
		}

		this.setIndexes(indexes);
		loc.exiting();
	}

	/**
	 *  Reads the primary key for this table from database and adds a
	 *  primary key object of class DbPrimaryKey to this table if the primary
	 *  key exists.
	 * */
	public void setPrimaryKeyViaDb() throws JddException {
		loc.entering(cat, "setPrimaryKeyViaDb()");
		DbDb4PrimaryKey primKey =
			new DbDb4PrimaryKey(
				this.getDbFactory(),
				this.getSchema(),
				this.getName());
		primKey.setCommonContentViaDb();
		if (!primKey.isEmpty()) {

			// columns are set => primary key exists; set remaining attributes
			primKey.setSpecificContentViaDb(); // up to now a noop
			this.setPrimaryKey(primKey); // add primary key to table object
		}
		loc.exiting();
	}

	// Taken from .dbs to add quotes to table name
	public DbObjectSqlStatements getDdlStatementsForCreate()
		throws JddException {
		/*
		 * Method throws exception in case of missing parameters.
		 */
		loc.entering(cat, "getDdlStatementsForCreate()");
		String tableName = null;
		DbIndexes indexes = this.getIndexes();
		DbColumns columns = this.getColumns();
		DbPrimaryKey primaryKey = this.getPrimaryKey();
		DbObjectSqlStatements tableDef = new DbObjectSqlStatements(tableName);
		DbSqlStatement createLine = new DbSqlStatement();

		try {
			tableName = this.getName().trim().toUpperCase();
			createLine.addLine("CREATE TABLE " + "\"" + tableName + "\"");
			createLine.merge(columns.getDdlClause());
			tableDef.add(createLine);
			if (indexes != null) {
				tableDef.merge(indexes.getDdlStatementsForCreate());
			}
			if (primaryKey != null) {
				tableDef.merge(primaryKey.getDdlStatementsForCreate());
			}
			loc.debugT(cat, "Generated: {0}.", new Object[] { tableDef });
			return tableDef;
		} catch (Exception ex) { //$JL-EXC$
			cat.errorT(loc, "Exception caught: " + ex.getMessage());
			throw JddException.createInstance(ex);
		} finally {
			loc.exiting();
		}
	}

	// Taken from .dbs to add quotes to table name
	public DbObjectSqlStatements getDdlStatementsForDrop()
		throws JddException {
		/*
		 * Method throws exception in case of missing parameters.
		 */
		loc.entering(cat, "getDdlStatementsForDrop()");
		String tableName = null;

		DbObjectSqlStatements tableDef =
			new DbObjectSqlStatements(this.getName());
		DbSqlStatement dropLine = new DbSqlStatement(true);
		// tolerate "Object not found"
		try {
			tableName = this.getName().trim().toUpperCase();
			dropLine.addLine("DROP TABLE " + "\"" + tableName + "\" CASCADE");
			tableDef.add(dropLine);
			loc.debugT(cat, "Generated: {0}.", new Object[] { tableDef });
			return tableDef;
		} catch (Exception ex) { //$JL-EXC$
			cat.errorT(loc, "Exception caught: " + ex.getMessage());
			throw JddException.createInstance(ex);
		} finally {
			loc.exiting();
		}
	}

	/**
	 *  Analyses if table iexists on database or not
	 *  @return true - if table exists in database, false otherwise. 
	 * */
	public boolean existsOnDb() throws JddException {
		loc.entering(cat, "existsOnDb()");
		boolean existsOnDb = true;
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String tableName = null;
		String stmtStr =
			"SELECT TABLE_NAME "
				+ "FROM SYSTABLES "
				+ "WHERE TABLE_NAME = ? AND TABLE_TYPE = ?";
		try {
			tableName = this.getName().trim().toUpperCase();
			con = this.getDbFactory().getConnection();

			// Retrieve Native SQL PreparedStatement from Open SQL connection
			pstmt = NativeSQLAccess.prepareNativeStatement(con, stmtStr);
			pstmt.setString(1, tableName);
			pstmt.setString(2, "T");
			rs = pstmt.executeQuery();
			existsOnDb = rs.next() ? true : false;
		} catch (SQLException sqlEx) { //$JL-EXC$
			String msg =
				"Exception caught executing '"
					+ stmtStr
					+ "'. The exception was: "
					+ sqlEx.getErrorCode()
					+ ", "
					+ sqlEx.getSQLState()
					+ ": "
					+ sqlEx.getMessage();
			cat.errorT(loc, msg);
			loc.exiting();
			throw JddException.createInstance(sqlEx);
		} finally {
			try {
				rs.close();
				pstmt.close();
			} catch (Exception e) { //$JL-EXC$
				cat.errorT(
					loc,
					"Exception caught closing statement resources:\n {0}",
					new Object[] { e.getMessage()});
				loc.exiting();
				throw JddException.createInstance(e);
			}
		}
		loc.debugT(
			cat,
			"existsOnDb() returns {0}.",
			new Object[] { new Boolean(existsOnDb)});
		loc.exiting();
		return existsOnDb;
	}

	/** 
	 *  Analyses if table has content 
	 *  @return true - if table contains at least one record, false otherwise
	 *  @exception JddException - error during analysis detected
	 * */
	public boolean existsData() throws JddException {
		loc.entering(cat, "existsData()");
		boolean existsData = true;
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String tableName = null;
		String stmtStr = null;

		try {
			tableName = this.getName().trim().toUpperCase();

			// use cheap call to table statistics
			stmtStr = "SELECT COUNT(*) FROM " + "\"" + tableName + "\"";

			con = this.getDbFactory().getConnection();

			// Retrieve Native SQL PreparedStatement from Open SQL connection
			pstmt = NativeSQLAccess.prepareNativeStatement(con, stmtStr);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				existsData = rs.getLong(1) > 0 ? true : false;
			} else {
				cat.errorT(
					loc,
					"No data found executing '{0}'.",
					new Object[] { stmtStr });
			}
		} catch (SQLException sqlEx) {
			if (sqlEx.getErrorCode() == -204) {
				// Ok, table not there
				existsData = false;
				cat.warningT(
					loc,
					"existsData(): Table {0} does not exist.",
					new Object[] { tableName });
			} else {
				String msg =
					"Exception caught executing '"
						+ stmtStr
						+ "'. The exception was: "
						+ sqlEx.getErrorCode()
						+ ", "
						+ sqlEx.getSQLState()
						+ ": "
						+ sqlEx.getMessage();
				cat.errorT(loc, msg);
				loc.exiting();
				throw JddException.createInstance(sqlEx);
			}
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) { //$JL-EXC$
				cat.warningT(
					loc,
					"Exception caught closing statement resources: {0}",
					new Object[] { e.getMessage()});
				loc.exiting();
				throw JddException.createInstance(e);
			}
			loc.exiting();
		}
		loc.debugT(
			cat,
			"existsData() returns {0}.",
			new Object[] { new Boolean(existsData)});
		return existsData;
	}

	/**
	 *  Check the table-width 
	 *  @return true - if table-width is o.
	**/
	public boolean checkWidth() {
		loc.entering(cat, "checkWidth()");
		boolean widthOk = true;
		int fixedLength = 0;
		int numberOfColumns = 0;
		int sqlType = 0;
		int multiplier = 1;
		long realLength = 0;
		DbDb4Column col = null;
		DbColumnIterator iter = this.getColumns().iterator();

		while (iter.hasNext()) {
			col = (DbDb4Column) iter.next();
			numberOfColumns++;
			multiplier = 1;
			switch (col.getJavaSqlType()) {
				case Types.VARCHAR :
				case Types.LONGVARCHAR : //$JL-SWITCH$
					multiplier = 2; // UCS-2
				case Types.VARBINARY :
				case Types.LONGVARBINARY :
					fixedLength += multiplier
						* col.getLengthOrDdlDefaultLength()
						+ 2;
					break;
				case Types.BINARY :
					fixedLength += multiplier
						* col.getLengthOrDdlDefaultLength();
					break;
				case Types.CLOB : //$JL-SWITCH$
					multiplier = 2; // UCS-2
				case Types.BLOB :
					/*
					 * Add an overhead of <= 32 bytes to the fixed length portion
					 * (Data length plus 2 bytes for length field adds to varying 
					 *  length portion. - Not counted.) 
					 */
					fixedLength += 32;
					break;
				case Types.SMALLINT :
					fixedLength += 2;
					break;
				case Types.INTEGER :
					fixedLength += 4;
					break;
				case Types.BIGINT :
					fixedLength += 8;
					break;
				case Types.REAL :
					fixedLength += 4;
					break;
				case Types.DOUBLE :
					fixedLength += 8;
					break;
				case Types.DATE :
					fixedLength += 10;
					break;
				case Types.TIME :
					fixedLength += 8;
					break;
				case Types.TIMESTAMP :
					fixedLength += 26;
					break;
				case Types.DECIMAL :
					fixedLength += (col.getLengthOrDdlDefaultLength() / 2) + 1;
					break;
				default :
					cat.errorT(
						loc,
						"Invalid type {0}.",
						new Object[] { new Integer(col.getJavaSqlType())});
					widthOk = false;
			}
		}

		// Add 8 byte bytemaps for null value handling
		fixedLength += (numberOfColumns + 7) / 8 * 8;

		/*
		 * Always add 64 bytes overhead per row to be on the safe side. 
		 * The overhead includes
		 *   -  1 byte dentation
		 *   -  In case there are varying length types (VARCHAR, VARGRAPHIC, BLOB, DBCLOB)
		 *        -  up to 15 bytes to align varying length data to a 16 byte boundary
		 *        -  pointer to varying length data
		 *        -  ... ?
		 */
		fixedLength += 64;

		if (fixedLength > DbDb4Environment.getMaxTableWidthBytes()) {
			widthOk = false;
		}
		DbDb4Environment.traceCheckResult(
			true,
			widthOk,
			cat,
			loc,
			"Table {0}:" + " fixed length: {1} ({2}).",
			new Object[] {
				this.getName(),
				new Integer(fixedLength),
				new Integer(DbDb4Environment.getMaxTableWidthBytes()),
				new Boolean(widthOk)});
		loc.exiting();
		return widthOk;
	}

	/**
	 *  Check the table's name according to its length  
	 *  @return true - if name-length is o.k
	**/
	public boolean checkNameLength() {
		boolean isOk =
			(this.getName().trim().length()
				<= DbDb4Environment.getMaxTableNameLength())
				? true
				: false;
		DbDb4Environment.traceCheckResult(
			true,
			isOk,
			cat,
			loc,
			"checkNameLength() returns {0}.",
			new Object[] { new Boolean(isOk)});
		return isOk;
	}

	/**
	 *  Checks if tablename is a reserved word
	 *  @return true - if table-name has no conflict with reserved words, 
	 *                    false otherwise
	**/
	public boolean checkNameForReservedWord() {

		// ------------------------------------------------------------------ //
		// Method is not supported anymore: keyword check does no longer      //
		// include DB specific checks                                         //
		// ------------------------------------------------------------------ //
		// boolean isReserved = !(DbDb4Environment.isReservedWord(this.getName()));

		cat.warningT(
			loc,
			"Method checkNameForReservedWord() should not be used anymore!");
		return true;
	}

	//-------------------
	//  private methods  ----------------------------------------------------------------
	//-------------------

	/**
	 * Gets the list of indexes belonging to this table from the database.
	 * @return ArrayList    - List of indexes that belong to the table
	 */
	private ArrayList getIndexNamesViaDb(DbFactory factory)
		throws JddException {
		loc.entering(cat, "getIndexesViaDb(Connection)");
		ArrayList indexNames = new ArrayList();
		String tableName = null;
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String stmtStr =
			"SELECT INDEX_NAME FROM SYSINDEXES" + " WHERE TABLE_NAME = ?";

		if ((tableName = this.getName()) == null) {
			cat.errorT(loc, "Empty table name.");
			loc.exiting();
			throw new JddException(ExType.OTHER, "Empty table name.");
		}
		tableName = tableName.trim().toUpperCase();

		if ((factory == null) || (con = factory.getConnection()) == null) {
			cat.errorT(loc, "No connection.");
			loc.exiting();
			throw new JddException(ExType.OTHER, "No connection.");
		}

		try {
			// Retrieve Native SQL PreparedStatement from Open SQL connection
			pstmt = NativeSQLAccess.prepareNativeStatement(con, stmtStr);
			pstmt.setString(1, tableName);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				indexNames.add(rs.getString(1));
			}
		} catch (SQLException sqlEx) { //$JL-EXC$
			String msg =
				"Exception caught executing '"
					+ stmtStr
					+ "'. The exception was: "
					+ sqlEx.getErrorCode()
					+ ", "
					+ sqlEx.getSQLState()
					+ ": "
					+ sqlEx.getMessage();
			cat.errorT(loc, msg);
			loc.exiting();
			throw JddException.createInstance(sqlEx);
		} finally {
			try {
				rs.close();
				pstmt.close();
			} catch (Exception e) { //$JL-EXC$
				cat.errorT(
					loc,
					"Exception caught closing statement resources:\n {0}",
					new Object[] { e.getMessage()});
				loc.exiting();
				throw JddException.createInstance(e);
			}
			loc.exiting();
		}
		return indexNames;
	}

	/**
	 * Uses setCommonContentViaDb() and setSpecificContentViaDb() to set the
	 * properties of a single index.
	 * @param  indexName    - Name of index to set
	 * @return DbDb4Index   - index object
	 */
	private DbDb4Index setIndexViaDb(String indexName) throws JddException {
		loc.entering(cat, "setIndexViaDb({0})", new Object[] { indexName });
		DbDb4Index index =
			new DbDb4Index(
				this.getDbFactory(),
				this.getSchema(),
				this.getName(),
				indexName);
		index.setCommonContentViaDb();
		index.setSpecificContentViaDb();
		loc.exiting();
		return index;
	}

	/**
	 *  Delivers the names of views using this table as basetable
	 *  @return The names of dependent views as ArrayList
	 *  @exception JddException error during selection detected  
	 **/
	public ArrayList getDependentViews() throws JddException {
		loc.entering(cat, "getDependentViews()");
		String tableName = null;
		String msg = null;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList viewList = new ArrayList();

		String stmt =
			"SELECT VIEW_NAME, VIEW_SCHEMA, TABLE_SCHEMA "
				+ "FROM QSYS2.SYSVIEWDEP "
				+ "WHERE TABLE_NAME = ? AND "
				+ "TABLE_SCHEMA = CURRENT_SCHEMA";

		// ------------------------------------------------- //
		// Consistency Checks                                //
		// ------------------------------------------------- //
		if (this.getName() == null) {
			msg = "No table name provided.";
			cat.errorT(loc, msg);
			loc.exiting();
			throw new JddException(ExType.OTHER, msg);
		}
		tableName = this.getName().trim().toUpperCase();

		if (this.getDbFactory() == null
			|| (con = this.getDbFactory().getConnection()) == null) {
			msg = "No connection provided.";
			cat.errorT(loc, msg);
			loc.exiting();
			throw new JddException(ExType.OTHER, msg);
		}

		// ------------------------------------------------- //
		// Retrieve columns                                  //
		// ------------------------------------------------- //
		try {
			ps = NativeSQLAccess.prepareNativeStatement(con, stmt);
			ps.setString(1, tableName);
			rs = ps.executeQuery();

			while (rs.next()) {
				String viewName = rs.getString(1).trim().toUpperCase();
				String viewSchema = rs.getString(2).trim().toUpperCase();
				String curSchema = rs.getString(3).trim().toUpperCase();

				// -------------------------------------------------------//
				// Table referenced by view in different schema           //      
				// -------------------------------------------------------//
				if (!viewSchema.equalsIgnoreCase(curSchema)) {
					msg =
						"Table "
							+ tableName
							+ " has got dependent view "
							+ viewName
							+ " in foreign schema "
							+ viewSchema
							+ ". "
							+ "(Current schema is "
							+ curSchema
							+ ".)";
					cat.errorT(loc, msg);
					loc.exiting();
					throw new JddException(ExType.OTHER, msg);
				}
				viewList.add(viewName);
			}
		} catch (SQLException sqlEx) { //$JL-EXC$
			msg =
				"SQLException caught executing '"
					+ stmt
					+ "'. The exception was: "
					+ sqlEx.getErrorCode()
					+ ", "
					+ sqlEx.getSQLState()
					+ ": "
					+ sqlEx.getMessage();
			cat.errorT(loc, msg);
			loc.exiting();
			throw JddException.createInstance(sqlEx);
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (Exception e) { //$JL-EXC$
				cat.errorT(
					loc,
					"Exception caught closing statement resources:\n {0}",
					new Object[] { e.getMessage()});
				loc.exiting();
				throw JddException.createInstance(e);
			}
		}
		loc.exiting();

		return viewList;
	}

}
