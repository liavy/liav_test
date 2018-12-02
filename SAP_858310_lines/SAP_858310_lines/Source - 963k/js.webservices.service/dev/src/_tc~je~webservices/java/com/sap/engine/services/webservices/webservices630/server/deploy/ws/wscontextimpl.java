package com.sap.engine.services.webservices.webservices630.server.deploy.ws;

import com.sap.engine.interfaces.webservices.server.deploy.ws.WSContext;
import com.sap.engine.interfaces.webservices.server.deploy.ws.SEIContext;
import com.sap.engine.services.webservices.runtime.definition.WSRuntimeDefinition;
import com.sap.engine.services.webservices.runtime.definition.ServiceEndpointDefinition;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSContextImpl implements WSContext {

  private WSRuntimeDefinition wsRuntimeDefinition = null;
  private SEIContext[] seiContexts = new SEIContextImpl[0];

  public WSContextImpl() {
  }

  public WSContextImpl(WSRuntimeDefinition wsRuntimeDefinition) {
    init(wsRuntimeDefinition);
  }

  public void init(WSRuntimeDefinition wsRuntimeDefinition) {
    this.wsRuntimeDefinition = wsRuntimeDefinition;
    seiContexts = createSEIContexts(wsRuntimeDefinition.getServiceEndpointDefinitions());
  }

  public WSRuntimeDefinition getAssociatedWebService() {
    return wsRuntimeDefinition;
  }

  public String getApplicationName() {
    return wsRuntimeDefinition.getWSIdentifier().getApplicationName();
  }

  public String getWebServiceName() {
    return wsRuntimeDefinition.getWSIdentifier().getServiceName();
  }

  public SEIContext[] getSEIContexts() {
    return seiContexts;
  }

  public String getWSDName() /*?*/ {
    return wsRuntimeDefinition.getWsdName();
  }

  private SEIContext[] createSEIContexts(ServiceEndpointDefinition[] endpointDefinitions) {
    if(endpointDefinitions == null) {
      return new SEIContextImpl[0];
    }

    SEIContext[] seiContexts = new SEIContextImpl[endpointDefinitions.length];
    for(int i = 0; i < endpointDefinitions.length; i++) {
      ServiceEndpointDefinition endpointDefinition = endpointDefinitions[i];
      SEIContextImpl seiContext = new SEIContextImpl(endpointDefinition);
      seiContexts[i] = seiContext;
    }

    return seiContexts;
  }

}
