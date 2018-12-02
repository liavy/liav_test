/*
 * Created on 2005-5-18
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.engine.services.webservices.espbase.client.bindings;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.Stub;

import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientConfigurationContextImpl;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.jaxrpc.encoding.ExtendedTypeMapping;

/**
 * @author ivan-m
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class DynamicCall implements Call {

  protected static Vector SUPPORTED_PROEPRTIES = new Vector();

  protected DynamicServiceImpl dynamicService;
  protected ClientConfigurationContextImpl clientConfigContext;
  protected QName portTypeName;
  protected QName operationName;
  protected String endpointAdress;
  protected Vector propNames;
  protected Vector propValues;
  protected Hashtable outParamsNameToValueMapping;
  protected boolean isInvoked;
  
  static {
    SUPPORTED_PROEPRTIES.add(USERNAME_PROPERTY);
    SUPPORTED_PROEPRTIES.add(PASSWORD_PROPERTY);
    SUPPORTED_PROEPRTIES.add(OPERATION_STYLE_PROPERTY);
    SUPPORTED_PROEPRTIES.add(SOAPACTION_USE_PROPERTY);
    SUPPORTED_PROEPRTIES.add(SOAPACTION_URI_PROPERTY);
    SUPPORTED_PROEPRTIES.add(ENCODINGSTYLE_URI_PROPERTY);
    SUPPORTED_PROEPRTIES.add(SESSION_MAINTAIN_PROPERTY);
  }

  protected DynamicCall(DynamicServiceImpl dynamicService) {
    this.dynamicService = dynamicService;
    propNames = new Vector();
    propValues = new Vector();
    outParamsNameToValueMapping = new Hashtable();
    isInvoked = false;
  }
  
  protected DynamicCall(DynamicServiceImpl dynamicService, QName portTypeName) {
    this(dynamicService);
    this.portTypeName = portTypeName;
  }
  
  protected DynamicCall(DynamicServiceImpl dynamicService, QName portTypeName, QName operationName) {
    this(dynamicService, portTypeName);
    setOperationName(operationName);
  }
  
  protected DynamicCall(DynamicServiceImpl dynamicService, QName portTypeName, String operationName) {
    this(dynamicService, portTypeName, new QName(portTypeName.getNamespaceURI(), operationName));
  }
  
  public abstract boolean isParameterAndReturnSpecRequired(QName paramName);
  
  public abstract void addParameter(String paramName, QName xmlType, ParameterMode paramMode);

  public abstract void addParameter(String paramName, QName xmlType, Class javaType, ParameterMode paramMode);
  
  public QName getParameterTypeByName(String paramName) {
    OperationMapping operationMapping = determineOperationMapping();
    if(operationMapping != null) {
      ParameterMapping[] parameterMappings = operationMapping.getParameter();
      for(int i = 0; i < parameterMappings.length; i++) {
        ParameterMapping parameterMapping = parameterMappings[i];
        if(parameterMapping.getWSDLParameterName().equals(paramName)) {
          return(parameterMapping.isElement() ? ((ExtendedTypeMapping)clientConfigContext.getTypeMaping()).getTypeForElement(parameterMapping.getSchemaQName()) : parameterMapping.getSchemaQName());
        }
      }
    }
    return(null);
  }

  protected abstract OperationMapping determineOperationMapping();
  
  protected abstract void initOperationMapping(QName operationName);
  
  public abstract void setReturnType(QName paramName);

  public abstract void setReturnType(QName paramName, Class javaType);
  
  public QName getReturnType() {
    OperationMapping operationMapping = determineOperationMapping();
    if(operationMapping != null) {
      ParameterMapping[] parameterMappings = operationMapping.getParameter();
      for(int i = 0; i < parameterMappings.length; i++) {
        ParameterMapping parameterMapping = parameterMappings[i];
        if(parameterMapping.getParameterType() == ParameterMapping.RETURN_TYPE) {
          return(parameterMapping.isElement() ? ((ExtendedTypeMapping)clientConfigContext.getTypeMaping()).getTypeForElement(parameterMapping.getSchemaQName()) : parameterMapping.getSchemaQName());
        }
      }
    }
    return(null);
  }

  public abstract void removeAllParameters();

  public QName getOperationName() {
    return(operationName == null ? new QName("") : operationName);
  }

  public void setOperationName(QName operationName) {
    this.operationName = operationName;
  }

  public QName getPortTypeName() {
    return(portTypeName == null ? new QName("") : portTypeName);
  }

  public void setPortTypeName(QName portTypeName) {
    this.portTypeName = portTypeName;
  }

  public void setTargetEndpointAddress(String endpointAdress) {
    this.endpointAdress = endpointAdress;
  }

  public String getTargetEndpointAddress() {
    if(clientConfigContext == null) {
      return(endpointAdress);
    }
    String clientContextEndpointAdress = (String)(clientConfigContext.getPersistableContext().getProperty(Stub.ENDPOINT_ADDRESS_PROPERTY)); 
    return(endpointAdress == null ? clientContextEndpointAdress : endpointAdress);
  }
  
  private void validatePropertyName(String propertyName) {
    if(!SUPPORTED_PROEPRTIES.contains(propertyName)) {
      throw new JAXRPCException("Property '" + propertyName + "' is not supported.");
    }
  }
  
  public void setProperty(String name, Object value) {
    validatePropertyName(name);
    for(int i = 0; i < propNames.size(); i++) {
      if(propNames.get(i).equals(name)) {
        propValues.set(i, value);
        return;
      }
    }
    propNames.add(name);
    propValues.add(value);
  }

  public Object getProperty(String name) {
    validatePropertyName(name);
    for(int i = 0; i < propNames.size(); i++) {
      if(propNames.get(i).equals(name)) {
        return(propValues.get(i));
      }
    }
    return(null);
  }

  public void removeProperty(String name) {
    validatePropertyName(name);
    for(int i = 0; i < propNames.size(); i++) {
      if(propNames.get(i).equals(name)) {
        propNames.remove(i);
        propValues.remove(i);
        return;
      }
    }
  }

  public Iterator getPropertyNames() {
    return(new PropertyNamesIterrator());
  }

  public Object invoke(Object args[]) throws RemoteException {
    return(invoke(operationName, args));
  }
  
  public Object invoke(QName operationName, Object args[]) throws RemoteException {
    validateCallConfig(operationName);
    initPort();
    initClientConfigContext();
    if(args == null) {
      throw new JAXRPCException("The array of operation parameters, passed as an argument of the invoke method, is null.");
    }
    initOperationMapping(operationName);
    OperationMapping operationMapping = clientConfigContext.getStaticContext().getInterfaceData().getOperationByWSDLName(operationName.getLocalPart());
    if(operationMapping == null) {
      throw new JAXRPCException("Operation '" + operationName + "' is not available.");
    }
    if(endpointAdress != null) {
      clientConfigContext.getPersistableContext().setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, endpointAdress);
    }
    
    Vector faultParamObjects = new Vector();
    ParameterMapping[] parameterMappings = operationMapping.getParameter();
    ClassLoader clientAppClassLoader = clientConfigContext.getClientAppClassLoader();
    ParameterObject[] paramObjects = new ParameterObject[parameterMappings.length];
    ParameterObject returnParamObject = null;
    int argsIndex = 0;
    for(int i = 0; i < parameterMappings.length; i++) {
      ParameterMapping parameterMapping = parameterMappings[i]; 
      paramObjects[i] = new ParameterObject();
      try {
        if(parameterMapping.getParameterType() == ParameterMapping.IN_TYPE || parameterMapping.getParameterType() == ParameterMapping.IN_OUT_TYPE || parameterMapping.getParameterType() == ParameterMapping.OUT_TYPE) {
          paramObjects[i].parameterValue = args[argsIndex];
          paramObjects[i].parameterType = args[argsIndex++].getClass();
        } else if(parameterMapping.getParameterType() == ParameterMapping.FAULT_TYPE) {
          paramObjects[i].parameterType = clientAppClassLoader.loadClass(parameterMapping.getJavaType());
          faultParamObjects.add(paramObjects[i]);
        } else {
          paramObjects[i].parameterType = clientAppClassLoader.loadClass(parameterMapping.getJavaType());
          returnParamObject = paramObjects[i];
        }
      } catch(ClassNotFoundException classNFExc) {
        throw new JAXRPCException(classNFExc);
      }
    }
    clientConfigContext.setInvokedOperation(operationMapping.getJavaMethodName(), paramObjects);
    clientConfigContext.getTransportBinding().call(clientConfigContext);
    
    outParamsNameToValueMapping.clear();
    for(int i = 0; i < paramObjects.length; i++) {
      ParameterMapping parameterMapping = parameterMappings[i]; 
      if(parameterMapping.getParameterType() == ParameterMapping.OUT_TYPE || parameterMapping.getParameterType() == ParameterMapping.IN_OUT_TYPE) {
        outParamsNameToValueMapping.put(parameterMapping.getWSDLParameterName(), ((ParameterObject)(faultParamObjects.get(i))).parameterValue);
      }
    }
    
    isInvoked = true;
    for(int i = 0; i < faultParamObjects.size(); i++) {
      ParameterObject faltParamObject = (ParameterObject)(faultParamObjects.get(i));
      if(faltParamObject.parameterValue != null) {
        throw (RemoteException)(faltParamObject.parameterValue);
      }
    }
    return(returnParamObject == null ? null : returnParamObject.parameterValue);
  }
  
  protected abstract void initPort();
  
  private void validateCallConfig(QName operationName) {
    if(operationName == null) {
      throw new JAXRPCException("Operation name is not specified.");
    }
  }
  
  protected abstract void initClientConfigContext();
  
  public void invokeOneWay(Object args[]) {
  }
  
  public Map getOutputParams() {
    if(!isInvoked) {
      throw new JAXRPCException("Invoke method should be called.");
    }
    return(outParamsNameToValueMapping);
  }
  
  public List getOutputValues() {
    if(!isInvoked) {
      throw new JAXRPCException("Invoke method should be called.");
    }
    return(new ArrayList(outParamsNameToValueMapping.values()));
  }
  
  private class PropertyNamesIterrator implements Iterator {
    
    private int index;
    
    private PropertyNamesIterrator() {
      index = 0;
    }
    
    public boolean hasNext() {
      return(index < propNames.size());
    }
    
    public Object next() {
      if(index < propNames.size()) {
        return(propNames.get(index++));
      }
      return(null);
    }
    
    public void remove() {
      if(index < propNames.size()) {
        propNames.remove(index);
        propValues.remove(index);
      }
    }
  }
}
