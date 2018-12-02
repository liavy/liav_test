package com.sap.engine.services.webservices.jaxrpc.service;

import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ClientTransportBinding;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.httpgetpost.HttpGetPostBinding;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding.MimeHttpBinding;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;
import com.sap.engine.services.webservices.wsdl.WSDLPortType;
import com.sap.engine.services.webservices.wsdl.WSDLBinding;
import com.sap.engine.services.webservices.wsdl.WSDLDefinitions;
import com.sap.engine.services.webservices.wsdl.WSDLOperation;

import javax.xml.rpc.JAXRPCException;
import javax.xml.namespace.QName;
import java.util.*;
import java.rmi.RemoteException;

/**
 * Created by IntelliJ IDEA.
 * User: ivan-m
 * Date: 2004-7-22
 * Time: 14:40:53
 * To change this template use Options | File Templates.
 */
public class SpecificTransportBindingInvoker {

  private static ClientTransportBinding[] supportedTransportBindings = new ClientTransportBinding[]{new MimeHttpBinding(), new HttpGetPostBinding()};
  private Hashtable operationInvokersCollector;
  private SpecificServiceInvoker invoker;
  private WSDLPortType wsdlPortType;

  protected SpecificTransportBindingInvoker(WSDLPortType wsdlPortType, WSDLBinding wsdlBinding, WSDLDefinitions wsdlDefinitions, TypeMappingRegistryImpl typeMappingRegitry, String endpointAdress, SpecificServiceInvoker invoker) {
    this.invoker = invoker;
    this.wsdlPortType = wsdlPortType;
    operationInvokersCollector = new Hashtable();
    ArrayList wsdlOperationsCollector = wsdlPortType.getOperations();
    for(int i = 0; i < wsdlOperationsCollector.size(); i++) {
      WSDLOperation wsdlOperation = (WSDLOperation)(wsdlOperationsCollector.get(i));
      ClientTransportBinding transportBinding = null;
      for(int j = 0; j < supportedTransportBindings.length; j++) {
        ClientTransportBinding supportedTransportBinding = supportedTransportBindings[j];
        if(supportedTransportBinding.recognizeBinding(wsdlBinding)) {
          transportBinding = supportedTransportBinding;
          break;
        }
      }
      if(transportBinding == null) {
        throw new JAXRPCException(createErrorInfo() + " ERROR : Binding '{" + wsdlBinding.getNamespace() + "}:" + wsdlBinding.getName() + "' is not supported.");
      }
      QName operationName = new QName(wsdlOperation.getNamespace(), wsdlOperation.getName());
      operationInvokersCollector.put(operationName, new SpecificOperationInvoker(wsdlOperation, wsdlBinding, transportBinding, wsdlDefinitions, typeMappingRegitry, endpointAdress, this));
    }
  }

  protected Object invoke(QName operationName, Object[] inputParams, String endpointAddress) throws RemoteException {
    SpecificOperationInvoker operationInvoker = getOperationInvoker(operationName);
    operationInvoker.setTargetEndpointAddress(endpointAddress);
    return(operationInvoker.invoke(inputParams));
  }

  private SpecificOperationInvoker getOperationInvoker(QName operationName) {
    if(operationName == null) {
      throw new JAXRPCException(createErrorInfo() + " ERROR : Operation name is not configured.");
    }
    SpecificOperationInvoker operationInvoker = (SpecificOperationInvoker)(operationInvokersCollector.get(operationName));
    if(operationInvoker == null) {
      throw new JAXRPCException(createErrorInfo() + " ERROR : Operation '{" + operationName.getNamespaceURI() + "}:" + operationName.getLocalPart() + "' is not defined.");
    }
    return(operationInvoker);
  }

  protected String createErrorInfo() {
    return(invoker.createErrorInfo() + wsdlPortType == null ? "" : " [WSDLPortType (namespace : " + wsdlPortType.getNamespace() + "; name : " + wsdlPortType.getName() + ")]");
  }

  protected QName getParameterTypeByName(QName operationName, String parameterName) {
    return(getOperationInvoker(operationName).getParameterType(parameterName));
  }

  protected QName getReturnType(QName operationName) {
    return(getOperationInvoker(operationName).getReturnType());
  }

  protected Map getOutputParams(QName operationName) {
    return(getOperationInvoker(operationName).getOutputParams());
  }

  protected List getOutputValues(QName operationName) {
    return(getOperationInvoker(operationName).getOutputValues());
  }

  protected void setTargetEndpointAddress(QName operationName, String endpointAdress) {
    getOperationInvoker(operationName).setTargetEndpointAddress(endpointAdress);
  }

  protected String getTargetEndpointAddress(QName operationName) {
    return(getOperationInvoker(operationName).getTargetEndpointAddress());
  }

  protected Enumeration getSpecificOperationInvokers() {
    return(operationInvokersCollector.elements());
  }
}
