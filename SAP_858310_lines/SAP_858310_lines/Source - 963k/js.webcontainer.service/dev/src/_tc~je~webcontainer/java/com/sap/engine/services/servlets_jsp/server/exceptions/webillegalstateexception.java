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

/*
 *
 *
 * @author Violeta Uzunova
 * @version 6.30
 */

public class WebIllegalStateException extends IllegalStateException implements IBaseException {
  private BaseExceptionInfo exceptionInfo = null;

  public static String Method_called_on_invalid_session = "servlet_jsp_0170";
  public static String Session_already_invalidated = "servlet_jsp_0171";
  public static String Stream_is_already_taken_with_method = "servlet_jsp_0172";
  public static String Stream_is_already_commited = "servlet_jsp_0173";
  public static String Cannot_use_setBufferSize_if_anything_has_written_in_the_ServletOutputStream = "servlet_jsp_0174";
  public static String Error_while_closing_stream = "servlet_jsp_0175";
  public static String Output_is_commited = "servlet_jsp_0176";
  public static final String ILLEGAL_BUFFER_SIZE_VALUE = "servlet_jsp_0177";
  public static final String CANNOT_WRITE_TO_STREAM = "servlet_jsp_0178";
  public static final String CANNOT_FLUSH_STREAM = "servlet_jsp_0179";
  public static final String CANNOT_INVALIDATE_SESSION_APPP_CONTEXT_NOT_FOUND = "servlet_jsp_0157";
  public static final String ILLEGAL_URL_PATTERN_TYPE = "servlet_jsp_0180";
  public static final String APPLICATIONS_NAMES_EQUALS_BUT_VENDORS_NOT = "servlet_jsp_0181";
  public static final String GET_SESSION_METHOD_CALLED_ON_COMMITTED_STREAM = "servlet_jsp_0182";
  public static final String INVALID_REQUEST_OBJECT_USED = "servlet_jsp_0183";
  public static final String INVALID_REQUEST_CONTEXT = "servlet_jsp_0184";
  public static final String TIMEOUT_REQUEST_OBJECT_USED = "servlet_jsp_0185";

  public WebIllegalStateException(String msg) {
    super();
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, null);
  }

  public WebIllegalStateException(String msg, Throwable linkedException) {
    super();
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, linkedException);
  }

  public WebIllegalStateException(String msg, Object [] parameters) {
    super();
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg, parameters);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, null);
  }

  public WebIllegalStateException(String msg, Object [] parameters, Throwable linkedException) {
    super();
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg, parameters);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, linkedException);
  }

  public Throwable initCause(Throwable throwable) {
    return exceptionInfo.initCause(throwable);
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

  public String getLocalizedMessage(Locale locale) {
    return exceptionInfo.getLocalizedMessage(locale);
  }

  public String getLocalizedMessage(TimeZone timezone) {
    return exceptionInfo.getLocalizedMessage(timezone);
  }

  public String getLocalizedMessage(Locale locale, TimeZone timezone) {
    return exceptionInfo.getLocalizedMessage(locale, timezone);
  }

  public String getNestedLocalizedMessage() {
    return exceptionInfo.getNestedLocalizedMessage();
  }

  public String getNestedLocalizedMessage(Locale locale) {
    return exceptionInfo.getNestedLocalizedMessage(locale);
  }

  public String getNestedLocalizedMessage(TimeZone timezone) {
    return exceptionInfo.getNestedLocalizedMessage(timezone);
  }

  public String getNestedLocalizedMessage(Locale locale, TimeZone timezone) {
    return exceptionInfo.getNestedLocalizedMessage(locale, timezone);
  }

  public void finallyLocalize() {
    exceptionInfo.finallyLocalize();
  }

  public void finallyLocalize(Locale locale) {
    exceptionInfo.finallyLocalize(locale);
  }

  public void finallyLocalize(TimeZone timezone) {
    exceptionInfo.finallyLocalize(timezone);
  }

  public void finallyLocalize(Locale locale, TimeZone timezone) {
    exceptionInfo.finallyLocalize(locale, timezone);
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
    return new IllegalStateException(stringWriter.toString());
  }

}
