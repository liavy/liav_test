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
package com.sap.engine.services.webservices.wsdl;

import com.sap.engine.lib.xml.util.QName;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import java.util.ArrayList;

/**
 * SAP Feature WSDL Extension. This does not affect normal WSDL 1.1 processing.
 *
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class SAPFeature extends WSDLNamedNode {//$JL-EQUALS$
  
  public static final String SAP_FEATURES = "http://www.sap.com/webas/630/wsdl/features";

  protected String uri;
  protected ArrayList properties;  
  
  public SAPFeature() {
    super();
    properties = new ArrayList();
  }

  public SAPFeature(WSDLNode parent) {
    super(parent);
    properties = new ArrayList();
  }
  
  /**
   * Add's property to SAP Feature.
   */ 
  public void addProperty(SAPProperty property) {
    properties.add(property); 
  }
  
  /**
   * Returns SAP property by property name.
   * Returns null if property not found. 
   */ 
  public SAPProperty getProperty(QName qname) {
    for (int i=0; i<properties.size(); i++) {
      SAPProperty property = (SAPProperty) properties.get(i);
      if (property.getQname().equals(qname)) {
        return property;
      }
    }
    return null;
  }
      
  /**
   * Returns ArrayList of properties in this SAP Feature.
   */ 
  public ArrayList getProperties() {
    return this.properties;
  }
  
  /**
   * Removes property from SAP Feature.
   * Returns removed property or null if nothing removed.
   */ 
  public SAPProperty removeProperty(QName propertyQName) {
    for (int i=0; i<properties.size(); i++) {
      SAPProperty property = (SAPProperty) properties.get(i);
      if (property.getQname().equals(propertyQName)) {
        properties.remove(i);
        return property;
      }
    }
    return null;    
  }
  
  /**
   * Removes all properties from SAP Feature.
   */ 
  public void clearProperties() {
    properties.clear();
  }  
  
  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  /**
   * Overrides default equals.
   */ 
  public boolean equals(Object obj) { 
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SAPFeature)) {
      return false;
    }
    SAPFeature testedFeature = (SAPFeature) obj;
    if (!this.uri.equals(testedFeature.uri)) {
      return false;
    }
    if (this.properties.size() != testedFeature.properties.size()) {
      return false;
    }
    for (int i=0; i<properties.size(); i++) {
      SAPProperty property = (SAPProperty) properties.get(i);
      if (!property.equals(testedFeature.properties.get(i))) {
        return false;
      }
    }
    return true;
  }    
  
  public int hashCode() {
    return super.hashCode();
  }
  /**
   * Loads properties from dom element.
   */
  public void loadAttributes(Element element) throws WSDLException {
    // Loads Feature name.
    Attr attribute = element.getAttributeNode("name");    
    if (attribute == null) {
      throw new WSDLException(" No name attribute found !");
    } else {
      this.name = attribute.getValue();
    }
    
    // Loads Feature
    Attr uriAttr = element.getAttributeNode("uri");    
    if (uriAttr == null ) {
      throw new WSDLException(" SAP Feature must have 'uri' attribute !");
    }
    this.uri = uriAttr.getValue();
    
  }
  
}
