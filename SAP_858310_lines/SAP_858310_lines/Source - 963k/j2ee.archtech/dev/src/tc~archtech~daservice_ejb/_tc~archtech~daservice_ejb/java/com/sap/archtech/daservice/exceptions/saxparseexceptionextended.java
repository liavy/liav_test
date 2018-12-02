package com.sap.archtech.daservice.exceptions;

import java.io.IOException;

public class SAXParseExceptionExtended extends IOException {

	private static final long serialVersionUID = 1234567890l;

	public SAXParseExceptionExtended() {
		super();
	}

	public SAXParseExceptionExtended(String s) {
		super(s);
	}
}
