/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime;

/**
 *  This is base exception which is thrown by the TransportBinding methods
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class TransportBindingException extends Exception {

  public TransportBindingException() {
  }

  public TransportBindingException(Throwable cause) {
    super(cause);
    // TODO Auto-generated constructor stub
  }

  public TransportBindingException(String message, Throwable cause) {
    super(message, cause);
    // TODO Auto-generated constructor stub
  }

  public TransportBindingException(String s) {
    super(s);
  }
}