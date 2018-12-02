package com.sap.engine.services.webservices.espbase.client.jaxws.metadata;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import com.sap.engine.services.webservices.espbase.client.api.AttachmentHandler;
import com.sap.engine.services.webservices.espbase.client.api.impl.AttachmentHandlerNYImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.ConfigurationUtil;
import com.sap.engine.services.webservices.espbase.client.bindings.ParameterObject;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientConfigurationContextImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientServiceContextImpl;
import com.sap.engine.services.webservices.espbase.client.jaxws.core.JAXWSProxy;
import com.sap.engine.services.webservices.espbase.client.jaxws.core.WSInvocationHandler;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceData;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.ServiceData;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.jaxb.AttachmentUnmarshallerImpl;
import com.sap.engine.services.webservices.jaxb.ClientAttachmentMarshaller;
import com.sap.tc.logging.Location;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;

public class InterfaceMetadata {
  
  private Hashtable<Method, OperationMetadata> methodsToOperationMetadatasHash;
  private InterfaceMapping interfaceMapping;
  private InterfaceData interfaceData;
  private JAXBContext jaxbContext;
  private Class seiClass;
  
  private static final Location LOCATION = Location.getLocation(InterfaceMetadata.class);
  
  //private ClientServiceContextImpl clientServiceCtx;
  
  public InterfaceMetadata(Class seiClass, InterfaceMapping interfaceMapping) {
    this.interfaceMapping = interfaceMapping;
    this.seiClass = seiClass;
    //this.clientServiceCtx = clientServiceCtx;
    methodsToOperationMetadatasHash = new Hashtable<Method,OperationMetadata>();
  }
  
  public InterfaceMapping getInterfaceMapping() {
    return(interfaceMapping);
  }
  
  protected void addOperationMetadata(Method method, OperationMetadata operationMetadata) {
    methodsToOperationMetadatasHash.put(method, operationMetadata);
  }
  
  public OperationMetadata getOperationMetadata(Method method) {
    return(methodsToOperationMetadatasHash.get(method));
  }
  
  public Object getProxy(QName portName, ClientServiceContextImpl clientServiceContext) {
    ClientConfigurationContextImpl clientConfigurationCtx = (ClientConfigurationContextImpl)createClientConfigurationContext(portName,clientServiceContext);
    
    WSInvocationHandler invocationHandler = new WSInvocationHandler(clientConfigurationCtx, this);
    try {
      seiClass.getClassLoader().loadClass(JAXWSProxy.class.getName());
      return(Proxy.newProxyInstance(seiClass.getClassLoader(), new Class[]{seiClass, JAXWSProxy.class, BindingProvider.class}, invocationHandler));
    } catch (ClassNotFoundException x) {
      // Poxy is loaded from application library.
      LOCATION.debugT("JAX-WS Proxy is loaded from library and does not have reference to webservices library.");
      return(Proxy.newProxyInstance(seiClass.getClassLoader(), new Class[]{seiClass, BindingProvider.class}, invocationHandler));      
    }
    
  }
  
  private JAXBContext getJAXBContext() {
    if(jaxbContext == null) {
      jaxbContext = createJAXBContext();
    }
    return(jaxbContext);
  }
  
  private JAXBContext createJAXBContext() {
    ArrayList<TypeReference> typeNodes = createTypeNodes();
    Class[] classNodes = createClassNodes(typeNodes);
    try {
      JAXBRIContext context = JAXBRIContext.newInstance(classNodes, typeNodes, null, false);
      return(context);
    } catch (Exception x) {
      throw new WebServiceException(x);
    }
  }
  
  private Class[] createClassNodes(ArrayList<TypeReference> typeNodes) {
    Class[] classNodes = new Class[typeNodes.size()];
    for (int i = 0; i < classNodes.length; i++) {
      TypeReference typeRef = typeNodes.get(i);
      classNodes[i] = (Class)(typeRef.type);      
    }
    return(classNodes);
  }
  
  private ArrayList<TypeReference> createTypeNodes() {
    ArrayList<TypeReference> typeNodes = new ArrayList<TypeReference>();
    Enumeration<OperationMetadata> operationMetadatas = methodsToOperationMetadatasHash.elements();
    while(operationMetadatas.hasMoreElements()) {
      OperationMetadata operationMetadata = operationMetadatas.nextElement();
      initTypeNodes(typeNodes, operationMetadata.getOperationParameters());
      initTypeNodes_ReturnParameter(typeNodes, operationMetadata);
      initTypeNodes(typeNodes, operationMetadata.getOperationFaultParameters());
      initTypeNodes_OperationTypeReferences(typeNodes, operationMetadata);
    }
    return(typeNodes);
  }
  
  private void initTypeNodes_OperationTypeReferences(ArrayList<TypeReference> typeNodes, OperationMetadata operationMetadata) {
    Vector<TypeReference> typeRefs = operationMetadata.getTypeReferences();
    for(int i = 0; i < typeRefs.size(); i++) {
      TypeReference typeRef = typeRefs.get(i); 
      typeNodes.add(typeRef);
    }
  }
  
  private void initTypeNodes_ReturnParameter(ArrayList<TypeReference> typeNodes, OperationMetadata operationMetadata) {
    ParameterObject returnParamObject = operationMetadata.getOperationReturnParameter();
    if(returnParamObject != null) {
      initTypeNodes(typeNodes, returnParamObject.typeReference);
    }
  }
  
  private void initTypeNodes(ArrayList<TypeReference> typeNodes, Vector<ParameterObject> parameterObjects) {
    for(int i = 0; i < parameterObjects.size(); i++) {
      ParameterObject paramObject = parameterObjects.get(i);
      initTypeNodes(typeNodes, paramObject.typeReference);
    }
  }
  
  private void initTypeNodes(ArrayList<TypeReference> typeNodes, TypeReference typeRef) {
    if(typeRef != null) {
      typeNodes.add(typeRef);
    }
  }
  
  private InterfaceData getInterfaceData(ClientServiceContextImpl serviceContext) {
    ConfigurationRoot config = serviceContext.getCompleteConfiguration();
    if(interfaceData == null) {
      interfaceData = getInterfaceData(config);
    }
    return(interfaceData);
  }
  
  private InterfaceData getInterfaceData(ConfigurationRoot config) {
    QName portTypeName = interfaceMapping.getPortType();
    String portTypeNs = portTypeName.getNamespaceURI();
    String portTypeLocalName = portTypeName.getLocalPart();
    InterfaceDefinition[] interfaceDefinitions = config.getDTConfig().getInterfaceDefinition();
    for(int i = 0; i < interfaceDefinitions.length; i++) {
      InterfaceDefinition interfaceDefinition = interfaceDefinitions[i];
      InterfaceData interfaceData = interfaceDefinition.getVariant()[0].getInterfaceData();
      if(portTypeNs.equals(interfaceData.getNamespace()) && portTypeLocalName.equals(interfaceData.getName())) {
        return(interfaceData);
      }
    }
    throw new WebServiceException("InterfaceData '" + portTypeName + "' is missing!");
  }
  
  private ClientConfigurationContext createClientConfigurationContext(QName portName, ClientServiceContextImpl clientServiceContext) {
    BindingData bindingData = getBindingData(portName, clientServiceContext.getServiceData());
    try {
      ClientConfigurationContext result = (ConfigurationUtil.createClientConfiguration( bindingData, 
                                                                                        getInterfaceData(clientServiceContext), 
                                                                                        interfaceMapping, 
                                                                                        getJAXBContext(), 
                                                                                        seiClass.getClassLoader(), 
                                                                                        clientServiceContext 
                                                                                        ));
      //initClientConfigurationContext_SOAPApplication(result);
      initClientConfigurationContext_AttachmentHandlers(result);
      return result;
    } catch(Exception exc) {
      throw new WebServiceException(exc);
    }
  }
  
  private void initClientConfigurationContext_AttachmentHandlers(ClientConfigurationContext clientContext) {
    AttachmentHandler attachmentHandler = new AttachmentHandlerNYImpl(clientContext);
    AttachmentMarshaller attachmentMarshaller = new ClientAttachmentMarshaller(attachmentHandler,clientContext);
    AttachmentUnmarshaller attachmentUnmarshaller = new AttachmentUnmarshallerImpl(attachmentHandler);
    clientContext.setAttachmentMarshaller(attachmentMarshaller);
    clientContext.setAttachmentUnmarshaller(attachmentUnmarshaller);
  }
  /*
  private void initClientConfigurationContext_SOAPApplication(ClientConfigurationContext clientContext) {
    PropertyType property = clientContext.getStaticContext().getRTConfig().getSinglePropertyList().getProperty(BuiltInConfigurationConstants.PROTOCOL_ORDER_PROPERTY_QNAME);
    if (property == null) {
      //check for SOAPApplication property
      PropertyType soapApp = clientContext.getStaticContext().getDTConfig().getSinglePropertyList().getProperty(BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME);
      String protocolOrder;
      if (soapApp != null) {
        if (BuiltInConfigurationConstants.SOAPAPPLICATION_CLIENT_EXTENDED_VALUE.equals(soapApp.get_value())) {
          protocolOrder = ConsumerProtocolFactory.EXTENDED_DEPL_SOAP_APP;
        } else {
          protocolOrder = ConsumerProtocolFactory.DEFAULT_DEPL_SOAP_APP;
        }
      } else {
        // TODO: Fix it 
        //protocolOrder = ConsumerProtocolFactory.DEFAULT_DEPL_SOAP_APP;
        protocolOrder = ConsumerProtocolFactory.DEFAULT_STANDALONE_APP;
      }
      PropertyType propType = new PropertyType();
      propType.setNamespace(BuiltInConfigurationConstants.PROTOCOL_ORDER_PROPERTY_QNAME.getNamespaceURI());
      propType.setName(BuiltInConfigurationConstants.PROTOCOL_ORDER_PROPERTY_QNAME.getLocalPart());
      propType.set_value(protocolOrder);        
      clientContext.getStaticContext().getRTConfig().getSinglePropertyList().addProperty(propType);
    }    
  }*/
  
  public static BindingData getBindingData(QName portName, ServiceData serviceData) {
    String portLocalName = portName.getLocalPart();
    BindingData[] bindingDatas = serviceData.getBindingData();
    for(int i = 0; i < bindingDatas.length; i++) {
      BindingData bindingData = bindingDatas[i];
      if(bindingData.getName().equals(portLocalName)) {                
        return(bindingData);
      }
    }
    throw new WebServiceException("Binding data '" + portLocalName + "' is missing!");
  }
}
