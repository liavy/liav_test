package com.sap.engine.services.webservices.webservices630.server.deploy.common;

import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;

import java.util.Properties;
import java.util.Enumeration;

/**
 * Title: DeployResult
 * Description: The class is a container for web services/ws clients deployment generated data.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */
public class DeployResult {

  private Properties deployedWSPerModule = new Properties();
  private String[] filesForClassLoader = new String[0];
  private String[] deployedComponentNames = new String[0];

  public DeployResult() {   
  }

  public Properties getDeployedWSPerModule() {
    return deployedWSPerModule;
  }

  public void setDeployedWSPerModule(Properties deployedWSPerModule){
    this.deployedWSPerModule = deployedWSPerModule;
  }

  public void addDeployedWSPerModule(Properties deployedWSPerModule) {
    this.deployedWSPerModule = WSUtil.unifyProperties(this.deployedWSPerModule, deployedWSPerModule, "");
  }

  public String[] getFilesForClassLoader() {
    return filesForClassLoader;
  }

  public void setFilesForClassLoader(String filesForClassLoader[]) {
    this.filesForClassLoader = filesForClassLoader;
  }

  public void addFilesForClassLoader(String[] filesForClassLoader) {
    this.filesForClassLoader = WSUtil.unifyStrings(new String[][] {this.filesForClassLoader, filesForClassLoader});
  }

  public String[] getDeployedComponentNames() {
    return deployedComponentNames;
  }

  public void setDeployedComponentNames(String deployedComponentNames[]){
    this.deployedComponentNames = deployedComponentNames;
  }

  public void addDeployedComponentNames(String deployedComponentNames[]) {
    this.deployedComponentNames = WSUtil.unifyStrings(new String[][] {this.deployedComponentNames, deployedComponentNames});
  }

  public void addDeployResult(DeployResult deployResult){
    if(deployResult == null) {
      return;
    }

    addDeployedWSPerModule(deployResult.getDeployedWSPerModule());
    addDeployedComponentNames(deployResult.getDeployedComponentNames());
    addFilesForClassLoader(deployResult.getFilesForClassLoader());
  }

  public String toString() {
    StringBuffer strBuffer = new StringBuffer();
    strBuffer.append(toStringdeployedComponentNames());
    strBuffer.append(toStringFilesForClassLoader());
    strBuffer.append(toStringDeployedComponentPerModule());
    return strBuffer.toString();
  }

  public String toStringdeployedComponentNames() {
    String nl = System.getProperty("line.separator");
    StringBuffer strBuffer = new StringBuffer();

    if(deployedComponentNames.length == 0) {
      strBuffer.append("No deployed components. ");
    } else {
      strBuffer.append("Deployed component names: " + nl);
      for(int i = 0; i < deployedComponentNames.length; i++) {
        strBuffer.append(deployedComponentNames[i] + nl);
      }
    }

    return strBuffer.toString();
  }

  public String toStringFilesForClassLoader() {
    String nl = System.getProperty("line.separator");
    StringBuffer strBuffer = new StringBuffer();

    if(filesForClassLoader.length == 0) {
        strBuffer.append("No files for classloader. ");
    } else {
      strBuffer.append("Files for classloader: " + nl);
      for(int i = 0; i < filesForClassLoader.length; i++) {
        strBuffer.append(filesForClassLoader[i] + nl);
      }
    }

    return strBuffer.toString();
  }

  public String toStringDeployedComponentPerModule() {
    String nl = System.getProperty("line.separator");
    StringBuffer strBuffer = new StringBuffer();

    if(deployedWSPerModule.size() == 0) {
      strBuffer.append("No deployed components per module. ");
    } else {
      strBuffer.append("Deployed components per module: " + nl);
      String moduleName;
      for(Enumeration enum1 = deployedWSPerModule.keys();enum1.hasMoreElements(); strBuffer.append(deployedWSPerModule.get(moduleName) + nl)) {
        moduleName = (String)enum1.nextElement();
        strBuffer.append(moduleName + ":" + nl);
      }
    }

    return strBuffer.toString();
  }

}
