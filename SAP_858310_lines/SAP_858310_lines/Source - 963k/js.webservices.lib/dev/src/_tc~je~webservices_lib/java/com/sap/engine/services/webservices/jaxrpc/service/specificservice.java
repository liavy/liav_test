package com.sap.engine.services.webservices.jaxrpc.service;

import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ServiceParam;
import com.sap.engine.services.webservices.wsdl.*;

import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.Call;
import javax.xml.rpc.ServiceException;
import javax.xml.namespace.QName;
import java.util.*;
import java.rmi.Remote;
import java.net.URL;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: ivan-m
 * Date: 2004-8-18
 * Time: 13:03:16
 * To change this template use Options | File Templates.
 */
public class SpecificService extends AbstractService {

  protected WSDLDefinitions wsdlDefinitions;
  protected WSDLService wsdlService;
  protected SpecificServiceInvoker serviceInvoker;
  private URL wsdlUrl;

  private static WSDLDOMLoader wsdlDOMLoader;

  static {
    if(wsdlDOMLoader == null) {
      wsdlDOMLoader = new WSDLDOMLoader();
    }
  }

  public SpecificService(QName serviceName, TypeMappingRegistryImpl typeMappingRegistry, URL wsdlUrl) {
    super(serviceName, typeMappingRegistry);
    try {
      wsdlDefinitions = wsdlDOMLoader.loadWSDLDocument(wsdlUrl.openStream());
      this.wsdlUrl = wsdlUrl;
      ArrayList wsdlServicesCollector = wsdlDefinitions.getServices();
      for(int i = 0; i < wsdlServicesCollector.size(); i++) {
        WSDLService wsdlService = (WSDLService)(wsdlServicesCollector.get(i));
        com.sap.engine.lib.xml.util.QName wsdlServiceName = wsdlService.getQName();
        if((new QName(wsdlServiceName.getURI(), wsdlServiceName.getLocalName())).equals(serviceName)) {
          this.wsdlService = wsdlService;
          break;
        }
      }
      if(wsdlService == null) {
        throw new JAXRPCException("Service '{" + serviceName.getNamespaceURI() + "}:" +serviceName.getLocalPart()  + "' is not defined in the wsdl with location '" + wsdlUrl.toExternalForm() + "'.");
      }
      serviceInvoker = new SpecificServiceInvoker(wsdlService, wsdlDefinitions, typeMappingRegistry);
    } catch(Exception exc) {
      throw new JAXRPCException(exc.getMessage());
    }
  }

  private QName getWSDLPortTypeName(QName portName) throws ServiceException {
    return(getWSDLNamedNodeName(getWSDLPortType(portName)));
  }

  private QName getWSDLNamedNodeName(WSDLNamedNode wsdlNamedNode) {
    return(new QName(wsdlNamedNode.getQName().getURI(), wsdlNamedNode.getQName().getLocalName()));
  }

  private WSDLPortType getWSDLPortType(QName portName) throws ServiceException {
    ArrayList wsdlPorts = wsdlService.getPorts();
    WSDLPort questedWSDLPort = null;
    for(int i = 0; i < wsdlPorts.size(); i++) {
      WSDLPort wsdlPort = (WSDLPort)(wsdlPorts.get(i));
      com.sap.engine.lib.xml.util.QName wsdlPortTypeName = wsdlPort.getQName();
      QName wsdlPortQName = new QName(wsdlPortTypeName.getURI(), wsdlPortTypeName.getLocalName());
      if(wsdlPortQName.equals(portName)) {
        questedWSDLPort = wsdlPort;
        break;
      }
    }
    if(questedWSDLPort == null) {
      throw new ServiceException(createErrorInfo() + " ERROR : WSDLPort '{" + portName.getNamespaceURI() + "}:" + portName.getLocalPart() + "' is not defined.");
    }
    WSDLBinding wsdlBinding = wsdlDefinitions.getBinding(questedWSDLPort.getBinding());
    com.sap.engine.lib.xml.util.QName wsdlPortTypeName = wsdlBinding.getType();
    return(wsdlDefinitions.getPortType(wsdlPortTypeName.getLocalName(), wsdlPortTypeName.getURI()));
  }

  public Call[] getCalls(QName portName) throws ServiceException {
    try {
      WSDLPortType wsdlPortType = getWSDLPortType(portName);
      QName wsdlPortTypeName = getWSDLNamedNodeName(wsdlPortType);
      ArrayList wsdlOperations = wsdlPortType.getOperations();
      Call[] calls = new Call[wsdlOperations.size()];
      for(int i = 0; i < wsdlOperations.size(); i++) {
        calls[i] = createCall(portName, wsdlPortTypeName, getWSDLNamedNodeName((WSDLOperation)(wsdlOperations.get(i))));
      }
      return(calls);
    } catch(Exception exc) {
      if(exc instanceof ServiceException) {
        throw (ServiceException)exc;
      }
      throw new ServiceException(createErrorInfo() + " ERROR : " + exc.getMessage());
    }
  }

  protected AbstractCall createCall(QName portName, QName portTypeName, QName operationName) throws ServiceException {
    try {
      AbstractCall call = new SpecificCall(typeMappingRegistry, serviceInvoker);
      if(portTypeName != null) {
        call.setPortTypeName(portTypeName);
      } else if(portName != null) {
        call.setPortTypeName(getWSDLPortTypeName(portName));
      }
      call.setOperationName(operationName);
      return(call);
    } catch(Exception exc) {
      if(exc instanceof ServiceException) {
        throw (ServiceException)exc;
      }
      throw new ServiceException(exc);
    }
  }

  public Remote getPort(Class endpointInterfaceClass) throws ServiceException {
    checkEndpointInterface(endpointInterfaceClass);
    ArrayList wsdlPortsCollector = wsdlService.getPorts();
    for(int i = 0; i < wsdlPortsCollector.size(); i++) {
      WSDLPort wsdlPort = (WSDLPort)(wsdlPortsCollector.get(i));
      checkEndpointInterface(wsdlPort, endpointInterfaceClass);
      return((Remote)(Proxy.newProxyInstance(endpointInterfaceClass.getClassLoader(), new Class[]{endpointInterfaceClass}, new DynamicProxyInvokationHandler(getCalls(new QName(wsdlPort.getQName().getURI(), wsdlPort.getQName().getLocalName()))))));
    }
    throw new ServiceException(createErrorInfo() + " ERROR : No port type is suitable for the endpoint interface '" + endpointInterfaceClass.getName() + "'.");
  }

  public Remote getPort(QName portName, Class endpointInterfaceClass) throws ServiceException {
    checkEndpointInterface(endpointInterfaceClass);
    ArrayList wsdlPortsCollector = wsdlService.getPorts();
    for(int i = 0; i < wsdlPortsCollector.size(); i++) {
      WSDLPort wsdlPort = (WSDLPort)(wsdlPortsCollector.get(i));
      if(wsdlPort.getQName().getURI().equals(portName.getNamespaceURI()) && wsdlPort.getQName().getLocalName().equals(portName.getLocalPart())) {
        checkEndpointInterface(wsdlPort, endpointInterfaceClass);
        return((Remote)(Proxy.newProxyInstance(endpointInterfaceClass.getClassLoader(), new Class[]{endpointInterfaceClass}, new DynamicProxyInvokationHandler(getCalls(portName)))));
      }
    }
    throw new ServiceException(createErrorInfo() + " ERROR : Port with name '" + portName + "' is not defined.");
  }

  private void checkEndpointInterface(Class endpointInterfaceClass) throws ServiceException {
    if(!endpointInterfaceClass.isInterface()) {
      throw new ServiceException(createErrorInfo() + " ERROR : Class '" + endpointInterfaceClass.getName() + "' is not an interface.");
    }
    if(!Remote.class.isAssignableFrom(endpointInterfaceClass)) {
      throw new ServiceException(createErrorInfo() + " ERROR : Class '" + endpointInterfaceClass.getName() + "' is not assignable for java.rmi.Remote.");
    }
  }

  private void checkEndpointInterface(WSDLPort wsdlPort, Class endpointInterfaceClass) throws ServiceException {
    Method[] methodsCollector = endpointInterfaceClass.getDeclaredMethods();
    WSDLBinding wsdlBinding = wsdlDefinitions.getBinding(wsdlPort.getBinding());
    Vector specificOperationInvokersCollector = serviceInvoker.getSpecificOperationInvokers(new QName(wsdlBinding.getType().getURI(), wsdlBinding.getType().getLocalName()));
    if(methodsCollector.length != specificOperationInvokersCollector.size()) {
      throw new ServiceException(createErrorInfo() + " ERROR : Endpoint interface '" + endpointInterfaceClass.getName() + "' is not suitable for wsdl port '" + wsdlPort.getName() + "'.");
    }
    for(int a = 0; a < methodsCollector.length; a++) {
      Method method = methodsCollector[a];
      boolean methodIsDefined = false;
      int operationInvokersIndex = 0;
      while(operationInvokersIndex < specificOperationInvokersCollector.size()) {
        SpecificOperationInvoker operationInvoker = (SpecificOperationInvoker)(specificOperationInvokersCollector.get(operationInvokersIndex));
        String operationName = operationInvoker.getOperationName().getLocalPart();
        if(operationName.equals(method.getName())) {
          Class returnType = method.getReturnType();
          ParameterWrapper returnParameterWrapper = operationInvoker.getParametersConfiguration().getReturnParameterWrapper();
          if(!(returnType.equals(Void.class) ^ returnParameterWrapper == null) && (!returnType.equals(Void.class) && !determineParameterClass(returnType).equals(returnParameterWrapper.getServiceParam().contentClass))) {
            throw new ServiceException(createErrorInfo() + " ERROR : Endpoint interface '" + endpointInterfaceClass.getName() + "' is not suitable for wsdl port '" + wsdlPort.getName() + "'. The returntype associated with method '" + method.getName() + "' is not defined.");
          }

          Class[] parameterTypesCollector = method.getParameterTypes();
          Vector inputParameterWrappersCollector = operationInvoker.getParametersConfiguration().getInputParameterWrappers();
          if(parameterTypesCollector.length != inputParameterWrappersCollector.size()) {
            throw new ServiceException(createErrorInfo() + " ERROR : Endpoint interface '" + endpointInterfaceClass.getName() + "' is not suitable for wsdl port '" + wsdlPort.getName() + "'. The count of the input parameters associated with method '" + method.getName() + "' does not match with the defined.");
          }
          for(int b = 0; b < inputParameterWrappersCollector.size(); b++) {
            ParameterWrapper parameterWrapper = (ParameterWrapper)(inputParameterWrappersCollector.get(b));
            Class parameterType = parameterTypesCollector[b];
            if(!parameterWrapper.getServiceParam().contentClass.equals(determineParameterClass(parameterType))) {
              throw new ServiceException(createErrorInfo() + " ERROR : Endpoint interface '" + endpointInterfaceClass.getName() + "' is not suitable for wsdl port '" + wsdlPort.getName() + "'. Parameter with type '" + parameterType.getName() + "' associated with method '" + method.getName() + "' is not defined.");
            }
          }
          methodIsDefined = true;
          specificOperationInvokersCollector.remove(operationInvokersIndex);
          break;
        } else {
          operationInvokersIndex++;
        }
      }
      if(!methodIsDefined) {
        throw new ServiceException(createErrorInfo() + " ERROR : Endpoint interface '" + endpointInterfaceClass.getName() + "' is not suitable for wsdl port '" + wsdlPort.getName() + "'. Method '" + method.getName() + "' is not defined.");
      }
    }
  }

  private Class determineParameterClass(Class parameterClass) {
    if(parameterClass.isPrimitive()) {
      return(ClassResolver.resolveWrapperClass(parameterClass.getName(), getClass().getClassLoader()));
    }
    return(parameterClass);
  }

  public Iterator getPorts() {
    return(new PortsIterator(wsdlService.getPorts()));
  }

  public URL getWSDLDocumentLocation() {
    return(wsdlUrl);
  }

  protected String createErrorInfo() {
    if(wsdlDefinitions == null) {
      return("");
    }
    return("[WSDLDefinitions (namespace : " + wsdlDefinitions.getNamespace() + "; name : " + wsdlDefinitions.getName() + "] [WSDLService (namespace : " + wsdlService.getNamespace() + "; name : " + wsdlService.getName() + ")]");
  }
}
