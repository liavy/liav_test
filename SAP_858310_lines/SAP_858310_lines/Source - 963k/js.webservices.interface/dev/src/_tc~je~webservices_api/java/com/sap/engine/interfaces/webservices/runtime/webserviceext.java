/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.interfaces.webservices.runtime;

/**
 * @author Alexander Zubev
 */
public class WebServiceExt implements Comparable {
  private String nameURI;
  private String localName;
  private String uddiKey;
  private String description;
  private String[] endpointURIs = new String[0];
  private String[] wsdlStyles = new String[0];
  private UDDIPublicationExt[] publications;

  public String getNameURI() {
    return nameURI;
  }

  public void setNameURI(String nameURI) {
    this.nameURI = nameURI;
  }

  public String getLocalName() {
    return localName;
  }

  public void setLocalName(String localName) {
    this.localName = localName;
  }

  /**
   * @return The UDDI Key
   * @deprecated This method is replaced by the getUDDIPublications method and may be removed in future versions
   */
  public String getUddiKey() {
    return uddiKey;
  }

  /**
   * @param uddiKey
   * @deprecated This method is replaced by the setUDDIPublications method and may be removed in future versions 
   */
  public void setUddiKey(String uddiKey) {
    this.uddiKey = uddiKey;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String[] getEndpointURIs() {
    return endpointURIs;
  }

  public void setEndpointURIs(String[] endpointURIs) {
    this.endpointURIs = endpointURIs;
  }
  
  public String[] getWSDLStyles() {
    return wsdlStyles;
  }
  
  public void setWSDLStyles(String[] wsdlStyles) {
    this.wsdlStyles = wsdlStyles;
  }
  
  public UDDIPublicationExt[] getUDDIPublications() {
    return publications;
  }
  
  public void setUDDIPublication(UDDIPublicationExt[] publications) {
    this.publications = publications;
  }
  
  public int compareTo(Object obj) {
    WebServiceExt wsObj = (WebServiceExt) obj; //throw ClassCast if the obj is not an instance of WebServiceExt
    if (obj == null) {
      throw new NullPointerException("Passed object cannot be null");
    }
    return this.localName.compareToIgnoreCase(wsObj.getLocalName());
  }
}
