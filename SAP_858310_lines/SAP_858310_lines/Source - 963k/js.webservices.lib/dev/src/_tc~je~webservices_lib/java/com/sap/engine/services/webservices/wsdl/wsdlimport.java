/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.wsdl;

import com.sap.engine.lib.xml.parser.handlers.SimpleAttr;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * 
 *
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class WSDLImport extends WSDLNode {
  /**
   * Location attribute of this node.
   */
  private String location;
  /**
   * Namespace attribute of this node.
   */ 
  private String namespace;

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  /**
   * Default constructor.
   */
  public WSDLImport() {
    super();
  }

  /**
   * Constructor with parent.
   */
  public WSDLImport(WSDLNode parent) {
    super(parent);
  }

  /**
   * Constructor with parent and location attribute.
   */
  public WSDLImport(WSDLNode parent, String location) {
    super(parent);
    this.location = location;
  }

  /**
   * Constructor with parent ,location and namepsace attribute.
   */
  public WSDLImport(WSDLNode parent, String location, String namespace) {
    super(parent);
    this.location = location;
    this.namespace = namespace;
  }
  
  /**
   * Sets name attribute for this node.
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Returns name attribute for this node.
   */
  public String getLocation() {
    return location;
  }

  /**
   * Loads name property form List of attributes. (Used from WSDL Handler will be deprecated)
   */
  public void loadAttributes(SimpleAttr[] attr, int attrCount) throws WSDLException {
    String location = SimpleAttr.getAttribute("location", attr, attrCount);
    this.namespace = SimpleAttr.getAttribute("namespace", attr, attrCount);

    if (location == null) {
      throw new WSDLException(" No location attribute !");
    } else {
      this.location = location;
    }
  }

  /**
   * Loads attributes from dom element.
   */
  public void loadAttributes(Element element) throws WSDLException {
    Attr attribute = element.getAttributeNode("location");
    if (element.getAttributeNode("namespace")!= null) {
      this.namespace = element.getAttribute("namespace");
    }    
    if (attribute == null) {
      throw new WSDLException(" No location attribute found !");
    } else {
      this.location = attribute.getValue();
    }
  }
  
  
}
