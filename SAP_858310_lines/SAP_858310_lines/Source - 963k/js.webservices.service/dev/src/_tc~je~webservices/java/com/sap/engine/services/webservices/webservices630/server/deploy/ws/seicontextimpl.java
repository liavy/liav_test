package com.sap.engine.services.webservices.webservices630.server.deploy.ws;

import com.sap.engine.interfaces.webservices.server.deploy.ws.SEIContext;
import com.sap.engine.interfaces.webservices.runtime.ImplLink;
import com.sap.engine.interfaces.webservices.runtime.Feature;
import com.sap.engine.interfaces.webservices.runtime.OperationDefinition;
import com.sap.engine.services.webservices.runtime.definition.ServiceEndpointDefinition;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class SEIContextImpl implements SEIContext {

  private ServiceEndpointDefinition serviceEndpointDefinition = null;

  public SEIContextImpl() {

  }

  public SEIContextImpl(ServiceEndpointDefinition serviceEndpointDefinition) {
    this.serviceEndpointDefinition = serviceEndpointDefinition;
  }

  public void init(ServiceEndpointDefinition serviceEndpointDefinition) {
    this.serviceEndpointDefinition = serviceEndpointDefinition;
  }

  public ServiceEndpointDefinition getAssociatedServiceEndpoing() {
    return serviceEndpointDefinition;
  }

  public String getConfigurationName() {
    return serviceEndpointDefinition.getConfigurationName();
  }

  public String getTransportAddress() {
    return serviceEndpointDefinition.getServiceEndpointId();
  }

  public ImplLink getImplementationLink() {
    return serviceEndpointDefinition.getImplLink();
  }

  public String getRuntimeTransportBinding() {
    return serviceEndpointDefinition.getTransportBindingId();
  }

  public Feature[] getProtocolChain() {
    return serviceEndpointDefinition.getFeaturesChain();
  }

  public OperationDefinition[] getOperations() {
    return serviceEndpointDefinition.getOperations();
  }

  public String getVIName() {
    return serviceEndpointDefinition.getvInterfaceName();
  }

}
