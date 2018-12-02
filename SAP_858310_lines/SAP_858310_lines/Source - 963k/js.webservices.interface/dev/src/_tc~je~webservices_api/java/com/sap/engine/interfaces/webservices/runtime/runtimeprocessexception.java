/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime;

/**
 * Thrown when there is exception at runtime processing.
 * 
 * @author       Dimiter Angelov
 * @version      6.30
 */
public class RuntimeProcessException extends Exception {

  public RuntimeProcessException() {
  }

	/**
	 * @param cause
	 */
	public RuntimeProcessException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RuntimeProcessException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

  public RuntimeProcessException(String s) {
    super(s);
  }
  
  
}
