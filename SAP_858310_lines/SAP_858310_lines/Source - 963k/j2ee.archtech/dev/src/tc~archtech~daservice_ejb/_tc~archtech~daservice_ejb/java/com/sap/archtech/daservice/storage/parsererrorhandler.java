package com.sap.archtech.daservice.storage;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class ParserErrorHandler implements ErrorHandler {
	
	public String message;

	public ParserErrorHandler() {
		super();
		message = "";
	}

	public void warning(SAXParseException e) throws SAXParseException {
		this.message = "SAXParseException: warning in line "
				+ e.getLineNumber() + ": " + e.getMessage();
		throw new SAXParseException(this.message, null);
	}

	public void error(SAXParseException e) throws SAXParseException {
		this.message = "SAXParseException: error in line " + e.getLineNumber()
				+ ": " + e.getMessage();
		throw new SAXParseException(this.message, null);
	}

	public void fatalError(SAXParseException e) throws SAXParseException {
		this.message = "SAXParseException: fatalError in line "
				+ e.getLineNumber() + ": " + e.getMessage();
		throw new SAXParseException(this.message, null);
	}
}