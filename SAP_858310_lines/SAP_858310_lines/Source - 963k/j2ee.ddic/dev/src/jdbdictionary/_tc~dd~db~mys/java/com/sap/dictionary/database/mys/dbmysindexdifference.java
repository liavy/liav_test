package com.sap.dictionary.database.mys;

import com.sap.dictionary.database.dbs.*;

/**
 * Ueberschrift:   
 * Beschreibung:
 * Copyright:     Copyright (c) 2001
 * Organisation:
 * @author        Hakan Kuecuekyilmaz, <hakan at mysql dot com>, 2005-07-10
 * @version 1.0
 */

public class DbMysIndexDifference extends DbIndexDifference {

    public DbMysIndexDifference(DbIndex origin, DbIndex target, Action action) {
        super(origin, target, action);
    }

    public DbObjectSqlStatements getDdlStatements(String tableName)
            throws JddException {
        Action action = this.getAction();
        DbObjectSqlStatements statements = null;

        if (action == Action.CREATE)
            statements = this.getTarget().getDdlStatementsForCreate();
        else if (action == Action.DROP)
            statements = this.getOrigin().getDdlStatementsForDrop();
        else if (action == Action.DROP_CREATE) {
            statements = this.getOrigin().getDdlStatementsForDrop();
            statements.merge(this.getTarget().getDdlStatementsForCreate());
        } else {
            throw new JddException(ExType.OTHER, "getDdlStatements: action "
                    + action.getName() + " not supported");
        }

        return statements;
    }

    public DbObjectSqlStatements getDdlStatements(String tableName,
            DbTable tableForStorageInfo) throws JddException {
        Action action = this.getAction();
        DbObjectSqlStatements statements = null;

        if (action == Action.CREATE)
            statements = this.getTarget().getDdlStatementsForCreate();
        else if (action == Action.DROP)
            statements = this.getOrigin().getDdlStatementsForDrop();
        else if (action == Action.DROP_CREATE) {
            statements = this.getOrigin().getDdlStatementsForDrop();
            statements.merge(this.getTarget().getDdlStatementsForCreate());
        } else {
            throw new JddException(ExType.OTHER, "getDdlStatements: action "
                    + action.getName() + " not supported");
        }

        return statements;
    }
}