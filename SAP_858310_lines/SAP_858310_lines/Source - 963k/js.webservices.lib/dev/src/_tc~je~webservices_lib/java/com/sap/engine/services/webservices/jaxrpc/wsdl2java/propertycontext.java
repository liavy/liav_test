/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java;

import com.sap.engine.interfaces.webservices.server.deploy.wsclient.LPPropertyContext;

import java.util.Enumeration;
import java.util.Hashtable;


/**
 * Common configuration class for cliene side components.<br>
 * This is a hash wich can contain values ot other subhashes.
 * Use <i>.toString()</i> method to see it's contents.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class PropertyContext implements LPPropertyContext {

  private Hashtable properties;
  private Hashtable subContexts;
  private boolean isDefined;

  /**
   * Default constructor.
   */
  public PropertyContext() {
    properties = new Hashtable();
    subContexts = new Hashtable();
    isDefined = false;
  }

  /**
   * Returns property content or null if not set.
   * @param key Property key.
   * @return
   */
  public Object getProperty(String key) {
    return properties.get(key);
  }

  /**
   * Sets sub context in current PropertyContext. This will override if old one has been set.
   * @param key
   * @param context
   */
  public void setSubContext(String key, PropertyContext context) {
    if (key != null && context != null) {
      subContexts.put(key, context);
    }
    this.isDefined = true;
  }

  /**
   * Sets PropertyContext property with some value.
   * @param key
   * @param content
   */
  public void setProperty(String key, Object content) {
    if (key != null && content != null) {
      properties.put(key, content);
      this.isDefined = true;
    }
  }

  /**
   * Clears all properties and subcontexts.
   */
  public void clear() {
    properties.clear();
    subContexts.clear();
    this.isDefined = false;
  }

  /**
   * Returns property context by given key. If property context is not set then new one is created.
   * The method .isDefined() may be used on the result to check if it is an wmpty one.
   * @param key
   * @return
   */
  public PropertyContext getSubContext(String key) {
    PropertyContext result = (PropertyContext) subContexts.get(key);
    if (result == null) {
      result = new PropertyContext();
      subContexts.put(key, result);
    }
    this.isDefined = true;
    return result;
  }

  /**
   * Returns enumeration of all property keys.
   * @return
   */
  public Enumeration getProperyKeys() {
    return properties.keys();
  }

  /**
   * Returns all set subcontext keys.
   * @return
   */
  public Enumeration getSubcontextKeys() {
    return subContexts.keys();
  }

  /**
   * Marks this property context as non empty despite of no property being set.
   */
  public void define() {
    this.isDefined = true;
  }

  /**
   * Checks if this PropertyContext is empty.
   * @return
   */
  public boolean isDefined() {
    return this.isDefined;
  }

  /**
   * Merges property context with the subcontext with passed key.
   * @param key
   * @param context
   */
  public void joinProperyContext(String key, PropertyContext context) {
    PropertyContext perm = getSubContext(key);
    if (perm.isDefined()) {
      Enumeration keys = context.getProperyKeys();
      while (keys.hasMoreElements()) {
        String pkey = (String) keys.nextElement();
        Object value = context.getProperty(pkey);
        perm.setProperty(pkey,value);
      }
      keys = context.getSubcontextKeys();
      while (keys.hasMoreElements()) {
        String pkey = (String) keys.nextElement();
        PropertyContext value = context.getSubContext(pkey);
        perm.joinProperyContext(pkey,value);
      }
    } else {
      setSubContext(key,context);
    }
  }

  /**
   * Returns clone of this property context.
   * @return
   */
  public PropertyContext getClone() {
    PropertyContext result = new PropertyContext();
    result.loadFrom(this);
    return result;
  }

  /**
   * Removes some subcontext.
   * @param key
   */
  public void clearSubcontext(String key) {
    subContexts.remove(key);
  }

  /**
   * Removes given property contents.
   * @param key
   */
  public void clearProperty(String key) {
    properties.remove(key);
  }

  /**
   * Totally loads contents from given property contents. Only oblects are not cloned.
   * @param inputContext
   */
  private void loadFrom(PropertyContext inputContext) {
    this.clear();
    Enumeration keys = inputContext.getSubcontextKeys();
    while (keys.hasMoreElements()) {
      String key = (String)  keys.nextElement();
      PropertyContext context = inputContext.getSubContext(key);
      PropertyContext newContext = getSubContext(key);
      this.setSubContext(key,newContext);
      newContext.loadFrom(context);
    }
    keys = inputContext.getProperyKeys();
    while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      Object content = inputContext.getProperty(key);
      this.setProperty(key,content);
    }
  }

  /**
   * Internal toString implementation.
   * @param level
   * @return
   */
  private String toString(int level) {
    StringBuffer result = new StringBuffer();
    StringBuffer indent = new StringBuffer();
    for (int i=0; i<level; i++) {
      indent.append(' ');
    }
    String indentS = indent.toString();
    if (this.isDefined == false) {
      return indent+"<empty>\n";
    }
    Enumeration propertyKeys = properties.keys();
    while (propertyKeys.hasMoreElements()) {
      String key = (String) propertyKeys.nextElement();
      Object value = properties.get(key);
      result.append(indentS);
      result.append(key);
      result.append(" = ");
      result.append('\'');
      result.append(value.toString());
      result.append('\'');
      result.append("\n");
    }
    Enumeration subContextKeys = subContexts.keys();
    while (subContextKeys.hasMoreElements()) {
      String key = (String) subContextKeys.nextElement();
      PropertyContext value = (PropertyContext) subContexts.get(key);
      result.append(indentS);
      result.append(key);
      result.append(" = ");
      result.append("{\n");
      result.append(value.toString(level+1));
      result.append(indentS);
      result.append("}\n");
    }
    return result.toString();
  }

  public String toString() {
    return toString(0);
  }
/*
  public static void main(String[] args) {
    PropertyContext pc = new PropertyContext();
    pc.setProperty("prop1","value1");
    pc.setProperty("prop2","value2");
    pc.setProperty("prop3","value3");
    PropertyContext pc1 = pc.getSubContext("prop1");
    pc1.setProperty("prop1","value1");
    pc1.setProperty("prop2","value2");
    pc1.setProperty("prop3","value3");
    PropertyContext pc2 = pc.getSubContext("prop2");
    pc2.setProperty("prop1","value1");
    pc2.setProperty("prop2","value2");
    pc2.setProperty("prop3","value3");
    PropertyContext pc3 = pc1.getSubContext("prop1");
    pc3.setProperty("prop1","value1");
    pc3.setProperty("prop2","value2");
    pc3.setProperty("prop3","value3");
    System.out.println(pc.toString());
  }*/
}
