/* $Id: //engine/j2ee.ddic/dev/src/jdbdictionary/_tc~dd~db~jdb/java/com/sap/dictionary/database/jdb/DbJdbEnvironment.java#4 $
 * Last changelist: $Change: 257822 $
 * Last changed at: $DateTime: 2009/04/22 13:13:41 $
 * Last changed by: $Author: d019347 $
 */
package com.sap.dictionary.database.jdb;

import java.sql.Connection;
import java.util.Arrays;
import com.sap.dictionary.database.dbs.DbColumn;
import com.sap.dictionary.database.dbs.DbEnvironment;

public class DbJdbEnvironment extends DbEnvironment {
    private static final String[] reservedWords = new String[] { "ADD", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "AS",
            "ASC", "ASSERTION", "AT", "AUTHORIZATION", "AVG", "BEGIN", "BETWEEN", "BIGINT", "BIT", "BOOLEAN", "BOTH", "BY",
            "CALL", "CASCADE", "CASCADED", "CASE", "CAST", "CHAR", "CHARACTER", "CHECK", "CLOSE", "COALESCE", "COLLATE",
            "COLLATION", "COLUMN", "COMMIT", "CONNECT", "CONNECTION", "CONSTRAINT", "CONSTRAINTS", "CONTINUE", "CONVERT",
            "CORRESPONDING", "CREATE", "CURRENT", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER",
            "CURSOR", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFERRABLE", "DEFERRED", "DELETE", "DESC",
            "DESCRIBE", "DIAGNOSTICS", "DISCONNECT", "DISTINCT", "DOUBLE", "DROP", "ELSE", "END", "END-EXEC", "ESCAPE",
            "EXCEPT", "EXCEPTION", "EXEC", "EXECUTE", "EXISTS", "EXPLAIN", "EXTERNAL", "FALSE", "FETCH", "FIRST", "FLOAT",
            "FOR", "FOREIGN", "FOUND", "FROM", "FULL", "FUNCTION", "GET", "GETCURRENTCONNECTION", "GLOBAL", "GO", "GOTO",
            "GRANT", "GROUP", "HAVING", "HOUR", "IDENTITY", "IMMEDIATE", "IN", "INDICATOR", "INITIALLY", "INNER", "INOUT",
            "INPUT", "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTERSECT", "INTO", "IS", "ISOLATION", "JOIN", "KEY", "LAST",
            "LEFT", "LIKE", "LOWER", "LTRIM", "MATCH", "MAX", "MIN", "MINUTE", "NATIONAL", "NATURAL", "NCHAR", "NVARCHAR",
            "NEXT", "NO", "NOT", "NULL", "NULLIF", "NUMERIC", "OF", "ON", "ONLY", "OPEN", "OPTION", "OR", "ORDER", "OUTER",
            "OUTPUT", "OVERLAPS", "PAD", "PARTIAL", "PREPARE", "PRESERVE", "PRIMARY", "PRIOR", "PRIVILEGES", "PROCEDURE",
            "PUBLIC", "READ", "REAL", "REFERENCES", "RELATIVE", "RESTRICT", "REVOKE", "RIGHT", "ROLLBACK", "ROWS", "RTRIM",
            "SCHEMA", "SCROLL", "SECOND", "SELECT", "SESSION_USER", "SET", "SMALLINT", "SOME", "SPACE", "SQL", "SQLCODE",
            "SQLERROR", "SQLSTATE", "SUBSTR", "SUBSTRING", "SUM", "SYSTEM_USER", "TABLE", "TEMPORARY", "TIMEZONE_HOUR",
            "TIMEZONE_MINUTE", "TO", "TRANSACTION", "TRANSLATE", "TRANSLATION", "TRUE", "UNION", "UNIQUE", "UNKNOWN", "UPDATE",
            "UPPER", "USER", "USING", "VALUES", "VARCHAR", "VARYING", "VIEW", "WHENEVER", "WHERE", "WITH", "WORK", "WRITE",
            "XML", "XMLEXISTS", "XMLPARSE", "XMLSERIALIZE", "YEAR", };
    static {
        // just in case there were a mistake in the array
        Arrays.sort(reservedWords);
    }

    public DbJdbEnvironment() {
        super();
    }

    public DbJdbEnvironment(final Connection con) {
        super(con);
    }

    public static int MaxNameLength() {
        return 128;
    }

    public static int MaxTableLength() {
        return Integer.MAX_VALUE;
    }

    public static int MaxIndexLength() {
        return Integer.MAX_VALUE;
    }

    public static int MaxKeyLength() {
        return Integer.MAX_VALUE;
    }

    public static int MaxColumnsPerTable() {
        return 1012;
    }

    public static int MaxColumnsPerIndex() {
        return 1012;
    }

    public static int MaxKeysPerTable() {
        return 1012;
    }

    public static int MaxIndicesPerTable() {
        return 32767;
    }

    public static int MaxCharacterLength() {
        return 32672;
    }

    public static int MaxBinaryLength() {
        return 32672;
    }

    public static int MaxDecimalLength() {
        return 30;
    }

    public static long GetColumnLength(DbColumn col, boolean isKey) {
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
            len = (col.getLength() + 1) / 2 + 2;
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

    public static boolean isReservedWord(String id) {
        return Arrays.binarySearch(reservedWords, id) >= 0;
    }
}