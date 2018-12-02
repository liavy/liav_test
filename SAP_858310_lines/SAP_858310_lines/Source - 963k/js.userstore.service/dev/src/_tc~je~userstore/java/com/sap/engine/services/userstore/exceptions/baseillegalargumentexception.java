/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.userstore.exceptions;

import java.util.Locale;
import java.util.TimeZone;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.PrintStream;

import com.sap.exception.IBaseException;
import com.sap.exception.BaseExceptionInfo;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.localization.LocalizableText;

public class BaseIllegalArgumentException extends IllegalArgumentException implements IBaseException {
  public static final String PROPERTY_MUST_BE_INTEGER = "userstore_0913";
  public static final String PROPERTY_MUST_BE_DATE = "userstore_0914";
  public static final String PROPERTY_MUST_BE_BOOLEAN = "userstore_0915";
  public static final String PROPERTY_MUST_BE_STRING = "userstore_0916";
  public static final String PROPERTY_MUST_BE_INTEGER1 = "userstore_0919";
  public static final String UNACCEPTABLE_PROPERTY_VALUE = "userstore_0920";

  private BaseExceptionInfo info = null;

  public BaseIllegalArgumentException(String key) {
    super(key);
    info = new BaseExceptionInfo(UserstoreResourceAccessor.category,
                                 Severity.ERROR,
                                 UserstoreResourceAccessor.location,
                                 new LocalizableTextFormatter(UserstoreResourceAccessor.getResourceAccessor(), key, null),
                                 this,
                                 null);
  }

  public BaseIllegalArgumentException(String key, Object[] args) {
    super(key);
    info = new BaseExceptionInfo(UserstoreResourceAccessor.category,
                                 Severity.ERROR,
                                 UserstoreResourceAccessor.location,
                                 new LocalizableTextFormatter(UserstoreResourceAccessor.getResourceAccessor(), key, args),
                                 this,
                                 null);
  }


  public BaseIllegalArgumentException(String key, Throwable linkedException) {
    super(key);
    info = new BaseExceptionInfo(UserstoreResourceAccessor.category,
                                 Severity.ERROR,
                                 UserstoreResourceAccessor.location,
                                 new LocalizableTextFormatter(UserstoreResourceAccessor.getResourceAccessor(), key, null),
                                 this,
                                 linkedException);
  }


  public BaseIllegalArgumentException(String key, Object[] args, Throwable linkedException) {
    super(key);
    info = new BaseExceptionInfo(UserstoreResourceAccessor.category,
                                 Severity.ERROR,
                                 UserstoreResourceAccessor.location,
                                 new LocalizableTextFormatter(UserstoreResourceAccessor.getResourceAccessor(), key, args),
                                 this,
                                 linkedException);
  }


  public Throwable initCause(Throwable throwable) {
    return info.initCause(throwable);
  }

  public Throwable getCause() {
    return info.getCause();
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
    info.setLogSettings(category, i, location);
  }

  public void log() {
    info.log();
  }

  private Object writeReplace() {
    StringWriter stringWriter = new StringWriter();
    printStackTrace(new PrintWriter(stringWriter, true));
    return new IllegalArgumentException(stringWriter.toString());
  }
}
