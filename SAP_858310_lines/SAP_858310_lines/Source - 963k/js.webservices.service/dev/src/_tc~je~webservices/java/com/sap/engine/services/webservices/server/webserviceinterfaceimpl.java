package com.sap.engine.services.webservices.server;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;

import com.sap.engine.interfaces.webservices.esp.ImplementationContainer;
import com.sap.engine.interfaces.webservices.runtime.component.BaseRegistryException;
import com.sap.engine.interfaces.webservices.runtime.component.ClientProtocolFactory;
import com.sap.engine.interfaces.webservices.runtime.component.ClientTransportBindingFactory;
import com.sap.engine.interfaces.webservices.runtime.component.ProtocolFactory;
import com.sap.engine.interfaces.webservices.runtime.component.TransportBindingFactory;
import com.sap.engine.interfaces.webservices.runtime.definition.WSClientIdentifier;
import com.sap.engine.interfaces.webservices.runtime.definition.WSIdentifier;
import com.sap.engine.interfaces.webservices.server.ServiceContextAccessHandler;
import com.sap.engine.interfaces.webservices.server.WSClientStructure;
import com.sap.engine.interfaces.webservices.server.WebServiceInterface;
import com.sap.engine.interfaces.webservices.server.WebServiceStructure;
import com.sap.engine.interfaces.webservices.server.WebServicesContainerManipulator;
import com.sap.engine.interfaces.webservices.server.event.EventContext;
import com.sap.engine.interfaces.webservices.server.management.WSClientReferencedStructure;
import com.sap.engine.interfaces.webservices.server.management.exception.WSBaseException;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.mappings.ImplementationLink;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.exceptions.WSException;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ServiceBaseServer;
import com.sap.engine.services.webservices.runtime.definition.wsclient.ComponentDescriptor;
import com.sap.engine.services.webservices.runtime.definition.wsclient.WSClientRuntimeInfo;
import com.sap.engine.services.webservices.runtime.registry.wsclient.WSClientRegistry;
import com.sap.engine.services.webservices.server.container.configuration.ApplicationConfigurationContext;
import com.sap.engine.services.webservices.server.container.configuration.ServiceRegistry;
import com.sap.engine.services.webservices.server.container.mapping.ServiceMappingRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.ServiceRefContext;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.ServiceRefGroupMetaData;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.ServiceRefGroupMetaDataRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.ServiceRefMetaData;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.ServiceRefMetaDataRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.WSClientsApplicationMetaDataContext;
import com.sap.engine.services.webservices.server.lcr.api.cim.CIMInstance;
import com.sap.engine.services.webservices.server.lcr.api.cim.CIMValueNamedInstance;
import com.sap.engine.services.webservices.server.lcr.api.cim.CIMValueNamedInstanceList;
import com.sap.engine.services.webservices.server.lcr.api.cimclient.CIMNames;
import com.sap.engine.services.webservices.server.lcr.api.cimclient.CIMOMClient;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.WSClientRuntimetHelper;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.WSClientsConstants;
import com.sap.tc.logging.Location;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2000
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WebServiceInterfaceImpl implements WebServiceInterface {
  
  public WebServiceInterfaceImpl() {
  
  }
  
  public WSClientReferencedStructure[] getWSClientReferencedObjects(String applicationName) throws WSException {
    WSClientRegistry  wsClientRegistry = WSContainer.getWsClientRegistry();
    WSClientRuntimeInfo[] wsClients = wsClientRegistry.getWSClientsByApplicationName(applicationName);
   
    WSClientReferencedStructure[][] wsClientReferencedStructures = new WSClientReferencedStructure[wsClients.length][];  
    WSClientRuntimeInfo wsClientRuntimeInfo; 
    for(int i = 0; i < wsClients.length; i++) {
      wsClientRuntimeInfo = wsClients[i];
      wsClientReferencedStructures[i] = getWSClientReferencedStructure(wsClientRuntimeInfo);      
    }
    
    WSClientReferencedStructure[] wsClientRefererencedStructures630= unifyWSClientReferencedStructures(wsClientReferencedStructures);
    WSClientReferencedStructure[] wsClientReferencedStructures710 = getWSClientsReferencedStructures710(applicationName);
    
    return unifyWSClientReferencedStructures(new WSClientReferencedStructure[][]{wsClientRefererencedStructures630, wsClientReferencedStructures710});
  }

  public void registerProtocolFactory(String id, ProtocolFactory protocolFactory) throws BaseRegistryException {
    //WSContainer.getProtocolFactoryRegistry().registerProtocolFactory(id, protocolFactory);
  }

  public void unregisterProtocolFactory(String id) {
    //WSContainer.getProtocolFactoryRegistry().unregisterProtocolFactory(id);
  }

  public void registerTransportBindingFactory(String id, TransportBindingFactory transportBindingFactory) throws BaseRegistryException {
    WSContainer.getTransportBindingFactoryRegistry().registerTransportBindingFactory(id, transportBindingFactory);
  }

  public void unregisterTransportBindingFactory(String id) {
    WSContainer.getTransportBindingFactoryRegistry().unregisterTransportBindingFactory(id);
  }

  public void registerClientProtocolFactory(String id, ClientProtocolFactory clientProtocolFactory) throws BaseRegistryException {
    WSContainer.getClientProtocolFactoryRegistry().registerClientProtocolFactory(id, clientProtocolFactory);
  }

  public void unregisterClientProtocolFactory(String id) {
    WSContainer.getClientProtocolFactoryRegistry().unregisterClientProtocolFactory(id);
  }

  public void registerClientTransportBindingFactory(String id, ClientTransportBindingFactory clientTransportBindingFactory) throws BaseRegistryException {
    WSContainer.getClientTransportBindingFactoryRegistry().registerClientTransportBindingFactory(id, clientTransportBindingFactory);
  }

  public void unregisterClientTransportBindingFactory(String id) {
    WSContainer.getClientTransportBindingFactoryRegistry().unregisterClientTransportBindingFactory(id);
  }

  public void registerImplementationContainerInterface(ImplementationContainer implementationContainer) throws BaseRegistryException {
    WSContainer.getImplementationContainerManager().register(implementationContainer);
  }

  public void unregisterImplementationContainerInterface(String id) {
    WSContainer.getImplementationContainerManager().unregister(id);
  }

  public EventContext getEventContext() {
    return WSContainer.getEventContext();
  }

  public WebServiceStructure[] listWebServices() {
    WSIdentifier[] wsIdentifiers = WSContainer.getWSRegistry().listWebServices();

    WebServiceStructure[] webServiceStructures = new WebServiceStructure[wsIdentifiers.length];
    for(int i = 0; i < webServiceStructures.length; i++) {
      WSIdentifier wsIdentifier = wsIdentifiers[i];

      WebServiceStructure webServiceStructure = new WebServiceStructure();
      webServiceStructure.setApplicationName(wsIdentifier.getApplicationName());
      webServiceStructure.setArchiveName(wsIdentifier.getJarName());
      webServiceStructure.setWebServiceName(wsIdentifier.getServiceName());

      webServiceStructures[i] = webServiceStructure;
    }

    return webServiceStructures;
  }

  public WSClientStructure[] listWSClients() {
    WSClientIdentifier[] wsClientIds = WSContainer.getWsClientRegistry().listWSClients();

    WSClientStructure[] wsClientStructures = new WSClientStructure[wsClientIds.length];
    for (int i = 0; i < wsClientIds.length; i++) {
      WSClientIdentifier wsClientIdentifier = wsClientIds[i];

      WSClientStructure wsClientStructure = new WSClientStructure();
      wsClientStructure.setApplicationName(wsClientIdentifier.getApplicationName());
      wsClientStructure.setArchiveName(wsClientIdentifier.getJarName());
      wsClientStructure.setProxyName(wsClientIdentifier.getServiceRefName());

      wsClientStructures[i] = wsClientStructure;
    }
    return wsClientStructures;
  }

  private WSClientReferencedStructure[] getWSClientReferencedStructure(WSClientRuntimeInfo wsClientRuntimeInfo) throws WSException {
    String excMsg = "Error occurred trying to get reference to service instance in case of web clients. ";
    WSClientIdentifier wsClientId = wsClientRuntimeInfo.getWsClientId();
    String moduleExtension = IOUtil.getFileExtension(wsClientId.getJarName());

    WSClientReferencedStructure[] wsClientReferencedStructures = new WSClientReferencedStructure[0];
    if (moduleExtension.equals(".war")) {
      ComponentDescriptor[] componentDescriptors = wsClientRuntimeInfo.getComponentDescriptors();

      try {
        int componentsSize = componentDescriptors.length;
        wsClientReferencedStructures = new WSClientReferencedStructure[componentsSize];
        for (int i = 0; i < componentsSize; i++) {
          ComponentDescriptor componentDescriptor = componentDescriptors[i];


          String serviceJndiName = WSClientRuntimetHelper.getServiceJndiNameInRootCtx(wsClientId);
          Reference linkedServiceReference = WSUtil.getLinkReference(serviceJndiName);

          WSClientReferencedStructure wsClientReferencedStructure = new WSClientReferencedStructure();
          wsClientReferencedStructure.setContextRoot(componentDescriptor.getName());
          wsClientReferencedStructure.setJndiLinkName(wsClientRuntimeInfo.getLinkServiceRefName());
          wsClientReferencedStructure.setReferencedObject(linkedServiceReference);

          wsClientReferencedStructures[i] = wsClientReferencedStructure;
        }
      } catch(Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
        wsLocation.catching(excMsg, e);

        Object[] args = new String[]{excMsg, "not available"};
        throw new WSException(WSException.WS_BASE_EXCEPTION, args, e);
      }
    }

    return wsClientReferencedStructures;
  }

  private ServiceBaseServer getServiceBaseServer(String applicationName, String jndiName) throws WSException {
    WSClientIdentifier wsClientID = new WSClientIdentifier(applicationName, "", jndiName);
    String serviceJndiName = WSClientRuntimetHelper.getServiceJndiName(wsClientID);
    try {
      Context proxiesCtx = (Context) new InitialContext().lookup(WSClientsConstants.WS_CLIENTS_PROXY_CONTEXT);
      return (ServiceBaseServer) proxiesCtx.lookup(serviceJndiName);
    } catch (NamingException e) {
      throw new WSException("webservices_4001", new Object[] {applicationName, jndiName}, e);
    }
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.webservices.server.WebServiceInterface#updateWSClientFromSLD(java.lang.String, java.lang.String, java.lang.String, com.sap.engine.interfaces.webservices.server.SLDLogicalPort)
   */
  public Object updateWSClientFromSLD(String applicationName, String jndiName, String lpName, com.sap.engine.interfaces.webservices.server.SLDLogicalPort sldLP) throws WSBaseException {
    WSClientIdentifier wsClientID = new WSClientIdentifier(applicationName, "", jndiName);
    String serviceJndiName = WSClientRuntimetHelper.getServiceJndiName(wsClientID);
    ServiceBaseServer serviceInterface;
    try {
      Context proxiesCtx = (Context) new InitialContext().lookup(WSClientsConstants.WS_CLIENTS_PROXY_CONTEXT);
      serviceInterface = (ServiceBaseServer) proxiesCtx.lookup(serviceJndiName);
    } catch (NamingException e) {
      throw new WSException("webservices_4001", new Object[] {applicationName, jndiName}, e);
    }
    try {
      return serviceInterface.updateLPTypeFromSLD(lpName, sldLP);
    } catch (Throwable thr) {
      throw new WSException("webservices_4002", new Object[0], thr);
    }
  }

  public void changeEndpointURL(String applicationName, String jndiName, String lpName, String newURL) throws WSBaseException {
    ServiceBaseServer serviceInterface = getServiceBaseServer(applicationName, jndiName);
    try {
      serviceInterface.changeEndpointURL(lpName, newURL);
    } catch (Throwable thr) {
      throw new WSException("webservices_4003", new Object[0], thr);
    }
  }

  private Vector getSLDEntries(String className, Properties props) throws WSException {
    CIMOMClient cimClient = null;
    try {
      cimClient = SLDConnectionImpl.getCIMOMClient();
      CIMValueNamedInstanceList instances = cimClient.enumerateInstances(CIMNames.getCimClassNameValue(className), false, true, true, true, null);
      int size = instances.size();
      Vector entries = new Vector(size);
      for (int i = 0; i < size; i++) {
        CIMValueNamedInstance valueNamedInstance = instances.get(i);
        CIMInstance instance = valueNamedInstance.getInstance();
        String nameProp = instance.getPropertyValue(CIMNames.getProperty("P_NAME"));
        String captionProp = instance.getPropertyValue(CIMNames.getProperty("P_CAPTION"));
        if (captionProp == null) {
          captionProp = nameProp;
        }
        boolean skip = false;
        if (props != null) {
          Enumeration propsEnum = props.keys();
          while (propsEnum.hasMoreElements()) {
            String property = propsEnum.nextElement().toString();
            String value = props.getProperty(property);
            String realValue = instance.getPropertyValue(CIMNames.getProperty(property));
            if (!value.equals(realValue)) {
              skip = true;
              break;
            }
          }
        }
        if (!skip) {
          Vector entry = new Vector();
          entry.addElement(nameProp);
          entry.addElement(captionProp);
          entries.addElement(entry);
        }
      }
      return entries;
    } catch (Throwable thr) {
      throw new WSException("webservices_4004", new Object[0], thr);
    } finally {
      if (cimClient != null) {
        try {
          cimClient.disconnect();
        } catch (Throwable thr) {
          throw new WSException("webservices_4005", new Object[0], thr);
        }
      }
    }
  }

  public Vector getAvailableSLDSystems() throws WSBaseException {
    return getSLDEntries("C_SAP_J2EEEngineCluster", null);
  }

  public Vector getAvailableSLDWebServices(String systemID) throws WSBaseException {
    Properties props;
    if (systemID == null || "".equals(systemID)) {
      props = null;
    } else {
      props = new Properties();
      props.setProperty("P_SYSTEMNAME", systemID);
    }

    return getSLDEntries("C_SAP_WebService", props);
  }

  public Vector getAvailableSLDWSPorts(String systemID, String wsID) throws WSBaseException {
    Properties props;
    if (systemID == null || "".equals(systemID)) {
      props = null;
    } else {
      props = new Properties();
      props.setProperty("P_SYSTEMNAME", systemID);
    }

    return getSLDEntries("C_SAP_WebServicePort", props);
  }
  
  public WebServicesContainerManipulator getWebServicesContainerManipulator(boolean isConsumer) {
    WebServicesContainerManipulatorImpl wsContainerManipulator = null;
    if (!isConsumer) {
      wsContainerManipulator = WebServicesContainer.getWSContainerManipulator();
    } else {
      wsContainerManipulator = WebServicesContainer.getWSClientsContainerManipulator();
    }
    //flag necessary to disctinct whether webservice or wsclient will be modified
    wsContainerManipulator.setConsumer(isConsumer);  
    return wsContainerManipulator;
  }

  public ServiceContextAccessHandler getServiceContextAccessHandler() {
    return WebServicesContainer.getServiceContext();
  }
  
  private WSClientReferencedStructure[] getWSClientsReferencedStructures710(String applicationName) {
    ServiceRefContext serviceContext = WebServicesContainer.getServiceRefContext();
        
    ApplicationConfigurationContext appConfigurationContext = (ApplicationConfigurationContext)serviceContext.getConfigurationContext().getApplicationConfigurationContexts().get(applicationName);
    if(appConfigurationContext == null) {
      return new WSClientReferencedStructure[0]; 	
    }
    
    WSClientsApplicationMetaDataContext appMetaDataContext = (WSClientsApplicationMetaDataContext)serviceContext.getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().get(applicationName);
    ServiceRefMetaDataRegistry serviceRefMetaDataRegistry = appMetaDataContext.getServiceRefMetaDataRegistry(); 
    ServiceRefGroupMetaDataRegistry serviceRefGroupMetaDataRegistry = appMetaDataContext.getServiceRefGroupMetaDataRegistry();
    ServiceMappingRegistry serviceMappingRegistry = serviceContext.getMappingContext().getServiceMappingRegistry(); 
             
    ServiceRegistry serviceRegistry = appConfigurationContext.getServiceRegistry();    
    Enumeration<Service> enumer = serviceRegistry.getServices().elements(); 
    Service service;     
    ServiceRefMetaData serviceRefMetaData; 
    ServiceRefGroupMetaData serviceRefGroupMetaData;  
    ServiceMapping serviceMapping; 
    ImplementationLink implementationLink; 
    String componentMode; 
    ArrayList<WSClientReferencedStructure> wsClientReferencedStructures = new ArrayList<WSClientReferencedStructure>(); 
    WSClientReferencedStructure wsClientReferencedStructure;     
    while(enumer.hasMoreElements()) {      
      service = enumer.nextElement();      
      serviceRefMetaData = serviceRefMetaDataRegistry.getServiceRefMetaData(service.getName());
      serviceRefGroupMetaData = serviceRefGroupMetaDataRegistry.getServiceRefGroupMetaData(serviceRefMetaData.getServiceRefGroupName()); 
      if(serviceRefGroupMetaData.getModuleName().endsWith(".war")) {      
	    serviceMapping = serviceMappingRegistry.getServiceMapping(service.getServiceMappingId());
	    implementationLink = serviceMapping.getImplementationLink();
	    componentMode = implementationLink.getProperty("IsComponentMode"); 
	    if(componentMode != null && componentMode.equals("true")) {
	      wsClientReferencedStructure = new WSClientReferencedStructure();	    
	      wsClientReferencedStructure.setContextRoot(implementationLink.getProperty("ComponentName")); 
	      wsClientReferencedStructure.setJndiLinkName(implementationLink.getServiceRefJNDIName());
	      wsClientReferencedStructure.setReferencedObject(new Reference("wsclients/proxies/" + applicationName + "/" + implementationLink.getServiceRefJNDIName(), WSClientsConstants.COMPONENT_FACTORY, null));
	      wsClientReferencedStructures.add(wsClientReferencedStructure);
	    }
      }
    }
    
    return wsClientReferencedStructures.toArray(new WSClientReferencedStructure[wsClientReferencedStructures.size()]);
  } 
  
  private WSClientReferencedStructure[] unifyWSClientReferencedStructures(WSClientReferencedStructure[][] wsClientReferencedStructures) {
    if(wsClientReferencedStructures == null || wsClientReferencedStructures.length == 0) {
      return new WSClientReferencedStructure[0]; 
    }
	
    if(wsClientReferencedStructures.length == 1) {
      return wsClientReferencedStructures[0]; 	
    }
    
    ArrayList<WSClientReferencedStructure> wsClientReferencedStructuresList = new ArrayList<WSClientReferencedStructure>();      
    for(WSClientReferencedStructure[] currentWSClientReferencedStructures: wsClientReferencedStructures) {
      addWSClientReferencedStructures(currentWSClientReferencedStructures, wsClientReferencedStructuresList);  	
    }    	 
    
    return wsClientReferencedStructuresList.toArray(new WSClientReferencedStructure[wsClientReferencedStructuresList.size()]);
  }
  
  private void addWSClientReferencedStructures(WSClientReferencedStructure[] wsClientReferencedStructures, ArrayList<WSClientReferencedStructure> wsClientReferencedStructuresList) {
    if(wsClientReferencedStructures == null || wsClientReferencedStructures.length == 0) {
      return; 	
    }	  
    
    for(WSClientReferencedStructure wsClientReferencedStructure: wsClientReferencedStructures) {
      wsClientReferencedStructuresList.add(wsClientReferencedStructure);     	
    }    
  } 
}
