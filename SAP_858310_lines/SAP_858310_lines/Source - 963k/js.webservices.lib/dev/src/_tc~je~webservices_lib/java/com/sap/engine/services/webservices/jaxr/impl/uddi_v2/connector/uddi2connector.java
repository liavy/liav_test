/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.jaxr.impl.uddi_v2.connector;

import com.sap.engine.services.webservices.jaxr.*;
import com.sap.engine.services.webservices.jaxr.impl.RegistryConnector;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.*;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.*;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.BaseGeneratedStub;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.Key;
import javax.xml.rpc.Stub;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * @author Alexander Zubev (alexander.zubev@sap.com)
 */
public class UDDI2Connector implements RegistryConnector {
  static final String GENERIC = "2.0";
  
  private String queryManagerURL;
  private String lifeCycleManagerURL;
  private int maxRows;
  private UDDI2Service service;
  private String proxyHost;
  private int proxyPort;
  private Connection con;
  private String authInfo;
  private Set credentials;
  
  public UDDI2Connector(ConnectionImpl con, Properties props) throws JAXRException {
    this.con = con;

    try {
      service = new UDDI2ServiceImpl();
    } catch (Exception ex) {
      throw new JAXRException(ex);
    }

    queryManagerURL = props.getProperty(ConnectionFactoryImpl.QUERY_MANAGER_URL);
    lifeCycleManagerURL = props.getProperty(ConnectionFactoryImpl.LIFE_CYCLE_MANAGER_URL);

    if (queryManagerURL == null || lifeCycleManagerURL == null) {
      throw new InvalidRequestException("Query Manager URL or LifeCycle Manager URL are missing");
    }

    String max = props.getProperty(ConnectionFactoryImpl.MAX_ROWS, "-1");

    try {
      maxRows = Integer.parseInt(max);
    } catch (NumberFormatException nfe) {
      throw new JAXRException("javax.xml.registry.uddi.maxRows must be set to an Integer");
    }

    proxyHost = props.getProperty(ConnectionFactoryImpl.PROXY_HOST);

    if (proxyHost != null) {
      String portStr = props.getProperty(ConnectionFactoryImpl.PROXY_PORT);

      if (portStr == null) {
        throw new JAXRException("Proxy Host is set, but the proxy port is not");
      }

      try {
        proxyPort = Integer.parseInt(portStr);
      } catch (NumberFormatException nfe) {
        throw new JAXRException("http.proxyPort msut be set to an Integer");
      }
    }
  }
  
  private void setProxyToStub(Stub stub) {
    if (proxyHost != null) {
      stub._setProperty(BaseGeneratedStub.HTTPPROXYHOST, proxyHost);
      stub._setProperty(BaseGeneratedStub.HTTPPROXYPORT, "" + proxyPort);
    }
  }
  
  private Inquire getInquireAPI() throws JAXRException {
    if (queryManagerURL == null) {
      throw new JAXRException("QueryManager URL has not been set");
    }
    try {
      Inquire inquire = (Inquire) service.getLogicalPort("Inquire", Inquire.class);
      inquire._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, queryManagerURL);
      setProxyToStub(inquire);
      return inquire;
    } catch (Throwable thr) {
      throw new JAXRNestedException("Cannot get Inquire LP", thr);
    }
  }
  
  private Publish getPublishAPI() throws JAXRException {
    try {
      if (lifeCycleManagerURL == null || lifeCycleManagerURL.length() == 0) {
        throw new JAXRException("LifeCycleManagerURL not set");
      }
      Publish publish = (Publish) service.getLogicalPort("Publish", Publish.class);
      publish._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, lifeCycleManagerURL);
      setProxyToStub(publish);
      return publish;
    } catch (JAXRException jaxrExc) {
      throw jaxrExc;
    } catch (Throwable thr) {
      throw new JAXRNestedException("Cannot get Publish LP", thr);
    }
  }

  public void close() {
    //do nothing, connection is always closed
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.jaxr.impl.RegistryConnector#findAssociations(java.util.Collection, java.lang.String, java.lang.String, java.util.Collection, java.lang.Boolean, java.lang.Boolean)
   */
  public BulkResponse findAssociations(Collection findQualifiers, String sourceObjectId, String targetObjectId, Collection associationTypes) throws JAXRException {
    Inquire inquire = getInquireAPI();
    FindRelatedBusinesses req = new FindRelatedBusinesses();
    String searchKey;
    if (sourceObjectId != null) {
      searchKey = sourceObjectId;
    } else if (targetObjectId != null) {
      searchKey = targetObjectId;
    } else {
      throw new JAXRException("Either source or target ID must be set");
    }
    req.setBusinessKey(sourceObjectId);
    req.setFindQualifiers(Converter.uddiFindQualifiers(findQualifiers));
    req.setGeneric(GENERIC);
    if (maxRows > -1) {
      req.setMaxRows(maxRows);
    }
    BulkResponseImpl response = new BulkResponseImpl();
    try {
      RelatedBusinessesList list = inquire.findRelatedBusinesses(req);
      response.setPartialResponse("true".equalsIgnoreCase(list.getTruncated()));
      Converter converter = new Converter(inquire, response, con);
      converter.addJaxrAssociations(list, sourceObjectId, targetObjectId);
    } catch (RemoteException re) {
      throw new JAXRNestedException(re);
    } catch (DispositionReport dre) {
      response.addException(new FindNestedException(dre));
    }
    return response;
  }

  public BulkResponse findClassificationSchemes(Collection findQualifiers, Collection namePatterns, Collection classifications, Collection externalLinks) throws JAXRException {
    return findTModels(findQualifiers, namePatterns, classifications, null, externalLinks, true);
  }

  public BulkResponse findConcepts(Collection findQualifiers, Collection namePatterns, Collection classifications, Collection externalIdentifiers, Collection externalLinks) throws JAXRException {
    return findTModels(findQualifiers, namePatterns, classifications, externalIdentifiers, externalLinks, false);
  }

  private BulkResponse findTModels(Collection findQualifiers, Collection namePatterns, Collection classifications, Collection externalIdentifiers, Collection externalLinks, boolean schemes) throws JAXRException {
    Inquire inquire = getInquireAPI();
    FindTModel req = new FindTModel();
    KeyedReference[] categories = Converter.uddiCategoryBag(classifications);
    if (schemes) {
      KeyedReference[] newCategories;
      if (categories == null) {
        newCategories = new KeyedReference[1];
      } else {
        newCategories = new KeyedReference[categories.length + 1];
        System.arraycopy(categories, 0, newCategories, 0, categories.length);
      }
      
      KeyedReference ref = new KeyedReference();
      ref.setTModelKey("uuid:c1acf26d-9672-4404-9d70-39b756e62ab4");
      ref.setKeyName("uddi-org:types:categorization");
      ref.setKeyValue("categorization");
      
      newCategories[newCategories.length - 1] = ref;
      
      categories = newCategories;
    }
    req.setCategoryBag(categories);
    req.setFindQualifiers(Converter.uddiFindQualifiers(findQualifiers));
    req.setGeneric(GENERIC);
    req.setIdentifierBag(Converter.uddiIdentifiersBag(externalIdentifiers));
    if (maxRows > -1) {
      req.setMaxRows(maxRows);
    }
    Name[] names = Converter.uddiNames(namePatterns);
    if (names != null && names.length > 0) {
      req.setName(names[0]);
    }
    BulkResponseImpl response = new BulkResponseImpl();
    try {
      TModelList list = inquire.findTModel(req);
      response.setPartialResponse("true".equalsIgnoreCase(list.getTruncated()));
      Converter converter = new Converter(inquire, response, con);
      converter.addJaxrConceptsOrSchemes(list.getTModelInfos(), schemes);
    } catch (RemoteException re) {
      throw new JAXRNestedException(re);
    } catch (DispositionReport dre) {
      response.addException(new FindNestedException(dre));
    }
    return response;
  }

  public BulkResponse findOrganizations(Collection findQualifiers, Collection namePatterns, Collection classifications, Collection specifications, Collection externalIdentifiers, Collection externalLinks) throws JAXRException {
    Inquire inquire = getInquireAPI();
    FindBusiness req = new FindBusiness();
    req.setFindQualifiers(Converter.uddiFindQualifiers(findQualifiers));
    req.setName(Converter.uddiNames(namePatterns));
    req.setCategoryBag(Converter.uddiCategoryBag(classifications));    
    req.setTModelBag(Converter.uddiTModelBag(specifications));
    req.setIdentifierBag(Converter.uddiIdentifiersBag(externalIdentifiers));
    req.setDiscoveryURLs(Converter.uddiDiscoveryURLs(externalLinks));
    if (maxRows > -1) {
      req.setMaxRows(maxRows);
    }
    req.setGeneric(GENERIC);
    BulkResponseImpl response = new BulkResponseImpl();
    try {
      BusinessList list = inquire.findBusiness(req);
      response.setPartialResponse("true".equalsIgnoreCase(list.getTruncated()));
      Converter converter = new Converter(inquire, response, con);
      converter.addJaxrOrganizations(list.getBusinessInfos());
    } catch (RemoteException re) {
      throw new JAXRNestedException(re);
    } catch (DispositionReport dre) {
      response.addException(new FindNestedException(dre));
    }
    return response;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.jaxr.impl.RegistryConnector#findServiceBindings(javax.xml.registry.infomodel.Key, java.util.Collection, java.util.Collection, java.util.Collection)
   */
  public BulkResponse findServiceBindings(Key serviceKey, Collection findQualifiers, Collection classifications, Collection specifications) throws JAXRException {
    Inquire inquire = getInquireAPI();
    FindBinding req = new FindBinding();
    req.setFindQualifiers(Converter.uddiFindQualifiers(findQualifiers));
    req.setGeneric(GENERIC);
    if (maxRows > -1) {
      req.setMaxRows(maxRows);
    }
    if (serviceKey != null) {
      req.setServiceKey(serviceKey.getId());
    }
    req.setTModelBag(Converter.uddiTModelBag(specifications));
    BulkResponseImpl response = new BulkResponseImpl();
    response.setPartialResponse(response.isPartialResponse());
    try {
      BindingDetail detail = inquire.findBinding(req);
      response.setPartialResponse("true".equalsIgnoreCase(detail.getTruncated()));
      Converter converter = new Converter(inquire, response, con);
      converter.addJaxrServiceBindings(detail.getBindingTemplate());
    } catch (RemoteException re) {
      throw new JAXRNestedException(re);
    } catch (DispositionReport dre) {
      response.addException(new FindNestedException(dre));
    }
    return response;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.jaxr.impl.RegistryConnector#findServices(javax.xml.registry.infomodel.Key, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection)
   */
  public BulkResponse findServices(Key orgKey, Collection findQualifiers, Collection namePatterns, Collection classifications, Collection specifications) throws JAXRException {
    Inquire inquire = getInquireAPI();
    FindService req = new FindService();
    if (orgKey != null) {
      req.setBusinessKey(orgKey.getId());
    }
    req.setCategoryBag(Converter.uddiCategoryBag(classifications));
    req.setFindQualifiers(Converter.uddiFindQualifiers(findQualifiers));
    req.setGeneric(GENERIC);
    if (maxRows > -1) {
      req.setMaxRows(maxRows);
    }
    req.setName(Converter.uddiNames(namePatterns));
    req.setTModelBag(Converter.uddiTModelBag(specifications));
    BulkResponseImpl response = new BulkResponseImpl();
    try {
      ServiceList list = inquire.findService(req);
      response.setPartialResponse("true".equalsIgnoreCase(list.getTruncated()));
      Converter converter = new Converter(inquire, response, con);
      converter.addJaxrServices(list.getServiceInfos());
    } catch (RemoteException re) {
      throw new JAXRNestedException(re);
    } catch (DispositionReport dre) {
      response.addException(new FindNestedException(dre));
    }
    return response;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.jaxr.impl.RegistryConnector#saveOrganizations(java.util.Collection)
   */
  public BulkResponse saveOrganizations(Collection organizations) throws JAXRException {
    Publish publish = getPublishAPI();
    SaveBusiness req = new SaveBusiness();
    req.setAuthInfo(getAuthInfo());
    req.setBusinessEntity(Converter.uddiBusinessEntities(organizations));
    req.setGeneric(GENERIC);
    //req.setUploadRegister() - UploadRegister is still found in schema, but it is not used
    BulkResponseImpl response = new BulkResponseImpl();
    try {
      BusinessDetail detail = publish.saveBusiness(req);
      response.setPartialResponse("true".equalsIgnoreCase(detail.getTruncated()));
      response.addObjects(Converter.entitiesToKeys(detail.getBusinessEntity()));
    } catch (RemoteException re) {
      throw new JAXRException(re);
    } catch (DispositionReport dre) {
      response.addException(new SaveNestedException(dre));
    }
    return response;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.jaxr.impl.RegistryConnector#saveServices(java.util.Collection)
   */
  public BulkResponse saveServices(Collection services) throws JAXRException {
    Publish publish = getPublishAPI();
    SaveService req = new SaveService();
    req.setAuthInfo(getAuthInfo());
    req.setBusinessService(Converter.uddiBusinessServices(services));
    req.setGeneric(GENERIC);
    BulkResponseImpl response = new BulkResponseImpl();
    try {
      ServiceDetail detail = publish.saveService(req);
      response.setPartialResponse("true".equalsIgnoreCase(detail.getTruncated()));
      response.addObjects(Converter.servicesToKeys(detail.getBusinessService(), services));
    } catch (RemoteException re) {
      throw new JAXRException(re);
    } catch (DispositionReport dre) {
      response.addException(new SaveNestedException(dre));
    }
    return response;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.jaxr.impl.RegistryConnector#saveServiceBindings(java.util.Collection)
   */
  public BulkResponse saveServiceBindings(Collection bindings) throws JAXRException {
    Publish publish = getPublishAPI();
    SaveBinding req = new SaveBinding();
    req.setAuthInfo(getAuthInfo());
    req.setBindingTemplate(Converter.uddiBindingTemplates(bindings));
    req.setGeneric(GENERIC);
    BulkResponseImpl response = new BulkResponseImpl();
    try {
      BindingDetail detail = publish.saveBinding(req);
      response.setPartialResponse("true".equalsIgnoreCase(detail.getTruncated()));
      response.addObjects(Converter.templatesToKeys(detail.getBindingTemplate()));
    } catch (RemoteException re) {
      throw new JAXRException(re);
    } catch (DispositionReport dre) {
      response.addException(new SaveNestedException(dre));
    }
    return response;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.jaxr.impl.RegistryConnector#saveConcepts(java.util.Collection)
   */
  public BulkResponse saveConcepts(Collection concepts) throws JAXRException {
    return saveTModels(concepts, false);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.jaxr.impl.RegistryConnector#saveClassificationSchemes(java.util.Collection)
   */
  public BulkResponse saveClassificationSchemes(Collection schemes) throws JAXRException {
    return saveTModels(schemes, true);
  }
  
  private BulkResponse saveTModels(Collection conceptsOrSchemes, boolean schemes) throws JAXRException {
    Publish publish = getPublishAPI();
    SaveTModel req = new SaveTModel();
    req.setAuthInfo(getAuthInfo());
    req.setGeneric(GENERIC);
    req.setTModel(Converter.uddiTModels(conceptsOrSchemes));

    if (schemes) {
      Converter.applyTModelSchemeRefs(req.getTModel());
    }

    BulkResponseImpl response = new BulkResponseImpl();

    try {
      TModelDetail detail = publish.saveTModel(req);
      response.setPartialResponse("true".equalsIgnoreCase(detail.getTruncated()));
      response.addObjects(Converter.tModelsToKeys(detail.getTModel()));
    } catch (RemoteException re) {
      throw new JAXRException(re);
    } catch (DispositionReport dre) {
      response.addException(new SaveNestedException(dre));
    }
    return response;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.jaxr.impl.RegistryConnector#saveAssociations(java.util.Collection, boolean)
   */
  public BulkResponse saveAssociations(Collection associations, boolean replace) throws JAXRException {
    Publish publish = getPublishAPI();
    SetPublisherAssertions req = new SetPublisherAssertions();
    req.setAuthInfo(getAuthInfo());
    req.setPublisherAssertion(Converter.uddiPublisherAssertions(associations));
    req.setGeneric(GENERIC);
    BulkResponseImpl response = new BulkResponseImpl();
    try {
      PublisherAssertions assertions = publish.setPublisherAssertions(req);
      response.addObjects(Converter.jaxrAssociations(assertions.getPublisherAssertion()));
    } catch (RemoteException re) {
      throw new JAXRException(re);
    } catch (DispositionReport dre) {
      response.addException(new SaveNestedException(dre));
    }
    return response;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.jaxr.impl.RegistryConnector#deleteOrganizations(java.util.Collection)
   */
  public BulkResponse deleteOrganizations(Collection organizationKeys) throws JAXRException {
    Publish publish = getPublishAPI();
    DeleteBusiness req = new DeleteBusiness();
    req.setAuthInfo(getAuthInfo());
    req.setBusinessKey(Converter.keysToStrings(organizationKeys));
    req.setGeneric(GENERIC);
    BulkResponseImpl response = new BulkResponseImpl();
    try {
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport report = publish.deleteBusiness(req);
      response.setPartialResponse("true".equalsIgnoreCase(report.getTruncated()));
      response.addObjects(organizationKeys);
    } catch (RemoteException re) {
      throw new JAXRException(re);
    } catch (DispositionReport dre) {
      response.addException(new DeleteNestedException(dre));
      response.setStatus(JAXRResponse.STATUS_FAILURE);
    }
    return response;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.jaxr.impl.RegistryConnector#deleteServices(java.util.Collection)
   */
  public BulkResponse deleteServices(Collection serviceKeys) throws JAXRException {
    Publish publish = getPublishAPI();
    DeleteService req = new DeleteService();
    req.setAuthInfo(getAuthInfo());
    req.setGeneric(GENERIC);
    req.setServiceKey(Converter.keysToStrings(serviceKeys));
    BulkResponseImpl response = new BulkResponseImpl();
    try {
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport report = publish.deleteService(req);
      response.setPartialResponse("true".equalsIgnoreCase(report.getTruncated()));
      response.addObjects(serviceKeys);
    } catch (RemoteException re) {
      throw new JAXRException(re);
    } catch (DispositionReport dre) {
      response.addException(new DeleteNestedException(dre));
      response.setStatus(JAXRResponse.STATUS_FAILURE);
    }
    return response;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.jaxr.impl.RegistryConnector#deleteServiceBindings(java.util.Collection)
   */
  public BulkResponse deleteServiceBindings(Collection bindingKeys) throws JAXRException {
    Publish publish = getPublishAPI();
    DeleteBinding req = new DeleteBinding();
    req.setAuthInfo(getAuthInfo());
    req.setBindingKey(Converter.keysToStrings(bindingKeys));
    req.setGeneric(GENERIC);
    BulkResponseImpl response = new BulkResponseImpl();
    try {
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport report = publish.deleteBinding(req);
      response.setPartialResponse("true".equalsIgnoreCase(report.getTruncated()));
      response.addObjects(bindingKeys);
    } catch (RemoteException re) {
      throw new JAXRException(re);
    } catch (DispositionReport dre) {
      response.addException(new DeleteNestedException(dre));
      response.setStatus(JAXRResponse.STATUS_FAILURE);
    }
    return response;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.jaxr.impl.RegistryConnector#deleteConcepts(java.util.Collection)
   */
  public BulkResponse deleteConcepts(Collection conceptKeys) throws JAXRException {
    return deleteTModels(conceptKeys);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.jaxr.impl.RegistryConnector#deleteClassificationSchemes(java.util.Collection)
   */
  public BulkResponse deleteClassificationSchemes(Collection schemeKeys) throws JAXRException {
    return deleteTModels(schemeKeys);
  }
  
  private BulkResponse deleteTModels(Collection keys) throws JAXRException {
    Publish publish = getPublishAPI();
    DeleteTModel req = new DeleteTModel();
    req.setAuthInfo(getAuthInfo());
    req.setGeneric(GENERIC);
    req.setTModelKey(Converter.keysToStrings(keys));
    BulkResponseImpl response = new BulkResponseImpl();
    try {
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport report = publish.deleteTModel(req);
      response.setPartialResponse("true".equalsIgnoreCase(report.getTruncated()));
      response.addObjects(keys);
    } catch (RemoteException re) {
      throw new JAXRException(re);
    } catch (DispositionReport dre) {
      response.addException(new DeleteNestedException(dre));
      response.setStatus(JAXRResponse.STATUS_FAILURE);
    }
    return response;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.jaxr.impl.RegistryConnector#deleteAssociations(java.util.Collection)
   */
  public BulkResponse deleteAssociations(Collection associationKeys) throws JAXRException {
    return null;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.jaxr.impl.RegistryConnector#getAuthInfo()
   */
  public String getAuthInfo() throws JAXRException {
    Set credentials = con.getCredentials();
    if (authInfo != null && credentials.equals(this.credentials)) {
      return authInfo;
    }
    Publish publish = getPublishAPI();
    GetAuthToken req = new GetAuthToken();
    req.setGeneric(GENERIC);
    Iterator it = credentials.iterator();
    if (it.hasNext()) {
      PasswordAuthentication passwrdAuth = (PasswordAuthentication) it.next();
      req.setUserID(passwrdAuth.getUserName());
      req.setCred(new String(passwrdAuth.getPassword()));
    } else {
      throw new JAXRException("Credentials not set");
    }
    try {
      AuthToken token = publish.getAuthToken(req);
      authInfo = token.getAuthInfo();
      this.credentials = credentials;
      return authInfo;
    } catch (RemoteException re) {
      throw new JAXRNestedException(re);
    } catch (DispositionReport dre) {
      throw new RegistryNestedException(dre);
    }
  }
  
  public BulkResponse getRegistryObjects() throws JAXRException {
    Publish publish = getPublishAPI();
    Inquire inquire = getInquireAPI();
    GetRegisteredInfo req = new GetRegisteredInfo();
    req.setAuthInfo(getAuthInfo());
    req.setGeneric(GENERIC);
    BulkResponseImpl response = new BulkResponseImpl();
    try {
      RegisteredInfo info = publish.getRegisteredInfo(req);
      response.setPartialResponse("true".equalsIgnoreCase(info.getTruncated()));
      Converter converter = new Converter(inquire, response, con);
      converter.addJaxrOrganizations(info.getBusinessInfos());
      converter.addJaxrConceptsOrSchemes(info.getTModelInfos(), false);
      converter.addJaxrConceptsOrSchemes(info.getTModelInfos(), true);
    } catch (RemoteException re) {
      throw new JAXRException(re);
    } catch (DispositionReport dre) {
      response.addException(new DeleteNestedException(dre));
      response.setStatus(JAXRResponse.STATUS_FAILURE);
    }
    return response;
  }

  public BulkResponse getConceptsOrSchemes(String[] keys, boolean schemes) throws JAXRException {
    Inquire inquire = getInquireAPI();

    BulkResponseImpl response = new BulkResponseImpl();

    try {
      GetTModelDetail req = new GetTModelDetail();
      req.setGeneric(UDDI2Connector.GENERIC);
      req.setTModelKey(keys);

      TModelDetail tModel = inquire.getTModelDetail(req);
      response.setPartialResponse("true".equalsIgnoreCase(tModel.getTruncated()));

      Converter converter = new Converter(inquire, response, con);
      converter.addJaxrConceptsOrSchemes(tModel.getTModel(), schemes);
    } catch (RemoteException re) {
      throw new JAXRNestedException(re);
    } catch (DispositionReport dre) {
      response.addException(new FindNestedException(dre));
      response.setStatus(JAXRResponse.STATUS_FAILURE);
    }

    return response;
  }

  public BulkResponse getServiceBindings(String[] keys) throws JAXRException {
    Inquire inquire = getInquireAPI();

    BulkResponseImpl response = new BulkResponseImpl();

    try {
      GetBindingDetail req = new GetBindingDetail();

      req.setGeneric(UDDI2Connector.GENERIC);
      req.setBindingKey(keys);

      BindingDetail binding = inquire.getBindingDetail(req);
      response.setPartialResponse("true".equalsIgnoreCase(binding.getTruncated()));

      Converter converter = new Converter(inquire, response, con);
      converter.addJaxrServiceBindings(binding.getBindingTemplate());
    } catch (RemoteException re) {
      throw new JAXRNestedException(re);
    } catch (DispositionReport dre) {
      response.addException(new FindNestedException(dre));
      response.setStatus(JAXRResponse.STATUS_FAILURE);
    }

    return response;
  }

  public BulkResponse getOrganizations(String[] keys) throws JAXRException {
    Inquire inquire = getInquireAPI();

    BulkResponseImpl response = new BulkResponseImpl();

    try {
      GetBusinessDetailExt req = new GetBusinessDetailExt();

      req.setGeneric(UDDI2Connector.GENERIC);
      req.setBusinessKey(keys);

      BusinessDetailExt business = inquire.getBusinessDetailExt(req);
      response.setPartialResponse("true".equalsIgnoreCase(business.getTruncated()));

      Converter converter = new Converter(inquire, response, con);
      converter.addJaxrOrganizations(business.getBusinessEntityExt());
    } catch (RemoteException re) {
      throw new JAXRNestedException(re);
    } catch (DispositionReport dre) {
      response.addException(new FindNestedException(dre));
      response.setStatus(JAXRResponse.STATUS_FAILURE);
    }

    return response;
  }

  public BulkResponse getServices(String[] keys) throws JAXRException {
    Inquire inquire = getInquireAPI();

    BulkResponseImpl response = new BulkResponseImpl();

    try {
      GetServiceDetail req = new GetServiceDetail();

      req.setGeneric(UDDI2Connector.GENERIC);
      req.setServiceKey(keys);

      ServiceDetail service = inquire.getServiceDetail(req);
      response.setPartialResponse("true".equalsIgnoreCase(service.getTruncated()));

      Converter converter = new Converter(inquire, response, con);
      converter.addJaxrServices(service.getBusinessService());
    } catch (RemoteException re) {
      throw new JAXRNestedException(re);
    } catch (DispositionReport dre) {
      response.addException(new FindNestedException(dre));
      response.setStatus(JAXRResponse.STATUS_FAILURE);
    }

    return response;
  }

  public String makeRegistrySpecificRequest(String request) throws JAXRException {
    String result = null;

    try {
      URL url = new URL(queryManagerURL);
      URLConnection conn = url.openConnection();

      conn.setRequestProperty("Content-Length", String.valueOf(request.length()));
      conn.setRequestProperty("Content-Type", "text/xml; charset=\"UTF-8\"");
      conn.setRequestProperty("Connection", "close");
      conn.setRequestProperty("Accept", "text/xml");
      conn.setRequestProperty("SOAPAction", "");

      conn.setDoOutput(true);

      OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");

      wr.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
      wr.write("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Body>");
      wr.write(request.replaceAll("xml:lang=\"en\"", ""));
      wr.write("</SOAP-ENV:Body></SOAP-ENV:Envelope>");
      wr.flush();

      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream())); //$JL-I18N$

      String line = null;
      StringBuffer buff = new StringBuffer();

      while ((line = rd.readLine()) != null) {
        buff.append(line);
      }

      result = buff.toString();

      wr.close();
      rd.close();
    } catch (Exception e) {
      throw new JAXRNestedException(e);
    }

    return result;
  }
}
