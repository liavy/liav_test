package com.sap.dictionary.database.dbs;

import com.sap.tc.logging.*;

import java.sql.*;

import com.sap.sql.NativeSQLAccess;

/**
 * @author d003550
 * 
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DbModifier implements DbsConstants, DbsSeverity {
	String type = null;
	DbFactory factory = null;
	DbRuntimeObjects runtimeObjects = null;
	IDbDeployObjects deployObjects = null;
	IDbDeployStatements deployStatements = null;
	DbDeployLogs dblogs = null;
	DbDeployResult result = null;
	private static final Location loc = Location.getLocation(DbModifier.class);
	private static final Category cat = Category.getCategory(
	    Category.SYS_DATABASE, Logger.CATEGORY_NAME);

	public DbModifier(DbModificationController controller, String type) {
		this.type = type;
		factory = controller.getFactory();
		deployObjects = type.equals("T") ? controller.getDeployTables()
		    : controller.getDeployViews();
		deployStatements = controller.getDeployStatements();
		runtimeObjects = controller.getRuntimeObjects();
		result = controller.getDeployResult();
		dblogs = DbDeployLogs.getInstance(controller);
	}

	public void modify() {
		boolean exOk = true;
		DbObjectSqlStatements statements = null;
		String name = null;
		Object xmlData = null;
		Action action = null;
		String hints = null;
		Object[] obj = null;
		boolean objectLogIsOpen = false;
		while ((obj = deployObjects.nextToModify()) != null) {
			name = (String) obj[IDbDeployObjects.NAME];
			xmlData = obj[IDbDeployObjects.XMLMAP];
			hints = (String) obj[IDbDeployObjects.HINTS];
			try {
				statements = deployStatements.get(name);
				if (statements != null) {
					dblogs.openObjectLog(name, type, "M", 0);
					objectLogIsOpen = true;
				}
				deployObjects.setStatus(name, IDbDeployObjects.RUNNING);
				action = deployObjects.getAction(name);

				exOk = true;
				if (statements != null) {
					cat.info(loc, MODIFY_OBJECT, new Object[] { name });
					exOk = statements.execute(factory);
				}
				if (!exOk) {
					// Log error and set error-flag
					cat.error(loc, STATEMENT_EXEC_ERR);
					deployObjects.setStatus(name, IDbDeployObjects.ERROR);
					result.set(ERROR);
					dblogs.closeObjectLog(name, action, true);
				} else if (action != Action.CONVERT) {
					boolean bufferIsAlreadyReset = false;
					if (runtimeObjects != null) {
						if (action == Action.DROP) {
							if (!name.equals(factory.getEnvironment()
									.getRuntimeObjectsTableName())) {
								runtimeObjects.remove(name);
								bufferIsAlreadyReset = true;
							}
						} else if (xmlData != null
						    && !(hints != null && hints.equals(DO_NOT_WRITE_RT))) {
							runtimeObjects.put(name, type, xmlData);
							bufferIsAlreadyReset = true;
						}
					}
					// Delete object from list of deployed objects
					deployObjects.remove(name);
					DbTools dbTools = factory.getTools();
					if (dbTools.commit() >= ERROR)
						result.set(ERROR);
					if (!bufferIsAlreadyReset)
						dbTools.invalidate(name);
				}
				if (objectLogIsOpen)
					dblogs.closeObjectLog(name, action, false);
			} catch (Exception ex) {
				// Invalidate entry for name in table-buffer (Reason Dbs with dirty
				// read)
				factory.getTools().invalidate(name);
				// Log error and set error-flag
				JddException.log(ex, TABLE_MODIFY_ERR, new Object[] { name }, cat,
				    Severity.ERROR, loc);
				if (deployObjects.get(name) != null)
					deployObjects.setStatus(name, IDbDeployObjects.ERROR);
				factory.getTools().invalidate(name);
				result.set(ERROR);
				if (objectLogIsOpen)
					dblogs.closeObjectLog(name, action, true);
			}
		}
	}
}
