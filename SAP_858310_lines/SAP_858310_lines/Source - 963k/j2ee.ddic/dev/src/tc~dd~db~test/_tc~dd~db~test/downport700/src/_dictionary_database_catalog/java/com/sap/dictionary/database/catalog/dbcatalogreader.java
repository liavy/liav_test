package com.sap.dictionary.database.catalog;

import com.sap.sql.catalog.*;
import java.sql.*;
import com.sap.dictionary.database.dbs.*; 

/**
 * Ueberschrift:   Tests zu jdbpersistency
 * Beschreibung:
 * Copyright:     Copyright (c) 2001
 * Organisation:
 * @author
 * @version 1.0
 */

public class DbCatalogReader implements CatalogReader {
  private Connection con = null;
  private Database   database   = null;
  private DbFactory factory     = null;

  public DbCatalogReader(Connection con) {
    this.con = con;
    try {
      this.database  = Database.getDatabase(con);
      this.factory = new DbFactory(con);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }	  
  }

  public Table getTable(String tableName)
        throws SQLException {   	
    Table gs = null;
    try {
      gs = new DbGeneralStructure(tableName,con,factory);
      if (tableName.compareTo(gs.getName()) != 0) return null;
    }
    catch (JddException ex) {
      if (ex.getExType() == ExType.SQL_ERROR)
        throw new SQLException(ex.getMessage());
      else if (ex.getExType() == ExType.NOT_ON_DB)
        return null;  
      else  
        return null;
    }
    return gs;
  }

  public Table getTable(String schemaName,String tableName)
        throws SQLException {
    Table gs = null;
    try {
      gs  =  new DbGeneralStructure(tableName,con,factory);
      if (tableName.compareTo(gs.getName()) != 0) return null;
    }
    catch (JddException ex) {
      if (ex.getExType() == ExType.SQL_ERROR)
        throw new SQLException(ex.getMessage());
      else if (ex.getExType() == ExType.NOT_ON_DB)
        return null;  
      else  
        return null;
    }
    return gs;
  }

  public boolean existsTable(String tableName)
        throws SQLException {
    Table gs = null;
    try {
      gs  =  new DbGeneralStructure(tableName,con,factory);
    }
    catch (JddException ex) {
      throw new SQLException(ex.getMessage());
    }
    if (gs != null) return true; else return false;
  }

  public boolean existsTable(String schemaName, String tableName)
        throws SQLException {
    Table gs = null;
    try {
      gs  =  new DbGeneralStructure(tableName,con,factory);
    }
    catch (JddException ex) {
      throw new SQLException(ex.getMessage());
    }
    if (gs != null) return true; else return false;
  }

  public boolean isLogicalCatalogReader() {return false;}
}