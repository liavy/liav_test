/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.configuration;

import com.sap.exception.BaseException;
import com.sap.exception.BaseExceptionInfo;
import com.sap.localization.LocalizableText;
import com.sap.localization.ResourceAccessor;
import com.sap.localization.LocalizableText.Msg;
import com.sap.tc.logging.Location;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-12-13
 */
public class ConfigurationMarshallerException extends BaseException {
  
  private static final Location LOC = Location.getLocation(ConfigurationMarshallerException.class);
  
	public ConfigurationMarshallerException() {
		this(LOC);
	}

	public ConfigurationMarshallerException(Location loc) {
		super(loc);
	}

	public ConfigurationMarshallerException(BaseExceptionInfo info) {
		super(info);
	}

	public ConfigurationMarshallerException(Throwable rootCause) {
		this(LOC, rootCause);
	}

	public ConfigurationMarshallerException(Location loc, Throwable rootCause) {
		super(loc, rootCause);
	}

	public ConfigurationMarshallerException(LocalizableText locMessage) {
		super(LOC, locMessage);
	}

	public ConfigurationMarshallerException(Location loc, LocalizableText locMessage) {
		super(loc, locMessage);
	}

	public ConfigurationMarshallerException(
		LocalizableText locMessage,
		Throwable rootCause) {
		super(LOC, locMessage, rootCause);
	}

	public ConfigurationMarshallerException(
		Location loc,
		LocalizableText locMessage,
		Throwable rootCause) {
		super(loc, locMessage, rootCause);
	}

	public ConfigurationMarshallerException(
		ResourceAccessor accessor,
		String patternKey) {
		super(LOC, accessor, patternKey);
	}

	public ConfigurationMarshallerException(
		Location loc,
		ResourceAccessor accessor,
		String patternKey) {
		super(loc, accessor, patternKey);
	}

	public ConfigurationMarshallerException(
		ResourceAccessor accessor,
		String patternKey,
		Throwable rootCause) {
		super(LOC, accessor, patternKey, rootCause);
	}

	public ConfigurationMarshallerException(
		Location loc,
		ResourceAccessor accessor,
		String patternKey,
		Throwable rootCause) {
		super(loc, accessor, patternKey, rootCause);
	}

	public ConfigurationMarshallerException(
		ResourceAccessor accessor,
		String patternKey,
		Object[] parameters) {
		super(LOC, accessor, patternKey, parameters);
	}

	public ConfigurationMarshallerException(
		Location loc,
		ResourceAccessor accessor,
		String patternKey,
		Object[] parameters) {
		super(loc, accessor, patternKey, parameters);
	}

	public ConfigurationMarshallerException(
		ResourceAccessor accessor,
		String patternKey,
		Object[] parameters,
		Throwable rootCause) {
		super(LOC, accessor, patternKey, parameters, rootCause);
	}

	public ConfigurationMarshallerException(
		Location loc,
		ResourceAccessor accessor,
		String patternKey,
		Object[] parameters,
		Throwable rootCause) {
		super(loc, accessor, patternKey, parameters, rootCause);
	}

	public ConfigurationMarshallerException(ResourceAccessor accessor, Msg msg) {
		super(LOC, accessor, msg);
	}

	public ConfigurationMarshallerException(
		Location loc,
		ResourceAccessor accessor,
		Msg msg) {
		super(loc, accessor, msg);
	}

	public ConfigurationMarshallerException(
		ResourceAccessor accessor,
		Msg msg,
		Throwable rootCause) {
		super(LOC, accessor, msg, rootCause);
	}

	public ConfigurationMarshallerException(
		Location loc,
		ResourceAccessor accessor,
		Msg msg,
		Throwable rootCause) {
		super(loc, accessor, msg, rootCause);
	}

}
