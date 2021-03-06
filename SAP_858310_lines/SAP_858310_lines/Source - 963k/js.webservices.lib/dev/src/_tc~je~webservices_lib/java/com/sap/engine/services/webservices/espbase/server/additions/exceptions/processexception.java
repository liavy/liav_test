﻿package com.sap.engine.services.webservices.espbase.server.additions.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.TimeZone;

import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.exception.BaseExceptionInfo;
import com.sap.exception.IBaseException;
import com.sap.localization.LocalizableText;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Thrown when in the runtime processing problem occurs.
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */
public class ProcessException extends RuntimeProcessException implements IBaseException {
  
  private static final Location LOC = Location.getLocation(ProcessException.class);
  
  private BaseExceptionInfo exceptionInfo = null;

  public ProcessException() {
    super();
    exceptionInfo = new BaseExceptionInfo(LOC, this);
  }

  public ProcessException(Throwable rootCause) {
    exceptionInfo = new BaseExceptionInfo(LOC, this, rootCause);
  }

  public ProcessException(String pattern) {
    exceptionInfo = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(TBindingResourceAccessor.getResourceAccessor(), pattern), this);
  }

  public ProcessException(String pattern, Object[] obj) {
    exceptionInfo = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(TBindingResourceAccessor.getResourceAccessor(), pattern,  obj), this);
  }


  public ProcessException(String pattern, Object[] obj, Throwable cause) {
    exceptionInfo = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(TBindingResourceAccessor.getResourceAccessor(), pattern,  obj), this, cause);
  }

  public ProcessException(String pattern, Throwable cause) {
    exceptionInfo = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(TBindingResourceAccessor.getResourceAccessor(), pattern), this, cause);
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

  private String soapFaultCode;

  public String getSOAPFaultCode() {
    return this.soapFaultCode;
  }

  public void setSOAPFaultCode(String faultCode) {
    this.soapFaultCode = faultCode;
  }

}

