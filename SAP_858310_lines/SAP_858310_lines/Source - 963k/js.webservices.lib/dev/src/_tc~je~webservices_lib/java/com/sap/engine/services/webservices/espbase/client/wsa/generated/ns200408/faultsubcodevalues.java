﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Mon Oct 02 10:42:28 EEST 2006
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200408;

/**
 * Enumeration Java representation.
 * Represents type {http://schemas.xmlsoap.org/ws/2004/08/addressing}FaultSubcodeValues
 */
public class FaultSubcodeValues implements java.io.Serializable {

  private static final long serialVersionUID = -4772276636449118L;

  public static final javax.xml.namespace.QName _wsaInvalidMessageInformationHeader = new javax.xml.namespace.QName("http://schemas.xmlsoap.org/ws/2004/08/addressing", "InvalidMessageInformationHeader", "wsa");
  public static final javax.xml.namespace.QName _wsaMessageInformationHeaderRequired = new javax.xml.namespace.QName("http://schemas.xmlsoap.org/ws/2004/08/addressing", "MessageInformationHeaderRequired", "wsa");
  public static final javax.xml.namespace.QName _wsaDestinationUnreachable = new javax.xml.namespace.QName("http://schemas.xmlsoap.org/ws/2004/08/addressing", "DestinationUnreachable", "wsa");
  public static final javax.xml.namespace.QName _wsaActionNotSupported = new javax.xml.namespace.QName("http://schemas.xmlsoap.org/ws/2004/08/addressing", "ActionNotSupported", "wsa");
  public static final javax.xml.namespace.QName _wsaEndpointUnavailable = new javax.xml.namespace.QName("http://schemas.xmlsoap.org/ws/2004/08/addressing", "EndpointUnavailable", "wsa");

  public static final FaultSubcodeValues wsaInvalidMessageInformationHeader = new FaultSubcodeValues(_wsaInvalidMessageInformationHeader);
  public static final FaultSubcodeValues wsaMessageInformationHeaderRequired = new FaultSubcodeValues(_wsaMessageInformationHeaderRequired);
  public static final FaultSubcodeValues wsaDestinationUnreachable = new FaultSubcodeValues(_wsaDestinationUnreachable);
  public static final FaultSubcodeValues wsaActionNotSupported = new FaultSubcodeValues(_wsaActionNotSupported);
  public static final FaultSubcodeValues wsaEndpointUnavailable = new FaultSubcodeValues(_wsaEndpointUnavailable);

  //  Enumeration Content
  protected javax.xml.namespace.QName _value;

  public FaultSubcodeValues(javax.xml.namespace.QName _value) {
    if (_wsaInvalidMessageInformationHeader.equals(_value)) {
      this._value = _value;
      return;
    }
    if (_wsaMessageInformationHeaderRequired.equals(_value)) {
      this._value = _value;
      return;
    }
    if (_wsaDestinationUnreachable.equals(_value)) {
      this._value = _value;
      return;
    }
    if (_wsaActionNotSupported.equals(_value)) {
      this._value = _value;
      return;
    }
    if (_wsaEndpointUnavailable.equals(_value)) {
      this._value = _value;
      return;
    }
    throw new IllegalArgumentException("Invalid Enumeration value ["+_value+"] passed.");
  }

  public javax.xml.namespace.QName getValue() {
    return _value;
  }

  public static FaultSubcodeValues fromValue(javax.xml.namespace.QName value) {
    if (_wsaInvalidMessageInformationHeader.equals(value)) {
      return wsaInvalidMessageInformationHeader;
    }
    if (_wsaMessageInformationHeaderRequired.equals(value)) {
      return wsaMessageInformationHeaderRequired;
    }
    if (_wsaDestinationUnreachable.equals(value)) {
      return wsaDestinationUnreachable;
    }
    if (_wsaActionNotSupported.equals(value)) {
      return wsaActionNotSupported;
    }
    if (_wsaEndpointUnavailable.equals(value)) {
      return wsaEndpointUnavailable;
    }
    throw new IllegalArgumentException("Invalid Enumeration value ["+value+"] passed.");
  }

  public static FaultSubcodeValues fromString(String value) {
    if ("wsa:InvalidMessageInformationHeader".equals(value)) {
      return wsaInvalidMessageInformationHeader;
    }
    if ("wsa:MessageInformationHeaderRequired".equals(value)) {
      return wsaMessageInformationHeaderRequired;
    }
    if ("wsa:DestinationUnreachable".equals(value)) {
      return wsaDestinationUnreachable;
    }
    if ("wsa:ActionNotSupported".equals(value)) {
      return wsaActionNotSupported;
    }
    if ("wsa:EndpointUnavailable".equals(value)) {
      return wsaEndpointUnavailable;
    }
    throw new IllegalArgumentException("Invalid Enumeration value ["+value+"] passed.");
  }

  public java.lang.String toString() {
    if (_wsaInvalidMessageInformationHeader.equals(_value)) {
      return "wsa:InvalidMessageInformationHeader";
    }
    if (_wsaMessageInformationHeaderRequired.equals(_value)) {
      return "wsa:MessageInformationHeaderRequired";
    }
    if (_wsaDestinationUnreachable.equals(_value)) {
      return "wsa:DestinationUnreachable";
    }
    if (_wsaActionNotSupported.equals(_value)) {
      return "wsa:ActionNotSupported";
    }
    if (_wsaEndpointUnavailable.equals(_value)) {
      return "wsa:EndpointUnavailable";
    }
    return java.lang.String.valueOf(_value);
  }

  public boolean equals(java.lang.Object obj) {
    if (obj != null) {
      if (obj instanceof FaultSubcodeValues) {
        if (_value.equals(((FaultSubcodeValues)obj)._value)) {
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