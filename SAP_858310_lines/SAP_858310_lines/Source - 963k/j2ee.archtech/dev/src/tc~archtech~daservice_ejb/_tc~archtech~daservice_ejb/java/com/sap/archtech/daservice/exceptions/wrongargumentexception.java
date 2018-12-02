package com.sap.archtech.daservice.exceptions;

public class WrongArgumentException extends Exception {

	private static final long serialVersionUID = 1234567890l;

	public WrongArgumentException() {
		super();
	}

	public WrongArgumentException(String s) {
		super(s);
	}
}
