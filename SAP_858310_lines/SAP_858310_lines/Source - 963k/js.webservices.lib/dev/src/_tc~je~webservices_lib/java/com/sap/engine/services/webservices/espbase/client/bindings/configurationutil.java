package com.sap.engine.services.webservices.espbase.client.bindings;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.TypeMappingRegistry;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientConfigurationContextImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientServiceContextImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.HTTPTransportBinding;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.SOAPTransportBinding;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.StaticConfigurationContextImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.GenericServiceFactory;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.BuiltInConfigurationConstants;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceData;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.configuration.ServiceData;
import com.sap.engine.services.webservices.espbase.configuration.Variant;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.jaxrpc.encoding.ExtendedTypeMapping;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;
import com.sap.engine.services.webservices.jaxrpc.exceptions.WebserviceClientException;
import commonj.sdo.helper.HelperContext;

/**
 * Utility Class for operations with the configuration api.
 * @author i024072
 *
 */
public class ConfigurationUtil {

  /**
   * Returns specified logical port from the passed service data.
   * @param serviceData
   * @param portName
   * @return returns NULL if there is no binding data.
   * @throws WebserviceClientException
   */
  public static BindingData _getPortBindingData(ServiceData serviceData, QName portName) throws WebserviceClientException {
    BindingData[] bindings = serviceData.getBindingData();
    String portLocalName = portName.getLocalPart();
    BindingData selectedPort = null;
    for (int i=0; i<bindings.length; i++) {
      if (portLocalName.equals(bindings[i].getName())) {
        selectedPort = bindings[i];
      }
    }  
    return selectedPort;
  }
  
  /**
   * Returns new client configuration context for specific port.
   * @param portQName
   * @return
   * @throws WebserviceClientException
   */
  public static ClientConfigurationContext createClientConfiguration(ClientServiceContext serviceContext, BindingData selectedPort) throws WebserviceClientException {
    ConfigurationRoot cfgRoot = serviceContext.getCompleteConfiguration();
    InterfaceData interfaceData = getInterfaceData(selectedPort, cfgRoot);
    InterfaceMapping interfaceMapping = getInterfaceMapping(serviceContext, selectedPort, interfaceData);
    // Finds what serialization framework the client is using. The priority is in this order JAXB, SDO, JAX-RPC
    Object serializationFramework = serviceContext.getJAXBContext();
    if (serviceContext.getProperty(ClientServiceContextImpl.HELPER_CONTEXT) != null) {
      serializationFramework = serviceContext.getProperty(ClientServiceContextImpl.HELPER_CONTEXT);
    } 
    if (serializationFramework == null) {
      serializationFramework = serviceContext.getTypeMappingRegistry();
    }
    return createClientConfiguration(selectedPort,
                                     interfaceData,
                                     interfaceMapping, 
                                     serializationFramework,
                                     serviceContext.getApplicationClassLoader(),
                                     serviceContext);
  }

  public static ClientConfigurationContextImpl createClientConfiguration(BindingData selectedPort,
                                                                         InterfaceData interfaceData,
                                                                         InterfaceMapping interfaceMapping, 
                                                                         Object serializationFramework,
                                                                         ClassLoader classLoader,
                                                                         ClientServiceContext clientServiceCtx) throws WebserviceClientException {    
    ClientConfigurationContextImpl clientContext = new ClientConfigurationContextImpl();    
    // Sets application class loader
    clientContext.setClientApppClassLoader(classLoader);
    // Sets serialziation framework
    if (serializationFramework != null) {
      if (serializationFramework instanceof TypeMappingRegistry) {
        TypeMappingRegistryImpl typeMappingRegistry = (TypeMappingRegistryImpl) serializationFramework; 
        ExtendedTypeMapping typeMapping = typeMappingRegistry.getDefaultTypeMappingImpl();    
        clientContext.setTypeMapping(typeMapping);        
      } else if (serializationFramework instanceof JAXBContext) {
        clientContext.setJAXBContext((JAXBContext) serializationFramework);
      } else if (serializationFramework instanceof HelperContext) {
        clientContext.setHelperContext((HelperContext) serializationFramework);
      }
    }
    // Sets reference to the client service context
    if (clientServiceCtx != null) {
      clientContext.setProperty(ClientConfigurationContextImpl.SERVICE_CONTEXT, clientServiceCtx);
    } else {
      throw new RuntimeException("Client Service Context is required parameter when creating Client Context.");
    }
    // Inits static and persistable sub contexts.
    initStaticConfigurationContext(clientContext, interfaceData, selectedPort, interfaceMapping);
    initPersistableContext(clientContext, clientServiceCtx.isStandalone());
    clientContext.setTransportBinding(getTransportBinding(interfaceMapping.getBindingType()));    
    initProtocolOrder(clientContext);
    return(clientContext);
  }
  
  /**
   * Convert SOAPApplication property to protocolOrder
   * @param clientContext
   */
  private static void initProtocolOrder(ClientConfigurationContext clientContext) {
    PropertyType property = clientContext.getStaticContext().getRTConfig().getSinglePropertyList().getProperty(BuiltInConfigurationConstants.PROTOCOL_ORDER_PROPERTY_QNAME);    
    if (property == null) {
      String protocolOrder = null;
      //    check for SOAPApplication property
      PropertyType soapApp = clientContext.getStaticContext().getDTConfig().getSinglePropertyList().getProperty(BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME);
      ClientServiceContext context = clientContext.getServiceContext(); 
      String clientType = context.getClientType();
      if (ClientServiceContext.DYNAMIC.equals(clientType) || ClientServiceContext.JEE5.equals(clientType)) { //this is Dynamic Client or JEE5
        if (GenericServiceFactory.engineHelper != null) { //TODO: executed on the engine
          if (soapApp != null) {
            protocolOrder = mapSOAPApplication(soapApp.get_value());
          } else {
            protocolOrder = ConsumerProtocolFactory.DEFAULT_DEPL_SOAP_APP;
          }
        } else {
          protocolOrder = ConsumerProtocolFactory.DEFAULT_STANDALONE_APP;
        }
      } else { //this is NY or JAX-RPC client
        if (context.isStandalone()) {
          protocolOrder = ConsumerProtocolFactory.DEFAULT_STANDALONE_APP;
        } else { //this should be deployable client
          if (soapApp != null) {
            protocolOrder = mapSOAPApplication(soapApp.get_value());
          } else {
            protocolOrder = ConsumerProtocolFactory.DEFAULT_DEPL_SOAP_APP;
          }
        }
      }
      if (protocolOrder != null) {
        clientContext.getPersistableContext().setProperty(BuiltInConfigurationConstants.PROTOCOL_ORDER_PROPERTY_QNAME.toString(),protocolOrder);
      }
    }    
  }
  
  private static void initPersistableContext(ClientConfigurationContextImpl clientContext, boolean isStandalone) {
    if (isStandalone) {
      clientContext.getPersistableContext().setProperty(PublicProperties.P_CLIENT_TYPE, "true");
    }
  }
  
  /**
   * Inits static client sub context.
   * @param clientContext
   * @param interfaceData
   * @param selectedPort
   * @param interfaceMapping
   */
  private static void initStaticConfigurationContext(ClientConfigurationContextImpl clientContext, InterfaceData interfaceData, BindingData selectedPort, InterfaceMapping interfaceMapping) {
    StaticConfigurationContextImpl staticConfig = (StaticConfigurationContextImpl) clientContext.getStaticContext();
    staticConfig.setDTConfig(interfaceData);
    staticConfig.setRTConfig(selectedPort);
    staticConfig.setInterfaceData(interfaceMapping);    
  }
  
  private static String mapSOAPApplication(String soapApp) {
    if (BuiltInConfigurationConstants.SOAPAPPLICATION_CLIENT_EXTENDED_VALUE.equals(soapApp)) {
      return ConsumerProtocolFactory.EXTENDED_DEPL_SOAP_APP;
    } else {
      return ConsumerProtocolFactory.DEFAULT_DEPL_SOAP_APP;
    }
  }
  
  /**
   * Returns the corresponding interface data for specific logical port.
   * @param selectedPort
   * @param cfgRoot
   * @return
   * @throws WebserviceClientException
   */
  private static InterfaceData getInterfaceData(BindingData selectedPort, ConfigurationRoot cfgRoot) throws WebserviceClientException {
    String interfaceId = selectedPort.getInterfaceId();
    String variantName = selectedPort.getVariantName();
    InterfaceDefinition[] interfaceDefinitions = cfgRoot.getDTConfig().getInterfaceDefinition();
    InterfaceData interfaceData = null;        

    for (int i = 0; i < interfaceDefinitions.length; i++) {
      if (interfaceId.equals(interfaceDefinitions[i].getId())) {
        Variant[] variants = interfaceDefinitions[i].getVariant();
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
      throw new WebserviceClientException(WebserviceClientException.INVALID_PORT, variantName, interfaceId);
    }    
    return(interfaceData);
  }
  
  /**
   * Returns the corresponding interface data for specific logical port.
   * @param selectedPort
   * @param cfgRoot
   * @return
   * @throws WebserviceClientException
   */
  public static InterfaceData getInterfaceData(InterfaceMapping iMapping, ConfigurationRoot cfgRoot) {
    String interfaceMappingId = iMapping.getInterfaceMappingID();
    InterfaceDefinition[] interfaceDefinitions = cfgRoot.getDTConfig().getInterfaceDefinition();
    for (int i = 0; i < interfaceDefinitions.length; i++) {
      if (interfaceMappingId.equals(interfaceDefinitions[i].getInterfaceMappingId())) {
        Variant[] variants = interfaceDefinitions[i].getVariant();
        InterfaceData interfaceData = variants[0].getInterfaceData();
        return interfaceData;
      }
    }
    return(null);
  }
  
  
  private static InterfaceMapping getInterfaceMapping(ClientServiceContext serviceContext, BindingData selectedPort, InterfaceData interfaceData) {
    MappingRules mappingRules = serviceContext.getMappingRules();    
    QName bindingName = new QName(selectedPort.getBindingNamespace(),selectedPort.getBindingName());
    QName interfaceName = new QName(interfaceData.getNamespace(),interfaceData.getName());
    InterfaceMapping interfaceMapping = mappingRules.getInterface(interfaceName, bindingName);
    return(interfaceMapping);
  }

  /**
   * Returns the correct transport binding instance.
   * @param bindingId
   * @return
   */
  public static TransportBinding getTransportBinding(String bindingType) {
    return(InterfaceMapping.SOAPBINDING.equals(bindingType) ? new SOAPTransportBinding() : new HTTPTransportBinding());
  }
}
