package com.sap.engine.services.webservices.jaxr;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.Connection;
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.RegistryService;
import javax.xml.registry.UnsupportedCapabilityException;

public class DeclarativeQueryManagerImpl extends QueryManagerImpl implements DeclarativeQueryManager {
  public DeclarativeQueryManagerImpl(Connection[] connections, RegistryService registryService) {
    super(connections, registryService);
  }
  
  public Query createQuery(int queryType, java.lang.String queryString) throws InvalidRequestException, JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(createQuery)");
  }
  
  public BulkResponse executeQuery(Query query) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(executeQuery)");
  }
}