/*
 * Created on 24.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.dictionary.database.temptests;

import java.sql.Connection;
import java.sql.DriverManager;

import javax.sql.DataSource;

import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.sql.connect.OpenSQLDataSource;
import com.sap.sql.jdbc.internal.SAPDataSource;

/**
 * @author d019347
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ConnectionService {

	public static void main(String[] args) {
	}
	
	public static Connection getConnection(String testDbsName) throws Exception {
		DataSource ds = null;
		if (testDbsName != null)
			if (testDbsName.equalsIgnoreCase("sap"))
				ds = SAPDataSource.getOpenSQLDataSource("jdbc/common/B5S");
			else if (testDbsName.equalsIgnoreCase("mss"))
				ds = SAPDataSource.getOpenSQLDataSource("jdbc/common/BSE");
			else if (testDbsName.equalsIgnoreCase("ora"))
				ds = SAPDataSource.getOpenSQLDataSource("jdbc/common/BIN");
			else if (testDbsName.equalsIgnoreCase("db6"))
				ds = SAPDataSource.getOpenSQLDataSource("jdbc/common/UNI");
			else if (testDbsName.equalsIgnoreCase("db4"))
				ds = SAPDataSource.getOpenSQLDataSource("jdbc/common/I70");
			else if (testDbsName.equalsIgnoreCase("db2"))
				ds = SAPDataSource.getOpenSQLDataSource("jdbc/common/D8J");

		if (ds != null) {
			Connection con = ds.getConnection();
			//con.setAutoCommit(false);	
			return con;
		}
		else
			return null;
	}
	
	public static Connection getConnection(String driver,
			String url, String user, String password) throws Exception {
//  String DRIVER = "com.sap.dbtech.jdbc.DriverSapDB";
//  String URL = "jdbc:sapdb://WDFD00130360A/D70?spaceoption=true";
//  String USER = "SAPD70DB";
//  String PASSWORD = "abc123";
//
		OpenSQLDataSource osds = OpenSQLDataSource.newInstance();
		osds.setDriverProperties(driver, url, user, password);
		Connection con = osds.getConnection();
  
		con.setAutoCommit(false);
		
		return con;

	}
}
