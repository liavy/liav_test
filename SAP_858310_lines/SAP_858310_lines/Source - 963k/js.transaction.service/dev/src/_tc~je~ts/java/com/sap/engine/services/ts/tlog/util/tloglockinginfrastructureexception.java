package com.sap.engine.services.ts.tlog.util;

/**
 * Thrown when unexpected exception with enqueue occurred.
 * 
 * @author I024163
 *
 */
public class TLogLockingInfrastructureException extends Exception {

	private static final long serialVersionUID = 2381071526610269780L;

	public TLogLockingInfrastructureException(String msg) {
		super(msg);
	}

	public TLogLockingInfrastructureException(String msg, Throwable e) {
		super(msg, e);
	}

	public TLogLockingInfrastructureException(Throwable e) {
		super(e);
	}
}
