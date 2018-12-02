package com.sap.engine.services.ts.tlog;

public class TLogFullException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1933744891580760262L;

	public TLogFullException(String msg) {
		super(msg);
	}

	public TLogFullException(String msg, Throwable e) {
		super(msg, e);
	}

	public TLogFullException(Throwable e) {
		super(e);
	}
}
