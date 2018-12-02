package com.sap.engine.services.ts.tlog;

/**
 * Thrown when TLog with specified TLogVersion already exist.
 *  
 * @author I024163
 *
 */
public class TLogAlreadyExistException extends Exception {

	private static final long serialVersionUID = -5620880541699497719L;

	public TLogAlreadyExistException(String msg) {
		super(msg);
	}

	public TLogAlreadyExistException(String msg, Throwable e) {
		super(msg, e);
	}

	public TLogAlreadyExistException(Throwable e) {
		super(e);
	}
}
