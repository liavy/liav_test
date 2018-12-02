package com.sap.engine.services.webservices.exceptions;

import com.sap.engine.services.webservices.exceptions.accessors.ServerResourceAccessor;
import com.sap.exception.BaseException;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;

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

public class WSGenerationException extends BaseException {

  public WSGenerationException() {
    this(new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), PatternKeys.EMPTY_PATTERN, new Object[0]));
  }

  public WSGenerationException(String s) {
    this(new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), PatternKeys.DEFAULT_PATTERN, new Object[]{s}));
  }

  public WSGenerationException(Throwable cause) {
    this(new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), PatternKeys.EMPTY_PATTERN, new Object[0]), cause);
  }

  public WSGenerationException(String patternKey, Object[] args) {
    this(new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), patternKey, args));
  }

  public WSGenerationException(LocalizableTextFormatter locFormatter) {
    this(locFormatter, null, Severity.PATH, null);
  }

  public WSGenerationException(String patternKey, Object[] args, Throwable cause) {
    this(new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), patternKey, args), cause);
  }

  public WSGenerationException(LocalizableTextFormatter locFormatter, Throwable cause) {
    this(locFormatter, cause, null, Severity.PATH, null);
  }

  public WSGenerationException(LocalizableTextFormatter locFormatter, Category cat, int severity, Location loc) {
    this(locFormatter, null, cat, severity, loc);
  }

  public WSGenerationException(LocalizableTextFormatter locFormatter, Throwable cause, Category cat, int severity, Location loc) {
    super(cat, severity, loc, locFormatter, cause);
  }

   private Object writeReplace() {
    StringWriter stringWriter = new StringWriter();
    printStackTrace(new PrintWriter(stringWriter, true));
    return new Exception(stringWriter.toString());
  }

}