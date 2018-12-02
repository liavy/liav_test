/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime;

/**
 *   This exception is base exception for exceptions which are thrown
 * from protocol methods.
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */
public class ProtocolException extends Exception {

  public ProtocolException() {
  }

	/**
	 * @param cause
	 */
	public ProtocolException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ProtocolException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

  public ProtocolException(String s) {
    super(s);
  }
}

