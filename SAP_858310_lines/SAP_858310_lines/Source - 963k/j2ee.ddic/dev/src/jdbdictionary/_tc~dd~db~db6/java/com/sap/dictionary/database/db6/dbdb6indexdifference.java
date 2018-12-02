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

public class DbDb6IndexDifference extends DbIndexDifference
{

  public DbDb6IndexDifference( DbIndex origin, DbIndex target, Action action )
  {
    super ( origin, target, action );
  }

  public DbObjectSqlStatements getDdlStatements( String tableName ) throws JddException
  {
    Action                action     = this.getAction();
    DbObjectSqlStatements statements = null;

    if      ( action == Action.CREATE      ) statements = this.getTarget().getDdlStatementsForCreate();
    else if ( action == Action.DROP        ) statements = this.getOrigin().getDdlStatementsForDrop();
    else if ( action == Action.DROP_CREATE )
    {
      statements = this.getOrigin().getDdlStatementsForDrop();
      statements.merge( this.getTarget().getDdlStatementsForCreate() );
    }
    else
    {
      throw new JddException( ExType.OTHER,
                              "getDdlStatements: action " + action.getName() + " not supported");
    }

    return statements;
  }

  public DbObjectSqlStatements getDdlStatements( String tableName,
                                                 DbTable tableForStorageInfo) throws JddException
  {
    //
    // currently DB6 supports no index specific attributes
    // therefore parameter tableForStorageInfo can be ignored
    //
    return getDdlStatements( tableName );
  }
}