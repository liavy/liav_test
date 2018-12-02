package com.sap.dictionary.database.mss;

import com.sap.dictionary.database.dbs.DbColumn;
import com.sap.dictionary.database.dbs.DbColumnDifference;
import com.sap.dictionary.database.dbs.DbColumnDifferencePlan;
import com.sap.dictionary.database.dbs.DbColumnsDifference;
import com.sap.dictionary.database.dbs.DbObjectSqlStatements;
import com.sap.dictionary.database.dbs.DbSchema;
import com.sap.dictionary.database.dbs.DbSqlStatement;

/**
 * Title:        Analysis of table and view changes: MSSQL specific classes
 * Description:  MSSQL specific analysis of table and view changes. Tool to deliver MSSQL specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author      Guenther Drach
 * @version     1.0
 */

public class DbMssColumnsDifference extends DbColumnsDifference {

	public DbMssColumnsDifference() {
	}

	public DbObjectSqlStatements getDdlStatementsForAlter(String tableName)
		throws Exception {
		DbObjectSqlStatements statements = new DbObjectSqlStatements(tableName);
		DbSqlStatement statement;
		DbSqlStatement statement1;
		DbColumnsDifference.MultiIterator iterator = this.iterator();

		DbColumnDifference difference;
		DbColumn column;

		String userdot = null;
		String prefix = null;
		
		if (iterator.hasNext()) {
			difference = iterator.next();
			column = difference.getOrigin();
			if (column == null)
				column = difference.getTarget();

			{
				// retrieve schema information, compute usable strings
				DbSchema schema = column.getColumns().getTable().getSchema();
				String schemaName = null;
				if (schema != null)
					schemaName = schema.getSchemaName();
				if (schemaName == null) {
					prefix = "user";
					userdot = "";
				}
                        	else {
                                	prefix = "'" + schemaName + "'";
					userdot = schemaName + ".";
				}
			}

			// create help stored procedure (default dropping)
			statement = new DbSqlStatement();
			statement.addLine(
				"if exists(select 1 from tempdb..sysobjects where name = '##dropdef"
					+ tableName
					+ "') ");
			statement.addLine(
				"exec ('drop procedure [##dropdef" + tableName + "]') ");
			statements.add(statement);
			statement = new DbSqlStatement();
			statement.addLine("create procedure [##dropdef" + tableName + "] ");
			statement.addLine("@field varchar(256) as ");
			statement.addLine("begin ");
			statement.addLine("declare @stmt varchar(256) ");
			statement.addLine("declare @dname varchar(128) ");
			statement.addLine("declare @constr int ");
			statement.addLine("select @dname = '', @constr = 0 ");
			statement.addLine(
				"select @dname = so2.name, @constr = ObjectProperty(sc.cdefault, 'IsConstraint') "
					+ "from syscolumns sc, sysobjects so2 "
					+ "where "
                                        + "sc.id = object_id(" + prefix + " + '.' + '" + tableName + "') and "
					+ "sc.name = @field and "
					+ "so2.id = sc.cdefault ");
			statement.addLine("if @dname != '' ");
			statement.addLine("  begin ");
			statement.addLine("  if @constr = 0 ");
			statement.addLine(
				"    select @stmt = 'sp_unbindefault ''"
					+ tableName
					+ ".' + @field + '''' ");
			statement.addLine("  else ");
			statement.addLine(
				"    select @stmt = 'ALTER TABLE ["
					+ tableName
					+ "] DROP CONSTRAINT [' + @dname + ']' ");
			statement.addLine("  exec (@stmt) ");
			statement.addLine("  end ");
			statement.addLine("end");
			statements.add(statement);
		}

		iterator = this.iterator();

		//Statements for fields to be dropped
		statement = new DbSqlStatement();
		while (iterator.hasNextWithDrop()) {
			// produce the comma-separated list of the columns to be dropped
			column = iterator.nextWithDrop().getOrigin();
			statement.addLine(
				"["
					+ column.getName()
					+ "]"
					+ (iterator.hasNextWithDrop() ? " , " : ""));

			// before the dropping the defaults on the columns must be removed
			statement1 = new DbSqlStatement();
			statement1.addLine(
				"exec [##dropdef"
					+ tableName
					+ "] '"
					+ column.getName()
					+ "' ");
			statements.add(statement1);
		}
		if (!statement.isEmpty()) {
			statement1 = new DbSqlStatement();
			statement1.addLine(
				"ALTER TABLE " + userdot + "[" + tableName + "] DROP COLUMN ");
			statement1.merge(statement);
			statements.add(statement1);
		}

		//Statements for fields to be added
		statement = new DbSqlStatement();
		while (iterator.hasNextWithAdd()) {
			column = iterator.nextWithAdd().getTarget();
			statement.addLine(
				column.getDdlClause()
					+ (iterator.hasNextWithAdd() ? " , " : ""));
		}
		if (!statement.isEmpty()) {
			statement1 = new DbSqlStatement();
			statement1.addLine(
				"ALTER TABLE " + userdot + "[" + tableName + "] ADD ");
			statement1.merge(statement);
			statements.add(statement1);
		}

		//Preparation of nullability-change-statements
		while (iterator.hasNextWithNullabilityChange()) {
			column = iterator.nextWithNullabilityChange().getTarget();
			if (column.isNotNull()
				&& column.getDefaultValue() != null) { //NULL -> NOT NULL
				statement = new DbSqlStatement();
				statement.addLine("UPDATE " + userdot + "[" + tableName + "]");
				statement.addLine(
					" SET "
						+ column.getName()
						+ " = "
						+ ((DbMssColumn) column).getDdlDefaultValueString());
				statement.addLine(" WHERE " + column.getName() + " IS NULL ");
				statements.add(statement);
			}
		}
		// if (!statement.isEmpty()) statements.add(statement); // ????

		// why not this way? let's try it: alter the column according the new description.
		while (iterator.hasNext()) {
			difference = iterator.next();

			DbColumn orgColumn = difference.getOrigin();
			DbColumn targetColumn = difference.getTarget();

			if (orgColumn != null && targetColumn != null) {
				String colName = targetColumn.getName();

				statement = new DbSqlStatement();
				statement.addLine(
					"exec [##dropdef" + tableName + "] '" + colName + "' ");
				statements.add(statement);

				DbColumnDifferencePlan plan = difference.getDifferencePlan();
				if (plan.typeLenDecIsChanged()
					|| plan.nullabilityIsChanged()) {
					statement = new DbSqlStatement();
					statement.addLine(
						"ALTER TABLE " + tableName + " ALTER COLUMN ");
					// can't do
					// statement.addLine(targetColumn.getDdlClause());
					// because ALTER
					statement.addLine(
						colName + " " + targetColumn.getDdlTypeClause());
					if (targetColumn.isNotNull() == true)
						statement.addLine(" " + " NOT NULL");
					else
						statement.addLine(" " + " NULL");
					statements.add(statement);
				}

				if (targetColumn.getDefaultValue() != null) {
					statement = new DbSqlStatement();

					statement.addLine(
						"ALTER TABLE "
							+ tableName
							+ " ADD "
							+ targetColumn.getDdlDefaultValueClause()
							+ " FOR "
							+ colName);
					statements.add(statement);
				}
			}
		}

		// remove the help stored procedure (default dropping)
		statement = new DbSqlStatement();
		statement.addLine(
			"if exists(select 1 from tempdb..sysobjects where name = '##dropdef"
				+ tableName
				+ "') ");
		statement.addLine(
			"exec ('drop procedure [##dropdef" + tableName + "]') ");
		statements.add(statement);

		return statements;
	}

}
