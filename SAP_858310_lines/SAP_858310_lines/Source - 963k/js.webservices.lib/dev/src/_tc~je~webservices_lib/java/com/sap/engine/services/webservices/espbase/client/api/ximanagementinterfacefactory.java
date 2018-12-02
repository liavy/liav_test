package com.sap.engine.services.webservices.espbase.client.api;

import com.sap.engine.services.webservices.espbase.client.api.impl.XIManagementInterfaceImpl;
import com.sap.engine.services.webservices.espbase.client.jaxws.core.JAXWSProxy;

public class XIManagementInterfaceFactory {

  public static final XIManagementInterface create(Object port) {
    if (port instanceof JAXWSProxy) {
      return(new XIManagementInterfaceImpl(port));
    }
    return null;
  }
}
