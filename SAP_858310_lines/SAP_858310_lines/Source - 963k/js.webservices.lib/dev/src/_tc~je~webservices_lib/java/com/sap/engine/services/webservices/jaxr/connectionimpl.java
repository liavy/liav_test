package com.sap.engine.services.webservices.jaxr;

import com.sap.engine.services.webservices.jaxr.impl.RegistryConnector;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.connector.UDDI2Connector;

import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class ConnectionImpl implements Connection {
  private boolean closed = false;
  private Set credentials = new HashSet();
  private boolean synchronous = true;
  private int version;
  private RegistryConnector registry;

//  private String factoryClass;
//  private String authInfo;

  public ConnectionImpl(Properties props) throws JAXRException {
    if (props == null) {
      throw new JAXRException("Properties to the ConnectionFactory have not been set");
    }
    
    //determines uddi_v2 or uddi_v3
    String prop = props.getProperty(ConnectionFactoryImpl.UDDI_VERSION);

    if (prop != null) {
      version = Integer.parseInt(prop);
      if (version != 2 && version != 3) {
        throw new JAXRException("The specified UDDI API Version (" + version + ") is not supported");
      }
    } else {
      version = 2;
    }

    registry = new UDDI2Connector(this, props);
  }

  public RegistryConnector getRegistryConnector() {
    return registry;
  }
  
  public int getVersion() {
    return version;
  }

  public void close() throws JAXRException {
    //to do - some operation for closing http connection...
    registry.close();
    closed = true;
  }
  
  public Set getCredentials() throws JAXRException {
    if (closed) {
      throw new JAXRException("Connection closed");
    }
    return credentials;
  }
  
  public RegistryService getRegistryService() throws JAXRException {
    if (closed) {
      throw new JAXRException("Connection closed");
    }
    return new RegistryServiceImpl(new Connection[] {this});
  }
  
  public boolean isClosed() throws JAXRException {
    return closed;
  }
  
  public boolean isSynchronous() throws JAXRException {
    if (closed) {
      throw new JAXRException("Connection closed");
    }
    return synchronous;
  }
  
  public void setCredentials(Set credentials) throws JAXRException {
    if (closed) {
      throw new JAXRException("Connection closed");
    }
    if (credentials == null) { 
      throw new JAXRException("The specified credential set is NULL");
    }
    this.credentials = credentials;
    
    registry.getAuthInfo();
  }
  
  public void setSynchronous(boolean sync) throws JAXRException {
    if (closed) {
      throw new JAXRException("Connection closed");
    }
    synchronous = sync;
  }
}