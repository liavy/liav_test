/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime;

/**
 *   This exception marks that there is an error in the
 * recieved message.
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */
public class MessageException extends Exception {

  public MessageException() {
  }

	/**
	 * @param cause
	 */
	public MessageException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MessageException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

  public MessageException(String s) {
    super(s);
  }
}
