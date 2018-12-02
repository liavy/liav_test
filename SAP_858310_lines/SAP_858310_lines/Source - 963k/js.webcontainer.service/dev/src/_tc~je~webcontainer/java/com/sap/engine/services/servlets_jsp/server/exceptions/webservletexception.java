/*
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
import com.sap.exception.IBaseException;
import com.sap.exception.BaseExceptionInfo;
import com.sap.localization.LocalizableText;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

import javax.servlet.ServletException;

/**
 *
 * @author Violeta Uzunova
 * @version 6.30
 */
public class WebServletException extends ServletException implements IBaseException {
  public static final String Error_starting_servlet_as_privileged_action_at_filter_chain = "servlet_jsp_0200";
  public static final String Cannot_find_servlet_instance_for_path = "servlet_jsp_0201";
  public static final String Error_occured_while_servlet_is_started_with_run_as_identity = "servlet_jsp_0202";
  public static final String ROOT_DIRECTORY_FOR_PUT_NOT_FOUND = "servlet_jsp_0203";
  public static final String ERROR_OCCURED_WHILE_SERVLET_IS_INIT_WITH_RUN_AS_IDENTITY = "servlet_jsp_0204";
  public static final String PUT_NOT_ALLOWED = "servlet_jsp_0205";
  public static final String WEB_INF_NOT_ACCESSIBLE = "servlet_jsp_0206";
  public static final String FILE_OUTSIDE_ROOT_DIR = "servlet_jsp_0207";
  public static final String ERROR_IN_INCLUDED_SERVLET = "servlet_jsp_0208";
  public static final String Method_invocation_error = "servlet_jsp_0209";
  public static final String CLASS_CAST_EXCEPTION = "servlet_jsp_0210";
  public static final String RESOURCE_CANNOT_BE_INJECTED = "servlet_jsp_0211";
  public static final String CANNOT_PARCE_JSP_FOR_SERVLET = "servlet_jsp_0212";
  public static final String CLASS_CANNOT_BE_LOADED_ERROR_IS = "servlet_jsp_0213";
  public static final String CLASS_IS_NEITHER_SERVLER_NOR_WS_END_POINT = "servlet_jsp_0214";
  public static final String CANNOT_CHECK_CLASS_FOR_WS_END_POINT_ERROR_IS = "servlet_jsp_0215";
  
  private BaseExceptionInfo exceptionInfo = null;

  public WebServletException(String msg, Object [] parameters, Throwable linkedException) {
    super(msg, linkedException);
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg, parameters);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, linkedException);
  }

  public WebServletException(String msg, Throwable linkedException) {
    super(msg, linkedException);
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, linkedException);
  }

  public WebServletException(String msg, Object [] parameters) {
    super(msg);
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg, parameters);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, null);
  }

  public WebServletException(String msg) {
    super(msg);
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, null);
  }

  public Throwable initCause(Throwable cause) {
    return exceptionInfo.initCause(cause);
  }

  public Throwable getCause() {
    if (getRootCause() != null) {
      return getRootCause();
    } else {
      return exceptionInfo.getCause();
    }
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
    return new ServletException(stringWriter.toString());
  }

}