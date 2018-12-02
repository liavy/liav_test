package com.sap.dictionary.database.sap;

import com.sap.dictionary.database.dbs.*;

/**
 * Title:        Analysis of table and view changes: SAPDB specific classes
 * Description:  SAPDB specific analysis of table and view changes. Tool to deliver SAPDB specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Thomas Elvers
 * @version 1.0
 */

public class DbSapColumnsDifference extends DbColumnsDifference {

  public DbSapColumnsDifference() {}

  public DbObjectSqlStatements getDdlStatementsForAlter(String tableName)
          throws Exception
  {
    DbObjectSqlStatements statements = new DbObjectSqlStatements(tableName);
    DbSqlStatement statement;
    DbColumnsDifference.MultiIterator iterator = this.iterator();
    DbColumnDifference difference;
    DbColumn column;

    String addCmd    = "ALTER TABLE \"" + tableName + "\" ADD ( ";
    String dropCmd   = "ALTER TABLE \"" + tableName + "\" DROP ";
    String modifyCmd = "ALTER TABLE \"" + tableName + "\" MODIFY ( ";
    String columnCmd = "ALTER TABLE \"" + tableName + "\" COLUMN ";

    iterator = this.iterator();

    // Statements for fields to be dropped

    statement = new DbSqlStatement();
    while (iterator.hasNextWithDrop()) {
      // produce the comma-separated list of the columns to be dropped
      column = iterator.nextWithDrop().getOrigin();
      statement.addLine("\"" + column.getName() + (iterator.hasNextWithDrop() ? "\", " : "\"" ));
    }
    addStatement(statements, statement, dropCmd, false);

    // Statements for fields to be added

    while (iterator.hasNextWithAdd()) {
      column = iterator.nextWithAdd().getTarget();

      // ADD column
      statement = new DbSqlStatement();
      statement.addLine("\"" + column.getName() +  "\" " + column.getDdlTypeClause());
      addStatement(statements, statement, addCmd, true);
      
      if (column.getJavaSqlType() == java.sql.Types.LONGVARBINARY) {
        statement = new DbSqlStatement();
        statement.addLine(((DbSapColumn)(column)).getDdlLongVarbinaryClause(tableName));
        statements.add(statement);
      }
        
      // DEFAULT
      if (column.getDefaultValue() != null) {
        // DEFAULT + NOT NULL -> set the default value
        if (column.isNotNull()) {
          statement = new DbSqlStatement();
          statement.addLine("UPDATE \"" + tableName + "\" ");
          statement.addLine("SET \"" + column.getName() + "\" = " +
                          column.getJavaSqlTypeInfo().getDefaultValuePrefix() +
                          column.getDefaultValue() +
                          column.getJavaSqlTypeInfo().getDefaultValueSuffix());
          statement.addLine(" WHERE \"" + column.getName() + "\" IS NULL ");
          statements.add(statement);
        }

        statement = new DbSqlStatement();
        statement.addLine("\"" + column.getName() + "\" ADD DEFAULT " +
                          column.getJavaSqlTypeInfo().getDefaultValuePrefix() +
                          column.getDefaultValue() +
                          column.getJavaSqlTypeInfo().getDefaultValueSuffix());
        addStatement(statements, statement, columnCmd, false);
      }

      // NOT NULL
      statement = new DbSqlStatement();
      if (column.isNotNull() == true )
        statement.addLine("\"" + column.getName() + "\" NOT NULL ");
      addStatement(statements, statement, columnCmd, false);
    }

    // Statements of type, length or decimal changes

    while (iterator.hasNextWithTypeLenDecChange()) {
      difference = iterator.nextWithTypeLenDecChange();
      if ( difference.getAction() == Action.ALTER ) {
        column = difference.getTarget();
        if (column.getJavaSqlType() == java.sql.Types.LONGVARBINARY &&
            ! DbSapEnvironment.isSpecJ2EEColumn(tableName, column.getName(), null)) {
          statement = new DbSqlStatement();
          statement.addLine(((DbSapColumn)(column)).getDdlLongVarbinaryClause(tableName));
          statements.add(statement);
        }
        else {
          statement = new DbSqlStatement();
          statement.addLine("\"" + column.getName() +  "\" " + column.getDdlTypeClause());
          addStatement(statements, statement, modifyCmd, true);                            
        }
      }
    }

    //Statements of default value changes

    statement = new DbSqlStatement();
    while (iterator.hasNextWithDefaultValueChange()) {
      column = iterator.nextWithDefaultValueChange().getTarget();
      if (column.getDefaultValue() != null) {
        statement = new DbSqlStatement();
        statement.addLine("\"" + column.getName() + "\" " + column.getDdlDefaultValueClause());
        addStatement(statements, statement, modifyCmd, true);
      }
      else {
        statement = new DbSqlStatement();
        statement.addLine("\"" + column.getName() + "\" DROP DEFAULT");
        addStatement(statements, statement, columnCmd, false);      }
    }

    //Preparation of nullability-change-statements

    while (iterator.hasNextWithNullabilityChange()) {
      column = iterator.nextWithNullabilityChange().getTarget();
      if (column.isNotNull()) { //NULL -> NOT NULL
        statement = new DbSqlStatement();
        statement.addLine("UPDATE \"" + tableName + "\" ");
        statement.addLine("SET \"" + column.getName() + "\" = " +
                          column.getJavaSqlTypeInfo().getDefaultValuePrefix() +
                          column.getDefaultValue() +
                          column.getJavaSqlTypeInfo().getDefaultValueSuffix());
        statement.addLine(" WHERE \"" + column.getName() + "\" IS NULL ");
        statements.add(statement);
      }
    }

    //Statements of nullability changes

    statement = new DbSqlStatement();
    DbColumnsDifference.MultiIterator iterator1 = this.iterator();
    while (iterator1.hasNextWithNullabilityChange()) {
      column = iterator1.nextWithNullabilityChange().getTarget();
      if (column.isNotNull()) {
        statement.addLine("\"" + column.getName() + "\" NOT NULL" +
                          (iterator1.hasNextWithNullabilityChange() ? " , " : "" ));
      }
      else {
        statement.addLine("\"" + column.getName() + "\" NULL" +
                          (iterator1.hasNextWithNullabilityChange() ? " , " : "" ));
      }
    }
    addStatement(statements, statement, modifyCmd, true);

    return statements;
  }

  private void addStatement (DbObjectSqlStatements statements,
                             DbSqlStatement statement,
                             String s,
                             boolean close_bracket) {
    DbSqlStatement st;

    if (!statement.isEmpty()) {
      if (close_bracket)
        statement.addLine(" ) ");
      st =  new DbSqlStatement();
      st.addLine(s);
      st.merge(statement);
      statements.add(st);
    }
  }

}
