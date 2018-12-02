/**
 * Title:        xml2000
 * Description:  This is class for binding input and output
 *
 * Copyright:    Copyright (c) 2001
 * Company:      InQMy
 * @author       Chavdar Baykov, Chavdarb@abv.bg
 * @version      July 2001
 */
package com.sap.engine.services.webservices.wsdl;

import com.sap.engine.lib.xml.parser.handlers.SimpleAttr;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import java.util.ArrayList;

public class WSDLBindingChannel extends WSDLNamedNode {

  private ArrayList extensions;

  public WSDLBindingChannel() {
    super();
    extensions = new ArrayList();
  }

  public WSDLBindingChannel(WSDLNode parent) {
    super(parent);
    extensions = new ArrayList();
  }

  public void addExtension(WSDLExtension extension) {
    extensions.add(extension);
  }

  public ArrayList getExtensions() {
    return extensions;
  }

  public void loadAttributes(Element element) throws WSDLException {
    Attr attribute = element.getAttributeNode("name");

    if (attribute != null) {
      super.setName(attribute.getValue());
    }
  }

  /**
   * Loads name property form List of attributes
   */
  public void loadAttributes(SimpleAttr[] attr, int attrCount) throws WSDLException {
    String name = SimpleAttr.getAttribute("name", attr, attrCount);
    super.setName(name);
  }

}

