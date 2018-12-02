/**
 * Title:        xml2000
 * Description:  Holds PortType operation Input/Output Channels
 *
 * Copyright:    Copyright (c) 2001
 * Company:      InQMy
 * @author       Chavdar Baykov, Chavdarb@yahoo.com
 * @version      July 2001
 */
package com.sap.engine.services.webservices.wsdl;

import com.sap.engine.lib.xml.parser.handlers.SimpleAttr;
import com.sap.engine.lib.xml.util.NamespaceContainer;
import com.sap.engine.lib.xml.util.QName;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

public class WSDLChannel extends WSDLNamedNode {

  private QName message;

  public WSDLChannel() {
    super();
    message = null;
  }

  public WSDLChannel(WSDLNode parent) {
    super(parent);
    message = null;
  }

  public void setMessage(QName messageid) {
    this.message = messageid;
  }

  public QName getMessage() {
    return message;
  }

  /**
   * Used by WSDLHandler
   */
  public void loadAttributes(SimpleAttr[] attr, int attrCount, NamespaceContainer uriContainer) throws Exception {
    try {
      super.loadAttributes(attr, attrCount);
    } catch (Exception e) {
      //$JL-EXC$
      // This Element can have no name attribute
    }
    String qname = SimpleAttr.getAttribute("message", attr, attrCount);

    if (qname != null) {
      message = QName.qnameWSDLCreate(qname, uriContainer, ((WSDLDefinitions) getDocument()).targetNamespace);
    } else {
      throw new WSDLException(" Must contain message attribute !");
    }
  }

  public void loadAttributes(Element element) throws WSDLException {
    Attr attribute = element.getAttributeNode("name");

    if (attribute != null) {
      this.setName(attribute.getValue());
    }

    Attr messageAttr = element.getAttributeNode("message");

    if (messageAttr != null) {
      this.message = super.getQName(messageAttr.getValue(), element);
    } else {
      throw new WSDLException(" WSDLChannel (Input/Output) should contain message attribute !");
    }
  }

}

