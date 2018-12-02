/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 *
 */
package com.sap.engine.services.ssl.exception;

import com.sap.exception.BaseExceptionInfo;
import com.sap.exception.IBaseException;
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

/**
 *  This is the root exception for all exceptions thrown from SSL service on
 * configuration operations that cannot be acompilshed due to illegal or invalid
 * arguments or access restrictions.
 *
 * @author  Stephan Zlatarev
 * @version 4.0.2
 */
public class SSLConfigurationException extends SecurityException implements IBaseException {

  public static final String NO_ENABLED_CIPHER_SUITES = "ssl_0020";
  public static final String NO_ENABLED_CERTIFICATES = "ssl_0021";
  public static final String UNABLE_TO_CONFIGURE_SOCKETS = "ssl_0022";
  public static final String SOCKET_NOT_REGISTERED = "ssl_0023";
  public static final String GENERAL_EXCEPTION = "ssl_0024";

  private BaseExceptionInfo info = null;

  public static SSLConfigurationException wrapException(Exception e, String host, int port) {
    if (e instanceof SSLConfigurationException) {
      return (SSLConfigurationException) e;
    } else if (e instanceof NullPointerException) {
      return new SSLConfigurationException(SOCKET_NOT_REGISTERED, new Object[] { host, new Integer(port) }, e);
    }
    return new SSLConfigurationException(GENERAL_EXCEPTION, e);
  }

  public SSLConfigurationException(String key) {
    super(key);
    info = new BaseExceptionInfo(SSLResourceAccessor.category,
                                 Severity.ERROR,
                                 SSLResourceAccessor.location,
                                 new LocalizableTextFormatter(SSLResourceAccessor.getResourceAccessor(), key, null),
                                 this,
                                 null);
  }

  public SSLConfigurationException(String key, Object[] args) {
    super(key);
    info = new BaseExceptionInfo(SSLResourceAccessor.category,
                                 Severity.ERROR,
                                 SSLResourceAccessor.location,
                                 new LocalizableTextFormatter(SSLResourceAccessor.getResourceAccessor(), key, args),
                                 this,
                                 null);
  }


  public SSLConfigurationException(String key,Throwable linkedException) {
    super(key);
    info = new BaseExceptionInfo(SSLResourceAccessor.category,
                                 Severity.ERROR,
                                 SSLResourceAccessor.location,
                                 new LocalizableTextFormatter(SSLResourceAccessor.getResourceAccessor(), key, null),
                                 this,
                                 linkedException);
  }


  public SSLConfigurationException(String key, Object[] args, Throwable linkedException) {
    super(key);
    info = new BaseExceptionInfo(SSLResourceAccessor.category,
                                 Severity.ERROR,
                                 SSLResourceAccessor.location,
                                 new LocalizableTextFormatter(SSLResourceAccessor.getResourceAccessor(), key, args),
                                 this,
                                 linkedException);
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
    return new SecurityException(stringWriter.toString());
  }
}

