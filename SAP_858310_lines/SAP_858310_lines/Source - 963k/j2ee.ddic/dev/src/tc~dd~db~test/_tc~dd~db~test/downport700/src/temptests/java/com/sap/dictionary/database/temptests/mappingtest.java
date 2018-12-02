package com.sap.dictionary.database.temptests;

import com.sap.dictionary.database.dbs.*;

public class MappingTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
	    check();
    } catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }
		

	}
	
	public static void check() throws Exception {
		DbFactory factory = new DbFactory(Database.getDatabase("DB2"));
		for (long i = 1; true ; i++) {
			if (!checkMap(factory,java.sql.Types.VARCHAR,i,0)) {
				System.out.println(i);
				break;
			}    
    }

	}
	
	public static boolean checkMap(DbFactory factory,int jdbcType,long len,int dec)
			throws Exception {
		DbColumn col = factory.makeDbColumn();
		col.constructorPart(factory, "F1", 1,jdbcType, null, len, dec, false,null);
		return col.checkTypeAttributes();
	}


}
