/* $Id: //engine/j2ee.ddic/dev/src/jdbdictionary/_tc~dd~db~jdb/java/com/sap/dictionary/database/jdb/DbJdbIndexDifference.java#4 $
 * Last changelist: $Change: 257822 $
 * Last changed at: $DateTime: 2009/04/22 13:13:41 $
 * Last changed by: $Author: d019347 $
 */
package com.sap.dictionary.database.jdb;

import com.sap.dictionary.database.dbs.Action;
import com.sap.dictionary.database.dbs.DbIndex;
import com.sap.dictionary.database.dbs.DbIndexDifference;
import com.sap.dictionary.database.dbs.DbObjectSqlStatements;
import com.sap.dictionary.database.dbs.DbTable;
import com.sap.dictionary.database.dbs.ExType;
import com.sap.dictionary.database.dbs.JddException;

public class DbJdbIndexDifference extends DbIndexDifference {
    public DbJdbIndexDifference(final DbIndex origin, final DbIndex target, final Action action) {
        super(origin, target, action);
    }

    @Override
    public DbObjectSqlStatements getDdlStatements(String tableName) throws JddException {
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
            throw new JddException(ExType.OTHER, "getDdlStatements: action " + action.getName() + " not supported");
        }
        return statements;
    }

    @Override
    public DbObjectSqlStatements getDdlStatements(String tableName, DbTable tableForStorageInfo) throws JddException {
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
            throw new JddException(ExType.OTHER, "getDdlStatements: action " + action.getName() + " not supported");
        }
        return statements;
    }
}
