/*
 * Copyright (c) 2004-2009 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.deploy;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.lib.descriptors.webj2eeengine.SecurityRoleMapType;
import com.sap.engine.lib.descriptors5.javaee.RunAsType;
import com.sap.engine.lib.descriptors5.javaee.SecurityRoleType;
import com.sap.engine.lib.descriptors5.web.ServletType;
import com.sap.engine.lib.io.FileUtils;
import com.sap.engine.services.deploy.container.ApplicationDeployInfo;
import com.sap.engine.services.deploy.container.ContainerDeploymentInfo;
import com.sap.engine.services.deploy.container.ContainerInfo;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.deploy.container.FileUpdateInfo;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.WebContainerInterface;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.WebDeploymentDescriptor;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.IWebContainer;
import com.sap.engine.services.servlets_jsp.server.deploy.util.ConfigurationUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.util.Constants;
import com.sap.engine.services.servlets_jsp.server.deploy.util.XmlUtils;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIOException;
import com.sap.tc.logging.Location;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * @author Violeta Georgieva
 * @version 7.10
 */
public final class SingleFileUpdateAction extends ActionBase {
  private static final Location currentLocation = Location.getLocation(SingleFileUpdateAction.class);
  private static final Location traceLocation = LogContext.getLocationDeploy();

  /**
   * @param iWebContainer
   * @param containerInfo
   * @param servletsAndJsp
   * @param runtimeInterface
   */
  public SingleFileUpdateAction(IWebContainer iWebContainer, ContainerInfo containerInfo, WebContainerHelper servletsAndJsp,
                                WebContainerInterface runtimeInterface) {
    super(iWebContainer, containerInfo, servletsAndJsp, runtimeInterface);
  }//end of constructor

  /**
   * @param files
   * @param dInfo
   * @param props
   * @return
   * @throws DeploymentException
   */
  public boolean needStopOnSingleFileUpdate(FileUpdateInfo[] files, ContainerDeploymentInfo dInfo, Properties props) throws DeploymentException {
    boolean needRestart = false;

    String applicationName = dInfo.getApplicationName();

    Configuration appConfig = dInfo.getConfiguration();

    Configuration servlet_jspConfig = ConfigurationUtils.getSubConfiguration(appConfig, Constants.CONTAINER_NAME, applicationName, true);

    for (int i = 0; i < files.length; i++) {
      if (files[i].getFileEntryName().endsWith("WEB-INF/web.xml") || files[i].getFileEntryName().endsWith("WEB-INF/web-j2ee-engine.xml")) {
        String warName = files[i].getArchiveName();
        String alias = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getAlias(warName, applicationName);
        String aliasDir = WebContainerHelper.getAliasDirName(ParseUtils.convertAlias(alias));
        if (isSecurityChanged(files[i], applicationName, files, aliasDir, servlet_jspConfig) ||
          isRunAsChanged(files[i].getFileName(), aliasDir, servlet_jspConfig)) {
          WebDeploymentException wde = new WebDeploymentException(WebDeploymentException.UPDATEING_DEPLOYMENT_DESCRIPTORU_IS_NOT_ALLOWED_AT_THIS_OPERATION);
          wde.setDcName(applicationName);
          throw wde;
        } else {
          needRestart = true;
        }
      } else if (files[i].getFileEntryName().startsWith("WEB-INF") || files[i].getFileEntryName().startsWith("/WEB-INF")) {
        needRestart = true;
      }
    }
    props.setProperty("needRestart", (new Boolean(needRestart)).toString());
    return needRestart;
  }//end of needStopOnSingleFileUpdate(FileUpdateInfo[] files, ContainerDeploymentInfo dInfo, Properties props)

  /**
   * @param file
   * @param aliasDir
   * @param applicationName
   * @param files
   * @param config
   * @return
   */
  private boolean isSecurityChanged(FileUpdateInfo file, String applicationName, FileUpdateInfo[] files, String aliasDir, Configuration config) {
    String fileName = file.getFileName();

    WebDeploymentDescriptor webDesc = null;
    if (fileName.endsWith("web.xml")) {
      webDesc = XmlUtils.getDescriptor(fileName, null);
    } else {
      boolean found = false;

      for (int i = 0; i < files.length; i++) {
        if (files[i].getFileEntryName().endsWith("WEB-INF/web.xml") && (file.getArchiveName()).equals(files[i].getArchiveName())) {
          found = true;
          webDesc = XmlUtils.getDescriptor(files[i].getFileName(), fileName);
          break;
        }
      }

      if (!found) {
        webDesc = XmlUtils.getDescriptor((getDescriptorPath(applicationName, aliasDir) + "web.xml").trim(), fileName);
      }
    }

    if (webDesc != null) {
      WebDeploymentDescriptor oldDescr = null;
      try {
        oldDescr = loadWebDDObjectFromDBase(config, aliasDir);
      } catch (DeploymentException e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000032",
          "Cannot load an old web DD descriptor from configuration for [{0}] web application.", new Object[]{aliasDir}, e, null, null);
      }

      if (oldDescr != null) {
        SecurityRoleType[] old = null;
        SecurityRoleType[] nw = null;
        try {
          old = oldDescr.getSecurityRoles();
          nw = webDesc.getSecurityRoles();
        } catch (Exception ex) {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000033",
            "Cannot load SecurityRoles from the descriptors of the web application [{0}].", new Object[]{aliasDir}, ex, null, null);
        }

        //security roles
        if (old != null && nw != null) {
          if (old.length == nw.length) {
            for (int k = 0; k < old.length; k++) {
              if (old[k].equals(nw[k])) {
                SecurityRoleMapType oldAdd = oldDescr.getSecurityRoleFromAdditional(old[k]);
                SecurityRoleMapType newAdd = webDesc.getSecurityRoleFromAdditional(nw[k]);
                if ((oldAdd != null && newAdd != null && !oldAdd.equals(newAdd))
                  || (oldAdd != null && newAdd == null) || (oldAdd == null && newAdd != null)) {
                  return true;
                }
              } else {
                return true;
              }
            }
          } else {
            return true;
          }
        } else if (old == null && nw == null) {
          // $JL-EXC$ - nothing to do this is OK
        } else {
          return true;
        }

        if (oldDescr.getLoginConfig() != null && webDesc.getLoginConfig() != null) {
          //auth method
          if (oldDescr.getLoginConfig().getAuthMethod() != null && webDesc.getLoginConfig().getAuthMethod() != null) {
            if (!oldDescr.getLoginConfig().getAuthMethod().equals(webDesc.getLoginConfig().getAuthMethod())) {
              return true;
            }
          } else if (oldDescr.getLoginConfig().getAuthMethod() == null && webDesc.getLoginConfig().getAuthMethod() == null) {
            // $JL-EXC$ - nothing to do this is OK
          } else {
            return true;
          }

          //realm name
          if (oldDescr.getLoginConfig().getRealmName() != null && webDesc.getLoginConfig().getRealmName() != null) {
            if (!oldDescr.getLoginConfig().getRealmName().equals(webDesc.getLoginConfig().getRealmName())) {
              return true;
            }
          } else if (oldDescr.getLoginConfig().getRealmName() == null && webDesc.getLoginConfig().getRealmName() == null) {
            // $JL-EXC$ - nothing to do this is OK
          } else {
            return true;
          }
        } else if (oldDescr.getLoginConfig() == null && webDesc.getLoginConfig() == null) {
          // $JL-EXC$ - nothing to do this is OK
        } else {
          return true;
        }

        //login modules configuration
        if (oldDescr.getWebJ2EEEngine().getLoginModuleConfiguration() != null && webDesc.getWebJ2EEEngine().getLoginModuleConfiguration() != null) {
          if (!oldDescr.getWebJ2EEEngine().getLoginModuleConfiguration().equals(webDesc.getWebJ2EEEngine().getLoginModuleConfiguration())) {
            return true;
          }
        } else if (oldDescr.getWebJ2EEEngine().getLoginModuleConfiguration() == null && webDesc.getWebJ2EEEngine().getLoginModuleConfiguration() == null) {
          // $JL-EXC$ - nothing to do this is OK
        } else {
          return true;
        }
      } else {
        return true;
      }

    } else {
      return true;
    }
    return false;
  }//end of isSecurityChanged(FileUpdateInfo file, String applicationName, FileUpdateInfo[] files, String aliasDir, Configuration config)

  /**
   * Return WEB-INF directory.
   *
   * @param aliasDir
   * @param applicationName
   * @return
   */
  private String getDescriptorPath(String applicationName, String aliasDir) {
    return WebContainerHelper.getDirName(new String[]{WebContainerHelper.getDeployTempDir(applicationName), aliasDir, "root", "WEB-INF"});
  }//end of getDescriptorPath(String applicationName, String aliasDir)

  /**
   * @param fileName
   * @param aliasDir
   * @param config
   * @return
   */
  private boolean isRunAsChanged(String fileName, String aliasDir, Configuration config) {
    if (fileName.endsWith("web-j2ee-engine.xml")) {
      return false;
    }

    WebDeploymentDescriptor webDesc = XmlUtils.getDescriptor(fileName, null);

    if (webDesc != null) {
      WebDeploymentDescriptor oldDescr = null;
      try {
        oldDescr = loadWebDDObjectFromDBase(config, aliasDir);
      } catch (DeploymentException e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000034",
          "Cannot load an old web DD descriptor from configuration for [{0}] web application.", new Object[]{aliasDir}, e, null, null);
      }

      ServletType[] servlets1 = null;
      ServletType[] servlets2 = webDesc.getServlets();
      if (servlets2 == null || servlets2.length == 0) {
        return false;
      }

      if (oldDescr != null) {
        servlets1 = oldDescr.getServlets();

        for (int j = 0; j < servlets2.length; j++) {
          if (servlets2[j].getRunAs() == null) {
            continue;
          }

          boolean found = false;

          for (int k = 0; k < servlets1.length; k++) {
            RunAsType rasid = servlets1[k].getRunAs();
            if (rasid != null && servlets2[j].getRunAs().equals(rasid)) {
              found = true;
              break;
            }
          }

          if (!found) {
            return true;
          }
        }
      } else {
        if (servlets2 != null) {
          for (int j = 0; j < servlets2.length; j++) {
            if (servlets2[j].getRunAs() != null) {
              return true;
            }
          }
        }
      }

    } else {
      return true;
    }

    return false;
  }//end of isRunAsChanged(String fileName, String aliasDir, Configuration config)

  /**
   * @param files
   * @param dInfo
   * @param props
   * @return
   * @throws DeploymentException
   */
  public ApplicationDeployInfo makeSingleFileUpdate(FileUpdateInfo[] files, ContainerDeploymentInfo dInfo, Properties props) throws DeploymentException {
    ArrayList<String> aliases = new ArrayList<String>();
    ArrayList<String> aliasesForAdmin = new ArrayList<String>();
    String applicationName = dInfo.getApplicationName();

    Configuration servlet_jspConfig = ConfigurationUtils.getSubConfiguration(dInfo.getConfiguration(), containerInfo.getName(), applicationName, true);
    Configuration servlet_jspUpdate = ConfigurationUtils.getSubConfiguration(servlet_jspConfig, Constants.UPDATE, applicationName, true);

    String fileDir = WebContainerHelper.getDeployTempDir(applicationName);

    String filePathStr;

    for (int i = 0; i < files.length; i++) {
      String alias = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getAlias(files[i].getArchiveEntryName(), applicationName);
      if (alias == null) {
      	if (traceLocation.beDebug()) {
      		traceLocation.debugT("NULL is returned from method getAlias(" + files[i].getArchiveEntryName() 
      				+ ", " + applicationName + "). The possible problem can be the wrong war file name."); 
      	}
      	throw new WebDeploymentException(WebDeploymentException.INCORRECT_PROPERTIES_FOR_DEPLOY_CANNOT_FIND_WAR_ARCHIVE_WITH_NAME,
      			new Object[]{files[i].getArchiveEntryName()});
      }
      String aliasDir = WebContainerHelper.getAliasDirName(ParseUtils.convertAlias(alias));
      filePathStr = WebContainerHelper.getDirName(new String[]{fileDir, aliasDir, "root"}) + files[i].getFileEntryName();

      File nf = new File(filePathStr);
      int index = filePathStr.lastIndexOf('/');
      if (nf.isDirectory() || (index == filePathStr.length() - 1)) {
        throw new WebDeploymentException(WebDeploymentException.UPDATE_OF_DIRECTORY_IS_NOT_SUPPORTED,
          new Object[]{files[i].getFileEntryName(), files[i].getArchiveName()});
      }

      try {
        if (index != -1) {
          String packEntryName = filePathStr.substring(0, index);
          packEntryName = packEntryName.replace('/', File.separatorChar);
          File d = new File(packEntryName);

          if (!d.exists()) {
            if (!d.mkdirs()) {
              if (!d.mkdirs()) {
                throw new WebIOException(WebIOException.CANNOT_MAKE_DIRS, new Object[]{d.getAbsolutePath()});
              }
            }
          }
        }

        store2DBase(new File(files[i].getFileName()), aliasDir, files[i].getFileEntryName().replace('/', File.separatorChar), servlet_jspUpdate, nf, servlet_jspConfig);
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_UPDATE_FILE_IN_ARCHIVE,
          new Object[]{files[i].getFileEntryName(), files[i].getArchiveName()}, e);
      }

      if (!aliases.contains(alias)) {
        aliases.add(alias);
      }

      if (files[i].getFileEntryName().endsWith("WEB-INF/web.xml") || files[i].getFileEntryName().endsWith("WEB-INF/web-j2ee-engine.xml")) {
        if (!aliasesForAdmin.contains(alias)) {
          aliasesForAdmin.add(alias);
        }
      }
    }

    Configuration servlet_jspAdmin = null;
    try {
      if (servlet_jspConfig.existsSubConfiguration(Constants.ADMIN)) {
        servlet_jspAdmin = servlet_jspConfig.getSubConfiguration(Constants.ADMIN);
      }
    } catch (ConfigurationException e) {
      //$JL-EXC$
      if (traceLocation.bePath()) {
      	traceLocation.pathT("The existing version of application [" +
          applicationName + "] does not contain admin configuration." + e.toString());
      }
    }

    try {
      AppMetaData appMetaData = loadAppMetaDataObjectFromDBase(servlet_jspConfig, applicationName);
      for (int i = 0; i < aliasesForAdmin.size(); i++) {
        String aliasCanonicalized = ParseUtils.convertAlias(aliasesForAdmin.get(i));
        String aliasDir = WebContainerHelper.getAliasDirName(aliasCanonicalized);
        WebDeploymentDescriptor webDesc = getWebDeploymentDescriptor(aliasCanonicalized, applicationName, aliasDir);

        if (servlet_jspAdmin != null) {
          store2DBase(aliasesForAdmin.get(i), applicationName, servlet_jspAdmin, webDesc);
          String newFlag = ServiceContext.getServiceContext().getServerId() + "_" + System.currentTimeMillis();
          ConfigurationUtils.addConfigEntry(servlet_jspAdmin, Constants.MODIFICATION_FLAG, newFlag, applicationName, true, true);
        }

        //Store web model as a structure in the configuration
        storeWebDDObject2DBase(webDesc, servlet_jspConfig, aliasDir);

        //Store "url-session-tracking" value in the configuration
        ConfigurationUtils.addConfigEntry(servlet_jspConfig, Constants.URL_SESSION_TRACKING + aliasDir, webDesc.getWebJ2EEEngine().getUrlSessionTracking(), aliasDir, true, true);
        //update url-session-tracking in appMetaData
        if (appMetaData != null) {
          HashMap<String, Boolean> urlSessionTrackingPerModule = appMetaData.getUrlSessionTrackingPerModule();
          urlSessionTrackingPerModule.put(aliasDir, webDesc.getWebJ2EEEngine().getUrlSessionTracking());
          appMetaData.setUrlSessionTrackingPerModule(urlSessionTrackingPerModule);
          storeAppMetaDataObject2DBase(appMetaData, servlet_jspConfig, applicationName);
        }
        
      }
    } catch (Exception e) {
      throw new WebDeploymentException(WebDeploymentException.CANNOT_UPDATE_WEB_DD_IN_ADMIN, new Object[]{applicationName}, e);
    }

    ApplicationDeployInfo info = new ApplicationDeployInfo();
    String aliasArray[] = (String[]) aliases.toArray(new String[aliases.size()]);
    info.setDeployedComponentNames(aliasArray);

    String[] aliasesCanonicalized = new String[aliasArray.length];
    for (int i = 0; i < aliasArray.length; i++) {
      aliasesCanonicalized[i] = ParseUtils.convertAlias(aliasArray[i]);
    }
    info.addFilesForClassloader(getFilesForClassPath(applicationName, aliasesCanonicalized, false));

    info.setDeployProperties(props);

    return info;
  }//end of makeSingleFileUpdate(FileUpdateInfo[] files, ContainerDeploymentInfo dInfo, Properties props)

  /**
   * @param applicationName
   * @return
   */
  public ApplicationDeployInfo commitSingleFileUpdate(String applicationName) {
    ApplicationDeployInfo info = new ApplicationDeployInfo();
    try {
      info.setDeployedComponentNames(ServiceContext.getServiceContext().getDeployContext().getAliases(applicationName));
      info.setFilesForClassloader(getFilesForClassPath(applicationName, ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(applicationName), false));
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000095",
        "Cannot set ApplicationDeployInfo for [{0}] in commitSingleFileUpdate phase.", new Object[]{applicationName}, e, null, null);
    }
    return info;
  }//end of commitSingleFileUpdate(String applicationName)

  /**
   * Store file in data base
   *
   * @param file     the file that had to be stored in data base
   * @param aliasDir the alias name
   * @param path     relative path from the root of the application to the file
   * @param config   configuration where the file have to be stored
   * @param nf
   * @throws java.io.IOException
   * @throws com.sap.engine.frame.core.configuration.ConfigurationException
   *                             when problem is raised while storing in data base
   * @throws DeploymentException
   */
  private void store2DBase(File file, String aliasDir, String path, Configuration config, File nf, Configuration servlet_jspConfig) throws IOException, ConfigurationException, DeploymentException {
    if (path.startsWith(ParseUtils.separator)) {
      path = aliasDir + ParseUtils.separator + "root" + path;
    } else {
      path = aliasDir + ParseUtils.separator + "root" + ParseUtils.separator + path;
    }

    long fileId = 0;
    fileId = Long.parseLong(((String) config.getConfigEntry(Constants.FILE_COUNTER)));

    int foundEntry = -1;
    for (int i = 1; i < fileId; i++) {
      String entryPath = (String) config.getConfigEntry(i + "");
      if (entryPath != null && path.equals(entryPath)) {
        foundEntry = i;
        break;
      }
    }

    InputStream is = new FileInputStream(file);
    byte[] tempStream = null;
    try {
      if (file.getName().endsWith("web.xml") || file.getName().endsWith("web-j2ee-engine.xml")) {
        if (file.getName().endsWith("web.xml")) {
          tempStream = ServiceContext.getServiceContext().getDeployContext().getWebConverter().convertXML(is, "web", "simple");

          //merge web DD with the information from annotations before storing the object into the configuration
          WebDeploymentDescriptor webDesc = XmlUtils.parseXml(new ByteArrayInputStream(tempStream), null, aliasDir, "web.xml", "", false);
          if (webDesc.getWebAppVersion() != null && webDesc.getWebAppVersion().getValue().equals("2.5")) {
            mergeDescriptors(webDesc, servlet_jspConfig, aliasDir, aliasDir);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            webDesc.writeStandartDescriptorToStream(baos);
            tempStream = baos.toByteArray();
          }
        } else if (file.getName().endsWith("web-j2ee-engine.xml")) {
          tempStream = ServiceContext.getServiceContext().getDeployContext().getWebConverter().convertXML(is, "web-j2ee-engine", "simple");
        }

        FileUtils.writeToFile(new ByteArrayInputStream(tempStream), nf);
        is = new ByteArrayInputStream(tempStream);
      } else {
        FileUtils.writeToFile(is, nf);
        is = new FileInputStream(file);
      }
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      throw new WebDeploymentException(WebDeploymentException.EXCEPTION_OCCURED_WHILE_CONVERTING_WAR,
        new Object[]{file.getName(), aliasDir}, e);
    }

    if (foundEntry > -1) {
      ConfigurationUtils.updateFileAsStream(config, "#" + foundEntry, is, aliasDir, true);
    } else {
      ConfigurationUtils.modifyConfigEntry(config, Constants.FILE_COUNTER, "" + (fileId + 1), aliasDir, true);

      ConfigurationUtils.addConfigEntry(config, "" + fileId, path, aliasDir, true, true);

      ConfigurationUtils.addFileAsStream(config, "#" + fileId, is, aliasDir, true, true);
    }
  }//end of store2DBase(File file, String aliasDir, String path, Configuration config, File nf)

  /**
   * @param aliasCanonicalized canonicalized alias
   * @param applicationName
   * @param aliasDir
   * @return
   * @throws DeploymentException
   */
  private WebDeploymentDescriptor getWebDeploymentDescriptor(String aliasCanonicalized, String applicationName, String aliasDir) throws DeploymentException {
    WebDeploymentDescriptor webDesc = null;

    String webInfDirectory = WebContainerHelper.getDirName(new String[]{WebContainerHelper.getDeployTempDir(applicationName), aliasDir, "root", "WEB-INF"});

    String xmlFile = webInfDirectory + "web.xml";
    if (!(new File(xmlFile)).exists()) {
      WebDeploymentException wde = new WebDeploymentException(WebDeploymentException.THE_ARCHIVE_FOR_WEB_APPLICATION_DOES_NOT_CONTAIN_WEBINF_WEBXML_FILE_OR_IT_IS_SPELT_INCORRECTLY,
        new Object[]{aliasCanonicalized});
      wde.setDcName(applicationName);
      throw wde;
    }

    String additonalXmlFile = webInfDirectory + "web-j2ee-engine.xml";
    if (!new File(additonalXmlFile).exists()) {
      additonalXmlFile = null;
    }

    Thread currentThread = Thread.currentThread();
    ClassLoader currentThreadLoader = currentThread.getContextClassLoader();
    currentThread.setContextClassLoader(ServiceContext.getServiceContext().getServiceLoader());
    try {
      FileInputStream additonalXmlInputFile = null;
      if (additonalXmlFile != null && (new File(additonalXmlFile)).exists()) {
        additonalXmlInputFile = new FileInputStream(additonalXmlFile);
      }

      webDesc = XmlUtils.parseXml(new FileInputStream(xmlFile), additonalXmlInputFile, aliasCanonicalized, xmlFile, additonalXmlFile, false);
    } catch (IOException io) {
      WebDeploymentException wde = new WebDeploymentException(WebDeploymentException.THE_ARCHIVE_FOR_WEB_APPLICATION_DOES_NOT_CONTAIN_WEBINF_WEBXML_FILE_OR_IT_IS_SPELT_INCORRECTLY,
        new Object[]{aliasCanonicalized}, io);
      wde.setDcName(applicationName);
      throw wde;
    } finally {
      currentThread.setContextClassLoader(currentThreadLoader);
    }

    return webDesc;
  }//end of getWebDeploymentDescriptor(String aliasCanonicalized, String applicationName, String aliasDir)

}//end of class
