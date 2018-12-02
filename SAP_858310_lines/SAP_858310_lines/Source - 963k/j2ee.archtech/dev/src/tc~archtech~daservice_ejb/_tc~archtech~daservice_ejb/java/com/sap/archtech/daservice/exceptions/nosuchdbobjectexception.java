package com.sap.archtech.daservice.exceptions;

public class NoSuchDBObjectException extends Exception {

	private static final long serialVersionUID = 1234567890l;

	public NoSuchDBObjectException() {
		super();
	}

	public NoSuchDBObjectException(String s) {
		super(s);
	}
}
