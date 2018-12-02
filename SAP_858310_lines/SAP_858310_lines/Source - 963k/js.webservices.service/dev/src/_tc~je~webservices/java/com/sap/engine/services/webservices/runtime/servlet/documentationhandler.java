/*
 * Copyright (c) 2002 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Sofia. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia.
 */
package com.sap.engine.services.webservices.runtime.servlet;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Alexander Zubev
 */
public class DocumentationHandler extends DefaultHandler {
  public static final String TRANS_UNIT_ELEMENT = "trans-unit";
  public static final String SOURCE_ELEMENT = "source";
  public static final String RESNAME_ATTR = "resname";
  public static final String LONG_DOCUMENTATION_ATTR_VALUE = "Documentation@longTextDocu";

  private String documentation;
  private boolean transUnitElementStarted = false;
  private boolean sourceElementStarted = false;

  public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
    if (TRANS_UNIT_ELEMENT.equals(localName) && LONG_DOCUMENTATION_ATTR_VALUE.equals(atts.getValue(RESNAME_ATTR))) {
      transUnitElementStarted = true;
    } else if (transUnitElementStarted && SOURCE_ELEMENT.equals(localName)) {
      sourceElementStarted = true;
    }
  }

  public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
    if (SOURCE_ELEMENT.equals(localName)) {
      sourceElementStarted = false;
    } else if (TRANS_UNIT_ELEMENT.equals(localName)) {
      transUnitElementStarted = false;
    }
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    if (sourceElementStarted) {
      documentation = new String(ch, start, length).trim();
    }
  }

  public String getDocumentation() {
    return documentation;
  }

  public void clear() {
    documentation = null;
    transUnitElementStarted = false;
    sourceElementStarted = false;
  }

}
