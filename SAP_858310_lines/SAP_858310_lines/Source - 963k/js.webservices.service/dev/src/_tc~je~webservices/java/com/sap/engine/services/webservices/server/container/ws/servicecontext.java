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

package com.sap.engine.services.webservices.server.container.ws;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import com.sap.bc.proj.jstartup.sadm.ShmAccessPoint;
import com.sap.engine.interfaces.webservices.server.ServiceContextAccessHandler;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.server.ContainerEnvironment;
import com.sap.engine.services.webservices.espbase.server.MetaDataAccessor;
import com.sap.engine.services.webservices.server.container.BaseServiceContext;
import com.sap.engine.services.webservices.server.container.configuration.ApplicationConfigurationContext;
import com.sap.engine.services.webservices.server.container.configuration.ServiceRegistry;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeData;
import com.sap.engine.services.webservices.server.container.metadata.service.BindingDataMetaData;
import com.sap.engine.services.webservices.server.container.ws.descriptors.ServiceDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.ws.descriptors.WSApplicationDescriptorContext;
import com.sap.engine.services.webservices.server.container.ws.descriptors.WebServicesDescriptorContext;
import com.sap.engine.services.webservices.server.container.ws.metaData.ServiceMetaData;
import com.sap.engine.services.webservices.server.container.ws.metaData.WSApplicationMetaDataContext;
import com.sap.engine.services.webservices.server.container.ws.metaData.WebServicesMetaDataContext;
import com.sap.engine.services.webservices.server.container.ws.wsdl.ApplicationWSDLContext;
import com.sap.engine.services.webservices.server.container.ws.wsdl.GlobalWSDLContext;
import com.sap.engine.services.webservices.server.container.ws.wsdl.WSDLContext;
import com.sap.engine.services.webservices.server.container.ws.wsdl.WSDLRegistry;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlFileType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlType;
import com.sap.security.api.IUser;
import com.sap.security.api.UMFactory;

/**
 * Title: ServiceContext
 * Description: ServiceContext is a context for service data
 *
 * @author Dimitrina Stoyanova 
 * @version
 */
public class ServiceContext extends BaseServiceContext implements ServiceContextAccessHandler, ContainerEnvironment {

  private WebServicesDescriptorContext webServicesDescriptorContext;
  private WebServicesMetaDataContext metaDataContext;
  private GlobalWSDLContext globalWSDLContext;
  private Hashtable<String, ApplicationServiceTypeMappingContext> applicationServiceTypeMappingContexts;  
  
  public ServiceContext() {
	  
  }
  
  /**
   * @return - web services descriptor context
   */
  public synchronized WebServicesDescriptorContext getWebServicesDescriptorContext() {
    if(webServicesDescriptorContext == null) {
      webServicesDescriptorContext = new WebServicesDescriptorContext();
    }
    return webServicesDescriptorContext;
  }

  /**
   * @return - meta data context
   */
  public synchronized WebServicesMetaDataContext getMetaDataContext() {
    if(metaDataContext == null) {
      metaDataContext = new WebServicesMetaDataContext();
    }
    return metaDataContext;
  }

  /**
   * @return - global wsdl context
   */
  public synchronized GlobalWSDLContext getGlobalWSDLContext() {
    if(globalWSDLContext == null) {
      globalWSDLContext = new GlobalWSDLContext();
    }
    return globalWSDLContext;
  }

  /**
   * @return - a hashtable of service type mapping registries
   */
  public synchronized Hashtable getApplicationServiceTypeMappingContexts() {
    if(applicationServiceTypeMappingContexts == null) {
      applicationServiceTypeMappingContexts = new Hashtable();
    }
    return applicationServiceTypeMappingContexts;
  }
  
  public BindingDataMetaData getBindingDataMetaData(String bindingDataURL) {	
    BindingDataMetaData bindingDataMetaData = getMetaDataContext().getGlobalBindingDataMetaDataRegistry().getBindingDataMetaData(bindingDataURL);
    
	try {
      if(bindingDataMetaData == null) {	  
	    bindingDataMetaData = getMetaDataContext().getGlobalBindingDataMetaDataRegistry().getBindingDataMetaData(URLDecoder.decode(bindingDataURL, "UTF-8"));
      }
	  if(bindingDataMetaData == null) {
	    bindingDataMetaData = getMetaDataContext().getGlobalBindingDataMetaDataRegistry().getBindingDataMetaData(encodeURL(bindingDataURL)); 	  
	  }      
	} catch(UnsupportedEncodingException e) {
      // $JL-EXC$	
	}
	
    return bindingDataMetaData; 
  } 

  public BindingData getBindingData(String applicationName, String bindingDataLink) {
    String moduleName;
    String configurationId = bindingDataLink;
    int cutIndex = bindingDataLink.lastIndexOf("#");
    if(cutIndex != -1) {
      moduleName = bindingDataLink.substring(0, bindingDataLink.indexOf(0, cutIndex));
      configurationId = bindingDataLink.substring(cutIndex + 1);
    }

    ApplicationConfigurationContext appConfigurationContext = (ApplicationConfigurationContext)getConfigurationContext().getApplicationConfigurationContexts().get(applicationName);
    if(appConfigurationContext == null) {
      return null;
      //TODO throw exception ast alternative
    }

    BindingData resultBindingData = null;

    ServiceRegistry serviceRegistry = appConfigurationContext.getServiceRegistry();
    Enumeration enum1 = serviceRegistry.getServices().elements();
    Service service;
    BindingData[] bindingDatas;
    BindingData bindingData;
    while(enum1.hasMoreElements()) {
      service = (Service)enum1.nextElement();
      bindingDatas = service.getServiceData().getBindingData();
      if(bindingDatas == null || bindingDatas.length == 0) {
        continue;
      }
      for(int i = 0; i < bindingDatas.length; i++) {
        bindingData = bindingDatas[i];
        if(bindingData.getConfigurationId() != null && bindingData.getConfigurationId().equals(configurationId)) {
          resultBindingData = bindingData;
          break;
        }
      }
    }

    return resultBindingData;
  }
  
  public Service getServiceForBindingData(String bindingDataURL) {	
	BindingDataMetaData bindingDataMetaData = getBindingDataMetaData(bindingDataURL);	
    if(bindingDataMetaData == null) {  
	  //TODO - throw exception as alternative
      return null;
    }

    String applicationName = bindingDataMetaData.getApplicationName();
    String serviceName = bindingDataMetaData.getServiceName();

    if(!getConfigurationContext().getApplicationConfigurationContexts().containsKey(applicationName)) {
      //TODO - throw exception as alternative
      return null;
    }

    ApplicationConfigurationContext applicationConfigurationContext = (ApplicationConfigurationContext)getConfigurationContext().getApplicationConfigurationContexts().get(applicationName);
    ServiceRegistry serviceRegistry = applicationConfigurationContext.getServiceRegistry();
    return serviceRegistry.getService(serviceName);
  }

  public InterfaceMapping getInterfaceMappingForBindingData(String bindingDataURL) {
    BindingData bindingData = getBindingData(bindingDataURL);              
    if(bindingData == null) {
      //TODO - throw exception as alternative
      return null;
    }

    return getInterfaceMappingForBindingData(bindingData);
  }
  
  public InterfaceMapping getInterfaceMappingForBindingData(BindingData bindingData) {    	     
	InterfaceDefinition interfaceDefinition = getConfigurationContext().getInterfaceDefinitionRegistry().getInterfaceDefinition(bindingData.getInterfaceId().trim());
    String interfaceMappingId = interfaceDefinition.getInterfaceMappingId();
    if(interfaceMappingId == null) {
      interfaceMappingId = bindingData.getInterfaceMappingId();	  
    }             
        
    return getMappingContext().getInterfaceMappingRegistry().getInterfaceMapping(interfaceMappingId.trim());	  
  }
  
  public TypeMappingRegistry getTypeMappingRegistryForBindingData(String bindingDataURL) {
    BindingDataMetaData bindingDataMetaData = getBindingDataMetaData(bindingDataURL);    
    if(bindingDataMetaData == null) {  
      //TODO - throw exception as alternative
      return null;
    }    

    String applicationName = bindingDataMetaData.getApplicationName();
    String serviceName = bindingDataMetaData.getServiceName();

    if(!getApplicationServiceTypeMappingContexts().containsKey(applicationName)) {
      //TODO - throw exception as alternative
      return null;
    }

    ApplicationServiceTypeMappingContext applicationServiceTypeMappingContext = (ApplicationServiceTypeMappingContext)applicationServiceTypeMappingContexts.get(applicationName);
    if(!applicationServiceTypeMappingContext.containsServiceTypeMappingRegistryKey(ApplicationServiceTypeMappingContext.DEFAULT_USE)) { //always use 'default', other are irrelevant
      //TODO - throw exception as alternative
      return null;
    }

    ServiceTypeMappingRegistry serviceTypeMappingRegistry = applicationServiceTypeMappingContext.getServiceTypeMappingRegistry(ApplicationServiceTypeMappingContext.DEFAULT_USE); //always use 'default', other are irrelevant

    return serviceTypeMappingRegistry.getTypeMappingRegistry(serviceName);
  }
  
  public JAXBContext getJAXBContextForBindingData(String bindingDataURL) {
	BindingDataMetaData bindingDataMetaData = getBindingDataMetaData(bindingDataURL);
	if(bindingDataMetaData == null) {
      //TODO - throw exception as alternative
      return null;
    }
	
    String applicationName = bindingDataMetaData.getApplicationName();
    String serviceName = bindingDataMetaData.getServiceName();

    ApplicationServiceTypeMappingContext applicationServiceTypeMappingContext = (ApplicationServiceTypeMappingContext)getApplicationServiceTypeMappingContexts().get(applicationName);
    if(applicationServiceTypeMappingContext == null) {
      //TODO - throw exception as alternative
      return null;
    }
    
    InterfaceMapping intfMapp = getInterfaceMappingForBindingData(bindingDataURL);
    String intfMapID = intfMapp.getInterfaceMappingID();
    String key = JAXBContextRegistry.generateKey(serviceName, intfMapID);
    return applicationServiceTypeMappingContext.getJaxbContextRegistry().getJAXBContext(key);    
  }

  public String getWSDLRelPath(String bindingDataURL, String type, String style) {
	BindingDataMetaData bindingDataMetaData = getBindingDataMetaData(bindingDataURL);;      
	if(bindingDataMetaData == null) {
      //throw exception as alternative
      return null;
    }    
    
	String applicationName = bindingDataMetaData.getApplicationName();
    String serviceName = bindingDataMetaData.getServiceName();

    if(!getGlobalWSDLContext().getApplicationWSDLContexts().containsKey(applicationName)) {
      //TODO - throw exception as alternative
      return null;
    }
    ApplicationWSDLContext applicationWsdlContext = (ApplicationWSDLContext)getGlobalWSDLContext().getApplicationWSDLContexts().get(applicationName);

    if(!applicationWsdlContext.containsWSDLContextKey(type)) {
      //TODO - throw exception as alternative
      return null;
    }
    WSDLContext wsdlContext = applicationWsdlContext.getWSDLContext(type);

    if(!wsdlContext.containsWSDLRegistryKey(style)) {
      //TODO - throw exception as alternative
      return null;
    }
    WSDLRegistry wsdlRegistry = (WSDLRegistry)wsdlContext.getWSDLRegistry(style);

    if(!wsdlRegistry.containsWsdlDescriptorID(serviceName)) {
      //TODO - throw exception as alternative
      return null;
    }
    WsdlType wsdlDesctiptor = wsdlRegistry.getWsdlDescriptor(serviceName);

    return wsdlDesctiptor.get_value().trim();
  }

  public String getWsdlPath(String bindingDataURL, String type, String style) {
    BindingDataMetaData bindingDataMetaData = getBindingDataMetaData(bindingDataURL);	      
	if(bindingDataMetaData == null) {
      //throw exception as alternative
      return null;
    }	    
	
	String applicationName = bindingDataMetaData.getApplicationName();
    String serviceName = bindingDataMetaData.getServiceName();

    if(!getGlobalWSDLContext().getApplicationWSDLContexts().containsKey(applicationName)) {
      //TODO - throw exception as alternative
      return null;
    }
    ApplicationWSDLContext applicationWsdlContext = (ApplicationWSDLContext)getGlobalWSDLContext().getApplicationWSDLContexts().get(applicationName);

    if(!applicationWsdlContext.containsWSDLContextKey(type)) {
      //TODO - throw exception as alternative
      return null;
    }
    WSDLContext wsdlContext = applicationWsdlContext.getWSDLContext(type);

    if(!wsdlContext.containsWSDLRegistryKey(style)) {
      //TODO - throw exception as alternative
      return null;
    }
    WSDLRegistry wsdlRegistry = (WSDLRegistry)wsdlContext.getWSDLRegistry(style);

    if(!wsdlRegistry.containsWsdlDescriptorID(serviceName)) {
      //TODO - throw exception as alternative
      return null;
    }
    WsdlType wsdlDesctiptor = wsdlRegistry.getWsdlDescriptor(serviceName);

    if(!getMetaDataContext().getWSApplicationMetaDataContexts().containsKey(applicationName)) {
      //TODO - throw exception as alternative
      return null;
    }
    WSApplicationMetaDataContext applicationMetaDataContext = (WSApplicationMetaDataContext)getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);

    if(!applicationMetaDataContext.getServiceMetaDataRegistry().containsServiceMetaDataID(serviceName)) {
      //TODO - throw exception as alternative
      return null;
    }
    ServiceMetaData serviceMetaData = applicationMetaDataContext.getServiceMetaDataRegistry().getServiceMetaData(serviceName);
    String moduleName = serviceMetaData.getModuleName();

    if(!applicationMetaDataContext.getModuleRuntimeDataRegistry().containsModuleRuntimeDataID(moduleName)) {
      //TODO - throw exception as alternative
      return null;
    }
    ModuleRuntimeData moduleRuntimeData = applicationMetaDataContext.getModuleRuntimeDataRegistry().getModuleRuntimeData(moduleName);

    return moduleRuntimeData.getFilePathName(getWSDLRelPath(bindingDataURL, type, style));
  }

  public WsdlFileType getWSDLFileDescriptor(String bindingDataURL) {
    BindingDataMetaData bindingDataMetaData = getBindingDataMetaData(bindingDataURL);
    if(bindingDataMetaData == null) {
      //TODO - throw exception as alternative
      return null;
    }
    
    String applicationName = bindingDataMetaData.getApplicationName();
    String serviceName = bindingDataMetaData.getServiceName();

    ServiceDescriptorRegistry serviceDescriptorRegistry = ((WSApplicationDescriptorContext)getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName)).getServiceDescriptorRegistry();
    if(serviceDescriptorRegistry == null) {
      //TODO - throw exception as alternative
      return null;
    }

    WebserviceDescriptionType serviceDescriptor = serviceDescriptorRegistry.getServiceDescriptor(serviceName);
    if(serviceDescriptor == null) {
      //TODO - throw exception as alternative
      return null;
    }

    return serviceDescriptor.getWsdlFile();
  }

  public Object[] getInterfaceDefinitionsByApplication(boolean consumer, String applicationName) {
    if(consumer) {
      return new InterfaceDefinition[0]; //TODO - Implement for clients
    } else {
      //TODO - Use an appl<->interfaces registry or add interfaces that have no BindingDatas to them
      ArrayList interfaceNames = new ArrayList();
      ApplicationConfigurationContext applicationConfigurationContext = getApplicationConfigurationContext(applicationName);
      Service[] services = applicationConfigurationContext.getServiceRegistry().listServices();
      for (int i = 0; i < services.length; i++) {
        Service service = services[i];
        if (service.getServiceData() != null) {
          BindingData[] bindings = service.getServiceData().getBindingData();
          if (bindings != null) {
            for (int j = 0; j < bindings.length; j++) {
              BindingData binding = bindings[j];
              String ifName = binding.getInterfaceId();
              if (!interfaceNames.contains(ifName)) {
                interfaceNames.add(ifName);
              }
            }
          }
        }
      }

      InterfaceDefinition[] interfaces = getConfigurationContext().getInterfaceDefinitionRegistry().listInterfaceDefinitions();
      ArrayList interfacesList = new ArrayList(interfaceNames.size());
      for (int i = 0; i < interfaces.length; i++) {
        InterfaceDefinition ifDef = interfaces[i];
        if (interfaceNames.contains(ifDef.getId())) {
          interfacesList.add(ifDef);
        }
      }
      interfaces = new InterfaceDefinition[interfacesList.size()];
      interfacesList.toArray(interfaces);
      return interfaces;
    }
  }

  public Object[] getServicesPerInterface(boolean consumer, Object interfaceDefinition) {
    //TODO - Optimize. For clients it must be got also from the deployment descriptor due to there may be default services with no bindings per interface...
    Service[] services = (Service[]) getServicesByApplication(consumer, getApplicationOfInterface(consumer, interfaceDefinition));
    String ifName = ((InterfaceDefinition) interfaceDefinition).getId();
    ArrayList servicesList = new ArrayList(services.length);
    for (int i = 0; i < services.length; i++) {
      Service service = services[i];
      if (service.getServiceData() != null) {
        BindingData[] bindings = service.getServiceData().getBindingData();
        if (bindings != null) {
          for (int j = 0; j < bindings.length; j++) {
            BindingData binding = bindings[j];
            if (ifName.equals(binding.getInterfaceId())) {
              servicesList.add(service);
              break;
            }
          }
        }
      }
    }
    services = new Service[servicesList.size()];
    servicesList.toArray(services);
    return services;
  }

  public Object getInterfaceDefinitionByName(boolean consumer, String ifDefName) {
    // TODO Optimize
    InterfaceDefinition[] ifDefs;
    if (consumer) {
      return null;
      //ifDefs = new InterfaceDefinition[0]; //TODO - Implement
    } else {
      return getConfigurationContext().getInterfaceDefinitionRegistry().getInterfaceDefinition(ifDefName);
    }
  }

  private ApplicationConfigurationContext getApplicationConfigurationContext(String applicationName) {
    Hashtable appConfigContexts = getConfigurationContext().getApplicationConfigurationContexts();
    return (ApplicationConfigurationContext) appConfigContexts.get(applicationName);
  }

  public Object[] getServicesByApplication(boolean consumer, String applicationName) {
    if (consumer) {
      return new Service[0]; //TODO - Implement for clients
    } else {
      ArrayList servicesResult = new ArrayList();

      ApplicationConfigurationContext applicationConfigurationContext = getApplicationConfigurationContext(applicationName);
      Service[] services = applicationConfigurationContext.getServiceRegistry().listServices();
      for (int i = 0; i < services.length; i++) {
        servicesResult.add(services[i]);
      }

      Service[] servicesArr = new Service[servicesResult.size()];
      servicesResult.toArray(servicesArr);
      return servicesArr;
    }
  }

  public String[] getApplicationNames(boolean consumer) {
    Hashtable appConfigContexts = getConfigurationContext().getApplicationConfigurationContexts();
    synchronized (appConfigContexts) {
      String[] applicationNames = new String[appConfigContexts.size()];
      Enumeration enum1 = appConfigContexts.keys();
      int i = 0;
      while (enum1.hasMoreElements()) {
        applicationNames[i++] = (String)enum1.nextElement();
      }
      return applicationNames;
    }
  }

  public String getApplicationOfInterface(boolean consumer, Object interfaceDef) {
    //TODO - Optimize. Replace with a registry
    String[] appNames = getApplicationNames(consumer);
    for (int i = 0; i < appNames.length; i++) {
      String appName = appNames[i];
      InterfaceDefinition[] interfaces = (InterfaceDefinition[]) getInterfaceDefinitionsByApplication(consumer, appName);
      for (int j = 0; j < interfaces.length; j++) {
        InterfaceDefinition ifDef = interfaces[j];
        if (ifDef == interfaceDef) {
          return appName;
        }
      }
    }
    return null;
  }

  public void createService(boolean consumer, Object serviceObj, String appName) {
    //TODO Add references to WSDL elements, mappings, etc...
    ApplicationConfigurationContext applicationConfigurationContext = getApplicationConfigurationContext(appName);
    ServiceRegistry serviceRegistry = applicationConfigurationContext.getServiceRegistry();
    Service service = (Service) serviceObj;
    try {

    serviceRegistry.putService(service.getName(), service);
    } catch(Exception e) {
      //TODO
      e.printStackTrace();
      //throw e;
    }
    // TODO Store service permanently and notify the other cluster nodes
  }

  public void removeService(boolean consumer, Object serviceObj, String appName) {
    //TODO Remove references to WSDL elements, mappings, etc...
    ApplicationConfigurationContext applicationConfigurationContext = getApplicationConfigurationContext(appName);
    ServiceRegistry serviceRegistry = applicationConfigurationContext.getServiceRegistry();
    Service service = (Service) serviceObj;
    serviceRegistry.removeService(service.getName());
    // TODO Remove service permanently and notify the other cluster nodes
  }

  public void updateService(boolean consumer, Object service, String appName) {
    //TODO Add references to WSDL elements, mappings, etc... for the bindings
    //TODO Store service permanently and notify the other cluster nodes
  }  

  public Object[] getInterfaceDefinitions() {
    return getConfigurationContext().getInterfaceDefinitionRegistry().listInterfaceDefinitions();
  }

  public Object[] getServices() {
    Hashtable appConfigContexts = getConfigurationContext().getApplicationConfigurationContexts();
    Vector servicesResult = new Vector();

    Enumeration enum1 = appConfigContexts.keys();
    while(enum1.hasMoreElements()) {
      String applicationName = (String)enum1.nextElement();
      ApplicationConfigurationContext applicationConfigurationContext = (ApplicationConfigurationContext)appConfigContexts.get(applicationName);
      Service[] services = applicationConfigurationContext.getServiceRegistry().listServices();
      for (int i = 0; i < services.length; i++) {
	    servicesResult.addElement(services[i]);
	  }
    }

    Service[] servicesArr = new Service[servicesResult.size()];
    for (int i = 0; i < servicesResult.size(); i++) {
	  servicesArr[i] = (Service)servicesResult.elementAt(i);
	}
    return servicesArr;
  }
    
  public String getApplicationName(String uriID) {
    BindingDataMetaData bdMD = getBindingDataMetaData(uriID);
    return bdMD.getApplicationName();
  }
  
  public String getWebServiceName(String uriID) {
    BindingDataMetaData bdMD = getBindingDataMetaData(uriID);
    return bdMD.getServiceName();
  }
    
  public Service[] getServices(String appName) {
    ApplicationConfigurationContext appCtx = (ApplicationConfigurationContext) getConfigurationContext().getApplicationConfigurationContexts().get(appName);
    if (appCtx == null) { //this application has no web service
      return null; 
    }
    return appCtx.getServiceRegistry().listServices();
  }
  
  public int getServerPort(String portID) {
    int port = -1;
    try {
      if (MetaDataAccessor.PORT_HTTP.equals(portID)) {
        ShmAccessPoint[] aps = ShmAccessPoint.getAllAccessPoints(ShmAccessPoint.PID_HTTP);
        if (aps.length > 0) {
          port = aps[0].getPort();
        }      
      } else if (MetaDataAccessor.PORT_HTTPS.equals(portID)) {
        ShmAccessPoint[] aps = ShmAccessPoint.getAllAccessPoints(ShmAccessPoint.PID_HTTPS);
        if (aps.length > 0) {
          port = aps[0].getPort();
        }      
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return port;
  }
  
  public Set getWSDLStyles(String bdUrl) {
    Set result = new HashSet();    
    WsdlFileType wsdlDscr = getWSDLFileDescriptor(bdUrl);
    if(wsdlDscr == null) {
    	//logger.logT(Severity.ERROR, "getWSDLFileDescriptor("+ bdUrl +") is NULL");
    	return result;
    }
    
    WsdlType[] wsdls = wsdlDscr.getWsdl();
    for (int i = 0; i < wsdls.length; i++) {
      result.add(wsdls[i].getStyle().getValue());
    }
    return result;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.espbase.server.ContainerEnvironment#getCurrentUser()
   */
  public String getCurrentUser() {
    IUser user = UMFactory.getAuthenticator().getLoggedInUser();
    if (user != null) {
      return user.getName();
    }
    return "";
  }
 
  
}
