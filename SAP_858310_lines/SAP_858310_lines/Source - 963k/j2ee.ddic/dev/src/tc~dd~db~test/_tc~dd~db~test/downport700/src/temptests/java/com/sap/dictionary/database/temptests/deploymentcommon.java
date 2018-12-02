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
public class DeploymentCommon implements DbsConstants {
	private static int analyseOnly = 0;
	private static int fillObjects = 0; //for first deployment only
	private static int deleteAfterDeploy = 0;
	private static String[][] preActions = {{"someTableName","CONVERT"}};
	public static final int LOG = 3; //no log = 0;console = 1;file = 2; both = 3
	
//	private static String filesRoot = "C:\\temp\\buildTest\\WCR_WEBCONTENTSTAT.gdbtable";
	private static String filesRoot = "C:\\temp\\buildTest\\test1.sda";
//	private static String filesRoot = "C:\\temp\\buildTest\\dtrviews";
//	private static String filesRoot = "C:\\Documents and Settings\\d019347\\workspace.jdi\\LocalDevelopment\\DCs\\demo.sap.com\\tables1\\_comp\\gen\\default\\deploy\\demo.sap.com~tables1.sda";
	private static String[] filesEnds = null;
	
//	private static String filesRoot = "C:\\Documents and Settings\\d019347\\workspace.jdi\\LocalDevelopment\\DCs\\demo.sap.com\\";
//	
//	private static String[] filesEnds = {"tables1\\_comp\\bin","tables2\\_comp\\bin"}; // for one or many deployments executed in rotation
	
	private static String[][] deployConfigSet = {{"acceptDataLoss","TMP_TABLEXXX1",""},{}};
	
	private static boolean testDbs = false;
	private static int SAP = 0;
	private static int MSS = 1;
	private static int ORA = 0;
	private static int DB6 = 0;
	private static int DB4 = 0;
	private static int DB2 = 0;
		
//	private static String driver = "com.sap.nwmss.jdbc.sqlserver.SQLServerDriver";
//	private static String url = "jdbc:nwmss:sqlserver://10.66.212.202:1433";
//	private static String user = "SAPLKGDB";
//	private static String password = "abcd1234";
//	private static String driver = "com.sap.nwmss.jdbc.sqlserver.SQLServerDriver";
//	private static String url = "jdbc:nwmss:sqlserver://10.66.211.73:1433;databasename=W08";
//	private static String user = "SAPW08DB";
//	private static String password = "abc123";
//private static String driver = "com.sap.dbtech.jdbc.DriverSapDB";
//private static String url =
//	"jdbc:sapdb://bsw6511/EP1?timeout=0&spaceoption=true&unicode=yes";
//private static String user = "SAPEP1DB";
//private static String password = "abcd1234";

	private static String url =
		"jdbc:db2://ihsapdc:8321/DDFD8T0:keepDynamic=yes;currentPackageSet=SAPJH8KDDFD8T0;currentSQLID=SAPH8KDB;";
	private static String driver = "com.ibm.db2.jcc.DB2Driver";
	private static String user = "SAPH8KDB";
	private static String password = "sapr3adm";
//	private static String driver = "oracle.jdbc.OracleDriver";
//	private static String url = "jdbc:oracle:thin:@isi005:1527:N4S";
//	private static String user = "SAPXX3DB";
//	private static String password = "isi005xx";

	
// ***************************************************************************
	private static final String LOGDIR = "C:\\logs\\database dev\\";
	private static int SEVERITY = Severity.INFO;

	private static String[] SUFFIXES = {".gdbtable", ".gdbview"};
	
	private static DbFactory factory = null;
	
	public static final SimpleDateFormat DATE_FORMAT = 
		new SimpleDateFormat("ddMMyyyy_kkmmss");
	private static final Location loc = Location.getLocation(
		"com.sap.dictionary.database");
	private static final Category cat = Category.getCategory(
			Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	private static String filesPath = null;
	private static File[] files = null;
	private static File[] usedfiles = null;
	public static PrintStream out = null;

	DeploymentCommon() {
	}
	
	public static void main(String[] args) {
		out = System.out;
		DataSource ds;
		Connection con;
		//cat.infoT(loc,"777777777 ERROR 77777777777");
		//Logger.setLoggingConfiguration("default");
		loc.setResourceBundleName("com.sap.dictionary.database.dbs.messages.messages");         
    cat.setResourceBundleName("com.sap.dictionary.database.dbs.messages.messages");         
    loc.setClassLoader(DbTable.class.getClassLoader());
    cat.setClassLoader(DbTable.class.getClassLoader());
    if (LOG == 2 || LOG == 3) {
			FileLog fileLog = new FileLog(LOGDIR + DATE_FORMAT.format(new Date())
					+ ".xls");
			fileLog.setFormatter(new DbTraceFormatter());
			loc.addLog(fileLog);
		}
		if (LOG == 1 || LOG == 3) {
			ConsoleLog consoleLog = new ConsoleLog();
			consoleLog.setFormatter(new DbTraceFormatter());
			loc.addLog(consoleLog);
		}
		loc.setEffectiveSeverity(SEVERITY);
		cat.setEffectiveSeverity(Severity.FATAL);
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
				cnns = VeriTools.getTestConnections(SAP,MSS,ORA,DB6,DB4,DB2);
				for (int i = 0; i < cnns.size(); i++) {
					out.println(dbnames[i]);
					exec((Connection)cnns.get(i));	      
	      }
			}
			catch (Exception ex) {
      		ex.printStackTrace();
			}
		} else
	    try {
	      exec(ConnectionService.getConnection(driver,url,user,password));
      } catch (Exception e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      }
			
	}

	public static void exec(Connection con) throws Exception{
		//Location cat = Location.getLocation("com.sap.dictionary.database");
		//cat.setEffectiveSeverity(com.sap.tc.logging.Severity.ALL);
		//cat.addLog(new FileLog("C:/Users/Temp/test.txt",new
		// TraceFormatter()));
		//cat.addLog(new ConsoleLog(new TraceFormatter()));
		//cat.infoT("SDA-Deployment");
		HashMap alusedfiles = new HashMap(); //String,File
		DbModificationController controller = null;
		if (filesEnds == null || filesEnds.length == 0)
			filesEnds = new String[]{""}; 
		for (int i = 0; i < filesEnds.length; i++) {
			factory = new DbFactory(con);
			if (deployConfigSet != null && deployConfigSet.length != 0)
				factory.getEnvironment().setDeployConfig(DbDeployConfig.getInstance(
						deployConfigSet));
			filesPath = filesRoot + filesEnds[i];
			System.out.println("*** PATH = " + filesPath);
			controller = new DbModificationController(factory);
			controller.switchOnTrace(Severity.ERROR);
			
			File fl = new File(filesPath);
			if (fl.isDirectory()) {
				ArrayList alfiles = new ArrayList(); //File
				String[] list = fl.list();
				for (int j = 0; j < list.length; j++) {
					if (list[j].endsWith(".gdbtable") || list[j].endsWith(".gdbview")) {
						File f = new File(filesPath + "\\" + list[j]);
						alfiles.add(f);
						alusedfiles.put(list[j],f);
					}
				}
				files = (File[])alfiles.toArray(new File[] {});
				if (analyseOnly > 0) {
					controller.fillDeployObjects(files);
					controller.analyse();
				} else {
					if (filesEnds.length < 2 || i > 0)
						controller.distribute(files,preActions);
					else
						controller.distribute(files);
				}
			} else if (filesPath.endsWith(".gdbtable")
			    || filesPath.endsWith(".gdbview")) {
				File f = new File(filesPath);
				files = new File[] { f };
				alusedfiles.put(filesPath,f);
				if (analyseOnly > 0) {
					controller.fillDeployObjects(files);
					controller.analyse();
				} else {
					if (filesEnds.length < 2 || i > 0)
						controller.distribute(files,preActions);
					else
						controller.distribute(files);
				}
			} else {
				if (analyseOnly > 0) {
					controller.fillDeployObjects(filesPath);
					controller.analyse();
				} else {
					if (filesEnds.length < 2 || i > 0)
						controller.distribute(filesPath,preActions);
					else
						controller.distribute(filesPath);
				}
			}

			if (fillObjects > 0 && i == 0)
				fillObjects(con);
			
			if (analyseOnly == 0 && deleteAfterDeploy > 0 && i == filesEnds.length - 1) {
				usedfiles = (File[])alusedfiles.values().toArray(new File[] {});
				deleteDeployedObjects(controller);
			}
			
			if (analyseOnly == 0) {
				try {
					if (!con.getAutoCommit())
						con.commit();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		}
		controller.switchOffTrace();
		out.println(controller.getInfoText());
	}
	
	private static void fillObjects(Connection con) {
		if (files != null) {
			String name = null;
			for (int i = 0; i < files.length; i++) {
	      name = files[i].getName();
	      if (name.endsWith(".gdbview"))
					continue;
	      name = name.substring(name.lastIndexOf('/') + 1,name.indexOf("."));
	      try {
					DbRuntimeObjects ro = DbRuntimeObjects.getInstance(factory);
					DbTable tab = factory.makeTable(name);				
					tab.setCommonContentViaDb(factory);
					VeriTools.fillTable(con,tab,1,0);
				} catch (JddException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
      }
			return;
		}
		ArchiveReader ar = new ArchiveReader(filesPath, SUFFIXES, true);
		ArchiveEntry entry = null;
		String name = null;
		while ((entry = ar.getNextEntry()) != null) {
			name = entry.getName();
			if (name.endsWith(".gdbview"))
				continue;
			name = name.substring(0, name.indexOf('.'));
			try {
				DbRuntimeObjects ro = DbRuntimeObjects.getInstance(factory);
				DbTable tab = factory.makeTable(name);				
				tab.setCommonContentViaDb(factory);
				VeriTools.fillTable(con,tab,1,0);
			} catch (JddException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	private static void deleteDeployedObjects(DbModificationController controller) {
		if (files != null) {
			controller.getDeployTables().reset();
			controller.getDeployViews().reset();
			controller.fillDeployObjectsForDelete(usedfiles);
			controller.distribute();
		} else {
			controller.delete(filesPath);
		}
	}
	
//	private static String filesRoot = "C:\\eclipseProjects30\\tableDefinition1\\tableDefinition1.sda";
	
	//private static String sdaRoot = "C:\\temp\\buildTest\\alterWithViewTest";
	//private static String[] sdaEnds = {"1.sda","2.sda"};
//	private static String sdaRoot = "C:\\temp\\buildTest\\cgt";
//	private static String[] sdaEnds = {".zip"};
	
//private static String sdaPath = "C:\\eclipseProjects30\\tableDefinition1\\tableDefinition1.sda";
	//private static String sdaPath = "C:\\eclipseProjects30\\tableDefinition2\\tableDefinition2.sda";
	//private static String sdaPath = "C:\\temp\\buildTest\\alterWithViewTest2.sda";
	//private static String sdaPath = "C:\\temp\\buildTest\\G5EE2.zip";
	//private static String sdaPath = "C:\\eclipseProjects30\\TypeTest\\TypeTest.sda";
	//private static String sdaPath = "C:\\temp\\buildTest\\misc.sda";
	//private static String sdaPath = "C:\\temp\\buildTest\\BC_SLD_MP_INSTA.zip";
	//private static String sdaPath = "C:\\eclipseWorkspaceJDT30\\floattest\\floattest.sda";
	//private static String sdaPath = "C:\\eclipseProjects2\\test1\\test1.sda";
	//private static String sdaPath = "C:\\temp\\buildTest\\com.sap.xi.dbschema.ibrep.sda";
	//private static String sdaPath = "C:\\eclipseWorkspaceJDT30\\misc\\misc.sda";
	//private static String sdaPath = "C:\\eclipseWorkspaceJDT30\\misc\\misc2.zip";
	//private static String logPath = "C:\\temp\\buildTest\\delpoylog.txt";
	
	//private static String testDbsName = null;
	//private static String testDbsName = "ora";
	
	
	//private static String connectionKind = "OPEN";
	
//	private static String driver = "oracle.jdbc.OracleDriver";
//	private static String url = "jdbc:oracle:thin:@pwdf6156:1527:PKB";
//	private static String user = "SAPPKBSH";
//	private static String password = "abcd1234";
	
//	private static String driver = "com.ibm.db2.jcc.DB2Driver";
// //private final static String url = "jdbc:db2://lsi230:5901/QD1:fullyMaterializeLobData=false;";
//	 private final static String url = "jdbc:db2://lsi230:5901/QD1:deferPrepares=false;fullyMaterializeLobData=false;currentSchema=hugo;";
//  private final static String user = "SAPQD1DB";
//  private final static String password = "empass12";



	
//	private static String driver = "oracle.jdbc.OracleDriver";
//	private static String url = "jdbc:oracle:thin:@hwi045:1527:QO1";
//	private static String user = "SAPSR3DB";
//	private static String password = "empass12";
	
	
//private static String driver = "com.sap.dbtech.jdbc.DriverSapDB";
//private static String url =
//	//"jdbc:sapdb://p76796/N35?timeout=0&spaceoption=true&unicode=yes&trace=c://temp/MaxDBJDBCTrace.prt";
//	"jdbc:sapdb://p76796/N35?timeout=0&spaceoption=true&unicode=yes";
//private static String user = "SAPN35DB";
//private static String password = "abc123";

//private static String driver = "com.sap.dbtech.jdbc.DriverSapDB";
//private static String url =
//	"jdbc:sapdb://p77292/A00?timeout=0&spaceoption=true&unicode=yes";
//private static String user = "SAPA00DB";
//private static String password = "abc123";
	
//	private static String driver = "com.sap.dbtech.jdbc.DriverSapDB";
//	private static String url = "jdbc:sapdb://icodcetst/KM2?timeout=0&spaceoption=true&unicode=yes";
//	//private static String url = "jdbc:sapdb://pwdfm021/A00";
//	private static String user = "SAPKM2DB";
//	private static String password = "pssdb";	
	
//private static String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
//private static String url = "jdbc:sqlserver://10.66.212.202:1433";
//private static String user = "SAPLKGDB";
//private static String password = "abcd1234";

}