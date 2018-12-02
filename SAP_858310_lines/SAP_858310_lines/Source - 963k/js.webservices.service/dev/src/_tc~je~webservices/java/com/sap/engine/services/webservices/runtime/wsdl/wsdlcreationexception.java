package com.sap.engine.services.webservices.runtime.wsdl;

import com.sap.engine.services.webservices.exceptions.accessors.ServerResourceAccessor;
import com.sap.exception.BaseException;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Location;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Thrown when errors in wsdl building information(VI) are found.
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class WSDLCreationException extends BaseException {
  
  private static final Location LOC = Location.getLocation(WSDLCreationException.class);
  
  public WSDLCreationException(String pattern) {
    super(LOC, new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), pattern));
  }

  public WSDLCreationException(String pattern, Throwable cause) {
    super(LOC, new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), pattern), cause);
  }

  public WSDLCreationException(String pattern, Object[] obj) {
    super(LOC, new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), pattern, obj));
  }

  public WSDLCreationException(String pattern, Object[] obj, Throwable cause) {
    super(LOC, new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), pattern, obj), cause);
  }

  public WSDLCreationException(Throwable cause) {
    super(LOC, cause);
  }

  public WSDLCreationException() {
    super(LOC);
  }

}