package com.sap.engine.services.webservices.jaxr;

import java.util.Collection;
import java.util.Vector;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.Connection;
import javax.xml.registry.DeleteException;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;
import javax.xml.registry.SaveException;
import javax.xml.registry.infomodel.Association;

/**
 *  This is the publishing manager.
 *
 *  @author Alexander Zubev, alexander.zubev@sap.com
 */

public class BusinessLifeCycleManagerImpl extends LifeCycleManagerImpl implements BusinessLifeCycleManager {
  public BusinessLifeCycleManagerImpl(Connection[] connections, RegistryService registryService) {
    super(connections, registryService);
  }

  public BulkResponse saveOrganizations(Collection organizations) throws JAXRException {
    if ((organizations == null) || (organizations.size() == 0)) {
      throw new JAXRException("The specified collection of organizations is either null or empty");
    }
    
    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());
    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];      
      BulkResponse resp = connection.getRegistryConnector().saveOrganizations(organizations);
      ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
      response.addAnotherBulkResponse(resp);
    }
    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }
  
  public BulkResponse saveServices(Collection services) throws JAXRException {
    if ((services == null) || (services.size() == 0)) {
      throw new JAXRException("The specified collection of services is either null or empty");
    }
    
    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());
    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];      
      BulkResponse resp = connection.getRegistryConnector().saveServices(services);
      ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
      response.addAnotherBulkResponse(resp);
    }
    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }
  
  public BulkResponse saveServiceBindings(Collection bindings) throws JAXRException {
    if ((bindings == null) || (bindings.size() == 0)) {
      throw new JAXRException("The specified collection of bindings is either null or empty");
    }
    
    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());
    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];      
      BulkResponse resp = connection.getRegistryConnector().saveServiceBindings(bindings);
      ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
      response.addAnotherBulkResponse(resp);
    }
    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }
  
  public BulkResponse saveConcepts(Collection concepts) throws JAXRException {
    if ((concepts == null) || (concepts.size() == 0)) {
      throw new JAXRException("The specified collection of concepts is either null or empty");
    }
    
    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());
    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];      
      BulkResponse resp = connection.getRegistryConnector().saveConcepts(concepts);
      ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
      response.addAnotherBulkResponse(resp);
    }
    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }
  
  public BulkResponse saveClassificationSchemes(Collection schemes) throws JAXRException {
    if ((schemes == null) || (schemes.size() == 0)) {
      throw new JAXRException("The specified collection of schemes is either null or empty");
    }
    
    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());
    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];
      BulkResponse resp = connection.getRegistryConnector().saveClassificationSchemes(schemes);
      ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
      response.addAnotherBulkResponse(resp);
    }
    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }
  
  public BulkResponse saveAssociations(Collection associations, boolean replace) throws JAXRException {
    if ((associations == null) || (associations.size() == 0)) {
      throw new SaveException("The specified collection of associations is either null or empty");
    }
    
    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());
    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];      
      BulkResponse resp = connection.getRegistryConnector().saveAssociations(associations, replace);
      ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
      response.addAnotherBulkResponse(resp);
    }
    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }
  
  public BulkResponse deleteOrganizations(Collection organizationKeys) throws JAXRException {
    if ((organizationKeys == null) || (organizationKeys.size() == 0)) {
      throw new DeleteException("The specified collection of organizationKeys is either null or empty");
    }
    
    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());
    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];      
      BulkResponse resp = connection.getRegistryConnector().deleteOrganizations(organizationKeys);
      ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
      response.addAnotherBulkResponse(resp);
    }
    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }
  
  public BulkResponse deleteServices(Collection serviceKeys) throws JAXRException {
    if ((serviceKeys == null) || (serviceKeys.size() == 0)) {
      throw new DeleteException("The specified collection of serviceKeys is either null or empty");
    }
    
    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());
    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];      
      BulkResponse resp = connection.getRegistryConnector().deleteServices(serviceKeys);
      ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
      response.addAnotherBulkResponse(resp);
    }
    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }
  
  public BulkResponse deleteServiceBindings(Collection bindingKeys) throws JAXRException {
    if ((bindingKeys == null) || (bindingKeys.size() == 0)) {
      throw new DeleteException("The specified collection of bindingKeys is either null or empty");
    }
    
    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());
    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];      
      BulkResponse resp = connection.getRegistryConnector().deleteServiceBindings(bindingKeys);
      ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
      response.addAnotherBulkResponse(resp);
    }
    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }
  
  public BulkResponse deleteConcepts(Collection conceptKeys) throws JAXRException {
    if ((conceptKeys == null) || (conceptKeys.size() == 0)) {
      throw new DeleteException("The specified collection of conceptKeys is either null or empty");
    }
    
    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());
    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];      
      BulkResponse resp = connection.getRegistryConnector().deleteConcepts(conceptKeys);
      ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
      response.addAnotherBulkResponse(resp);
    }
    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }
  
  public BulkResponse deleteClassificationSchemes(Collection schemeKeys) throws JAXRException {
    if ((schemeKeys == null) || (schemeKeys.size() == 0)) {
      throw new DeleteException("The specified collection of schemeKeys is either null or empty");
    }
    
    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());
    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];      
      BulkResponse resp = connection.getRegistryConnector().deleteClassificationSchemes(schemeKeys);
      ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
      response.addAnotherBulkResponse(resp);
    }
    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }
  
  public BulkResponse deleteAssociations(Collection associationKeys) throws JAXRException {
    if ((associationKeys == null) || (associationKeys.size() == 0)) {
      throw new DeleteException("The specified collection of associationKeys is either null or empty");
    }
    
    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());
    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];      
      BulkResponse resp = connection.getRegistryConnector().deleteAssociations(associationKeys);
      ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
      response.addAnotherBulkResponse(resp);
    }
    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }
  
  public void confirmAssociation(Association arg0) throws JAXRException, InvalidRequestException {
    // TODO Auto-generated method stub
  }

  public void unConfirmAssociation(Association arg0) throws JAXRException, InvalidRequestException {
    // TODO Auto-generated method stub
  }
}