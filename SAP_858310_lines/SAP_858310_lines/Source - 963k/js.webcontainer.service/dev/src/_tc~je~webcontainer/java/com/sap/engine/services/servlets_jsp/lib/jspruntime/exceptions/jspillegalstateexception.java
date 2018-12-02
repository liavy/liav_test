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
package com.sap.engine.services.servlets_jsp.lib.jspruntime.exceptions;

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

public class JspIllegalStateException extends IllegalStateException implements IBaseException {
  public static final String PAGE_NEEDS_SESSION_AND_NONE_IS_AVAILABLE = "jsp_runtime_0009";
  public static final String FAILED_INITILIZE_JSP_WRITER = "jsp_runtime_0010";
  public static final String ADD_RESOLVER_CALLED_AFTER_FIRST_REQUEST = "jsp_runtime_0019";
  public static final String ATTEMP_FORWARD_NO_BUFFER = "jsp_runtime_0020";
  
  private BaseExceptionInfo exceptionInfo = null;

  public JspIllegalStateException(String msg) {
    super();
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, null);
  }

  public JspIllegalStateException(String msg, Throwable linkedException) {
    super();
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, linkedException);
  }

  public JspIllegalStateException(String msg, Object [] parameters) {
    super();
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg, parameters);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, null);
  }

  public JspIllegalStateException(String msg, Object [] parameters, Throwable linkedException) {
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
