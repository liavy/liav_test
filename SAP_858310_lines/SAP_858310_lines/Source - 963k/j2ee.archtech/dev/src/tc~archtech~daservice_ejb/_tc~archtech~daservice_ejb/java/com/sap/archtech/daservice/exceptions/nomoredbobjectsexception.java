package com.sap.archtech.daservice.exceptions;

public class NoMoreDBObjectsException extends Exception {

	private static final long serialVersionUID = 1234567890l;

	public NoMoreDBObjectsException() {
		super();
	}

	public NoMoreDBObjectsException(String s) {
		super(s);
	}
}
