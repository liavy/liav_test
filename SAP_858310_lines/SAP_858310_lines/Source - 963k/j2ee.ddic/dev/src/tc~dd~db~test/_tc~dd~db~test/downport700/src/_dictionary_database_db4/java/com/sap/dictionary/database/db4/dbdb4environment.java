package com.sap.dictionary.database.db4;

import com.sap.dictionary.database.dbs.DbEnvironment; 
import com.sap.dictionary.database.dbs.ExType; 
import com.sap.dictionary.database.dbs.JddException; 
import com.sap.dictionary.database.dbs.Logger; 
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;
import java.sql.Connection;
import java.sql.Types;
import java.util.ArrayList;
import java.text.SimpleDateFormat;

/**
 * Title:        DbDb4Environment
 * Copyright:    Copyright (c) 2002
 * Company:      SAP AG
 * @author       Michael Tsesis & Kerstin Hoeft, Dorothea Rink
 */


public class DbDb4Environment extends DbEnvironment {


    private static final Location loc = Logger.getLocation("db4.DbDb4Environment");
    private static final Category cat = Category.getCategory(Category.SYS_DATABASE,
                                                             Logger.CATEGORY_NAME);


    private Connection con;
    
    private static ArrayList reservedWords;
    

    private static final int  MAX_COLUMN_NAME_LENGTH        = 30;
    private static final int  MAX_TABLE_NAME_LENGTH     = 127;
    private static final int  MAX_VIEW_NAME_LENGTH      = 127;
    private static final int  MAX_INDEX_NAME_LENGTH     = 127;
    private static final int  MAX_CONSTRAINT_NAME_LENGTH    = 127;
    
    private static final long MAX_BLOB_LENGTH           = 2147483647;   // 2GB - 1B
    private static final long MAX_CLOB_LENGTH_BYTES     = 2147483646;   // 2GB - 1B
    private static final long MAX_BINARY_LENGTH         = 32000;
    private static final long MAX_VARBINARY_LENGTH      = 32000;
    private static final long MAX_VARCHAR_LENGTH_BYTES  = 32000;        // 32768 B - overhead
    
    private static final long DFT_BLOB_LENGTH           = 2147483647;   // 2GB - 1B
    private static final long DFT_CLOB_LENGTH_BYTES     = 2147483647;

    private static final int  MAX_DECIMAL_LENGTH_CHARS  = 31;
    
    private static final int  MAX_TABLE_WIDTH_BYTES     = 32767;        // 32kB - 1B
    private static final int  MAX_VIEW_WIDTH_BYTES      = 32767;        
    private static final int  MAX_INDEX_WIDTH_BYTES     = 2000;

    private static final int  MAX_COLUMNS_PER_TABLE     = 8000;
    private static final int  MAX_COLUMNS_PER_VIEW      = 8000;
    private static final int  MAX_COLUMNS_PER_INDEX     = 120;
    private static final int  MAX_COLUMNS_PER_PRIMKEY   = 120;
    private static final int  MAX_INDEXES_PER_TABLE     = 3500;         // ~ 4000

    private static final int  MAX_TABLES_PER_STMT       = 256;
    private static final int  MAX_TABLES_PER_VIEW       = 32;


    public static int getMaxTableWidthBytes() { return MAX_TABLE_WIDTH_BYTES; }
    public static int getMaxViewWidthBytes() { return MAX_VIEW_WIDTH_BYTES; }
    public static int getMaxIndexWidthBytes() { return MAX_INDEX_WIDTH_BYTES; }

    public static int getMaxColumnsPerTable() { return MAX_COLUMNS_PER_TABLE; }
    public static int getMaxColumnsPerView() { return MAX_COLUMNS_PER_VIEW; }
    public static int getMaxColumnsPerIndex() { return MAX_COLUMNS_PER_INDEX; }
    public static int getMaxColumnsPerPrimKey() { return MAX_COLUMNS_PER_PRIMKEY; }
    public static int getMaxIndexesPerTable() { return MAX_INDEXES_PER_TABLE; }

    public static int getMaxTablesPerStmt() { return MAX_TABLES_PER_STMT; }
    public static int getMaxTablesPerView() { return MAX_TABLES_PER_VIEW; }
    
    public static int getMaxColumnNameLength() { return MAX_COLUMN_NAME_LENGTH; }
    public static int getMaxTableNameLength() { return MAX_TABLE_NAME_LENGTH; }
    public static int getMaxViewNameLength() { return MAX_VIEW_NAME_LENGTH; }
    public static int getMaxIndexNameLength() { return MAX_INDEX_NAME_LENGTH; }
    public static int getMaxConstraintNameLength() { return MAX_CONSTRAINT_NAME_LENGTH; }
    
    public static long getMaxClobLengthBytes() { return MAX_CLOB_LENGTH_BYTES; }
    public static long getMaxBlobLength() { return MAX_BLOB_LENGTH; }
    public static long getMaxVarcharLengthBytes() { return MAX_VARCHAR_LENGTH_BYTES; }
    public static long getMaxVarbinaryLength() { return MAX_VARBINARY_LENGTH; }
    public static long getMaxBinaryLength() { return MAX_BINARY_LENGTH; }
    public static long getDftClobLengthBytes() { return DFT_CLOB_LENGTH_BYTES; }
    public static long getDftBlobLength() { return DFT_BLOB_LENGTH; }
    public static long getMaxDecimalLengthChars() { return MAX_DECIMAL_LENGTH_CHARS; }
    
    public DbDb4Environment() {}

    public DbDb4Environment(Connection con) {
        super(con);
    }
    

    /**
     * Given the Db4 database type of a column, this returns 
     * the sql type.
     * @return - sql type (JDBC type)
     */ 
    public static int mapToSqlType (String dbType, int length, int ccsid) {
        int sqlType = java.sql.Types.OTHER; 
        
        // Invalid argument
        if (dbType == null) {
            cat.warningT(loc, "Empty database type. Mapped to {0}",
                                    new Object[] {new Integer(sqlType)});
            loc.exiting();
            return sqlType;
        }
        
        
        /*
         * LONGVARCHAR/VARBINARY types not needed on DB4. 
         * If a user demands a field of type LONGVARCHAR/VARBINARY 
         * at design time, for DB4, JDDIC tools overwrite that 
         * with VARCHAR/VARBINARY at deploy time. 
         * Consequently, mapping from DB->JDDIC always 
         * returns VARCHAR/VARBINARY.
         * However, DB4 JDDIC supports LONG types.  for in all
         */
        if (dbType.equals("VARG")) { 
            sqlType = java.sql.Types.VARCHAR;
            
        /*
         *  Note: there are no real (VAR)BINARY types, but (VAR)CHAR
         *        are only used in binary context
         */
        } else if (dbType.equals("VARCHAR") && (ccsid == 65535)) { 
            sqlType = java.sql.Types.VARBINARY;
        } else if (dbType.equals("CHAR") && (ccsid == 65535)) { 
            sqlType = java.sql.Types.BINARY;
        } else if (dbType.equals("SMALLINT")) { 
            sqlType = java.sql.Types.SMALLINT;
        } else if (dbType.equals("INTEGER")) { 
            sqlType = java.sql.Types.INTEGER;
        } else if (dbType.equals("BIGINT")) { 
            sqlType = java.sql.Types.BIGINT;
        } else if (dbType.equals("FLOAT")) {
            if (length == 4) {
                sqlType = java.sql.Types.REAL;
            } else {
                sqlType = java.sql.Types.DOUBLE;
            }
        } else if (dbType.equals("DECIMAL")) { 
            sqlType = java.sql.Types.DECIMAL;
        } else if (dbType.equals("DATE")) { 
            sqlType = java.sql.Types.DATE;
        } else if (dbType.equals("TIME")) { 
            sqlType = java.sql.Types.TIME;
        } else if (dbType.equals("TIMESTMP")) { 
            sqlType = java.sql.Types.TIMESTAMP;
        } else if (dbType.equals("DBCLOB")) { 
            sqlType = java.sql.Types.CLOB;
        } else if (dbType.equals("BLOB")) { 
            sqlType = java.sql.Types.BLOB;
        } else {
            cat.warningT(loc, "Unknown database type {0}", new Object[] {dbType});
        }
        loc.debugT(cat, "mapToSqlType({0}) returns {1}.", new Object[] {dbType, new Integer(sqlType)});
        return sqlType;
    
    }
    
    /**
     * Maps the database system catalog's representation of a default value of 
     * JDBC type sqlType on its JDDIC format. 
     * @return    JDDIC format of the default value
     **/
    public static String mapToJddicDefaultValue(String dbDefaultValue, int sqlType) 
                                                            throws JddException {
        loc.entering(cat, "mapToJddicDefaultValue(String, int)");
        SimpleDateFormat dbFormatter = null;
        String jddicDefaultValue = null;
        if (dbDefaultValue != null) {
            
            // ---------------------------------
            // ---------- STRING types ---------
            // ---------------------------------
            if (    (sqlType == Types.VARCHAR)
                 || (sqlType == Types.LONGVARCHAR)
                 || (sqlType == Types.CLOB)) {
                    
                if (    dbDefaultValue.startsWith("UX'") 
                    &&  dbDefaultValue.endsWith("'")) {
                    String strippedHexDefault 
                        = dbDefaultValue.substring(3, dbDefaultValue.length()-1);
                    /*
                     * At the time two table objects are compared to each other
                     * we don't know where they come from. => Convert hex string
                     * back to normal layout. 
                     */
                    jddicDefaultValue 
                        = new DbDb4HexString(null, strippedHexDefault).getJavaString(); 
                } else {
                    /*
                     * Tables with other than hex string default values will not be 
                     * handled correctly by JDDIC if defaults contains non-ECCS 
                     * characters. Since we create all tables known by JDDIC with
                     * hex defaults, this restriction should not hurt.
                     */
                    loc.exiting();
                    throw new JddException(ExType.OTHER, "Default value ");
                }
            } else {
                jddicDefaultValue = dbDefaultValue;
                
                // ---------------------------------
                // ------ DATE/TIME types ----------
                // ---------------------------------
                if (    (sqlType == Types.TIME)
                     || (sqlType == Types.TIMESTAMP)
                     || (sqlType == Types.DATE)) {
                        
                    // Strip off single quotes
                    if (jddicDefaultValue.startsWith("'")) {
                        jddicDefaultValue = 
                            jddicDefaultValue.substring(1, jddicDefaultValue.length() - 1);
                    }
                        
                    // Internal database jobs which update the system catalogs 
                    // use a fixed date format
                    if (sqlType == Types.TIME) {
                        dbFormatter = new SimpleDateFormat("HH.mm.ss");
                    } else if (sqlType == Types.DATE) {
                        dbFormatter = new SimpleDateFormat("yyyy-MM-dd");
                    } else if (sqlType == Types.TIMESTAMP) {
                        dbFormatter = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
                        // Workaround: strip off microseconds manually. 
                        // For whatever reason, "yyyy-MM-dd-HH.mm.ss.SSS000" does not work.
                        jddicDefaultValue = 
                            jddicDefaultValue.substring(1, jddicDefaultValue.length() - 3);
                    }
                    
                    // generate dummy info object to retrieve the formatter
                    JavaDb4SqlTypeInfo typeInfo = new JavaDb4SqlTypeInfo(null, null, sqlType);
                    SimpleDateFormat jddicFormatter = (SimpleDateFormat) 
                                                typeInfo.getFormatterForDefaultString();
                    try {
                        jddicDefaultValue 
                            = jddicFormatter.format(dbFormatter.parse(jddicDefaultValue));
                    } catch (Exception e) {     //$JL-EXC$
                        cat.errorT(loc, "Exception caught reformatting default value:\n"
                                        + "   {0}\n "
                                        + "   Failed to convert {1} from {2} to {3}.",
                                        new Object[] {e.toString(), 
                                                        dbDefaultValue, 
                                                        dbFormatter.toPattern(),
                                                        jddicFormatter.toPattern()});
                        loc.exiting();
                        throw JddException.createInstance(e);
                    }
                // ---------------------------------
                // ---------- OTHER types ----------
                // ---------------------------------
                } else {
                    loc.exiting();
                    return jddicDefaultValue;   // Leave value unchanged, don't log.
                }
            }

            loc.debugT(cat, "Converted default value {0} to {1}.",
                                new Object[] {dbDefaultValue, jddicDefaultValue});
        }
        loc.exiting();
        return jddicDefaultValue;
    }

    /**
     * Writes a trace message msg of category cat with parameters params 
     * depending on the boolean value of result. 
     * If trcFalseAsError == true, an error message gets written if result == false, 
     * an info message otherwise. If trcFalseAsError == false, the behaviour is reversed.
     **/
    public static void traceCheckResult(boolean trcFalseAsError, 
                                    boolean result, 
                                    Category realCat,
                                    Location realLoc,
                                    String message, 
                                    Object[] params) {
        if (trcFalseAsError ^ result) {
            realCat.errorT(realLoc, message, params);
        } else {
            realLoc.debugT(realCat, message, params);
        }
    }

}

    
