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

import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;

/**
 * Title: ServiceMappingRegistry  
 * Description: ServiceMappingRegistry is a registry for service mappings
 * 
 * @author Dimitrina Stoyanova
 * @version
 */
public class ServiceMappingRegistry {
  
  private Hashtable<String, ServiceMapping> serviceMappings; 
  
  public ServiceMappingRegistry() {
    this.serviceMappings = new Hashtable<String, ServiceMapping>(); 
  }  
  
  /**
   * @return - a hashtable of service mappings 
   */
  public Hashtable getServiceMappings() {
    if(serviceMappings == null) {
      serviceMappings = new Hashtable();
    }
    return serviceMappings;
  }
  
  public boolean containsServiceMappingID(String id) {
    return getServiceMappings().containsKey(id);
  }
  
  public boolean containsServiceMapping(ServiceMapping serviceMapping) {
    return getServiceMappings().contains(serviceMapping);
  }

  public void putServiceMapping(String id, ServiceMapping serviceMapping) {
    getServiceMappings().put(id, serviceMapping);
  }
  
  public ServiceMapping getServiceMapping(String id) {
    return (ServiceMapping)getServiceMappings().get(id); 
  }
  
  public ServiceMapping removeServiceMapping(String id) {
    return (ServiceMapping)getServiceMappings().remove(id); 
  }
  
  public String toString() {
    String resultStr = ""; 
    String nl = System.getProperty("line.separator");    

    Hashtable serviceMappings = getServiceMappings();

    if(serviceMappings.size() == 0) {
      return "EMPTY";
    }

    Enumeration enum1 = serviceMappings.keys();
    int i = 0;     
    while(enum1.hasMoreElements()) {           
      resultStr += "Service[" + i++ + "]: " +enum1.nextElement() + nl;      
    }            

    return resultStr;
  }

}
