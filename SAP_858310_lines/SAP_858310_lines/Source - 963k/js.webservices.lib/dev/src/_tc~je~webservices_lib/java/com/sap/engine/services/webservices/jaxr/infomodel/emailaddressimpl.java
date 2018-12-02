package com.sap.engine.services.webservices.jaxr.infomodel;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.EmailAddress;

public class EmailAddressImpl implements EmailAddress {
  private String address;
  private String type;
  
  public String getAddress() throws JAXRException {
    return address;
  }
  
  public void setAddress(String address) throws JAXRException {
    this.address = address;
  }
  
  public String getType() throws JAXRException {
    return type;
  }
  
  public void setType(String type) throws JAXRException {
    this.type = type;
  }
}