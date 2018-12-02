package com.sap.dictionary.database.sap;

import com.sap.dictionary.database.dbs.*;
import java.sql.*;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;
import com.sap.sql.NativeSQLAccess;

/**
 * Title:        DbSapColumns
 * Copyright:    Copyright (c) 2002
 * Company:      SAP AG
 * @author       Thomas Elvers
 */

  public class DbSapColumns extends DbColumns {

  private static Location loc = Logger.getLocation("sap.DbSapColumns");
  private static final Category cat = Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);

  public DbSapColumns(DbFactory factory) {
    super(factory);}

  public DbSapColumns(DbFactory factory,DbColumns other) {
    super(factory,other);}

  public DbSapColumns(DbFactory factory,XmlMap xmlMap) throws Exception {
    super(factory,xmlMap);}


  public void setContentViaDb(DbFactory factory) throws JddException {
    loc.entering("setContentViaDb()");

    try {
      String stmt = "SELECT COLUMNNAME, RTRIM(DATATYPE) || ' ' || CODETYPE, "+
                    "LEN, DEC, \"DEFAULT\", NULLABLE, COMMENT " +
                    "FROM DOMAIN.COLUMNS " +
                    "WHERE SCHEMANAME=USER AND TABLENAME=? " +
                    "ORDER BY POS";
      String tableName = getTable().getName();
      boolean found = false;
      int pos = 0;
      
      PreparedStatement ps = NativeSQLAccess.prepareNativeStatement(factory.getConnection(), stmt);
      ps.setString(1, tableName);
      java.sql.ResultSet rs = ps.executeQuery();
      
      while (rs.next()) {
        found = true;
        String colName    = rs.getString (1).trim().toUpperCase();
        String dbType     = rs.getString(2).trim().toUpperCase();
        int colSize       = rs.getInt(3);
        int decDigits     = rs.getInt(4);
        String defVal     = rs.getString(5);
        boolean isNotNull = rs.getString(6).equals("NO") ? true : false;
        String varInfo    = rs.getString(7);
        pos++;
        
        // get java SQL type
        int sqlType       = getJavaSqlType (dbType, colSize);
                
        // special default value handling
        if (defVal != null) {
          if (dbType.equals("DATE") || dbType.equals("TIME") || dbType.equals("TIMESTAMP") ) {
            defVal = defVal.trim();
            String defValUpper = defVal.toUpperCase();
            if (defValUpper.startsWith("DEFAULT")) {
              int beg = defValUpper.indexOf("DEFAULT ");
              beg += 8; // skip "DEFAULT "
              defVal = defVal.substring(beg);
              defVal = defVal.trim();
            }
          }
          else if (dbType.equals("FLOAT")) {
            try {
              Double v = Double.valueOf(defVal);
              defVal = v.toString();
            }
            catch (NumberFormatException ex) { 
              //$JL-EXC$
              /* ignore */ 
            }
          }
          else if (dbType.equals("INTEGER") || dbType.equals("SMALLINT")) {
            try {
              Integer v = Integer.valueOf(defVal);
              defVal = v.toString();
            }
            catch (NumberFormatException ex) {
              //$JL-EXC$
              /* ignore */ 
            }
          }
          else if (dbType.equals("VARCHAR BYTE") || dbType.equals("CHAR BYTE")) {
            // add trailing x'00'
            if (defVal.length() < 2 * colSize) {
              StringBuffer sb = new StringBuffer(2 * colSize);
              int defValLen = 0;
                
              // default value x"00" returns blank
              if (!defVal.equals(" ")) {
                sb.append(defVal);
                defValLen = defVal.length();
              }
              // add x'00'
              for (int i = defValLen; i < 2*colSize; i++)
                sb.append('0');
                
              defVal = sb.toString();
            }
          }
        }
        
        // handle LONGVARBINARY columns
        if (sqlType == java.sql.Types.BLOB && 
            varInfo != null && varInfo.startsWith("LONGVARBINARY")) {
          sqlType = java.sql.Types.LONGVARBINARY;
          colSize = Integer.valueOf(varInfo.substring(varInfo.indexOf('(') + 1, varInfo.lastIndexOf(')'))).intValue();
        }
                    
        DbColumn col = factory.makeDbColumn(colName, pos, sqlType, dbType,
                                            colSize, decDigits, isNotNull, defVal);
        this.add(col);
      }
      rs.close();
      
      if (!found) {                               
        Object[] arguments = { tableName };
        cat.infoT(loc, "Table {0} not found on DB.", arguments);
      }                            
    } catch (Exception ex) {
      Object[] arguments = {ex.getMessage()};
      cat.errorT(loc, "setContentViaDb failed: {0}", arguments);
      loc.exiting();
      throw JddException.createInstance(ex);
    }
    loc.exiting();
  }

  
  /**
   *  Checks if number of columns is allowed
   *  @return true if number of columns is o.k, false otherwise
   * */
  public boolean checkNumber() {
    DbColumnIterator iter = this.iterator();
    int cnt = 0;

    while (iter.hasNext()) {
      iter.next();
      cnt ++;
    }

    return (cnt <= DbSapEnvironment.MaxColumnsPerTable());
  }

  /**
   * Given the SAPDB database type of a column, this returns the java sql type.
   * @return - java sql type (JDBC type)
   */   
  private  int getJavaSqlType (String dbType, int length) {
    int sqlType = java.sql.Types.OTHER;

    if (dbType.equals("VARCHAR") || dbType.equals("VARCHAR ASCII") || dbType.equals("VARCHAR UNICODE")) { 
      sqlType = java.sql.Types.VARCHAR;
    } 
    else if (dbType.equals("VARCHAR BYTE")) {
      if (length > 255) {
        sqlType = java.sql.Types.VARBINARY;
      } else {
        sqlType = java.sql.Types.BINARY;
      }
    } 
    else  if (dbType.equals("CHAR") || dbType.equals("CHAR ASCII") || dbType.equals("CHAR UNICODE")) { 
      sqlType = java.sql.Types.CHAR;
    } 
    else if (dbType.equals("CHAR BYTE")) { 
      sqlType = java.sql.Types.BINARY;
    } 
    else  if (dbType.equals("LONG") || dbType.equals("LONG ASCII") || dbType.equals("LONG UNICODE")) { 
      sqlType = java.sql.Types.CLOB;
    } 
    else  if (dbType.equals("LONG BYTE") || dbType.equals("LONG RAW")) {    
      sqlType = java.sql.Types.BLOB;    
    } 
    else if (dbType.equals("SMALLINT")) { 
      sqlType = java.sql.Types.SMALLINT;
    } 
    else if (dbType.equals("INTEGER")) { 
      sqlType = java.sql.Types.INTEGER;    
    } 
    else if (dbType.equals("FIXED") || dbType.equals("DECIMAL")) {
      if (length == 19) {
        sqlType = java.sql.Types.BIGINT;
      } else { 
        sqlType = java.sql.Types.DECIMAL;
      }   
    } 
    else if (dbType.equals("NUMBER")) { 
      sqlType = java.sql.Types.NUMERIC;   
    } 
    else if (dbType.equals("REAL")) { 
      sqlType = java.sql.Types.REAL;  
    } 
    else if (dbType.equals("FLOAT")) {
      if (length == 38) {
        sqlType = java.sql.Types.DOUBLE;
      } else if (length == 16) {
        sqlType = java.sql.Types.REAL;
      } else {
        sqlType = java.sql.Types.FLOAT;
      } 
    } 
    else if (dbType.equals("DOUBLE")) { 
      sqlType = java.sql.Types.DOUBLE;                     
    } 
    else if (dbType.equals("DATE")) { 
      sqlType = java.sql.Types.DATE;
    } 
    else if (dbType.equals("TIME")) { 
      sqlType = java.sql.Types.TIME;
    } 
    else if (dbType.equals("TIMESTAMP")) { 
      sqlType = java.sql.Types.TIMESTAMP;    
    } 
    else if (dbType.equals("BOOLEAN")) { 
      sqlType = java.sql.Types.BIT;    
    } 
    else {
      cat.infoT(loc, "Unknown database type {0}", new Object[] {dbType});
    }

    return (sqlType); 
  }
}
