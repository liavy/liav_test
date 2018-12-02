package com.sap.engine.services.webservices.jaxrpc.service;

import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;

import javax.xml.rpc.Call;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.JAXRPCException;
import javax.xml.namespace.QName;
import java.util.*;
import java.rmi.RemoteException;

/**
 * Created by IntelliJ IDEA.
 * User: ivan-m
 * Date: 2004-7-22
 * Time: 17:39:13
 * To change this template use Options | File Templates.
 */
public abstract class AbstractCall implements Call {

  protected QName operationName;
  protected QName portTypeName;
  protected Hashtable properties;
  protected TypeMappingRegistryImpl typeMappingRegistry;

  public AbstractCall(TypeMappingRegistryImpl typeMappingRegistry) {
    properties = new Hashtable();
    this.typeMappingRegistry = typeMappingRegistry;
  }

  public abstract boolean isParameterAndReturnSpecRequired(QName qName);

  public void addParameter(String paramName, QName xmlType, ParameterMode parameterMode) {
    addParameter(paramName, xmlType, determineClass(xmlType), parameterMode);
  }

  private Class determineClass(QName xmlType) {
    String className = typeMappingRegistry.getDefaultTypeMappingImpl().getDefaultJavaType(xmlType);
    if(className == null) {
      return(null);
    }
    return(ClassResolver.resolve(className, getClass().getClassLoader()));
  }

  public abstract void addParameter(String paramName, QName xmlType, Class parameterClass, ParameterMode parameterMode);

  public abstract QName getParameterTypeByName(String parameterName);

  public void setReturnType(QName xmlType) {
    setReturnType(xmlType, determineClass(xmlType));
  }

  public abstract void setReturnType(QName xmlType, Class parameterClass);

  public abstract QName getReturnType();

  public abstract void removeAllParameters();

  public QName getOperationName() {
    if(operationName == null) {
      return(new QName(""));
    }
    return(operationName);
  }

  public void setOperationName(QName operationName) {
    this.operationName = operationName;
  }

  public QName getPortTypeName() {
    if(portTypeName == null) {
      return(new QName(""));
    }
    return(portTypeName);
  }

  public void setPortTypeName(QName qName) {
    portTypeName = qName;
  }

  public abstract void setTargetEndpointAddress(String endpointAdress);

  public abstract String getTargetEndpointAddress();

  public void setProperty(String name, Object value) {
    checkPropertyName(name);
    properties.put(name, value);
  }

  public Object getProperty(String name) {
    checkPropertyName(name);
    return(properties.get(name));
  }

  public void removeProperty(String name) {
    checkPropertyName(name);
    properties.remove(name);
  }

  public Iterator getPropertyNames() {
    return(properties.keySet().iterator());
  }

  public Object invoke(Object[] parameters) throws RemoteException {
    return(invoke(operationName, parameters));
  }

  public abstract Object invoke(QName operationName, Object[] parameters) throws RemoteException;

  public void invokeOneWay(Object[] objects) {
  }

  public abstract Map getOutputParams();

  public abstract List getOutputValues();

  private void checkPropertyName(String name) {
    if(name.equals(ENCODINGSTYLE_URI_PROPERTY)) {
      return;
    }
    if(name.equals(OPERATION_STYLE_PROPERTY)) {
      return;
    }
    if(name.equals(PASSWORD_PROPERTY)) {
      return;
    }
    if(name.equals(SESSION_MAINTAIN_PROPERTY)) {
      return;
    }
    if(name.equals(SOAPACTION_URI_PROPERTY)) {
      return;
    }
    if(name.equals(SOAPACTION_USE_PROPERTY )) {
      return;
    }
    if(name.equals(USERNAME_PROPERTY )) {
      return;
    }
    throw new JAXRPCException("Property '" + name + "' is not supported.");
  }
}
