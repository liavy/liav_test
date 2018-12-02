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
 * PortType representation.
 * @author Chavdar Baykov, chavdar.baikov@sap.com
 * @version 2.0
 */
public class WSDLPart extends WSDLNamedNode {//$JL-EQUALS$

  public static final int UNKNOWN = 0;
  public static final int STRUCTURED_TYPE = 1;
  public static final int SIMPLE_TYPE = 2;
  public boolean inout = false;
  private int partStyle = UNKNOWN;
  private QName type;

  public WSDLPart() {
    super();
    type = null;
  }

  public WSDLPart(WSDLNode parent) {
    super(parent);
    type = null;
  }

  public void setType(int style, QName type) {
    this.partStyle = style;
    this.type = type;
  }

  public QName getType() {
    return type;
  }

  public int getStyle() {
    return partStyle;
  }

  /**
   * Loads properties. (used by WSDLHandler)
   */
  public void loadAttributes(SimpleAttr[] attr, int attrCount, NamespaceContainer uriContainer) throws Exception {
    super.loadAttributes(attr, attrCount);
    String element = SimpleAttr.getAttribute("element", attr, attrCount);
    String type = SimpleAttr.getAttribute("type", attr, attrCount);

    if ((element != null && type != null) || (element == null && type == null)) {
      throw new Exception(" Only one of \"type\" and \"element\" attributes must appear !");
    }

    if (element != null) {
      this.type = QName.qnameWSDLCreate(element, uriContainer, ((WSDLDefinitions) getDocument()).targetNamespace);
      partStyle = STRUCTURED_TYPE;
    }

    if (type != null) {
      this.type = QName.qnameWSDLCreate(type, uriContainer, ((WSDLDefinitions) getDocument()).targetNamespace);
      partStyle = SIMPLE_TYPE;
    }
  }

  /**
   * Loads properties from dom element.
   */
  public void loadAttributes(Element element) throws WSDLException {
    super.loadAttributes(element);
    Attr elementAttr = element.getAttributeNode("element");
    Attr typeAttr = element.getAttributeNode("type");


    if (elementAttr != null && typeAttr != null) {
      throw new WSDLException(" Both \"type\" and \"element\" attributes are not allowed in part '"+this.getName()+"'.");
    }
    if (elementAttr == null && typeAttr == null) {
      throw new WSDLException("\"type\" or \"element\" attribute must present in part '"+this.getName()+"'.");
    }
    if (elementAttr != null) {
      this.type = super.getQName(elementAttr.getValue(), element);
      partStyle = STRUCTURED_TYPE;
    }

    if (typeAttr != null) {
      this.type = super.getQName(typeAttr.getValue(), element);
      partStyle = SIMPLE_TYPE;
    }
  }
  
  public boolean equals(Object object) {
    if (object instanceof WSDLPart) {
      WSDLPart perm = (WSDLPart) object;

      if (partStyle != perm.partStyle) {
        return false;
      }

      return this.getName().equals(perm.getName()) && type.equals(perm.type);
    } else {
      return super.equals(object);
    }
  }

  public int hashCode() {
    return super.hashCode();
  }
  public WSDLDocumentation getDocumentation() {
    return null;
  }
  
}

