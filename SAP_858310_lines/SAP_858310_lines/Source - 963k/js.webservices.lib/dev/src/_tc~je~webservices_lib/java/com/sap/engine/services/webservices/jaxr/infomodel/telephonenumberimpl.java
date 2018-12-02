package com.sap.engine.services.webservices.jaxr.infomodel;

import javax.xml.registry.JAXRException;
import javax.xml.registry.UnsupportedCapabilityException;
import javax.xml.registry.infomodel.TelephoneNumber;

public class TelephoneNumberImpl implements TelephoneNumber {
  private String number;
  private String type;
    
  public TelephoneNumberImpl() {
    number = "";
    type = "";
  }
  
  public String getCountryCode() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getCountryCode)");
  }
  
  public String getAreaCode() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getAreaCode)");
  }
  
  public String getNumber() throws JAXRException {
    return number; 
  }
  
  public String getExtension() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getExtension)");
  }
  
  public String getUrl() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getUrl)");
  }
  
  public String getType() throws JAXRException {
    return type;
  }
  
  public void setCountryCode(String countryCode) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(setCountryCode)");
  }
  
  public void setAreaCode(String areaCode) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(setAreaCode)");
  }
  
  public void setNumber(String number) throws JAXRException {
    this.number = number;    
  }
  
  public void setExtension(String extension) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(setExtension)");
  }
  
  public void setUrl(String url) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(setUrl)");
  }
  
  public void setType(String type) throws JAXRException {
    this.type = type;
  }
}