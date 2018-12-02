package com.sap.engine.interfaces.webservices.runtime.component;

/**
 * Title: BaseRegistryException
 * Description: The exception is thrown in case a problem has occured(no such component or dublicate exception), while working with any regisry.
 * The exception should be cast to com.sap.exception.IBaseException in order the correct message format be used.
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class BaseRegistryException extends Exception {

  public BaseRegistryException() {
    super();
  }

  public BaseRegistryException(String str) {
    super(str);
  }

}