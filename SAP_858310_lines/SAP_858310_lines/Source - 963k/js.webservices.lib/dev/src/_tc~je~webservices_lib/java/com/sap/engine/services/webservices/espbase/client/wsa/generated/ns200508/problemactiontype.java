﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Mon Oct 02 10:43:09 EEST 2006
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200508;

/**
 * Schema complexType Java representation.
 * Represents type {http://www.w3.org/2005/08/addressing}ProblemActionType
 */
public  class ProblemActionType implements java.io.Serializable {

  private static final long serialVersionUID = 8324244889321417314L;

  // Element field for element {http://www.w3.org/2005/08/addressing}Action
  private com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200508.AttributedURIType _f_Action;
  /**
   * Set method for element {http://www.w3.org/2005/08/addressing}Action
   */
  public void setAction(com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200508.AttributedURIType _Action) {
    this._f_Action = _Action;
  }
  /**
   * Get method for element {http://www.w3.org/2005/08/addressing}Action
   */
  public com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200508.AttributedURIType getAction() {
    return this._f_Action;
  }

  // Element field for element {http://www.w3.org/2005/08/addressing}SoapAction
  private java.net.URI _f_SoapAction;
  /**
   * Set method for element {http://www.w3.org/2005/08/addressing}SoapAction
   */
  public void setSoapAction(java.net.URI _SoapAction) {
    this._f_SoapAction = _SoapAction;
  }
  /**
   * Get method for element {http://www.w3.org/2005/08/addressing}SoapAction
   */
  public java.net.URI getSoapAction() {
    return this._f_SoapAction;
  }

  /**
   * Equals method implementation.
   */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (!(object instanceof ProblemActionType)) return false;
    ProblemActionType typed = (ProblemActionType) object;
    if (this._f_Action != null) {
      if (typed._f_Action == null) return false;
      if (!this._f_Action.equals(typed._f_Action)) return false;
    } else {
      if (typed._f_Action != null) return false;
    }
    if (this._f_SoapAction != null) {
      if (typed._f_SoapAction == null) return false;
      if (!this._f_SoapAction.equals(typed._f_SoapAction)) return false;
    } else {
      if (typed._f_SoapAction != null) return false;
    }
    return true;
  }

  /**
   * Hashcode method implementation.
   */
  public int hashCode() {
    int result = 0;
    if (this._f_Action != null) {
      result+= this._f_Action.hashCode();
    }
    if (this._f_SoapAction != null) {
      result+= this._f_SoapAction.hashCode();
    }
    return result;
  }
}
