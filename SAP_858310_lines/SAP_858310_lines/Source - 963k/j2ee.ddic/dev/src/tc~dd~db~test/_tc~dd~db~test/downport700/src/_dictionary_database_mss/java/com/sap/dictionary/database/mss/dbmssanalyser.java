package com.sap.dictionary.database.mss;

import com.sap.dictionary.database.dbs.*;

// gd 040901
import java.sql.*;

// gd 170303 enabling dictionary on Open SQl connections
import com.sap.sql.NativeSQLAccess;



/**
 * Title:        Analysis of table and view changes: MS SQL Server specific classes
 * Description:  MS SQL Server specific analysis of table and view changes. Tool to deliver MS SQL Server specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */
public class DbMssAnalyser extends DbAnalyser {

  Connection con = null;

  public DbMssAnalyser() {
  }

  public boolean hasData(String tabname) {
    boolean hasData = false;

/* gd030505 without a connection this makes no sense
    // gd040901
    Statement stmt;

    try {
      stmt = NativeSQLAccess.createNativeStatement(con); // gd 170303 con.createStatement();

      ResultSet rs = stmt.executeQuery("select top 1 'a' from [" + tabname + "]");
      while (rs.next()) {
	// there's a row =>
	hasData = true;
      }
      rs.close();
      stmt.close();
    }
    catch (Exception ex) {
      //$JL-EXC$ 
      System.out.println("Exception caught : " + ex.getMessage());
    }

 */

    return hasData;
  }

  public boolean existsTable(String tablename) {
    boolean existsTable = false;

/* gd030505 without a connection this makes no sense

    // gd040901
    Statement stmt;

    try {
      stmt = NativeSQLAccess.createNativeStatement(con); // gd 170303 con.createStatement();

      ResultSet rs = stmt.executeQuery(
        "select name from sysobjects where name = '" + tablename +
        "' and xtype = 'U' and uid = user_id()");
      while (rs.next()) {
	// there's a table with that name =>
	existsTable = true;
      }
      rs.close();
      stmt.close();
    }
    catch (Exception ex) {
      //$JL-EXC$ 
      System.out.println("Exception caught : " + ex.getMessage());
    } 

 */

    return existsTable;
  }

  public boolean existsIndex(String tableName, String indexName) {
    boolean existsIndex = false;
    // should come with table name!!!

/* gd030505 without a connection this makes no sense

    // gd040901
    Statement stmt;

    try {
      stmt = NativeSQLAccess.createNativeStatement(con); // gd 170303 con.createStatement();

      ResultSet rs = stmt.executeQuery(
        "select si.name from sysindexes si, sysobjects so " +
        "  where si.name = '" + indexName + "' and si.indid between 1 and 254 and " +
        "        si.id = so.id and so.name = " + tableName + " so.uid = user_id()");
      if (rs.next()) {
	existsIndex = true;
      }
      rs.close();
      stmt.close();
    }
    catch (Exception ex) {
      //$JL-EXC$ 
      System.out.println("Exception caught : " + ex.getMessage());
    }

 */

    return existsIndex;
  }

  public boolean existsView(String viewname) {
    boolean existsView = false;

/* gd030505 without a connection this makes no sense

    // gd040901
    Statement stmt;

    try {
      stmt = NativeSQLAccess.createNativeStatement(con); // gd 170303 con.createStatement();

      ResultSet rs = stmt.executeQuery(
        "select name from sysobjects where name = '" + viewname +
        "' and xtype = 'V' and uid = user_id()");
      while (rs.next()) {
	// there's a view with that name =>
	existsView = true;
      }
      rs.close();
      stmt.close();
    }
    catch (Exception ex) {
      //$JL-EXC$ 
      System.out.println("Exception caught : " + ex.getMessage());
    }

 */

    return existsView;
  }

  public boolean hasIndexes(String tablename) {
    boolean existsIndexes = false;

/* gd030505 without a connection this makes no sense

    // gd040901
    Statement stmt;

    try {
      stmt = NativeSQLAccess.createNativeStatement(con); // gd 170303 con.createStatement();

      ResultSet rs = stmt.executeQuery(
        "select count(*) from sysindexes " +
        "  where id = (select id from sysobjects where name = '" +
                       tablename +
                       "' and xtype = 'U' and uid = user_id()) " +
        "  and   indid between 1 and 254 " +
        "  and   name  not like '[_]WA[_]Sys%'");
      while (rs.next()) {
	// there's a view with that name =>
	existsIndexes = rs.getInt(1) > 0;
      }
      rs.close();
      stmt.close();
    }
    catch (Exception ex) {
      //$JL-EXC$ 
      System.out.println("Exception caught : " + ex.getMessage());
    }

 */

    return existsIndexes;
  }

  public boolean isPrimary(String indexname) {
    boolean isPrimary = false;

/* gd030505 without a connection this makes no sense

    // gd040901
    Statement stmt;

    try {
      stmt = NativeSQLAccess.createNativeStatement(con); // gd 170303 con.createStatement();

      ResultSet rs = stmt.executeQuery(
        "select name from sysobjects where name = '" + indexname +
        "' and xtype = 'PK' and uid = user_id()");
      while (rs.next()) {
	// there's a table with that name =>
	isPrimary = true;
      }
      rs.close();
      stmt.close();
    }
    catch (Exception ex) {
      //$JL-EXC$ 
      System.out.println("Exception caught : " + ex.getMessage());
    }
 
 */

    return isPrimary;
  }
}