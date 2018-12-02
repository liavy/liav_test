package com.sap.engine.services.webservices.server.deploy.migration.ws.exception;

import com.sap.engine.services.webservices.exceptions.accessors.ServerResourceAccessor;
import com.sap.exception.BaseException;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Location;

/**
 * Exception class thrown when convertion of old data is made.
 * @author aneta-a
 */
public class ConversionException extends BaseException {
  
  private static final Location LOC = Location.getLocation(ConversionException.class);
  
  public ConversionException(String errorMessage) {
    super(LOC, new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), errorMessage));
  } 
  
  public ConversionException(String errorMessage, Throwable exc) {
    super(LOC, new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), errorMessage), exc);
  }
  
	public ConversionException(String errorMessage, Object[] args) {
    super(LOC, new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), errorMessage, args));
	}
  
  public ConversionException(String errorMessage, Object[] args, Throwable exc) {
    super(LOC, new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), errorMessage, args), exc);
  }
}
