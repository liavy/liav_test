package com.sap.engine.services.webservices.runtime;

import com.sap.engine.services.webservices.exceptions.accessors.ServerResourceAccessor;
import com.sap.exception.BaseExceptionInfo;
import com.sap.exception.IBaseException;
import com.sap.localization.LocalizableText;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */
public class ServerRuntimeProcessException extends com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException implements IBaseException {

  private static final Location LOC = Location.getLocation(ServerRuntimeProcessException.class);
  
  private BaseExceptionInfo exceptionInfo;

  public ServerRuntimeProcessException(String pattern) {
    exceptionInfo = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), pattern), this);
  }

  public ServerRuntimeProcessException(String pattern, Throwable cause) {
    exceptionInfo = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), pattern), this, cause);
  }

  public ServerRuntimeProcessException(String pattern, Object[] obj) {
    exceptionInfo = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), pattern, obj), this);
  }

  public ServerRuntimeProcessException(String pattern, Object[] obj, Throwable cause) {
    exceptionInfo = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(ServerResourceAccessor.getResourceAccessor(), pattern, obj), this, cause);
  }

  public ServerRuntimeProcessException(Throwable cause) {
    exceptionInfo = new BaseExceptionInfo(LOC, this, cause);
  }

  public ServerRuntimeProcessException() {
    exceptionInfo = new BaseExceptionInfo(LOC, this);
  }

  public Throwable initCause(Throwable cause) {
    return exceptionInfo.initCause(cause);
  }

  public Throwable getCause() {
    return exceptionInfo.getCause();
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

  public void setLogSettings(Category cat, int severity, Location loc) {
//    exceptionInfo.setLogSettings(cat, severity, loc);
  }

  public void log() {
//    exceptionInfo.log();
  }

}

