package com.sap.dictionary.database.temptests;

import java.io.*;

import com.sap.dictionary.database.catalog.XmlCatalogReader;
import com.sap.dictionary.database.dbs.*;

import java.sql.*;

import com.sap.sql.NativeSQLAccess;
import com.sap.sql.catalog.Column;
import com.sap.sql.catalog.ColumnIterator;
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
public class CatalogCheck implements DbsConstants {
	private static String logPath = "C:\\temp\\buildTest\\delpoylog.txt";
	
	private static String testDbsName = null;
	//private static String testDbsName = "mss";
	
	private static String connectionKind = "";
	//private static String connectionKind = "OPEN";
	
//	private static String driver = "oracle.jdbc.OracleDriver";
//	private static String url = "jdbc:oracle:thin:@us4028.wdf.sap.corp:50077:E00";
//	private static String user = "SAPE00DB";
//	private static String password = "pssdb";
	
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
	
	private static String driver = "com.ibm.db2.jcc.DB2Driver";
	private static String url = "jdbc:db2://ihsapbc:9520/DDFFT52:keepDynamic=yes;currentPackageSet=SAPDRDA;currentSQLID=SAPT52DB;fullyMaterializeLobData=0;";
	private static String user = "SAPT52DB";
	private static String password = "db2conne";		

	private static Connection con = null;
	

	CatalogCheck() {
	}

	public static void main(String[] argv) {
		//Location cat = Location.getLocation("com.sap.dictionary.database");
		//cat.setEffectiveSeverity(com.sap.tc.logging.Severity.ALL);
		//cat.addLog(new FileLog("C:/Users/Temp/test.txt",new
		// TraceFormatter()));
		//cat.addLog(new ConsoleLog(new TraceFormatter()));
		//cat.infoT("SDA-Deployment");

		try {
			con = null;
			if (testDbsName != null && testDbsName.trim().length() != 0)
				con = ConnectionService.getConnection(testDbsName);
			else
				con = ConnectionService.getConnection(driver,url,user,password);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		XmlCatalogReader reader = new XmlCatalogReader(con);
		try {
			testRead(con);
		} catch (Exception e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		try {
			System.out.println(reader.getTable("BC_DDDBTABLERT").getName());
			//System.out.println(reader.getTable("HUGO").getName());
			System.out.println(reader.getTable("EP_LOCK_ENTRIES").getName());
			ColumnIterator ci = reader.getTable("EP_LOCK_ENTRIES").getColumns();
			while (ci.hasNext()) {
				Column col = (Column) ci.next();
				System.out.println(col.getName());
				System.out.println(col.getTypeName());
				
			}
			String statement = "SELECT COUNT(*) FROM \"EP_LOCK_ENTRIES\"";	
      
		DbFactory factory = null;
		try {
			factory = new DbFactory(con);
		} catch (JddException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
				PreparedStatement statementObject = 
  					 NativeSQLAccess.prepareNativeStatement(factory.getConnection(),statement);	  
  	    ResultSet result = statementObject.executeQuery();
  	    
  	    result.close(); 
  	    statementObject.close();
			//System.out.println(reader.getTable("J2EE_CONFIGCOPY").getName());
			//System.out.println(reader.getTable("J2EE_CONFIGCOPY1").getName());
			con.commit();
			//System.out.println(reader.getTable("J2EE_CONFIGCOPY2").getName());
			//System.out.println(reader.getTable("J2EE_CONFIGCOPY3").getName());
			//System.out.println(reader.getTable("J2EE_CONFIGCOPY4").getName());
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private static void testRead(Connection con) throws Exception {
		con.setAutoCommit(false);
		//con.setTransactionIsolation(2);
		
		String tab = "EP_LOCK_ENTRIES";
		
		String str = "SELECT COUNT(*) FROM " + tab;
		PreparedStatement pstmt = con.prepareStatement(str);
		ResultSet rs = pstmt.executeQuery();
		int pos = 1;
		rs.next();
		int count = rs.getInt(pos);
		System.out.println(count);
		rs.close();
		pstmt.close();
		str = "SELECT * FROM " + tab;
		pstmt = con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pos = rs.findColumn("RESOURCE_ID");
		for (int i = 1; i <= count; i++) {
			rs.next();
			System.out.println(rs.getString(pos));			
		}
	}


}