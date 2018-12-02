package com.sap.dictionary.database.db4;

import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.JavaSqlTypeInfo;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import java.sql.Types;
import java.text.Format;
import java.text.SimpleDateFormat;

/**
 * Title:        JavaDb4SqlTypeInfo
 * Copyright:    Copyright (c) 2002
 * Company:      SAP AG
 * @author       Michael Tsesis & Kerstin Hoeft, Dorothea Rink
 */


public class JavaDb4SqlTypeInfo extends JavaSqlTypeInfo {


    private static final SimpleDateFormat FDATE_TARGET = 
                     new SimpleDateFormat("yyyy-MM-dd");    // like FDATE
                     
    private static final SimpleDateFormat FTIME_TARGET =   
                     new SimpleDateFormat("HH:mm:ss");
                     
    private static final SimpleDateFormat FTIMESTAMP_TARGET =
                     new SimpleDateFormat("yyyy-MM-dd-H.mm.ss.SSS000");
                     
    private Format targetFormatter = null;                   
    
    private static final Location loc = Logger.getLocation("db4.JavaDb4SqlTypeInfo");
    private static final Category cat = Category.getCategory(Category.SYS_DATABASE,
                                                             Logger.CATEGORY_NAME);

    /*
     * The max values given below only hold if the table does not
     * have any other columns. Thus, JDD will 
     * 1) test the single string fields against the max values given below 
     * 2) test whether the fixed row length (32kB) would not be exceeded.
     *    If so, the user gets the chance to manually choose for a lob 
     *    type instead.
     *
     * How this is calculated:
     *                   
     *                  (32 kB - 1 B) - 64 B overhead per table
     *                                -  8 B null byte map
     *                                -  2 B length field
     */
    public static final int VARCHAR_LIMIT                   = 16346;
    public static final int VARBINARY_LIMIT                     = 32692;

    /*
     * Does not apply. If VARCHAR/VARBINARY is not usable, we 
     * have to use lob's.
     */
    public static final int LONGVARCHAR_LIMIT               = 0;
    public static final int LONGVARBINARY_LIMIT                 = 0;
    
    /*
     * VARCHAR/VARBINARY are comparable over their full field length.
     */
    public static final int VARCHAR_WHERE_CONDITION_LIMIT   = 16346;
    public static final int VARBINARY_WHERE_CONDITION_LIMIT     = 32692;

    //----------------
    //  constructors  ---------------------------------------------------------
    //----------------
    
    public JavaDb4SqlTypeInfo(  DbFactory factory, 
                                String name, 
                                int intCode) {
        super(  factory,
                name,
                intCode);
    
        switch (intCode) {
        
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                /*
                 * VARCHAR:
                 *   JDDIC: 1 <= length (chars) <=   127
                 *   DB4:   1 <= length (chars) <= 32767
                 * LONGVARCHAR:
                 *   JDDIC: 128 <= length (chars) <=  1333
                 *   DB4:     1 <= length (chars) <= 32767
                 */
                setDdlName("VARGRAPHIC");
                setDdlSuffix(" CCSID 13488");
                /*
                 * These are the delimiters that common JDDIC coding expects around
                 * default values when returned from db. We use a string default value's 
                 * hex representation if (and only if) we talk to database, and then 
                 * generate the hex string delimiters UX'' under the covers.
                 * it is thus a little safer to let common JDDIC think there are no 
                 * delimiters for string type default values.
                 */
                setDefaultValuePrefix("");
                setDefaultValueSuffix("");
                /* 
                 * Though we use UCS-2, byteFactor should be equal to 1, 
                 * as in DDL statements, the database wants the length 
                 * in characters, not in bytes. 
                 */
                setByteFactor(1);
                break;
                    
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                /*
                 * VARBINARY:
                 *   JDDIC: 1 <= length (byte) <=   255
                 *   DB4:   1 <= length (byte) <= 32767
                 * LONGVARBINARY:
                 *   JDDIC: 256 <= length (byte) <=  2000
                 *   DB4:     1 <= length (byte) <= 32767
                 */
                setDdlName("VARCHAR");
                setDdlSuffix(" FOR BIT DATA");
                break;
                
            case Types.BINARY:
                /*
                 * BINARY:
                 *   JDDIC: 1 <= length (byte) <=   255
                 *   DB4:   1 <= length (byte) <= 32767
                 */
                setDdlName("CHAR");
                setDdlSuffix(" FOR BIT DATA");
                setDefaultValuePrefix("X'");
                setDefaultValueSuffix("'");
                break;
                
            case Types.CLOB:
                setDdlName("DBCLOB");
                setDdlSuffix(" CCSID 13488");
                /* 
                 * Though we use UCS-2, byteFactor should be equal to 1, 
                 * as in DDL statements, the database wants the length 
                 * in characters, not in bytes. 
                 */
                setByteFactor(1);
                setHasLengthAttribute(true);
                setDdlDefaultLength((int)DbDb4Environment.getDftClobLengthBytes() / 2);
                setMaxLength((int)DbDb4Environment.getMaxClobLengthBytes() / 2);
                
                setDefaultValuePrefix("");  // --> (LONG)VARCHAR
                setDefaultValueSuffix("");
                break;
                
            case Types.BLOB:
                setDdlName("BLOB");
                setHasLengthAttribute(true);
                setDdlDefaultLength((int)DbDb4Environment.getDftBlobLength());
                setMaxLength((int)DbDb4Environment.getMaxBlobLength());
                break;
                
            case Types.SMALLINT:
                /*
                 * JDDIC: -32767 <= x <= 32767
                 * DB4:   -32768 <= x <= 32767 => ok
                 */
                setDdlName("SMALLINT");
                break;
                
            case Types.INTEGER:
                /*
                 * JDDIC: -2147483647 <= x <= 2147483647
                 * DB4:   -2147483648 <= x <= 2147483647 => ok
                 */
                setDdlName("INTEGER");
                break;
        
            case Types.BIGINT:
                /*
                 * JDDIC: -9223372036854775808 <= x <= 9223372036854775807;
                 * DB4:   same
                 */
                setDdlName("BIGINT");
                break;
                
            case Types.REAL:
                /*
                 * JDDIC: +/-1.175e-37      <= x <= +/-3.4e+38
                 * DB4:   +/-1.17549436e-38 <= x <= +/-3.40282356e+38
                 */
                setDdlName("REAL");
                break;
                
            case Types.DOUBLE:
                /*
                 * JDDIC: +/-1.0e-64    <= x <= +/-9.9e+62
                 * DB4:   +/-2.225e-308 <= x <= +/-1.79769e+308
                 */
                setDdlName("DOUBLE");
                break;
                
            case Types.DECIMAL:
                setHasLengthAttribute(true);
                setHasDecimals(true);
                setDefaultValuePrefix("");
                setDefaultValueSuffix("");
                setDdlName("DECIMAL");
                break;
                
            case Types.TIME:
                setDdlName("TIME");
                setDefaultValuePrefix("'");
                setDefaultValueSuffix("'");
                targetFormatter = FTIME_TARGET;         
                break;
                
            case Types.TIMESTAMP:
                setDdlName("TIMESTAMP");
                setDefaultValuePrefix("'");
                setDefaultValueSuffix("'");
                targetFormatter = FTIMESTAMP_TARGET;            
                break;
                
            case Types.DATE:
                setDdlName("DATE");
                setDefaultValuePrefix("'");
                setDefaultValueSuffix("'");
                targetFormatter = FDATE_TARGET;         
                break;
                
            default:
                // ----------------------------------------------------------------- //
                // Temporarily commented out: currently, type objects are generated  //
                // generated for all types regardless whether they are valid or not. //
                // ----------------------------------------------------------------- //
                // cat.errorT(loc, "Unknown type {0}.", new Object[] {new Integer(intCode)});
        }
    }

    public Format getTargetFormatterForDefaultString() {
        return targetFormatter;
    }

    /**
     * Returns the maximum precision of type decimal on DB4 
     */
    public short getMaxDecimalLength() {
        
        // --------------------------------------------- //
        // Minimal DB release V5R3; prior to that: 31    // 
        // --------------------------------------------- //
        return 63;
    } 

}
