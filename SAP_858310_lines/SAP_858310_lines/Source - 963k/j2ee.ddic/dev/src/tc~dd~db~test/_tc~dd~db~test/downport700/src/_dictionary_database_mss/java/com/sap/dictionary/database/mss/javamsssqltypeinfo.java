package com.sap.dictionary.database.mss;

import com.sap.dictionary.database.dbs.*;

/**
 * Title:        Analysis of table and view changes: MS SQL Server specific classes
 * Description:  MS SQL Server specific analysis of table and view changes. Tool to deliver MS SQL Server specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author       Guenther Drach
 * @version 1.0
 */

public class JavaMssSqlTypeInfo extends JavaSqlTypeInfo {

  public static final int VARCHAR_WHERE_CONDITION_LIMIT = 4000;
  public static final int VARCHAR_LIMIT = 4000;
  public static final int LONGVARCHAR_LIMIT = 0;

  public static final int VARBINARY_WHERE_CONDITION_LIMIT = 8000;
  public static final int VARBINARY_LIMIT = 8000;
  public static final int LONGVARBINARY_LIMIT = 0;

  public JavaMssSqlTypeInfo(DbFactory factory, String name, int intCode) {
    super(factory, name,intCode);

    //Override of attributes of JavaSqlTypeInfo if necessary.
    //setDbTypeName() in which cases?
    setDefaultValuePrefix("");
    setDefaultValueSuffix("");
    switch (intCode)
    {
      case (java.sql.Types.CHAR):
	    setDdlName("NCHAR"); // Unicode
        setDefaultValuePrefix("N'");
        setDefaultValueSuffix("'");
        setHasDefaultValue(true);
        break;
      case (java.sql.Types.LONGVARCHAR):
      case (java.sql.Types.VARCHAR):
        setDdlName("NVARCHAR"); // Unicode
        setDefaultValuePrefix("N'");
        setDefaultValueSuffix("'");
        setHasDefaultValue(true);
        break;
      case (java.sql.Types.BINARY):
        setDdlName("BINARY");
        setDefaultValuePrefix("0x");
        setHasDefaultValue(true); // testing testing
        break;
      case (java.sql.Types.LONGVARBINARY):
      case (java.sql.Types.VARBINARY):
        setDdlName("VARBINARY");
        setDefaultValuePrefix("0x");
        setHasDefaultValue(true); // testing testing
        break;
      case (java.sql.Types.SMALLINT):
        //-32767 <= x <= 32767
        setDbTypeName("SMALLINT");
        break;
      case (java.sql.Types.INTEGER):
         //-2147483647 <= x <= 2147483647
         setDbTypeName("INTEGER");
        break;
      case (java.sql.Types.BIGINT):
         //-9223372036854775808 <= x <= 9223372036854775807
         setDbTypeName("BIGINT");
        break;
      case (java.sql.Types.FLOAT):
        //1,402e-45 <= x <= 3.402e+38
        setDdlName("REAL");
        break;
      case (java.sql.Types.DOUBLE):
        //1,402e-45 <= x <= 3.402e+38
        setDdlName("FLOAT");
        break;
      case (java.sql.Types.CLOB):
        setDdlName("NTEXT"); // Unicode!
        setDefaultValuePrefix("0x");
        setHasLengthAttribute(false);
        break;
      case (java.sql.Types.BLOB):
        setDdlName("IMAGE");
        setDefaultValuePrefix("0x");
        setHasLengthAttribute(false);
        break;
      case (java.sql.Types.DATE):
        setDdlName("DATETIME");
        // gd 020403 just use strings, will avoid timestamp problem
        // setDefaultValuePrefix("{d '");
        // setDefaultValueSuffix("'}");
        setDefaultValuePrefix("'");
        setDefaultValueSuffix("'");
        break;
      case (java.sql.Types.TIME):
        setDdlName("DATETIME");
        // gd 020403 just use strings, will avoid timestamp problem
        // setDefaultValuePrefix("{ts '1900-01-01 ");
        //setDefaultValueSuffix("'}");
        setDefaultValuePrefix("'");
        setDefaultValueSuffix("'");
        break;
      case (java.sql.Types.TIMESTAMP):
        setDdlName("DATETIME");
        // gd 020403 just use strings, will avoid timestamp problem
        // setDefaultValuePrefix("{ts '");
        // setDefaultValueSuffix("'}");
        setDefaultValuePrefix("'");
        setDefaultValueSuffix("'");
        break;
    }
  }

  public short getMaxDecimalLength() {return 38;}

}