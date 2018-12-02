package com.sap.engine.services.webservices.jaxr.infomodel;

import com.sap.engine.services.webservices.jaxr.JAXRNestedException;

import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.UnsupportedCapabilityException;
import javax.xml.registry.infomodel.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

public class OrganizationImpl extends RegistryObjectImpl implements Organization {
  private Vector users;
  private Vector phoneNumbers;
  private Vector childServices;
  
  public OrganizationImpl(Connection connection) {
    super(connection);
    childServices = new Vector();
    users = new Vector();
    phoneNumbers = new Vector();
  }
  
  public void addChildOrganization(Organization organization) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(addChildOrganization)");
  }
  
  public void addChildOrganizations(Collection organization) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(addChildOrganizations)");
  }
  
  public void addService(Service service) throws JAXRException {
    service.setProvidingOrganization(this);
    childServices.addElement(service);
  }

  public void addServices(Collection services) throws JAXRException {
    Iterator it = services.iterator();
    while (it.hasNext()) {
      try {
        addService((Service) it.next());
      } catch (Exception e) {
        throw new JAXRNestedException(e);
      }
    }
  }
  
  public void addUser(User user) throws JAXRException {
    users.addElement(user);
    ((UserImpl) user).setOrganization(this);
  }
  
  public void addUsers(Collection users) throws JAXRException {
    Iterator it = users.iterator();
    while (it.hasNext()) {
      try {
        addUser((User) it.next());
      } catch (Exception e) {
        throw new JAXRNestedException(e);
      }
    }
  }
  
  public int getChildOrganizationCount() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getChildOrganizationCount)");
  }
  
  public Collection getChildOrganizations() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getChildOrganizations)");
  }
  
  public Collection getDescendantOrganizations() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getDescendantOrganizations)");
  }
  
  public Organization getParentOrganization()  throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getParentOrganization)");
  }

  public PostalAddress getPostalAddress() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getPostalAddress)");
  }

  public User getPrimaryContact() throws JAXRException {
    for (int i = 0; i < users.size(); i++) {
      User user = (User) users.elementAt(i);
      if (UserImpl.PRIMARY_CONTACT.equalsIgnoreCase(user.getType())) {
        return user;
      }
    }
    if (users.size() > 0) {
      User user = (User) users.elementAt(0);
      setPrimaryContact(user);
      return user;
    } else {
      throw new JAXRException("The primary Contact has not been set");
    }
  }
  
  public Organization getRootOrganization()  throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getRootOrganization)");
  }
  
  public Collection getServices() throws JAXRException {
    return childServices;
  }
  
  public Collection getTelephoneNumbers(String phoneType) throws JAXRException {
    if (phoneType == null) {
      return phoneNumbers;
    }
    Vector tel = new Vector(phoneNumbers.size());
    for (int i=0; i < phoneNumbers.size(); i++) {
      TelephoneNumber number = (TelephoneNumber) phoneNumbers.elementAt(i);
      if (phoneType.equals(number.getType())) {
        tel.addElement(number);
      }
    }
    return tel;
  }
  
  public Collection getUsers() throws JAXRException {
    return users;
  }
  
  public void removeChildOrganization(Organization organization) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(removeChildOrganization)");
  }
  
  public void removeChildOrganizations(Collection organization) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(removeChildOrganizations)");
  }

  public void removeService(Service service) throws JAXRException {
    if (childServices.remove(service)) {
      service.setProvidingOrganization(null);
    } else {
      InternationalString name = service.getName();
      throw new JAXRException("The organization does not contain this service: " + ((name != null) ? name.getValue() : "<NO NAME SPECIFIED>"));
    }
  }
  
  public void removeServices(Collection services) throws JAXRException {
    try {
      Service[] parts = (Service[]) services.toArray(new Service[services.size()]);

      for (int i = 0; i < parts.length; i++) {
        removeService(parts[i]);
      }
    } catch (Exception ex) {
      throw new JAXRNestedException(ex);
    }
  }

  public void removeUser(User user) throws JAXRException {
    if (users.remove(user)) {
      ((UserImpl) user).setOrganization(null);
    } else {
      InternationalString name = user.getName();
      throw new JAXRException("The organization does not contain this user: " + ((name != null) ? name.getValue() : "<NO NAME SPECIFIED>"));
    }
  }
  
  public void removeUsers(Collection users) throws JAXRException {
    try {
      User[] parts = (User[]) users.toArray(new User[users.size()]);

      for (int i = 0; i < parts.length; i++) {
        removeUser(parts[i]);
      }
    } catch (Exception ex) {
      throw new JAXRNestedException(ex);
    }
  }
  
  public void setPostalAddress(PostalAddress address) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(setPostalAddress)");
  }
  
  public void setPrimaryContact(User primaryContact) throws JAXRException {
    primaryContact.setType(UserImpl.PRIMARY_CONTACT);
    users.addElement(primaryContact);
  }

  public void setTelephoneNumbers(Collection phoneNumbers) throws JAXRException {
    this.phoneNumbers = new Vector(phoneNumbers);
  }
}