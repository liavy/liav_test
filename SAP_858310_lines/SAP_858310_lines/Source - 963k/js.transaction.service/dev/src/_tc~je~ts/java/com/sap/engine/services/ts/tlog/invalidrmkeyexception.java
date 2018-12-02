package com.sap.engine.services.ts.tlog;

/**
 * Thrown when provided resource manager name does not exist into TLog.
 *  
 * @author I024163
 *
 */
public class InvalidRMKeyException extends Exception {

	private static final long serialVersionUID = 3008556103603561445L;

	public InvalidRMKeyException(String msg, Throwable e) {
		super(msg, e);
	}

	public InvalidRMKeyException(String msg) {
		super(msg);
	}

	public InvalidRMKeyException(Throwable e) {
		super(e);
	}
}
