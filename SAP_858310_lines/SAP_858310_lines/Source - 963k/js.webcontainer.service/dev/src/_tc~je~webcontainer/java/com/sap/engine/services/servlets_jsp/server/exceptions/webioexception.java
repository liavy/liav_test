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
import com.sap.exception.BaseExceptionInfo;
import com.sap.exception.IBaseException;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.localization.LocalizableText;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 *
 * @author Violeta Uzunova
 * @version 6.30
 */
public class WebIOException extends IOException implements IBaseException {
  private BaseExceptionInfo exceptionInfo = null;

  public static final String Attempt_to_write_after_stream_is_closed = "servlet_jsp_0060";
  public static final String Error_invoking_the_error_page = "servlet_jsp_0061";
  public static final String Cannot_create_configuration = "servlet_jsp_0062";
  public static final String Cannot_write_in_database = "servlet_jsp_0063";
  public static final String Cannot_read_from_database = "servlet_jsp_0064";
  public static final String Error_while_parsing_jsp = "servlet_jsp_0065";
  public static final String CANNOT_RENAME_WAR_FILE = "servlet_jsp_0066";
  public static final String CANNOT_FIND_FILE_IN_WAR_FILE = "servlet_jsp_0067";
  public static final String STREAM_IS_CLOSED = "servlet_jsp_0068";
  public static final String CANNOT_DELETE_FILE = "servlet_jsp_0069";
  public static final String Cannot_create_configuration_CHF_NOT_FOUND = "servlet_jsp_0070";
  public static final String CANNOT_OPEN_CONFIGURATION_FOR_UPLOAD = "servlet_jsp_0071";
  public static final String CANNOT_CLOSE_CONFIGURATION_FOR_UPLOAD = "servlet_jsp_0072";
  public static final String ERROR_FINDING_FILE_ENTRY = "servlet_jsp_0073";
  public static final String ERROR_MODIFYING_ENTRY = "servlet_jsp_0074";
  public static final String CANNOT_GENERATE_FILE_HASH = "servlet_jsp_0075";
  public static final String CANNOT_COMMIT_UPLOAD = "servlet_jsp_0076";
  public static final String CANNOT_CLOSE_CONFIGURATION = "servlet_jsp_0077";
  public static final String CANNOT_START_RUNTIME_CHANGES = "servlet_jsp_0078";
  public static final String CANNOT_LOCK_CONFIGURATION = "servlet_jsp_0079";
  public static final String UNEXCPECTED_ERROR_IN_UPLOAD = "servlet_jsp_0080";
  public static final String CANNOT_UNLOCK_CONFIGURATION = "servlet_jsp_0081";
  public static final String CANNOT_ROLLBACK_RUNTIME_CHANGES = "servlet_jsp_0082";
  public static final String CANNOT_MAKE_RUNTIME_CHANGES = "servlet_jsp_0083";
  public static final String CANNOT_LOCK_CONFIGURATION_JSP_CLASSES = "servlet_jsp_0084";
  public static final String CANNOT_UNLOCK_CONFIGURATION_JSP_CLASSES = "servlet_jsp_0085";
  public static final String CONNECTION_IS_CLOSED = "servlet_jsp_0086";
  public static final String CANNOT_MAKE_DIRS = "servlet_jsp_0087";

  public WebIOException(String msg) {
    super();
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, null);
  }

  public WebIOException(String msg, Object [] parameters) {
    super();
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg, parameters);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, null);
  }

  public WebIOException(String msg, Throwable linkedException) {
    super();
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, linkedException);
  }

  public WebIOException(String msg, Object [] parameters, Throwable linkedException) {
    super();
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg, parameters);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, linkedException);
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

  private Object writeReplace(){
    StringWriter stringWriter = new StringWriter();
    printStackTrace(new PrintWriter(stringWriter, true));
    return new IOException(stringWriter.toString());
  }

}
