/*
 * Created on Apr 26, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.dictionary.database.dbs;

import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;

import org.xml.sax.InputSource;

/**
 * @author d003550
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class DbTableConverter implements DbsConstants, DbsSeverity {
	private static final Location loc = Location.getLocation(DbModifier.class);
	private static final Category cat = Category.getCategory(
	    Category.SYS_DATABASE, Logger.CATEGORY_NAME);
	DbModificationController controller = null;
	DbFactory factory = null;
	IDbDeployObjects deployTables = null;
	IDbDeployObjects deployViews = null;
	DbDeployResult result = null;
	DbRuntimeObjects runtimeObjects = null;
	DbDeployLogs dblogs = null;

	public DbTableConverter(DbModificationController controller) {
		this.controller = controller;
		factory = controller.getFactory();
		deployTables = controller.getDeployTables();
		deployViews = controller.getDeployViews();
		runtimeObjects = controller.getRuntimeObjects();
		result = controller.getDeployResult();
		dblogs = DbDeployLogs.getInstance(controller);
	}

	public void convert() {
		Object[] obj = null;
		String name = null;
		XmlMap targetMap = null;
		XmlMap tempTargetMap = null;
		DbTable origin = null; // Original table
		DbTable tempTarget = null; // Temproray target table without indexes
		long tmst = 0;

		while ((obj = deployTables.nextToConvert()) != null) {
			name = (String) obj[IDbDeployObjects.NAME];
			dblogs.openObjectLog(name, "T", "U", 0);
			tmst = ((Long) obj[IDbDeployObjects.TIMESTAMP]).longValue();
			try {
				origin = factory.makeTable(name);
				origin.setCommonContentViaDb(factory);
				String tempName = getTemporaryName(origin.getName());
				tempTarget = factory.makeTable(tempName);
				Object targetMapData = deployTables
				    .convertXmlData(obj[IDbDeployObjects.XMLMAP]);
				if (targetMapData instanceof String) {
					targetMap = XmlHelper.extractXmlMap((String) targetMapData);
				} else {
					targetMap = (XmlMap) targetMapData;
				}
				// Temporary Target is created without indexes
				tempTargetMap = getTargetXmlMap(targetMap, tempName);
				tempTarget.setCommonContentViaXml(tempTargetMap);
				if (tempTarget.specificForce()) {
					tempTarget.setSpecificContentViaXml(tempTargetMap);
				} else {
					// Preserve Db specific parameters from Db
					origin.setSpecificContentViaDb();
					tempTarget.replaceSpecificContent(origin);
				}
				convertTable(origin,tempTarget,targetMap,tempTargetMap,tmst);
				// Invalidate entry for name in table-buffer
				factory.getTools().invalidate(name);
				dblogs.closeObjectLog(name, Action.CONVERT,false);
			} catch (Exception e) {
				// Invalidate entry for name in table-buffer
				factory.getTools().invalidate(name);
				JddException.log(e, TABLE_CONVERSION_ERR, new Object[] { name }, cat,
				    Severity.ERROR, loc);
				deployTables.setStatus(name, IDbDeployObjects.ERROR);
				result.set(ERROR);
				dblogs.closeObjectLog(name, Action.CONVERT,true);
			}
		}
	}

	private void convertTable(DbTable origin,DbTable tempTarget,
	    XmlMap targetMap,XmlMap tempTargetMap, long tmst) throws JddException {
		int nextStep = 0;

		if (factory.getEnvironment().getConversionInterruptPoint() > 0) {
			convertTableForVeri(origin,tempTarget,targetMap,tempTargetMap,tmst);
			return;
		}

		deployTables.setStatus(origin.getName(), IDbDeployObjects.RUNNING);
		nextStep = checkTempTarget(origin,tempTarget,targetMap);
		if (nextStep == States.LOCK_ORIGIN) {
			lockOrigin(origin.getName());
			try {
				createTempTarget(tempTarget, origin.getName(),tempTargetMap);
				transferData(origin,tempTarget);
			} catch (JddException ex) {
				resetConversion(tempTarget, origin.getName());
				throw ex;
			}
			ArrayList views = saveDependentViews(origin, tmst);
			deleteDependentViews(origin, views, tmst);
			deleteOrigin(origin);
			renameTempTarget(tempTarget.getName(),origin.getName(),targetMap);
			createTargetIndexes(targetMap, origin);
			

		} else if (nextStep == States.RENAME_TEMP_TARGET) {
			cat.info(loc, RESTART_STEP_RENAME, new Object[] { origin.getName() });
			renameTempTarget(tempTarget.getName(), origin.getName(),targetMap);
			createTargetIndexes(targetMap, origin);
			
		}
	}

	private void convertTableForVeri(DbTable origin,DbTable tempTarget, XmlMap
			targetMap,XmlMap tempTargetMap,long tmst) throws JddException {
		int nextStep = 0;

		int interruptPoint = factory.getEnvironment().getConversionInterruptPoint();

		deployTables.setStatus(origin.getName(), IDbDeployObjects.RUNNING);
		if (interruptPoint == States.CHECK_TARGET)
			throw new JddException(CONVERSION_INTERRUPT, new Object[] {
			    States.toString(States.BEGIN), States.toString(interruptPoint) },
			    cat, Severity.ERROR, loc);
		nextStep = checkTempTarget(origin,tempTarget,targetMap);
		if (nextStep == States.LOCK_ORIGIN) {
			if (interruptPoint == States.LOCK_ORIGIN)
				throw new JddException(CONVERSION_INTERRUPT, new Object[] {
				    States.toString(States.CHECK_TARGET),
				    States.toString(interruptPoint) }, cat, Severity.ERROR, loc);
			lockOrigin(origin.getName());
			if (interruptPoint == States.CREATE_TARGET)
				throw new JddException(CONVERSION_INTERRUPT, new Object[] {
				    States.toString(States.LOCK_ORIGIN),
				    States.toString(interruptPoint) }, cat, Severity.ERROR, loc);
			createTempTarget(tempTarget, origin.getName(),tempTargetMap);
			if (interruptPoint == States.TRANSFER_DATA)
				throw new JddException(CONVERSION_INTERRUPT, new Object[] {
				    States.toString(States.CREATE_TARGET),
				    States.toString(interruptPoint) }, cat, Severity.ERROR, loc);
			transferData(origin, tempTarget);
			if (interruptPoint == States.SAVE_DEPENDENT_VIEWS)
				throw new JddException(CONVERSION_INTERRUPT, new Object[] {
				    States.toString(States.TRANSFER_DATA),
				    States.toString(interruptPoint) }, cat, Severity.ERROR, loc);
			ArrayList views = saveDependentViews(origin, tmst);
			if (interruptPoint == States.DELETE_DEPENDENT_VIEWS)
				throw new JddException(CONVERSION_INTERRUPT, new Object[] {
				    States.toString(States.SAVE_DEPENDENT_VIEWS),
				    States.toString(interruptPoint) }, cat, Severity.ERROR, loc);
			deleteDependentViews(origin, views, tmst);
			if (interruptPoint == States.DELETE_ORIGIN)
				throw new JddException(CONVERSION_INTERRUPT, new Object[] {
				    States.toString(States.DELETE_DEPENDENT_VIEWS),
				    States.toString(interruptPoint) }, cat, Severity.ERROR, loc);
			deleteOrigin(origin);
			if (interruptPoint == States.RENAME_TEMP_TARGET)
				throw new JddException(CONVERSION_INTERRUPT, new Object[] {
				    States.toString(States.DELETE_ORIGIN),
				    States.toString(interruptPoint) }, cat, Severity.ERROR, loc);
			renameTempTarget(tempTarget.getName(), origin.getName(),targetMap);
			if (interruptPoint == States.CREATE_TARGET_INDEXES)
				throw new JddException(CONVERSION_INTERRUPT, new Object[] {
				    States.toString(States.RENAME_TEMP_TARGET),
				    States.toString(interruptPoint) }, cat, Severity.ERROR, loc);
			createTargetIndexes(targetMap, origin);
		} else if (nextStep == States.RENAME_TEMP_TARGET) {
			if (interruptPoint == States.RENAME_TEMP_TARGET)
				throw new JddException(CONVERSION_INTERRUPT, new Object[] {
				    States.toString(States.CHECK_TARGET),
				    States.toString(interruptPoint) }, cat, Severity.ERROR, loc);
			cat.info(loc, RESTART_STEP_RENAME, new Object[] { origin.getName() });
			renameTempTarget(tempTarget.getName(), origin.getName(),targetMap);
			if (interruptPoint == States.CREATE_TARGET_INDEXES)
				throw new JddException(CONVERSION_INTERRUPT, new Object[] {
				    States.toString(States.RENAME_TEMP_TARGET),
				    States.toString(interruptPoint) }, cat, Severity.ERROR, loc);
			createTargetIndexes(targetMap, origin);
		}
	}

	private String getTemporaryName(String originName) {
		int pos = originName.indexOf('_');
		if (pos == -1)
			return originName.substring(0, originName.length() - 1) + "+";
		else 
			return new StringBuffer(originName).replace(pos, pos + 1, "+").toString();
	}

	private boolean originExists(DbTable origin) throws JddException {
		boolean exists = false;

		try {
			exists = origin.existsOnDb();
			if (exists)
				cat.info(loc, ORIGIN_EXISTS_ON_DB, new Object[] { origin.getName() });
			else
				cat.info(loc, ORIGIN_DOES_NOT_EXIST_ON_DB, new Object[] { origin
				    .getName() });
			return exists;
		} catch (Exception ex) {
			deployTables.setStatus(origin.getName(), IDbDeployObjects.ERROR);
			throw new JddException(ex, ORIGIN_EXISTS_ON_DB_ERR, new Object[] { origin
			    .getName() }, cat, Severity.ERROR, loc);
		}
	}

	private boolean tempTargetExists(DbTable tempTarget, String originName)
	    throws JddException {
		boolean exists = false;
		try {
			exists = tempTarget.existsOnDb();
			if (exists) {
				cat.info(loc,TARGET_EXISTS_ON_DB,new Object[] { tempTarget.getName() });
				return true;
			} else {
				cat.info(loc, TARGET_DOES_NOT_EXIST_ON_DB, new Object[] { tempTarget
				    .getName() });
				return false;
			}
		} catch (Exception ex) {
			deployTables.setStatus(originName, IDbDeployObjects.ERROR);
			throw new JddException(ex, TARGET_EXISTS_ON_DB_ERR,
			    new Object[] { tempTarget.getName() }, cat, Severity.ERROR, loc);
		}
	}

	private boolean deleteTempTarget(DbTable tempTarget, String originName)
	    throws JddException {
		try {
			DbObjectSqlStatements statements = tempTarget.getDdlStatementsForDrop();
			if (statements.execute(factory)) {
				runtimeObjects.remove(tempTarget.getName());
				cat.info(loc, RTXML_DBDELETE_SUCC,new Object[] { tempTarget.getName() });
				return true;
			} else {
				deployTables.setStatus(originName, IDbDeployObjects.ERROR);
				throw new JddException(CNV_STATEMENT_EXEC_ERR,
				    new Object[] { tempTarget.getName() }, cat, Severity.ERROR, loc);
			}
		} catch (Exception ex) {
			deployTables.setStatus(originName, IDbDeployObjects.ERROR);
			throw new JddException(ex, CNV_STATEMENT_EXEC_ERR,
			    new Object[] { tempTarget.getName() }, cat, Severity.ERROR, loc);
		}
	}

	public int checkTempTarget(DbTable origin,DbTable tempTarget,XmlMap targetMap)
			throws JddException {
		String originName = origin.getName();
		int step = States.LOCK_ORIGIN;

		if (originExists(origin)) {// Origin exists, no abortion after deleting origin
			if (tempTargetExists(tempTarget, originName))
				deleteTempTarget(tempTarget, originName);
		} else {// Origin does not exist and conversion is requested
			cat.info(loc, ORIGIN_DELETED_CNV_REQUESTED, new Object[] { origin
			    .getName() });
			if (tempTargetExists(tempTarget, originName)) {
				try {
					DbTable oldTempTarget = factory.makeTable();
					oldTempTarget.setCommonContentViaDb(factory);
					oldTempTarget.setSpecificContentViaDb();
					DbTableDifference difference = tempTarget.compareTo(oldTempTarget);
					if (difference != null) {// Target version in sda differs from temporary version on database
						deployTables.setStatus(originName, IDbDeployObjects.ERROR);
						throw new JddException(TARGET_DIFFERS_FROM_TEMP_TARGET,
						    new Object[] { oldTempTarget.getName() }, cat, Severity.ERROR, loc);
					} else {
						step = States.RENAME_TEMP_TARGET;
						cat.info(loc, TARGET_AND_TEMP_TARGET_ARE_EQUAL,
						    new Object[] { oldTempTarget.getName() });
					}
				} catch (Exception ex) {
					deployTables.setStatus(tempTarget.getName(), IDbDeployObjects.ERROR);
					throw new JddException(ex, TARGET_CHECK_ERR, new Object[] { tempTarget
					    .getName() }, cat, Severity.ERROR, loc);
				}
			}
		}
		return step;
	}

	private void setRuntimeObjectAccessFlag(String tableName, String accessFlag)
	    throws JddException {
		try {
			runtimeObjects.setField(tableName, "ACCESS", accessFlag);
			cat.info(loc, SET_ACCESS_FIELD_SUCC, new Object[] { tableName });
		} catch (Exception ex) {
			deployTables.setStatus(tableName, IDbDeployObjects.ERROR);
			throw new JddException(ex, SET_ACCESS_FIELD_ERR, new Object[] {
			    tableName, accessFlag }, cat, Severity.ERROR, loc);
		}
	}

	private void lockOrigin(String tableName) throws JddException {
		try {
			runtimeObjects.setField(tableName, "ACCESS", "U");
			cat.info(loc, LOCK_ORIGIN_SUCC, new Object[] { tableName });
		} catch (Exception ex) {
			deployTables.setStatus(tableName, IDbDeployObjects.ERROR);
			throw new JddException(ex, LOCK_ORIGIN_ERR, new Object[] { tableName },
			    cat, Severity.ERROR, loc);
		}
	}

	private void unlockOrigin(String tableName) throws JddException {
		try {
			runtimeObjects.setField(tableName, "ACCESS", " ");
			cat.info(loc, UNLOCK_ORIGIN_SUCC, new Object[] { tableName });
		} catch (Exception ex) {
			deployTables.setStatus(tableName, IDbDeployObjects.ERROR);
			throw new JddException(ex, UNLOCK_ORIGIN_ERR, new Object[] { tableName },
			    cat, Severity.ERROR, loc);
		}
	}

	private void lockView(String viewName) throws JddException {
		try {
			runtimeObjects.setField(viewName, "ACCESS", "V");
			cat.info(loc, LOCK_ORIGIN_SUCC, new Object[] { viewName });
		} catch (Exception ex) {
			deployTables.setStatus(viewName, IDbDeployObjects.ERROR);
			throw new JddException(ex, LOCK_ORIGIN_ERR, new Object[] { viewName },
			    cat, Severity.ERROR, loc);
		}
	}

	private void createTempTarget(DbTable tempTarget,String originName,
			XmlMap tempTargetMap) throws JddException {
		try {
			// Target is created with primary key but without secondary indexes
			DbObjectSqlStatements statements = tempTarget.getDdlStatementsForCreate();
			statements.exec(factory);
			runtimeObjects.put(tempTarget.getName(), "T", tempTargetMap);
			cat.info(loc, RTXML_DBWRITE_SUCC, new Object[] { tempTarget.getName() });
		} catch (Exception ex) {
			deployTables.setStatus(originName, IDbDeployObjects.ERROR);
			throw new JddException(ex,CNV_STATEMENT_EXEC_ERR,new Object[] { tempTarget
			    .getName() }, cat, Severity.ERROR, loc);
		}
	}

	private void transferData(DbTable origin, DbTable tempTarget)
	    throws JddException {
		DbDataTransfer dataTransfer = new DbDataTransfer(origin, tempTarget);
		try {
			dataTransfer.transfer();
		} catch (Exception ex) {
			deployTables.setStatus(origin.getName(), IDbDeployObjects.ERROR);
			throw new JddException(ex, DATA_TRANSFER_ERR, new Object[] {
			    origin.getName(), tempTarget.getName() }, cat, Severity.ERROR, loc);
		}
	}

	private ArrayList saveDependentViews(DbTable origin, long tmst)
	    throws JddException {
		ArrayList views = null;
		Iterator iter = null;
		String name = null;

		try {
			views = origin.getDependentViews();
			if (!views.isEmpty()) {
				iter = views.iterator();
				while (iter.hasNext()) {
					name = (String) iter.next();
					lockView(name); // Set access-flag in runtime object
					if (!deployViews.contains(name)) // xmlValue = null is sign for
						deployViews.put(name, tmst, (String) null); // dependent view not in sda
					// what generates a create statement for this view
					cat.info(loc, VIEW_SAVE_SUCCESS, new Object[] { name });
				}
				// Analyse views and save statements
				DbViewModificationAnalyser analyserForConvert = 
					new DbViewModificationAnalyser(controller);
				analyserForConvert.analyse();
			}
		} catch (Exception ex) {
			deployTables.setStatus(origin.getName(), IDbDeployObjects.ERROR);
			throw new JddException(ex, VIEW_SAVE_ERR, new Object[] { name }, cat,
			    Severity.ERROR, loc);
		}
		return views;
	}

	private void deleteDependentViews(DbTable origin, ArrayList views, long tmst)
	    throws JddException {
		Iterator iter = null;
		String name = null;
		DbView view = null;
		DbObjectSqlStatements statements = null;

		try {
			if (!views.isEmpty()) {
				iter = views.iterator();
				while (iter.hasNext()) {
					name = (String) iter.next();
					try {
						// BC_DDDBTABLERT entries for views are not deleted. In case of
						// aborted
						// conversion with deleted origin, the only chance to get the
						// dependent views
						// is with locked views in BC_DDDBTABLERT.
						view = factory.makeView(name);
						statements = view.getDdlStatementsForDrop();
						statements.exec(factory); // throws JddRuntimeException
						cat.info(loc, VIEW_DELETION_SUCC, new Object[] { name });
					} catch (Exception ex) {
						deployTables.setStatus(origin.getName(), IDbDeployObjects.ERROR);
						throw new JddException(ex, VIEW_DELETION_ERR,
						    new Object[] { name }, cat, Severity.ERROR, loc);
					}
				} // end while
			}
		} catch (Exception ex) {
			deployTables.setStatus(origin.getName(), IDbDeployObjects.ERROR);
			throw new JddException(ex, DEPENDENT_VIEWS_DELETION_ERR,
			    new Object[] { origin.getName() }, cat, Severity.ERROR, loc);
		}
	}

	private void deleteOrigin(DbTable origin) throws JddException {
		try {
			DbObjectSqlStatements statements = origin.getDdlStatementsForDrop();
			statements.exec(factory);
			runtimeObjects.remove(origin.getName());
			cat.info(loc, RTXML_DBDELETE_SUCC, new Object[] { origin.getName() });
		} catch (Exception ex) {
			deployTables.setStatus(origin.getName(), IDbDeployObjects.ERROR);
			throw new JddException(ex, CNV_STATEMENT_EXEC_ERR, new Object[] { origin
			    .getName() }, cat, Severity.ERROR, loc);
		}
	}

	private void renameTempTarget(String source, String destination, XmlMap targetMap)
	    throws JddException {
		DbTools tools = null;

		try {
			tools = factory.makeTools();
			tools.renameTable(source, destination);			
			cat.info(loc, RENAME_SUCC, new Object[] { source, destination });
			runtimeObjects.remove(source);
			runtimeObjects.put(destination, "T", targetMap);
			cat.info(loc, RTXML_DBWRITE_SUCC, new Object[] { destination });
		} catch (Exception ex) {
			deployTables.setStatus(destination, IDbDeployObjects.ERROR);
			throw new JddException(ex, RENAME_ERR,
			    new Object[] { source, destination }, cat, Severity.ERROR, loc);
		}
	}

	private void createTargetIndexes(XmlMap tableMap, DbTable origin)
	    throws JddException {
		DbTable target = null;
		DbIndexes indexes = null;
		DbIndexes originIndexes = null;
		DbIndexIterator iter = null;
		DbIndex index = null;
		DbIndex originIndex = null;
		DbObjectSqlStatements statements = null;

		try {
			target = factory.makeTable(origin.getName());
			target.setCommonContentViaXml(tableMap);
			if (target.specificForce())
				target.setSpecificContentViaXml(tableMap);
			indexes = target.getIndexes();
			originIndexes = origin.getIndexes();
			if (indexes != null && !indexes.isEmpty()) {
				iter = indexes.iterator();
				while (iter.hasNext()) {
					index = (DbIndex) iter.next();
					if (!target.specificForce() && originIndexes != null) {
						// Preserve Dbspecific parameters of original index
						originIndex = originIndexes.getIndex(index.getName());
						if (originIndex != null)
							index.replaceSpecificContent(originIndex);
					}
					statements = index.getDdlStatementsForCreate();
					statements.exec(factory); // throws JddRuntimeException
					cat.info(loc, INDEX_CREATE_SUCC, new Object[] { index.getName() });
				} // end while
			}
		} catch (Exception ex) {
			deployTables.setStatus(origin.getName(), IDbDeployObjects.ERROR);
			throw new JddException(ex, INDEX_CREATE_ERR, new Object[] { index
			    .getName() }, cat, Severity.ERROR, loc);
		}
	}

	private void resetConversion(DbTable tempTarget, String originName)
	    throws JddException {
		try {
			deleteTempTarget(tempTarget, originName);
			unlockOrigin(originName);
		} catch (Exception ex) {
			deployTables.setStatus(originName, IDbDeployObjects.ERROR);
			throw new JddException(ex, RESET_ERROR_AFTER_TRANSFER_ERROR,
			    new Object[] { originName }, cat, Severity.ERROR, loc);
		}
	}

	private XmlMap getTargetXmlMap(XmlMap map, String name) {
		XmlMap targetMap = new XmlMap();
		XmlMap innerMap = (XmlMap) map.getXmlMap("Dbtable").clone();
		innerMap.put("name", name);
		XmlMap prkeyMap = (XmlMap) innerMap.getXmlMap("primary-key").clone();
		if (!(prkeyMap == null || prkeyMap.isEmpty())) {
			prkeyMap.put("tabname", name);
			innerMap.put("primary-key", prkeyMap);
		}
		innerMap.remove("indexes");
		targetMap.put("Dbtable", innerMap);
		return targetMap;
	}

	public static class States {
		public static final int BEGIN = 0;
		public static final int CHECK_TARGET = 10;
		public static final int LOCK_ORIGIN = 20;
		public static final int CREATE_TARGET = 30;
		public static final int TRANSFER_DATA = 40;
		public static final int SAVE_DEPENDENT_VIEWS = 50;
		public static final int DELETE_DEPENDENT_VIEWS = 60;
		public static final int DELETE_ORIGIN = 70;
		public static final int RENAME_TEMP_TARGET = 80;
		public static final int CREATE_TARGET_INDEXES = 90;

		public static String toString(int state) {
			switch (state) {
			case BEGIN:
				return "BEGIN";
			case CHECK_TARGET:
				return "CHECK_TARGET";
			case LOCK_ORIGIN:
				return "LOCK_ORIGIN";
			case CREATE_TARGET:
				return "CREATE_TARGET";
			case TRANSFER_DATA:
				return "TRANSFER_DATA";
			case SAVE_DEPENDENT_VIEWS:
				return "SAVE_DEPENDENT_VIEWS";
			case DELETE_DEPENDENT_VIEWS:
				return "DELETE_DEPENDENT_VIEWS";
			case DELETE_ORIGIN:
				return "DELETE_ORIGIN";
			case RENAME_TEMP_TARGET:
				return "RENAME_TEMP_TARGET";
			case CREATE_TARGET_INDEXES:
				return "CREATE_TARGET_INDEXES";
			default:
				return "";
			}
		}
	}
}