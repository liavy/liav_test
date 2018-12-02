/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.espbase.client.api.impl;

import com.sap.engine.services.webservices.espbase.client.api.SessionInterface;
import com.sap.engine.services.webservices.espbase.client.bindings.DynamicStubImpl;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.BaseGeneratedStub;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.builtin.SessionProtocol;

import java.rmi.RemoteException;

/**
 * HTTP Session interface control implementation.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class SessionInterfaceImpl implements SessionInterface {

  BaseGeneratedStub port;
  SessionProtocol sessionProtocol;
  
  public SessionInterfaceImpl(BaseGeneratedStub stub) {
    this.port = stub;
    this.sessionProtocol = (SessionProtocol) stub._getGlobalProtocols().getProtocol("SessionProtocol");
  }

  public boolean isMaintainSession() {
    return(sessionProtocol.isMaintainSession());
  }

  public void closeSession() {
      sessionProtocol.closeSession();
  }

  public void releaseServerResources() throws RemoteException {
    if(port != null) {
      port._flush();
    }
  }

}
