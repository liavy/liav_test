package com.sap.engine.interfaces.webservices.server.management.exception;

/**
 * Title: WSBaseException
 * Description: The exception is thrown in case a problem while using webservices interface instance  has occurred.
 * The exception should be cast to com.sap.exception.IBaseException in order the correct message format be used.
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSBaseException extends Exception {

  public WSBaseException() {
    super();
  }

  public WSBaseException(String str) {
    super(str);
  }

}

