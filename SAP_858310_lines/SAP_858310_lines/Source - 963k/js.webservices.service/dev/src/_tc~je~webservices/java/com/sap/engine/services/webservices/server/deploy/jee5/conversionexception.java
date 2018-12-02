package com.sap.engine.services.webservices.server.deploy.jee5;

import com.sap.engine.services.webservices.exceptions.accessors.ServerResourceAccessor;
import com.sap.exception.BaseException;
import com.sap.localization.LocalizableTextFormatter;

public class ConversionException extends BaseException {

	 public ConversionException(String patterKey, Object[] args, Throwable cause) {
		    this(new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), patterKey, args), cause);     
		  }

	public ConversionException(
			LocalizableTextFormatter textFormater, Throwable cause) {
		super(textFormater,cause);
	}  
}
