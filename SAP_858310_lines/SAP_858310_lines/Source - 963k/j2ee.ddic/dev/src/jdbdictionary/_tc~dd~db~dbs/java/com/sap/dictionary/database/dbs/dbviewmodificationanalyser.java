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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.GregorianCalendar;

import org.xml.sax.InputSource;

import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class DbViewModificationAnalyser implements DbsConstants,DbsSeverity {
	private static final Location loc = 
		Location.getLocation(DbViewModificationAnalyser.class);
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	DbFactory factory = null;
	IDbDeployObjects deployViews = null;
	IDbDeployStatements deployStatements = null;
	Object[] infoRow = null;
	Action action = null;
	String hints = null;
	String name = null;
	DbView viewViaXml = null;
	DbView viewViaDb = null;
	DbTableDifference difference = null;
	DbObjectSqlStatements statements = null;
	DbDeployResult result = null;

	public DbViewModificationAnalyser(DbModificationController controller) {
		factory = controller.getFactory();
		deployViews = controller.getDeployViews();
		deployStatements = controller.getDeployStatements();
		result = controller.getDeployResult();
	}

	public void analyse() {
		while ((infoRow = deployViews.nextToAnalyse()) != null) {
			try {
				analyseView();
			} catch (Exception e) {
				JddException.log(e,VIEW_ANALYSE_ERR,new Object[]{name},cat,
					Severity.ERROR,loc);
				deployViews.setAnalyseResult(name, null,null, IDbDeployObjects.ERROR);
				result.set(ERROR);
			}
		}
	}

	private void analyseView() throws Exception {
		action = null;
		hints = null;
		viewViaDb = null;
		statements = null;
		name = (String) infoRow[IDbDeployObjects.NAME];
		cat.info(loc, VIEW_ANALYSE_START, new Object[] { name });
		viewViaXml = factory.makeView(name);
		defineAction();
		if (action == Action.CREATE)
			statements = viewViaXml.getDdlStatementsForCreate();
		else if (action == Action.DROP)
			statements = viewViaXml.getDdlStatementsForDrop();
		else if (action == Action.DROP_CREATE) {
			if (viewViaXml != null && viewViaXml.getColumns() != null) {
				statements = viewViaXml.getDdlStatementsForDrop();
				statements.merge(viewViaXml.getDdlStatementsForCreate());
			} else {
				statements = viewViaDb.getDdlStatementsForDrop();
				statements.merge(viewViaDb.getDdlStatementsForCreate());
			}
		}
		if (statements != null)
			deployStatements.put(name,DbTools.currentTime(),statements);
		deployViews.setAnalyseResult(name,action,hints,IDbDeployObjects.ANALYSED);
	}

	private void defineAction() throws Exception {
		boolean doNotCreate = false;
		boolean deleteIfExisting = false;
		action = (Action)infoRow[IDbDeployObjects.ACTION_];
		if (action == Action.DROP) {
		  cat.info(loc,ACTION,new Object[] {action.toString()});	
		  return;
		}

		if (infoRow[IDbDeployObjects.XMLMAP] != null) {
			//Get newest view-version via xml
			XmlMap viewMap = extractXmlMap(infoRow[IDbDeployObjects.XMLMAP]);
			viewViaXml = factory.makeView();
			viewViaXml.setCommonContentViaXml(viewMap);
			if (!name.equals(viewViaXml.getName()))
				throw new JddRuntimeException(WRONG_FILENAME,
				    new Object[] {name,"gdbview",viewViaXml.getName()},
				    cat, Severity.ERROR, loc);
			DbDeploymentInfo info = viewViaXml.getDeploymentInfo();
			doNotCreate = info.doNotCreate();
			deleteIfExisting = info.deleteIfExisting();
			if (action == null)
				action = info.getPredefinedAction();
		}
			
		//Get version from database
		viewViaDb = factory.makeView(name);
		viewViaDb.setCommonContentViaDb();
		
		//Analyse:
		//1. predefinedAction
		//2. Does table exist on database
		//2.1 if not -> CREATE

		if (action == Action.DROP_CREATE) {
			if (doNotCreate) {
				action = Action.DROP;
				cat.info(loc,VIEW_CREATE_FORBID,new Object[]{factory.getDatabaseName()});
			}
		} else if (viewViaDb.getColumns() == null) { //View does not exist on database
			if (doNotCreate) {
				action = Action.NOTHING;
				hints = DO_NOT_WRITE_RT;
				cat.info(loc,VIEW_CREATE_FORBID,new Object[]{factory.getDatabaseName()});
			} else {
				action = Action.CREATE;
			}
		} else { //View exists in database 
			if (doNotCreate) {
				//View should not exist but has already been created in destination-
				//database. If view should be deleted we do this, changes for view
				//are ignored
				if (deleteIfExisting) {
					cat.info(loc, VIEW_DROP,new Object[]{factory.getDatabaseName()});
					action = Action.DROP;
				}
			} else {
				//No compare, view is dropped and recreated
				action = Action.DROP_CREATE;
			}
		}
		if (action == null) {
			action = Action.NOTHING;
			cat.info(loc, VIEW_NOACTION);
		} else {
			cat.info(loc,ACTION,new Object[]{action});
		}
	}

	private XmlMap extractXmlMap(Object xmlData) throws Exception {
		if (xmlData == null)
			return null;
		else if (xmlData instanceof XmlMap)
			return (XmlMap) xmlData;
		else if (xmlData instanceof String)
			return new XmlExtractor().map(new InputSource(new StringReader(
				(String)xmlData)));
		else
			return null;

	}
}
