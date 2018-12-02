package com.sap.dictionary.database.sap;

import com.sap.dictionary.database.dbs.*;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;
import com.sap.sql.NativeSQLAccess;
import java.sql.*;

/**
 * Title:        Analyse Tables and Views for structure changes (SAPDB-specific extension)
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and XML-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author       Thomas Elvers
 * @version 1.0
 */

public class DbSapTools extends DbTools
{
  private static Location loc = Logger.getLocation("sap.DbSapTools");
  private static final Category cat = Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);
  
  public DbSapTools(DbFactory factory) {
    super(factory);
  }

  /**
  * Renames a table on the database. If no exception is send, the table
  * could be renamed.
  * @param sourceName - current name of table
  * @param destinationName - new name of table
  * @exception JddException - The following error-situations should be
  *                  distinguished by the exception's ExType:
  *            ExType.NOT_ON_DB: Source-table does not exist on database
  *            ExType.EXISTS_ON_DB: Destination table already exists.
  *            Every other error should be send with ExType.SQL_ERROR or
  *            ExType.OTHER.
  **/

  public void renameTable(String sourceName, String destinationName) throws JddException {
    loc.entering("renameTable");

    try {
      Connection conn = this.getFactory().getConnection();
      Statement  stmt = NativeSQLAccess.createNativeStatement(conn); 

      stmt.execute("RENAME TABLE \"" + sourceName.toUpperCase() + "\" " +
                   "TO \"" + destinationName + "\" ");
      stmt.close();
    }
    catch (SQLException sqlEx) {
      ExType xt;

      Object[] arguments = {sourceName, destinationName, sqlEx.getMessage()};
      cat.errorT(loc, "renameTable({0},{1}) failed: {2}", arguments);
      loc.exiting();

      switch (sqlEx.getErrorCode()) {
        case -4004:
          xt = ExType.NOT_ON_DB;
          break;

        case -6000:
          xt = ExType.EXISTS_ON_DB;
          break;

        default:
          xt = ExType.SQL_ERROR;
          break;
      }
      throw new JddException(xt, sqlEx.getMessage());
    }
    catch (Exception ex) {
      Object[] arguments = {sourceName, destinationName, ex.getMessage()};
      cat.errorT(loc, "renameTable({0},{1}) failed: {2}", arguments);
      loc.exiting();
      throw new JddException(ExType.OTHER, ex.getMessage());
    }

    Object[] arguments = {sourceName, destinationName};
    cat.infoT(loc, "renameTable: renamed {0} to {1}", arguments);
    loc.exiting();

    return;   
  }
   
  /**
   * Examines if a given table name is an alias.
   * @param tableName - current name of table
   * @exception JddException - The following error-situations should be
   *                           distinguished by the exception's ExType:
   *            ExType.SQL_ERROR: Object with tableName could not be examined 
   **/
  public boolean isAlias(String tableName) throws JddException {
    loc.entering("isAlias");
    
    boolean    isAlias = false;
    Connection conn    = this.getFactory().getConnection();
    String     stmtStr = "SELECT 1 FROM DOMAIN.TABLES " +
                         "WHERE SCHEMANAME = USER AND " +
                         "TABLENAME = ? AND TABLETYPE = 'SYNONYM'";

    try 
    {     
      PreparedStatement  ps = NativeSQLAccess.prepareNativeStatement(conn, stmtStr);
      java.sql.ResultSet rs;
      
      ps.setString(1, tableName.toUpperCase());
      rs = ps.executeQuery();
      isAlias = rs.next() ? true : false;   
      rs.close();
      ps.close();
    } 
    catch (SQLException ex) 
    {
      Object[] arguments = {tableName, ex.getMessage()};
      cat.errorT(loc, "existence check for table {0} failed: {1}", arguments);
      loc.exiting();
      throw JddException.createInstance( ex );
    } 
  
    Object[] arguments = {tableName, isAlias ? "is an" : "is NO"};
    cat.infoT(loc, "database object {0} {1} Alias", arguments);
    loc.exiting();
    return isAlias;
  }
  
  /** 
     * Checks what kind of tablelike database object corresponds to name. It is checked
     * if we have an alias or a view on database with the given name. If this is the case the result 
     * is delivered as DbTools.KindOfTableLikeDbObject. In all other cases (including object 
     * is a table on database or object does not exist at all) the return value is null.
     * @param name Name of object to check
     * @return DbTools.KindOfTableLikeDbObject.VIEW, if object is a view on database,
     *         DbTools.KindOfTableLikeDbObject.ALIAS, if object is an Alias on database,
     *         null in all other cases
     * @exception JddException is thrown if error occurs during analysis        
    **/
  public int getKindOfTableLikeDbObject(String tableName) throws JddException {
      loc.entering("getKindOfTableLikeDbObject");

      int tableObjType = DbTools.TABLE;
      String     tabType = "Unknown";
      Connection conn    = this.getFactory().getConnection();
      String     stmtStr = "SELECT TABLETYPE FROM DOMAIN.TABLES " +
                           "WHERE SCHEMANAME = USER AND TABLENAME = ?";


      try {
          PreparedStatement  ps = NativeSQLAccess.prepareNativeStatement(conn, stmtStr);
          java.sql.ResultSet rs;
      
          ps.setString(1, tableName.toUpperCase());
          rs = ps.executeQuery();

          if (rs.next()) {
              tabType = rs.getString(1);

              if (tabType.equals("T")) {
                  tabType = "Table";
              } else if (tabType.equals("S")) {
                  tabType = "Synonym";
                  tableObjType = DbTools.ALIAS;
              } else if (tabType.equals("V")) {
                  tabType = "View";
                  tableObjType = DbTools.VIEW;
              }
          }
          rs.close();
          ps.close();
      } catch (SQLException ex) {
          Object[] arguments = { tableName, ex.getMessage()};
          cat.errorT(loc, "type check for table {0} failed: {1}", arguments);
          loc.exiting();
          throw JddException.createInstance(ex);
      }

      Object[] arguments = { tableName, tabType };
      cat.infoT(loc, "database object type for {0} is {1}", arguments);
      loc.exiting();

      return tableObjType;
  }
}
