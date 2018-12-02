﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Mon May 29 10:14:50 EEST 2006
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.sapcontrol.client.types.soap;

/**
 * Schema complexType Java representation.
 * Represents type {http://schemas.xmlsoap.org/soap/encoding/}Struct
 */
public  class Struct implements java.io.Serializable {

  // Attribute field for attribute {http://schemas.xmlsoap.org/soap/encoding/}id
  private java.lang.String _a_Id;
  /**
   * Set method for attribute {http://schemas.xmlsoap.org/soap/encoding/}id
   */
  public void setId(java.lang.String _Id) {
    this._a_Id = _Id;
  }
  /**
   * Get method for attribute {http://schemas.xmlsoap.org/soap/encoding/}id
   */
  public java.lang.String getId() {
    return _a_Id;
  }

  // Attribute field for attribute {http://schemas.xmlsoap.org/soap/encoding/}href
  private java.net.URI _a_Href;
  /**
   * Set method for attribute {http://schemas.xmlsoap.org/soap/encoding/}href
   */
  public void setHref(java.net.URI _Href) {
    this._a_Href = _Href;
  }
  /**
   * Get method for attribute {http://schemas.xmlsoap.org/soap/encoding/}href
   */
  public java.net.URI getHref() {
    return _a_Href;
  }

  /**
   * Equals method implementation.
   */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (!(object instanceof Struct)) return false;
    Struct typed = (Struct) object;
    if (this._a_Id != null) {
      if (typed._a_Id == null) return false;
      if (!this._a_Id.equals(typed._a_Id)) return false;
    } else {
      if (typed._a_Id != null) return false;
    }
    if (this._a_Href != null) {
      if (typed._a_Href == null) return false;
      if (!this._a_Href.equals(typed._a_Href)) return false;
    } else {
      if (typed._a_Href != null) return false;
    }
    return true;
  }

  /**
   * Hashcode method implementation.
   */
  public int hashCode() {
    int result = 0;
    if (this._a_Id != null) {
      result+= this._a_Id.hashCode();
    }
    if (this._a_Href != null) {
      result+= this._a_Href.hashCode();
    }
    return result;
  }
}