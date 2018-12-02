package com.sap.engine.services.webservices.jaxr;

import javax.xml.registry.JAXRException;
import javax.xml.registry.JAXRResponse;

public class JAXRResponseImpl implements JAXRResponse {
  private String id;
  private int status;
  
  public String getRequestId() throws JAXRException {
    return id;
  }
  
  public void setRequestId(String id) {
    this.id = id;
  }
  
  public int getStatus() throws JAXRException {
    return status;
  }
  
  public void setStatus(int status) {
    this.status = status;
  }
  
  public boolean isAvailable() throws JAXRException {
    return (status == STATUS_UNAVAILABLE) ? false : true;
  }
}