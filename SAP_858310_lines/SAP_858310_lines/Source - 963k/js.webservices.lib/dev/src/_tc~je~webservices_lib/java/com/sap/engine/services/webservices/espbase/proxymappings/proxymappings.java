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

public class ProxyMappings {
  private String host;
  private int port;
  private String scheme;
  private boolean override = false;
  private boolean complete = false;

  public ProxyMappings() {
    host = null;
    port = -1;
    scheme = "http";
  }

  public ProxyMappings(String host, int port, String scheme, boolean override) {
    this.host = host;
    this.port = port;
    this.scheme = scheme;
    this.override = override;
    setComplete();
  }

  public boolean isOverride() {
    return override;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
    setComplete();
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
    setComplete();
  }

  public String getScheme() {
    return scheme;
  }

  public void setScheme(String scheme) {
    this.scheme = scheme;
    setComplete();
  }

  public boolean isComplete() {
    return complete;
  }

  public boolean equals(ProxyMappings p) {
    if (((this.host == null && p.host == null) || this.host.equals(p.host))
            && this.port == p.port && this.override == override
            && ((this.scheme == null && p.scheme == null) || this.scheme.equals(p.scheme))) {
      return true;
    } else {
      return false;
    }
  }

  private void setComplete() {
    if (scheme != null && host != null && port != -1) {
      complete = true;
    } else {
      complete = false;
    }
  }
}
