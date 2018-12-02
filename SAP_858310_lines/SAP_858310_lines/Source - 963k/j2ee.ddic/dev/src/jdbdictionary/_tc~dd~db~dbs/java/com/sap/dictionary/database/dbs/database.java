package com.sap.dictionary.database.dbs;

import java.sql.*;
import com.sap.sql.NativeSQLAccess;
import java.util.HashMap;
/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database 
 *               and XML-sources. Analyser-classes allow to examine this objects for structure-changes, 
 *               code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public class Database {
  String name = "";
  String abbr = "";

  private Database(String name,String abbr) {
    this.name = name;
    this.abbr = abbr;
  }

  public static final Database ORACLE     = new Database("ORACLE","Ora");
  public static final Database INFORMIX   = new Database("INFORMIX","Inf");
  public static final Database SAPDB      = new Database("SAPDB","Sap");
  public static final Database MSSQL      = new Database("MSSQL","Mss");
  public static final Database DB2        = new Database("DB2","Db2");
  public static final Database DB4        = new Database("DB4","Db4");
  public static final Database DB6        = new Database("DB6","Db6");
  public static final Database MYSQL      = new Database("MYSQL","Mys");
  public static final Database JAVADB      = new Database("JAVADB","Jdb");
  public static final Database ARTIFICIAL = new Database("ARTIFICIAL","Art");
  private static final String[] dbs = { "ORACLE", "INFORMIX", "SAPDB", "MSSQL", "DB2", "DB4", "DB6", "MYSQL", "JAVADB" };
  private static final String[] dbsAbbr = {"Ora","Sap","Mss","Db2","Db4","Db6"};
  
  /**
     * Returns the database name as String
     * @return the database name
     * 
     */
  public String getName() {return name;}
  
  /**
   *  Returns the abbreviation for every supported database  
   *  @return the abbreviation for a database           
   * 
   * */
  public String getAbbreviation() {return abbr;}

  /**
   * Returns the corresponding Database-object for connection
   * 
   * @param connection
   *            A database connection
   * @return the Database-object corresponding to this connection
   *  
   */
    public static Database getDatabase(Connection connection)
            throws SQLException {
        if (connection == null) {
            return Database.ARTIFICIAL;
        }
        String dbProductName = NativeSQLAccess.getVendorName(connection);
        if (0 == dbProductName.compareToIgnoreCase("ORACLE")) {
            return Database.ORACLE;
        } else if (0 == dbProductName.compareToIgnoreCase("SAPDB")) {
            return Database.SAPDB;
        } else if (0 == dbProductName.compareToIgnoreCase("INFORMIX")) {
            return Database.INFORMIX;
        } else if (0 == dbProductName.compareToIgnoreCase("MS_SQL_SERVER")) {
            return Database.MSSQL;
        } else if (0 == dbProductName.compareToIgnoreCase("MYSQL")) {
            return Database.MYSQL;
        } else if (0 == dbProductName.compareToIgnoreCase("DB2_UDB")) {
            return Database.DB6;
        } else if (0 == dbProductName.compareToIgnoreCase("DB2_UDB_AS400")) {
            return Database.DB4;
        } else if (0 == dbProductName.compareToIgnoreCase("DB2_UDB_OS390")) {
            return Database.DB2;
        } else if (0 == dbProductName.compareToIgnoreCase("JAVADB")) {
            return Database.JAVADB;
        } else
            throw new SQLException();
    }
  
  
  /**
   * Returns the corresponding Database-object for the specified name
   * 
   * @param name
   *            A database name
   * @return the Database-object corresponding to this connection
   *  
   */
    public static Database getDatabase(String name) {
        if (0 == name.compareToIgnoreCase("ORACLE")) {
            return Database.ORACLE;
        } else if (0 == name.compareToIgnoreCase("SAPDB")) {
            return Database.SAPDB;
        } else if (0 == name.compareToIgnoreCase("INFORMIX")) {
            return Database.INFORMIX;
        } else if (0 == name.compareToIgnoreCase("MS_SQL_SERVER")
                || 0 == name.compareToIgnoreCase("MSSQL")) {
            return Database.MSSQL;
        } else if (0 == name.compareToIgnoreCase("MYSQL")) {
            return Database.MYSQL;
        } else if (0 == name.compareToIgnoreCase("DB2_UDB")
                || 0 == name.compareToIgnoreCase("DB6")) {
            return Database.DB6;
        } else if (0 == name.compareToIgnoreCase("DB2_UDB_AS400")
                || 0 == name.compareToIgnoreCase("DB4")) {
            return Database.DB4;
        } else if (0 == name.compareToIgnoreCase("DB2_UDB_OS390")
                || 0 == name.compareToIgnoreCase("DB2")) {
            return Database.DB2;
        } else if (0 == name.compareToIgnoreCase("JAVADB")) {
            return Database.JAVADB;
        } else
            return null;
    }
   
   /**
   *  Returns all database names as String  
   *  @return the Database-names            
   * */
   public static String[] getDatabaseNames() {return dbs;}	
   
   /**
   *  Returns all database abbreviations for all relevant databases. Those ar
   *  Oracle, MaxDb, Mssql, Db2, Db4, Db6 and MySql. 
   *  @return the Database-abbreviations            
   * */
   public static String[] getDatabaseAbbreviations() {return dbsAbbr;} 
   
   /**
   *  Returns all database vendor titles  
   *  @return the vendor titles as String in a HashMap with the database
   *          name used above as key            
   * */
   public static HashMap getVendorTitleForDatabaseName() {
   	 HashMap dbVendorTitles = new HashMap();
	 dbVendorTitles.put(Database.DB6.getName(),NativeSQLAccess.getVendorTitle(NativeSQLAccess.VENDOR_DB2_UDB));	
	 dbVendorTitles.put(Database.DB4.getName(),NativeSQLAccess.getVendorTitle(NativeSQLAccess.VENDOR_DB2_UDB_AS400));	
	 dbVendorTitles.put(Database.DB2.getName(),NativeSQLAccess.getVendorTitle(NativeSQLAccess.VENDOR_DB2_UDB_OS390));	
	 dbVendorTitles.put(Database.MSSQL.getName(),NativeSQLAccess.getVendorTitle(NativeSQLAccess.VENDOR_MS_SQL_SERVER));	
	 dbVendorTitles.put(Database.MYSQL.getName(),NativeSQLAccess.getVendorTitle(NativeSQLAccess.VENDOR_MYSQL));
	 dbVendorTitles.put(Database.SAPDB.getName(),NativeSQLAccess.getVendorTitle(NativeSQLAccess.VENDOR_SAPDB));	
	 dbVendorTitles.put(Database.ORACLE.getName(),NativeSQLAccess.getVendorTitle(NativeSQLAccess.VENDOR_ORACLE));	
     return dbVendorTitles;
  }
}
