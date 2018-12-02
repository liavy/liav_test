package com.sap.dictionary.database.sap;

import com.sap.dictionary.database.dbs.*;
import java.sql.*;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;
import com.sap.sql.NativeSQLAccess;
import java.util.ArrayList;

/**
 * Title:        DbSapView
 * Copyright:    Copyright (c) 2002
 * Company:      SAP AG
 * @author       Thomas Elvers
 */

public class DbSapView extends DbView implements DbsConstants {
	
  private static Location loc = Logger.getLocation("sap.DbSapView");
  private static final Category cat = Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);
  
  public DbSapView() {
  }

  public DbSapView(DbFactory factory) {
	 super(factory);
  }

  public DbSapView(DbFactory factory, String name) {
	 super(factory,name);
  }

  public DbSapView(DbFactory factory, DbView other) {
  	super(factory,other);
  }

  public DbSapView(DbFactory factory, DbSchema schema, String name) {
	 super(factory,schema,name);
  }	

  /**
   *  Analyses if view exists on database or not
   *  @return true - if table exists in database, false otherwise
   *  @exception JddException – error during analysis detected	 
   **/
  
  public boolean existsOnDb() throws JddException {
    loc.entering("existsOnDb");
    
    boolean exists  = false;
    String stmtStr  = "SELECT 1 FROM DOMAIN.VIEWS WHERE SCHEMANAME = USER AND " +
                      "VIEWNAME = ? and TYPE = 'VIEW' ";
                      
    try {
      Connection        conn = getDbFactory().getConnection();
      PreparedStatement ps = NativeSQLAccess.prepareNativeStatement(conn, stmtStr);
      
      ps.setString (1, this.getName());
      java.sql.ResultSet rs = ps.executeQuery();
      exists = rs.next() ? true : false; 
      rs.close();
      ps.close();
    }
    catch (Exception ex) {
      Object[] arguments = {ex.getMessage()};
      cat.errorT(loc, "existsOnDb failed: {0}", arguments);
      loc.exiting();
      throw JddException.createInstance(ex);
    }    
    loc.exiting();
    return exists;
  }
  
  /**
   *  gets the base table Names of this view from database and sets it 
   *  for this view 
   *  @exception JddException – error during analysis detected	 
   **/
  public void setBaseTableNamesViaDb() throws JddException {
    loc.entering("setBaseTableNamesViaDb");

    ArrayList  names = new ArrayList();  
    String stmtStr = "SELECT DISTINCT TABLENAME FROM DOMAIN.VIEWCOLUMNS WHERE " +
                     "SCHEMANAME = USER AND VIEWNAME = ? ";

    try {
      Connection        conn  = this.getDbFactory().getConnection();
      PreparedStatement ps    = NativeSQLAccess.prepareNativeStatement(conn, stmtStr);
      
      ps.setString (1, this.getName().toUpperCase());
      java.sql.ResultSet rs = ps.executeQuery();

      while ( rs.next() ) {
        names.add(rs.getString(1));
      }
      rs.close();
      ps.close();
    }
    catch (Exception ex) {
      Object[] arguments = {ex.getMessage()};
      cat.errorT(loc, "setBaseTableNamesViaDb failed: {0}", arguments);
      loc.exiting();
      throw JddException.createInstance( ex );
    }

    setBaseTableNames( names );
    loc.exiting();
  }
  
  /**
  *  Gets the create statement of this view from the database and 
  *  sets it to this view with method setCreateStatement
  *  @exception JddException � error during detection detected   
  **/  
  public void setCreateStatementViaDb() throws JddException {
    loc.entering("setCreateStatementViaDb");
    
    String createStatement = "";
    String stmtStr = "SELECT DEFINITION FROM DOMAIN.VIEWDEFS WHERE " +
                     "SCHEMANAME = USER AND VIEWNAME = ? ";

    try {
      Connection        conn  = this.getDbFactory().getConnection();
      PreparedStatement ps    = NativeSQLAccess.prepareNativeStatement(conn, stmtStr);
      
      ps.setString (1, this.getName().toUpperCase());
      java.sql.ResultSet rs = ps.executeQuery();

      if ( rs.next() ) {
        createStatement = rs.getString(1);
      }
      rs.close();
      ps.close();
    }
    catch (Exception ex) {
      Object[] arguments = {ex.getMessage()};
      cat.errorT(loc, "setBaseTableNamesViaDb failed: {0}", arguments);
      loc.exiting();
      throw JddException.createInstance( ex );
    }
    setCreateStatement(createStatement);
    loc.exiting();
  } 

}
