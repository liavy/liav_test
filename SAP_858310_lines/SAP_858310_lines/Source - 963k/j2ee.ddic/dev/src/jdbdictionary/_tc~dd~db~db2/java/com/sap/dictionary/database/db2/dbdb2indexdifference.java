package com.sap.dictionary.database.db2;

import com.sap.dictionary.database.dbs.*;

public class DbDb2IndexDifference extends DbIndexDifference {

	public DbDb2IndexDifference(
		DbIndex origin,
		DbIndex target,
		Action action) {
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
			throw new JddException(
				ExType.OTHER,
				"getDdlStatements: action "
					+ action.getName()
					+ " not supported");
		}

		return statements;
	}

	public DbObjectSqlStatements getDdlStatements(
		String tableName,
		DbTable tableForStorageInfo)
		throws JddException {
		// to do 
		return getDdlStatements(tableName);
	}

}