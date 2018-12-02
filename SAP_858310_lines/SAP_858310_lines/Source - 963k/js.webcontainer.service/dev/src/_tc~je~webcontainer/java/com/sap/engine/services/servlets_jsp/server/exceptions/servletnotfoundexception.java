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

package com.sap.engine.services.servlets_jsp.server.exceptions;

import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * This exception must be thrown when the requested jsp or servlet not exists.
 *
 *
 * @author Maria Jurova
 * @version 4.0
 */
public class ServletNotFoundException extends WebIOException {

  public static String Requested_resource_not_found = "servlet_jsp_0280";
  public static final String CANNOT_LOAD_SERVLET = "servlet_jsp_0281";


  /**
   *Constructs a new ServletNotFoundException exception.
   *
   * @param   msg  message of the exception
   * @param   parameters  parameters of the message of the exception
   * @param   linkedException root cause of the exception
   */
  public ServletNotFoundException(String msg, Object [] parameters, Throwable linkedException) {
    super(msg, parameters, linkedException);
  }

  /**
   *Constructs a new ServletNotFoundException exception.
   *
   * @param   msg  message of the exception
   * @param   parameters  parameters of the message of the exception
   */
  public ServletNotFoundException(String msg, Object [] parameters) {
    super(msg, parameters);
  }


  /**
   *Constructs a new ServletNotFoundException exception.
   *
   * @param   msg  message of the exception
   * @param   linkedException root cause of the exception
   */
  public ServletNotFoundException(String msg, Throwable linkedException) {
    super(msg, linkedException);
  }

  /**
   *Constructs a new ServletNotFoundException exception.
   *
   * @param   msg  message of the exception
   */
  public ServletNotFoundException(String msg) {
    super(msg);
  }

  private Object writeReplace(){
    StringWriter stringWriter = new StringWriter();
    printStackTrace(new PrintWriter(stringWriter, true));
    return new WebIOException(stringWriter.toString());
  }

}

