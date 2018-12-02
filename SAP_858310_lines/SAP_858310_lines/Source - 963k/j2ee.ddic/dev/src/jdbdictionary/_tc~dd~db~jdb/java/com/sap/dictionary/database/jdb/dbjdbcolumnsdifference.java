/* $Id: //engine/j2ee.ddic/dev/src/jdbdictionary/_tc~dd~db~jdb/java/com/sap/dictionary/database/jdb/DbJdbColumnsDifference.java#4 $
 * Last changelist: $Change: 257822 $
 * Last changed at: $DateTime: 2009/04/22 13:13:41 $
 * Last changed by: $Author: d019347 $
 */package com.sap.dictionary.database.jdb;

import com.sap.dictionary.database.dbs.Action;
import com.sap.dictionary.database.dbs.DbColumn;
import com.sap.dictionary.database.dbs.DbColumnDifference;
import com.sap.dictionary.database.dbs.DbColumnsDifference;
import com.sap.dictionary.database.dbs.DbObjectSqlStatements;
import com.sap.dictionary.database.dbs.DbSqlStatement;

public class DbJdbColumnsDifference extends DbColumnsDifference {
    public DbJdbColumnsDifference() {
    }

    @Override
    public DbObjectSqlStatements getDdlStatementsForAlter(String tableName) throws Exception {
        DbObjectSqlStatements statements = new DbObjectSqlStatements(tableName);
        DbSqlStatement statement;
        DbColumnsDifference.MultiIterator iterator = this.iterator();
        DbColumnDifference difference;
        DbColumn column;
        String addCmd = "ALTER TABLE \"" + tableName + "\" ADD ( ";
        String dropCmd = "ALTER TABLE \"" + tableName + "\" DROP ";
        String modifyCmd = "ALTER TABLE \"" + tableName + "\" MODIFY ( ";
        String columnCmd = "ALTER TABLE \"" + tableName + "\" COLUMN ";
        iterator = this.iterator();
        // Statements for fields to be dropped
        statement = new DbSqlStatement();
        while (iterator.hasNextWithDrop()) {
            // produce the comma-separated list of the columns to be dropped
            column = iterator.nextWithDrop().getOrigin();
            statement.addLine("\"" + column.getName() + (iterator.hasNextWithDrop() ? "\", " : "\""));
        }
        addStatement(statements, statement, dropCmd, false);
        // Statements for fields to be added
        while (iterator.hasNextWithAdd()) {
            column = iterator.nextWithAdd().getTarget();
            // ADD column
            statement = new DbSqlStatement();
            statement.addLine("\"" + column.getName() + "\" " + column.getDdlTypeClause());
            addStatement(statements, statement, addCmd, true);
            // DEFAULT
            if (column.getDefaultValue() != null) {
                // DEFAULT + NOT NULL -> set the default value
                if (column.isNotNull()) {
                    statement = new DbSqlStatement();
                    statement.addLine("UPDATE \"" + tableName + "\" ");
                    statement.addLine("SET \"" + column.getName() + "\" = "
                            + column.getJavaSqlTypeInfo().getDefaultValuePrefix() + column.getDefaultValue()
                            + column.getJavaSqlTypeInfo().getDefaultValueSuffix());
                    statement.addLine(" WHERE \"" + column.getName() + "\" IS NULL ");
                    statements.add(statement);
                }
                statement = new DbSqlStatement();
                statement.addLine("\"" + column.getName() + "\" ADD DEFAULT "
                        + column.getJavaSqlTypeInfo().getDefaultValuePrefix() + column.getDefaultValue()
                        + column.getJavaSqlTypeInfo().getDefaultValueSuffix());
                addStatement(statements, statement, columnCmd, false);
            }
            // NOT NULL
            statement = new DbSqlStatement();
            if (column.isNotNull() == true)
                statement.addLine("\"" + column.getName() + "\" NOT NULL ");
            addStatement(statements, statement, columnCmd, false);
        }
        // Statements of type, length or decimal changes
        while (iterator.hasNextWithTypeLenDecChange()) {
            difference = iterator.nextWithTypeLenDecChange();
            if (difference.getAction() == Action.ALTER) {
                column = difference.getTarget();
                statement = new DbSqlStatement();
                statement.addLine("\"" + column.getName() + "\" " + column.getDdlTypeClause());
                addStatement(statements, statement, modifyCmd, true);
            }
        }
        // Statements of default value changes
        statement = new DbSqlStatement();
        while (iterator.hasNextWithDefaultValueChange()) {
            column = iterator.nextWithDefaultValueChange().getTarget();
            if (column.getDefaultValue() != null) {
                statement.addLine("\"" + column.getName() + "\" " + column.getDdlDefaultValueClause()
                        + (iterator.hasNextWithDefaultValueChange() ? " , " : ""));
            } else {
                statement.addLine("\"" + column.getName() + "\" DEFAULT NULL"
                        + (iterator.hasNextWithDefaultValueChange() ? " , " : ""));
            }
        }
        addStatement(statements, statement, modifyCmd, true);
        // Preparation of nullability-change-statements
        while (iterator.hasNextWithNullabilityChange()) {
            column = iterator.nextWithNullabilityChange().getTarget();
            if (column.isNotNull()) { // NULL -> NOT NULL
                statement = new DbSqlStatement();
                statement.addLine("UPDATE \"" + tableName + "\" ");
                statement.addLine("SET \"" + column.getName() + "\" = " + column.getJavaSqlTypeInfo().getDefaultValuePrefix()
                        + column.getDefaultValue() + column.getJavaSqlTypeInfo().getDefaultValueSuffix());
                statement.addLine(" WHERE \"" + column.getName() + "\" IS NULL ");
                statements.add(statement);
            }
        }
        // Statements of nullability changes
        statement = new DbSqlStatement();
        DbColumnsDifference.MultiIterator iterator1 = this.iterator();
        while (iterator1.hasNextWithNullabilityChange()) {
            column = iterator1.nextWithNullabilityChange().getTarget();
            if (column.isNotNull()) {
                statement.addLine("\"" + column.getName() + "\" NOT NULL"
                        + (iterator1.hasNextWithNullabilityChange() ? " , " : ""));
            } else {
                statement
                        .addLine("\"" + column.getName() + "\" NULL" + (iterator1.hasNextWithNullabilityChange() ? " , " : ""));
            }
        }
        addStatement(statements, statement, modifyCmd, true);
        return statements;
    }

    private void addStatement(DbObjectSqlStatements statements, DbSqlStatement statement, String s, boolean close_bracket) {
        DbSqlStatement st;
        if (!statement.isEmpty()) {
            if (close_bracket)
                statement.addLine(" ) ");
            st = new DbSqlStatement();
            st.addLine(s);
            st.merge(statement);
            statements.add(st);
        }
    }
}
