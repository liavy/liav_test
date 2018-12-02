/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.proxymappings;

import java.util.HashMap;

import com.sap.tc.logging.Location;

public abstract class ProxyMappingsReader {

  private static final Location LOCATION = Location.getLocation(ProxyMappingsReader.class);
  protected static ProxyMappingsReader instance = null;

  public synchronized static ProxyMappingsReader getInstance() throws javax.xml.rpc.ServiceException {
    if (instance == null) {
      if (LOCATION.beDebug()) {
        LOCATION.debugT("INSTANCE_NOT_AVAILABLE :ProxyMappingsReader getInstance()");
      }
    }

    return instance;
  }

  public abstract ProxyMappings[] getAllProxyMappings() throws javax.xml.rpc.ServiceException;

  public abstract HashMap<Integer, ProxyMappings> getAllProxyMappingsHashMap() throws javax.xml.rpc.ServiceException;

  public abstract ProxyMappings getProxyMappingsForPort(int port) throws javax.xml.rpc.ServiceException;

  public abstract ProxyMappings getWSDLMapping(int requestedPort) throws javax.xml.rpc.ServiceException;

  public abstract ProxyMappings getEndpointMapping(int requestedPort, String schemeForResponse) throws javax.xml.rpc.ServiceException;

}
