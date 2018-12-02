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
package com.sap.engine.services.webservices.jaxrpc.exceptions;

import com.sap.exception.IBaseException;
import com.sap.exception.BaseExceptionInfo;
import com.sap.localization.LocalizableText;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

import javax.xml.rpc.ServiceException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This exception is thrown for all Client exceptions.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class WebserviceClientException extends ServiceException implements IBaseException {

  public static final String UNAVAILABLE_PORT="webservices_3600";
  public static final String INVALID_PORT="webservices_3601";
  public static final String SLD_CONNECTION_FAIL = "webservices_3602";
  public static final String SLD_MISSING_ENTRY = "webservices_3603";
  public static final String SLD_WSDL_DOWNLOAD_ERR = "webservices_3604";
  public static final String SLD_UPDATE_ERR = "webservices_3605";
  public static final String STUB_INSTANTIATION_ERR = "webservices_3606";
  public static final String NO_PORT_AVAILABLE = "webservices_3607";
  public static final String NO_TEMPORARY_DIRECTORY ="webservices_3608";
  public static final String WS_CREATE_ERROR = "webservices_3609";
  public static final String INCOMPATIBLE_INTERFACE = "webservices_3611";
  public static final String UNLOADABLE_RESOURCE = "webservices_3612";
  public static final String PROTOCOL_INIT_FAILURE = "webservices_3613";
  
  private static final Location LOC = Location.getLocation(WebserviceClientException.class);  

  private BaseExceptionInfo info;

  public WebserviceClientException(String patternKey) {
    super(patternKey);
    info = new BaseExceptionInfo(LOC,new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey), this);
  }

  public WebserviceClientException(String patternKey, Throwable cause) {
    super(patternKey);
    info = new BaseExceptionInfo(LOC,new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey), this, cause);
  }

  public WebserviceClientException(String patternKey,String arg1) {
    super(patternKey);
    info = new BaseExceptionInfo(LOC,new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey, new Object[] {arg1}), this);
  }


  public WebserviceClientException(String patternKey, Throwable cause ,String arg1) {
    super(patternKey);
    info = new BaseExceptionInfo(LOC,new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey, new Object[] {arg1}), this, cause);
  }

  public WebserviceClientException(String patternKey, String arg1, String arg2) {
    super(patternKey);
    info = new BaseExceptionInfo(LOC,new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey, new Object[] {arg1,arg2}), this);
  }

  public WebserviceClientException(String patternKey, Throwable cause, String arg1, String arg2) {
    super(patternKey);
    info = new BaseExceptionInfo(LOC,new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey, new Object[] {arg1,arg2}), this, cause);
  }

  public WebserviceClientException(String patternKey, Throwable cause, String arg1, String arg2, String arg3) {
    super(patternKey);
    info = new BaseExceptionInfo(LOC,new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey, new Object[] {arg1,arg2,arg3}), this, cause);
  }

  public WebserviceClientException(String patternKey, String arg1, String arg2, String arg3) {
    super(patternKey);
    info = new BaseExceptionInfo(LOC,new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey, new Object[] {arg1,arg2,arg3}), this);
  }

  public WebserviceClientException(String patternKey, Throwable cause, String arg1, String arg2, String arg3,String arg4, String arg5) {
    super(patternKey);
    info = new BaseExceptionInfo(LOC,new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey, new Object[] {arg1,arg2,arg3,arg4,arg5}), this, cause);
  }

  public WebserviceClientException(String patternKey, String arg1, String arg2, String arg3,String arg4, String arg5) {
    super(patternKey);
    info = new BaseExceptionInfo(LOC,new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey, new Object[] {arg1,arg2,arg3,arg4,arg5}), this);
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
    info.setLogSettings(cat, severity, loc);
  }

  public void log() {
    info.log();
  }

}
