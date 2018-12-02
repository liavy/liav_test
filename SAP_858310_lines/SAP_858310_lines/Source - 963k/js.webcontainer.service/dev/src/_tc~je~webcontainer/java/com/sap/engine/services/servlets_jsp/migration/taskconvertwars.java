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
package com.sap.engine.services.servlets_jsp.migration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.lib.io.FileUtils;
import com.sap.engine.lib.io.hash.HashUtils;
import com.sap.engine.lib.jar.JarExtractor;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.util.ConfigurationUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.util.Constants;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.tc.logging.Location;

/**
 * @author Violeta Georgieva
 * @version 7.0
 */
public class TaskConvertWars {
  private static final Location currentLocation = Location.getLocation(TaskConvertWars.class);

  public static void convertAndStoreAltDDToDB(Configuration configuration, String aliasDir) throws DeploymentException {
    InputStream altDD = ConfigurationUtils.getFile(configuration, Constants.ALT_DD + aliasDir, aliasDir);

    if (altDD != null) {
      try {
        byte[] tempStream = null;
        try {
          tempStream = ServiceContext.getServiceContext().getDeployContext().getWebConverter().convertXML(altDD, "web", "extended");
        } finally {
          altDD.close();
        }
        altDD = new ByteArrayInputStream(tempStream);
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
    	  //TODO:Polly remove log
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000263",
          "Exception occurred while converting alternate descriptor for [{0}].", new Object[]{aliasDir}, e, null, null);
        throw new WebDeploymentException(WebDeploymentException.EXCEPTION_OCCURED_WHILE_CONVERTING_WAR,
          new Object[]{aliasDir, ""}, e);
      }

      ConfigurationUtils.updateFileAsStream(configuration, Constants.ALT_DD + aliasDir, altDD, aliasDir, true);
    }
  }//end of convertAndStoreAltDDToDB(Configuration configuration, String aliasDir)

  public static void convertAndStoreWarToDB(Configuration configuration, Configuration backupConfiguration, File war, String warName) throws DeploymentException, IOException {
    try {
      ServiceContext.getServiceContext().getDeployContext().getWebConverter().convertWAR(war, warName, backupConfiguration, "extended", null);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
    	//TODO:Polly remove log
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000264",
        "Exception occurred while converting war [{0}].", new Object[]{war.getName()}, e, null, null);
      throw new WebDeploymentException(WebDeploymentException.EXCEPTION_OCCURED_WHILE_CONVERTING_WAR,
        new Object[]{war.getName(), ""}, e);
    }

    ConfigurationUtils.updateFileEntryByKey(configuration, warName.replace('/', '_'), war, "", true);
  }//end of convertAndStoreWarToDB(Configuration configuration, Configuration backupConfiguration, File war, String warName)

  public static String getDeployTempDir(String applicationName) {
    String tempDir = null;
    try {
      tempDir = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getMyWorkDirectory(applicationName);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
    	//TODO:Polly type:ok
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000265",
        "Cannot get working directory.", e, null, null);
      tempDir = ServiceContext.getServiceContext().getWorkDirectory();
    }
    return tempDir;
  }//end of getDeployTempDir(String applicationName)

  public static void extractWar(String applicationName, File rootDirectory, File warFile) throws DeploymentException {
    if (!warFile.exists()) {
      return;
    }

    if (rootDirectory.exists()) {
      FileUtils.deleteDirectory(rootDirectory);
    }

    JarExtractor jarExtractor = new JarExtractor(warFile.getAbsolutePath(), rootDirectory.getAbsolutePath());

    try {
      jarExtractor.extractJar();
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
    	//TODO:Polly remove log
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000266",
        "Cannot extract the war file of the application.", e, null, null);
      throw new WebDeploymentException(WebDeploymentException.CANNOT_EXTRACT_THE_WAR_FILE_OF_THE_APPLICATION,
        new Object[]{warFile.getName(), applicationName}, e);
    }
  }//end of extractWar(String applicationName, String alias, File warFile)

  public static void storeWebXmlToDB(Configuration configuration, String aliasDir, String webInfDir) throws DeploymentException, IOException {
    File webDD = new File(webInfDir + "web.xml");
    if (!webDD.exists()) {
      throw new WebDeploymentException(WebDeploymentException.THE_ARCHIVE_FOR_WEB_APPLICATION_DOES_NOT_CONTAIN_WEBINF_WEBXML_FILE_OR_IT_IS_SPELT_INCORRECTLY,
        new Object[]{aliasDir});
    }

    File addWebDD = new File(webInfDir + "web-j2ee-engine.xml");

    InputStream altDD = ConfigurationUtils.getFile(configuration, Constants.ALT_DD + aliasDir, aliasDir);
    if (altDD != null) {
      FileUtils.writeToFile(altDD, webDD);
      ConfigurationUtils.deleteConfigEntry(configuration, Constants.ALT_DD + aliasDir, aliasDir);
    }

    ConfigurationUtils.addFileEntryByKey(configuration, Constants.WEB_DD + aliasDir, webDD, aliasDir, true, true);

    if (addWebDD.exists()) {
      ConfigurationUtils.addFileEntryByKey(configuration, Constants.ADD_WEB_DD + aliasDir, addWebDD, aliasDir, true, true);
    }
  }//end of storeWebXmlToDB(Configuration configuration, String aliasDir, String webInfDir)

}//end of class
