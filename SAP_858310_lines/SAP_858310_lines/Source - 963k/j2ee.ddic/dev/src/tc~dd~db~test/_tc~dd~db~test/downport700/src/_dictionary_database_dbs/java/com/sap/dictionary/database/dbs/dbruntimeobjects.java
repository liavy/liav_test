package com.sap.dictionary.database.dbs;

/**
 * @author d003550
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.GregorianCalendar;

import org.xml.sax.InputSource;

import com.sap.sql.services.OpenSQLServices;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class DbRuntimeObjects extends DbBasicTable implements DbsSeverity {
	private static final Location loc = Location.getLocation(DbRuntimeObjects.class);
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);
    private DbFactory factory = null;

	private DbRuntimeObjects(DbFactory factory) {
        this.factory = factory;
		factory.getEnvironment().setRuntimeObjects((DbRuntimeObjects)this);
		constructorPart(factory,getXmlMap(factory));
	}

	private DbRuntimeObjects(Connection con) {
		try {
			factory = new DbFactory(con);
			factory.getEnvironment().setRuntimeObjects((DbRuntimeObjects)this);
			constructorPart(factory,getXmlMap(factory));
		} catch (Exception e) {
			throw new JddRuntimeException(e,XML_ANALYSE_ERR,new Object[]{
					RUNTIME_OBJECTS_TABLE_NAME},cat,Severity.ERROR, loc);
		}
	}
	
	public static DbRuntimeObjects getInstance(DbFactory factory) {
		DbRuntimeObjects instance = factory.getEnvironment().getRuntimeObjects();
		if (instance != null)
			return instance;
		else {
			// set into environment during constructor run
			return new DbRuntimeObjects(factory);
		}
	}

	public void put(String name, String type, Object xmlData) {
		if (type.equalsIgnoreCase("T")) {
			putTable(name,xmlData);
		} else {
			putView(name,xmlData);
		}
	}
		
	public void putTable(String name, Object xmlData) {
		XmlMap xmlMap = extractXmlMap(xmlData);
		StringWriter swriter = new StringWriter();
		PrintWriter writer = new PrintWriter(swriter);
		XmlMap table = xmlMap.getXmlMap("Dbtable");
		try {
			DbTable tabViaXml = factory.makeTable();
			tabViaXml.setCommonContentViaXml(xmlMap);
			tabViaXml.getColumns().setDatabasePosition();
			tabViaXml.writeCommonContentToXmlFile(writer, "");
		} catch (Exception ex) {
			throw new JddRuntimeException(ex,RTXML_DBREAD_ERR, new Object[] { name },
				 cat, Severity.ERROR, loc);
		}
		//Component and Prefix are not given yet
		putRow(new Object[] { name, new Long(DbTools.currentTime()), "T", "*",
				swriter.toString()});
        //Invalidate entry for name in table-buffer
        factory.getTools().invalidate(name);
	}
		
	public void putTableWithoutCorrections(String name, Object xmlData) {
		//Component and Prefix are not given yet
		putRow(new Object[] { name, new Long(DbTools.currentTime()), "T", "*", xmlData});
        //Invalidate entry for name in table-buffer
        factory.getTools().invalidate(name);
	}
	
	public void putTableWithoutCorrections(DbTable dbtable) {
		String name = dbtable.getName();
		StringWriter swriter = new StringWriter();
		PrintWriter writer = new PrintWriter(swriter);
		try {
	    dbtable.writeCommonContentToXmlFile(writer, "");
		} catch (Exception ex) {
			throw new JddRuntimeException(ex,RTXML_DBREAD_ERR, new Object[] { name },
				 cat, Severity.ERROR, loc);
		}	
		//Component and Prefix are not given yet
		putRow(new Object[] { name, new Long(DbTools.currentTime()),
				"T", "*",swriter.toString()});
		//Invalidate entry for name in table-buffer
		factory.getTools().invalidate(name);
	}
	
	public void putView(String name, Object xmlData) {
		if (xmlData instanceof XmlMap) {
			XmlMap xmlMap = (XmlMap) xmlData;
			StringWriter swriter = new StringWriter();
			PrintWriter writer = new PrintWriter(swriter);
			try {
				DbView viewViaXml = factory.makeView();
				viewViaXml.setCommonContentViaXml(xmlMap);
				viewViaXml.writeCommonContentToXmlFile(writer, "");
				xmlData = swriter.toString();
			} catch (Exception ex) {
				throw new JddRuntimeException(ex,RTXML_DBREAD_ERR_VIEW,new Object[]{name},
						cat, Severity.ERROR, loc);
			}
		}
		//Component and Prefix are not given yet
		putRow(new Object[] { name, new Long(DbTools.currentTime()), "V", "*", xmlData});
        //Invalidate entry for name in table-buffer
        factory.getTools().invalidate(name);
	}

	public void put(Object xmlData) {
		XmlMap xmlMap = extractXmlMap(xmlData);
		String name = null;
		String type = "T";
		XmlMap table = xmlMap.getXmlMap("Dbtable");
		if (table.isEmpty()) {
			XmlMap view = xmlMap.getXmlMap("Dbview");
			name = view.getString("name");
			type = "V";
			putView(name,xmlMap);
		} else {
			name = table.getString("name");
			putTable(name,xmlMap);		
		}
	}

	public XmlMap get(String name) {
		String xmlAsString = (String) getRow(name)[4];
		if (xmlAsString != null)
			return new XmlExtractor().map(new InputSource(new StringReader(xmlAsString)));
		else
			return null;
	}

	public String getXmlAsString(String name) {
		return (String) getRow(name)[4];
	}

	public Object[] getRow(String name) {
		return super.getRow(name);
	}

	public String getType(String name) throws JddException {
		return (String) super.getField(name, "TYPE");
	}

	public String getAccess(String name) throws JddException {
		return (String) super.getField(name, "ACCESS");
	}
	
	public void remove(String name) {
		super.removeRow(name);
        //Invalidate entry for name in table-buffer
        factory.getTools().invalidate(name);
	}
	
	private XmlMap extractXmlMap(Object xmlData) {
		if (xmlData == null)
			return null;
		else if (xmlData instanceof XmlMap)
			return (XmlMap) xmlData;
		else if (xmlData instanceof String)
			return new XmlExtractor().map(new InputSource(new StringReader(
				(String)xmlData)));
		else if (xmlData instanceof Reader)
			return new XmlExtractor().map(new InputSource((Reader)xmlData));
		else if (xmlData instanceof InputStream)
			return new XmlExtractor().map(new InputSource((InputStream)xmlData));
		else if (xmlData instanceof File)
			try {
				return new XmlExtractor().map(new InputSource (new FileInputStream((
						File)xmlData)));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				return null;
			}
		else
			return null;
	}
	
	private static XmlMap getXmlMap(DbFactory factory) {
		String specialName = factory.getEnvironment().getSpecialName(
				RUNTIME_OBJECTS_TABLE_NAME);
		XmlMap tableMap = getXmlMap(RUNTIME_OBJECTS_TABLE_NAME);
		if (!RUNTIME_OBJECTS_TABLE_NAME.equals(specialName)) {
			XmlMap innerMap = tableMap.getXmlMap("Dbtable"); 
			innerMap.put("name",specialName);
			XmlMap prkeyMap = (XmlMap) innerMap.getXmlMap("primary-key");
			prkeyMap.put("tabname", specialName);
			XmlMap indexesMap = (XmlMap) innerMap.getXmlMap("indexes");
			XmlMap indexMap = null;
			for (int j = 0;!(indexMap =
					indexesMap.getXmlMap("index", j)).isEmpty();j++) {
				indexMap.put("tabname", specialName);
			}
		}
		return tableMap;
	}
}
