package com.sap.engine.services.webservices.espbase.client.api;

import com.sap.engine.services.webservices.espbase.client.api.impl.IdempotencyManagementInterfaceImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.DynamicStubImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.impl.DInterfaceInvokerImpl;
import com.sap.engine.services.webservices.espbase.client.jaxws.core.JAXWSProxy;
import com.sap.engine.services.webservices.espbase.wsdas.impl.WSDASImpl;

public class IdempotencyManagementFactory {

  public static final IdempotencyManagementInterface getInterface(Object port) {
    if (port == null) {
      return null;
    }
    if (port instanceof DInterfaceInvokerImpl) {
      return new IdempotencyManagementInterfaceImpl(((DInterfaceInvokerImpl)port)._getConfigurationContext());
    }
    if (port instanceof WSDASImpl && ((WSDASImpl) port)._getConfigurationContext()!= null) {
        return new IdempotencyManagementInterfaceImpl(((WSDASImpl) port)._getConfigurationContext());
    }
    if (port instanceof DynamicStubImpl) {
      return new IdempotencyManagementInterfaceImpl(((DynamicStubImpl)port)._getConfigurationContext());
    }
    if(port instanceof JAXWSProxy) {
      return new IdempotencyManagementInterfaceImpl(((JAXWSProxy)port)._getConfigurationContext());
    }
    return(null);
  }
}
