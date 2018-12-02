﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Mon May 29 10:14:50 EEST 2006
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.sapcontrol.client.types;

/**
 * Schema complexType Java representation.
 * Represents type of namespace {urn:SAPControl} anonymous with xpath [/definitions/types/schema/element[82]/complexType]
 */
public  class J2EEGetClusterMsgListResponse implements java.io.Serializable {

  // Element field for element {}msg
  private com.sap.sapcontrol.client.types.ArrayOfJ2EEClusterMsg _f_Msg;
  /**
   * Set method for element {}msg
   */
  public void setMsg(com.sap.sapcontrol.client.types.ArrayOfJ2EEClusterMsg _Msg) {
    this._f_Msg = _Msg;
  }
  /**
   * Get method for element {}msg
   */
  public com.sap.sapcontrol.client.types.ArrayOfJ2EEClusterMsg getMsg() {
    return this._f_Msg;
  }

  /**
   * Equals method implementation.
   */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (!(object instanceof J2EEGetClusterMsgListResponse)) return false;
    J2EEGetClusterMsgListResponse typed = (J2EEGetClusterMsgListResponse) object;
    if (this._f_Msg != null) {
      if (typed._f_Msg == null) return false;
      if (!this._f_Msg.equals(typed._f_Msg)) return false;
    } else {
      if (typed._f_Msg != null) return false;
    }
    return true;
  }

  /**
   * Hashcode method implementation.
   */
  public int hashCode() {
    int result = 0;
    if (this._f_Msg != null) {
      result+= this._f_Msg.hashCode();
    }
    return result;
  }
}
