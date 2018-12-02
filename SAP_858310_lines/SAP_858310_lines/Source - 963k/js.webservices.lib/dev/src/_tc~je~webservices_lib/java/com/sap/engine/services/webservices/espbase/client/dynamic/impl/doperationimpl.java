/*
 * Created on 2005-7-15
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.engine.services.webservices.espbase.client.dynamic.impl;

import java.util.Locale;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.client.dynamic.DOperation;
import com.sap.engine.services.webservices.espbase.client.dynamic.DParameter;
import com.sap.engine.services.webservices.espbase.client.dynamic.DesignProperties;
import com.sap.engine.services.webservices.espbase.client.dynamic.util.Util;
import com.sap.engine.services.webservices.espbase.configuration.Behaviour;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceData;
import com.sap.engine.services.webservices.espbase.configuration.OperationData;
import com.sap.engine.services.webservices.espbase.configuration.PropertyListType;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.configuration.marshallers.wsrm.RMConfigurationMarshaller;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.espbase.wsdl.Operation;
import com.sap.engine.services.webservices.jaxrpc.encoding.ExtendedTypeMapping;

/**
 * @author Ivan-M
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DOperationImpl extends DDocumentableImpl implements DOperation {
  
  private Vector inputParameters;
  private Vector outputParameters;
  private Vector inoutParameters;
  private Vector faultParameters;
  private DParameter returnParameter;
  private DParameter[] allParameters = null;
  private OperationMapping operationMapping;
  private InterfaceData iData;
  
  protected DOperationImpl(Operation operation, OperationMapping operationMapping, ExtendedTypeMapping typeMapping, InterfaceData iData) throws ParserConfigurationException {
    this.operationMapping = operationMapping;
    this.iData = iData;
    initParameters(typeMapping);
    initDocumentationElement(operation);
  }
  
  private void initParameters(ExtendedTypeMapping typeMapping) {
    inputParameters = new Vector();
    outputParameters = new Vector();
    inoutParameters = new Vector();
    faultParameters = new Vector();
    ParameterMapping[] parameterMappings = operationMapping.getParameter();
    allParameters = new DParameter[parameterMappings.length];
    for(int i = 0 ; i < parameterMappings.length; i++) {
      ParameterMapping parameterMapping = parameterMappings[i];
      int parameterType = parameterMapping.getParameterType();
      DParameter parameter = null;
      switch(parameterType) {
        case ParameterMapping.IN_TYPE : {
          parameter = new DParameterImpl(parameterMapping, DParameter.INPUT, typeMapping);
          inputParameters.add(parameter);          
          break;
        }
        case ParameterMapping.OUT_TYPE : {
          parameter = new DParameterImpl(parameterMapping, DParameter.OUTPUT, typeMapping);
          outputParameters.add(parameter);
          break;
        }
        case ParameterMapping.FAULT_TYPE : {
          parameter = new DParameterImpl(parameterMapping, DParameter.FAULT, typeMapping);
          faultParameters.add(parameter);
          break;
        }
        case ParameterMapping.IN_OUT_TYPE : {
          parameter = new DParameterImpl(parameterMapping, DParameter.INOUT, typeMapping);
          inoutParameters.add(parameter);
          break;
        }
        case ParameterMapping.RETURN_TYPE : {
          parameter = new DParameterImpl(parameterMapping, DParameter.RETURN, typeMapping);
          returnParameter = parameter;
          break;
        }
      }
      allParameters[i] = parameter;
    }
  }
  
  public DParameter[] getInputParameters() {
    return(getDParameters(inputParameters));
  }
  
  protected Vector getInputParametersCollector() {
    return(inputParameters);
  }
  
  private DParameter[] getDParameters(Vector dParametersCollector) {
    DParameter[] dParameters = new DParameter[dParametersCollector.size()];
    dParametersCollector.copyInto(dParameters);
    return(dParameters);
  }
  
  public DParameter[] getOutputParameters() {
    return(getDParameters(outputParameters));
  }
  
  protected Vector getOutputParametersCollector() {
    return(outputParameters);
  }
  
  public DParameter[] getInOutParameters() {
    return(getDParameters(inoutParameters));
  }
  
  protected Vector getInoutParametersCollector() {
    return(inoutParameters);
  }
  
  public DParameter getReturnParameter() {
    return(returnParameter);
  }
  
  public DParameter[] getFaultParameters() {
    return(getDParameters(faultParameters));
  }
  
  protected Vector getFaultParametersCollector() {
    return(faultParameters);
  }
  
  public String getName() {
    return(operationMapping.getWSDLOperationName());
  }
  
  protected String getJavaMethodName() {
    return(operationMapping.getJavaMethodName());
  }
  
  protected OperationMapping getOperationMapping() {
    return(operationMapping);
  }
  
  protected DParameter[] getAllParameters() {
    return allParameters;
  }
  
  public String toString() {
    StringBuffer toStringBuffer = new StringBuffer();
    initToStringBuffer(toStringBuffer, "");
    return(toStringBuffer.toString());
  }
  
  protected void initToStringBuffer(StringBuffer toStringBuffer, String offset) {
    Util.initToStringBuffer_AppendOffsetAndId(toStringBuffer, offset, "Operation");
    Util.initToStringBuffer_ObjectValue(toStringBuffer, offset + Util.TO_STRING_OFFSET, "operation name : ", getName());
    initToStringBuffer_Documentation(toStringBuffer, offset + Util.TO_STRING_OFFSET);
    initToStringBuffer_OperationParameters(toStringBuffer, offset + Util.TO_STRING_OFFSET, "input parameters", inputParameters);
    initToStringBuffer_OperationParameters(toStringBuffer, offset + Util.TO_STRING_OFFSET, "output parameters", outputParameters);
    initToStringBuffer_OperationParameters(toStringBuffer, offset + Util.TO_STRING_OFFSET, "inout parameters", inoutParameters);
    initToStringBuffer_OperationParameters(toStringBuffer, offset + Util.TO_STRING_OFFSET, "fault parameters", faultParameters);
    initToStringBuffer_ReturnParameters(toStringBuffer, offset + Util.TO_STRING_OFFSET);
  }
  
  private void initToStringBuffer_OperationParameters(StringBuffer toStringBuffer, String offset, String parametersId, Vector parameters) {
    Util.initToStringBuffer_AppendCReturnOffsetAndId(toStringBuffer, offset, parametersId);
    for(int i = 0; i < parameters.size(); i++) {
      DParameterImpl parameter = (DParameterImpl)(parameters.get(i));
      toStringBuffer.append("\n");
      parameter.initToStringBuffer(toStringBuffer, offset + Util.TO_STRING_OFFSET);
    }
  }
  
  private void initToStringBuffer_ReturnParameters(StringBuffer toStringBuffer, String offset) {
    if(returnParameter != null) {
      Util.initToStringBuffer_AppendCReturnOffsetAndId(toStringBuffer, offset, "return parameter");
      toStringBuffer.append("\n");
      ((DParameterImpl)returnParameter).initToStringBuffer(toStringBuffer, offset + Util.TO_STRING_OFFSET);
    }
  }
  
  /**
   * Returns the value of some operation design time property.
   * @param key
   * @return
   */
  public String getProperty(String key) {
    if (DesignProperties.PROPERTY_MEP.equals(key)) {
      String operationMep = this.operationMapping.getProperty(OperationMapping.OPERATION_MEP);
      if (OperationMapping.MEP_ONE_WAY.equals(operationMep)) {
        return DesignProperties.MEP_ONEWAY;
      }
      if (OperationMapping.MEP_REQ_RESP.equals(operationMep)) {
        return DesignProperties.MEP_REQ_RESP;
      }
    }
    if (DesignProperties.PROPERTY_WSRM.equals(key)) {
      if (this.iData == null) {
        return DesignProperties.DISABLED;
      }
      OperationData opData = this.iData.getOperationData(operationMapping.getWSDLOperationName());
      if (opData != null) {
        PropertyListType[] propLists = opData.getPropertyList();
        if (propLists != null && propLists.length > 0) {
          for (PropertyListType propList : propLists) {
            PropertyType prop = propList.getProperty("http://www.sap.com/NW05/soap/features/wsrm/", "enableWSRM");              
            if (prop != null) {
              String propValue = prop.get_value();              
              if (propValue != null && propValue.toLowerCase(Locale.ENGLISH).equals("true")) {
                return DesignProperties.ENABLED;               
              }
            }
          }
        }                  
      }
      return DesignProperties.DISABLED;
    }
    if (DesignProperties.PROPERTY_IDEMPOTENCY.equals(key)) {
      if (this.iData == null) {
        return DesignProperties.DISABLED;
      }      
      OperationData opData = this.iData.getOperationData(operationMapping.getWSDLOperationName());
      if(opData != null && getProperty(RMConfigurationMarshaller.NS_IDEMPOTENCY, RMConfigurationMarshaller.IDEMPOTENCY_PROPERTY, opData) != null) {
        return DesignProperties.ENABLED;
      }
      return DesignProperties.DISABLED;
    }
    // TODO: Transactional check
    return null;
  }
  
  public static final String getProperty(String propertyNS, String propertyName, Behaviour behaviour) {
    PropertyType property = behaviour.getSinglePropertyList().getProperty(propertyNS, propertyName);
    return(getProperty(property));
  }
  
  public static final String getProperty(PropertyType property) {
    return(property != null ? property.get_value() : null);
  }
  
  
  
}
