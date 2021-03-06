﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Wed Oct 18 15:02:39 EEST 2006
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext;

/**
 * Schema complexType Java representation.
 * Represents type {http://www.sap.com/webas/710/ws/webservices-j2ee-engine-ext-descriptor}webservicesExtType
 */
public  class WebservicesExtType implements java.io.Serializable {

  private static final long serialVersionUID = -4840240929903465158L;

  // Element field for element {}webservice-description
  private java.util.ArrayList _f_WebserviceDescription = new java.util.ArrayList();
  /**
   * Set method for element {}webservice-description
   */
  public void setWebserviceDescription(com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType[] _WebserviceDescription) {
    this._f_WebserviceDescription.clear();
    if (_WebserviceDescription != null) {
      for (int i=0; i<_WebserviceDescription.length; i++) {
        if (_WebserviceDescription[i] != null)
          this._f_WebserviceDescription.add(_WebserviceDescription[i]);
      }
    }
  }
  /**
   * Get method for element {}webservice-description
   */
  public com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType[] getWebserviceDescription() {
    com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType[] result = new com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType[_f_WebserviceDescription.size()];
    _f_WebserviceDescription.toArray(result);
    return result;
  }

  /**
   * Equals method implementation.
   */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (!(object instanceof WebservicesExtType)) return false;
    WebservicesExtType typed = (WebservicesExtType) object;
    com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType[] _f_WebserviceDescription1 = this.getWebserviceDescription();
    com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType[] _f_WebserviceDescription2 = typed.getWebserviceDescription();
    if (_f_WebserviceDescription1 != null) {
      if (_f_WebserviceDescription2 == null) return false;
      if (_f_WebserviceDescription1.length != _f_WebserviceDescription2.length) return false;
      for (int i1 = 0; i1 < _f_WebserviceDescription1.length ; i1++) {
        if (_f_WebserviceDescription1[i1] != null) {
          if (_f_WebserviceDescription2[i1] == null) return false;
          if (!_f_WebserviceDescription1[i1].equals(_f_WebserviceDescription2[i1])) return false;
        } else {
          if (_f_WebserviceDescription2[i1] != null) return false;
        }
      }
    } else {
      if (_f_WebserviceDescription2 != null) return false;
    }
    return true;
  }

  /**
   * Hashcode method implementation.
   */
  public int hashCode() {
    int result = 0;
    com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType[] _f_WebserviceDescription1 = this.getWebserviceDescription();
    if (_f_WebserviceDescription1 != null) {
      for (int i1 = 0; i1 < _f_WebserviceDescription1.length ; i1++) {
        if (_f_WebserviceDescription1[i1] != null) {
          result+= _f_WebserviceDescription1[i1].hashCode();
        }
      }
    }
    return result;
  }
}
