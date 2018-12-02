/* $Id: //engine/j2ee.ddic/dev/src/jdbdictionary/_tc~dd~db~jdb/java/com/sap/dictionary/database/jdb/JavaJdbSqlTypeInfo.java#4 $
 * Last changelist: $Change: 257822 $
 * Last changed at: $DateTime: 2009/04/22 13:13:41 $
 * Last changed by: $Author: d019347 $
 */
package com.sap.dictionary.database.jdb;

import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.JavaSqlTypeInfo;

public class JavaJdbSqlTypeInfo extends JavaSqlTypeInfo {
    public JavaJdbSqlTypeInfo(DbFactory factory, String name, int intCode) {
        super(factory, name, intCode);
        switch (intCode) {
        case (java.sql.Types.CHAR):
            setDdlName("VARCHAR");
            break;
        case (java.sql.Types.BINARY):
            setDdlName("VARCHAR");
            setDdlSuffix(" FOR BIT DATA");
            setDefaultValuePrefix("x'");
            break;
        case (java.sql.Types.VARCHAR):
            break;
        case (java.sql.Types.VARBINARY):
            setDdlName("VARCHAR");
            setDdlSuffix(" FOR BIT DATA");
            setDefaultValuePrefix("x'");
            break;
        case (java.sql.Types.LONGVARCHAR):
            setDdlName("VARCHAR");
            break;
        case (java.sql.Types.LONGVARBINARY):
            setDdlName("VARCHAR");
            setDdlSuffix(" FOR BIT DATA");
            setDefaultValuePrefix("x'");
            break;
        case (java.sql.Types.CLOB):
            break;
        case (java.sql.Types.BLOB):
            break;
        case (java.sql.Types.SMALLINT):
            break;
        case (java.sql.Types.INTEGER):
            break;
        case (java.sql.Types.BIGINT):
            break;
        case (java.sql.Types.DECIMAL):
            break;
        case (java.sql.Types.FLOAT):
            break;
        case (java.sql.Types.DOUBLE):
            break;
        case (java.sql.Types.REAL):
            break;
        case (java.sql.Types.DATE):
            break;
        case (java.sql.Types.TIME):
            break;
        case (java.sql.Types.TIMESTAMP):
            break;
        case (java.sql.Types.BIT):
            setDdlName("SMALLINT");
            break;
        }
    }
}
