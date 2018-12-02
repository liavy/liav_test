package com.sap.engine.services.webservices.jaxr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.Connection;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;
import javax.xml.registry.UnsupportedCapabilityException;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.Key;

import com.sap.engine.services.webservices.jaxr.impl.RegistryConnector;

/**
 *  This is the inquire manager.
 *
 *  @author Alexander Zubev, alexander.zubev@sap.com
 *  @version Aplril 2002
 */

public class BusinessQueryManagerImpl extends QueryManagerImpl implements BusinessQueryManager {   
  protected BusinessQueryManagerImpl(Connection[] connections, RegistryService registryService) {
    super(connections, registryService);
  }
  
  public BulkResponse findAssociations(Collection findQualifiers, String sourceObjectId, String targetObjectId, Collection associationTypes) throws JAXRException {

    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());
    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];      
      BulkResponse resp = connection.getRegistryConnector().findAssociations(findQualifiers, sourceObjectId, targetObjectId, associationTypes);
      ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
      response.addAnotherBulkResponse(resp);
    }
    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }
  
  public ClassificationScheme findClassificationSchemeByName(String namePattern) throws JAXRException {
    Vector namePatterns = new Vector(1);
    namePatterns.addElement(namePattern);
    BulkResponse response = findClassificationSchemes(null, namePatterns, null, null);
    Collection schemes = response.getCollection();
    if ((schemes == null) || (schemes.size() == 0)) { // try to find predefined classification scheme
      return ((RegistryServiceImpl) getRegistryService()).getSchemeLocator().getClassificationScheme(namePattern);
    } else {
      return (ClassificationScheme) schemes.iterator().next();
    }
  }

  public ClassificationScheme findClassificationSchemeByName(Collection findQualifiers, String namePattern) throws JAXRException {
    Vector namePatterns = new Vector(1);
    namePatterns.addElement(namePattern);
    BulkResponse response = findClassificationSchemes(findQualifiers, namePatterns, null, null);
    Collection schemes = response.getCollection();

    if ((schemes == null) || (schemes.size() == 0)) { // try to find predefined classification scheme
      return ((RegistryServiceImpl) getRegistryService()).getSchemeLocator().getClassificationScheme(namePattern);
    } else if (schemes.size() == 1) {
      return (ClassificationScheme) schemes.iterator().next();
    } else {
      throw new InvalidRequestException("Multiple matches for Classification scheme was found with these search parameters");
    }
  }

  public BulkResponse findClassificationSchemes(Collection findQualifiers, Collection namePatterns, Collection classifications,
           Collection externalLinks) throws JAXRException {
    
    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());
    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];
      Collection namePatternForRegistry = new ArrayList();
      boolean searchForSchemes = false;
      if (findQualifiers == null && classifications == null && externalLinks == null) {
        searchForSchemes = true;
        Iterator itr = namePatterns.iterator();
        while (itr.hasNext()) {
          String cName = (String) itr.next();
          ClassificationScheme scheme = ((RegistryServiceImpl) getRegistryService()).getSchemeLocator().getClassificationScheme(cName);
          if (scheme != null) {
            response.addObject(scheme);
          } else {
            namePatternForRegistry.add(cName);
          }
        }
      } else {
        namePatternForRegistry = namePatterns;
      }
      
      if (!searchForSchemes || (namePatternForRegistry.size() > 0)) {
        BulkResponse resp = connection.getRegistryConnector().findClassificationSchemes(findQualifiers, namePatternForRegistry, classifications, externalLinks);
        ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
        response.addAnotherBulkResponse(resp);
      }
    }
    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }
  
  public Concept findConceptByPath(String path) throws JAXRException {
    return ((RegistryServiceImpl) getRegistryService()).getSchemeLocator().getConceptByPath(path);
  }
  
  public BulkResponse findConcepts(Collection findQualifiers, Collection namePatterns, Collection classifications,
           Collection externalIdentifiers, Collection externalLinks) throws JAXRException {
    
    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());
    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];      
      BulkResponse resp = connection.getRegistryConnector().findConcepts(findQualifiers, namePatterns, classifications, externalIdentifiers, externalLinks);
      ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
      response.addAnotherBulkResponse(resp);
    }
    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }
  
  public BulkResponse findOrganizations(Collection findQualifiers, Collection namePatterns,
           Collection classifications, Collection specifications, Collection externalIdentifiers,
           Collection externalLinks) throws JAXRException {

    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());
    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];
      RegistryConnector specificRegistry = connection.getRegistryConnector();      
      BulkResponse resp = specificRegistry.findOrganizations(findQualifiers, namePatterns, classifications, specifications, externalIdentifiers, externalLinks);
      ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
      response.addAnotherBulkResponse(resp);
    }
    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }
  
  public BulkResponse findRegistryPackages(Collection findQualifiers, Collection namePatterns, Collection classifications,
           Collection externalLinks) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(findRegistryPackages)");
  }
  
  public BulkResponse findServiceBindings(Key serviceKey, Collection findQualifiers, Collection classifications,
           Collection specifications) throws JAXRException {
    
    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());
    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];      
      BulkResponse resp = connection.getRegistryConnector().findServiceBindings(serviceKey, findQualifiers, classifications, specifications);
      ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
      response.addAnotherBulkResponse(resp);
    }
    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }
  
  public BulkResponse findServices(Key orgKey, Collection findQualifiers, Collection namePatterns,
           Collection classifications, Collection specifications) throws JAXRException {

    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());
    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];      
      BulkResponse resp = connection.getRegistryConnector().findServices(orgKey, findQualifiers, namePatterns, classifications, specifications);
      ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
      response.addAnotherBulkResponse(resp);
    }
    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }

  public BulkResponse findCallerAssociations(Collection arg0, Boolean arg1, Boolean arg2, Collection arg3) throws JAXRException {
    // TODO Auto-generated method stub
    return null;
  }
}