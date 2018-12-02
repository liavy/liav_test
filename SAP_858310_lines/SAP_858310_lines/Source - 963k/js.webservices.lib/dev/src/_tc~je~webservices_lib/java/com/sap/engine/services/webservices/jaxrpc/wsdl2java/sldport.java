/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java;

import com.sap.engine.interfaces.webservices.runtime.HTTPProxy;
import com.sap.engine.interfaces.webservices.runtime.HTTPProxyResolver;
import com.sap.engine.services.webservices.wsdl.WSDLDOMLoader;
import com.sap.engine.services.webservices.wsdl.WSDLDefinitions;

/**
 * @author Alexander Zubev (alexander.zubev@sap.com)
 */
public class SLDPort {
  private String wsdl;
  private String portName;
  
  public SLDPort(String wsdl, String portName) {
    this.wsdl = wsdl;
    this.portName = portName;
  }
  
  /**
   * @return
   */
  public String getPortName() {
    return portName;
  }

  /**
   * @return
   */
  public String getWsdl() {
    return wsdl;
  }

  /**
   * @param string
   */
  public void setPortName(String string) {
    portName = string;
  }

  /**
   * @param string
   */
  public void setWsdl(String string) {
    wsdl = string;
  }

  public WSDLDefinitions getAsDefinitions(HTTPProxyResolver proxyResolver) throws Exception {
    String proxyHost = null;
    int proxyPort = -1;
    if (wsdl.startsWith("http://") || wsdl.startsWith("https://")) {
      int schemaLength; 
      if (wsdl.startsWith("http://")) {
        schemaLength = 7;
      } else {
        schemaLength = 8;
      }
      int index = wsdl.indexOf("/", schemaLength);
      if (index == -1) {
        throw new Exception("Cannot determine host from WSDL: " + wsdl);
      }
      String hostPortPair = wsdl.substring(schemaLength, index);
      int colon = hostPortPair.indexOf(":");
      String host;
      int port;
      if (colon != -1) {
        host = hostPortPair.substring(0, colon);
        port = Integer.parseInt(hostPortPair.substring(colon + 1));
      } else {
        host = hostPortPair;
        port = 80;
      }
      if (host != null) {
        HTTPProxy proxy = proxyResolver.getHTTPProxyForHost(host);
        if (proxy != null && proxy.useProxyForAddress(host)) {
          proxyHost = proxy.getProxyHost();
          proxyPort = proxy.getProxyPort();
        }
      }
    }
        
    
    WSDLDOMLoader loader = new WSDLDOMLoader();
    if (proxyHost != null) {
      loader.setHttpProxy(proxyHost, "" + proxyPort);
    }
    return loader.loadWSDLDocument(wsdl);
  }
}
