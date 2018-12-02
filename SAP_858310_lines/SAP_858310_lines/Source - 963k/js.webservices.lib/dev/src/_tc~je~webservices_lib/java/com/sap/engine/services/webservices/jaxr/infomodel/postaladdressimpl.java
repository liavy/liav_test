package com.sap.engine.services.webservices.jaxr.infomodel;

import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.PostalAddress;

public class PostalAddressImpl extends ExtensibleObjectImpl implements PostalAddress {
  private String street;
  private String streetNumber;
  private String city;
  private String state;
  private String postalCode;
  private String country;
  private String type;
  private ClassificationScheme postalScheme;
  private Connection connection;
  
  public PostalAddressImpl(Connection con) {
    super(con);
    connection = con;
    street = "";
    streetNumber = "";
    city = "";
    state = "";
    postalCode = "";
    country = "";
    type = "";
  }
  
  public String getStreet() throws JAXRException {
    return street;
  }
  
  public void setStreet(String street) throws JAXRException {
    this.street = street;
  }
  
  public String getStreetNumber() throws JAXRException {
    return streetNumber;
  }
  
  public void setStreetNumber(String street) throws JAXRException {
    streetNumber = street;
  }
  
  public String getCity() throws JAXRException {
    return city;
  }
  
  public void setCity(String city) throws JAXRException {
    this.city = city;
  }
  
  public String getStateOrProvince() throws JAXRException {
    return state;
  }
  
  public void setStateOrProvince(String stateOrProvince) throws JAXRException {
    state = stateOrProvince;
  }
  
  public String getPostalCode() throws JAXRException {
    return postalCode;
  }
  
  public void setPostalCode(String postalCode) throws JAXRException {
    this.postalCode = postalCode;
  }
  
  public String getCountry() throws JAXRException {
    return country;
  }
  
  public void setCountry(String country) throws JAXRException {
    this.country = country;
  }
  
  public String getType() throws JAXRException {
    return type;
  }
  
  public void setType(String type) throws JAXRException {
    this.type = type;
  }
  
  public void setPostalScheme(ClassificationScheme scheme) throws JAXRException {
    postalScheme = scheme;
  }
  
  public ClassificationScheme getPostalScheme() throws JAXRException {
    if (postalScheme != null) {
      return postalScheme;
    } else {
      try {
        return connection.getRegistryService().getDefaultPostalScheme();
      } catch (NullPointerException npe) {
        return null;
      }
    }
  }
}