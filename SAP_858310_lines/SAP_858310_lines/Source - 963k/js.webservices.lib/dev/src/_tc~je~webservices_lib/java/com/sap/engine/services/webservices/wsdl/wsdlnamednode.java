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
import com.sap.engine.lib.xml.util.QName;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Class for all named WSDL elements.
 * @author Chavdar Baykov, chavdar.baikov@sap.com
 * @version 2.0
 */
public class WSDLNamedNode extends WSDLNode {

  private WSDLDocumentation documentation = null;
  /**
   * Name attribute of this node.
   */
  protected String name;
  protected String namespace;

  /**
   * Default constructor.
   */
  public WSDLNamedNode() {
    super();
  }

  /**
   * Constructor with parent.
   */
  public WSDLNamedNode(WSDLNode parent) {
    super(parent);
    WSDLNode defintions = getDocument();
    if (defintions != null && defintions instanceof WSDLDefinitions) {
      this.namespace = ((WSDLDefinitions) defintions).getTargetNamespace();
    }
  }

  /**
   * Constructor with parent and name attribute. If added to tree namespace is set from Document targetNamespace.
   * @param parent
   * @param name
   */
  public WSDLNamedNode(WSDLNode parent, String name) {
    super(parent);
    WSDLNode defintions = getDocument();
    if (defintions != null && defintions instanceof WSDLDefinitions) {
      this.namespace = ((WSDLDefinitions) defintions).getTargetNamespace();
    }
    this.name = name;
  }

  /**
   * Sets name attribute for this node.
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  public QName getQName() {
    return new QName(name,namespace);
  }
  /**
   * Sets namespace attribute of this node.
   * @param namespace
   */
  public void setNamespace(String namespace) {
    this.namespace = name;
  }

  /**
   * Returns name attribute for this node.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns this node's namespace.
   * @return
   */
  public String getNamespace() {
    return namespace;
  }
  /**
   * Loads name property form List of attributes. (Used from WSDL Handler will be deprecated)
   */
  public void loadAttributes(SimpleAttr[] attr, int attrCount) throws WSDLException {
    String name = loadAttributesNoException(attr, attrCount);

    if (name == null) {
      throw new WSDLException("No \"name\" attribute");
    }
  }

  protected String loadAttributesNoException(SimpleAttr[] attr, int attrCount) {
    this.name = SimpleAttr.getAttribute("name", attr, attrCount);
    return name;
  }

  /**
   * Loads attributes from dom element.
   */
  public void loadAttributes(Element element) throws WSDLException {
    Attr attribute = element.getAttributeNode("name");

    if (attribute == null) {
      throw new WSDLException(" No name attribute found !");
    } else {
      this.name = attribute.getValue();
    }
  }

  public WSDLDocumentation getDocumentation() {
    return documentation;
  }

  public void setDocumentation(WSDLDocumentation documentation) {
    this.documentation = documentation;
  }
  
}

