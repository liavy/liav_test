package com.sap.engine.services.webservices.jaxr;

import javax.xml.registry.CapabilityProfile;
import javax.xml.registry.JAXRException;

public class CapabilityProfileImpl implements CapabilityProfile {
  private String version;
  private int level;
  
  public CapabilityProfileImpl() {
    version = "1.0";
    level = 0;
  }
  
  public String getVersion() throws JAXRException {
    return version;
  }
  
  public int getCapabilityLevel() throws JAXRException {
    return level;
  }
}