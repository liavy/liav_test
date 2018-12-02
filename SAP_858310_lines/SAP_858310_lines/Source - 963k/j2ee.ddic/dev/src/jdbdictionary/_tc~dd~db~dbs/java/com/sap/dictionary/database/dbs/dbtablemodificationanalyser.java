/*
 * Created on Apr 30, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.sap.dictionary.database.dbs;

/**
 * @author d003550
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
import java.io.Reader;
import java.io.StringReader;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;

import org.xml.sax.InputSource;

import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class DbTableModificationAnalyser implements DbsConstants, DbsSeverity {
	private static final String[] DTR_PSEUDO_TABLES = {"PVC_ACTVERSVIEW",
		"PVC_OBJVERSVIEW","IMS_SYNCINXVIEW"};
	private static final Location loc = Location
	    .getLocation(DbTableModificationAnalyser.class);
	private static final Category cat = Category.getCategory(
	    Category.SYS_DATABASE, Logger.CATEGORY_NAME);
	DbModificationController controller = null;
	DbFactory factory = null;
	IDbDeployObjects deployTables = null;
	IDbDeployObjects deployViews = null;
	IDbDeployStatements deployStatements = null;
	DbDeployLogs dblogs = null;
	Object[] infoRow = null;
	Action action = null;
	String hints = null;
	String name = null;
	XmlMap tableMap = null;
	DbTable tabViaXml = null;
	DbTable tabViaDb = null;
	boolean tabViaDbIsSet = false;
	DbTableDifference difference = null;
	DbObjectSqlStatements statements = null;
	DbDeployResult result = null;

	public DbTableModificationAnalyser(DbModificationController controller) {
		this.controller = controller;
		factory = controller.getFactory();
		deployTables = controller.getDeployTables();
		deployViews = controller.getDeployViews();
		deployStatements = controller.getDeployStatements();
		result = controller.getDeployResult();
		dblogs = DbDeployLogs.getInstance(controller);
	}

	public void analyse() {
		while ((infoRow = deployTables.nextToAnalyse()) != null) {
			try {
				analyseTable();
				dblogs.closeObjectLog(name, action,false);
			} catch (Exception e) {
				JddException.log(e, TABLE_ANALYSE_ERR, new Object[] { name }, cat,
				    Severity.ERROR, loc);
				deployTables.setAnalyseResult(name, null, null, IDbDeployObjects.ERROR);
				result.set(ERROR);
				dblogs.closeObjectLog(name, null,true);
			}
		}
	}

	protected void analyseTable() throws Exception {
		action = null;
		hints = null;
		tabViaDb = null;
		tabViaDbIsSet = false;
		difference = null;
		statements = null;
		// name = <tabname>.gdbtable
		name = (String) infoRow[IDbDeployObjects.NAME];
		dblogs.openObjectLog(name, "T", "A", 0);
		cat.info(loc, TABLE_ANALYSE_START, new Object[] { name });
		tabViaXml = factory.makeTable(name);
		defineAction();
		if (action == Action.CREATE) {
			tabViaXml.setSpecificContentViaXml(tableMap);
			// Check table: consistency check including Db specific parameters
			if (!tabViaXml.check()) {
				cat.warning(loc, TABLE_CHECK_NOT_SUCCESSFUL, new Object[] { name });
				result.set(ERROR);
				return;
			}
			statements = tabViaXml.getDdlStatementsForCreate();
		} else if (action == Action.DROP) {
			if (tabViaXml.existsOnDb())
				statements = tabViaXml.getDdlStatementsForDrop();
		} else if (action == Action.ALTER) {
			checkSpecificContentCompatibility();
			statements = difference.getDdlStatements(name);
		} else if (action == Action.CONVERT) {
			if (!tabViaXml.acceptConversion()) {
				throw new JddRuntimeException(MODIFICATION_NOT_POSSIBLE,
						new Object[] { name }, cat, Severity.ERROR, loc);
			}
			if (!tabViaXml.specificForce())
				checkSpecificContentCompatibility();
			checkRuntimeObjectCompatibility();
		} else if (action == Action.DROP_CREATE) {
			if (!tabViaXml.specificForce())
				checkSpecificContentCompatibility();
			setTabViaDb();
			if (tabViaDb == null)
				statements = tabViaXml.getDdlStatementsForCreate();
			else {
				statements = tabViaXml.getDdlStatementsForDrop();
				statements.merge(tabViaXml.getDdlStatementsForCreate());
				// Get dependent views and store them in deployObjects
				ArrayList dependentViews = tabViaDb.getDependentViews();
				if (dependentViews != null)
					for (int i = 0; i < dependentViews.size(); i++) {
						if (!deployViews.contains((String) dependentViews.get(i)))
							deployViews.put((String) dependentViews.get(i),
									((Long) infoRow[IDbDeployObjects.TIMESTAMP]).longValue(),
									(String) null);
					}
			}
		}
		if (statements != null)
			deployStatements.put(name, DbTools.currentTime(), statements);
		deployTables.setAnalyseResult(name, action, hints,
		    IDbDeployObjects.ANALYSED);
	}

	private void defineAction() throws Exception {
		boolean doNotCreate = false;
		boolean deleteIfExisting = false;
		// These lines are used for undeployment, to set action before
		action = (Action) infoRow[IDbDeployObjects.ACTION_];
		if (action == Action.DROP || action == Action.CONVERT) {
			cat.info(loc, ACTION, new Object[] { action.toString() });
			return;
		}
		// Get newest table-version via xml
		tableMap = extractXmlMap(infoRow[IDbDeployObjects.XMLMAP]);
		tabViaXml = factory.makeTable();
		tabViaXml.setCommonContentViaXml(tableMap);
		if (!name.equals(tabViaXml.getName()))
			throw new JddRuntimeException(WRONG_FILENAME,
			    new Object[] {name,"gdbtable",tabViaXml.getName()},
			    cat, Severity.ERROR, loc);
		if (factory.getTools().isAlias(tabViaXml.getName())) {
			cat.info(loc, ALIAS_NO_ACTION, new Object[] { tabViaXml.getName() });
			action = Action.NOTHING;
			return;
		}

		// check name to do
		hints = (String) infoRow[IDbDeployObjects.HINTS];
		if (hints == CREATE_WITHOUT_INDEXES) {
			DbTable tabViaXmlWithoutIndexes = factory.makeTable(tabViaXml.getName());
			tabViaXmlWithoutIndexes.setColumns(tabViaXml.getColumns());
			tabViaXml = tabViaXmlWithoutIndexes;
		}

		// Get deployment info
		DbDeploymentInfo info = tabViaXml.getDeploymentInfo();
		if (info != null) {
			doNotCreate = info.doNotCreate();
			deleteIfExisting = info.deleteIfExisting();
		}
		if (action == null)
			action = info.getPredefinedAction();
		if (action == Action.DROP || action == Action.CONVERT)
			return;
		if (tabViaXml.dropCreateForce()) {
			action = Action.DROP_CREATE;
		}
		if (tabViaXml.conversionForce()) {
			action = Action.CONVERT;
			return;
		}

		// Get version from database or from Environment for local check
		// without connection
		setTabViaDb();
		if (tabViaDb == null)
			return;

		// Analyse:
		// 1. predefinedAction
		// 2. Does table exist on database
		// 2.1 if not -> CREATE
		// 2.2 if yes -> Compare Xml with database version
		if (action == Action.DROP_CREATE) {
			if (doNotCreate) {
				action = Action.DROP;
				cat.info(loc, TABLE_CREATE_FORBID, new Object[] { factory
				    .getDatabaseName() });
			}
		} else if (tabViaDb.getColumns() == null) { // Table does not exist on
																								// database
			if (doNotCreate) {
				action = Action.NOTHING;
				if (!isDtrPseudoTable(name))
					hints = DO_NOT_WRITE_RT;
				cat.info(loc, TABLE_CREATE_FORBID, new Object[] { factory
				    .getDatabaseName() });
			} else {
				action = Action.CREATE;
			}
		} else { // Table exists in database and as Xml-version
			if (doNotCreate) {
				// Table should not exist but has already been created in destination-
				// database. If table should be deleted we do this, changes for table
				// are ignored
				if (deleteIfExisting) {
					cat.info(loc, TABLE_DELETE,
					    new Object[] { factory.getDatabaseName() });
					action = Action.DROP;
				}
			} else {
				// compare the two table-versions
				difference = tabViaDb.compareTo(tabViaXml);
				if (difference != null) {
					DbColumnsDifference coldiff = difference.getColumnsDifference();
					DbIndexesDifference inddiff = difference.getIndexesDifference();
					DbPrimaryKeyDifference pkeydiff = difference.getPrimaryKeyDifference();
					cat.info(loc, ANALYSIS_DETAILS, new Object[] { 
							coldiff == null ? "null" : coldiff.getAction().getName(),
									inddiff == null ? "null" : "ALTER",
											pkeydiff == null ? "null" : pkeydiff.getAction().getName()});
					if (action == Action.CREATE) {
						boolean coldiffOk = (coldiff == null ||
								coldiff.getAction() == Action.NOTHING);
						boolean pkeydiffOk = (pkeydiff == null
						    || pkeydiff.getAction() == Action.NOTHING || pkeydiff
						    .getAction() == Action.CREATE);
						boolean inddiffOk = true;
						if (coldiffOk && pkeydiffOk && inddiff != null) {
							Iterator it = inddiff.iterator();
							while (it.hasNext()) {
								DbIndexDifference diff = (DbIndexDifference) it.next();
								if (diff != null && diff.getAction() != Action.NOTHING
								    && diff.getAction() != Action.CREATE) {
									inddiffOk = false;
									break;
								}
							}
						}
						if (!(coldiffOk && pkeydiffOk && inddiffOk))
							// CREATE was given as action but forbidden differences are found
							throw new JddRuntimeException(ACTION_CREATE_NOT_POSSIBLE,
							    new Object[] { tabViaXml.getName() }, cat, Severity.ERROR,
							    loc);
					}
					action = difference.getAction();
					if (action == Action.REFUSE) {
						if (tabViaXml.acceptAbortRisk() && 
								factory.getConnection() != null && !tabViaDb.existsData()) {
							action = Action.DROP_CREATE;
							cat.info(loc,ALLOWED_DUE_TO_EMPTY_TABLE,new Object[] {
									tabViaXml.getName()});
						} else 
							throw new JddRuntimeException(MODIFICATION_NOT_POSSIBLE,
						    new Object[] { tabViaXml.getName() }, cat, Severity.ERROR, loc);
					}
					if (action == Action.ALTER || action == Action.CONVERT) {
						// Check wether data exists
						if (factory.getConnection() != null) {
							if (!tabViaDb.existsData()) {
								cat.info(loc, TABLE_NOT_ON_DB,new Object[] {tabViaXml.getName()});
								action = Action.DROP_CREATE;
							}
						} else if (action == Action.CONVERT) {
							cat.info(loc, TABLE_TOCONVERT);
							// result.set(ERROR);
						}
					}
				} else {
					action = Action.NOTHING;
				}
			}
		}
		if (action != Action.CREATE && tabViaXml.specificForce()) {
			tabViaXml.setSpecificContentViaXml(tableMap);
			setTabViaDb();
			tabViaDb.setSpecificContentViaDb();
			if (!tabViaDb.equalsSpecificContent(tabViaXml))
				action = Action.CONVERT;
		}
		if (action == null) {
			action = Action.NOTHING;
			cat.info(loc, TABLE_NOACTION);
		} else {
			cat.info(loc, ACTION, new Object[] { action });
		}
	}

	private void checkSpecificContentCompatibility() throws Exception {
		boolean result = false;
		setTabViaDb();
		if (tabViaDb == null)
			throw new JddRuntimeException(TABLE_ONDB_NOTFOUND,new Object[]{
					tabViaXml.getName()},cat,Severity.ERROR, loc);
		// Preserve Db specific parameters from Db
		tabViaDb.setSpecificContentViaDb();
		tabViaXml.replaceSpecificContent(tabViaDb);
		// Check table: consistency check including Db specific parameters
		if (!tabViaXml.check()) 
			throw new JddRuntimeException(SPECIFIC_NOT_COMPATIBLE,new Object[]{
					tabViaXml.getName()},cat,Severity.ERROR, loc);
	}
	
	private void checkRuntimeObjectCompatibility() throws Exception {
		String name = tabViaXml.getName();
		setTabViaDb();
		if (tabViaDb == null)
			throw new JddRuntimeException(TABLE_ONDB_NOTFOUND,new Object[]{
					name},cat,Severity.ERROR, loc);
		DbRuntimeObjects runtimeObjects = DbRuntimeObjects.getInstance(factory);
		DbTable tabViaRt = factory.makeTable();
		XmlMap xmlMapViaRt = runtimeObjects.get(name);
		if (xmlMapViaRt == null) {
			runtimeObjects.putTableWithoutCorrections(tabViaDb);
			xmlMapViaRt = runtimeObjects.get(name);
		}
		tabViaRt.setCommonContentViaXml(xmlMapViaRt);
		if (tabViaRt.getDeploymentInfo() == null) 
			tabViaRt.setDeploymentInfo(new DbDeploymentInfo());
		tabViaRt.getDeploymentInfo().setPositionIsRelevant(true);
		tabViaRt.getDeploymentInfo().setIgnoreConfig(true);
		tabViaRt.replaceSpecificContent(tabViaDb);
		DbTableDifference diff = tabViaDb.compareTo(tabViaRt);
		if (diff != null)
			throw new JddRuntimeException(RUNTIME_NOT_COMPATIBLE,new Object[]{
					tabViaXml.getName()},cat,Severity.ERROR, loc);			
	}

	private void setTabViaDb() throws Exception {
		// Get version from database or from Environment for local check
		// without connection
		if (tabViaDbIsSet)
			return;
		tabViaDb = factory.getEnvironment().getTable(name);
		if (tabViaDb == null) {
			if (factory.getEnvironment().checkAgainstFile() == true) {
				// Error: table not found in original but in destination
				// file
				cat.warning(loc, TABLE_FILE_NOT_FOUND, new Object[] { name });
				result.set(ERROR);
			} else {
				tabViaDb = factory.makeTable(name);
				tabViaDb.setCommonContentViaDb(factory);
			}
		}
		tabViaDbIsSet = true;
		return;
	}

	protected static XmlMap extractXmlMap(Object xmlData) throws Exception {
		if (xmlData == null)
			return null;
		else if (xmlData instanceof XmlMap)
			return (XmlMap) xmlData;
		else if (xmlData instanceof String)
			return new XmlExtractor().map(new InputSource(new StringReader(
			    (String) xmlData)));
		else
			return null;

	}

	protected String getName() {
		return name;
	}
	

	public boolean isDtrPseudoTable(String name) {
		for (int i = 0; i < DTR_PSEUDO_TABLES.length; i++) {
			if (name.equals(DTR_PSEUDO_TABLES[i]))
				return true;
		}
		return false;
	}
}
