package com.sap.engine.services.webservices.exceptions;

import com.sap.exception.BaseException;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.engine.services.webservices.exceptions.accessors.ServerResourceAccessor;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * Title:  
 * Description: 
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSConfigurationException extends BaseException {

  public WSConfigurationException() {
    this(new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), PatternKeys.EMPTY_PATTERN, new Object[0]));
  }

  public WSConfigurationException(String s) {
    this(new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), PatternKeys.DEFAULT_PATTERN, new Object[]{s}));
  }

  public WSConfigurationException(Throwable cause) {
    this(new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), PatternKeys.EMPTY_PATTERN, new Object[0]), cause);
  }

  public WSConfigurationException(String patternKey, Object[] args) {
    this(new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), patternKey, args));
  }

  public WSConfigurationException(LocalizableTextFormatter locFormatter) {
    this(locFormatter, null, Severity.PATH, null);
  }

  public WSConfigurationException(String patternKey, Object[] args, Throwable cause) {
    this(new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), patternKey, args), cause);
  }

  public WSConfigurationException(LocalizableTextFormatter locFormatter, Throwable cause) {
    this(locFormatter, cause, null, Severity.PATH, null);
  }


  public WSConfigurationException(LocalizableTextFormatter locFormatter, Category cat, int severity, Location loc) {
    this(locFormatter, null, cat, severity, loc);
  }

  public WSConfigurationException(LocalizableTextFormatter locFormatter, Throwable cause, Category cat, int severity, Location loc) {
    super(cat, severity, loc, locFormatter, cause);
  }

   private Object writeReplace() {
    StringWriter stringWriter = new StringWriter();
    printStackTrace(new PrintWriter(stringWriter, true));
    return new Exception(stringWriter.toString());
  }

}