package com.sap.engine.services.ts.tlog;

public class RMNameAlreadyInUseException extends Exception {

	private static final long serialVersionUID = -8439898822789566726L;

	public RMNameAlreadyInUseException(String msg, Throwable e) {
		super(msg, e);
	}

	public RMNameAlreadyInUseException(String msg) {
		super(msg);
	}

	public RMNameAlreadyInUseException(Throwable e) {
		super(e);
	}
}
