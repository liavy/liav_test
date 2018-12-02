/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.espbase.client.api;

import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.rmi.MarshalException;
import java.rmi.UnmarshalException;

/**
 * Public client interface for client SOAPHeader manipulation.
 * This protocol is available on SOAP based webservices only.
 * Request soap headers
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public interface SOAPHeaderInterface {

  /**
   * Returns response SOAP header as DOM Element by it's QName.
   * @param headerName
   * @return
   */
  public org.w3c.dom.Element getInputHeader(QName headerName);

  /**
   * Returns response SOAP header by it's QName and deserializes it as an instance of the headerType
   * provided. For this deserialization to work the header element should be declared in the
   * Web service client WSDL Schema using "xsd:element" declaration.
   * @param headerName
   * @param headerType
   * @return
   */
  public Object getInputHeader(QName headerName, Class headerType) throws UnmarshalException;


  /**
   * Sets request header for the next SOAP request.
   * You can pass org.w3c.dom.Element and Null header name or typed object and
   * xsd:element name from the WSDL Schema.
   * @param headerName
   * @param headerContent
   */
  public void setOutputHeader(QName headerName, Object headerContent) throws MarshalException;

}
