package com.sap.engine.services.webservices.espbase.client.jaxws.metadata;

import java.awt.Image;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.util.concurrent.Future;

import javax.activation.DataHandler;
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingType;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebFault;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.http.HTTPBinding;

import com.sap.engine.services.webservices.espbase.client.bindings.ParameterObject;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.JAXWSUtil;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.espbase.wsdl.Binding;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sap.engine.services.webservices.espbase.wsdl.Operation;
import com.sap.engine.services.webservices.espbase.wsdl.Parameter;
import com.sap.engine.services.webservices.espbase.wsdl.SOAPBindingOperation;
import com.sap.engine.services.webservices.espbase.wsdl.Service;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;
import com.sun.xml.bind.api.TypeReference;

public class InterfaceMetadataFactory {
  
  private static final String WRAPPER_BEAN_CLASSES_DEFAULT_SUBPACKAGE = "jaxws";
  private static final String RESPONSE_WRAPPER_BEAN_DEFAULT_CLASS_NAME_SUFFIX = "Response";
  private static final String DEFAULT_PARAMETER_NAME_PREFIX = "arg";
  private static final String DEFAULT_RETURN_PARAMETER_NAME = "return";
  private static final String GET_FAULT_INFO_METHOD_NAME = "getFaultInfo";
  private static final String DEFAULT_FAULT_BEAN_CLASS_NAME_SUFIX = "Bean";
  private static final String TRUE_BOOLEAN_PROPERTY_VALUE = "true";
  private static final String FALSE_BOOLEAN_PROPERTY_VALUE = "false";
  private static final String ASYNC_METHOD_NAME_SUFIX = "Async";

  /**
   * Singleton utility class for conversion of Type to Class.
   */
  public static final TypeErasure TYPE_ERASER = new TypeErasure();
  
  public static InterfaceMetadata createInterfaceMetadata(Class seiClass, Definitions wsdlDefinitions) {
    InterfaceMapping interfaceMapping = new InterfaceMapping();
    InterfaceMetadata interfaceMetadata = new InterfaceMetadata(seiClass, interfaceMapping);

    WebService webService = (WebService)(seiClass.getAnnotation(WebService.class));
    setInterfaceMappingPortType(seiClass, interfaceMapping, webService);
    
    SOAPBinding soapBinding = (SOAPBinding)(seiClass.getAnnotation(SOAPBinding.class));
    SOAPBindingConfiguration seiSoapBindingCfg = createSEISOAPBindingConfiguration(soapBinding);
    
    interfaceMapping.setJAXWSInterfaceFlag(true);
    setSEIName(interfaceMapping, seiClass);

    BindingType bindingType = (BindingType)(seiClass.getAnnotation(BindingType.class));
    setBinding(seiClass, interfaceMapping, wsdlDefinitions, bindingType);
    
    setInterfaceMappingID(interfaceMapping);
    setOperations(interfaceMetadata, interfaceMapping, seiClass, seiSoapBindingCfg, wsdlDefinitions.getBinding(interfaceMapping.getBindingQName()));
    
    return(interfaceMetadata);
  }
  
  private static void setInterfaceMappingID(InterfaceMapping interfaceMapping) {
    UID uid = new UID();
    interfaceMapping.setProperty(InterfaceMapping.INTERFACE_MAPPING_ID, uid.toString());
  }
  
  private static void setOperations(InterfaceMetadata interfaceMetadata, InterfaceMapping interfaceMapping, Class seiClass, SOAPBindingConfiguration seiSoapBindingCfg, Binding binding) {
    Method[] methods = seiClass.getMethods();
    for(int i = 0; i < methods.length; i++) {
      Method method = methods[i];
      Object[] operations = createOperations(seiClass, interfaceMapping, method, seiSoapBindingCfg, binding);
      if(operations != null) {
        interfaceMetadata.addOperationMetadata(method, (OperationMetadata)operations[0]);
        if(operations[1] != null) {
          interfaceMapping.addOperation((OperationMapping)operations[1]);
        }
      }
    }
  }
  
  private static SOAPBindingConfiguration createSEISOAPBindingConfiguration(SOAPBinding soapBinding) {
    SOAPBindingConfiguration soapBindingCfg = new SOAPBindingConfiguration(); 
    if(soapBinding != null) {
      initSOAPBindingConfiguration(soapBinding, soapBindingCfg);
    }
    return(soapBindingCfg);
  }
  
  private static void initSOAPBindingConfiguration(SOAPBinding soapBinding, SOAPBindingConfiguration soapBindingCfg) {
    setParameterStyle(soapBinding, soapBindingCfg);
    setStyle(soapBinding, soapBindingCfg);
    setUse(soapBinding, soapBindingCfg);
  }

  private static void setUse(SOAPBinding soapBinding, SOAPBindingConfiguration soapBindingCfg) {
    SOAPBinding.Use use = soapBinding.use();
    if(use != null) {
      soapBindingCfg.setUse(use);
    }
  }
  
  private static void setStyle(SOAPBinding soapBinding, SOAPBindingConfiguration soapBindingCfg) {
    SOAPBinding.Style style = soapBinding.style();
    if(style != null) {
      soapBindingCfg.setStyle(style);
    }
  }
  
  private static void setParameterStyle(SOAPBinding soapBinding, SOAPBindingConfiguration soapBindingCfg) {
    SOAPBinding.ParameterStyle paramStyle = soapBinding.parameterStyle();
    if(paramStyle != null) {
      soapBindingCfg.setParameterStyle(paramStyle);
    }
  }

  private static Object[] createOperations(Class seiClass, InterfaceMapping interfaceMapping, Method method, SOAPBindingConfiguration seiSoapBindingCfg, Binding binding) {
    WebMethod webMethod = (WebMethod)(method.getAnnotation(WebMethod.class));
    
    if(webMethod != null && webMethod.exclude()) {
      return(null);
    }

    Method syncMethod = determineSyncMethod(seiClass, method);
    return(syncMethod == null ? createSyncOperations(seiClass, interfaceMapping, method, seiSoapBindingCfg, binding) : createAsyncOperations(syncMethod));
  }
  
  private static Method determineSyncMethod(Class seiClass, Method method) {
    String methodName = method.getName();
    int asyncSufixIndex = methodName.endsWith(ASYNC_METHOD_NAME_SUFIX) ? methodName.lastIndexOf(ASYNC_METHOD_NAME_SUFIX) : -1;
    if(asyncSufixIndex > 0) {
      Class returnClass = method.getReturnType();
      if(Future.class.isAssignableFrom(returnClass)) {
        Class[] paramClasses = method.getParameterTypes();
        Class[] syncParamClasses = paramClasses.length != 0 && AsyncHandler.class.isAssignableFrom(paramClasses[paramClasses.length - 1]) ? createSyncParameterClassesArray(paramClasses) : paramClasses;
        return(determineSyncMethod(seiClass, methodName, asyncSufixIndex, syncParamClasses));
      }
    }
    return(null);
  }
  
  private static Class[] createSyncParameterClassesArray(Class[] paramClasses) {
    Class[] syncParamClasses = new Class[paramClasses.length - 1];
    System.arraycopy(paramClasses, 0, syncParamClasses, 0, paramClasses.length - 1);
    return(syncParamClasses);
  }
  
  private static Method determineSyncMethod(Class seiClass, String asyncMethodName, int asyncSufixIndex, Class[] syncParamClasses) {
    String syncMethodName = asyncMethodName.substring(0, asyncSufixIndex);
    Method syncMethod = null;
    try {
      syncMethod = seiClass.getDeclaredMethod(syncMethodName, syncParamClasses);
    } catch(NoSuchMethodException noSuchMethodExc) {
      return(null);
    }
    return(syncMethod);
  }

  private static Object[] createAsyncOperations(Method syncMethod) {
    OperationMetadata operationMetadata = new OperationMetadata();
    operationMetadata.setSyncMehtod(syncMethod);
    return(new Object[]{operationMetadata, null});
  }
  
  private static Object[] createSyncOperations(Class seiClass, InterfaceMapping interfaceMapping, Method method, SOAPBindingConfiguration seiSoapBindingCfg, Binding binding) {
    WebMethod webMethod = (WebMethod)(method.getAnnotation(WebMethod.class));
    
    if(webMethod != null && webMethod.exclude()) {
      return(null);
    }
    
    OperationMetadata operationMetadata = new OperationMetadata();
    OperationMapping operationMapping = new OperationMapping();

    operationMapping.setJavaMethodName(method.getName());
    setOperationMapping_WSDLOperationName_SoapAction(operationMapping, webMethod, method);
    
    SOAPBinding soapBinding = (SOAPBinding)(method.getAnnotation(SOAPBinding.class));
    SOAPBindingConfiguration soapBindingCfg = createMethodSOAPBindingConfiguration(operationMapping, soapBinding, seiSoapBindingCfg);
    
    setOperationStyle(operationMapping, soapBindingCfg);
    setOperationUse(operationMapping, soapBindingCfg);
    
    Oneway oneway = (Oneway)(method.getAnnotation(Oneway.class));
    setOperationMep(operationMapping, oneway);
    
    if(soapBindingCfg.isDocumentWrapped()) {
      setRequestWrapperBean_DocumentWrappedStyle(seiClass, interfaceMapping, operationMetadata, operationMapping, method);
      if(OperationMapping.MEP_REQ_RESP.equals(operationMapping.getProperty(OperationMapping.OPERATION_MEP))) {
        setResponseWrapperBean_DocumentWrappedStyle(seiClass, interfaceMapping, operationMetadata, operationMapping, method);
      }
    }
    
    IndexProvider indexProvider = new IndexProvider(); 
    Service service = determineService(binding);
    SOAPBindingOperation soapBindingOperation =  determineSOAPBindingOperation(seiClass, binding, operationMapping);
    
    try {
      setParameters(operationMetadata, operationMapping, seiClass, interfaceMapping, method, soapBindingCfg, service, indexProvider, soapBindingOperation.getReferencedOperation());
    } catch(WSDLException wsdlExc) {
      throw new IllegalArgumentException(wsdlExc);
    }
    
    if(soapBindingCfg.isRpcLiteral()) {
      set_SOAPRequestWrapper_InputNS_RpcStyle(operationMapping, soapBindingOperation);
    }
    return(new Object[]{operationMetadata, operationMapping});
  }
  
  private static void setResponseWrapperBean_DocumentWrappedStyle(Class seiClass, 
                                                                  InterfaceMapping interfaceMapping,
                                                                  OperationMetadata operationMetadata,
                                                                  OperationMapping operationMapping, 
                                                                  Method method) {
    ResponseWrapper responseWrapper = (ResponseWrapper)(method.getAnnotation(ResponseWrapper.class));
    String annotationResponseWrapperBeanClassName = null;
    String annotationResponseNamespace = null;
    String annotationResponseName = null;
    if(responseWrapper != null) {
      annotationResponseWrapperBeanClassName = responseWrapper.className();
      annotationResponseNamespace = responseWrapper.targetNamespace();
      annotationResponseName = responseWrapper.localName();
    }
    setResponseWrapperBean(seiClass, operationMapping, annotationResponseWrapperBeanClassName, method);
    String responseWrapperBeanClassName = operationMapping.getProperty(OperationMapping.RESPONSE_WRAPPER_BEAN);
    Class responseWrapperBeanClass = loadJAXPBeanClass(seiClass, operationMapping, responseWrapperBeanClassName); 
    set_SOAPResponseWrapper_OutputNs_DocumentWrappedStyle(interfaceMapping, operationMetadata, operationMapping, annotationResponseNamespace, annotationResponseName, responseWrapperBeanClass);
  }
  
  private static void setRequestWrapperBean_DocumentWrappedStyle(Class seiClass, 
                                                                 InterfaceMapping interfaceMapping, 
                                                                 OperationMetadata operationMetadata, 
                                                                 OperationMapping operationMapping, 
                                                                 Method method) { 
    RequestWrapper requestWrapper = (RequestWrapper)(method.getAnnotation(RequestWrapper.class));
    String annotatioinRequestWrapperBeanClassName = null;
    String annotationRequestNamespace = null;
    String annotationRequestName = null;
    if(requestWrapper != null) {
      annotatioinRequestWrapperBeanClassName = requestWrapper.className();
      annotationRequestNamespace = requestWrapper.targetNamespace();
      annotationRequestName = requestWrapper.localName();
    }
    setRequestWrapperBean(seiClass, operationMapping, annotatioinRequestWrapperBeanClassName, method);
    String reqestWrapperBeanClassName = operationMapping.getProperty(OperationMapping.REQUEST_WRAPPER_BEAN);
    Class requestWrapperBeanClass = loadJAXPBeanClass(seiClass, operationMapping, reqestWrapperBeanClassName); 
    set_SOAPRequestWrapper_InputNS_DocumentWrappedStyle(interfaceMapping, operationMetadata, operationMapping, annotationRequestNamespace, annotationRequestName, requestWrapperBeanClass);
  }
  
  private static SOAPBindingOperation determineSOAPBindingOperation(Class seiClass, Binding binding, OperationMapping operationMapping) {
    String wsdlOperationName = operationMapping.getWSDLOperationName();
    SOAPBindingOperation soapBindingOperation = (SOAPBindingOperation)(binding.getOperationByName(wsdlOperationName));
    if(soapBindingOperation == null) {
      throw new IllegalArgumentException(createErrorMessage(seiClass, operationMapping, "WSDL SOAP binding operation '" + wsdlOperationName + "' is not defined!"));
    }
    return(soapBindingOperation);
  }
  
  private static Service determineService(Binding binding) {
    Definitions definitions = (Definitions)(binding.getParent());
    return((Service)(definitions.getServices().item(0)));
  }

  private static void set_SOAPResponseWrapper_OutputNs_DocumentWrappedStyle(InterfaceMapping interfaceMapping, 
                                                                            OperationMetadata operationMetadata, 
                                                                            OperationMapping operationMapping, 
                                                                            String annotationNamespace, 
                                                                            String annotationName, 
                                                                            Class jaxbBeanClass) {
    QName namespaceAndName = determineNamespaceAndName_DocumentWrappedStyle(interfaceMapping, operationMapping, annotationNamespace, annotationName, jaxbBeanClass, true);
    String namespace = namespaceAndName.getNamespaceURI();
    String soapResponseWrapper = namespaceAndName.getLocalPart();
    operationMapping.setProperty(OperationMapping.SOAP_RESPONSE_WRAPPER, soapResponseWrapper);
    operationMapping.setProperty(OperationMapping.OUTPUT_NAMESPACE, namespace);
    addTypeReferenece(operationMetadata, annotationNamespace, namespace, jaxbBeanClass);
  }
  
  private static void set_SOAPRequestWrapper_InputNS_DocumentWrappedStyle(InterfaceMapping interfaceMapping, OperationMetadata operationMetadata, OperationMapping operationMapping, String annotationNamespace, String annotationName, Class jaxbBeanClass) {
    QName namespaceAndName = determineNamespaceAndName_DocumentWrappedStyle(interfaceMapping, operationMapping, annotationNamespace, annotationName, jaxbBeanClass, false);
    String inputNamespace = namespaceAndName.getNamespaceURI();
    String soapRequestWrapper = namespaceAndName.getLocalPart();
    operationMapping.setProperty(OperationMapping.SOAP_REQUEST_WRAPPER, soapRequestWrapper);
    operationMapping.setProperty(OperationMapping.INPUT_NAMESPACE, inputNamespace);
    addTypeReferenece(operationMetadata, inputNamespace, soapRequestWrapper, jaxbBeanClass);
  }
  
  private static void addTypeReferenece(OperationMetadata operationMetadata, String namespace, String name, Class jaxbBeanClass) {
    QName schemaName = new QName(namespace, name);
    TypeReference typeRef = new TypeReference(schemaName, jaxbBeanClass, jaxbBeanClass.getAnnotations());
    operationMetadata.addTypeReferenece(typeRef);
  }
  
  private static QName determineNamespaceAndName_DocumentWrappedStyle(InterfaceMapping interfaceMapping, OperationMapping operationMapping, String annotationNamespace, String annotationName, Class jaxbBeanClass, boolean isResponse) {
    XmlRootElement xmlRootElement = (XmlRootElement)(jaxbBeanClass.getAnnotation(XmlRootElement.class));
    String namespace = determineNamespace(interfaceMapping, annotationNamespace, xmlRootElement, jaxbBeanClass);
    String name = determineName_DocumentWrappedStyle(operationMapping, annotationName, xmlRootElement, jaxbBeanClass, isResponse);
    return(new QName(namespace, name));
  }
  
  private static String determineNamespace(InterfaceMapping interfaceMapping, String annotationNamespace, XmlRootElement xmlRootElement, Class jaxbBeanClass) {
    if(annotationNamespace != null) {
      return(annotationNamespace);
    }
    if(xmlRootElement != null) {
      String namespace = determineNamespace_XmlRootElement(xmlRootElement, jaxbBeanClass);
      if(namespace != null) {
        return(namespace);
      }
    }
    return(interfaceMapping.getPortType().getNamespaceURI());
  }
  
  private static String determineNamespace_XmlRootElement(XmlRootElement xmlRootElement, Class jaxbBeanClass) {
    String namespace = xmlRootElement.namespace();
    if(namespace != null) {
      if (JAXWSUtil.DEFAULT.equals(namespace)) {
        namespace = JAXWSUtil.getPackageNamespace(jaxbBeanClass.getPackage());
      }   
      return(namespace);
    }
    return(null);
  }
  
  private static String determineName_DocumentWrappedStyle(OperationMapping operationMapping, String annotationName, XmlRootElement xmlRootElement, Class jaxbBeanClass, boolean isResponse) {
    if(hasField(annotationName)) {
      return(annotationName);
    }
    if(xmlRootElement != null) {
      return(determineName_XmlRootElement(xmlRootElement, jaxbBeanClass));
    }
    String name = operationMapping.getWSDLOperationName();
    return(isResponse ? name + RESPONSE_WRAPPER_BEAN_DEFAULT_CLASS_NAME_SUFFIX : name);
  }
  
  private static String determineName_XmlRootElement(XmlRootElement xmlRootElement, Class jaxbBeanClass) {
    String name = xmlRootElement.name();
    if(!hasField(name)) {
      throw new IllegalArgumentException("JAXB bean class '" + jaxbBeanClass.getName() + "' is annotated with annotation javax.xml.bind.annotation.XmlRootElement which mandatory field 'name' is missing!");
    }
    return(name);
  }
  
  private static Class loadJAXPBeanClass(Class seiClass, OperationMapping operationMapping, String jaxbBeanClassName) {
    try {
      return(seiClass.getClassLoader().loadClass(jaxbBeanClassName));
    } catch(ClassNotFoundException classNFExc) {
      throw new IllegalArgumentException(createErrorMessage(seiClass, operationMapping, "Failed to load request wrapper bean class '" + jaxbBeanClassName + "'!"));
    }
  }
  
  private static void set_SOAPRequestWrapper_InputNS_RpcStyle(OperationMapping operationMapping, SOAPBindingOperation soapBindingOperation) {
    operationMapping.setProperty(OperationMapping.SOAP_REQUEST_WRAPPER, operationMapping.getWSDLOperationName());
    setInputNS_RpcStyle(operationMapping, soapBindingOperation);
  }
  
  private static void setInputNS_RpcStyle(OperationMapping operationMapping, SOAPBindingOperation soapBindingOperation) {
    String requestNS = soapBindingOperation.getProperty(SOAPBindingOperation.INPUT_NAMESPACE);
    operationMapping.setProperty(OperationMapping.INPUT_NAMESPACE, requestNS == null ? "" : requestNS);
  }
  
  private static SOAPBindingConfiguration createMethodSOAPBindingConfiguration(OperationMapping operationMapping, SOAPBinding soapBinding, SOAPBindingConfiguration seiSoapBindingCfg) {
    if(soapBinding != null) {
      SOAPBindingConfiguration soapBindingCfg = new SOAPBindingConfiguration();
      initSOAPBindingConfiguration(soapBinding, soapBindingCfg);
      return(soapBindingCfg);
    }
    return(seiSoapBindingCfg);
  }
  
  private static void setParameters(OperationMetadata operationMetadata, OperationMapping operationMapping, Class seiClass, InterfaceMapping interfaceMapping, Method method, SOAPBindingConfiguration soapBindingCfg, Service service, IndexProvider indexProvider, Operation operation) {
    setOperationParameters(operationMetadata, operationMapping, seiClass, method, soapBindingCfg, service, indexProvider, operation);
    setReturnParameter(operationMetadata, operationMapping, seiClass, method, soapBindingCfg, service, operation);
    setFaultParameters(operationMetadata, operationMapping, seiClass, interfaceMapping, method);    
  }
  
  private static void setReturnParameter(OperationMetadata operationMetadata, OperationMapping operationMapping, Class seiClass, Method method, SOAPBindingConfiguration soapBindingCfg, Service service, Operation operation) {
    Object[] params = createReturnParameters(seiClass, operationMapping, method, soapBindingCfg, service, operation);
    if(params != null) {
      operationMetadata.setReturnParameter((ParameterObject)params[0]);
      operationMapping.addParameter((ParameterMapping)params[1]);
    }
  }
  
  private static Object[] createReturnParameters(Class seiClass, OperationMapping operationMapping, Method method, SOAPBindingConfiguration soapBindingCfg, Service service, Operation operation) {
    Class returnClass = method.getReturnType();
    Type returnGenericType = method.getGenericReturnType();
    if(returnClass.equals(void.class)) {
      return(null);
    }
    
    WebResult webResult = (WebResult)(method.getAnnotation(WebResult.class));
    String annotationName = null;
    String annotationPartName = null;
    String annotationNamespace = null;
    boolean isHeader = false;
    Annotation[] annotations = method.getAnnotations();
    if(webResult != null) {
      annotationName = webResult.name();
      annotationPartName = webResult.partName();
      annotationNamespace = webResult.targetNamespace();
      isHeader = webResult.header();       
    } 
    if (annotations == null) {
      annotations = new Annotation[0];
    }
    ParameterObject paramObject = new ParameterObject();
    ParameterMapping paramMapping = new ParameterMapping();
    setParameterProperties(seiClass, 
                           operationMapping,
                           paramObject,
                           paramMapping, 
                           annotationName,
                           annotationPartName,
                           annotationNamespace,
                           null,
                           isHeader,
                           false,
                           true,
                           soapBindingCfg, 
                           returnGenericType,
                           returnClass, 
                           service, 
                           null, 
                           operation,
                           Parameter.RETURN,
                           annotations);
    return(new Object[]{paramObject, paramMapping});
  }
  
  private static void setFaultParameters(OperationMetadata operationMetadata, OperationMapping operationMapping, Class seiClass, InterfaceMapping interfaceMapping, Method method) {
    Class[] exceptionClasses = method.getExceptionTypes();
    for(int i = 0; i < exceptionClasses.length; i++) {
      Class exceptionClass = exceptionClasses[i]; 
      Object[] params = createFaultParameters(seiClass, operationMapping, interfaceMapping, exceptionClass);
      if(params != null) {
        operationMetadata.addFaultParameter((ParameterObject)params[0]);
        operationMapping.addParameter((ParameterMapping)params[1]);
      }
    }
  }
  
  private static Object[] createFaultParameters(Class seiClass, 
                                                OperationMapping operationMapping, 
                                                InterfaceMapping interfaceMapping, 
                                                Class exceptionClass) {
    if(RemoteException.class.isAssignableFrom(exceptionClass) || RuntimeException.class.isAssignableFrom(exceptionClass)) {
      return(null);
    }
    
    ParameterObject paramObject = new ParameterObject();
    ParameterMapping paramMapping = new ParameterMapping();
    paramMapping.setIsElement(false);
    paramMapping.setHeader(false);
    paramMapping.setParameterType(ParameterMapping.FAULT_TYPE);
    paramMapping.setJavaType(exceptionClass.getName());
    paramObject.parameterType = exceptionClass;
    
    WebFault webFault = (WebFault)(exceptionClass.getAnnotation(WebFault.class));
    set_FaultElementQName_JAXBBeanClass(seiClass, 
                                        operationMapping, 
                                        interfaceMapping,
                                        paramObject,
                                        paramMapping, 
                                        webFault, 
                                        exceptionClass);
    return(new Object[]{paramObject, paramMapping});
  }
  
  private static void set_FaultElementQName_JAXBBeanClass(Class seiClass, 
                                                          OperationMapping operationMapping, 
                                                          InterfaceMapping interfaceMapping, 
                                                          ParameterObject paramObject, 
                                                          ParameterMapping paramMapping, 
                                                          WebFault webFault, 
                                                          Class exceptionClass) {
    String annotationBeanClassName = null;
    String annotationNamespace = null;
    String annotationName = null;
    Annotation[] annotations = null;
    if(webFault != null) {
      annotationBeanClassName = webFault.faultBean();
      annotationNamespace = webFault.targetNamespace();
      annotationName = webFault.name();
      annotations = new Annotation[]{webFault};
    } else {
      annotations = new Annotation[0];
    }
    Class jaxbBeanClass = determineFaultBeanClass(seiClass, operationMapping, annotationBeanClassName, exceptionClass);
    paramMapping.setProperty(ParameterMapping.JAXB_BEAN_CLASS, jaxbBeanClass.getName());
    setFaultElementQName(seiClass, 
                         interfaceMapping, 
                         operationMapping, 
                         paramObject,
                         paramMapping, 
                         annotationNamespace, 
                         annotationName, 
                         jaxbBeanClass,
                         annotations);    
  }
  
  private static void setFaultElementQName(Class seiClass, 
                                           InterfaceMapping interfaceMapping, 
                                           OperationMapping operationMapping, 
                                           ParameterObject paramObject, 
                                           ParameterMapping paramMapping, 
                                           String annotationNamespace, 
                                           String annotationName, 
                                           Class jaxbBeanClass, 
                                           Annotation[] annotations) {
    XmlRootElement xmlRootElement = (XmlRootElement)(jaxbBeanClass.getAnnotation(XmlRootElement.class));
    String namespace = determineNamespace(interfaceMapping, annotationNamespace, xmlRootElement, jaxbBeanClass);
    String name = determineName_FaultParameter(seiClass, operationMapping, annotationName, xmlRootElement, jaxbBeanClass);
    QName faultName = new QName(namespace, name);
    paramMapping.setFaultElementQName(faultName);
    paramObject.typeReference = new TypeReference(faultName, jaxbBeanClass, annotations);
  }
  
  private static String determineName_FaultParameter(Class seiClass, OperationMapping operationMapping, String annotationName, XmlRootElement xmlRootElement, Class jaxbBeanClass) {
    if(hasField(annotationName)) {
      return(annotationName);
    }
    if(xmlRootElement == null) {
      throw new IllegalArgumentException(createErrorMessage(seiClass, operationMapping, "JAXB bean class '" + jaxbBeanClass.getName() + "' should be annotated with javax.xml.bind.annotation.XmlRootElement annotation!"));
    }
    return(determineName_XmlRootElement(xmlRootElement, jaxbBeanClass));
  }
  
  private static Class determineFaultBeanClass(Class seiClass, OperationMapping operationMapping, String annotationBeanClassName, Class exceptionClass) {
    if(!hasField(annotationBeanClassName)) {
      try {
        Method getFaultInfoMethod = exceptionClass.getMethod(GET_FAULT_INFO_METHOD_NAME, new Class[0]);
        return(getFaultInfoMethod.getReturnType());
      } catch(NoSuchMethodException noSuchMethodExc) {
        return(loadJAXPBeanClass(seiClass, operationMapping, createDefaultBeanClassName(seiClass, exceptionClass)));  
      }      
    }
    return(loadJAXPBeanClass(seiClass, operationMapping, annotationBeanClassName));
  }
  
  private static String createDefaultBeanClassName(Class seiClass, Class exceptionClass) {
    StringBuffer buffer = new StringBuffer();
    initWrapperBeanDefaultClassNameBuffer_Package(buffer, seiClass);
    buffer.append(determineClassNameWithoutThePackage(exceptionClass));
    buffer.append(DEFAULT_FAULT_BEAN_CLASS_NAME_SUFIX);
    return(buffer.toString());
  }
  
  private static void setOperationParameters(OperationMetadata operationMetadata, OperationMapping operationMapping, Class seiClass, Method method, SOAPBindingConfiguration soapBindingCfg, Service service, IndexProvider indexProvider, Operation operation) {
    Annotation[][] paramsAnnotations = method.getParameterAnnotations();
    Type[] paramTypes = method.getGenericParameterTypes();
    Class[] paramClasses = method.getParameterTypes();
    for(int i = 0; i < paramsAnnotations.length; i++) {
      Annotation[] paramAnnotations = paramsAnnotations[i];
      Type paramType = paramTypes[i];
      Class paramClass = paramClasses[i];
      Object[] params = createParameters(seiClass, 
                                         operationMapping, 
                                         paramAnnotations, 
                                         paramType, 
                                         paramClass,
                                         soapBindingCfg, 
                                         service, 
                                         indexProvider, 
                                         operation);
      operationMetadata.addOperationParameter((ParameterObject)params[0]);
      operationMapping.addParameter((ParameterMapping)params[1]);
    }
  }
  
  private static Object[] createParameters(Class seiClass, OperationMapping operationMapping, Annotation[] paramAnnotations, Type paramType, Class paramClass, SOAPBindingConfiguration soapBindingCfg, Service service, IndexProvider indexProvider, Operation operation) {
    ParameterMapping paramMapping = new ParameterMapping();
    ParameterObject paramObject = new ParameterObject();
    WebParam webParam = null;
    for(int i = 0; i < paramAnnotations.length; i++) {
      Annotation annotation = paramAnnotations[i];
      if(annotation instanceof WebParam) {
        webParam = (WebParam)annotation;
      }
    }
    setParameterProperties(seiClass,
                           operationMapping,
                           paramObject,
                           paramMapping,
                           webParam,
                           soapBindingCfg, 
                           paramType, 
                           paramClass,
                           service, 
                           indexProvider, 
                           operation,
                           paramAnnotations);
    return(new Object[]{paramObject, paramMapping});
  }
  
  private static void setParameterProperties(Class seiClass,
                                             OperationMapping operationMapping,
                                             ParameterObject paramObject,
                                             ParameterMapping paramMapping,
                                             WebParam webParam,
                                             SOAPBindingConfiguration soapBindingCfg, 
                                             Type paramType,
                                             Class paramClass,
                                             Service service, 
                                             IndexProvider indexProvider, 
                                             Operation operation,
                                             Annotation[] paramAnnotations) {
    String annotationName = null;
    String annotationPartName = null;
    String annotationNamespace = null;
    WebParam.Mode mode = null;
    boolean isHeader = false;
    boolean mandatoryAnnotationName = false;
    boolean isReturn = false;
    if(webParam != null) {
      annotationName = webParam.name();
      annotationPartName = webParam.partName();
      annotationNamespace = webParam.targetNamespace();
      mode = webParam.mode();
      isHeader = webParam.header();
      mandatoryAnnotationName = true;
    }
    int pramsMask = Parameter.IN + Parameter.INOUT + Parameter.OUT;  
    setParameterProperties(seiClass,
                           operationMapping, 
                           paramObject,
                           paramMapping, 
                           annotationName,
                           annotationPartName,
                           annotationNamespace,
                           mode,
                           isHeader,
                           mandatoryAnnotationName,
                           isReturn,
                           soapBindingCfg, 
                           paramType, 
                           paramClass,
                           service, 
                           indexProvider, 
                           operation,
                           pramsMask,
                           paramAnnotations);
  }

  private static void setParameterProperties(Class seiClass,
                                             OperationMapping operationMapping, 
                                             ParameterObject paramObject,
                                             ParameterMapping paramMapping, 
                                             String annotationName,
                                             String annotationPartName,
                                             String annotationNamespace,
                                             WebParam.Mode mode,
                                             boolean isHeader,
                                             boolean mandatoryAnnotationName,
                                             boolean isReturn,
                                             SOAPBindingConfiguration soapBindingCfg, 
                                             Type paramType,
                                             Class paramClass,
                                             Service service, 
                                             IndexProvider indexProvider, 
                                             Operation operation,
                                             int parametersMask,
                                             Annotation[] paramAnnotations) {
    paramMapping.setHeader(isHeader);
    setParameterType(seiClass, operationMapping, paramMapping, mode, paramClass, isReturn);
    setHolderName(paramMapping, paramClass);
    Class realParamClass = determineParamClass(paramMapping, paramClass, paramType);
    setJavaType(paramObject, paramMapping, realParamClass);
    setIsAttachment(paramMapping, realParamClass);
    if(soapBindingCfg.isDocumentBare()) {
      setParameterProperties_DocumentBareStyle(seiClass, operationMapping, paramMapping, annotationNamespace, annotationName, annotationPartName, mandatoryAnnotationName, service);
      setTypeReference(paramObject, paramMapping, paramAnnotations);
    } else if(soapBindingCfg.isDocumentWrapped()) {
      setParameterProperties_DocumentWrappedStyle(paramMapping, annotationNamespace, annotationName, service, indexProvider);
    } else if(soapBindingCfg.isRpcLiteral()) {
      setParameterProperties_RpcLiteralStyle(seiClass, operationMapping, paramMapping, annotationPartName, annotationName, indexProvider, operation, parametersMask);
      setTypeReference(paramObject, paramMapping, paramAnnotations);
    }
  }
  
  private static void setIsAttachment(ParameterMapping paramMapping, Class paramClass) {
    boolean isAttachment = paramClass.equals(Image.class) || paramClass.equals(DataHandler.class) || paramClass.equals(Source.class); 
    paramMapping.setProperty(ParameterMapping.IS_ATTACHMENT, isAttachment ? TRUE_BOOLEAN_PROPERTY_VALUE : FALSE_BOOLEAN_PROPERTY_VALUE);
  }
  
  private static Class determineParamClass(ParameterMapping paramMapping, Class paramClass, Type paramType) {
    return(paramMapping.getHolderName() == null ? paramClass : determineHolderValueClass(paramType));
  }
  
  private static void setTypeReference(ParameterObject paramObject, ParameterMapping paramMapping, Annotation[] paramAnnotations) {
    QName schemaQName = !paramMapping.isElement() ? new QName(paramMapping.getNamespace(), paramMapping.getWSDLParameterName()) : paramMapping.getSchemaQName();
    paramObject.typeReference = new TypeReference(schemaQName, paramObject.parameterType, paramAnnotations);
  }
  
  private static void setJavaType(ParameterObject paramObject, ParameterMapping paramMapping, Class paramClass) {
    paramObject.parameterType = paramClass; 
    paramMapping.setJavaType(paramClass.getName());
  }
  
  private static Class determineHolderValueClass(Type paramType) {
    Type[] argumetTypes = ((ParameterizedType)paramType).getActualTypeArguments();    
    return TYPE_ERASER.visit(argumetTypes[0]);
  }
  
  private static void setHolderName(ParameterMapping paramMapping, Class paramClass) {
    int paramType = paramMapping.getParameterType();
    if(paramType == ParameterMapping.IN_OUT_TYPE || paramType == ParameterMapping.OUT_TYPE) {
      paramMapping.setHolderName(paramClass.getName());
    }
  }
  
  private static void setParameterProperties_RpcLiteralStyle(Class seiClass, OperationMapping operationMapping, ParameterMapping paramMapping, String annotationPartName, String annotationName, IndexProvider indexProvider, Operation operation, int parametersMask) {
    paramMapping.setIsElement(paramMapping.isHeader());
    setWSDLPartName_RpcLiteralStyle(paramMapping, annotationPartName, annotationName, indexProvider);
    setSchemaQName_RpcLiteralStyle(seiClass, operationMapping, paramMapping, operation, parametersMask);
  }
  
  private static void setWSDLPartName_RpcLiteralStyle(ParameterMapping paramMapping, String annotationPartName, String annotationName, IndexProvider indexProvider) {
    paramMapping.setWSDLParameterName(!hasField(annotationPartName) ? determineParameterDefaultName_NoDocumentBareStyle(annotationName, indexProvider) : annotationPartName);
  }
  
  private static void setSchemaQName_RpcLiteralStyle(Class seiClass, OperationMapping operationMapping, ParameterMapping paramMapping, Operation operation, int parametersMask) {
    QName schemaQName = determineSchemaQName_RpcLiteralStyle(seiClass, operationMapping, paramMapping, operation, parametersMask);
    paramMapping.setSchemaQName(schemaQName);
  }
  
  private static QName determineSchemaQName_RpcLiteralStyle(Class seiClass, OperationMapping operationMapping, ParameterMapping paramMapping, Operation operation, int parametersMask) {
    ObjectList parameters = operation.getParameters(parametersMask);
    String paramName = paramMapping.getWSDLParameterName();
    for(int i = 0; i < parameters.getLength(); i++) {
      Parameter parameter = (Parameter)(parameters.item(i));
      if(parameter.getName().equals(paramName)) {
        return(parameter.getXSDTypeRef().getQName());
      }
    }
    throw new IllegalArgumentException(createErrorMessage(seiClass, operationMapping, "WSDL part '" + paramName + "' is not defined!"));
  }
  
  private static void setParameterProperties_DocumentWrappedStyle(ParameterMapping paramMapping, String annotationNamespace, String annotationName, Service service, IndexProvider indexProvider) {
    paramMapping.setIsElement(paramMapping.isHeader());
    setWSDLPartName_DocumentWrappedStyle(paramMapping, annotationName, indexProvider);
    setNamespace_DocumentWrappedStyle(paramMapping, annotationNamespace, service);
  }
  
  private static void setNamespace_DocumentWrappedStyle(ParameterMapping paramMapping, String annotationNamespace, Service service) {
    if(annotationNamespace == null) {
      paramMapping.setNamespace(paramMapping.isHeader() ? service.getName().getNamespaceURI() : "");
    }
    paramMapping.setNamespace(annotationNamespace);
  }
  
  private static void setWSDLPartName_DocumentWrappedStyle(ParameterMapping paramMapping, String annotationName, IndexProvider indexProvider) {
    String name = determineParameterDefaultName_NoDocumentBareStyle(annotationName, indexProvider);
    paramMapping.setWSDLParameterName(name);
  }
  
  private static String determineParameterDefaultName_NoDocumentBareStyle(String annotationName, IndexProvider indexProvider) {
    if(!hasField(annotationName)) {
      if(indexProvider == null) {
        return(DEFAULT_RETURN_PARAMETER_NAME);
      } 
      return(createDefaultParameterName_UseIndexProvider(indexProvider));
    }
    return(annotationName);
  }
  
  private static String createDefaultParameterName_UseIndexProvider(IndexProvider indexProvider) {
    return(DEFAULT_PARAMETER_NAME_PREFIX + indexProvider.provide());
  }
  
  private static void setParameterProperties_DocumentBareStyle(Class seiClass, OperationMapping operationMapping, ParameterMapping paramMapping, String annotationNamespace, String annotationName, String annotationPartName, boolean mandatoryAnnotationName, Service service) {
    paramMapping.setIsElement(true);
    setSchemaQName_DocumentBareStyle(seiClass, operationMapping, paramMapping, annotationNamespace, annotationName, mandatoryAnnotationName, service);
    setWSDLPartName_DocumentBareStyle(paramMapping, annotationPartName);
  }
  
  private static void setWSDLPartName_DocumentBareStyle(ParameterMapping paramMapping, String annotationPartName) {
    paramMapping.setWSDLParameterName(!hasField(annotationPartName) ? paramMapping.getSchemaQName().getLocalPart() : annotationPartName);
  }
  
  private static void setSchemaQName_DocumentBareStyle(Class seiClass, OperationMapping operationMapping, ParameterMapping paramMapping, String annotationNamespace, String annotationName, boolean mandatoryAnnotationName, Service service) {
    String name = determineParameterName_DocumentBareStyle(seiClass, operationMapping, paramMapping, annotationName, mandatoryAnnotationName);
    String namespace = determineParameterNamespace_DocumentBareStyle(annotationNamespace, service);
    paramMapping.setSchemaQName(new QName(namespace, name));
  }
  
  private static String determineParameterName_DocumentBareStyle(Class seiClass, OperationMapping operationMapping, ParameterMapping paramMapping, String annotationName, boolean mandatoryAnnotationName) {
    if(hasField(annotationName)) {
      return(annotationName);
    }
    if(mandatoryAnnotationName && paramMapping.getParameterType() != ParameterMapping.IN_TYPE) {
      throw new IllegalArgumentException(createErrorMessage(seiClass, operationMapping, "Annotation javax.jws.WebParam should have field 'name' if the operation is document style, the parameter style is bare, and the mode is out or inout!"));
    }
    return(operationMapping.getWSDLOperationName());
  }
  
  private static String determineParameterNamespace_DocumentBareStyle(String annotationNamespace, Service service) {
    return(annotationNamespace == null ? service.getName().getNamespaceURI() : annotationNamespace);
  }
  
  private static void setParameterType(Class seiClass, OperationMapping operationMapping, ParameterMapping paramMapping, WebParam.Mode mode, Class paramClass, boolean isReturn) {
    if(isReturn) {
      paramMapping.setParameterType(ParameterMapping.RETURN_TYPE);
    } else {
      boolean isHolder = Holder.class.isAssignableFrom(paramClass);
      if(mode != null) {
        if(isHolder && mode.equals(WebParam.Mode.IN)) {
          throw new IllegalArgumentException(createErrorMessage(seiClass, operationMapping, "Parameters that are javax.xml.ws.Holder types must be of type out or inout!"));
        }
        if(mode.equals(WebParam.Mode.IN)) {
          paramMapping.setParameterType(ParameterMapping.IN_TYPE);
        } else if(mode.equals(WebParam.Mode.INOUT)) {
          paramMapping.setParameterType(ParameterMapping.IN_OUT_TYPE);
        } else {
          paramMapping.setParameterType(ParameterMapping.OUT_TYPE);
        }
      } else {
        paramMapping.setParameterType(isHolder ? ParameterMapping.IN_OUT_TYPE : ParameterMapping.IN_TYPE);
      }
    }
  }
  
  private static void setResponseWrapperBean(Class seiClass, OperationMapping operationMapping, String annotationRespWrapperBeanClassName, Method method) {
    String resposneWrapperClassName = !hasField(annotationRespWrapperBeanClassName) ? createResponseWrapperBeanDefaultClassName(method) : annotationRespWrapperBeanClassName;
    if(resposneWrapperClassName == null) {
      throw new IllegalArgumentException(createErrorMessage(seiClass, operationMapping, "Manadatory field 'className' of javax.xml.ws.ResponseWrapper annotation is missing!"));
    }
    operationMapping.setProperty(OperationMapping.RESPONSE_WRAPPER_BEAN, resposneWrapperClassName);
  }
  
  private static void setRequestWrapperBean(Class seiClass, OperationMapping operationMapping, String annotationReqWrapperBeanClassName, Method method) {
    String requestWrapperBeanClassName = !hasField(annotationReqWrapperBeanClassName) ? createRequestWrapperBeanDefaultClassName(method) : annotationReqWrapperBeanClassName;
    if(requestWrapperBeanClassName == null) {
      throw new IllegalArgumentException(createErrorMessage(seiClass, operationMapping, "Manadatory field 'className' of javax.xml.ws.RequestWrapper annotation is missing!"));
    }
    operationMapping.setProperty(OperationMapping.REQUEST_WRAPPER_BEAN, requestWrapperBeanClassName);
  }

  private static String createResponseWrapperBeanDefaultClassName(Method method) {
    StringBuffer buffer = new StringBuffer();
    initWrapperBeanDefaultClassNameBuffer(buffer, method);
    buffer.append(RESPONSE_WRAPPER_BEAN_DEFAULT_CLASS_NAME_SUFFIX);
    return(buffer.toString());
  }
  
  private static String createRequestWrapperBeanDefaultClassName(Method method) {
    StringBuffer buffer = new StringBuffer();
    initWrapperBeanDefaultClassNameBuffer(buffer, method);
    return(buffer.toString());
  }
  
  private static void initWrapperBeanDefaultClassNameBuffer(StringBuffer buffer, Method method) {
    initWrapperBeanDefaultClassNameBuffer_Package(buffer, method.getDeclaringClass());
    buffer.append(createWrapperBeanDefaultClassName_Capitalize(method));
  }
  
  private static String createWrapperBeanDefaultClassName_Capitalize(Method method) {
    String className = method.getName();
    char[] chars = className.toCharArray();
    chars[0] = Character.toUpperCase(chars[0]);
    return(new String(chars));
  }
  
  private static void initWrapperBeanDefaultClassNameBuffer_Package(StringBuffer buffer, Class seiClass) {
    buffer.append(seiClass.getPackage().getName());
    buffer.append(".");
    buffer.append(WRAPPER_BEAN_CLASSES_DEFAULT_SUBPACKAGE);
    buffer.append(".");
  }
  
  private static void setOperationMep(OperationMapping operationMapping, Oneway oneway) {
    String operationMep = oneway != null ? OperationMapping.MEP_ONE_WAY : OperationMapping.MEP_REQ_RESP;   
    operationMapping.setProperty(OperationMapping.OPERATION_MEP, operationMep);
  }
  
  private static void setOperationUse(OperationMapping operationMapping, SOAPBindingConfiguration soapBindingCfg) {
    String use = soapBindingCfg.getUse().equals(SOAPBinding.Use.ENCODED) ? OperationMapping.ENCODED_USE : OperationMapping.LITERAL_USE; 
    operationMapping.setProperty(OperationMapping.OPERATION_USE, use);
  }
  
  private static void setOperationStyle(OperationMapping operationMapping, SOAPBindingConfiguration soapBindingCfg) {
    String style = soapBindingCfg.getStyle().equals(SOAPBinding.Style.DOCUMENT) ? OperationMapping.DOCUMENT_OPERATION_STYLE : OperationMapping.RPC_OPERATION_STYLE; 
    operationMapping.setProperty(OperationMapping.OPERATION_STYLE, style);
  }
  
  private static void setOperationMapping_WSDLOperationName_SoapAction(OperationMapping operationMapping, WebMethod webMethod, Method method) {
    String annotationOperationName = null;
    String annotationSoapAction = null;
    if(webMethod != null) {
      annotationOperationName = webMethod.operationName();
      annotationSoapAction = webMethod.action();
    }
    operationMapping.setWSDLOperationName(hasField(annotationOperationName) ? annotationOperationName : method.getName());
    operationMapping.setProperty(OperationMapping.SOAP_ACTION, annotationSoapAction != null ? annotationSoapAction : "");
  }
  
  private static boolean hasField(String field) {
    return(field != null && field.length() != 0);
  }

  private static void setBinding(Class seiClass, InterfaceMapping interfaceMapping, Definitions wsdlDefinitions, BindingType bindingType) {
    Binding binding = getBinding(seiClass, wsdlDefinitions, interfaceMapping.getPortType());
    interfaceMapping.setBindingQName(binding.getName());
    setBindingType(interfaceMapping, bindingType, binding);
  }
  
  private static void setBindingType(InterfaceMapping interfaceMapping, BindingType bindingType, Binding binding) {
    if(bindingType == null) {
      setBindingType_SOAP11(interfaceMapping);
    } else {
      String value = bindingType.value();
      if(hasField(value)) {
        if(value.equals(javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING)) {
          setBindingType_SOAP11(interfaceMapping);
        } else if(value.equals(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)) {
          setBindingType_SOAP12(interfaceMapping);
        } else if(value.equals(javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_MTOM_BINDING)) {
          setBindingType_SOAP11MTOM(interfaceMapping);
        } else if(value.equals(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_MTOM_BINDING)) {
          setBindingType_SOAP12MTOM(interfaceMapping);
        } else if(value.equals(HTTPBinding.HTTP_BINDING)) {
          setBindingType_HTTP(interfaceMapping, binding);
        } else {
          throw new WebServiceException("Binding uri '" + value + "' is not supported!");
        }
      } else {
        setBindingType_SOAP11(interfaceMapping);
      }
    }
  }
  
  private static void setBindingType_HTTP(InterfaceMapping interfaceMapping, Binding binding) {
    String httpMethod = binding.getProperty(com.sap.engine.services.webservices.espbase.wsdl.HTTPBinding.HTTP_METHOD);
    interfaceMapping.setProperty(InterfaceMapping.BINDING_TYPE, com.sap.engine.services.webservices.espbase.wsdl.HTTPBinding.HTTP_POST_METHOD.equals(httpMethod) ? InterfaceMapping.HTTPPOSTBINDING : InterfaceMapping.HTTPGETBINDING);
  }

  private static void setBindingType_SOAP11MTOM(InterfaceMapping interfaceMapping) {
    interfaceMapping.setProperty(InterfaceMapping.BINDING_TYPE, InterfaceMapping.SOAPBINDING);
    interfaceMapping.setProperty(InterfaceMapping.SOAP_VERSION, InterfaceMapping.SOAP_VERSION_11_MTOM);
  }
  
  private static void setBindingType_SOAP12MTOM(InterfaceMapping interfaceMapping) {
    interfaceMapping.setProperty(InterfaceMapping.BINDING_TYPE, InterfaceMapping.SOAPBINDING);
    interfaceMapping.setProperty(InterfaceMapping.SOAP_VERSION, InterfaceMapping.SOAP_VERSION_12_MTOM);
  }
  
  public static void setBindingType_SOAP11(InterfaceMapping interfaceMapping) {
    interfaceMapping.setProperty(InterfaceMapping.BINDING_TYPE, InterfaceMapping.SOAPBINDING);
    interfaceMapping.setProperty(InterfaceMapping.SOAP_VERSION, InterfaceMapping.SOAP_VERSION_11);
  }
  
  private static void setBindingType_SOAP12(InterfaceMapping interfaceMapping) {
    interfaceMapping.setProperty(InterfaceMapping.BINDING_TYPE, InterfaceMapping.SOAPBINDING);
    interfaceMapping.setProperty(InterfaceMapping.SOAP_VERSION, InterfaceMapping.SOAP_VERSION_12);
  }
  
  private static Binding getBinding(Class seiClass, Definitions wsdlDefinitions, QName portTypeName) {
    ObjectList bindings = wsdlDefinitions.getBindings();
    for(int i = 0; i < bindings.getLength(); i++) {
      Binding binding = (Binding)(bindings.item(i));
      if(binding.getInterface().equals(portTypeName)) {
        return(binding);
      }
    }
    throw new IllegalArgumentException(createErrorMessage(seiClass, "WSDL Binding associated with port type '" + portTypeName + "' is not defined!"));
  }
  
  private static void setSEIName(InterfaceMapping interfaceMapping, Class seiClass) {
    String seiName = seiClass.getName();
    interfaceMapping.setSEIName(seiName);
  }
  
  private static void setInterfaceMappingPortType(Class seiClass, InterfaceMapping interfaceMapping, WebService webService) {
    if(webService == null) {
      throw new IllegalArgumentException(createErrorMessage(seiClass, "Service endpoint interface should be annotated with 'javax.jws.WebService' annotation!"));
    }
    
    String[] seiClassNameSplit = null;
    String portTypeName = webService.name();
    if(!hasField(portTypeName)) {
      String seiClassName = seiClass.getName(); 
      seiClassNameSplit = seiClassName.split(".");
      portTypeName = seiClassNameSplit.length == 0 ? seiClassName : seiClassNameSplit[seiClassNameSplit.length - 1]; 
    }
    String portTypeNS = webService.targetNamespace();
    if(portTypeNS == null) {
      if(seiClassNameSplit == null) {
        seiClassNameSplit = seiClass.getName().split(".");
      }
      portTypeNS = createTargetNamespace_UseSEIClass(seiClassNameSplit);
    }
    interfaceMapping.setPortType(new QName(portTypeNS, portTypeName));
  }
  
  private static String determineClassNameWithoutThePackage(Class _class) {
    String className = _class.getName(); 
    int packageDelimiterIndex = className.lastIndexOf(".");
    return(packageDelimiterIndex > 0 ? className.substring(packageDelimiterIndex + 1) : className);
  }
  
  private static String createTargetNamespace_UseSEIClass(String[] seiClassNameSplit) {
    if(seiClassNameSplit.length == 0) {
      return("");
    }
    StringBuffer buffer = new StringBuffer();
    buffer.append("http://");
    for(int i = seiClassNameSplit.length - 2; i >= 0; i--) {
      buffer.append(seiClassNameSplit[i]);
      buffer.append(i == 0 ? "/" : ".");
    }
    return(buffer.toString());
  }
  
  private static String createErrorMessage(Class seiClass, String message) {
    StringBuffer buffer = new StringBuffer();
    initErrorMessageBuffer(seiClass, buffer);
    buffer.append(message);
    return(buffer.toString());
  }
  
  private static String createErrorMessage(Class seiClass, OperationMapping operationMapping, String message) {
    StringBuffer buffer = new StringBuffer();
    initErrorMessageBuffer(seiClass, operationMapping, buffer);
    buffer.append(message);
    return(buffer.toString());
  }
  
  private static void initErrorMessageBuffer(Class seiClass, StringBuffer buffer) {
    buffer.append("SEI class name " + seiClass + "; ");
  }
  
  private static void initErrorMessageBuffer(Class seiClass, OperationMapping operationMapping, StringBuffer buffer) {
    initErrorMessageBuffer(seiClass, buffer);
    buffer.append("Operation " + operationMapping.getJavaMethodName() + "; ");
  }
}
