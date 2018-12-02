package com.sap.engine.services.webservices.jaxr.infomodel;

import com.sap.engine.services.webservices.jaxr.JAXRNestedException;

import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

public class ServiceImpl extends RegistryEntryImpl implements Service {
  private Organization organization;
  private Vector bindings;
  
  public ServiceImpl(Connection connection) {
    super(connection);
    bindings = new Vector();
  }
  
  public void addServiceBinding(ServiceBinding serviceBinding) throws JAXRException {
    bindings.addElement(serviceBinding);
    ((ServiceBindingImpl) serviceBinding).setService(this);
  }
  
  public void addServiceBindings(Collection serviceBindings) throws JAXRException {
    Iterator it = serviceBindings.iterator();
    while (it.hasNext()) {
      try {
        addServiceBinding((ServiceBinding) it.next());
      } catch (Exception e) {
        throw new JAXRNestedException(e);
      }
    }
  }
  
  public void removeServiceBinding(ServiceBinding serviceBinding) throws JAXRException {
    if (bindings.remove(serviceBinding)) {
      ((ServiceBindingImpl) serviceBinding).setService(null);
    } else {
      InternationalString name = serviceBinding.getName();
      throw new JAXRException("The Service does not contain the specified serviceBinding: " + ((name != null) ? name.getValue(): "<NO NAME SPECIFIED>"));
    }
  }
  
  public void removeServiceBindings(Collection serviceBindings) throws JAXRException {
    try {
      ServiceBinding[] parts = (ServiceBinding[]) serviceBindings.toArray(new ServiceBinding[serviceBindings.size()]);

      for (int i = 0; i < parts.length; i++) {
        removeServiceBinding(parts[i]);
      }
    } catch (Exception ex) {
      throw new JAXRNestedException(ex);
    }
  }
  
  public Collection getServiceBindings() throws JAXRException {
    return bindings;
  }
  
  public Organization getProvidingOrganization() {
    return organization;
  }
  
  public void setProvidingOrganization(Organization parentOrganization) {
    organization = parentOrganization;
  }
}