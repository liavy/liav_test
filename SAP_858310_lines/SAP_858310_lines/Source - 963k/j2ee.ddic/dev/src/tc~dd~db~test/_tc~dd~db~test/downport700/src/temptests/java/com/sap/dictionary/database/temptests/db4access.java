package com.sap.dictionary.database.temptests;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import com.sap.dictionary.database.dbs.*;
import com.sap.sql.NativeSQLAccess;
import com.sap.sql.jdbc.internal.SAPDataSource;


public class Db4Access {
	
	/**
	 * Returns a direct, native or common connection depending on the 
	 * input string.
	 * @return - Connection
	 * @param conKind - Possible values are "VENDOR", "NATIVE", "OPEN"
	 */
	private static Connection getConnection(String kind) throws Exception {
	    Connection con = null;
        String url = "jdbc:as400://as0016";
        String user = "SAPBBEDB";
        String password = "SAPSAP";
        String driver = "com.ibm.as400.access.AS400JDBCDriver";
        String dsName = "";
        
        if (kind.equalsIgnoreCase("VENDOR")) {
        	kind = "vendorsql";
        } else if (kind.equalsIgnoreCase("NATIVE")) {
        	kind = "nativesql";
        } else if (kind.equalsIgnoreCase("OPEN")) {
        	kind = "opensql";
        } else {
        	System.out.println("Unknown connection type. - Return.");
        	return null;
        }
        	
        dsName =    kind + "&" 
                  + driver + "&" 
      	          + url + "&" 
                  + user + "&"
                  + password + "&";
	
	    //Get connection
	    try {
	        System.out.println("Connecting to " + dsName);
	        DataSource ds = SAPDataSource.getDataSource(dsName);
	        con = ds.getConnection();
	    }
	      catch (Exception ex) {
	      	ex.printStackTrace();
		}
		return con;
	}


	
	public static void main(String argv[]) throws Exception {
		//Connection con = getConnection("OPEN");
		Connection con = getConnection("NATIVE");
	
		testRt(con);
		testDbPos(con);
		testRead(con);
	}
	

	private static void testDbPos(Connection con) throws Exception {
	  DatabaseMetaData dbmd = null;
	  String tableName = null;
    DbColumn col = null;
    
      tableName = "XI_DIRCACHEERRORS";
      dbmd = NativeSQLAccess.getNativeMetaData(con);  
      java.sql.ResultSet rs = dbmd.getColumns(null, null, tableName, null);
      while (rs.next()) {
      	System.out.println(rs.getString("COLUMN_NAME"));
      	System.out.println(rs.getInt("ORDINAL_POSITION"));
		  }  
      rs.close();   
	}
	
	private static void testRt(Connection con) throws Exception {
		DbFactory factory = new DbFactory(con);
		DbRuntimeObjects rt = DbRuntimeObjects.getInstance(factory);
		System.out.println(rt.get("XI_DIRCACHEERRORS"));
	}
	
	private static void testRead(Connection con) throws Exception {
        String tab = "BC_COMPVERS"; 
		String str = "SELECT * FROM " + tab;
        con.setAutoCommit(false);
		con.setTransactionIsolation(2);
		PreparedStatement pstmt = con.prepareStatement(str);
        
        ResultSet rs = pstmt.executeQuery();
        rs.next();
        // System.out.println(rs.getString(1));
        rs.close();
        pstmt.close();
        
	}
		

}
