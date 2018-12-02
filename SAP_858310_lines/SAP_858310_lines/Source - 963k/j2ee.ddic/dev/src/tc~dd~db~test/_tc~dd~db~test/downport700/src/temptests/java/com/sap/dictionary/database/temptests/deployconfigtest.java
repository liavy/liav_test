package com.sap.dictionary.database.temptests;

import java.sql.Connection;

import javax.sql.DataSource;

import com.sap.dictionary.database.dbs.DbDeployConfig;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.sql.NativeSQLAccess;
import com.sap.sql.jdbc.internal.SAPDataSource;

public class DeployConfigTest {
	private static Connection con = null;
	private static final String s0 = "INSERT INTO \"BC_EXTRA_RULES\" (\"ATTRIBUTE\",\"INCLUSIVE\",\"EXCLUSIVE\") VALUES (";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("STANDARD");
		DbDeployConfig standard = new DbDeployConfig();
		System.out.println(standard);
		fromDatabase();
		fromArray();
	}
	
	public static void fromArray() {
		try {
	    DbDeployConfig dc = DbDeployConfig.getInstance(arins());
	    System.out.println("FROM ARRAY");
	    System.out.println(dc.toString());
    } catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }
	}
	
	public static void fromDatabase() {
		try {
			DataSource ds = SAPDataSource.getOpenSQLDataSource("jdbc/common/B5S");			
			con = ds.getConnection();
			con.setAutoCommit(false);
	    dbins();
	    DbFactory factory = new DbFactory(con);
//			DbDeployConfig dc = DbDeployConfig.getInstance(factory);
			con.commit();
			System.out.println("FROM DATABASE");
			System.out.println(factory.getEnvironment().getDeployConfig());
    } catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }
		
	}
		
		
	public static void dbins() throws Exception {
		//sqlex("\'\',\'\',\'\'");
		sqlex("\'modifyEmptyTableForce\',\'TMP_TABLEXXX1\',\'\'");
		sqlex("\'specificForce\',\'TABSPECFORCEI1\',\'\'");
		sqlex("\'specificForce\',\'\',\'TABSPECFORCEE1\'");
		sqlex("\'specificForce\',\'*\',\'TABSPECFORCEE2\'");
		sqlex("\'acceptLongTimeContentDependent\',\'\',\'TABLOSSE1\'");
		sqlex("\'acceptLongTimeContentDependent\',\'\',\'TABLOSSE2\'");
		sqlex("\'acceptLongTimeContentDependent\',\'\',\'TABLOSSE3*\'");
		con.commit();
	}
	
	public static void sqlex(String st) throws Exception {
		NativeSQLAccess.createNativeStatement(con).execute(s0 + st + ")");
	}
	
	public static String[][] arins() throws Exception {
		return new String[][]{
		{"modifyEmptyTableForce","TMP_TABLEXXX1",""},
		{"specificForce","TABSPECFORCEI1",""},
		{"specificForce","","TABSPECFORCEE1"},
		{"specificForce","*","TABSPECFORCEE2"},
		{"acceptLongTimeContentDependent","","TABLOSSE1"},
		{"acceptLongTimeContentDependent","","TABLOSSE2"},
		{"acceptLongTimeContentDependent","","TABLOSSE3*"}};
		
	}

}
