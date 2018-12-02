/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.wsdl;

import java.util.Hashtable;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;

/**
 * This is the base class of the WSDL API and all classes from the API extend it.
 * It provides a tree-like methods - append and remove of children, access to the parent and root nodes.
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-18
 */
public abstract class Base {
  //id constants
  public static final int NONE_ID = 0;    
  public static final int EXTENSION_CONTEXT_ID = 1;
  public static final int EXTENSION_ELEMENT_ID = 2;
  public static final int INTERFACE_ID = 4;
  public static final int OPERATION_ID = 8;
  public static final int PARAMETER_ID = 16;
  public static final int DEFINITIONS_ID = 32;
  public static final int SERVICE_ID = 64;
  public static final int ENDPOINT_ID = 128;
  public static final int SOAPBINDING_ID = 256;
  public static final int SOAPBINDING_OPERATION_ID = 512;
  public static final int XSD_TYPEREF_ID = 1024;
  public static final int XSD_TYPECONTAINER_ID = 2048;
  public static final int HTTPBINDING_ID = 4096;
  public static final int HTTPBINDING_OPERATION_ID = 8192;
  public static final int ATTACHMENTSCONTAINER_ID = 2 * 8192;
  public static final int MIMEPART_ID = 2 * 2 * 8192;
  
  //object name constants
  public static final String EXTENSION_CONTEXT_NAME  =  "extension-context";
  public static final String EXTENSION_ELEMENT_NAME  =  "extension-element";
  public static final String INTERFACE_NAME = "interface";
  public static final String OPERATION_NAME = "operation";
  public static final String PARAMETER_NAME = "parameter";
  public static final String DEFINITIONS_NAME = "definitions";
  public static final String SERVICE_NAME = "service";
  public static final String ENDPOINT_NAME = "endpoint";
  public static final String SOAPBINDING_NAME = "soap-binding";
  public static final String SOAPBINDING_OPERATION_NAME = "soap-binding-operation";
  public static final String XSD_TYPEREF_NAME = "xsd-typeref";
  public static final String XSD_TYPECONTAINER_NAME = "xsd-typecontainer";
  public static final String HTTPBINDING_NAME = "http-binding";
  public static final String HTTPBINDING_OPERATION_NAME = "http-binding-operation";
  public static final String ATTACHMENTSCONTAINER_NAME = "attachments-container";
  public static final String MIMEPART_NAME = "mimepart";
  public static final String SYSTEM_ID  = "system-id";
  
  //general properties
  //System property
  public static final String LINE_SEPARATOR  =  "line.separator";
  
  protected final int type;
  protected final String objectName;
  protected Base parent;
  protected Hashtable properties;
  
  private ObjectList children;
  
  /**
   * Creates Base object of certain type, with certain object name, 
   * and attaches it to a parent. If the parent is null, it is not attached.
   * 
   * @param type type of the object, e.g Base.INTERFACE_ID, ...
   * @param objectName object name, e.g Base.INTERFACE_NAME, ...
   * @param parent parent to which to attach the new node. If the parent is null, the node is not attached.
   * @exception WSDLException @see #appendChild(Base, int)
   */  
  protected Base(int type, String objectName, Base parent) throws WSDLException {
    this.type = type;
    this.objectName = objectName;
    this.children = new ObjectList();
    this.properties = new Hashtable();
    if (parent != null) {
      parent.appendChild(this);
    }
  }
  
  /**
   * Each Base objects maintains a set of key-value string pairs, know as properties.
   * This method returns the property value, given its key.
   * 
   * @return corresponding value of a property requested by the key param, or null if such property does not exist. 
   */
  public String getProperty(String key) {
    return (String) properties.get(key); 
  }

  /**
   * Each Base objects maintains a set of key-value string pairs, know as properties.
   * This method registers such property. If a property with the same key already exists, 
   * it will be replaced.
   * 
   * @param key property key
   * @param value property value
   * @return value of old property with same key. If there is no old property, null is returned.
   */  
  public String setProperty(String key, String value) {
    return (String) properties.put(key, value);
  }
  
  /**
   * Each Base object maintains a set of qname-value pairs, which represent attribute names and values.
   * These are the non-standard attributes that are applied to this node.
   * Via this method, such non-standard attribute data could be set.
   * 
   * @param qname attribute qualified name.
   * @param value attribute value. Cannot be null.
   * @return if attribute with same qname is already registered, it is replaced and its value is returned. Otherwise null is returned. 
   */
  public String setExtensionAttr(QName qname, String value) {
    return (String) properties.put(qname, value);
  }
  
  /**
   * 
   * @param qname attribute qualified name.
   * @return attribute value corresponding to the qname param, or null is such attribute does not exist.
   */
  public String getExtensionAttr(QName qname) {
    return (String) properties.get(qname);
  }
  
  /**
   * @return type of this Base object. The values that this method could return are available as constants in the Base class -
   *         Base.INTERFACE_ID, Base.OPERATION_ID,... 
   */
  public int getType() {
    return this.type;
  }
  
  /**
   * @return object-name of this Base object. The values that this method returns are available as constants in the Base class -
   *         Base.Interface_Name,Base.OPERATION_NAME,...
   */
  public String getObjectName() {
    return this.objectName;
  }
  
  /**
   * @return parent of this object, or null if this object is not attached to any parent.
   */
  public Base getParent() {
    return parent;
  }
  
  /**
   * Removes a child from this node.
   * 
   * @param child Base object to be removed from the child list of this object.
   * @return child which is removed (same as the child paremeter), or null if such child does not exist in the list of children of this node.  
   */
  public Base removeChild(Base child) {
    child.parent = null;
    return children.remove(child);  
  }
  
  /**
   * @return all the children of this node.
   */
  public ObjectList getChildren() {
    return this.children;  
  }
  
  /**
   * @return all the children, which have type equal to the <code>id</code> parameter.
   */
  public ObjectList getChildren(int id) {
    ObjectList res = new ObjectList();
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i).getType() == id) {
        res.add(children.item(i));      
      }
    }
    return res;
  }
  
  /**
   * Appends a child to the list of children of this node.
   * 
   * @param child Child to be append
   * @param mask this mask is calculated using OR operation over the ID values of Base objects, which are able to be children of this node.
   *        Example: If this can have as children only Interface and ExtensionElement objects, the mask is calculated as 
   *        Base.INTERFACE_ID | BASE.EXTENSION_ELEMENT
   * @throws WSDLException Exception with detailed message is thrown when <b>child</b> is null, or <b>mask</b> does not allow such child to be added,
   *         or <code>child</code> is already added as child to another node.  
   *         
   */    
  protected void appendChild(Base child, int mask) throws WSDLException {
    if (child == null || ((mask & child.getType()) == 0)) {
      String exParam = child != null ? child.getObjectName() : "null";
      throw new WSDLException(WSDLException.ENTITY_CANNOT_BE_APPEND, new Object[]{exParam, getObjectName()});  
    }
    if (child.parent != null) {
      throw new WSDLException(WSDLException.ENTITY_ALREADY_ATTACHED_TO_TREE, new Object[]{child, this});
    }
    
    child.parent = this;
    this.children.add(child);
  }
  
  /**
   * Usually implementation of this method, makes a check for the type of <code>child</code>, and whether
   * it is allowed to be attached as child.
   */ 
  public abstract void appendChild(Base child) throws WSDLException; 
  
  /**
   * @return Root node of the tree. If the node is not attached to any parent, the node itself is returned.
   */
  public Base getRoot() {
    if (parent == null) {
      return this;
    }
    return parent.getRoot();
  }
  
  public String toString() {
    StringBuffer res = new StringBuffer();
    this.toStringInternal(res, 0);
    return res.toString();   
  }
  
  private void toStringInternal(StringBuffer buffer, int level) {
    String pref = "";
    for (int i = 0; i < level; i++) {
      pref += "  ";
    }
    buffer.append(pref);
    buffer.append("objectName: " ).append(getObjectName()).append(", ");  
    buffer.append("properties: " ).append(properties.toString()).append(", ");  
    buffer.append("additions: ");
    toStringAdditionals(buffer);
    buffer.append(System.getProperty(LINE_SEPARATOR));
    
    ObjectList list = getChildren();
    for (int i = 0; i < list.getLength(); i++) {
      list.item(i).toStringInternal(buffer, level + 1);  
    }
  }
  
  /**
   * This abstract method is invoked in the implementation of #toString().
   * The implementations should append in the buffer any additioinal data
   * which the concrete Base instance contains.
   * The only data that is retrived by the #toString() in a generic way is the object name, 
   * and the properties of the base object. All other data should be added in 
   * this method.
   */
  protected abstract void toStringAdditionals(StringBuffer buffer);
    
}
