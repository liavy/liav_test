package com.sap.engine.services.webservices.espbase.client.jaxws.core;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executor;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.spi.ServiceDelegate;

import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientServiceContext;
import com.sap.engine.services.webservices.espbase.client.bindings.ConfigurationUtil;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientServiceContextImpl;
import com.sap.engine.services.webservices.espbase.client.jaxws.cts.CTSServiceDelegate;
import com.sap.engine.services.webservices.espbase.client.jaxws.cts.ServiceRefMetaData;
import com.sap.engine.services.webservices.espbase.client.jaxws.metadata.InterfaceMetadataFactory;
import com.sap.engine.services.webservices.espbase.client.jaxws.metadata.InterfaceMetadata;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.BuiltInConfigurationConstants;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationBuilder;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceData;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinitionCollection;
import com.sap.engine.services.webservices.espbase.configuration.ServiceCollection;
import com.sap.engine.services.webservices.espbase.configuration.ServiceData;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.espbase.wsdl.Binding;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.HTTPBinding;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sap.engine.services.webservices.espbase.wsdl.WSDLLoader;
import com.sap.engine.services.webservices.jaxrpc.exceptions.WebserviceClientException;


/**
 * SAP ServiceDelegate implementation.
 * @author I024072
 *
 */
public class SAPServiceDelegate extends ServiceDelegate {
  
  private static InterfaceData DUMMY_SOAP_INTERFACE_DATA = new InterfaceData();
  private static InterfaceMapping DUMMY_SOAP_INTERFACE_MAPPING = new InterfaceMapping();
  private static final String DUMMY_SOAP_INTERFACE_NAMESPACE = "SOAP:NS";
  private static final  QName DUMMY_SOAP_INTERFACE_NAME = new QName(DUMMY_SOAP_INTERFACE_NAMESPACE, "FakeSOAPInterface");
  private static final String DUMMY_SOAP_INTERFACE_MAPPING_ID = "DummySOAPInterfaceMappingId";
  
  private static InterfaceData DUMMY_HTTP_INTERFACE_DATA = new InterfaceData();
  private static InterfaceMapping DUMMY_HTTP_INTERFACE_MAPPING = new InterfaceMapping();
  private static final String DUMMY_HTTP_INTERFACE_NAMESPACE = "HTTP:NS";
  private static final  QName DUMMY_HTTP_INTERFACE_NAME = new QName(DUMMY_HTTP_INTERFACE_NAMESPACE, "FakeHTTPInterface");
  private static final String DUMMY_HTTP_INTERFACE_MAPPING_ID = "DummyHTTPInterfaceMappingId";
  
  static {
    initDummyInterfaceData(DUMMY_SOAP_INTERFACE_DATA, DUMMY_SOAP_INTERFACE_NAME);
    initDummyInterfaceMapping(DUMMY_SOAP_INTERFACE_MAPPING, DUMMY_SOAP_INTERFACE_NAME, DUMMY_SOAP_INTERFACE_MAPPING_ID);
    initDummyInterfaceData(DUMMY_HTTP_INTERFACE_DATA, DUMMY_HTTP_INTERFACE_NAME);
    initDummyInterfaceMapping(DUMMY_HTTP_INTERFACE_MAPPING, DUMMY_HTTP_INTERFACE_NAME, DUMMY_HTTP_INTERFACE_MAPPING_ID);
  }
  
  private static void initDummyInterfaceData(InterfaceData dummyInterfaceData, QName dummyInterfaceName) {
    dummyInterfaceData.setNamespace(dummyInterfaceName.getNamespaceURI());
    dummyInterfaceData.setName(dummyInterfaceName.getLocalPart());
  }
  
  private static void initDummyInterfaceMapping(InterfaceMapping dummyInterfaceMapping, QName dummyInterfaceName, String dummyInterfaceMappingID) {
    dummyInterfaceMapping.setPortType(dummyInterfaceName);
    dummyInterfaceMapping.setInterfaceMappingID(dummyInterfaceMappingID);
    if(dummyInterfaceName.getNamespaceURI().equals(DUMMY_SOAP_INTERFACE_NAMESPACE)) {
      InterfaceMetadataFactory.setBindingType_SOAP11(dummyInterfaceMapping);
    } else {
      dummyInterfaceMapping.setProperty(InterfaceMapping.BINDING_TYPE, InterfaceMapping.HTTPPOSTBINDING);
    }
  }
  
  private URL wsdlURL;
  private Hashtable<Class, InterfaceMetadata> seiClassesToInterfaceMetadatasHash;
  private Hashtable<QName, QName> bindingNameToInterfaceNameHash; 
  private HandlerResolver handlerResolver;
  private ClientServiceContextImpl clientServiceCtx;
  private String serviceRef;
  private List<BindingData> soapDispatchPorts;
  private List<BindingData> httpDispatchPorts;
    
  public SAPServiceDelegate(URL wsdlURL, QName serviceName, Class serviceClass, ServiceRefMetaData serviceRefMetadata) {
    soapDispatchPorts = new ArrayList<BindingData>();
    httpDispatchPorts = new ArrayList<BindingData>();
    ConfigurationRoot configRoot = null;
    Definitions wsdlDefinitions = null;
    if(wsdlURL == null) {
      if(!Service.class.equals(serviceClass)) {
        if (serviceRefMetadata == null) {
          throw new WebServiceException("JAX-WS deployable proxy is instantiated with 'new' instead of been injected. Cannot find WSDL URL for service ["+serviceClass+"] ");
        } else {
          throw new WebServiceException("No WSDL URL is specified for service ["+serviceClass+"] on service creation.");
        }
      } else { // There is no WSDL passed and generic Service class is used.
        configRoot = createDummyConfig(serviceName);
      }
    } else {
      this.wsdlURL = wsdlURL;
      wsdlDefinitions = loadWSDLDefinitions(wsdlURL);
    }
    bindingNameToInterfaceNameHash = createBindingNameToInterfaceNameHash(wsdlDefinitions);
    MappingRules mappingRules = new MappingRules();
    initInterfaceMetadatas(serviceClass, mappingRules, wsdlDefinitions);
    String applicationName = null;    
    String jndiName = null;
    if (serviceRefMetadata != null) {
      applicationName = serviceRefMetadata.getApplicationName();
      configRoot = serviceRefMetadata.getConfigurationDescriptor();
      jndiName = serviceRefMetadata.getServiceJndiName();
    }
    clientServiceCtx = createClientServiceContext(wsdlDefinitions, mappingRules, applicationName, jndiName, serviceClass, serviceName, configRoot);
    handlerResolver = new HandlerResolverImpl(serviceClass, seiClassesToInterfaceMetadatasHash, clientServiceCtx);
  } 
  
  private Hashtable createBindingNameToInterfaceNameHash(Definitions wsdlDefinitions) {
    Hashtable<QName, QName> result = new Hashtable();
    if(wsdlDefinitions != null) {
      ObjectList bindings = wsdlDefinitions.getBindings();
      for(int i = 0; i < bindings.getLength(); i++) {
        Binding binding = (Binding)(bindings.item(i));
        result.put(binding.getName(), binding.getInterface());
      }
    }
    return(result);
  }
  
  /**
   * Configuration that is created when there is not WSDL passed to the service delegate.
   * @param serviceQName
   * @return
   */
  private ConfigurationRoot createDummyConfig(QName serviceQName) {
    ConfigurationRoot result = new ConfigurationRoot();
    InterfaceDefinitionCollection dtConfig = new InterfaceDefinitionCollection();
    dtConfig.setInterfaceDefinition(new InterfaceDefinition[0]);
    result.setDTConfig(dtConfig);
    ServiceCollection rtConfig = new ServiceCollection();
    result.setRTConfig(rtConfig);
    com.sap.engine.services.webservices.espbase.configuration.Service[] services = new com.sap.engine.services.webservices.espbase.configuration.Service[1];    
    com.sap.engine.services.webservices.espbase.configuration.Service service = new com.sap.engine.services.webservices.espbase.configuration.Service();
    services[0] = service;
    service.setName(serviceQName.getLocalPart());
    rtConfig.setService(services);    
    ServiceData serviceData = new ServiceData();
    service.setServiceData(serviceData);
    serviceData.setName(serviceQName.getLocalPart());
    serviceData.setNamespace(serviceQName.getNamespaceURI());
    service.setServiceData(serviceData);    
    return result;
  }  
  
  /**
   * Parses the service class. And for each method annotated with @WebEndpoint annotation loads the SEI metadata for this SEI.
   * @param serviceClass
   */
  private void initInterfaceMetadatas(Class serviceClass, MappingRules mappingRules, Definitions wsdlDefinitions) {
    seiClassesToInterfaceMetadatasHash = new Hashtable();
    ArrayList<InterfaceMapping> iMappings = new ArrayList<InterfaceMapping>();    
    Method[] methods = serviceClass.getMethods();
    for(int i = 0; i < methods.length; i++) {
      Method method = methods[i];
      WebEndpoint webEndpoint = (WebEndpoint)(method.getAnnotation(WebEndpoint.class));
      if(webEndpoint != null) { 
        Class seiClass = method.getReturnType();
        if(!seiClass.equals(void.class) && !seiClassesToInterfaceMetadatasHash.containsKey(seiClass)) {
          InterfaceMetadata interfaceMetadata = InterfaceMetadataFactory.createInterfaceMetadata(seiClass, wsdlDefinitions);
          seiClassesToInterfaceMetadatasHash.put(seiClass, interfaceMetadata);
          iMappings.add(interfaceMetadata.getInterfaceMapping());
        }
      }
    }
    // Sets the interface mappings to the mapping rules.
    InterfaceMapping[] iMappingsArray = new InterfaceMapping[iMappings.size()];
    iMappingsArray = iMappings.toArray(iMappingsArray);
    mappingRules.setInterface(iMappingsArray);
  }
  
  public void setServiceRef(String serviceRef) {
    this.serviceRef = serviceRef;
  }  
  
  /**
   * Loads the WSDL Definitions.
   * @param wsdlURL
   * @return
   */
  private Definitions loadWSDLDefinitions(URL wsdlURL) {
    WSDLLoader wsdlLoader = new WSDLLoader();
    InputStream wsdlInputStream = null;
    try {      
      return(wsdlLoader.load(wsdlURL.toExternalForm()));
    } catch(Exception exc) {
      throw new WebServiceException(exc);
    } finally {
      if(wsdlInputStream != null) {
        try {
          wsdlInputStream.close();
        } catch(IOException ioExc) {
          ioExc.printStackTrace();
        }
      }
    }
  }
  
  /**
   * Returns portInfo for specific port.
   * @param serviceName
   * @param portName
   * @param clientContext
   * @return
   */
  private static PortInfo getPortInfo(QName serviceName, QName portName,String bindingId,String interfaceMappingId) {
    // TODO: Set correct binding ID.
    PortInfo portInfo = new PortInfoImpl(serviceName, portName,bindingId,interfaceMappingId);
    return portInfo;
  }
  
  private void setupHandlerChain(QName portName, BindingProvider client, String interfaceMappingId) {
    PortInfo portInfo = getPortInfo(this.clientServiceCtx.getServiceName(),portName,SOAPBinding.SOAP11HTTP_BINDING,interfaceMappingId); 
    client.getBinding().setHandlerChain(handlerResolver.getHandlerChain(portInfo));    
  }
  
  
  public Object getPort(QName portName, Class seiClass) {
    InterfaceMetadata interfaceMetadata = seiClassesToInterfaceMetadatasHash.get(seiClass);
    if (interfaceMetadata == null) {
      throw new WebServiceException("SEI <" + seiClass.getName() + "> is not known to client " + clientServiceCtx.getName() + "!");
    }
    Object result = interfaceMetadata.getProxy(portName, clientServiceCtx);
    setupHandlerChain(portName,(BindingProvider) result,interfaceMetadata.getInterfaceMapping().getInterfaceMappingID()); 
    CTSServiceDelegate.setCTSPropertiesOnPort(result, seiClass.getName(), serviceRef);
    return(result);
  }
  
  public Object getPort(Class serviceEndpointInterface) {
    if (serviceEndpointInterface == null) {
      throw new IllegalArgumentException("Passed [null] parameter in "+Service.class.getName()+".getPort(Class) method.");
    }
    InterfaceMetadata interfaceMetadata = this.seiClassesToInterfaceMetadatasHash.get(serviceEndpointInterface);
    if (interfaceMetadata == null) {
      // There is no such service loaded
      if (this.wsdlURL != null) {
        Definitions wsdlDefinitions = loadWSDLDefinitions(wsdlURL);
        interfaceMetadata = InterfaceMetadataFactory.createInterfaceMetadata(serviceEndpointInterface, wsdlDefinitions);
        seiClassesToInterfaceMetadatasHash.put(serviceEndpointInterface, interfaceMetadata);
        clientServiceCtx.getMappingRules().addInterface(interfaceMetadata.getInterfaceMapping());
        if (handlerResolver instanceof HandlerResolverImpl) {
          ((HandlerResolverImpl) handlerResolver).appendSEI(serviceEndpointInterface);
        }
      }
    }
    QName bindingQName = interfaceMetadata.getInterfaceMapping().getBindingQName(); 
    Vector<BindingData> ports = getPortsAsVectorBData(bindingQName); 
    if(ports.size() == 0) {
      throw new WebServiceException("No ports are available!");
    }
    String serviceNamespace = clientServiceCtx.getServiceName().getNamespaceURI();
    QName portName = new QName(serviceNamespace, ports.get(0).getName());;
    for (int i=0; i<ports.size(); i++) {
      BindingData bdata = ports.get(i);
      if (bdata.getSinglePropertyList().getProperty(BuiltInConfigurationConstants.DEFAULT_PROPERTIES_NS,BuiltInConfigurationConstants.DEFAULT_LP_FLAG) != null) {
        portName = new QName(serviceNamespace, bdata.getName());;
      }      
    }
    Object result = getPort(portName, serviceEndpointInterface);
    return(result);
  }
  
  public Iterator getPorts() {
    return(getPortsAsVector(null).iterator());
  }
  
  /**
   * Returns all ports pointing to specific binding.
   * @param bindingNameFilter
   * @return
   */
  private Vector<BindingData> getPortsAsVectorBData(QName bindingNameFilter) {
    Vector<BindingData> portNames = new Vector<BindingData>();
    BindingData[] ports = clientServiceCtx.getServiceData().getBindingData();
    for (BindingData port: ports) {
      QName bindingName = new QName(port.getBindingNamespace(), port.getBindingName());
      QName portName = new QName(clientServiceCtx.getServiceName().getNamespaceURI(), port.getName());
      if (bindingNameFilter != null) { // A Port to specific binding is required
        if (bindingName.equals(bindingNameFilter)) {
          portNames.add(port);
        }
      } else {
        portNames.add(port);        
      }
    }
    return(portNames);
  }  
  
  
  /**
   * Returns all ports pointing to specific binding.
   * @param bindingNameFilter
   * @return
   */
  private Vector<QName> getPortsAsVector(QName bindingNameFilter) {
    Vector<QName> portNames = new Vector<QName>();
    BindingData[] ports = clientServiceCtx.getServiceData().getBindingData();
    for (BindingData port: ports) {
      QName bindingName = new QName(port.getBindingNamespace(), port.getBindingName());
      QName portName = new QName(clientServiceCtx.getServiceName().getNamespaceURI(), port.getName());
      if (bindingNameFilter != null) { // A Port to specific binding is required
        if (bindingName.equals(bindingNameFilter)) {
          portNames.add(portName);
        }
      } else {
        portNames.add(portName);        
      }
    }
    return(portNames);
  }  
  
  public QName getServiceName() {
    return(clientServiceCtx.getServiceName());
  }
  
  public URL getWSDLDocumentLocation() {
    return(wsdlURL);
  }
  
  /**
   * Adds port for dispatch calls.
   */  
  public void addPort(QName portName, String bindingId, String endpointAddress) {
    // Throw exception if such port exists.
    searchForExistingWSDLPort(portName);
    searchForExistingDispatchPort(soapDispatchPorts, portName);
    searchForExistingDispatchPort(httpDispatchPorts, portName);
    addDispatchPort(determineDispatchPortsCollector(bindingId), portName, endpointAddress);
  }
  
  private List determineDispatchPortsCollector(String bindingId) {
    if(javax.xml.ws.http.HTTPBinding.HTTP_BINDING.equals(bindingId)) {
      return(httpDispatchPorts);
    }
    if(SOAPBinding.SOAP11HTTP_BINDING.equals(bindingId) ||
       SOAPBinding.SOAP11HTTP_MTOM_BINDING.equals(bindingId) ||
       SOAPBinding.SOAP12HTTP_BINDING.equals(bindingId) || 
       SOAPBinding.SOAP12HTTP_MTOM_BINDING.equals(bindingId)) {
      return(soapDispatchPorts);
    }
    throw new WebServiceException("Binding ID '" + bindingId + "' is not supported!");
  }
  
  private void addDispatchPort(List dispatchPorts, QName portName, String endpointAddress) {
    BindingData bData = new BindingData();
    bData.setName(portName.getLocalPart());
    bData.setUrl(endpointAddress);
    dispatchPorts.add(bData);
  }
  
  private void searchForExistingWSDLPort(QName portName) {
    Vector<QName> wsdlPorts = getPortsAsVector(null);
    for (int i=0; i<wsdlPorts.size(); i++) {
      if (wsdlPorts.get(i).equals(portName)) {
        throw new WebServiceException("Port with name [" + portName + "] already exists !");
      }
    }
  }
  
  private void searchForExistingDispatchPort(List<BindingData> dispatchPorts, QName portName) {
    for (int i=0; i<dispatchPorts.size(); i++) {
      BindingData bdata = dispatchPorts.get(i);
      if (bdata.getName().equals(portName.getLocalPart())) {
        throw new WebServiceException("Port with name ["+portName+"] is defined in the corresponding WSDL or is added!");
      }
    }
  }
  
  /**
   * Returns the interface mapping that is responsible for specific binding.
   * @param bindingQName
   * @return
   */
  public static InterfaceMapping getInterfaceMapping(QName bindingQName,ClientServiceContext context) {
    InterfaceMapping[] interfaces = context.getMappingRules().getInterface();
    for (int i=0; i<interfaces.length; i++) {
      if (bindingQName.equals(interfaces[i].getBindingQName())) {
        return interfaces[i];
      }
    }  
    return null;
  }
  
  /**
   * Returns InterfacMapping using BindingQName and PortTypeQName.
   * @param bindingQName
   * @param portTypeQName
   * @param context
   * @return
   */
  public static InterfaceMapping getInterfaceMapping(QName bindingQName,QName portTypeQName, ClientServiceContext context) {
	InterfaceMapping result = getInterfaceMapping(bindingQName, context);
	if (result == null) {
	  // Searching using portTypeName
	  InterfaceMapping[] interfaces = context.getMappingRules().getInterface();
	  for (int i=0; i<interfaces.length; i++) {
	    if (portTypeQName.equals(interfaces[i].getPortType())) {
	      return interfaces[i];
	    }
	  } 	  
	}
	return result;
  }
  
  /**
   * Returns interface data for specific portType.
   * @param portTypeName
   * @return
   */
  private InterfaceData getInterfaceData(QName portTypeName) {
    InterfaceDefinition[] collection = this.clientServiceCtx.getCompleteConfiguration().getDTConfig().getInterfaceDefinition();
    for (int i=0; i<collection.length; i++) {
      InterfaceData interfaceData = collection[i].getVariant()[0].getInterfaceData();
      QName iDataName = new QName(interfaceData.getNamespace(),interfaceData.getName());
      if (portTypeName.equals(iDataName)) {
        return interfaceData;
      }
    }
    return null;
  }
    
  /**
   * Creates client context for Dispatch only port.
   * @param portName
   * @param jaxbContext
   * @return
   */
  private ClientConfigurationContext createDummyDispatchContext(BindingData bindingData, JAXBContext jaxbContext, boolean isSOAPBinding) {
    try {      
      ClientConfigurationContext context = (ConfigurationUtil.createClientConfiguration( bindingData,            
                                                                                         isSOAPBinding ? DUMMY_SOAP_INTERFACE_DATA : DUMMY_HTTP_INTERFACE_DATA, 
                                                                                         isSOAPBinding ? DUMMY_SOAP_INTERFACE_MAPPING : DUMMY_HTTP_INTERFACE_MAPPING,
                                                                                         jaxbContext, 
                                                                                         getClass().getClassLoader(), 
                                                                                         clientServiceCtx));
      return(context);
    } catch (WebserviceClientException webClientExc) {
      throw new WebServiceException(webClientExc);      
    }    
  }
  
  
  /**
   * Creates client context for existing port.
   * @param portName
   * @param jaxbContext
   * @return
   */
  private ClientConfigurationContext createDispatchContextExistingPort(QName portName, JAXBContext jaxbContext) {
    BindingData bindingData = this.clientServiceCtx.getServiceData().getBindingData(portName);
    if (bindingData == null) {
      throw new WebServiceException("Port with name '" + portName.toString() + "' does not exist !");
    }
    // The BindingData points to specific WSDL Binding.
    // The WSDL Binding must exist in the WSDL from which the WS Client is loaded.    
    QName bindingName = new QName(bindingData.getBindingNamespace(),bindingData.getBindingName());
    QName portTypeName = bindingNameToInterfaceNameHash.get(bindingName);
    if (portTypeName == null) {
      throw new WebServiceException("Port with name '" + portName.toString() + "' does exist in web service configuration but points to unavailable Binding in WSDL ["+bindingName.toString()+"].");
    }    
    InterfaceMapping interfaceMap = getInterfaceMapping(bindingName,portTypeName,this.clientServiceCtx);
    InterfaceData interfaceData = null;
    if (interfaceMap == null) { 
      // No SEI was found but portType exists in the WSDL.
      interfaceData = getInterfaceData(portTypeName);
      interfaceMap = DUMMY_SOAP_INTERFACE_MAPPING;            
    } else {
      interfaceData = getInterfaceData(interfaceMap.getPortType());
    }
    try {
      ClientConfigurationContext result = ConfigurationUtil.createClientConfiguration(bindingData, 
                                                                                      interfaceData, 
                                                                                      interfaceMap, 
                                                                                      jaxbContext, 
                                                                                      this.getClass().getClassLoader(), 
                                                                                      this.clientServiceCtx);
      return result;
    } catch (WebserviceClientException x) {
      throw new WebServiceException(x);      
    }    
  }
  
  private BindingData getDispatchBindingData(List<BindingData> dispatchPorts, QName portName) {
    for (int i = 0; i < dispatchPorts.size(); i++) {
      BindingData bData = dispatchPorts.get(i);
      if (bData.getName().equals(portName.getLocalPart())) {
        return(bData);
      }
    }
    return(null);
  }
  
  private ClientConfigurationContext createDispatchContext(QName portName, JAXBContext jaxbContext) {
    ClientConfigurationContext context = null;
    BindingData dispatchBindingData = getDispatchBindingData(soapDispatchPorts, portName);
    if(dispatchBindingData != null) {
      context = createDummyDispatchContext(dispatchBindingData, jaxbContext, true);
    } else if((dispatchBindingData = getDispatchBindingData(httpDispatchPorts, portName)) != null) {
      context = createDummyDispatchContext(dispatchBindingData, jaxbContext, false);
    } else {
      context = createDispatchContextExistingPort(portName, jaxbContext);          
    }
    return(context);
  }
  
  /**
   * Creates Dispatch Object using SOAP TransportBinding.
   */
  public <T> Dispatch<T> createDispatch(QName portName, Class<T> type, Service.Mode mode) {
    return(createDispatch(portName, type, null, mode));
  }
  
  public Dispatch createDispatch(QName portName, JAXBContext context, Service.Mode mode) {
    return(createDispatch(portName, null, context, mode));
  }
  
  private Dispatch createDispatch(QName portName, Class type, JAXBContext context, Service.Mode mode) {
    ClientConfigurationContext clientContext = createDispatchContext(portName, context);
    String bindingType = clientContext.getStaticContext().getInterfaceData().getBindingType();
    DispatchImpl dispatch = InterfaceMapping.HTTPGETBINDING.equals(bindingType) || 
                            InterfaceMapping.HTTPPOSTBINDING.equals(bindingType) ? new HTTPDispatchImpl(context != null ? JAXBContext.class : type, mode, clientContext)
                                                                                 : new SOAPDispatchImpl(context != null ? JAXBContext.class : type, mode, clientContext);
    setupHandlerChain(portName, dispatch,clientContext.getStaticContext().getInterfaceData().getInterfaceMappingID());
    return(dispatch);
  }
  
  public void setExecutor(Executor executor) {
    this.clientServiceCtx.setExecutor(executor);
  }
  
  public Executor getExecutor() {
    return(this.clientServiceCtx.getExecutor());
  }
  
  public void setHandlerResolver(HandlerResolver handlerResolver) {
    this.handlerResolver = handlerResolver;
  }
  
  public HandlerResolver getHandlerResolver() {
    return(handlerResolver);
  }
  
  /**
   * Returns ServiceData for specific service from the ConfigurationRoot.
   * @param serviceName
   * @param configRoot
   * @return
   */
  private static ServiceData getServiceData(QName serviceName, ConfigurationRoot configRoot) {
    String serviceNs = serviceName.getNamespaceURI();
    String serviceLocalName = serviceName.getLocalPart();
    com.sap.engine.services.webservices.espbase.configuration.Service[] servces = configRoot.getRTConfig().getService();
    for(int i = 0; i < servces.length; i++) {
      com.sap.engine.services.webservices.espbase.configuration.Service service = servces[i];
      ServiceData serviceData = service.getServiceData();  
      if(serviceNs.equals(serviceData.getNamespace()) && serviceLocalName.equals(serviceData.getName())) {
        return(serviceData);
      }
    }
    throw new WebServiceException("ServiceData " + serviceName + " is missing!");
  }
  
  /**
   * Parses the WSDL configuration data.
   * @return
   */
  private static ConfigurationRoot createConfigurationRoot(Definitions wsdl) {
    try {
      ConfigurationBuilder configBuilder = new ConfigurationBuilder(); 
      ConfigurationRoot configRoot = configBuilder.create(wsdl);
      return(configRoot);
    } catch(Exception exc) {
      throw new WebServiceException(exc);
    }
  }

  /**
   * Creates ClientServiceContext for the specific 
   * @param applicationName
   * @param serviceClass
   * @param serviceName
   * @param externalConfig
   * @return
   */
  private static ClientServiceContextImpl createClientServiceContext(Definitions wsdl, MappingRules mappingRules, String applicationName, String jndiName, Class serviceClass, QName serviceName, ConfigurationRoot externalConfig) {
    ClientServiceContextImpl clientServiceCtx = new ClientServiceContextImpl();
    clientServiceCtx.setClientType(ClientServiceContextImpl.JEE5);
    // Sets empty mapping rules.     
    clientServiceCtx.setMappingRules(mappingRules);
    if (externalConfig != null) {
      clientServiceCtx.setCompleteConfiguration(externalConfig);
    } else {
      ConfigurationRoot configRoot = createConfigurationRoot(wsdl);
      clientServiceCtx.setCompleteConfiguration(configRoot);
    }        
    clientServiceCtx.setServiceName(serviceName);
    clientServiceCtx.setServiceData(getServiceData(serviceName, clientServiceCtx.getCompleteConfiguration()));
    if (!Service.class.equals(serviceClass) && serviceClass.getClassLoader() != null) {
      clientServiceCtx.setApplicationClassLoader(serviceClass.getClassLoader());
    }
    if (applicationName != null) {
      clientServiceCtx.setApplicationName(applicationName);
    }    
    if (jndiName != null) {
      clientServiceCtx.setProperty(ClientServiceContextImpl.JNDI_NAME,jndiName);
    }    
    return(clientServiceCtx);
  }

  @Override
  public <T> Dispatch<T> createDispatch(QName portName, Class<T> type, Mode mode, WebServiceFeature... features) {    
    throw new RuntimeException("Method not supported");    
//    return null;
  }

  @Override
  public <T> Dispatch<T> createDispatch(EndpointReference endpointReference, Class<T> type, Mode mode,
      WebServiceFeature... features) {
    throw new RuntimeException("Method not supported");
  }

  @Override
  public Dispatch<Object> createDispatch(QName portName, JAXBContext context, Mode mode, WebServiceFeature... features) {
    throw new RuntimeException("Method not supported");
    //return null;
  }

  @Override
  public Dispatch<Object> createDispatch(EndpointReference endpointReference, JAXBContext context, Mode mode,
      WebServiceFeature... features) {
    throw new RuntimeException("Method not supported");
//    return null;
  }


  @Override
  public <T> T getPort(Class<T> serviceEndpointInterface, WebServiceFeature... features) {
    throw new RuntimeException("Method not supported");
//    return null;
  }

  @Override
  public <T> T getPort(QName portName, Class<T> serviceEndpointInterface, WebServiceFeature... features) {
    throw new RuntimeException("Method not supported");    
//    return null;
  }

  @Override
  public <T> T getPort(EndpointReference endpointReference, Class<T> serviceEndpointInterface, WebServiceFeature... features) {
    throw new RuntimeException("Method not supported");
//    return null;
  }
}
