/*
 * Copyright (c) 2002 by SAP AG.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP AG.
 * Created : July 2001
 */
package com.sap.engine.services.webservices.wsdl;

import com.sap.engine.lib.xml.parser.handlers.SimpleAttr;
import com.sap.engine.lib.xml.util.NamespaceContainer;
import com.sap.engine.lib.xml.util.QName;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Port reptresentation.
 * @author Chavdar Baykov, chavdar.baikov@sap.com
 * @version 2.0
 */
public class WSDLPort extends WSDLNamedNode {

  /**
   * Name of binding connected to that port.
   */
  private QName binding;
  /**
   * Extensibility element found in this port.
   */
  private WSDLExtension extension;

  /**
   * Default constructor.
   */
  public WSDLPort() {
    super();
    binding = null;
    extension = null;
  }

  /**
   * Constructor with parent.
   */
  public WSDLPort(WSDLNode parent) {
    super(parent);
    binding = null;
    extension = null;
  }

  /**
   * Sets linked binding.
   */
  public void setBinding(QName binding) {
    this.binding = binding;
  }

  /**
   * Returns linked binding.
   */
  public QName getBinding() {
    return binding;
  }

  /**
   * Sets extension element.
   */
  public void setExtension(WSDLExtension extension) {
    this.extension = extension;
  }

  /**
   * Returns extension element.
   */
  public WSDLExtension getExtension() {
    return extension;
  }

  /**
   * Loads WSDL Port attributes (used by WSDL Handler)
   */
  public void loadAttributes(SimpleAttr[] attr, int attrCount, NamespaceContainer uriContainer) throws Exception {
    super.loadAttributes(attr, attrCount);
    String binding = SimpleAttr.getAttribute("binding", attr, attrCount);

    if (binding == null) {
      throw new WSDLException(" Binding attribute must present !");
    } else {
      this.binding = QName.qnameWSDLCreate(binding, uriContainer, ((WSDLDefinitions) getDocument()).targetNamespace);
    }
  }

  /**
   * Loads attributes from dom element.
   */
  public void loadAttributes(Element element) throws WSDLException {
    super.loadAttributes(element);
    Attr attribute = element.getAttributeNode("binding");

    if (attribute == null) {
      throw new WSDLException(" Binding attribute must present in wsdl:portType !");
    } else {
      this.binding = super.getQName(attribute.getValue(), element);
    }
  }

}

