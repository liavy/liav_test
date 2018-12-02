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
package com.sap.engine.services.servlets_jsp.security;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

public interface HttpCallbackHandler extends CallbackHandler {
  /**
   * Retrieve the information requested in the provided Callbacks.
   */
  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException;
}

