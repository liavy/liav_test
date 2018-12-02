/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.interfaces.webservices.uddi4j;

import java.util.Hashtable;

/**
 * This interface can be used in order to connect uddi4j with the engine
 * and the jaxm implementation
 *
 * @author Alexander Zubev
 */
public interface SAPTransportInterface {

  /**
   * This constant is used for mapping the real request into the hashtable.
   * The request must be an org.w3c.dom.Element object
   */
  public static final String UDDI_ELEMENT = "UDDI_ELEMENT";

  /**
   * This constant is used for mapping the URL to which the request will be sent
   * The URL must be a java.net.URL object
   */
  public static final String URL = "URL";

  /**
   * This constant is used for mapping the proxy hostname into the hashtable
   * The hostname must be a String
   */
  public static final String PROXY_HOST = "PROXY_HOST";

  /**
   * This constant is used for mapping the proxy port number into the hashtable
   * The port number must be a String
   */
  public static final String PROXY_PORT = "PROXY_PORT";

  /**
   * This method is used for sending the UDDI request.
   *
   * @param parameters A hashtable containing a mapping between the decalred constants and the real objects
   * @return The SOAP-BODY element as an org.w3c.dom.Element object
   * @throws Exception in case of I/O errors or a SOAP exception.
   */
  public Object send(Hashtable parameters) throws Exception;
}
