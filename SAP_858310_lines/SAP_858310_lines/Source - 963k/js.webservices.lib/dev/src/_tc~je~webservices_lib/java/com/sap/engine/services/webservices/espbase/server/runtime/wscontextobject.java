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
package com.sap.engine.services.webservices.espbase.server.runtime;

import com.sap.engine.frame.core.thread.ContextObject;
import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-22
 */
public class WSContextObject implements ContextObject {

  public static final String WSCONTEXT_OBJECT_NAME  =  "webservices_contextObject";

  ConfigurationContext cfgContext;

  public ContextObject childValue(ContextObject parent, ContextObject child) {
    WSContextObject childObject;

    if (child == null) {
      childObject = new WSContextObject();
      childObject.cfgContext = ((WSContextObject) parent).cfgContext;
    } else {
      childObject = (WSContextObject) child;
      childObject.cfgContext = ((WSContextObject) parent).cfgContext;
    }

    return childObject;
  }

  public ContextObject getInitialValue() {
    return new WSContextObject();
  }

  public void empty() {
    cfgContext = null;
  }
}