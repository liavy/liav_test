/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;

/**
 * Base class for single jsp tags
 *
 * @author Galin Galchev,Ivo Simeonov
 * @version 4.0
 */
public abstract class JspTag extends Element {

  /*
   * tag element types
   */
  public static final int START_TAG = 0;
  public static final int END_TAG = 1;
  public static final int SINGLE_TAG = 2;
  /*
   * this tag type
   */
  public int tagType;
  /*
   * attributes of this tag
   */
  protected Attribute[] attributes;
  /**
   * Only for JSP documents that have declared the taglib not in jsp:root but in the tag itself.
   */
  private List<Attribute> nameSpaceAttributes;
  /*
   * body of this tag
   */
  protected Element body;

  public boolean hasJspAttribute = false;
  public boolean hasJspBody = false;

  /**
   * Reads all attributes of this tag as tokens
   *
   * @param   end  String containing all chas
   * that denotes end of this tag
   * @exception   JspParseException  thrown if error occures during
   * parsing
   */
  public void readAttributes(String end) throws JspParseException {
    char c;
    LinkedList l = new LinkedList();

    do {
      parser.skipWhiteSpace();
      c = parser.currentChar();

      if (end.indexOf(c) != -1) {
        break;
      } else {
        l.add(new Attribute().parse(parser));
      }
    } while (true);

    if (l.size() != 0) {
      attributes = new Attribute[l.size()];
      attributes = (Attribute[]) l.toArray(attributes);
    }
  }

  /**
   * Searches for attribute with the specified name
   *
   * @param   name  the name of the searched attributes
   * @return  Element that is attribute of this tag or null
   * if attribute with the specified name is not found
   */
  public Attribute getAttribute(String name) throws JspParseException {
    if (attributes == null) {
      return null;
    }

    Attribute ret = null;

    for (int i = 0; i < attributes.length; i++) {
      if (attributes[i].isName(name)) {
        ret = attributes[i];
        break;
      }
    } 

    return ret;
  }

  /**
   * Searches for attribute with the specified name
   *
   * @param   name  the name of the searched attributes
   * @return  Element that is attribute of this tag or null
   * if attribute with the specified name is not found
   */
  public Attribute getAttribute(char[] name) throws JspParseException {
    if (attributes == null) {
      return null;
    }

    Attribute ret = null;

    for (int i = 0; i < attributes.length; i++) {
      if (attributes[i].isName(name)) {
        ret = attributes[i];
        break;
      }
    } 

    return ret;
  }

  /**
   * Searches for attribute with the specified name and
   * returns its value
   *
   * @param   name  the name of the searched attributes
   * @return  String that is value of attribute of this tag
   * or null if attribute with the specified name is not found
   */
  public String getAttributeValue(String name) throws JspParseException {
    if (attributes == null) {
      return null;
    }

    Indentifier ret = null;

    for (int i = 0; i < attributes.length; i++) {
      if (attributes[i].isName(name)) {
        ret = attributes[i].value;
        break;
      }
    } 

    return (ret == null) ? null : ret.toString();
  }

  /**
   * Searches for attribute with the specified name and
   * returns its value
   *
   * @param   name  the name of the searched attributes
   * @return  String that is value of attribute of this tag
   * or null if attribute with the specified name is not found
   */
  public String getAttributeValue(char[] name) throws JspParseException {
    if (attributes == null) {
      return null;
    }

    Indentifier ret = null;

    for (int i = 0; i < attributes.length; i++) {
      if (attributes[i].isName(name)) {
        ret = attributes[i].value;
        break;
      }
    } 

    return (ret == null) ? null : ret.toString();
  }

  /**
   * Returns all attributes of this tag
   *
   * @return all attributes or null if there are no
   * attributes in this tag
   */
  public Attribute[] getAttributes() {
    return attributes;
  }

  /**
   * Sets all attributes of this tag
   *
   * @param  attributes array of Attributes
   */
  public void setAttributes(Attribute[] attributes) {
    this.attributes = attributes;
  }

  /**
   * Verifies the attributes of this tag
   *
   * @exception   JspParseException  thrown if error occures
   * during verification
   */
  public abstract void verifyAttributes() throws JspParseException;

  /**
   * Returns body of this tag
   *
   * @return Element
   */
  public Element getBody() {
    return body;
  }

  /**
   * Set body of this tag
   *
   * @param bodyEl Element
   */
  public void setBody(Element bodyEl) {
    body = bodyEl;
  }

  public String getString(IDCounter id) throws JspParseException {
    return null;
  }
  
  /**
   * Adds the specified attribute to the attributes collection of this tag.
   * @param attribute - the attribute to be added.
   */
  public void addAttribute(Attribute attribute){
    if (attributes == null) {
      attributes = new Attribute[]{attribute};
      return;
    }
    Attribute[] newAttributes = new Attribute[attributes.length+1];
    System.arraycopy(attributes, 0, newAttributes, 0, attributes.length);
    newAttributes[newAttributes.length-1] = attribute;
    attributes = newAttributes;
  }
  
  /**
   * When namespace of the custom tag is not specified in jsp:root but in the tag itself like this:
   * <c:forEach xmlns:c="http://java.sun.com/jsp/jstl/core" var="counter" begin="1" end="${3}">
   * @return List<Attribute>
   */
  public List<Attribute> getNameSpaceAttribute() {
    return nameSpaceAttributes;
  }
  
  /**
   * When namespace of the custom tag is not specified in jsp:root but in the tag itself like this:
   * <c:forEach xmlns:c="http://java.sun.com/jsp/jstl/core" var="counter" begin="1" end="${3}">
   * @param nameSpaceAttribute - null or the List with namespace attribute
   */
  public void setNameSpaceAttribute(List<Attribute> nameSpaceAttribute) {
    this.nameSpaceAttributes = nameSpaceAttribute;
  }
  
  /**
   * When namespace of the custom tag is not specified in jsp:root but in the tag itself like this:
   * <c:forEach xmlns:c="http://java.sun.com/jsp/jstl/core" var="counter" begin="1" end="${3}">
   * @param nameSpaceAttribute - null or the namespace attribute
   */
  public void addNameSpaceAttribute(Attribute nameSpaceAttribute) {
    if( nameSpaceAttribute == null ) {
      return;
    }
    if( nameSpaceAttributes == null ) {
      nameSpaceAttributes = new ArrayList<Attribute>(3);
    }
    nameSpaceAttributes.add(nameSpaceAttribute);
  }
  
  /**
   * Adds empty default namespace attribute if no default atribute is specified for this tag.
   * This method would be used in case of importing JSP documents and the root tag(s) should clear the default namespace.
   * 
   *    JSP.1.10.5 When including a JSP document (written in the XML syntax), in the resulting
   * XML View of the translation unit the root element of the included segment
   * must have the default namespace reset to "". This is so that any namespaces
   * associated with the empty prefix in the including document are not carried
   * over to the included document.
   */
  public void clearDefaultnamespace() {    
    if( nameSpaceAttributes == null ) {
      nameSpaceAttributes = new ArrayList<Attribute>(3);      
    }
    //search for other default namepace attribute
    boolean foundDefault = false;
    for (int i = 0; i < nameSpaceAttributes.size(); i++) {
      Attribute attribute = nameSpaceAttributes.get(i);
      if( attribute.name.value.equals("xmlns") ) {
        foundDefault = true;
        break;
      }
    }
    //if not found add the empty one
    if( !foundDefault ) {
      nameSpaceAttributes.add(createDefaultNamespaceAttr());
    }
  }
  /**
   * Utility method for creating default namespace attribute 
   * with name "xmlns" and empty string as a value.
   * @return  Attribute
   */
  private Attribute createDefaultNamespaceAttr() {
    Indentifier name = new Indentifier("xmlns");
    Indentifier value = new Indentifier("");
    Attribute defaultNamespace = new Attribute(name, value, false);    
    return defaultNamespace;
  }
}

