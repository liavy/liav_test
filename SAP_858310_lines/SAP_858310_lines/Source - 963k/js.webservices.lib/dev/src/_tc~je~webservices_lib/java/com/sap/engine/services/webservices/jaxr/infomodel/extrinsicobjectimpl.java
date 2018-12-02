package com.sap.engine.services.webservices.jaxr.infomodel;

import javax.activation.DataHandler;
import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.UnsupportedCapabilityException;
import javax.xml.registry.infomodel.ExtrinsicObject;

public class ExtrinsicObjectImpl extends RegistryEntryImpl implements ExtrinsicObject {   
  
  public ExtrinsicObjectImpl(Connection con) {
    super(con);
  }
  
  public String getMimeType() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getMimeType)");
  }
  
  public void setMimeType(String mimeType) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(setMimeType)");
  }
  
  public boolean isOpaque() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(isOpaque)");
  }
  
  public void setOpaque(boolean isOpaque) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(setOpaque)");
  }

  public DataHandler getRepositoryItem() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getRepositoryItem)");
  }
  
  public void setRepositoryItem(DataHandler repositoryItem) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(setRepositoryItem)");
  }
 
}