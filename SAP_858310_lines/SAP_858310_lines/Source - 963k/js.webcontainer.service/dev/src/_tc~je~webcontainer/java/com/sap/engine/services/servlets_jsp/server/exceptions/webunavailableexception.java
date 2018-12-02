﻿/*
 * Copyright (c) 2002 by SAP Labs Bulgaria AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.servlets_jsp.server.exceptions;

import java.util.*;
import java.io.*;
import com.sap.exception.BaseExceptionInfo;
import com.sap.exception.IBaseException;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.localization.LocalizableText;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

import javax.servlet.UnavailableException;

/**
 *
 * @author Violeta Uzunova
 * @version 6.30
 */
public class WebUnavailableException extends UnavailableException implements IBaseException {
  private BaseExceptionInfo exceptionInfo = null;

  public static final String Servlet_is_currently_unavailable = "servlet_jsp_0320";
  public static final String PUT_SERVLET_NOT_INITIALIZED = "servlet_jsp_0321"; 
  public static final String Filter_is_currently_unavailable = "servlet_jsp_0320";

  /**
   * Constructs a new UnavailableException exception.
   *
   * @param   msg  message of the exception
   * @param   parameters  parameters of the message of the exception
   * @param   linkedException  parameters of the message of the exception
   */
  public WebUnavailableException(String msg, Object [] parameters, Throwable linkedException) {
    super(msg);
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg, parameters);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, linkedException);
  }

  /**
   * Constructs a new UnavailableException exception.
   *
   * @param   msg  message of the exception
   * @param   parameters  parameters of the message of the exception
   */
  public WebUnavailableException(String msg, Object [] parameters) {
    super(msg);
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg, parameters);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, null);
  }

  /**
   * Constructs a new UnavailableException exception.
   *
   * @param   msg  message of the exception
   * @param   linkedException  parameters of the message of the exception
   */
  public WebUnavailableException(String msg, Throwable linkedException) {
    super(msg);
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, linkedException);
  }

  /**
   * Constructs a new UnavailableException exception.
   *
   * @param   msg  message of the exception
   */
  public WebUnavailableException(String msg) {
    super(msg);
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, null);
  }

  /**
   * Constructs a new UnavailableException exception.
   *
   * @param   msg  message of the exception
   * @param   millisec  message of the exception
   * @param   parameters  parameters of the message of the exception
   * @param   linkedException  parameters of the message of the exception
   */
  public WebUnavailableException(String msg, int millisec, Object [] parameters, Throwable linkedException) {
    super(msg, millisec);
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg, parameters);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, linkedException);
  }

  /**
   * Constructs a new UnavailableException exception.
   *
   * @param   msg  message of the exception
   * @param   millisec  message of the exception
   * @param   parameters  parameters of the message of the exception
   */
  public WebUnavailableException(String msg, int millisec, Object [] parameters) {
    super(msg, millisec);
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg, parameters);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, null);
  }

  /**
   * Constructs a new UnavailableException exception.
   *
   * @param   msg  message of the exception
   * @param   millisec  message of the exception
   * @param   linkedException  parameters of the message of the exception
   */
  public WebUnavailableException(String msg, int millisec, Throwable linkedException) {
    super(msg, millisec);
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, linkedException);
  }

  /**
   * Constructs a new UnavailableException exception.
   *
   * @param   msg  message of the exception
   * @param   millisec  message of the exception
   */
  public WebUnavailableException(String msg, int millisec) {
    super(msg, millisec);
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, null);
  }


  public Throwable initCause(Throwable cause) {
    return exceptionInfo.initCause(cause);
  }

  public Throwable getCause() {
    return exceptionInfo.getCause();
  }

  public String getMessage() {
    return getLocalizedMessage();
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
	 * @deprecated
	 */
  public void setLogSettings(Category cat, int severity, Location loc) {
    //exceptionInfo.setLogSettings(cat, severity, loc);
  }

  /**
	 * Logs the exception message.
	 * @deprecated
	 */
	public void log() {
		//exceptionInfo.log();
	}

  private Object writeReplace() {
    StringWriter stringWriter = new StringWriter();
    printStackTrace(new PrintWriter(stringWriter, true));
    return new UnavailableException(stringWriter.toString());
  }

}
