/*
 * Created on Nov 6, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.sap.dictionary.database.dbs;

import java.io.PrintWriter;
/**
 * @author D003550
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DbColumnAdditionalTypeInfo {
  String database = null;
  String javaSqlTypeName = null;
  String dbType = null; 
  
  public DbColumnAdditionalTypeInfo(String database, String javaSqlTypeName,
	                    String dbType) {
	this.database = database;
	this.javaSqlTypeName = javaSqlTypeName;	
	this.dbType = dbType;	
  }
  
  public void writeCommonContentToXmlFile(PrintWriter file, String offset0)
	   throws Exception {
	file.println(offset0 + "<database>" + database +  "</database>");   	
	file.println(offset0 + "<java-sql-type>" + javaSqlTypeName +  "</java-sql-type>");   	
	file.println(offset0 + "<db-type>" + dbType +  "</db-type>");
  }	   	
  
  public String toString() {
  	return "AdditionalInfo = " +  		   "Database    : " + database + "\n" +
	       "JavaSqlType : " + javaSqlTypeName + "\n" +
	       "dbType      : " + dbType;
  }
}
