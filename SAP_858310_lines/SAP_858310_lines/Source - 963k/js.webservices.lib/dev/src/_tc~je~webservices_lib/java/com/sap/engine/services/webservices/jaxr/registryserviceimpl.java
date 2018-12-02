package com.sap.engine.services.webservices.jaxr;

import com.sap.engine.services.webservices.jaxr.impl.RegistryConnector;
import com.sap.engine.services.webservices.jaxr.impl.class_schemes.ClassificationSchemeLocator;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.ClassificationScheme;
import java.util.Hashtable;

public class RegistryServiceImpl implements RegistryService {
  private Connection[] connections;
  private Hashtable bulkResponses;
  private ClassificationSchemeLocator schemeLocator;

  protected RegistryServiceImpl(Connection[] con) {
    this.connections = con;
    bulkResponses = new Hashtable();

    if (con.length > 0) { // take the first and only connection
      schemeLocator = new ClassificationSchemeLocator(con[0]);
    }
  }
  
  public BulkResponse getBulkResponse(String requestId) throws InvalidRequestException, JAXRException {
    BulkResponse response = (BulkResponse) bulkResponses.remove(requestId);
    if (response == null) {
      throw new InvalidRequestException("No response exists for specified requestId");
    }
    return response;
  }
  
  public BusinessLifeCycleManager getBusinessLifeCycleManager() throws JAXRException {
    return new BusinessLifeCycleManagerImpl(connections, this);
  }
  
  public BusinessQueryManager getBusinessQueryManager() throws JAXRException {
    return new BusinessQueryManagerImpl(connections, this);
  }
  
  public CapabilityProfile getCapabilityProfile() throws JAXRException {
    return new CapabilityProfileImpl();
  }
  
  public DeclarativeQueryManager getDeclarativeQueryManager() throws JAXRException, UnsupportedCapabilityException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getDeclarativeQueryManager)");
  }
  
  public ClassificationScheme getDefaultPostalScheme()  throws JAXRException {
    throw new JAXRException("Not suppored(getDefaultPostalScheme) yet");
  }
  
  public String makeRegistrySpecificRequest(String request) throws JAXRException {
    String result = null;

    if (connections.length > 0) {
      RegistryConnector rc = ((ConnectionImpl) connections[0]).getRegistryConnector();
      result = rc.makeRegistrySpecificRequest(request);
    }

    return result;
  }

  public void storeBulkResponse(BulkResponse bulkresponse) throws JAXRException {
    bulkResponses.put(bulkresponse.getRequestId(), bulkresponse);
  }

  public ClassificationSchemeLocator getSchemeLocator() {
    return schemeLocator;
  }
}