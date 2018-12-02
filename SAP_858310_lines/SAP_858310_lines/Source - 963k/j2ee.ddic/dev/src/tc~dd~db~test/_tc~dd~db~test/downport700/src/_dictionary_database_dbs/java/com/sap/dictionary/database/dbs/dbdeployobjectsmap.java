/*
 * Created on Apr 28, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.sap.dictionary.database.dbs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.*;
import java.util.*;

import org.xml.sax.InputSource;

import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
/**
 * @author d003550
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DbDeployObjectsMap implements DbsConstants, IDbDeployObjects {
	private static final Location loc = 
		Location.getLocation(DbBasicTable.class);
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	HashMap deployObjects = new HashMap();
	LinkedList listForFullScan = new LinkedList();
	LinkedList listForAnalyse = new LinkedList();
	LinkedList listForModify = new LinkedList();
	LinkedList listForDelete = new LinkedList();
	LinkedList listForConvert = new LinkedList();
	LinkedList listForError = new LinkedList();
	boolean dbModificationNec = false;
	boolean dbConversionNec = false;

	public DbDeployObjectsMap() {
		
	}

	public void put(String name, long timestamp, XmlMap xmlMap) {
		put(name, timestamp, null,null, xmlMap);
	}

	public void put(String name, Action action, Object xmlData) {
		put(name, DbTools.currentTime(), action,null, xmlData);
	}
	
	public void put(String name,Action action,String hints,Object xmlData) {
		put(name,DbTools.currentTime(),action,hints,xmlData);
	}

	public void put(String name, Object xmlData) {
		put(name, DbTools.currentTime(), null,null, xmlData);
	}

	public void put(String name, long timestamp, Object xmlData) {
		put(name,timestamp, null,null,xmlData);
	}
	
	public void put(String name,long timestamp,Action action,Object xmlData) {
		put(name,timestamp, action,null,xmlData);
	}

	public void put(String name,long timestamp,Action action,String hints,
			Object xmlData) {
		put(name, new Object[]{name.toUpperCase(), new Long(timestamp), "",
				action, hints, WAIT, xmlData});
	}

	/**
	 * Variable info contains String name, Long timestamp, String(1) modKind,
	 * Action action, String(1) state, XmlMap xmlMap in this sequence
	 */
	public synchronized void put(String name, Object[] info) {
		String upperCaseName = name.toUpperCase();
		try {
			info[XMLMAP] = convertXmlData(info[XMLMAP]);
		} catch (Exception ex) {
			throw new JddRuntimeException(ex,XML_ANALYSE_ERR,new Object[]{name},
					 cat, Severity.ERROR, loc);
		}
		deployObjects.put(upperCaseName, info);
		listForFullScan.add(upperCaseName);
		listForAnalyse.add(upperCaseName);
	}

	public synchronized XmlMap get(String name) {
		return (XmlMap) (((Object[]) deployObjects.get(name))[XMLMAP]);
	}

	public synchronized Object[] getRow(String name) {
		return (Object[]) deployObjects.get(name);
	}

	public synchronized boolean contains(String name) {
		return deployObjects.containsKey(name);
	}

	public synchronized boolean isEmpty() {
		return deployObjects.isEmpty();
	}
	
	public synchronized Object[] nextToFullScan() {
		while (!listForFullScan.isEmpty()) {
			Object name = listForFullScan.removeFirst();
			Object[] deployInfo = (Object[]) deployObjects.get(name);
			if (deployInfo != null)
				return deployInfo;			
		}
		return null;
	}

	public synchronized Object[] nextToAnalyse() {
		while (!listForAnalyse.isEmpty()) {
			Object name = listForAnalyse.removeFirst();
			Object[] deployInfo = (Object[]) deployObjects.get(name);
			if (deployInfo != null && deployInfo[STATUS].equals(WAIT))
				return deployInfo;			
		}
		return null;
	}
	
	public synchronized Object[] nextToModify() {
		while (!listForModify.isEmpty()) {
			Object name = listForModify.removeFirst();
			Object[] deployInfo = (Object[]) deployObjects.get(name);
			if (deployInfo != null && deployInfo[STATUS].equals(ANALYSED) &&
					!deployInfo[ACTION_].equals(Action.CONVERT))
				return deployInfo;			
		}
		return null;
	}	
	
	public synchronized boolean hasNextToModify() {	
	  return !listForModify.isEmpty();
	}
	
	public synchronized Object[] nextToDelete() {
		while (!listForDelete.isEmpty()) {
			Object name = listForDelete.removeFirst();
			Object[] deployInfo = (Object[]) deployObjects.get(name);
			if (deployInfo != null && deployInfo[STATUS].equals(ANALYSED) &&
					deployInfo[ACTION_].equals(Action.DROP))
				return deployInfo;			
		}
		return null;
	}
	
	public synchronized Object[] nextForError() {
		while (!listForError.isEmpty()) {
			Object name = listForError.removeFirst();
			Object[] deployInfo = (Object[]) deployObjects.get(name);
			if (deployInfo != null && deployInfo[STATUS].equals(ERROR))
				return deployInfo;			
		}
		return null;
	}

	public synchronized Object[] nextToConvert() {
		while (!listForConvert.isEmpty()) {
			Object name = listForConvert.removeFirst();
			Object[] deployInfo = (Object[]) deployObjects.get(name);
			if (deployInfo != null && deployInfo[STATUS].equals(ANALYSED) &&
					deployInfo[ACTION_].equals(Action.CONVERT))
				return deployInfo;			
		}
		return null;
	}		

	public synchronized boolean hasNextToConvert() {	
		  return !listForConvert.isEmpty();
		}
	
	public synchronized void reset() {
		deployObjects.clear();
		listForFullScan.clear();
		listForAnalyse.clear();
		listForModify.clear();
		listForDelete.clear();
		listForConvert.clear();
		listForError.clear();
		boolean dbModificationNec = false;
		boolean dbConversionNec = false;
	}

	public synchronized void remove(String name) {
		deployObjects.remove(name);
		listForFullScan.remove(name);
		listForAnalyse.remove(name);
		listForModify.remove(name);
		listForDelete.remove(name);
		listForConvert.remove(name);
		listForError.remove(name);
	}

	public synchronized Action getAction(String name) {
		return (Action) ((Object[]) deployObjects.get(name))[ACTION_];
	}

	public synchronized void setAction(String name, Action action) {
		Object[] deployInfo = (Object[]) deployObjects.get(name);
		if (deployInfo != null) {
			deployInfo[ACTION_] = action;
			adjustLists(name,deployInfo);
		}
	}

	public synchronized void setStatus(String name, String status) {
		Object[] deployInfo = (Object[]) deployObjects.get(name);
		deployInfo[STATUS] = status;
		adjustLists(name,deployInfo);
	}

	public synchronized void setAnalyseResult(String name, Action action,String hints,
			String status) {
		Object[] deployInfo = (Object[]) deployObjects.get(name);
		deployInfo[ACTION_] = action;
		deployInfo[HINTS] = hints;
		deployInfo[STATUS] = status;
		adjustLists(name,deployInfo);
	}
	
	private void adjustLists(String name,Object[] deployInfo) {
		Action action = (Action)deployInfo[ACTION_];
		String status = (String)deployInfo[STATUS];
		if (status.equals(WAIT))
			listForAnalyse.addLast(name);
		if (status.equals(ANALYSED) && !action.equals(Action.CONVERT)) {
			listForModify.addLast(name);
			if (!action.equals(Action.CONVERT) && !action.equals(Action.NOTHING))
				dbModificationNec = true;	
		}	
		if (status.equals(ERROR))
			listForError.addLast(name);
		if (status.equals(ANALYSED) && action.equals(Action.DROP))
			listForDelete.addLast(name);
		if (status.equals(ANALYSED) && action.equals(Action.CONVERT)) {
			listForConvert.addLast(name);
		    dbConversionNec = true;
		}	
	}
	
	public Object convertXmlData(Object xmlData) throws Exception {
		if (xmlData instanceof BufferedReader)
			return getString((BufferedReader)xmlData);
		if (xmlData instanceof Reader) {
			BufferedReader bufferedReader = new BufferedReader((Reader)xmlData);
			return getString(bufferedReader);
		}
		else if (xmlData instanceof InputStream) {
			InputStreamReader reader = new InputStreamReader((InputStream)xmlData);
			BufferedReader bufferedReader = new BufferedReader(reader);
			return getString(bufferedReader);
		}
		else if (xmlData instanceof File) {
			InputStreamReader reader = new InputStreamReader(new FileInputStream(
					(File)xmlData));
			BufferedReader bufferedReader = new BufferedReader(reader);
			return getString(bufferedReader);
		}
		else
			return xmlData;	
	}
	
	private static String getString(BufferedReader bufferedReader)
			throws Exception {
		String s;
		StringBuffer buffer = new StringBuffer();
		while ((s = bufferedReader.readLine()) != null) {
			buffer.append(s);
		}
		return buffer.toString();
	}
	
	public boolean dbModificationNecessary() {
		return dbModificationNec;
	}
	
	public boolean dbConversionNecessary() {
		return dbConversionNec;
	}
	
	public synchronized String toString() {
		String result = "\n";
		if (dbModificationNec)
			result += "Modification is necessary \n";
		if (dbConversionNec)
			result += "Conversion is necessary \n";
		result += "listForFullScan" + listForFullScan + "\n";
		result += "listForAnalyse" + listForAnalyse + "\n";
		result += "listForModify" + listForModify + "\n";
		result += "listForDelete" + listForDelete + "\n";
		result += "listForConvert" + listForConvert + "\n";
		result += "listForError" + listForError + "\n";
		result += "deployObjects" + deployObjects + "\n";
		Set ds = deployObjects.entrySet();
		Iterator it = ds.iterator();
		Map.Entry entry = null;
		String key = null;
		Object[] value = null;
		String delta = null;
		while (it.hasNext()) {
			entry = (Map.Entry)it.next();
			key = (String)entry.getKey();
			value = (Object[])entry.getValue();
			delta = "";
			for (int i = 0; i < value.length; i++) {
	      delta += value[i] + " ";
      }
			result += "\n-> " + key + " = " + delta + "\n";
		}
		
		return result;
	}
}