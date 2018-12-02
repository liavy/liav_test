package com.sap.dictionary.database.mss;

import com.sap.dictionary.database.dbs.*;

/**
 * Ueberschrift:   Tests zu jdbpersistency
 * Beschreibung:
 * Copyright:     Copyright (c) 2001
 * Organisation:
 * @author        d000312
 * @version 1.0
 */

public class DbMssIndexDifference extends DbIndexDifference {

  public DbMssIndexDifference(DbIndex origin,DbIndex target, Action action) {
    super ( origin, target, action );
  }

  public DbObjectSqlStatements getDdlStatements(String tableName)
    throws JddException {
    Action action = this.getAction();
    DbObjectSqlStatements statements = null;

    if (action == Action.CREATE) statements = this.getTarget().getDdlStatementsForCreate();
    else
    if (action == Action.DROP) statements = this.getOrigin().getDdlStatementsForDrop();
    else
    if (action == Action.DROP_CREATE) {
      statements = this.getOrigin().getDdlStatementsForDrop();
      statements.merge(this.getTarget().getDdlStatementsForCreate());
    }
    else {
      throw new JddException(ExType.OTHER,
                          "getDdlStatements: action " + action.getName() + " not supported");
    }

    return statements;
  }

  public DbObjectSqlStatements getDdlStatements(String tableName,
                                                DbTable tableForStorageInfo)
    throws JddException {
    Action action = this.getAction();
    DbObjectSqlStatements statements = null;

    if (action == Action.CREATE) statements = this.getTarget().getDdlStatementsForCreate();
    else
    if (action == Action.DROP) statements = this.getOrigin().getDdlStatementsForDrop();
    else
    if (action == Action.DROP_CREATE) {
      statements = this.getOrigin().getDdlStatementsForDrop();
      statements.merge(this.getTarget().getDdlStatementsForCreate());
    }
    else {
      throw new JddException(ExType.OTHER,
                          "getDdlStatements: action " + action.getName() + " not supported");
    }

    return statements;
  }
}