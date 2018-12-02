package com.sap.engine.services.webservices.jaxr.infomodel;

import javax.xml.registry.JAXRException;
import javax.xml.registry.UnsupportedCapabilityException;
import javax.xml.registry.infomodel.PersonName;

public class PersonNameImpl implements PersonName {
  private String fullName = "";
  
  public String getLastName() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getLastName)");
  }
  
  public void setLastName(String lastName) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(setLastName)");
  }
  
  public String getFirstName() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getFirstName)");
  }
  
  public void setFirstName(String firstName) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(setFirstName)");
  }
  
  public String getMiddleName() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getMiddleName)");
  }
  
  public void setMiddleName(String middleName) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(setMiddleName)");
  }
  
  public String getFullName() throws JAXRException {
    return fullName;
  }
  
  public void setFullName(String fullName) throws JAXRException {
    this.fullName = fullName;
  }
}