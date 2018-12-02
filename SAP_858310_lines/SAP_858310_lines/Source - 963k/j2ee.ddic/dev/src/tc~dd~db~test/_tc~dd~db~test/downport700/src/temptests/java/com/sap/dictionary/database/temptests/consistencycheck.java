/*
 * Created on 24.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.dictionary.database.temptests;

import java.sql.Connection;
import com.sap.dictionary.database.dbs.*;
import com.sap.dictionary.database.dbs.DbFactory;

/**
 * @author d019347
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ConsistencyCheck {
	private static String name = null;
	
	private static String testDbsName = "mss";
	private static String connectionKind = "OPEN";
	private static String driver = "oracle.jdbc.OracleDriver";
	private static String url = "jdbc:oracle:thin:@us4028.wdf.sap.corp:50077:E00";
	private static String user = "SAPE00DB";
	private static String password = "pssdb";
	
	private static DbFactory factory = null;

	public static void main(String[] args) {
		try {
			Connection con = null;
			if (testDbsName != null && testDbsName.trim().length() != 0)
				con = ConnectionService.getConnection(testDbsName);
			else
				con = ConnectionService.getConnection(connectionKind,driver,url,user,password);
			factory = new DbFactory(con);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DbRuntimeObjects runtimeObjects = DbRuntimeObjects.getInstance(factory);
	}
}
