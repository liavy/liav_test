﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Fri Jun 01 12:58:39 EEST 2007
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.engine.services.webservices.server.deploy.descriptors.ws;

/**
 * Enumeration Java representation.
 * Represents type {http://www.sap.com/webas/710/ws/webservices-j2ee-engine-descriptor}schemaTypeType
 */
public class SchemaTypeType implements java.io.Serializable {

  private static final long serialVersionUID = 8916504258583988139L;

  public static final java.lang.String _config = "config";
  public static final java.lang.String _framework = "framework";

  public static final SchemaTypeType config = new SchemaTypeType(_config);
  public static final SchemaTypeType framework = new SchemaTypeType(_framework);

  //  Enumeration Content
  protected java.lang.String _value;

  public SchemaTypeType(java.lang.String _value) {
    if (_config.equals(_value)) {
      this._value = _value;
      return;
    }
    if (_framework.equals(_value)) {
      this._value = _value;
      return;
    }
    throw new IllegalArgumentException("Invalid Enumeration value ["+_value+"] passed.");
  }

  public java.lang.String getValue() {
    return _value;
  }

  public static SchemaTypeType fromValue(java.lang.String value) {
    if (_config.equals(value)) {
      return config;
    }
    if (_framework.equals(value)) {
      return framework;
    }
    throw new IllegalArgumentException("Invalid Enumeration value ["+value+"] passed.");
  }

  public static SchemaTypeType fromString(String value) {
    if ("config".equals(value)) {
      return config;
    }
    if ("framework".equals(value)) {
      return framework;
    }
    throw new IllegalArgumentException("Invalid Enumeration value ["+value+"] passed.");
  }

  public java.lang.String toString() {
    if (_config.equals(_value)) {
      return "config";
    }
    if (_framework.equals(_value)) {
      return "framework";
    }
    return java.lang.String.valueOf(_value);
  }

  public boolean equals(java.lang.Object obj) {
    if (obj != null) {
      if (obj instanceof SchemaTypeType) {
        if (_value.equals(((SchemaTypeType)obj)._value)) {
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
