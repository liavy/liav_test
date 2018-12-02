/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.jaxws.handlers;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, Jul 25, 2006
 */
public class JAXWSHandlersException extends Exception {

  public JAXWSHandlersException() {
    super();
  }

  public JAXWSHandlersException(String message, Throwable cause) {
    super(message, cause);
  }

  public JAXWSHandlersException(String message) {
    super(message);
  }

  public JAXWSHandlersException(Throwable cause) {
    super(cause);
  }
}
