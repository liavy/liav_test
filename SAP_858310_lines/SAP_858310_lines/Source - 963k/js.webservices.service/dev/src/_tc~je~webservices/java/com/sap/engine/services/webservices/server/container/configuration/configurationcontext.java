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
 
package com.sap.engine.services.webservices.server.container.configuration;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Title: ConfigurationContext 
 * Description: ConfigurationContext is a context for interface and service configuration registries
 * 
 * @author Dimitrina Stoyanova
 * @version
 */
public class ConfigurationContext {
  
  private InterfaceDefinitionRegistry interfaceDefinitionRegistry;
  private BindingDataRegistry globalBindingDataRegistry; 
  private ServiceRegistry globalServiceRegistry;   
  private Hashtable<String, ApplicationConfigurationContext> applicationConfigurationContexts;

  public ConfigurationContext() {         
    	  
  }
  
  /**
   * @return InterfaceDefinitionRegistry
   */
  public synchronized InterfaceDefinitionRegistry getInterfaceDefinitionRegistry() {
    if(this.interfaceDefinitionRegistry == null) {
      this.interfaceDefinitionRegistry = new InterfaceDefinitionRegistry();
    }
    return this.interfaceDefinitionRegistry;
  }

  /**
   * @return BindingDataRegistry
   */  
  public synchronized BindingDataRegistry getGlobalBindingDataRegistry() {
    if(this.globalBindingDataRegistry == null) {
      this.globalBindingDataRegistry = new BindingDataRegistry();
    }
    return this.globalBindingDataRegistry;
  } 
              
  /**
   * @return ServiceRegistry
   */
  public synchronized ServiceRegistry getGlobalServiceRegistry() {
    if(this.globalServiceRegistry == null) {
      this.globalServiceRegistry = new ServiceRegistry();
    }
    return this.globalServiceRegistry;
  }
  
  /**
   * @return - a hashtable of application configuration contexts
   */
  public synchronized Hashtable getApplicationConfigurationContexts() {
    if(applicationConfigurationContexts == null) {
      applicationConfigurationContexts = new Hashtable();
    }
    return applicationConfigurationContexts;
  }
  
  public String toString() {
    String resultStr = "";         
    String nl = System.getProperty("line.separator");    
            
    resultStr += "INTERFACE DEFINITION REGISTRY: " + nl + interfaceDefinitionRegistry.toString() + nl;
    resultStr += "GLOBAL BINDING DATA REGISTRY: " + nl + globalBindingDataRegistry.toString() + nl; 
    resultStr += "GLOBAL SERVICE REGISTRY: " + nl + globalServiceRegistry.toString() + nl;
    resultStr += "APPLICATION CONFIGURATION CONTEXTS: " + nl + applicationConfigurationContextsToString() + nl;
     
    return resultStr;             
  }
  
  public String applicationConfigurationContextsToString() {
    String resultStr = "";
    String nl = System.getProperty("line.separator");
    
    Hashtable applicationConfigurationContexts = getApplicationConfigurationContexts(); 
    if(applicationConfigurationContexts.size() == 0) {
      return "EMPTY";
    }
        
    Enumeration enum1 = applicationConfigurationContexts.keys();
    int i = 0; 
    while(enum1.hasMoreElements()) {
      String applicationName = (String)enum1.nextElement(); 
      ApplicationConfigurationContext applicationConfigurationContext = (ApplicationConfigurationContext)applicationConfigurationContexts.get(applicationName);
      resultStr += "Application[" + i + "]: " + applicationName + nl + applicationConfigurationContext.toString() + nl;  
    } 
    
    return resultStr;     
  }
  
}
