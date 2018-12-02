package com.sap.engine.services.webservices.espbase.wsdas.impl;

import com.sap.engine.services.webservices.espbase.client.dynamic.DOperation;
import com.sap.engine.services.webservices.espbase.client.dynamic.ParametersConfiguration;
import com.sap.engine.services.webservices.espbase.wsdas.OperationConfig;

public class OperationConfigImpl implements OperationConfig {
  private String operationName = null;
  private DOperation operation = null;
  private ParametersConfiguration config = null; 
  
  public OperationConfigImpl(String opName, DOperation op, ParametersConfiguration paramsConfig) {
    operationName = opName;
    operation = op;
    config = paramsConfig;
  }
  
  public String getOperationName() {
    return operationName;  
  }
  
  public ParametersConfiguration getParametersConfiguration() {
    return config;
  }
  
  public void setInputParamValue(String paramName, Object value) {
    config.setInputParameterValue(paramName, value);
  }

  public Object getOutputParamValue(String paramName) {
    return config.getOutputParameterValue(paramName);
  }
  
  public String getProperty(String propertyName) {
    return operation.getProperty(propertyName);
  }

}
