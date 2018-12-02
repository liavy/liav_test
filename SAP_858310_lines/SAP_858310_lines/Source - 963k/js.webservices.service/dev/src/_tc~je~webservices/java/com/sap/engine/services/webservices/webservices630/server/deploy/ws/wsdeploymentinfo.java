package com.sap.engine.services.webservices.webservices630.server.deploy.ws;

import com.sap.engine.lib.descriptors.ws04wsdd.WSDeploymentDescriptor;
import com.sap.engine.services.webservices.runtime.definition.WSRuntimeDefinition;

import java.util.ArrayList;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSDeploymentInfo {

  private WSRuntimeDefinition wsRuntimeDefinition = null;
  private WSDeploymentDescriptor wsDeploymentDescriptor = null;

  private ArrayList virtualInterfaces = null; // absolute paths
  private ArrayList virtualInterfaceEntries = null;

  private String wsdRefEntry = null;
  private String wsdRef = null;

  private String wsDocEntry = null;
  private String wsDocPath = null;

  // outside-in info
  private String wsdlRefEntry = null;
  private String wsdlRefPath = null;
  private String javaQNameMappingRefEntry = null;
  private String javaQNameMappingRefPath = null;

  private String wsWorkingDirectory = null;

  public WSDeploymentInfo() {

  }

  public WSDeploymentInfo(WSRuntimeDefinition wsRuntimeDefinition, WSDeploymentDescriptor wsDeploymentDescriptor) {
    this.wsRuntimeDefinition = wsRuntimeDefinition;
    this.wsDeploymentDescriptor = wsDeploymentDescriptor;
  }

  public WSRuntimeDefinition getWsRuntimeDefinition() {
    return wsRuntimeDefinition;
  }

  public void setWsRuntimeDefinition(WSRuntimeDefinition wsRuntimeDefinition) {
    this.wsRuntimeDefinition = wsRuntimeDefinition;
  }

  public WSDeploymentDescriptor getWsDeploymentDescriptor() {
    return wsDeploymentDescriptor;
  }

  public void setWsDeploymentDescriptor(WSDeploymentDescriptor wsDeploymentDescriptor) {
    this.wsDeploymentDescriptor = wsDeploymentDescriptor;
  }

  public ArrayList getVirtualInterfaces() {
    return virtualInterfaces;
  }

  public void setVirtualInterfaces(ArrayList virtualInterfaces) {
    this.virtualInterfaces = virtualInterfaces;
  }

  public void addVirtualInterface(String viPath) {
    if(virtualInterfaces == null) {
      virtualInterfaces = new ArrayList();
    }

    virtualInterfaces.add(viPath);
  }

  public ArrayList getVirtualInterfaceEntries() {
    return virtualInterfaceEntries;
  }

  public void setVirtualInterfaceEntries(ArrayList virtualInterfaceEntries) {
    this.virtualInterfaceEntries = virtualInterfaceEntries;
  }

  public void addVirtualInterfaceEntry(String virtualInterfaceEntry) {
    if(virtualInterfaceEntries == null) {
      virtualInterfaceEntries = new ArrayList();
    }

    virtualInterfaceEntries.add(virtualInterfaceEntry);
  }

  public String getWsdRefEntry() {
    return wsdRefEntry;
  }

  public void setWsdRefEntry(String wsdRefEntry) {
    this.wsdRefEntry = wsdRefEntry;
  }

  public String getWsdRef() {
    return wsdRef;
  }

  public void setWsdRef(String wsdRef) {
    this.wsdRef = wsdRef;
  }

  public String getWsDocEntry() {
    return wsDocEntry;
  }

  public void setWsDocEntry(String wsDocEntry) {
    this.wsDocEntry = wsDocEntry;
  }

  public String getWsDocPath() {
    return wsDocPath;
  }

  public void setWsDocPath(String wsDocPath) {
    this.wsDocPath = wsDocPath;
  }

  public boolean hasOutsideInDescriptor() {
    return wsdlRefEntry!= null && javaQNameMappingRefEntry != null;
  }

  public String getJavaQNameMappingRefPath() {
    return javaQNameMappingRefPath;
  }

  public void setJavaQNameMappingRefPath(String javaQNameMappingRefPath) {
    this.javaQNameMappingRefPath = javaQNameMappingRefPath;
  }

  public String getWsdlRefPath() {
    return wsdlRefPath;
  }

  public void setWsdlRefPath(String wsdlRefPath) {
    this.wsdlRefPath = wsdlRefPath;
  }

  public String getWsWorkingDirectory() {
    return wsWorkingDirectory;
  }

  public void setWsWorkingDirectory(String wsWorkingDirectory) {
    this.wsWorkingDirectory = wsWorkingDirectory;
  }

}