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
public class DeploymentFromIde implements DbsConstants{
	private static String sdaName = "C:\\eclipseProjects2\\test1\\test1.sda";
	private static String logName = "C:\\eclipseProjects2\\test1\\delpoylog.txt";
	private static String[] SUFFIXES = {".gdbtable",".gdbview"};
  private static Connection con = null;
  private static DbFactory factory = null;
	
  DeploymentFromIde() {}	

  public static void main(String[] argv) { 
  	//Location cat = Location.getLocation("com.sap.dictionary.database");
    //cat.setEffectiveSeverity(com.sap.tc.logging.Severity.ALL);
    //cat.addLog(new FileLog("C:/Users/Temp/test.txt",new TraceFormatter()));
    //cat.addLog(new ConsoleLog(new TraceFormatter()));
    //cat.infoT("SDA-Deployment");
    
  	try {
        DataSource ds = SAPDataSource.getDataSource("jdbc/direct/BSE");
        con = ds.getConnection();                
                                                       
        factory = new DbFactory(con);
    }
    catch (Exception ex) {
      	ex.printStackTrace();
    }
    
    DbModificationController distributor = new DbModificationController(
    		factory,logName);
    distributor.distribute(sdaName);
    
    ArchiveReader ar = new ArchiveReader(sdaName,SUFFIXES,true);
		ArchiveEntry entry = null;
		String name = null;
		while ((entry = ar.getNextEntry()) != null) {
			name = entry.getName();
			if (name.endsWith(".gdbtable")) {
				name = name.substring(0,name.indexOf("."));
				distributor.delete(new String[]{name});
			}
			else {
				name = name.substring(0,name.indexOf("."));
			}
		}
      
   
  }     
}