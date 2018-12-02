package com.sap.dictionary.database.db2;

import com.sap.dictionary.database.dbs.*;

/**
 * @author d022204
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DbDb2PrimaryKeyDifference extends DbPrimaryKeyDifference {

	public DbDb2PrimaryKeyDifference(
		DbPrimaryKey origin,
		DbPrimaryKey target,
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
			if (!this.getTarget().getSpecificIsSet()) {
				if (!this.getOrigin().getSpecificIsSet())
					this.getOrigin().setSpecificContentViaDb();
				if (this.getTarget() instanceof DbDb2PrimaryKey)
					(
						(DbDb2PrimaryKey) this
							.getTarget())
							.setSpecificContentViaRef(
						(DbDb2PrimaryKey) this.getOrigin());
			}
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
		return getDdlStatements(tableName);
	}

}
