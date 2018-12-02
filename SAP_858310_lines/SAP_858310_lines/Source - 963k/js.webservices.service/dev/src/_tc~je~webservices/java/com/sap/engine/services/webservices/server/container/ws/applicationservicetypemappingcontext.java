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

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Title: ServiceTypeMappingContext
 * Description: ServiceTypeMappingContext is a context for service type mapping registries.
 * 
 * @author Dimitrina Stoyanova
 * @version
 */
public class ApplicationServiceTypeMappingContext {
 
  public static final String LITERAL_USE = "literal";
  public static final String ENCODED_USE = "encoded";   
  public static final String DEFAULT_USE = "default";
 
  private Hashtable<String, ServiceTypeMappingRegistry> serviceTypeMappingRegistries; 
  private JAXBContextRegistry jaxbContextRegistry; 
  
  public ApplicationServiceTypeMappingContext() {    
  
  }
  
  /**
   * @return - a hashtable of service type mapping registries
   */
  public Hashtable getServiceTypeMappingRegistries() {
    if(serviceTypeMappingRegistries == null) {
      serviceTypeMappingRegistries = new Hashtable(); 
    } 
    
    return serviceTypeMappingRegistries; 
  }
  
  public boolean containsServiceTypeMappingRegistryKey(String key) {
    return getServiceTypeMappingRegistries().containsKey(key);
  }
  
  public ServiceTypeMappingRegistry putServiceTypeMappingRegistry(String key, ServiceTypeMappingRegistry serviceTypeMappingRegistry) {
    if(isValidServiceTypeMappingRegistryKey(key)) {
      return (ServiceTypeMappingRegistry)getServiceTypeMappingRegistries().put(key, serviceTypeMappingRegistry);
    }    
    
    return null; 
  } 
  
  public ServiceTypeMappingRegistry getServiceTypeMappingRegistry(String key) {
    return (ServiceTypeMappingRegistry)getServiceTypeMappingRegistries().get(key);   
  }
  
  public ServiceTypeMappingRegistry removeServiceTypeMappingRegistry(String key) {
    return (ServiceTypeMappingRegistry)getServiceTypeMappingRegistries().remove(key);   
  }
    
  public JAXBContextRegistry getJaxbContextRegistry() {
    if(jaxbContextRegistry == null) {
      jaxbContextRegistry = new JAXBContextRegistry(); 	
    }    
    
	return jaxbContextRegistry;
  }

  public boolean isValidServiceTypeMappingRegistryKey(String key) {
    if(key.equals(LITERAL_USE)) {
      return true; 
    }    
    if(key.equals(ENCODED_USE)) {
      return true; 
    }          
    if(key.equals(DEFAULT_USE)) {
      return true; 
    }
    return false;     
  }  

  public String toString() {
    String resultStr = ""; 
    String nl = System.getProperty("line.separator");    
    
    Hashtable serviceTypeMappingRegistries = getServiceTypeMappingRegistries();
    
    if(serviceTypeMappingRegistries.size() == 0) {
      return "EMPTY";
    }
    
    Enumeration enum1 = serviceTypeMappingRegistries.keys();
    int i = 0; 
    while(enum1.hasMoreElements()) {
      String use = (String)enum1.nextElement();
      ServiceTypeMappingRegistry serviceTypeMappingRegistry = (ServiceTypeMappingRegistry)serviceTypeMappingRegistries.get(use);
      resultStr += "SERVICE TYPE MAPPING REGISTRY[" + use + "]: " + nl + serviceTypeMappingRegistry.toString();
    }            
    
    return resultStr;
  }

}
