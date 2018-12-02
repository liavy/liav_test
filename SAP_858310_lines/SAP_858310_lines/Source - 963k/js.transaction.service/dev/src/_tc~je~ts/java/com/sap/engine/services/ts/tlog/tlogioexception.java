package com.sap.engine.services.ts.tlog;

/**
 * Thrown when unexpected IOException or SQLException occurred.
 * 
 * @author I024163
 *
 */
public class TLogIOException extends Exception {

	private static final long serialVersionUID = 5710416640449628397L;

	public TLogIOException(String msg, Throwable e) {
		super(msg, e);
	}

	public TLogIOException(String msg) {
		super(msg);
	}

	public TLogIOException(Throwable e) {
		super(e);
	}
}
