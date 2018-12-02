package com.sap.dictionary.database.db6;

import com.sap.dictionary.database.dbs.*;

/**
 * Title:        Analysis of table and view changes: DB6 specific classes
 * Description:  DB6 specific analysis of table and view changes. Tool to deliver DB6 specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author       Frank-Martin Haas , Frank-Herwig Walter
 * @version 1.0
 */

public class JavaDb6SqlTypeInfo extends JavaSqlTypeInfo
{

  public static final int VARCHAR_WHERE_CONDITION_LIMIT = 5431;
  public static final int VARCHAR_LIMIT                 = 5431;    // 16K pagesize
  public static final int LONGVARCHAR_LIMIT             = 5431;    // LONGVARCHAR range 128..1333

  public static final int VARBINARY_WHERE_CONDITION_LIMIT = 16293;
  public static final int VARBINARY_LIMIT                 = 16293;  // 16K pagesize
  public static final int LONGVARBINARY_LIMIT             = 16293;  // LONGVARBINARY range 256..2000


  public JavaDb6SqlTypeInfo( DbFactory factory, String name, int intCode )
  {
    super( factory, name, intCode );

    //
    // DB6 specific changes of attributes of JavaSqlTypeInfo
    //
    switch ( intCode )
    {
       case java.sql.Types.CHAR:
       case java.sql.Types.LONGVARCHAR:  // OPEN SQL uses LONGVARCHAR as VARCHAR
       case java.sql.Types.VARCHAR:
        //
        // we use UTF-8 data types for CHAR types
        //
        setDdlName( "VARCHAR" );
        // setDdlName( "VARGRAPHIC" );
        setByteFactor(3);
        setHasDefaultValue(true);
        setHasDecimals( false );
        setDefaultValuePrefix( "'" );
        setDefaultValueSuffix( "'" );
        setMaxLength( 5431 );  // max # of chars in 16K pagesize
        break;

      case java.sql.Types.BINARY:
      case java.sql.Types.LONGVARBINARY: // OPEN SQL uses LONGVARBINARY as VARBINARY
      case java.sql.Types.VARBINARY:
        setDdlName( "VARCHAR" );
        setDdlSuffix( " FOR BIT DATA" );
        setByteFactor( 1 );
        setHasDecimals( false );
        setHasDefaultValue(true);
        setDefaultValuePrefix( "x'" );
        setDefaultValueSuffix( "'" );
        setMaxLength( 16293 );  // 16K pagesize
        break;

      case java.sql.Types.BLOB:
        setDdlName( "BLOB" );
        setDdlSuffix( " LOGGED" );
        setByteFactor( 1 );
        setHasDecimals( false );
        setHasDefaultValue( true );
        setDefaultValuePrefix( "x'" );
        setDefaultValueSuffix( "'" );
        //
        // maximum size for logged BLOB's = 1024 MB
        //
        setHasLengthAttribute( true );
        setMaxLength( 1073741824 );
        setDdlDefaultLength( 1073741824 );
        break;

      case java.sql.Types.CLOB:
        //
        // we use UTF-8 data types for CHAR types
        //
        setDdlName( "CLOB" );
        setDdlSuffix( " LOGGED" );
        setByteFactor(3);
        setHasDecimals( false );
        setHasDefaultValue( true );
        setDefaultValuePrefix( "'" );
        setDefaultValueSuffix( "'" );
        //
        // maximum size for logged CLOB's = 1024 MB = 357913941 chars
        //
        setHasLengthAttribute( true );
        setMaxLength( 357913941 );   // length in CHAR's ?
        setDdlDefaultLength( 1073741824 );
        break;

      case (java.sql.Types.DATE):
        setDdlName("DATE");
        setHasDecimals( false );
        setHasDefaultValue( true );
        break;

      case (java.sql.Types.TIME):
        setDdlName("TIME");
        setHasDecimals( false );
        setHasDefaultValue( true );
        break;

      case (java.sql.Types.TIMESTAMP):
        setDdlName("TIMESTAMP");
        setHasDecimals( false );
        setHasDefaultValue( true );
        break;

      case (java.sql.Types.SMALLINT):
        //-32767 <= x <= 32767
        setDdlName("SMALLINT");
        setHasDecimals( false );
        setHasDefaultValue( true );
        break;

      case (java.sql.Types.INTEGER):
         //-2147483647 <= x <= 2147483647
         setDdlName("INTEGER");
         setHasDecimals( false );
         setHasDefaultValue( true );
        break;

      case (java.sql.Types.BIGINT):
         //-9223372036854775808 <= x <= 9223372036854775807
         setDdlName("BIGINT");
         setHasDecimals( false );
         setHasDefaultValue( true );
        break;

      case (java.sql.Types.DOUBLE):
        setDdlName("DOUBLE");
        setHasDecimals( false );
        setHasDefaultValue( true );
        break;

      //
      // for JVER tests only
      // setBoolean/getBoolean methods work only on CHAR data type and SMALLINT data type
      //
      case (java.sql.Types.BIT) :
        setDdlName("SMALLINT");
        setHasDecimals( false );
        setHasDefaultValue( true );
        setHasLengthAttribute( false );
        break;

    }
  }
}