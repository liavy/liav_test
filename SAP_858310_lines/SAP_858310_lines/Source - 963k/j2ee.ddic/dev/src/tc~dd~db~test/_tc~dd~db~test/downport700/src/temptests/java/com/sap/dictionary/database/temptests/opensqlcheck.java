package com.sap.dictionary.database.temptests;

import java.io.*;

import com.sap.dictionary.database.catalog.XmlCatalogReader;
import com.sap.dictionary.database.dbs.*;
import com.sap.dictionary.database.veris.VeriTools;

import java.sql.*;

import com.sap.sql.catalog.Column;
import com.sap.sql.catalog.ColumnIterator;
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
public class OpenSqlCheck implements DbsConstants {
//	public static final String tabname = "BC_MSG_LOCK";
	public static final String tabname = "BC_DDDBDP";
//	public static final String tabname = "BC_DDDBTABLERT";
	public static final int linesToPrint = -1;
	public static final int columnsToPrint = 3;
	public static final int LOG = 1; //no log = 0;console = 1;file = 2; both = 3
	private static boolean testDbs = true;
	private static int SAP = 1;
	private static int MSS = 0;
	private static int ORA = 0;
	private static int DB6 = 0;
	private static int DB4 = 0;
	private static int DB2 = 0;
	private static int SEVERITY = Severity.INFO;
	private static final String LOGDIR = "C:\\logs\\database dev\\";
	
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
	
	private static String connectionKind = "";
	//private static String connectionKind = "OPEN";
	
//	private static String driver = "com.ibm.db2.jcc.DB2Driver";
//	private final static String url = "jdbc:db2://isi061:5901/D3D:deferPrepares=false;fullyMaterializeLobData=false;"; 
//  //private final static String url = "jdbc:db2://lsi230:5901/QD1:fullyMaterializeLobData=false;";
//  private final static String user = "SAPD3DDB";
//  private final static String password = "empass12";

	private static String driver = "oracle.jdbc.OracleDriver";
	private static String url = "jdbc:oracle:thin:@pwdf6156:1527:PKB";
	private static String user = "SAPPKBSH";
	private static String password = "abcd1234";


	
//	private static String driver = "oracle.jdbc.OracleDriver";
//	private static String url = "jdbc:oracle:thin:@hwi045:1527:QO1";
//	private static String user = "SAPSR3DB";
//	private static String password = "empass12";
	
//	private static String driver = "com.sap.dbtech.jdbc.DriverSapDB";
//	private static String url = "jdbc:sapdb://usi078/QE1?timeout=0&spaceoption=true&unicode=true";
//	//private static String url = "jdbc:sapdb://pwdfm021/A00";
//	private static String user = "SAPQE1DB";
//	private static String password = "sapqe1d1";
	
//	private static String driver = "com.sap.dbtech.jdbc.DriverSapDB";
//	private static String url =
//		//"jdbc:sapdb://p76796/N35?timeout=0&spaceoption=true&unicode=yes&trace=c://temp/MaxDBJDBCTrace.prt";
//		"jdbc:sapdb://p76796/N35?timeout=0&spaceoption=true&unicode=yes";
//	private static String user = "SAPN35DB";
//	private static String password = "abc123";
	
//	private static String driver = "com.sap.dbtech.jdbc.DriverSapDB";
//	private static String url =
//		"jdbc:sapdb://p77292/A00?timeout=0&spaceoption=true&unicode=yes";
//	private static String user = "SAPA00DB";
//	private static String password = "abc123";

	
	private static DbFactory factory = null;
	
	public static final SimpleDateFormat DATE_FORMAT = 
		new SimpleDateFormat("ddMMyyyy_kkmmss");
	private static final Location loc = Location.getLocation(
		"com.sap.dictionary.database");
	private static final Category cat = Category.getCategory(
			Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	public static PrintStream out = null;

	OpenSqlCheck() {
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
			ArrayList<Connection> cnns = null;
			try {
				cnns = VeriTools.getTestConnections(SAP,MSS,ORA,DB6,DB4,DB2);
				for (int i = 0; i < cnns.size(); i++) {
					out.println(dbnames[i]);
					exec(cnns.get(i));	      
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
		read(con);
	}
	
	public static void read(Connection con) throws Exception{
		XmlCatalogReader reader = new XmlCatalogReader(con);
		out.println(reader.getTable(tabname).getName());
		ColumnIterator ci = reader.getTable(tabname).getColumns();
		ArrayList<String> cols = new ArrayList<String>();
		int i = 0;
		while (ci.hasNext()) {
			i++;
			Column col = (Column) ci.next();
			cols.add(col.getName());
			System.out.println(" " + i + " " + col.getName() + " " + col.getTypeName());			
		}
		DbFactory factory = new DbFactory(con);
		DbTools tools = factory.makeTools();
		if (!(tools.tableExistsOnDb(tabname)))
			out.println("***********TABLE DOES NOT EXIST************");
		DbTable tabViaDb = factory.makeTable(tabname);
		if (!(tabViaDb.existsData()))
			out.println("***********TABLE DOES NOT HAVE DATA************");
		ResultSet selSet = null;
		String selTemplate = "SELECT * FROM \"" + tabname + "\"";
		PreparedStatement selStatement = con.prepareStatement(selTemplate);
		//selStatement.setFetchSize(100);
		selSet = selStatement.executeQuery();
		String line = "";
		i = 0;
		while (selSet.next()) {
			i++;
			if (i == linesToPrint + 1)
				break;
			int colLimit = cols.size();
			if (colLimit > columnsToPrint)
				colLimit = columnsToPrint;
			line = "";
			for (int j = 0; j < colLimit; j++) {
				line = line + selSet.getObject(cols.get(j)) + " ";	      
      }
			out.println(line);
		}
	}
	

}