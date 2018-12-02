package com.sap.dictionary.database.db6;

import com.sap.dictionary.database.dbs.*;
import java.sql.*;
import java.util.*;
import com.sap.tc.logging.*;

/**
 * Title:        Analysis of table and view changes: DB6 specific classes
 * Description:  DB6 specific analysis of table and view changes. Tool to deliver DB6 specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author       Frank-Martin Haas , Frank-Herwig Walter
 * @version 1.0
 */

public class DbDb6Column extends DbColumn
{

  private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);    
  private static Location loc = Logger.getLocation("db6.DbDb6Column");

  public DbDb6Column( DbFactory factory )
  {
    super( factory );
  }

  public DbDb6Column( DbFactory factory,DbColumn other)
  {
    super( factory, other );
  }

  public DbDb6Column( DbFactory factory, XmlMap xmlMap)
  {
    super( factory, xmlMap );
  }

  public DbDb6Column( DbFactory   factory,
                      String      name,
                      int         position,
                      int         javaSqlType,
                      String      dbType,
                      long        length,
                      int         decimals ,
                      boolean     isNotNull,
                      String      defaultValue  )
  {

    //
    // DB6 does not distinguish between types DOUBLE and FLOAT
    // We have to map dbType=DOUBLE to javaSqlType=FLOAT
    //
    if ( dbType.equals ("FLOAT") )
    {
      javaSqlType = Types.FLOAT;
    }

    constructorPart( factory , name, position, javaSqlType, dbType, length,
                     decimals, isNotNull, defaultValue);
  }

  public String getTypeClauseForDdl() throws Exception
  {
    String clause = "";

    //
    // currently no DB6 specific implememtation needed
    //
    clause = super.getDdlTypeClause();

    return clause;
  }

  public String getDdlDefaultValueClause() throws Exception
  {
    String clause     = "";
    String defaultVal = null;

    //
    // append COMPRESS SYSTEM DEFAULT clause to save space
    //
    defaultVal = this.getDefaultValue();
    if ( defaultVal != null )
    {
      if (    this.getJavaSqlType() == java.sql.Types.DATE
           || this.getJavaSqlType() == java.sql.Types.TIME
           || this.getJavaSqlType() == java.sql.Types.TIMESTAMP )
      {
        if (     this.getJavaSqlType() == java.sql.Types.DATE
              && defaultVal.equals("CURRENT DATE") )
        {
          clause = "WITH DEFAULT CURRENT DATE";
        }
        else if ( this.getJavaSqlType() == java.sql.Types.TIME
               && defaultVal.equals("CURRENT TIME") )
        {
          clause = "WITH DEFAULT CURRENT TIME";
        }
        else if ( this.getJavaSqlType() == java.sql.Types.TIMESTAMP
               && defaultVal.equals("CURRENT TIMESTAMP") )
        {
          clause = "WITH DEFAULT CURRENT TIMESTAMP";
        }
        else clause = super.getDdlDefaultValueClause();
      }
      else
      {
        clause = super.getDdlDefaultValueClause() + " COMPRESS SYSTEM DEFAULT";
      }
    }

    return clause;
  }

  public boolean checkNameLength()
  {
    //
    // Check the column's name according to its length
    // return true - if name-length is o.k
    //
    int     nameLen = this.getName().length();
    boolean check   = ( nameLen > 0 && nameLen <= 128 );

    loc.entering( "checkNameLength" );
    if ( check == false )
    {
      cat.errorT(loc, "checkNameLength: column name length must range from 0 to 128 ." );
    }
    loc.exiting();

    return ( nameLen > 0 && nameLen <= 128 );
  }


  public boolean checkNameForReservedWord()
  {
    //
    // Checks if column-name is a reserved word
    // return true - if column-name has no conflict with reserved words,
    //                   false otherwise
    //
    // no DB specific checking as decided in JDBC meeting
    return ( super.checkNameForReservedWord() );
  }


  public boolean checkTypeAttributes()
  {
    //
    //  Check the columns's attributes: type, length and decimals, e.g
    //  if length is to big for current type
    //  return true - if name-length is o.k
    //
    long len;
    boolean check = super.checkTypeAttributes();

    loc.entering("checkTypeAttributes");
    Object[] arguments = { this.getName() };

    switch ( this.getJavaSqlType() )
    {
      case java.sql.Types.DECIMAL:
      case java.sql.Types.NUMERIC:
        len = this.getLength();
        if ( len < 1 || len > 31 )
        {
          cat.errorT(loc, "checkTypeAttributes: length of decimal column {0} must range from 1 to 31 .", arguments );
          check = false;
        }
        if ( len < this.getDecimals() )
        {
          cat.errorT(loc, "checkTypeAttributes: number of decimals for column {0} is higher than length of decimal column.", arguments  );
          check = false;
        }
        break;

      case java.sql.Types.LONGVARBINARY:
      case java.sql.Types.VARBINARY:
      case java.sql.Types.BINARY:
        len = this.getLength();
        //
        // size limit for 16K Tablespaces
        //
        if ( len < 0 || len > 16293 )
        {
          cat.errorT(loc, "checkTypeAttributes: binary column {0} does not fit into 16K tablespace.", arguments );
          check = false;
        }
        break;

      case java.sql.Types.LONGVARCHAR:
      case java.sql.Types.VARCHAR:
      case java.sql.Types.CHAR:
        len = this.getLength();
        //
        // size limit for 16K Tablespaces
        //
        if (len < 0 || len > 5431 )
        {
	  cat.errorT(loc, "checkTypeAttributes: char column {0} does not fit into 16K tablespace.", arguments );
          check = false;
        }
        break;

      case java.sql.Types.BLOB:
      case java.sql.Types.CLOB:
        len = this.getLength();
        if ( this.getJavaSqlType() == java.sql.Types.CLOB ) len *= 3;  // byte length
        if ( len > 1073741824 )
        {
	  cat.errorT(loc, "checkTypeAttributes: LOB column {0} exceeds maximum byte length of 1 GB.", arguments );
          check = false;
        }
        break;

    }

    loc.exiting();
    return check;
  }


    protected DbColumnDifference comparePartTo( DbColumn target )
    {
      DbColumnDifference     colDiff       = null;
      DbDb6Column            targetCol     = (DbDb6Column) target;
      DbColumnDifferencePlan plan          = new DbColumnDifferencePlan();
      JavaSqlTypeInfo        tInfo         = this.getJavaSqlTypeInfo();
      int                    originalType  = this.getJavaSqlType();
      int                    targettype    = targetCol.getJavaSqlType();
      boolean                convertTable  = false;
	  DbDb6Environment       environment   = (DbDb6Environment) this.getColumns().getTable().getDbFactory().getEnvironment();
	  
	  // DB2 Version info may not be available if DB6Environment has not been initialized
	  // with connection information ( inside IDE )
	  String   dbVersion = environment.getDatabaseVersion();
      boolean  assumeV8  = ( dbVersion == null || dbVersion.compareTo( "SQL09" ) < 0 );

      loc.entering("compareTo");
      Object[] arguments = { this.getName() };

      //
      // map CHAR and BINARY types to equivalent types for easier type compare
      //
      // CHAR, VARCHAR, LONGVARCHAR and BINARY, VARBINARY, LONGVARBINARY types
      // are compatible if length is unchanged
      //
      if ( originalType == java.sql.Types.BINARY || originalType == java.sql.Types.LONGVARBINARY )
      {
        originalType = java.sql.Types.VARBINARY;
      }
      if ( originalType == java.sql.Types.CHAR || originalType == java.sql.Types.LONGVARCHAR )
      {
        originalType = java.sql.Types.VARCHAR;
      }
      if ( targettype == java.sql.Types.BINARY || targettype == java.sql.Types.LONGVARBINARY )
      {
        targettype = java.sql.Types.VARBINARY;
      }
      if ( targettype == java.sql.Types.CHAR || targettype == java.sql.Types.LONGVARCHAR )
      {
        targettype = java.sql.Types.VARCHAR;
      }

      //
      // COLUMN TYPE CHANGES
      //
	  // Some type changes are supported in DB2 V9
	  //
	  // SMALLINT     -> INTEGER, BIGINT, DECIMAL (p,m) p - m > 4, DOUBLE
	  // INTEGER      -> BIGINT, DECIMAL(p, m) p - m > 9, DOUBLE
	  // BIGINT       -> DECIMAL(p,m) p - m > 19
	  // DECIMAL(n,m) -> DECIMAL(p,q) p >= n q>=m (p-q) >= (n-m); no reorg if n even and p = n+1
	  //
	  // in general a REORG is required after changing the column type
	  //
      if ( originalType != targettype )
      {
		Object[] typeargs = { this.getName(), new Integer(originalType), new Integer(targettype) };
      	
        plan.setTypeIsChanged( true );
        cat.infoT( loc,
                   "compareTo: column {0}: target column of type {1} differs from target type {2} .", 
                   typeargs );
          
		if ( assumeV8 )
		{
          convertTable = true;
        }
        else
        {
          switch( originalType )
          {
          	//
            // SMALLINT -> INTEGER, BIGINT, DOUBLE
            //             DECIMAL (p,m) p - m > 4  
            //
			case java.sql.Types.SMALLINT :
			  if (    targettype != java.sql.Types.INTEGER 
			       && targettype != java.sql.Types.BIGINT
			       && targettype != java.sql.Types.DOUBLE
			       && (    targettype != java.sql.Types.DECIMAL
			            || ( targetCol.getLength() - targetCol.getDecimals() <= 4 ) ) )
			  {
				convertTable = true;
			  }
		      break;
		      
			//
			// INTEGER  -> BIGINT, DOUBLE
			//             DECIMAL(p, m) p - m > 9  
			//
			case java.sql.Types.INTEGER :
			  if (    targettype != java.sql.Types.BIGINT
				   && targettype != java.sql.Types.DOUBLE
				   && (    targettype != java.sql.Types.DECIMAL
						|| ( targetCol.getLength() - targetCol.getDecimals() <= 9 ) ) )
			  {
				convertTable = true;
			  }
			  break;
			  
			//
			// BIGINT   -> DOUBLE
			//             DECIMAL(p, m) p - m > 19  
			//
			case java.sql.Types.BIGINT :
			  if (    targettype != java.sql.Types.DOUBLE
				   && (    targettype != java.sql.Types.DECIMAL
						|| ( targetCol.getLength() - targetCol.getDecimals() <= 19 ) ) )
			  {
				convertTable = true;
			  }
			  break;
			  
			default:
			  convertTable = true;
			  break;	  
          }
        }
        
        if ( convertTable )
        {
		  cat.infoT(loc,
            "compareTo: column {0}: A table conversion is required to change column type {1} to type {2} .", 
            typeargs );
        } 
	  }  // end type changes
	  else
	  {
        //
        // COLUMN LENGTH CHANGES
        //
        // Note: type changes are already excluded 
        //
        if ( tInfo.hasLengthAttribute() )
        {
      	  long colLength     = this.getLength();
      	  long targetLength  = targetCol.getLength();

          //
          // set length for LOB types
          //
      	  if ( colLength == 0 )    colLength = tInfo.getDdlDefaultLength()/tInfo.getByteFactor();
      	  if ( targetLength == 0 ) targetLength = tInfo.getDdlDefaultLength()/tInfo.getByteFactor();


          if ( colLength != targetLength )
          {
            plan.setLengthIsChanged(true);
            cat.infoT(loc,"compareTo: column {0}: target column differs in length.", arguments );
 
		    if ( colLength > targetLength ) 
		    {
			  cat.infoT(loc,"compareTo: column {0}: table conversion is required to shrink column length.", arguments );
			  convertTable = true;
		    }
          
            switch ( originalType )
            {
              //
              //        CHAR -> VARCHAR, LONGVARCHAR ; convert
              //     VARCHAR ->    CHAR, LONGVARCHAR ; alter
              // LONGVARCHAR ->    CHAR,     VARCHAR ; alter
              //
          	  case java.sql.Types.VARCHAR :
          	    if ( this.getJavaSqlType() == java.sql.Types.CHAR ) 
          	    {
          	      convertTable = true;
          	    }
          	    break;
          	
		      //
		      //    BIN              <-> VARBIN, LONGVARBIN ; convert
			  // VARBIN, LONGVARBIN  <-> VARBIN, LONGVARBIN ; alter      
			  //
           // allow BINARY to LONGVARBINARY (ALTER is the same as CONVERT)	    
			  case java.sql.Types.VARBINARY :
			    if (  //  this.getJavaSqlType()      == java.sql.Types.BINARY || 
			         targetCol.getJavaSqlType() == java.sql.Types.BINARY ) 
			    {
			  	  convertTable = true;
			    }
			    break;
			
			  //
			  // Allowed DECIMAL alterations in V9:
			  //
			  // DECIMAL(n,m) -> DECIMAL(p,q) p >= n; q >= m; (p-q) >= (n-m)
			  //                 no reorg required if n even and p = n+1
			  //
			  case java.sql.Types.DECIMAL :
			    if (    assumeV8 
			         || ( this.getDecimals() > targetCol.getDecimals() ) 
			         || ( ( this.getLength()      - this.getDecimals() ) >
			              ( targetCol.getLength() - targetCol.getDecimals() ) ) ) 
			    {
			      convertTable = true;
			    }
			    break;
			
			  default:
			    convertTable = true;
			    break;
            }
          }
        }

        //
        // CHANGE OF DECIMALS
        //
        if (    tInfo.hasDecimals() 
             && getDecimals() != targetCol.getDecimals() )
        {
          plan.setDecimalsAreChanged( true );
          cat.infoT(loc,"compareTo: column {0}: target column differs in number of decimals.", arguments );
        
		  //
		  // Allowed DECIMAL alterations in V9:
		  //
		  // DECIMAL(n,m) -> DECIMAL(p,q) p >= n; q >= m; (p-q) >= (n-m)
		  //                 no reorg required if n even and p = n+1
		  //
		  if (    assumeV8 
		       || originalType != java.sql.Types.DECIMAL
			   || ( this.getDecimals() > targetCol.getDecimals() ) 
			   || ( ( this.getLength()      - this.getDecimals() ) >
			        ( targetCol.getLength() - targetCol.getDecimals() ) ) ) 
		  {
			convertTable = true;
		  }
        } 
	  }

      //
      // NULLABILITY CHANGES
      //
      if ( isNotNull() != targetCol.isNotNull() )
      {
        plan.setNullabilityIsChanged(true);
        cat.infoT(loc,"compareTo: column {0}: target column differs in nullability.", arguments );
      
		//
		// changing nullability is supported in DB2 V9
		//
		// DROP NOT NULL is allowed if the column 
		// is not part of the primary key ( or a unique constraint )
		//
		if ( assumeV8 )
		{
	      cat.infoT(loc,"compareTo: column {0}: altering nullability is not supported in DB2 V8.", arguments );
		  convertTable = true;
		}
		else if ( ! targetCol.isNotNull() )
		{
		  DbPrimaryKey primKey = this.getColumns().getTable().getPrimaryKey();
		  
		  if ( primKey != null )
		  {
			Iterator iter         = primKey.getColumnNames().iterator();
			boolean  colInPrimKey = false;
			
			while ( ! colInPrimKey && iter.hasNext () )
			{
			  if ( this.getName().equals( ((DbIndexColumnInfo) iter.next()).getName() ) )
			  {
				cat.infoT(loc,"compareTo: column {0}: DROP NOT NULL not allowed since column occurs in primary key.", arguments );
				colInPrimKey = true;
				convertTable = true;
			  }
			}			
		  }
		}
      }

      //
      // DEFAULT VALUE CHANGES
      //
      String origDefault    = this.getDefaultValue();
      String targetDefault  = targetCol.getDefaultValue();
      
      if (    ( origDefault == null && targetDefault != null )
           || ( origDefault != null && targetDefault == null )
           || ( origDefault != null && targetDefault != null && ! origDefault.equals( targetDefault )  )  )
      {
        plan.setDefaultValueIsChanged( true );
        cat.infoT(loc,"compareTo: column {0}: target column differs in default value.", arguments );
      }

      //
      // set Action to return 
      //
      if ( plan.somethingIsChanged() )
      {
        if ( convertTable == true )
        {
          cat.infoT(loc,"compareTo: column {0}: a table conversion is required", arguments );
          colDiff = new DbColumnDifference( this, target,plan, Action.CONVERT );
        }
        else
        {
          colDiff = new DbColumnDifference( this, target,plan, Action.ALTER );
        }
      }

      loc.exiting();
      return colDiff;

    }

    public boolean acceptedAdd() {
      //
      // ALTER TABLE ADD ... is supported in general
      // but columns with NOT NULL without DEFAULT can not be added.
      //
      if (super.isNotNull() == false || super.getDefaultValue() != null)
      {
	    return true;
      } 
      else
      {
    	loc.entering("acceptedAdd");
    	
    	Object[] arguments = { this.getName() };
    	cat.infoT(loc,"acceptedAdd: column {0} can not be added without conversion because it is NOT NULL without DEFAULT", arguments );
    	
	    loc.exiting();
	
	    return false;
      }
    }

    public boolean acceptedDrop()
    {
      //
      // ALTER TABLE DROP ... is supported with DB2 V9
      //
      // a REORG is required after ALTER TABLE DROP column
      //
	  DbDb6Environment       environment   = (DbDb6Environment) this.getColumns().getTable().getDbFactory().getEnvironment();
	  String                 dbVersion     = environment.getDatabaseVersion();

	  if ( dbVersion == null || dbVersion.compareTo( "SQL09" ) < 0 ) return false;
	  else                                                           return true;
    }
}