package com.sap.engine.services.webservices.jaxr.impl;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Key;
import java.util.Collection;

/**
 *  This interfcace is used from the jaxr implementation to call
 *  a method for a specific registry. For now there are two implementations
 *  UDDI1Registry and UDDI2Registry. Later may be added EbXMLRegistry or 
 *  other registries.
 *
 *  @author Alexander Zubev, aleksandar.zubev@sap.com, alexander.zubev@sap.com
 *  @version April 2002
 */

public interface RegistryConnector {
  public void close();
  public String getAuthInfo() throws JAXRException;
  
  //from BusinessQueryManager
  public BulkResponse findAssociations(Collection findQualifiers, String sourceObjectId, String targetObjectId, Collection associationTypes) throws JAXRException;
  public BulkResponse findClassificationSchemes(Collection findQualifiers, Collection namePatterns, Collection classifications,
           Collection externalLinks) throws JAXRException;
  public BulkResponse findConcepts(Collection findQualifiers, Collection namePatterns, Collection classifications,
           Collection externalIdentifiers, Collection externalLinks) throws JAXRException;

  public BulkResponse findOrganizations(Collection findQualifiers, Collection namePatterns,
           Collection classifications, Collection specifications, Collection externalIdentifiers,
           Collection externalLinks) throws JAXRException;
  public BulkResponse findServiceBindings(Key serviceKey, Collection findQualifiers, Collection classifications,
           Collection specifications) throws JAXRException;
  public BulkResponse findServices(Key orgKey, Collection findQualifiers, Collection namePatterns,
           Collection classifications, Collection specifications) throws JAXRException;
           
  //from BusinessLifeCycleManager
  public BulkResponse saveOrganizations(Collection organizations) throws JAXRException;
  public BulkResponse saveServices(Collection services) throws JAXRException;
  public BulkResponse saveServiceBindings(Collection bindings) throws JAXRException;
  public BulkResponse saveConcepts(Collection concepts) throws JAXRException;
  public BulkResponse saveClassificationSchemes(Collection schemes) throws JAXRException;
  public BulkResponse saveAssociations(Collection associations, boolean replace) throws JAXRException;
  public BulkResponse deleteOrganizations(Collection organizationKeys) throws JAXRException;
  public BulkResponse deleteServices(Collection serviceKeys) throws JAXRException;
  public BulkResponse deleteServiceBindings(Collection bindingKeys) throws JAXRException;
  public BulkResponse deleteConcepts(Collection conceptKeys) throws JAXRException;
  public BulkResponse deleteClassificationSchemes(Collection schemeKeys) throws JAXRException;
  public BulkResponse deleteAssociations(Collection associationKeys) throws JAXRException;
  
  public BulkResponse getRegistryObjects() throws JAXRException;

  public BulkResponse getConceptsOrSchemes(String[] keys, boolean schemes) throws JAXRException;
  public BulkResponse getServiceBindings(String[] keys) throws JAXRException;
  public BulkResponse getOrganizations(String[] keys) throws JAXRException;
  public BulkResponse getServices(String[] keys) throws JAXRException;

  public String makeRegistrySpecificRequest(String request) throws JAXRException;
}