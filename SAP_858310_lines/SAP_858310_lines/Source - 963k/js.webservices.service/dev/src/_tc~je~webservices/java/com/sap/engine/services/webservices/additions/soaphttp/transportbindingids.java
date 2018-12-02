package com.sap.engine.services.webservices.additions.soaphttp;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description: Contains the IDs of buildIn transportbiniding implementations.
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */

public interface TransportBindingIDs extends java.io.Serializable {

  /**
   * SOAP over HTTP transport binding ID
   */
  public static final String SOAPHTTP_TRANSPORTBINDING  =  "SOAPHTTP_TransportBinding";

  /**
   * MIME transport binding ID
   */
  public static final String MIME_TRANSPORTBINDING  =  "MIME_TransportBinding";

  /**
   * HTTP (POST and GET) transport binding ID
   */
  public static final String HTTP_TRANSPORTBINDING  =  "HTTP_TransportBinding";

}
