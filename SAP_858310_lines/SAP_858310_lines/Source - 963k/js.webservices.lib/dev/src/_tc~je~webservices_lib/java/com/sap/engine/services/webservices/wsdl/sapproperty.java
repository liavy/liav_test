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

import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.util.QName;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import java.util.ArrayList;

/**
 * Class representing SAP Feature property.
 * It's qName property represents  
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class SAPProperty extends WSDLNode {
  
  protected ArrayList options;
  protected QName qname;
  
  public SAPProperty() {
    super();
    options = new ArrayList();
  }
  
  public SAPProperty(WSDLNode parent) {
    super(parent);
    options = new ArrayList();
  }
  
  public QName getQname() {
    return qname;
  }

  public void setQname(QName qname) {
    this.qname = qname;
  }
  
  public ArrayList getOptions() {
    return this.options;
  }
  
  public void addOption(SAPOption option) {
    this.options.add(option);
  }
  
  public void loadAttributes(Element element) throws WSDLException {
    Attr qname = element.getAttributeNode("qname");    
    if (qname == null ) {
      throw new WSDLException(" SAP Property must have 'qname' attribute !");
    }            
    String name = qname.getValue();    
    String uri = DOM.qnameToURI(name, element);
    if (uri == null) {
      throw new WSDLException(" QName prefix mapping not found for '"+name+"' in SAPProperty !");
    }
    this.qname = new QName(name,uri);
  }
  
}
                                 