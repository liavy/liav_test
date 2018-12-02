/*
 * Created on May 11, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.sap.dictionary.database.opentools;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.GregorianCalendar;

import com.sap.dictionary.database.dbs.*;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.StreamLog;
import com.sap.sql.services.OpenSQLServices;
import org.xml.sax.InputSource;

/**
 * @author d003550
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DbTableOpenTools implements OpenTools, DbsConstants, DbsSeverity {
	private Connection con = null;
	private DbFactory factory = null;
	private static final Location loc = Location
	    .getLocation(DbTableOpenTools.class);
	private static final Category cat = Category.getCategory(
	    Category.SYS_DATABASE, Logger.CATEGORY_NAME);
	private HashMap actionResultsForAllDbs = null;
	private int severity = Severity.INFO;
	private Logger logger = new Logger();

	public DbTableOpenTools(Connection con) throws JddException {
		this.con = con;
		factory = new DbFactory(con);
//		ConsoleLog consoleLog = new ConsoleLog();
//		consoleLog.setFormatter(new DbTraceFormatter());
//		cat.addLog(consoleLog);
//		Logger.adjust();
	}

	DbFactory getDbFactory() {
		return factory;
	}

	public boolean createTable(String name, File file) {
		loc.entering("opentools.createTable()");

		FileInputStream stream = null;
		try {
			stream = new FileInputStream(file);
		} catch (IOException ex) {
			JddException.log(ex, cat, Severity.ERROR, loc);
			loc.exiting();
			return false;
		}
		return createTable(name, stream);
	}

	public boolean createTable(String name, InputStream stream) {
		return createTable(name,stream,false,false);
	}

	public boolean createTable(String name, InputStream stream,
	    boolean withoutPrimaryKey, boolean withoutIndexes) {
		loc.entering("opentools.createTable()");
		DbModificationController controller = new DbModificationController(factory,
		    false, null);
		try {
			Object xmlData = controller.getDeployTables().convertXmlData(stream);
			XmlMap tableMap = new XmlExtractor().map(new InputSource(
			    new StringReader((String) xmlData)));
			controller.getDeployTables().put(name, Action.CREATE,
			    getModifiedXmlMap(tableMap, withoutPrimaryKey, withoutIndexes));
			controller.switchOnTrace(Severity.ERROR);
			controller.distribute();
			controller.switchOffTrace();
			DbDeployResult result = controller.getDeployResult();
			loc.exiting();
			if (result.get() < ERROR)
				return true;
			throw new JddRuntimeException("Table " + name +
					" could not be created in database.\nVersion: " + getSubmitInfo() + 
					"\nProtocol: \n" + controller.getInfoText());
		} catch (Exception ex) {
			loc.exiting();
			throw new JddRuntimeException(ex, XML_ANALYSE_ERR, new Object[] { name },
			    cat, Severity.ERROR, loc);
		}
	}

	public String getTableCreateStatementFromDb(String name) {
		loc.entering("opentools.getTableCreateStatementFromDb()");
		String statement = null;
		try {
			XmlMap tableMap = DbRuntimeObjects.getInstance(factory).get(name);
			DbTable table = factory.makeTable(name);
			table.setCommonContentViaXml(tableMap);
			statement = table.getDdlStatementsForCreate().toString();
		} catch (Exception ex) {
			JddException.log(ex, cat, Severity.ERROR, loc);
			loc.exiting();
		}
		loc.exiting();
		return statement;
	}

	public boolean dropTable(String name) {
		DbModificationController controller = new DbModificationController(factory,
		    false, null);
		controller.getDeployTables().put(name, Action.DROP, null);
		controller.switchOnTrace(Severity.ERROR);
		controller.distribute();
		controller.switchOffTrace();
		DbDeployResult result = controller.getDeployResult();
		if (result.get() < ERROR)
			return true;
		throw new JddRuntimeException("Table " + name +
				" could not be droped in database.\nVersion: " + getSubmitInfo() + 
				"\nProtocol: \n" + controller.getInfoText());
	}

	public boolean createView(String name, File file) {
		loc.entering("opentools.createView()");

		FileInputStream stream = null;
		try {
			stream = new FileInputStream(file);
		} catch (IOException ex) {
			JddException.log(ex, cat, Severity.ERROR, loc);
			loc.exiting();
			return false;
		}
		return createView(name, stream);
	}

	public boolean createView(String name, InputStream stream) {
		DbModificationController controller = new DbModificationController(factory,
		    false, null);
		controller.getDeployViews().put(name, Action.CREATE, stream);
		controller.switchOnTrace(Severity.ERROR);
		controller.distribute();
		controller.switchOffTrace();
		DbDeployResult result = controller.getDeployResult();
		if (result.get() < ERROR)
			return true;
		throw new JddRuntimeException("View " + name +
				" could not be created in database.\nVersion: " + getSubmitInfo() + 
				"\nProtocol: \n" + controller.getInfoText());
	}

	public boolean dropView(String name) {
		DbModificationController controller = new DbModificationController(factory,
		    false, null);
		controller.getDeployViews().put(name, Action.DROP, null);
		controller.switchOnTrace(Severity.ERROR);
		controller.distribute();
		controller.switchOffTrace();
		DbDeployResult result = controller.getDeployResult();
		if (result.get() < ERROR)
			return true;
		throw new JddRuntimeException("View " + name +
				" could not be droped in database.\nVersion: " + getSubmitInfo() + 
				"\nProtocol: \n" + controller.getInfoText());
	}

	public HashMap checkTableStructure(String archiveFileNameOrig,
	    String archiveFileNameDest) {
		return DbTableDefinitionCompare.checkTableStructure(archiveFileNameOrig,
		    archiveFileNameDest);
	}

	public void switchOnTrace() {
		//logger.switchOn();
	}

	public void switchOnTrace(int severity) {
		//logger.switchOn(severity);
	}

	public void switchOffTrace() {
		//logger.switchOff();
	}

	public String getInfoText() {
		return "";
		//return logger.getText();
	}

	public void setTraceSeverity(int severity) {
		this.severity = severity;
	}

	public int getTraceSeverity() {
		return severity;
	}

	private XmlMap getModifiedXmlMap(XmlMap map, boolean withoutPrimaryKey,
	    boolean withoutIndexes) {
		XmlMap targetMap = new XmlMap();
		XmlMap innerMap = (XmlMap) map.getXmlMap("Dbtable").clone();
		if (withoutPrimaryKey)
			innerMap.remove("primary-key");
		if (withoutIndexes)
			innerMap.remove("indexes");
		targetMap.put("Dbtable", innerMap);
		return targetMap;
	}
	
	private static String getSubmitInfo() {
		InputStream stream = DbModificationController.class.getResourceAsStream(
				"config/" + "submit" + ".txt");
		if (stream == null)
			return "no submit info";
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String res = "no submit info";
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("*") || line.startsWith("//") || line.length() == 0)
					continue;
				res = line;
			}
			reader.close();
			stream.close();
		} catch (IOException e) {
			return "no submit info";
		}
		return res;
	}
}