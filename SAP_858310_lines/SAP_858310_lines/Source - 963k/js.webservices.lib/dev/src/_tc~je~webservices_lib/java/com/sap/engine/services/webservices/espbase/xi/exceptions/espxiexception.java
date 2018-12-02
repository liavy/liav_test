package com.sap.engine.services.webservices.espbase.xi.exceptions;

public class ESPXIException extends Exception {
  
  public ESPXIException() {
    super();
  }

  public ESPXIException(String message) {
    super(message);
  }
  
  public ESPXIException(String message, Throwable cause) {
    super(message, cause);
  }

  public ESPXIException(Throwable cause) {
    super(cause);
  } 
}
