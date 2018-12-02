package com.sap.archtech.initservice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * The <code>DatabaseAccessor</code> class is responsible for providing all required database content
 * to the XML DAS Initialization Service.
 */
class DatabaseAccessor
{
  private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Initialization Service");
  private static final Location loc = Location.getLocation("com.sap.archtech.initservice");
  // Constants for SQL expressions
  private static final String LOAD_ARCH_SETS = "SELECT DISTINCT asetname from BC_XMLA_ASETS"; 
  
  private final DataSource dbPool;
    
  DatabaseAccessor() throws NamingException
  {
    dbPool = getDbPool();
  }
  
  /**
   * Load the names of all Archiving Sets from database
   * @return The names of all registered Archiving Sets
   * @throws SQLException Thrown if loading from database failed
   */
  String[] getRegisteredArchivingSets() throws SQLException
  {
    Connection conn = null;
    PreparedStatement loadStmt = null;
    try
    {
      conn = dbPool.getConnection();
      conn.setAutoCommit(false);
      loadStmt = conn.prepareStatement(LOAD_ARCH_SETS);
      ResultSet loadResult = loadStmt.executeQuery();
      ArrayList<String> archSetNames = new ArrayList<String>();
      while(loadResult.next())
      {
        archSetNames.add(loadResult.getString("ASETNAME"));
      }
      loadResult.close();
      return archSetNames.toArray(new String[archSetNames.size()]);
    }
    finally
    {
      if(loadStmt != null)
      {
        loadStmt.close();
      }
      if(conn != null)
      {
        conn.close();
      }
    }
  }
  
  private DataSource getDbPool() throws NamingException
  {
    try
    {
      Context initCtx = new InitialContext();
      return (DataSource)initCtx.lookup("java:comp/env/SAP/BC_XMLA");
    }
    catch(NamingException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Lookup for Database Connection Pool failed:" + e.getMessage(), e);
      throw e;
    }
  }
}
