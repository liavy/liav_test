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
package com.sap.engine.services.webservices.jaxrpc.wsdl2java;

import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPortType;

/**
 * This class holds container client information and is set only in the context of clients that are deployed on the server.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class RuntimeInformation {

  private String applicationName;
  private String jndiName;
  private String archiveName;
  private String logicalPortName;
  private String endpoint;
  private String sldSystemName;
  private String sldWServiceName;
  private String sldWServicePort;
  private LogicalPortType logicalPortType;

  public LogicalPortType getLogicalPortType() {
    return logicalPortType;
  }

  public void setLogicalPortType(LogicalPortType logicalPortType) {
    this.logicalPortType = logicalPortType;
  }

  /**
   * Returns SLD System name for this endpoint.
   * @return
   */
  public String getSLDSystemName() {
    return sldSystemName;
  }

  /**
   * Sets SLD System name for this endpoint.
   * @param sldSystemName
   */
  public void setSLDSystemName(String sldSystemName) {
    this.sldSystemName = sldSystemName;
  }

  /**
   * Returns SLD Webservice name for this endpoint.
   * @return
   */
  public String getSLDWServiceName() {
    return sldWServiceName;
  }

  /**
   * Sets SLD Webservice name for this endpoint.
   * @param sldWServiceName
   */
  public void setSLDWServiceName(String sldWServiceName) {
    this.sldWServiceName = sldWServiceName;
  }

  /**
   * Returns SLD Webservice port.
   * @return
   */
  public String getSLDWServicePort() {
    return sldWServicePort;
  }

  /**
   * Sets SLD Webservice port for this endpoint.
   * @param sldWServicePort
   */
  public void setSLDWServicePort(String sldWServicePort) {
    this.sldWServicePort = sldWServicePort;
  }

  /**
   * Returns application name of this endpoint.
   * @return
   */
  public String getApplicationName() {
    return applicationName;
  }

  /**
   * Sets application name for this endpoint.
   * @param applicationName
   */
  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  /**
   * Returns jndi name of this endpoint.
   * @return
   */
  public String getJndiName() {
    return jndiName;
  }

  /**
   * Sets jndi name of this endpoint (This does not rebind the endpoint).
   * @param jndiName
   */
  public void setJndiName(String jndiName) {
    this.jndiName = jndiName;
  }

  /**
   * Returns the archive name of this endpoint.
   * @return
   */
  public String getArchiveName() {
    return archiveName;
  }

  /**
   * Sets the archive name of this endpoint (must be used only my the envrinment).
   * @param archiveName
   */
  public void setArchiveName(String archiveName) {
    this.archiveName = archiveName;
  }

  /**
   * Returns the logical port of this endpoint.
   * @return
   */
  public String getLogicalPortName() {
    return logicalPortName;
  }

  /**
   * Sets this logical port name of this endpoint.
   * @param logicalPortName
   */
  public void setLogicalPortName(String logicalPortName) {
    this.logicalPortName = logicalPortName;
  }

  /**
   * Returns the endpoint URL.
   * @return
   */
  public String getEndpoint() {
    return endpoint;
  }

  /**
   * Sets the endpoint URL.
   * @param endpoint
   */
  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

}
