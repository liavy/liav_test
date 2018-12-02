package com.sap.dictionary.database.mss;

import com.sap.dictionary.database.dbs.Action;
import com.sap.dictionary.database.dbs.DbObjectSqlStatements;
import com.sap.dictionary.database.dbs.DbPrimaryKey;
import com.sap.dictionary.database.dbs.DbPrimaryKeyDifference;
import com.sap.dictionary.database.dbs.DbTable;
import com.sap.dictionary.database.dbs.ExType;
import com.sap.dictionary.database.dbs.JddException;

/**
 * @author d000312
 *
 * Primary Key Difference
*/
public class DbMssPrimaryKeyDifference extends DbPrimaryKeyDifference {

	public DbMssPrimaryKeyDifference(
		DbPrimaryKey origin,
		DbPrimaryKey target,
		Action action) {
		super(origin, target, action);
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbPrimaryKeyDifference#getDdlStatements(java.lang.String)
	 * Build statements needed to bring primary key in 'new form'
	 */
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

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbPrimaryKeyDifference#getDdlStatements(java.lang.String, com.sap.dictionary.database.dbs.DbTable)
	 * Build statements needed to bring primary key in 'new form'
	 */
	public DbObjectSqlStatements getDdlStatements(
		String tableName,
		DbTable tableForStorageInfo)
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

}
