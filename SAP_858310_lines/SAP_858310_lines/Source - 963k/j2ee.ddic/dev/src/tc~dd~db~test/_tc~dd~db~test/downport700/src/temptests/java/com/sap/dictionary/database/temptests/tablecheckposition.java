/*
 * Created on Dec 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.dictionary.database.temptests;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.xml.sax.InputSource;

import com.sap.dictionary.database.dbs.*;

import com.sap.sql.NativeSQLAccess;
import com.sap.sql.jdbc.internal.SAPDataSource;

/**
 * @author d003550
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TableCheckPosition {
  
  public TableCheckPosition() {}	
	
  public static void main(String[] argv) { 
    Connection con = null;
  	DbFactory factory = null;
  	
	try {
//	  Class.forName("com.sap.sql.jdbc.common.CommonDriver"); 
////    Get connetction
//      con =  java.sql.DriverManager.getConnection(
//    				"jdbc:db2://us4049:5754/B20",
//    				"sapb20db",
//    				"sap"); 
  	  
		String dbSource = "jdbc/common/B5S";
  		System.out.println(dbSource);
        DataSource ds = SAPDataSource.getDataSource(dbSource);
        con = ds.getConnection();    
      
      factory = new DbFactory(con);
      int cnt = checkTables(con,factory);
      if (cnt >0)
      	System.out.println((new Integer(cnt)).toString() + "Tables with Position error");
    }
    catch (Exception ex) {
      	ex.printStackTrace();
    }
  }  
    	
  private static int checkTables(Connection con, DbFactory factory) {
  	int cnt = 0;
  	boolean wrongField = false;
  	
  	ResultSet res = null;
  	try {
      PreparedStatement statement = NativeSQLAccess.prepareNativeStatement(con,
          "SELECT * FROM BC_DDDBTABLERT");	
      res = statement.executeQuery();
  	}
  	catch (SQLException ex) {
  	  ex.printStackTrace();	
  	}
    String name = null;
    DbTable tabViaXml = null;
    DbTable tabViaDb = null;
    DbColumns colsDb = null;
    DbColumns colsXml = null;
    DbColumnIterator iter = null;
    DbColumn colDb = null;
    DbColumn colXml = null;
    try {
      while (res.next()) {
        wrongField = false;
        name = res.getString("name");
        try {
          tabViaDb = factory.makeTable(name);
          tabViaDb.setCommonContentViaDb(factory);
          colsDb = tabViaDb.getColumns();
          tabViaXml = computeXmlValue(name,con,factory);
        }
        catch (Exception ex) {
      	  System.out.println("Table " + name + ": could not be analysed");
      	  cnt = cnt + 1;
      	  continue;
        }
        colsXml = tabViaXml.getColumns();
        if (colsXml == null) {
          System.out.println("Table " + name + ": Xml-problems");
          continue;
        }  	
        if (colsDb == null) {
      	  System.out.println("Table " + name + ": No database fields");
      	  continue;
        }	
        iter = colsDb.iterator();
        while (iter.hasNext()) {
          colDb = (DbColumn) iter.next();
          colXml = colsXml.getColumn(colDb.getName());
          if (colXml == null) {
            System.out.println("Table :" + name + " inconsistent. DbField: " + colDb.getName() +
            		" does not exist in xml");
            continue;
          }  
          if (colDb.getPosition() !=  colXml.getPosition()) {
        	   System.out.println("Table :" + name + " Field: " + colXml.getName() + " "
      	  	 		+ "DbPos: " + colDb.getPosition() + "xmlPos: " + colXml.getPosition());
      	     wrongField = true;
          }	  	
        }
        if (wrongField) 
      	  cnt = cnt + 1;
  	  }
    }
    catch (SQLException ex) {
      ex.printStackTrace();	
    }

  	return cnt;
  }
  
  private static DbTable computeXmlValue(String name, Connection con, DbFactory factory) 
                      throws Exception {
  	DbTable tabViaXml = null;   
  	
  	try {
      PreparedStatement statement = NativeSQLAccess.prepareNativeStatement(con,
       "SELECT XMLVALUE FROM BC_DDDBTABLERT WHERE NAME = ?");
      statement.setString(1,name);
      ResultSet res = statement.executeQuery();
      String xml = null;
      while (res.next()) {
        Reader reader = res.getCharacterStream("XMLVALUE");	
        xml = getString(new BufferedReader(res.getCharacterStream("XMLVALUE")));
       }
       tabViaXml = factory.makeTable(name);
	   XmlMap tableMap = new XmlExtractor().map(new InputSource(new StringReader(
					(String)xml)));
       tabViaXml.setCommonContentViaXml(tableMap);
      }
      catch (Exception ex) {     	
    	ex.printStackTrace();
      }
     return tabViaXml;
  }
  	
  private static String getString(BufferedReader bufferedReader)
	                 throws Exception{
	String s;
	StringBuffer buffer = new StringBuffer();
	StringBuffer buffer1 = null;
	while ((s = bufferedReader.readLine()) != null) {
		buffer1 = buffer.append(s);
		//System.out.println(buffer1.toString());
	}	
	return buffer.toString();	
}	
  
}
