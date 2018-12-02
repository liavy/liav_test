package com.sap.archtech.daservice.exceptions;

public class MissingParameterException extends Exception {

	private static final long serialVersionUID = 1234567890l;

	public MissingParameterException() {
		super();
	}

	public MissingParameterException(String s) {
		super(s);
	}
}
