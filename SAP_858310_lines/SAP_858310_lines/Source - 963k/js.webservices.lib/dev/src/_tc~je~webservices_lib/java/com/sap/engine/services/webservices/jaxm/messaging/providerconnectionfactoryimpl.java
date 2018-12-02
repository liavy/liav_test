package com.sap.engine.services.webservices.jaxm.messaging;

import javax.xml.messaging.ProviderConnection;
import javax.xml.messaging.ProviderConnectionFactory;
import java.io.Serializable;

public class ProviderConnectionFactoryImpl extends ProviderConnectionFactory implements Serializable {

  public ProviderConnectionFactoryImpl() {
  }

  public ProviderConnection createConnection() {
    return new ProviderConnectionImpl();
  }

}
