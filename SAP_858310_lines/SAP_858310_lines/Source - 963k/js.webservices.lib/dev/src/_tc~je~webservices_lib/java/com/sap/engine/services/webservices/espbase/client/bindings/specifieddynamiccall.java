/*
 * Created on 2005-4-14
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.engine.services.webservices.espbase.client.bindings;

import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.ParameterMode;

import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientConfigurationContextImpl;
import com.sap.engine.services.webservices.espbase.mappings.EndpointMapping;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.jaxrpc.exceptions.WebserviceClientException;

/**
 * @author ivan-m
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SpecifiedDynamicCall extends DynamicCall {

  protected SpecifiedDynamicCall(DynamicServiceImpl dynamicService) {
    super(dynamicService);
  }
  
  protected SpecifiedDynamicCall(DynamicServiceImpl dynamicService, QName portTypeName) {
    super(dynamicService, portTypeName);
  }
  
  protected SpecifiedDynamicCall(DynamicServiceImpl dynamicService, QName portTypeName, QName operationName) {
    super(dynamicService, portTypeName, operationName);
  }
  
  protected SpecifiedDynamicCall(DynamicServiceImpl dynamicService, QName portTypeName, String operationName) {
    super(dynamicService, portTypeName, operationName);
  }
  
  public boolean isParameterAndReturnSpecRequired(QName paramName) {
    return(false);
  }

  public void addParameter(String paramName, QName xmlType, ParameterMode paramMode) {
    throw new UnsupportedOperationException("Configuration of operation parameters is forbidden.");
  }

  public void addParameter(String paramName, QName xmlType, Class javaType, ParameterMode paramMode) {
    throw new UnsupportedOperationException("Configuration of operation parameters is forbidden.");
  }
  
  public void setReturnType(QName paramName) {
    throw new UnsupportedOperationException("Configuration of operation parameters is not allowed.");
  }

  public void setReturnType(QName paramName, Class javaType) {
    throw new UnsupportedOperationException("Configuration of operation parameters is not allowed.");
  }
  
  public void removeAllParameters() {
    throw new UnsupportedOperationException("Configuration of operation parameters is not allowed.");
  }

  protected void initPort() {
    if(portTypeName == null) {
      ServiceMapping serviceMapping = dynamicService._getServiceContext().getMappingRules().getService(dynamicService.getServiceName());
      EndpointMapping[] endpointMappings = serviceMapping.getEndpoint();
      for(int i = 0; i < endpointMappings.length; i++) {
        EndpointMapping endpointMapping = endpointMappings[i];
        portTypeName = new QName(serviceMapping.getServiceName().getNamespaceURI(), endpointMapping.getPortQName());
        break;
      }
    }
  }
  
  protected void initClientConfigContext() {
    try {
      if(clientConfigContext == null) {
        clientConfigContext = (ClientConfigurationContextImpl)(dynamicService.createClientConfiguration(portTypeName));
      } else if(!portTypeName.equals(clientConfigContext.getStaticContext().getInterfaceData().getPortType())) {
        dynamicService.updateClientConfiguration(portTypeName, clientConfigContext);
      }
    } catch(WebserviceClientException wsClientExc) {
      throw new JAXRPCException(wsClientExc);
    }
  }
  
  protected OperationMapping determineOperationMapping() {
    return(clientConfigContext == null || operationName == null ? null : clientConfigContext.getStaticContext().getInterfaceData().getOperationByWSDLName(operationName.getLocalPart()));
  }
  
  protected void initOperationMapping(QName operationName) {
  }
}
