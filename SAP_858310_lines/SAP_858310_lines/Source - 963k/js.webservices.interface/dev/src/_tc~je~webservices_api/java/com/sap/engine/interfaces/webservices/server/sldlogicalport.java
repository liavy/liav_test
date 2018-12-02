/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.interfaces.webservices.server;

/**
 * This class represents a structure of an SLD LP
 * 
 * @author Alexander Zubev (alexander.zubev@sap.com)
 */
public class SLDLogicalPort {
  private String systemName;
  private String wsName;
  private String wsPortName;
  
  /**
   * @return The name key of the CIM_J2EEEngineCluster instance, under which the WebServicePort is bound.
   */
  public String getSystemName() {
    return systemName;
  }

  /**
   * @return The name key of the CIM_WebService instance
   */
  public String getWSName() {
    return wsName;
  }

  /**
   * @return The name key of the CIM_WebServicePort instance
   */
  public String getWSPortName() {
    return wsPortName;
  }

  /**
   * @param systemName The name key of the CIM_J2EEEngineCluster instance, under which the WebServicePort is bound
   */
  public void setSystemName(String systemName) {
    this.systemName = systemName;
  }

  /**
   * @param wsName The name key of the CIM_WebService instance
   */
  public void setWSName(String wsName) {
    this.wsName = wsName;
  }

  /**
   * @param wsPortName The name key of the CIM_WebServicePort instance
   */
  public void setWSPortName(String wsPortName) {
    this.wsPortName = wsPortName;
  }
}
