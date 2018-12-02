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
package com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding;

import java.util.Enumeration;

/**
 * Interface for configuring HTTP transport.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public interface HTTPTransportInterface {

  /**
   * Returns array of header values for given header name.
   * @param headerName
   * @return
   */
  public String[] getHeader(String headerName);

  /**
   * Adds header values to the http request.
   * @param headerName
   * @param headerValue
   */
  public void setHeader(String headerName, String headerValue);

  /**
   * Returns endpoint url for this connection.
   * @return
   */
  public String getEndpoint();

  /**
   * Sets new Endpoint.
   */
  public void setEndpoint(String endpoint) throws Exception;

  /**
   * Returns available headers.
   * @return
   */
  public Enumeration listHeaders();

  /**
   * Sets array of values to a header.
   * @param headerName
   * @param headerValues
   */
  public void setHeader(String headerName, String[] headerValues);

}
