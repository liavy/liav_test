package com.sap.engine.services.webservices.webservices630.server.deploy.common;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Properties;

/**
 * Title: AppDeployResult
 * Description: This class represents the base web services/ ws clients deployment result.
 * Copyright: Copyright (c) 2003
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class AppDeployResult {

  private Hashtable moduleCrcTable = null;
  private Properties deployedComponentsPerModule = null;

  private String[] deployedComponentNames = new String[0];
  private String[] filesForClassloader = new String[0];

  private Vector warnings = new Vector();

  public AppDeployResult() {
  }

  public Hashtable getModuleCrcTable() {
    return moduleCrcTable;
  }

  public void setModuleCrcTable(Hashtable moduleCrcTable) {
    this.moduleCrcTable = moduleCrcTable;
  }

  public Properties getDeployedComponentsPerModule() {
    return deployedComponentsPerModule;
  }

  public void setDeployedComponentsPerModule(Properties deployedComponentsPerModule) {
    this.deployedComponentsPerModule = deployedComponentsPerModule;
  }

  public String[] getDeployedComponentNames() {
    return deployedComponentNames;
  }

  public void setDeployedComponentNames(String[] deployedComponentNames) {
    this.deployedComponentNames = deployedComponentNames;
  }

  public String[] getFilesForClassloader() {
    return filesForClassloader;
  }

  public void setFilesForClassloader(String[] filesForClassloader) {
    this.filesForClassloader = filesForClassloader;
  }

  public Vector getWarnings() {
    return warnings;
  }

  public void setWarnings(Vector warnings) {
    this.warnings = warnings;
  }

  public void addWarning(String warning) {
    this.warnings.add(warning);
  }

  public void addWarnings(Vector warnings) {
    this.warnings.addAll(warnings);
  }

}
