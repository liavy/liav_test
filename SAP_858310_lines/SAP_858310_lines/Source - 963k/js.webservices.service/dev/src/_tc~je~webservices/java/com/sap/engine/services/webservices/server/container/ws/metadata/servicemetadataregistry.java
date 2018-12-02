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

/**
 * Title: ServiceMetaDataRegistry
 * Description: ServiceMetaDataRegistry is a registry for service meta data
 * 
 * @author Dimitrina Stoyanova
 * @version
 */
public class ServiceMetaDataRegistry {
  
  private Hashtable<String, ServiceMetaData> serviceMetaDatas; 
  
  public ServiceMetaDataRegistry() {
    this.serviceMetaDatas = new Hashtable<String, ServiceMetaData>(); 
  }  

  /**
   * @return - a hashtable of service meta data
   */
  public Hashtable getServiceMetaDatas() {
    if(serviceMetaDatas == null) {
      serviceMetaDatas = new Hashtable();
    }
    return serviceMetaDatas;
  }
  
  public boolean containsServiceMetaDataID(String id) {    
    return getServiceMetaDatas().containsKey(id);    
  }
  
  public boolean containsServiceMetaData(ServiceMetaData serviceMetaData) {
    return getServiceMetaDatas().contains(serviceMetaData);    
  }
  
  public void putServiceMetaData(String id, ServiceMetaData serviceMetaData) {
    getServiceMetaDatas().put(id, serviceMetaData);    
  }
  
  public ServiceMetaData getServiceMetaData(String id) {
    return (ServiceMetaData)getServiceMetaDatas().get(id);    
  }
  
  public ServiceMetaData removeServiceMetaData(String id) {
    return (ServiceMetaData)getServiceMetaDatas().remove(id);   
  }
  
  public String toString() {
    String resultStr = ""; 
    String nl = System.getProperty("line.separator");    

    Hashtable serviceMetaDatas = getServiceMetaDatas();

    if(serviceMetaDatas.size() == 0) {
      return "EMPTY";
    }

    Enumeration enum1 = serviceMetaDatas.keys();    
    while(enum1.hasMoreElements()) {
      String serviceName = (String)enum1.nextElement();
      ServiceMetaData serviceMetaData = (ServiceMetaData)serviceMetaDatas.get(serviceName);
      resultStr += serviceMetaData.toString() + nl;      
    }            

    return resultStr;
  }
   
}
