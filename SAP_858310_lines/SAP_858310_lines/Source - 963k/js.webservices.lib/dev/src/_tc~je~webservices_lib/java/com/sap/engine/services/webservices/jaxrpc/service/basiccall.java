package com.sap.engine.services.webservices.jaxrpc.service;

import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding.MimeHttpBinding;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import java.util.Map;
import java.util.List;
import java.rmi.RemoteException;

/**
 * Created by IntelliJ IDEA.
 * User: ivan-m
 * Date: 2004-7-22
 * Time: 17:51:08
 * To change this template use Options | File Templates.
 */
public class BasicCall extends AbstractCall {

  private BasicOperationInvoker operationInvoker;

  public BasicCall(TypeMappingRegistryImpl typeMappingRegistry) {
    super(typeMappingRegistry);
    operationInvoker = new BasicOperationInvoker(new MimeHttpBinding(), typeMappingRegistry);
  }

  public boolean isParameterAndReturnSpecRequired(QName qName) {
    return(true);
  }

  public void setTargetEndpointAddress(String endpointAdress) {
    operationInvoker.setTargetEndpointAddress(endpointAdress);
  }

  public String getTargetEndpointAddress() {
    return(operationInvoker.getTargetEndpointAddress());
  }

  public Object invoke(QName operationName, Object[] parameters) throws RemoteException {
    operationInvoker.setOperationName(operationName);
    return(operationInvoker.invoke(parameters));
  }

  public Map getOutputParams() {
    return(operationInvoker.getOutputParams());
  }

  public List getOutputValues() {
    return(operationInvoker.getOutputValues());
  }

  public void addParameter(String paramName, QName xmlType, Class parameterClass, ParameterMode parameterMode) {
    operationInvoker.addParameter(paramName, xmlType, parameterClass, parameterMode);
  }

  public QName getParameterTypeByName(String parameterName) {
    return(operationInvoker.getParameterType(parameterName));
  }

  public void setReturnType(QName xmlType, Class parameterClass) {
    operationInvoker.setReturnType(xmlType, parameterClass);
  }

  public QName getReturnType() {
    return(operationInvoker.getReturnType());
  }

  public void removeAllParameters() {
    operationInvoker.removeAllParameters();
  }
}
