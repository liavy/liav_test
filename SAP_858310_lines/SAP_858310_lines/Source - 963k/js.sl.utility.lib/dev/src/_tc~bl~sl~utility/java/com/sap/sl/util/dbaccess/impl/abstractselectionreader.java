package com.sap.sl.util.dbaccess.impl;

import java.io.IOException;
import java.util.ArrayList;

import com.sap.sl.util.dbaccess.api.XMLException;

/**
 * Title:        Software Delivery Manager
 * Description:  Abstract class for parsing select files.
 *               Reads the xml file containing the selection description and
 *               builds the selection list.
 * 
 *               Instances of this class are created via
 *               com.sap.sdm.util.xml.XMLParserAccessFactory.
 * 
 * Copyright:    Copyright (c) 2003
 * Company:      SAP AG
 * @author Software Logistics - here: Carsten Thiel
 * @version 1.0
 */

abstract class AbstractSelectionReader {
	/* instance fields */
	private String selectFileName = null;
	
	/* static package methods */
	/**
	 * Create an instance of AbstractSelectionReader
	 * 
   * @param selectFileName the name of the select file
	 */
	static AbstractSelectionReader createSelectionReader(String selectFileName)
	{
		AbstractSelectionReader reader = new SelectionReader();
		reader.selectFileName = selectFileName;
  	return reader;
	}

	/* Niladic constructor for implementing classes */
	protected AbstractSelectionReader() {
		super();
	}

  /* public abstract methods */
	public abstract void parseSelections() throws IOException, XMLException;

	/**
	 * Returns a list with all selections and entries
	 */
  public abstract ArrayList getSelectionList();
  
  public abstract ArrayList getEntryList();

  /* protected final methods */
  /**
   * get the name of the select file
   */
  protected final String getSelectFileName() {
  	return this.selectFileName;
  }

	/** Methode zum lokalen Test */
	public static void main(String[] arg) throws Exception
	{
		AbstractSelectionReader selection =
		  AbstractSelectionReader.createSelectionReader("select.xml");
		selection.parseSelections();
		selection.getSelectionList();
		selection.getEntryList();
	}

}
