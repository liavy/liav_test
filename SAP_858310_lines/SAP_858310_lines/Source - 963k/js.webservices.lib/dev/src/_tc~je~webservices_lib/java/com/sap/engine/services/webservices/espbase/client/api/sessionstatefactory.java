/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.client.api;

import com.sap.engine.services.webservices.espbase.client.api.impl.SessionStateInterfaceImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.DynamicStubImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.DInterfaceInvoker;
import com.sap.engine.services.webservices.espbase.client.dynamic.impl.DInterfaceInvokerImpl;
import com.sap.engine.services.webservices.espbase.client.jaxws.core.JAXWSProxy;
import com.sap.engine.services.webservices.espbase.wsdas.impl.WSDASImpl;

public class SessionStateFactory {
  
  public static final SessionStateInterface getInterface(Object port) {
    if (port == null) {
      // No port parameter is passed - return null
      return null;
    }
    if (port instanceof DynamicStubImpl) {
      return new SessionStateInterfaceImpl(((DynamicStubImpl) port)._getConfigurationContext());
    }
    if (port instanceof DInterfaceInvoker) {
      return new SessionStateInterfaceImpl(((DInterfaceInvokerImpl) port)._getConfigurationContext());
    }
    if (port instanceof WSDASImpl && ((WSDASImpl) port)._getConfigurationContext()!= null) {
      return new SessionStateInterfaceImpl(((WSDASImpl) port)._getConfigurationContext());
    }
    if(port instanceof JAXWSProxy) {
      return new SessionStateInterfaceImpl(((JAXWSProxy)port)._getConfigurationContext());
    }    
    return null;    
  }
}
