﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Tue Jun 12 14:57:36 EEST 2007
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.engine.services.webservices.espbase.sr.classifications;

/**
 * Schema complexType Java representation.
 * Represents type {http://www.sap.com/webas/2006/11/sr/classifications}GroupClassificationType
 */
public  class GroupClassificationType implements java.io.Serializable {

  private static final long serialVersionUID = -6381377763346868861L;

  // Element field for element {http://www.sap.com/webas/2006/11/sr/classifications}qname
  private javax.xml.namespace.QName _f_Qname;
  /**
   * Set method for element {http://www.sap.com/webas/2006/11/sr/classifications}qname
   */
  public void setQname(javax.xml.namespace.QName _Qname) {
    this._f_Qname = _Qname;
  }
  /**
   * Get method for element {http://www.sap.com/webas/2006/11/sr/classifications}qname
   */
  public javax.xml.namespace.QName getQname() {
    return this._f_Qname;
  }

  // Element field for element {http://www.sap.com/webas/2006/11/sr/classifications}Classification
  private java.util.ArrayList _f_Classification = new java.util.ArrayList();
  /**
   * Set method for element {http://www.sap.com/webas/2006/11/sr/classifications}Classification
   */
  public void setClassification(com.sap.engine.services.webservices.espbase.sr.classifications.ClassificationType[] _Classification) {
    this._f_Classification.clear();
    if (_Classification != null) {
      for (int i=0; i<_Classification.length; i++) {
        if (_Classification[i] != null)
          this._f_Classification.add(_Classification[i]);
      }
    }
  }
  /**
   * Get method for element {http://www.sap.com/webas/2006/11/sr/classifications}Classification
   */
  public com.sap.engine.services.webservices.espbase.sr.classifications.ClassificationType[] getClassification() {
    com.sap.engine.services.webservices.espbase.sr.classifications.ClassificationType[] result = new com.sap.engine.services.webservices.espbase.sr.classifications.ClassificationType[_f_Classification.size()];
    _f_Classification.toArray(result);
    return result;
  }

  /**
   * Equals method implementation.
   */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (!(object instanceof GroupClassificationType)) return false;
    GroupClassificationType typed = (GroupClassificationType) object;
    if (this._f_Qname != null) {
      if (typed._f_Qname == null) return false;
      if (!this._f_Qname.equals(typed._f_Qname)) return false;
    } else {
      if (typed._f_Qname != null) return false;
    }
    com.sap.engine.services.webservices.espbase.sr.classifications.ClassificationType[] _f_Classification1 = this.getClassification();
    com.sap.engine.services.webservices.espbase.sr.classifications.ClassificationType[] _f_Classification2 = typed.getClassification();
    if (_f_Classification1 != null) {
      if (_f_Classification2 == null) return false;
      if (_f_Classification1.length != _f_Classification2.length) return false;
      for (int i1 = 0; i1 < _f_Classification1.length ; i1++) {
        if (_f_Classification1[i1] != null) {
          if (_f_Classification2[i1] == null) return false;
          if (!_f_Classification1[i1].equals(_f_Classification2[i1])) return false;
        } else {
          if (_f_Classification2[i1] != null) return false;
        }
      }
    } else {
      if (_f_Classification2 != null) return false;
    }
    return true;
  }

  /**
   * Hashcode method implementation.
   */
  public int hashCode() {
    int result = 0;
    if (this._f_Qname != null) {
      result+= this._f_Qname.hashCode();
    }
    com.sap.engine.services.webservices.espbase.sr.classifications.ClassificationType[] _f_Classification1 = this.getClassification();
    if (_f_Classification1 != null) {
      for (int i1 = 0; i1 < _f_Classification1.length ; i1++) {
        if (_f_Classification1[i1] != null) {
          result+= _f_Classification1[i1].hashCode();
        }
      }
    }
    return result;
  }
}
