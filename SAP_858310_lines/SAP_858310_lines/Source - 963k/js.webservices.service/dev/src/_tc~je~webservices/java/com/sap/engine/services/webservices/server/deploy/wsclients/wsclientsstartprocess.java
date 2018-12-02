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

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import com.sap.bc.proj.jstartup.sadm.ShmAccessPoint;
import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.services.webservices.espbase.client.bindings.DynamicServiceImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.SerializableDynamicServiceImpl;
import com.sap.engine.services.webservices.espbase.client.jaxws.cts.CTSProvider;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.mappings.ImplementationLink;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;
import com.sap.engine.services.webservices.jaxws.handlers.PredefinedCTSHandlerResolverUtil;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.SchemaTypeType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ServiceRefGroupDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.TypeMappingFileType;
import com.sap.engine.services.webservices.server.deploy.wsclients.notification.WSClientsStartNotificationHandler;
import com.sap.engine.services.webservices.server.container.descriptors.ConfigurationDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.descriptors.MappingDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.mapping.ServiceMappingRegistry;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeData;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeDataRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.ServiceRefContext;
import com.sap.engine.services.webservices.server.container.wsclients.descriptors.ServiceRefGroupDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.descriptors.ServiceRefGroupExtDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.descriptors.WSClientsApplicationDescriptorContext;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.ServiceRefGroupMetaData;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.ServiceRefGroupMetaDataRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.WSClientsApplicationMetaDataContext;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * 
 * Title: WSClientsStartProcess 
 * Description: WSClientsStartProcess
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class WSClientsStartProcess extends WSClientsAbstractStartProcess {  
  
  private ArrayList<String> warnings; 
	
  public WSClientsStartProcess(String applicationName, String webServicesContainerDir, String webServicesContainerTempDir, Configuration appConfiguration, ServiceRefContext serviceContext, ClassLoader appLoader) {
    this.applicationName = applicationName; 
    this.webServicesContainerDir = webServicesContainerDir; 
    this.webServicesContainerTempDir = webServicesContainerTempDir;
    this.serviceContext = serviceContext; 
    this.appConfiguration = appConfiguration;  
    this.appLoader = appLoader;
    this.wsClientsDNotificationHandler = new WSClientsStartNotificationHandler(applicationName, WSContainer.getRuntimeProcessingEnv()); 
  } 
  
  public ArrayList<String> getWarnings() {
    if(this.warnings == null) {
      this.warnings = new ArrayList<String>(); 	
    }	  
    
    return this.warnings; 
  }
  
  public void preProcess() throws WSDeploymentException, WSWarningException {
	  
  }
  
  public void init() throws WSDeploymentException {    
    loadWSClientsJ2EEEngineDescriptorsInitially();           
  }
  
  public void execute() throws WSDeploymentException {    
    loadWSClientsJ2EEEngineDescriptors();      
    bindServiceImplInstances();     
  }

  public void finish() throws WSDeploymentException {
    try {
      wsClientsDNotificationHandler.onExecutePhase();
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), getWarnings());    	
    }
  }
  
  public void postProcess() throws WSDeploymentException, WSWarningException {
	  
  }
  
  public void notifyProcess() throws WSWarningException {
	  
  }
  
  public void commitProcess() throws WSWarningException {
    wsClientsDNotificationHandler.onCommitPhase();
  }
    
  public void rollbackProcess() throws WSWarningException {
    ArrayList<String> warnings = new ArrayList<String>(); 
	
    try {
      wsClientsDNotificationHandler.onRollbackPhase();
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);       
    }
   
	try {    
      unregister();
    } catch(Exception e) {
      //TODO - add message
      StringWriter strWriter = new StringWriter(); 
      e.printStackTrace(new PrintWriter(strWriter));      
      warnings.add(strWriter.toString());   
    } 
    
    if(warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.setWarning(warnings.toArray(new String[warnings.size()])); 
      throw e; 
    }
  }
  
  private void bindServiceImplInstances() throws WSDeploymentException {     
    WSClientsApplicationDescriptorContext wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName); 
    if(wsClientsApplicationDescriptorContext == null) {
      return; 
    }
    
    ServiceRefGroupDescriptorRegistry serviceRefGroupDescriptorRegistry = wsClientsApplicationDescriptorContext.getServiceRefGroupDescriptorRegistry();
    ServiceRefGroupExtDescriptorRegistry serviceRefGroupExtDescriptorRegistry = wsClientsApplicationDescriptorContext.getServiceRefGroupExtDescriptorRegistry();
    ConfigurationDescriptorRegistry configurationDescriptorRegistry = wsClientsApplicationDescriptorContext.getConfigurationDescriptorRegistry(); 
    MappingDescriptorRegistry mappingDescriptorRegistry = wsClientsApplicationDescriptorContext.getMappingDescriptorRegistry();
    
    WSClientsApplicationMetaDataContext wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)getServiceContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().get(applicationName);       
    ServiceRefGroupMetaDataRegistry serviceRefGroupMetaDataRegistry = wsClientsApplicationMetaDataContext.getServiceRefGroupMetaDataRegistry(); 
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = wsClientsApplicationMetaDataContext.getModuleRuntimeDataRegistry(); 
       
    Enumeration enumer = serviceRefGroupDescriptorRegistry.getServiceRefGroupDescriptors().elements();
    String serviceRefGroupName; 
    ServiceRefGroupDescriptionType serviceRefGroupDescriptor; 
    com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ext.ServiceRefGroupDescriptionType serviceRefGroupExtDescriptor; 
    ConfigurationRoot configurationDescriptor;
    MappingRules mappingDescriptor; 
    ServiceRefGroupMetaData serviceRefGroupMetaData; 
    ModuleRuntimeData moduleRuntimeData;      
    try {
      Context context = new InitialContext();
      Context proxiesContext = (Context)context.lookup(PROXIES_CONTEXT);       
      while(enumer.hasMoreElements()) {
    	serviceRefGroupDescriptor = (ServiceRefGroupDescriptionType)enumer.nextElement(); 
        serviceRefGroupName = serviceRefGroupDescriptor.getServiceRefGroupName().trim();
        serviceRefGroupExtDescriptor = serviceRefGroupExtDescriptorRegistry.getServiceRefGroupExtDescriptor(serviceRefGroupName); 
        configurationDescriptor = configurationDescriptorRegistry.getConfigurationDescriptor(serviceRefGroupName);
        mappingDescriptor = mappingDescriptorRegistry.getMappingDescriptor(serviceRefGroupName); 
        serviceRefGroupMetaData = serviceRefGroupMetaDataRegistry.getServiceRefGroupMetaData(serviceRefGroupName);
        moduleRuntimeData = moduleRuntimeDataRegistry.getModuleRuntimeData(serviceRefGroupMetaData.getModuleName());
        if(!containsJAXWSService(configurationDescriptor.getRTConfig().getService(), appLoader)) {          
          bindServiceImplInstances(proxiesContext, serviceRefGroupDescriptor, configurationDescriptor, mappingDescriptor, loadTypeMappingRegistry(moduleRuntimeData, serviceRefGroupDescriptor));
        } else {
          bindJAXWSServiceImplInstances(proxiesContext, moduleRuntimeData, serviceRefGroupDescriptor, serviceRefGroupExtDescriptor, configurationDescriptor); 
        }
      }
    } catch(WSDeploymentException e) {
      throw e; 
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_CL_D_EXCEPTION_BIND_SI_IMPL, new Object[]{applicationName}, e);    
    }       
  }
  
  private void bindServiceImplInstances(Context proxiesContext, ServiceRefGroupDescriptionType serviceRefGroupDescriptor, ConfigurationRoot configurationDescriptor, MappingRules mappingDescriptor, TypeMappingRegistry typeMappingRegistry) throws WSDeploymentException {
    Service[] services = configurationDescriptor.getRTConfig().getService();
    if(services == null || services.length == 0) {
      return; 
    }  
    
    boolean hasWSDL = serviceRefGroupDescriptor.getWsdlFile() != null;
    
    ServiceMappingRegistry serviceMappingRegistry = getServiceContext().getMappingContext().getServiceMappingRegistry();
        
    QName serviceQName; 
    Service service;  
    ServiceMapping serviceMapping;
    Class serviceImplClass; 
    Object serviceImplInstance;  
    try {    
      for(int i = 0; i < services.length; i++) {
        service = services[i];
        serviceMapping = serviceMappingRegistry.getServiceMapping(service.getServiceMappingId().trim());                
        if(hasWSDL) {
          serviceImplClass = appLoader.loadClass(serviceMapping.getImplementationLink().getSIImplName().trim());          
          serviceQName = new QName(service.getServiceData().getNamespace().trim(), service.getServiceData().getName().trim());        
          serviceImplInstance = serviceImplClass.newInstance(); 
          if(serviceImplInstance instanceof DynamicServiceImpl) { //this is NY client
            ((DynamicServiceImpl)serviceImplInstance).init(serviceQName, typeMappingRegistry, mappingDescriptor, configurationDescriptor, appLoader, applicationName);
            ((DynamicServiceImpl)serviceImplInstance).setServiceMode(DynamicServiceImpl.JAXRPC_MODE);
            ((DynamicServiceImpl)serviceImplInstance).isDeployableClient = true;
            
            ShmAccessPoint[] aps = ShmAccessPoint.getAllAccessPoints(ShmAccessPoint.PID_HTTP);
            int port = -1;
            if (aps.length > 0) {
              port = aps[0].getPort();
            }
            ((DynamicServiceImpl)serviceImplInstance)._setProperty(DynamicServiceImpl.HTTP_PORT_PROP_NAME, Integer.toString(port));
          }   
        } else {        
          serviceImplClass = SerializableDynamicServiceImpl.class; 
          serviceImplInstance = serviceImplClass.newInstance(); 
        }                                     
        WSUtil.rebind(proxiesContext, applicationName + "/" + serviceMapping.getImplementationLink().getProperty(ImplementationLink.SERVICE_REF_JNDI_NAME), serviceImplInstance);
      }
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_CL_D_EXCEPTION_BIND_SI_IMPL_2, new Object[]{applicationName, serviceRefGroupDescriptor.getServiceRefGroupName().trim()}, e); 
    }    
  }
  
  private void bindJAXWSServiceImplInstances(Context proxiesContext, ModuleRuntimeData moduleRuntimeData, ServiceRefGroupDescriptionType serviceRefGroupDescriptor, com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ext.ServiceRefGroupDescriptionType serviceRefGroupExtDescriptor, ConfigurationRoot configurationDescriptor) throws WSDeploymentException {    
    Service[] services = configurationDescriptor.getRTConfig().getService(); 
	if(services == null || services.length == 0) {
      return; 	
    }	
    
	String wsdlFilePath = null;                  
    if(serviceRefGroupExtDescriptor != null && serviceRefGroupExtDescriptor.getWsdlFile() != null) {
      //wsdlFilePath = serviceRefGroupExtDescriptor.getWsdlFile().trim();
      wsdlFilePath = null; 
    } else {    	 
      wsdlFilePath = (moduleRuntimeData.getModuleDir() + "/" + serviceRefGroupDescriptor.getWsdlFile().trim()).replace('\\', '/');
      if(wsdlFilePath.startsWith("/")) {
        wsdlFilePath = "file:" + wsdlFilePath;
      } else {
  	    wsdlFilePath = "file:/" + wsdlFilePath;
      }
    }     
		
    ServiceMappingRegistry serviceMappingRegistry = getServiceContext().getMappingContext().getServiceMappingRegistry(); 
        
    ServiceMapping serviceMapping; 
    Class serviceImplClass; 
    Object serviceImplInstance;
    ClassLoader contextClassLoader = null; 
    try {      	                      
      contextClassLoader = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(appLoader);      
      for(Service service: services) {
		serviceMapping = serviceMappingRegistry.getServiceMapping(service.getServiceMappingId().trim());			
      com.sap.engine.services.webservices.espbase.client.jaxws.cts.ServiceRefMetaData serviceRefMetaData = new com.sap.engine.services.webservices.espbase.client.jaxws.cts.ServiceRefMetaData(applicationName, serviceRefGroupDescriptor.getServiceRefGroupName().trim(), service.getName().trim(), configurationDescriptor, wsdlFilePath, new QName(service.getServiceData().getNamespace().trim(), service.getServiceData().getName().trim()));
      serviceRefMetaData.setServiceJndiName(applicationName + "/" + serviceMapping.getImplementationLink().getProperty(ImplementationLink.SERVICE_REF_JNDI_NAME));
      CTSProvider.SERVICE_REF_META_DATA.set(serviceRefMetaData);
		CTSProvider.BRIDGE.set(serviceMapping.getImplementationLink().getProperty(ImplementationLink.SERVICE_REF_JNDI_NAME));		
		if(serviceMapping.getSIName().trim().equals("javax.xml.ws.Service")) {
		  continue; 	
		}
	    serviceImplClass = appLoader.loadClass(serviceMapping.getSIName().trim()); 	    
	    serviceImplInstance = serviceImplClass.newInstance();
	    //TODO - check if necessary for ports 
	    PredefinedCTSHandlerResolverUtil.setHandlerResolverIfNeeded(serviceImplInstance); 
	    WSUtil.rebind(proxiesContext, applicationName + "/" + serviceMapping.getImplementationLink().getProperty(ImplementationLink.SERVICE_REF_JNDI_NAME), serviceImplInstance);             
	  }	  
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      //TODO - new message
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_CL_D_EXCEPTION_BIND_SI_IMPL_2, new Object[]{applicationName, serviceRefGroupDescriptor.getServiceRefGroupName().trim()}, e); 
    } finally {     
      CTSProvider.SERVICE_REF_META_DATA.remove();      
      CTSProvider.BRIDGE.remove();           
	  if(contextClassLoader != null) {
	    Thread.currentThread().setContextClassLoader(contextClassLoader);	  
	  }      
    }	 
  }
  
  private TypeMappingRegistry loadTypeMappingRegistry(ModuleRuntimeData moduleRuntimeData, ServiceRefGroupDescriptionType serviceRefGroupDescriptor) throws Exception {    
    TypeMappingFileType[] typeMappingFileDescriptors = serviceRefGroupDescriptor.getTypeMappingFile();                        
    if(typeMappingFileDescriptors == null) {
      return null;      
    }
    
    TypeMappingFileType typeMappingFileDescriptor; 
    String typeMappinFileRelPath; 
    int cutIndex; 
    TypeMappingRegistryImpl typeMappingRegistry = null;
    InputStream typeMappingIn = null; 
    try { 
      for(int i = 0; i < typeMappingFileDescriptors.length; i++) {
        typeMappingFileDescriptor = typeMappingFileDescriptors[i];
        if(typeMappingFileDescriptor.getType().getValue().trim().equals(SchemaTypeType._framework)) {
          typeMappingRegistry = new TypeMappingRegistryImpl();
          typeMappinFileRelPath = typeMappingFileDescriptor.get_value(); 
          cutIndex = typeMappinFileRelPath.indexOf("#"); 
          if(cutIndex!= -1) {
            typeMappinFileRelPath = typeMappinFileRelPath.substring(typeMappinFileRelPath.indexOf("#") + 1);
            typeMappingIn = appLoader.getResourceAsStream(typeMappinFileRelPath);
            typeMappingRegistry.fromXML(typeMappingIn, appLoader);             
          } else {
            typeMappingRegistry.fromXML(moduleRuntimeData.getFilePathName(typeMappinFileRelPath), appLoader);  
          }
          break; 
        }
      }
    } catch(Exception e) {
       //TODO 
       e.printStackTrace();  
       throw e; 
    } finally {
      try {
        if(typeMappingIn != null) {        
          typeMappingIn.close();
        } 
      } catch(Exception e) {
        // $JL-EXC$
      } 
    }
           
    return typeMappingRegistry; 
  }  
  
}
