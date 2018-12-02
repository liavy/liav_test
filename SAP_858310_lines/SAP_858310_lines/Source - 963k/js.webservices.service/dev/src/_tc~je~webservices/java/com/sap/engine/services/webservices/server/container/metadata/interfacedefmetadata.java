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
 
package com.sap.engine.services.webservices.server.container.metadata;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Title: InterfaceDefMetaData 
 * Description: InterfaceDefMetaData
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class InterfaceDefMetaData {

  private String interfaceDefId; 
  private String applicationName; 
  private String moduleName; 
  private HashSet services; 
   
  public InterfaceDefMetaData() {   
  }
  
  public InterfaceDefMetaData(String applicationName, String moduleName, String interfaceDefId) {    
    this.applicationName = applicationName; 
    this.moduleName = moduleName;
    this.interfaceDefId = interfaceDefId;      
  }

  /**
   * @param interfaceDefId
   */
  public void setInterfaceDefId(String interfaceDefId) {
    this.interfaceDefId = interfaceDefId;
  }
  
  /**
   * @param applicationName
   */
  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  /**
   * @param moduleName
   */
  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }

  /**
   * @return interface definition id
   */
  public String getInterfaceDefId() {
    return interfaceDefId;
  }

  /**
   * @return application name 
   */
  public String getApplicationName() {
    return applicationName;
  }

  /**
   * @return module name
   */
  public String getModuleName() {
    return moduleName;
  }  
  
  public void addService(String service) {
    getServices().add(service);  
  }
  
  public void removeService(String service) {
    getServices().remove(service);  
  }
 
  /**
   * @return - a hash set of services
   */
  public HashSet getServices() {
    if(services == null) {
      services = new HashSet();  
    }  
    
    return services;
  }
  
}
