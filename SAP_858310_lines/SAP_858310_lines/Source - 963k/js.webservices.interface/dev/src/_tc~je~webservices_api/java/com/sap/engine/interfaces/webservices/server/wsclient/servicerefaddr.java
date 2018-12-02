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
 
package com.sap.engine.interfaces.webservices.server.wsclient;

import javax.naming.RefAddr;

/**
 * Title: ServiceRefAddr 
 * Description: ServiceRefAddr
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class ServiceRefAddr extends RefAddr {

  public static final String EJB_MODULE = "EJB"; 
  public static final String WEB_MODULE = "WEB";  
  public static final String JAVA_MODULE = "JAVA";  
     
  public static final String WEBSERVICE_REF_ADDR_TYPE = "webservice-ref-addr";     
    
  private String applicationName;  
  private String moduleType; 
  private String moduleName; 
  private String componentName;
  private String serviceRefName;   
  private Class serviceInterface; 
  private Class serviceEndpiontInterface; 

  public ServiceRefAddr(String applicationName, String componentName, String serviceRefName) {
    super(WEBSERVICE_REF_ADDR_TYPE);   
    this.applicationName = applicationName;   
    this.componentName = componentName;    
    this.serviceRefName = serviceRefName; 
  }
    
  public ServiceRefAddr(String applicationName, String moduleType, String moduleName, String componentName, String serviceRefName) {
    super(WEBSERVICE_REF_ADDR_TYPE);   
    this.applicationName = applicationName;
    this.moduleType = moduleType; 
    this.moduleName = moduleName; 
    this.componentName = componentName;    
    this.serviceRefName = serviceRefName; 
  }
  
  public ServiceRefAddr(String applicationName, String moduleType, String moduleName, String componentName, String serviceRefName, Class serviceInterface) {
    super(WEBSERVICE_REF_ADDR_TYPE);   
    this.applicationName = applicationName;
    this.moduleType = moduleType; 
    this.moduleName = moduleName; 
    this.componentName = componentName;    
    this.serviceRefName = serviceRefName; 
    this.serviceInterface = serviceInterface; 
  }
  
  public ServiceRefAddr(String applicationName, String moduleType, String moduleName, String componentName, String serviceRefName, Class serviceInterface, Class serviceEndpointtInterface) {
    super(WEBSERVICE_REF_ADDR_TYPE);   
    this.applicationName = applicationName;
    this.moduleType = moduleType; 
    this.moduleName = moduleName; 
    this.componentName = componentName;    
    this.serviceRefName = serviceRefName; 
    this.serviceInterface = serviceInterface;
    this.serviceEndpiontInterface = serviceEndpointtInterface; 
  }

  public Object getContent() {
    return ""; 
  }
     
  /**
   * @return application name
   */
  public String getApplicationName() {
    return applicationName;
  }
  
  /**
    * @return module type
    */
   public String getModuleType() {
     return moduleType;
   }

  /**
   * @return module name
   */
  public String getModuleName() {
    return moduleName;
  }
   
  /**
   * @return component unique name
   */
  public String getComponentName() {
    return componentName;
  }  
  
  /**
   * @return service reference name
   */
  public String getServiceRefName() {
    return serviceRefName;
  }  
  
  /**
   * @return service interface
   */
  public Class getServiceInterface() {
	return serviceInterface;
  }  
  
  /**
   * @return service endpoint interface
   */
  public Class getServiceEndpointInterface() {
	return serviceEndpiontInterface;
  }    
    
  public String getModifiedModuleName() {
    if(moduleName == null) {
      return moduleName; 
    }
    
    if(moduleName.lastIndexOf(".") == -1) {
      return moduleName + "_" + moduleType;
    } else {
      return moduleName.substring(0, moduleName.lastIndexOf(".")) + "_" + moduleType; 
    } 
  }

}
