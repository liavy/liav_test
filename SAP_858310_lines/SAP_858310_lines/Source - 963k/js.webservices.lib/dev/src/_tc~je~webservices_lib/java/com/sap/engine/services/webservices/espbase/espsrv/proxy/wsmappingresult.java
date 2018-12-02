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
package com.sap.engine.services.webservices.espbase.espsrv.proxy;

import java.io.Serializable;

/**
 * @author Nikolai Dimitrov
 * @version ENHP1
 */
public class WSMappingResult implements Serializable {

  private StringBuilder transportMapping;

  //if true then it has been mapped
  boolean mapped;

  public WSMappingResult() {
  }

  public StringBuilder getTransportMapping() {
    return transportMapping;
  }

  public void setTransportMapping(StringBuilder transportMapping) {
    this.transportMapping = transportMapping;
  }

  public boolean isMapped() {
    return mapped;
  }

  public void setMapped(boolean mapped) {
    this.mapped = mapped;
  }

  public String toString() {
    return "WSMappingResult{" +
            "transportMapping=" + transportMapping +
            ", mapped=" + mapped +
            '}';
  }
}
