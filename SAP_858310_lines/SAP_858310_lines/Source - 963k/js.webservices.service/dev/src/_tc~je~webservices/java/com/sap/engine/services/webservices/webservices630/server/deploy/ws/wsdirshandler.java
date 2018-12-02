package com.sap.engine.services.webservices.webservices630.server.deploy.ws;

import com.sap.engine.services.webservices.webservices630.server.deploy.common.ModuleDirsHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;

import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
  * Title: WSDirsHandler
  * Description: The class contains methods, that provides access to web services deploy files, located on the local file system.
  * Copyright: Copyright (c) 2004
  * Company: Sap Labs Sofia
  *
  * @author Dimitrina Stoyanova
  * @version 6.30
  */

public class WSDirsHandler extends ModuleDirsHandler implements WSFilesLocationHandler {

  private Properties mappings = null;
  private String wsDirectory = null;

  private String descriptorsRelDir = null;
  private String descriptorsDir = null;
  private String typeMappingFileName = null;
  private String typeMappingPath = null;
  private String javaQNameMappingFileName = null;
  private String javaQNameMappingPath = null;
  private String docRelDir = null;
  private String docDir = null;
  private String wsdRelDir = null;
  private String wsdDir  = null;
  private String viRelDir = null;
  private String viDir = null;
  private String jarsRelDir = null;
  private String jarsDir = null;
  private String jarFileName = null;
  private String jarPath = null;
  private String wsdlRelDir = null;
  private String wsdlDir = null;
  private String outsideInReldDir = null;
  private String outsideInDir = null;
  private String outsideInWsdlRelDir = null;
  private String outsideInWsdlDir = null;

  public WSDirsHandler() {
  }

  public WSDirsHandler(Properties mappings, String wsDirectory) {
    this.mappings = mappings;
    this.wsDirectory = wsDirectory;
  }

  public static Properties generateBaseMappings(int index) {
    Properties mappings = new Properties();
    mappings.setProperty(WebServicesConstants.INDEX, new Integer(index).toString());
    mappings.setProperty(WebServicesConstants.DESCRIPTORS_NAME, WebServicesConstants.DESCRIPTORS_NAME);
    mappings.setProperty(WebServicesConstants.DOC_NAME, WebServicesConstants.DOC_NAME);
    mappings.setProperty(WebServicesConstants.WSD_NAME, WebServicesConstants.WSD_NAME);
    mappings.setProperty(WebServicesConstants.VI_NAME, WebServicesConstants.VI_NAME);
    mappings.setProperty(WebServicesConstants.TYPE_MAPPING_FILE_NAME, WebServicesConstants.TYPE_MAPPING_DESCRIPTOR);
    mappings.setProperty(WebServicesConstants.JAVA_QNAME_MAPPING_FILE_NAME, WebServicesConstants.JAVA_QNAME_MAPPING_DESCRIPTOR);
    mappings.setProperty(WebServicesConstants.JARS_NAME, WebServicesConstants.JARS_NAME);
    mappings.setProperty(WebServicesConstants.WSDL_NAME, WebServicesConstants.WSDL_NAME);
    mappings.setProperty(WebServicesConstants.OUTSIDE_IN_NAME, WebServicesConstants.OUTSIDE_IN_NAME);
    mappings.setProperty(WebServicesConstants.OUSIDE_IN_WSDL_DIR_NAME, "");

    return mappings;
  }

  public static Properties generateBaseDefaultMappings() {
    Properties mappings = new Properties();
    mappings.setProperty(WebServicesConstants.DESCRIPTORS_NAME, WebServicesConstants.DESCRIPTORS_NAME);
    mappings.setProperty(WebServicesConstants.DOC_NAME, WebServicesConstants.DOC_NAME);
    mappings.setProperty(WebServicesConstants.WSD_NAME, WebServicesConstants.WSD_NAME);
    mappings.setProperty(WebServicesConstants.VI_NAME, WebServicesConstants.VI_NAME);
    mappings.setProperty(WebServicesConstants.TYPE_MAPPING_FILE_NAME, WebServicesConstants.TYPE_MAPPING_DESCRIPTOR);
    mappings.setProperty(WebServicesConstants.JAVA_QNAME_MAPPING_FILE_NAME, WebServicesConstants.JAVA_QNAME_MAPPING_DESCRIPTOR);
    mappings.setProperty(WebServicesConstants.JARS_NAME, WebServicesConstants.JARS_NAME);
    mappings.setProperty(WebServicesConstants.WSDL_NAME, WebServicesConstants.WSDL_NAME);
    mappings.setProperty(WebServicesConstants.OUTSIDE_IN_NAME, WebServicesConstants.OUTSIDE_IN_NAME);
    mappings.setProperty(WebServicesConstants.OUSIDE_IN_WSDL_DIR_NAME, WebServicesConstants.WSDL_NAME);

    return mappings;
  }

  public static void upgradeBaseMappings(Properties mappings, String serviceName) {
    String jarFileName = getDefaultJarFileName(serviceName);
    mappings.setProperty(WebServicesConstants.JAR_FILE_NAME, jarFileName);
  }

  public static Properties generateDefaultMappings(String serviceName) {
    Properties defaultMappings = generateBaseDefaultMappings();
    String jarFileName = getDefaultJarFileName(serviceName);
    defaultMappings.setProperty(WebServicesConstants.JAR_FILE_NAME, jarFileName);
    return defaultMappings;
  }

  public static Properties generateMappings(String serviceName, int index) {
    Properties mappings = generateBaseMappings(index);
    String jarFileName = getDefaultJarFileName(serviceName);
    mappings.setProperty(WebServicesConstants.JAR_FILE_NAME, jarFileName);
    return mappings;
  }

  public static String getWebServicesConfigRelName() {
    return WebServicesConstants.WEBSERVICES_CONFIG_NAME;
  }

  public static String getWebServicesConfigName(String wsContainerPath) {
    return wsContainerPath + WebServicesConstants.SEPARATOR + getWebServicesConfigRelName();
  }

  public static String getWebServicesRelDir() {
    return WebServicesConstants.WEBSERVICES_CONFIG_NAME;
  }

  public static String getWebServicesDir(String wsContainerDir) {
    return wsContainerDir + WebServicesConstants.SEPARATOR + getWebServicesRelDir();
  }

  public static String getWebServicesWorkingDir(String wsContainerWorkingDir) {
    return wsContainerWorkingDir + WebServicesConstants.SEPARATOR + WebServicesConstants.WEBSERVICES_CONFIG_NAME;
  }

  public static String getModuleWorkingDir(String webServicesWorkingDir, String moduleName) {
    return webServicesWorkingDir + WebServicesConstants.SEPARATOR + WSUtil.getModuleNameByType(moduleName);
  }

  public static String getModuleExtractDir(String moduleWorkingDir) {
    return moduleWorkingDir + WebServicesConstants.SEPARATOR + WebServicesConstants.EXTRACT_DIR_NAME;
  }

  public static String getWSDirectory(String webServicesDir, int index) {
    return webServicesDir + WebServicesConstants.SEPARATOR + "ws_" + index;
  }

  public static String getWSDefaultDirectory(String webServicesDir, String serviceName) {
    return webServicesDir + WebServicesConstants.SEPARATOR + WSUtil.replaceForbiddenChars(serviceName);    
  }

  public static String getWSWorkingDir(String webServicesWorkingDir, int number) {
    return webServicesWorkingDir + WebServicesConstants.SEPARATOR + "ws_" + number;
  }

  public static String getWSDefaultWorkingDir(String webServicesWorkingDir, String serviceName) {
    return webServicesWorkingDir + WebServicesConstants.SEPARATOR + WSUtil.replaceForbiddenChars(serviceName);
  }

  public static int getIndex(Properties mappings) {
    if(mappings.containsKey(WebServicesConstants.INDEX)) {
      return new Integer(mappings.getProperty(WebServicesConstants.INDEX)).intValue();
    }

    return -1;
  }

  public static String getMappingsFileName() {
    return WebServicesConstants.MAPPINGS_FILE_NAME;
  }

  public static String getMappingsParentRelDir() {
    return "";
  }

  public static String getMappingsParentDir(String wsDirectory) {
    return wsDirectory + WebServicesConstants.SEPARATOR + getMappingsParentRelDir();
  }

  public static String getMappingsPath(String wsDirectory) {
    return getMappingsParentDir(wsDirectory) + WebServicesConstants.SEPARATOR + getMappingsFileName();
  }

  public static String getDescriptorsRelDir(Properties mappings) {
    return mappings.getProperty(WebServicesConstants.DESCRIPTORS_NAME);
  }

  public static String getDescriptorsDir(String wsDirectory, Properties mappings) {
    return wsDirectory + WebServicesConstants.SEPARATOR + getDescriptorsRelDir(mappings);
  }

  public static String getWSDeploymentDescriptorFileName() {
    return WebServicesConstants.WS_DEPLOYMENT_DESCRIPTOR;
  }

  public static String getWSDeploymentDescriptorParentRelDir(Properties mappings) {
    return getDescriptorsRelDir(mappings);
  }

  public static String getWSDeploymentDescriptorParentDir(String wsDirectory, Properties mappings) {
    return getDescriptorsDir(wsDirectory, mappings);
  }

  public static String getWSDeploymentDescriptorPath(String wsDirectory, Properties mappings) {
    return getWSDeploymentDescriptorParentDir(wsDirectory, mappings) + WebServicesConstants.SEPARATOR + getWSDeploymentDescriptorFileName();
  }

  public static InputStream getWSDeploymentDescriptorInputStream(String wsDirectory, Properties mappings) throws IOException {
    return new FileInputStream(getWSDeploymentDescriptorPath(wsDirectory, mappings));
  }

  public static String getWSRuntimeDescriptorFileName() {
    return WebServicesConstants.WS_RUNTIME_DESCRIPTOR;
  }

  public static String getWSRuntimeDescriptorParentRelDir(Properties mappings) {
    return getDescriptorsRelDir(mappings);
  }

  public static String getWSRuntimeDescriptorParentDir(String wsDirectory, Properties mappings) {
    return getDescriptorsDir(wsDirectory, mappings);
  }

  public static String getWSRuntimeDescriptorPath(String wsDirectory, Properties mappings) {
    return getWSRuntimeDescriptorParentDir(wsDirectory, mappings) + WebServicesConstants.SEPARATOR + getWSRuntimeDescriptorFileName();
  }

  public static String getTypeMappingFileName(Properties mappings) {
    return mappings.getProperty(WebServicesConstants.TYPE_MAPPING_FILE_NAME);
  }

   public static String getTypeMappingPath(String wsDirectory, Properties mappings) {
    return getDescriptorsDir(wsDirectory, mappings) + WebServicesConstants.SEPARATOR + getTypeMappingFileName(mappings);
  }

  public static String getJavaQNameMappingFileName(Properties mappings) {
     return mappings.getProperty(WebServicesConstants.JAVA_QNAME_MAPPING_FILE_NAME);
  }

  public static String getJavaToQNameMappingPath(String wsDirectory, Properties mappings) {
    return getDescriptorsDir(wsDirectory, mappings) + WebServicesConstants.SEPARATOR + getJavaQNameMappingFileName(mappings);
  }

  public static String getDocRelDir(Properties mappings) {
    return getDescriptorsRelDir(mappings) + WebServicesConstants.SEPARATOR + mappings.getProperty(WebServicesConstants.DOC_NAME);
  }

  public static String getDocDir(String wsDirectory, Properties mappings) {
    return wsDirectory + WebServicesConstants.SEPARATOR + getDocRelDir(mappings);
  }

  public static String getDocPath(String wsDirectory, Properties mappings, String docRelPath) {
    return getDocDir(wsDirectory, mappings) + WebServicesConstants.SEPARATOR + docRelPath;
  }

  public static InputStream getDocInputStream(String wsDirectory, Properties mappings, String docRelPath) throws IOException {
    return new FileInputStream(getDocPath(wsDirectory, mappings, docRelPath));
  }

  public static String getWsdRelDir(Properties mappings) {
    return getDescriptorsRelDir(mappings) + WebServicesConstants.SEPARATOR + mappings.getProperty(WebServicesConstants.WSD_NAME);
  }

  public static String getWsdDir(String wsDirectory, Properties mappings) {
    return  wsDirectory + WebServicesConstants.SEPARATOR + getWsdRelDir(mappings);
  }

  public static String getWsdPath(String wsDirectory, Properties mappings, String wsdRelPath) {
    return getWsdDir(wsDirectory, mappings) + WebServicesConstants.SEPARATOR + wsdRelPath;
  }

  public static InputStream getWsdInputStream(String wsDirectory, Properties mappings, String wsdRelPath) throws IOException {
    return new FileInputStream(getWsdPath(wsDirectory, mappings, wsdRelPath));
  }

  public static String getViRelDir(Properties mappings) {
    return getDescriptorsRelDir(mappings) + WebServicesConstants.SEPARATOR + mappings.getProperty(WebServicesConstants.VI_NAME);
  }

  public static String getViDir(String wsDirectory, Properties mappings) {
    return wsDirectory + WebServicesConstants.SEPARATOR + getViRelDir(mappings);
  }

  public static String getViPath(String wsDirectory, Properties mappings, String viRelPath) {
    return getViDir(wsDirectory, mappings) + WebServicesConstants.SEPARATOR + viRelPath;
  }

  public static InputStream getViInputStream(String wsDirectory, Properties mappings, String viRelPath)throws IOException {
    return new FileInputStream(getViPath(wsDirectory, mappings, viRelPath));
  }

  public static String getJarsRelDir(Properties mappings) {
    return mappings.getProperty(WebServicesConstants.JARS_NAME);
  }

  public static String getJarsDir(String wsDirectory, Properties mappings) {
    return wsDirectory + WebServicesConstants.SEPARATOR + getJarsRelDir(mappings);
  }

  public static String getJarFileName(Properties mappings) {
    return mappings.getProperty(WebServicesConstants.JAR_FILE_NAME);
  }

  public static String getJarPath(String wsDirectory, Properties mappings) {
    return getJarsDir(wsDirectory, mappings) + WebServicesConstants.SEPARATOR + getJarFileName(mappings);
  }

  public static String getDefaultJarFileName(String serviceName) {
    return getAllowedString(serviceName) + ".jar";
  }

  public static String getWsdlRelDir(Properties mappings) {
    return mappings.getProperty(WebServicesConstants.WSDL_NAME);
  }

  public static String getWsdlDir(String wsDirectory, Properties mappings) {
    return wsDirectory + WebServicesConstants.SEPARATOR + getWsdlRelDir(mappings);
  }

  public static String getOutsideInRelDir(Properties mappings) {
    return mappings.getProperty(WebServicesConstants.OUTSIDE_IN_NAME);
  }

  public static String getOutsideInDir(String wsDirectory, Properties mappings) {
    return wsDirectory + WebServicesConstants.SEPARATOR  + getOutsideInRelDir(mappings);
  }

  public static String getOutsideInWsdlRelDir(Properties mappings) {
    return getOutsideInRelDir(mappings) + WebServicesConstants.SEPARATOR + mappings.getProperty(WebServicesConstants.OUSIDE_IN_WSDL_DIR_NAME);
  }

  public static String getOutsideInWsdlDir(String wsDirectory, Properties mappings) {
   return  wsDirectory + WebServicesConstants.SEPARATOR + getOutsideInWsdlRelDir(mappings);
  }

  public Properties getMappings() {
    return mappings;
  }

  public void setMappings(Properties mappings) {
    this.mappings = mappings;
  }

  public String getWsDirectory() {
    return wsDirectory;
  }

  public void setWsDirectory(String wsDirectory) {
    this.wsDirectory = wsDirectory;
  }

  public int getIndex() {
    if(mappings.containsKey(WebServicesConstants.INDEX)) {
      return new Integer(mappings.getProperty(WebServicesConstants.INDEX)).intValue();
    }

    return -1;
  }

  public String getMappingsPath() {
    return wsDirectory + WebServicesConstants.SEPARATOR + getMappingsFileName();
  }

  public String getWSConfigName() {
    return new File(getWsDirectory()).getName();
  }

  public String getWSConfigPath(String wsContainerConfigPath) {
    return getWebServicesConfigName(wsContainerConfigPath) + WebServicesConstants.SEPARATOR + getWSConfigName();
  }

  public String getDescriptorsRelDir() {
    if(descriptorsRelDir == null) {
     descriptorsRelDir = mappings.getProperty(WebServicesConstants.DESCRIPTORS_NAME);
    }

    return descriptorsRelDir;
  }

  public String getDescriptorsDir() {
    if(descriptorsDir == null) {
      descriptorsDir = wsDirectory + WebServicesConstants.SEPARATOR + getDescriptorsRelDir();
    }

    return descriptorsDir;
  }

  public String getWSDeploymentDescriptorPath() {
    return getDescriptorsDir() + WebServicesConstants.SEPARATOR + getWSDeploymentDescriptorFileName();
  }

  public String getWSDeploymentDescriptorParentRelDir() {
    return getDescriptorsRelDir();
  }

  public InputStream getWSDeploymentDescriptorInputStream() throws IOException {
    return new FileInputStream(getWSDeploymentDescriptorPath());
  }

  public String getBaseLocationMsg() {
    return wsDirectory;
  }

  public String getWSDeploymentDescriptorLocationMsg() {
    String locationMsg = "Type: file; ";
    locationMsg += "File location: " + getWSDeploymentDescriptorPath() + "; ";

    return locationMsg;
  }

  public String getWSRuntimeDescriptorPath() {
    return getDescriptorsDir() + WebServicesConstants.SEPARATOR + getWSRuntimeDescriptorFileName();
  }

  public String getWSRuntimeDescriptorBaseParentDir() {
    return getDescriptorsDir();
  }

  public String getTypeMappingFileName() {
    if(typeMappingFileName == null) {
     typeMappingFileName = mappings.getProperty(WebServicesConstants.TYPE_MAPPING_FILE_NAME);
    }

    return typeMappingFileName;
  }

  public String getTypeMappingPath() {
    if(typeMappingPath == null) {
      typeMappingPath = getDescriptorsDir() + WebServicesConstants.SEPARATOR + getTypeMappingFileName();
    }

    return typeMappingPath;
  }

  public String getJavaQNameMappingFileName() {
    if(javaQNameMappingFileName == null) {
      javaQNameMappingFileName = mappings.getProperty(WebServicesConstants.JAVA_QNAME_MAPPING_FILE_NAME);
    }

    return javaQNameMappingFileName;
  }

  public String getJavaToQNameMappingPath() {
    if(javaQNameMappingPath == null) {
      javaQNameMappingPath = getDescriptorsDir() + WebServicesConstants.SEPARATOR + getJavaQNameMappingFileName();
    }

    return javaQNameMappingPath;
  }

  public String getDocRelDir() {
    if(docRelDir == null) {
      docRelDir = getDescriptorsRelDir() + WebServicesConstants.SEPARATOR + mappings.getProperty(WebServicesConstants.DOC_NAME);
    }

    return docRelDir;
  }

  public String getDocDir() {
    if(docDir == null) {
      docDir = wsDirectory + WebServicesConstants.SEPARATOR + getDocRelDir();
    }

    return docDir;
  }

  public String findDocRelPath(String[] docRelPathTemplates) {
    if(docRelPathTemplates == null) {
      return null;
    }

    for(int i = 0; i < docRelPathTemplates.length; i++) {
      String docRelPathTemplate = docRelPathTemplates[i];
      String docPath = getDocPath(docRelPathTemplate);
      if(new File(docPath).exists()) {
        return docRelPathTemplate;
      }
    }

    return null;
  }

  public String getDocPath(String docRelPath) {
    return getDocDir() + WebServicesConstants.SEPARATOR + docRelPath;
  }

  public String getWsdRelDir() {
    if(wsdRelDir == null) {
      wsdRelDir = getDescriptorsRelDir() + WebServicesConstants.SEPARATOR + mappings.getProperty(WebServicesConstants.WSD_NAME);
    }

    return wsdRelDir;
  }

  public InputStream getDocInputStream(String docRelPath) throws IOException {
    return new FileInputStream(getDocPath(docRelPath));
  }

  public String getDocLocationMsg(String docRelPath) {
    String locationMsg = "type: file, ";
    locationMsg += "file location: " + getDocPath(docRelPath) + " ";

    return locationMsg;
  }

  public String getWsdDir() {
    if(wsdDir == null) {
      wsdDir = wsDirectory + WebServicesConstants.SEPARATOR + getWsdRelDir();
    }

    return wsdDir;
  }

  public String getWsdPath(String wsdRelPath) {
    return getWsdDir() + WebServicesConstants.SEPARATOR + wsdRelPath;
  }

  public InputStream getWsdInputStream(String wsdRelPath) throws IOException {
    return new FileInputStream(getWsdPath(wsdRelPath));
  }

  public String getWsdLocationMsg(String wsdRelPath) {
    String locationMsg = "type: file, ";
    locationMsg += "file location: " + getWsdPath(wsdRelPath) + " ";

    return locationMsg;
  }

  public String getViRelDir() {
    if(viRelDir == null) {
      viRelDir = getDescriptorsRelDir() + WebServicesConstants.SEPARATOR + mappings.getProperty(WebServicesConstants.VI_NAME);
    }

    return viRelDir;
  }

  public String getViDir() {
    if(viDir == null) {
      viDir = wsDirectory + WebServicesConstants.SEPARATOR + getViRelDir();
    }

    return viDir;
  }

  public String getViPath(String viRelPath) {
    return getViDir() + WebServicesConstants.SEPARATOR + viRelPath;
  }

  public InputStream getViInputStream(String viRelPath)throws IOException {
    return new FileInputStream(getViPath(viRelPath));
  }

  public String getViLocationMsg(String viRelPath) {
    String locationMsg = "type: file, ";
    locationMsg += "file location: " + getViPath(viRelPath) + " ";

    return locationMsg;
  }

  public String getJarsRelDir() {
    if(jarsRelDir == null) {
      jarsRelDir = mappings.getProperty(WebServicesConstants.JARS_NAME);
    }

    return jarsRelDir;
  }

  public String getJarsDir() {
    if(jarsDir == null) {
      jarsDir = wsDirectory + WebServicesConstants.SEPARATOR + getJarsRelDir();
    }

    return jarsDir;
  }

  public String getJarFileName() {
    if(jarFileName == null) {
      jarFileName = mappings.getProperty(WebServicesConstants.JAR_FILE_NAME);
    }

    return jarFileName;
  }

  public String getJarPath() {
    if(jarPath == null) {
      jarPath = getJarsDir() + WebServicesConstants.SEPARATOR + getJarFileName();
    }

    return jarPath;
  }

  public String getWsdlRelDir() {
    if(wsdlRelDir == null) {
      wsdlRelDir = mappings.getProperty(WebServicesConstants.WSDL_NAME);
    }

    return wsdlRelDir;
  }

  public String getWsdlDir() {
    if(wsdlDir == null) {
      wsdlDir = wsDirectory + WebServicesConstants.SEPARATOR + getWsdlRelDir();
    }

    return wsdlDir;
  }

  public String getViWsdlPath(String viRelPath, String configurationName, String style, boolean isSapMode) {
    String viRelPathWithoutExt = IOUtil.getFileNameWithoutExt(viRelPath);
    String viWsdlRelPath = null;

    if (isSapMode) {
      viWsdlRelPath = viRelPathWithoutExt + "_" + configurationName + "_" + style + "_sap.wsdl";
    } else {
      viWsdlRelPath = viRelPathWithoutExt + "_" + configurationName + "_" + style + ".wsdl";
    }

    return getWsdlDir() + WebServicesConstants.SEPARATOR + viWsdlRelPath;
  }

  public String getBindingWsdlPath(String configurationName, String style, boolean isSapMode) {
    String bindingWsdlFileName = null;
    if (isSapMode) {
      bindingWsdlFileName = configurationName + "_" + style + "_sap.wsdl";
    } else {
      bindingWsdlFileName = configurationName + "_" + style + ".wsdl";
    }

    return getWsdlDir() + WebServicesConstants.SEPARATOR + bindingWsdlFileName;
  }

  public String getOutsideInRelDir() {
    if(outsideInReldDir == null) {
      outsideInReldDir = mappings.getProperty(WebServicesConstants.OUTSIDE_IN_NAME);
    }

    return outsideInReldDir;
  }

  public String getOutsideInDir() {
    if(outsideInDir == null) {
      outsideInDir = wsDirectory + WebServicesConstants.SEPARATOR  + getOutsideInRelDir();
    }

    return outsideInDir;
  }

  public String getOutsideInWsdlRelDir() {
    if(outsideInWsdlRelDir == null) {
      outsideInWsdlRelDir = getOutsideInRelDir() + WebServicesConstants.SEPARATOR + mappings.getProperty(WebServicesConstants.OUSIDE_IN_WSDL_DIR_NAME);
    }
    return outsideInWsdlRelDir;
  }

  public String getOutsideInWsdlDir() {
    if(outsideInWsdlDir == null) {
      outsideInWsdlDir = wsDirectory + WebServicesConstants.SEPARATOR + getOutsideInWsdlRelDir();
    }

    return outsideInWsdlDir;
  }

  public String getOutsideInWsdlPath(String wsdlRelPath) {
    return getOutsideInWsdlDir() + WebServicesConstants.SEPARATOR + wsdlRelPath;
  }

  public String getOutsideInJavaToQNameMapppingPath(String javaToQNameMappingRelPath) {
    return getOutsideInDir() + WebServicesConstants.SEPARATOR + javaToQNameMappingRelPath;
  }

  private static String getAllowedString(String str) {
    return WSUtil.replaceForbiddenChars(str);
  }

  public InputStream getOutsideInWsdlInputStream(String wsdlRelPath) throws IOException {
    return new FileInputStream(getOutsideInWsdlPath(wsdlRelPath));
  }

  public String getOutsideInWsdlLocationMsg(String wsdlrelPath) {
    String locationMsg = "type: file ";
    locationMsg += "file location: " + getOutsideInWsdlPath(wsdlrelPath) + " ";

    return locationMsg;
  }

  public InputStream getOutsideInJavaToQNameMappingStream(String javaToQNameMappingRelPath) throws IOException {
    return new FileInputStream(getOutsideInJavaToQNameMapppingPath(javaToQNameMappingRelPath));
  }

  public String getOutsideInJavaToQNameMappingLocationMsg(String javaToQNameMappingRelPath) {
    String locationMsg = "type: file, ";
    locationMsg += "file location: " + getOutsideInJavaToQNameMapppingPath(javaToQNameMappingRelPath) + " ";

    return locationMsg;
  }

  public String getLocationMsg() {
    String locationMsg = "type: file system location, ";
    locationMsg += "location: " + wsDirectory + " ";

    return locationMsg;
  }

}
