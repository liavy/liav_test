/*
 * Copyright (c) 2000 by InQMy Software AG.,
 * http://www.inqmy.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of InQMy Software AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with InQMy.
 */
package com.sap.engine.services.servlets_jsp.server.exceptions;

/**
 * This exception must be thrown when a non valid session is tried to be used.
 *
 * @author Diyan Yordanov
 */
public class InvalidSessionException extends IllegalStateException {

  /**
   * Constructs an InvalidSessionException with no detail message.
   * A detail message is a String that describes this particular exception.
   */
  public InvalidSessionException() {
    super();
  }

  /**
   * Constructs an InvalidSessionException with the specified detail
   * message.  A detail message is a String that describes this particular
   * exception.
   *
   * @param message the String that contains a detailed message
   */
  public InvalidSessionException(String message) {
    super(message);
  }

  /**
   * Constructs a new InvalidSessionException with the specified detail message and
   * cause.
   *
   * @param  message the detail message.
   * @param  linkedException the Throwable that indicates the cause for this exception.
   */
  public InvalidSessionException(String message, Throwable linkedException) {
    super(message, linkedException);
  }

  /**
   * Constructs a new InvalidSessionException with the specified cause.
   *
   * @param  message the detail message.
   * @param  linkedException the Throwable that indicates the cause for this exception.
   */
  public InvalidSessionException(Throwable linkedException) {
    super(linkedException);
  }

}

