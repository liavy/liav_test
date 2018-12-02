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
 
package com.sap.engine.services.webservices.server.deploy.wsclients;

import java.io.File;
import java.util.Enumeration;

import javax.naming.Context;
import javax.naming.InitialContext;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationFactory;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.configuration.marshallers.sec.SecurityUtil;
import com.sap.engine.services.webservices.espbase.mappings.ImplementationLink;
import com.sap.engine.services.webservices.espbase.mappings.MappingFactory;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.server.deploy.WSBaseAbstractDProcess;
import com.sap.engine.services.webservices.server.deploy.WebServicesDNotificationInterface;
import com.sap.engine.services.webservices.server.deploy.WebServicesDeployManager;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ServiceRefGroupDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.WSClientsJ2EEEngineExtFactory;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.WSClientsJ2EEEngineFactory;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.WsClientsType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ext.WsClientsExtType;
import com.sap.engine.services.webservices.server.container.configuration.ApplicationConfigurationContext;
import com.sap.engine.services.webservices.server.container.configuration.ConfigurationContext;
import com.sap.engine.services.webservices.server.container.configuration.ServiceRegistry;
import com.sap.engine.services.webservices.server.container.descriptors.ConfigurationDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.descriptors.MappingDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.mapping.ServiceMappingRegistry;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeData;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeDataRegistry;
import com.sap.engine.services.webservices.server.container.metadata.service.BindingDataMetaData;
import com.sap.engine.services.webservices.server.container.metadata.service.BindingDataMetaDataRegistry;
import com.sap.engine.services.webservices.server.container.ws.metaData.WSApplicationMetaDataContext;
import com.sap.engine.services.webservices.server.container.wsclients.ServiceRefContext;
import com.sap.engine.services.webservices.server.container.wsclients.descriptors.ServiceRefGroupDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.descriptors.WSClientsApplicationDescriptorContext;
import com.sap.engine.services.webservices.server.container.wsclients.descriptors.WSClientsJ2EEEngineDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.descriptors.WSClientsJ2EEEngineExtDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.ServiceRefGroupMetaData;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.ServiceRefGroupMetaDataRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.ServiceRefMetaData;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.ServiceRefMetaDataRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.WSClientsApplicationMetaDataContext;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.WSClientsMetaDataContext;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Title: WSClientsAbstractDProcess
 * Description: WSClientsAbstractDProcess
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public abstract class WSClientsAbstractDProcess extends WSBaseAbstractDProcess {
   
  public static final String WS_CLIENTS_J2EE_ENGINE_DESCRIPTOR = "ws-clients-j2ee-engine.xml";
  public static final String WS_CLIENTS_J2EE_ENGINE_EXT_DESCRIPTOR = "ws-clients-j2ee-engine-ext.xml";
  public static String PROXIES_CONTEXT = "wsclients/proxies";
 
  protected WebServicesDNotificationInterface wsClientsDNotificationHandler; 
  
  public ServiceRefContext getServiceContext() {
    return (ServiceRefContext)serviceContext;
  }      
  
  protected void loadWSClientsJ2EEEngineDescriptorsInitially() throws WSDeploymentException {    
    try {    
      if(!appConfiguration.existsSubConfiguration(WebServicesDeployManager.WEBSERVICES_CONTAINER_NAME)) {
        return; 
      }   
             
      Configuration webServicesContainerConfiguration = appConfiguration.getSubConfiguration(WebServicesDeployManager.WEBSERVICES_CONTAINER_NAME);    
      loadWSClientsApplicationMetaData(webServicesContainerConfiguration);
      loadWSClientsJ2EEEngineDescriptorsAlone(); 
    } catch(WSDeploymentException e) {       
      throw e; 
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_CL_D_EXCEPTION_LOAD, new Object[]{applicationName}, e); 
    }
  }

  private void loadWSClientsApplicationMetaData(Configuration webServicesContainerConfiguration) throws ConfigurationException {           
    if(!webServicesContainerConfiguration.existsSubConfiguration(WSApplicationMetaDataContext.METADATA)) {
      return; 
    }

    WSClientsApplicationMetaDataContext wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)getServiceContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().get(applicationName); 
    if(wsClientsApplicationMetaDataContext == null) {
      getServiceContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().put(applicationName, new WSClientsApplicationMetaDataContext()); 
      wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)getServiceContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().get(applicationName);
    } 
  
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = wsClientsApplicationMetaDataContext.getModuleRuntimeDataRegistry();

    loadApplicationMetaData(webServicesContainerConfiguration, moduleRuntimeDataRegistry);
        
  }
 
  private void loadWSClientsJ2EEEngineDescriptorsAlone() throws WSDeploymentException {        
    WSClientsApplicationMetaDataContext wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)getServiceContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().get(applicationName);
    if(wsClientsApplicationMetaDataContext == null) {
      return; 
    }
  
    Enumeration enum1 = wsClientsApplicationMetaDataContext.getModuleRuntimeDataRegistry().getModuleRuntimeDatas().elements();
    ModuleRuntimeData moduleRuntimeData;     
    while(enum1.hasMoreElements()) {                 
     moduleRuntimeData = (ModuleRuntimeData)enum1.nextElement();             
     loadWSClientsJ2EEEngineDescriptorsAlone(moduleRuntimeData);              
    }                    
  } 

  private void loadWSClientsJ2EEEngineDescriptorsAlone(ModuleRuntimeData moduleRuntimeData) throws WSDeploymentException {
    String wsClientsJ2EEEngineDescriptorRelPath = moduleRuntimeData.getMetaInfRelDir() + "/" + WS_CLIENTS_J2EE_ENGINE_DESCRIPTOR;     
    String wsClientsJ2EEEngineExtDescriptorRelPath = moduleRuntimeData.getMetaInfRelDir() + "/" + WS_CLIENTS_J2EE_ENGINE_EXT_DESCRIPTOR;
        
    if(!new File(moduleRuntimeData.getFilePathName(wsClientsJ2EEEngineDescriptorRelPath)).exists()) {
      return; 
    } 
                                        
    WSClientsApplicationDescriptorContext wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName);
    if(wsClientsApplicationDescriptorContext == null) {
      getServiceContext().getWsClientsApplicationDescriptorContexts().put(applicationName, new WSClientsApplicationDescriptorContext());
      wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName);     
    }
      
    WSClientsJ2EEEngineDescriptorRegistry wsClientsJ2EEEngineDescriptorRegistry = wsClientsApplicationDescriptorContext.getWsClientsJ2EEEngineDescriptorRegistry();
    WSClientsJ2EEEngineExtDescriptorRegistry wsClientsJ2EEEngineExtDescriptorRegistry = wsClientsApplicationDescriptorContext.getWsClientsJ2EEEngineExtDescriptorRegistry();          
  
    try {          
      WsClientsType wsClientsJ2EEEngineDescriptor = WSClientsJ2EEEngineFactory.load(moduleRuntimeData.getFilePathName(wsClientsJ2EEEngineDescriptorRelPath));      
      wsClientsJ2EEEngineDescriptorRegistry.putWsClientsJ2EEEngineDescriptor(moduleRuntimeData.getModuleName(), wsClientsJ2EEEngineDescriptor);      
      if(new File(moduleRuntimeData.getFilePathName(wsClientsJ2EEEngineExtDescriptorRelPath)).exists()) {        
        WsClientsExtType wsClientsJ2EEEngineExtDescriptor = WSClientsJ2EEEngineExtFactory.load(moduleRuntimeData.getFilePathName(wsClientsJ2EEEngineExtDescriptorRelPath));
        wsClientsJ2EEEngineExtDescriptorRegistry.putWsClientsJ2EEEngineExtDescriptor(moduleRuntimeData.getModuleName(), wsClientsJ2EEEngineExtDescriptor);
      }
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_CL_D_EXCEPTION_LOAD_2, new Object[]{applicationName, moduleRuntimeData.getModuleName()}, e); 
    }
  }   
    
  protected void loadWSClientsJ2EEEngineDescriptors() throws WSDeploymentException {    
    WSClientsApplicationDescriptorContext wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName);
    if(wsClientsApplicationDescriptorContext == null) {
      return; 
    }
     
    WSClientsJ2EEEngineDescriptorRegistry wsClientsJ2EEEngineDescriptorRegistry = wsClientsApplicationDescriptorContext.getWsClientsJ2EEEngineDescriptorRegistry();      
    WSClientsApplicationMetaDataContext wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)getServiceContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().get(applicationName);          
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = wsClientsApplicationMetaDataContext.getModuleRuntimeDataRegistry();
 
    Enumeration enum1 = wsClientsJ2EEEngineDescriptorRegistry.getWsClientsJ2EEEngineDescriptors().keys();     
    while(enum1.hasMoreElements()) {     
      String moduleName = (String)enum1.nextElement(); 
      WsClientsType wsClientsJ2EEEngineDescriptor = (WsClientsType)wsClientsJ2EEEngineDescriptorRegistry.getWsClientsJ2EEEngineDescriptor(moduleName);
      ModuleRuntimeData moduleRuntimeData = (ModuleRuntimeData)moduleRuntimeDataRegistry.getModuleRuntimeData(moduleName);
      loadWSClientsJ2EEEngineDescriptors(moduleRuntimeData, wsClientsJ2EEEngineDescriptor);         
    }      
  }
    
  private void loadWSClientsJ2EEEngineDescriptors(ModuleRuntimeData moduleRuntimeData, WsClientsType wsClientsJ2EEEngineDescriptor) throws WSDeploymentException {
    ServiceRefGroupDescriptionType[] serviceRefGroupDescriptors = wsClientsJ2EEEngineDescriptor.getServiceRefGroupDescription();     
    if(serviceRefGroupDescriptors == null || serviceRefGroupDescriptors.length == 0) {
      return;  
    }  
    
    try {      
      for(int i = 0; i < serviceRefGroupDescriptors.length; i++) {            
        loadServiceRefGroupDescriptors(moduleRuntimeData, serviceRefGroupDescriptors[i]);                   
      }    
      loadServiceRefGroupDescriptors(serviceRefGroupDescriptors);     
      loadServiceRefGroupMetaDatas(moduleRuntimeData.getModuleName(), serviceRefGroupDescriptors);
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_CL_D_EXCEPTION_LOAD_2, new Object[]{applicationName, moduleRuntimeData.getModuleName()}, e);
    }     
  }
    
  private void loadServiceRefGroupDescriptors(ModuleRuntimeData moduleRuntimeData, ServiceRefGroupDescriptionType serviceRefGroupDescriptor) throws Exception {
    ConfigurationRoot configurationDescriptor;
    MappingRules mappingDescriptor; 
    
    WSClientsApplicationDescriptorContext wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName); 
        
    String serviceRefGroupName = serviceRefGroupDescriptor.getServiceRefGroupName().trim();
    
    String configurationDescriptorFilePath = moduleRuntimeData.getFilePathName(serviceRefGroupDescriptor.getConfigurationFile().trim()); 
    configurationDescriptor = ConfigurationFactory.load(configurationDescriptorFilePath);
    //apply required security properties. Without them the SecurityProtocol throws exceptions at runtime.
    SecurityUtil.addMissingSecurityProperties(configurationDescriptor);
    wsClientsApplicationDescriptorContext.getConfigurationDescriptorRegistry().putConfigurationDescriptor(serviceRefGroupName, configurationDescriptor);
    
    String mappingDescriptorFilePath = moduleRuntimeData.getFilePathName(serviceRefGroupDescriptor.getWsdlMappingFile().trim()); 
    mappingDescriptor = MappingFactory.load(mappingDescriptorFilePath);             
    wsClientsApplicationDescriptorContext.getMappingDescriptorRegistry().putMappingDescriptor(serviceRefGroupName, mappingDescriptor);
    
    loadConfigurationDescriptor(configurationDescriptor);
    loadMappingDescriptor(moduleRuntimeData.getModuleName(), mappingDescriptor);       
    loadServiceRefMetaDatas(serviceRefGroupDescriptor, configurationDescriptor.getRTConfig().getService());
    loadBindingDataMetaDatas(applicationName, configurationDescriptor.getRTConfig().getService());    
  }
  
  private void loadServiceRefMetaDatas(ServiceRefGroupDescriptionType serviceRefGroupDescriptor, Service[] services) throws Exception {
    if(services == null || services.length == 0) {
      return;       
    }     
    
    WSClientsApplicationMetaDataContext wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)getServiceContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().get(applicationName);
    ServiceRefMetaDataRegistry serviceRefMetaDataRegistry = wsClientsApplicationMetaDataContext.getServiceRefMetaDataRegistry();     
    
    String serviceName;      
    String serviceRefGroupName = serviceRefGroupDescriptor.getServiceRefGroupName().trim(); 
    ServiceRefMetaData serviceRefMetaData;     
    for(int i = 0; i < services.length; i++) {
      serviceName = services[i].getName().trim(); 
      serviceRefMetaData = new ServiceRefMetaData(applicationName, serviceRefGroupName, serviceName);            
      serviceRefMetaDataRegistry.putServiceRefMetaData(serviceName, serviceRefMetaData);                         
    }  
  }
  
  protected void loadBindingDataMetaDatas(String applicationName, Service service, BindingData[] bindingDatas) {
    if(bindingDatas == null || bindingDatas.length == 0)  {
      return; 
    }
        
    String serviceName = service.getName().trim(); 
    String serviceContextRoot = service.getServiceData().getContextRoot().trim(); 
    if(!serviceContextRoot.equals("") && !serviceContextRoot.startsWith("/")) {
      serviceContextRoot = "/" + serviceContextRoot; 
    } 
    
    WSClientsMetaDataContext wsClientsMetaDataContext = getServiceContext().getWsClientsMetaDataContext(); 
    BindingDataMetaDataRegistry globalBindingDataMetaDataRegistry = wsClientsMetaDataContext.getGlobalBindingDataMetaDataRegistry();  
    WSClientsApplicationMetaDataContext wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)wsClientsMetaDataContext.getWsClientsApplicationMetaDataContexts().get(applicationName);
    BindingDataMetaDataRegistry bindingDataMetaDataRegistry = (BindingDataMetaDataRegistry)wsClientsApplicationMetaDataContext.getBindingDataMetaDataRegistries().get(serviceName);     
    if(bindingDataMetaDataRegistry == null) {
      wsClientsApplicationMetaDataContext.getBindingDataMetaDataRegistries().put(serviceName, new BindingDataMetaDataRegistry());
      bindingDataMetaDataRegistry = (BindingDataMetaDataRegistry)wsClientsApplicationMetaDataContext.getBindingDataMetaDataRegistries().get(serviceName);
    }
    
    String bindingDataUrl;
    String bindingDataName; 
    BindingData bindingData;
    BindingDataMetaData bindingDataMetaData;          
    for(int i = 0; i < bindingDatas.length; i++ ) {
      bindingData = bindingDatas[i];
      bindingDataName = bindingData.getName().trim(); 
      bindingDataUrl = bindingData.getUrl().trim();
      bindingDataUrl = bindingData.getUrl().trim();            
      if(!bindingDataUrl.startsWith("/")) {
        bindingDataUrl = "/" + bindingDataUrl;
      }
      
      bindingDataMetaData = new BindingDataMetaData(applicationName, serviceName, bindingData.getName().trim());
      bindingDataMetaDataRegistry.putBindingDataMetaData(bindingDataName, bindingDataMetaData);
      globalBindingDataMetaDataRegistry.putBindingDataMetaData(bindingDataUrl, bindingDataMetaData);        
    }  
  }  
  
  private void loadServiceRefGroupMetaDatas(String moduleName, ServiceRefGroupDescriptionType[] serviceRefGroupDescriptors) throws Exception {
    if(serviceRefGroupDescriptors == null || serviceRefGroupDescriptors.length == 0) {
      return;   
    }
        
    WSClientsApplicationMetaDataContext wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)getServiceContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().get(applicationName);    
    ServiceRefGroupMetaDataRegistry serviceRefGroupMetaDataRegistry = wsClientsApplicationMetaDataContext.getServiceRefGroupMetaDataRegistry();
      
    String serviceRefGroupName;  
    ServiceRefGroupMetaData serviceRefGroupMetaData; 
    for(int i = 0; i < serviceRefGroupDescriptors.length; i++) {
      serviceRefGroupName = serviceRefGroupDescriptors[i].getServiceRefGroupName().trim();            
      serviceRefGroupMetaData = new ServiceRefGroupMetaData(applicationName, moduleName, serviceRefGroupName);                 
      serviceRefGroupMetaDataRegistry.putServiceRefGroupMetaData(serviceRefGroupName, serviceRefGroupMetaData);      
    }
  }  
  
  private void loadServiceRefGroupDescriptors(ServiceRefGroupDescriptionType[] serviceRefGroupDescriptors) throws Exception {
    if(serviceRefGroupDescriptors == null || serviceRefGroupDescriptors.length == 0) {
      return; 
    }
    
    WSClientsApplicationDescriptorContext wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName);
    ServiceRefGroupDescriptorRegistry serviceRefGroupDescriptorRegistry = wsClientsApplicationDescriptorContext.getServiceRefGroupDescriptorRegistry(); 
     
    ServiceRefGroupDescriptionType serviceRefGroupDescriptor; 
    for(int i = 0; i < serviceRefGroupDescriptors.length; i++) {
      serviceRefGroupDescriptor = serviceRefGroupDescriptors[i];          
      serviceRefGroupDescriptorRegistry.putServiceRefGroupDescriptor(serviceRefGroupDescriptor.getServiceRefGroupName().trim(), serviceRefGroupDescriptor);        
    } 
  }
  
  private void unregisterMappingContext() {
    WSClientsApplicationDescriptorContext wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName);
    if(wsClientsApplicationDescriptorContext == null) {
      return; 
    } 
    
    MappingDescriptorRegistry mappingDescriptorRegistry = wsClientsApplicationDescriptorContext.getMappingDescriptorRegistry();
    
    Enumeration enum1 = mappingDescriptorRegistry.getMappingDescriptors().elements();
    MappingRules mappingDescriptor;         
    while(enum1.hasMoreElements()) {
      mappingDescriptor = (MappingRules)enum1.nextElement();
      unregisterInterfaceMappingDescriptors(mappingDescriptor.getInterface());
      unregisterServiceMappingDescriptors(mappingDescriptor.getService());       
    }       
  }
  
  private void unregisterConfigurationContext() {  
    unregisterInterfaceDefinitionDescriptors();     
    unregisterServiceConfigurationDescriptors();
  }  
  
  private void unregisterInterfaceDefinitionDescriptors() {
    WSClientsApplicationDescriptorContext wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName);
    if(wsClientsApplicationDescriptorContext == null) {
      return; 
    }
    
    ConfigurationDescriptorRegistry configurationDescriptorRegistry = wsClientsApplicationDescriptorContext.getConfigurationDescriptorRegistry();
    Enumeration enum1 = configurationDescriptorRegistry.getConfigurationDescriptors().elements(); 
    while(enum1.hasMoreElements()) {
      unregisterInterfaceDefinitionDescriptors(((ConfigurationRoot)enum1.nextElement()).getDTConfig().getInterfaceDefinition());   
    }     
  }
  
  private void unregisterMetaDataContext() {
    unregisterGlobalBindingDataMetaDatas();
    getServiceContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().remove(applicationName);
  }
  
  private void unregisterGlobalBindingDataMetaDatas() {
    ConfigurationContext configurationContext = serviceContext.getConfigurationContext(); 
    if(!configurationContext.getApplicationConfigurationContexts().containsKey(applicationName)) {
      return; 
    }
      
    ApplicationConfigurationContext applicationConfigurationContext = (ApplicationConfigurationContext)configurationContext.getApplicationConfigurationContexts().get(applicationName);
    ServiceRegistry serviceRegistry = applicationConfigurationContext.getServiceRegistry();               
    BindingDataMetaDataRegistry bindingDataMetaDataRegistry = getServiceContext().getWsClientsMetaDataContext().getGlobalBindingDataMetaDataRegistry();   
    
    Enumeration enum1 = serviceRegistry.getServices().keys();        
    while(enum1.hasMoreElements()) {
      String serviceName = (String)enum1.nextElement();      
      Service service = (Service)serviceRegistry.getService(serviceName);
      unregisterGlobalBindingDataMetaData(service, bindingDataMetaDataRegistry);
    }
  }
  
  private void unbindServiceImplInstances() throws Exception {
    ConfigurationContext configurationContext = getServiceContext().getConfigurationContext(); 
    ApplicationConfigurationContext wsApplicationConfigurationContext = (ApplicationConfigurationContext)configurationContext.getApplicationConfigurationContexts().get(applicationName);
    if(wsApplicationConfigurationContext == null) {
      return; 
    }
  
    ServiceMappingRegistry serviceMappingRegistry = getServiceContext().getMappingContext().getServiceMappingRegistry(); 
    ServiceRegistry serviceRegistry = wsApplicationConfigurationContext.getServiceRegistry();     
  
    Context proxiesContext; 
    try {   
      Context context = new InitialContext();
      proxiesContext = (Context)context.lookup(PROXIES_CONTEXT);
    } catch(Exception e) {
      //TODO
      e.printStackTrace(); 
      throw e; 
    }        
    
    Enumeration enum1 = serviceRegistry.getServices().elements(); 
    Service service;
    ServiceMapping serviceMapping;     
    while(enum1.hasMoreElements()) {
      try {
        service = (Service)enum1.nextElement(); 
        serviceMapping = serviceMappingRegistry.getServiceMapping(service.getServiceMappingId().trim());
        WSUtil.unbind(proxiesContext, applicationName + "/" + serviceMapping.getImplementationLink().getProperty(ImplementationLink.SERVICE_REF_JNDI_NAME));            
      } catch(Exception e) {
        //TODO - add warning
        e.printStackTrace();        
      } 
    }        
  
    //TODO - warning    
  } 
    
  public void unregister() throws Exception {    
    try {
      unbindServiceImplInstances();    
    } catch(Exception e) {
      //TODO - add warning
      e.printStackTrace(); 
    }
    
    unregisterMetaDataContext();    
    unregisterMappingContext();
    unregisterConfigurationContext();
   
    getServiceContext().getWsClientsApplicationDescriptorContexts().remove(applicationName);   
  }
            
}
