package com.sap.engine.services.webservices.jaxr.infomodel;

import java.util.Collection;
import java.util.Set;

import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.UnsupportedCapabilityException;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;

public class RegistryPackageImpl extends RegistryEntryImpl implements RegistryPackage {
    
  public RegistryPackageImpl(Connection con) {
    super(con);
  }
  
  public void addRegistryObject(RegistryObject registryObject) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(addRegistryObject)");
  }
  
  public void addRegistryObjects(Collection registryObjects) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(addRegistryObjects)");
  }
  
  public void removeRegistryObject(RegistryObject registryObject) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(removeRegistryObject)");
  }
  
  public void removeRegistryObjects(Collection registryObjects) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(removeRegistryObjects)");
  }
  
  public Set getRegistryObjects() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getRegistryObjects)");
  }
}