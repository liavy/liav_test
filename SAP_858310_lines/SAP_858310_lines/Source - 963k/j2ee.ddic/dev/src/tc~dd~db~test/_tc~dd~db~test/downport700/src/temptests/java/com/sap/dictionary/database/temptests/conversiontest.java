package com.sap.dictionary.database.temptests;  

import java.io.BufferedReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
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
public class ConversionTest {
	private static int SAP = 1;
	private static int MSS = 0;
	private static int ORA = 0;
	private static int DB6 = 0;
	private static int DB4 = 0;
	private static int DB2 = 0;	
	private static int numberOfRecords = 200;
	private static int BATCH_PACK_SIZE = 100;
	private static int SEVERITY = Severity.INFO;
	private static String originSdaPath = "C:\\temp\\buildTest\\cnvorigin.sda";
	private static String targetSdaPath = "C:\\temp\\buildTest\\cnvtarget.sda";
	//private static String logPath = "C:\\temp\\buildTest\\cnvlog.txt";
	private static final Location loc = Location.getLocation(
			"com.sap.dictionary.database");
	private static final Category cat = Category.getCategory(
			Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	public static PrintStream out = null;
	public static Connection con = null;
	public static DbFactory factory = null;
	public static DbModificationController controller = null;
	public static final String[] SUFFIXES = {".gdbtable",".gdbview"};
	public static final String[] objectNames = getObjectNames();


	public static String[] getObjectNames() {
		ArrayList a = new ArrayList();
		ArchiveReader ar = new ArchiveReader(targetSdaPath,SUFFIXES,true);
		ArchiveEntry entry = null;
		String name = null;
		int i = 0;
		while ((entry = ar.getNextEntry()) != null) {
			name = entry.getName();
			a.add(i++,name.substring(name.lastIndexOf('/') + 1,name.indexOf(".")));			
		}
		return (String[])a.toArray(new String[0]);
		
	}
	
	public static void fillOriginTables() {
		try {
			DbRuntimeObjects ro = DbRuntimeObjects.getInstance(factory);
			for (int i = 0; i < objectNames.length; i++) {
				if (ro.getType(objectNames[i]).equalsIgnoreCase("T")) {
					DbTable table = factory.makeTable();
					table.setCommonContentViaXml(ro.get(objectNames[i]));
					VeriTools.fillTable(con,table,numberOfRecords,BATCH_PACK_SIZE);
				}
			}
		} catch (JddException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	public static boolean check() {
		boolean result = true;
		Statement stmt = null; 
		try {
			DbRuntimeObjects ro = DbRuntimeObjects.getInstance(factory);
			for (int i = 0; i < objectNames.length; i++) {
				//stmt = NativeSQLAccess.createNativeStatement(con);
				stmt = con.createStatement();
				ResultSet selset = stmt.executeQuery("SELECT * FROM \"" +
						objectNames[i] + "\"");
				if (!selset.next()) {
					System.out.println("<<<<ERROR: " + objectNames[i] + " lost data");
					result = false;
				} 
				stmt.close();
				String accessFlag = ro.getAccess(objectNames[i]);
				if (!accessFlag.equals("*") && !accessFlag.equals(" ")) {
					System.out.println("<<<<ERROR: " + objectNames[i] + " access flag = "
							+ accessFlag);
					result = false;	
				}
				if (ro.getType(objectNames[i]).equalsIgnoreCase("T")) {
					DbTable xmltarget = factory.makeTable();
					xmltarget.setCommonContentViaXml(ro.get(objectNames[i]));
					DbTable dbstarget = factory.makeTable(objectNames[i]);
					dbstarget .setCommonContentViaDb(factory);
					DbTableDifference diff = xmltarget.compareTo(dbstarget);
					if (diff != null) {
						System.out.println("<<<<ERROR: " + objectNames[i] + 
								" differences between runtimeObjects and database");
						result = false;							
					}	
				}
			}
			return result;
		} catch (JddException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public static void main(String[] args) {
		DbDataTransfer.BATCH_PACK_SIZE = BATCH_PACK_SIZE;
		out = System.out;
		DataSource ds;
		Connection con;
		Logger.setLoggingConfiguration("default");
		loc.setEffectiveSeverity(SEVERITY);
		cat.setEffectiveSeverity(Severity.INFO);
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
			reset();
			deploy();
			result &= check();	
//			reset();
//			deploy(DbTableConverter.States.CHECK_TARGET);
//			deploy();
//			result &= check();
//			reset();
//			deploy(DbTableConverter.States.LOCK_ORIGIN);
//			deploy();
//			result &= check();
//			reset();
//			deploy(DbTableConverter.States.CREATE_TARGET);
//			deploy();
//			result &= check();
//			reset();
//			deploy(DbTableConverter.States.TRANSFER_DATA);
//			deploy();
//			result &= check();
//			reset();
//			deploy(DbTableConverter.States.SAVE_DEPENDENT_VIEWS);
//			deploy();
//			result &= check();
//			reset();
//			deploy(DbTableConverter.States.DELETE_DEPENDENT_VIEWS);
//			deploy();
//			result &= check();
//			reset();
//			deploy(DbTableConverter.States.DELETE_ORIGIN);
//			deploy();
//			result &= check();
//			reset();
//			deploy(DbTableConverter.States.RENAME_TEMP_TARGET);
//			deploy();
//			result &= check();
//			reset();
//			deploy(DbTableConverter.States.CREATE_TARGET_INDEXES);
//			deploy();
//			result &= check();
//			reset();			
		}	
		return result;
	}
	
	public static void reset() {
		factory.getEnvironment().setConversionInterruptPoint(0);
		controller = new DbModificationController(factory);
		controller.delete(targetSdaPath);
		controller = new DbModificationController(factory);
		controller.distribute(originSdaPath);
		fillOriginTables();
	}
	
	public static void deploy() {
		factory.getEnvironment().setConversionInterruptPoint(0);
		controller = new DbModificationController(factory);
		controller.distribute(targetSdaPath);
	}
	
	public static void deploy(int interruptPoint) {
		factory.getEnvironment().setConversionInterruptPoint(interruptPoint);
		controller = new DbModificationController(factory);
		controller.distribute(targetSdaPath);
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
