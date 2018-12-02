/*
 * Created on 2005-5-18
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.engine.services.webservices.espbase.client.bindings;

import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;

import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientConfigurationContextImpl;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.jaxrpc.encoding.ExtendedTypeMapping;
import com.sap.engine.services.webservices.jaxrpc.util.NameConvertor;

/**
 * @author ivan-m
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GenericDynamicCall extends DynamicCall {
  
  private OperationMapping operationMapping;
  private NameConvertor nameConvertor;
  
  protected GenericDynamicCall(DynamicServiceImpl dynamicService) {
    super(dynamicService);
    init();
  }
  
  protected GenericDynamicCall(DynamicServiceImpl dynamicService, QName portTypeName) {
    super(dynamicService, portTypeName);
    init();
  }
  
  protected GenericDynamicCall(DynamicServiceImpl dynamicService, QName portTypeName, QName operationName) {
    super(dynamicService, portTypeName, operationName);
    init();
  }
  
  protected GenericDynamicCall(DynamicServiceImpl dynamicService, QName portTypeName, String operationName) {
    super(dynamicService, portTypeName, operationName);
    init();
  }
  
  private void init() {
    nameConvertor = new NameConvertor();
    initOperationMapping();
    initClientConfigContext();
  }
  
  private void initOperationMapping() {
    operationMapping = new OperationMapping();
    operationMapping.setProperty(OperationMapping.OPERATION_STYLE, "rpc");
    operationMapping.setProperty(OperationMapping.OPERATION_MEP, OperationMapping.MEP_REQ_RESP);
    operationMapping.setProperty(OperationMapping.OPERATION_USE, OperationMapping.ENCODED_USE);
    operationMapping.setProperty(OperationMapping.IN_ENCODING_STYLE, NS.SOAPENC);
    operationMapping.setProperty(OperationMapping.OUT_ENCODING_STYLE, NS.SOAPENC);
  }
  
  protected OperationMapping determineOperationMapping() {
    return(operationMapping);
  }
  
  protected void initOperationMapping(QName operationName) {
    operationMapping.setWSDLOperationName(operationName.getLocalPart());
    operationMapping.setJavaMethodName(operationName.getLocalPart());
    operationMapping.setProperty(OperationMapping.SOAP_REQUEST_WRAPPER, operationName.getLocalPart());
    operationMapping.setProperty(OperationMapping.INPUT_NAMESPACE, operationName.getNamespaceURI());
    operationMapping.setProperty(OperationMapping.SOAP_RESPONSE_WRAPPER, operationName.getLocalPart() + OperationMapping.OPERATION_RESPONSE_SUFFIX);
    operationMapping.setProperty(OperationMapping.OUTPUT_NAMESPACE, operationName.getNamespaceURI());
  }
  
  public boolean isParameterAndReturnSpecRequired(QName paramName) {
    return(true);
  }

  public void addParameter(String paramName, QName xmlType, ParameterMode paramMode) {
    operationMapping.addParameter(createParameterMapping(paramName, xmlType, determineJavaType(xmlType), paramMode));
  }
  
  private ParameterMapping createParameterMapping(String paramName, QName xmlType, String javaType, ParameterMode paramMode) {
    ParameterMapping paramMapping = new ParameterMapping();
    if(paramName!= null) {
      paramMapping.setWSDLParameterName(paramName);
    }
    paramMapping.setSchemaQName(xmlType);
    paramMapping.setIsElement(false);
    paramMapping.setJavaType(javaType);
    if(paramMode != null && paramMode != ParameterMode.IN) {
      paramMapping.setHolderName(nameConvertor.primitiveToHolder(javaType));
    }
    paramMapping.setParameterType(determineParameterType(paramMode));
    return(paramMapping);
  }
  
  private String determineJavaType(QName xmlType) {
    return(((ExtendedTypeMapping)clientConfigContext.getServiceContext().getTypeMappingRegistry().getDefaultTypeMapping()).getDefaultJavaType(xmlType));
  }
  
  private int determineParameterType(ParameterMode paramMode) {
    if(paramMode == null) {
      return(ParameterMapping.RETURN_TYPE);
    }
    if(paramMode.equals(ParameterMode.IN)) {
      return(ParameterMapping.IN_TYPE);
    }
    if(paramMode.equals(ParameterMode.OUT)) {
      return(ParameterMapping.OUT_TYPE);
    }
    return(ParameterMapping.IN_OUT_TYPE);
  }

  public void addParameter(String paramName, QName xmlType, Class javaType, ParameterMode paramMode) {
    operationMapping.addParameter(createParameterMapping(paramName, xmlType, javaType.getName(), paramMode));
  }
  
  public void setReturnType(QName xmlType) {
    operationMapping.addParameter(createParameterMapping("result", xmlType, determineJavaType(xmlType), null));
  }

  public void setReturnType(QName xmlType, Class javaType) {
    operationMapping.addParameter(createParameterMapping("result", xmlType, javaType.getName(), null));
  }
  
  public void removeAllParameters() {
    ParameterMapping[] paramMappings = operationMapping.getParameter();
    for(int i = 0; i < paramMappings.length; i++) {
      ParameterMapping paramMapping = paramMappings[i];
      operationMapping.removeParameter(paramMapping);
    }
  }
  
  protected void initPort() {
  }
  
  protected void initClientConfigContext() {
    try {
      if(clientConfigContext == null) {
        clientConfigContext = (ClientConfigurationContextImpl)(dynamicService.createConfigNoWSDL());
        clientConfigContext.getStaticContext().getInterfaceData().setOperation(new OperationMapping[]{operationMapping});
      }
    } catch(ServiceException serviceExc) {
      throw new JAXRPCException(serviceExc);
    }
  }
}
