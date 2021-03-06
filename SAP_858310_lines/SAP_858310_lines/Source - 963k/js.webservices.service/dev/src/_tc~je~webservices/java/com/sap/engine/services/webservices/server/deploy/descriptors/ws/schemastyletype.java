﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Fri Jun 01 12:58:39 EEST 2007
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.engine.services.webservices.server.deploy.descriptors.ws;

/**
 * Enumeration Java representation.
 * Represents type {http://www.sap.com/webas/710/ws/webservices-j2ee-engine-descriptor}schemaStyleType
 */
public class SchemaStyleType implements java.io.Serializable {

  private static final long serialVersionUID = 7090358645361218228L;

  public static final java.lang.String _literal = "literal";
  public static final java.lang.String _encoded = "encoded";
  public static final java.lang.String _defaultTemp = "default";

  public static final SchemaStyleType literal = new SchemaStyleType(_literal);
  public static final SchemaStyleType encoded = new SchemaStyleType(_encoded);
  public static final SchemaStyleType defaultTemp = new SchemaStyleType(_defaultTemp);

  //  Enumeration Content
  protected java.lang.String _value;

  public SchemaStyleType(java.lang.String _value) {
    if (_literal.equals(_value)) {
      this._value = _value;
      return;
    }
    if (_encoded.equals(_value)) {
      this._value = _value;
      return;
    }
    if (_defaultTemp.equals(_value)) {
      this._value = _value;
      return;
    }
    throw new IllegalArgumentException("Invalid Enumeration value ["+_value+"] passed.");
  }

  public java.lang.String getValue() {
    return _value;
  }

  public static SchemaStyleType fromValue(java.lang.String value) {
    if (_literal.equals(value)) {
      return literal;
    }
    if (_encoded.equals(value)) {
      return encoded;
    }
    if (_defaultTemp.equals(value)) {
      return defaultTemp;
    }
    throw new IllegalArgumentException("Invalid Enumeration value ["+value+"] passed.");
  }

  public static SchemaStyleType fromString(String value) {
    if ("literal".equals(value)) {
      return literal;
    }
    if ("encoded".equals(value)) {
      return encoded;
    }
    if ("default".equals(value)) {
      return defaultTemp;
    }
    throw new IllegalArgumentException("Invalid Enumeration value ["+value+"] passed.");
  }

  public java.lang.String toString() {
    if (_literal.equals(_value)) {
      return "literal";
    }
    if (_encoded.equals(_value)) {
      return "encoded";
    }
    if (_defaultTemp.equals(_value)) {
      return "default";
    }
    return java.lang.String.valueOf(_value);
  }

  public boolean equals(java.lang.Object obj) {
    if (obj != null) {
      if (obj instanceof SchemaStyleType) {
        if (_value.equals(((SchemaStyleType)obj)._value)) {
          return true;
        }
      }
    }
    return false;
  }

  public int hashCode() {
    return this._value.hashCode();
  }

}
