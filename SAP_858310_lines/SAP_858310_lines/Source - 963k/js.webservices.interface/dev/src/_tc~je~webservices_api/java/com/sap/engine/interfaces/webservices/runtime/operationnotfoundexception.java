/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime;

/**
 *   This exception is thrown when the Runtime finds zero
 * or more than one operations given the message keys from
 * RuntimeTransportBinding getMessageKeys() method.
 *
 * @author Dimitar Angelov
 * @version 6.30
 */

public class OperationNotFoundException extends Exception {

  public OperationNotFoundException() {
  }

  public OperationNotFoundException(String s) {
    super(s);
  }
}
