package com.sap.engine.services.webservices.jaxr.infomodel;

import java.util.Date;

import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.UnsupportedCapabilityException;
import javax.xml.registry.infomodel.RegistryEntry;

public class RegistryEntryImpl extends RegistryObjectImpl implements RegistryEntry {
  
  public RegistryEntryImpl(Connection connection) {
    super(connection);
  }
  
  public int getStatus() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getStatus)");
  }
  
  public int getStability() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getStability)");
  }
  
  public void setStability(int stability) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(setStability)");
  }
  
  public Date getExpiration() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getExpiration)");
  }
  
  public void setExpiration(Date date) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(setExpiration)");
  }
  
  public int getMajorVersion() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getMajorVersion)");
  }
  
  public void setMajorVersion(int majorVersion) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(setMajorVersion)");
  }
  
  public int getMinorVersion() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getMinorVersion)");
  }
  
  public void setMinorVersion(int minorVersion) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(setMinorVersion)");
  }
  
  public String getUserVersion() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getUserVersion)");
  }
  
  public void setUserVersion(String userVersion) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(setUserVersion)");
  }
}