package com.sap.engine.services.webservices.espbase.client.api;

import com.sap.engine.services.webservices.espbase.client.api.PerformanceInfoInterface;
import com.sap.engine.services.webservices.espbase.client.api.impl.HTTPControlInterfaceNYImpl;
import com.sap.engine.services.webservices.espbase.client.api.impl.PerformanceInfoInterfaceImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.DynamicStubImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.impl.DInterfaceInvokerImpl;
import com.sap.engine.services.webservices.espbase.client.jaxws.core.JAXWSProxy;
import com.sap.engine.services.webservices.espbase.wsdas.impl.WSDASImpl;

public class PerformanceInfoFactory {

  public static final PerformanceInfoInterface getInterface(Object port) {
    if (port == null) {
      return null;
    }
    if (port instanceof DInterfaceInvokerImpl) {
      return new PerformanceInfoInterfaceImpl(((DInterfaceInvokerImpl)port)._getConfigurationContext());
    }
    if (port instanceof DynamicStubImpl) {
      return new PerformanceInfoInterfaceImpl(((DynamicStubImpl)port)._getConfigurationContext());
    }
    if(port instanceof JAXWSProxy) {
      return new PerformanceInfoInterfaceImpl(((JAXWSProxy)port)._getConfigurationContext());
    }
    if (port instanceof WSDASImpl && ((WSDASImpl) port)._getConfigurationContext()!= null) {
      return new PerformanceInfoInterfaceImpl(((WSDASImpl) port)._getConfigurationContext());
  }

    return(null);
  }
}
