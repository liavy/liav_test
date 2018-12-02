package com.sap.engine.services.webservices.jaxrpc.service;

import com.sap.engine.services.webservices.jaxrpc.encoding.ExtendedTypeMapping;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.JAXRPCException;
import java.util.Map;
import java.util.List;
import java.rmi.RemoteException;

/**
 * Created by IntelliJ IDEA.
 * User: ivan-m
 * Date: 2004-7-23
 * Time: 13:39:47
 * To change this template use Options | File Templates.
 */
public class SpecificCall extends AbstractCall {

  private SpecificServiceInvoker serviceInvoker;
  private String endpointAddress;

  public SpecificCall(TypeMappingRegistryImpl typeMappingRegistry, SpecificServiceInvoker serviceInvoker) {
    super(typeMappingRegistry);
    this.serviceInvoker = serviceInvoker;
  }

  public boolean isParameterAndReturnSpecRequired(QName operationName) {
    return(false);
  }

  public void addParameter(String paramName, QName xmlType, Class parameterClass, ParameterMode parameterMode) {
    throw new JAXRPCException("Configuration of parameters is not required.");
  }

  public QName getParameterTypeByName(String parameterName) {
    return(serviceInvoker.getParameterTypeByName(portTypeName, operationName, parameterName));
  }

  public void setReturnType(QName xmlType, Class parameterClass) {
    throw new JAXRPCException("Configuration of parameters is not required.");
  }

  public QName getReturnType() {
    return(serviceInvoker.getReturnType(portTypeName, operationName));
  }

  public void removeAllParameters() {
    throw new JAXRPCException("Configuration of parameters is not required.");
  }

  public void setTargetEndpointAddress(String endpointAddress) {
    this.endpointAddress = endpointAddress;
  }

  public String getTargetEndpointAddress() {
    return(endpointAddress);
  }

  public Object invoke(QName operationName, Object[] parameters) throws RemoteException {
    return(serviceInvoker.invoke(portTypeName, operationName, parameters, endpointAddress));
  }

  public Map getOutputParams() {
    return(serviceInvoker.getOutputParams(portTypeName, operationName));
  }

  public List getOutputValues() {
    return(serviceInvoker.getOutputValues(portTypeName, operationName));
  }
}
