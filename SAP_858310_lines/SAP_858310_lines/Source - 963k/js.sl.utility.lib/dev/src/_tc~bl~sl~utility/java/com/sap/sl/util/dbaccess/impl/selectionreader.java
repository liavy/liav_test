package com.sap.sl.util.dbaccess.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sap.sl.util.dbaccess.api.*;

/**
 * Title:        Software Delivery Manager
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      SAP AG
 * @author Software Logistics - here: Carsten Thiel
 * @version 1.0
 */

public class SelectionReader extends AbstractSelectionReader {
  /* instance fields */
	private ArrayList selectionList = new ArrayList();
	private ArrayList entryList = new ArrayList();

	private SAXParser parser = null; // the XML parser.
	private SaxContentHandler contentHandler = null; // the content handler

  /**
   * niladic constructor for instantiation via Class.newInstance()
   */
  SelectionReader() {
    super();
		try {
			SAXParserFactory parserfactory = SAXParserFactory.newInstance();
			parserfactory.setValidating(false);
			this.parser = parserfactory.newSAXParser();
		} catch (ParserConfigurationException pcE) {
			throw new RuntimeException("Cannot create XML parser.", pcE);
		} catch (SAXException saxE) {
			throw new RuntimeException("Cannot create XML parser.", saxE);
		}
  }

  /* (non-Javadoc)
   * @see com.sap.sl.util.dbaccess.impl.AbstractSelectionReader#parseSelections()
   */
  public void parseSelections() throws IOException, XMLException
  {
		this.selectionList = new ArrayList();
		this.entryList = new ArrayList();

		File xmlfile = new File(this.getSelectFileName());      
		if (xmlfile.exists() && xmlfile.canRead())
		{
			/** @todo trace.finer("parse the xml file: "+xmlfile.getPath()); */
			try {
  			this.contentHandler = this.new SaxContentHandler();
  			this.parser.parse(xmlfile, this.contentHandler);
			} catch (SAXException se)	{
					throw new XMLException("Syntax error in "+xmlfile.getPath()+": "+se.getMessage());
			} catch (IOException ioe) {
    			throw new XMLException("Cannot read "+xmlfile.getPath()+": "+ioe.getMessage());
			}
		}
		else
		{
			System.out.println("Cannot access file: "+this.getSelectFileName());
  			throw new IOException("Cannot access file: "+this.getSelectFileName());
		}
  }

  /* (non-Javadoc)
   * @see com.sap.sl.util.dbaccess.impl.AbstractSelectionReader#getSelectionList()
   */
  public ArrayList getSelectionList() {
    return this.selectionList;
  }

  /* (non-Javadoc)
   * @see com.sap.sl.util.dbaccess.impl.AbstractSelectionReader#getEntryList()
   */
  public ArrayList getEntryList() {
    return this.entryList;
  }

	/* inner class SaxContentHandler */
	private class SaxContentHandler extends DefaultHandler {
		/* reference to enclosing instance for convenience */
		private SelectionReader reader;
		/* instance fields */
		private Selection selection = null;
		private int counter = 0;
		private boolean selectionstagopen = false;
		private boolean tabletagopen = false;
		private boolean whereclausetagopen = false;
		private boolean entrytagopen = false;
		private boolean entry_scanned = false;
		private boolean unknowntagopen = false;
		private String currentTableName = null;
		private String currentWhereclause = null;
		private TableEntry currentTableEntry = null;

		SaxContentHandler() {
			super();
			this.reader = SelectionReader.this;
		} // SaxContentHandler()

		public void startElement(String ns, String localname, String qname, Attributes attributes) throws SAXException
		{
			String elementname=("".equals(localname))?qname:localname;

			if (elementname.equalsIgnoreCase("selections"))
			{
				if (selectionstagopen)
					throw new SAXException("selections tag is already open");
				selectionstagopen = true;
			}
			else if (elementname.equalsIgnoreCase("table"))
			{
				if (tabletagopen)
					throw new SAXException("table tag is already open");
        
				tabletagopen = true;
    
				if (attributes.getIndex("name")!=-1)
				{
					currentTableName = attributes.getValue(attributes.getIndex("name")).trim();
					currentWhereclause = null;
				}
				else
					throw new SAXException("missing table name");
			}
			else if (elementname.equalsIgnoreCase("where") ||
							 elementname.equalsIgnoreCase("whereclause") ||
							 elementname.equalsIgnoreCase("wc"))
			{
				if (!tabletagopen)
					throw new SAXException("whereclause without preceeding table element");
				if (whereclausetagopen)
				{
					// this is not the first wherecluase, so create a Selection instance for the previously read whereclause
					selection = new Selection(Integer.toString(counter),currentTableName,currentWhereclause);
					this.reader.selectionList.add(selection);
					counter++;
				}

				whereclausetagopen = true;
      
				if (attributes.getIndex("text")!=-1)
				{
					currentWhereclause = attributes.getValue(attributes.getIndex("text")).trim();
				}
				else
					throw new SAXException("missing whereclause text");
			}
			else if (elementname.equalsIgnoreCase("entry"))
			{
				if (!tabletagopen)
					throw new SAXException("entry without preceeding table element");

				entrytagopen = true;
				entry_scanned = true;
				currentTableEntry = new TableEntry(Integer.toString(counter),currentTableName);
				counter++;
			}
			else if (elementname.equalsIgnoreCase("field"))
			{
				String current_name = null;
				String current_value = null;
      
				if (!tabletagopen)
					throw new SAXException("field without preceeding table element");
				if (!entrytagopen)
					 throw new SAXException("field without preceeding entry element");
      
				if (attributes.getIndex("name")!=-1)
					current_name = attributes.getValue(attributes.getIndex("name")).trim();
				else
					throw new SAXException("missing name in field entry");
        
				if (attributes.getIndex("value")!=-1)
					current_value = attributes.getValue(attributes.getIndex("value"));
				else
					throw new SAXException("missing value in field entry");
        
				currentTableEntry.addField(new NameValuePair(current_name,current_value));
			}
			else
			{
				throw new SAXException("invalid token "+elementname);
				// unknowntagopen = true;
			}
		}
  
		public void endElement(String ns, String localname, String qname) throws SAXException
		{
			String elementname=("".equals(localname))?qname:localname;
			if (elementname.equalsIgnoreCase("selections"))
			{
				selectionstagopen = false;
			}
			else if (elementname.equalsIgnoreCase("table"))
			{
				selection = new Selection(Integer.toString(counter),currentTableName,currentWhereclause);
				if (!entry_scanned)
				{
					this.reader.selectionList.add(selection);
					counter++;
				}
				tabletagopen = false;
				whereclausetagopen = false;
				entry_scanned = false;
			}
			else if (elementname.equalsIgnoreCase("where") ||
							 elementname.equalsIgnoreCase("whereclause") ||
							 elementname.equalsIgnoreCase("wc"))
			{
				/* nothing to do */
			}
			else if (elementname.equalsIgnoreCase("entry"))
			{
				entrytagopen = false;
				if (currentTableEntry != null && currentTableEntry.getFieldCount() > 0)
					this.reader.entryList.add(currentTableEntry);
			}
			else if (elementname.equalsIgnoreCase("field"))
			{
				/* nothing to do */
			}
			else
			{
				if (!unknowntagopen)
					throw new SAXException("incomplete implementation of "+elementname+" tag");  
				unknowntagopen = false;
			}
		}

		public void startDocument() throws SAXException {};
		public void endDocument() throws SAXException {};

	} // inner class SaxContentHandler
}
