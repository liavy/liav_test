package com.sap.engine.services.ssl.dispatcher;

import com.sap.engine.frame.cluster.transport.TransportSupplier;
import com.sap.engine.frame.cluster.transport.TransportFactory;
import com.sap.engine.services.ssl.factory.SSLTransportFactory;

public class SSLTransportSupplier implements TransportSupplier {

  public TransportFactory getTransportFactory(TransportFactory underlineFactory) {
    return new SSLTransportFactory(underlineFactory);
  }

}

