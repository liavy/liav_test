package com.sap.engine.interfaces.webservices.runtime.component;

/**
 * Title: ComponentInstantiationException
 * Description: The exception is thrown in case a component could not be instantiated.
 * The exception should be cast to com.sap.exception.IBaseException in order the correct message format be used.
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class ComponentInstantiationException extends Exception {

  public ComponentInstantiationException() {
    super();
  }

  public ComponentInstantiationException(String str) {
    super(str);
  }

}