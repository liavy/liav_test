package com.sap.engine.services.ssl.exception;

import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Locale;
import java.util.TimeZone;

import com.sap.exception.BaseExceptionInfo;
import com.sap.exception.IBaseException;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.localization.LocalizableText;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

public class KeyStoreConnectorException extends SecurityException implements IBaseException {

  public static final String KEYSTORE_IS_NOT_INITIALIZED = "ssl_0010";
  public static final String KEYSTORE_CANNOT_BE_BROWSED = "ssl_0011";
  public static final String SERVER_IDENTITY_NOT_FOUND = "ssl_0012";
  public static final String KEYSTORE_ENTRY_CANNOT_BE_CONSTRUCTED = "ssl_0013";
  public static final String MISSING_CERTIFICATE = "ssl_0014";
  public static final String MISSING_CERTIFICATE_CHAIN = "ssl_0015";

  private BaseExceptionInfo info = null;

  public KeyStoreConnectorException(String key) {
    super(key);
    info = new BaseExceptionInfo(SSLResourceAccessor.category,
                                 Severity.ERROR,
                                 SSLResourceAccessor.location,
                                 new LocalizableTextFormatter(SSLResourceAccessor.getResourceAccessor(), key, null),
                                 this,
                                 null);
  }

  public KeyStoreConnectorException(String key, Object[] args) {
    super(key);
    info = new BaseExceptionInfo(SSLResourceAccessor.category,
                                 Severity.ERROR,
                                 SSLResourceAccessor.location,
                                 new LocalizableTextFormatter(SSLResourceAccessor.getResourceAccessor(), key, args),
                                 this,
                                 null);
  }


  public KeyStoreConnectorException(String key,Throwable linkedException) {
    super(key);
    info = new BaseExceptionInfo(SSLResourceAccessor.category,
                                 Severity.ERROR,
                                 SSLResourceAccessor.location,
                                 new LocalizableTextFormatter(SSLResourceAccessor.getResourceAccessor(), key, null),
                                 this,
                                 linkedException);
  }


  public KeyStoreConnectorException(String key, Object[] args, Throwable linkedException) {
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
