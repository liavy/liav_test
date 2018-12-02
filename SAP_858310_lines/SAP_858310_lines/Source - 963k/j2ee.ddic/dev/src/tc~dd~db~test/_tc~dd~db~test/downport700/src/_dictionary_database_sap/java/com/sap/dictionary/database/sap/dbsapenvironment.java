package com.sap.dictionary.database.sap;

import com.sap.dictionary.database.dbs.*;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

import java.util.*;
import java.sql.*;

/**
 * Title:        Analysis of table and view changes: SAP DB specific classes
 * Description:  SAP DB specific analysis of table and view changes. Tool to deliver SAP DB specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author       Thomas Elvers
 * @version 1.0
 */

public class DbSapEnvironment extends DbEnvironment {

  private static Location loc = Logger.getLocation("sap.DbSapEnvironment");
  private static final Category cat = Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);

  private String supportedDatabaseVersions[] = { "7.6", "7.7", "7.8" }; 
  
  private static ArrayList reservedWords = null;
  private String databaseVersion = null;
  private Connection activeConnection = null;

  public DbSapEnvironment() {
    super();
  }

  public DbSapEnvironment( Connection con ) {
    super(con);
    
    loc.entering("DbSapEnvironment");
    
    if (con == null) {
      databaseVersion = null;
    }
    else if (con != activeConnection) {
      try {
        // KERNEL    7.7.06   BUILD 007-123-197-046
        String dbvers = con.getMetaData().getDatabaseProductVersion();
        int startpos = 0;

        while (!Character.isDigit(dbvers.charAt(startpos))) {
          startpos++;
        }

        databaseVersion  = dbvers.substring(startpos, startpos + 3);

        Object[] arguments = { databaseVersion };
        cat.infoT(loc, "DbSapEnvironment: database version is MaxDB {0} ", arguments);
      } 
      catch (Exception e) {
        cat.warningT(loc, "DbSapEnvironment: Could not retrieve database product version.");
        cat.warningT(loc, e.getMessage());
        databaseVersion = null;
      }
    }

    activeConnection = con;
    loc.exiting();
  }
  
  public String getDatabaseVersion() {
    return databaseVersion;
  }
  
  public String[] getSupportedDatabaseVersions() {
    return supportedDatabaseVersions;
  }
 
  public void setDatabaseVersion(String version) {
    for (int i = 0; i < supportedDatabaseVersions.length; i++)
    {
      if (supportedDatabaseVersions[i].equals(version))
      {
        databaseVersion = supportedDatabaseVersions[i];
        return;
      }
      throw new JddRuntimeException("setDatabaseVersion: version " + version + " not supported");
    }
  }
  
  public int MaxTableLength() {
    String dbVers = getDatabaseVersion();
    if (dbVers == null || dbVers.compareTo("7.7") < 0)
      return (8088);
    else
      return (32767); 
  }
  
  public static int MaxNameLength()       { return (32); }
  public static int MaxIndexLength()      { return (1024); }
  public static int MaxKeyLength()        { return (1024); }

  public static int MaxColumnsPerTable()  { return (1024); }
  public static int MaxColumnsPerIndex()  { return (16); }
  public static int MaxKeysPerTable()     { return (512); }
  public static int MaxIndicesPerTable()  { return (255); }

  public static int MaxCharacterLength()  { return (4000); }
  public static int MaxBinaryLength()     { return (8000); }
  public static int MaxDecimalLength()    { return (38); }

  public static long GetColumnLength (DbColumn col, boolean isKey) {
    long len = 0;
    long collen = 0;

    switch (col.getJavaSqlType()) {
      case java.sql.Types.BLOB:
      case java.sql.Types.CLOB:
      case java.sql.Types.LONGVARBINARY:
         len = 9;
        break;
      case java.sql.Types.BIGINT:
        // FIXED(19)
        len = 12;
        break;
      case java.sql.Types.BINARY:
      case java.sql.Types.VARBINARY:
        collen = col.getLength();
        if ((collen <= 30) || (isKey == true && collen <= 254))
          len = collen + 1;
        else if (collen <= 254)
          len = collen + 2;
        else
          len = collen + 3;
        break;
      case java.sql.Types.CHAR:
      case java.sql.Types.VARCHAR:
      case java.sql.Types.LONGVARCHAR:
        collen = col.getLength();
        if ((collen <= 15) || (isKey == true && collen <= 127))
          len = (collen * 2) + 1;
        else if (collen <= 127)
          len = (collen * 2) + 2;
        else
          len = (collen * 2) + 3;
        break;
      case java.sql.Types.DATE:
      case java.sql.Types.TIME:
        len = 9;
        break;
      case java.sql.Types.TIMESTAMP:
        len = 21;
        break;
      case java.sql.Types.DECIMAL:
      case java.sql.Types.NUMERIC:
        len = (col.getLength() + 1)/2 + 2;
        break;
      case java.sql.Types.DOUBLE:
      case java.sql.Types.FLOAT:
        // FLOAT(38)
        len = 22;
        break;
      case java.sql.Types.REAL:
        // FLOAT(16)
        len = 11;
        break;
      case java.sql.Types.INTEGER:
        // FIXED(10)
        len = 7;
        break;
      case java.sql.Types.SMALLINT:
        // FIXED(5)
        len = 5;
        break;
    }

    return len;
  }

  public static boolean isReservedWord( String id ) {
    if (reservedWords == null) {
      // build list with all known reserved words
      reservedWords = new ArrayList();

      // currently reserved words
      reservedWords.add("ABS"); reservedWords.add("ABSOLUTE"); reservedWords.add("ACOS");
      reservedWords.add("ADDDATE"); reservedWords.add("ADDTIME"); reservedWords.add("ALL");
      reservedWords.add("ALPHA"); reservedWords.add("ALTER"); reservedWords.add("ANY");
      reservedWords.add("ASCII"); reservedWords.add("ASIN"); reservedWords.add("ATAN");
      reservedWords.add("ATAN2"); reservedWords.add("AVG"); reservedWords.add("BINARY");
      reservedWords.add("BIT"); reservedWords.add("BOOLEAN"); reservedWords.add("BYTE");
      reservedWords.add("CEIL"); reservedWords.add("CEILING"); reservedWords.add("CHAR");
      reservedWords.add("CHARACTER"); reservedWords.add("CHECK"); reservedWords.add("CHR");
      reservedWords.add("COLUMN"); reservedWords.add("CONCAT"); reservedWords.add("CONNECTED");
      reservedWords.add("CONSTRAINT"); reservedWords.add("COS"); reservedWords.add("COSH");
      reservedWords.add("COT"); reservedWords.add("COUNT"); reservedWords.add("CROSS");
      reservedWords.add("CURDATE"); reservedWords.add("CURRENT"); reservedWords.add("CURTIME");
      reservedWords.add("DATABASE"); reservedWords.add("DATE"); reservedWords.add("DATEDIFF");
      reservedWords.add("DAY"); reservedWords.add("DAYNAME"); reservedWords.add("DAYOFMONTH");
      reservedWords.add("DAYOFWEEK"); reservedWords.add("DAYOFYEAR"); reservedWords.add("DBYTE");
      reservedWords.add("DEC"); reservedWords.add("DECIMAL"); reservedWords.add("DECODE");
      reservedWords.add("DEFAULT"); reservedWords.add("DEGREES"); reservedWords.add("DELETE");
      reservedWords.add("DIGITS"); reservedWords.add("DIRECT"); reservedWords.add("DISTINCT");
      reservedWords.add("DOUBLE"); reservedWords.add("EBCDIC"); reservedWords.add("EXCEPT");
      reservedWords.add("EXISTS"); reservedWords.add("EXP"); reservedWords.add("EXPAND");
      reservedWords.add("FIRST"); reservedWords.add("FIXED"); reservedWords.add("FLOAT");
      reservedWords.add("FLOOR"); reservedWords.add("FOR"); reservedWords.add("FROM");
      reservedWords.add("FULL"); reservedWords.add("GRAPHIC"); reservedWords.add("GREATEST");
      reservedWords.add("GROUP"); reservedWords.add("HAVING"); reservedWords.add("HEX");
      reservedWords.add("HOUR"); reservedWords.add("IFNULL"); reservedWords.add("IGNORE");
      reservedWords.add("INDEX"); reservedWords.add("INITCAP"); reservedWords.add("INNER");
      reservedWords.add("INSERT"); reservedWords.add("INT"); reservedWords.add("INTEGER");
      reservedWords.add("INTERNAL"); reservedWords.add("INTERSECT"); reservedWords.add("INTO");
      reservedWords.add("JOIN"); reservedWords.add("KEY"); reservedWords.add("LAST");
      reservedWords.add("LCASE"); reservedWords.add("LEAST"); reservedWords.add("LEFT");
      reservedWords.add("LENGTH"); reservedWords.add("LFILL"); reservedWords.add("LINK");
      reservedWords.add("LIST"); reservedWords.add("LN"); reservedWords.add("LOCALSYSDBA");
      reservedWords.add("LOCATE"); reservedWords.add("LOG"); reservedWords.add("LOG10");
      reservedWords.add("LONG"); reservedWords.add("LONGFILE"); reservedWords.add("LOWER");
      reservedWords.add("LPAD"); reservedWords.add("LTRIM"); reservedWords.add("MAKEDATE");
      reservedWords.add("MAKETIME"); reservedWords.add("MAPCHAR"); reservedWords.add("MAX");
      reservedWords.add("MBCS"); reservedWords.add("MICROSECOND"); reservedWords.add("MIN");
      reservedWords.add("MINUTE"); reservedWords.add("MOD"); reservedWords.add("MONTH");
      reservedWords.add("MONTHNAME"); reservedWords.add("NATURAL"); reservedWords.add("NCHAR");
      reservedWords.add("NEXT"); reservedWords.add("NOROUND"); reservedWords.add("NO");
      reservedWords.add("NOT"); reservedWords.add("NOW"); reservedWords.add("NULL");
      reservedWords.add("NUM"); reservedWords.add("NUMERIC"); reservedWords.add("OBJECT");
      reservedWords.add("OF"); reservedWords.add("ON"); reservedWords.add("ORDER");
      reservedWords.add("PACKED"); reservedWords.add("PI"); reservedWords.add("POWER");
      reservedWords.add("PREV"); reservedWords.add("PRIMARY"); reservedWords.add("RADIANS");
      reservedWords.add("REAL"); reservedWords.add("REFERENCED"); reservedWords.add("REJECT");
      reservedWords.add("RELATIVE"); reservedWords.add("REPLACE"); reservedWords.add("RFILL");
      reservedWords.add("RIGHT"); reservedWords.add("ROUND"); reservedWords.add("ROWID");
      reservedWords.add("ROWNO"); reservedWords.add("RPAD"); reservedWords.add("RTRIM");
      reservedWords.add("SECOND"); reservedWords.add("SELECT"); reservedWords.add("SELUPD");
      reservedWords.add("SERIAL"); reservedWords.add("SET"); reservedWords.add("SHOW");
      reservedWords.add("SIGN"); reservedWords.add("SIN"); reservedWords.add("SINH");
      reservedWords.add("SMALLINT"); reservedWords.add("SOME"); reservedWords.add("SOUNDEX");
      reservedWords.add("SPACE"); reservedWords.add("SQRT"); reservedWords.add("STAMP");
      reservedWords.add("STATISTICS"); reservedWords.add("STDDEV"); reservedWords.add("SUBDATE");
      reservedWords.add("SUBSTR"); reservedWords.add("SUBSTRING"); reservedWords.add("SUBTIME");
      reservedWords.add("SUM"); reservedWords.add("SYSDBA"); reservedWords.add("TABLE");
      reservedWords.add("TAN"); reservedWords.add("TANH"); reservedWords.add("TIME");
      reservedWords.add("TIMEDIFF"); reservedWords.add("TIMESTAMP"); reservedWords.add("TIMEZONE");
      reservedWords.add("TO"); reservedWords.add("TOIDENTIFIER"); reservedWords.add("TRANSACTION");
      reservedWords.add("TRANSLATE"); reservedWords.add("TRIM"); reservedWords.add("TRUNC");
      reservedWords.add("TRUNCATE"); reservedWords.add("UCASE"); reservedWords.add("UID");
      reservedWords.add("UNICODE"); reservedWords.add("UNION"); reservedWords.add("UPDATE");
      reservedWords.add("UPPER"); reservedWords.add("USER"); reservedWords.add("USERGROUP");
      reservedWords.add("USING"); reservedWords.add("UTCDIFF"); reservedWords.add("VALUE");
      reservedWords.add("VALUES"); reservedWords.add("VARCHAR"); reservedWords.add("VARGRAPHIC");
      reservedWords.add("VARIANCE"); reservedWords.add("WEEK"); reservedWords.add("WEEKOFYEAR");
      reservedWords.add("WHERE"); reservedWords.add("WITH"); reservedWords.add("YEAR");
      reservedWords.add("ZONED");

      // sort the list
      Collections.sort(reservedWords);
    }

    return (Collections.binarySearch(reservedWords, id) >= 0);
  }
  
  public static boolean isSpecJ2EEColumn(String tabName, String colName, String dbType) {
    if (dbType == null || 
        dbType.equalsIgnoreCase("") || 
        dbType.equalsIgnoreCase("LONGVARBINARY")) {   
      if ( colName.equalsIgnoreCase("VBYTES") && tabName.equalsIgnoreCase("J2EE_CONFIGENTRY") ) {
        return true;
      }
    }   
    return false;
  }
}