/*
 * Copyright (c) 2002 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.security;

import com.sap.engine.services.servlets_jsp.security.HttpRequestClientInfo;
import com.sap.engine.services.servlets_jsp.security.HttpCallbackHandler;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebUnsupportedCallbackException;
import com.sap.engine.lib.security.http.HttpGetterCallback;
import com.sap.engine.lib.security.http.HttpSetterCallback;
import com.sap.engine.lib.security.PasswordChangeCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import java.io.IOException;

public class HttpCallbackHandlerImpl implements HttpCallbackHandler {

  private HttpRequestClientInfo clientInfo = null;

  // the new constructor that will be used
  public HttpCallbackHandlerImpl(HttpRequestClientInfo info) {
    this.clientInfo = info;
  }

  /**
   * Retrieve the information requested in the provided Callbacks.
   */
  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    for (int i = 0; i < callbacks.length; i++) {
      setCallback(callbacks[i]);
    }
  }

  private void setCallback(Callback callback) throws UnsupportedCallbackException {
    if (callback instanceof NameCallback) {
      setCallback((NameCallback) callback);
    } else if (callback instanceof PasswordChangeCallback) {
      setCallback((PasswordChangeCallback) callback);
    } else if (callback instanceof PasswordCallback) {
      setCallback((PasswordCallback) callback);
    } else if (callback instanceof HttpGetterCallback) {
      setCallback((HttpGetterCallback) callback);
    } else if (callback instanceof HttpSetterCallback) {
      setCallback((HttpSetterCallback) callback);
    }
  }

  private void setCallback(HttpGetterCallback callback) throws UnsupportedCallbackException {
    byte type = callback.getType();
    if (HttpGetterCallback.CERTIFICATE == type) {
      callback.setValue(clientInfo.getCertificateChain());
    } else if (HttpGetterCallback.COOKIE == type) {
      callback.setValue(clientInfo.getHttpCookie(callback.getName()));
    } else if (HttpGetterCallback.HEADER == type) {
      callback.setValue(clientInfo.getHttpHeader(callback.getName()));
    } else if (HttpGetterCallback.REQUEST_PARAMETER == type) {
      callback.setValue(clientInfo.getHttpParameterValues(callback.getName()));
    } else if (HttpGetterCallback.CLIENT_IP == type) {
      callback.setValue(clientInfo.getClientIp());
    } else if (HttpGetterCallback.METHOD_TYPE == type) {
      callback.setValue(clientInfo.getMethod());
    } else if (HttpGetterCallback.IS_SECURE == type) {
      callback.setValue(new Boolean(clientInfo.isSecure()));
    } else if (HttpGetterCallback.BODY == callback.getType()) {
       callback.setValue(clientInfo.getBody());
    } else if (HttpGetterCallback.SESSION_ATTRIBUTE == type) {
      callback.setValue(clientInfo.getSessionAttribute(callback.getName()));
    } else if (HttpGetterCallback.ALL_SESSION_ATTRIBUTES == type) {
      callback.setValue(clientInfo.getSessionAttributesNames());      
    } else {
      throw new WebUnsupportedCallbackException(WebUnsupportedCallbackException.UNSUPPORTED_CALLBACK_CONSTANT,
              new Object[]{String.valueOf(type)}, callback);
    }
  }

  private void setCallback(HttpSetterCallback callback) throws UnsupportedCallbackException {
    if (HttpGetterCallback.COOKIE == callback.getType()) {
      clientInfo.addResponseCookie(callback.getName(), (String)callback.getValue());
    } else if (HttpGetterCallback.HEADER == callback.getType()) {
      clientInfo.addResponseHeader(callback.getName(), (String)callback.getValue());
    } else if (HttpGetterCallback.SET_HEADER == callback.getType()) {
      clientInfo.setResponseHeader(callback.getName(), (String)callback.getValue());
    } else if (HttpGetterCallback.RESPONSE_CODE == callback.getType()) {
      clientInfo.setResponseStatusCode(new Integer((String)callback.getValue()).intValue());
    } else if (HttpGetterCallback.BODY == callback.getType()) {
      clientInfo.setHttpBody(((String)callback.getValue()).getBytes());
    } else if (HttpGetterCallback.SESSION_ATTRIBUTE == callback.getType()) {
      clientInfo.setSessionAttribute(callback.getName(), callback.getValue());
    } else if (HttpGetterCallback.REMOVE_SESSION_ATTRIBUTE == callback.getType()) {
      clientInfo.removeSessionAttribute(callback.getName());
    } else if (HttpGetterCallback.REQUEST_ATTRIBUTE == callback.getType()) {
      clientInfo.setRequestAttribute(callback.getName(), callback.getValue());
    } else if (HttpGetterCallback.REMOVE_HEADER == callback.getType()) {
      clientInfo.removeRequestHeader(callback.getName(), (String)callback.getValue());
    } else {
      throw new WebUnsupportedCallbackException(WebUnsupportedCallbackException.UNSUPPORTED_CALLBACK_CONSTANT,
              new Object[]{String.valueOf(callback.getType())}, callback);
    }
  }

  private void setCallback(NameCallback callback) {
    callback.setName(clientInfo.getUserName());
  }

  private void setCallback(PasswordCallback callback) {
    callback.setPassword(clientInfo.getPassword());
  }

  private void setCallback(PasswordChangeCallback callback) {
    callback.setPassword(clientInfo.getNewPassword());
  }
}

