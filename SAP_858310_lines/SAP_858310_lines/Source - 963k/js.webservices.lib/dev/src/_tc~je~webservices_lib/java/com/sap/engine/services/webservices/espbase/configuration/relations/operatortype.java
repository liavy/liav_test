﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Fri May 26 15:19:50 EEST 2006
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.engine.services.webservices.espbase.configuration.relations;

/**
 * Enumeration Java representation.
 * Represents type {http://www.sap.com/webas/710/ws/property-relations}OperatorType
 */
public class OperatorType implements java.io.Serializable {

  public static final java.lang.String _required = "required";
  public static final java.lang.String _forbidden = "forbidden";

  public static final OperatorType required = new OperatorType(_required);
  public static final OperatorType forbidden = new OperatorType(_forbidden);

  //  Enumeration Content
  protected java.lang.String _value;

  public OperatorType(java.lang.String _value) {
    if (_required.equals(_value)) {
      this._value = _value;
      return;
    }
    if (_forbidden.equals(_value)) {
      this._value = _value;
      return;
    }
    throw new IllegalArgumentException("Invalid Enumeration value ["+_value+"] passed.");
  }

  public java.lang.String getValue() {
    return _value;
  }

  public static OperatorType fromValue(java.lang.String value) {
    if (_required.equals(value)) {
      return required;
    }
    if (_forbidden.equals(value)) {
      return forbidden;
    }
    throw new IllegalArgumentException("Invalid Enumeration value ["+value+"] passed.");
  }

  public static OperatorType fromString(String value) {
    if ("required".equals(value)) {
      return required;
    }
    if ("forbidden".equals(value)) {
      return forbidden;
    }
    throw new IllegalArgumentException("Invalid Enumeration value ["+value+"] passed.");
  }

  public java.lang.String toString() {
    if (_required.equals(_value)) {
      return "required";
    }
    if (_forbidden.equals(_value)) {
      return "forbidden";
    }
    return java.lang.String.valueOf(_value);
  }

  public boolean equals(java.lang.Object obj) {
    if (obj != null) {
      if (obj instanceof OperatorType) {
        if (_value.equals(((OperatorType)obj)._value)) {
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
