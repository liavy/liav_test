package com.sap.dictionary.database.temptests;

import java.io.*;

import com.sap.dictionary.database.dbs.*;

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
public class DeploymentCommonSp implements DbsConstants {
	private static boolean analyseOnly = false;
	private static boolean deleteAfterDeploy = true;
	//private static String sdaPath = "C:\\eclipseWorkspaceJDT30\\floattest\\floattest.sda";
	//private static String sdaPath = "C:\\eclipseProjects2\\test1\\test1.sda";
	//private static String sdaPath = "C:\\temp\\buildTest\\BC_SLD_MP_INSTA.zip";
	//private static String sdaPath = "C:\\temp\\buildTest\\com.sap.dqe.dbschema.sda";
	//private static String sdaPath = "C:\\temp\\buildTest\\XAPPS3.sda";
	//private static String sdaPath = "C:\\eclipseWorkspaceJDT30\\misc\\misc.sda";
	private static String sdaPath = "C:\\eclipseWorkspaceJDT30\\misc\\misc2.zip";
	private static String logPath = "C:\\temp\\buildTest\\delpoylog.txt";
	
	private static String testDbsName = "sap";
	//private static String testDbsName = "ora";
	//private static String testDbsName = "mss";
	
	private static String connectionKind = "";
	//private static String connectionKind = "OPEN";
	
//	private static String driver = "com.sap.dbtech.jdbc.DriverSapDB";
//	//private static String url = "jdbc:sapdb://pwdfm021/A00";
//	private static String url = "jdbc:sapdb://usi078/QE1?timeout=0&spaceoption=true&unicode=true";
//	private static String user = "SAPQE1DB";
//	private static String password = "sapqe1d1";
	
	private static String driver = "com.ibm.db2.jcc.DB2Driver";
	//private static String url = "jdbc:sapdb://pwdfm021/A00";
	private static String url = "jdbc:db2://lsi153:5912/QD1";
	private static String user = "SAPQD1DB";
	private static String password = "empass12";

	private static String SUFFIX = ".gdbtable";
	private static Connection con = null;
	private static DbFactory factory = null;
	private static DbModificationManager controller = null;

	DeploymentCommonSp() {
	}

	public static void main(String[] argv) {
		//Location cat = Location.getLocation("com.sap.dictionary.database");
		//cat.setEffectiveSeverity(com.sap.tc.logging.Severity.ALL);
		//cat.addLog(new FileLog("C:/Users/Temp/test.txt",new
		// TraceFormatter()));
		//cat.addLog(new ConsoleLog(new TraceFormatter()));
		//cat.infoT("SDA-Deployment");

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
			if (con.getAutoCommit())
				con.setAutoCommit(false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		controller = new DbModificationManager(factory);

		if (analyseOnly) {
		} else {
			controller.distribute(sdaPath,logPath);
		}

		if (!analyseOnly && deleteAfterDeploy) {
			deleteDeployedObjects();
		}
	}

	private static void deleteDeployedObjects() {
		try {
			ArchiveReader ar = new ArchiveReader(sdaPath, SUFFIX, true);
			ArchiveEntry entry = null;
			XmlExtractor extractor = null;
			XmlMap tableMap = null;
			DbObjectSqlStatements stmts = null;
			DbTable table = null;
			String name = null;
			while ((entry = ar.getNextEntry()) != null) {
				name = entry.getName();
				if (name.endsWith(".gdbtable")) {
					name = name.substring(0, name.indexOf("."));
					extractor = new XmlExtractor();
					tableMap = extractor.map(entry.getInputSource());
					table = factory.makeTable();
					table.setCommonContentViaXml(tableMap);
					stmts = table.getDdlStatementsForDrop();
					stmts.execute(factory);
					factory.getConnection().commit();
				} else {
					name = name.substring(0, name.indexOf("."));
				}
			}
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