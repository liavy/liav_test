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
public class Example17 implements DbsConstants{
  private static Connection con = null;
  private static DbFactory factory = null;
	
  Example17() {}	

  public static void main(String[] argv) { 
  	//Location cat = Location.getLocation("com.sap.dictionary.database");
    //cat.setEffectiveSeverity(com.sap.tc.logging.Severity.ALL);
    //cat.addLog(new FileLog("C:/Users/Temp/test.txt",new TraceFormatter()));
    //cat.addLog(new ConsoleLog(new TraceFormatter()));
    //cat.infoT("SDA-Deployment");
    
  	try {
        //DataSource ds = SAPDataSource.getDataSource("jdbc/direct/BA1");
        //Connection con = ds.getConnection();
               
        //DataSource ds = SAPDataSource.getDataSource("jdbc/direct/BCO");
        //Connection con = ds.getConnection();
 
        //MSS -SPECIAL       
        //Class.forName("com.inet.tds.TdsDriver");
        //Connection con = DriverManager.getConnection(
        //                    "jdbc:inetdae7:pwdf0071:1433","sapr3","sap");
         
        // ADABAS 
        DataSource ds = SAPDataSource.getDataSource("jdbc/direct/BCO");
        con = ds.getConnection();                
        //DataSource ds = SAPDataSource.getDataSource("jdbc/common/TEL");
                                                       
        factory = new DbFactory(con);
    }
    catch (Exception ex) {
      	ex.printStackTrace();
    }
    
    //System.out.println(
    //		DbMsgHandler.get(TABLE_ONDB_NOTFOUND,new Object[]{Action.DROP_CREATE}));
    DbModificationController distributor = new DbModificationController(factory,"C:\\Temp\\buildTest\\delpoylog.txt");
    distributor.distribute("C:\\Temp\\buildTest\\test1.sda");
    //distributor.distribute("C:\\Temp\\dbtables\\KMC_TQ_PROP.zip");
    //distributor.distribute("C:\\Temp\\buildTest\\TMP_DFF.sda");
	//distributor.distribute("C:\\Temp\\buildTest\\j2ee_jddschema.sda",
    distributor.distribute("C:\\Temp\\buildTest\\CBSDBDefs.sda");
      //"C:\\Temp\\buildTest\\delpoylog.txt"); 
    distributor.delete(new String[]{"BC_TABLE1"});
    //distributor.delete(new String[]{RUNTIME_OBJECTS_TABLE_NAME});
      
   
  }     
}