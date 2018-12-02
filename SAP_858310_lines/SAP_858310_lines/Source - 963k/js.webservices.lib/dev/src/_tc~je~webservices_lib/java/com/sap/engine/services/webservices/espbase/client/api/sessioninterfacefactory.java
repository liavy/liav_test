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
package com.sap.engine.services.webservices.espbase.client.api;

import com.sap.engine.services.webservices.espbase.client.api.impl.SessionInterfaceImpl;
import com.sap.engine.services.webservices.espbase.client.api.impl.SessionInterfaceNYImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.DynamicStubImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.impl.DInterfaceInvokerImpl;
import com.sap.engine.services.webservices.espbase.client.jaxws.core.JAXWSProxy;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.BaseGeneratedStub;

/**
 * HTTP SessionInterface implementation factory.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class SessionInterfaceFactory {

  /**
   * Returns new SessionInterface implementation if the SessionProtocol is available.
   * @param port
   * @return
   */
  public static final SessionInterface getInterface(Object port) {
    if (port == null) {
      return null;
    }
    if (port instanceof DInterfaceInvokerImpl) {
      port = ((DInterfaceInvokerImpl) port).getStubInstance();
    }
    if(port instanceof DynamicStubImpl) {
      return(new SessionInterfaceNYImpl(((DynamicStubImpl)port)._getConfigurationContext()));
    }    
    if (port instanceof BaseGeneratedStub) {
      BaseGeneratedStub stub =  (BaseGeneratedStub) port;
      if (stub._getGlobalProtocols().getProtocol("SessionProtocol") != null) {
        // SessionProtocol is available
        return new SessionInterfaceImpl(stub);
      }
    }
    if (port instanceof JAXWSProxy) {      
      return (new SessionInterfaceNYImpl(((JAXWSProxy) port)._getConfigurationContext()));
    }    
    return null;
  }

}
