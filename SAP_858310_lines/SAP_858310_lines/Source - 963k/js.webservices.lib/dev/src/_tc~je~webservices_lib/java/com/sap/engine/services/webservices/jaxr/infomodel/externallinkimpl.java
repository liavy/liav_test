package com.sap.engine.services.webservices.jaxr.infomodel;

import java.util.Collection;
import java.util.Vector;

import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.ExternalLink;

public class ExternalLinkImpl extends RegistryObjectImpl implements ExternalLink {
  private Vector objects;
  private String uri;
  private boolean validate;
  
  public ExternalLinkImpl(Connection connection) {
    super(connection);
    objects = new Vector();
  }
  
  public ExternalLinkImpl(Connection connection, Collection objects) {
    this(connection);
    this.objects = new Vector(objects);
  }
  
  public Collection getLinkedObjects() throws JAXRException {
    return objects;
  }
  
  public String getExternalURI() throws JAXRException {
    return uri;
  }
  
  public void setExternalURI(String uri) throws JAXRException {
    this.uri = uri;
  }

  public void setValidateURI(boolean validate) throws JAXRException {
    this.validate = validate;
  }
  
  public boolean getValidateURI() throws JAXRException {
    return validate;
  }
}