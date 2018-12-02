/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.interfaces.webservices.esp;

/**
 * Thrown when something an error has occured during some hibernation process.
 * Instances of this exception are thrown by methods of HibernationEnviroment interface.
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-5-9
 */
public class HibernationEnvironmentException extends Exception {

  public HibernationEnvironmentException() {
    super();
  }
  public HibernationEnvironmentException(String message) {
    super(message);
  }
  public HibernationEnvironmentException(String message, Throwable cause) {
    super(message, cause);
  }
  public HibernationEnvironmentException(Throwable cause) {
    super(cause);
  }
}
