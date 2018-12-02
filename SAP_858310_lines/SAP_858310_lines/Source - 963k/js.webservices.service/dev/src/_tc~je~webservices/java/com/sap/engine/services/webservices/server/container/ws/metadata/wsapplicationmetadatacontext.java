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

package com.sap.engine.services.webservices.server.container.ws.metaData;

import java.util.Enumeration;
import java.util.Hashtable;

import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeDataRegistry;
import com.sap.engine.services.webservices.server.container.metadata.service.BindingDataMetaDataRegistry;

/**
 * Title: ApplicationMetaDataContext  
 * Description: ApplicationMetaDataContext is a context for application meta data
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class WSApplicationMetaDataContext {
  
  public static String METADATA = "metadata";
  public static String BACKUP = "backup"; 
  public static String VERSION = "version";
  public static String VERSION_71 = "7.1";
    
  private ModuleRuntimeDataRegistry moduleRuntimeDataRegistry;
  private ServiceMetaDataRegistry serviceMetaDataRegistry;
  private Hashtable<String, BindingDataMetaDataRegistry> bindingDataMetaDataRegistries; 
  
  public WSApplicationMetaDataContext() {
	  
  }  
    
  /**
   * @return ModuleRuntimeDataRegistry
   */
  public ModuleRuntimeDataRegistry getModuleRuntimeDataRegistry() {
    if(moduleRuntimeDataRegistry == null) {
      moduleRuntimeDataRegistry = new ModuleRuntimeDataRegistry();
    }
    return moduleRuntimeDataRegistry;
  }
  
  /**
   * @return ServiceMetaDataRegistry
   */
  public ServiceMetaDataRegistry getServiceMetaDataRegistry() {
    if(serviceMetaDataRegistry == null) {
      serviceMetaDataRegistry = new ServiceMetaDataRegistry();
    }
    return serviceMetaDataRegistry;
  }     
  
  /**
   * @return - a hashtable of binding data meta data registries
   */
  public Hashtable getBindingDataMetaDataRegistries() {
    if(bindingDataMetaDataRegistries == null) {
      bindingDataMetaDataRegistries = new Hashtable();
    }
    
    return bindingDataMetaDataRegistries;
  }  
  
  public String toString() {
    String resultStr = "";         
    String nl = System.getProperty("line.separator");    
          
    resultStr += "MODULE RUNTIME DATA REGISTRY: " + nl + moduleRuntimeDataRegistry.toString() + nl;
    resultStr += "SERVICE META DATA REGISTRY: " + nl + serviceMetaDataRegistry.toString();             
    resultStr += "BINDING DATA META DATA REGISTRIES: " + nl + bindingDataRegistriesToString(); 
    
    return resultStr;             
  }
  
  public String bindingDataRegistriesToString() {
    String resultStr = "";
    String nl = System.getProperty("line.separator");
  
    Hashtable bindingDataMetaDataRegistries = getBindingDataMetaDataRegistries(); 
    if(bindingDataMetaDataRegistries.size() == 0) {
      return "EMPTY";
    }
      
    Enumeration enum1 = bindingDataMetaDataRegistries.keys();
    int i = 0; 
    while(enum1.hasMoreElements()) {
      String serviceName = (String)enum1.nextElement(); 
      BindingDataMetaDataRegistry bindingDataMetaDataRegistry = (BindingDataMetaDataRegistry)bindingDataMetaDataRegistries.get(serviceName);
      resultStr += "Service[" + i + "]: " + serviceName + nl + bindingDataMetaDataRegistry.toString() + nl;  
    } 
  
    return resultStr;     
  }
 
}
