package com.sap.dictionary.database.mys;

import com.sap.dictionary.database.dbs.*;

/**
 * Title:        Analysis of table and view changes: MySQL specific classes
 * Description:  type mapping
 * Copyright:    
 * Company:      SAP AG
 * @author       Eric Herman MySQL AB, Hakan Kuecuekyilmaz MySQL AB       
 * @version 1.0
 */

public class JavaMysSqlTypeInfo extends JavaSqlTypeInfo {

    public static final int VARCHAR_WHERE_CONDITION_LIMIT = 255;

    public static final int VARCHAR_LIMIT = 65532;

    public static final int LONGVARCHAR_LIMIT = 65532;

    public static final int VARBINARY_WHERE_CONDITION_LIMIT = 255;

    public static final int VARBINARY_LIMIT = 65532;

    public static final int LONGVARBINARY_LIMIT = 65532;

    public JavaMysSqlTypeInfo(DbFactory factory, String name, int intCode) {
        super(factory, name, intCode);

        // Override of attributes of JavaSqlTypeInfo if necessary.
        // setDbTypeName() in which cases?
        // setDdlName()
        // setDefaultValuePrefix()
        // setDefaultValueSuffix()
        // setHasDefaultValue()
        // setHasLengthAttribute(false);

        setDefaultValuePrefix("");
        setDefaultValueSuffix("");
        
        switch (intCode) {
        case (java.sql.Types.CHAR):
            setDbTypeName("CHAR");
            setDefaultValuePrefix("'");
            setDefaultValueSuffix("'");
            break;
        case (java.sql.Types.LONGVARCHAR):
        case (java.sql.Types.VARCHAR):
            setDdlName("VARCHAR");
            setDefaultValuePrefix("'");
            setDefaultValueSuffix("'");
            break;
        case (java.sql.Types.BINARY):
            setDbTypeName("BINARY");
            setDefaultValuePrefix("0x");
            break;
        case (java.sql.Types.LONGVARBINARY):
        case (java.sql.Types.VARBINARY):
            setDdlName("VARBINARY");
            setDefaultValuePrefix("0x");
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
            setDbTypeName("FLOAT");
            break;
        case (java.sql.Types.DOUBLE):
            //1,402e-45 <= x <= 3.402e+38
            setDbTypeName("DOUBLE");
            break;
        case (java.sql.Types.CLOB):       
            setDdlName("LONGTEXT");
            setDefaultValuePrefix("0x");
            setHasLengthAttribute(false);
            break;
        case (java.sql.Types.BLOB):
            setDbTypeName("LONGBLOB");
            setDdlName("LONGBLOB");
            setDefaultValuePrefix("0x");
            setHasLengthAttribute(false);
            break;
        case (java.sql.Types.DATE):
            setDbTypeName("DATE");
            setDefaultValuePrefix("'");
            setDefaultValueSuffix("'");
            break;
        case (java.sql.Types.TIME):
            setDbTypeName("TIME");
            setDefaultValuePrefix("'");
            setDefaultValueSuffix("'");
            break;
        case (java.sql.Types.TIMESTAMP):
            // setDbTypeName("DATETIME");
            setDdlName("DATETIME");
            setDefaultValuePrefix("'");
            setDefaultValueSuffix("'");
            break;
        }
    }
}