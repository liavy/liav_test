package com.sap.engine.services.webservices.jaxr.infomodel;

import java.util.Collection;
import java.util.Vector;

import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.SpecificationLink;

public class SpecificationLinkImpl extends RegistryObjectImpl implements SpecificationLink {
  private RegistryObject object;
  private InternationalString description;
  private Vector parameters;
  private ServiceBinding binding;
 
  public SpecificationLinkImpl(Connection connection, ServiceBinding binding) {
    super(connection);
    this.binding = binding;
  }
  
  public ServiceBinding getServiceBinding() throws RegistryException {
    return binding;
  }
  
  public RegistryObject getSpecificationObject() throws JAXRException {
    return object;
  }
  
  public void setSpecificationObject(RegistryObject obj) throws JAXRException {
    if (obj == null) {
      throw new NullPointerException("Registry Object cannot be null");
    }
    if (obj instanceof Concept) {
      object = obj;
    } else {
      throw new JAXRException("In the case of a UDDI provider the specification object must be a Concept, but not an instance of " + obj.getClass().getName());
    }
  }
  
  public InternationalString getUsageDescription() throws JAXRException {
    return description;
  }
  
  public void setUsageDescription(InternationalString usageDescription) throws JAXRException {
    description = usageDescription;
  }
  
  public Collection getUsageParameters() throws JAXRException {
    return parameters;
  }
  
  public void setUsageParameters(Collection usageParamaters) throws JAXRException {
    parameters = new Vector(usageParamaters);
  }
  
  public void setServiceBinding(ServiceBinding binding) throws JAXRException {
    this.binding = binding;
  }
}