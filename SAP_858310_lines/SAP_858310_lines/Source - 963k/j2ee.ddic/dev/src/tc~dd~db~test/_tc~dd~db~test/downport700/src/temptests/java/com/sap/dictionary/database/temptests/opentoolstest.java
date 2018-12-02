package com.sap.dictionary.database.temptests;

import java.io.*;

import com.sap.dictionary.database.dbs.*;
import com.sap.dictionary.database.dbs.opentools.DbTableOpenTools;

import java.sql.*;

import com.sap.sql.jdbc.internal.SAPDataSource;
import javax.sql.*;

import java.util.*;

import com.sap.tc.logging.*;
import java.sql.Timestamp;

/**
 * @author d003550
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */
public class OpenToolsTest implements DbsConstants {
	private static boolean deleteAfterCreate = false;
	private static String tablename = "TMP_OPENSQL1";
//	private static String filePath =
//	"C:\\eclipseWorkspaceJDT30OLD\\ddictest\\bin\\TMP_OPCHECK.gdbtable";
	//private static String filePath = "C:\\eclipseProjects2\\test1\\test1.sda";
	private static String filePath = "C:\\temp\\buildTest\\TMP_OPENSQL1.gdbtable";
	//private static String filePath = "C:\\temp\\buildTest\\com.sap.dqe.dbschema.sda";
	private static String logPath = "C:\\temp\\buildTest\\delpoylog.txt";
	
	private static String testDbsName = "sap"; //"ora";
	private static String connectionKind = "OPEN";
//	private static String driver = "com.sap.nwmss.jdbc.sqlserver.SQLServerDriver";
//	private static String url = "jdbc:nwmss:sqlserver://10.55.81.164:1433;databasename=E36";
//	private static String user = "SAPE36DB";
//	private static String password = "abcd1234";
	private static String driver = "oracle.jdbc.OracleDriver";
	private static String url = "jdbc:oracle:thin:@isi005:1527:N4S";
	private static String user = "SAPXX3DB";
	private static String password = "isi005xx";

	private static Connection con = null;
	private static DbFactory factory = null;

	OpenToolsTest() {
	}

	public static void main(String[] argv) {
//		Location loc = Location.getLocation("com.sap.dictionary.database");
//		loc.setEffectiveSeverity(com.sap.tc.logging.Severity.INFO);
//		loc.setResourceBundleName("com.sap.dictionary.database.dbs.messages.messages");
//		loc.addLog(new FileLog("C:/Users/Temp/test.txt",new
//		 DbTraceFormatter()));
//		loc.addLog(new ConsoleLog(new DbTraceFormatter()));
//		loc.infoT("SDA-Deployment");

		try {
			Connection con = null;
			if (testDbsName != null && testDbsName.trim().length() != 0)
				con = ConnectionService.getConnection(testDbsName);
			else
				con = ConnectionService.getConnection(driver,url,user,password);
//			Class.forName("com.sap.sql.jdbc.common.CommonDriver"); 
//			//get connetction
//			 con =
//				java.sql.DriverManager.getConnection(
//						"jdbc:sap:sapdb://PWDFM021/A00?spaceoption=true&unicode=yes",
//						"SAPA00DB",
//						"sapsap"); 
			factory = new DbFactory(con);
			DbTableOpenTools openTools = new DbTableOpenTools(con);
//			openTools.switchOnTrace();
			//openTools.createTable(tablename,new File(filePath));
			openTools.createTable(tablename,new File(filePath));
			openTools.createTable(tablename, new FileInputStream(new File(filePath)),
					true,true);
//			openTools.switchOffTrace();
			System.out.println("****************");
//			System.out.println(openTools.getInfoText());

			if (deleteAfterCreate) {
				deleteCreatedObject();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void deleteCreatedObject() {
		try {
			DbTable table = factory.makeTable(tablename);
			table.setCommonContentViaDb(factory);
			DbObjectSqlStatements stmts = table.getDdlStatementsForDrop();
			stmts.execute(factory);			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JddException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}