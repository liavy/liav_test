package com.sap.engine.services.webservices.webservices630.server.deploy.wsclient;

import com.sap.engine.interfaces.webservices.server.deploy.wsclient.WSClientContext;
import com.sap.engine.interfaces.webservices.server.deploy.wsclient.LPPropertyContext;
import com.sap.engine.services.webservices.runtime.definition.wsclient.WSClientRuntimeInfo;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.PropertyContext;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ServiceBaseServer;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSClientContextImpl extends WSClientBaseContextImpl implements WSClientContext {

  private PropertyContext[] propertyContexts = null;

  public WSClientContextImpl() {
    super();
  }

  public WSClientContextImpl(WSClientRuntimeInfo wsClientRuntimeInfo, PropertyContext[] propertyContexts) {
    super();
    init(wsClientRuntimeInfo, propertyContexts);
  }

  public void init(WSClientRuntimeInfo wsClientRuntimeInfo, PropertyContext[] propertyContexts) {
    super.init(wsClientRuntimeInfo);
    this.propertyContexts = propertyContexts;
  }

  public void setPropertyContexts(PropertyContext[] propertyContexts) {
    this.propertyContexts = propertyContexts;
  }

  public LPPropertyContext[] getLPPropertyContexts() {
    return propertyContexts;
  }

}
