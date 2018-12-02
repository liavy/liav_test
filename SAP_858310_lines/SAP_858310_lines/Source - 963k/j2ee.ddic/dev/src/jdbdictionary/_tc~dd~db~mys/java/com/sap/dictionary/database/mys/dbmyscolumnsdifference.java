package com.sap.dictionary.database.mys;

import com.sap.dictionary.database.dbs.*;
import java.sql.*;


/**
 * Title:        Analysis of table and view changes: MySQL specific classes
 * Description:  MySQL specific analysis of table and view changes. 
 *               Tool to deliver MySQL specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author       Hakan Kuecuekyilmaz, <hakan at mysql dot com>, 2005-07-10
 * @version     1.0
 */

public class DbMysColumnsDifference extends DbColumnsDifference {
    private Connection con;

    // Needed for Class.forName().
    public DbMysColumnsDifference() {
        super();
    }
  
  public DbMysColumnsDifference(Connection con) {
      this.con = con;
  }


  /**
   *  Generates the ddl-statment for these columns
   *  @param tableName	            the current table 
   *  @return The statements as DbObjectSqlStatements   
   */
  public DbObjectSqlStatements getDdlStatementsForAlter(String tableName) throws Exception{
      // could this work?

        DbObjectSqlStatements statements = new DbObjectSqlStatements(tableName);
        DbSqlStatement statement;
        DbSqlStatement statement1;
        DbColumnsDifference.MultiIterator iterator = this.iterator();

        DbColumnDifference difference;
        DbColumn column;

        iterator = this.iterator();

        //Statements for fields to be dropped
        statement = new DbSqlStatement();
        while (iterator.hasNextWithDrop()) {
            // produce the comma-separated list of the columns to be dropped
            column = iterator.nextWithDrop().getOrigin();
            statement.addLine("DROP COLUMN `" + column.getName() + "`"
                    + (iterator.hasNextWithDrop() ? " , " : ""));
        }
        if (!statement.isEmpty()) {
            statement1 = new DbSqlStatement();
            statement1.addLine("ALTER TABLE `" + tableName + "` ");
            statement1.merge(statement);
            statements.add(statement1);
        }

        //Statements for fields to be added
        statement = new DbSqlStatement();
        while (iterator.hasNextWithAdd()) {
            column = iterator.nextWithAdd().getTarget();
            statement.addLine("ADD COLUMN " + column.getDdlClause()
                    + (iterator.hasNextWithAdd() ? " , " : ""));
        }
        if (!statement.isEmpty()) {
            statement1 = new DbSqlStatement();
            statement1.addLine("ALTER TABLE `" + tableName + "` ");
            statement1.merge(statement);
            statements.add(statement1);
        }

        //Preparation of nullability-change-statements
        while (iterator.hasNextWithNullabilityChange()) {
            column = iterator.nextWithNullabilityChange().getTarget();
            if (column.isNotNull() && column.getDefaultValue() != null) { //NULL
                                                                          // ->
                                                                          // NOT
                                                                          // NULL
                statement = new DbSqlStatement();
                statement.addLine("UPDATE `" + tableName + "`");
                statement.addLine(" SET `" + column.getName() + "` = "
                        + ((DbMysColumn) column).getDdlDefaultValueClause()); //getDdlDefaultValueString());
                statement.addLine(" WHERE `" + column.getName() + "` IS NULL ");
                statements.add(statement);
            }
        }

        // not sure if this would work:
        while (iterator.hasNext()) {
            difference = iterator.next();

            DbColumn orgColumn = difference.getOrigin();
            DbColumn targetColumn = difference.getTarget();

            if (orgColumn != null && targetColumn != null) {
                String colName = targetColumn.getName();

                DbColumnDifferencePlan plan = difference.getDifferencePlan();
                if (plan.typeLenDecIsChanged() || plan.nullabilityIsChanged()) {
                    statement = new DbSqlStatement();
                    statement.addLine("ALTER TABLE `" + tableName
                            + "` MODIFY COLUMN ");
                    statement.addLine("`" + colName + "` "
                            + targetColumn.getDdlTypeClause());
                    if (targetColumn.isNotNull() == true)
                        statement.addLine(" " + " NOT NULL");
                    else
                        statement.addLine(" " + " NULL");
                    if (targetColumn.getDefaultValue() != null)
                        statement.addLine(targetColumn
                                .getDdlDefaultValueClause());
                    statements.add(statement);
                }
            }
        }
        return statements;
    }
}
