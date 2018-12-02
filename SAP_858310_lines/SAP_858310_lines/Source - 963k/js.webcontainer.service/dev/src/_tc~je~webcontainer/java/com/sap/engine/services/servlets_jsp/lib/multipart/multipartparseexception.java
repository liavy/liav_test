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
package com.sap.engine.services.servlets_jsp.lib.multipart;

import java.util.*;
import java.io.*;
import com.sap.exception.BaseExceptionInfo;
import com.sap.exception.IBaseException;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.localization.LocalizableText;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebResourceAccessor;

/**
 * Throwing an instance of this exception indicates problems in parsing multipart http messages.
 * The reason could be missing boundaries, headers, etc. in the multipart message or its submessages
 * that impedes correct parsing the message.
 *
 * @author Maria Jurova
 * @version 6.30
 */
public class MultipartParseException extends Exception implements IBaseException {
  /**
   * No such MultipartMessage part.
   */
  public static final String NO_SUCH_MULTIPART_PART = "servlet_jsp_0460";
  /**
   * Content-Type header not found or has no value. Multipart body requires a Content-Type header.
   */
  public static final String CONTENT_TYPE_HEADER_NOT_FOUND = "servlet_jsp_0461";
  /**
   * Incorrect Content-Type header. Parameter boundary not found.
   */
  public static final String BOUNDARY_NOT_FOUND = "servlet_jsp_0462";
  /**
   * Start boundary not found.
   */
  public static final String START_BOUNDARY_NOT_FOUND = "servlet_jsp_0463";
  /**
   * Incorrect request header: [{0}]. Header value not specified.
   */
  public static final String HEADER_VALUE_NOT_FOUND = "servlet_jsp_0464";
  /**
   * Final boundary not found.
   */
  public static final String FINAL_BOUNDARY_NOT_FOUND = "servlet_jsp_0465";

  private BaseExceptionInfo exceptionInfo = null;

  /**
   * Creates an instance of this exception with a specified error message.
   *
   * @param   msg     The message of the exception
   */
  public MultipartParseException(String msg) {
    super();
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, null);
  }

  /**
   * Creates an instance of this exception with a specified error message.
   *
   * @param   msg         The message of the exception
   * @param   parameters  Exception message parameters that will be inserted into its message
   */
  public MultipartParseException(String msg, Object [] parameters) {
    super();
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg, parameters);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, null);
  }

  /**
   * Creates an instance of this exception with a specified error message and linked exception.
   *
   * @param   msg         The message of the exception
   * @param   linkedException  The exception linked with this one
   */
  public MultipartParseException(String msg, Throwable linkedException) {
    super();
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, linkedException);
  }

  /**
   * Creates an instance of this exception with a specified error message and linked exception.
   *
   * @param   msg         The message of the exception
   * @param   parameters  Exception message parameters that will be inserted into its message
   * @param   linkedException  The exception linked with this one
   */
  public MultipartParseException(String msg, Object [] parameters, Throwable linkedException) {
    super();
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg, parameters);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, linkedException);
  }

  /**
   * Initializes the <i>cause</i> of this exception to the specified value.
   * (The cause is the throwable that caused this throwable to get thrown.)
   * @param  cause the cause
   * @return  a reference to this <code>Throwable</code> instance.
   */ 
  public Throwable initCause(Throwable cause) {
    return exceptionInfo.initCause(cause);
  }

  /**
   * Returns the cause of the error of this exception.
   *
   * @return   The cause exception
   */
  public Throwable getCause() {
    return exceptionInfo.getCause();
  }

  /**
   * Gets localizable message.
   * 
   * @return localizable message or null
   */
  public LocalizableText getLocalizableMessage() {
    return exceptionInfo.getLocalizableMessage();
  }

  /**
   * Gets localized message.
   * <p>The default locale and default time zone are used for localization. 
   * <p>These values have no effect if the <tt>finallyLocalize</tt> method 
   * has already been called.
   *
   * @return message string or null
   */
  public String getLocalizedMessage() {
    return exceptionInfo.getLocalizedMessage();
  }

  /**
   * Gets localized message.
   * <p>The specified locale and the default time zone are
   * used for localization. The default locale will be used 
   * if the locale parameter is null.
   * <p>These values have no effect if the <tt>finallyLocalize</tt>
   * method has been already called. 
   * 
   * @param loc locale
   * @return message string or null
  */
  public String getLocalizedMessage(Locale loc) {
    return exceptionInfo.getLocalizedMessage(loc);
  }

  /**
   * Gets localized message.
   * <p>The specified time zone and the default locale are
   * used for localization. The default time zone will be
   * used, if the time zone parameter is null.
   * <p>These values have no effect if the <tt>finallyLocalize</tt>
   * method has been already called. 
   * 
   * @param timeZone time zone
   * @return message string or null
   */
  public String getLocalizedMessage(TimeZone timeZone) {
    return exceptionInfo.getLocalizedMessage(timeZone);
  }

  /**
   * Gets localized message.
   * <p>The specified time zone and locale are
   * used for localization. The default time zone and the
   * default locale will be used, if the time zone parameter
   * or the locale are null respectively.
   * <p>These values have no effect if the <tt>finallyLocalize</tt>
   * method has been already called. 
   * 
   * @param loc locale
   * @param timeZone time zone
   * @return message string or null
   */
  public String getLocalizedMessage(Locale loc, TimeZone timeZone) {
    return exceptionInfo.getLocalizedMessage(loc, timeZone);
  }

  /**
   * Chains localized messages of the nested exceptions.
   * <p>The default locale and the default time zone are
   * used for localization.
   * <p>These values have no effect if the <tt>finallyLocalize</tt> method 
   * has already been called.
   * 
   * @param loc locale
   * @return message string or null
   */
  public String getNestedLocalizedMessage() {
    return exceptionInfo.getNestedLocalizedMessage();
  }

  /**
   * Chains localized messages of the nested exceptions.
   * <p>The specified locale and the default time zone are
   * used for localization. The default locale will be used 
   * if the Locale parameter is null.
   * <p>These values have no effect if the <tt>finallyLocalize</tt>
   * method has been already called. 
   * 
   * @param loc locale
   * @return message string or null
   */
  public String getNestedLocalizedMessage(Locale loc) {
    return exceptionInfo.getNestedLocalizedMessage(loc);
  }

  /**
   * Chains localized message of the nested exceptions.
   * <p>The specified time zone and the default locale are
   * used for localization. The default time zone will be
   * used, if the time zone parameter is null.
   * <p>These values have no effect if the <tt>finallyLocalize</tt>
   * method has been already called. 
   * <p> If the localized message is not specified, the 
   * non-localizable message is returned (if specified).   
   * 
   * @param timeZone time zone
   * @return message string or null
   */
  public String getNestedLocalizedMessage(TimeZone timeZone) {
    return exceptionInfo.getNestedLocalizedMessage(timeZone);
  }

  /**
   * Chains localized message of the nested exceptions.
   * <p>The specified time zone and locale are
   * used for localization. The default time zone and the
   * default locale will be used, if the time zone parameter 
   * or the locale parameter are null respectively.
   * <p>These values have no effect if the <tt>finallyLocalize</tt>
   * method has been already called. 
   * 
   * @param loc locale
   * @param timeZone time zone
   * @return message string
  */
  public String getNestedLocalizedMessage(Locale loc, TimeZone timeZone) {
    return exceptionInfo.getNestedLocalizedMessage(loc, timeZone);
  }

  /**
   * Finally localizes the <code>LocalizableText</code> message 
   * (if there is one attached).
   * <p>That means no further localization process can be performed
   * on that object. If there is a nested exception implementing
   * <code>IBaseException</code>, it will be localized recursively.
   * <p> The default locale and the default time zone are
   * used for localization.
   */
  public void finallyLocalize() {
    exceptionInfo.finallyLocalize();
  }

  /**
   * Finally localizes the <code>LocalizableText</code> message 
   * (if there is one attached).
   * <p>That means no further localization process can be performed
   * on that object. If there is a nested exception implementing
   * <code>IBaseException</code>, it will be localized recursively.
   * <p> The specified locale and the default time zone are used
   * for localization. If the locale parameter is null,
   * the default locale will be used.
   * 
   * @param loc locale
   */
  public void finallyLocalize(Locale loc) {
    exceptionInfo.finallyLocalize(loc);
  }

  /**
   * Finally localizes the <code>LocalizableText</code> message 
   * (if there is one attached).
   * <p>That means no further localization process can be performed
   * on that object. If there is a nested exception implementing
   * <code>IBaseException</code>, it will be localized recursively.
   * <p> The specified time zone and the default locale are
   * used for localization. If time zone parameter is null,
   * the default time zone will be used.
   * 
   * @param timeZone time zone
   */
  public void finallyLocalize(TimeZone timeZone) {
    exceptionInfo.finallyLocalize(timeZone);
  }

  /**
   * Finally localizes the <code>LocalizableText</code> message 
   * (if there is one attached).
   * <p>That means no further localization process can be performed
   * on that object.If there is a nested exception implementing 
   * <code>IBaseException</code>, it will be localized recursively.
   * <p> The specified locale and the specified time zone are
   * used for localization. If the time zone parameter or
   * the locale parameter are null, the default values
   * will be used respectively.
   * 
   * @param loc locale
   * @param timeZone time zone
   */
  public void finallyLocalize(Locale loc, TimeZone timeZone) {
    exceptionInfo.finallyLocalize(loc, timeZone);
  }

  /**
   * Gets the stack information of this exception 
   * in respect of the current system environment.
   * 
   * @return the stack trace as a string in respect of the
   *         current system
   */
  public String getSystemStackTraceString() {
    StringWriter s = new StringWriter();
    super.printStackTrace(new PrintWriter(s));
    return s.toString();
  }

  /**
   * Gets stack trace information of this exception only.
   * <p>The stack traces of nested exceptions are not chained.
   * 
   * @return the stack trace as a string without information of chained 
   *          exceptions.
   */
  public String getStackTraceString() {
    return exceptionInfo.getStackTraceString();
  }

  /**
   * Chains the stack trace information of nested exceptions.
   * <p>The caused stack trace is displayed first.
   * 
   * @return the stack trace as a string
   */
  public String getNestedStackTraceString() {
    return exceptionInfo.getNestedStackTraceString();
  }

  /**
   * Prints this exception and its backtrace to the 
   * standard error stream. This method prints a stack trace for this 
   * exception object on the error output stream that is 
   * the value of the field <code>System.err</code>.
   */
  public void printStackTrace() {
    exceptionInfo.printStackTrace();
  }

  /**
   * Prints this exception and its backtrace to the 
   * specified print stream.
   *
   * @param s <code>PrintStream</code> to use for output
   */
  public void printStackTrace(PrintStream s) {
    exceptionInfo.printStackTrace(s);
  }

  /**
   * Prints the <code>action</code> object and its backtrace to 
   * the specified print writer.
   *
   * @param s <code>PrintWriter</code> to use for output
   */
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
    return new Exception(stringWriter.toString());
  }

}
