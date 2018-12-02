package com.sap.archtech.daservice.exceptions;

public class WrongChecksumException extends Exception {

	private static final long serialVersionUID = 1234567890l;

	public WrongChecksumException() {
		super();
	}

	public WrongChecksumException(String s) {
		super(s);
	}
}
