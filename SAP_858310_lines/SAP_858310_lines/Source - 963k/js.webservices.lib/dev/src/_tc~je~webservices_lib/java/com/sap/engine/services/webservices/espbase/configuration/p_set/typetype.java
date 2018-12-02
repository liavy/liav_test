﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Thu Nov 30 13:49:03 EET 2006
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.engine.services.webservices.espbase.configuration.p_set;

/**
 * Enumeration Java representation.
 * Represents type {http://xml.sap.com/2006/11/esi/conf/feat/}typeType
 */
public class TypeType implements java.io.Serializable {

  private static final long serialVersionUID = -6446858411028082057L;

  public static final java.lang.String _ENUM = "ENUM";
  public static final java.lang.String _INTEGER = "INTEGER";
  public static final java.lang.String _STRING = "STRING";
  public static final java.lang.String _booleanTemp = "Boolean";

  public static final TypeType ENUM = new TypeType(_ENUM);
  public static final TypeType INTEGER = new TypeType(_INTEGER);
  public static final TypeType STRING = new TypeType(_STRING);
  public static final TypeType booleanTemp = new TypeType(_booleanTemp);

  //  Enumeration Content
  protected java.lang.String _value;

  public TypeType(java.lang.String _value) {
    if (_ENUM.equals(_value)) {
      this._value = _value;
      return;
    }
    if (_INTEGER.equals(_value)) {
      this._value = _value;
      return;
    }
    if (_STRING.equals(_value)) {
      this._value = _value;
      return;
    }
    if (_booleanTemp.equals(_value)) {
      this._value = _value;
      return;
    }
    throw new IllegalArgumentException("Invalid Enumeration value ["+_value+"] passed.");
  }

  public java.lang.String getValue() {
    return _value;
  }

  public static TypeType fromValue(java.lang.String value) {
    if (_ENUM.equals(value)) {
      return ENUM;
    }
    if (_INTEGER.equals(value)) {
      return INTEGER;
    }
    if (_STRING.equals(value)) {
      return STRING;
    }
    if (_booleanTemp.equals(value)) {
      return booleanTemp;
    }
    throw new IllegalArgumentException("Invalid Enumeration value ["+value+"] passed.");
  }

  public static TypeType fromString(String value) {
    if ("ENUM".equals(value)) {
      return ENUM;
    }
    if ("INTEGER".equals(value)) {
      return INTEGER;
    }
    if ("STRING".equals(value)) {
      return STRING;
    }
    if ("Boolean".equals(value)) {
      return booleanTemp;
    }
    throw new IllegalArgumentException("Invalid Enumeration value ["+value+"] passed.");
  }

  public java.lang.String toString() {
    if (_ENUM.equals(_value)) {
      return "ENUM";
    }
    if (_INTEGER.equals(_value)) {
      return "INTEGER";
    }
    if (_STRING.equals(_value)) {
      return "STRING";
    }
    if (_booleanTemp.equals(_value)) {
      return "Boolean";
    }
    return java.lang.String.valueOf(_value);
  }

  public boolean equals(java.lang.Object obj) {
    if (obj != null) {
      if (obj instanceof TypeType) {
        if (_value.equals(((TypeType)obj)._value)) {
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
