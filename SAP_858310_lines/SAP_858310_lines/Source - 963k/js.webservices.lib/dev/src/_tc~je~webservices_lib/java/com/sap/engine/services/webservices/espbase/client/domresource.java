/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.client;

import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.dom.DOMSource;

/**
 * Parsed to DOM representation of xml file loaded to the file system.
 * The resource also contains a list of all references from the xml file to external files.
 * @version 1.0 (2006-5-26)
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class DOMResource {
  
  private DOMSource content;
  private HashMap<String,String> resourceReferences;
  
  /**
   * Default constructor.
   *
   */
  public DOMResource() {
    resourceReferences = new HashMap();    
  }
  
  /**
   * Adds resource reference.
   * @param href
   * @param systemId
   */
  public void addResourceRef(String href, String systemId) {
    this.resourceReferences.put(href,systemId);
  }
  
  /**
   * Returns map containing all the resource references.
   * @return
   */
  public Map<String,String> getResourceRefs() {
    return this.resourceReferences;  
  }
  
  /**
   * Returns the system id for speficic resource reference.
   * @param href
   * @return
   */
  public String getResourceSystemId(String href) {
    return this.resourceReferences.get(href);    
  }
  
  /**
   * Clears all available resource references.
   *
   */
  public void clearResourceRefs() {
    this.resourceReferences.clear();
  }
  
  /**
   * Sets resource content.
   * @param content
   */
  public void setContent(DOMSource content) {
    this.content = content;    
  }
  
  /**
   * Returns resource content.
   * @return
   */
  public DOMSource getContent() {
    return this.content;
  }
    
}
