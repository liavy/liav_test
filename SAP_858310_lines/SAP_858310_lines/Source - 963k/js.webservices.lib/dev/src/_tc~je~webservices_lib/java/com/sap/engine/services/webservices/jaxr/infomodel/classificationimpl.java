package com.sap.engine.services.webservices.jaxr.infomodel;

import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.RegistryObject;

public class ClassificationImpl extends RegistryObjectImpl implements Classification {
  private Concept concept;
  private ClassificationScheme classificationScheme;
  private String value;
  private RegistryObject registryObject;
  private int external;
  
  public ClassificationImpl(Connection connection) {
    super(connection);
    external = 0;
  }

  public ClassificationScheme getClassificationScheme() throws JAXRException {
    if (isExternal()) {
      return classificationScheme;
    }
    Concept c = getConcept();
    if (c != null) {
      return c.getClassificationScheme();
    } else {
      throw new JAXRException("No concept is assigned to this internal Classification");
    }
  }
  
  public RegistryObject getClassifiedObject() throws JAXRException {
    return registryObject;
  }
  
  public Concept getConcept() throws JAXRException {
    return concept;
  }
  
  public String getValue() throws JAXRException {
    if (isExternal()) {
      return value;
    }
    Concept c = getConcept();
    if (c != null) {
      return c.getValue();
    } else {
      throw new JAXRException("No concept is assigned to this internal Classification");
    }
  }

  public boolean isExternal() throws JAXRException {
    if (external == 0) {
      throw new JAXRException("You must use setClassificationScheme or setConcept before using isExternal");
    }
    return (external == 1);
  }

  public void setClassificationScheme(ClassificationScheme classificationScheme) throws JAXRException {
    if (external == 2) {
      throw new JAXRException("Already defined an internal Classification by using setConcept");
    }
    this.classificationScheme = classificationScheme;
    external = 1;
  }
  
  public void setClassifiedObject(RegistryObject object) throws JAXRException {
    registryObject = object;
  }

  public void setConcept(Concept concept) throws JAXRException {
    if (external == 1) {
      throw new JAXRException("Already defined an external Classification by using setClassificationScheme");
    }
    this.concept = concept;
    external = 2;
  }
  
  public void setValue(String value) throws JAXRException {
    if (isExternal()) {
      this.value = value;;
    } else {
      Concept c = getConcept();
      if (c != null) {
        c.setValue(value);
      } else {
        throw new JAXRException("No concept is assigned to this internal Classification");
      }
    }
  }
}