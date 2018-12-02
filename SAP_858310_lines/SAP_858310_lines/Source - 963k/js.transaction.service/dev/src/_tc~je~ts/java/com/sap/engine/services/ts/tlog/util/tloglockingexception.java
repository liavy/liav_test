package com.sap.engine.services.ts.tlog.util;

/**
 * Thrown from lock operation when specified lock already exist. 
 * The exception is thrown also from unlock operation when lock does not exist.
 * 
 * @author I024163
 *
 */
public class TLogLockingException extends Exception {

	private static final long serialVersionUID = -38920073215169964L;

	public TLogLockingException(String msg) {
		super(msg);
	}

	public TLogLockingException(String msg, Throwable e) {
		super(msg, e);
	}

	public TLogLockingException(Throwable e) {
		super(e);
	}
}
