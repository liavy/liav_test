package com.sap.dictionary.database.temptests;

import java.io.*;

import com.sap.dictionary.database.dbs.*;
import com.sap.dictionary.database.veris.VeriTools;

import java.sql.*;

import com.sap.sql.jdbc.internal.SAPDataSource;
import javax.sql.*;

import java.util.*;
import java.util.Date;

import com.sap.tc.logging.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * @author d003550
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */
public class ReadLogs implements DbsConstants {
	private static int INFO_ONLY = 1;
	private static String MIN = "2009-05-25 00:00:00.000";
	private static String MAX = "2018-06-06 00:00:00.000";
//	private static String NAME = "TMP_VERI000";
	private static String NAME = "";
	private static boolean testDbs = false;
	private static int SAP = 1;
	private static int MSS = 0;
	private static int ORA = 0;
	private static int DB6 = 0;
	private static int DB4 = 0;
	private static int DB2 = 0;

	// private static String driver =
	// "com.sap.nwmss.jdbc.sqlserver.SQLServerDriver";
	// private static String url = "jdbc:nwmss:sqlserver://10.66.212.202:1433";
	// private static String user = "SAPLKGDB";
	// private static String password = "abcd1234";
//	private static String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
//	private static String url = "jdbc:sqlserver://PWDF6208:1433;databasename=NWP";
//	private static String user = "SAPNWPDB";
//	private static String password = "abcd1234";
	private static String driver = "oracle.jdbc.OracleDriver";
	private static String url = "jdbc:oracle:thin:@isi005:1527:N4S";
	private static String user = "SAPXX3DB";
	private static String password = "isi005xx";
	
//	// MSS 720
//	private static String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
//	private static String url = "jdbc:sqlserver://10.66.211.54:1433;databasename=LKG";
//	private static String user = "SAPLKGDB";
//	private static String password = "abcd1234";
	
//	// MSS 702
//	private static String driver = "com.sap.nwmss.jdbc.sqlserver.SQLServerDriver";
//	private static String url = "jdbc:nwmss:sqlserver://10.66.214.62:1433;databasename=W08";
//	private static String user = "SAPW08DB";
//	private static String password = "abc123";
	
	// private static String driver = "com.sap.dbtech.jdbc.DriverSapDB";
	// private static String url =
	// "jdbc:sapdb://ldcil1a/L1A?timeout=0&spaceoption=true&unicode=yes";
	// private static String user = "SAPL1ADB";
	// private static String password = "pssdb";

	// ***************************************************************************

	public static PrintStream out = null;

	ReadLogs() {
	}

	public static void main(String[] args) {
		out = System.out;
		DataSource ds;
		Connection con;
		if (testDbs) {
			String[] dbnames = new String[6];
			int k = 0;
			if (SAP > 0)
				dbnames[k++] = "**************MAX DB****************";
			if (MSS > 0)
				dbnames[k++] = "**************MSS*******************";
			if (ORA > 0)
				dbnames[k++] = "**************ORA***********************";
			if (DB6 > 0)
				dbnames[k++] = "***************DB6***********************";
			if (DB4 > 0)
				dbnames[k++] = "***************DB4**************************";
			if (DB2 > 0)
				dbnames[k++] = "***************DB2***************************";
			ArrayList cnns = null;
			try {
				cnns = VeriTools.getTestConnections(SAP, MSS, ORA, DB6, DB4, DB2);
				for (int i = 0; i < cnns.size(); i++) {
					out.println(dbnames[i]);
					exec((Connection) cnns.get(i));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else
			try {
				exec(ConnectionService.getConnection(driver, url, user, password));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	public static void exec(Connection con) throws Exception {
		DbDeployLogs logs = DbDeployLogs.getInstance(new DbModificationController(
				new DbFactory(con)));
		DbDeployLogs.Record[] records = null;
		if (INFO_ONLY == 0) {
			if (NAME != null && NAME.trim().length() != 0)
				records = logs.getObjectLogs(NAME);
			else
				records = logs.getObjectLogs(MIN,MAX);
		} else {
			if (NAME != null && NAME.trim().length() != 0)
				records = logs.getObjectLogsInfo(NAME);
			else
				records = logs.getObjectLogsInfo(MIN,MAX);
		}
		for (int i = 0; i < records.length; i++) {
	    out.println(records[i].toString());
    }
	}

	// private static String filesRoot =
	// "C:\\eclipseProjects30\\tableDefinition1\\tableDefinition1.sda";

	// private static String sdaRoot = "C:\\temp\\buildTest\\alterWithViewTest";
	// private static String[] sdaEnds = {"1.sda","2.sda"};
	// private static String sdaRoot = "C:\\temp\\buildTest\\cgt";
	// private static String[] sdaEnds = {".zip"};

	// private static String sdaPath =
	// "C:\\eclipseProjects30\\tableDefinition1\\tableDefinition1.sda";
	// private static String sdaPath =
	// "C:\\eclipseProjects30\\tableDefinition2\\tableDefinition2.sda";
	// private static String sdaPath =
	// "C:\\temp\\buildTest\\alterWithViewTest2.sda";
	// private static String sdaPath = "C:\\temp\\buildTest\\G5EE2.zip";
	// private static String sdaPath =
	// "C:\\eclipseProjects30\\TypeTest\\TypeTest.sda";
	// private static String sdaPath = "C:\\temp\\buildTest\\misc.sda";
	// private static String sdaPath = "C:\\temp\\buildTest\\BC_SLD_MP_INSTA.zip";
	// private static String sdaPath =
	// "C:\\eclipseWorkspaceJDT30\\floattest\\floattest.sda";
	// private static String sdaPath = "C:\\eclipseProjects2\\test1\\test1.sda";
	// private static String sdaPath =
	// "C:\\temp\\buildTest\\com.sap.xi.dbschema.ibrep.sda";
	// private static String sdaPath =
	// "C:\\eclipseWorkspaceJDT30\\misc\\misc.sda";
	// private static String sdaPath =
	// "C:\\eclipseWorkspaceJDT30\\misc\\misc2.zip";
	// private static String logPath = "C:\\temp\\buildTest\\delpoylog.txt";

	// private static String testDbsName = null;
	// private static String testDbsName = "ora";

	// private static String connectionKind = "OPEN";

	// private static String driver = "oracle.jdbc.OracleDriver";
	// private static String url = "jdbc:oracle:thin:@pwdf6156:1527:PKB";
	// private static String user = "SAPPKBSH";
	// private static String password = "abcd1234";

	// private static String driver = "com.ibm.db2.jcc.DB2Driver";
	// //private final static String url =
	// "jdbc:db2://lsi230:5901/QD1:fullyMaterializeLobData=false;";
	// private final static String url =
	// "jdbc:db2://lsi230:5901/QD1:deferPrepares=false;fullyMaterializeLobData=false;currentSchema=hugo;";
	// private final static String user = "SAPQD1DB";
	// private final static String password = "empass12";

	// private static String driver = "oracle.jdbc.OracleDriver";
	// private static String url = "jdbc:oracle:thin:@hwi045:1527:QO1";
	// private static String user = "SAPSR3DB";
	// private static String password = "empass12";

	// private static String driver = "com.sap.dbtech.jdbc.DriverSapDB";
	// private static String url =
	// //"jdbc:sapdb://p76796/N35?timeout=0&spaceoption=true&unicode=yes&trace=c://temp/MaxDBJDBCTrace.prt";
	// "jdbc:sapdb://p76796/N35?timeout=0&spaceoption=true&unicode=yes";
	// private static String user = "SAPN35DB";
	// private static String password = "abc123";

	// private static String driver = "com.sap.dbtech.jdbc.DriverSapDB";
	// private static String url =
	// "jdbc:sapdb://p77292/A00?timeout=0&spaceoption=true&unicode=yes";
	// private static String user = "SAPA00DB";
	// private static String password = "abc123";

	// private static String driver = "com.sap.dbtech.jdbc.DriverSapDB";
	// private static String url =
	// "jdbc:sapdb://icodcetst/KM2?timeout=0&spaceoption=true&unicode=yes";
	// //private static String url = "jdbc:sapdb://pwdfm021/A00";
	// private static String user = "SAPKM2DB";
	// private static String password = "pssdb";

	// private static String driver =
	// "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	// private static String url = "jdbc:sqlserver://10.66.212.202:1433";
	// private static String user = "SAPLKGDB";
	// private static String password = "abcd1234";

}