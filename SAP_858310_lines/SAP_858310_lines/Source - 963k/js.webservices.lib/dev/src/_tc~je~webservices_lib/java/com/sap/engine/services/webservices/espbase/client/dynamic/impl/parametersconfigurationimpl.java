/*
 * Created on 2005-9-15
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.engine.services.webservices.espbase.client.dynamic.impl;

import java.util.Hashtable;
import javax.xml.rpc.holders.ObjectHolder;

import com.sap.engine.services.webservices.espbase.client.bindings.ParameterObject;
import com.sap.engine.services.webservices.espbase.client.dynamic.DParameter;
import com.sap.engine.services.webservices.espbase.client.dynamic.ParametersConfiguration;


/**
 * @author ivan-m
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ParametersConfigurationImpl implements ParametersConfiguration {
  
  private Hashtable<String,ParameterObject> nameToInParamObjectsMapping;
  private Hashtable<String,ParameterObject> nameToOutParamObjectsMapping;
  private Hashtable<String,ParameterObject> nameToFaultParamObjectsMapping;
  private ParameterObject[] parameterObjects;
  
  protected ParametersConfigurationImpl(DOperationImpl dOperation) {
    initNameToParamObjectsMapping(dOperation);
  }
  
  private void initNameToParamObjectsMapping(DOperationImpl dOperation) {
      nameToInParamObjectsMapping = new Hashtable<String,ParameterObject>();
      nameToOutParamObjectsMapping = new Hashtable<String,ParameterObject>();
      nameToFaultParamObjectsMapping = new Hashtable<String,ParameterObject>();
      
      DParameter[] parameters = dOperation.getAllParameters();
      parameterObjects = new ParameterObject[parameters.length];      
      for(int i = 0; i < parameters.length; i++) {
        createAndCollectParameterObject(i, parameters[i]);
      }
  }
  
  private void createAndCollectParameterObject( int parameterObjIndex, DParameter parameter) {
    ParameterObject parameterObject = new ParameterObject();    
    parameterObject.parameterType = parameter.getParameterClass();
    // ParameterObject parameterObject = createParameterObject(parameterMapping, typeMapping);
    int parameterType = parameter.getParameterType();
    String paramName = parameter.getName();
    switch(parameterType) {
      case DParameter.INPUT : {
        nameToInParamObjectsMapping.put(paramName, parameterObject);
        break;
      }
      case DParameter.OUTPUT : {
        nameToOutParamObjectsMapping.put(paramName, parameterObject);
        parameterObject.parameterValue = new ObjectHolder();
        break;
      }
      case DParameter.RETURN : {
        nameToOutParamObjectsMapping.put(paramName, parameterObject);
        break;
      }
      case DParameter.INOUT : {
        nameToInParamObjectsMapping.put(paramName, parameterObject);
        nameToOutParamObjectsMapping.put(paramName, parameterObject);
        parameterObject.parameterValue = new ObjectHolder();
        break;
      }
      case DParameter.FAULT : {
        nameToFaultParamObjectsMapping.put(paramName, parameterObject);
        break;
      }
    }
    parameterObjects[parameterObjIndex] = parameterObject;
  }
  
  public void setInputParameterValue(String name, Object value) {
    ParameterObject paramObject = determineParameterObject(name, nameToInParamObjectsMapping); 
    if (paramObject.parameterValue != null && paramObject.parameterValue instanceof ObjectHolder) {
      // This is in or out parameter.
      if (value != null && value instanceof ObjectHolder) {
        // Holder already is created
        paramObject.parameterValue = value;
      } else {
        ((ObjectHolder) paramObject.parameterValue).value = value;
      }      
    } else {
      paramObject.parameterValue = value;
    }
  }
  
  private ParameterObject determineParameterObject(String paramName, Hashtable nameToParamObjectsMapping) {
    ParameterObject paramObject = (ParameterObject)(nameToParamObjectsMapping.get(paramName));
    if(paramObject == null) {
      throw new IllegalArgumentException("Parameter with name '" + paramName + "' does not exist.");
    }
    return(paramObject);
  }
  
  public Object getOutputParameterValue(String name) {
    ParameterObject paramObject = determineParameterObject(name, nameToOutParamObjectsMapping); 
    if (paramObject.parameterValue != null && paramObject.parameterValue instanceof ObjectHolder) {
      return ((ObjectHolder) paramObject.parameterValue).value;
    } else {
      return(paramObject.parameterValue);
    }
  }
  
  public Throwable getFaultParameterValue(String name) {
    ParameterObject paramObject = determineParameterObject(name, nameToFaultParamObjectsMapping); 
    return((Throwable)(paramObject.parameterValue));
  }
  
  protected void resetFaultParameterValue(String name) {
    ParameterObject paramObject = determineParameterObject(name, nameToFaultParamObjectsMapping);
    paramObject.parameterValue = null;
  }
  
  protected ParameterObject[] getParameterObjects() {
    return(parameterObjects);
  }
}
