package com.sap.engine.services.webservices.jaxr.infomodel;

import com.sap.engine.services.webservices.jaxr.JAXRNestedException;

import javax.xml.registry.Connection;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.SpecificationLink;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

public class ServiceBindingImpl extends RegistryObjectImpl implements ServiceBinding {
  private Service service;
  private String accessURI;
  private ServiceBinding targetBinding;
  private Vector links;
  private boolean validate;

  public ServiceBindingImpl(Connection connection) {
    super(connection);
    links = new Vector();
  }

  public void addSpecificationLink(SpecificationLink specificationLink) throws JAXRException {
    links.addElement(specificationLink);
    ((SpecificationLinkImpl) specificationLink).setServiceBinding(this);
  }
  
  public void addSpecificationLinks(Collection specificationLinks) throws JAXRException {
    Iterator it = specificationLinks.iterator();
    while (it.hasNext()) {
      try {
        addSpecificationLink((SpecificationLink) it.next());
      } catch (Exception e) {
        throw new JAXRNestedException(e);
      }
    }
  }
  
  public void removeSpecificationLink(SpecificationLink specificationLink) throws JAXRException {
    if (links.remove(specificationLink)) {
      ((SpecificationLinkImpl) specificationLink).setServiceBinding(null);
    } else {
      InternationalString name = specificationLink.getName();
      throw new JAXRException("The serviceBinding does not contain the specified SpecificationLink: " + ((name != null) ? name.getValue() : "<NO NAME SPECIFIED>"));
    }
  }
  
  public void removeSpecificationLinks(Collection specificationLinks) throws JAXRException {
    try {
      SpecificationLink[] parts = (SpecificationLink[]) specificationLinks.toArray(new SpecificationLink[specificationLinks.size()]);

      for (int i = 0; i < parts.length; i++) {
        removeSpecificationLink(parts[i]);
      }
    } catch (Exception ex) {
      throw new JAXRNestedException(ex);
    }
  }
  
  public Collection getSpecificationLinks() throws JAXRException {
    return links;
  }
  
  public String getAccessURI() throws JAXRException {
    return accessURI;
  }
  
  public void setAccessURI(String uri) throws JAXRException {
    if (getTargetBinding() != null) {
      throw new InvalidRequestException("There is a non-null targetBinding defined");
    } else {
      accessURI = uri;
    }
  }

  public Service getService() throws JAXRException {
    return service;
  }

  public void setService(Service s) {
    service = s; 
  }
  
  public ServiceBinding getTargetBinding() throws JAXRException {
    return targetBinding;
  }
  
  public void setTargetBinding(ServiceBinding binding) throws JAXRException {
    if (getAccessURI() != null) {
      throw new InvalidRequestException("There is already a non-null accessURI defined");
    } else {
      targetBinding = binding;
    }
  }
  
  public void setValidateURI(boolean validate) throws JAXRException {
    this.validate = validate;
  }
  
  public boolean getValidateURI() throws JAXRException {
    return validate;
  }
}
  