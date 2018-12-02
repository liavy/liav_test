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

package com.sap.engine.services.webservices.server.deploy.ws;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.frame.core.configuration.ConfigurationHandler;
import com.sap.engine.frame.core.configuration.ConfigurationIntersectionException;
import com.sap.engine.frame.core.configuration.ConfigurationLockedException;
import com.sap.engine.frame.core.configuration.NameNotFoundException;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationFactory;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.configuration.marshallers.sec.SecurityUtil;
import com.sap.engine.services.webservices.espbase.mappings.MappingFactory;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;
import com.sap.engine.services.webservices.server.deploy.WSBaseAbstractDProcess;
import com.sap.engine.services.webservices.server.deploy.WebServicesDNotificationInterface;
import com.sap.engine.services.webservices.server.deploy.WebServicesDeployManager;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.InterfaceDefinitionDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.TypeMappingFileType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebServicesJ2EEEngineExtFactory;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebServicesJ2EEEngineFactory;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebservicesType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlFileType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlStyleType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlTypeType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebservicesExtType;
import com.sap.engine.services.webservices.server.container.OfflineBaseServiceContext;
import com.sap.engine.services.webservices.server.container.configuration.ApplicationConfigurationContext;
import com.sap.engine.services.webservices.server.container.configuration.ConfigurationContext;
import com.sap.engine.services.webservices.server.container.configuration.ServiceRegistry;
import com.sap.engine.services.webservices.server.container.descriptors.ConfigurationDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.descriptors.MappingDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.metadata.InterfaceDefMetaData;
import com.sap.engine.services.webservices.server.container.metadata.InterfaceDefMetaDataRegistry;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeData;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeDataRegistry;
import com.sap.engine.services.webservices.server.container.metadata.service.BindingDataMetaData;
import com.sap.engine.services.webservices.server.container.metadata.service.BindingDataMetaDataRegistry;
import com.sap.engine.services.webservices.server.container.ws.ServiceContext;
import com.sap.engine.services.webservices.server.container.ws.descriptors.InterfaceDefinitionDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.ws.descriptors.ServiceDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.ws.descriptors.ServiceExtDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.ws.descriptors.WSApplicationDescriptorContext;
import com.sap.engine.services.webservices.server.container.ws.descriptors.WebServicesDescriptorContext;
import com.sap.engine.services.webservices.server.container.ws.descriptors.WebServicesJ2EEEngineDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.ws.descriptors.WebServicesJ2EEEngineExtDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.ws.metaData.WSApplicationMetaDataContext;
import com.sap.engine.services.webservices.server.container.ws.metaData.WebServicesMetaDataContext;
import com.sap.engine.services.webservices.server.container.ws.metaData.ServiceMetaData;
import com.sap.engine.services.webservices.server.container.ws.metaData.ServiceMetaDataRegistry;
import com.sap.engine.services.webservices.server.container.ws.wsdl.ApplicationWSDLContext;
import com.sap.engine.services.webservices.server.container.ws.wsdl.WSDLContext;
import com.sap.engine.services.webservices.server.container.ws.wsdl.WSDLRegistry;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Title: WSAbstractDProcessor 
 * Description: WSAbstractDProcessor is a base, abstract implementation of WebServiceDInterface
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public abstract class WSAbstractDProcess extends WSBaseAbstractDProcess {

  protected static final String META_INF = "META-INF";
  protected static final String WEB_INF = "WEB-INF";
  public static final String WEBSERVICES_J2EE_ENGINE_DESCRIPTOR = "webservices-j2ee-engine.xml";
  protected static final String WEBSERVICES_J2EE_ENGINE_EXT_DESCRIPTOR = "webservices-j2ee-engine-ext.xml";
  protected static final String SR_PUBLICATION_METADATA_DESCRIPTOR = "sr-publication.metadata";
   
  protected WebServicesDNotificationInterface wsDNotificationHandler;         
  
  public ServiceContext getServiceContext() {
    return (ServiceContext)serviceContext;
  }    
  
  protected void loadWebServicesJ2EEEngineDescriptorsInitially() throws WSDeploymentException {            
    try {
      if(!appConfiguration.existsSubConfiguration(WebServicesDeployManager.WEBSERVICES_CONTAINER_NAME)) {
        return; 
      }  
    
      loadApplicationMetaData(appConfiguration.getSubConfiguration(new File(webServicesContainerDir).getName()));    
      loadWebServicesJ2EEEngineDescriptorsAlone();              
    } catch(WSDeploymentException e) {
      throw e; 
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_D_EXCEPTION_LOAD, new Object[]{applicationName}, e); 
    }            
  }
  
  private void loadApplicationMetaData(Configuration configuration) throws ConfigurationException {    
    if(!configuration.existsSubConfiguration(WSApplicationMetaDataContext.METADATA)) {
      return; 
    }

    if(!getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().containsKey(applicationName)) {
      getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().put(applicationName, new WSApplicationMetaDataContext()); 
    } 

    WSApplicationMetaDataContext applicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = applicationMetaDataContext.getModuleRuntimeDataRegistry();

    loadApplicationMetaData(configuration, moduleRuntimeDataRegistry);      
  }
   
  private void loadWebServicesJ2EEEngineDescriptorsAlone() throws WSDeploymentException {
    WSApplicationMetaDataContext wsApplicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);
    if(wsApplicationMetaDataContext == null) {
      return; 
    }         
      
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = wsApplicationMetaDataContext.getModuleRuntimeDataRegistry();
 
    Enumeration enum1 = moduleRuntimeDataRegistry.getModuleRuntimeDatas().elements(); 
    ModuleRuntimeData moduleRuntimeData; 
    
    while(enum1.hasMoreElements()) {        
      loadWebServicesJ2EEEngineDescriptorsAlone((ModuleRuntimeData)enum1.nextElement());     
    }   
  }  
  
  private void loadWebServicesJ2EEEngineDescriptorsAlone(ModuleRuntimeData moduleRuntimeData) throws WSDeploymentException {
    String webServicesJ2EEEngineDescriptorRelPath = META_INF + "/" + WEBSERVICES_J2EE_ENGINE_DESCRIPTOR;      
    String webServicesJ2EEEngineExtDescriptorRelPath = META_INF + "/" + WEBSERVICES_J2EE_ENGINE_EXT_DESCRIPTOR;
    
    if(!new File(moduleRuntimeData.getFilePathName(webServicesJ2EEEngineDescriptorRelPath)).exists()) {
      return; 
    }
    
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName); 
    if(wsApplicationDescriptorContext == null) {
      getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().put(applicationName, new WSApplicationDescriptorContext());
      wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);
    }
    
    WebServicesJ2EEEngineDescriptorRegistry webServicesJ2EEEngineDescriptorRegistry = wsApplicationDescriptorContext.getWebServicesJ2EEEngineDescriptorRegistry(); 
    WebServicesJ2EEEngineExtDescriptorRegistry webServicesJ2EEEngineExtDescriptorRegistry = wsApplicationDescriptorContext.getWebServicesJ2EEEngineExtDescriptorRegistry(); 
    try {
      WebservicesType webServicesJ2EEEngineDescriptor = WebServicesJ2EEEngineFactory.load(moduleRuntimeData.getModuleDir() + "/" + webServicesJ2EEEngineDescriptorRelPath);
      webServicesJ2EEEngineDescriptorRegistry.putWebServicesJ2EEEngineDescriptor(moduleRuntimeData.getModuleName(), webServicesJ2EEEngineDescriptor);
      
      if(new File(moduleRuntimeData.getFilePathName(webServicesJ2EEEngineExtDescriptorRelPath)).exists()) {
        WebservicesExtType webServicesJ2EEEngineExtDescriptor = WebServicesJ2EEEngineExtFactory.load(moduleRuntimeData.getFilePathName(webServicesJ2EEEngineExtDescriptorRelPath));
        webServicesJ2EEEngineExtDescriptorRegistry.putWebServicesJ2EEEngineExtDescriptor(moduleRuntimeData.getModuleName(), webServicesJ2EEEngineExtDescriptor);
      }
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_D_EXCEPTION_LOAD_2, new Object[]{applicationName, moduleRuntimeData.getModuleName()}, e); 
    }
  }
  
  protected void loadWebServicesJ2EEEngineDescriptors() throws WSDeploymentException {    
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);    
    if(wsApplicationDescriptorContext == null) {
      return; 
    }
        
    WSApplicationMetaDataContext applicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);          
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = applicationMetaDataContext.getModuleRuntimeDataRegistry();
    WebServicesJ2EEEngineDescriptorRegistry webServicesJ2EEEngineDescriptorRegistry = wsApplicationDescriptorContext.getWebServicesJ2EEEngineDescriptorRegistry(); 
    WebServicesJ2EEEngineExtDescriptorRegistry webServicesJ2EEEngineExtDescriptorRegistry = wsApplicationDescriptorContext.getWebServicesJ2EEEngineExtDescriptorRegistry(); 
  
    Enumeration enum1 = webServicesJ2EEEngineDescriptorRegistry.getWebServicesJ2EEEngineDescriptors().keys();     
    WebservicesExtType webServicesJ2EEEngineExtDescriptor; 
    while(enum1.hasMoreElements()) {     
      String moduleName = (String)enum1.nextElement(); 
      WebservicesType webServicesJ2EEEngineDescriptor = (WebservicesType)webServicesJ2EEEngineDescriptorRegistry.getWebServicesJ2EEEngineDescriptor(moduleName);
      webServicesJ2EEEngineExtDescriptor = webServicesJ2EEEngineExtDescriptorRegistry.getWebServicesJ2EEEngineExtDescriptor(moduleName);
      ModuleRuntimeData moduleRuntimeData = (ModuleRuntimeData)moduleRuntimeDataRegistry.getModuleRuntimeData(moduleName);      
      loadWebServicesJ2EEEngineDescriptors(applicationName, webServicesJ2EEEngineDescriptor, webServicesJ2EEEngineExtDescriptor, moduleRuntimeData);                   
    }      
  }  

  protected void loadWebServicesJ2EEEngineDescriptors(String applicationName, WebservicesType webServicesJ2EEEngineDescriptor, WebservicesExtType webServicesJ2EEEngineExtDescriptor, ModuleRuntimeData moduleRuntimeData) throws WSDeploymentException {                   
    try {          
      WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);
 
      String configurationDescriptorFilePath = moduleRuntimeData.getFilePathName(webServicesJ2EEEngineDescriptor.getConfigurationFile().trim());      
      ConfigurationRoot configurationDescriptor = ConfigurationFactory.load(configurationDescriptorFilePath);
      //apply required security properties. Without them the SecurityProtocol throws exceptions at runtime.
      SecurityUtil.addMissingSecurityProperties(configurationDescriptor);
      wsApplicationDescriptorContext.getConfigurationDescriptorRegistry().putConfigurationDescriptor(moduleRuntimeData.getModuleName(), configurationDescriptor);
      
      String mappingDescriptorFilePathName = moduleRuntimeData.getFilePathName(webServicesJ2EEEngineDescriptor.getWsdlMappingFile().trim());
      MappingRules mappingDescriptor = MappingFactory.load(mappingDescriptorFilePathName);                 
      wsApplicationDescriptorContext.getMappingDescriptorRegistry().putMappingDescriptor(moduleRuntimeData.getModuleName(), mappingDescriptor);

      loadWebServicesJ2EEEngineDescriptors(applicationName, webServicesJ2EEEngineDescriptor, webServicesJ2EEEngineExtDescriptor, configurationDescriptor, mappingDescriptor, moduleRuntimeData);            
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);   
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_D_EXCEPTION_LOAD_2, new Object[]{applicationName, moduleRuntimeData.getModuleName()}, e);      
    }                       
  }
  
  protected void loadWebServicesJ2EEEngineDescriptors(String applicationName, WebservicesType webServicesJ2EEEngineDescriptor, WebservicesExtType webServicesJ2EEEngineExtDescriptor, ConfigurationRoot configurationDescriptor, MappingRules mappingDescriptor, ModuleRuntimeData moduleRuntimeData) throws Exception {                   
    loadConfigurationDescriptor(configurationDescriptor); 
    loadMappingDescriptor(moduleRuntimeData.getModuleName(), mappingDescriptor);              
    
    Service services[] = configurationDescriptor.getRTConfig().getService();      
    loadServiceMetaDatas(applicationName, services, moduleRuntimeData);
    loadInterfaceDefMetaDatas(); 
    loadAdditionalInterfaceDefMetaDatas();       
    loadBindingDataMetaDatas(applicationName, services);        
    
    WebserviceDescriptionType[] serviceDescriptors = webServicesJ2EEEngineDescriptor.getWebserviceDescription();              
    loadServiceDescriptors(applicationName, serviceDescriptors);
    loadInterfaceDefinitionDescriptors(webServicesJ2EEEngineDescriptor.getInterfaceDefinitionDescription());
    
    if(webServicesJ2EEEngineExtDescriptor != null) {
      loadServiceExtDescriptors(webServicesJ2EEEngineExtDescriptor.getWebserviceDescription());
    }
    loadServiceWSDLFileDescriptors(applicationName, serviceDescriptors);
    loadInterfaceDefWSDLFileDescriptors(webServicesJ2EEEngineDescriptor.getInterfaceDefinitionDescription());      
         
    //TODO - check granularity             
  }
  
  protected void loadServiceDescriptors(String applicationName, WebserviceDescriptionType[] serviceDescriptors) throws Exception {
    if(serviceDescriptors == null || serviceDescriptors.length == 0) {
      return;
    }    
    
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);   
    if(wsApplicationDescriptorContext == null) {
      getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().put(applicationName, new WSApplicationDescriptorContext());
      wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);      
    }
            
    ServiceDescriptorRegistry serviceDescriptorRegistry = wsApplicationDescriptorContext.getServiceDescriptorRegistry();                 
    
    for(int i = 0; i < serviceDescriptors.length; i++) {
      WebserviceDescriptionType serviceDescriptor = serviceDescriptors[i];                            
      serviceDescriptorRegistry.putServiceDescriptor(serviceDescriptor.getWebserviceName().trim(), serviceDescriptor);                                
    }
  }
  
  private void loadInterfaceDefinitionDescriptors(InterfaceDefinitionDescriptionType[] interfaceDefinitionDescriptors) throws Exception {
    if(interfaceDefinitionDescriptors == null || interfaceDefinitionDescriptors.length == 0) {
      return; 
    }
   
    WebServicesDescriptorContext webServicesDescriptorContext = getServiceContext().getWebServicesDescriptorContext();
    InterfaceDefinitionDescriptorRegistry interfaceDefinitionDescriptorRegistry = webServicesDescriptorContext.getInterfaceDefinitionDescriptorRegistry();   
      
    InterfaceDefinitionDescriptionType interfaceDefinitionDescriptor;
    for(int i = 0; i < interfaceDefinitionDescriptors.length; i++) {
      interfaceDefinitionDescriptor = interfaceDefinitionDescriptors[i];
      interfaceDefinitionDescriptorRegistry.putInterfaceDefinitionDescriptor(interfaceDefinitionDescriptor.getInterfaceDefinitionId().trim(), interfaceDefinitionDescriptor);      
    }
  }
    
  private void loadServiceExtDescriptors(com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType[] serviceExtDescriptors) throws Exception {
    if(serviceExtDescriptors == null || serviceExtDescriptors.length == 0) {
      return; 
    }
  
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName); 
    ServiceExtDescriptorRegistry serviceExtDescriptorRegistry = wsApplicationDescriptorContext.getServiceExtDescriptorRegistry();     
    com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType serviceExtDescriptor;     
     
    for(int i = 0; i < serviceExtDescriptors.length; i++) {
      serviceExtDescriptor = serviceExtDescriptors[i]; 
      serviceExtDescriptorRegistry.putServiceExtDescriptor(serviceExtDescriptor.getWebserviceName().trim(), serviceExtDescriptor);         
    }       
  }
  
  protected void loadServiceWSDLFileDescriptors(String applicationName, WebserviceDescriptionType[] serviceDescriptors) {
    if(serviceDescriptors == null || serviceDescriptors.length == 0) {
      return;
    }                   
    
    WebserviceDescriptionType serviceDescriptor; 
    WsdlFileType wsdlFileDescriptor; 
    for(int i = 0; i < serviceDescriptors.length; i++) {
      serviceDescriptor = serviceDescriptors[i];                           
      wsdlFileDescriptor = serviceDescriptor.getWsdlFile(); 
      if(wsdlFileDescriptor != null) {      
        loadWSDLFileDescriptor(applicationName, serviceDescriptor.getWebserviceName().trim(), wsdlFileDescriptor);
      }
    }
  }
  
  protected void loadInterfaceDefWSDLFileDescriptors(InterfaceDefinitionDescriptionType[] interfaceDefinitionDescriptors) {     
    if(interfaceDefinitionDescriptors == null || interfaceDefinitionDescriptors.length == 0) {
      return;   
    }  
    
    ApplicationWSDLContext interfaceDefWSDLContextRegistry = getServiceContext().getGlobalWSDLContext().getInterfaceDefWSDLContextRegistry();
    InterfaceDefinitionDescriptionType interfaceDefinitionDescriptor; 
    WsdlFileType wsdlFileDescriptor; 
    for(int i = 0; i < interfaceDefinitionDescriptors.length; i++) {
      interfaceDefinitionDescriptor = interfaceDefinitionDescriptors[i];
      wsdlFileDescriptor = interfaceDefinitionDescriptor.getWsdlFile();
      if(wsdlFileDescriptor != null) { 
        loadWSDLFileDescriptors(interfaceDefinitionDescriptor.getInterfaceDefinitionId(), interfaceDefWSDLContextRegistry, wsdlFileDescriptor.getWsdl());
      }
    }
  }
         
  protected void loadWSDLFileDescriptor(String applicationName, String serviceName, WsdlFileType wsdlFileDescriptor) {
    WsdlType[] wsdlDescriptors = wsdlFileDescriptor.getWsdl();
    if(wsdlDescriptors == null || wsdlDescriptors.length == 0) {
      return; 
    }
    
    if(!getServiceContext().getGlobalWSDLContext().getApplicationWSDLContexts().containsKey(applicationName)) {
      getServiceContext().getGlobalWSDLContext().getApplicationWSDLContexts().put(applicationName, new ApplicationWSDLContext());
    }
    ApplicationWSDLContext applicationWSDLContext = (ApplicationWSDLContext)getServiceContext().getGlobalWSDLContext().getApplicationWSDLContexts().get(applicationName);
    
    //Hashtable wsdlContexts = getServiceContext().getWsdlContexts(); 
          
    for(int i = 0; i < wsdlDescriptors.length; i++) {
      WsdlType wsdlDescriptor = wsdlDescriptors[i];
      WsdlTypeType type = wsdlDescriptor.getType();           
      WsdlStyleType style = wsdlDescriptor.getStyle();
      //TODO - !!!Type._root;        
      String typeValue = WsdlTypeType._root;  
      String styleValue = WsdlStyleType._defaultTemp; 
      
      if(type != null) {
        typeValue = type.getValue().trim(); 
      }        
      if(style != null) {
        styleValue = style.getValue().trim();
      }                               
      
      WSDLContext targetWSDLContext = null;
      WSDLRegistry targetWSDLRegistry = null;
                   
      if(!applicationWSDLContext.containsWSDLContextKey(typeValue)) {
        applicationWSDLContext.putWSDLContext(typeValue, new WSDLContext());
      }
      targetWSDLContext = applicationWSDLContext.getWSDLContext(typeValue);                             
                        
      if(!targetWSDLContext.containsWSDLRegistryKey(styleValue)) {
        targetWSDLContext.putWSDLRegistry(styleValue, new WSDLRegistry());
      }
      targetWSDLRegistry = (WSDLRegistry)targetWSDLContext.getWSDLRegistry(styleValue);                              

      targetWSDLRegistry.putWsdlDescriptor(serviceName, wsdlDescriptor);
    }                 
  }
  
  protected void loadWSDLFileDescriptors(String id, ApplicationWSDLContext wsdlContextRegistry, WsdlType[] wsdlDescriptors) {    
    if(wsdlDescriptors == null || wsdlDescriptors.length == 0) {
      return; 
    } 
          
    for(int i = 0; i < wsdlDescriptors.length; i++) {
      WsdlType wsdlDescriptor = wsdlDescriptors[i];
      WsdlTypeType type = wsdlDescriptor.getType();           
      WsdlStyleType style = wsdlDescriptor.getStyle();
      //TODO - !!!Type._root;        
      String typeValue = WsdlTypeType._root;  
      String styleValue = WsdlStyleType._defaultTemp; 
      
      if(type != null) {
        typeValue = type.getValue().trim(); 
      }        
      if(style != null) {
        styleValue = style.getValue().trim();
      }                               
      
      WSDLContext targetWSDLContext = null;
      WSDLRegistry targetWSDLRegistry = null;
                   
      if(!wsdlContextRegistry.containsWSDLContextKey(typeValue)) {
        wsdlContextRegistry.putWSDLContext(typeValue, new WSDLContext());
      }
      targetWSDLContext = wsdlContextRegistry.getWSDLContext(typeValue);                             
                        
      if(!targetWSDLContext.containsWSDLRegistryKey(styleValue)) {
        targetWSDLContext.putWSDLRegistry(styleValue, new WSDLRegistry());
      }
      targetWSDLRegistry = (WSDLRegistry)targetWSDLContext.getWSDLRegistry(styleValue);                              

      targetWSDLRegistry.putWsdlDescriptor(id, wsdlDescriptor);
    }                    
  } 
  
  protected void unregisterInterfaceDefWSDLDescriptors(InterfaceDefinitionDescriptionType[] interfaceDefinitionDescriptors) {     
    if(interfaceDefinitionDescriptors == null || interfaceDefinitionDescriptors.length == 0) {
      return;   
    }  
    
    ApplicationWSDLContext interfaceDefWSDLContextRegistry = getServiceContext().getGlobalWSDLContext().getInterfaceDefWSDLContextRegistry();
    InterfaceDefinitionDescriptionType interfaceDefinitionDescriptor; 
    WsdlFileType wsdlFileDescriptor; 
    for(int i = 0; i < interfaceDefinitionDescriptors.length; i++) {
      interfaceDefinitionDescriptor = interfaceDefinitionDescriptors[i];
      wsdlFileDescriptor = interfaceDefinitionDescriptor.getWsdlFile();
      if(wsdlFileDescriptor != null) {
        unregisterWSDLDescriptors(interfaceDefinitionDescriptor.getInterfaceDefinitionId(), interfaceDefWSDLContextRegistry, wsdlFileDescriptor.getWsdl());
      }
    }
  }
  
  protected void unregisterWSDLDescriptors(String id, ApplicationWSDLContext wsdlContextRegistry, WsdlType[] wsdlDescriptors) {        
    if(wsdlDescriptors == null || wsdlDescriptors.length == 0) {
      return; 
    } 
         
    for(int i = 0; i < wsdlDescriptors.length; i++) {
      WsdlType wsdlDescriptor = wsdlDescriptors[i];
      WsdlTypeType type = wsdlDescriptor.getType();           
      WsdlStyleType style = wsdlDescriptor.getStyle();
      //TODO - !!!Type._root;        
      String typeValue = WsdlTypeType._root;  
      String styleValue = WsdlStyleType._defaultTemp; 
      
      if(type != null) {
        typeValue = type.getValue().trim(); 
      } 
             
      if(style != null) {
        styleValue = style.getValue().trim();
      }                                              
                   
      WSDLContext targetWSDLContext = wsdlContextRegistry.getWSDLContext(typeValue);
      if(targetWSDLContext == null) {
        return; 
      }                             
                        
      WSDLRegistry targetWSDLRegistry = (WSDLRegistry)targetWSDLContext.getWSDLRegistry(styleValue);
      if(targetWSDLRegistry == null) {
        return; 
      }
                                    
      targetWSDLRegistry.removeWsdlDescriptor(id);
    }                    
  }  
   
  protected void loadServiceMetaDatas(String applicationName, WebserviceDescriptionType[] serviceDescriptors, ModuleRuntimeData moduleRuntimeData) {
    if(serviceDescriptors == null || serviceDescriptors.length == 0) {
      return;
    }
        
    WSApplicationMetaDataContext applicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);  
    ServiceMetaDataRegistry serviceMetaDataRegistry = applicationMetaDataContext.getServiceMetaDataRegistry();             

    for(int i = 0; i < serviceDescriptors.length; i++) {
      WebserviceDescriptionType serviceDescriptor = serviceDescriptors[i];           
      
      String serviceName = serviceDescriptor.getWebserviceName().trim();  
      ServiceMetaData serviceMetaData = new ServiceMetaData(applicationName, moduleRuntimeData.getModuleName(), serviceName);
      serviceMetaDataRegistry.putServiceMetaData(serviceMetaData.getServiceName(), serviceMetaData);             
    }     
  }
  
  protected void loadServiceMetaDatas(String applicationName, Service services[], ModuleRuntimeData moduleRuntimeData) {
    if(services == null || services.length == 0) {    
      return;
    }
      
    WSApplicationMetaDataContext applicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);
    ServiceMetaDataRegistry serviceMetaDataRegistry = applicationMetaDataContext.getServiceMetaDataRegistry();
  
    String serviceName; 
    Service service; 
    ServiceMetaData serviceMetaData; 
    for(int i = 0; i < services.length; i++) {
      service = services[i];
      serviceName = service.getName().trim();
      serviceMetaData = new ServiceMetaData(applicationName, moduleRuntimeData.getModuleName(), serviceName);
      serviceMetaDataRegistry.putServiceMetaData(serviceMetaData.getServiceName(), serviceMetaData);
    }
  }
    
  protected void loadInterfaceDefMetaDatas() {          
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);    
    if(wsApplicationDescriptorContext == null) {
      return; 
    }
    
    ConfigurationDescriptorRegistry configurationDescriptorRegistry = wsApplicationDescriptorContext.getConfigurationDescriptorRegistry();         
    Enumeration enum1 = configurationDescriptorRegistry.getConfigurationDescriptors().keys(); 
    
    ConfigurationRoot configurationDescriptor;  
    String moduleName; 
    while(enum1.hasMoreElements()) {
      moduleName = (String)enum1.nextElement();
      configurationDescriptor = configurationDescriptorRegistry.getConfigurationDescriptor(moduleName); 
      loadInterfaceDefMetaDatas(moduleName, configurationDescriptor.getDTConfig().getInterfaceDefinition());           
    }             
  }
      
  protected void loadInterfaceDefMetaDatas(String moduleName, InterfaceDefinition[] interfaceDefinitions) {
    if(interfaceDefinitions == null) {
      return; 
    }
     
    InterfaceDefMetaDataRegistry interfaceDefMetaDataRegisry = getServiceContext().getMetaDataContext().getInterfaceDefMetaDataRegistry(); 
    
    InterfaceDefinition interfaceDefinition;  
    String interfaceDefinitionId; 
    InterfaceDefMetaData interfaceDefMetaData; 
    for(int i = 0; i < interfaceDefinitions.length; i++) {
      interfaceDefinition = interfaceDefinitions[i];
      interfaceDefinitionId = interfaceDefinition.getId().trim();  
      interfaceDefMetaData = new InterfaceDefMetaData(applicationName, moduleName, interfaceDefinitionId);
      interfaceDefMetaDataRegisry.putInterfaceDefMetaData(interfaceDefinitionId, interfaceDefMetaData);                 
    }         
  }  
    
  protected void loadAdditionalInterfaceDefMetaDatas() {
    ApplicationConfigurationContext applicationConfigurationContext = (ApplicationConfigurationContext)getServiceContext().getConfigurationContext().getApplicationConfigurationContexts().get(applicationName);
    if(applicationConfigurationContext == null) {
      return; 
    }
    
    InterfaceDefMetaDataRegistry interfaceDefMetaDataRegistry = getServiceContext().getMetaDataContext().getInterfaceDefMetaDataRegistry();  
    
    ServiceRegistry serviceRegistry = applicationConfigurationContext.getServiceRegistry();
    Enumeration enum1 = serviceRegistry.getServices().keys(); 
    
    Service service; 
    String serviceName; 
    String moduleName;  
    BindingData[] bindingDatas;      
    InterfaceDefMetaData interfaceDefMetaData; 
    while(enum1.hasMoreElements()) {
      serviceName = (String)enum1.nextElement();
      service = serviceRegistry.getService(serviceName);  
      
      bindingDatas = service.getServiceData().getBindingData();
      if(bindingDatas != null && bindingDatas.length != 0) {  
        for(int i = 0; i < bindingDatas.length; i++) {
          interfaceDefMetaData = interfaceDefMetaDataRegistry.getInterfaceDefMetaData(bindingDatas[i].getInterfaceId().trim()); 
          if(interfaceDefMetaData != null) {
            interfaceDefMetaData.addService(serviceName);
          }     
        }           
      } 
    }       
  }
    
  protected void loadBindingDataMetaDatas(String applicationName, Service service, BindingData bindingDatas[]) {
    if(bindingDatas == null || bindingDatas.length == 0) {
      return;
    }        
    
    WebServicesMetaDataContext metaDataContext = getServiceContext().getMetaDataContext();
    BindingDataMetaDataRegistry globalBindingDataMetaDataRegistry = metaDataContext.getGlobalBindingDataMetaDataRegistry();
    WSApplicationMetaDataContext applicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);
    Hashtable bindingDataMetaDataRegistries = applicationMetaDataContext.getBindingDataMetaDataRegistries();    
                 
    String serviceName = service.getName().trim();
    String serviceContextRoot = service.getServiceData().getContextRoot().trim(); 
    if(!serviceContextRoot.equals("") && !serviceContextRoot.startsWith("/")) {
      serviceContextRoot = "/" + serviceContextRoot; 
    } 
                   
    BindingDataMetaDataRegistry bindingDataMetaDataRegistry = new BindingDataMetaDataRegistry();        
    BindingData bindingData; 
    String bindingDataName; 
    String bindingDataUrl; 
    BindingDataMetaData bindingDataMetaData; 
    for(int i = 0; i < bindingDatas.length; i++) {
      bindingData = bindingDatas[i];            
      bindingDataName = bindingData.getName().trim();
      bindingDataUrl = bindingData.getUrl().trim();
      bindingDataUrl = bindingData.getUrl().trim();            
      if(!bindingDataUrl.startsWith("/")) {
        bindingDataUrl = "/" + bindingDataUrl;
      }
      bindingDataMetaData = new BindingDataMetaData(applicationName, serviceName, bindingDataName);      
      globalBindingDataMetaDataRegistry.putBindingDataMetaData(serviceContextRoot + bindingDataUrl, bindingDataMetaData);            
      bindingDataMetaDataRegistry.putBindingDataMetaData(bindingDataName, bindingDataMetaData);
    }
   
    bindingDataMetaDataRegistries.put(serviceName, bindingDataMetaDataRegistry);             
  }  
    
  protected void uploadMetaData(Configuration configuration) throws ConfigurationException {    
    if(!getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().containsKey(applicationName)) {
      return; 
    }
    
    WSApplicationMetaDataContext applicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = applicationMetaDataContext.getModuleRuntimeDataRegistry();   
    uploadMetaData(configuration, moduleRuntimeDataRegistry);          
  }
         
  private void unregisterConfigurationContext() {         
    unregisterInterfaceDefinitionDescriptors();     
    unregisterServiceConfigurationDescriptors(); 
  }  
  
  private void unregisterInterfaceDefinitionDescriptors() {                                          
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);
    if(wsApplicationDescriptorContext == null) {
      return; 
    }
    
    ConfigurationDescriptorRegistry configurationDescriptorRegistry = wsApplicationDescriptorContext.getConfigurationDescriptorRegistry(); 
    Enumeration enum1 = configurationDescriptorRegistry.getConfigurationDescriptors().elements(); 
    ConfigurationRoot configurationDescriptor; 
    while(enum1.hasMoreElements()) {     
      configurationDescriptor = (ConfigurationRoot)enum1.nextElement(); 
      unregisterInterfaceDefinitionDescriptors(configurationDescriptor.getDTConfig().getInterfaceDefinition());                       
    }    
  }
  
  protected void unregisterInterfaceDefMetaDatas() {          
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);    
    if(wsApplicationDescriptorContext == null) {
      return; 
    }
    
    ConfigurationDescriptorRegistry configurationDescriptorRegistry = wsApplicationDescriptorContext.getConfigurationDescriptorRegistry();         
    Enumeration enum1 = configurationDescriptorRegistry.getConfigurationDescriptors().elements(); 
    
    ConfigurationRoot configurationDescriptor;  
    String moduleName; 
    while(enum1.hasMoreElements()) {      
      configurationDescriptor = (ConfigurationRoot)enum1.nextElement(); 
      unregisterInterfaceDefMetaDatas(configurationDescriptor.getDTConfig().getInterfaceDefinition());           
    }             
  }
      
  protected void unregisterInterfaceDefMetaDatas(InterfaceDefinition[] interfaceDefinitions) {
    if(interfaceDefinitions == null) {
      return; 
    }
     
    InterfaceDefMetaDataRegistry interfaceDefMetaDataRegisry = getServiceContext().getMetaDataContext().getInterfaceDefMetaDataRegistry(); 
      
    String interfaceDefinitionId; 
    InterfaceDefMetaData interfaceDefMetaData; 
    for(int i = 0; i < interfaceDefinitions.length; i++) {      
      interfaceDefMetaDataRegisry.removeInterfaceDefMetaData(interfaceDefinitions[i].getId().trim());                       
    }         
  }  
    
  private void unregisterMetaDataContext() {   
    unregisterInterfaceDefMetaDatas(); 
    unregisterBindingDataMetaDatas(); 
    getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().remove(applicationName);     
  }
   
  private void unregisterBindingDataMetaDatas() {
    ConfigurationContext configurationContext = serviceContext.getConfigurationContext(); 
    if(!configurationContext.getApplicationConfigurationContexts().containsKey(applicationName)) {
      return; 
    }
      
    ApplicationConfigurationContext applicationConfigurationContext = (ApplicationConfigurationContext)configurationContext.getApplicationConfigurationContexts().get(applicationName);
    ServiceRegistry serviceRegistry = applicationConfigurationContext.getServiceRegistry();               
    BindingDataMetaDataRegistry bindingDataMetaDataRegistry = getServiceContext().getMetaDataContext().getGlobalBindingDataMetaDataRegistry();   
    
    Enumeration enum1 = serviceRegistry.getServices().keys();        
    while(enum1.hasMoreElements()) {
      String serviceName = (String)enum1.nextElement();      
      Service service = (Service)serviceRegistry.getService(serviceName);
      unregisterGlobalBindingDataMetaData(service, bindingDataMetaDataRegistry);
    }
  }  
     
  public void unregisterBindingDataMetaDataGlobal(Service service, BindingDataMetaDataRegistry bindingDataMetaDataRegistry) {
    String serviceContextRoot = service.getServiceData().getContextRoot().trim();
    if(!serviceContextRoot.equals("") && !serviceContextRoot.startsWith("/")) {
      serviceContextRoot = "/" + serviceContextRoot; 
    } 
            
    BindingData[] bindingDatas = service.getServiceData().getBindingData(); 
    if(bindingDatas != null && bindingDatas.length !=0) {
      for(int i = 0; i < bindingDatas.length; i++) {        
        BindingData bindingData = bindingDatas[i];
        String bindingDataUrl = bindingData.getUrl().trim();            
        if(!bindingDataUrl.startsWith("/")) {
          bindingDataUrl = "/" + bindingDataUrl;
        }
        bindingDataMetaDataRegistry.removeBindingDataMetaData(serviceContextRoot + bindingDataUrl);
      }
    }      
  }  
         
  private void unregisterMappingContext() {    
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);
    if(wsApplicationDescriptorContext == null) {
      return; 
    }
    
    MappingDescriptorRegistry mappingDescriptorRegistry = wsApplicationDescriptorContext.getMappingDescriptorRegistry();
    Enumeration enum1 = mappingDescriptorRegistry.getMappingDescriptors().elements(); 
    
    MappingRules mappingDescriptor;
    while(enum1.hasMoreElements()) {
      mappingDescriptor = (MappingRules)enum1.nextElement(); 
      unregisterInterfaceMappingDescriptors(mappingDescriptor.getInterface());
      unregisterServiceMappingDescriptors(mappingDescriptor.getService()); 
    }
  }
   
  private void unregisterWebServicesDescriptorContext() {
    unregisterInterfaceDefinitionDescriptors2();
    getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().remove(applicationName);    
  }
  
  private void unregisterInterfaceDefinitionDescriptors2() {
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);
    if(wsApplicationDescriptorContext == null) {
      return; 
    }
    
    WebServicesJ2EEEngineDescriptorRegistry webServicesJ2EEEngineDescriptorRegistry = wsApplicationDescriptorContext.getWebServicesJ2EEEngineDescriptorRegistry();
    
    Enumeration enum1 = webServicesJ2EEEngineDescriptorRegistry.getWebServicesJ2EEEngineDescriptors().elements();
    WebservicesType webServicesJ2EEEngineDescriptor; 
    while(enum1.hasMoreElements()) {
      webServicesJ2EEEngineDescriptor = (WebservicesType)enum1.nextElement(); 
      unregisterInterfaceDefinitionDescriptors2(webServicesJ2EEEngineDescriptor.getInterfaceDefinitionDescription());
    }        
  }
  
  private void unregisterInterfaceDefinitionDescriptors2(InterfaceDefinitionDescriptionType[] interfaceDefinitionDescriptors) {
    if(interfaceDefinitionDescriptors == null || interfaceDefinitionDescriptors.length == 0) {
      return;   
    }
    
    InterfaceDefinitionDescriptorRegistry interfaceDefinitionDescriptorRegistry = getServiceContext().getWebServicesDescriptorContext().getInterfaceDefinitionDescriptorRegistry(); 
    for(int i = 0; i < interfaceDefinitionDescriptors.length; i++) {
      interfaceDefinitionDescriptorRegistry.removeInterfaceDefinitionDescriptor(interfaceDefinitionDescriptors[i].getInterfaceDefinitionId().trim());
    }       
  }
  
  private void unregisterGlobalWSDLContext() {
    unregisterInterfaceDefWSDLContexts();         
    getServiceContext().getGlobalWSDLContext().getApplicationWSDLContexts().remove(applicationName);
  }
  
  private void unregisterInterfaceDefWSDLContexts() {    
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName); 
    if(wsApplicationDescriptorContext == null) {
      return; 
    }
    
    WebServicesJ2EEEngineDescriptorRegistry webServicesJ2EEEngineDescriptorRegistry = wsApplicationDescriptorContext.getWebServicesJ2EEEngineDescriptorRegistry();
    Enumeration enum1 = webServicesJ2EEEngineDescriptorRegistry.getWebServicesJ2EEEngineDescriptors().elements(); 
    
    WebservicesType webServicesJ2EEEngineDescriptor; 
    while(enum1.hasMoreElements()) {
      webServicesJ2EEEngineDescriptor = (WebservicesType)enum1.nextElement();
      unregisterInterfaceDefWSDLDescriptors(webServicesJ2EEEngineDescriptor.getInterfaceDefinitionDescription()); 
    }      
  } 
  
  protected void unregister() {       
    unregisterMetaDataContext(); 
    unregisterConfigurationContext();
    unregisterMappingContext();                  
    unregisterGlobalWSDLContext(); 
    unregisterWebServicesDescriptorContext();
    
    getServiceContext().getApplicationServiceTypeMappingContexts().remove(applicationName);              
  }    

  protected void saveWebServicesJ2EEEngineDescriptors(String applicationName) throws TypeMappingException, IOException {
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName); 
    if(wsApplicationDescriptorContext == null) {
      return; 
    }
  
    WSApplicationMetaDataContext wsApplicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = wsApplicationMetaDataContext.getModuleRuntimeDataRegistry();
    WebServicesJ2EEEngineDescriptorRegistry webServicesJ2EEEngineDescriptorRegistry = wsApplicationDescriptorContext.getWebServicesJ2EEEngineDescriptorRegistry();     
     
    Enumeration enumer = webServicesJ2EEEngineDescriptorRegistry.getWebServicesJ2EEEngineDescriptors().keys();    
    String moduleName; 
    WebservicesType webServicesJ2EEEngineDescriptor; 
    ModuleRuntimeData moduleRuntimeData; 
    while(enumer.hasMoreElements()) {        
      moduleName = (String)enumer.nextElement();      
      webServicesJ2EEEngineDescriptor = (WebservicesType)webServicesJ2EEEngineDescriptorRegistry.getWebServicesJ2EEEngineDescriptor(moduleName);
      moduleRuntimeData = (ModuleRuntimeData)moduleRuntimeDataRegistry.getModuleRuntimeData(moduleName);     
      WebServicesJ2EEEngineFactory.save(webServicesJ2EEEngineDescriptor, moduleRuntimeData.getFilePathName(META_INF + "/" + WEBSERVICES_J2EE_ENGINE_DESCRIPTOR));                                 
    }       
  }  
  
  protected String[] getServiceFilesForClassLoader(WebserviceDescriptionType serviceDescriptor, ModuleRuntimeData moduleRuntimeData) {
    TypeMappingFileType[] typesArchiveFileDescriptors = serviceDescriptor.getTypesArchiveFile(); 
    if(typesArchiveFileDescriptors == null) {
      return new String[0];
    }    
    
    String[] filesForClassLoader = new String[typesArchiveFileDescriptors.length];
    for(int i = 0; i < typesArchiveFileDescriptors.length; i++) {
      TypeMappingFileType typesArchiveFileDescriptor = typesArchiveFileDescriptors[i];
      filesForClassLoader[i] = moduleRuntimeData.getFilePathName(typesArchiveFileDescriptor.get_value().trim());
    }       
    
    return filesForClassLoader; 
  } 
  
  protected void upload() throws WSDeploymentException {
    if(!getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().containsKey(applicationName)) {
      return; 
    }
    
    WSApplicationMetaDataContext wsApplicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName); 
    
    try {          
      String configurationName = new File(webServicesContainerDir).getName();
      Configuration configuration = appConfiguration.createSubConfiguration(configurationName);
      uploadMetaData(configuration);
      uploadBackUp(configuration, wsApplicationMetaDataContext.getModuleRuntimeDataRegistry());
      Set skippedChildDirs = new HashSet(); 
      skippedChildDirs.add(WSApplicationMetaDataContext.BACKUP); 
      uploadDirectory(new File(webServicesContainerDir), configuration, skippedChildDirs);
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_D_EXCEPTION_UPLOAD, new Object[]{applicationName}, e);
    }       
  }  
  
  protected Configuration createApplicationConfiguration(String applicationName, ConfigurationHandler configurationHandler) throws ConfigurationException {
    Configuration appsConfiguration = null;
        
    try {    	
      appsConfiguration = configurationHandler.openConfiguration(OfflineBaseServiceContext.APPS_CONFIGURATION_PATH, ConfigurationHandler.WRITE_ACCESS);	   
    } catch(NameNotFoundException e) {
      // $JL-EXC$     	
    } catch(ConfigurationLockedException e) {		
      throw e;    
    } 
		
	try {
	  if(appsConfiguration == null) {
	    appsConfiguration = createSubConfiguration(configurationHandler, OfflineBaseServiceContext.APPS_CONFIGURATION_PATH); 	
	  }
	} catch(ConfigurationLockedException e) {
	  //TODO 	
	  throw e; 
	}
	           
	deleteSubConfiguration(appsConfiguration, applicationName); 
	return createSubConfiguration(appsConfiguration, applicationName); 
  }
  
  protected void uploadWSData(String applicationName, Configuration appConfiguration) throws ConfigurationException {        
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);
    if(wsApplicationDescriptorContext == null) {
      return; 
    }
    
	ConfigurationDescriptorRegistry configurationDescriptorRegistry = wsApplicationDescriptorContext.getConfigurationDescriptorRegistry();
	
    Iterator<Entry<String, ConfigurationRoot>> iterator = configurationDescriptorRegistry.getConfigurationDescriptors().entrySet().iterator(); 
	Entry<String, ConfigurationRoot> entry; 
    String moduleName; 
    Configuration moduleConfiguration = null; 
	Configuration wsConfiguration; 	
    while(iterator.hasNext()) {
	  entry = iterator.next();
      moduleName = entry.getKey(); 
	  try {
	    moduleConfiguration = appConfiguration.getSubConfiguration(moduleName);
	  } catch(NameNotFoundException e) {
	    // $JL-EXC$  	  
	  }
	  
	  if(moduleConfiguration == null) {
	    moduleConfiguration = appConfiguration.createSubConfiguration(moduleName);
	  }	  
	  
	  wsConfiguration = moduleConfiguration.createSubConfiguration(OfflineBaseServiceContext.WS_CONFIGURATION_NAME);	  
	  wsConfiguration.addConfigEntry(OfflineBaseServiceContext.INTERFACE_DEFINITIONS_NAME, getInterfaceDefinitionIds(entry.getValue().getDTConfig().getInterfaceDefinition()));	  
	} 
  } 
  
  private String[] getInterfaceDefinitionIds(InterfaceDefinition[] interfaceDefinitions) {
    if(interfaceDefinitions == null || interfaceDefinitions.length == 0) {
      return new String[0];	
    }	  
    
    String[] interfaceDefinitionIds = new String[interfaceDefinitions.length];
    int i = 0; 
    for(InterfaceDefinition interfaceDefinition: interfaceDefinitions) {
      interfaceDefinitionIds[i++] = interfaceDefinition.getId(); 	
    }
    
    return interfaceDefinitionIds; 
  }
  
}
