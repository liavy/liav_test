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
package com.sap.engine.services.webservices.jaxm.soap.accessor;

import com.sap.exception.IBaseException;
import com.sap.exception.BaseExceptionInfo;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.localization.LocalizableText;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

import javax.xml.soap.SOAPException;
import java.util.Locale;
import java.util.TimeZone;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.PrintStream;

/**
 *
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class NestedSOAPException extends SOAPException implements IBaseException {

  public static final String UNKNOWN_PREFIX = "webservices_3700";
  public static final String DUBLICATE_DETAIL = "webservices_3701";
  public static final String DUBLICATE_BODY = "webservices_3702";
  public static final String DUBLICATE_HEADER = "webservices_3703";
  public static final String GET_CONTENTPROBLEM = "webservices_3704";
  public static final String NON_SOAP_MESSAGE = "webservices_3705";
  public static final String SET_CONTENTPROBLEM = "webservices_3706";
  public static final String MIME_CONVERT_PROBLEM = "webservices_3707";
  public static final String CONNECTION_PROBLEM = "webservices_3708";
  public static final String IO_PROBLEM = "webservices_3709";
  public static final String MIME_PART_PROBLEM = "webservices_3710";
  public static final String SERVER_PROBLEM = "webservices_3711";
  public static final String INVALID_CONTENTTYPE = "webservices_3712";
  public static final String MIME_BUILD_PROBLEM = "webservices_3713";
  public static final String CONNECTION_CLOSED = "webservices_3714";
  public static final String CONNECTION_CLOSED_AGAIN = "webservices_3715";
  public static final String EMPTY_ATTACHMENT = "webservices_3716";
  public static final String EMPTY_DATAHANDLER = "webservices_3717";

  private BaseExceptionInfo info;
  
  private static final Location LOC = Location.getLocation(NestedSOAPException.class);
  
  public NestedSOAPException(String patternKey) {
    super(patternKey);
    info = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(SOAPAccessor.getResourceAccessor(), patternKey), this);
  }

  public NestedSOAPException(String patternKey, Throwable cause) {
    super(patternKey);
    info = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(SOAPAccessor.getResourceAccessor(), patternKey), this, cause);
  }

  public NestedSOAPException(String patternKey,Object arg1) {
    super(patternKey);
    info = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(SOAPAccessor.getResourceAccessor(), patternKey, new Object[] {arg1}), this);
  }

  public NestedSOAPException(String patternKey, Throwable cause ,Object arg1) {
    super(patternKey);
    info = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(SOAPAccessor.getResourceAccessor(), patternKey, new Object[] {arg1}), this, cause);
  }

  public NestedSOAPException(String patternKey, Object arg1, Object arg2) {
    super(patternKey);
    info = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(SOAPAccessor.getResourceAccessor(), patternKey, new Object[] {arg1,arg2}), this);
  }

  public NestedSOAPException(String patternKey, Throwable cause, Object arg1, Object arg2) {
    super(patternKey);
    info = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(SOAPAccessor.getResourceAccessor(), patternKey, new Object[] {arg1,arg2}), this, cause);
  }

  public NestedSOAPException(String patternKey, Throwable cause, Object arg1, Object arg2, Object arg3) {
    super(patternKey);
    info = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(SOAPAccessor.getResourceAccessor(), patternKey, new Object[] {arg1,arg2,arg3}), this, cause);
  }

  public NestedSOAPException(String patternKey, Object arg1, Object arg2, Object arg3) {
    super(patternKey);
    info = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(SOAPAccessor.getResourceAccessor(), patternKey, new Object[] {arg1,arg2,arg3}), this);
  }

  public NestedSOAPException(String patternKey, Object arg1, Object arg2, Object arg3,Object arg4) {
    super(patternKey);
    info = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(SOAPAccessor.getResourceAccessor(), patternKey, new Object[] {arg1,arg2,arg3,arg4}), this);
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
//    info.setLogSettings(cat, severity, loc);
  }

  public void log() {
//    info.log();
  }


}
