package com.sap.dictionary.database.sap;

import com.sap.dictionary.database.dbs.*;
import java.text.Format;
import java.text.SimpleDateFormat;

/**
 * Title:        Analysis of table and view changes: SAPDB specific classes
 * Description:  SAP DB specific analysis of table and view changes. Tool to deliver SAPDB specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @autor:       Thomas Elvers
 * @version 1.0
 */

public class JavaSapSqlTypeInfo extends JavaSqlTypeInfo {

  public static final int VARCHAR_WHERE_CONDITION_LIMIT = 4000;
  public static final int VARCHAR_LIMIT = 4000;
  public static final int LONGVARCHAR_LIMIT = 0;

  public static final int VARBINARY_WHERE_CONDITION_LIMIT = 255;
  public static final int VARBINARY_LIMIT = 255;
  public static final int LONGVARBINARY_LIMIT = 8000;
  
  private static final SimpleDateFormat FDATE = new SimpleDateFormat("yyyy-MM-dd");
  private static final SimpleDateFormat FTIME = new SimpleDateFormat("HH:mm:ss");
  private static final SimpleDateFormat FTIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
                     
  private Format targetFormatter = null;                   

  public JavaSapSqlTypeInfo(DbFactory factory, String name, int intCode) {
    super(factory,name,intCode);

    switch (intCode) {

      case (java.sql.Types.CHAR):
        setDdlName("CHAR");
        setDdlSuffix(" UNICODE");
        setHasDefaultValue(true);
        setDefaultValuePrefix("'");
        setDefaultValueSuffix("'");
        break;

      case (java.sql.Types.BINARY):
        setDdlName("CHAR");
        setDdlSuffix(" BYTE");
        setHasDefaultValue(true);
        setDefaultValuePrefix("x'");
        setDefaultValueSuffix("'");
        break;

      case (java.sql.Types.VARCHAR):
      case (java.sql.Types.LONGVARCHAR):
        setDdlName("VARCHAR");
        setDdlSuffix(" UNICODE");
        setHasDefaultValue(true);
        setDefaultValuePrefix("'");
        setDefaultValueSuffix("'");
        break;

      case (java.sql.Types.VARBINARY):
        setDdlName("VARCHAR");
        setDdlSuffix(" BYTE");
        setHasDefaultValue(true);
        setDefaultValuePrefix("x'");
        setDefaultValueSuffix("'");
        break;
        
      case (java.sql.Types.LONGVARBINARY):
        setDdlName("LONG");
        setDdlSuffix(" BYTE");
        setHasDefaultValue(true);
        setDefaultValuePrefix("x'");
        setDefaultValueSuffix("'");
        setHasLengthAttribute(false);
        break;
        
      case (java.sql.Types.CLOB):
        setDdlName("LONG");
        setDdlSuffix(" UNICODE");
        break;

      case (java.sql.Types.BLOB):
        setDdlName("LONG");
        setDdlSuffix(" BYTE");
        break;

      case (java.sql.Types.SMALLINT):
      case (java.sql.Types.INTEGER):
        break;

      case (java.sql.Types.BIGINT):
        setDdlName("FIXED");
        setDdlDefaultLength(19);
        setHasLengthAttribute(true);
        break;

      case (java.sql.Types.DECIMAL):
        setDdlName("FIXED");
        break;

      case (java.sql.Types.FLOAT):
      case (java.sql.Types.DOUBLE):
        setDdlName("FLOAT");
        setDdlDefaultLength(38);
        setHasLengthAttribute(true);
        break;
        
     case (java.sql.Types.REAL):
        setDdlName("FLOAT");
        setDdlDefaultLength(16);
        setHasLengthAttribute(true);
        break;
        
      case (java.sql.Types.DATE):
        targetFormatter = FDATE;
        break;
              
      case (java.sql.Types.TIME):
        targetFormatter = FTIME;
        break;
        
      case (java.sql.Types.TIMESTAMP):
        targetFormatter = FTIMESTAMP;
        break;
        
      case (java.sql.Types.BIT):
        setDdlName("SMALLINT");
        setHasDefaultValue(true);
        setHasLengthAttribute(false);
        break;
    }
  }


  public Format getTargetFormatterForDefaultString() {
    return targetFormatter;
  }
  
}
