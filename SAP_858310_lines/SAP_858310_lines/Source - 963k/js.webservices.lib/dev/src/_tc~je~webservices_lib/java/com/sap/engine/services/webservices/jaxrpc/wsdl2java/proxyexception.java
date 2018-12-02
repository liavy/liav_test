/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java;

import javax.xml.namespace.QName;

/**
 * Base exception for all custom exceptions throwed by web service.
 *
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public abstract class ProxyException extends Exception {
  
  public abstract Class getContentClass();

  private String _faultActor = null;
  private QName _faultCode = null;
  private String _faultString = null;


  public QName _getFaultCode() {
    return this._faultCode;
  }

  public void _setFaultCode(QName faultCode) {
    this._faultCode = faultCode;
  }

  public String _getFaultString() {
    return this._faultString;
  }

  public void _setFaultString(String faultString) {
    this._faultString = faultString;
  }

  public String _getFaultActor() {
    return this._faultActor;
  }

  public void _setFaultActor(String faultActor) {
    this._faultActor = faultActor;
  }

}
