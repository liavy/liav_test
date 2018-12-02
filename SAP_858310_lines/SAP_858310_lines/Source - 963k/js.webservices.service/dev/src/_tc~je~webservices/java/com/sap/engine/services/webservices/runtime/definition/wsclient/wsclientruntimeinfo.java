package com.sap.engine.services.webservices.runtime.definition.wsclient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import com.sap.engine.interfaces.webservices.runtime.definition.IWSClient;
import com.sap.engine.interfaces.webservices.runtime.definition.WSClientIdentifier;
import com.sap.engine.services.webservices.exceptions.WSException;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ServiceBaseServer;
import com.sap.engine.services.webservices.webservices630.server.deploy.common.WSBaseConstants;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.WSClientDirsHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.WSClientRuntimetHelper;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.WSClientsConstants;
import com.sap.engine.services.webservices.wsdl.WSDLDOMLoader;
import com.sap.engine.services.webservices.wsdl.WSDLDefinitions;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSClientRuntimeInfo implements IWSClient {

  protected String version = WSBaseConstants.VERSION_630;
  protected String linkServiceRefName = null;

  protected WSClientIdentifier wsClientId = null;
  protected String serviceInterfaceName = null;

  protected String packageName = null;
  protected String[] wsdlFileNames = new String[0];
  protected String logicalPortsFileName = null;
  protected String[] uriMappingFiles = new String[0];
  protected String packageMappingFile = null;

  protected ComponentDescriptor[] componentDescriptors = new ComponentDescriptor[0];

  protected WSClientDirsHandler wsClientDirsHandler = null;//$JL-SER$

  public WSClientRuntimeInfo() {

  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getLinkServiceRefName() {
    return linkServiceRefName;
  }

  public void setLinkServiceRefName(String linkServiceRefName) {
    this.linkServiceRefName = linkServiceRefName;
  }

  public WSClientIdentifier getWsClientId() {
    return wsClientId;
  }

  public void setWsClientId(WSClientIdentifier wsClientId) {
    this.wsClientId = wsClientId;
  }

  public String getServiceInterfaceName() {
    return serviceInterfaceName;
  }

  public void setServiceInterfaceName(String serviceInterfaceName) {
    this.serviceInterfaceName = serviceInterfaceName;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String[] getWsdlFileNames() {
    return wsdlFileNames;
  }

  public String[] getWsdlFileNamesRelToWSClientOwnDir() {
    return wsClientDirsHandler.getWsdlRelPaths(wsdlFileNames);
  }

  public String[] getWsdlFullFileNames() {
    return wsClientDirsHandler.getWsdlPaths( wsdlFileNames);
  }

  public void setWsdlFileNames(String[] wsdlFileNames) {
    this.wsdlFileNames = wsdlFileNames;
  }

  public String getLogicalPortsFileName() {
    return logicalPortsFileName;
  }

  public String getLogicalPortsFileNameRelToWSClientOwnDir() {
    return wsClientDirsHandler.getLogPortsRelPath(logicalPortsFileName);
  }

  public String getLogicalPortsFullFileName() {
    return wsClientDirsHandler.getLogPortsPath(logicalPortsFileName);
  }

  public void setLogicalPortsFileName(String logicalPortsFileName) {
    this.logicalPortsFileName = logicalPortsFileName;
  }

  public boolean hasLogicalPortsFile() {
    return logicalPortsFileName != null;
  }

  public String[] getUriMappingFiles() {
    return uriMappingFiles;
  }

  public String[] getUriMappingFileNamesRelToWSClientOwnDir() {
    return wsClientDirsHandler.getUriMappingRelPaths(uriMappingFiles);
  }

  public String[] getUriMappingFullFileNames() {
    return wsClientDirsHandler.getUriMappingPaths(uriMappingFiles);
  }

  public void setUriMappingFiles(String[] uriMappingFiles) {
    this.uriMappingFiles = uriMappingFiles;
  }

  public boolean hasUriMappingFiles() {
    return (uriMappingFiles != null && uriMappingFiles.length != 0);
  }

  public String getPackageMappingFile() {
    return packageMappingFile;
  }

  public String getPackageMappingFileRelToWSClientOwnDir() {
    return wsClientDirsHandler.getPackageMappingRelPath(packageMappingFile);
  }

  public String getPackageMappingFullFileName() {
    return wsClientDirsHandler.getPackageMappingPath(packageMappingFile);
  }

  public void setPackageMappingFile(String packageMappingFile) {
    this.packageMappingFile = packageMappingFile;
  }

  public boolean hasPackageMappingFile() {
    return packageMappingFile != null;
  }

  public ComponentDescriptor[] getComponentDescriptors() {
    return componentDescriptors;
  }

 
  public Object[] getLogicalPorts() throws Exception {
    WSClientRuntimetHelper helper = new WSClientRuntimetHelper();
    ServiceBaseServer serviceInterface;
    try {
      serviceInterface = (ServiceBaseServer) getServiceImpl();
    } catch (Exception e) {
      WSClientIdentifier wsClientID = getWsClientId();
      String serviceJndiName = WSClientRuntimetHelper.getServiceJndiName(wsClientID);
      
      throw new WSException("webservices_4001", new Object[] {wsClientID.getApplicationName(), serviceJndiName}, e);
    }
    return serviceInterface.getLogicalPorts();
  }  
  
  public void setComponentDescriptors(ComponentDescriptor[] componentDescriptors) {
    this.componentDescriptors = componentDescriptors;
  }

  public WSClientDirsHandler getWsClientDirsHandler() {
    return wsClientDirsHandler;
  }

  public void setWsClientDirsHandler(WSClientDirsHandler wsClientDirsHandler) {
    this.wsClientDirsHandler = wsClientDirsHandler;
  }

  public String toString() {
    String nl = WSBaseConstants.LINE_SEPARATOR;
    String result = "";

    result += wsClientId.toString() + nl;
    result += "Service interface name: " + serviceInterfaceName + nl;
    result += "Package : " + packageName + nl;

    int wsdlsSize = getWsdlFileNames().length;
    for (int i = 0; i < wsdlsSize; i++) {
      result += "Wsdl file[" + i + "] : " + wsdlFileNames[i] + nl;
    }

    if (hasLogicalPortsFile()) {
      result += " Logical ports file name: " + logicalPortsFileName + nl;
    }

    int uriMappingsSize = uriMappingFiles.length;
    for (int i = 0; i < uriMappingsSize; i++) {
      result += "Uri mapping file: " + uriMappingFiles[i] + nl;
    }

    if (hasPackageMappingFile()) {
      result += "Package mapping file: " + packageMappingFile + nl;
    }

    int componentsSize = getComponentDescriptors().length;
    for (int i = 0; i < componentsSize; i++) {
      result += "Client[" + i + "] = " + componentDescriptors[i].toString() + nl;
    }

    return result;
  }

  public Object getWSDLDefinitions() throws Exception {
    boolean hasUriMappings = hasUriMappingFiles();
    Properties[] uriMappingProps = new Properties[0];
    if (hasUriMappings) {
      uriMappingProps = loadUriMappingProps(getUriMappingFullFileNames());
    }

    WSDLDefinitions def = null;
    WSDLDOMLoader loader = new WSDLDOMLoader();
    String[] wsdlFiles = getWsdlFullFileNames();
    for(int i = 0; i < wsdlFiles.length; i++) {
      String wsdlFile = wsdlFiles[i];
      Properties uriMappingProperties = new Properties();

      if (hasUriMappings) {
        uriMappingProperties = uriMappingProps[i];
      }
      WSDLDefinitions newDef = loader.loadMirrorWSDLDocument(wsdlFile, uriMappingProperties);
      if (def == null) {
        def = newDef;
      } else {
        loader.joinWSDL(def, newDef);
      }
    }
    return def;
  }
  
  private static Properties[] loadUriMappingProps(String[] uriMappingFileNames) throws IOException {
    if (uriMappingFileNames == null) {
      return new Properties[0];
    }

    Properties[] uriMappingProps = new Properties[uriMappingFileNames.length];
    for(int i = 0; i < uriMappingFileNames.length; i++) {
      String uriMappingFileName = uriMappingFileNames[i];
      Properties uriMappingProperties = new Properties();
      InputStream uriMappingInput = null;
      uriMappingInput = new FileInputStream(uriMappingFileName);
      uriMappingProperties = new Properties();
      try {
        uriMappingProperties.load(uriMappingInput);
      } finally {
        uriMappingInput.close();
      }

      uriMappingProps[i] = uriMappingProperties;
    }

    return uriMappingProps;
  }
  
  public Object getServiceImpl() throws Exception {
    WSClientIdentifier wsClientID = getWsClientId();
    String serviceJndiName = WSClientRuntimetHelper.getServiceJndiName(wsClientID);

    Hashtable env = new Hashtable();
    env.put("domain", "true");
    Context ctx = new InitialContext(env);
    Context proxiesCtx = (Context) ctx.lookup(WSClientsConstants.WS_CLIENTS_PROXY_CONTEXT);
    return proxiesCtx.lookup(serviceJndiName);
  }
}
