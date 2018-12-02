﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Tue Jun 12 14:57:45 EEST 2007
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.engine.services.webservices.espbase.sr.metadata;

/**
 * Schema complexType Java representation.
 * Represents type {http://www.sap.com/webas/2006/11/sr/metadata}ClassificationValuesMetaData
 */
public  class ClassificationValuesMetaData implements java.io.Serializable {

  private static final long serialVersionUID = -7917985032704768603L;

  // Element field for element {http://www.sap.com/webas/2006/11/sr/metadata}ClassificationValue
  private java.util.ArrayList _f_ClassificationValue = new java.util.ArrayList();
  /**
   * Set method for element {http://www.sap.com/webas/2006/11/sr/metadata}ClassificationValue
   */
  public void setClassificationValue(com.sap.engine.services.webservices.espbase.sr.metadata.ClassificationValue[] _ClassificationValue) {
    this._f_ClassificationValue.clear();
    if (_ClassificationValue != null) {
      for (int i=0; i<_ClassificationValue.length; i++) {
        if (_ClassificationValue[i] != null)
          this._f_ClassificationValue.add(_ClassificationValue[i]);
      }
    }
  }
  /**
   * Get method for element {http://www.sap.com/webas/2006/11/sr/metadata}ClassificationValue
   */
  public com.sap.engine.services.webservices.espbase.sr.metadata.ClassificationValue[] getClassificationValue() {
    com.sap.engine.services.webservices.espbase.sr.metadata.ClassificationValue[] result = new com.sap.engine.services.webservices.espbase.sr.metadata.ClassificationValue[_f_ClassificationValue.size()];
    _f_ClassificationValue.toArray(result);
    return result;
  }

  /**
   * Equals method implementation.
   */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (!(object instanceof ClassificationValuesMetaData)) return false;
    ClassificationValuesMetaData typed = (ClassificationValuesMetaData) object;
    com.sap.engine.services.webservices.espbase.sr.metadata.ClassificationValue[] _f_ClassificationValue1 = this.getClassificationValue();
    com.sap.engine.services.webservices.espbase.sr.metadata.ClassificationValue[] _f_ClassificationValue2 = typed.getClassificationValue();
    if (_f_ClassificationValue1 != null) {
      if (_f_ClassificationValue2 == null) return false;
      if (_f_ClassificationValue1.length != _f_ClassificationValue2.length) return false;
      for (int i1 = 0; i1 < _f_ClassificationValue1.length ; i1++) {
        if (_f_ClassificationValue1[i1] != null) {
          if (_f_ClassificationValue2[i1] == null) return false;
          if (!_f_ClassificationValue1[i1].equals(_f_ClassificationValue2[i1])) return false;
        } else {
          if (_f_ClassificationValue2[i1] != null) return false;
        }
      }
    } else {
      if (_f_ClassificationValue2 != null) return false;
    }
    return true;
  }

  /**
   * Hashcode method implementation.
   */
  public int hashCode() {
    int result = 0;
    com.sap.engine.services.webservices.espbase.sr.metadata.ClassificationValue[] _f_ClassificationValue1 = this.getClassificationValue();
    if (_f_ClassificationValue1 != null) {
      for (int i1 = 0; i1 < _f_ClassificationValue1.length ; i1++) {
        if (_f_ClassificationValue1[i1] != null) {
          result+= _f_ClassificationValue1[i1].hashCode();
        }
      }
    }
    return result;
  }
}
