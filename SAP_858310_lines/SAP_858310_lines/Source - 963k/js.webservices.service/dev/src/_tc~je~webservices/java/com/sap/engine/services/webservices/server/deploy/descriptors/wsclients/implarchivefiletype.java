﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Wed Feb 20 16:00:14 EET 2008
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.engine.services.webservices.server.deploy.descriptors.wsclients;

/**
 * Schema complexType Java representation.
 * Represents type {http://www.sap.com/webas/710/wsclients/ws-clients-j2ee-engine-descriptor}implArchiveFileType
 */
public  class ImplArchiveFileType implements java.io.Serializable {

  private static final long serialVersionUID = -4907848326903733853L;

  // Attribute field for attribute {}type
  private com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ImplTypeType _a_Type;
  /**
   * Set method for attribute {}type
   */
  public void setType(com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ImplTypeType _Type) {
    this._a_Type = _Type;
  }
  /**
   * Get method for attribute {}type
   */
  public com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ImplTypeType getType() {
    return _a_Type;
  }

  // Simple content field
  private java.lang.String _f__value;
  /**
   * Set method for simple content.
   */
  public void set_value(java.lang.String __value) {
    this._f__value = __value;
  }
  /**
   * Get method for simple content.
   */
  public java.lang.String get_value() {
    return this._f__value;
  }

  /**
   * Equals method implementation.
   */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (!(object instanceof ImplArchiveFileType)) return false;
    ImplArchiveFileType typed = (ImplArchiveFileType) object;
    if (this._a_Type != null) {
      if (typed._a_Type == null) return false;
      if (!this._a_Type.equals(typed._a_Type)) return false;
    } else {
      if (typed._a_Type != null) return false;
    }
    if (this._f__value != null) {
      if (typed._f__value == null) return false;
      if (!this._f__value.equals(typed._f__value)) return false;
    } else {
      if (typed._f__value != null) return false;
    }
    return true;
  }

  /**
   * Hashcode method implementation.
   */
  public int hashCode() {
    int result = 0;
    if (this._a_Type != null) {
      result+= this._a_Type.hashCode();
    }
    if (this._f__value != null) {
      result+= this._f__value.hashCode();
    }
    return result;
  }
}