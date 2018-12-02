package com.sap.dictionary.database.db2;

import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Severity;
import com.sap.sql.NativeSQLAccess;
import com.sap.dictionary.database.dbs.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Collections;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Title: Analyse Tables and Views for structure changes Description: Contains
 * Extractor-classes which gain table- and view-descriptions from database and
 * XML-sources. Analyser-classes allow to examine this objects for
 * structure-changes, code can be generated and executed on the database.
 * Copyright: Copyright (c) 2001 Company: SAP AG
 * 
 * @author Burkhard Diekmann
 * @version 1.0
 */

public class DbDb2SqlStatement extends DbSqlStatement {
	private static Location loc = Logger.getLocation("db2.DbDb2SqlStatement");
	private static Category cat =
		Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);

	private static final int DEFAULT = 0;
	private static final int CREATE_DATABASE = 1;
	private static final int CREATE_INDEX = 2;
	private static final int DROP_TABLE = 3;
	private static final int CREATE_TABLESPACE = 4;
	private static ArrayList db2DdlWords = null;
	private boolean catchIfNotFound = false;
	private String dbName = "";
	private String tblspName = "";
	private String bpool = "";
	 
	// if true: statement needs substitution of place holders
	private boolean isDb2DdlStmt = false;

	public DbDb2SqlStatement() {
		super();
	}

	public DbDb2SqlStatement(boolean catchIfNotFound) {
		super(catchIfNotFound);
		this.catchIfNotFound = catchIfNotFound;
	}

	public boolean execute(Connection con) {
		loc.entering("execute");
		
		boolean autocommit = false;
		try {
			if (con.getAutoCommit()) {
				autocommit = true;
				con.setAutoCommit(false);
			}

			String sql = this.toString().trim();
			ArrayList slst = null;
			int type = typeDb2DdlStmt(sql);
			slst = substituteDdlStmt(con, type, sql);
			if (null == slst) {
				if ( DbDb2Environment.isVersionV8(con) 
					 && (    (type == CREATE_DATABASE)
						  || (type == CREATE_TABLESPACE) ) ) {
					return executeCreateImplicitObject(con, sql, type);
				}

				boolean rc = super.execute(con);
				if (autocommit) {
					con.setAutoCommit(true);
				}
				loc.exiting();
				return rc;
			} else {
				Iterator iter = slst.iterator();

				Statement statementObject =
					NativeSQLAccess.createNativeStatement(con);
				while (iter.hasNext()) {
					String s = (String) iter.next();
					try {
						statementObject.executeUpdate(s);
					} catch (SQLException ex) {
						boolean rc;
						if (!(rc =
							analyseError(
								con,
								ex,
								s,
								(type == DROP_TABLE ? true : false)))) {
							loc.exiting();
							if (autocommit)
								con.setAutoCommit(true);
							return rc;
						}
					}
				}
				statementObject.close();
				if (autocommit) {
					con.setAutoCommit(true);
				}
				return true;
			}

		} catch (Exception ex) {
			if (autocommit)
				try {
					con.setAutoCommit(true);
				} catch (SQLException e) {
					throw ( new JddRuntimeException("unable to reset autocommit") );					
				}
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "DbDb2SqlStatement: {0}", arguments);
			loc.exiting();
			return false;
		}
	}

	private ArrayList substituteDdlStmt(Connection con, int type, String sql)
		throws SQLException {

		if (!isDb2DdlStmt)
			return null;

		switch (type) {
			case CREATE_DATABASE :
				return SubstituteDdlStmtForCreateDatabase(con, sql);
			case CREATE_INDEX :
				return SubstituteDdlStmtForCreateIndex(con, sql);
			case DROP_TABLE :
				return SubstituteDdlStmtForDropTable(con, sql);
			default :
				return null;
		}
	}

	private String getTabName(String sql) {
		StringTokenizer st = new StringTokenizer(sql);
		boolean tok1Found = false;
		boolean tok2Found = false;
		while (st.hasMoreTokens()) {
			String s = st.nextToken().trim();
			if (tok2Found) {
				if (s.startsWith("\"") && s.endsWith("\""))
					s = s.substring(1, s.length() - 1);
				if (s.startsWith("#"))
					s = s.substring(1, s.length());
				return s;
			}
			if (tok1Found && (0 == s.compareTo("TABLE")))
				tok2Found = true;
			if ((0 == s.compareTo("CREATE")) || (0 == s.compareTo("DROP")))
				tok1Found = true;
			else
				tok1Found = false;
		}
		return null;
	}

	private ArrayList SubstituteDdlStmtForCreateDatabase(
		Connection con,
		String sql)
		throws SQLException {
		String tabName = getTabName(sql);
		if (tabName == null)
			return null;
		boolean[] dbExists = new boolean[1];
		String dbName = null;
		try {
			String tspName = DbDb2Environment.getTspName(tabName);
			dbName =
				DbDb2Environment.getDbName(
					con,
					tabName,
					tspName,
					DbDb2Environment.getPageSizeFromBufferPool(this.bpool),
					getSchema(con),
					dbExists);
		} catch (JddException ex) {
			return null;
		}
		StringTokenizer st = new StringTokenizer(sql, "|");
		String dbNameTok = dbName.substring(5, 8);
		String stgName = getStogroup(con);
		ArrayList slst = new ArrayList();
		int stmt = 0;
		int rc = 0;
		while (st.hasMoreTokens()) {
			stmt++;
			String dbStmt = st.nextToken();
			switch (stmt) {
				case 1 : // CREATE DATABASE
					if (!dbExists[0]) {
						StringTokenizer ststmt = new StringTokenizer(dbStmt);
						int stmtCnt = ststmt.countTokens();
						if (stmtCnt < 3)
							return null;
						String stmtNew = "";
						int cnt = 0;
						String s = "";
						while (ststmt.hasMoreTokens()) {
							cnt++;
							s = ststmt.nextToken();

							switch (cnt) {
								case 1 :
									if (0 != s.compareToIgnoreCase("CREATE"))
										return null;
									break;
								case 2 :
									if (0 != s.compareToIgnoreCase("DATABASE"))
										return null;
									break;
								case 3 :
									s = sReplace(s, "___", dbNameTok);
									break;
								default :
									}
							stmtNew += " " + s;
						}
						slst.add(stmtNew);
					}
					break;
				case 2 : // CREATE TABLESPACE
					StringTokenizer ststmt = new StringTokenizer(dbStmt);
					int stmtCnt = ststmt.countTokens();
					if (stmtCnt < 4)
						return null;
					String stmtNew = "";
					int cnt = 0;
					while (ststmt.hasMoreTokens()) {
						cnt++;
						String s = ststmt.nextToken();
						switch (cnt) {
							case 1 :
								if (0 != s.compareToIgnoreCase("CREATE"))
									return null;
								break;
							case 2 :
								if (0 != s.compareToIgnoreCase("TABLESPACE"))
									return null;
								break;
							case 4 :
								if (0 != s.compareToIgnoreCase("IN"))
									return null;
								break;
							case 5 :
								s = sReplace(s, "___", dbNameTok);
								break;
							case 6 :
								if (0 != s.compareToIgnoreCase("USING"))
									return null;

								break;
							case 7 :
								if (0 != s.compareToIgnoreCase("STOGROUP"))
									return null;
								break;
							case 8 :
								s = sReplace(s, "________", stgName);
								break;
							default :
								}
						stmtNew += " " + s;
					}
					slst.add(stmtNew);
					break;
				case 3 : // CREATE TABLE
					ststmt = new StringTokenizer(dbStmt);
					stmtCnt = ststmt.countTokens();
					if (stmtCnt < 4)
						return null;
					stmtNew = "";
					cnt = 0;
					while (ststmt.hasMoreTokens()) {
						cnt++;
						String s = ststmt.nextToken();
						if (cnt == stmtCnt - 3) {
							if (0 != s.compareToIgnoreCase("IN"))
								return null;
						}
						if (cnt == stmtCnt - 2) {
							s = sReplace(s, "___", dbNameTok);
						}
						stmtNew += " " + s;
					}
					slst.add(stmtNew);
					break;
				default :
					if (stmt >= 5 && ((stmt - 5) % 3 == 0)) {
						// LOB TABLESPACE
						ststmt = new StringTokenizer(dbStmt);
						stmtCnt = ststmt.countTokens();
						if (stmtCnt < 4)
							return null;
						stmtNew = "";
						cnt = 0;
						while (ststmt.hasMoreTokens()) {
							cnt++;
							String s = ststmt.nextToken();
							switch (cnt) {
								case 1 :
									if (0 != s.compareToIgnoreCase("CREATE"))
										return null;
									break;
								case 2 :
									if (0 != s.compareToIgnoreCase("LOB"))
										return null;
									break;
								case 3 :
									if (0
										!= s.compareToIgnoreCase("TABLESPACE"))
										return null;
									break;
								case 5 :
									if (0 != s.compareToIgnoreCase("IN"))
										return null;
									break;
								case 6 :
									s = sReplace(s, "___", dbNameTok);
									break;
								case 7 :
									if (0 != s.compareToIgnoreCase("USING"))
										return null;

									break;
								case 8 :
									if (0 != s.compareToIgnoreCase("STOGROUP"))
										return null;
									break;
								case 9 :
									s = sReplace(s, "________", stgName);
									break;
								default :
									}
							stmtNew += " " + s;
						}
					} else if ((stmt >= 5) && ((stmt - 5) % 3 == 1)) {
						// AUX TABLE
						ststmt = new StringTokenizer(dbStmt);
						stmtCnt = ststmt.countTokens();
						if (stmtCnt < 4)
							return null;
						stmtNew = "";
						cnt = 0;
						while (ststmt.hasMoreTokens()) {
							cnt++;
							String s = ststmt.nextToken();
							switch (cnt) {
								case 1 :
									if (0 != s.compareToIgnoreCase("CREATE"))
										return null;
									break;
								case 2 :
									if (0 != s.compareToIgnoreCase("AUX"))
										return null;
									break;
								case 3 :
									if (0 != s.compareToIgnoreCase("TABLE"))
										return null;
									break;
								case 5 :
									if (0 != s.compareToIgnoreCase("IN"))
										return null;
									break;
								case 6 :
									s = sReplace(s, "___", dbNameTok);
									break;
								default :
									}
							stmtNew += " " + s;
						}
					} else if ((stmt >= 5) && ((stmt - 5) % 3 == 2)) {
						// AUX INDEX
						ststmt = new StringTokenizer(dbStmt);
						stmtCnt = ststmt.countTokens();
						if (stmtCnt < 4)
							return null;
						stmtNew = "";
						cnt = 0;
						while (ststmt.hasMoreTokens()) {
							cnt++;
							String s = ststmt.nextToken();
							switch (cnt) {
								case 1 :
									if (0 != s.compareToIgnoreCase("CREATE"))
										return null;
									break;
								case 2 :
									if (0 != s.compareToIgnoreCase("INDEX"))
										return null;
									break;
								case 4 :
									if (0 != s.compareToIgnoreCase("ON"))
										return null;
									break;
								case 6 :
									if (0 != s.compareToIgnoreCase("USING"))
										return null;
									break;
								case 7 :
									if (0 != s.compareToIgnoreCase("STOGROUP"))
										return null;
									break;
								case 8 :
									s = sReplace(s, "________", stgName);
									break;
								default :
									}
							stmtNew += " " + s;
						}
					} else
						stmtNew = dbStmt;
					slst.add(stmtNew);
					break;
			}
		}
		return slst;
	}

	private ArrayList SubstituteDdlStmtForCreateIndex(
		Connection con,
		String sql)
		throws SQLException {

		StringTokenizer ststmt = new StringTokenizer(sql);
		int stmtCnt = ststmt.countTokens();
		if (stmtCnt < 3)
			return null;
		String stmtNew = "";
		int cnt = 0;
		String s = "";
		boolean indexTokenFound = false;
		boolean usingTokenFound = false;
		boolean stogroupTokenFound = false;
		boolean replace = false;
		while (ststmt.hasMoreTokens()) {
			cnt++;
			s = ststmt.nextToken();

			switch (cnt) {
				case 1 :
					if (0 != s.compareToIgnoreCase("CREATE"))
						return null;
					break;
				default :
					if (0 == s.compareToIgnoreCase("INDEX"))
						indexTokenFound = true;
					else if (0 == s.compareToIgnoreCase("USING"))
						usingTokenFound = true;
					else if (0 == s.compareToIgnoreCase("STOGROUP"))
						stogroupTokenFound = true;
					else if (
						indexTokenFound
							&& usingTokenFound
							&& stogroupTokenFound
							&& !replace) {
						s = sReplace(s, "________", getStogroup(con));
						replace = true;
					}
					break;
			}
			stmtNew += " " + s;
		}

		if (replace) {
			ArrayList slst = new ArrayList();
			slst.add(stmtNew);
			return slst;
		} else
			return null;
	}

	private ArrayList SubstituteDdlStmtForDropTable(Connection con, String sql)
		throws SQLException {

		StringTokenizer ststmt = new StringTokenizer(sql);
		int stmtCnt = ststmt.countTokens();
		if (stmtCnt != 4)
			return null;
		String stmtNew = "";
		int cnt = 0;
		String s = "";
		String tabname = null;

		while (ststmt.hasMoreTokens()) {
			cnt++;
			s = ststmt.nextToken();

			switch (cnt) {
				case 1 :
					if (0 != s.compareToIgnoreCase("DROP"))
						return null;
					break;
				case 2 :
					if (0 != s.compareToIgnoreCase("TABLE"))
						return null;
					break;
				case 3 :
					tabname = s.trim();
					if (tabname.startsWith("\"") && tabname.endsWith("\""))
						tabname = tabname.substring(1, tabname.length() - 1);
					break;
				case 4 :
					if (0 != s.compareToIgnoreCase("+LOCATION"))
						return null;
					break;
				default :
					break;
			}
		}
		
		try {
		       DbObjectSqlStatements newDropStmts = DbDb2Environment
					.getDdlStatementsForDrop(con, tabname, getSchema(con),
							DbDb2Environment.isVersionV9(con),
							false);
			ArrayList slst = new ArrayList();
			try {
				for (int i = 0;; i++) {
					DbSqlStatement stmt = newDropStmts.getStatement(i);
					s = stmt.toString().trim();
					slst.add(s);
				}
			} catch (IndexOutOfBoundsException ex) {
				if (slst.isEmpty())
					return null;
				else
					return slst;
			}
		} catch (Exception ex) {
			return null;
		}

	}

	private String sReplace(String s, String t, String u) {
		int i = s.indexOf(t);
		if (i < 0)
			return s;
		StringBuffer buf = new StringBuffer(s);
		buf.replace(i, i + t.length(), u);
		return new String(buf);
	}

	// check if databse exists on db
	private boolean dbExists(Connection con, String dbname)
		throws SQLException {
		String stmtTxt = null;
		if (dbname.startsWith("\"") && dbname.endsWith("\""))
			dbname = dbname.substring(1, dbname.length() - 1);
		boolean dbExists = false;
		stmtTxt =
			"SELECT NAME FROM SYSIBM.SYSDATABASE "
				+ " WHERE NAME = ? "
				+ DbDb2Environment.fetch_first_row
				+ DbDb2Environment.optimize_for_one_row
				+ DbDb2Environment.fetch_only_with_ur;
		PreparedStatement pstmt1 = con.prepareStatement(stmtTxt);
		pstmt1.setString(1, dbname);
		ResultSet rs1 = pstmt1.executeQuery();

		if (rs1.next()) {
			dbExists = true;
		}
		rs1.close();
		pstmt1.close();
		return dbExists;
	}

	private String getStogroup(Connection con) throws SQLException {
		return DbDb2Stogroup.getStogroup(con);
	}

	/**
	 * retrieve current schema from db
	 * 
	 * @param con
	 *            connection
	 */
	private String getSchema(Connection con) throws SQLException {
		return DbDb2Environment.getSchema(con);
	}

	private int typeDb2DdlStmt(String sql) {

		int type = DEFAULT;
		StringTokenizer st = new StringTokenizer(sql);
		if (st.hasMoreTokens()) {
			String s = st.nextToken();
			int ddlType = indexReservedWord(s);
			switch (ddlType) {
				case 0 : // CREATE
					boolean found = false;
					if (st.hasMoreTokens()) {
						s = st.nextToken();
						if (0 == s.compareToIgnoreCase("DATABASE")) {
							type = CREATE_DATABASE;
							if (st.hasMoreTokens()) {
								dbName = st.nextToken();
							}
							if (st.hasMoreTokens() && (0 == st.nextToken().compareTo("BUFFERPOOL"))) {
								if (st.hasMoreTokens())
								  bpool = st.nextToken();
							}
						} else if (0 == s.compareToIgnoreCase("TABLESPACE")) {
							type = CREATE_TABLESPACE;
							if (st.hasMoreTokens())
								tblspName = st.nextToken();
							if (st.hasMoreTokens())
								st.nextToken();
							if (st.hasMoreTokens())
								dbName = st.nextToken();
						} else if (
							(0 == s.compareToIgnoreCase("INDEX"))
								|| (0 == s.compareToIgnoreCase("UNIQUE"))
								|| (0 == s.compareToIgnoreCase("TYPE"))) {
							type = CREATE_INDEX;
						}
						if (type != DEFAULT) {
							while (st.hasMoreTokens()) {
								s = st.nextToken();
								if (0 <= s.indexOf("___")) {
									isDb2DdlStmt = true;
									break;
								}
							}

						}
					}
					break;
				case 1 : // DROP
					if (st.hasMoreTokens())
						s = st.nextToken();
					if (0 == s.compareToIgnoreCase("TABLE")) {
						type = DROP_TABLE;
						if (type != DEFAULT) {
							while (st.hasMoreTokens()) {
								s = st.nextToken();
								if (0 <= s.indexOf("+LOCATION")) {
									isDb2DdlStmt = true;
									break;
								}
							}

						}
					}
					break;
			}
		}
		return type;
	}

	private static int indexReservedWord(String id) {
		if (db2DdlWords == null) {
			// build list with all known reserved words
			db2DdlWords = new ArrayList();
			db2DdlWords.add("CREATE");
			db2DdlWords.add("DROP");
			// sort the list
			Collections.sort(db2DdlWords);
		}
		return Collections.binarySearch(db2DdlWords, id);
	}

	public boolean analyseError(
		Connection con,
		SQLException ex,
		String stmtTxt,
		boolean isDrop) {
		loc.entering("analyseError");
		String tabName = getTabName(stmtTxt);
		if (tabName == null)
			tabName = " ";
		try {
			if (isDrop && (ex.getErrorCode() == -204)) {
				Object[] arguments =
					{ tabName, DbDb2Environment.getSQLError(ex, stmtTxt)};
				cat.infoT(loc, "DbDb2SqlStatment ({0}): {1}", arguments);
				loc.exiting();
				return true;
			} else {
				Object[] arguments =
					{ tabName, DbDb2Environment.getSQLError(ex, stmtTxt)};
				cat.errorT(loc, "DbDb2SqlStatment ({0}): {1}", arguments);
				loc.exiting();
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
    // catch errors when creating databases or tablespaces:
	// due to parallel processing objects might be already created or missing
	private boolean executeCreateImplicitObject(
		Connection con,
		String sql,
		int type) {

		Statement stmt = null;
		int retry = 0;

		while (true) {
			try {
				stmt = NativeSQLAccess.createNativeStatement(con);
			} catch (Exception ex) {
				throw new JddRuntimeException(ex, STATEMENT_PREP_EX,
						new Object[]{ex.getMessage(),stmt},cat,Severity.ERROR,loc);			
			}

			try {
				stmt.executeUpdate(sql);
				cat.info(loc, STATEMENT_EXEC_SU, new Object[] { sql });
				return true;
			} catch (SQLException ex) {
				int sqlCode = ex.getErrorCode();

				if (sqlCode == -601) {
					String ObjectExists = "";
					if (type == CREATE_DATABASE)
						ObjectExists =
							"database  " + dbName + " exists, ignore error.";
					else if (type == CREATE_TABLESPACE)
						ObjectExists =
							"tablespace " + tblspName + " exists, ignore error.";
					cat.info(loc, ObjectExists);
					return true;
				} else if (
					sqlCode == -204
						&& (type == CREATE_TABLESPACE)
						&& (retry++ <= 10)) {
					String dbcreate =
						"CREATE DATABASE "
							+ dbName
							+ " CCSID "
							+ DbDb2Environment.ccsid;
					String ObjectDoesNotExists =
						"database  "
							+ dbName
							+ " for tablesapace "
							+ tblspName
							+ " does not exists. "
							+ "Create database: "
							+ dbcreate;
					cat.info(loc, ObjectDoesNotExists);

					if (!executeCreateImplicitObject(con,
						dbcreate,
						CREATE_DATABASE))
						return false;
				} else {
					throw new JddRuntimeException(ex, STATEMENT_EXEC_EX,
							new Object[]{ex.getMessage(),sql},cat,Severity.ERROR,loc);					
				}
			} finally {
				try {
					stmt.close();
				} catch (SQLException e) {
					throw JddRuntimeException.createInstance(
						e,
						cat,
						Severity.ERROR,
						loc);
				}
			}
		}
	}

}
