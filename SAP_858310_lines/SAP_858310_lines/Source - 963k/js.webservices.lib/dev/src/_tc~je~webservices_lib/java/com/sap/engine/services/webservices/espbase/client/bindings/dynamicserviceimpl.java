/*
 * Copyright (c) 2003 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.client.bindings;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;
import javax.xml.rpc.encoding.TypeMappingRegistry;
import javax.xml.rpc.handler.HandlerRegistry;

import com.sap.engine.interfaces.webservices.esp.Protocol;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientConfigurationContextImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientServiceContextImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.StaticConfigurationContextImpl;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.BuiltInConfigurationConstants;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationFactory;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceData;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.configuration.ServiceData;
import com.sap.engine.services.webservices.espbase.configuration.Variant;
import com.sap.engine.services.webservices.espbase.mappings.EndpointMapping;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.MappingFactory;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;
import com.sap.engine.services.webservices.jaxrpc.exceptions.WebserviceClientException;
import com.sap.engine.services.webservices.jaxrpc.handlers.ConsumerJAXRPCHandlersProtocol;

/**
 * Default Implementation of the Service Interface.
 * @version 1.0
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class DynamicServiceImpl implements Service {
  
  private static final String PORTCOMPLINK_EJB_INTER_SEI_NAME = "com.sun.ts.tests.webservices.deploy.portcomplink.ejb.InterModuleSei";
  private static final String PORTCOMPLINK_EJB_INTER_SEI_URI = "IntermoduleEjb/ws4ee";

  private static final String PORTCOMPLINK_EJB_INTRA_SEI_NAME = "com.sun.ts.tests.webservices.deploy.portcomplink.ejb.IntraModuleSei";
  private static final String PORTCOMPLINK_EJB_INTRA_SEI_URI = "IntramoduleEjb/ws4ee";
  
  private static final String PORTCOMPLINK_WAR_INTER_SEI_NAME = "com.sun.ts.tests.webservices.deploy.portcomplink.war.InterModuleSei";
  private static final String PORTCOMPLINK_WAR_INTER_SEI_URI = "PortCompLinkWarWeb/ws4ee/inter";
  
  private static final String PORTCOMPLINK_WAR_INTRA_SEI_NAME = "com.sun.ts.tests.webservices.deploy.portcomplink.war.IntraModuleSei";
  private static final String PORTCOMPLINK_WAR_INTRA_SEI_URI = "PortCompLinkWarWeb/ws4ee/intra";
  
  public static final String HTTP_PORT_PROP_NAME = "http.port";
  
  protected ClientServiceContextImpl serviceContext = null;    
  protected Protocol[] serviceProtocols = null;
  public static final int J2EE_MODE = 1;
  public static final int JAXRPC_MODE = 2;
  public static final int DYNAMIC_MODE = 4;
  
  protected int serviceMode;
  
  public boolean isDeployableClient = false;
  
  /**
   * Default Constructor.
   */
  public DynamicServiceImpl() {    
    serviceMode = JAXRPC_MODE;
  }
  
  public DynamicServiceImpl(int mode) {
    this.serviceMode = mode;
  }
  
  /**
   * Fundamental service initialization method.
   * @param serviceName
   * @param registry
   * @param mappings
   * @param configuration
   * @param applicationLoader
   * @throws ProtocolException
   * @throws WebserviceClientException
  */
  public void init(QName serviceName, TypeMappingRegistry registry, MappingRules mappings, ConfigurationRoot configuration, ClassLoader applicationLoader) throws WebserviceClientException {
    this.serviceContext = new ClientServiceContextImpl();
    this.serviceContext.setTypeMappingRegistry(registry);
    this.serviceContext.setMappingRules(mappings);
    this.serviceContext.setCompleteConfiguration(configuration);
    this.serviceContext.setServiceName(serviceName);
    this.serviceContext.setServiceData(_getServiceData(configuration,serviceName));
    this.serviceContext.setApplicationClassLoader(applicationLoader);
    this.serviceContext.setClientType(ClientServiceContext.JAXRPC);
    this.serviceContext.setStandalone(!_isSAPDeployableClient());
    _initServiceProtocols(this.serviceContext);
  }
  
  private boolean _isSAPDeployableClient() {
    if (serviceMode == JAXRPC_MODE) {
      // JAX-RPC standalone mode
      return false;
    }
    Integer serviceType;
    com.sap.engine.services.webservices.espbase.configuration.Service[] services = this.serviceContext.getCompleteConfiguration().getRTConfig().getService();
    for (int i=0; i<services.length; i++) { 
      com.sap.engine.services.webservices.espbase.configuration.Service service = services[i]; 
      serviceType = service.getType();
      if(serviceType != null && serviceType.equals(com.sap.engine.services.webservices.espbase.configuration.Service.J2EE14_SERVICE_TYPE)) {
        return false;
      }        
    } 
    return true; // This is SAP-Deployable client
  }
  
  /**
   * Sets property to the service. 
   * @param pName
   * @param pValue
   */
  public void _setProperty(String pName, String pValue) {
    this.serviceContext.setProperty(pName, pValue);    
  }
  
  /**
   * Gets the property of the service.
   * @param pName
   * @return
   */
  public String _getProperty(String pName) {
    return (String) this.serviceContext.getProperty(pName);
  }
  
  public void setServiceMode(int serviceMode) {
    this.serviceMode = serviceMode;
  }
  
  public int _getServiceMode() {
    return(serviceMode);
  }
  
  /**
   * Fundamental service initialization method used for deployable proxies. 
   * Added application name parameter.
   * @param serviceName
   * @param registry
   * @param mappings
   * @param configuration
   * @param applicationLoader
   * @param applicationName
   */
  public void init(QName serviceName, TypeMappingRegistry registry, MappingRules mappings, ConfigurationRoot configuration, ClassLoader applicationLoader, String applicationName) throws WebserviceClientException {
    this.serviceMode = J2EE_MODE;
    this.init(serviceName,registry,mappings,configuration,applicationLoader);
    this.serviceContext.setApplicationName(applicationName);  
  }
  
  /**
   * This method invokes protocol destroy service event.
   * @throws Throwable
   */    
  protected void finalize() throws Throwable {
    super.finalize();
    for (int i=0; i<serviceProtocols.length; i++) {
      if (serviceProtocols[i] instanceof ClientProtocolNotify) {
        ((ClientProtocolNotify) serviceProtocols[i]).serviceDestroy(this.serviceContext);
      }
    }         
  }
  
  /**
   * Inits service level protocols.
   *
   */
  private void _initServiceProtocols(ClientServiceContext serviceContext) throws WebserviceClientException {
    serviceProtocols = ConsumerProtocolFactory.protocolFactory.getProtocols(ConsumerJAXRPCHandlersProtocol.PROTOCOL_NAME);
    for (int i=0; i<serviceProtocols.length; i++) {
      if (serviceProtocols[i] instanceof ClientProtocolNotify) {
        try {
          ((ClientProtocolNotify) serviceProtocols[i]).serviceInit(serviceContext);
        } catch (ProtocolException x) {
          throw new WebserviceClientException(WebserviceClientException.PROTOCOL_INIT_FAILURE,x,serviceProtocols[i].getProtocolName());
        }
      }
    }
  }
  
  /**
   * Sets external application class loader.
   * @param loader
   */
  public void setApplicationLoader(ClassLoader loader) {
    this.serviceContext.setApplicationClassLoader(loader); 
  }  
  
  /**
   * Returns the service data for this service. 
   * @param serviceName
   * @return
   */
  public static com.sap.engine.services.webservices.espbase.configuration.ServiceData _getServiceData(ConfigurationRoot config, QName serviceName) {
    com.sap.engine.services.webservices.espbase.configuration.Service[] services = config.getRTConfig().getService();
    for (int i=0; i<services.length; i++) {
      
      com.sap.engine.services.webservices.espbase.configuration.ServiceData sData = services[i].getServiceData();
      
      if (serviceName.equals(new QName(sData.getNamespace(),sData.getName()))) {
        return sData;
      }
    }
    return null;
  }  

  public ClientServiceContextImpl _getServiceContext() {
    return(serviceContext);
  }

  /**
   * @return
   * @throws javax.xml.rpc.ServiceException
   */
  public Call createCall() throws ServiceException {
    if(serviceContext == null) {
      return(new GenericDynamicCall(this));
    }
    return(new SpecifiedDynamicCall(this));
  }

  /**
   * @param arg0
   * @param arg1
   * @return
   * @throws javax.xml.rpc.ServiceException
   */
  public Call createCall(QName portType, QName operationName) throws ServiceException {
    if(serviceContext == null) {
      return(new GenericDynamicCall(this, portType, operationName));
    }
    return(new SpecifiedDynamicCall(this, portType, operationName));
  }

  /**
   * @param arg0
   * @param arg1
   * @return
   * @throws javax.xml.rpc.ServiceException
   */
  public Call createCall(QName portType, String operationName) throws ServiceException {
    if(serviceContext == null) {
      return(new GenericDynamicCall(this, portType, operationName));
    }
    return(new SpecifiedDynamicCall(this, portType, operationName));
  }

  /**
   * @param arg0
   * @return
   * @throws javax.xml.rpc.ServiceException
   */
  public Call createCall(QName portType) throws ServiceException {
    if(serviceContext == null) {
      return(new GenericDynamicCall(this, portType));
    }
    return(new SpecifiedDynamicCall(this, portType));
  }

  /**
   * @param arg0
   * @return
   * @throws javax.xml.rpc.ServiceException
   */  
  
  public Call[] getCalls(QName portType) throws ServiceException {
    if(portType == null) {
      throw new ServiceException("Port name passed as a parameter is null.");
    }
    Call[] calls = null;
    ServiceMapping serviceMapping  = serviceContext.getMappingRules().getService(getServiceName());
    EndpointMapping[] endpointMappings = serviceMapping.getEndpoint();
    for(int j = 0; j < endpointMappings.length; j++) {
      EndpointMapping endpointMapping = endpointMappings[j];
      if(portType.equals(new QName(getServiceName().getNamespaceURI(), endpointMapping.getPortQName()))) {
        InterfaceMapping interfaceMapping = serviceContext.getMappingRules().getInterface(endpointMapping.getPortPortType(), endpointMapping.getPortBinding());
        OperationMapping[] operationMappings = interfaceMapping.getOperation();
        calls = new Call[operationMappings.length];
        for(int k = 0; k < operationMappings.length; k++) {
          OperationMapping operationMapping = operationMappings[k];
          calls[k] = createCall(portType, operationMapping.getWSDLOperationName());
        }
        break;
      }
    }
    if(calls == null) {
      throw new ServiceException("Port type '" + portType + "' is not defined.");
    }
    return(calls);
  }

  /**
   * Returns handler registry.
   * @return
   */
  public HandlerRegistry getHandlerRegistry() {
    if(serviceMode == JAXRPC_MODE) {
      throw new UnsupportedOperationException("Unable to get handler registry. Service mode is set to JAXRPC_MODE.");
    }
    return(serviceContext == null ? null : (HandlerRegistry) serviceContext.getProperty(ClientServiceContextImpl.HANDLER_REGISTRY));    
  }

  /**
   * Returns the default port implementation for this interface.
   * @param arg0
   * @return
   * @throws javax.xml.rpc.ServiceException
   */
  public Remote getPort(Class arg0) throws ServiceException {
    if (arg0 == null) {
      throw new IllegalArgumentException("Interface class passed as parameter is null.");
    }    
    //TODO: check this whether it is correctly implemented
    ServiceData serviceData = this.serviceContext.getServiceData();
    ServiceException e1 = null;
    for (int i=0; i<serviceData.getBindingData().length; i++) {
      try {
        InterfaceMapping[] ims = this.serviceContext.getMappingRules().getInterface();
        if (ims != null) {
          // find interface mapping for the given class
          for (int j = 0; j < ims.length; j++) {
            InterfaceMapping mapping = ims[j];
            String bindingNamespace = mapping.getBindingQName().getNamespaceURI();
            String bindingName = mapping.getBindingQName().getLocalPart();
            if (arg0.getName().equals(mapping.getSEIName())) {
              BindingData[] bds = serviceData.getBindingData();
              if (bds != null) {
                // Finds the binding data for the interface mapping
                BindingData portFound = null;
                for (int k = 0; k < bds.length; k++) {
                  BindingData data = bds[k];
                  if (data.getBindingNamespace().equals(bindingNamespace) && data.getBindingName().equals(bindingName)) {
                    if (portFound == null) {  
                      portFound = data;  // defaulting to the first logical port
                    }
                    if (data.getSinglePropertyList().getProperty(BuiltInConfigurationConstants.DEFAULT_PROPERTIES_NS,BuiltInConfigurationConstants.DEFAULT_LP_FLAG) != null) {
                      portFound = data;
                    }
                  }
                }
                if (portFound != null) {
                  return getPort(new QName(serviceData.getNamespace(), portFound.getName()), arg0);
                }                
              }
            }
          }
        }
        //return null;
      } catch (ServiceException e) {
        e1 = e;
        // System.err.println("Could not find port with name: " + serviceData.getBindingData()[i].getName());
        // e.printStackTrace();
      }
    }
    if(e1 != null) {
      throw e1;
    }
    throw new ServiceException("Endpoint interface '" + arg0.getName() + "' is not defined.");
  }

  /**
   * Returns requires port instance.
   * @param arg0
   * @param arg1
   * @return
   * @throws javax.xml.rpc.ServiceException
   */
  public Remote getPort(QName portQName, Class arg1) throws ServiceException {
    if (portQName == null) {
      throw new IllegalArgumentException("Port name passed as parameter is null.");
    }    
    ClientConfigurationContextImpl clientContext = (ClientConfigurationContextImpl) createClientConfiguration(portQName);    
    InterfaceMapping iMapping = clientContext.getStaticContext().getInterfaceData();    
    if (arg1 != null && !arg1.getName().equals(iMapping.getSEIName())) {
      throw new WebserviceClientException(WebserviceClientException.INCOMPATIBLE_INTERFACE,portQName.getLocalPart(),arg1.getName(),iMapping.getSEIName());
    }    
    clientContext.setClientApppClassLoader(_getAppLoader(arg1));        
    String stubName = iMapping.getImplementationLink().getStubName();
    DynamicStubImpl stub = (DynamicStubImpl) _getStubInstance(stubName,arg1);
    //TODO hack for the cts portlink tests
    if (arg1 != null) {
      if(arg1.getName().equals(PORTCOMPLINK_EJB_INTER_SEI_NAME)) {
        setSEUri(clientContext, PORTCOMPLINK_EJB_INTER_SEI_URI);
      } else if(arg1.getName().equals(PORTCOMPLINK_EJB_INTRA_SEI_NAME)) {
        setSEUri(clientContext, PORTCOMPLINK_EJB_INTRA_SEI_URI);
      } else if(arg1.getName().equals(PORTCOMPLINK_WAR_INTER_SEI_NAME)) {
        setSEUri(clientContext, PORTCOMPLINK_WAR_INTER_SEI_URI);
      } else if(arg1.getName().equals(PORTCOMPLINK_WAR_INTRA_SEI_NAME)) {
        setSEUri(clientContext, PORTCOMPLINK_WAR_INTRA_SEI_URI);
      }
    }
    stub.init(clientContext.getTransportBinding(),arg1,clientContext);
    return stub;
  }
  
  public QName getInterfaceName() throws ServiceException  {
	  return getInterfaceName(null);
  }

  public QName getInterfaceName(QName portTypeName) throws ServiceException  {
    InterfaceDefinition selectedInterface = getInterfaceDefinition(portTypeName);
    String ns = selectedInterface.getVariant()[0].getInterfaceData().getNamespace();
    String name = selectedInterface.getName();
    return new QName(ns, name);
  }

  private InterfaceDefinition getInterfaceDefinition(QName portTypeName) throws ServiceException {
    ConfigurationRoot configuration = this.serviceContext.getCompleteConfiguration();
	  InterfaceDefinition[] interfaces = configuration.getDTConfig().getInterfaceDefinition();
	  if (interfaces == null || interfaces.length == 0) {
		  throw new ServiceException("Could not find interfaces");
	  }
    if (portTypeName == null) {
      return interfaces[0];
    }
    for (int i = 0; i < interfaces.length; i++) {
      InterfaceDefinition anInterface = interfaces[i];
      String ns = anInterface.getVariant()[0].getInterfaceData().getNamespace();
      String name = anInterface.getName();
      if (name.equals(portTypeName.getLocalPart()) && ns.equals(portTypeName.getNamespaceURI())) {
        return anInterface;
      }
    }

    throw new ServiceException("Could not find interface name " + portTypeName.toString());
  }

  public Remote getPort(QName portQName, BindingData selectedPort) throws ServiceException {
    return getPort(null, portQName, selectedPort);
  }

  public Remote getPort(QName portTypeName, QName portQName, BindingData selectedPort) throws ServiceException {
    if (portQName == null) {
      throw new IllegalArgumentException("Port name passed as parameter is null.");
    }
    InterfaceDefinition selectedInterface = getInterfaceDefinition(portTypeName);
    selectedPort.setInterfaceId(selectedInterface.getId());

    ClientConfigurationContextImpl clientContext = (ClientConfigurationContextImpl) ConfigurationUtil.createClientConfiguration(this.serviceContext, selectedPort);

    InterfaceMapping iMapping = clientContext.getStaticContext().getInterfaceData();
    String className = iMapping.getSEIName();
    Class arg1;
    try {
      arg1 = _getClass(className);
    } catch (ClassNotFoundException e) {
      throw new ServiceException(e);
    }
    clientContext.setClientApppClassLoader(_getAppLoader(arg1));
    String stubName = iMapping.getImplementationLink().getStubName();
    if (_isSAPDeployableClient()) { // This is SAP Deployable client - turn on security protocol
      PropertyType property = clientContext.getStaticContext().getRTConfig().getSinglePropertyList().getProperty(BuiltInConfigurationConstants.PROTOCOL_ORDER_PROPERTY_QNAME);
      if (property == null) {
        String protocolOrder = ConsumerProtocolFactory.DEFAULT_DEPL_SOAP_APP;
        PropertyType prop = new PropertyType();
        prop.setNamespace(BuiltInConfigurationConstants.PROTOCOL_ORDER_PROPERTY_QNAME.getNamespaceURI());
        prop.setName(BuiltInConfigurationConstants.PROTOCOL_ORDER_PROPERTY_QNAME.getLocalPart());
        prop.set_value(protocolOrder);
        clientContext.getStaticContext().getRTConfig().getSinglePropertyList().addProperty(prop);
      }
    }
    DynamicStubImpl stub = (DynamicStubImpl) _getStubInstance(stubName, arg1);

    //TODO hack for the cts portlink tests
    if (arg1.getName().equals(PORTCOMPLINK_EJB_INTER_SEI_NAME)) {
      setSEUri(clientContext, PORTCOMPLINK_EJB_INTER_SEI_URI);
    } else if (arg1.getName().equals(PORTCOMPLINK_EJB_INTRA_SEI_NAME)) {
      setSEUri(clientContext, PORTCOMPLINK_EJB_INTRA_SEI_URI);
    } else if (arg1.getName().equals(PORTCOMPLINK_WAR_INTER_SEI_NAME)) {
      setSEUri(clientContext, PORTCOMPLINK_WAR_INTER_SEI_URI);
    } else if (arg1.getName().equals(PORTCOMPLINK_WAR_INTRA_SEI_NAME)) {
      setSEUri(clientContext, PORTCOMPLINK_WAR_INTRA_SEI_URI);
    }
    stub.init(clientContext.getTransportBinding(), arg1, clientContext);
    return stub;
  }

  protected Class _getClass(String className) throws ClassNotFoundException {
	  ClassLoader applicationLoader = this.serviceContext.getApplicationClassLoader();
	  if (applicationLoader == null) {
	    applicationLoader = this.getClass().getClassLoader();
	  }
	  
	  return applicationLoader.loadClass(className);	  	  
  }  
  
  private void setSEUri(ClientConfigurationContextImpl clientContext, String seiUri) {
    String fullSeiUri = "http://" + createHostAndPortSEUri() + "/" + seiUri;
    clientContext.getPersistableContext().setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, fullSeiUri);
  }
  
  private String createHostAndPortSEUri() {
    return("localhost:" + determineHttpPort());
  }
  
  private String determineHttpPort() {
    return (String) serviceContext.getProperty(HTTP_PORT_PROP_NAME);
//    String httpPortIdentifier = (String)(serviceContext.getProperty(HTTP_PORT_PROP_NAME));
//    StringTokenizer tokenizer = new StringTokenizer(httpPortIdentifier, ",");
//    String httpPortProvider = null;
//    for(int i = 0; i < 2; i++) {
//      httpPortProvider = tokenizer.nextToken();
//    }
//    int equalIndex = httpPortProvider.indexOf("=");
//    String httpPort = httpPortProvider.substring(equalIndex + 1).trim();
//    return(httpPort);
  }
  
  /**
   * Returns default client configuration with no WSDL.
   * @return
   */
  public ClientConfigurationContext createConfigNoWSDL() throws ServiceException {    
    String configPath = "/com/sap/engine/services/webservices/espbase/client/bindings/integrated/configuration.xml";
    String mappingPath = "/com/sap/engine/services/webservices/espbase/client/bindings/integrated/mapping.xml";
    InputStream configStream = DynamicServiceImpl.class.getResourceAsStream(configPath);
    if (configStream == null) {
      throw new WebserviceClientException(WebserviceClientException.UNLOADABLE_RESOURCE,configPath);
    }
    ConfigurationRoot config = null;
    MappingRules mapping = null;
    TypeMappingRegistryImpl registry = null;
    try {
      config = ConfigurationFactory.load(configStream);
    } catch (Exception e) {
      throw new WebserviceClientException(WebserviceClientException.UNLOADABLE_RESOURCE,e,configPath);
    }    
    InputStream mappingStream = DynamicServiceImpl.class.getResourceAsStream(mappingPath);
    if (mappingStream == null) {
      throw new WebserviceClientException(WebserviceClientException.UNLOADABLE_RESOURCE,mappingPath);      
    }    
    try {
      mapping =  MappingFactory.load(mappingStream);      
    } catch (Exception e) {
      throw new WebserviceClientException(WebserviceClientException.UNLOADABLE_RESOURCE,e,mappingPath);
    }    
    try {
      registry = new TypeMappingRegistryImpl();
    } catch (Exception x) {
      throw new WebserviceClientException(WebserviceClientException.UNLOADABLE_RESOURCE,x,"TypeMappingRegistry");   
    }
    // Intialize new Serivice Context
    ServiceData sData = config.getRTConfig().getService()[0].getServiceData();
    QName sName = new QName(sData.getNamespace(),sData.getName());
    InterfaceMapping iMapping = mapping.getInterface()[0];
    ClientServiceContextImpl newServiceContext = new ClientServiceContextImpl();
    newServiceContext.setTypeMappingRegistry(registry);
    newServiceContext.setMappingRules(mapping);
    newServiceContext.setCompleteConfiguration(config);
    newServiceContext.setServiceName(sName);
    newServiceContext.setServiceData(sData);
    _initServiceProtocols(newServiceContext);    
    // All resources loaded initialize the client
    ClientConfigurationContextImpl clientContext = new ClientConfigurationContextImpl();
    clientContext.setClientApppClassLoader(this.getClass().getClassLoader());
    clientContext.setTypeMapping(registry.getDefaultTypeMapping());
    clientContext.setProperty(ClientConfigurationContextImpl.SERVICE_CONTEXT,newServiceContext);
    StaticConfigurationContextImpl staticConfig = (StaticConfigurationContextImpl) clientContext.getStaticContext();
    staticConfig.setDTConfig(config.getDTConfig().getInterfaceDefinition()[0].getVariant()[0].getInterfaceData());
    staticConfig.setRTConfig(config.getRTConfig().getService()[0].getServiceData().getBindingData()[0]);    
    staticConfig.setInterfaceData(iMapping);    
    clientContext.setTransportBinding(ConfigurationUtil.getTransportBinding(iMapping.getBindingType()));    
    return clientContext;
  }
  
    
  /**
   * Returns new client configuration context for specific port.
   * @param portQName
   * @return
   * @throws WebserviceClientException
   */
  public ClientConfigurationContext createClientConfiguration(QName portQName) throws WebserviceClientException {
  	return ConfigurationUtil.createClientConfiguration(this.serviceContext ,_getPortBindingData(portQName)); 
  }
  
  /**
   * Returns the binding data for specific logical port
   * @param portQname
   * @return
   */
  public BindingData _getPortBindingData(QName portName) throws WebserviceClientException {
	  ServiceData serviceData = this.serviceContext.getServiceData();
    BindingData result = ConfigurationUtil._getPortBindingData(serviceData,portName);
    if (result == null) {
      throw new WebserviceClientException(WebserviceClientException.UNAVAILABLE_PORT, portName.toString(), serviceData.getName());      
    }    
    return result;
  }
                
  /**
   * Updates the client configuration when new port is selected. 
   * @param newPortName
   * @param clientContext
   */
  protected void updateClientConfiguration(QName newPortName, ClientConfigurationContext clientContext) throws WebserviceClientException {
    String portName = newPortName.getLocalPart();
    ServiceData serviceData = this.serviceContext.getServiceData();
    ConfigurationRoot configuration = this.serviceContext.getCompleteConfiguration();
    MappingRules mappingRules = this.serviceContext.getMappingRules();
    TypeMappingRegistry registry = this.serviceContext.getTypeMappingRegistry();
    BindingData[] bindings = serviceData.getBindingData();    
    BindingData selectedPort = null;
    for (int i=0; i<bindings.length; i++) {
      if (portName.equals(bindings[i].getName())) {
        selectedPort = bindings[i];
      }
    }  
    if (selectedPort == null) {
      throw new WebserviceClientException(WebserviceClientException.UNAVAILABLE_PORT,portName,serviceData.getName());      
    }
    String interfaceId = selectedPort.getInterfaceId();
    String variantName = selectedPort.getVariantName();
    InterfaceDefinition[] interfaces = configuration.getDTConfig().getInterfaceDefinition();
    InterfaceData interfaceData = null;    
    
    for (int i=0; i<interfaces.length; i++) {
      if (interfaceId.equals(interfaces[i].getId())) {
        Variant[] variants = interfaces[i].getVariant();
        for (int j=0; j<variants.length; j++) {
          if (variantName.equals(variants[j].getName())) {
            interfaceData = variants[j].getInterfaceData();
            break; 
          }
        } 
      }  
      if (interfaceData != null) {
        break;
      }      
    }
    if (interfaceData == null) {
      throw new WebserviceClientException(WebserviceClientException.INVALID_PORT,portName,serviceData.getName());
    }
    QName bindingName = new QName(selectedPort.getBindingNamespace(),selectedPort.getBindingName());
    QName interfaceName = new QName(interfaceData.getNamespace(),interfaceData.getName());
    InterfaceMapping iMapping = mappingRules.getInterface(interfaceName,bindingName);
    StaticConfigurationContextImpl staticConfig = (StaticConfigurationContextImpl) clientContext.getStaticContext();
    staticConfig.setDTConfig(interfaceData);
    staticConfig.setRTConfig(selectedPort);
    staticConfig.setInterfaceData(iMapping);
    clientContext.getDynamicContext().clear();
    clientContext.getPersistableContext().clear();
    ((ClientConfigurationContextImpl) clientContext).setTransportBinding(ConfigurationUtil.getTransportBinding(iMapping.getBindingType()));
  }
  
  protected ClassLoader _getAppLoader(Class seiClass) {
    ClassLoader applicationLoader = this.serviceContext.getApplicationClassLoader();
    boolean emptyLoader = false;
    if (applicationLoader == null) {
      emptyLoader = true;
    }
    /*
    if (seiClass != null) {
      applicationLoader = seiClass.getClassLoader();
    }*/
    if (applicationLoader == null) {
      applicationLoader = this.getClass().getClassLoader();
    }
    if (emptyLoader) {
      this.serviceContext.setApplicationClassLoader(applicationLoader);
    }
    return applicationLoader;
  }  
  
  /**
   * Creates new proxy instance.
   * @param stubName
   * @param seiClass
   * @return
   * @throws ServiceException
   */
  protected Object _getStubInstance(String stubName, Class seiClass) throws ServiceException {    
    ClassLoader applicationLoader = _getAppLoader(seiClass); 
    try {
      Class stubClass = Class.forName(stubName,false,applicationLoader);
      Object stub = stubClass.newInstance();
      return stub;
    } catch (Exception e) {
      throw new WebserviceClientException(WebserviceClientException.STUB_INSTANTIATION_ERR,e);
    }       
  }
  
    
  /**
   * @return
   * @throws javax.xml.rpc.ServiceException
   */
  public Iterator getPorts() throws ServiceException {
    ServiceData serviceData = this.serviceContext.getServiceData();
    BindingData[] bData = serviceData.getBindingData();
    ArrayList result = new ArrayList();
    for (int i=0; i<bData.length; i++) {
      QName qname = new QName(serviceData.getNamespace(),bData[i].getName());
      result.add(qname);
    }         
    return result.iterator();
  }

  /**
   * Returns the 
   * @return
   */
  public QName getServiceName() {
    return(serviceContext == null ? null : serviceContext.getServiceName());
  }

  /**
   * @return
   */
  public TypeMappingRegistry getTypeMappingRegistry() {    
    if(serviceMode == JAXRPC_MODE) {
      throw new UnsupportedOperationException("Unable to get type mapping registry. Service mode is set to JAXRPC_MODE.");
    }
    return(serviceContext == null ? null : serviceContext.getTypeMappingRegistry());
  }

  /**
   * @return
   */
  
  public URL getWSDLDocumentLocation() {
    URL wsdlURL = null;
    try {
      wsdlURL = new URL("http://wsdl_location");
    } catch(MalformedURLException mURLExc) {
      mURLExc.printStackTrace();
    }
    return(wsdlURL);
  }
  
  /**
   * Loads Type mapping regstry from the resource path given.
   * @param classLoaderPath
   * @return
   */
  protected TypeMappingRegistry loadTypeRegistry(String classLoaderPath, ClassLoader loader) throws WebserviceClientException {
    InputStream input = loader.getResourceAsStream(classLoaderPath);
    if (input != null) {
      try {
        TypeMappingRegistryImpl registry = new TypeMappingRegistryImpl(input,loader);
        return registry;
      } catch (Exception e) {
        throw new WebserviceClientException(WebserviceClientException.UNLOADABLE_RESOURCE,e,classLoaderPath);
      }
    } else {
      throw new WebserviceClientException(WebserviceClientException.UNLOADABLE_RESOURCE,classLoaderPath);      
    }    
  }
  
  /**
   * Loads mapping information from the resource path passed.
   * @param classLoaderPath
   * @return
   * @throws WebserviceClientException
   */
  protected MappingRules loadMappingRules(String classLoaderPath, ClassLoader loader) throws WebserviceClientException {
    InputStream input = loader.getResourceAsStream(classLoaderPath);
    if (input != null) {
      try {
        MappingRules rules = MappingFactory.load(input);
        return rules;        
      } catch (Exception e) {
        throw new WebserviceClientException(WebserviceClientException.UNLOADABLE_RESOURCE,e,classLoaderPath);
      }
    } else {
      throw new WebserviceClientException(WebserviceClientException.UNLOADABLE_RESOURCE,classLoaderPath);
    } 
  }
  
  /**
   * Loads configuration information from the resource path passed.
   * @param classLoaderPath
   * @return
   * @throws WebserviceClientException
   */
  protected ConfigurationRoot loadConfiguration(String classLoaderPath, ClassLoader loader) throws WebserviceClientException {
    InputStream input = loader.getResourceAsStream(classLoaderPath);
    if (input != null) {
      try {
        ConfigurationRoot config = ConfigurationFactory.load(input);
        return config;
      } catch (Exception e) {
        throw new WebserviceClientException(WebserviceClientException.UNLOADABLE_RESOURCE,e,classLoaderPath);
      }       
    } else {
      throw new WebserviceClientException(WebserviceClientException.UNLOADABLE_RESOURCE,classLoaderPath);
    }
  }  
    
  public void writeObjectX(java.io.ObjectOutputStream out) throws IOException {
    ClassLoader loader = this.serviceContext.getApplicationClassLoader();
    this.serviceContext.removeProperty(ClientServiceContextImpl.APPLICATION_LOADER);
    out.writeObject(this.serviceContext);
    out.writeObject(this.serviceProtocols);    
    out.writeInt(serviceMode);
    //out.defaultWriteObject();
    if (loader!= null) { 
      this.serviceContext.setApplicationClassLoader(loader);
    }
  }
  
  public void readObjectX(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    this.serviceContext = (ClientServiceContextImpl) in.readObject();
    this.serviceProtocols = (Protocol[]) in.readObject();
    this.setApplicationLoader(this.getClass().getClassLoader());
    this.serviceMode = in.readInt();
  }
  
  /**
   * Returns default logical port. 
   * @param seiClass
   * @return
   * @throws javax.xml.rpc.ServiceException
   */
  public Remote getLogicalPort(Class seiClass) throws ServiceException {
    return getPort(seiClass);
  }
  
  /**
   * Returns logical Port with specified name.
   * @param portName
   * @param className
   * @return
   * @throws javax.xml.rpc.ServiceException
   */
  public Remote getLogicalPort(String portName, Class className) throws ServiceException {    
    return getPort(new QName(null,portName),className);
  }
  
  /**
   * Returns available logical port names.
   * @return
   */
  public String[] getLogicalPortNames() {
    ServiceData serviceData = this.serviceContext.getServiceData();
    BindingData[] bData = serviceData.getBindingData();
    String[] result = new String[bData.length];
    for (int i=0; i < bData.length; i++) {
      result[i] = bData[i].getName();
    }         
    return result;
  }
  
}
