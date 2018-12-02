package com.sap.archtech.daservice.util;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.validation.Schema;

public class XmlSchemaProvider {

	public static final int XMLSCHEMACACHESIZE = 10;
	
	public static ArrayList<Long> xmlSchemaAccessList;
	public static HashMap<Long, Schema> xmlSchemaMap;

	// Add XML Schema Object To XML Schema Pool
	public synchronized static boolean addSchemaObject(long colId, Schema schema) {
		Long colIdLong = new Long(colId);
		if (XmlSchemaProvider.xmlSchemaMap.containsKey(colIdLong)) {
			return false;
		} else {
			if (XmlSchemaProvider.xmlSchemaAccessList.size() > XmlSchemaProvider.XMLSCHEMACACHESIZE) {
				Long key = (Long) XmlSchemaProvider.xmlSchemaAccessList
						.get(XmlSchemaProvider.xmlSchemaAccessList.size() - 1);
				XmlSchemaProvider.xmlSchemaAccessList
						.remove(XmlSchemaProvider.xmlSchemaAccessList.size() - 1);
				XmlSchemaProvider.xmlSchemaMap.remove(key);
			}
			XmlSchemaProvider.xmlSchemaAccessList.add(0, colIdLong);
			XmlSchemaProvider.xmlSchemaMap.put(colIdLong, schema);
			return true;
		}
	}

	// Remove XML Schema Object From XML Schema Pool
	public synchronized static boolean removeSchemaObject(long colId) {
		Long colIdLong = new Long(colId);
		if (XmlSchemaProvider.xmlSchemaMap.containsKey(colIdLong)) {
			XmlSchemaProvider.xmlSchemaAccessList.remove(colIdLong);
			XmlSchemaProvider.xmlSchemaMap.remove(colIdLong);
			return true;
		} else {
			return false;
		}
	}

	// Get XML Schema Object From XML Schema Pool
	public synchronized static Schema getSchemaObject(long colId) {
		Long colIdLong = new Long(colId);
		if (XmlSchemaProvider.xmlSchemaMap.containsKey(colIdLong)) {
			return (Schema) XmlSchemaProvider.xmlSchemaMap.get(colIdLong);
		} else {
			return null;
		}
	}

	// Checks If A XML Schema Object For A Collection Exists In XML Schema Pool
	public synchronized static boolean containsSchemaObject(long colId) {
		Long colIdLong = new Long(colId);
		if (XmlSchemaProvider.xmlSchemaMap.containsKey(colIdLong))
			return true;
		else
			return false;
	}
}
