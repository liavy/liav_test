package com.sap.dictionary.database.db6;

import com.sap.tc.logging.*;
import com.sap.dictionary.database.dbs.*;

/**
 * Title:        Analysis of table and view changes: DB6 specific classes
 * Description:  DB6 specific analysis of table and view changes. Tool to deliver DB6 specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author       Frank-Martin Haas , Frank-Herwig Walter
 * @version 1.0
 */

public class DbDb6TableDifference extends DbTableDifference 
{
  private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);  
  private static       Location loc = Logger.getLocation("db6.DbDb6TableDifference");

  public DbDb6TableDifference(DbTable origin, DbTable target) 
  {
    super ( origin, target );
  }
  

  public DbObjectSqlStatements 
         getDdlStatements( String tableName, DbTable tableForStorageInfo ) throws Exception 
  {
	DbObjectSqlStatements stmts = new DbObjectSqlStatements( tableName );

    if ( ( (DbDb6Table) getOrigin() ).isReorgPending() )
    {
	  loc.entering("getDdlStatements");
	
	  Object[] arguments = { tableName };
	  cat.infoT( loc, "getDdlStatements: table {0} is reorg pending and needs to be reorged.", arguments);

      stmts.add( this.getDdlStatementforReorgPending( tableName ) );
      
	  loc.exiting();
    }

	stmts.merge( super.getDdlStatements( tableName, tableForStorageInfo ) );

	return stmts;
  }
  
  
  //
  // the table may be in REORG pending state after V9 ALTER statements.
  // This method adds a CALL ADMIN_CMD( 'REORG' ) statement if neccessary.
  //
  private DbSqlStatement getDdlStatementforReorgPending( String tableName ) throws Exception
  {
	DbSqlStatement  reorg_stmt = new DbSqlStatement();
	
	// Note: 
	// We have to include the SELECT on SYSIBM.SYSDUMMY1 in the ADMIN_CMD
	// statement to avoid SQL0873 errors on non-unicode target databases
	// with DB2_IMPLICIT_UNICODE=YES. 
	// "SQL0873 Different encoding schemes in one statement"
	//
	cat.infoT(loc, "getDdlStatementforReorgPending: REORG is required because alterations have put the table in reorg pending state." );

	reorg_stmt.addLine( "CALL ADMIN_CMD( " );
	reorg_stmt.addLine( "( SELECT \'REORG TABLE \"" + tableName.toUpperCase() + "\" USE \' || TEMP.TBSPACE " );
	reorg_stmt.addLine( "  FROM SYSIBM.SYSDUMMY1 D, SYSCAT.TABLESPACES TEMP, SYSCAT.TABLESPACES DATA, SYSCAT.TABLES TAB " );
	reorg_stmt.addLine( "  WHERE TEMP.DATATYPE = \'T\' AND TEMP.PAGESIZE = DATA.PAGESIZE " );
	reorg_stmt.addLine( "  AND DATA.TBSPACE = TAB.TBSPACE AND TAB.TABSCHEMA = CURRENT SCHEMA " );
	reorg_stmt.addLine( "  AND TAB.TABNAME = \'" + tableName.toUpperCase() + "\' FETCH FIRST 1 ROWS ONLY ) )" );

	return reorg_stmt;
  }

}