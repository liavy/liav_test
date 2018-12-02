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

package com.sap.engine.services.webservices.server;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.xml.bind.JAXBContext;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.interfaces.webservices.server.WebServicesContainerManipulator;
import com.sap.engine.services.deploy.container.ComponentReference;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.espbase.server.runtime.BuiltInWSEndpointImplContainer;
import com.sap.engine.services.webservices.espbase.sr.ClassificationsXMLLoader;
import com.sap.engine.services.webservices.espbase.sr.MetadataXMLLoader;
import com.sap.engine.services.webservices.espbase.sr.metadata.ClassificationsMetaData;
import com.sap.engine.services.webservices.exceptions.RegistryException;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.server.container.BaseServiceContext;
import com.sap.engine.services.webservices.server.container.OfflineBaseServiceContext;
import com.sap.engine.services.webservices.server.container.configuration.ApplicationConfigurationContext;
import com.sap.engine.services.webservices.server.container.configuration.BindingDataRegistry;
import com.sap.engine.services.webservices.server.container.configuration.ConfigurationContext;
import com.sap.engine.services.webservices.server.container.configuration.ServiceRegistry;
import com.sap.engine.services.webservices.server.container.descriptors.ConfigurationDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.metadata.InterfaceDefMetaData;
import com.sap.engine.services.webservices.server.container.metadata.InterfaceDefMetaDataRegistry;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeData;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeDataRegistry;
import com.sap.engine.services.webservices.server.container.metadata.service.BindingDataMetaData;
import com.sap.engine.services.webservices.server.container.metadata.service.BindingDataMetaDataRegistry;
import com.sap.engine.services.webservices.server.container.ws.ApplicationServiceTypeMappingContext;
import com.sap.engine.services.webservices.server.container.ws.JAXBContextRegistry;
import com.sap.engine.services.webservices.server.container.ws.ServiceContext;
import com.sap.engine.services.webservices.server.container.ws.ServiceTypeMappingRegistry;
import com.sap.engine.services.webservices.server.container.ws.descriptors.InterfaceDefinitionDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.ws.descriptors.ServiceDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.ws.descriptors.WSApplicationDescriptorContext;
import com.sap.engine.services.webservices.server.container.ws.descriptors.WebServicesDescriptorContext;
import com.sap.engine.services.webservices.server.container.ws.metaData.ServiceMetaData;
import com.sap.engine.services.webservices.server.container.ws.metaData.ServiceMetaDataRegistry;
import com.sap.engine.services.webservices.server.container.ws.metaData.WSApplicationMetaDataContext;
import com.sap.engine.services.webservices.server.container.ws.metaData.WebServicesMetaDataContext;
import com.sap.engine.services.webservices.server.container.ws.wsdl.ApplicationWSDLContext;
import com.sap.engine.services.webservices.server.container.ws.wsdl.WSDLContext;
import com.sap.engine.services.webservices.server.container.ws.wsdl.WSDLRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.ServiceRefContext;
import com.sap.engine.services.webservices.server.container.wsclients.descriptors.WSClientsApplicationDescriptorContext;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.ServiceRefMetaData;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.ServiceRefMetaDataRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.WSClientsApplicationMetaDataContext;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.WSClientsMetaDataContext;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.InterfaceDefinitionDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlFileType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlStyleType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlTypeType;
import com.sap.engine.services.webservices.server.deploy.ws.WS630DeployProcess;
import com.sap.engine.services.webservices.server.deploy.ws.WSDeployProcess;
import com.sap.engine.services.webservices.server.deploy.ws.notification.WSRTChangesNotificationHandler;
import com.sap.engine.services.webservices.server.deploy.wsclients.WSClientsDeployProcess;
import com.sap.engine.services.webservices.server.deploy.wsclients.WSClientsStartProcess;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Title: WebServicesContainerManipulatorImpl
 * Description: WebServicesContainerManipulatorImpl 
 * 
 * @author Aneta Angova 
 * @author Dimitrina Stoyanova
 */

public class WebServicesContainerManipulatorImpl implements WebServicesContainerManipulator {
	
  private Hashtable wsRTChangesNotificationHandlers; 
  private boolean isConsumer = false;
 
  public void setConsumer(boolean isConsumer) {
    this.isConsumer = isConsumer;
  } 
  
  /**
   * @param com.sap.engine.services.webservices.espbase.configuration.Service service  
   * @param applicationName
   * @exception Exception
   */
  public void createService(Object service, String applicationName) throws Exception {
	if (service == null) {
	  return;
	}
		
	if (isConsumer) {
	  throw new OperationNotSupportedException("Create of Runtime Configuration for webservice client is not allowed!");	
	}
	
  	Service serviceToCreate = (Service)service;
	ConfigurationContext configurationContext = null;
	if (!isConsumer) {
	  configurationContext = getServiceContext().getConfigurationContext();	
	} else {
	  configurationContext = getServiceRefContext().getConfigurationContext();
	}

	ServiceRegistry globalServiceRegistry = configurationContext.getGlobalServiceRegistry();
	ApplicationConfigurationContext applicationConfigurationContext = (ApplicationConfigurationContext)configurationContext.getApplicationConfigurationContexts().get(applicationName);
	ServiceRegistry serviceRegistry = applicationConfigurationContext.getServiceRegistry();
	String serviceName = serviceToCreate.getName().trim();
	String serviceContextRoot = serviceToCreate.getServiceData().getContextRoot().trim();
	if (!serviceContextRoot.equals("") && !serviceContextRoot.startsWith("/")) {
	  serviceContextRoot = "/" + serviceContextRoot;
	}
		
	Service oldService = serviceRegistry.getService(serviceName);
	if (!serviceContextRoot.equals("") && globalServiceRegistry.containsServiceID(serviceContextRoot)) {
	  throw new RegistryException((isConsumer ? "WSClients:" : "WebServices:") + " There has already been registered service with id '" + serviceContextRoot + "' in globalServiceRegistry!");		
	}
	if (serviceRegistry.containsServiceID(serviceName)) {
	   throw new RegistryException((isConsumer ? "WSClients:" : "WebServices:") + " There has already been registered service with id '" + serviceName + "' in serviceRegistry!");
	}
	globalServiceRegistry.putService(serviceContextRoot, serviceToCreate);
	serviceRegistry.putService(serviceName, serviceToCreate);
	
	if (!isConsumer) {
	  loadServiceMetaDatasWS(applicationName, serviceToCreate, oldService, serviceName);
	} else {
	  loadServiceRefMetaDatasWSClients(applicationName, serviceToCreate, oldService, serviceName);	
	}
		
	loadBindingDatas(configurationContext, applicationConfigurationContext, serviceToCreate, oldService, applicationName, serviceName, serviceContextRoot, true);
	
	if (!isConsumer) {
	  loadInterfaceDefMetaDatasWS(applicationName);
	  loadAdditionalInterfaceDef(serviceToCreate);
	}
	
	if (!isConsumer) {
	  loadServiceDescrptor(serviceToCreate, applicationName);
	}
      
	try {    
	  if(!isConsumer) {    
	    WSRTChangesNotificationHandler wsRuntimeChangesNotificationHandler = new WSRTChangesNotificationHandler(applicationName, serviceToCreate, null, WSContainer.getRuntimeProcessingEnv());
	    getWSRTChangesNotificationHandlers().put(applicationName, wsRuntimeChangesNotificationHandler); 
	    wsRuntimeChangesNotificationHandler.onExecutePhase();
	  }
	} catch(Exception e) { 
	  //TODO - e, warning    
	  Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);             
    }    
	      
	try { 
	  if(!isConsumer) {
	    WSRTChangesNotificationHandler wsRTChangesNotificationHandler = (WSRTChangesNotificationHandler)getWSRTChangesNotificationHandlers().remove(applicationName);  
	    if(wsRTChangesNotificationHandler != null) {
	      wsRTChangesNotificationHandler.onCommitPhase();
	    } 
	  }         
	} catch(Exception e) { 
	  //TODO - warning
	  Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);             
	}      
  }
  
  /**
   * @param com.sap.engine.services.webservices.espbase.configuration.Service service  
   * @param applicationName
   * @exception Exception
   */
  public void updateService(Object service, String applicationName) throws Exception {
	if (service == null) {
	  return;
	}
		
	String serviceName = ((Service)service).getName().trim();
	Service serviceToUpdate = (Service)service;
	ConfigurationContext configurationContext = null;
	if (!isConsumer) {
	  configurationContext = getServiceContext().getConfigurationContext();
	} else {
	  configurationContext = getServiceRefContext().getConfigurationContext();		
	}

	ApplicationConfigurationContext applicationConfigurationContext = (ApplicationConfigurationContext)configurationContext.getApplicationConfigurationContexts().get(applicationName);
	ServiceRegistry serviceRegistry = applicationConfigurationContext.getServiceRegistry();
	ServiceRegistry globalServiceRegistry = configurationContext.getGlobalServiceRegistry();
	Service serviceToDelete = serviceRegistry.getService(serviceName);
	String serviceContextRoot = serviceToDelete.getServiceData().getContextRoot().trim();
	if (!serviceContextRoot.equals("") && !serviceContextRoot.startsWith("/")) {
	  serviceContextRoot = "/" + serviceContextRoot;		
	}
        
    //the new service has to be added to list of services in the configuration descriptor, otherwise will be not bound in the JNDI and can not be used on later time on client consumption
	if (isConsumer) {		
	  WSClientsApplicationDescriptorContext wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)(getServiceRefContext().getWsClientsApplicationDescriptorContexts()).get(applicationName);
	  ConfigurationDescriptorRegistry configurationDescriptorRegistry = wsClientsApplicationDescriptorContext.getConfigurationDescriptorRegistry();
	  
	  ServiceRefMetaDataRegistry metaDataRegistry = ((WSClientsApplicationMetaDataContext)(getServiceRefContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts()).get(applicationName)).getServiceRefMetaDataRegistry();	 
	  String metaName = metaDataRegistry.getServiceRefMetaData(serviceName).getServiceRefGroupName();
	  ConfigurationRoot configurationDescriptor = configurationDescriptorRegistry.getConfigurationDescriptor(metaName);
	  
	  Service[] services = configurationDescriptor.getRTConfig().getService();
	  if (services != null) {
		  for (int i = 0; i < services.length; i++) {
				if (services[i].getName().equals(serviceName)) {
				  services[i] = serviceToUpdate;
				}
		  }
	  }
	  configurationDescriptor.getRTConfig().setService(services);  
	  configurationDescriptorRegistry.putConfigurationDescriptor(metaName, configurationDescriptor);
	}
    		
	removeBindingDatas(configurationContext, applicationConfigurationContext, serviceToDelete, applicationName, serviceName, serviceContextRoot);
	loadBindingDatas(configurationContext, applicationConfigurationContext, serviceToUpdate, serviceToDelete, applicationName, serviceName, serviceContextRoot, false);
	globalServiceRegistry.removeService(serviceContextRoot);
	globalServiceRegistry.putService(serviceContextRoot, serviceToUpdate);
	serviceRegistry.removeService(serviceName);
	serviceRegistry.putService(serviceName, serviceToUpdate);

    try {    
      if(!isConsumer) {    
        WSRTChangesNotificationHandler wsRuntimeChangesNotificationHandler = new WSRTChangesNotificationHandler(applicationName, serviceToUpdate, serviceToDelete.getName().trim(), WSContainer.getRuntimeProcessingEnv());
        getWSRTChangesNotificationHandlers().put(applicationName, wsRuntimeChangesNotificationHandler); 
        wsRuntimeChangesNotificationHandler.onExecutePhase();
      }
    } catch(Exception e) { 
      //TODO - e, warning    
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);             
    }    
      
	try { 
	  if(!isConsumer) {
	    WSRTChangesNotificationHandler wsRTChangesNotificationHandler = (WSRTChangesNotificationHandler)getWSRTChangesNotificationHandlers().remove(applicationName);  
	    if(wsRTChangesNotificationHandler != null) {
	      wsRTChangesNotificationHandler.onCommitPhase();
	    } 
	  }         
	} catch(Exception e) { 
	  //TODO - warning
	  Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);             
    }      
  }
  
  /**
   * @param com.sap.engine.services.webservices.espbase.configuration.Service service  
   * @param applicationName
   * @exception Exception
   */    
  public void deleteService(Object service, String applicationName) {
    if (service == null) return;

    ConfigurationContext configurationContext = null;
    Service serviceToDelete = (Service)service;
    if (!isConsumer) {
      configurationContext = getServiceContext().getConfigurationContext();
    } else {
      configurationContext = getServiceRefContext().getConfigurationContext();
    }

    //unregisration of the service from globalServiceRegistry and serviceRegistry
    ServiceRegistry globalServiceRegistry = configurationContext.getGlobalServiceRegistry();
    ApplicationConfigurationContext applicationConfigurationContext = (ApplicationConfigurationContext)configurationContext.getApplicationConfigurationContexts().get(applicationName);
    ServiceRegistry serviceRegistry = applicationConfigurationContext.getServiceRegistry();

    String serviceName = serviceToDelete.getName().trim();
    String serviceContextRoot = serviceToDelete.getServiceData().getContextRoot().trim();
    if(!serviceContextRoot.equals("") && !serviceContextRoot.startsWith("/")) {
      serviceContextRoot = "/" + serviceContextRoot;
    }
    globalServiceRegistry.removeService(serviceContextRoot);
    serviceRegistry.removeService(serviceName);

    if (!isConsumer) {
      removeServiceMetaDatasWS(applicationName, serviceName);
    } else {
      removeServiceRefMetaDatasWSClients(applicationName, serviceName);
    }

	removeBindingDatas(configurationContext, applicationConfigurationContext, serviceToDelete, applicationName, serviceName, serviceContextRoot);
	
    if (!isConsumer) {
      removeInterfaceDefMetaDatasWS(serviceToDelete);
    }
        
    removeBindingDataMetaDatas(applicationName, serviceToDelete);
    
    if (!isConsumer) {
      removeServiceDescrptor(serviceToDelete, applicationName);
    }
                 
    if(!isConsumer) {
      WSRTChangesNotificationHandler wsRTChangesNotificationHandler = null; 
      try {          
        wsRTChangesNotificationHandler = new WSRTChangesNotificationHandler(applicationName, null, serviceToDelete.getName().trim(), WSContainer.getRuntimeProcessingEnv());                   
        wsRTChangesNotificationHandler.onExecutePhase();
      } catch(Exception e) { 
        //TODO - warning    
        Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);             
      }
      try {      
        if(wsRTChangesNotificationHandler != null) {        
          wsRTChangesNotificationHandler.onCommitPhase();
        }
      } catch(Exception e) { 
        //TODO - warning    
        Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);             
      }
    }                    
  }
  
  public String getInterfaceDefinitionId(String bindingDataURL) {
	String URI_PREFIX = "/sapws/";
    String URI_SEPARATOR = "/";

	if(bindingDataURL == null) {
      throw new NullPointerException("parameter could not be null");
    }
     
	int cutIndex = bindingDataURL.indexOf(URI_PREFIX);
	if(cutIndex == -1) {
	  return null; 	
	}  
    
    String interfaceDefinitionId = bindingDataURL.substring(cutIndex + URI_PREFIX.length());
    interfaceDefinitionId = interfaceDefinitionId.substring(0, interfaceDefinitionId.lastIndexOf("/"));
    interfaceDefinitionId = interfaceDefinitionId.substring(0, interfaceDefinitionId.lastIndexOf("/"));
    
    return interfaceDefinitionId;
  }
  
  /**
   * @deprecated Use getInterfaceDefinitionById
   */
  public Object getInterfaceDefinitionByName(String ifDefName) {
    ConfigurationContext configurationContext = null;
    if(!isConsumer) {
      configurationContext = getServiceContext().getConfigurationContext();
    } else {
      configurationContext = getServiceRefContext().getConfigurationContext();
    }

    return configurationContext.getInterfaceDefinitionRegistry().getInterfaceDefinition(ifDefName);
  }

  public InterfaceDefinition getInterfaceDefinitionById(String interfaceDefinitionId) {
    return getInterfaceDefinitionById(null, interfaceDefinitionId, RUNTIME_MODE);    
  }
  
  public InterfaceDefinition getInterfaceDefinitionById(String applicationName, String interfaceDefinitionId, int mode) {
    BaseServiceContext baseServiceContext = null;
    if(isConsumer) {    	
      if(mode == DEPLOY_MODE) {
        WSClientsDeployProcess wsClientsDeployProcess = WebServicesFrame.webServicesDeployManager.getWsClientsDeployProcesses().get(applicationName); 
        if(wsClientsDeployProcess != null) {
          baseServiceContext = wsClientsDeployProcess.getServiceContext(); 	
        }
      }
      if(mode == RUNTIME_MODE) {
        baseServiceContext = getServiceRefContext();
      }	       
    } else {
      if(mode == DEPLOY_MODE) {
        WSDeployProcess wsDeployProcess = WebServicesFrame.webServicesDeployManager.getWsDeployProcesses().get(applicationName);
        if(wsDeployProcess != null) {
          baseServiceContext = wsDeployProcess.getServiceContext();	
        } else {
          WS630DeployProcess ws630DeployProcess	= WebServicesFrame.webServicesDeployManager.getWs630DeployProcesses().get(applicationName);
          if(ws630DeployProcess != null) {
            baseServiceContext = ws630DeployProcess.getServiceContext();   
          }
        }  	          
      }
      if(mode == RUNTIME_MODE) {
        baseServiceContext = getServiceContext();	  
      } 	
    }
    
    if(baseServiceContext == null) {
      return null;	
    }

    return baseServiceContext.getConfigurationContext().getInterfaceDefinitionRegistry().getInterfaceDefinition(interfaceDefinitionId);
  }

  public String[] getApplicationNames() {	
    ConfigurationContext configurationContext;
    if(!isConsumer) {
      configurationContext = getServiceContext().getConfigurationContext();
    } else {
      configurationContext = getServiceRefContext().getConfigurationContext();
    }

    Hashtable appConfigContexts = configurationContext.getApplicationConfigurationContexts();
    synchronized (appConfigContexts) {
      String[] applicationNames = new String[appConfigContexts.size()];
      Enumeration appContextsEnum = appConfigContexts.keys();
      int i = 0;
      while (appContextsEnum.hasMoreElements()) {
        applicationNames[i++] = (String)appContextsEnum.nextElement();
      }
      return applicationNames;
    }
  }

  public Object[] getInterfaceDefinitionsByApplication(String applicationName) {
    return getInterfaceDefinitionsForApplication(applicationName, RUNTIME_MODE);
  }
  
  public Object[] getInterfaceDefinitionsByApplicationName(String applicationName, boolean isRuntimeMode) {
    if(!isRuntimeMode) {
      return getInterfaceDefinitionsForApplication(applicationName, DEPLOY_MODE); 	
    } else {
      return getInterfaceDefinitionsForApplication(applicationName, RUNTIME_MODE);  	
    }	      
  }
  
  public InterfaceDefinition[] getInterfaceDefinitionsForApplication(String applicationName, int mode) {
    BaseServiceContext baseServiceContext = null;
    if(isConsumer) {
      if(mode == DEPLOY_MODE) {
        WSClientsDeployProcess wsClientsDeployProcess = WebServicesFrame.webServicesDeployManager.getWsClientsDeployProcesses().get(applicationName);
        if(wsClientsDeployProcess != null){
          baseServiceContext = wsClientsDeployProcess.getServiceContext();
        }
      }       
      if(mode == RUNTIME_MODE) {
        baseServiceContext = getServiceRefContext();	  
      }       
    } else {     
      if(mode == DEPLOY_MODE) {    	      	 
        WSDeployProcess wsDeployProcess = WebServicesFrame.webServicesDeployManager.getWsDeployProcesses().get(applicationName);         
        if(wsDeployProcess != null) {
          baseServiceContext = wsDeployProcess.getServiceContext();
        } else {
          WS630DeployProcess ws630DeployProcess = WebServicesFrame.webServicesDeployManager.getWs630DeployProcesses().get(applicationName);	
          if(ws630DeployProcess != null) {
            baseServiceContext = ws630DeployProcess.getServiceContext();
          }
        }                      
      } 
      if(mode == RUNTIME_MODE) {
        baseServiceContext = getServiceContext();
      }
    }    
    
    if(baseServiceContext == null) {
      return new InterfaceDefinition[0]; 	
    }
    
    return getInterfaceDefinitionsByApplicationName(baseServiceContext, applicationName);    
  }

  public Object[] getServicesByApplication(String applicationName) {
    ConfigurationContext configurationContext = null;
    if (!isConsumer) {
      configurationContext = getServiceContext().getConfigurationContext();
    } else {
      configurationContext = getServiceRefContext().getConfigurationContext();
    }
    ApplicationConfigurationContext applicationConfigurationContext = (ApplicationConfigurationContext)configurationContext.getApplicationConfigurationContexts().get(applicationName);
    if (applicationConfigurationContext != null) { 
      return applicationConfigurationContext.getServiceRegistry().listServices();
    } else {
    	return new Object[0];
    }
  }

  public Object[] getServicesPerInterface(Object interfaceDefinition) {
    //TODO - Optimize. For clients it must be got also from the deployment descriptor due to there may be default services with no bindings per interface...	
	return getServicesForInterfaceDefinition(getApplicationOfInterface(interfaceDefinition), ((InterfaceDefinition)interfaceDefinition).getId(), RUNTIME_MODE);
  }
  
  public Service[] getServicesForInterfaceDefinition(String applicationName, String interfaceDefinitionId, int mode) {         
	BaseServiceContext baseServiceContext = null; 
    if(isConsumer) {    	
      if(mode == DEPLOY_MODE) {
        WSClientsDeployProcess wsClientsDeployProcess = WebServicesFrame.webServicesDeployManager.getWsClientsDeployProcesses().get(applicationName);
        if(wsClientsDeployProcess != null) {
          baseServiceContext = wsClientsDeployProcess.getServiceContext();
        }       
      } 	
      if(mode == RUNTIME_MODE) {
        baseServiceContext = getServiceRefContext();        
      }
    } else {
      if(mode == DEPLOY_MODE) {
        WSDeployProcess wsDeployProcess = WebServicesFrame.webServicesDeployManager.getWsDeployProcesses().get(applicationName);        
        if(wsDeployProcess != null) {
          baseServiceContext = wsDeployProcess.getServiceContext();                              	    	
        } else {
          WS630DeployProcess ws630DeployProcess = WebServicesFrame.webServicesDeployManager.getWs630DeployProcesses().get(applicationName);	
          if(ws630DeployProcess != null) {
            baseServiceContext = ws630DeployProcess.getServiceContext(); 	  
          }	
        }
      }
      if(mode == RUNTIME_MODE) {
        baseServiceContext = getServiceContext(); 
      }
    }     
    
    if(baseServiceContext == null) {
      return new Service[0];	
    }
    
    return getServicesForInterfaceDefinition(interfaceDefinitionId, baseServiceContext.getServicesForApplication(applicationName));      
  }

  public String getApplicationOfInterface(Object interfaceDef) {
    //TODO - Optimize. Replace with a registry
    String[] appNames = getApplicationNames();
    for (int i = 0; i < appNames.length; i++) {
      String appName = appNames[i];
      InterfaceDefinition[] interfaces = (InterfaceDefinition[]) getInterfaceDefinitionsByApplication(appName);
      for (int j = 0; j < interfaces.length; j++) {
        InterfaceDefinition ifDef = interfaces[j];
        if (ifDef == interfaceDef) {
          return appName;
        }
      }
    }
    return null;
  }
  
  public String getApplicationOfInterfaceDefinition(String interfaceDefinitionId, int mode) throws ConfigurationException {
    if(isConsumer) {
      return null; 	
    }
    
    InterfaceDefMetaData interfaceDefMetaData = null; 
    
	if(mode == OFFLINE_MODE) {      	   
      OfflineBaseServiceContext offlineBaseServiceContext = WebServicesContainer.getOfflineBaseServiceContext();  
      interfaceDefMetaData = offlineBaseServiceContext.getInterfaceDefMetaData(interfaceDefinitionId);
	}
	
    if(interfaceDefMetaData == null) {
      return null;  	
    }   
    
    return interfaceDefMetaData.getApplicationName();       
  } 

  public Set getWSDLStyles(String bdUrl) throws UnsupportedOperationException {
    if (isConsumer) {
      throw new UnsupportedOperationException();
    }
    return getServiceContext().getWSDLStyles(bdUrl);
  }
 
  public Object getConfigurationManipulator() throws Exception {
    return WSContainer.getRuntimeProcessingEnv().getWSDLVisualizer().getInternalCFGBuilder();
  } 
  
  public Object getBuiltInWSEndpointManager() {
    return BuiltInWSEndpointImplContainer.SINGLETON;
  }
  
  public Object[] getSRPublicationMetaDataDescriptors(String applicationName, boolean isRuntimeMode) throws Exception {
    if(isConsumer) {
	  return new Object[0]; 	
	}    
	    
	ServiceContext serviceContext = null;
	if(isRuntimeMode) {
	  serviceContext = getServiceContext();      
	} else {
	  WSDeployProcess wsDeployProcess = (WSDeployProcess)WebServicesFrame.webServicesDeployManager.getWsDeployProcesses().get(applicationName);
	  if(wsDeployProcess != null) {
	    serviceContext = wsDeployProcess.getServiceContext();
	  }
	}
	    
	if(serviceContext == null) {
	  return new Object[0]; 	
	}	
	
	WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)serviceContext.getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName); 		
	if(wsApplicationDescriptorContext == null) {
	  return new Object[0];  
	}
	
	WSApplicationMetaDataContext wsApplicationMetaDataContext = (WSApplicationMetaDataContext)serviceContext.getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);
	ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = wsApplicationMetaDataContext.getModuleRuntimeDataRegistry();
	
	ArrayList<ClassificationsMetaData> srPublicationMetaDataDescriptors = new ArrayList<ClassificationsMetaData>();
	Enumeration enumer = moduleRuntimeDataRegistry.getModuleRuntimeDatas().elements();
	ModuleRuntimeData moduleRuntimeData; 	 
	File srPublicationMetaDatadDescriptorFile; 
	while(enumer.hasMoreElements()) {
	  moduleRuntimeData = (ModuleRuntimeData)enumer.nextElement();
	  srPublicationMetaDatadDescriptorFile = new File(moduleRuntimeData.getModuleDir() + "/META-INF/sr-publication.metadata"); 
	  if(srPublicationMetaDatadDescriptorFile.exists()) {
	    srPublicationMetaDataDescriptors.add(MetadataXMLLoader.load(srPublicationMetaDatadDescriptorFile));   	  
	  }
	}	
			
	return srPublicationMetaDataDescriptors.toArray(new ClassificationsMetaData[srPublicationMetaDataDescriptors.size()]);
  }
  
  public Object getSRPublication(String applicationName, String interfaceDefinitionID, boolean isRuntimeMode) throws Exception {	
    if(isConsumer) {
      return null; 	
    }    
    
    ServiceContext serviceContext = null;
    if(isRuntimeMode) {
      serviceContext = getServiceContext();      
    } else {
      WSDeployProcess wsDeployProcess = (WSDeployProcess)WebServicesFrame.webServicesDeployManager.getWsDeployProcesses().get(applicationName);
      if(wsDeployProcess != null) {
        serviceContext = wsDeployProcess.getServiceContext();
      }
    }
    
    if(serviceContext == null) {
      return null; 	
    }
     
	WebServicesDescriptorContext webServicesDescriptorContext = serviceContext.getWebServicesDescriptorContext();
    WebServicesMetaDataContext webServicesMetaDataContext = serviceContext.getMetaDataContext();
    
    InterfaceDefinitionDescriptorRegistry interfaceDefinitionDescriptorRegistry = webServicesDescriptorContext.getInterfaceDefinitionDescriptorRegistry();
    InterfaceDefinitionDescriptionType interfaceDefinitionDescriptor = interfaceDefinitionDescriptorRegistry.getInterfaceDefinitionDescriptor(interfaceDefinitionID);
    if(interfaceDefinitionDescriptor == null) {
      // TODO - throw exception as alternative
      return null; 	
    }
    
    InterfaceDefMetaDataRegistry interfaceDefMetaDataRegistry = webServicesMetaDataContext.getInterfaceDefMetaDataRegistry();
    InterfaceDefMetaData interfaceDefMetaData = interfaceDefMetaDataRegistry.getInterfaceDefMetaData(interfaceDefinitionID);
    
    WSApplicationMetaDataContext wsApplicationMetaDataContext = (WSApplicationMetaDataContext)webServicesMetaDataContext.getWSApplicationMetaDataContexts().get(interfaceDefMetaData.getApplicationName());    
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = wsApplicationMetaDataContext.getModuleRuntimeDataRegistry();
    ModuleRuntimeData moduleRuntimeData = moduleRuntimeDataRegistry.getModuleRuntimeData(interfaceDefMetaData.getModuleName());
    
    String srPublicationFileRelPath = interfaceDefinitionDescriptor.getSrPublicationFile();
    String srPublicationFilePath; 
    if(srPublicationFileRelPath.startsWith("/")) {
      srPublicationFilePath = moduleRuntimeData.getModuleDir() + srPublicationFileRelPath;
    } else{
      srPublicationFilePath = moduleRuntimeData.getModuleDir() + "/" + srPublicationFileRelPath;	
    }
    
    return ClassificationsXMLLoader.load(new File(srPublicationFilePath));
  } 
  
  public void startApplicationAndWait(String applicationName) throws RemoteException {
    WebServicesFrame.webServicesDeployManager.getDeployCommunicator().startApplicationAndWait(applicationName); 	  
  } 
    
  public Object getServiceImplInstance(String consumerApplicationName, String siName) throws NameNotFoundException, NamingException {
    if(!isConsumer) {
	  return null; 	
	}      
	    
	ServiceRefContext serviceRefContext = getServiceRefContext(); 
			
    Service[] services = serviceRefContext.getServicesBySIName(consumerApplicationName, siName, false);
	String applicationName = null;  
	Service service = null; 
	if(services != null && services.length != 0) {
	  applicationName = consumerApplicationName;
	  service = services[0];		  
    }
			
	if(service == null) {
	  Set<String> references = WebServicesFrame.webServicesDeployManager.getDeployCommunicator().getReferences(consumerApplicationName, ComponentReference.APPLICATION);
	  if(references != null && references.size() != 0) {
	    Hashtable<String, Service[]> servicesReferencedApplications = serviceRefContext.getServicesBySIName(references.toArray(new String[references.size()]), siName, false);
	    if(servicesReferencedApplications != null || servicesReferencedApplications.size() != 0) {
	      Entry<String, Service[]> entry = servicesReferencedApplications.entrySet().iterator().next(); 
	      applicationName = entry.getKey(); 
	      service = entry.getValue()[0];    	
	    }
	  }
	}
		
	if(service == null) {
	  return null;	
	}    
		
	ServiceMapping serviceMapping = serviceRefContext.getServiceMapping(service.getServiceMappingId());
	Context ctx = new InitialContext();
    return ctx.lookup(WSClientsStartProcess.PROXIES_CONTEXT + "/" + applicationName + "/" + serviceMapping.getImplementationLink().getServiceRefJNDIName());    
  } 
  
  public String constructBindingDataURLPath(String contextRoot, String bindingDataURL) {    
    return BaseServiceContext.constructBindingDataURLPath(contextRoot, bindingDataURL); 
  }
  
  public String getWSEndpointURL(String contextRoot, String bdUrl){
    return WSContainer.getRuntimeProcessingEnv().getWSDLVisualizer().getWSEndpointURL(contextRoot, bdUrl);
  }  
  
  public String getWSPolicyWsdlURLPath(String contextRoot, String bdUrl, WsdlSection wsSection){
    return WSContainer.getRuntimeProcessingEnv().getWSDLVisualizer().getWSPolicyWsdlURLPath(contextRoot, bdUrl, wsSection);  
  } 
  
  public List<String> getAllWSDLURLPaths(String contextRoot, String bdUrl){
    return WSContainer.getRuntimeProcessingEnv().getWSDLVisualizer().getAllWSDLURLPaths(contextRoot , bdUrl);
  }
  
  public String getSCAName(String applicationName) {
    Properties props = WebServicesFrame.webServicesDeployManager.getDeployProperties().get(applicationName); 
    if(props == null) {
      //TODO - throw exception as alternative	
      return null; 
    } 
    
    return props.getProperty("scaName");	  
  }
  
  public String getSCAVendor(String applicationName) {
    Properties props = WebServicesFrame.webServicesDeployManager.getDeployProperties().get(applicationName); 
	if(props == null) {
	  //TODO - throw exception as alternative
	  return null; 
	} 
	
	return props.getProperty("scaVendor");	  
  }
  
  private ServiceContext getServiceContext() {
    return WebServicesContainer.getServiceContext();
  }

  private ServiceRefContext getServiceRefContext() {
    return WebServicesContainer.getServiceRefContext();
  }
  
  private Hashtable getWSRTChangesNotificationHandlers()  {
    if(wsRTChangesNotificationHandlers == null) {
      wsRTChangesNotificationHandlers = new Hashtable();       
    }
    
    return wsRTChangesNotificationHandlers; 
  } 
  
  private void loadServiceDescrptor(Service serviceToCreate, String applicationName) {
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);
    ServiceDescriptorRegistry serviceDescriptorRegistry = wsApplicationDescriptorContext.getServiceDescriptorRegistry();
    InterfaceDefMetaDataRegistry interfaceDefMetaDataRegistry = getServiceContext().getMetaDataContext().getInterfaceDefMetaDataRegistry();
    
    BindingData[] bindingDatas = serviceToCreate.getServiceData().getBindingData();
    if (bindingDatas != null && bindingDatas.length != 0) {
      String interfaceId = bindingDatas[0].getInterfaceId();
      InterfaceDefMetaData interfaceDefMetaData = interfaceDefMetaDataRegistry.getInterfaceDefMetaData(interfaceId);
      Object[] services = interfaceDefMetaData.getServices().toArray();
      String serviceForIDef = "";
      WebserviceDescriptionType serviceDescriptor = null;
      for (int i = 0; i < services.length; i++) {
        serviceForIDef = (String)services[i];
        serviceDescriptor = serviceDescriptorRegistry.getServiceDescriptor(serviceForIDef);
        if (serviceDescriptor != null) {
          WebserviceDescriptionType newServiceDescriptor = new WebserviceDescriptionType();
          newServiceDescriptor.setWsdlFile(serviceDescriptor.getWsdlFile());
          newServiceDescriptor.setTypeMappingFile(serviceDescriptor.getTypeMappingFile());
          newServiceDescriptor.setTypesArchiveFile(serviceDescriptor.getTypesArchiveFile());
          newServiceDescriptor.setWebserviceName(serviceDescriptor.getWebserviceName());
          try {
            serviceDescriptorRegistry.putServiceDescriptor(serviceToCreate.getName(), newServiceDescriptor);
          } catch (Exception e) {
            //$JL-EXC$
          }

          loadServiceWSDLFileDescriptors(applicationName, serviceToCreate.getName(), newServiceDescriptor);

          ApplicationServiceTypeMappingContext appTypeMappingContext = (ApplicationServiceTypeMappingContext)getServiceContext().getApplicationServiceTypeMappingContexts().get(applicationName);
          ServiceTypeMappingRegistry serviceTypeMappingRegistry = appTypeMappingContext.getServiceTypeMappingRegistry(ApplicationServiceTypeMappingContext.DEFAULT_USE);
          if(serviceTypeMappingRegistry != null) {
            TypeMappingRegistry typeMappingRegistry = serviceTypeMappingRegistry.getTypeMappingRegistry(serviceForIDef);
            if (typeMappingRegistry != null) {
              serviceTypeMappingRegistry.putTypeMappingRegistry(serviceToCreate.getName(), typeMappingRegistry);   
            }           
          }
          JAXBContextRegistry jaxbReg = appTypeMappingContext.getJaxbContextRegistry();
          if (jaxbReg != null) {
            String interfaceMappingID = getInterfaceMappingIDForInterfaceDefinition(applicationName, interfaceDefMetaData.getModuleName(), interfaceId);
            String key1 = JAXBContextRegistry.generateKey(serviceForIDef, interfaceMappingID);
            JAXBContext jaxbContext = jaxbReg.getJAXBContext(key1);
            if (jaxbContext != null) {
              String key_new = JAXBContextRegistry.generateKey(serviceToCreate.getName(), interfaceMappingID);
              jaxbReg.putJAXBContext(key_new, jaxbContext);
            }
          }
          break;
        }
      }    
    }
  }

  private void loadServiceMetaDatasWS(String applicationName, Service serviceToCreate, Service oldService, String serviceName) throws Exception {
    // for the given service one ServiceMetaData object is added in serviceMetaDataRegistry
    WSApplicationMetaDataContext applicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);
    ServiceMetaDataRegistry serviceMetaDataRegistry = applicationMetaDataContext.getServiceMetaDataRegistry();    
    InterfaceDefMetaDataRegistry interfaceMetaDataRegistry = getServiceContext().getMetaDataContext().getInterfaceDefMetaDataRegistry();
    InterfaceDefMetaData interfaceDefMetaData = interfaceMetaDataRegistry.getInterfaceDefMetaData(serviceToCreate.getServiceData().getBindingData()[0].getInterfaceId()); 
    
    String moduleName = interfaceDefMetaData.getModuleName(); 
    if (serviceMetaDataRegistry.containsServiceMetaDataID(serviceName)) {
	  rollbackCreate(serviceToCreate, oldService, applicationName);
      throw new RegistryException("WebServices: There has already been registered ServiceMetaData with id '" + serviceName + "' in ServiceMetaDataRegistry!");
    }
    ServiceMetaData serviceMetaData = new ServiceMetaData(applicationName, moduleName, serviceName);
    serviceMetaDataRegistry.putServiceMetaData(serviceName, serviceMetaData);    
  }

  private void loadServiceRefMetaDatasWSClients(String applicationName, Service serviceToCreate, Service oldService, String serviceName) throws Exception {
    WSClientsApplicationMetaDataContext wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)getServiceRefContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().get(applicationName);
    ServiceRefMetaDataRegistry serviceRefMetaDataRegistry = wsClientsApplicationMetaDataContext.getServiceRefMetaDataRegistry();
    Enumeration moduleRuntimeEnum = wsClientsApplicationMetaDataContext.getModuleRuntimeDataRegistry().getModuleRuntimeDatas().keys();

    //while(moduleRuntimeEnum.hasMoreElements()) {
      String moduleName = (String)moduleRuntimeEnum.nextElement();
      if (serviceRefMetaDataRegistry.containsServiceRefMetaDataID(serviceName)) {
		rollbackCreate(serviceToCreate, oldService, applicationName);
        throw new RegistryException("WSClients: There has already been registered ServiceRefMetaData with id '" + serviceName + "' in ServiceRefMetaDataRegistry!");
      }
      ServiceRefMetaData serviceRefMetaData = new ServiceRefMetaData(applicationName, moduleName, serviceName);
      serviceRefMetaDataRegistry.putServiceRefMetaData(serviceName, serviceRefMetaData);
    //}
  }

  private void loadInterfaceDefMetaDatasWS(String applicationName) throws Exception {
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);
    if(wsApplicationDescriptorContext == null) {
      return;
    }

    ConfigurationDescriptorRegistry configurationDescriptorRegistry = wsApplicationDescriptorContext.getConfigurationDescriptorRegistry();
    Enumeration moduleRuntimeEnum = configurationDescriptorRegistry.getConfigurationDescriptors().keys();
    String moduleName;
    ConfigurationRoot configurationDescriptor;
    while(moduleRuntimeEnum.hasMoreElements()) {
      moduleName = (String)moduleRuntimeEnum.nextElement();
      configurationDescriptor = configurationDescriptorRegistry.getConfigurationDescriptor(moduleName);
      loadInterfaceDefMetaDatasWS(applicationName, moduleName, configurationDescriptor.getDTConfig().getInterfaceDefinition());
    }
  }

  private String getInterfaceMappingIDForInterfaceDefinition(String appName, String moduleName,  String interfaceId) {
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(appName);
    if(wsApplicationDescriptorContext == null) {
      return null;
    }

    ConfigurationDescriptorRegistry configurationDescriptorRegistry = wsApplicationDescriptorContext.getConfigurationDescriptorRegistry();
    ConfigurationRoot configurationDescriptor = configurationDescriptorRegistry.getConfigurationDescriptor(moduleName);

    InterfaceDefinition[] intfDefs = configurationDescriptor.getDTConfig().getInterfaceDefinition();
    for (InterfaceDefinition definition : intfDefs) {
      if (interfaceId.equals(definition.getId())) {
        return definition.getInterfaceMappingId();
      }
    }
    return null;
  }
  
  protected void loadInterfaceDefMetaDatasWS(String applicationName, String moduleName, InterfaceDefinition[] interfaceDefinitions) {
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
      if (interfaceDefMetaDataRegisry.getInterfaceDefMetaData(interfaceDefinitionId) == null) {
        interfaceDefMetaData = new InterfaceDefMetaData(applicationName, moduleName, interfaceDefinitionId);
        interfaceDefMetaDataRegisry.putInterfaceDefMetaData(interfaceDefinitionId, interfaceDefMetaData);  
      }            
    }         
  }
  
  private void loadAdditionalInterfaceDef(Service serviceToCreate) throws Exception {
    BindingData[] bindingDatas = serviceToCreate.getServiceData().getBindingData();
    if(bindingDatas == null) {
      return;
    }
    InterfaceDefMetaDataRegistry interfaceDefMetaDataRegisry = getServiceContext().getMetaDataContext().getInterfaceDefMetaDataRegistry();
    String interfaceId;
    InterfaceDefMetaData interfaceDefMetaData;
    for(int i = 0; i < bindingDatas.length; i++) {
      interfaceId = bindingDatas[i].getInterfaceId().trim();
      interfaceDefMetaData = interfaceDefMetaDataRegisry.getInterfaceDefMetaData(interfaceId);
      if (interfaceDefMetaData != null) {
        interfaceDefMetaData.addService(serviceToCreate.getName());
      }
    }
  }

  // for each BindingData from the service is generated one BindingDataMetaData
  private void loadBindingDataMetaDatas(ConfigurationContext configurationContext, ApplicationConfigurationContext applicationConfigurationContext, Service serviceToUpdate,  Service serviceToDelete, String applicationName, String serviceName, String serviceContextRoot, boolean isCreated) throws Exception {
    BindingData bindingDatas[] = serviceToUpdate.getServiceData().getBindingData();
    if(bindingDatas == null || bindingDatas.length == 0) {
      return;
    }
    
    if(!serviceContextRoot.equals("") && !serviceContextRoot.startsWith("/")) {
      serviceContextRoot = "/" + serviceContextRoot;
    }

    BindingDataMetaDataRegistry globalBindingDataMetaDataRegistry = null;
    Hashtable bindingDataMetaDataRegistries;
    BindingDataMetaDataRegistry bindingDataMetaDataRegistry = new BindingDataMetaDataRegistry();
    if (!isConsumer) {
      WebServicesMetaDataContext metaDataContext = getServiceContext().getMetaDataContext();
      globalBindingDataMetaDataRegistry = metaDataContext.getGlobalBindingDataMetaDataRegistry();
      WSApplicationMetaDataContext applicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);
      bindingDataMetaDataRegistries = applicationMetaDataContext.getBindingDataMetaDataRegistries();
    } else {
      WSClientsMetaDataContext wsClientsMetaDataContext = getServiceRefContext().getWsClientsMetaDataContext();
      globalBindingDataMetaDataRegistry = wsClientsMetaDataContext.getGlobalBindingDataMetaDataRegistry();
      WSClientsApplicationMetaDataContext wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)wsClientsMetaDataContext.getWsClientsApplicationMetaDataContexts().get(applicationName);
      bindingDataMetaDataRegistries = wsClientsApplicationMetaDataContext.getBindingDataMetaDataRegistries();
    }

    BindingData bindingData;
    String bindingDataName;
    String bindingDataUrl;
    BindingDataMetaData bindingDataMetaData;
    for(int i = 0; i < bindingDatas.length; i++) {
      bindingData = bindingDatas[i];
      bindingDataName = bindingData.getName().trim();
      bindingDataUrl = bindingData.getUrl().trim();
      if(!bindingDataUrl.startsWith("/")) {
        bindingDataUrl = "/" + bindingDataUrl;
      }

      String bindingDataMetaDataId = serviceContextRoot + bindingDataUrl;
      if (bindingDataMetaDataId.length() != 1  && !isConsumer && globalBindingDataMetaDataRegistry.containsBindingDataMetaDataID(bindingDataMetaDataId)) {
      	if (isCreated) {
		  rollbackCreate(serviceToUpdate, serviceToDelete, applicationName);      	
      	} else {
      	  rollbackBindingDatas(configurationContext, applicationConfigurationContext, serviceToUpdate, serviceToDelete, applicationName, serviceName, serviceContextRoot);
      	}
        throw new RegistryException(((!isConsumer) ? "WebServices:" : "WSClients:") + " There has already been registered BindingDataMetaData with id '" + bindingDataMetaDataId + "' in globalBindingDataMetaDataRegistry!");
      }
      if (bindingDataMetaDataRegistry.containsBindingDataMetaDataID(bindingDataName)) {
		if (isCreated) {
		  rollbackCreate(serviceToUpdate, serviceToDelete, applicationName);      	
		} else {
		  rollbackBindingDatas(configurationContext, applicationConfigurationContext, serviceToUpdate, serviceToDelete, applicationName, serviceName, serviceContextRoot);
		}
        throw new RegistryException(((!isConsumer) ? "WebServices:" : "WSClients:") + " There has already been registered BindingDataMetaData with id '" + bindingDataName + "' in bindingDataMetaDataRegistry!");
      }
      bindingDataMetaData = new BindingDataMetaData(applicationName, serviceName, bindingDataName);
      globalBindingDataMetaDataRegistry.putBindingDataMetaData(bindingDataMetaDataId, bindingDataMetaData);
      bindingDataMetaDataRegistry.putBindingDataMetaData(bindingDataName, bindingDataMetaData);
    }

    bindingDataMetaDataRegistries.put(serviceName, bindingDataMetaDataRegistry);
  }

  protected void loadServiceWSDLFileDescriptors(String applicationName, String serviceName, WebserviceDescriptionType serviceDescriptor) {   
    if(serviceDescriptor == null) {
      return;
    }                   
   
    loadWSDLFileDescriptors(applicationName, serviceName, serviceDescriptor.getWsdlFile());
  }

  protected void loadWSDLFileDescriptors(String applicationName, String serviceName, WsdlFileType wsdlFileDescriptor) {
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
  
  private void removeServiceDescrptor(Service serviceToDelete, String applicationName) {
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);
    ServiceDescriptorRegistry serviceDescriptorRegistry = wsApplicationDescriptorContext.getServiceDescriptorRegistry();
    WebserviceDescriptionType webserviceDescriptionType = serviceDescriptorRegistry.removeServiceDescriptor(serviceToDelete.getName());
    
    ApplicationWSDLContext interfaceDefWSDLContextRegistry = getServiceContext().getGlobalWSDLContext().getInterfaceDefWSDLContextRegistry();
    removeWSDLDescriptors(serviceToDelete.getName(), interfaceDefWSDLContextRegistry,webserviceDescriptionType.getWsdlFile().getWsdl());
    
    ApplicationServiceTypeMappingContext appTypeMappingContext = (ApplicationServiceTypeMappingContext)getServiceContext().getApplicationServiceTypeMappingContexts().get(applicationName);
    ServiceTypeMappingRegistry serviceTypeMappingRegistry = appTypeMappingContext.getServiceTypeMappingRegistry(ApplicationServiceTypeMappingContext.DEFAULT_USE);
    if(serviceTypeMappingRegistry != null) {
      serviceTypeMappingRegistry.removeTypeMappingRegistry(serviceToDelete.getName());
    }
  }

  private void removeServiceMetaDatasWS(String applicationName, String serviceName) {
    // for the given service the corresponding ServiceMetaData object is removed from serviceMetaDataRegistry
    WSApplicationMetaDataContext applicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);
    ServiceMetaDataRegistry serviceMetaDataRegistry = applicationMetaDataContext.getServiceMetaDataRegistry();
    serviceMetaDataRegistry.removeServiceMetaData(serviceName);
  }

  private void removeServiceRefMetaDatasWSClients(String applicationName, String serviceName) {
    // for the given service the corresponding ServiceRefMetaData object is removed from serviceRefMetaDataRegistry
    WSClientsApplicationMetaDataContext wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)getServiceRefContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().get(applicationName);
    ServiceRefMetaDataRegistry serviceRefMetaDataRegistry = wsClientsApplicationMetaDataContext.getServiceRefMetaDataRegistry();
    serviceRefMetaDataRegistry.removeServiceRefMetaDataDescriptor(serviceName);
  }

  private void removeInterfaceDefMetaDatasWS(Service serviceToDelete) {
    BindingData[] bindingDatas = serviceToDelete.getServiceData().getBindingData();
    if(bindingDatas == null) {
      return;
    }
    InterfaceDefMetaDataRegistry interfaceDefMetaDataRegisry = getServiceContext().getMetaDataContext().getInterfaceDefMetaDataRegistry();
    for(int i = 0; i < bindingDatas.length; i++) {
      String interfaceId = bindingDatas[i].getInterfaceId().trim();
      InterfaceDefMetaData interfaceDefMetaData = interfaceDefMetaDataRegisry.getInterfaceDefMetaData(interfaceId);
      if (interfaceDefMetaData != null) {
        Object[] services = interfaceDefMetaData.getServices().toArray();
        if (services.length == 1 && serviceToDelete.getName().equals((String)services[0])) {
          interfaceDefMetaDataRegisry.removeInterfaceDefMetaData(interfaceId);        
        } else {
          interfaceDefMetaData.removeService(serviceToDelete.getName());
        }
      }      
    }
  }

  private void removeBindingDataMetaDatas(String applicationName, Service serviceToDelete) {
    BindingData[] bindingDatas = serviceToDelete.getServiceData().getBindingData();
    if(bindingDatas == null || bindingDatas.length == 0) {
      return;
    }

    String serviceName = serviceToDelete.getName().trim();
    String serviceContextRoot = serviceToDelete.getServiceData().getContextRoot().trim();
    if(!serviceContextRoot.equals("") && !serviceContextRoot.startsWith("/")) {
      serviceContextRoot = "/" + serviceContextRoot;
    }

    BindingDataMetaDataRegistry globalBindingDataMetaDataRegistry = null;
    BindingDataMetaDataRegistry bindingDataMetaDataRegistry= null;
    Hashtable bindingDataMetaDataRegistries = null;
    if (!isConsumer) {
      WebServicesMetaDataContext metaDataContext = getServiceContext().getMetaDataContext();
      globalBindingDataMetaDataRegistry = metaDataContext.getGlobalBindingDataMetaDataRegistry();
      WSApplicationMetaDataContext applicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);
      bindingDataMetaDataRegistries = applicationMetaDataContext.getBindingDataMetaDataRegistries();
      bindingDataMetaDataRegistry = (BindingDataMetaDataRegistry)applicationMetaDataContext.getBindingDataMetaDataRegistries().get(serviceName);
    } else {
      WSClientsMetaDataContext wsClientsMetaDataContext = getServiceRefContext().getWsClientsMetaDataContext();
      globalBindingDataMetaDataRegistry = wsClientsMetaDataContext.getGlobalBindingDataMetaDataRegistry();
      WSClientsApplicationMetaDataContext wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)wsClientsMetaDataContext.getWsClientsApplicationMetaDataContexts().get(applicationName);
      bindingDataMetaDataRegistries = wsClientsApplicationMetaDataContext.getBindingDataMetaDataRegistries();
      bindingDataMetaDataRegistry = (BindingDataMetaDataRegistry)wsClientsApplicationMetaDataContext.getBindingDataMetaDataRegistries().get(serviceName);
    }

    BindingData bindingData;
    String bindingDataName;
    String bindingDataUrl;
    BindingDataMetaData bindingDataMetaData;
    for(int i = 0; i < bindingDatas.length; i++) {
      bindingData = bindingDatas[i];
      bindingDataName = bindingData.getName().trim();
      bindingDataUrl = bindingData.getUrl().trim();
      if(!bindingDataUrl.startsWith("/")) {
        bindingDataUrl = "/" + bindingDataUrl;
      }

      globalBindingDataMetaDataRegistry.removeBindingDataMetaData(serviceContextRoot + bindingDataUrl);
      if (bindingDataMetaDataRegistry != null) {
        bindingDataMetaDataRegistry.removeBindingDataMetaData(bindingDataName);
      }
    }

    bindingDataMetaDataRegistries.remove(serviceName);
  }
  
  protected void removeWSDLDescriptors(String serviceName, ApplicationWSDLContext wsdlContextRegistry, WsdlType[] wsdlDescriptors) {        
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
                                    
      targetWSDLRegistry.removeWsdlDescriptor(serviceName);
    }                    
  }

  private void removeBindingDatas(ConfigurationContext configurationContext, ApplicationConfigurationContext applicationConfigurationContext, Service serviceToDelete, String applicationName, String serviceName, String serviceContextRoot) {
	BindingDataRegistry globalBindingDataRegistry = configurationContext.getGlobalBindingDataRegistry();
	BindingDataRegistry serviceBindingDataRegistry = (BindingDataRegistry)applicationConfigurationContext.getBindingDataRegistries().get(serviceName);
	BindingData bindingDatas[] = serviceToDelete.getServiceData().getBindingData();
	if (bindingDatas != null && bindingDatas.length != 0) {
	  for (int j = 0; j < bindingDatas.length; j++) {
		BindingData bindingData = bindingDatas[j];
		String bindingDataUrl = bindingData.getUrl().trim();
		if (!bindingDataUrl.startsWith("/")) {
		  bindingDataUrl = "/" + bindingDataUrl;
		}

		globalBindingDataRegistry.removeBindingData(serviceContextRoot + bindingDataUrl);
		if (serviceBindingDataRegistry != null) {
		  serviceBindingDataRegistry.removeBindingData(bindingData.getName().trim());		
		}
	  }
	  applicationConfigurationContext.getBindingDataRegistries().remove(serviceName);
    }
    removeBindingDataMetaDatas(applicationName, serviceToDelete);
  }
  
  private void loadBindingDatas(ConfigurationContext configurationContext, ApplicationConfigurationContext applicationConfigurationContext, Service serviceToUpdate, Service serviceToDelete, String applicationName, String serviceName, String serviceContextRoot, boolean isCreated) throws Exception {
	BindingDataRegistry globalBindingDataRegistry = configurationContext.getGlobalBindingDataRegistry();
	BindingData bindingDatas[] = serviceToUpdate.getServiceData().getBindingData();
	BindingDataRegistry serviceBindingDataRegistry = null;
	if (bindingDatas != null && bindingDatas.length != 0) {
	  serviceBindingDataRegistry = new BindingDataRegistry();
//	  applicationConfigurationContext.getBindingDataRegistries().put(serviceName, serviceBindingDataRegistry);
	  
	  for (int j = 0; j < bindingDatas.length; j++) {
	    BindingData bindingData = bindingDatas[j];
	    String bindingDataUrl = bindingData.getUrl().trim();
	    if (!bindingDataUrl.startsWith("/")) {
	      bindingDataUrl = "/" + bindingDataUrl;
	    }

	    String bindingDataId = serviceContextRoot + bindingDataUrl;
	    if (bindingDataId.length() != 1 && !isConsumer && globalBindingDataRegistry.containsBindingDataID(bindingDataId)) {
	      if (isCreated) {
	        rollbackCreate(serviceToUpdate, serviceToDelete, applicationName);
	      } else {
			rollbackBindingDatas(configurationContext, applicationConfigurationContext, serviceToUpdate, serviceToDelete, applicationName, serviceName, serviceContextRoot);	      
	      }
	      throw new RegistryException((isConsumer ? "WSClients:" : "WebServices:") + " There has already been registered BindingData with id '" + bindingDataId + "' in globalBindingDataRegistry!");
	    }
	    if (serviceBindingDataRegistry.containsBindingDataID(bindingData.getName().trim())) {
		  if (isCreated) {
			rollbackCreate(serviceToUpdate, serviceToDelete, applicationName);
		  } else {	    	
		    rollbackBindingDatas(configurationContext, applicationConfigurationContext, serviceToUpdate, serviceToDelete, applicationName, serviceName, serviceContextRoot);
		  }
	      throw new RegistryException((isConsumer ? "WSClients:" : "WebServices:") + " There has already been registered BindingData with id '" + bindingData.getName().trim() + "' in serviceBindingDataRegistry!");
	    }
	    globalBindingDataRegistry.putBindingData(bindingDataId, bindingData);
	    serviceBindingDataRegistry.putBindingData(bindingData.getName().trim(), bindingData);
	    if (configurationContext.getInterfaceDefinitionRegistry().getInterfaceDefinition(bindingData.getInterfaceId().trim()) == null) {
		  if (isCreated) {
		    rollbackCreate(serviceToUpdate, serviceToDelete, applicationName);
		  } else {
		    rollbackBindingDatas(configurationContext, applicationConfigurationContext, serviceToUpdate, serviceToDelete, applicationName, serviceName, serviceContextRoot);
		  }
	      throw new RegistryException("BindingData '" + bindingData.getName().trim() + "' with url '" + bindingDataId + "' does not refer to interface definition via id '" + bindingData.getInterfaceId().trim() + "'");
	    }
	  }
	  applicationConfigurationContext.getBindingDataRegistries().put(serviceName, serviceBindingDataRegistry);
	}
	loadBindingDataMetaDatas(configurationContext, applicationConfigurationContext, serviceToUpdate, serviceToDelete, applicationName,  serviceName, serviceContextRoot, isCreated);  
  }
  
  private void rollbackCreate(Service serviceToCreate, Service oldService, String applicationName) {       
    try {
      deleteService(serviceToCreate, applicationName);
//    createService(oldService, applicationName);
    } catch (Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);   
	}
    try { 
      if(!isConsumer) {
        WSRTChangesNotificationHandler wsRTChangesNotificationHandler = (WSRTChangesNotificationHandler)getWSRTChangesNotificationHandlers().remove(applicationName);
        if(wsRTChangesNotificationHandler != null) {          
          wsRTChangesNotificationHandler.onRollbackPhase();
        } 
      }    
    } catch(Exception e) { 
      //TODO - warning
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);             
    }    
  }
  
  private void rollbackBindingDatas(ConfigurationContext configurationContext, ApplicationConfigurationContext applicationConfigurationContext, Service serviceToUpdate,  Service serviceToDelete, String applicationName, String serviceName, String serviceContextRoot) {
	try {
	  removeBindingDatas(configurationContext, applicationConfigurationContext, serviceToUpdate, applicationName, serviceName, serviceContextRoot);
	  loadBindingDatas(configurationContext, applicationConfigurationContext, serviceToDelete, serviceToUpdate, applicationName, serviceName, serviceContextRoot, false);
	} catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);
	}
  
    try { 
      if(!isConsumer) {
        WSRTChangesNotificationHandler wsRTChangesNotificationHandler = (WSRTChangesNotificationHandler)getWSRTChangesNotificationHandlers().remove(applicationName);
        if(wsRTChangesNotificationHandler != null) {          
          wsRTChangesNotificationHandler.onRollbackPhase();
        } 
      }    
    } catch(Exception e) { 
      //TODO - warning
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);             
    }   
  }
  
  private InterfaceDefinition[] getInterfaceDefinitionsByApplicationName(BaseServiceContext serviceContext, String applicationName) {    
    ConfigurationContext configurationContext = serviceContext.getConfigurationContext();    
    //TODO - Use an appl<->interfaces registry or add interfaces that have no BindingDatas to them
    ArrayList interfaceNames = new ArrayList();
    ApplicationConfigurationContext applicationConfigurationContext = (ApplicationConfigurationContext)configurationContext.getApplicationConfigurationContexts().get(applicationName);
    if (applicationConfigurationContext != null) {
      Service[] services = applicationConfigurationContext.getServiceRegistry().listServices();
      for (int i = 0; i < services.length; i++) {
        Service service = services[i];
        if (service.getServiceData() != null) {
          BindingData[] bindings = service.getServiceData().getBindingData();
          if(bindings != null) {
            for (int j = 0; j < bindings.length; j++) {
              BindingData binding = bindings[j];
              String ifName = binding.getInterfaceId();
              if(!interfaceNames.contains(ifName)) {
                interfaceNames.add(ifName);
              }
            }
          }
        }
      }
    }

    InterfaceDefinition[] interfaceDefinitions = configurationContext.getInterfaceDefinitionRegistry().listInterfaceDefinitions();
    ArrayList interfacesDefinitionsResult = new ArrayList(interfaceNames.size());
    for (int i = 0; i < interfaceDefinitions.length; i++) {
      InterfaceDefinition ifDef = interfaceDefinitions[i];
      if (interfaceNames.contains(ifDef.getId())) {
        interfacesDefinitionsResult.add(ifDef);
      }
    }
    interfaceDefinitions = new InterfaceDefinition[interfacesDefinitionsResult.size()];
    interfacesDefinitionsResult.toArray(interfaceDefinitions);
    return interfaceDefinitions;
  }
  
  private Service[] getServicesForInterfaceDefinition(String interfaceDefinitionId, Service[] services) { 
    if(services == null || services.length == 0) {
	  return new Service[0]; 	
	}
	
    ArrayList<Service> servicesForInterfaceDefinition = new ArrayList<Service>();
    BindingData[] bindingDatas;
    for(Service service: services) {      
      bindingDatas = service.getServiceData().getBindingData();
      if(bindingDatas != null && bindingDatas.length != 0) {
        for(BindingData bindingData: bindingDatas) {          
          if(bindingData.getInterfaceId().equals(interfaceDefinitionId)) {
            servicesForInterfaceDefinition.add(service);
            break;
          }
        }
      }      
    }
    
    return servicesForInterfaceDefinition.toArray(new Service[servicesForInterfaceDefinition.size()]);
  }  
  
}

