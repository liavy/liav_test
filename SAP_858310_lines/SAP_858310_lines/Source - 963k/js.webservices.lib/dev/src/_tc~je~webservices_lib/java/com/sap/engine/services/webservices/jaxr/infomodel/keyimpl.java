package com.sap.engine.services.webservices.jaxr.infomodel;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Key;

public class KeyImpl implements Key {
  private String id;
  
  public KeyImpl() {
    this(null);
  }
  
  public KeyImpl(String id) {
    this.id = id;
  }
  
  public String getId() throws JAXRException {
    return id;
  }
  
  public void setId(String id) throws JAXRException {
    this.id = id;
  }
}