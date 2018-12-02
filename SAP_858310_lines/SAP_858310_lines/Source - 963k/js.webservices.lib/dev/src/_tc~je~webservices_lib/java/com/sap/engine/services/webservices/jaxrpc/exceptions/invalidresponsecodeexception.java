/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.jaxrpc.exceptions;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Locale;
import java.util.TimeZone;

import com.sap.exception.BaseExceptionInfo;
import com.sap.exception.IBaseException;
import com.sap.localization.LocalizableText;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * @author Alexander Zubev (alexander.zubev@sap.com)
 */
public class InvalidResponseCodeException extends IOException implements IBaseException {
  public static final String INVALID_RESPONSE = "webservices_3500";
  public static final String INVALID_RESPONSE_NOT_FOUND = "webservices_3501";
  
  private static final Location LOC = Location.getLocation(InvalidResponseCodeException.class);
  
  private BaseExceptionInfo info;
  
  private Hashtable responseHeaders;
  private int responseCode;

  public InvalidResponseCodeException(int responseCode, String responseMessage, Hashtable responseHeaders, String url) {
    super();
    if (responseCode == 404) {
      this.info = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), INVALID_RESPONSE_NOT_FOUND, new Object[] {Integer.toString(responseCode), responseMessage,url}), this);
    } else {
      this.info = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), INVALID_RESPONSE, new Object[] {Integer.toString(responseCode), responseMessage,url}), this);
    }
    this.responseHeaders = responseHeaders;
    this.responseCode = responseCode;
  }

  public Hashtable getResponseHeaders() {
    return responseHeaders;
  }
  
  public String[] getResponseHeader(String headerName) {
    return (String[]) responseHeaders.get(headerName);
  }
  
  public int getResponseCode() {
    return responseCode;
  }

  public Throwable initCause(Throwable cause) {
    if (info != null) {
      return info.initCause(cause);
    } else {
      return this;
    }
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

  public String getLocalizedMessage(Locale loc) {
    return info.getLocalizedMessage(loc);
  }

  public String getLocalizedMessage(TimeZone timeZone) {
    return info.getLocalizedMessage(timeZone);
  }

  public String getLocalizedMessage(Locale loc, TimeZone timeZone) {
    return info.getLocalizedMessage(loc, timeZone);
  }

  public String getNestedLocalizedMessage() {
    return info.getNestedLocalizedMessage();
  }

  public String getNestedLocalizedMessage(Locale loc) {
    return info.getNestedLocalizedMessage(loc);
  }

  public String getNestedLocalizedMessage(TimeZone timeZone) {
    return info.getNestedLocalizedMessage(timeZone);
  }

  public String getNestedLocalizedMessage(Locale loc, TimeZone timeZone) {
    return info.getNestedLocalizedMessage(loc, timeZone);
  }

  public void finallyLocalize() {
    info.finallyLocalize();
  }

  public void finallyLocalize(Locale loc) {
    info.finallyLocalize(loc);
  }

  public void finallyLocalize(TimeZone timeZone) {
    info.finallyLocalize(timeZone);
  }

  public void finallyLocalize(Locale loc, TimeZone timeZone) {
    info.finallyLocalize(loc, timeZone);
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

  public void printStackTrace(PrintStream s) {
    info.printStackTrace(s);
  }

  public void printStackTrace(PrintWriter s) {
    info.printStackTrace(s);
  }

  public void setLogSettings(Category cat, int severity, Location loc) {
//    info.setLogSettings(cat, severity, loc);
  }

  public void log() {
//    info.log();
  }
}
