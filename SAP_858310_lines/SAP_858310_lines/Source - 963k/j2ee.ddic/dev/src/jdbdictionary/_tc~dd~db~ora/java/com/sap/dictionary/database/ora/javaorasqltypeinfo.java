package com.sap.dictionary.database.ora;

import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.JavaSqlTypeInfo;

import java.sql.Types;

/**
 * Title:        Analysis of dictionary changes, Oracle-specific part
 * Description:  Oracle specific analysis of dictionary changes
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Andrea Neufeld & Tobias Wenner. Markus Maurer
 * @version 1.0
 */

/**
 * 
 */
public class JavaOraSqlTypeInfo
extends JavaSqlTypeInfo
{
   /**
    *
    */
   public static final int VARCHAR_LIMIT                   = 1333;
   public static final int VARBINARY_LIMIT                 = 2000;
   public static final int VARCHAR_WHERE_CONDITION_LIMIT   = 1333;
   public static final int VARBINARY_WHERE_CONDITION_LIMIT =  255;
   public static final int LONGVARCHAR_LIMIT               =    0;
   public static final int LONGVARBINARY_LIMIT             =    0;
   
   /**
    * Constructor.
    * <p>
    * @param DbFactory factory
    *    The factory used for creating this instance.
    * @param String name
    *    The type name.
    * @param int code
    *    The type code.
    */
   public JavaOraSqlTypeInfo(DbFactory factory, String name, int code)
   {
      super(factory, name, code);

      /*
       * Overriding the attributes of JavaSqlTypeInfo,
       * if necessary
       */
      switch(code)
      {
         case java.sql.Types.CHAR:           this.setByteFactor(1);              // NCHAR(n) has character semantics!
                                             this.setDdlName("NCHAR");
      
                                             break;

        case java.sql.Types.VARCHAR:         this.setByteFactor(1);              // NVARCHAR2(n) has character semantics!
                                             this.setDdlName("NVARCHAR2");       

                                             break;

        case java.sql.Types.LONGVARCHAR:     this.setByteFactor(1);              // NVARCHAR2(n) has character semantics!
                                             this.setDdlName("NVARCHAR2");       

                                             break;

        case java.sql.Types.BINARY:          this.setDdlName("RAW");
                                             this.setDefaultValuePrefix("'");
                                             this.setDefaultValueSuffix("'");
                                             
                                             break;

        case java.sql.Types.VARBINARY:       this.setDdlName("RAW");
                                             this.setDefaultValuePrefix("'");
                                             this.setDefaultValueSuffix("'");

                                             break;

        case java.sql.Types.LONGVARBINARY:   this.setDdlName("RAW");
                                             this.setDefaultValuePrefix("'");
                                             this.setDefaultValueSuffix("'");

                                             break;

        case java.sql.Types.BIT:             this.setDdlName("NUMBER");
                                             this.setDbTypeName("NUMBER");
                                             this.setDdlDefaultLength(1);
                
                                             break;

        case java.sql.Types.SMALLINT:        this.setDdlName("NUMBER");          // -32767 <= x <= 32767.
                                             this.setDbTypeName("NUMBER");       // SQL type SMALLINT within the CREATE statement
                                             this.setDdlDefaultLength(5);        // results in NUMBER(38). Use NUMBER(5) instead.
                
                                             break;

        case java.sql.Types.INTEGER:         this.setDdlName("NUMBER");          // -2147483647 <= x <= 2147483647
                                             this.setDbTypeName("NUMBER");       // SQL type INTEGER within the CREATE statement
                                             this.setDdlDefaultLength(10);       // results in NUMBER(38). Use NUMBER(10) instead.
       
                                             break;

        case java.sql.Types.BIGINT:          this.setDdlName("NUMBER");
                                             this.setDbTypeName("NUMBER");
                                             this.setDdlDefaultLength(19);
        
                                             break;

        case java.sql.Types.FLOAT:           this.setDdlName("FLOAT");
                                             this.setDbTypeName("FLOAT");
        
                                             break;

        case java.sql.Types.DOUBLE:          this.setDdlName("FLOAT");
                                             this.setDbTypeName("FLOAT");
       
                                             break;

        case java.sql.Types.DATE:            this.setDdlName("DATE");
                                             this.setDefaultValuePrefix("DATE '");
                                             this.setDefaultValueSuffix("'");

                                             break;

        case java.sql.Types.TIME:            this.setDdlName("DATE");
                                             this.setDefaultValuePrefix("TO_DATE('1900-01-01 ");
                                             this.setDefaultValueSuffix("', 'YY-MM-DD HH24:MI:SS')");

                                             break;

        case java.sql.Types.TIMESTAMP:       this.setDdlName("TIMESTAMP");
                                             this.setDefaultValuePrefix("TIMESTAMP '");
                                             this.setDefaultValueSuffix("'");

                                             break;

        case java.sql.Types.CLOB:            this.setHasLengthAttribute(false);
                                             this.setDdlName("NCLOB");
                                             this.setHasDefaultValue(true);
                                             this.setDefaultValuePrefix("'");
                                             this.setDefaultValueSuffix("'");
        
                                             break;

        case java.sql.Types.BLOB:            this.setHasLengthAttribute(false);
                                             this.setHasDefaultValue(true);
                                             this.setDefaultValuePrefix("'");
                                             this.setDefaultValueSuffix("'");
        
                                             break;

         default:                            ;
      }
   }

   /**
    * Returns the maximum allowed decimal
    * length.
    * <p>
    * @return short
    *    The maximum allowed decimal length.
    */
   public short getMaxDecimalLength()
   {
      return 38;
   }
}
