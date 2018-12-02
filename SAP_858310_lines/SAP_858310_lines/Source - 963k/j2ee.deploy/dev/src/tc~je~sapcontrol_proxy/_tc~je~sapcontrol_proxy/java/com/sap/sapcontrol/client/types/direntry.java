﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Mon May 29 10:14:50 EEST 2006
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.sapcontrol.client.types;

/**
 * Schema complexType Java representation.
 * Represents type {urn:SAPControl}DirEntry
 */
public  class DirEntry implements java.io.Serializable {

  // Element field for element {}filename
  private java.lang.String _f_Filename;
  /**
   * Set method for element {}filename
   */
  public void setFilename(java.lang.String _Filename) {
    this._f_Filename = _Filename;
  }
  /**
   * Get method for element {}filename
   */
  public java.lang.String getFilename() {
    return this._f_Filename;
  }

  // Element field for element {}size
  private long _f_Size;
  /**
   * Set method for element {}size
   */
  public void setSize(long _Size) {
    this._f_Size = _Size;
  }
  /**
   * Get method for element {}size
   */
  public long getSize() {
    return this._f_Size;
  }

  // Element field for element {}modtime
  private java.lang.String _f_Modtime;
  /**
   * Set method for element {}modtime
   */
  public void setModtime(java.lang.String _Modtime) {
    this._f_Modtime = _Modtime;
  }
  /**
   * Get method for element {}modtime
   */
  public java.lang.String getModtime() {
    return this._f_Modtime;
  }

  /**
   * Equals method implementation.
   */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (!(object instanceof DirEntry)) return false;
    DirEntry typed = (DirEntry) object;
    if (this._f_Filename != null) {
      if (typed._f_Filename == null) return false;
      if (!this._f_Filename.equals(typed._f_Filename)) return false;
    } else {
      if (typed._f_Filename != null) return false;
    }
    if (this._f_Size != typed._f_Size) return false;
    if (this._f_Modtime != null) {
      if (typed._f_Modtime == null) return false;
      if (!this._f_Modtime.equals(typed._f_Modtime)) return false;
    } else {
      if (typed._f_Modtime != null) return false;
    }
    return true;
  }

  /**
   * Hashcode method implementation.
   */
  public int hashCode() {
    int result = 0;
    if (this._f_Filename != null) {
      result+= this._f_Filename.hashCode();
    }
    result+= (int) this._f_Size;
    if (this._f_Modtime != null) {
      result+= this._f_Modtime.hashCode();
    }
    return result;
  }
}
