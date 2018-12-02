package com.sap.engine.services.webservices.webservices630.server.deploy.wsclient;

import com.sap.engine.services.webservices.webservices630.server.deploy.common.ModuleDirsHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WebServicesConstants;

import java.util.Properties;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;

/**
 * Title: WSClientDirsHandler
 * Description: The class contains methods, that provides access to ws client deploy files, located on the local file system.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSClientDirsHandler extends ModuleDirsHandler implements WSClientFilesLocationHandler {

  public static final String WS_CLIENT_DIR_PREFIX = "ws_cl_";

  private String wsClientDirectory = null;
  private Properties mappings = null;

  private String descriptorsRelDir = null;
  private String descriptorsDir = null;
  private String logPortsRelDir = null;
  private String logPortsDir = null;
  private String wsdlRelDir = null;
  private String wsdlDir = null;
  private String jarsRelDir = null;
  private String jarsDir = null;
  private String jarFileName = null;
  private String jarPath = null;

  public static String getWSClientsRelConfigName() {
    return WSClientsConstants.WS_CLIENTS_CONFIG_NAME;
  }

  public static String getWSClientsConfigName(String wsContainerConfigPath) {
    return wsContainerConfigPath + WSClientsConstants.SEPARATOR + getWSClientsRelConfigName();
  }

  public static String getWSClientsRelDir() {
    return WSClientsConstants.WS_CLIENTS_CONFIG_NAME;
  }

  public static String getWSClientsDir(String wsContainerDir) {
    return wsContainerDir + WSClientsConstants.SEPARATOR + getWSClientsRelDir();
  }

  public static String getWSClientsWorkingDir(String wsContainerWorkingDir) {
    return wsContainerWorkingDir + WSClientsConstants.SEPARATOR + WSClientsConstants.WS_CLIENTS_CONFIG_NAME;
  }

  public static String getWSClientWorkingDir(String wsClientsWorkingDir, int number) {
    return wsClientsWorkingDir + WSClientsConstants.SEPARATOR + WS_CLIENT_DIR_PREFIX + number;
  }

  public static String getDefaultWSClientWorkingDir(String wsClientsWorkingDir, String serviceRefName) {
    return wsClientsWorkingDir + WSClientsConstants.SEPARATOR + getAllowedString(serviceRefName);
  }

  public static String getWSClientGeneratedDir(String wsClientWorkingDir) {
    return wsClientWorkingDir + WSClientsConstants.SEPARATOR + WSClientsConstants.GENERATED_DIR_NAME;
  }

  public static Properties generateMappings(String serviceRefName, int index) {
    Properties mappings = generateBaseMappings(index);
    String jarFileName = getDefaultJarFileName(serviceRefName);
    mappings.setProperty(WSClientsConstants.JAR_FILE_NAME, jarFileName);

    return mappings;
  }

  public static Properties generateDefaultMappings(String serviceRefName) {
    Properties mappings = generateBaseDefaultMappings();
    mappings.setProperty(WSClientsConstants.JAR_FILE_NAME, getDefaultJarFileName(serviceRefName));

    return mappings;
  }

  public static Properties generateBaseMappings(int index) {
    Properties mappings = generateBaseDefaultMappings();
    mappings.setProperty(WSClientsConstants.INDEX, new Integer(index).toString());
    return mappings;
  }

  public static Properties generateBaseDefaultMappings() {
    Properties mappings = new Properties();
    mappings.setProperty(WSClientsConstants.DESCRIPTORS_NAME, WSClientsConstants.DESCRIPTORS_NAME);
    mappings.setProperty(WSClientsConstants.LOG_PORTS_NAME, WSClientsConstants.LOG_PORTS_NAME);
    mappings.setProperty(WSClientsConstants.WSDL_NAME, WSClientsConstants.WSDL_NAME);
    mappings.setProperty(WSClientsConstants.JARS_NAME, WSClientsConstants.JARS_NAME);

    return mappings;
  }

  public static String getDefaultJarFileName(String serviceRefName) {
    return getAllowedString(serviceRefName) + ".jar";
  }

  public static void updateDefaultMappings(String serviceRefName, Properties mappings) {
    mappings.setProperty(WSClientsConstants.JAR_FILE_NAME, getDefaultJarFileName(serviceRefName));
  }

  public static String getDefaultWSClientDir(String wsClientsDir, String serviceRefName) {
    return wsClientsDir + WSClientsConstants.SEPARATOR + getAllowedString(serviceRefName);
  }

  public static String getWSClientDir(String wsClientsDir, int number) {
    return wsClientsDir + WSClientsConstants.SEPARATOR + WS_CLIENT_DIR_PREFIX + number;
  }

  public static String getMappingsFileName() {
    return WSClientsConstants.MAPPINGS_FILE_NAME;
  }

  public static String getMappingsPath(String wsClientDir) {
    return getMappingsParentDir(wsClientDir) + WSClientsConstants.SEPARATOR + getMappingsFileName();
  }

  public static String getMappingsParentRelDir() {
    return "";
  }

  public static String getMappingsParentDir(String wsClientDir) {
    return wsClientDir + WSClientsConstants.SEPARATOR + getMappingsParentRelDir();
  }

  public static String getDescriptorsRelDir(Properties mappings) {
    return mappings.getProperty(WSClientsConstants.DESCRIPTORS_NAME);
  }

  public static String getDescriptorsDir(String wsClientDir, Properties mappings) {
    return wsClientDir + WSClientsConstants.SEPARATOR + getDescriptorsRelDir(mappings);
  }

  public static String getWSClientsDeploymentDescriptorFileName() {
    return WSClientsConstants.WS_CLIENTS_DEPLOYMENT_DESCRIPTOR;
  }

  public static String getWSClientsDeploymentDescriptorPath(String wsClientDir, Properties mappings) {
    return getDescriptorsDir(wsClientDir, mappings) + WSClientsConstants.SEPARATOR + WSClientsConstants.WS_CLIENTS_DEPLOYMENT_DESCRIPTOR;
  }

  public static String getWSClientsDDescriptorParentRelDir(Properties mappings) {
    return getDescriptorsRelDir(mappings);
  }

  public static String getWSClientsDDescriptorParentDir(String wsClientDir, Properties mappings) {
    return getDescriptorsDir(wsClientDir, mappings);
  }

  public static String getWSClientsDeploymentDescriptorsDir(String wsClientDir, Properties mappings) {
    return getDescriptorsDir(wsClientDir, mappings) + WSClientsConstants.SEPARATOR + WSClientsConstants.WS_CLIENTS_DEPLOYMENT_DESCRIPTOR_DIR;
  }

  public static String getWSClientsRuntimeDescriptorFileName() {
    return WSClientsConstants.WS_CLIENTS_RUNTIME_DESCRIPTOR;
  }

  public static String getWSClientsRuntimeDescriptorPath(String wsClientDir, Properties mappings) {
    return getDescriptorsDir(wsClientDir, mappings) + WSClientsConstants.SEPARATOR + WSClientsConstants.WS_CLIENTS_RUNTIME_DESCRIPTOR;
  }

  public static String getWSClientsRuntimeDescriptorParentRelDir(Properties mappings) {
    return getDescriptorsRelDir(mappings);
  }

  public static String getWSClientsRuntimeDescriptorParentDir(String wsClientDir, Properties mappings) {
    return getDescriptorsDir(wsClientDir, mappings);
  }

  public static String getLogPortsDir(String wsClientDir, Properties mappings) {
    return getDescriptorsDir(wsClientDir, mappings) + WSClientsConstants.SEPARATOR + mappings.getProperty(WSClientsConstants.LOG_PORTS_NAME);
  }

  public static String getLogPortsPath(String wsClientDir, Properties mappings, String logPortsFileName) {
    return getLogPortsDir(wsClientDir, mappings) + WSClientsConstants.SEPARATOR + logPortsFileName;
  }

  public static String getWsdlDir(String wsClientDir, Properties mappings) {
    return getDescriptorsDir(wsClientDir, mappings) + WSClientsConstants.SEPARATOR + mappings.getProperty(WSClientsConstants.WSDL_NAME);
  }

  public static String[] getWsdlPaths(String wsClientDir, Properties mappings, String[] wsdlFileNames) {
    return WSUtil.addPrefixToStrings(getWsdlDir(wsClientDir, mappings) + WSClientsConstants.SEPARATOR, wsdlFileNames);
  }

  public static String[] getUriMappingPaths(String wsClientDir, Properties mappings, String[] uriMappingFileNames) {
    return WSUtil.addPrefixToStrings(getWsdlDir(wsClientDir, mappings) + WSClientsConstants.SEPARATOR, uriMappingFileNames);
  }

  public static String getPackageMappingPaths(String wsClientDir, Properties mappings, String packageMappingFileName) {
    return getWsdlDir(wsClientDir, mappings) + WSClientsConstants.SEPARATOR + packageMappingFileName;
  }

  public static String getJarsRelDir(Properties mappings) {
    return mappings.getProperty(WSClientsConstants.JARS_NAME);
  }

  public static String getJarsDir(String wsClientDir, Properties mappings) {
    return wsClientDir + WSClientsConstants.SEPARATOR + mappings.getProperty(WSClientsConstants.JARS_NAME);
  }

  public static String getJarPath(String wsClientDir, Properties mappings) {
    return getJarsDir(wsClientDir, mappings) + WSClientsConstants.SEPARATOR + mappings.getProperty(WSClientsConstants.JAR_FILE_NAME);
  }

  public WSClientDirsHandler() {
  }

  public WSClientDirsHandler(String wsClientDirectory, Properties dirsMapping) {
    this.wsClientDirectory = wsClientDirectory;
    this.mappings = dirsMapping;
  }

  public String getWsClientDirectory() {
    return wsClientDirectory;
  }

  public int getIndex() {
    if(mappings.containsKey(WebServicesConstants.INDEX)) {
      return new Integer(mappings.getProperty(WebServicesConstants.INDEX)).intValue();
    }

    return extractIndex(getWsClientDirectory());
  }

  public String getWsClientConfigName() {
    return new File(wsClientDirectory).getName();
  }

  public String getWsClientConfigPath(String wsContainerConfigPath) {
    return getWSClientsConfigName(wsContainerConfigPath) + WSClientsConstants.SEPARATOR + getWsClientConfigName();
  }

  public void setWsClientDirectory(String wsClientDirectory) {
    this.wsClientDirectory = wsClientDirectory;
  }

  public Properties getMappings() {
    return mappings;
  }

  public void setMappings(Properties dirsMapping) {
    this.mappings = dirsMapping;
  }

  public void updateDefaultMappings(String serviceRefName) {
    mappings.setProperty(WSClientsConstants.JAR_FILE_NAME, getDefaultJarFileName(serviceRefName));
  }

  public String getMappingsPath() {
    return wsClientDirectory + WSClientsConstants.SEPARATOR + getMappingsFileName();
  }

  public String getDescriptorsRelDir() {
    if (descriptorsRelDir == null) {
      descriptorsRelDir = mappings.getProperty(WSClientsConstants.DESCRIPTORS_NAME);
    }

    return descriptorsRelDir;
  }

  public String getDescriptorsDir() {
    if(descriptorsDir == null) {
      descriptorsDir = wsClientDirectory + WSClientsConstants.SEPARATOR + getDescriptorsRelDir();
    }

    return descriptorsDir;
  }

  public String getWSClientDeploymentDescriptorsDir() {
    return getDescriptorsDir() + WSClientsConstants.SEPARATOR + WSClientsConstants.WS_CLIENTS_DEPLOYMENT_DESCRIPTOR_DIR;
  }

  public String getWSClientsDeploymentDescriptorPath() {
    return getDescriptorsDir() + WSClientsConstants.SEPARATOR + WSClientsConstants.WS_CLIENTS_DEPLOYMENT_DESCRIPTOR;
  }

  public boolean existsWSClientsDeploymentDescriptor() {
    return new File(getWSClientsDeploymentDescriptorPath()).exists();
  }

  public InputStream getWSClientsDeploymentDescriptorInputStream() throws IOException {
    return new FileInputStream(getWSClientsDeploymentDescriptorPath());
  }

  public String getWSClientsDeploymentDescriptorLocationMsg() {
    String locationMsg = "Type: file; ";
    locationMsg += "File location: " + getWSClientsDeploymentDescriptorPath() + "; ";

    return locationMsg;
  }

  public String[] getWSClientsDeploymentDescriptorPathsMultipleMode() {
    return new String[0];
  }

  public boolean existsWSClientsDeploymentDescriptorMultipleMode() throws IOException {
    return false;
  }

  public InputStream[] getWSClientsDeploymentDescriptorInputStreamMultipleMode() throws IOException {
    return new InputStream[0];
  }

  public String[] getWSClientsDeploymentDescriptorLocationMsgMultipleMode() throws IOException {
    return new String[0];
  }

  public String getWSClientsRuntimeDescriptorPath() {
    return getDescriptorsDir() + WSClientsConstants.SEPARATOR + WSClientsConstants.WS_CLIENTS_RUNTIME_DESCRIPTOR;
  }

  public String getLogPortRelDir() {
    if (logPortsRelDir == null) {
      logPortsRelDir = getDescriptorsRelDir() + WSClientsConstants.SEPARATOR + mappings.getProperty(WSClientsConstants.LOG_PORTS_NAME);
    }

    return logPortsRelDir;
  }

  public String getLogPortsDir() {
    if(logPortsDir == null) {
      logPortsDir = wsClientDirectory + WSClientsConstants.SEPARATOR + getLogPortRelDir();
    }

    return logPortsDir;
  }

  public String getLogPortsRelPath(String logPortsFileName) {
    return getLogPortRelDir() + WSClientsConstants.SEPARATOR + logPortsFileName;
  }

  public String getLogPortsPath(String logPortsFileName) {
    return getLogPortsDir() + WSClientsConstants.SEPARATOR + logPortsFileName;
  }

  public InputStream getLogPortsInputStream(String logPortsRelPath) throws IOException {
    return new FileInputStream(getLogPortsPath(logPortsRelPath));
  }

  public String getLogPortsLocationMsg(String logPortsRelPath) {
    String locationMsg = "Type: file; ";
    locationMsg += "File location: " + getLogPortsPath(logPortsRelPath) + "; ";

    return locationMsg;
  }

  public String getWsdlRelDir() {
    if(wsdlRelDir == null) {
      wsdlRelDir = getDescriptorsRelDir() + WSClientsConstants.SEPARATOR + mappings.getProperty(WSClientsConstants.WSDL_NAME);
    }

    return wsdlRelDir;
  }

  public String getWsdlDir() {
    if(wsdlDir == null) {
      wsdlDir = wsClientDirectory + WSClientsConstants.SEPARATOR + getWsdlRelDir();
    }

    return wsdlDir;
  }

  public String[] getWsdlRelPaths(String[] wsdlFileNames) {
    return WSUtil.addPrefixToStrings(getWsdlRelDir() + WSClientsConstants.SEPARATOR, wsdlFileNames);
  }

  public String[] getWsdlPaths(String[] wsdlFileNames) {
    return WSUtil.addPrefixToStrings(getWsdlDir() + WSClientsConstants.SEPARATOR, wsdlFileNames);
  }

  public InputStream[] getWsdlInputStream(String[] wsdlRelPaths) throws IOException {
    String[] wsdlPaths = getWsdlRelPaths(wsdlRelPaths);
    if(wsdlPaths == null) {
      return new InputStream[0];
    }

    InputStream[] wsdlInputStreams = new InputStream[wsdlPaths.length];
    for(int i = 0; i < wsdlPaths.length; i++) {
      wsdlInputStreams[i] = new FileInputStream(wsdlPaths[i]);
    }

    return wsdlInputStreams;
  }

  public String[] getWsdlLocationMsg(String[] wsdlRelPaths) {
    String[] wsdlPaths = getWsdlRelPaths(wsdlRelPaths);
    if(wsdlPaths == null) {
      return new String[0];
    }

    String[] wsdlLocationMsgs = new String[wsdlPaths.length];
    for(int i = 0; i < wsdlPaths.length; i++) {
      String locationMsg = "Type: file; ";
      locationMsg += "File location: " + wsdlPaths[i] + "; ";
      wsdlLocationMsgs[i] = locationMsg;
    }

    return wsdlLocationMsgs;
  }

  public String[] getUriMappingRelPaths(String[] uriMappingFileNames) {
    return WSUtil.addPrefixToStrings(getWsdlRelDir() + WSClientsConstants.SEPARATOR, uriMappingFileNames);
  }

  public String[] getUriMappingPaths(String[] uriMappingFileNames) {
    return WSUtil.addPrefixToStrings(getWsdlDir() + WSClientsConstants.SEPARATOR, uriMappingFileNames);
  }

  public InputStream[] getUriMappingInputStream(String[] uriMappingsRelPaths) throws IOException {
    String[] uriMappingPaths = getUriMappingPaths(uriMappingsRelPaths);
    if(uriMappingPaths == null) {
      return new InputStream[0];
    }

    InputStream[] uriMappingInputStreams = new InputStream[uriMappingPaths.length];
    for(int i = 0; i < uriMappingPaths.length; i++) {
      uriMappingInputStreams[i] = new FileInputStream(uriMappingPaths[i]);
    }

    return uriMappingInputStreams;
  }

  public String[] getUriMappingLocationMsg(String[] uriMappingRelPaths) {
    String[] uriMappingPaths = getUriMappingPaths(uriMappingRelPaths) ;
    if(uriMappingPaths == null) {
      return new String[0];
    }

    String[] uriMappingLocationMsgs = new String[uriMappingPaths.length];
    for(int i = 0; i < uriMappingPaths.length; i++) {
      String locationMsg = "Type: file; ";
      locationMsg += "File location: " + uriMappingPaths[i] + "; ";
      uriMappingLocationMsgs[i] = locationMsg;
    }

    return uriMappingLocationMsgs;
  }

  public String getPackageMappingRelPath(String packageMappingFileName) {
    return getWsdlRelDir() + WSClientsConstants.SEPARATOR +  packageMappingFileName;
  }

  public String getPackageMappingPath(String packageMappingFileName) {
    return getWsdlDir() + WSClientsConstants.SEPARATOR +  packageMappingFileName;
  }

  public InputStream getPackageMappingInputStream(String packageMappingRelPath) throws IOException {
    return new FileInputStream(getPackageMappingPath(packageMappingRelPath));
  }

  public String getPackageMappingLocationMsg(String packageMappingRelPath) {
    String locationMsg = "Type: file; ";
    locationMsg += "File location: " + getPackageMappingPath(packageMappingRelPath) + "; ";

    return locationMsg;
  }

  public String getLocationMsg() {
    String locationMsg = "Type: file; ";
    locationMsg += "File location: " + getWSClientsDeploymentDescriptorPath() + "; ";

    return locationMsg;
  }

  public String getJarsRelDir() {
    if(jarsRelDir == null) {
      jarsRelDir = mappings.getProperty(WSClientsConstants.JARS_NAME);
    }

    return jarsRelDir;
  }

  public String getJarsDir() {
    if(jarsDir == null) {
      jarsDir =  wsClientDirectory + WSClientsConstants.SEPARATOR + getJarsRelDir();
    }
    return jarsDir;
  }

  public String getJarFileName() {
    if(jarFileName == null) {
      jarFileName = mappings.getProperty(WSClientsConstants.JAR_FILE_NAME);
    }
    return jarFileName;
  }

  public String getJarPath() {
    if(jarPath == null) {
      jarPath = getJarsDir() + WSClientsConstants.SEPARATOR + getJarFileName();
    }

    return jarPath;
  }

  public static String getAllowedString(String str) {
    return WSUtil.replaceForbiddenChars(str);
  }

  private int extractIndex(String str) {
    int index = -1;
    if(str.startsWith(WS_CLIENT_DIR_PREFIX)) {
      String suffix = str.substring(WS_CLIENT_DIR_PREFIX.length());
      try {
        index = new Integer(suffix).intValue();
      } catch(NumberFormatException e) {
        // $JL-EXC$
      }
    }

    return index;
  }

}

