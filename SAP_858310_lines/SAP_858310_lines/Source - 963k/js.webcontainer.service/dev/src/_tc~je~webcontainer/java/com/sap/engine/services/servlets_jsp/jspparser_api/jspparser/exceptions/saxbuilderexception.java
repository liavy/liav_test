/*
 * Created on 2004-7-5
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.exceptions;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.TimeZone;

import org.xml.sax.SAXException;

import com.sap.engine.services.servlets_jsp.jspparser_api.exception.WebResourceAccessor;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Position;
import com.sap.exception.BaseExceptionInfo;
import com.sap.exception.IBaseException;
import com.sap.localization.LocalizableText;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.localization.LocalizationException;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * @author ivo-s
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SAXBuilderException extends SAXException implements IBaseException {


  private BaseExceptionInfo exceptionInfo = null;
  private String fileName = null;
  private Position pos = null;

  public SAXBuilderException(Throwable linkedException) {
    super(linkedException.getMessage());
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, linkedException);
  }


  public SAXBuilderException(String msg) {
    super(msg);
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, null);
  }

  public SAXBuilderException(String msg, Object [] parameters) {
    super(msg);
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg, parameters);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, null);
  }

  public SAXBuilderException(String msg, Throwable linkedException) {
    super(linkedException.getMessage());
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, linkedException);
  }

  public SAXBuilderException(String msg, Object [] parameters, Throwable linkedException) {
    super(linkedException.getMessage());
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg, parameters);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, linkedException);
  }

  /**
   *Constructs a new ParseException exception.
   *
   * @param   msg  message of the exception
   * @param   pos  the position where the error occured
   */
  public SAXBuilderException(String msg, Position pos, String fileName) {
    this(msg);
    this.pos = pos;
    this.fileName = fileName;
  }

  /**
   *Constructs a new ParseException exception.
   *
   * @param   msg  message of the exception
   * @param   parameters  parameters of exception message
   * @param   pos  the position where the error occured
   */
  public SAXBuilderException(String msg, Object [] parameters, Position pos, String fileName) {
    this(msg, parameters);
    this.pos = pos;
    this.fileName = fileName;
  }

  /**
   *Constructs a new ParseException exception.
   *
   * @param   msg  message of the exception
   * @param   parameters  parameters of exception message
   * @param   pos  the position where the error occured
   * @param   linkedException  root cause of this exception
   */
  public SAXBuilderException(String msg, Object [] parameters, Position pos, String fileName, Throwable linkedException) {
    this(msg, parameters, linkedException);
    this.pos = pos;
    this.fileName = fileName;
  }

  public String getMessage() {
    return getLocalizedMessage();
  }

  public Throwable initCause(Throwable cause) {
    return exceptionInfo.initCause(cause);
  }

  public Throwable getCause() {
    return exceptionInfo.getCause();
  }

  public LocalizableText getLocalizableMessage() {
    return exceptionInfo.getLocalizableMessage();
  }

  public String getLocalizedMessage() {
    if (fileName == null || pos == null) {
      return exceptionInfo.getLocalizedMessage();
    } else {
      return exceptionInfo.getLocalizedMessage() + "\r\n" + formatPosition();
    }
  }

  public String getLocalizedMessage(Locale loc) {
    if (fileName == null || pos == null) {
      return exceptionInfo.getLocalizedMessage(loc);
    } else {
      return exceptionInfo.getLocalizedMessage(loc) + "\r\n" + formatPosition();
    }
  }

  public String getLocalizedMessage(TimeZone timeZone) {
    if (fileName == null || pos == null) {
      return exceptionInfo.getLocalizedMessage(timeZone);
    } else {
      return exceptionInfo.getLocalizedMessage(timeZone) + "\r\n" + formatPosition();
    }
  }

  public String getLocalizedMessage(Locale loc, TimeZone timeZone) {
    if (fileName == null || pos == null) {
      return exceptionInfo.getLocalizedMessage(loc, timeZone);
    } else {
      return exceptionInfo.getLocalizedMessage(loc, timeZone) + "\r\n" + formatPosition();
    }
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

  /**
   * Creates info string from position where the error has occured and localized it
   *
   * @return  formated info
   */
  private String formatPosition() {
    if (fileName == null || pos == null) {
      return "";
    }
    try {
      fileName = fileName.replace('\\', File.separatorChar);
      Object[] params = new Object[]{fileName, pos.getLine() + "",  pos.getLinePos() + ""};
      LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), "jsp_parser_0001", params);
      return formater.format();
    } catch (LocalizationException e) {
      return "";
    }
  }

  private Object writeReplace() {
    StringWriter stringWriter = new StringWriter();
    printStackTrace(new PrintWriter(stringWriter, true));
    return new SAXException(stringWriter.toString());
  }

}
