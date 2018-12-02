﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Mon May 29 10:14:50 EEST 2006
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.sapcontrol.client.types;

/**
 * Enumeration Java representation.
 * Represents type {urn:SAPControl}J2EE-PSTATE
 */
public class J2EEPSTATE implements java.io.Serializable {

  public static final java.lang.String _SAPControlJ2EESTOPPED = "SAPControl-J2EE-STOPPED";
  public static final java.lang.String _SAPControlJ2EESTARTING = "SAPControl-J2EE-STARTING";
  public static final java.lang.String _SAPControlJ2EECORERUNNING = "SAPControl-J2EE-CORE-RUNNING";
  public static final java.lang.String _SAPControlJ2EERUNNING = "SAPControl-J2EE-RUNNING";
  public static final java.lang.String _SAPControlJ2EESTOPPING = "SAPControl-J2EE-STOPPING";
  public static final java.lang.String _SAPControlJ2EEMAINTENANCE = "SAPControl-J2EE-MAINTENANCE";
  public static final java.lang.String _SAPControlJ2EEUNKNOWN = "SAPControl-J2EE-UNKNOWN";

  public static final J2EEPSTATE SAPControlJ2EESTOPPED = new J2EEPSTATE(_SAPControlJ2EESTOPPED);
  public static final J2EEPSTATE SAPControlJ2EESTARTING = new J2EEPSTATE(_SAPControlJ2EESTARTING);
  public static final J2EEPSTATE SAPControlJ2EECORERUNNING = new J2EEPSTATE(_SAPControlJ2EECORERUNNING);
  public static final J2EEPSTATE SAPControlJ2EERUNNING = new J2EEPSTATE(_SAPControlJ2EERUNNING);
  public static final J2EEPSTATE SAPControlJ2EESTOPPING = new J2EEPSTATE(_SAPControlJ2EESTOPPING);
  public static final J2EEPSTATE SAPControlJ2EEMAINTENANCE = new J2EEPSTATE(_SAPControlJ2EEMAINTENANCE);
  public static final J2EEPSTATE SAPControlJ2EEUNKNOWN = new J2EEPSTATE(_SAPControlJ2EEUNKNOWN);

  //  Enumeration Content
  protected java.lang.String _value;

  public J2EEPSTATE(java.lang.String _value) {
    if (_SAPControlJ2EESTOPPED.equals(_value)) {
      this._value = _value;
      return;
    }
    if (_SAPControlJ2EESTARTING.equals(_value)) {
      this._value = _value;
      return;
    }
    if (_SAPControlJ2EECORERUNNING.equals(_value)) {
      this._value = _value;
      return;
    }
    if (_SAPControlJ2EERUNNING.equals(_value)) {
      this._value = _value;
      return;
    }
    if (_SAPControlJ2EESTOPPING.equals(_value)) {
      this._value = _value;
      return;
    }
    if (_SAPControlJ2EEMAINTENANCE.equals(_value)) {
      this._value = _value;
      return;
    }
    if (_SAPControlJ2EEUNKNOWN.equals(_value)) {
      this._value = _value;
      return;
    }
    throw new IllegalArgumentException("Invalid Enumeration value ["+_value+"] passed.");
  }

  public java.lang.String getValue() {
    return _value;
  }

  public static J2EEPSTATE fromValue(java.lang.String value) {
    if (_SAPControlJ2EESTOPPED.equals(value)) {
      return SAPControlJ2EESTOPPED;
    }
    if (_SAPControlJ2EESTARTING.equals(value)) {
      return SAPControlJ2EESTARTING;
    }
    if (_SAPControlJ2EECORERUNNING.equals(value)) {
      return SAPControlJ2EECORERUNNING;
    }
    if (_SAPControlJ2EERUNNING.equals(value)) {
      return SAPControlJ2EERUNNING;
    }
    if (_SAPControlJ2EESTOPPING.equals(value)) {
      return SAPControlJ2EESTOPPING;
    }
    if (_SAPControlJ2EEMAINTENANCE.equals(value)) {
      return SAPControlJ2EEMAINTENANCE;
    }
    if (_SAPControlJ2EEUNKNOWN.equals(value)) {
      return SAPControlJ2EEUNKNOWN;
    }
    throw new IllegalArgumentException("Invalid Enumeration value ["+value+"] passed.");
  }

  public static J2EEPSTATE fromString(String value) {
    if ("SAPControl-J2EE-STOPPED".equals(value)) {
      return SAPControlJ2EESTOPPED;
    }
    if ("SAPControl-J2EE-STARTING".equals(value)) {
      return SAPControlJ2EESTARTING;
    }
    if ("SAPControl-J2EE-CORE-RUNNING".equals(value)) {
      return SAPControlJ2EECORERUNNING;
    }
    if ("SAPControl-J2EE-RUNNING".equals(value)) {
      return SAPControlJ2EERUNNING;
    }
    if ("SAPControl-J2EE-STOPPING".equals(value)) {
      return SAPControlJ2EESTOPPING;
    }
    if ("SAPControl-J2EE-MAINTENANCE".equals(value)) {
      return SAPControlJ2EEMAINTENANCE;
    }
    if ("SAPControl-J2EE-UNKNOWN".equals(value)) {
      return SAPControlJ2EEUNKNOWN;
    }
    throw new IllegalArgumentException("Invalid Enumeration value ["+value+"] passed.");
  }

  public java.lang.String toString() {
    if (_SAPControlJ2EESTOPPED.equals(_value)) {
      return "SAPControl-J2EE-STOPPED";
    }
    if (_SAPControlJ2EESTARTING.equals(_value)) {
      return "SAPControl-J2EE-STARTING";
    }
    if (_SAPControlJ2EECORERUNNING.equals(_value)) {
      return "SAPControl-J2EE-CORE-RUNNING";
    }
    if (_SAPControlJ2EERUNNING.equals(_value)) {
      return "SAPControl-J2EE-RUNNING";
    }
    if (_SAPControlJ2EESTOPPING.equals(_value)) {
      return "SAPControl-J2EE-STOPPING";
    }
    if (_SAPControlJ2EEMAINTENANCE.equals(_value)) {
      return "SAPControl-J2EE-MAINTENANCE";
    }
    if (_SAPControlJ2EEUNKNOWN.equals(_value)) {
      return "SAPControl-J2EE-UNKNOWN";
    }
    return java.lang.String.valueOf(_value);
  }

  public boolean equals(java.lang.Object obj) {
    if (obj != null) {
      if (obj instanceof J2EEPSTATE) {
        if (_value.equals(((J2EEPSTATE)obj)._value)) {
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
