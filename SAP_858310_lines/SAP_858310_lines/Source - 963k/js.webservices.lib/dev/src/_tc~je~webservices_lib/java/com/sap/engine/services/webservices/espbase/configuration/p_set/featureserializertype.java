﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Tue Oct 23 15:26:07 EEST 2007
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.engine.services.webservices.espbase.configuration.p_set;

/**
 * Schema complexType Java representation.
 * Represents type {http://xml.sap.com/2006/11/esi/conf/feat/}featureSerializerType
 */
public  class FeatureSerializerType implements java.io.Serializable,com.sap.engine.services.webservices.jaxrpc.encoding.IdenticObject {

  private static final long serialVersionUID = -7505025315499055048L;

  // Attribute field for attribute {}javaClass
  private java.lang.String _a_JavaClass;
  /**
   * Set method for attribute {}javaClass
   */
  public void setJavaClass(java.lang.String _JavaClass) {
    this._a_JavaClass = _JavaClass;
  }
  /**
   * Get method for attribute {}javaClass
   */
  public java.lang.String getJavaClass() {
    return _a_JavaClass;
  }

  /**
   * Equals method implementation.
   */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (!(object instanceof FeatureSerializerType)) return false;
    FeatureSerializerType typed = (FeatureSerializerType) object;
    if (this._a_JavaClass != null) {
      if (typed._a_JavaClass == null) return false;
      if (!this._a_JavaClass.equals(typed._a_JavaClass)) return false;
    } else {
      if (typed._a_JavaClass != null) return false;
    }
    return true;
  }

  /**
   * Hashcode method implementation.
   */
  public int hashCode() {
    int result = 0;
    if (this._a_JavaClass != null) {
      result+= this._a_JavaClass.hashCode();
    }
    return result;
  }

  public java.lang.String get__ID() {
    return java.lang.String.valueOf(super.hashCode());
  }
}
