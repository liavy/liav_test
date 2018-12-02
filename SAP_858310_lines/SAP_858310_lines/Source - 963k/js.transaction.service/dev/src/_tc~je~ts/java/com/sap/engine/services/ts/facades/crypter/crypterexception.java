package com.sap.engine.services.ts.facades.crypter;

public class CrypterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6991088815459083304L;

	public CrypterException(String msg) {
		super(msg);
	}

	public CrypterException(String msg, Throwable e) {
		super(msg, e);
	}

	public CrypterException(Throwable e) {
		super(e);
	}
}
