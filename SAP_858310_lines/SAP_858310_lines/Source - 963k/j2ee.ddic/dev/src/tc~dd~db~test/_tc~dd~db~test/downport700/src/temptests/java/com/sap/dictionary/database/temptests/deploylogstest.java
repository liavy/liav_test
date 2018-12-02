package com.sap.dictionary.database.temptests;  

import java.io.BufferedReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import javax.sql.DataSource;

import com.sap.dictionary.database.dbs.*;
import com.sap.dictionary.database.veris.VeriTools;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import java.sql.*;

/**
 * @author d019347
 */
public class DeployLogsTest implements DbsConstants {
	private static int SAP = 1;
	private static int MSS = 0;
	private static int ORA = 0;
	private static int DB6 = 0;
	private static int DB4 = 0;
	private static int DB2 = 0;
	private static int CHECK_ONLY = 0;
	private static int READ_NATIVE = 1;	
	private static int SEVERITY = Severity.INFO;
	private static final Location locRoot = Location.getLocation(
			"com.sap.dictionary.database");
	private static final Location loc = Location.getLocation(
			DeployLogsTest.class);
	private static final Category cat = Category.getCategory(
			Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	public static PrintStream out = null;
	public static Connection con = null;
	public static DbFactory factory = null;
	public static DbModificationController controller = null;
	public static final String[] SUFFIXES = {".gdbtable",".gdbview"};
	public static final String EOL = System.getProperty("line.separator");
	
	public static void main(String[] args) {
		out = System.out;
		DataSource ds;
		Connection con;
		Logger.setLoggingConfiguration("default");
		locRoot.setEffectiveSeverity(Severity.FATAL);
		cat.setEffectiveSeverity(SEVERITY);
		try {
			ArrayList cnns = VeriTools.getTestConnections(SAP,MSS,ORA,DB6,DB4,DB2);                                                    
      if (exec(cnns, false))
        System.out.println("!!!!!!!!!!!!VERI  SUCCESS !!!!!!!!!!!!");
      else
        System.out.println("????????????VERI  ERRORS  ????????????");      
    }
    catch (Exception ex) {
      	ex.printStackTrace();
    }
	}
	
	public static boolean exec(ArrayList cnns) {
		return exec(cnns,true);
	}
	
	public static boolean exec(ArrayList cnns, boolean breakIfError) {
		boolean result = true;
		long t1,t2,t3,t4,t5;
		
		for (int i = 0; i < cnns.size(); i++) {
			con = (Connection) cnns.get(i);
//			try {
//				con.setAutoCommit(true);
//			} catch (SQLException e2) {
//				// TODO Auto-generated catch block
//				e2.printStackTrace();
//			}
			try {
				System.out.println(" ");
				System.out.println("----------------------------------------------");
				System.out.println(con.getMetaData().getDatabaseProductName());
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				factory = new DbFactory(con);
			} catch (JddException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//checkLogs();
			if (CHECK_ONLY == 0) {
				DbDeployLogs.getInstance(factory);
				writeLog("A","A",false,1);
				writeLog("A","A",false,2);
				writeLog("A","A",true,3);
//				writeLog("A","A",false,4);
//				writeLog("A","A",false,5);
//				writeLog("A","A",false,6);
//				writeLog("A","A",false,7);
//				try {
//					con.commit();
//				} catch (SQLException e2) {
//					// TODO Auto-generated catch block
//					e2.printStackTrace();
//				}
			}
			System.out.println("00000000000000000000000000000");
			//checkLogs();
			System.out.println("11111111111111111111111111111");
			checkLogs();
//			try {
//				con.commit();
//			} catch (SQLException e2) {
//				// TODO Auto-generated catch block
//				e2.printStackTrace();
//			}
			
		}
		return true;
			
	}
	
	public static void writeLog(String name,String phase,boolean errors,int i) {
		DbDeployLogs.openObjLog(name,"T",phase,0);
		cat.warning(loc,TABLE_FILE_NOT_FOUND,new Object[] {new Integer(i)});
		if (errors)
			cat.error(loc,TABLE_FILE_NOT_FOUND,new Object[] {new Integer(i+1)});
		else
			cat.info(loc,TABLE_FILE_NOT_FOUND,new Object[] {new Integer(i+1)});
		cat.info(loc,TABLE_FILE_NOT_FOUND,new Object[] {new Integer(i+2)});
		DbDeployLogs.closeObjLog(name,Action.CREATE);
	}
	
	public static void checkLogs() {
		Statement stmt = null;
		try {
			if (READ_NATIVE != 0) 
				stmt = NativeSQLAccess.createNativeStatement(con);
			else
				stmt = NativeSQLAccess.createNativeStatement(con);
			ResultSet result = stmt.executeQuery("SELECT * FROM \"" +
					"BC_DDDBDP" + "\" ORDER BY NAME, PHASE, ACTION, ERRORS, TIMESTMP");
			while (result.next()) {
				for (int i = 0; i < 4; i++) {
					System.out.println(" ");
				}
				System.out.println(
						result.getString("NAME") + "#"
					+ result.getString("PHASE") + "#"
					+ result.getString("ACTION") + "#"
					+ result.getString("ERRORS") + "#"
					+ new java.util.Date(result.getLong("TIMESTMP")) + "#"
					+ getString(new BufferedReader(result.getCharacterStream("MESSAGES"))) + "#"
					); 
			}
//			result.beforeFirst();
			result.close();
			stmt.close();			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
	private static String getString(BufferedReader bufferedReader)
	 throws Exception{
	String s;
	StringBuffer buffer = new StringBuffer();
	while ((s = bufferedReader.readLine()) != null) {
		buffer.append(EOL);
		buffer.append(s);
	}	
	return buffer.toString();	
}
}
