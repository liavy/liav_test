package com.sap.engine.services.webservices.webservices630.server.deploy.common;

import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;

import java.util.Hashtable;
import java.util.Properties;

/**
 * Title: ModuleDeployResult
 * Description: The class is a container for deployment generated  module data.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class ModuleDeployResult {

  private Hashtable moduleCrcTable = new Hashtable();
  private Properties moduleMappings = new Properties();
  private String[] filesForClassLoader = new String[0];

  public ModuleDeployResult() {
  }

  public Hashtable getModuleCrcTable() {
    return moduleCrcTable;
  }

  public void setModuleCrcTable(Hashtable moduleCrcTable) {
    this.moduleCrcTable = moduleCrcTable;
  }

  public void addModuleCrcTable(Hashtable moduleCrcTable) {
    this.moduleCrcTable.putAll(moduleCrcTable);
  }

  public Properties getModuleMappings() {
    return moduleMappings;
  }

  public void setModuleMappings(Properties moduleMappings) {
    this.moduleMappings = moduleMappings;
  }

  public void addModuleMappings(Properties moduleMappings) {
    this.moduleMappings.putAll(moduleMappings);
  }

  public String[] getFilesForClassLoader() {
    return filesForClassLoader;
  }

  public void setFilesForClassLoader(String[] filesForClassLoader) {
    this.filesForClassLoader = filesForClassLoader;
  }

  public void addFilesForClassLoader(String[] filesForClassLoader) {
    this.filesForClassLoader = WSUtil.unifyStrings(new String[][]{this.filesForClassLoader, filesForClassLoader});
  }

  public void addModuleDeployResult(ModuleDeployResult moduleDeployResult) {
    if(moduleDeployResult == null) {
      return;
    }
    
    this.addModuleCrcTable(moduleDeployResult.getModuleCrcTable());
    this.addModuleMappings(moduleDeployResult.getModuleMappings());
    this.addFilesForClassLoader(moduleDeployResult.getFilesForClassLoader());
  }

}
