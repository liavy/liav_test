package com.sap.engine.services.webservices.espbase.server.runtime.metering;

public class ServiceMeteringException extends Exception {
  public ServiceMeteringException(String message){
    super(message);
  }
  
  public ServiceMeteringException(String message, Exception e){
    super(message, e);
  }
}
