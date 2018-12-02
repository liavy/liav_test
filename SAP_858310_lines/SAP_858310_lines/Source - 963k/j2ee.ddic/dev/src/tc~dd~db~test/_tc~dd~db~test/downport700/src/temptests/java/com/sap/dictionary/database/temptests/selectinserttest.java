package com.sap.dictionary.database.temptests;  

import java.io.BufferedReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

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
public class SelectInsertTest {
	private static int SAP = 0;
	private static int MSS = 0;
	private static int ORA = 0;
	private static int DB6 = 0;
	private static int DB4 = 1;
	private static int DB2 = 0;
	private static String originName = "J2EE_CONFIGCOPY2";
	private static String targetName = "J2EE_CONFIGCOPY3";
	private static int numberOfRecords = 200;
	private static int checkFrom = 199;
	private static int[] fillTarget = {};
	private static int BATCH_PACK_SIZE = 0;
	private static int rsNavigation = 0;
	private static int deleteOrigin = 0;
	private static int deleteTarget = 1;
	private static int deploy =       1;
	private static int fillOrigin =   0;
	private static int checkOrigin =  0;
	private static int copyToTarget = 1;
	private static int checkTarget =  0;
	private static int SEVERITY = Severity.ERROR;
	private static String sdaPath = "C:\\temp\\buildTest\\J2EE_CONFIGCOPY3.zip";
	//private static String sdaPath = "C:\\eclipseProjects30\\copytest\\copytest.sda";
	//private static String logPath = "C:\\temp\\buildTest\\delpoylog.txt";
	private static final Location loc = Location.getLocation(
			"com.sap.dictionary.database");
	private static final Category cat = Category.getCategory(
			Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	public static PrintStream out = null;
	public static Connection con = null;
	public static DbFactory factory = null;
	public static DbModificationController controller = null;


	public static void fillTarget() {
		
			try {
				DbRuntimeObjects ro = DbRuntimeObjects.getInstance(factory);
				DbTable target = factory.makeTable();
				target.setCommonContentViaXml(ro.get(targetName));
				VeriTools.fillTable(con,target,fillTarget,BATCH_PACK_SIZE);
			} catch (JddException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
	}
	
	public static void fillOrigin() {
		try {
			DbRuntimeObjects ro = DbRuntimeObjects.getInstance(factory);
			DbTable origin = factory.makeTable();
			origin.setCommonContentViaXml(ro.get(originName));
			VeriTools.fillTable(con,origin,numberOfRecords,BATCH_PACK_SIZE);
		} catch (JddException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		VeriTools.fillTable(con,originName,numberOfRecords,BATCH_PACK_SIZE);
//		PreparedStatement insPstmt = null;
//		String templ = "INSERT INTO \"" + originName + "\" VALUES (?, ?, ?, ?)";
//		try {
//			insPstmt = con.prepareStatement(templ);
//			insPstmt.setString(2,"12345");
//			insPstmt.setString(3,"12345");
//			int m = 0;
//			for (int i = 0; i < numberOfRecords; i++) {
//				insPstmt.setInt(1,i);
//				insPstmt.setString(4,"1234567890");
//				insPstmt.addBatch();
//				m++;
//				if (m > BATCH_PACK_SIZE) {
//					insPstmt.executeBatch();
//					con.commit();
//					m = 0;
//				}
//			}
//			if (m > 0) {
//				insPstmt.executeBatch();
//				con.commit();
//			}
//			insPstmt.close();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
	
	public static void checkOrigin() {
		Statement stmt = null;
		try {
			if (rsNavigation != 0) 
				stmt = NativeSQLAccess.createNativeStatement(con,
						ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			else
				stmt = NativeSQLAccess.createNativeStatement(con);
			ResultSet result = stmt.executeQuery("SELECT * FROM \"" +
					originName + "\"");
			if (rsNavigation != 0)
				result.absolute(checkFrom);
			else
				for (int i = 0; i < checkFrom; i++) {
					result.next();
				}
			while (result.next()) {
				System.out.println(
						result.getInt(1) + "#"
					+ result.getString(2) + "#"
					+ result.getString(3) + "#"
//					+ result.getTimestamp(56) + "#"
//					+ new java.sql.Timestamp(result.getTime(57).getTime()) + "#"
//					+ result.getTimestamp(58) + "#"
//					+ getString(new BufferedReader(result.getCharacterStream(4))) + "#"
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
	
	public static void checkTarget() {
		Statement stmt = null;
		try {
			if (rsNavigation != 0) 
				stmt = NativeSQLAccess.createNativeStatement(con,
						ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			else
				stmt = NativeSQLAccess.createNativeStatement(con);
			ResultSet result = stmt.executeQuery("SELECT * FROM \"" +
					targetName + "\"");
			if (rsNavigation != 0)
				result.absolute(checkFrom);
			else
				for (int i = 0; i < checkFrom; i++) {
					result.next();
				}
			while (result.next()) {
				System.out.println(
							result.getInt(1) + "*"
						+ result.getString(2) + "*"
						+ result.getString(3) + "*"
//						+ getString(new BufferedReader(result.getCharacterStream(4))) + "*"
//						+ result.getString(5) + "*"  
//						+ result.getString(6) + "*"
//						+ result.getString(7) + "*"
//						+ result.getString(56) + "*"
//						+ result.getString(57) + "*"
//						+ result.getString(58) + "*"
						);
			}
			result.close();
			stmt.close();			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void copyToTarget() {
		try {
			DbRuntimeObjects ro = DbRuntimeObjects.getInstance(factory);
			DbTable origin = factory.makeTable();
			DbTable target = factory.makeTable();
			origin.setCommonContentViaXml(ro.get(originName));
			target.setCommonContentViaXml(ro.get(targetName));
			DbDataTransfer copier = new DbDataTransfer(origin,target);
			
//			Connection con1 = ConnectionService.getConnection(
//					"com.sap.dbtech.jdbc.DriverSapDB",
//					"jdbc:sapdb://WDFD00130360A/D70?spaceoption=true",
//					"SAPD70DB",
//					"abc123");
//			Connection con2 = ConnectionService.getConnection("sap");
//			DbDataTransfer copier = new DbDataTransfer(origin,con1, target,con2);
			
			loc.setEffectiveSeverity(Severity.INFO);
			copier.transfer();
			loc.setEffectiveSeverity(SEVERITY);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		DbDataTransfer.BATCH_PACK_SIZE = BATCH_PACK_SIZE;
		out = System.out;
		DataSource ds;
		Connection con;
		//LoggingConfiguration.setProperty("default","log[file].pattern",logPath);
		Logger.setLoggingConfiguration("default");
		loc.setEffectiveSeverity(SEVERITY);
		cat.setEffectiveSeverity(Severity.FATAL);
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
		boolean isResultOk = true;
		long t1,t2,t3,t4,t5;
		
		System.out.println("transfer " + numberOfRecords + " rows from " +
				originName + " to " + targetName);
		System.out.println("batch package size = " + BATCH_PACK_SIZE);
		System.out.print("duplicate keys: ");
		for (int i = 0; i < fillTarget.length; i++) {
			System.out.print(fillTarget[i] + " ");
		} 
		for (int i = 0; i < cnns.size(); i++) {
			con = (Connection) cnns.get(i);
			try {
				//System.out.println(con.getMetaData().getDatabaseProductName());
				con.setAutoCommit(false);
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
			DbRuntimeObjects ro = DbRuntimeObjects.getInstance(factory);
			ResultSet rs = ro.getRows(new String[]{"ACCESS"},new String[]{"*"});
			ro.putRow(new Object[]{"AAA1",new Long("22"),"T","*","12345"});
			ro.putRow(new Object[]{"AAA2",new Long("22"),"T","*","12345"});
			ro.putRow(new Object[]{"AAA3",new Long("22"),"T","*","12345"});
			ro.putRow(new Object[]{"AAA4",new Long("22"),"T","*","12345"});
			ro.putRow(new Object[]{"AAA5",new Long("22"),"T","*","12345"});
//			try {
//				con.commit();
//			} catch (SQLException e5) {
//				// TODO Auto-generated catch block
//				e5.printStackTrace();
//			}
			try {
				while(rs.next()) {
					System.out.println(rs.getString("NAME"));
				}
			} catch (SQLException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
			try {
				con.commit();
			} catch (SQLException e4) {
				// TODO Auto-generated catch block
				e4.printStackTrace();
			}
			ro.removeRow(new Object[]{"AAA1"});
			ro.removeRow(new Object[]{"AAA2"});
			ro.removeRow(new Object[]{"AAA3"});
			ro.removeRow(new Object[]{"AAA4"});
			ro.removeRow(new Object[]{"AAA5"});
			ro.removeRow(new Object[]{"AAA6"});
			ro.removeRow(new Object[]{"AAA7"});
			ro.removeRow(new Object[]{"AAA8"});
			ro.removeRow(new Object[]{"AAA9"});
			ro.removeRow(new Object[]{"AAA10"});
			try {
				con.commit();
			} catch (SQLException e4) {
				// TODO Auto-generated catch block
				e4.printStackTrace();
			}		
		}	
		return isResultOk;
	}
	
	public static void delete(String name) {
		controller = new DbModificationController(factory);
		//controller.delete(new String[]{name + ".gdbtable"});
		controller.delete(sdaPath);
	}
	
	public static void deploy() {
		controller = new DbModificationController(factory);
		controller.distribute(sdaPath);
	}
	
	private static String getString(BufferedReader bufferedReader)
	 throws Exception{
	String s;
	StringBuffer buffer = new StringBuffer();
	while ((s = bufferedReader.readLine()) != null) {
		buffer.append(s);
	}	
	return buffer.toString();	
}
}
