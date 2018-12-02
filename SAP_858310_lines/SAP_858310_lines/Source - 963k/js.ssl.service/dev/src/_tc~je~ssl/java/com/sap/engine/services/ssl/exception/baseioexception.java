/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.ssl.exception;

import com.sap.exception.IBaseException;
import com.sap.exception.BaseExceptionInfo;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.localization.LocalizableText;

import java.util.Locale;
import java.util.TimeZone;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.IOException;

public class BaseIOException extends IOException implements IBaseException {

  public static final String GENERAL_IO_EXCEPTION = "ssl_0030";

  private BaseExceptionInfo info = null;

  public BaseIOException(String key) {
    super(key);
    info = new BaseExceptionInfo(SSLResourceAccessor.category,
                                 Severity.ERROR,
                                 SSLResourceAccessor.location,
                                 new LocalizableTextFormatter(SSLResourceAccessor.getResourceAccessor(), key, null),
                                 this,
                                 null);
  }

  public BaseIOException(String key, Object[] args) {
    super(key);
    info = new BaseExceptionInfo(SSLResourceAccessor.category,
                                 Severity.ERROR,
                                 SSLResourceAccessor.location,
                                 new LocalizableTextFormatter(SSLResourceAccessor.getResourceAccessor(), key, args),
                                 this,
                                 null);
  }


  public BaseIOException(String key,Throwable linkedException) {
    super(key);
    info = new BaseExceptionInfo(SSLResourceAccessor.category,
                                 Severity.ERROR,
                                 SSLResourceAccessor.location,
                                 new LocalizableTextFormatter(SSLResourceAccessor.getResourceAccessor(), key, null),
                                 this,
                                 linkedException);
  }


  public BaseIOException(String key, Object[] args, Throwable linkedException) {
    super(key);
    info = new BaseExceptionInfo(SSLResourceAccessor.category,
                                 Severity.ERROR,
                                 SSLResourceAccessor.location,
                                 new LocalizableTextFormatter(SSLResourceAccessor.getResourceAccessor(), key, args),
                                 this,
                                 linkedException);
  }

  public static BaseIOException wrapException(Throwable e) {
    if (e instanceof BaseIOException) {
      return (BaseIOException) e;
    }

    return new BaseIOException(GENERAL_IO_EXCEPTION, e);
  }

  public Throwable initCause(Throwable throwable) {
    if (info != null) {
      return info.initCause(throwable);
    } else {
      return throwable;
    }
  }

  public Throwable getCause() {
    if (info != null) {
      return info.getCause();
    } else {
      return null;
    }
  }

  public String getMessage() {
    return info.getLocalizedMessage();
  }

  public LocalizableText getLocalizableMessage() {
    return info.getLocalizableMessage();
  }

  public String getLocalizedMessage() {
    return info.getLocalizedMessage();
  }

  public String getLocalizedMessage(Locale locale) {
    return info.getLocalizedMessage(locale);
  }

  public String getLocalizedMessage(TimeZone zone) {
    return info.getLocalizedMessage(zone);
  }

  public String getLocalizedMessage(Locale locale, TimeZone zone) {
    return info.getLocalizedMessage(locale,zone);
  }

  public String getNestedLocalizedMessage() {
    return info.getNestedLocalizedMessage();
  }

  public String getNestedLocalizedMessage(Locale locale) {
    return info.getNestedLocalizedMessage(locale);
  }

  public String getNestedLocalizedMessage(TimeZone zone) {
    return info.getNestedLocalizedMessage(zone);
  }

  public String getNestedLocalizedMessage(Locale locale, TimeZone zone) {
    return info.getNestedLocalizedMessage(locale,zone);
  }

  public void finallyLocalize() {
    info.finallyLocalize();
  }

  public void finallyLocalize(Locale locale) {
    info.finallyLocalize(locale);
  }

  public void finallyLocalize(TimeZone zone) {
    info.finallyLocalize(zone);
  }

  public void finallyLocalize(Locale locale, TimeZone zone) {
    info.finallyLocalize(locale,zone);
  }

  public String getSystemStackTraceString() {
    StringWriter s = new StringWriter();
    super.printStackTrace(new PrintWriter(s));
    return s.toString();
  }

  public String getStackTraceString() {
    return info.getStackTraceString();
  }

  public String getNestedStackTraceString() {
    return info.getNestedStackTraceString();
  }

  public void printStackTrace() {
    info.printStackTrace();
  }

  public void printStackTrace(PrintStream stream) {
    info.printStackTrace(stream);
  }

  public void printStackTrace(PrintWriter writer) {
    info.printStackTrace(writer);
  }

  public void setLogSettings(Category category, int i, Location location) {
    info.setLogSettings(category,i,location);
  }

  public void log() {
    info.log();
  }

  public void trace(int level, Location location) {
    info.trace(level, location);
  }

  private Object writeReplace() {
    StringWriter stringWriter = new StringWriter();
    printStackTrace(new PrintWriter(stringWriter, true));
    return new IOException(stringWriter.toString());
  }

}