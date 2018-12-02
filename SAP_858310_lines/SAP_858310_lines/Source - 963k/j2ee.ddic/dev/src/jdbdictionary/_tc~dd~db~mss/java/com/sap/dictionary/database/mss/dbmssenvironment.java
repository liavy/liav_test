package com.sap.dictionary.database.mss;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;

import com.sap.dictionary.database.dbs.Logger;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.*;

import com.sap.dictionary.database.dbs.DbEnvironment;

/**
 * Title:        Analysis of table and view changes: MS SQL Server specific classes
 * Description:  MS SQL Server specific analysis of table and view changes. Tool to deliver MS SQL Server specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public class DbMssEnvironment extends DbEnvironment {

	private Connection con = null;
	private static Location loc = Logger.getLocation("mss.DbMssEnvironment");
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);

	static private ArrayList reservedWords = null;

	private int databaseVersions[] =          {   8 ,   9 ,  10  };  /* ascending */
	private String databaseVersionStrings[] = { "08", "09", "10" }; 

	private int currentDBVIndex = -1;

	public DbMssEnvironment() {
		super();
	}

	public DbMssEnvironment(Connection con) {
		super(con);
		this.con = con;
	}

	public static boolean isReservedWord(String id) {
		if (reservedWords == null) {
			// build list with all known reserved words
			reservedWords = new ArrayList();

			// currently reserved words
			reservedWords.add("ALL");
			reservedWords.add("EXEC");
			reservedWords.add("PLAN");
			reservedWords.add("ALTER");
			reservedWords.add("EXECUTE");
			reservedWords.add("PRECISION");
			reservedWords.add("AND");
			reservedWords.add("EXISTS");
			reservedWords.add("PRIMARY");
			reservedWords.add("ANY");
			reservedWords.add("EXIT");
			reservedWords.add("PRINT");
			reservedWords.add("AS");
			reservedWords.add("FETCH");
			reservedWords.add("PROC");
			reservedWords.add("ASC");
			reservedWords.add("FILE");
			reservedWords.add("PROCEDURE");
			reservedWords.add("AUTHORIZATION");
			reservedWords.add("FILLFACTOR");
			reservedWords.add("PUBLIC");
			reservedWords.add("BACKUP");
			reservedWords.add("FOR");
			reservedWords.add("RAISERROR");
			reservedWords.add("BEGIN");
			reservedWords.add("FOREIGN");
			reservedWords.add("READ");
			reservedWords.add("BETWEEN");
			reservedWords.add("FREETEXT");
			reservedWords.add("READTEXT");
			reservedWords.add("BREAK");
			reservedWords.add("FREETEXTTABLE");
			reservedWords.add("RECONFIGURE");
			reservedWords.add("BROWSE");
			reservedWords.add("FROM");
			reservedWords.add("REFERENCES");
			reservedWords.add("BULK");
			reservedWords.add("FULL");
			reservedWords.add("REPLICATION");
			reservedWords.add("BY");
			reservedWords.add("FUNCTION");
			reservedWords.add("RESTORE");
			reservedWords.add("CASCADE");
			reservedWords.add("GOTO");
			reservedWords.add("RESTRICT");
			reservedWords.add("CASE");
			reservedWords.add("GRANT");
			reservedWords.add("RETURN");
			reservedWords.add("CHECK");
			reservedWords.add("GROUP");
			reservedWords.add("REVOKE");
			reservedWords.add("CHECKPOINT");
			reservedWords.add("HAVING");
			reservedWords.add("RIGHT");
			reservedWords.add("CLOSE");
			reservedWords.add("HOLDLOCK");
			reservedWords.add("ROLLBACK");
			reservedWords.add("CLUSTERED");
			reservedWords.add("IDENTITY");
			reservedWords.add("ROWCOUNT");
			reservedWords.add("COALESCE");
			reservedWords.add("IDENTITY_INSERT");
			reservedWords.add("ROWGUIDCOL");
			reservedWords.add("COLLATE");
			reservedWords.add("IDENTITYCOL");
			reservedWords.add("RULE");
			reservedWords.add("COLUMN");
			reservedWords.add("IF");
			reservedWords.add("SAVE");
			reservedWords.add("COMMIT");
			reservedWords.add("IN");
			reservedWords.add("SCHEMA");
			reservedWords.add("COMPUTE");
			reservedWords.add("INDEX");
			reservedWords.add("SELECT");
			reservedWords.add("CONSTRAINT");
			reservedWords.add("INNER");
			reservedWords.add("SESSION_USER");
			reservedWords.add("CONTAINS");
			reservedWords.add("INSERT");
			reservedWords.add("SET");
			reservedWords.add("CONTAINSTABLE");
			reservedWords.add("INTERSECT");
			reservedWords.add("SETUSER");
			reservedWords.add("CONTINUE");
			reservedWords.add("INTO");
			reservedWords.add("SHUTDOWN");
			reservedWords.add("CONVERT");
			reservedWords.add("IS");
			reservedWords.add("SOME");
			reservedWords.add("CREATE");
			reservedWords.add("JOIN");
			reservedWords.add("STATISTICS");
			reservedWords.add("CROSS");
			reservedWords.add("KEY");
			reservedWords.add("SYSTEM_USER");
			reservedWords.add("CURRENT");
			reservedWords.add("KILL");
			reservedWords.add("TABLE");
			reservedWords.add("CURRENT_DATE");
			reservedWords.add("LEFT");
			reservedWords.add("TEXTSIZE");
			reservedWords.add("CURRENT_TIME");
			reservedWords.add("LIKE");
			reservedWords.add("THEN");
			reservedWords.add("CURRENT_TIMESTAMP");
			reservedWords.add("LINENO");
			reservedWords.add("TO");
			reservedWords.add("CURRENT_USER");
			reservedWords.add("LOAD");
			reservedWords.add("TOP");
			reservedWords.add("CURSOR");
			reservedWords.add("NATIONAL");
			reservedWords.add("TRAN");
			reservedWords.add("DATABASE");
			reservedWords.add("NOCHECK");
			reservedWords.add("TRANSACTION");
			reservedWords.add("DBCC");
			reservedWords.add("NONCLUSTERED");
			reservedWords.add("TRIGGER");
			reservedWords.add("DEALLOCATE");
			reservedWords.add("NOT");
			reservedWords.add("TRUNCATE");
			reservedWords.add("DECLARE");
			reservedWords.add("NULL");
			reservedWords.add("TSEQUAL");
			reservedWords.add("DEFAULT");
			reservedWords.add("NULLIF");
			reservedWords.add("UNION");
			reservedWords.add("DELETE");
			reservedWords.add("OF");
			reservedWords.add("UNIQUE");
			reservedWords.add("DENY");
			reservedWords.add("OFF");
			reservedWords.add("UPDATE");
			reservedWords.add("DESC");
			reservedWords.add("OFFSETS");
			reservedWords.add("UPDATETEXT");
			reservedWords.add("DISK");
			reservedWords.add("ON");
			reservedWords.add("USE");
			reservedWords.add("DISTINCT");
			reservedWords.add("OPEN");
			reservedWords.add("USER");
			reservedWords.add("DISTRIBUTED");
			reservedWords.add("OPENDATASOURCE");
			reservedWords.add("VALUES");
			reservedWords.add("DOUBLE");
			reservedWords.add("OPENQUERY");
			reservedWords.add("VARYING");
			reservedWords.add("DROP");
			reservedWords.add("OPENROWSET");
			reservedWords.add("VIEW");
			reservedWords.add("DUMMY");
			reservedWords.add("OPENXML");
			reservedWords.add("WAITFOR");
			reservedWords.add("DUMP");
			reservedWords.add("OPTION");
			reservedWords.add("WHEN");
			reservedWords.add("ELSE");
			reservedWords.add("OR");
			reservedWords.add("WHERE");
			reservedWords.add("END");
			reservedWords.add("ORDER");
			reservedWords.add("WHILE");
			reservedWords.add("ERRLVL");
			reservedWords.add("OUTER");
			reservedWords.add("WITH");
			reservedWords.add("ESCAPE");
			reservedWords.add("OVER");
			reservedWords.add("WRITETEXT");
			// ODBC keywords
			reservedWords.add("ACTION");
			reservedWords.add("EXECUTE");
			reservedWords.add("PAD");
			reservedWords.add("ADA");
			reservedWords.add("EXISTS");
			reservedWords.add("PARTIAL");
			reservedWords.add("ADD");
			reservedWords.add("EXTERNAL");
			reservedWords.add("PASCAL");
			reservedWords.add("ALL");
			reservedWords.add("EXTRACT");
			reservedWords.add("POSITION");
			reservedWords.add("ALLOCATE");
			reservedWords.add("FALSE");
			reservedWords.add("PRECISION");
			reservedWords.add("ALTER");
			reservedWords.add("FETCH");
			reservedWords.add("PREPARE");
			reservedWords.add("AND");
			reservedWords.add("FIRST");
			reservedWords.add("PRESERVE");
			reservedWords.add("ANY");
			reservedWords.add("FLOAT");
			reservedWords.add("PRIMARY");
			reservedWords.add("ARE");
			reservedWords.add("FOR");
			reservedWords.add("PRIOR");
			reservedWords.add("AS");
			reservedWords.add("FOREIGN");
			reservedWords.add("PRIVILEGES");
			reservedWords.add("ASC");
			reservedWords.add("FORTRAN");
			reservedWords.add("PROCEDURE");
			reservedWords.add("ASSERTION");
			reservedWords.add("FOUND");
			reservedWords.add("PUBLIC");
			reservedWords.add("AT");
			reservedWords.add("FROM");
			reservedWords.add("READ");
			reservedWords.add("AUTHORIZATION");
			reservedWords.add("FULL");
			reservedWords.add("REAL");
			reservedWords.add("AVG");
			reservedWords.add("GET");
			reservedWords.add("REFERENCES");
			reservedWords.add("BEGIN");
			reservedWords.add("GLOBAL");
			reservedWords.add("RELATIVE");
			reservedWords.add("BETWEEN");
			reservedWords.add("GO");
			reservedWords.add("RESTRICT");
			reservedWords.add("BIT");
			reservedWords.add("GOTO");
			reservedWords.add("REVOKE");
			reservedWords.add("BIT_LENGTH");
			reservedWords.add("GRANT");
			reservedWords.add("RIGHT");
			reservedWords.add("BOTH");
			reservedWords.add("GROUP");
			reservedWords.add("ROLLBACK");
			reservedWords.add("BY");
			reservedWords.add("HAVING");
			reservedWords.add("ROWS");
			reservedWords.add("CASCADE");
			reservedWords.add("HOUR");
			reservedWords.add("SCHEMA");
			reservedWords.add("CASCADED");
			reservedWords.add("IDENTITY");
			reservedWords.add("SCROLL");
			reservedWords.add("CASE");
			reservedWords.add("IMMEDIATE");
			reservedWords.add("SECOND");
			reservedWords.add("CAST");
			reservedWords.add("IN");
			reservedWords.add("SECTION");
			reservedWords.add("CATALOG");
			reservedWords.add("INCLUDE");
			reservedWords.add("SELECT");
			reservedWords.add("CHAR");
			reservedWords.add("INDEX");
			reservedWords.add("SESSION");
			reservedWords.add("CHAR_LENGTH");
			reservedWords.add("INDICATOR");
			reservedWords.add("SESSION_USER");
			reservedWords.add("CHARACTER");
			reservedWords.add("INITIALLY");
			reservedWords.add("SET");
			reservedWords.add("CHARACTER_LENGTH");
			reservedWords.add("INNER");
			reservedWords.add("SIZE");
			reservedWords.add("CHECK");
			reservedWords.add("INPUT");
			reservedWords.add("SMALLINT");
			reservedWords.add("CLOSE");
			reservedWords.add("INSENSITIVE");
			reservedWords.add("SOME");
			reservedWords.add("COALESCE");
			reservedWords.add("INSERT");
			reservedWords.add("SPACE");
			reservedWords.add("COLLATE");
			reservedWords.add("INT");
			reservedWords.add("SQL");
			reservedWords.add("COLLATION");
			reservedWords.add("INTEGER");
			reservedWords.add("SQLCA");
			reservedWords.add("COLUMN");
			reservedWords.add("INTERSECT");
			reservedWords.add("SQLCODE");
			reservedWords.add("COMMIT");
			reservedWords.add("INTERVAL");
			reservedWords.add("SQLERROR");
			reservedWords.add("CONNECT");
			reservedWords.add("INTO");
			reservedWords.add("SQLSTATE");
			reservedWords.add("CONNECTION");
			reservedWords.add("IS");
			reservedWords.add("SQLWARNING");
			reservedWords.add("CONSTRAINT");
			reservedWords.add("ISOLATION");
			reservedWords.add("SUBSTRING");
			reservedWords.add("CONSTRAINTS");
			reservedWords.add("JOIN");
			reservedWords.add("SUM");
			reservedWords.add("CONTINUE");
			reservedWords.add("KEY");
			reservedWords.add("SYSTEM_USER");
			reservedWords.add("CONVERT");
			reservedWords.add("LANGUAGE");
			reservedWords.add("TABLE");
			reservedWords.add("CORRESPONDING");
			reservedWords.add("LAST");
			reservedWords.add("TEMPORARY");
			reservedWords.add("COUNT");
			reservedWords.add("LEADING");
			reservedWords.add("THEN");
			reservedWords.add("CREATE");
			reservedWords.add("LEFT");
			reservedWords.add("TIME");
			reservedWords.add("CROSS");
			reservedWords.add("LEVEL");
			reservedWords.add("TIMESTAMP");
			reservedWords.add("CURRENT");
			reservedWords.add("LIKE");
			reservedWords.add("TIMEZONE_HOUR");
			reservedWords.add("CURRENT_DATE");
			reservedWords.add("LOCAL");
			reservedWords.add("TIMEZONE_MINUTE");
			reservedWords.add("CURRENT_TIME");
			reservedWords.add("LOWER");
			reservedWords.add("TO");
			reservedWords.add("CURRENT_TIMESTAMP");
			reservedWords.add("MATCH");
			reservedWords.add("TRAILING");
			reservedWords.add("CURRENT_USER");
			reservedWords.add("MAX");
			reservedWords.add("TRANSACTION");
			reservedWords.add("CURSOR");
			reservedWords.add("MIN");
			reservedWords.add("TRANSLATE");
			reservedWords.add("DATE");
			reservedWords.add("MINUTE");
			reservedWords.add("TRANSLATION");
			reservedWords.add("DAY");
			reservedWords.add("MODULE");
			reservedWords.add("TRIM");
			reservedWords.add("DEALLOCATE");
			reservedWords.add("MONTH");
			reservedWords.add("TRUE");
			reservedWords.add("DEC");
			reservedWords.add("NAMES");
			reservedWords.add("UNION");
			reservedWords.add("DECIMAL");
			reservedWords.add("NATIONAL");
			reservedWords.add("UNIQUE");
			reservedWords.add("DECLARE");
			reservedWords.add("NATURAL");
			reservedWords.add("UNKNOWN");
			reservedWords.add("DEFAULT");
			reservedWords.add("NCHAR");
			reservedWords.add("UPDATE");
			reservedWords.add("DEFERRABLE");
			reservedWords.add("NEXT");
			reservedWords.add("UPPER");
			reservedWords.add("DEFERRED");
			reservedWords.add("NO");
			reservedWords.add("USAGE");
			reservedWords.add("DELETE");
			reservedWords.add("NONE");
			reservedWords.add("USER");
			reservedWords.add("DESC");
			reservedWords.add("NOT");
			reservedWords.add("USING");
			reservedWords.add("DESCRIBE");
			reservedWords.add("NULL");
			reservedWords.add("VALUE");
			reservedWords.add("DESCRIPTOR");
			reservedWords.add("NULLIF");
			reservedWords.add("VALUES");
			reservedWords.add("DIAGNOSTICS");
			reservedWords.add("NUMERIC");
			reservedWords.add("VARCHAR");
			reservedWords.add("DISCONNECT");
			reservedWords.add("OCTET_LENGTH");
			reservedWords.add("VARYING");
			reservedWords.add("DISTINCT");
			reservedWords.add("OF");
			reservedWords.add("VIEW");
			reservedWords.add("DOMAIN");
			reservedWords.add("ON");
			reservedWords.add("WHEN");
			reservedWords.add("DOUBLE");
			reservedWords.add("ONLY");
			reservedWords.add("WHENEVER");
			reservedWords.add("DROP");
			reservedWords.add("OPEN");
			reservedWords.add("WHERE");
			reservedWords.add("ELSE");
			reservedWords.add("OPTION");
			reservedWords.add("WITH");
			reservedWords.add("END");
			reservedWords.add("OR");
			reservedWords.add("WORK");
			reservedWords.add("END-EXEC");
			reservedWords.add("ORDER");
			reservedWords.add("WRITE");
			reservedWords.add("ESCAPE");
			reservedWords.add("OUTER");
			reservedWords.add("YEAR");
			reservedWords.add("EXCEPT");
			reservedWords.add("OUTPUT");
			reservedWords.add("ZONE");
			reservedWords.add("EXCEPTION");
			// future keywords
			reservedWords.add("ABSOLUTE");
			reservedWords.add("FOUND");
			reservedWords.add("PRESERVE");
			reservedWords.add("ACTION");
			reservedWords.add("FREE");
			reservedWords.add("PRIOR");
			reservedWords.add("ADMIN");
			reservedWords.add("GENERAL");
			reservedWords.add("PRIVILEGES");
			reservedWords.add("AFTER");
			reservedWords.add("GET");
			reservedWords.add("READS");
			reservedWords.add("AGGREGATE");
			reservedWords.add("GLOBAL");
			reservedWords.add("REAL");
			reservedWords.add("ALIAS");
			reservedWords.add("GO");
			reservedWords.add("RECURSIVE");
			reservedWords.add("ALLOCATE");
			reservedWords.add("GROUPING");
			reservedWords.add("REF");
			reservedWords.add("ARE");
			reservedWords.add("HOST");
			reservedWords.add("REFERENCING");
			reservedWords.add("ARRAY");
			reservedWords.add("HOUR");
			reservedWords.add("RELATIVE");
			reservedWords.add("ASSERTION");
			reservedWords.add("IGNORE");
			reservedWords.add("RESULT");
			reservedWords.add("AT");
			reservedWords.add("IMMEDIATE");
			reservedWords.add("RETURNS");
			reservedWords.add("BEFORE");
			reservedWords.add("INDICATOR");
			reservedWords.add("ROLE");
			reservedWords.add("BINARY");
			reservedWords.add("INITIALIZE");
			reservedWords.add("ROLLUP");
			reservedWords.add("BIT");
			reservedWords.add("INITIALLY");
			reservedWords.add("ROUTINE");
			reservedWords.add("BLOB");
			reservedWords.add("INOUT");
			reservedWords.add("ROW");
			reservedWords.add("BOOLEAN");
			reservedWords.add("INPUT");
			reservedWords.add("ROWS");
			reservedWords.add("BOTH");
			reservedWords.add("INT");
			reservedWords.add("SAVEPOINT");
			reservedWords.add("BREADTH");
			reservedWords.add("INTEGER");
			reservedWords.add("SCROLL");
			reservedWords.add("CALL");
			reservedWords.add("INTERVAL");
			reservedWords.add("SCOPE");
			reservedWords.add("CASCADED");
			reservedWords.add("ISOLATION");
			reservedWords.add("SEARCH");
			reservedWords.add("CAST");
			reservedWords.add("ITERATE");
			reservedWords.add("SECOND");
			reservedWords.add("CATALOG");
			reservedWords.add("LANGUAGE");
			reservedWords.add("SECTION");
			reservedWords.add("CHAR");
			reservedWords.add("LARGE");
			reservedWords.add("SEQUENCE");
			reservedWords.add("CHARACTER");
			reservedWords.add("LAST");
			reservedWords.add("SESSION");
			reservedWords.add("CLASS");
			reservedWords.add("LATERAL");
			reservedWords.add("SETS");
			reservedWords.add("CLOB");
			reservedWords.add("LEADING");
			reservedWords.add("SIZE");
			reservedWords.add("COLLATION");
			reservedWords.add("LESS");
			reservedWords.add("SMALLINT");
			reservedWords.add("COMPLETION");
			reservedWords.add("LEVEL");
			reservedWords.add("SPACE");
			reservedWords.add("CONNECT");
			reservedWords.add("LIMIT");
			reservedWords.add("SPECIFIC");
			reservedWords.add("CONNECTION");
			reservedWords.add("LOCAL");
			reservedWords.add("SPECIFICTYPE");
			reservedWords.add("CONSTRAINTS");
			reservedWords.add("LOCALTIME");
			reservedWords.add("SQL");
			reservedWords.add("CONSTRUCTOR");
			reservedWords.add("LOCALTIMESTAMP");
			reservedWords.add("SQLEXCEPTION");
			reservedWords.add("CORRESPONDING");
			reservedWords.add("LOCATOR");
			reservedWords.add("SQLSTATE");
			reservedWords.add("CUBE");
			reservedWords.add("MAP");
			reservedWords.add("SQLWARNING");
			reservedWords.add("CURRENT_PATH");
			reservedWords.add("MATCH");
			reservedWords.add("START");
			reservedWords.add("CURRENT_ROLE");
			reservedWords.add("MINUTE");
			reservedWords.add("STATE");
			reservedWords.add("CYCLE");
			reservedWords.add("MODIFIES");
			reservedWords.add("STATEMENT");
			reservedWords.add("DATA");
			reservedWords.add("MODIFY");
			reservedWords.add("STATIC");
			reservedWords.add("DATE");
			reservedWords.add("MODULE");
			reservedWords.add("STRUCTURE");
			reservedWords.add("DAY");
			reservedWords.add("MONTH");
			reservedWords.add("TEMPORARY");
			reservedWords.add("DEC");
			reservedWords.add("NAMES");
			reservedWords.add("TERMINATE");
			reservedWords.add("DECIMAL");
			reservedWords.add("NATURAL");
			reservedWords.add("THAN");
			reservedWords.add("DEFERRABLE");
			reservedWords.add("NCHAR");
			reservedWords.add("TIME");
			reservedWords.add("DEFERRED");
			reservedWords.add("NCLOB");
			reservedWords.add("TIMESTAMP");
			reservedWords.add("DEPTH");
			reservedWords.add("NEW");
			reservedWords.add("TIMEZONE_HOUR");
			reservedWords.add("DEREF");
			reservedWords.add("NEXT");
			reservedWords.add("TIMEZONE_MINUTE");
			reservedWords.add("DESCRIBE");
			reservedWords.add("NO");
			reservedWords.add("TRAILING");
			reservedWords.add("DESCRIPTOR");
			reservedWords.add("NONE");
			reservedWords.add("TRANSLATION");
			reservedWords.add("DESTROY");
			reservedWords.add("NUMERIC");
			reservedWords.add("TREAT");
			reservedWords.add("DESTRUCTOR");
			reservedWords.add("OBJECT");
			reservedWords.add("TRUE");
			reservedWords.add("DETERMINISTIC");
			reservedWords.add("OLD");
			reservedWords.add("UNDER");
			reservedWords.add("DICTIONARY");
			reservedWords.add("ONLY");
			reservedWords.add("UNKNOWN");
			reservedWords.add("DIAGNOSTICS");
			reservedWords.add("OPERATION");
			reservedWords.add("UNNEST");
			reservedWords.add("DISCONNECT");
			reservedWords.add("ORDINALITY");
			reservedWords.add("USAGE");
			reservedWords.add("DOMAIN");
			reservedWords.add("OUT");
			reservedWords.add("USING");
			reservedWords.add("DYNAMIC");
			reservedWords.add("OUTPUT");
			reservedWords.add("VALUE");
			reservedWords.add("EACH");
			reservedWords.add("PAD");
			reservedWords.add("VARCHAR");
			reservedWords.add("END-EXEC");
			reservedWords.add("PARAMETER");
			reservedWords.add("VARIABLE");
			reservedWords.add("EQUALS");
			reservedWords.add("PARAMETERS");
			reservedWords.add("WHENEVER");
			reservedWords.add("EVERY");
			reservedWords.add("PARTIAL");
			reservedWords.add("WITHOUT");
			reservedWords.add("EXCEPTION");
			reservedWords.add("PATH");
			reservedWords.add("WORK");
			reservedWords.add("EXTERNAL");
			reservedWords.add("POSTFIX");
			reservedWords.add("WRITE");
			reservedWords.add("FALSE");
			reservedWords.add("PREFIX");
			reservedWords.add("YEAR");
			reservedWords.add("FIRST");
			reservedWords.add("PREORDER");
			reservedWords.add("ZONE");
			reservedWords.add("FLOAT");
			reservedWords.add("PREPARE");

			// sort the list
			Collections.sort(reservedWords);
		}

		return (Collections.binarySearch(reservedWords, id) >= 0);
	}

        public String[] getSupportedDatabaseVersions() {
  		return databaseVersionStrings;
  	}
  
  	public String getDatabaseVersion() {
		int dbrel = 0;
		String dbrelString = null;
		int versI = 0;
		
		if (currentDBVIndex == -1) {
			if (this.con != null) {
				loc.entering("getDBVersion");

				try {
					/* DatabaseMetaData md = NativeSQLAccess.getNativeMetaData(this.con);
					/o dbrel = md.getDatabaseMajorVersion(); -- doesn't exist yet in 1.3 o/
					dbrelString = md.getDatabaseProductVersion(); -- doesn't work as expected with DataDirect 
					this should work with all (gd050209) */
					
					Statement stmt = NativeSQLAccess.createNativeStatement(this.con);
					ResultSet rs = stmt.executeQuery("select convert(nvarchar, serverproperty('ProductVersion'))");
					if (rs.next()) {
						dbrelString = rs.getString(1);
						dbrel = Integer.valueOf(dbrelString.substring(0, dbrelString.indexOf('.'))).intValue(); 
						for ( versI = 0; 
							versI < databaseVersions.length && databaseVersions[versI] < dbrel;
							versI ++ );
						if (versI == databaseVersions.length) versI --;
						else 
						if (databaseVersions[versI] > dbrel) {
							if (versI > 0) versI --;
						}
					}
					else {
						dbrel = 8;
						versI = 0;
					}
					rs.close();
					stmt.close();
				} catch (Exception ex) {
					Object[] arguments = { ex.getMessage()};
					cat.errorT(loc, "getDatabaseVersion: {0}", arguments);
					loc.exiting();
				}
				loc.exiting();
			}
			else {
				/* this.con == null -- assume the lowest version */
				versI = 0;
			}
			currentDBVIndex = versI; 
		}	

  		return databaseVersionStrings[currentDBVIndex];
  	}
  
  	public void setDatabaseVersion(String version) {
		int versI;
		int dbrel = 0;

		try {
			dbrel = Integer.valueOf(version).intValue();
		} catch (Exception ex) {
			dbrel = 0;
		}

		/* search for lowest fitting release (group) */
		for ( versI = 0; 
			versI < databaseVersions.length && databaseVersions[versI] < dbrel;
			versI ++ );
		if (versI == databaseVersions.length) versI --;
		else 
		if (databaseVersions[versI] > dbrel) {
			if (versI > 0) versI --;
		}

		currentDBVIndex = versI;
  	}
}