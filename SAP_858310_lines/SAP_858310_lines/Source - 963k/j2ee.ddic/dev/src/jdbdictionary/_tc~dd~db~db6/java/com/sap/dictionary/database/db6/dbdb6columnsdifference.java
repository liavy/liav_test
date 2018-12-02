package com.sap.dictionary.database.db6;

import com.sap.dictionary.database.dbs.*;
import com.sap.tc.logging.*;

/**
 * Title:        Analysis of table and view changes: DB6 specific classes
 * Description:  DB6 specific analysis of table and view changes. Tool to deliver DB6 specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author       Frank-Martin Haas , Frank-Herwig Walter
 * @version 1.0
 */

public class DbDb6ColumnsDifference extends DbColumnsDifference
{
  private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);  
  private static Location loc = Logger.getLocation("db6.DbDb6ColumnsDifference");

  public DbDb6ColumnsDifference()
  {
	super();
  }

  public DbObjectSqlStatements getDdlStatementsForAlter( String tableName ) throws Exception
  {
	DbObjectSqlStatements                  alterStatements = new DbObjectSqlStatements( tableName );
	DbColumnsDifference.MultiIterator      multiIterator   = this.iterator();
	boolean                                hasAlterStmt    = false;
	boolean                                alterFlag       = false;
	boolean                                reorgFlag       = false;
	DbColumnDifference                     diff;
	DbSqlStatement                         statement;
	DbColumn                               column;

	loc.entering("getDdlStatementsForAlter");

	if ( multiIterator.hasNext() )
	{
	  diff                           = multiIterator.next();

      DbDb6Environment   environment = null;
      String               dbVersion = null;

      if ( diff.getOrigin() != null )
      {
        environment = (DbDb6Environment) diff.getOrigin().getColumns().getTable().getDbFactory().getEnvironment();
        dbVersion   = environment.getDatabaseVersion();
      }
      else if ( diff.getTarget() != null )
      {
        environment = (DbDb6Environment) diff.getTarget().getColumns().getTable().getDbFactory().getEnvironment();
        dbVersion   = environment.getDatabaseVersion();
      }

	  //
	  // Note: DB2 Version info may not be available ( dbVersion = null ) inside IDE
	  // if DB6Environment has not been initialized with connection information
	  //
	  // DB2 V9 allows new ALTER statements:
	  // new ALTER SET DATA TYPE; ALTER nullability and ALTER DROP COLUMN
	  // Most of those statements put the table in REORG pending state. 
	  // Up to three of those new ALTER statements can be issued 
	  // before DB2 forces a REORG.
	  //
	  boolean  assumeV8  = ( dbVersion == null || dbVersion.compareTo( "SQL09" ) < 0 );

	  //
	  // 1st ALTER statement:
	  //
	  // ALTER TABLE ... SET NOT NULL and ALTER TABLE ... DROP NOT NULL statements
	  // are supported with DB2 V9; 
	  // REORG is not required after changing nullability if VALUE COMPRESSION is on.
	  //
	  // changing nullability can not be combined with other ALTER statements
	  // if more than one operation is done on one column.
	  //
	  // nullability can not be changed if the table is reorg pending. Therefore
	  // nullability has to be changed before other ALTER statements are executed.
	  //
	  if ( multiIterator.hasNextWithNullabilityChange() )
	  {
		hasAlterStmt = false;
		
		if ( assumeV8 )
		{
		  cat.errorT(loc, "getDdlStatementsForAlter: DB2 V8 does not support nullability changes" );
		}
		else
		{
		  statement    = new DbSqlStatement();		
		  alterFlag    = true;
		  hasAlterStmt = true;
                  reorgFlag    = true;
		  
		  statement.addLine( "ALTER TABLE " + '"' + tableName.toUpperCase() + '"' );
			
		  while ( multiIterator.hasNextWithNullabilityChange() )
		  {
			diff      = multiIterator.nextWithNullabilityChange();
			column    = diff.getTarget();
			
			statement.addLine( " ALTER COLUMN " + '"' + column.getName().toUpperCase() + '"' );
			
			if ( column.isNotNull() ) statement.addLine( " SET NOT NULL" );
			else                      statement.addLine( " DROP NOT NULL" );
		  }
		  
		  if ( hasAlterStmt )
		  {
			alterStatements.add( statement );
		  }		
		}
	  	
	  } // multiIterator.hasNextWithNullabilityChange()	  


	  //
	  // 2nd ALTER statement:
	  //
	  // ADD COLUMN, DROP COLUMN and ALTER COLUMN SET DATA TYPE clauses 
	  // may share the same ALTER statement
	  //
	  if (    multiIterator.hasNextWithAdd() 
		   || multiIterator.hasNextWithTypeLenDecChange()
		   || multiIterator.hasNextWithDrop()              )
	  {
		statement    = new DbSqlStatement();
		hasAlterStmt = false;
		
		statement.addLine( "ALTER TABLE " + '"' + tableName.toUpperCase() + '"' );

		//
		// ADD COLUMN clauses
		//
		if ( multiIterator.hasNextWithAdd() )
		{
		  alterFlag    = true;
		  hasAlterStmt = true;
		  
		  while ( multiIterator.hasNextWithAdd() )
		  {
			statement.addLine(   " ADD COLUMN " 
							   + multiIterator.nextWithAdd().getTarget().getDdlClause() );
		  }
		}
	    
		//
		// DROP COLUMN clauses
		//
		if ( multiIterator.hasNextWithDrop() )
		{
		  if ( assumeV8 )
		  {
			cat.errorT(loc, "getDdlStatementsForAlter: DB2 V8 does not allow DROP COLUMN operations" );
		  }
		  else
		  {
			alterFlag    = true;
			hasAlterStmt = true;
			reorgFlag    = true;
			
			while ( multiIterator.hasNextWithDrop() )
			{
			  statement.addLine(   " DROP COLUMN " + '"' 
								 + multiIterator.nextWithDrop().getOrigin().getName().toUpperCase() 
								 + '"' );
			}
		  }
		}

		//
		// ALTER COLUMN SET DATA TYPE clauses
		//
		while ( multiIterator.hasNextWithTypeLenDecChange() )
		{
		  diff = multiIterator.nextWithTypeLenDecChange();
		  if ( diff.getAction() == Action.ALTER )
		  {
			column = diff.getTarget();
	
			Object[] arguments = { column.getName() };
					
			//
			// we do not check the originaltype here and assume
			// that this has been done in DbDb6Column.compareTo()
			//
			switch( column.getJavaSqlType() )
			{
			  case java.sql.Types.CHAR :
			  case java.sql.Types.VARCHAR :
			  case java.sql.Types.LONGVARCHAR :
			    alterFlag    = true;
				hasAlterStmt = true;
				statement.addLine(   " ALTER COLUMN " + '"' + column.getName().toUpperCase() + '"'
								   + " SET DATA TYPE VARCHAR("
								   + ( column.getLength() * column.getJavaSqlTypeInfo().getByteFactor() )
								   + ") " );
				break;	
				
			  case java.sql.Types.VARBINARY :
			  case java.sql.Types.LONGVARBINARY :
			    alterFlag    = true;
			    hasAlterStmt = true;
			    statement.addLine(   " ALTER COLUMN " + '"' + column.getName().toUpperCase() + '"'
							  	   + " SET DATA TYPE VARCHAR("
								   + ( column.getLength() * column.getJavaSqlTypeInfo().getByteFactor() )
								   + ") FOR BIT DATA " );
			    break;	
				
			  case java.sql.Types.INTEGER :
			    if ( assumeV8 )
			    {
				  cat.errorT( loc, 
                    "getDdlStatementsForAlter: DB2 V8 does not allow ALTER COLUMN SET DATA TYPE INTEGER for column {0}" );				  	
			    }
			    else
			    {
			      alterFlag    = true;
				  hasAlterStmt = true;
				  reorgFlag    = true;
				  statement.addLine(   " ALTER COLUMN " + '"' + column.getName().toUpperCase() + '"'
				                     + " SET DATA TYPE INTEGER" );
				}
				break;	
				
			  case java.sql.Types.BIGINT :
			    if ( assumeV8 )
			    {
				  cat.errorT( loc, 
				    "getDdlStatementsForAlter: DB2 V8 does not allow ALTER COLUMN SET DATA TYPE BIGINT for column {0}" );				  	
			    }
			    else
			    {
			      alterFlag    = true;
			      hasAlterStmt = true;
				  reorgFlag    = true;
			      statement.addLine(   " ALTER COLUMN " + '"' + column.getName().toUpperCase() + '"'
				    				 + " SET DATA TYPE BIGINT" );
			    }
			    break;	
				
			  case java.sql.Types.DOUBLE :
			    if ( assumeV8 )
			    {
				  cat.errorT( loc, 
				    "getDdlStatementsForAlter: DB2 V8 does not allow ALTER COLUMN SET DATA TYPE DOUBLE for column {0}" );				  	
			    }
			    else
			    {
				  alterFlag    = true;
				  hasAlterStmt = true;
				  reorgFlag    = true;
				  statement.addLine(   " ALTER COLUMN " + '"' + column.getName().toUpperCase() + '"'
				  					 + " SET DATA TYPE DOUBLE" );
			    }
			    break;	
							  
			  case java.sql.Types.DECIMAL :
			    if ( assumeV8 )
			    {
			  	  cat.errorT( loc, 
				    "getDdlStatementsForAlter: DB2 V8 does not allow ALTER COLUMN SET DATA TYPE DECIMAL for column {0}" );				  	
			    }
			    else
			    {
				  alterFlag    = true;
				  hasAlterStmt = true;
				  reorgFlag    = true;
				  statement.addLine(   " ALTER COLUMN " + '"' + column.getName().toUpperCase() + '"'
				  				     + " SET DATA TYPE DECIMAL( " 
				  				     + column.getLength() + " , " + column.getDecimals() + " ) " );
			     }
			    break;	
							  		  
			  default:
			    cat.errorT( loc, 
				  "getDdlStatementsForAlter: ALTER COLUMN SET DATA for column {0} is not allowed for given target type" );
			    break;
			}
		  }		  
		} // end while ( SET DATA TYPE )

		if ( hasAlterStmt )
		{
		  alterStatements.add( statement );
		}
	  
	  }

	  //
	  // 3rd ALTER statement:
	  //
	  // ALTER TABLE ... SET DEFAULT and ALTER TABLE ... DROP DEFAULT statements
	  // supported with DB2 V8.2 ( V8 + FP7 )
	  //
	  // changing DEFAULT values can not be combined with ADD COLUMN or ALTER COLUMN
	  // statements if more than one operation is done on one column.
	  //
	  if ( multiIterator.hasNextWithDefaultValueChange() )
	  {
        statement    = new DbSqlStatement();
		alterFlag    = true;
   
        statement.addLine( "ALTER TABLE " + '"' + tableName.toUpperCase() + '"' );
		
		while ( multiIterator.hasNextWithDefaultValueChange() )
		{
	      diff      = multiIterator.nextWithDefaultValueChange();
	      column    = diff.getTarget();

		  if( column.getDefaultValue() != null )
		  {
		    statement.addLine(   " ALTER COLUMN " + '"' + column.getName().toUpperCase() + '"'
							   + " SET DEFAULT "
							   + column.getJavaSqlTypeInfo().getDefaultValuePrefix()
							   + column.getDefaultValue()
							   + column.getJavaSqlTypeInfo().getDefaultValueSuffix() );
		  }
		  else
		  {
	        statement.addLine(   " ALTER COLUMN " + '"' + column.getName().toUpperCase() + '"'
							   + " DROP DEFAULT " );
		  }

		}
		
		alterStatements.add( statement );
		
	  } // multiIterator.hasNextWithDefaultValueChange()
	  
	}

	
	//
	// the table may be in REORG pending state after V9 ALTER statements.
	// In this case we append a CALL ADMIN_CMD( 'REORG' ) statement.
	//
	// Note: 
	// We have to include the SELECT on SYSIBM.SYSDUMMY1 in the ADMIN_CMD
	// statement to avoid SQL0873 errors on non-unicode target databases
    // with DB2_IMPLICIT_UNICODE=YES. 
	// "SQL0873 Different encoding schemes in one statement"
	//
	if ( reorgFlag )
	{
	  statement    = new DbSqlStatement();

	  cat.infoT(loc, "getDdlStatementsForAlter: REORG is required because alterations have put the table in reorg pending state." );
	  statement.addLine( "CALL ADMIN_CMD( " );
	  statement.addLine( "( SELECT \'REORG TABLE \"" + tableName.toUpperCase() + "\" USE \' || TEMP.TBSPACE " );
	  statement.addLine( "  FROM SYSIBM.SYSDUMMY1 D, SYSCAT.TABLESPACES TEMP, SYSCAT.TABLESPACES DATA, SYSCAT.TABLES TAB " );
	  statement.addLine( "  WHERE TEMP.DATATYPE = \'T\' AND TEMP.PAGESIZE = DATA.PAGESIZE " );
	  statement.addLine( "  AND DATA.TBSPACE = TAB.TBSPACE AND TAB.TABSCHEMA = CURRENT SCHEMA " );
	  statement.addLine( "  AND TAB.TABNAME = \'" + tableName.toUpperCase() + "\' FETCH FIRST 1 ROWS ONLY ) )" );
	  
	  alterStatements.add( statement );	
	}

	loc.exiting();

	//
	// return ALTER statement
	//
	if ( alterFlag )
	{
	  return alterStatements;
	}
	else
	{
	  return null;
	}

  }

}
