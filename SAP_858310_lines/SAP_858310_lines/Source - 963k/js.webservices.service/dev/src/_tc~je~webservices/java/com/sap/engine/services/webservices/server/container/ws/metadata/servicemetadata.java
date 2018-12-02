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

/**
 * Title: ServiceMetaData
 * Description: ServiceMetaData 
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class ServiceMetaData {
   
  private static String TYPES_DIR_NAME = "types";
  
  private static final String DEFAULT_USE = "default";
  private static final String LITERAL_USE = "literal";
  private static final String ENCODED_USE = "encoded";  
  private static final String DEFAULT_USE_SUFFIX = ""; 
  private static final String ENCODED_USE_SUFFIX = "_enc"; 
    
  private String serviceName;    
  private String applicationName;
  private String moduleName;    
  
  public ServiceMetaData() {    
  }  
  
  public ServiceMetaData(String applicationName, String moduleName, String serviceName) {
    this.applicationName = applicationName; 
    this.moduleName = moduleName;    
    this.serviceName = serviceName;
  }  
    
  /**
   * @return - service name
   */
  public String getServiceName() {
    return serviceName;
  }

  /**
   * @param serviceName
   */
  public void setServiceName(String serviceName) {
    this.serviceName = serviceName ;
  }      
    
  /**
   * @param applicationName
   */
  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }
    
  /**
   * @return - application name
   */
  public String getApplicationName() {
    return applicationName;
  }

  /**
   * @param moduleName
   */
  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }

  /**
   * @return - module name
   */
  public String getModuleName() {
    return moduleName;
  }  

  public String getTypesArchiveFileRelPath(String use) {
    String typesArchiveFileRelPath  = "";           
    
    if(use.equals(DEFAULT_USE) || use.equals(LITERAL_USE)) {        
      typesArchiveFileRelPath = TYPES_DIR_NAME + "/" + serviceName + DEFAULT_USE_SUFFIX + ".jar";
    } 
    if(use.equals(ENCODED_USE)) {    
      typesArchiveFileRelPath = TYPES_DIR_NAME + "/" + serviceName + ENCODED_USE_SUFFIX + ".jar";
    }  

    return typesArchiveFileRelPath; 
  }
  
//  public String getTypeMappingRelPath(String use) {              
//    return TYPES_DIR_NAME + "/" + getTypesPackage(use) + "/types.xml";     
//  }
  
  public String getFrmTypeMappingRelPath(String use) {              
    return getFrmTypesPackage(use).replace('.', '/') + "/types.xml";     
  }
   
//  public String getFrmTypeMappingRelPath(String use) {              
//    return TYPES_DIR_NAME + "/" + getFrmTypesPackage(use).replace('.', '/') + "/types.xml";     
//  }
//  

//  public String getTypesPackage(String use) {
//    String typesPackage  = "";  
//    
//    if(use.equals(DEFAULT_USE_SUFFIX) || use.equals(LITERAL_USE)) {    
//      typesPackage = serviceName + DEFAULT_USE_SUFFIX;
//    } 
//    if(use.equals(ENCODED_USE)) {    
//      typesPackage = serviceName + ENCODED_USE_SUFFIX;
//    }  
//    
//    return typesPackage; 
//  }
  
  public String getFrmTypesPackage(String use) {
    String typesPackage  = "";  
  
    if(use.equals(DEFAULT_USE) || use.equals(LITERAL_USE)) {    
      typesPackage = serviceName + DEFAULT_USE_SUFFIX + ".frm";
    } 
    if(use.equals(ENCODED_USE)) {    
      typesPackage = serviceName + ENCODED_USE_SUFFIX + ".frm";
    }  
  
    return typesPackage; 
  }
  
  public String getServiceTempDirName() {
    return serviceName; 
//    String serviceTempDirName  = "";  
//    
//    if(use.equals(DEFAULT_USE) || use.equals(LITERAL_USE)) {    
//      serviceTempDirName = serviceName + DEFAULT_USE_SUFFIX;
//    } 
//    if(use.equals(ENCODED_USE)) {    
//      serviceTempDirName = serviceName + ENCODED_USE_SUFFIX;
//    }  
//
//    return serviceTempDirName; 
  }
  
  public String toString() {
    String resultStr = "";
    String nl = System.getProperty("line.separator"); 
        
    resultStr += "Application name: " + applicationName + nl;
    resultStr += "Module name     : " + moduleName + nl;
    resultStr += "Service name    : " + serviceName + nl;
    
    return resultStr;       
  } 
  
}
