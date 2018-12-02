package com.sap.engine.services.webservices.jaxr;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.xml.registry.Connection;
import javax.xml.registry.FederatedConnection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;

public class FederatedConnectionImpl extends ConnectionImpl implements FederatedConnection {
  private Connection[] connections;
  private Set credentials;
  
  public FederatedConnectionImpl(Collection connections) throws JAXRException {
    super(new Properties());
    if (connections == null) {
      throw new JAXRException("The specified collection of connections is NULL");
    }
    if (connections.size() <= 0) {
      throw new JAXRException("There are no connections in this collection");
    }
    this.connections = new Connection[connections.size()];
    Iterator it = connections.iterator();
    int i = 0;
    while (it.hasNext()) {
      Connection con = (Connection) it.next();
      this.connections[i++] = con;
    }
  }
  
  public RegistryService getRegistryService() {
    return new RegistryServiceImpl(connections);
  }
  
  public void close() throws JAXRException {
    for (int i=0; i < connections.length; i++) {
      connections[i].close();
    }
  }

  public boolean isClosed() throws JAXRException {
    for (int i=0; i < connections.length; i++) {
      if (!connections[i].isClosed()) {
        return false;
      }
    }
    return true;
  }
  
  public boolean isSynchronous() throws JAXRException {
    for (int i=0; i < connections.length; i++) {
      if (connections[i].isSynchronous() == false) {
        return false;
      }
    }
    return true;
  }
  
  public void setSynchronous(boolean sync) throws JAXRException {
    for (int i=0; i < connections.length; i++) {
      connections[i].setSynchronous(sync);
    }
  }
  
  public void setCredentials(Set credentials) throws JAXRException {
    for (int i=0; i < connections.length; i++) {
      connections[i].setCredentials(credentials);
    }
    this.credentials = credentials;
  }
  
  public Set getCredentials() throws JAXRException {
    return credentials;
  }
}