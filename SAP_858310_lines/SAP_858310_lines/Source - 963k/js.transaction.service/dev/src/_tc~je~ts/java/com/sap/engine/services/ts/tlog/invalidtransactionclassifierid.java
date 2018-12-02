package com.sap.engine.services.ts.tlog;

public class InvalidTransactionClassifierID extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidTransactionClassifierID(String msg, Throwable e) {
		super(msg, e);
	}

	public InvalidTransactionClassifierID(String msg) {
		super(msg);
	}

	public InvalidTransactionClassifierID(Throwable e) {
		super(e);
	}
}
