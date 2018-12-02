package com.sap.engine.services.webservices.jaxr.infomodel;

import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.UnsupportedCapabilityException;
import javax.xml.registry.infomodel.AuditableEvent;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.User;

public class AuditableEventImpl extends RegistryObjectImpl implements AuditableEvent {
  public AuditableEventImpl(Connection connection) {
    super(connection);
  }
    
  public User getUser() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getUser)");
  }
  
  public java.sql.Timestamp getTimestamp() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getTimestamp)");
  }
  
  public int getEventType() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getEventType)");
  }
  
  public RegistryObject getRegistryObject() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getRegistryObject)");
  }
}