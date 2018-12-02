package com.sap.dictionary.database.dbs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.xml.sax.InputSource;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class XmlHelper implements DbsConstants {
	static final String tabulator = "      ";
	public static final String xmlTitle =
				 "<?xml version=" + "\"" + "1.0" + "\"" + "?>";

	public XmlHelper() {
	}

	public static String tabulate() {
		return tabulator;
	}

	public static String checkAndGetName(String name, String type)
				 throws JddException {
		if ((name == null) || (name.trim().equalsIgnoreCase(""))) {
			//& name is missing in XML-File
			throw new JddException(ExType.XML_ERROR,
						 DbMsgHandler.get(XML_TAGNAME_MISS, new Object[] { type }));
		}
		if (name.startsWith("\""))
			if (name.endsWith("\"")) {
				return name.substring(1, name.length() - 1);
			} else {
				//Quotes at end of identifier are missing
				throw new JddException(ExType.XML_ERROR,
							 DbMsgHandler.get(XML_IDQUOTES_MISS));
			}
		else {
			return name.toUpperCase();
		}
	}
	
	protected static XmlMap extractXmlMap(Object xmlData) {
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
}
