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
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.ArrayList;

/**
 * Class representing all WSDl extension element that are not from WSDL document structure.
 * @author Chavdar Baykov, chavdar.baikov@sap.com
 * @version 2.0
 */
public class WSDLExtension extends WSDLNode {

  /**
   * Local Name and uri of Extension element.
   */
  private String localName;
  private String uri;
  private ArrayList attributes = null; // extension attributes
  private ArrayList children = null; // extension children

  /**
   * Default constructor.
   */
  public WSDLExtension() {
    super();
    attributes = new ArrayList();
    children = new ArrayList();
  }

  /**
   * Constructor with parent.
   */
  public WSDLExtension(WSDLNode parent) {
    super(parent);
    attributes = new ArrayList();
    children = new ArrayList();
  }

  /**
   * Sets local name of extension.
   */
  public void setLocalName(String name) {
    this.localName = name;
  }

  /**
   * Returns local name of extension element.
   */
  public String getLocalName() {
    return localName;
  }

  /**
   * Sets uri of extension element.
   */
  public void setURI(String uri) {
    this.uri = uri;
  }

  /**
   * Returns uri of extension element.
   */
  public String getURI() {
    return uri;
  }

  /**
   * Adds attribute to extension element.
   */
  public void addAttribute(Object attrib) {
    this.attributes.add(attrib);
  }

  /**
   * Returns attributes of Extension.
   */
  public ArrayList getAttributes() {
    return attributes;
  }

  public SimpleAttr removeAttribute(SimpleAttr obj) {
    SimpleAttr temp;

    for (int i = 0; i < attributes.size(); i++) {
      temp = (SimpleAttr) attributes.get(i);

      if (temp.localName.equals(obj.localName)) {
        return (SimpleAttr) attributes.remove(i);
      }
    } 

    return null;
  }

  public void setAttribute(SimpleAttr obj) {
    this.removeAttribute(obj);
    this.addAttribute(obj);
  }

  /**
   * Returns attribute value ny attribute name
   */
  public String getAttribute(String name) {
    SimpleAttr attr;

    for (int i = 0; i < attributes.size(); i++) {
      attr = (SimpleAttr) attributes.get(i);

      if (attr.localName.equals(name)) {
        return attr.value;
      }
    } 

    return null;
  }

  /**
   * Adds extension child.
   */
  public void addChild(WSDLNode child) {
    children.add(child);
  }

  /**
   * Returns extension children.
   */
  public ArrayList getChildren() {
    return children;
  }

  /**
   * Returns specific child.
   */
  public WSDLExtension getChild(int index) {
    return (WSDLExtension) children.get(index);
  }

  /**
   * Returns child count.
   */
  public int getChildCount() {
    return children.size();
  }

  /**
   * Used by WSDL Handler to load Attributes.
   */
  public void loadAttributes(SimpleAttr[] attr, int attrCount) throws WSDLException {
    for (int i = 0; i < attrCount; i++) {
      SimpleAttr perm = new SimpleAttr();
      perm.localName = attr[i].localName;
      perm.uri = attr[i].uri;
      perm.value = attr[i].value;
      attributes.add(perm);
    } 
  }

  /**
   * Loads attributes from dom Element.
   */
  public void loadAttributes(Element element) {
    NamedNodeMap elementAttr = element.getAttributes();

    for (int i = 0; i < elementAttr.getLength(); i++) {
      Node node = elementAttr.item(i);

      if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
        SimpleAttr perm = new SimpleAttr();
        Attr attribute = (Attr) node;
        perm.localName = attribute.getLocalName();
        perm.value = attribute.getValue();
        perm.uri = attribute.getNamespaceURI();
        this.attributes.add(perm);
      }
    } 
  }

  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append("localName: {" + this.localName + "} uri: {" + this.uri + "} Attributes: {" + this.attributes + "}\n").append("*****Childs*******\n");

    for (int i = 0; i < children.size(); i++) {
      result.append(children.get(i).toString() + "\n");
    } 

    result.append("****end Childs****\n");
    return result.toString();
  }

  public void setAttribute(String localName, String value, String uri) {
    this.setAttribute(new SimpleAttr(localName, value, uri));
  }
}

