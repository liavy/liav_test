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
 
package com.sap.engine.services.webservices.server.container.ws.descriptors;

import java.util.Enumeration;
import java.util.Hashtable;

import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType;

/**
 * Title: ServiceDescriptorRegistry
 * Description: ServiceDescriptorRegistry is a registry for service descriptors
 * 
 * @author Dimitrina Stoyanova
 * @version
 */
public class ServiceDescriptorRegistry {
  
  private Hashtable<String, WebserviceDescriptionType> serviceDescriptors;
  
  public ServiceDescriptorRegistry() {    
    this.serviceDescriptors = new Hashtable<String, WebserviceDescriptionType>(); 
  }
    
  /**
   * @return - a hashtable of web service descriptors
   */
  public Hashtable getServiceDescriptors() {
    if(serviceDescriptors == null) {
      serviceDescriptors = new Hashtable();
    }
    return serviceDescriptors;
  }
  
  public boolean containsServiceDescriptorID(String id) {
    return getServiceDescriptors().containsKey(id);    
  }
  
  public boolean containsServiceDescriptor(WebserviceDescriptionType serviceDescriptor) {
    return getServiceDescriptors().contains(serviceDescriptor);    
  }
  
  public void putServiceDescriptor(String id, WebserviceDescriptionType serviceDescriptor) throws Exception {    
    if(containsServiceDescriptorID(id)) {
      //throw new RegistryException(PatternKeys.WS_DUBLICATE_ELEMENT, new Object[]{id});
    }
    getServiceDescriptors().put(id, serviceDescriptor);
  }
  
  public WebserviceDescriptionType getServiceDescriptor(String id) {
    return (WebserviceDescriptionType)getServiceDescriptors().get(id);
  }
  
  public WebserviceDescriptionType removeServiceDescriptor(String id) {
    return (WebserviceDescriptionType)getServiceDescriptors().remove(id);
  }
  
  public String toString() {
    String resultStr = ""; 
    String nl = System.getProperty("line.separator");    
    
    Hashtable serviceDescriptors = getServiceDescriptors();
    
    if(serviceDescriptors.size() == 0) {
      return "EMPTY";
    }
    
    Enumeration enum1 = serviceDescriptors.keys();
    int i = 0; 
    while(enum1.hasMoreElements()) {
      resultStr += "Service[" + i++ + "]: " +enum1.nextElement() + nl;   
    }            
    
    return resultStr;
  }

}
