﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Wed Nov 10 18:12:45 EET 2004
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.engine.services.webservices.espbase.mappings;

import static com.sap.engine.services.webservices.espbase.mappings.ParameterMapping.JAVA_DOC;

import java.util.ArrayList;

import javax.xml.namespace.QName;

/**
 * Class responsible for Service mapping from WSDL to Java.
 * Schema complexType Java representation.
 * Represents type {http://sapframework.sap.com}ServiceMapping
 */
public  class ServiceMapping extends com.sap.engine.services.webservices.espbase.mappings.MappingContext implements java.io.Serializable,java.lang.Cloneable {
  
  public static final String SI_NAME = "SIName";
  public static final String SERVICE_NAME = "ServiceQName";
  public static final String SERVICE_MAPPING_ID = "ServiceMappingId";
  public static final String JAVA_DOC = "JavaDoc";
  
  /**
   * Returns javaDoc for the interface.
   * @return
   */
  public String getJavaDoc() {
    return super.getProperty(JAVA_DOC);
  }
  
  /**
   * Sets javaDoc for the interface.
   * @param javaDoc
   */
  public void setJavaDoc(String javaDoc) {
    super.setProperty(JAVA_DOC, javaDoc);
  }  

  // Element field for element {}endpoint
  private ArrayList _f_Endpoint = new ArrayList();
  //private com.sap.engine.services.webservices.espbase.mappings.EndpointMapping[] _f_Endpoint = new com.sap.engine.services.webservices.espbase.mappings.EndpointMapping[0];
  /**
   * Set method for element {}endpoint
   */
  public void setEndpoint(com.sap.engine.services.webservices.espbase.mappings.EndpointMapping[] _Endpoint) {
    _f_Endpoint.clear();
    if (_Endpoint != null) {
      for (int i=0; i<_Endpoint.length; i++) {
        if (_Endpoint[i] != null) {
          _f_Endpoint.add(_Endpoint[i]);    
        }
      }
    }
  }
  /**
   * Get method for element {}endpoint
   */
  public com.sap.engine.services.webservices.espbase.mappings.EndpointMapping[] getEndpoint() {
    EndpointMapping[] result = new EndpointMapping[_f_Endpoint.size()];
    _f_Endpoint.toArray(result);
    return result;        
  }
  
  /**
   * Adds endpoint mapping.
   * @param epMapping
   */
  public void addEndpoint(EndpointMapping _eMapping) {
    if (_eMapping != null) {
      _f_Endpoint.add(_eMapping);
    }
  }
  
  /**
   * Sets Service Interface name for WSDL Service.
   * @param SIName
   */
  public void setSIName(String SIName) {
    super.setProperty(SI_NAME,SIName);   
  }
  
  /**
   * Seturns service interface name that is mapped to WSDL Service.
   * @return
   */
  public String getSIName() {
    return super.getProperty(SI_NAME);
  }
  
  
  // Element field for element {}implementation-link
  private ImplementationLink _f_ImplementationLink;
  /**
   * Set method for element {}implementation-link
   */  
  public void setImplementationLink(ImplementationLink _ImplementationLink) {
    this._f_ImplementationLink = _ImplementationLink;
  }
  /**
   * Get method for element {}implementation-link
   */  
  public ImplementationLink getImplementationLink() {
    return this._f_ImplementationLink;
  }  
  
  /**
   * Returns WSDL Service QName.
   * @param serviceName
   */
  public void setServiceName(QName serviceName) {
    super.setProperty(SERVICE_NAME,serviceName.toString());
  }
  
  /**
   * Returns the WSDL Service QName.
   * @return
   */
  public QName getServiceName() {
    if (super.getProperty(SERVICE_NAME) == null) {
      return null;
    }
    return QName.valueOf(super.getProperty(SERVICE_NAME));
  }
  
  /**
   * Returns endpoint mapping with passed port name.
   * @param portName
   * @return
   */
  public EndpointMapping getPortName(QName portName) {
    if (portName == null) return null;
    for (int i=0; i<_f_Endpoint.size(); i++) {
      EndpointMapping ep = (EndpointMapping) _f_Endpoint.get(i);
      if (portName.equals(ep.getPortQName())) {
        return ep;        
      }
    }
    return null;
  }
  
  /**
   * Returns Unique Service Mapping ID.
   * @return
   */
  public String getServiceMappingId() {
    return (String) super.getProperty(SERVICE_MAPPING_ID);  
  }
  
  /**
   * Sets service mapping ID.
   * @param serviceId
   */
  public void setServiceMappingId(String serviceId) {
    super.setProperty(SERVICE_MAPPING_ID,serviceId);
  }
  
}
