package com.sap.engine.services.webservices.jaxr.infomodel;

import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.ExternalIdentifier;
import javax.xml.registry.infomodel.RegistryObject;

public class ExternalIdentifierImpl extends RegistryObjectImpl implements ExternalIdentifier {
  private RegistryObject registryObject;
  private String value;
  private ClassificationScheme idScheme;
  
  public ExternalIdentifierImpl(Connection connection) {
    super(connection);
  }

  public ClassificationScheme getIdentificationScheme() throws JAXRException {
    return idScheme;
  }
  
  public RegistryObject getRegistryObject() throws JAXRException {
    return registryObject;
  }
  
  public String getValue() throws JAXRException {
    return value;
  }
  
  public void setIdentificationScheme(ClassificationScheme classificationScheme) throws JAXRException {
    idScheme = classificationScheme;
  }
  
  public void setValue(String value) throws JAXRException {
    this.value = value;
  }
  
  protected void setRegistryObject(RegistryObject object) {
    registryObject = object;
  }
  
}