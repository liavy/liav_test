package com.sap.dictionary.database.db2;

import com.sap.sql.NativeSQLAccess;
import com.sap.dictionary.database.dbs.*;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Collections;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

/**
 * @author d022204
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DbDb2Environment extends DbEnvironment {
	public static final String fetch_first_row = " FETCH FIRST ROW ONLY";
	public static final String optimize_for_one_row = " OPTIMIZE FOR 1 ROW";
	public static final String fetch_only_with_ur = " FOR FETCH ONLY WITH UR";
	public static final String ccsid = " UNICODE ";
	public static DbSqlStatement commitLine = null;

	private static ArrayList reservedWords = null;
	private static ArrayList tblspNames = new ArrayList();
	private static Hashtable schemas = new Hashtable();
	private static Hashtable versions = new Hashtable();
	private static Location loc = Logger.getLocation("db2.DbDb2Environment");
	private static Category cat =
		Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);
	private DbDb2Parameters db2Parameter = new DbDb2Parameters();
	private static final String databaseVersions[] = { "DSN08015", "DSN09015" };
	private static final int UNSET = -1;

	private static final int DB2V8 = 0;
	private static final int DB2V9 = 1;

	private int dbVersion = UNSET;

    private Connection con;
    
	/**
	 * Constructor for DbDb2Environment.
	 */
	public DbDb2Environment() {
		super();
		setCommitLine();
	}

	public DbDb2Environment(Connection con) {
		super();
		setCommitLine();
	}
	
	public DbDb2Parameters getDb2Paramter() {
		return db2Parameter;
	}
	
	/**
	 *  retrieve current schema from db 
	 *  @param con	            connection  
	 * */
	public static String getSchema(Connection con) {
		loc.entering("getSchema");
		try {
			if (con == null) {
				loc.exiting();
				return null;
			}
			if (schemas.containsKey(con)) {
				loc.exiting();
				return ((String) schemas.get(con));
			}
			String schema = null;
			if (schema == null) {
				PreparedStatement pstmt1 =
					NativeSQLAccess.prepareNativeStatement(
						con,
						"SELECT CURRENT SQLID FROM SYSIBM.SYSDUMMY1");
				ResultSet rs = pstmt1.executeQuery();
				if (rs.next())
					schema = rs.getString(1).trim();
				rs.close();
				pstmt1.close();
			}
			schemas.put(con, schema);
			loc.exiting();
			return schema;
		} catch (SQLException ex) {
			Object[] arguments = { getSQLError(ex)};
			cat.errorT(loc, "getSchema failed: {0}", arguments);
			loc.exiting();
			return null;
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "getSchema failed: {0}", arguments);
			loc.exiting();
			return null;
		}
	}
	
	private static int getDbVersion(Connection con) {
		loc.entering("getDBVersion");
		int dbVers = UNSET;
		try {
			// set DB2 version 				
			if (con != null) {	
				if (versions.containsKey(con)) {
					loc.exiting();
					return (((Integer)versions.get(con)).intValue());
				}
				
				DatabaseMetaData md =
					NativeSQLAccess.getNativeMetaData(con);
				String db_vers = md.getDatabaseProductVersion();
				if (0 <= db_vers.compareTo(databaseVersions[DB2V9])) {
					// V9 new function mode						
					dbVers = DB2V9;
					cat.infoT(loc, "getDbVersion: running against DB2 z/OS V9");
				} else if (0 <= db_vers.compareTo(databaseVersions[DB2V8])) {
					// V8 new function mode
					dbVers = DB2V8;
					cat.infoT(loc, "getDbVersion: running against DB2 z/OS V8");
				} else {
					Object[] arguments = { db_vers };
					cat.errorT(
						loc,
						"setValues: database version not supported: version string: {0}",
						arguments);
					cat.infoT(
						loc,
						"setValues: using parameters of DB2 z/OS V8");
					dbVers = DB2V8;
				}
				versions.put(con, new Integer(dbVers));
			} else {
					// if no connection is provided assume limits from V8
					//cat.infoT(
					//	loc,
					//	"getDbVersion: no connection provided: assuming to run against DB2 z/OS V8");
					dbVers = DB2V8;
			}
			loc.exiting();
						
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "getDbVersion: {0}", arguments);
			loc.exiting();
		}
		return dbVers;	
	}
	public static String getSQLError(SQLException exc, String text) {
		return (text + getSQLError(exc));
	}

	private static String getSQLError(SQLException exc) {
		return (
			" ==> SQL-error="
				+ exc.getErrorCode()
				+ ", SQL-state="
				+ exc.getSQLState()
				+ ", SQL-message="
				+ exc.getMessage());
	}

	/**
	 *  check for reserved words db2/390
	 *  @param id	      string to check             
	 * */
	public static boolean isReservedWord(String id) {
		if (reservedWords == null) {
			// build list with all known reserved words
			reservedWords = new ArrayList();

			reservedWords.add("ADD");
			reservedWords.add("AFTER");
			reservedWords.add("ALL");
			reservedWords.add("ALLOCATE");
			reservedWords.add("ALLOW");
			reservedWords.add("ALTER");
			reservedWords.add("AND");
			reservedWords.add("ANY");
			reservedWords.add("AS");
			reservedWords.add("ASSOCIATE");
			reservedWords.add("ASUTIME");
			reservedWords.add("AUDIT");
			reservedWords.add("AUX");
			reservedWords.add("AUXILIARY");
			reservedWords.add("BEFORE");
			reservedWords.add("BEGIN");
			reservedWords.add("BETWEEN");
			reservedWords.add("BUFFERPOOL");
			reservedWords.add("BY");
			reservedWords.add("CALL");
			reservedWords.add("CAPTURE");
			reservedWords.add("CASCADED");
			reservedWords.add("CASE");
			reservedWords.add("CAST");
			reservedWords.add("CCSID");
			reservedWords.add("CHAR");
			reservedWords.add("CHARACTER");
			reservedWords.add("CHECK");
			reservedWords.add("CLOSE");
			reservedWords.add("CLUSTER");
			reservedWords.add("COLLECTION");
			reservedWords.add("COLLID");
			reservedWords.add("COLUMN");
			reservedWords.add("COMMENT");
			reservedWords.add("COMMIT");
			reservedWords.add("CONCAT");
			reservedWords.add("CONDITION");
			reservedWords.add("CONNECT");
			reservedWords.add("CONNECTION");
			reservedWords.add("CONSTRAINT");
			reservedWords.add("CONTAINS");
			reservedWords.add("CONTINUE");
			reservedWords.add("CREATE");
			reservedWords.add("CURRENT");
			reservedWords.add("CURRENT_DATE");
			reservedWords.add("CURRENT_LC_CTYPE");
			reservedWords.add("CURRENT_PATH");
			reservedWords.add("CURRENT_TIME");
			reservedWords.add("CURRENT_TIMESTAMP");
			reservedWords.add("CURSOR");
			reservedWords.add("DATA");
			reservedWords.add("DATABASE");
			reservedWords.add("DAY");
			reservedWords.add("DAYS");
			reservedWords.add("DBINFO");
			reservedWords.add("DB2SQL");
			reservedWords.add("DECLARE");
			reservedWords.add("DEFAULT");
			reservedWords.add("DELETE");
			reservedWords.add("DESCRIPTOR");
			reservedWords.add("DETERMINISTIC");
			reservedWords.add("DISALLOW");
			reservedWords.add("DISTINCT");
			reservedWords.add("DO");
			reservedWords.add("DOUBLE");
			reservedWords.add("DROP");
			reservedWords.add("DSNHATTR");
			reservedWords.add("DSSIZE");
			reservedWords.add("DYNAMIC");
			reservedWords.add("EDITPROC");
			reservedWords.add("ELSE");
			reservedWords.add("ELSEIF");
			reservedWords.add("ENCODING");
			reservedWords.add("END");
			reservedWords.add("END-EXEC(1)");
			reservedWords.add("ERASE");
			reservedWords.add("ESCAPE");
			reservedWords.add("EXCEPT");
			reservedWords.add("EXECUTE");
			reservedWords.add("EXISTS");
			reservedWords.add("EXIT");
			reservedWords.add("EXTERNAL");
			reservedWords.add("FENCED");
			reservedWords.add("FETCH");
			reservedWords.add("FIELDPROC");
			reservedWords.add("FINAL");
			reservedWords.add("FOR");
			reservedWords.add("FROM");
			reservedWords.add("FULL");
			reservedWords.add("FUNCTION");
			reservedWords.add("GENERAL");
			reservedWords.add("GENERATED");
			reservedWords.add("GET");
			reservedWords.add("GLOBAL");
			reservedWords.add("GO");
			reservedWords.add("GOTO");
			reservedWords.add("GRANT");
			reservedWords.add("GROUP");
			reservedWords.add("HANDLER");
			reservedWords.add("HAVING");
			reservedWords.add("HOUR");
			reservedWords.add("HOURS");
			reservedWords.add("IF");
			reservedWords.add("IMMEDIATE");
			reservedWords.add("IN");
			reservedWords.add("INDEX");
			reservedWords.add("INHERIT");
			reservedWords.add("INNER");
			reservedWords.add("INOUT");
			reservedWords.add("INSENSITIVE");
			reservedWords.add("INSERT");
			reservedWords.add("INTO");
			reservedWords.add("IS");
			reservedWords.add("ISOBID");
			reservedWords.add("JAR");
			reservedWords.add("JAVA");
			reservedWords.add("JOIN");
			reservedWords.add("KEY");
			reservedWords.add("LABEL");
			reservedWords.add("LANGUAGE");
			reservedWords.add("LC_CTYPE");
			reservedWords.add("LEAVE");
			reservedWords.add("LEFT");
			reservedWords.add("LIKE");
			reservedWords.add("LOCAL");
			reservedWords.add("LOCALE");
			reservedWords.add("LOCATOR");
			reservedWords.add("LOCATORS");
			reservedWords.add("LOCK");
			reservedWords.add("LOCKMAX");
			reservedWords.add("LOCKSIZE");
			reservedWords.add("LONG");
			reservedWords.add("LOOP");
			reservedWords.add("MICROSECOND");
			reservedWords.add("MICROSECONDS");
			reservedWords.add("MINUTE");
			reservedWords.add("MINUTES");
			reservedWords.add("MODIFIES");
			reservedWords.add("MONTH");
			reservedWords.add("MONTHS");
			reservedWords.add("NO");
			reservedWords.add("NOT");
			reservedWords.add("NULL");
			reservedWords.add("NULLS");
			reservedWords.add("NUMPARTS");
			reservedWords.add("OBID");
			reservedWords.add("OF");
			reservedWords.add("ON");
			reservedWords.add("OPEN");
			reservedWords.add("OPTIMIZATION");
			reservedWords.add("OPTIMIZE");
			reservedWords.add("OR");
			reservedWords.add("ORDER");
			reservedWords.add("OUT");
			reservedWords.add("OUTER");
			reservedWords.add("PACKAGE");
			reservedWords.add("PARAMETER");
			reservedWords.add("PART");
			reservedWords.add("PATH");
			reservedWords.add("PIECESIZE");
			reservedWords.add("PLAN");
			reservedWords.add("PRECISION");
			reservedWords.add("PREPARE");
			reservedWords.add("PRIQTY");
			reservedWords.add("PRIVILEGES");
			reservedWords.add("PROCEDURE");
			reservedWords.add("PROGRAM");
			reservedWords.add("PSID");
			reservedWords.add("QUERYNO");
			reservedWords.add("READS");
			reservedWords.add("REFERENCES");
			reservedWords.add("RELEASE");
			reservedWords.add("RENAME");
			reservedWords.add("REPEAT");
			reservedWords.add("RESTRICT");
			reservedWords.add("RESULT");
			reservedWords.add("RESULT_SET_LOCATOR");
			reservedWords.add("RETURN");
			reservedWords.add("RETURNS");
			reservedWords.add("REVOKE");
			reservedWords.add("RIGHT");
			reservedWords.add("ROLLBACK");
			reservedWords.add("RUN");
			reservedWords.add("SAVEPOINT");
			reservedWords.add("SCHEMA");
			reservedWords.add("SCRATCHPAD");
			reservedWords.add("SECOND");
			reservedWords.add("SECONDS");
			reservedWords.add("SECQTY");
			reservedWords.add("SECURITY");
			reservedWords.add("SELECT");
			reservedWords.add("SENSITIVE");
			reservedWords.add("SET");
			reservedWords.add("SIMPLE");
			reservedWords.add("SOME");
			reservedWords.add("SOURCE");
			reservedWords.add("SPECIFIC");
			reservedWords.add("STANDARD");
			reservedWords.add("STATIC");
			reservedWords.add("STAY");
			reservedWords.add("STOGROUP");
			reservedWords.add("STORES");
			reservedWords.add("STYLE");
			reservedWords.add("SUBPAGES");
			reservedWords.add("SYNONYM");
			reservedWords.add("SYSFUN");
			reservedWords.add("SYSIBM");
			reservedWords.add("SYSPROC");
			reservedWords.add("SYSTEM");
			reservedWords.add("TABLE");
			reservedWords.add("TABLESPACE");
			reservedWords.add("THEN");
			reservedWords.add("TO");
			reservedWords.add("TRIGGER");
			reservedWords.add("UNDO");
			reservedWords.add("UNION");
			reservedWords.add("UNIQUE");
			reservedWords.add("UNTIL");
			reservedWords.add("UPDATE");
			reservedWords.add("USER");
			reservedWords.add("USING");
			reservedWords.add("VALIDPROC");
			reservedWords.add("VALUES");
			reservedWords.add("VARIANT");
			reservedWords.add("VCAT");
			reservedWords.add("VIEW");
			reservedWords.add("VOLUMES");
			reservedWords.add("WHEN");
			reservedWords.add("WHERE");
			reservedWords.add("WHILE");
			reservedWords.add("WITH");
			reservedWords.add("WLM");
			reservedWords.add("YEAR");
			reservedWords.add("YEARS");

			// sort the list
			Collections.sort(reservedWords);
		}

		return (Collections.binarySearch(reservedWords, id) >= 0);
	}

	public static String quote(String s) {
		return ("\"" + s + "\"");
	}

	/**
	 *  get length of row  
	 *  @param cols            columns 
	 * */
	public static int getRowLength(DbColumns cols) {
		int rowLength = 0;
		DbColumnIterator iterator = cols.iterator();

		while (iterator.hasNext()) {
			DbDb2Column column = (DbDb2Column) iterator.next();
			rowLength += DbDb2Environment.getByteLengthTable(column);
		}
		if (hasLobs(cols))
			rowLength += 19; // for rowid field 
		return rowLength;
	}

	/**
	 *  check for lob columns 
	 *  @param tab            table 
	 *  @return true if table has lob columns 
	 * */
	public static boolean hasLobs(DbTable tab) {
		if (tab == null)
			return false;
		DbColumns cols = tab.getColumns();
		if (cols == null)
			return false;
		return hasLobs(cols);
	}

	/**
	 *  check for lob columns 
	 *  @param cols            columns 
	 *  @return true if row contains lob columns 
	 * */
	public static boolean hasLobs(DbColumns cols) {
		if (cols == null)
			return false;
		DbColumnIterator iterator = cols.iterator();
		while (iterator.hasNext()) {
			DbColumn column = iterator.next();
			if (DbDb2Environment.isLob(column))
				return true;
		}
		return false;
	}

	/**
	 *  check for lob columns 
	 *  @param cols            columns 
	 *  @return true if row contains lob columns 
	 * */
	public static DbSqlStatement getFieldNames(DbColumns cols) {
		String line = "";
		DbColumnIterator iterator = cols.iterator();
		DbSqlStatement fieldNames = new DbSqlStatement();

		while (iterator.hasNext()) {
			line = iterator.next().getName();
			if (iterator.hasNext())
				line = line + ", ";
			fieldNames.addLine(line);
		}
		return fieldNames;
	}

	/**
	 *  number of lob columns in row    
	 * */
	public static int lobCount(DbColumns cols) {
		DbColumnIterator iterator = cols.iterator();
		int count = 0;

		while (iterator.hasNext()) {
			DbColumn column = iterator.next();
			if (DbDb2Environment.isLob(column))
				count++;
			;
		}
		return count;
	}

	/**
	 *  check wether column is lob   
	 * */
	public static boolean isLob(DbColumn col) {
		int j = col.getJavaSqlType();
		// varchar with length > 13000 hava to be mapped to 
		// dbclob: no pagezize > 32K 
		if (j == java.sql.Types.CLOB
			|| j == java.sql.Types.BLOB
			|| (j == java.sql.Types.VARCHAR
				&& (col.getLength() > DbDb2Parameters.maxLongChar)))
			return true;
		else
			return false;
	}

	/**
	 *  DDL Clause for column  
	 * */
	public static DbSqlStatement getDdlClause(DbTable table, DbColumns cols)
		throws Exception {
		String line = "";
		DbColumnIterator iterator = cols.iterator();
		DbSqlStatement colDef = new DbSqlStatement();
		boolean hasLobs = DbDb2Environment.hasLobs(cols);
		// DbTable table = getTable(cols);
		DbFactory factory = table.getDbFactory();

		colDef.addLine("(");
		while (iterator.hasNext()) {
			DbColumn tempColumn = (DbColumn) iterator.next();
			DbColumn tempColumn2 = null;
			Integer l = null;
			int len = (int) tempColumn.getLength();
			int type = tempColumn.getJavaSqlType();
			String defaultValue = tempColumn.getDefaultValue();

			boolean noChange = true;

			if ((tempColumn.getJavaSqlType() == java.sql.Types.VARCHAR)
				&& (tempColumn.getLength() > DbDb2Parameters.maxLongChar)) {
				type = java.sql.Types.CLOB;
				defaultValue = null;
				noChange = false;
			}
			if (noChange)
				tempColumn2 = tempColumn;
			else
				tempColumn2 =
					new DbDb2Column(
						factory,
						tempColumn.getName(),
						tempColumn.getPosition(),
						type,
						tempColumn.getDbType(),
						len,
						tempColumn.getDecimals(),
						tempColumn.isNotNull(),
						defaultValue);

			line = tempColumn2.getDdlClause();

			if (iterator.hasNext())
				line = line + ", ";
			colDef.addLine(line);
		}

		colDef.addLine(")");
		return colDef;
	}

	/**
	 * length of column (in byte) for index
	 * */
	public static int getByteLengthIndex(DbColumn col) {
		return getByteLength(col, false);
	}

	/**
	 * length of column (in byte) for table
	 * */
	public static int getByteLengthTable(DbColumn col) {
		return getByteLength(col, true);
	}

	/**
		 * length of column (in byte) 
		 * */
	private static int getByteLength(DbColumn col, boolean inTable) {
		switch (col.getJavaSqlTypeInfo().getIntCode()) {
			case (java.sql.Types.CHAR) :
			case (java.sql.Types.VARCHAR) :
			case (java.sql.Types.LONGVARCHAR) :
				int length = (int) col.getLength();
				if (length > DbDb2Parameters.maxLongChar)
					// is mapped to lob
					return (6);
				else if (inTable)
					return ((int) (2 * col.getLength() + 2));
				else
					return ((int) (2 * col.getLength()));
			case (java.sql.Types.BINARY) :
			case (java.sql.Types.VARBINARY) :
			case (java.sql.Types.LONGVARBINARY) :
				if (inTable)
					return ((int) (col.getLength() + 2));
				else
					return ((int) col.getLength());
			case (java.sql.Types.SMALLINT) :
				return (2);
			case (java.sql.Types.REAL) :
				return (4);
			case (java.sql.Types.FLOAT) :
			case (java.sql.Types.DOUBLE) :
				return (8);
			case (java.sql.Types.DECIMAL) :
			case (java.sql.Types.NUMERIC) :
				return ((int) (col.getDecimals() / 2 + 1));
			case (java.sql.Types.INTEGER) :
				return (4);
			case (java.sql.Types.BIGINT) :
				return (10);
			case (java.sql.Types.TIME) :
				return (3);
			case (java.sql.Types.DATE) :
				return (4);
			case (java.sql.Types.TIMESTAMP) :
				return (10);
			case (java.sql.Types.BLOB) :
			case (java.sql.Types.CLOB) :
				return (6);
			default :
				return (0);
		}
	}

	/**
	 * create name for lob table 
	 * mask is: #<tabname>___   
	 * substitute last three characters by random characters 
	 * */
	public String getLobTabName(Connection con, String tabname)
		throws JddException {
		loc.entering("getLobTabName");
		try {
			String auxtbname = null;
			boolean done = false;
			int retry = 0;

			do {
				if (retry >= 30) {
					Object[] arguments = { tabname };
					cat.errorT(
						loc,
						"getLobTabName: No table name for lob table {0} found after 30 retries.",
						arguments);
					loc.exiting();
					throw new JddException(
						ExType.NOT_ON_DB,
						"no lob table name found");
				}
				retry++;

				int len = tabname.length();
				if (len < DbDb2Parameters.maxTabNameLen && (retry <= 1)) {
					auxtbname = "#" + tabname;
				} else {

					auxtbname = "#";
					len =
						Math.min(
							tabname.length(),
							DbDb2Parameters.maxTabNameLen - 4);
					auxtbname += tabname.substring(0, len);
					auxtbname += DbDb2Environment.getRandomString(3);
				}

			} while (tableExistsOnDb(con, auxtbname));

			Object[] arguments = { auxtbname, tabname };
			cat.infoT(
				loc,
				"getLobTabName: use {0} for lob table name for table {1}",
				arguments);
			loc.exiting();
			return auxtbname;
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "getLobTabName: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	/** 
	 * create names for  auxilliary lob tablespaces 
	 * mask is: L<tabelname>__   
	 * substitute last characters two by random characters 	
	 * */
	public static String getLobAuxName(
		String dbname,
		String tbname,
		ArrayList tblsps) {
		String tblsp = null;
		boolean done = false;
		while (!done) {
			char[] ltspname = new char[DbDb2Parameters.maxTspNameLen - 2];
			int j = tbname.indexOf("_");
			if ((j > 0) && (j < tbname.length() - 3))
				tbname = tbname.substring(j + 1);
			String tbnameUpper = tbname.toUpperCase();
			int len = Math.min(tbnameUpper.length(), 5);
			ltspname[0] = 'L';
			tbnameUpper.getChars(0, len, ltspname, 1);

			// erase invalid characters
			for (int l = 1; l < len + 1; l++) {
				if (-1 == DbDb2Parameters.CharsAllowed.indexOf(ltspname[l]))
					ltspname[l] = 'X';
			}

			// fill rest with 'X' if len < 5 
			for (int l = len + 1; l < 6; l++) {
				ltspname[l] = 'X';
			}
			tblsp = new String(ltspname);
			// set last two characters to  generated random chars 		
			tblsp += DbDb2Environment.getRandomString(2);
			// check wether tablespace name already in use				
			if (!(Collections.binarySearch(tblsps, tblsp) >= 0)
				&& !(Collections.binarySearch(tblspNames, dbname + "." + tblsp)
					>= 0)) {
				done = true;
				tblsps.add(tblsp);
				Collections.sort(tblsps);
				tblspNames.add(dbname + "." + tblsp);
				Collections.sort(tblspNames);
			}
		}
		return tblsp;
	}

	/** 
	* create name for  auxilliary lob table 
	* mask is: #<colname>___   
	* substitute last three characters by random characters 
	* */
	public String getAuxTabName(Connection con, String colname)
		throws JddException {
		loc.entering("getAuxTabName");
		try {
			String auxtbname = null;
			int len = Math.min(colname.length(), 14);
			boolean done = false;
			int retry = 0;

			do {
				if (retry >= 30) {
					Object[] arguments = { colname };
					cat.errorT(
						loc,
						"getAuxTabName: No table name for auxiliary lob table found for column {0} after 30 retries.",
						arguments);
					loc.exiting();
					throw new JddException(
						ExType.NOT_ON_DB,
						"no auxiliary lobtable found");
				}
				retry++;
				auxtbname = "#";
				auxtbname += colname.substring(0, len);
				auxtbname += DbDb2Environment.getRandomString(3);
			} while (tableExistsOnDb(con, auxtbname));

			Object[] arguments = { auxtbname, colname };
			cat.infoT(
				loc,
				"getAuxTabName: use {0} for auxiliary table for column {1}",
				arguments);
			loc.exiting();
			return auxtbname;
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "getAuxTabName: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	/** 
	 *  get name of database for table from db	
	 * */
	public String getDatabaseNameViaDb(Connection con, String tabName)
		throws JddException {
		loc.entering("getDatabaseNameViaDb");
		try {
			// workaround: only needed for V7
			//if (DbDb2Parameters.isV8())
			//	return tabName;
			if (con == null)
				return null;
			String schema = getSchema(con);
			String dbName = null;
			String stmtTxt = null;
			// Read catalog and set db specific parameters      
			stmtTxt =
				" SELECT DBNAME "
					+ " FROM SYSIBM.SYSTABLES "
					+ " WHERE NAME = ? "
					+ " AND CREATOR = ? "
					+ DbDb2Environment.fetch_first_row
					+ DbDb2Environment.optimize_for_one_row
					+ DbDb2Environment.fetch_only_with_ur;
			PreparedStatement stmt =
				NativeSQLAccess.prepareNativeStatement(con, stmtTxt);
			stmt.setString(1, tabName);
			stmt.setString(2, schema);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				dbName = rs.getString(1).trim();
			else {
				Object[] arguments = { tabName };
				cat.infoT(loc, "Table {0} not found in catalog", arguments);
			}
			stmt.close();
			rs.close();
			loc.exiting();
			return dbName;
		} catch (SQLException ex) {
			Object[] arguments = { getSQLError(ex)};
			cat.errorT(loc, "getDatabaseNameViaDb: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "getDatabaseNameViaDb: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	/** 
	 *  get all tablspaces in database of table
	 *  this list is needed to create a unique 
	 *  auxilliary tablespaces for a lob value  
	 * */
	public ArrayList getLobAuxTablespaces(
		Connection con,
		String tabName)
		throws JddException {
		loc.entering("getLobAuxTablespaces");
		try {
			if (con == null)
				return new ArrayList();
			String schema = this.getSchema(con);
			String stmtTxt = null;
			ArrayList tblsps = new ArrayList();
			// Read catalog for tablespaces in database of given table    
			stmtTxt =
				" SELECT B.NAME "
					+ " FROM SYSIBM.SYSTABLES A, SYSIBM.SYSTABLESPACE B "
					+ " WHERE A.NAME = ? "
					+ " AND A.CREATOR = ? "
					+ " AND B.CREATOR = ? "
					+ " AND A.DBNAME = B.DBNAME  "
					+ DbDb2Environment.fetch_only_with_ur;

			PreparedStatement stmt =
				NativeSQLAccess.prepareNativeStatement(con, stmtTxt);

			stmt.setString(1, tabName);
			stmt.setString(2, schema);
			stmt.setString(3, schema);
			ResultSet rs = stmt.executeQuery();
			while (rs.next())
				tblsps.add(rs.getString(1).trim());
			stmt.close();
			rs.close();
			// sort the list
			Collections.sort(tblsps);
			if (tblsps.isEmpty()) {
				Object[] arguments = { tabName };
				cat.errorT(
					loc,
					"getLobAuxTablespaces: Table {0} not found in catalog",
					arguments);
			}
			loc.exiting();
			return tblsps;
		} catch (SQLException ex) {
			Object[] arguments = { getSQLError(ex)};
			cat.errorT(loc, "getDatabaseNameViaDb: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "getDatabaseNameViaDb: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	/** 
	 *  get all tablspaces in given database  
	 * */
	public ArrayList getTablespacesInDatabase(
		Connection con,
		String dbName)
		throws JddException {
		loc.entering("getTablespacesInDatabase");
		try {
			if (con == null)
				return new ArrayList();
			;
			String schema = this.getSchema(con);
			String stmtTxt = null;
			ArrayList tblsps = new ArrayList();
			// Read catalog for tablespaces in database of given table    
			stmtTxt =
				" SELECT NAME "
					+ " FROM SYSIBM.SYSTABLESPACE "
					+ " WHERE DBNAME = ? "
					+ " AND CREATOR = ? "
					+ DbDb2Environment.fetch_only_with_ur;

			PreparedStatement stmt =
				NativeSQLAccess.prepareNativeStatement(con, stmtTxt);

			stmt.setString(1, dbName);
			stmt.setString(2, schema);
			ResultSet rs = stmt.executeQuery();
			while (rs.next())
				tblsps.add(rs.getString(1).trim());
			stmt.close();
			rs.close();
			// sort the list
			Collections.sort(tblsps);
			loc.exiting();
			return tblsps;
		} catch (SQLException ex) {
			Object[] arguments = { getSQLError(ex)};
			cat.errorT(loc, "getTablespacesInDatabase: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "getTablespacesInDatabase: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	public static String getRandomString(int l) {
		char[] c = new char[l];
		Random rn = new Random(System.currentTimeMillis());
		int range = 36;
		for (int i = 0; i < l; i++) {
			int ir = rn.nextInt(range);
			c[i] = DbDb2Parameters.CharsAllowed.charAt(ir);
		}
		return (new String(c, 0, l));
	}

	public static String getNameSpace(String tabName) {
		int j = tabName.indexOf("_");
		if (j > 0 && j < 4)
			return (tabName.substring(0, j));
		else {
			j = tabName.indexOf("+");
			if (j > 0 && j < 4)
				return (tabName.substring(0, j));
			return null;
		}
	}

	public static DbObjectSqlStatements getDdlStatementsForDrop(
		Connection con,
		String name,
		String schema,
		boolean isV9,
		boolean partitioned )
		throws JddException {
		loc.entering("getDdlStatementsForDrop");
		String stmtTxt = null;
		String tabName = name;
		String dbName = null;
		String tspName = null;

		try {
			int ntables = 0;
			int tspCount = 0;

			DbObjectSqlStatements tableDrop = new DbObjectSqlStatements(name);
			DbSqlStatement dropLine = new DbDb2SqlStatement(true);
			
			if (isV9 && !partitioned ) {
				dropLine.addLine(
					"DROP TABLE " + DbDb2Environment.quote(tabName));
				tableDrop.add(dropLine);
			} else {
				if (con != null) {
					stmtTxt =
						"SELECT A.NAME, A.DBNAME, A.TSNAME, DIGITS(B.NTABLES),B.CREATOR "
							+ " FROM SYSIBM.SYSTABLES A,SYSIBM.SYSTABLESPACE B "
							+ " WHERE B.DBNAME  = A.DBNAME "
							+ "   AND B.NAME    = A.TSNAME "
							+ "   AND A.CREATOR = ? "
							+ "   AND B.CREATOR = ? "
							+ "   AND ( A.NAME  = ? OR A.NAME  = ? ) "
							+ "   AND  A.TYPE  = 'T' "
							+ DbDb2Environment.fetch_first_row
							+ DbDb2Environment.optimize_for_one_row
							+ DbDb2Environment.fetch_only_with_ur;

					PreparedStatement pstmt1 =
						NativeSQLAccess.prepareNativeStatement(con, stmtTxt);
					pstmt1.setString(1, schema);
					pstmt1.setString(2, schema);
					pstmt1.setString(3, name);
					pstmt1.setString(4, "#" + name.trim());
					ResultSet rs = pstmt1.executeQuery();

					if (rs.next()) {
						tabName = rs.getString(1);
						dbName = rs.getString(2);
						tspName = rs.getString(3);
						ntables = rs.getInt(4);
					}
					rs.close();
					pstmt1.close();

					if (ntables == 1) {
						stmtTxt =
							"SELECT COUNT(*) FROM SYSIBM.SYSTABLESPACE "
								+ "WHERE TYPE <> 'O' AND DBNAME =? ";
						PreparedStatement pstmtdb =
							NativeSQLAccess.prepareNativeStatement(
								con,
								stmtTxt);
						pstmtdb.setString(1, dbName);

						ResultSet rsdb = pstmtdb.executeQuery();

						if (rsdb.next())
							tspCount = rsdb.getInt(1);

						rsdb.close();
						pstmtdb.close();

						/*if (tspCount == 1)
							dropLine.addLine("DROP DATABASE " + dbName);
						else*/
						dropLine.addLine(
								"DROP TABLESPACE " + dbName + "." + tspName);
					} else {
						dropLine.addLine(
							"DROP TABLE " + DbDb2Environment.quote(tabName));
					}
					tableDrop.add(dropLine);

					if (tspCount != 1) { // not needed if database is dropped
						stmtTxt =
							"SELECT  B.DBNAME, B.TSNAME "
								+ " FROM SYSIBM.SYSAUXRELS A, "
								+ " SYSIBM.SYSTABLES B "
								+ " WHERE A.TBNAME = ? "
								+ " AND A.TBOWNER = ? "
								+ " AND B.CREATOR = ? "
								+ " AND A.AUXTBNAME = B.NAME "
								+ " AND A.AUXTBOWNER = B.CREATOR "
								+ DbDb2Environment.fetch_only_with_ur;
						PreparedStatement pstmt2 =
							NativeSQLAccess.prepareNativeStatement(
								con,
								stmtTxt);
						pstmt2.setString(1, name);
						pstmt2.setString(2, schema);
						pstmt2.setString(3, schema);
						rs = pstmt2.executeQuery();
						while (rs.next()) {
							dbName = rs.getString(1).trim();
							tspName = rs.getString(2).trim();
							dropLine = new DbDb2SqlStatement(true);
							dropLine.addLine(
								"DROP TABLESPACE " + dbName + "." + tspName);
							tableDrop.add(dropLine);
						}
						rs.close();
						pstmt2.close();
					}
				} else {
					// drop table
					dropLine.addLine(
						"DROP TABLE "
							+ DbDb2Environment.quote(name)
							+ " +LOCATION ");
					tableDrop.add(dropLine);
				}
			}
			if (DbDb2Parameters.commit)
				tableDrop.add(commitLine);
			loc.exiting();
			return tableDrop;
		} catch (SQLException ex) {
			Object[] arguments = { DbDb2Environment.getSQLError(ex, stmtTxt)};
			cat.errorT(
				loc,
				"getDdlStatementsForDrop: generation of create statement failed: {0}",
				arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(
				loc,
				"getDdlStatementsForDrop: generation of create statement failed: {0}",
				arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	// create tablespace name
	// use table name and substitute 
	// special characters by 'X'
	public static String getTspName(String tbname) {
		char tsname[] = new char[DbDb2Parameters.maxTspNameLen];
		int j = tbname.indexOf("_");
		if ((j > 0) && (j < tbname.length() - 3))
			tbname = tbname.substring(j + 1);
		int len = Math.min(tbname.length(), DbDb2Parameters.maxTspNameLen - 1);

		for (int i = 0; i < len; i++) {
			char c = (tbname.toUpperCase()).charAt(i);
			if ((i == 0 && !Character.isLetter(c))
				|| (-1 == DbDb2Parameters.CharsAllowed.indexOf(c)))
				tsname[i] = 'X';
			else
				tsname[i] = c;
		}

		String str = new String(tsname, 0, len);
		return str;
	}

	public static String getDbName(
		Connection con,
		String tab,
		String tblsp,
		int pageSize,
		String schema,
		boolean dbExists[])
		throws JddException {
		loc.entering("getDbName");
		String rdb = null;
		String rdbc = DbDb2Environment.getNameSpace(tab);
		if (rdbc != null) {
			rdbc = rdbc.substring(0, Math.min(rdbc.length(), 3));
			if (rdbc.length() < 3) { // pad with 'X' if length < 3 
				rdbc += "XXX";
				rdbc = rdbc.substring(0, 3);
			}
		} else
			rdbc = "JD0"; // no namespace 

		// remove irregular characters if any
		char dbn[] = new char[3];
		rdbc.getChars(0, 3, dbn, 0);
		for (int i = 0; i < 3; i++) {
			if ((i == 0 && !Character.isLetter(dbn[0]))
				|| (-1 == DbDb2Parameters.CharsAllowed.indexOf(dbn[i])))
				dbn[i] = 'X';
		}
		rdbc = new String(dbn, 0, 3);
		
		rdbc += "X"; // add one spaceholders for later use 
		switch (pageSize) {
			case DbDb2Parameters.PAGESIZE_4K:
				rdbc += "0";
				break;		
			case DbDb2Parameters.PAGESIZE_8K:
				rdbc += "1";
				break;
			case DbDb2Parameters.PAGESIZE_16K:
				rdbc += "2";
		 			break;		
		 	default:
		 		rdbc += "3";
		 		break;
		 }

		if (con == null) {
			rdb = rdbc + "___";
			dbExists[0] = false;
		} else {
			rdb = checkExDb(con, rdbc, tblsp, getBufferPoolFromPageSize(pageSize), schema);
			if (rdb == null) {
				int retry = 0;
				do {
					if (retry >= 30) {
						Object[] arguments = { tab };
						cat.errorT(
							loc,
							"No database found found for table {0} after 30 retries.",
							arguments);
						loc.exiting();
						throw new JddException(
							ExType.NOT_ON_DB,
							"no database found");
					}
					retry++;
					String rs = DbDb2Environment.getRandomString(3);
					rdb = rdbc + rs;
				} while (existsDb(con, rdb));
				dbExists[0] = false;
			} else
				dbExists[0] = true;
		}

		// add complete name to list of created tablespace names 
		tblspNames.add(rdb + "." + tblsp);
		Collections.sort(tblspNames);

		loc.exiting();
		return rdb;
	}

	private static boolean existsDb(Connection con, String db)
		throws JddException {
		loc.entering("existsDb");
		boolean dbExists = false;
		if (con == null)
			return false;
		String stmtTxt = null;
		try {
			stmtTxt =
				" SELECT NAME FROM SYSIBM.SYSDATABASE "
					+ " WHERE NAME = ? "
					+ DbDb2Environment.fetch_first_row
					+ DbDb2Environment.optimize_for_one_row
					+ DbDb2Environment.fetch_only_with_ur;
			PreparedStatement pstmt =
				NativeSQLAccess.prepareNativeStatement(con, stmtTxt);
			pstmt.setString(1, db);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				dbExists = true;
			}
			rs.close();
			pstmt.close();
			loc.exiting();
			return dbExists;
		} catch (SQLException ex) {
			Object[] arguments = { DbDb2Environment.getSQLError(ex, stmtTxt)};
			cat.errorT(loc, "dbExists: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "dbExists: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	private static String checkExDb(
		Connection con,
		String sdb,
		String tblsp,
		String bpool,
		String schema)
		throws JddException {
		loc.entering("checkExDb");

		if (con == null)
			return null;
		String stmtTxt = null;
		String rdb = null;
		try {
			stmtTxt =
				" ( SELECT  0 AS C, NAME AS D "
					+ " FROM SYSIBM.SYSDATABASE A "
					+ " WHERE A.NAME LIKE ? AND STRIP(A.BPOOL) = ? AND NOT A.NAME IN "
					+ " ( SELECT DBNAME FROM SYSIBM.SYSTABLESPACE )"
					+ " AND A.CREATOR = ? )  "
					+ " UNION "
					+ " ( SELECT  C, D  FROM "
					+ " (SELECT DBNAME AS D, COUNT(*) AS C  FROM SYSIBM.SYSTABLESPACE A, "
					+ " SYSIBM.SYSDATABASE B  GROUP BY A.DBNAME, B.NAME,"
					+ " B.CREATOR  HAVING B.NAME LIKE ? AND B.NAME = A.DBNAME AND NOT B.NAME IN "
					+ " ( SELECT A.DBNAME  FROM SYSIBM.SYSTABLESPACE A  "
					+ " WHERE  A.DBNAME LIKE  ? AND A.NAME LIKE ? ) "
					+ " AND B.CREATOR = ? ) C WHERE C.C < 100  ) "
					+ "  ORDER BY       "
					+ "  C ASC "
				//+ DbDb2Environment.fetch_first_row
		//+ DbDb2Environment.optimize_for_one_row
	+DbDb2Environment.fetch_only_with_ur;
			PreparedStatement pstmt =
				NativeSQLAccess.prepareNativeStatement(con, stmtTxt);
			pstmt.setString(1, sdb + "%");
			pstmt.setString(2, bpool);
			pstmt.setString(3, schema);
			pstmt.setString(4, sdb + "%");
			pstmt.setString(5, sdb + "%");
			pstmt.setString(6, tblsp + "%");
			pstmt.setString(7, schema);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				String rdbtst = rs.getString(2).trim();
				if (!(Collections
					.binarySearch(tblspNames, rdbtst + "." + tblsp)
					>= 0)) {
					rdb = rdbtst;
					break;
				}
			}
			rs.close();
			pstmt.close();
			loc.exiting();
			return rdb;
		} catch (SQLException ex) {
			Object[] arguments = { DbDb2Environment.getSQLError(ex, stmtTxt)};
			cat.errorT(loc, "checkExDb: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "checkExDb: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	public boolean tableExistsOnDb(Connection con, String name) {
		loc.entering("tableExistsOnDb");
		String stmtTxt = null;
		try {
			if (con == null) {
				loc.exiting();
				return false;
			}
			boolean exists = false;
			String schema = this.getSchema(con);
			stmtTxt =
				"SELECT NAME  FROM SYSIBM.SYSTABLES WHERE "
					+ " NAME = ? AND CREATOR = ? AND TYPE = 'T' "
					+ DbDb2Environment.fetch_first_row
					+ DbDb2Environment.optimize_for_one_row
					+ DbDb2Environment.fetch_only_with_ur;
			PreparedStatement stmt =
				NativeSQLAccess.prepareNativeStatement(con, stmtTxt);
			stmt.setString(1, name);
			stmt.setString(2, schema);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				exists = true;
			rs.close();
			stmt.close();
			loc.exiting();
			return exists;
		} catch (SQLException ex) {
			Object[] arguments = { DbDb2Environment.getSQLError(ex, stmtTxt)};
			cat.errorT(loc, "tableExistsOnDb: {0}", arguments);
			loc.exiting();
			return false;
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "tableExistsOnDb: {0}", arguments);
			loc.exiting();
			return false;
		}
	}

	public boolean setDbPosition(DbFactory factory, DbColumns cols)
		throws JddException {
		loc.entering("setDbPosition");
		String stmtTxt = null;
		try {
			String tableName = cols.getTable().getName();
			Connection con = factory.getConnection();
			String schema = this.getSchema(con);
			int rowidCount = 0;

			stmtTxt =
				"SELECT NAME, COLNO, COLTYPE"
					+ " FROM SYSIBM.SYSCOLUMNS WHERE "
					+ " TBNAME = ? AND TBCREATOR = ? "
					+ " ORDER BY COLNO "
					+ DbDb2Environment.fetch_only_with_ur;
			PreparedStatement stmt =
				NativeSQLAccess.prepareNativeStatement(con, stmtTxt);
			stmt.setString(1, tableName);
			stmt.setString(2, schema);
			ResultSet rs = stmt.executeQuery();
			boolean found = false;
			while (rs.next()) {
				found = true;
				String colName = rs.getString(1);
				int pos = rs.getInt(2) - rowidCount;
				String dbType = rs.getString(3).trim();

				// ROWID column is db specific and 
				// not transparent to dictionary 
				if (!dbType.equalsIgnoreCase("ROWID")) {
					DbColumn col = cols.getColumn(colName);
					if (col == null) {
						found = false;
						break;
					} else {
						col.setPosition(pos);
					}
				} else
					rowidCount++;
			}
			rs.close();
			stmt.close();
			loc.exiting();
			return found;
		} catch (SQLException ex) {
			Object[] arguments = { DbDb2Environment.getSQLError(ex, stmtTxt)};
			cat.errorT(loc, "setDbPosition: {0}", arguments);
			loc.exiting();
			throw new JddException(
				ExType.NOT_ON_DB,
				DbMsgHandler.get(
					com
						.sap
						.dictionary
						.database
						.dbs
						.DbsConstants
						.TABLE_ANALYSE_ERR,
					arguments));

		} catch (Exception ex) {
			Object[] arguments = { ex.toString()};
			cat.errorT(loc, "setDbPosition: {0}", arguments);
			loc.exiting();
			throw new JddException(
				ExType.NOT_ON_DB,
				DbMsgHandler.get(
					com
						.sap
						.dictionary
						.database
						.dbs
						.DbsConstants
						.TABLE_ANALYSE_ERR,
					arguments));

		}
	}

	public boolean setColsViaDb(DbFactory factory, DbDb2Columns cols)
		throws JddException {
		loc.entering("setColsViaDb");
		String stmtTxt = null;
		int rowidCount = 0;
		
		try {
			String tableName = cols.getTable().getName();
			Connection con = factory.getConnection();
			String schema = this.getSchema(con);

			stmtTxt =
				"SELECT NAME, COLNO, COLTYPE, LENGTH,"
					+ " SCALE, NULLS, DEFAULT,  DEFAULTVALUE, FOREIGNKEY "
					+ " FROM SYSIBM.SYSCOLUMNS WHERE "
					+ " TBNAME = ? AND TBCREATOR = ? "
					+ " ORDER BY COLNO "
					+ DbDb2Environment.fetch_only_with_ur;
			PreparedStatement stmt =
				NativeSQLAccess.prepareNativeStatement(con, stmtTxt);
			stmt.setString(1, tableName);
			stmt.setString(2, schema);
			ResultSet rs = stmt.executeQuery();
			boolean found = false;
			while (rs.next()) {
				found = true;
				String colName = rs.getString(1);
				short sqlType =
					DbDb2Environment.getSqlType(
						rs.getString(3),
						rs.getString(9));
				String dbType = rs.getString(3).trim();
				int colSize = rs.getInt(4);
				String defVal =
					DbDb2Environment.getDefaultValue(
						rs.getString(7),
						rs.getString(8));
				JavaSqlTypeInfo javaSqlTypeInfo  = factory.getJavaSqlTypes().getInfo(sqlType);
				String defValProt = null;
				if (defVal != null) {
					defValProt =  javaSqlTypeInfo.getDefaultValuePrefix() + defVal + javaSqlTypeInfo.getDefaultValueSuffix();; 
				}
				
				int decDigits = rs.getInt(5);
				int pos = rs.getInt(2) - rowidCount;
				boolean isNotNull =
					rs.getString(6).trim().equalsIgnoreCase("N");
				DbColumn col =
					factory.makeDbColumn(
						colName,
						pos,
						sqlType,
						dbType,
						colSize,
						decDigits,
						isNotNull,
						defValProt);
				// ROWID column is db specific and 
				// not transparent to dictionary 
				if (!dbType.equalsIgnoreCase("ROWID"))
					cols.add(col);
				else
					rowidCount++;
			}

			rs.close();
			stmt.close();
			loc.exiting();
			return found;
		} catch (SQLException ex) {
			Object[] arguments = { DbDb2Environment.getSQLError(ex, stmtTxt)};
			cat.errorT(loc, "setColsViaDb: {0}", arguments);
			loc.exiting();
			throw new JddException(
				ExType.NOT_ON_DB,
				DbMsgHandler.get(
					com
						.sap
						.dictionary
						.database
						.dbs
						.DbsConstants
						.TABLE_ANALYSE_ERR,
					arguments));

		} catch (Exception ex) {
			Object[] arguments = { ex.toString()};
			cat.errorT(loc, "setColsViaDb: {0}", arguments);
			loc.exiting();
			throw new JddException(
				ExType.NOT_ON_DB,
				DbMsgHandler.get(
					com
						.sap
						.dictionary
						.database
						.dbs
						.DbsConstants
						.TABLE_ANALYSE_ERR,
					arguments));

		}
	}
	
	public static String getBufferPoolFromPageSize(int pageSize) {	
		String bp = null;
		switch (pageSize) {
			case DbDb2Parameters.PAGESIZE_4K:
				bp = DbDb2Parameters.DEFAULT_BP_4K;
				break;
			case DbDb2Parameters.PAGESIZE_8K:
				bp = DbDb2Parameters.DEFAULT_BP_8K;
				break;
			case DbDb2Parameters.PAGESIZE_16K:
				bp = DbDb2Parameters.DEFAULT_BP_16K;
				break;
			case DbDb2Parameters.PAGESIZE_32K:
				bp = DbDb2Parameters.DEFAULT_BP_32K;
				break;
			default:
				bp = DbDb2Parameters.DEFAULT_BP_32K;
				break;
		}
		return bp;
	}
	
	public static int getPageSizeFromBufferPool(String bp) {	
		
		if ( bp == null )
			return (DbDb2Parameters.PAGESIZE_32K);
		else if ( 0 == bp.compareToIgnoreCase(DbDb2Parameters.DEFAULT_BP_4K) )
			return (DbDb2Parameters.PAGESIZE_4K);
		else if ( 0 == bp.compareToIgnoreCase(DbDb2Parameters.DEFAULT_BP_8K) )
			return (DbDb2Parameters.PAGESIZE_8K);
		else if ( 0 == bp.compareToIgnoreCase(DbDb2Parameters.DEFAULT_BP_16K) )
			return (DbDb2Parameters.PAGESIZE_16K);
		else 
			return (DbDb2Parameters.PAGESIZE_32K);	
	}
	
	private static short getSqlType(String dbtype, String foreignkey)
		throws JddException {
		loc.entering("getSqlType");
		dbtype = dbtype.trim();
		short sqltype;

		if (dbtype.equalsIgnoreCase("VARG")
			|| dbtype.equalsIgnoreCase("VARCHAR"))
			if (foreignkey.equalsIgnoreCase("B"))
				sqltype = java.sql.Types.VARBINARY;
			else
				sqltype = java.sql.Types.VARCHAR;
		else if (
			dbtype.equalsIgnoreCase("GRAPHIC")
				|| dbtype.equalsIgnoreCase("CHAR"))
			if (foreignkey.equalsIgnoreCase("B"))
				sqltype = java.sql.Types.BINARY;
			else
				sqltype = java.sql.Types.CHAR;
		else if (dbtype.equalsIgnoreCase("BINARY"))
			return java.sql.Types.BINARY;
		else if (dbtype.equalsIgnoreCase("VARBIN"))
			return java.sql.Types.VARBINARY;
		else if (dbtype.equalsIgnoreCase("SMALLINT"))
			return java.sql.Types.SMALLINT;
		else if (dbtype.equalsIgnoreCase("INTEGER"))
			sqltype = java.sql.Types.INTEGER;
		else if (dbtype.equalsIgnoreCase("BIGINT"))
			sqltype = java.sql.Types.BIGINT;
		else if (dbtype.equalsIgnoreCase("DECIMAL"))
			sqltype = java.sql.Types.DECIMAL;
		else if (dbtype.equalsIgnoreCase("DATE"))
			sqltype = java.sql.Types.DATE;
		else if (dbtype.equalsIgnoreCase("TIME"))
			sqltype = java.sql.Types.TIME;
		else if (dbtype.equalsIgnoreCase("TIMESTMP"))
			sqltype = java.sql.Types.TIMESTAMP;
		else if (dbtype.equalsIgnoreCase("FLOAT"))
			sqltype = java.sql.Types.FLOAT;
		else if (dbtype.equalsIgnoreCase("DOUBLE"))
			sqltype = java.sql.Types.DOUBLE;
		else if (dbtype.equalsIgnoreCase("BLOB"))
			sqltype = java.sql.Types.BLOB;
		else if (dbtype.equalsIgnoreCase("DBCLOB"))
			sqltype = java.sql.Types.CLOB;
		else if (dbtype.equalsIgnoreCase("CLOB"))
			sqltype = sqltype = java.sql.Types.CLOB;
		else if (dbtype.equalsIgnoreCase("ROWID"))
			sqltype = sqltype = java.sql.Types.INTEGER;
		else {
			Object[] arguments = { dbtype };
			cat.errorT(loc, "getSqlType: unknown db type {0}", arguments);
			loc.exiting();
			throw new JddException(
				ExType.NOT_ON_DB,
				DbMsgHandler.get(
					com
						.sap
						.dictionary
						.database
						.dbs
						.DbsConstants
						.TABLE_ANALYSE_ERR,
					arguments));
		}
		loc.exiting();
		return sqltype;
	}

	private static void setCommitLine() {
		if (DbDb2Parameters.commit) {
			commitLine = new DbDb2SqlStatement();
			commitLine.addLine(DbDb2Parameters.commitStmt);
		}
	}

	private static String getDefaultValue(String deftype, String defvalue) {
		if (deftype.equalsIgnoreCase("1")
			|| deftype.equalsIgnoreCase("2")
			|| deftype.equalsIgnoreCase("3")
			|| deftype.equalsIgnoreCase("4")
			|| deftype.equalsIgnoreCase("5")
			|| deftype.equalsIgnoreCase("6")
			|| deftype.equalsIgnoreCase("7"))
			return defvalue;
		else
			return null;
	}

	/**
	 *  @return true if running against V8 	  
	 **/
	public boolean isV8(Connection con) {
		if (this.dbVersion == UNSET || (con != this.con))
			this.dbVersion = getDbVersion(con);
		if (this.dbVersion == DB2V8)
			return true;
		else
			return false;
	}
	/**
	 *  @return true if running against V8 	  
	 **/
	public static boolean isVersionV8(Connection con) {	
		if ( getDbVersion(con) == DB2V8)
			return true;
		else
			return false;
	}
	
	/**
	 *  @return true if running against V9 	  
	 **/
	public boolean isV9(Connection con) {
		if (this.dbVersion == UNSET || (con != this.con))
			this.dbVersion = getDbVersion(con);
			
		if (this.dbVersion == DB2V9)
			return true;
		else
			return false;
	}
	
	/**
	 *  @return true if running against V9 	  
	 **/
	public static boolean isVersionV9(Connection con) {
		if ( getDbVersion(con) == DB2V9)
			return true;
		else
			return false;
	}
	
	public String[] getSupportedDatabaseVersions() {
		return databaseVersions;
	}
		
	public void setDatabaseVersion(String version) {
		super.setDatabaseVersion(version);
		if ( this.con != null )
		  throw ( new JddRuntimeException("cannot set database version if connection is already defined") );	
		
		for ( int i = 0; i < databaseVersions.length; i++ ) {
			if ( 0 == version.compareTo(databaseVersions[i])) {
				this.dbVersion = i;
				return;
			}			
		}
		throw ( new JddRuntimeException("invalid database version " + version ) );		
	} 
}