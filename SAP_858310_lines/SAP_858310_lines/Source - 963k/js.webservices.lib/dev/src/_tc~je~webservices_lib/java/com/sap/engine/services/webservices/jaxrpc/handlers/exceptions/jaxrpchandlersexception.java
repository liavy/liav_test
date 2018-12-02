/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.jaxrpc.handlers.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.TimeZone;

import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.exception.BaseExceptionInfo;
import com.sap.exception.IBaseException;
import com.sap.localization.LocalizableText;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-3-12
 */
public class JAXRPCHandlersException extends ProtocolException implements IBaseException {
  
  public static final String ELEMENT_NOT_FOUND  =  "webservices_2001";
  public static final String MISSING_TEXT_DATA  =  "webservices_2002";  
  public static final String PREFIX_NOT_MAPPED  =  "webservices_2003";
  public static final String MISSING_CFG_PROPERTY  =  "webservices_2004";
  public static final String UNABLE_TO_LOAD_CLASS  =  "webservices_2005";
  public static final String UNABLE_TO_INSTANTIATE_CLASS  =  "webservices_2006";
  public static final String MISSING_CONFIGURAION  =  "webservices_2007";
  public static final String READER_NOT_ON_STARTELEMENT  =  "webservices_2008";
  
  private static final Location LOC = Location.getLocation(JAXRPCHandlersException.class);
  
  private BaseExceptionInfo exceptionInfo = null; 
  
  public JAXRPCHandlersException() {
    exceptionInfo = new BaseExceptionInfo(LOC, this);
  }

  public JAXRPCHandlersException(Throwable rootCause) {
    exceptionInfo = new BaseExceptionInfo(LOC, this, rootCause);
  }

  public JAXRPCHandlersException(String pattern) {
    exceptionInfo = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(ResourceAccessor.getResourceAccessor(), pattern), this);
  }

  public JAXRPCHandlersException(String pattern, Object[] obj) {
    exceptionInfo = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(ResourceAccessor.getResourceAccessor(), pattern,  obj), this);
  }


  public JAXRPCHandlersException(String pattern, Object[] obj, Throwable cause) {
    exceptionInfo = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(ResourceAccessor.getResourceAccessor(), pattern,  obj), this, cause);
  }

  public JAXRPCHandlersException(String pattern, Throwable cause) {
    exceptionInfo = new BaseExceptionInfo(LOC, new LocalizableTextFormatter(ResourceAccessor.getResourceAccessor(), pattern), this, cause);
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
    
  public void printStackTrace(java.io.PrintStream s) {
    exceptionInfo.printStackTrace(s);
  }

  public void printStackTrace(java.io.PrintWriter s) {
    exceptionInfo.printStackTrace(s);
  }

  public void setLogSettings(Category cat, int severity, Location loc) {
//    exceptionInfo.setLogSettings(cat, severity, loc);
  }

  public void log() {
//    exceptionInfo.log();
  }

  
}
