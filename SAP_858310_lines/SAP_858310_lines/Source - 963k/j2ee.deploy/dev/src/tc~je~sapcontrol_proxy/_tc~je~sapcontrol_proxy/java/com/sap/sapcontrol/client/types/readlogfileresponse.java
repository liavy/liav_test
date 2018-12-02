﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Mon May 29 10:14:50 EEST 2006
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.sapcontrol.client.types;

/**
 * Schema complexType Java representation.
 * Represents type of namespace {urn:SAPControl} anonymous with xpath [/definitions/types/schema/element[42]/complexType]
 */
public  class ReadLogFileResponse implements java.io.Serializable {

  // Element field for element {}format
  private java.lang.String _f_Format;
  /**
   * Set method for element {}format
   */
  public void setFormat(java.lang.String _Format) {
    this._f_Format = _Format;
  }
  /**
   * Get method for element {}format
   */
  public java.lang.String getFormat() {
    return this._f_Format;
  }

  // Element field for element {}startcookie
  private java.lang.String _f_Startcookie;
  /**
   * Set method for element {}startcookie
   */
  public void setStartcookie(java.lang.String _Startcookie) {
    this._f_Startcookie = _Startcookie;
  }
  /**
   * Get method for element {}startcookie
   */
  public java.lang.String getStartcookie() {
    return this._f_Startcookie;
  }

  // Element field for element {}endcookie
  private java.lang.String _f_Endcookie;
  /**
   * Set method for element {}endcookie
   */
  public void setEndcookie(java.lang.String _Endcookie) {
    this._f_Endcookie = _Endcookie;
  }
  /**
   * Get method for element {}endcookie
   */
  public java.lang.String getEndcookie() {
    return this._f_Endcookie;
  }

  // Element field for element {}fields
  private com.sap.sapcontrol.client.types.ArrayOfString _f_Fields;
  /**
   * Set method for element {}fields
   */
  public void setFields(com.sap.sapcontrol.client.types.ArrayOfString _Fields) {
    this._f_Fields = _Fields;
  }
  /**
   * Get method for element {}fields
   */
  public com.sap.sapcontrol.client.types.ArrayOfString getFields() {
    return this._f_Fields;
  }

  /**
   * Equals method implementation.
   */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (!(object instanceof ReadLogFileResponse)) return false;
    ReadLogFileResponse typed = (ReadLogFileResponse) object;
    if (this._f_Format != null) {
      if (typed._f_Format == null) return false;
      if (!this._f_Format.equals(typed._f_Format)) return false;
    } else {
      if (typed._f_Format != null) return false;
    }
    if (this._f_Startcookie != null) {
      if (typed._f_Startcookie == null) return false;
      if (!this._f_Startcookie.equals(typed._f_Startcookie)) return false;
    } else {
      if (typed._f_Startcookie != null) return false;
    }
    if (this._f_Endcookie != null) {
      if (typed._f_Endcookie == null) return false;
      if (!this._f_Endcookie.equals(typed._f_Endcookie)) return false;
    } else {
      if (typed._f_Endcookie != null) return false;
    }
    if (this._f_Fields != null) {
      if (typed._f_Fields == null) return false;
      if (!this._f_Fields.equals(typed._f_Fields)) return false;
    } else {
      if (typed._f_Fields != null) return false;
    }
    return true;
  }

  /**
   * Hashcode method implementation.
   */
  public int hashCode() {
    int result = 0;
    if (this._f_Format != null) {
      result+= this._f_Format.hashCode();
    }
    if (this._f_Startcookie != null) {
      result+= this._f_Startcookie.hashCode();
    }
    if (this._f_Endcookie != null) {
      result+= this._f_Endcookie.hashCode();
    }
    if (this._f_Fields != null) {
      result+= this._f_Fields.hashCode();
    }
    return result;
  }
}