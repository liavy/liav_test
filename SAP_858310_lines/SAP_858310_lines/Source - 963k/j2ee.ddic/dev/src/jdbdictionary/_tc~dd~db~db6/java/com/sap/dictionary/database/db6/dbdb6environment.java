package com.sap.dictionary.database.db6;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;

import com.sap.dictionary.database.dbs.DbEnvironment;
import com.sap.dictionary.database.dbs.JddRuntimeException;
import com.sap.dictionary.database.dbs.Logger;
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

public class DbDb6Environment extends DbEnvironment
{
	private static final String DATA_TBSP_ENV_VAR = "DB6_J2EE_DATA_TBSP";
	private static final String INDEX_TBSP_ENV_VAR = "DB6_J2EE_INDEX_TBSP";
	private static final String LONG_TBSP_ENV_VAR = "DB6_J2EE_LONG_TBSP";
	private static final String SAPSYSTEM_ENV_VAR = "SAPSYSTEMNAME";
	private static final String[] DBVERSIONS =
	{
		   "SQL08022", // DB6 V8.2
			"SQL09010", // DB6 V9.1
			"SQL09050"  // DB6 V9.5
	};

	private static ArrayList<String> reservedWords = null;
	private String defaultTablespaceClause = null;
	private String currentSchema = null;
	private String databaseVersion = null;
	private Connection activeConnection = null;

	private static final Category cat = Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);
	private static Location loc = Logger.getLocation("db6.DbDb6Environment");

	public DbDb6Environment()
	{
		super();
	}

	public DbDb6Environment(Connection con)
	{
		super(con);

		loc.entering("DbDb6Environment");

		if (con == null)
		{
			defaultTablespaceClause = null;
			currentSchema = null;
		} else if (con != activeConnection)
		{
			retrieveDefaultTablespace(con);

			try
			{
				databaseVersion = con.getMetaData().getDatabaseProductVersion();
				Object[] arguments = {databaseVersion};

				if (databaseVersion.compareTo("SQL08022") < 0)
				{
					cat.errorT(loc, "DbDb6Environment: database version is {0} . Minumum required version is SQL08022 .",
							arguments);
				} else
				{
					cat.infoT(loc, "DbDb6Environment: database version is {0} .", arguments);
				}
			} catch (Exception e)
			{
				cat.warningT(loc, "DbDb6Environment: Could not retrieve database product version.");
				cat.warningT(loc, e.getMessage());
				databaseVersion = null;
			}
		}

		activeConnection = con;

		loc.exiting();
	}

	void retrieveDefaultTablespace(Connection con)
	{
		loc.entering("retrieveDefaultTablespace");

		/*
		 * search SYSCAT.TABLESPACES for tablespaces matching the tablespace
		 * naming convention and construct a tablespace clause (1) use tablespaces
		 * beginning with SID#<ID> for schema sap<sid><id> (2) use tablespaces
		 * beginning with SID#DB if it exits (3) use tablespaces beginning with
		 * <SAPSYSTEMNAME>#DB, where <SAPSYSTEMNAME> is the value of the
		 * environment variable SAPSYSTEMNAME (4) use tablespaces provided in the
		 * environment variables DB6_J2EE_DATA_TBSP, DB6_J2EE_INDEX_TBSP and
		 * DB6_J2EE_LONG_TBSP (5) ommit tablespace clause and use DB2 default
		 * tablespaces
		 */
		defaultTablespaceClause = null;

		try
		{
			String tableSpacePrefix = null;
			String tableSpace = null;
			String dataTbs = null;
			String indexTbs = null;
			String longTbs = null;
			boolean dataTbsIsSMS = false;

			String sql;
			Statement schemaStatement;
			PreparedStatement tableSpaceStatement;
			ResultSet rs;

			//
			// store name of current schema in currentSchema
			//
			sql = "SELECT CURRENT SCHEMA FROM SYSCAT.PROCEDURES FETCH FIRST 1 ROWS ONLY WITH UR";
			schemaStatement = NativeSQLAccess.createNativeStatement(con);
			rs = schemaStatement.executeQuery(sql);
			rs.next();
			currentSchema = rs.getString(1).trim();
			rs.close();
			schemaStatement.close();

			if (currentSchema.startsWith("SAP"))
			{
				tableSpaceStatement = NativeSQLAccess.prepareNativeStatement(con,
						"SELECT TBSPACE, TBSPACETYPE FROM SYSCAT.TABLESPACES WHERE TBSPACE LIKE ?");
				//
				// (1) search for tablespaces beginning with SID#<ID>
				//
				if (currentSchema.startsWith("SAPR3") && currentSchema.length() == 7)
				{
					tableSpacePrefix = "PSAP" + currentSchema.substring(5, 7);
				} else if (currentSchema.length() > 7)
				{
					tableSpacePrefix = currentSchema.substring(3, 6) + "#" + currentSchema.substring(6, 8);
				} else
				{
					tableSpacePrefix = "PSAPDB";
				}

				tableSpaceStatement.setString(1, tableSpacePrefix + "_");
				rs = tableSpaceStatement.executeQuery();

				while (rs.next())
				{
					tableSpace = rs.getString(1);

					if (tableSpace.endsWith("D"))
					{
						dataTbs = tableSpace;
						dataTbsIsSMS = rs.getString(2).equals("S");
					} else if (tableSpace.endsWith("I"))
						indexTbs = tableSpace;
					else if (tableSpace.endsWith("L"))
						longTbs = tableSpace;
				}
				rs.close();

				if (dataTbs != null)
				{
					defaultTablespaceClause = " IN " + dataTbs;

					if (!dataTbsIsSMS && indexTbs != null)
					{
						defaultTablespaceClause = defaultTablespaceClause + " INDEX IN " + indexTbs;
					}
					if (!dataTbsIsSMS && longTbs != null)
					{
						defaultTablespaceClause = defaultTablespaceClause + " LONG IN " + longTbs;
					}
				}

				//
				// (2) search for tablespaces beginning with SID#DB
				//
				if (defaultTablespaceClause == null)
				{

					if (currentSchema.length() > 7)
					{
						tableSpacePrefix = currentSchema.substring(3, 6) + "#DB";
					} else
					{
						tableSpacePrefix = "PSAPDB";
					}

					tableSpaceStatement.setString(1, tableSpacePrefix + "_");
					rs = tableSpaceStatement.executeQuery();

					while (rs.next())
					{
						tableSpace = rs.getString(1);

						if (tableSpace.endsWith("D"))
						{
							dataTbs = tableSpace;
							dataTbsIsSMS = rs.getString(2).equals("S");
						} else if (tableSpace.endsWith("I"))
							indexTbs = tableSpace;
						else if (tableSpace.endsWith("L"))
							longTbs = tableSpace;

					}
					rs.close();

					if (dataTbs != null)
					{
						defaultTablespaceClause = " IN " + dataTbs;

						if (!dataTbsIsSMS && indexTbs != null)
						{
							defaultTablespaceClause = defaultTablespaceClause + " INDEX IN " + indexTbs;
						}
						if (!dataTbsIsSMS && longTbs != null)
						{
							defaultTablespaceClause = defaultTablespaceClause + " LONG IN " + longTbs;
						}
					}
				}

				//
				// (3) search for tablespaces beginning with <SAPSYSTEMNAME>#DB
				//
				if (defaultTablespaceClause == null)
				{
					String sapsystemname = System.getenv(SAPSYSTEM_ENV_VAR);
					if (sapsystemname != null)
					{
						if (currentSchema.length() > 7)
						{
							tableSpacePrefix = sapsystemname + "#DB";
						} else
						{
							tableSpacePrefix = "PSAPDB";
						}

						tableSpaceStatement.setString(1, tableSpacePrefix + "_");
						rs = tableSpaceStatement.executeQuery();

						while (rs.next())
						{
							tableSpace = rs.getString(1);

							if (tableSpace.endsWith("D"))
							{
								dataTbs = tableSpace;
								dataTbsIsSMS = rs.getString(2).equals("S");
							} else if (tableSpace.endsWith("I"))
								indexTbs = tableSpace;
							else if (tableSpace.endsWith("L"))
								longTbs = tableSpace;

						}
						rs.close();

						if (dataTbs != null)
						{
							defaultTablespaceClause = " IN " + dataTbs;

							if (!dataTbsIsSMS && indexTbs != null)
							{
								defaultTablespaceClause = defaultTablespaceClause + " INDEX IN " + indexTbs;
							}
							if (!dataTbsIsSMS && longTbs != null)
							{
								defaultTablespaceClause = defaultTablespaceClause + " LONG IN " + longTbs;
							}
						}
					} // sapsystemname != null
				}

				tableSpaceStatement.close();
				//
				// (4) use tablespaces provided in environment
				//
				if (defaultTablespaceClause == null)
				{
					String envDataTbsp = System.getenv(DATA_TBSP_ENV_VAR);
					String envIndexTbsp = System.getenv(INDEX_TBSP_ENV_VAR);
					String envLongTbsp = System.getenv(LONG_TBSP_ENV_VAR);

					if (envDataTbsp != null)
						defaultTablespaceClause = " IN " + envDataTbsp;
					if (envIndexTbsp != null)
						defaultTablespaceClause = defaultTablespaceClause + " INDEX IN " + envIndexTbsp;
					if (envLongTbsp != null)
						defaultTablespaceClause = defaultTablespaceClause + " LONG IN " + envLongTbsp;
				}
			} // end startsWith("SAP")

			if (defaultTablespaceClause != null)
			{
				Object[] arguments = {defaultTablespaceClause, currentSchema};
				cat.infoT(loc, "retrieveDefaultTablespace: using default tablespace clause \"{0}\" for schema {1} .",
						arguments);
			} else
			{
				//
				// set defaultTablespaceClause to empty string
				// to avoid reinitialization in DbDb6Table
				//
				defaultTablespaceClause = "";
			}

		} catch (SQLException sqlex)
		{
			cat.warningT(loc,
					"retrieveDefaultTablespace: default tablespace could not be determined. DB2 defaults will be used.");
			cat.warningT(loc, sqlex.getMessage());
		}

		loc.exiting();
	}

	public String getCurrentSchema()
	{
		return currentSchema;
	}

	public String getDatabaseVersion()
	{
		return databaseVersion;
	}

	public String getDefaultTablespaceClause()
	{
		return defaultTablespaceClause;
	}

	public static boolean isReservedWord(String id)
	{
		if (reservedWords == null)
		{
			//
			// initialize list with all known reserved words
			//
			reservedWords = new ArrayList<String>();

			//
			// check only for IBM SQL specific reserved words
			//
			reservedWords.add("ACQUIRE");
			reservedWords.add("CONNECT");
			reservedWords.add("EDITPROC");
			reservedWords.add("IN");
			reservedWords.add("ADD");
			reservedWords.add("CONNECTION");
			reservedWords.add("ELSE");
			reservedWords.add("INDEX");
			reservedWords.add("AFTER");
			reservedWords.add("CONSTRAINT");
			reservedWords.add("ELSEIF");
			reservedWords.add("INDICATOR");
			reservedWords.add("ALIAS");
			reservedWords.add("CONTAINS");
			reservedWords.add("END");
			reservedWords.add("INNER");
			reservedWords.add("ALL");
			reservedWords.add("CONTINUE");
			reservedWords.add("END-EXEC");
			reservedWords.add("INOUT");
			reservedWords.add("ALLOCATE");
			reservedWords.add("COUNT");
			reservedWords.add("ERASE");
			reservedWords.add("INSENSITIVE");
			reservedWords.add("ALLOW");
			reservedWords.add("COUNT_BIG");
			reservedWords.add("ESCAPE");
			reservedWords.add("INSERT");
			reservedWords.add("ALTER");
			reservedWords.add("CREATE");
			reservedWords.add("EXCEPT");
			reservedWords.add("INTEGRITY");
			reservedWords.add("AND");
			reservedWords.add("CROSS");
			reservedWords.add("EXCEPTION");
			reservedWords.add("INTERSECT");
			reservedWords.add("ANY");
			reservedWords.add("CURRENT");
			reservedWords.add("EXCLUSIVE");
			reservedWords.add("INTO");
			reservedWords.add("AS");
			reservedWords.add("CURRENT_DATE");
			reservedWords.add("EXECUTE");
			reservedWords.add("IS");
			reservedWords.add("ASC");
			reservedWords.add("CURRENT_LC_PATH");
			reservedWords.add("EXISTS");
			reservedWords.add("ISOBID");
			reservedWords.add("ASUTIME");
			reservedWords.add("CURRENT_PATH");
			reservedWords.add("EXIT");
			reservedWords.add("ISOLATION");
			reservedWords.add("AUDIT");
			reservedWords.add("CURRENT_SERVER");
			reservedWords.add("EXPLAIN");
			reservedWords.add("AUTHORIZATION");
			reservedWords.add("CURRENT_TIME");
			reservedWords.add("EXTERNAL");
			reservedWords.add("JAVA");
			reservedWords.add("AUX");
			reservedWords.add("CURRENT_TIMESTAMP");
			reservedWords.add("JOIN");
			reservedWords.add("AUXILIARY");
			reservedWords.add("CURRENT_TIMEZONE");
			reservedWords.add("FENCED");
			reservedWords.add("AVG");
			reservedWords.add("CURRENT_USER");
			reservedWords.add("FETCH");
			reservedWords.add("KEY");
			reservedWords.add("CURSOR");
			reservedWords.add("FIELDPROC");
			reservedWords.add("BEFORE");
			reservedWords.add("FILE");
			reservedWords.add("LABEL");
			reservedWords.add("BEGIN");
			reservedWords.add("DATA");
			reservedWords.add("FINAL");
			reservedWords.add("LANGUAGE");
			reservedWords.add("BETWEEN");
			reservedWords.add("DATABASE");
			reservedWords.add("FOR");
			reservedWords.add("LC_CTYPE");
			reservedWords.add("BINARY");
			reservedWords.add("DATE");
			reservedWords.add("FOREIGN");
			reservedWords.add("LEAVE");
			reservedWords.add("BUFFERPOOL");
			reservedWords.add("DAY");
			reservedWords.add("FREE");
			reservedWords.add("LEFT");
			reservedWords.add("BY");
			reservedWords.add("DAYS");
			reservedWords.add("FROM");
			reservedWords.add("LIKE");
			reservedWords.add("DBA");
			reservedWords.add("FULL");
			reservedWords.add("LINKTYPE");
			reservedWords.add("CALL");
			reservedWords.add("DBINFO");
			reservedWords.add("FUNCTION");
			reservedWords.add("LOCAL");
			reservedWords.add("CALLED");
			reservedWords.add("DBSPACE");
			reservedWords.add("LOCALE");
			reservedWords.add("CAPTURE");
			reservedWords.add("DB2GENERAL");
			reservedWords.add("GENERAL");
			reservedWords.add("LOCATOR");
			reservedWords.add("CASCADED");
			reservedWords.add("DB2SQL");
			reservedWords.add("GENERATED");
			reservedWords.add("LOCATORS");
			reservedWords.add("CASE");
			reservedWords.add("DECLARE");
			reservedWords.add("GO");
			reservedWords.add("LOCK");
			reservedWords.add("CAST");
			reservedWords.add("DEFAULT");
			reservedWords.add("GOTO");
			reservedWords.add("LOCKSIZE");
			reservedWords.add("CCSID");
			reservedWords.add("DELETE");
			reservedWords.add("GRANT");
			reservedWords.add("LONG");
			reservedWords.add("CHAR");
			reservedWords.add("DESC");
			reservedWords.add("GRAPHIC");
			reservedWords.add("LOOP");
			reservedWords.add("CHARACTER");
			reservedWords.add("DESCRIPTOR");
			reservedWords.add("GROUP");
			reservedWords.add("CHECK");
			reservedWords.add("DETERMINISTIC");
			reservedWords.add("MAX");
			reservedWords.add("CLOSE");
			reservedWords.add("DISALLOW");
			reservedWords.add("HANDLER");
			reservedWords.add("MICROSECOND");
			reservedWords.add("CLUSTER");
			reservedWords.add("DISCONNECT");
			reservedWords.add("HAVING");
			reservedWords.add("MICROSECONDS");
			reservedWords.add("COLLECTION");
			reservedWords.add("DISTINCT");
			reservedWords.add("HOUR");
			reservedWords.add("MIN");
			reservedWords.add("COLLID");
			reservedWords.add("DO");
			reservedWords.add("HOURS");
			reservedWords.add("MINUTE");
			reservedWords.add("COLUMN");
			reservedWords.add("DOUBLE");
			reservedWords.add("MINUTES");
			reservedWords.add("COMMENT");
			reservedWords.add("DROP");
			reservedWords.add("IDENTIFIED");
			reservedWords.add("MODE");
			reservedWords.add("COMMIT");
			reservedWords.add("DSSIZE");
			reservedWords.add("IF");
			reservedWords.add("MODIFIES");
			reservedWords.add("CONCAT");
			reservedWords.add("DYNAMIC");
			reservedWords.add("IMMEDIATE");
			reservedWords.add("MONTH");
			reservedWords.add("CONDITION");
			reservedWords.add("MONTHS");
			reservedWords.add("NAME");
			reservedWords.add("PACKAGE");
			reservedWords.add("SCHEDULE");
			reservedWords.add("UNDO");
			reservedWords.add("NAMED");
			reservedWords.add("PAGE");
			reservedWords.add("SCHEMA");
			reservedWords.add("UNION");
			reservedWords.add("NHEADER");
			reservedWords.add("PAGES");
			reservedWords.add("SCRATCHPAD");
			reservedWords.add("UNIQUE");
			reservedWords.add("NO");
			reservedWords.add("PARAMETER");
			reservedWords.add("SECOND");
			reservedWords.add("UNTIL");
			reservedWords.add("NODENAME");
			reservedWords.add("PART");
			reservedWords.add("SECONDS");
			reservedWords.add("UPDATE");
			reservedWords.add("NODENUMBER");
			reservedWords.add("PARTITION");
			reservedWords.add("SECQTY");
			reservedWords.add("USAGE");
			reservedWords.add("NOT");
			reservedWords.add("PATH");
			reservedWords.add("SECURITY");
			reservedWords.add("USER");
			reservedWords.add("NULL");
			reservedWords.add("PCTFREE");
			reservedWords.add("SELECT");
			reservedWords.add("USING");
			reservedWords.add("NULLS");
			reservedWords.add("PCTINDEX");
			reservedWords.add("SET");
			reservedWords.add("NUMPARTS");
			reservedWords.add("PIECESIZE");
			reservedWords.add("SHARE");
			reservedWords.add("VALIDPROC");
			reservedWords.add("PLAN");
			reservedWords.add("SIMPLE");
			reservedWords.add("VALUES");
			reservedWords.add("OBID");
			reservedWords.add("POSITION");
			reservedWords.add("SOME");
			reservedWords.add("VARIABLE");
			reservedWords.add("OF");
			reservedWords.add("PRECISION");
			reservedWords.add("SOURCE");
			reservedWords.add("VARIANT");
			reservedWords.add("ON");
			reservedWords.add("PREPARE");
			reservedWords.add("SPECIFIC");
			reservedWords.add("VCAT");
			reservedWords.add("ONLY");
			reservedWords.add("PRIMARY");
			reservedWords.add("SQL");
			reservedWords.add("VIEW");
			reservedWords.add("OPEN");
			reservedWords.add("PRIQTY");
			reservedWords.add("STANDARD");
			reservedWords.add("VOLUMES");
			reservedWords.add("OPTIMIZATION");
			reservedWords.add("PRIVATE");
			reservedWords.add("STATIC");
			reservedWords.add("OPTIMIZE");
			reservedWords.add("PRIVILEGES");
			reservedWords.add("STATISTICS");
			reservedWords.add("WHEN");
			reservedWords.add("OPTION");
			reservedWords.add("PROCEDURE");
			reservedWords.add("STAY");
			reservedWords.add("WHERE");
			reservedWords.add("OR");
			reservedWords.add("PROGRAM");
			reservedWords.add("STOGROUP");
			reservedWords.add("WHILE");
			reservedWords.add("ORDER");
			reservedWords.add("PSID");
			reservedWords.add("STORES");
			reservedWords.add("WITH");
			reservedWords.add("OUT");
			reservedWords.add("PUBLIC");
			reservedWords.add("STORPOOL");
			reservedWords.add("WLM");
			reservedWords.add("OUTER");
			reservedWords.add("STYLE");
			reservedWords.add("WORK");
			reservedWords.add("QUERYNO");
			reservedWords.add("SUBPAGES");
			reservedWords.add("WRITE");
			reservedWords.add("SUBSTRING");
			reservedWords.add("READ");
			reservedWords.add("SUM");
			reservedWords.add("YEAR");
			reservedWords.add("READS");
			reservedWords.add("SYNONYM");
			reservedWords.add("YEARS");
			reservedWords.add("RECOVERY");
			reservedWords.add("REFERENCES");
			reservedWords.add("TABLE");
			reservedWords.add("RELEASE");
			reservedWords.add("TABLESPACE");
			reservedWords.add("RENAME");
			reservedWords.add("THEN");
			reservedWords.add("REPEAT");
			reservedWords.add("TO");
			reservedWords.add("RESET");
			reservedWords.add("TRANSACTION");
			reservedWords.add("RESOURCE");
			reservedWords.add("TRIGGER");
			reservedWords.add("RESTRICT");
			reservedWords.add("TRIM");
			reservedWords.add("RESULT");
			reservedWords.add("TYPE");
			reservedWords.add("RETURN");
			reservedWords.add("RETURNS");
			reservedWords.add("REVOKE");
			reservedWords.add("RIGHT");
			reservedWords.add("ROLLBACK");
			reservedWords.add("ROW");
			reservedWords.add("ROWS");
			reservedWords.add("RRN");
			reservedWords.add("RUN");

			//
			// sort the list
			//
			Collections.sort(reservedWords);
		}

		return (Collections.binarySearch(reservedWords, id.toUpperCase()) >= 0);
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbEnvironment#getSupportedDatabaseVersions()
	 */
	@Override
	public String[] getSupportedDatabaseVersions()
	{
		return DBVERSIONS;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbEnvironment#setDatabaseVersion(java.lang.String)
	 */
	@Override
	public void setDatabaseVersion(String version)
	{
		for (int i = 0; i < DBVERSIONS.length; i++)
		{
			if (DBVERSIONS[i].equals(version))
			{
				this.databaseVersion = DBVERSIONS[i];
				return;
			}
			throw new JddRuntimeException("setDatabaseVersion: version " + version + " not supported");
		}
	}
}