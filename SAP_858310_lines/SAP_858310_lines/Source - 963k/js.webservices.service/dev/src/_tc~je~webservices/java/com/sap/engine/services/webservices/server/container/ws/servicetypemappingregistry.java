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

import javax.xml.rpc.encoding.TypeMappingRegistry;

/**
 * Title: ServiceTypeMappingRegistry 
 * Description: ServiceTypeMappingRegistry is a registry for type mapping registries 
 * 
 * @author Dimitrina Stoyanova
 * @version
 */
public class ServiceTypeMappingRegistry {

  private Hashtable<String, TypeMappingRegistry> typeMappingRegistries; 
  
  public ServiceTypeMappingRegistry() {
    this.typeMappingRegistries = new Hashtable<String, TypeMappingRegistry>(); 
  }
  
  /**
   * @return - a hashtable of type mapping registries
   */
  public Hashtable getTypeMappingRegistries() {
    if(typeMappingRegistries == null) {
      typeMappingRegistries = new Hashtable();
    }
    return typeMappingRegistries;
  } 
  
  public boolean containsTypeMappingRegistryID(String id) {
    return getTypeMappingRegistries().containsKey(id);
  }
  
  public TypeMappingRegistry putTypeMappingRegistry(String id, TypeMappingRegistry typeMappingRegistry) {
    return (TypeMappingRegistry)getTypeMappingRegistries().put(id, typeMappingRegistry);
  }
  
  public TypeMappingRegistry getTypeMappingRegistry(String id) {
    return (TypeMappingRegistry)getTypeMappingRegistries().get(id);
  }
  
  public TypeMappingRegistry removeTypeMappingRegistry(String id) {
    return (TypeMappingRegistry)getTypeMappingRegistries().remove(id);
  }
    
  public String toString() {
    String resultStr = ""; 
    String nl = System.getProperty("line.separator");    
    
    Hashtable typeMappingRegistries = getTypeMappingRegistries();
    
    if(typeMappingRegistries.size() == 0) {
      return "EMPTY";
    }
    
    Enumeration enumer = typeMappingRegistries.keys();
    int i = 0; 
    while(enumer.hasMoreElements()) {
      resultStr += "Service[" + i++ + "]: " + enumer.nextElement() + nl;   
    }            
    
    return resultStr;
  }
 
}
