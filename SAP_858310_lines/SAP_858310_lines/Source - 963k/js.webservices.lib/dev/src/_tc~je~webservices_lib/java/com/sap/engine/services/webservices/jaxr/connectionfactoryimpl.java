package com.sap.engine.services.webservices.jaxr;

import java.util.Collection;
import java.util.Properties;

import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.FederatedConnection;
import javax.xml.registry.JAXRException;

public class ConnectionFactoryImpl extends ConnectionFactory {
  public static final String QUERY_MANAGER_URL = "javax.xml.registry.queryManagerURL";
  public static final String LIFE_CYCLE_MANAGER_URL = "javax.xml.registry.lifeCycleManagerURL";
  public static final String MAX_ROWS = "javax.xml.registry.uddi.maxRows";
  public static final String UDDI_VERSION = "com.sap.engine.services.webservices.jaxr.uddiapi.version";
  public static final String PROXY_HOST = "http.proxyHost";
  public static final String PROXY_PORT = "http.proxyPort";
  
  private Properties props;
  
  public Connection createConnection() throws JAXRException {
    return new ConnectionImpl(props);
  }

  public FederatedConnection createFederatedConnection(Collection connections) throws JAXRException {
    return new FederatedConnectionImpl(connections);
  }
  
  public void setProperties(Properties properties) throws JAXRException {
    props = properties;
  }
  
  public Properties getProperties() throws JAXRException {
    return props;
  }
  
}