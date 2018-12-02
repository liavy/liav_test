package com.sap.engine.services.webservices.jaxr;

import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.connector.UDDI2Connector;
import com.sap.engine.services.webservices.jaxr.impl.RegistryConnector;
import com.sap.engine.services.webservices.jaxr.infomodel.ExternalIdentifierImpl;

import java.util.Collection;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

public class QueryManagerImpl implements QueryManager {
  protected Connection[] connections;
  protected RegistryService registryService;
  
  public QueryManagerImpl(Connection[] connections, RegistryService registryService) {
    this.connections = connections;
    this.registryService = registryService;
  }
  
  public RegistryObject getRegistryObject(String id)  throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getRegistryObject)");
  }
  
  public RegistryObject getRegistryObject(String id, String objectType) throws JAXRException {
    RegistryObject result = null;

    for (int i = 0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];
      RegistryConnector rc = connection.getRegistryConnector();

      BulkResponse response = null;
     
      if (LifeCycleManager.CONCEPT.equals(objectType)) {
        response = rc.getConceptsOrSchemes(new String[] { id }, false);
      } else if (LifeCycleManager.CLASSIFICATION_SCHEME.equals(objectType)) {
        response = rc.getConceptsOrSchemes(new String[] { id }, true);
      } else if (LifeCycleManager.SERVICE_BINDING.equals(objectType)) {
        response = rc.getServiceBindings(new String[] { id });
      } else if (LifeCycleManager.ORGANIZATION.equals(objectType)) {
        response = rc.getOrganizations(new String[] { id });
      } else if (LifeCycleManager.SERVICE.equals(objectType)) {
        response = rc.getServices(new String[] { id });
      }

      if (response != null) {
        Collection objects = response.getCollection();

        if (objects.size() != 1) {
          throw new JAXRException("For the key (" + id + "): Don't exist a registry object or exist more than one");
        }

        Iterator it = objects.iterator();
        if (it.hasNext()) {
          Object obj = it.next();

          if (obj instanceof RegistryObject) {
            result = (RegistryObject) obj;
          }
        }
      }
    }

    return result;
  }

  public BulkResponse getRegistryObjects()  throws JAXRException {
    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());
    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];      
      BulkResponse resp = connection.getRegistryConnector().getRegistryObjects();
      ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
      response.addAnotherBulkResponse(resp);
    }
    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }
  
  public BulkResponse getRegistryObjects(Collection objectKeys)  throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getRegistryObjects)");
  }
  
  public BulkResponse getRegistryObjects(String objectType) throws JAXRException {
    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());

    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];
      RegistryConnector rc = connection.getRegistryConnector();

      BulkResponse resp = null;

      Vector namePattern = new Vector(1);
      namePattern.add("%");

      if (LifeCycleManager.CONCEPT.equals(objectType)) {
        resp = rc.findConcepts(null, namePattern, null, null, null);
      } else if (LifeCycleManager.CLASSIFICATION_SCHEME.equals(objectType)) {
        resp = rc.findClassificationSchemes(null, namePattern, null, null);
      } else if (LifeCycleManager.ORGANIZATION.equals(objectType)) {
        resp = rc.findOrganizations(null, namePattern, null, null, null, null);
      } else if (LifeCycleManager.SERVICE.equals(objectType)) {
        resp = rc.findServices(null, null, namePattern, null, null);
      }

      if (resp != null) {
        ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
        response.addAnotherBulkResponse(resp);
      }
    }

    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }
  
  public RegistryService getRegistryService() {
    return registryService;
  }

  public BulkResponse getRegistryObjects(Collection objectKeys, String objectTypes) throws JAXRException {
    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());
    Iterator itor = objectKeys.iterator();

    while (itor.hasNext()) {
      String id = ((Key) itor.next()).getId();
      RegistryObject regObj = null;

      try {
        regObj = getRegistryObject(id, objectTypes);
      } catch (Exception ex) {
        response.addException(ex);
      }

      if (regObj != null) {
        response.addObject(regObj);
      }
    }
    return response;
  }
}