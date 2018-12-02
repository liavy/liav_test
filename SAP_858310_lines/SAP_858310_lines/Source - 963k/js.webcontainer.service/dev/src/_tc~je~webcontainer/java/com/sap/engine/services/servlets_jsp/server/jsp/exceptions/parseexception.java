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

/*
 *
 * @author Galin Galchev
 * @version 4.0
 */
import com.sap.engine.services.servlets_jsp.server.exceptions.WebServletException;

import java.io.StringWriter;
import java.io.PrintWriter;

public class ParseException extends WebServletException {
  public static final String PARSING_FAILED = "servlet_jsp_0440";
  
  /**
   *Constructs a new ParseException exception.
   *
   * @param   msg  message of the exception
   */
  public ParseException(String msg) {
    super(msg);
  }

  /**
   *Constructs a new ParseException exception.
   *
   * @param   msg  message of the exception
   * @param   parameters  parameters of exception message
   */
  public ParseException(String msg, Object [] parameters) {
    super(msg, parameters);
  }

  /**
   *Constructs a new ParseException exception.
   *
   * @param   msg  message of the exception
   * @param   linkedException  root cause of this exception
   */
  public ParseException(String msg, Throwable linkedException) {
    super(msg, linkedException);
  }

  /**
   *Constructs a new ParseException exception.
   *
   * @param   msg  message of the exception
   * @param   parameters  parameters of exception message
   * @param   linkedException  root cause of this exception
   */
  public ParseException(String msg, Object [] parameters, Throwable linkedException) {
    super(msg, parameters, linkedException);
  }

  private Object writeReplace() {
    StringWriter stringWriter = new StringWriter();
    printStackTrace(new PrintWriter(stringWriter, true));
    return new WebServletException(stringWriter.toString());
  }

}

