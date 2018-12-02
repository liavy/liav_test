/**
 * Title:        xml2000
 * Description:  This is class for binding faults
 *
 * Copyright:    Copyright (c) 2001
 * Company:      InQMy
 * @author       Chavdar Baykov, Chavdarb@abv.bg
 * @version      July 2001
 */
package com.sap.engine.services.webservices.wsdl;

import com.sap.engine.lib.xml.parser.handlers.SimpleAttr;
import com.sap.engine.lib.xml.util.NamespaceContainer;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import java.util.ArrayList;

public class WSDLBindingFault extends WSDLNamedNode {

  private ArrayList extensions;

  public WSDLBindingFault() {
    super();
    extensions = new ArrayList();
  }

  public WSDLBindingFault(WSDLNode parent) {
    super(parent);
    extensions = new ArrayList();
  }

  public void addExtension(WSDLExtension extension) {
    extensions.add(extension);
  }

  public ArrayList getExtensions() {
    return extensions;
  }

  public void loadAttributes(SimpleAttr[] attr, int attrCount, NamespaceContainer uriContainer) throws Exception {
    String name = SimpleAttr.getAttribute("name", attr, attrCount);
    if (name != null) {
      this.name = name;
    }
  }

  public void loadAttributes(Element element) {
    Attr attribute = element.getAttributeNode("name");

    if (attribute != null) {
      this.setName(attribute.getValue());
    }
  }

}

