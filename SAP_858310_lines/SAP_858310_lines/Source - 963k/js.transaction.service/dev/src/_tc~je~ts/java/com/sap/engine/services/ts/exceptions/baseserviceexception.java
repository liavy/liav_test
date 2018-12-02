/*
 * Copyright (c) 2002 by SAP Labs Bulgaria AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.ts.exceptions;

import com.sap.engine.frame.ServiceException;
import com.sap.localization.LocalizableTextFormatter;

import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * Wrapper of ServiceException.
 *
 * @author Peter Matov
 * @version 6.30
 */
public class BaseServiceException extends ServiceException {

  static final long serialVersionUID = 130984912102417043L;

  /**
   * Constructs exception with the specified message, many parameters
   * and linked exception.
   *
   * @param msg message for this exception to be set
   * @param parameters set of parameters
   * @param linkedException linked exception
   */
  public BaseServiceException(String msg, Object[] parameters, Throwable linkedException) {
    super(new LocalizableTextFormatter(TSResourceAccessor.getResourceAccessor(), msg, parameters), linkedException);
  }

  /**
   * Constructs exception with the specified message, one parameter
   * and linked exception.
   *
   * @param msg message for this exception to be set
   * @param parameter parameter value
   * @param linkedException linked exception
   */
  public BaseServiceException(String msg, Object parameter, Throwable linkedException) {
    super(new LocalizableTextFormatter(TSResourceAccessor.getResourceAccessor(), msg,  new Object[]{parameter}), linkedException);
  }

  /**
   * Constructs exception with the specified message and linked exception.
   *
   * @param msg message for this exception to be set
   * @param linkedException  linked exception
   */
  public BaseServiceException(String msg, Throwable linkedException) {
    super(new LocalizableTextFormatter(TSResourceAccessor.getResourceAccessor(), msg), linkedException);
  }

  /**
   * Constructs exception with the specified message and many parameters.
   *
   * @param msg message for this exception to be set
   * @param parameters set of parameters
   */
  public BaseServiceException(String msg, Object[] parameters) {
    super(new LocalizableTextFormatter(TSResourceAccessor.getResourceAccessor(), msg, parameters));
  }

  /**
   * Constructs exception with the specified message and one parameter.
   *
   * @param msg message for this exception to be set
   * @param parameter parameter value
   */
  public BaseServiceException(String msg, Object parameter) {
    super(new LocalizableTextFormatter(TSResourceAccessor.getResourceAccessor(), msg, new Object[]{parameter}));
  }

  /**
   * Constructs exception with the specified message.
   *
   * @param  msg  message for this exception to be set.
   */
  public BaseServiceException(String msg) {
    super(new LocalizableTextFormatter(TSResourceAccessor.getResourceAccessor(), msg));
  }

  private Object writeReplace(){
    StringWriter stringWriter = new StringWriter();
    printStackTrace(new PrintWriter(stringWriter,true));
    return new ServiceException(stringWriter.toString());
  }

}
