package com.sap.dictionary.database.mys;

import com.sap.dictionary.database.dbs.*;
import java.sql.*;
// import com.sap.sql.NativeSQLAccess;



/**
 * Title:        Analysis of table and view changes: MS SQL Server specific classes
 * Description:  MySQL specific analysis of table and view changes. Tool to deliver MySQL specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author       Hakan Kuecuekyilmaz, <hakan at mysql dot com>, 2005-07-10
 * @version 1.0
 */

/** PROBABLY STALE, not implementable without connection **/ 
/** BE AWARE: to use native sql statements, you have to acquire Statement-objects this way: 
    	Statement stmt = NativeSQLAccess.createNativeStatement(con);
              resp. PreparedStatement-objects:
        preparedStatement = NativeSQLAccess.prepareNativeStatement(connection, sql_string);
 **/

public class DbMysAnalyser extends DbAnalyser {

  Connection con = null;

  public DbMysAnalyser() {
  }

  public boolean hasData(String tabname) {
    boolean hasData = false;
//    Statement stmt;
//
//    // connection ????
//
//    try {
//    	stmt = NativeSQLAccess.createNativeStatement(con); // gd 170303 con.createStatement();
//
//    	ResultSet rs = stmt.executeQuery("select 'a' from `" + tabname + "` limit 1");
//    	while (rs.next()) {
//    		// there's a row =>
//    		hasData = true;
//    	}
//    	rs.close();
//    	stmt.close();
//    } catch (Exception ex) {
//    	//$JL-EXC$ 
//    	System.err.println("Exception caught : " + ex.getMessage());
//		ex.printStackTrace();
//    }
    return hasData;
  }

  public boolean existsTable(String tablename) {
    boolean existsTable = false;
//    Statement stmt;
//
//    try {
//    	stmt = NativeSQLAccess.createNativeStatement(con); // gd 170303 con.createStatement();
//
//    	ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME =  '" + tablename + "`");
//    	while (rs.next()) {
//    		// there's a row =>
//    		existsTable = true;
//    	}
//    	rs.close();
//    	stmt.close();
//    } catch (Exception ex) {
//    	//$JL-EXC$ 
//    	System.out.println("Exception caught : " + ex.getMessage());
//    }
    return existsTable;
  }

  public boolean existsIndex(String tableName, String indexName) {
    boolean existsIndex = false;
//    // should come with table name!!!
//    Statement stmt;
//
//    // connection ????
//
//    // to be implemented

    return existsIndex;
  }

  public boolean existsView(String viewname) {
    boolean existsView = false;
//    Statement stmt;
//
//    try {
//    	stmt = NativeSQLAccess.createNativeStatement(con); // gd 170303 con.createStatement();
//
//    	ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_NAME =  '" + viewname + "`");
//    	while (rs.next()) {
//    		// there's a row =>
//    		existsView = true;
//    	}
//    	rs.close();
//    	stmt.close();
//    } catch (Exception ex) {
//    	//$JL-EXC$ 
//    	System.out.println("Exception caught : " + ex.getMessage());
//    }
    return existsView;
  }

  public boolean hasIndexes(String tablename) {
    boolean existsIndexes = false;
//    Statement stmt;
//
//    // connection ????
//
//    // to be implemented

    return existsIndexes;
  }
}