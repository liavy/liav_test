package com.sap.engine.services.webservices.jaxr.infomodel;

import java.net.URL;
import java.util.Collection;
import java.util.Vector;

import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.UnsupportedCapabilityException;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.TelephoneNumber;
import javax.xml.registry.infomodel.User;

public class UserImpl extends RegistryObjectImpl implements User {
  public static final String PRIMARY_CONTACT = "Primary Contact";
  
  private PersonName personName;
  private String type;
  private Organization organization;
  private Vector postalAddresses;
  private Vector phoneNumbers;
  private Vector emailAddresses;
  
  public UserImpl(Connection con, Organization o) {
    super(con);
    organization = o;
    emailAddresses = new Vector();
    postalAddresses = new Vector();
    phoneNumbers = new Vector();
  }
  
  public Collection getEmailAddresses() throws JAXRException {
    return emailAddresses;
  }
  
  public Organization getOrganization() throws JAXRException {
    return organization;
  }

  public void setOrganization(Organization org) throws JAXRException {
    organization = org;
  }

  public PersonName getPersonName() throws JAXRException {
    return personName;
  }

  public Collection getPostalAddresses() throws JAXRException {
    return postalAddresses;
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
  
  public String getType() throws JAXRException {
    return type;
  }
  
  public URL getUrl() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getUrl)");
  }
  
  public void setEmailAddresses(Collection emailAddresses) throws JAXRException {
    this.emailAddresses = new Vector(emailAddresses);
  }

  public void setPersonName(PersonName personName) throws JAXRException {
    this.personName = personName;
  }
  
  public void setPostalAddresses(Collection addresses) throws JAXRException {
    postalAddresses = new Vector(addresses);
  }

  public void setTelephoneNumbers(Collection phoneNumbers) throws JAXRException {
    this.phoneNumbers = new Vector(phoneNumbers);
  }

  public void setType(String type) throws JAXRException {
    this.type = type;
  }

  public void setUrl(URL url) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(setUrl)");
  }
}