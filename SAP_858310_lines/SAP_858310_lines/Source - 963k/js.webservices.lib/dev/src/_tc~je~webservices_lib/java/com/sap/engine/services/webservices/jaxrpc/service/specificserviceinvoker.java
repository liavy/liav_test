package com.sap.engine.services.webservices.jaxrpc.service;

import com.sap.engine.services.webservices.wsdl.*;
import com.sap.engine.services.webservices.jaxrpc.encoding.ExtendedTypeMapping;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;

import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import java.util.*;
import java.rmi.RemoteException;

/**
 * Created by IntelliJ IDEA.
 * User: ivan-m
 * Date: 2004-7-22
 * Time: 14:06:30
 * To change this template use Options | File Templates.
 */
public class SpecificServiceInvoker implements Constants {

  private Hashtable transportBindingDyamicInvokCollector;
  private WSDLDefinitions wsdlDefinitions;
  private WSDLService wsdlService;

  public SpecificServiceInvoker(WSDLService wsdlService, WSDLDefinitions wsdlDefinitions, TypeMappingRegistryImpl typeMappingRegistry) throws Exception {
    this.wsdlDefinitions = wsdlDefinitions;
    this.wsdlService = wsdlService;
    transportBindingDyamicInvokCollector = new Hashtable();
    ArrayList wsdlPortsCollector = wsdlService.getPorts();
    for(int i = 0; i < wsdlPortsCollector.size(); i++) {
      WSDLPort wsdlPort = (WSDLPort)(wsdlPortsCollector.get(i));
      WSDLExtension wsdlExtension = wsdlPort.getExtension();
      String endpointAdress = null;
      if(wsdlExtension.getURI().equals(SOAP_WSDL_EXTENSION_URI) && wsdlExtension.getLocalName().equals(ADRESS_WSDL_EXTENSION_NAME)) {
        endpointAdress = wsdlExtension.getAttribute(ADRESS_WSDL_EXTENSION_LOCATION_ATTRIBUTE_NAME);
      }
      WSDLBinding wsdlBinding = wsdlDefinitions.getBinding(wsdlPort.getBinding());
      com.sap.engine.lib.xml.util.QName portTypeQName = wsdlBinding.getType();
      transportBindingDyamicInvokCollector.put(new QName(portTypeQName.getURI(), portTypeQName.getLocalName()), new SpecificTransportBindingInvoker(wsdlDefinitions.getPortType(portTypeQName.getLocalName(), portTypeQName.getURI()), wsdlBinding, wsdlDefinitions, typeMappingRegistry, endpointAdress, this));
    }
  }

  protected Object invoke(QName portTypeName, QName operationName, Object[] inputParams, String endpointAddress) throws RemoteException {
    return(getTransportBindingInvoker(portTypeName).invoke(operationName, inputParams, endpointAddress));
  }

  protected SpecificTransportBindingInvoker getTransportBindingInvoker(QName portTypeName) {
    if(portTypeName == null) {
      throw new JAXRPCException(createErrorInfo() + " ERROR : Port name is not set.");
    }
    SpecificTransportBindingInvoker transportBindingInvoker = (SpecificTransportBindingInvoker)(transportBindingDyamicInvokCollector.get(portTypeName));
    if(transportBindingInvoker == null) {
      throw new JAXRPCException(createErrorInfo() + " ERROR : Port with name '{" + portTypeName.getNamespaceURI() + "}:" + portTypeName.getLocalPart() + "' is not defined.");
    }
    return(transportBindingInvoker);
  }

  protected String createErrorInfo() {
    return("[WSDLDefinitions (namespace : " + wsdlDefinitions.getNamespace() + "; name : " + wsdlDefinitions.getName() + "] [WSDLService (namespace : " + wsdlService.getNamespace() + "; name : " + wsdlService.getName() + ")]");
  }

  protected Map getOutputParams(QName portTypeName, QName operationName) {
    return(getTransportBindingInvoker(portTypeName).getOutputParams(operationName));
  }

  protected List getOutputValues(QName portTypeName, QName operationName) {
    return(getTransportBindingInvoker(portTypeName).getOutputValues(operationName));
  }

  protected QName getParameterTypeByName(QName portTypeName, QName operationName, String parameterName) {
    return(getTransportBindingInvoker(portTypeName).getParameterTypeByName(operationName, parameterName));
  }

  protected QName getReturnType(QName portTypeName, QName operationName) {
    return(getTransportBindingInvoker(portTypeName).getReturnType(operationName));
  }

  protected Vector getSpecificOperationInvokers(QName portTypeName) {
    Vector collector = new Vector();
    Enumeration enum1 = getTransportBindingInvoker(portTypeName).getSpecificOperationInvokers();
    while(enum1.hasMoreElements()) {
      collector.add(enum1.nextElement());
    }
    return(collector);
  }
}