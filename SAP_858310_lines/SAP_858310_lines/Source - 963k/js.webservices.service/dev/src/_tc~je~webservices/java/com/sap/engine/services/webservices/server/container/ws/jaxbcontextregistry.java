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

package com.sap.engine.services.webservices.server.container.ws;

/**
 * Title: JAXBContext 
 * Description: JAXBContext 
 * 
 * @author Dimitrina Stoyanova
 * @version
 */
import java.util.Hashtable;

import javax.xml.bind.JAXBContext;

public class JAXBContextRegistry {
	
  private Hashtable<String, JAXBContext> jaxbContexts; 
	  
  public JAXBContextRegistry() {
    this.jaxbContexts = new Hashtable<String, JAXBContext>();  
  }
	  
  /**
   * @return - a hashtable of JAXB contexts
   */
  public Hashtable<String, JAXBContext> getJAXBContexts() {
    if(jaxbContexts == null) {
      jaxbContexts = new Hashtable<String, JAXBContext>();
    }
    return jaxbContexts;
  } 
	  
  public boolean containsJAXBContextId(String id) {
    return getJAXBContexts().containsKey(id);
  }
	  
  public boolean containsJAXBContext(JAXBContext jaxbContext) {
    return getJAXBContexts().contains(jaxbContext);    
  }
  
  public JAXBContext putJAXBContext(String id, JAXBContext jaxbContext) {
    return (JAXBContext)getJAXBContexts().put(id, jaxbContext);
  }
	  
  public JAXBContext getJAXBContext(String id) {
    return (JAXBContext)getJAXBContexts().get(id);
  }
	  
  public JAXBContext removeJAXBContext(String id) {
    return (JAXBContext)getJAXBContexts().remove(id);
  }
  
  public static String generateKey(String serviceName, String intfMappingID) {
    return serviceName + "int-map-id:" + intfMappingID;
  }
}
