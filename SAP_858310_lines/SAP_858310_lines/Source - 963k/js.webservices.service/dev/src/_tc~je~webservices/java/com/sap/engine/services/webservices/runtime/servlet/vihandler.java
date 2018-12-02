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

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Vector;

/**
 * @author Alexander Zubev
 */
public class VIHandler extends DefaultHandler {
  public static final String VI_REF_EL_NAME = "VirtualInterfaceReference";
  public static final String VI_GUID_ATTR_NAME = "viGuid";
  public static final String WSD_EL_NAME = "WebServiceDefinition";
  public static final String WSD_ATTR_NAME = "name";

  private Vector vis = new Vector();
  private String wsdName;

  public String[] getVIIds() {
    String[] viIds = new String[vis.size()];
    for (int i = 0; i < vis.size(); i++) {
      viIds[i] = vis.elementAt(i).toString();
    }
    return viIds;
  }

  public String getWSDName() {
    return wsdName;
  }

  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if (VI_REF_EL_NAME.equals(localName)) {
      String value = attributes.getValue(VI_GUID_ATTR_NAME);
      if (value != null) {
        vis.addElement(value);
      } else {
        throw new SAXException("Invalid WSD! Cannot determine the GUID of the VI!");
      }
    } else if (WSD_EL_NAME.equals(localName)) {
      wsdName = attributes.getValue(WSD_ATTR_NAME);
      if (wsdName == null) {
        throw new SAXException("Invalid WSD! Cannot determine its name!");
      }
    }
  }
}
