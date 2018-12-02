/**
 * Title:        xml2000
 * Description:  Holds PortType operation Fault Channels
 *
 * Copyright:    Copyright (c) 2001
 * Company:      InQMy
 * @author       Chavdar Baykov, Chavdarb@abv.bg
 * @version      July 2001
 */
package com.sap.engine.services.webservices.wsdl;

import com.sap.engine.lib.xml.parser.handlers.SimpleAttr;
import com.sap.engine.lib.xml.util.NamespaceContainer;
import com.sap.engine.lib.xml.util.QName;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

public class WSDLFault extends WSDLNamedNode {

  private QName message;

  public WSDLFault() {
    super();
    message = null;
  }

  public WSDLFault(WSDLNode parent) {
    super(parent);
    message = null;
  }

  public void setMessage(QName messageid) {
    this.message = messageid;
  }

  public QName getMessage() {
    return message;
  }

  public void loadAttributes(SimpleAttr[] attr, int attrCount, NamespaceContainer uriContainer) throws Exception {
    String name = SimpleAttr.getAttribute("name", attr, attrCount);

    if (name != null) {
      this.name = name;
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
      throw new WSDLException(" WSDLFault should contain message attribute !");
    }
  }

}

