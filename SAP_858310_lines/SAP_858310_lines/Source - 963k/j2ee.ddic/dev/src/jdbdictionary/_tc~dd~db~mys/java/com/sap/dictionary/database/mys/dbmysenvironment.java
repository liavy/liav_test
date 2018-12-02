package com.sap.dictionary.database.mys;

import com.sap.dictionary.database.dbs.*;

import java.util.*;
import java.sql.*;

/**
 * Title:        
 * Description:  
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author       Hakan Kuecuekyilmaz, <hakan at mysql dot com>, 2005-07-10
 * @version 1.0
 */

public class DbMysEnvironment extends DbEnvironment {

    private final Connection connection;

    public DbMysEnvironment() {
        super();
        connection = null;
    }

    public DbMysEnvironment(Connection con) {
        super(con);
        connection = con;
    }

    // @todo Find reserved words for MySQL
    private static Set reserved = new HashSet<String>(Arrays.asList(new String[] {
            "ANALYZE", "ASENSITIVE", "BEFORE", "BIGINT", "BINARY", "BLOB",
            "CALL", "CHANGE", "COLUMNS", "CONDITION", "DATABASE", "DATABASES",
            "DAY_HOUR", "DAY_MICROSECOND", "DAY_MINUTE", "DAY_SECOND",
            "DELAYED", "DETERMINISTIC", "DISTINCTROW", "DIV", "ELSEIF",
            "ENCLOSED", "ESCAPED", "EXIT", "EXPLAIN", "FIELDS", "FORCE",
            "FRAC_SECOND", "FULLTEXT", "HIGH_PRIORITY", "HOUR_MICROSECOND",
            "HOUR_MINUTE", "HOUR_SECOND", "IF", "IGNORE", "INDEX", "INFILE",
            "INOUT", "ITERATE", "KEYS", "KILL", "LEAVE", "LIMIT", "LINES",
            "LOAD", "LOCALTIME", "LOCALTIMESTAMP", "LOCK", "LONG", "LONGBLOB",
            "LONGTEXT", "8LOOP", "LOW_PRIORITY", "MEDIUMBLOB", "MEDIUMINT",
            "MEDIUMTEXT", "MIDDLEINT", "MINUTE_MICROSECOND", "MINUTE_SECOND",
            "MOD", "NO_WRITE_TO_BINLOG", "OPTIMIZE", "OPTIONALLY", "OUT",
            "OUTFILE", "PURGE", "REGEXP", "RENAME", "REPEAT", "REPLACE",
            "REQUIRE", "RETURN", "RLIKE", "SECOND_MICROSECOND", "SENSITIVE",
            "SEPARATOR", "SHOW", "SONAME", "SPATIAL", "SPECIFIC",
            "SQLEXCEPTION", "SQLWARNING", "SQL_BIG_RESULT",
            "SQL_CALC_FOUND_ROWS", "SQL_SMALL_RESULT", "SQL_TSI_FRAC_SECOND",
            "SSL", "STARTING", "STRAIGHT_JOIN", "TABLES", "TERMINATED",
            "TINYBLOB", "TINYINT", "TINYTEXT", "UNDO", "UNLOCK", "UNSIGNED",
            "USE", "UTC_DATE", "UTC_TIME", "UTC_TIMESTAMP", "VARBINARY",
            "VARCHARACTER", "WHILE", "XOR", "YEAR_MONTH", "ZEROFILL" }));

    public static boolean isReservedWord(String name) {
        return reserved.contains(name.toUpperCase(Locale.US));
    }

    @Override
    public String getSchemaName() throws SQLException {
        final String defaultSchemaName = super.getSchemaName();
        if (defaultSchemaName != null) {
            return defaultSchemaName;
        }
        if (connection == null) {
            return null;
        }
        return connection.getCatalog();
    }
}