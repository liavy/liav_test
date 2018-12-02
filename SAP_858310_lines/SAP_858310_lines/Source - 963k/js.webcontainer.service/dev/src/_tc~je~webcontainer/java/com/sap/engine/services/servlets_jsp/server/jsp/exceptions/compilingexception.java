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
package com.sap.engine.services.servlets_jsp.server.jsp.exceptions;

import com.sap.engine.services.servlets_jsp.server.exceptions.WebServletException;

import java.io.PrintWriter;
import java.io.StringWriter;

/*
 *
 * @author Galin Galchev
 * @version 4.0
 */

public class CompilingException extends WebServletException {

  public static String  ERROR_IN_EXECUTING_THE_PROCESS_OF_COMPILATION = "servlet_jsp_0420";
  public static String  ERROR_IN_COMPILING_THE_JSP_FILE = "servlet_jsp_0421";
  public static String  COMPILER_NOT_AVAILABLE = "servlet_jsp_0422";
  
  /**
   *Constructs a new Compiling exception.
   *
   * @param   msg  message of the exception
   */
  public CompilingException(String msg) {
    super(msg);
  }

  /**
   *Constructs a new Compiling exception.
   *
   * @param   msg  message of the exception
   * @param   parameters  parameters of the message
   */
  public CompilingException(String msg, Object[] parameters) {
    super(msg, parameters);
  }

  /**
   *Constructs a new Compiling exception.
   *
   * @param   msg  message of the exception
   * @param   rootCause  root cause of this exception
   */
  public CompilingException(String msg, Throwable rootCause) {
    super(msg, rootCause);
  }

  /**
   *Constructs a new Compiling exception.
   *
   * @param   msg  message of the exception
   * @param   parameters  parameters of the message
   * @param   rootCause  root cause of this exception
   */
  public CompilingException(String msg, Object[] parameters, Throwable rootCause) {
    super(msg, parameters, rootCause);
  }

  private Object writeReplace() {
    StringWriter stringWriter = new StringWriter();
    printStackTrace(new PrintWriter(stringWriter, true));
    return new WebServletException(stringWriter.toString());
  }

}


