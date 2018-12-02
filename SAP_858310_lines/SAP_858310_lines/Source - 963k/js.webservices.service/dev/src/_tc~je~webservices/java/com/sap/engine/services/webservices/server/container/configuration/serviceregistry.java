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

import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.exceptions.PatternKeys;
import com.sap.engine.services.webservices.exceptions.RegistryException;

/**
 * Title: ServiceRegistry 
 * Description: ServiceRegistry is a registry for web services
 * 
 * @author Dimitrina Stoyanova
 * @version
 */
public class ServiceRegistry {

  private Hashtable<String, Service> services;
  
  public ServiceRegistry() { 
    this.services = new Hashtable<String, Service>(); 
  }  
  
  /**
   * @return - a hashtable of web services
   */
  public Hashtable getServices() {
    if(services == null) {
      services = new Hashtable();
    }
    return services;
  }
  
  public Service[] listServices() {
    Hashtable services = getServices(); 
  
    if(services.size() == 0) {
      return new Service[0];
    }
  
    Enumeration enum1 = services.elements();
    Service[] servicesArr = new Service[services.size()];
    int i = 0;
    while(enum1.hasMoreElements()) {
      servicesArr[i++] = (Service)enum1.nextElement();
    }   
  
    return servicesArr; 
  }
  
  public boolean containsServiceID(String id) {
    return getServices().containsKey(id);
  }
  
  public boolean containsService(Service service) {
    return getServices().contains(service);
  }
  
  public void putService(String id, Service service) throws Exception {
    if(containsServiceID(id)) {
      //throw new RegistryException(PatternKeys.WS_DUBLICATE_ELEMENT, new Object[]{id});
    }
    getServices().put(id, service);
  }
  
  public Service getService(String id) {
    return (Service)getServices().get(id);
  }
  
  public Service removeService(String id) {
    return (Service)getServices().remove(id);
  }
  
  public String toString() {
    String resultStr = ""; 
    String nl = System.getProperty("line.separator");    
    
    Hashtable services = getServices();
    
    if(services.size() == 0) {
      return "EMPTY";
    }
    
    Enumeration enum1 = services.keys();
    int i = 0; 
    while(enum1.hasMoreElements()) {
      resultStr += "Service[" + i++ + "]: " +enum1.nextElement() + nl;   
    }            
    
    return resultStr;
  }
 
}
