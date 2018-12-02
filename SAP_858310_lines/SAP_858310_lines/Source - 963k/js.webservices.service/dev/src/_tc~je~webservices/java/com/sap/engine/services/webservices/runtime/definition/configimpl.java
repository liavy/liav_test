package com.sap.engine.services.webservices.runtime.definition;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description: Base class for configuration content.
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */
public final class ConfigImpl implements com.sap.engine.interfaces.webservices.runtime.Config {

  private java.util.Map properties;//$JL-SER$

  public ConfigImpl() {
    this.properties = new java.util.HashMap();
  }

  public ConfigImpl(Map properties) {
    this.properties = properties;
  }

  public com.sap.engine.interfaces.webservices.runtime.PropertyDescriptor getProperty(String key) {
    if (key == null) {
      throw new IllegalArgumentException("Key could not be 'null'");
    }
    return (com.sap.engine.interfaces.webservices.runtime.PropertyDescriptor) this.properties.get(key);
  }

  public com.sap.engine.interfaces.webservices.runtime.PropertyDescriptor[] getProperties() {
    Collection col = this.properties.values();
    return (com.sap.engine.interfaces.webservices.runtime.PropertyDescriptor[]) col.toArray(new com.sap.engine.interfaces.webservices.runtime.PropertyDescriptor[col.size()]);
  }

  public void setProperty(String key, String value) {
    //if (key == null || value == null) {
      //throw new NullPointerException("Either key or value can be 'null'. Key = '" + key + "' value = '" + value + "'");
    //}

    this.properties.put(key, new PropertyDescriptorInterfaceImpl(key, value));
  }

  public void addProperty(com.sap.engine.interfaces.webservices.runtime.PropertyDescriptor property) {
    String name = property.getPropertyDescriptorName();

    if (name == null) {
      throw new IllegalArgumentException("Property name can be 'null'");
    }

    this.properties.put(name, property);
  }

  public void setProperties(Hashtable properties) {
    this.properties = properties;
  }

  public void addProperties(Hashtable newProperties) {
    if (properties == null) {
      properties = newProperties;
    }

    Enumeration enum1 = newProperties.keys();
    while(enum1.hasMoreElements()) {
      Object key =enum1.nextElement();
      properties.put(key, newProperties.get(key));
    }
  }

  public String toString() {
    return this.properties.toString();
  }
}