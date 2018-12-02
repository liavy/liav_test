package com.sap.archtech.daservice.exceptions;

public class InvalidNameException extends Exception {

	private static final long serialVersionUID = 1234567890l;

	public InvalidNameException() {
		super();
	}

	public InvalidNameException(String s) {
		super(s);
	}
}
