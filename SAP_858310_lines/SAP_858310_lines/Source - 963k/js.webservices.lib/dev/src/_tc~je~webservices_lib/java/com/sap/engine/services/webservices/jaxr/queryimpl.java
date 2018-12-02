package com.sap.engine.services.webservices.jaxr;

import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.UnsupportedCapabilityException;

public class QueryImpl implements Query {
  public int getType() throws JAXRException {   
    throw new UnsupportedCapabilityException("Level 1 capability feature(getType)");
  }
  
  public String toString() {
    return "Level 1 capability feature(toString)";
  }
}