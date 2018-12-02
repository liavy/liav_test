package com.sap.engine.services.ts.tlog;

/**
 * Thrown when there is no transaction classifier for given id
 * 
 * @author C5117082
 *
 */
public class InvalidClassifierIDException extends Exception {
	
	private static final long serialVersionUID = 6697546506273454070L;

	public InvalidClassifierIDException(String msg) {
		super(msg);
	}

	public InvalidClassifierIDException(String msg, Throwable e) {
		super(msg, e);
	}

	public InvalidClassifierIDException(Throwable e) {
		super(e);
	}

}
