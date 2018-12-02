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
package com.sap.engine.services.webservices.server.container.wsclients;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.server.container.BaseServiceContext;
import com.sap.engine.services.webservices.server.container.configuration.ApplicationConfigurationContext;
import com.sap.engine.services.webservices.server.container.configuration.ServiceRegistry;
import com.sap.engine.services.webservices.server.container.mapping.ServiceMappingRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.descriptors.WSClientsApplicationDescriptorContext;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.WSClientsMetaDataContext;

/**
 * Title: ServiceRefContext 
 * Description: ServiceRefContext
 * 
 * @author Dimitrina Stoyanova
 * @version
 */
public class ServiceRefContext extends BaseServiceContext {

  private WSClientsMetaDataContext wsClientsMetaDataContext; 
  private Hashtable<String, WSClientsApplicationDescriptorContext> wsClientsApplicationDescriptorContexts; 

  public ServiceRefContext() {
	  
  }  
  
  /**
   * @return ws clients application descriptor contexts
   */
  public synchronized Hashtable<String, WSClientsApplicationDescriptorContext> getWsClientsApplicationDescriptorContexts() {
    if(wsClientsApplicationDescriptorContexts == null) {
      wsClientsApplicationDescriptorContexts = new Hashtable<String, WSClientsApplicationDescriptorContext>(); 
    }
    
    return wsClientsApplicationDescriptorContexts;
  }
  
  /**
   * @return WSClientsMetaDataContext
   */
  public synchronized WSClientsMetaDataContext getWsClientsMetaDataContext() {
    if(wsClientsMetaDataContext == null) {
      wsClientsMetaDataContext = new WSClientsMetaDataContext(); 
    }
    
    return wsClientsMetaDataContext;
  } 
 
  public Hashtable<String, Service[]> getServicesBySIName(String[] applicationNames, String siName, boolean checkAll) {
    if(applicationNames == null || applicationNames.length == 0) {
      return new Hashtable<String, Service[]>();
    }
    
    Hashtable<String, Service[]> serviceNames = new Hashtable<String, Service[]>();  
    Service[] currentServices; 
    for(String applicationName: applicationNames) {
      currentServices = getServicesBySIName(applicationName, siName, checkAll); 
      if(currentServices != null && currentServices.length != 0) {
    	serviceNames.put(applicationName, currentServices);  
        if(!checkAll) {
          break;  	
        } 	  
      }
    }
	  
    return serviceNames;  
  }
  
  public Service[] getServicesBySIName(String applicationName, String siName, boolean checkAll) {
    ApplicationConfigurationContext applicationConfigurationContext = (ApplicationConfigurationContext)this.getConfigurationContext().getApplicationConfigurationContexts().get(applicationName);
    if(applicationConfigurationContext == null) {
      return new Service[0]; 	
    }
    
    ServiceRegistry serviceRegistry = applicationConfigurationContext.getServiceRegistry();
    ServiceMappingRegistry serviceMappingRegistry = this.getMappingContext().getServiceMappingRegistry();
         
    Enumeration<Service> enumer = serviceRegistry.getServices().elements();
    Service service;
    ServiceMapping serviceMapping; 
    ArrayList<Service> services = new ArrayList<Service>(); 
    while(enumer.hasMoreElements()) {
      service = enumer.nextElement();
      serviceMapping = serviceMappingRegistry.getServiceMapping(service.getServiceMappingId());
      if(serviceMapping.getSIName().equals(siName)) {
        services.add(service);
        if(!checkAll) {
          break; 
        }         	 
      }             
    }	  
    
    return services.toArray(new Service[services.size()]);     
  }  
  
}
