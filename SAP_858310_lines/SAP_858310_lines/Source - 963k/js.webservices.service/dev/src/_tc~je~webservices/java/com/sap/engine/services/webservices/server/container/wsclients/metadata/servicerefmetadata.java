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
 
package com.sap.engine.services.webservices.server.container.wsclients.metadata;

/**
 * Title: ServiceRefMetaData
 * Description: ServiceRefMetaData
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class ServiceRefMetaData {
  
  private String applicationName; 
  private String serviceRefGroupName; 
  private String serviceRefName; 
  
  public ServiceRefMetaData() {
  
  }
  
  public ServiceRefMetaData(String applicationName, String serviceRefGroupName, String serviceRefName) {
    this.applicationName = applicationName; 
    this.serviceRefGroupName = serviceRefGroupName;
    this.serviceRefName = serviceRefName;  
  }
  
  /**
   * @param applicationName  
   *   
   */
  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  /**
   * @param serviceRefGroupName
   */
  public void setServiceRefGroupName(String serviceRefGroupName) {
    this.serviceRefGroupName = serviceRefGroupName;
  }

  /**
   * @param serviceRefName
   */
  public void setServiceRefName(String serviceRefName) {
    this.serviceRefName = serviceRefName;
  }
    
  /**
   * @return - application name
   */
  public String getApplicationName() {
    return applicationName;
  }

  /**
   * @return - service ref group name
   */
  public String getServiceRefGroupName() {
    return serviceRefGroupName;
  }

  /**
   * @return - service ref name
   */
  public String getServiceRefName() {
    return serviceRefName;
  }  
 
}
