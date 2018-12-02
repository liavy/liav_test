package com.sap.dictionary.database.db2;

import com.sap.dictionary.database.dbs.*;

import java.sql.Connection;
import java.util.ArrayList;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;

/**
 * Title:        Analysis of table and view changes: DB2/390 specific classes
 * Description:  DB2/390 specific analysis of table and view changes. Tool to deliver Db2/390 specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Burkhard Diekmann
 * @version 1.0
 */

public class DbDb2ColumnsDifference extends DbColumnsDifference {
	private static Location loc =
		Logger.getLocation("db2.DbDb2ColumnsDifference");
	private static Category cat =
		Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);
	
	
	public DbDb2ColumnsDifference() {
	}

	public DbObjectSqlStatements getDdlStatementsForAlter(String tableName)
		throws Exception {
		loc.entering("getDdlStatementsForAlter");
		boolean isV9 = false;

		try {
			DbObjectSqlStatements statements =
				new DbObjectSqlStatements(tableName);
			//			if (DbDb2Parameters.commit)
			//			   statements.add(DbDb2Environment.commitLine);	
			DbSqlStatement statement;
			// variables for 'db2 specials' 
			// lob db2 specials are:
			// - rowid field needed 
			// - lob table is hidden behind view
			// - lob is stored in auxiliary table		
			boolean alterFlagAdd = false; // flag  table is altered with add
			boolean alterFlagLength = false;
			// flag  table is altered with set length
			boolean hasLobs = false; // flag  original table has lob fields 
			boolean addLobs = false; // flag  lob fields are added

			// flag  table is changed to lob table
			DbTable tableTarget = null;
			//			DbTable tableOrg = null; // original table (null for add!) 
			DbColumns columnsOrg = null;
			// columns of original table (null for add!) 
			DbColumns columnsTarget = null; // columns of target table 
			DbFactory factory = null; // factory
			Connection con = null; // connection 
			DbDb2Environment db2Env = null;
			String dbName = null; // name of database table is sitting in  
			ArrayList tblsps = null;
			// list of tablespaces in database of table
			
			// start with commit 
			if (DbDb2Parameters.commit)
				statements.add(DbDb2Environment.commitLine);
			
			DbColumnsDifference.MultiIterator iterator = this.iterator();
			iterator = this.iterator();
			if (iterator.hasNextWithAdd()) {
				// checks for add lob fields		    
				if (iterator.hasNextWithAdd()) {
					DbColumnDifference diff = iterator.nextWithAdd();
					DbColumn columnTarget = diff.getTarget();
					columnsTarget = columnTarget.getColumns();
					tableTarget = columnsTarget.getTable();
					// get factory and schema 
					factory = tableTarget.getDbFactory();
					con = factory.getConnection();				
					db2Env = (DbDb2Environment) factory.getEnvironment();
					isV9 = db2Env.isV9(con);
				}

				// check if lob fields are added 
				// only V8 
				if (!isV9) {
					iterator = this.iterator();
					while (iterator.hasNextWithAdd()) {
						DbColumnDifference diff = iterator.nextWithAdd();
						DbColumn colTarget = diff.getTarget();
						if (DbDb2Environment.isLob(colTarget)) {
							addLobs = true;
							break;
						}
					}
				}

				if (addLobs) {
					statement = new DbSqlStatement();
					statement.addLine(" SET CURRENT RULES = 'DB2' ");
					statements.add(statement);
				}
				
				// for V9 we need to add lob columns with CURRENT RULES 'STD': 
				// otherwise if the table was created WITHOUT implicit object creation (e.g. on V8) 
				// auxiliary tables/tablespaces will NOT be created on V9 even with implicit object creation on.
				if (isV9) {
					statement = new DbSqlStatement();
					statement.addLine(" SET CURRENT RULES = 'STD' ");
					statements.add(statement);
				}

				if (addLobs) {
					if (con != null)
						dbName =
							db2Env.getDatabaseNameViaDb(
								con,
								tableName);
					else
						dbName = ("________");
					tblsps =
						db2Env.getLobAuxTablespaces(con, tableName);
				}
			}
			// Statements for fields to be added   
			iterator = this.iterator();
			while (iterator.hasNextWithAdd()) {
				DbColumnDifference diff = iterator.nextWithAdd();
				statement = new DbSqlStatement();
				statement.addLine(
					"ALTER TABLE " + DbDb2Environment.quote(tableName));
				statement.addLine(" ADD ");
				statement.addLine(diff.getTarget().getDdlClause());
				statements.add(statement);
				if (!isV9) {
					if (DbDb2Environment.isLob(diff.getTarget())) {
						String lTspName =
							DbDb2Environment.getLobAuxName(
								dbName,
								tableName,
								tblsps);
						statements.merge(
							getDdlStatementsForCreateLobAuxTables(
								con,
								db2Env,
								tableName,
								lTspName,
								dbName,
								diff.getTarget().getName()));
					}
				}
				alterFlagAdd = true;
			}
			
			if (isV9) {
				statement = new DbSqlStatement();
				statement.addLine(" SET CURRENT RULES = 'DB2' ");
				statements.add(statement);
			}

			//Statements of type, length or decimal changes
			//DbDb2PrimaryKey primaryKey = null;
			//ArrayList columnsInfo = null;
			DbObjectSqlStatements statementsAlterLength =
				new DbObjectSqlStatements(tableName);
			boolean dropCreatePrimaryKey = false;
			boolean first = true;
			while (iterator.hasNextWithTypeLenDecChange()) {
				DbColumnDifference diff = iterator.nextWithTypeLenDecChange();
				DbColumn column = diff.getTarget();

				if (first) {
					DbColumn columnOrg = diff.getOrigin();
					columnsOrg = columnOrg.getColumns();
					DbTable tableOrg = columnsOrg.getTable();
					//	factory = tableOrg.getDbFactory();
					//	con = factory.getConnection();
					DbColumn columnTarget = diff.getTarget();
					columnsTarget = columnTarget.getColumns();
					//				
					hasLobs = DbDb2Environment.hasLobs(tableOrg);
				}

				if (diff.getAction() == Action.ALTER) {
					statement = new DbSqlStatement();
					statement.addLine(
						" ALTER TABLE " + DbDb2Environment.quote(tableName));
					statement.addLine(
						" ALTER COLUMN "
							+ DbDb2Environment.quote(column.getName())
							+ " SET DATA TYPE "
							+ this.getAlterTypeClause(column));
					statementsAlterLength.add(statement);
					alterFlagLength = true;
				}
			}

			//			if (dropCreatePrimaryKey) {
			//				DbObjectSqlStatements dropPriKeyStmts =
			//					primaryKey.getDdlStatementsForDropKey();
			//				statements.merge(dropPriKeyStmts);
			//			}

			if (alterFlagLength)
				statements.merge(statementsAlterLength);
			
			// add commit 
			if (DbDb2Parameters.commit)
				statements.add(DbDb2Environment.commitLine);
			
			// return ALTER statement
			if (alterFlagAdd || alterFlagLength) {
				loc.exiting();
				return statements;
			} else {
				loc.exiting();
				return null;
			}
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "getDdlStatementsForAlter failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	public DbObjectSqlStatements getDdlStatementsForDropLobView(String viewName)
		throws JddException {
		loc.entering("getDdlStatementsForDropLobView");
		try {
			DbObjectSqlStatements dropViewStmts =
				new DbObjectSqlStatements(viewName);
			DbSqlStatement dropLine = new DbSqlStatement(false);
			
			// start with commit 
			if (DbDb2Parameters.commit)
				dropViewStmts.add(DbDb2Environment.commitLine);
			
			// do NOT handle as a drop statement
			dropLine.addLine("DROP  VIEW " + DbDb2Environment.quote(viewName));
			dropViewStmts.add(dropLine);
			
			// add commit 
			if (DbDb2Parameters.commit)
				dropViewStmts.add(DbDb2Environment.commitLine);
			
			loc.exiting();
			return dropViewStmts;
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(
				loc,
				"getDdlStatementsForDropLobView failed: {0}",
				arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	private DbObjectSqlStatements getDdlStatementsForCreateLobAuxTables(
		Connection con,
		DbDb2Environment db2Env,
		String tabName,
		String lTspName,
	// lob tablespace name
	String dbName, // database name 
	String colName) throws JddException {
		loc.entering("getDdlStatementsForCreateLobAuxTables");
		try {
			// select stogroup: 
			// stogroup name (by convention): <schema> or SAPJ 
			String SapjStogroup = DbDb2Stogroup.getStogroup(con);
			if (SapjStogroup == null) {
				Object[] arguments =
					{ tabName, db2Env.getSchema(con)};
				cat.errorT(
					loc,
					"getDdlStatementsForAlter {0}: No stogroup for java percistency for schema "
						+ db2Env.getSchema(con)
						+ " found in catalog",
					arguments);
				loc.exiting();
				throw new JddException(
					ExType.NOT_ON_DB,
					"No stogroup for schema "
						+ db2Env.getSchema(con));
			}
			DbObjectSqlStatements stmts = new DbObjectSqlStatements(tabName);
			// start with commit 
			if (DbDb2Parameters.commit)
				stmts.add(db2Env.commitLine);
			
			String ltb =
				db2Env.getAuxTabName(con, colName.toUpperCase());
			DbSqlStatement createLobTspLine = new DbSqlStatement();
			createLobTspLine.addLine(
				" CREATE LOB TABLESPACE " + lTspName + " IN " + dbName);
			createLobTspLine.addLine(" USING STOGROUP " + SapjStogroup);
			createLobTspLine.addLine(
				" LOG YES LOCKMAX 0 GBPCACHE SYSTEM LOCKSIZE LOB ");
			createLobTspLine.addLine(" BUFFERPOOL BP40 ");
			stmts.add(createLobTspLine);
			DbSqlStatement createAuxTbLine = new DbSqlStatement();
			createAuxTbLine.addLine(
				" CREATE AUX TABLE "
					+ DbDb2Environment.quote(ltb)
					+ " IN "
					+ dbName
					+ "."
					+ lTspName);
			createAuxTbLine.addLine(
				" STORES "
					+ DbDb2Environment.quote(tabName)
					+ " COLUMN "
					+ DbDb2Environment.quote(colName));
			stmts.add(createAuxTbLine);
			DbSqlStatement createAuxIndLine = new DbSqlStatement();
			createAuxIndLine.addLine(
				" CREATE INDEX "
					+ DbDb2Environment.quote(ltb)
					+ " ON  "
					+ DbDb2Environment.quote(ltb));
			createAuxIndLine.addLine(" USING STOGROUP " + SapjStogroup);
			createAuxIndLine.addLine(
				" FREEPAGE 10 PCTFREE 10 GBPCACHE CHANGED ");
			createAuxIndLine.addLine(" BUFFERPOOL BP40 ");
			stmts.add(createAuxIndLine);
			
			// add commit 
			if (DbDb2Parameters.commit)
				stmts.add(DbDb2Environment.commitLine);
			
			return stmts;
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(
				loc,
				"getDdlStatementsForCreateLobAuxTables failed: {0}",
				arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	private String getAlterTypeClause(DbColumn column) throws Exception {
		String s = column.getDdlTypeClause();
		switch (column.getJavaSqlType()) {
		    case java.sql.Types.LONGVARBINARY :
			case java.sql.Types.VARBINARY :
			case java.sql.Types.BINARY :
				int i = s.indexOf("FOR BIT DATA");
				if (i > 0)
					s = s.substring(0, i);
				break;
			default :
				break;
		}
		return s;
	}

}
