/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.userstore.exceptions;

import java.util.Locale;
import java.util.TimeZone;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.PrintStream;

import com.sap.exception.IBaseException;
import com.sap.exception.BaseExceptionInfo;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.localization.LocalizableText;

public class BaseSecurityException extends SecurityException implements IBaseException {
  public static final String TIMEOUT_WRITE_OPERATION = "userstore_0003";
  public static final String CANNOT_TRANSFER_USERSTORAGE = "userstore_0004";

  public static final String SET_TRANSACTION_ATTRIBUTE_ERROR = "userstore_0100";
  public static final String INIT_CONFIGURATION_PATH_ERROR = "userstore_0101";
  public static final String CANNOT_CREATE_ROOT_CONFIGURATION = "userstore_0102";
  public static final String CANNOT_GET_ROOT_CONFIGURATION = "userstore_0103";
  public static final String CANNOT_GET_CONFIGURATION = "userstore_0104";
  public static final String CANNOT_GET_LOCKED_CONFIGURATION = "userstore_0105";
  public static final String CANNOT_COMMIT = "userstore_0106";
  public static final String CANNOT_ROLLBACK = "userstore_0107";
  public static final String CANNOT_FORGET = "userstore_0108";
  public static final String CANNOT_GET_LOCKED_ROOT_CONFIGURATION = "userstore_0109";

  public static final String CANNOT_INITIALIZE_CONNECTOR_USERSTORE = "userstore_0200";
  public static final String UNEXPECTED_CONNECTOR_USERSTORE_EXCEPTION = "userstore_0201";

  public static final String CANNOT_LIST_GROUPS = "userstore_0500";
  public static final String CANNOT_LIST_GROUPS1 = "userstore_0501";
  public static final String CANNOT_LIST_GROUPS_OLD = "userstore_0502";
  public static final String CANNOT_GET_GROUP_INFO = "userstore_0503";
  public static final String CANNOT_GET_GROUP_INFO1 = "userstore_0504";
  public static final String CANNOT_GET_GROUP_INFO_OLD = "userstore_0505";
  public static final String CANNOT_CREATE_GROUP = "userstore_0506";
  public static final String CANNOT_DELETE_GROUP = "userstore_0507";
  public static final String CANNOT_DELETE_GROUP1 = "userstore_0508";
  public static final String CANNOT_DELETE_GROUP_OLD = "userstore_0509";
  public static final String CANNOT_ADD_GROUP_TO_PARENT = "userstore_0510";
  public static final String CANNOT_ADD_GROUP_TO_PARENT1 = "userstore_0511";
  public static final String CANNOT_ADD_GROUP_TO_PARENT_OLD = "userstore_0512";
  public static final String CANNOT_REMOVE_GROUP_FROM_PARENT = "userstore_0513";
  public static final String CANNOT_REMOVE_GROUP_FROM_PARENT1 = "userstore_0514";
  public static final String CANNOT_REMOVE_GROUP_FROM_PARENT_OLD = "userstore_0515";
  public static final String CANNOT_GET_CHILD_GROUPS = "userstore_0516";
  public static final String CANNOT_GET_CHILD_GROUPS1 = "userstore_0517";
  public static final String CANNOT_GET_CHILD_GROUPS_OLD = "userstore_0518";
  public static final String CANNOT_GET_GROUP_PARENT_GROUPS = "userstore_0519";
  public static final String CANNOT_GET_GROUP_PARENT_GROUPS1 = "userstore_0520";
  public static final String CANNOT_GET_GROUP_PARENT_GROUPS_OLD = "userstore_0521";
  public static final String CANNOT_ADD_USER_TO_GROUP = "userstore_0522";
  public static final String CANNOT_ADD_USER_TO_GROUP1 = "userstore_0523";
  public static final String CANNOT_ADD_USER_TO_GROUP_OLD = "userstore_0524";
  public static final String CANNOT_REMOVE_USER_FROM_GROUP = "userstore_0525";
  public static final String CANNOT_REMOVE_USER_FROM_GROUP1 = "userstore_0526";
  public static final String CANNOT_REMOVE_USER_FROM_GROUP_OLD = "userstore_0527";
  public static final String CANNOT_GET_USERS_OF_GROUP = "userstore_0528";
  public static final String CANNOT_GET_USERS_OF_GROUP1 = "userstore_0529";
  public static final String CANNOT_GET_USERS_OF_GROUP_OLD = "userstore_0530";
  public static final String CANNOT_LIST_ROOT_GROUPS = "userstore_0531";
  public static final String CANNOT_LIST_ROOT_GROUPS1 = "userstore_0532";
  public static final String CANNOT_LIST_ROOT_GROUPS_OLD = "userstore_0533";
  public static final String CANNOT_GET_GROUPS_OF_USER = "userstore_0534";
  public static final String CANNOT_GET_GROUPS_OF_USER1 = "userstore_0535";
  public static final String CANNOT_GET_GROUPS_OF_USER_OLD = "userstore_0536";
  public static final String CANNOT_DELETE_NOT_EMPTY_GROUP = "userstore_0537";
  public static final String CANNOT_CREATE_GROUP_OLD = "userstore_0538";

  public static final String CANNOT_STORE_INITIAL_GROUPS = "userstore_0550";
  public static final String CANNOT_INITIALIZE_GROUP_CONTEXT_SPI = "userstore_0551";


  public static final String CYCLE_FOUND = "userstore_0650";

  public static final String CANNOT_GET_PARENT_GROUPS = "userstore_0800";
  public static final String CANNOT_LIST_USERS = "userstore_0801";
  public static final String CANNOT_LIST_ROOT_USERS = "userstore_0802";
  public static final String CANNOT_GET_USER_INFO = "userstore_0803";
  public static final String USER_NOT_CORRESPONDING_TO_CERT = "userstore_0804";
  public static final String CANNOT_FIND_KEYSTORE_VIEW = "userstore_0805";
  public static final String UNACCEPTABLE_USER_NAME = "userstore_0806";
  public static final String CANNOT_CREATE_USER = "userstore_0807";
  public static final String CANNOT_DELETE_USER_INFO = "userstore_0808";
  public static final String CANNOT_LOAD_FILTER_USERNAME = "userstore_0809";
  public static final String CANNOT_STORE_FILTER_USERNAME = "userstore_0810";
  public static final String CANNOT_LOAD_FILTER_PASSWORD = "userstore_0811";
  public static final String CANNOT_STORE_FILTER_PASSWORD = "userstore_0812";
  public static final String NOT_SUPPORTED = "userstore_0813";
  public static final String NOT_SUPPORTED1 = "userstore_0814";
  public static final String CANNOT_MODIFY_USER_INFO = "userstore_0815";
  public static final String CANNOT_DECODE = "userstore_0816";
  public static final String CANNOT_LOCK_USER = "userstore_0817";

  public static final String CANNOT_STORE_INITIAL_USERS = "userstore_0820";
  public static final String CANNOT_INITIALIZE_USER_CONTEXT_SPI = "userstore_0821";

  public static final String CANNOT_GET_USER_CERT = "userstore_0900";
  public static final String INCORRECT_PASSWORD = "userstore_0901";
  public static final String UNACCEPTABLE_PASSWORD = "userstore_0902";
  public static final String CANNOT_FILTER_PASSWORD = "userstore_0903";
  public static final String CANNOT_FILTER_PASSWORD1 = "userstore_0904";
  public static final String CERT_BELONGS_TO_ANOTHER_USER = "userstore_0905";
  public static final String CANNOT_ASIGN_CERT = "userstore_0906";
  public static final String CANNOT_ASIGN_CERT1 = "userstore_0907";
  public static final String PROPERTY_MUST_BE_CHARARR = "userstore_0908";
  public static final String CANNOT_WRITE_PROP_PASSWORD = "userstore_0909";
  public static final String CANNOT_WRITE_PROP_PASSWORD1 = "userstore_0910";
  public static final String PROPERTY_MUST_BE_LONG_OR_STRING = "userstore_0911";
  public static final String PROPERTY_MUST_BE_POSITIVE = "userstore_0912";
  public static final String PROPERTY_NOT_SUPPORTED = "userstore_0917";
  public static final String PROPERTY_NOT_SUPPORTED1 = "userstore_0918";

  public static final String CANNOT_SET_NEW_PASSWORD = "userstore_0930";
  public static final String CANNOT_WRITE_USER_PROPERTY = "userstore_0931";

  public static final String CANNOT_CREATE_PERSISTENT_USER = "userstore_0950";
  public static final String CANNOT_MODIFY_PERSISTENT_USER = "userstore_0951";
  public static final String CANNOT_REMOVE_PERSISTENT_USER = "userstore_0952";
  public static final String CANNOT_READ_PERSISTENT_USER = "userstore_0953";

  public static final String PASSWORD_IS_DISABLED = "userstore_0960";

  public static final String GET_FILTER_USERNAME_ERROR = "userstore_1001";
  public static final String GET_FILTER_PASSWORD_ERROR = "userstore_1002";
  public static final String SET_FILTER_PASSWORD_ERROR = "userstore_1003";
  public static final String SET_FILTER_USERNAME_ERROR = "userstore_1004";
  public static final String NO_SEARCH_CRITERIA_IS_SET = "userstore_2000";
  public static final String ANONYMOUS_USER_CORRUPT = "userstore_2001";

  private BaseExceptionInfo info = null;

  public BaseSecurityException(String key) {
    super(key);
    info = new BaseExceptionInfo(UserstoreResourceAccessor.category,
                                 Severity.ERROR,
                                 UserstoreResourceAccessor.location,
                                 new LocalizableTextFormatter(UserstoreResourceAccessor.getResourceAccessor(), key, null),
                                 this,
                                 null);
  }

  public BaseSecurityException(String key, int severity) {
    super(key);
    info = new BaseExceptionInfo(UserstoreResourceAccessor.category,
                                 severity,
                                 UserstoreResourceAccessor.location,
                                 new LocalizableTextFormatter(UserstoreResourceAccessor.getResourceAccessor(), key, null),
                                 this,
                                 null);
  }

  public BaseSecurityException(String key, Object[] args) {
    super(key);
    info = new BaseExceptionInfo(UserstoreResourceAccessor.category,
                                 Severity.ERROR,
                                 UserstoreResourceAccessor.location,
                                 new LocalizableTextFormatter(UserstoreResourceAccessor.getResourceAccessor(), key, args),
                                 this,
                                 null);
  }


  public BaseSecurityException(String key, Throwable linkedException) {
    super(key);
    info = new BaseExceptionInfo(UserstoreResourceAccessor.category,
                                 Severity.ERROR,
                                 UserstoreResourceAccessor.location,
                                 new LocalizableTextFormatter(UserstoreResourceAccessor.getResourceAccessor(), key, null),
                                 this,
                                 linkedException);
  }

  public BaseSecurityException(String key, int severity, Throwable linkedException) {
    super(key);
    info = new BaseExceptionInfo(UserstoreResourceAccessor.category,
                                 severity,
                                 UserstoreResourceAccessor.location,
                                 new LocalizableTextFormatter(UserstoreResourceAccessor.getResourceAccessor(), key, null),
                                 this,
                                 linkedException);
  }

  public BaseSecurityException(String key, Object[] args, Throwable linkedException) {
    super(key);
    info = new BaseExceptionInfo(UserstoreResourceAccessor.category,
                                 Severity.ERROR,
                                 UserstoreResourceAccessor.location,
                                 new LocalizableTextFormatter(UserstoreResourceAccessor.getResourceAccessor(), key, args),
                                 this,
                                 linkedException);
  }

  public BaseSecurityException(String key, int severity, Object[] args, Throwable linkedException) {
    super(key);
    info = new BaseExceptionInfo(UserstoreResourceAccessor.category,
                                 severity,
                                 UserstoreResourceAccessor.location,
                                 new LocalizableTextFormatter(UserstoreResourceAccessor.getResourceAccessor(), key, args),
                                 this,
                                 linkedException);
  }


  public Throwable initCause(Throwable throwable) {
    return info.initCause(throwable);
  }

  public Throwable getCause() {
    return info.getCause();
  }

  public String getMessage() {
    return info.getLocalizedMessage();
  }

  public LocalizableText getLocalizableMessage() {
    return info.getLocalizableMessage();
  }

  public String getLocalizedMessage() {
    return info.getLocalizedMessage();
  }

  public String getLocalizedMessage(Locale locale) {
    return info.getLocalizedMessage(locale);
  }

  public String getLocalizedMessage(TimeZone zone) {
    return info.getLocalizedMessage(zone);
  }

  public String getLocalizedMessage(Locale locale, TimeZone zone) {
    return info.getLocalizedMessage(locale,zone);
  }

  public String getNestedLocalizedMessage() {
    return info.getNestedLocalizedMessage();
  }

  public String getNestedLocalizedMessage(Locale locale) {
    return info.getNestedLocalizedMessage(locale);
  }

  public String getNestedLocalizedMessage(TimeZone zone) {
    return info.getNestedLocalizedMessage(zone);
  }

  public String getNestedLocalizedMessage(Locale locale, TimeZone zone) {
    return info.getNestedLocalizedMessage(locale,zone);
  }

  public void finallyLocalize() {
    info.finallyLocalize();
  }

  public void finallyLocalize(Locale locale) {
    info.finallyLocalize(locale);
  }

  public void finallyLocalize(TimeZone zone) {
    info.finallyLocalize(zone);
  }

  public void finallyLocalize(Locale locale, TimeZone zone) {
    info.finallyLocalize(locale,zone);
  }

  public String getSystemStackTraceString() {
    StringWriter s = new StringWriter();
    super.printStackTrace(new PrintWriter(s));
    return s.toString();
  }

  public String getStackTraceString() {
    return info.getStackTraceString();
  }

  public String getNestedStackTraceString() {
    return info.getNestedStackTraceString();
  }

  public void printStackTrace() {
    info.printStackTrace();
  }

  public void printStackTrace(PrintStream stream) {
    info.printStackTrace(stream);
  }

  public void printStackTrace(PrintWriter writer) {
    info.printStackTrace(writer);
  }

  public void setLogSettings(Category category, int i, Location location) {
    info.setLogSettings(category, i, location);
  }

  public void log() {
    info.log();
  }

  private Object writeReplace() {
    StringWriter stringWriter = new StringWriter();
    printStackTrace(new PrintWriter(stringWriter, true));
    return new SecurityException(stringWriter.toString());
  }
}

