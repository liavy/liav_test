package com.sap.dictionary.database.dbs;

import java.util.HashMap;
import java.util.ArrayList;
import java.lang.reflect.*;
import java.text.Format;
import java.text.SimpleDateFormat;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and XML-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public class JavaSqlTypeInfo {
  private String  name               = "OTHER";
  private int     intCode            = java.sql.Types.OTHER;
  private boolean hasLengthAttribute = false;
  private boolean hasDecimals        = false;
  private boolean hasDefaultValue    = false;
  private String  defaultValuePrefix = "";
  private String  defaultValueSuffix = "";
  private boolean isAcceptedByAllDbs = false;
  private long    maxLength          = 0;
  private String  minValue           = null;
  private String  maxValue           = null;
  private String  ddlName            = null;
  private String  ddlSuffix          = "";
  private String  dbTypeName         = null;
  private int     ddlDefaultLength   = 0;
  private String  defaultDefault     = null;
  private int     byteFactor         = 0;
  private boolean trimDefaultValue   = true;
  private static ArrayList limits           = null;
  private static HashMap stringLimits       = new HashMap();
  private static HashMap binaryLimits       = new HashMap();	
  private static final SimpleDateFormat FDATE = 
                     new SimpleDateFormat("yyyy-MM-dd");
  private static final SimpleDateFormat FTIME =   
                     new SimpleDateFormat("H:mm:ss");
  private static final SimpleDateFormat FTIMESTAMP =
                     new SimpleDateFormat("yyyy-MM-dd H:mm:ss.SSS");
  private Format formatter = null;      
  private static final byte[][] BINARY_DEFAULTS = new byte[257][];
  private static final short SHORT_DEFAULT = 0; 
  private static final int INTEGER_DEFAULT = 0; 
  private static final String STRING_DEFAULT = " "; 
  private static final java.sql.Date DATE_DEFAULT = java.sql.Date.valueOf("1970-01-01"); 
  private static final java.sql.Time TIME_DEFAULT = java.sql.Time.valueOf("00:00:00"); 
  private static final java.sql.Timestamp TIMESTAMP_DEFAULT = 
  	   java.sql.Timestamp.valueOf("1970-01-01 00:00:00.000000000");
  
  static {
  	try {
      String className = null;
      String abbr = null;
  	  String[] databaseNames = Database.getDatabaseNames();
      for (int i=0;i<=databaseNames.length-1;i++) {
        limits = new ArrayList();
        abbr = Database.getDatabase(databaseNames[i]).getAbbreviation();
        try {
          className = "com.sap.dictionary.database." + abbr.toLowerCase() + "." + 
                      "Java" + abbr + "SqlTypeInfo";
          Class typeInfo = Class.forName(className);
          limits.add(new Integer(typeInfo.getField("VARCHAR_LIMIT").getInt(typeInfo)));
          limits.add(new Integer(typeInfo.getField("LONGVARCHAR_LIMIT").getInt(typeInfo)));
          limits.add(new Integer(typeInfo.getField("VARCHAR_WHERE_CONDITION_LIMIT").getInt(typeInfo)));
          stringLimits.put(databaseNames[i],limits);
          limits = new ArrayList();
          limits.add(new Integer(typeInfo.getField("VARBINARY_LIMIT").getInt(typeInfo)));
          limits.add(new Integer(typeInfo.getField("LONGVARBINARY_LIMIT").getInt(typeInfo)));
          limits.add(new Integer(typeInfo.getField("VARBINARY_WHERE_CONDITION_LIMIT").getInt(typeInfo)));
          binaryLimits.put(databaseNames[i],limits); 
        }
        catch (Exception ex) {
           //$JL-EXC$ If Db-classes not found do nothing
        }
      }
      for (int i=1;i<=256;i++) {
      	  BINARY_DEFAULTS[i] = new byte[i];
      	for (int j=1;j<i;j++) { 
      	  BINARY_DEFAULTS[i][j] = 0x00;
      	}				
      }
  	}
  	catch (Exception ex) {ex.printStackTrace();}  	
  }	

  public JavaSqlTypeInfo(DbFactory factory,String name,int intCode)
  {
    this.name    = name;
    this.ddlName = name;
    this.intCode = intCode;
    switch (intCode)
    {
      case (java.sql.Types.CHAR):
        hasLengthAttribute = true;
        hasDecimals        = false;
        hasDefaultValue    = false;
        defaultValuePrefix = "'";
        defaultValueSuffix = "'";
        defaultDefault     = " ";
        isAcceptedByAllDbs = false;
        maxLength          = 0;  //database dependent
        break;
      case (java.sql.Types.BINARY):
        hasLengthAttribute = true;
        hasDecimals        = false;
        hasDefaultValue    = true;
        defaultValuePrefix = "'";
        defaultValueSuffix = "'";
        defaultDefault     = "00x00";
        isAcceptedByAllDbs = false;
        maxLength          = 0;  //database dependent
        break;
      case (java.sql.Types.VARCHAR):
        hasLengthAttribute = true;
        hasDecimals        = false;
        hasDefaultValue    = true;
        defaultValuePrefix = "'";
        defaultValueSuffix = "'";
		trimDefaultValue   = false;
        isAcceptedByAllDbs = true;
        maxLength          = 0;  //database dependent
        break;
      case (java.sql.Types.VARBINARY):
        hasLengthAttribute = true;
        hasDecimals        = false;
        hasDefaultValue    = false;
        defaultValuePrefix = "";
        defaultValueSuffix = "";
        isAcceptedByAllDbs = true;
        maxLength          = 0;  //database dependent
        break;
      case (java.sql.Types.LONGVARCHAR):
        hasLengthAttribute = true;
        hasDecimals        = false;
        hasDefaultValue    = false;
        defaultValuePrefix = "";
        defaultValueSuffix = "";
				trimDefaultValue   = false;
        isAcceptedByAllDbs = false;
        maxLength          = 0;  //database dependent
        break;
      case (java.sql.Types.LONGVARBINARY):
        hasLengthAttribute = true;
        hasDecimals        = false;
        hasDefaultValue    = false;
        defaultValuePrefix = "";
        defaultValueSuffix = "";
        isAcceptedByAllDbs = false;
        maxLength          = 0; //database dependent
        break;  
      case (java.sql.Types.CLOB):
        //<= 2 GB
        hasLengthAttribute = false;
        hasDecimals        = false;
        hasDefaultValue    = false;
        defaultValuePrefix = "";
        defaultValueSuffix = "";
		trimDefaultValue   = false;
        isAcceptedByAllDbs = true;
        maxLength          = 0;
        break;
      case (java.sql.Types.BLOB):
         //<= 2 GB
        hasLengthAttribute = false;
        hasDecimals        = false;
        hasDefaultValue    = false;
        defaultValuePrefix = "";
        defaultValueSuffix = "";
        isAcceptedByAllDbs = true;
        maxLength          = 0;
        break;
      case (java.sql.Types.SMALLINT):
        //-32767 <= x <= 32767
        hasLengthAttribute = false;
        hasDecimals        = false;
        hasDefaultValue    = true;
        defaultValuePrefix = "";
        defaultValueSuffix = "";
        defaultDefault     = "0";
        isAcceptedByAllDbs = true;
        maxLength          = 0;
        minValue           = "-32767";
        maxValue           = "32767";
        break;
      case (java.sql.Types.INTEGER):
        //-2147483647 <= x <= 2147483647
        hasLengthAttribute = false;
        hasDecimals        = false;
        hasDefaultValue    = true;
        defaultValuePrefix = "";
        defaultValueSuffix = "";
        defaultDefault     = "0";
        isAcceptedByAllDbs = true;
        maxLength          = 0;
        minValue           = "-2147483647";
        maxValue           = "2147483647";
        break;
      case (java.sql.Types.BIGINT):
        hasLengthAttribute = false;
        hasDecimals        = false;
        hasDefaultValue    = true;
        defaultValuePrefix = "";
        defaultValueSuffix = "";
        defaultDefault     = "0";
        isAcceptedByAllDbs = true;
        maxLength          = 0;
        minValue           = "-9223372036854775808";
        maxValue           = "9223372036854775807";
        break;  
      case (java.sql.Types.DECIMAL):
        hasLengthAttribute = true;
        hasDecimals        = true;
        hasDefaultValue    = true;
        defaultValuePrefix = "";
        defaultValueSuffix = "";
        defaultDefault     = "0";
        isAcceptedByAllDbs = true;
        maxLength          = 31;
        break;
      case (java.sql.Types.NUMERIC):
        hasLengthAttribute = true;
        hasDecimals        = true;
        hasDefaultValue    = true;
        defaultValuePrefix = "";
        defaultValueSuffix = "";
        defaultDefault     = "0";
        isAcceptedByAllDbs = true;
        maxLength          = 31;
        break;  
      case (java.sql.Types.FLOAT):
        //1,402e-45 <= x <= 3.402e+38
        hasLengthAttribute = false;
        hasDecimals        = false;
        hasDefaultValue    = true;
        defaultValuePrefix = "";
        defaultValueSuffix = "";
        defaultDefault     = "0";
        isAcceptedByAllDbs = false;
        maxLength          = 0;
        minValue           = "-1.402e-45";
        maxValue           = "3.402e+38";
        break;
      case (java.sql.Types.REAL):
        //1,402e-45 <= x <= 3.402e+38
        hasLengthAttribute = false;
        hasDecimals        = false;
        hasDefaultValue    = true;
        defaultValuePrefix = "";
        defaultValueSuffix = "";
        defaultDefault     = "0";
        isAcceptedByAllDbs = true;
        maxLength          = 0;
        minValue           = "-1.402e-45";
        maxValue           = "3.402e+38";
        break;  
      case (java.sql.Types.DOUBLE):
        //1,402e-45 <= x <= 3.402e+38
        hasLengthAttribute = false;
        hasDecimals        = false;
        hasDefaultValue    = true;
        defaultValuePrefix = "";
        defaultValueSuffix = "";
        defaultDefault     = "0";
        isAcceptedByAllDbs = true;
        maxLength          = 0;
        minValue           = "-2.225e-308";
        maxValue           = "1.79769e+308";
        break;  
      case (java.sql.Types.DATE):
        hasLengthAttribute = false;
        hasDecimals        = false;
        hasDefaultValue    = true;
        defaultValuePrefix = "'";
        defaultValueSuffix = "'";
        isAcceptedByAllDbs = true;
        maxLength          = 0;
        formatter = FDATE;
        break;
      case (java.sql.Types.TIME):
        hasLengthAttribute = false;
        hasDecimals        = false;
        hasDefaultValue    = true;
        defaultValuePrefix = "'";
        defaultValueSuffix = "'";
        isAcceptedByAllDbs = true;
        maxLength          = 0;
        formatter = FTIME;
        break;
      case (java.sql.Types.TIMESTAMP):
        hasLengthAttribute = false;
        hasDecimals        = false;
        hasDefaultValue    = true;
        defaultValuePrefix = "'";
        defaultValueSuffix = "'";
        isAcceptedByAllDbs = true;
        maxLength          = 0;
        formatter = FTIMESTAMP;
        break;
     }
  }

  public void setHasLengthAttribute(boolean hasLengthAttribute ) {
    this.hasLengthAttribute = hasLengthAttribute;
  }

  public void setHasDecimals(boolean hasDecimals) {
    this.hasDecimals = hasDecimals;}

  public void setHasDefaultValue(boolean hasDefaultValue) {
    this.hasDefaultValue = hasDefaultValue;
  }

  public void setDefaultValuePrefix(String defaultValuePrefix) {
    this.defaultValuePrefix = defaultValuePrefix;
  }

  public void setDefaultValueSuffix(String defaultValueSuffix) {
    this.defaultValueSuffix = defaultValueSuffix;
  }

  public void setDefaultDefault(String defaultDefault) {
    this.defaultDefault = defaultDefault;
  }
  
  public void setIsAcceptedByAllDbs(boolean isAcceptedByAllDbs) {
    this.isAcceptedByAllDbs = isAcceptedByAllDbs;
  }

  public void setMaxLength(long maxLength) {
    this.maxLength = maxLength;
  }

  public void setMinValue(String minValue) {
    this.minValue = minValue;
  }

  public void setMaxValue(String maxValue) {
    this.maxValue = maxValue;
  }

  public void setDdlName(String ddlName) {
    this.ddlName = ddlName;
  }

  public void setDdlSuffix(String ddlSuffix) {
    this.ddlSuffix = ddlSuffix;
  }

  public void setDbTypeName(String dbTypeName) {
    this.dbTypeName = dbTypeName;
  }

  public void setDdlDefaultLength(int ddlDefaultLength) {
    this.ddlDefaultLength = ddlDefaultLength;
  }

  public void setByteFactor(int byteFactor) {this.byteFactor = byteFactor;}

  public String getName() {return name;}

  public int getIntCode() {return intCode;}

  public boolean hasLengthAttribute() {return hasLengthAttribute;}

  public boolean hasByteFactor() {return (byteFactor != 0);}

  public boolean hasDecimals() {return hasDecimals;}

  public boolean hasDefaultValue() {return hasDefaultValue;}

  public String getDefaultValuePrefix() {return defaultValuePrefix;}

  public String getDefaultValueSuffix() {return defaultValueSuffix;}
  
  public String getDefaultDefault() {return defaultDefault;}
  
	public boolean trimDefaultValue() {return trimDefaultValue;}

  public boolean isAcceptedByAllDbs() {return isAcceptedByAllDbs;}

  public long maxLength() {return maxLength;}

  public String getMinValue() {return minValue;}

  public String getMaxValue() {return maxValue;}

  public String getDdlName() {return ddlName;}

  public String getDdlSuffix() {return ddlSuffix;}

  public String getDbTypeName() {return dbTypeName;}

  public int getDdlDefaultLength() {return ddlDefaultLength;}

  public int getByteFactor() {return byteFactor;}

  public static HashMap getStringLimits() {return stringLimits;}
  
  public static HashMap getBinaryLimits() {return binaryLimits;}
  
  public Format getFormatterForDefaultString() {return formatter;}

  public String toString() {
    return "JavaSqlTypeInfo = " + name + "\n" +
           "Integer Code           : " + intCode + "\n" +
           "Has Length Attribute   : " + hasLengthAttribute + "\n" +
           "Has Decimals           : " + hasDecimals + "\n" +
           "Has Default Value      : " + hasDefaultValue + "\n" +
           "Default Value Prefix   : " + defaultValuePrefix + "\n" +
           "Default Value Suffix   : " + defaultValueSuffix + "\n" +
           "Is Accepted by all Dbs : " + isAcceptedByAllDbs + "\n" +
           "Max Length             : " + maxLength + "\n" +
           "Byte Factor            : " + byteFactor + "\n";
  }
  
  public static short getShortDefault() {return SHORT_DEFAULT;}
  
  public static int getIntDefault() {return INTEGER_DEFAULT;}
  
  public static String getStringDefault() {return STRING_DEFAULT;}
  
  public static byte[] getByteDefault(int length) {return BINARY_DEFAULTS[length];}
  
  public static java.sql.Date getDateDefault() {return DATE_DEFAULT;}
  
  public static java.sql.Time getTimeDefault() {return TIME_DEFAULT;}
  
  public static java.sql.Timestamp getTimestampDefault() {return TIMESTAMP_DEFAULT;}

  public short getMaxDecimalLength() {return 31;}
}
