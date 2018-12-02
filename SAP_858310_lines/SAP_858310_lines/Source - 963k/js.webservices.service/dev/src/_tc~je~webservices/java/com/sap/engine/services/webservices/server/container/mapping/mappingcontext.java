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
 
package com.sap.engine.services.webservices.server.container.mapping;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Title: MappingContext   
 * Description: MappingContext is a context for interface and service mapping registries
 * 
 * @author Dimitrina Stoyanova
 * @version
 */
public class MappingContext {

  private InterfaceMappingRegistry interfaceMappingRegistry;
  private Hashtable<String, OperationMappingRegistry> interfaceOperationMappingRegistries; 
  private ServiceMappingRegistry serviceMappingRegistry; 
    
  public MappingContext() {
	  
  }  
    
  /**
   * @return InterfaceMappingRegistry 
   */
  public synchronized InterfaceMappingRegistry getInterfaceMappingRegistry() {
    if(interfaceMappingRegistry == null) {
      interfaceMappingRegistry = new InterfaceMappingRegistry();
    }
    return interfaceMappingRegistry;
  }
 
  /**
   * @return - a hashtable of interface operation mapping registries
   */  
  public synchronized Hashtable getInterfaceOperationMappingRegistries() {
    if(interfaceOperationMappingRegistries == null) {
      interfaceOperationMappingRegistries = new Hashtable();
    }
    return interfaceOperationMappingRegistries;
  }
  
  /**
   * @return ServiceMappingRegistry 
   */
  public synchronized ServiceMappingRegistry getServiceMappingRegistry() {
    if(serviceMappingRegistry == null) {
      serviceMappingRegistry = new ServiceMappingRegistry();
    }
    return serviceMappingRegistry;
  } 
  
  public String toString() {
    String resultStr = ""; 
    String nl = System.getProperty("line.separator");    
  
  
    resultStr += "INTERFACE MAPPING REGISTRY: " + nl + interfaceMappingRegistry.toString() + nl; 
    resultStr += "SERVICE MAPPING REGISTRY: " + nl + serviceMappingRegistry.toString() + nl; 
    resultStr += "OPERATION MAPPING REGISTRIES: " + nl + operationMappingRegistriesToString();              

    return resultStr;
  }   
  
  public String operationMappingRegistriesToString() {
    String resultStr = ""; 
    String nl = System.getProperty("line.separator");    
    
    Hashtable operationMappingRegistries = getInterfaceOperationMappingRegistries(); 
    if (operationMappingRegistries.size() == 0) {
      return "EMPTY";
    }
    
    Enumeration enum1 = operationMappingRegistries.keys();
    int i = 0;     
    while(enum1.hasMoreElements()) {
      String interfaceName = (String)enum1.nextElement();
      OperationMappingRegistry operationMappingRegistry = (OperationMappingRegistry)operationMappingRegistries.get(interfaceName);
      resultStr += "Interface[" + i++ + "]: " + interfaceName + nl + operationMappingRegistry.toString();      
    }            

    return resultStr;      
  }
    
}
