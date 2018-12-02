package com.sap.engine.services.webservices.exceptions.lib;

import com.sap.exception.IBaseException;
import com.sap.exception.BaseExceptionInfo;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.localization.LocalizableText;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.engine.services.webservices.exceptions.lib.accessors.LibraryResourceAccessor;
import com.sap.engine.services.webservices.exceptions.lib.accessors.PatternKeys;

import java.util.Locale;
import java.util.TimeZone;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.IOException;


/**
 * Title:  
 * Description: 
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSConnectionException extends IOException implements IBaseException {

  private BaseExceptionInfo exceptionInfo = null;

  public WSConnectionException() {
    this((LocalizableTextFormatter)null);
  }

  public WSConnectionException(String s) {
    this(new LocalizableTextFormatter(LibraryResourceAccessor.getResourceAccessor(), PatternKeys.DEFAULT_PATTERN, new Object[]{s}));
  }

  public WSConnectionException(Throwable cause) {
    this(null, cause);
  }

  public WSConnectionException(String patternKey, Object[] args) {
    this(new LocalizableTextFormatter(LibraryResourceAccessor.getResourceAccessor(), patternKey, args));
  }

  public WSConnectionException(String patternKey, Object[] args, Throwable t) {
    this(new LocalizableTextFormatter(LibraryResourceAccessor.getResourceAccessor(), patternKey, args), t);
  }

  public WSConnectionException(LocalizableTextFormatter locFormatter) {
    this(locFormatter, null, Severity.PATH, null);
  }

  public WSConnectionException(LocalizableTextFormatter locFormatter, Category cat, int severity, Location loc) {
    this(locFormatter, cat, severity, loc, null);
  }

  public WSConnectionException(LocalizableTextFormatter locFormatter, Throwable cause) {
    this(locFormatter, null, Severity.PATH, null, cause);
  }

  public WSConnectionException(LocalizableTextFormatter locFormatter, Category cat, int severity, Location loc, Throwable cause) {
    exceptionInfo = new BaseExceptionInfo(cat, severity, loc, locFormatter, this, cause);
  }

  public Throwable initCause(Throwable cause) {
    return exceptionInfo.initCause(cause);
  }

  public Throwable getCause() {
    return exceptionInfo.getCause();
  }

  public String getMessage() {
    return null;
  }

  public String getNestedMessage() {
    return null;
  }

  public LocalizableText getLocalizableMessage() {
    return exceptionInfo.getLocalizableMessage();
  }

  public String getLocalizedMessage() {
    return exceptionInfo.getLocalizedMessage();
  }

  public String getLocalizedMessage(Locale loc) {
    return exceptionInfo.getLocalizedMessage(loc);
  }

  public String getLocalizedMessage(TimeZone timeZone) {
    return exceptionInfo.getLocalizedMessage(timeZone);
  }

  public String getLocalizedMessage(Locale loc, TimeZone timeZone) {
    return exceptionInfo.getLocalizedMessage(loc, timeZone);
  }

  public String getNestedLocalizedMessage() {
    return exceptionInfo.getNestedLocalizedMessage();
  }

  public String getNestedLocalizedMessage(Locale loc) {
    return exceptionInfo.getNestedLocalizedMessage(loc);
  }

  public String getNestedLocalizedMessage(TimeZone timeZone) {
    return exceptionInfo.getNestedLocalizedMessage(timeZone);
  }

  public String getNestedLocalizedMessage(Locale loc, TimeZone timeZone) {
    return exceptionInfo.getNestedLocalizedMessage(loc, timeZone);
  }

  public void finallyLocalize() {
    exceptionInfo.finallyLocalize();
  }

  public void finallyLocalize(Locale loc) {
    exceptionInfo.finallyLocalize(loc);
  }

  public void finallyLocalize(TimeZone timeZone) {
    exceptionInfo.finallyLocalize(timeZone);
  }

  public void finallyLocalize(Locale loc, TimeZone timeZone) {
    exceptionInfo.finallyLocalize(loc, timeZone);
  }

  public String getSystemStackTraceString() {
    StringWriter s = new StringWriter();
    super.printStackTrace(new PrintWriter(s));
    return s.toString();
  }

  public String getStackTraceString() {
    return exceptionInfo.getStackTraceString();
  }

  public String getNestedStackTraceString() {
    return exceptionInfo.getNestedStackTraceString();
  }

  public void printStackTrace() {
    exceptionInfo.printStackTrace();
  }

  public void printStackTrace(PrintStream s) {
    exceptionInfo.printStackTrace(s);
  }

  public void printStackTrace(PrintWriter s) {
    exceptionInfo.printStackTrace(s);
  }

  /**
   * Setter method for logging information.
   *
   * @param cat logging category
   * @param severity logging severity
   * @param loc logging location
   */
  public void setLogSettings(Category cat, int severity, Location loc) {
//    exceptionInfo.setLogSettings(cat, severity, loc);
  }

  /**
   * Logs the exception message.
   */
  public void log() {
//    exceptionInfo.log();
  }

  private Object writeReplace() {
    StringWriter stringWriter = new StringWriter();
    printStackTrace(new PrintWriter(stringWriter, true));
    return new Exception(stringWriter.toString());
  }
}