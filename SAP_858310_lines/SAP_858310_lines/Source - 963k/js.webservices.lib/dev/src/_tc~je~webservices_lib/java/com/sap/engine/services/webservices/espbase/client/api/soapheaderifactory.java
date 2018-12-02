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

import com.sap.engine.services.webservices.espbase.client.api.impl.SOAPHeaderInterfaceNYImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.DynamicStubImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.impl.DInterfaceInvokerImpl;
import com.sap.engine.services.webservices.espbase.client.jaxws.core.JAXWSProxy;
import com.sap.engine.services.webservices.espbase.wsdas.impl.WSDASImpl;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.BaseGeneratedStub;

/**
 * SOAPHeaderInterface factory. Gets interface for SOAP Header manipulation.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class SOAPHeaderIFactory {

  public static final SOAPHeaderInterface getInterface(Object port) {
    if (port == null) {
      // No port parameter is passed - return null
      return null;
    }
    if (port instanceof DInterfaceInvokerImpl) {
      return new SOAPHeaderInterfaceNYImpl(((DInterfaceInvokerImpl) port)._getConfigurationContext());      
    }
    if (port instanceof WSDASImpl && ((WSDASImpl) port)._getConfigurationContext()!= null) {
      return new SOAPHeaderInterfaceNYImpl(((WSDASImpl) port)._getConfigurationContext());
    }
    if (port instanceof DynamicStubImpl) {
      return new SOAPHeaderInterfaceNYImpl(((DynamicStubImpl) port)._getConfigurationContext());
    }    
    if (port instanceof BaseGeneratedStub) {
      return (SOAPHeaderInterface) ((BaseGeneratedStub) port)._getGlobalProtocols().getProtocol("SoapHeadersProtocol");
    }
    if (port instanceof JAXWSProxy) {
      return new SOAPHeaderInterfaceNYImpl(((JAXWSProxy) port)._getConfigurationContext());
    }
    return null;
  }
}
