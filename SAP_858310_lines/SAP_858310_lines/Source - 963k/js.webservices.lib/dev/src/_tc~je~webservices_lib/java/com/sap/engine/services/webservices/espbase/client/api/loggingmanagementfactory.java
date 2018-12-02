package com.sap.engine.services.webservices.espbase.client.api;

import com.sap.engine.services.webservices.espbase.client.api.impl.LoggingManagementInterfaceImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.DynamicStubImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.impl.DInterfaceInvokerImpl;
import com.sap.engine.services.webservices.espbase.client.jaxws.core.JAXWSProxy;
import com.sap.engine.services.webservices.espbase.wsdas.impl.WSDASImpl;

public class LoggingManagementFactory {

  public static final LoggingManagementInterface getInterface(Object port) {
    if (port == null) {
      return null;
    }

    if (port instanceof DInterfaceInvokerImpl) {
      return new LoggingManagementInterfaceImpl(((DInterfaceInvokerImpl)port)._getConfigurationContext());
    }

    if (port instanceof DynamicStubImpl) {
      return new LoggingManagementInterfaceImpl(((DynamicStubImpl)port)._getConfigurationContext());
    }
    
    if(port instanceof JAXWSProxy) {
      return new LoggingManagementInterfaceImpl(((JAXWSProxy)port)._getConfigurationContext());
    }
    
    if (port instanceof WSDASImpl && ((WSDASImpl) port)._getConfigurationContext()!= null) {
      return new LoggingManagementInterfaceImpl(((WSDASImpl) port)._getConfigurationContext());
    }

    return null;
  }
  
}
