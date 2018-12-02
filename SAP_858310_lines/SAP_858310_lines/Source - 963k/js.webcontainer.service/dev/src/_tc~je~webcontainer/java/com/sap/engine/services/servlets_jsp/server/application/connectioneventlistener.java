package com.sap.engine.services.servlets_jsp.server.application;

import com.sap.engine.services.servlets_jsp.server.exceptions.WebApplicationException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.EventListener;
import java.lang.reflect.Method;

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
public class ConnectionEventListener {
  public static final String METHOD_CONNECTION_CLOSED = "connectionClosed";
  private EventListener connectionClosedListener = null;
  private Method method = null;

  public ConnectionEventListener(EventListener connectionClosedListener, Method method) {
    this.connectionClosedListener = connectionClosedListener;
    this.method = method;
  }

  public void connectionClosed(ServletRequest request, ServletResponse response) throws WebApplicationException {
    try {
      method.invoke(connectionClosedListener, new Object[]{request, response});
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      throw new WebApplicationException(WebApplicationException.Cannot_notify_connection_listener,
              new Object[]{connectionClosedListener, e.toString()}, e);
    }
  }
}
