package com.sap.dictionary.database.temptests;

import java.io.*;
import com.sap.dictionary.database.dbs.*;
import java.sql.*;
import com.sap.sql.jdbc.internal.SAPDataSource;
import javax.sql.*;

import java.util.*;
import com.sap.tc.logging.*;
import java.sql.Timestamp;

/**
 * @author d003550
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MssDeployTest implements DbsConstants{
  private static Connection con = null;
  private static DbFactory factory = null;
  private static String sdaPath = "C:\\Temp\\buildTest\\uddi-schema.sda";
  private static String logPath = "C:\\Temp\\buildTest\\delpoylog.txt";
  private static String dataSourceName = null;
  private static String driverName = "com.ddtek.jdbc.sqlserver.SQLServerDriver";
  private static String conInfo = "jdbc:datadirect:sqlserver://pwdf2663:1433;databasename=KT5";
  private static String user = "SAPKT5DB";
  private static String pass = "pssdb";
  
  
	
  MssDeployTest() {}	

  public static void main(String[] argv) { 
    
  	try {
        //MSS -SPECIAL
				Class.forName(driverName);
				con = DriverManager.getConnection(conInfo,user,pass);

				//oder
				//con = DriverManager.getConnection(
				//"jdbc:datadirect:sqlserver://pwdf2663:1433;databasename=KT5;user=SAPKT5DB;password=pssdb");
         
                                                       
        factory = new DbFactory(con);
    }
    catch (Exception ex) {
      	ex.printStackTrace();
    }
    
    DbModificationController controller = new DbModificationController(
    		factory,logPath);
    controller.fillDeployObjects(sdaPath);
    controller.analyse();
      
   
  }     
}