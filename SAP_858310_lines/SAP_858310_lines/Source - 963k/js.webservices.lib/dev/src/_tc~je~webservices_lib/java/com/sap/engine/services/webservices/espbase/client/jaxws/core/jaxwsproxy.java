package com.sap.engine.services.webservices.espbase.client.jaxws.core;

import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;

public interface JAXWSProxy {
  
  /**
   * Returns the client configuration context.
   * @return
   */
  public ClientConfigurationContext _getConfigurationContext();
}
