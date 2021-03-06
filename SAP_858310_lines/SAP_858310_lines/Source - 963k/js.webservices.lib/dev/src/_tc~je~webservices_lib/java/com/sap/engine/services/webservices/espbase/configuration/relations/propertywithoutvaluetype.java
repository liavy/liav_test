﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Fri May 26 15:19:50 EEST 2006
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.engine.services.webservices.espbase.configuration.relations;

/**
 * Schema complexType Java representation.
 * Represents type {http://www.sap.com/webas/710/ws/property-relations}PropertyWithoutValueType
 */
public  class PropertyWithoutValueType implements java.io.Serializable {

  // Element field for element {}namespace
  private java.lang.String _f_Namespace;
  /**
   * Set method for element {}namespace
   */
  public void setNamespace(java.lang.String _Namespace) {
    this._f_Namespace = _Namespace;
  }
  /**
   * Get method for element {}namespace
   */
  public java.lang.String getNamespace() {
    return this._f_Namespace;
  }

  // Element field for element {}name
  private java.lang.String _f_Name;
  /**
   * Set method for element {}name
   */
  public void setName(java.lang.String _Name) {
    this._f_Name = _Name;
  }
  /**
   * Get method for element {}name
   */
  public java.lang.String getName() {
    return this._f_Name;
  }

  /**
   * Equals method implementation.
   */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (!(object instanceof PropertyWithoutValueType)) return false;
    PropertyWithoutValueType typed = (PropertyWithoutValueType) object;
    if (this._f_Namespace != null) {
      if (typed._f_Namespace == null) return false;
      if (!this._f_Namespace.equals(typed._f_Namespace)) return false;
    } else {
      if (typed._f_Namespace != null) return false;
    }
    if (this._f_Name != null) {
      if (typed._f_Name == null) return false;
      if (!this._f_Name.equals(typed._f_Name)) return false;
    } else {
      if (typed._f_Name != null) return false;
    }
    return true;
  }

  /**
   * Hashcode method implementation.
   */
  public int hashCode() {
    int result = 0;
    if (this._f_Namespace != null) {
      result+= this._f_Namespace.hashCode();
    }
    if (this._f_Name != null) {
      result+= this._f_Name.hashCode();
    }
    return result;
  }
}
