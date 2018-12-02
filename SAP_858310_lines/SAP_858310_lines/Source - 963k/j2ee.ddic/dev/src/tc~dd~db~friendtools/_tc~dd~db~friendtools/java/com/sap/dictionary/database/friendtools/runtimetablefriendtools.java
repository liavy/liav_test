/*
 * Created on May 14, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.sap.dictionary.database.friendtools;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.sap.dictionary.database.dbs.*;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Severity;
import com.sap.sql.services.OpenSQLServices;
import org.xml.sax.InputSource;
import com.sap.dictionary.database.opentools.*;
/**
 * @author d003550
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class RuntimeTableFriendTools
		implements
			FriendTools,
			DbsConstants,
			DbsSeverity {
	private static final Location loc = Location
			.getLocation(RuntimeTableFriendTools.class);
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	private DbFactory factory = null;
	private ICtNameTranslator translator = null;
	private DbRuntimeObjects originalRuntimeObjects = null;
	private DbRuntimeObjects runtimeObjects = null;
	private DbModificationController controller = null;

	public RuntimeTableFriendTools() throws JddException {
		if (!(Logger.hasLogs(cat) || Logger.hasLogs(loc))) {
			LoggingConfiguration.setProperty("default",
					"com.sap.dictionary.database.severity", "INFO");
			Logger.setLoggingConfiguration("default");
		}
	}	
	
	public RuntimeTableFriendTools(Connection con) throws JddException {
		this(con, null);
	}

	public RuntimeTableFriendTools(Connection con, ICtNameTranslator translator)
			throws JddException {
		if (!(Logger.hasLogs(cat) || Logger.hasLogs(loc))) {
			LoggingConfiguration.setProperty("default",
					"com.sap.dictionary.database.severity", "INFO");
			Logger.setLoggingConfiguration("default");
		}
		this.translator = translator;
		factory = new DbFactory(con);
		//Sets the translator in environment. Translator can also be null.
		factory.getEnvironment().setCtNamesTranslator(translator);
		/* Gets the runtimeObjects according to translator: If translator is
     * we get the shadow runtimeObjects. If not we get the 'normal' runtime
     * objects pointing to BC_DDDBTABLERT.		
		 */
		runtimeObjects = DbRuntimeObjects.getInstance(factory);
		if (translator != null
				&& !translator.translate(
						DbsConstants.RUNTIME_OBJECTS_TABLE_NAME).equals(
						RUNTIME_OBJECTS_TABLE_NAME)) {
		  /* In case translator is set we have already instantiated the shadow
		   * runtimeObjects. The original factory is built without translator.
		   * So the originalRuntimeObjects are those pointing to BC_DDDBTABLERT.
		   */
			DbFactory originalFactory = new DbFactory(con);
			originalRuntimeObjects = DbRuntimeObjects.getInstance(originalFactory);
		}
	}

	public boolean createTable(String name, InputStream stream) {
		DbModificationController controller = new DbModificationController(
				factory, false, translator, null);
		controller.getDeployTables().put(name, Action.CREATE, stream);
		controller.distribute();
		DbDeployResult result = controller.getDeployResult();
		result.log();
		if (result.get() < ERROR)
			return true;
		return false;
	}

	public boolean dropTable(String name) {
		DbModificationController controller = new DbModificationController(
				factory, translator, null);
		controller.getDeployTables().put(name, Action.DROP, null);
		controller.distribute();
		DbDeployResult result = controller.getDeployResult();
		result.log();
		if (result.get() < ERROR)
			return true;
		return false;
	}
	
	/**
	 * Writes stream to runtime-table. Database positions are accepted
	 * as written to xml-file and are not changed.
	 * 
	 * @param name Name of table
	 *        stream xml-File as stream
	 * @return true - if stream could be written, false otherwise
	 * @throws JddRuntimeException if xml could not be written
	 */
	public boolean xmlWrite(String name, InputStream stream) {
	  runtimeObjects.putTableWithoutCorrections(name,stream);
	  return true;
	}
	
	/**
	 * Writes stream to runtime-table. Database positions are newly set.
	 * The method only works if the database table exists
	 * 
	 * @param name Name of table
	 *        stream xml-File as stream
	 * @return true - if stream could be written, false otherwise
	 * @throws JddRuntimeException if xml could not be written
	 */
	public boolean xmlWriteSetDatabasePosition(String name, InputStream stream) {
	  runtimeObjects.put(name,"T",stream);
	  return true;
	}

	public String xmlRead(String name) {
	  return runtimeObjects.getXmlAsString(name);
	}
	
	public boolean xmlDelete(String name) {
	  runtimeObjects.remove(name);
	  return true;
	}

	public boolean xmlCopy(String[] names) {
		if (originalRuntimeObjects == null)
			return false;
		for (int i = 0; i < names.length; i++) {
		  runtimeObjects.putRow(originalRuntimeObjects.getRow(names[i]));
		}
		return true;
	}

	public boolean xmlCopy() {
		if (originalRuntimeObjects == null)
			return false;
		ArrayList origNames = originalRuntimeObjects.getAllValuesForColumn("NAME");
    ArrayList names = runtimeObjects.getAllValuesForColumn("NAME");
		for (int i = 0; i < origNames.size(); i++) {
		  if (!(names.contains(origNames.get(i))))
		    runtimeObjects.putRow(originalRuntimeObjects.getRow((String)origNames.get(i)));
		}
		return true;
	}	
	
	public boolean modifyTable(String name, InputStream stream) {
		DbModificationController controller = new DbModificationController(
				factory, false, translator, null);
		controller.getDeployTables().put(name, stream);
		controller.distribute();
		DbDeployResult result = controller.getDeployResult();
		result.log();
		if (result.get() < ERROR)
			return true;
		return false;
	}
	
	public Map getDifferences(String archiveFileNameOrig,
			String archiveFileNameDest) {
    return DbTableDefinitionCompare.getDifferences(archiveFileNameOrig,
    		archiveFileNameDest);
	}  
	
	public String[] getNames() {
      ArrayList names = runtimeObjects.getAllValuesForColumn("NAME");
	  return (String[])names.toArray(new String[0]);	
	}
}