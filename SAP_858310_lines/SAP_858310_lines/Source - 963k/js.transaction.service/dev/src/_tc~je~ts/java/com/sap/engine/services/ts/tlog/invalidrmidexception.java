package com.sap.engine.services.ts.tlog;

/**
 * Thrown when provided resource manager ID is wrong.
 * 
 * @author I024163
 *
 */
public class InvalidRMIDException extends Exception {
	
	private static final long serialVersionUID = 7525056113834585751L;

	public InvalidRMIDException(String msg) {
		super(msg);
	}

	public InvalidRMIDException(String msg, Throwable e) {
		super(msg, e);
	}

	public InvalidRMIDException(Throwable e) {
		super(e);
	}
}
