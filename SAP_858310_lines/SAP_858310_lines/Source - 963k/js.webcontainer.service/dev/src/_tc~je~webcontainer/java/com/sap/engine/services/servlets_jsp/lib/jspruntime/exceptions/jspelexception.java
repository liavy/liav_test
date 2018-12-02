/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.lib.jspruntime.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.jsp.el.ELException;

import com.sap.exception.BaseExceptionInfo;
import com.sap.exception.IBaseException;
import com.sap.localization.LocalizableText;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * @author Todor Mollov
 *
 * @version 7.0
 */
public class JspELException extends ELException implements IBaseException 
{
	public static final String COMMON_EL_EVALUATE_PROBLEM = "jsp_runtime_0017";
	public static final String COMMON_EL_EVALUATE_ERROR_MESSAGE = "jsp_runtime_0018";
  
	private BaseExceptionInfo exceptionInfo = null;
	
	public JspELException(String msg) {
		super();
		LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg);
		exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, null);
	}

	public JspELException(String msg, Throwable linkedException) {
		super();
		LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg);
		exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, linkedException);
	}

	public JspELException(String msg, Object [] parameters) {
		super();
		LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg, parameters);
		exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, null);
	}

	public JspELException(String msg, Object [] parameters, Throwable linkedException) {
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
