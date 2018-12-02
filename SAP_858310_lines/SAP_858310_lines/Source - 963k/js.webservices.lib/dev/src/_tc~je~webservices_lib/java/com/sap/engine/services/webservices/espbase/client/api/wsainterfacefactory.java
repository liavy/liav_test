/*
 * Copyright (c) 2005 by SAP Labs Bulgaria.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.espbase.client.api;

import com.sap.engine.services.webservices.espbase.client.api.impl.AddressingInterfaceImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.DynamicStubImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.impl.DInterfaceInvokerImpl;
import com.sap.engine.services.webservices.espbase.client.jaxws.core.JAXWSProxy;
import com.sap.engine.services.webservices.espbase.wsdas.impl.WSDASImpl;

/**
 * WSA Addressing interface factory
 * @author Vladimir Videlov
 * @version 7.10
 */
public class WSAInterfaceFactory {

  public static final AddressingInterface getInterface(Object port) {
    if (port == null) {
      // No port parameter is passed - return null
      return null;
    }
    if (port instanceof DInterfaceInvokerImpl) {
      return new AddressingInterfaceImpl(((DInterfaceInvokerImpl) port)._getConfigurationContext());
    }
    if (port instanceof WSDASImpl) {
      return new AddressingInterfaceImpl(((WSDASImpl) port)._getConfigurationContext());
    }
    if (port instanceof DynamicStubImpl) {
      return new AddressingInterfaceImpl(((DynamicStubImpl) port)._getConfigurationContext());
    }
    if (port instanceof JAXWSProxy) {
      return new AddressingInterfaceImpl(((JAXWSProxy) port)._getConfigurationContext());
    }

    return null;
  }
}