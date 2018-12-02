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
 
package com.sap.engine.services.webservices.server.container.metadata.service;

/**
 * Title: 
 * Description:
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class BindingDataMetaData {

  private String bindingDataName; 
  private String applicationName;
  private String serviceName;    
     
  public BindingDataMetaData() {    
  }  
  
  public BindingDataMetaData(String applicationName, String serviceName, String bindingDataName) {
    this.applicationName = applicationName;        
    this.serviceName = serviceName;
    this.bindingDataName = bindingDataName; 
  }  
  
  /**
   * @return - binding data name
   */
  public String getBindingDataName() {
    return bindingDataName;
  }

  /**
   * @param bindingDataName
   */
  public void setBindingDataName(String bindingDataName) {
    this.bindingDataName = bindingDataName;
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
  
  public String toString() {
    String resultStr = "";
    String nl = System.getProperty("line.separator"); 
      
    resultStr += "Application name : " + applicationName + nl;    
    resultStr += "Service name     : " + serviceName + nl;
    resultStr += "Binding data name: " + bindingDataName + nl;
    
    return resultStr;       
  }
      
}
