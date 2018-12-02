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
import com.sap.engine.lib.xml.dom.DOM;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Class Representing SAP Option WSDL Extension element.
 *  
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class SAPOption extends WSDLNode {
  
  protected String value;
  
  public SAPOption() {
    super();
  }
  
  public SAPOption(WSDLNode parent) {
    super(parent);    
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Loads name property form List of attributes. (Used from WSDL Handler will be deprecated)
   */
  public void loadAttributes(SimpleAttr[] attr, int attrCount) throws WSDLException {
    String feature = SimpleAttr.getAttribute("value", attr, attrCount);

    if (feature == null) {
      throw new WSDLException(" No 'value' attribute !");
    } else {
      this.value = value;
    }
  }

  /**
   * Loads attributes from dom element.
   */
  public void loadAttributes(Element element) throws WSDLException {
    Attr attribute = element.getAttributeNode("value");
    
    if (attribute == null) {
      throw new WSDLException(" No 'value' attribute found !");
    } else {
      this.value = attribute.getValue();
      this.value = DOM.qnameToLocalName(this.value);
    }
  }
  
}
