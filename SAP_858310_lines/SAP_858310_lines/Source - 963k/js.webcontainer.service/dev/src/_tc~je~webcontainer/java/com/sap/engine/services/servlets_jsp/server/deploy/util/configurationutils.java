/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.deploy.util;

import java.io.File;
import java.io.InputStream;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.frame.core.configuration.FrameUtils;
import com.sap.engine.frame.core.configuration.NameAlreadyExistsException;
import com.sap.engine.frame.core.configuration.NameNotFoundException;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.tc.logging.Location;

/**
 * Currently WebContainer gets from the deploy service the application's root configuration,
 * where it creates/uses its own subconfiguration named 
 * <code>
 * com.sap.engine.services.servlets_jsp.server.deploy.util.Constants.CONTAINER_NAME - servlet_jsp
 * </code>
 * and create/uses the following subconfigurations there:
 * <code>
 * com.sap.engine.services.servlets_jsp.server.deploy.util.Constants.UPDATE - update,
 * com.sap.engine.services.servlets_jsp.server.deploy.util.Constants.BACKUP - backup,
 * com.sap.engine.services.servlets_jsp.server.deploy.util.Constants.ADMIN - admin,
 * WCEPROVIDER_(WCE provider name)
 * </code>.
 * 
 * 
 * @author Violeta Georgieva
 * @version 7.0
 */
public class ConfigurationUtils {
  private static Location currentLocation = Location.getLocation(ConfigurationUtils.class);
  private static Location traceLocation = LogContext.getLocationDeploy();


  // ----- Configuration -----

  /**
   * @param rootConfiguration
   * @param subConfigurationName
   * @param applicationName
   * @return
   * @throws DeploymentException
   */
  public static Configuration createSubConfiguration(Configuration rootConfiguration, String subConfigurationName, String applicationName, boolean getSubConfiguration) throws DeploymentException {
    Configuration subConfiguration = null;
    try {
      subConfiguration = rootConfiguration.createSubConfiguration(subConfigurationName);
    } catch (NameAlreadyExistsException e) {
// $JL-EXC$
      if (getSubConfiguration) {
        subConfiguration = getSubConfiguration(rootConfiguration, subConfigurationName, applicationName, true);
      }
    } catch (ConfigurationException e) {
      throw new WebDeploymentException(WebDeploymentException.EXCEPTION_OCCURED_IN_METHOD_FOR_APPLICATION,
        new Object[]{"createSubConfiguration()", applicationName}, e);
    }
    return subConfiguration;
  }//end of createSubConfiguration(Configuration rootConfiguration, String subConfigurationName, String applicationName)

  /**
   * @param rootConfiguration
   * @param subConfigurationName
   * @param applicationName
   * @return
   */
  public static Configuration getSubConfiguration(Configuration rootConfiguration, String subConfigurationName, String applicationName, boolean throwException) throws DeploymentException {
    Configuration subConfiguration = null;
    try {
      subConfiguration = rootConfiguration.getSubConfiguration(subConfigurationName);
    } catch (ConfigurationException e) {
      if (throwException) {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_GET_CONFIGURATION_FOR_APPLICATION,
          new Object[]{subConfigurationName, applicationName}, e);
      }else{
    	  LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000047",
    		  "Exception occurred in getSubConfiguration() method of application [{0}].", new Object[]{applicationName}, e, null, null);
      }
    }
    return subConfiguration;
  }//end of getSubConfiguration(Configuration rootConfiguration, String subConfigurationName, String applicationName)

  /**
   * @param rootConfiguration
   * @param subConfigurationName
   * @param applicationName
   */
  public static void deleteConfiguration(Configuration rootConfiguration, String subConfigurationName, String applicationName) {
    try {
      rootConfiguration.deleteConfiguration(subConfigurationName);
    } catch (NameNotFoundException e) {
    	LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000009",
        "Cannot delete the configuration for application [{0}] because it does not exist.", 
        new Object[]{applicationName}, e, null, null);
    } catch (ConfigurationException e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000010",
        "Cannot delete the configuration for application [{0}].", new Object[]{applicationName}, e, null, null);
    }
  }//end of deleteConfiguration(Configuration rootConfiguration, String subConfigurationName, String applicationName)


  // ----- Config Entry -----

  /**
   * @param configuration
   * @param entryName
   * @param entryValue
   * @param applicationName
   * @param modify
   * @throws DeploymentException
   */
  public static void addConfigEntry(Configuration configuration, String entryName, Object entryValue, String applicationName, boolean modify, boolean throwException) throws DeploymentException {
    try {
      configuration.addConfigEntry(entryName, entryValue);
    } catch (NameAlreadyExistsException e) {
      if (modify) {
        modifyConfigEntry(configuration, entryName, entryValue, applicationName, throwException);
      } else {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000011",
          "Entry with key [{0}] already exists.", new Object[]{entryName}, e, null, null);
      }
    } catch (ConfigurationException e) {
      if (!throwException) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000012",
          "Entry with key [{0}] already exists.", new Object[]{entryName}, e, null, null);
      } else {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_ADD_CONFIGURATION_ENTRY_WITH_KEY,
          new Object[]{entryName}, e);
      }
    }
  }//end of addConfigEntry(Configuration configuration, String entryName, String entryValue, String applicationName)

  /**
   * @param configuration
   * @param entryName
   * @param entryValue
   * @param applicationName
   */
  public static void modifyConfigEntry(Configuration configuration, String entryName, Object entryValue, String applicationName, boolean throwException) throws DeploymentException {
    try {
      configuration.modifyConfigEntry(entryName, entryValue);
    } catch (ConfigurationException e) {
      if (!throwException) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000048",
          "Entry with key [{0}] already exists and cannot overwrite it. May be no write access.", 
          new Object[]{entryName}, e, null, null);
      } else {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_UPDATE_CONFIGURATION_ENTRY_WITH_KEY,
          new Object[]{entryName}, e);
      }
    }
  }//end of modifyConfigEntry(Configuration configuration, String entryName, String entryValue, String applicationName)

  /**
   * @param configuration
   * @param entryName
   * @param applicationName
   */
  public static void deleteConfigEntry(Configuration configuration, String entryName, String applicationName) {
    try {
      configuration.deleteConfigEntry(entryName);
    } catch (ConfigurationException e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000049",
        "Can not delete config entry [{0}].", new Object[]{entryName}, e, null, null);
    }
  }//end of deleteConfigEntry(Configuration configuration, String entryName, String applicationName)

  /**
   * @param configuration
   * @param entryName
   * @param alias
   * @return
   */
  public static Object getConfigEntry(Configuration configuration, String entryName, String alias) {
    Object configEntry = null;
    try {
      configEntry = configuration.getConfigEntry(entryName);
    } catch (NameNotFoundException e) {
      // $JL-EXC$
      if (traceLocation.bePath()) {
      	traceLocation.pathT("Entry [" + entryName + "] not found in DB for application [" + alias + "]. " + e.getMessage());
      }
    } catch (ConfigurationException e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000013",
        "Cannot read from database [{0}] property.", new Object[]{entryName}, e, null, null);
    }
    return configEntry;
  }//end of getConfigEntry(Configuration configuration, String entryName, String alias)


  // ----- File Entry -----

  /**
   * @param configuration
   * @param entryName
   * @param entryValue
   * @param applicationName
   * @param update
   * @throws DeploymentException
   */
  public static void addFileAsStream(Configuration configuration, String entryName, InputStream entryValue, String applicationName, boolean update, boolean throwException) throws DeploymentException {
    try {
      configuration.addFileAsStream(entryName, entryValue);
    } catch (NameAlreadyExistsException e) {
      if (update) {
        updateFileAsStream(configuration, entryName, entryValue, applicationName, throwException);
      } else {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000014",
          "Entry with key [{0}] already exists.", new Object[]{entryName}, e, null, null);
      }
    } catch (ConfigurationException e) {
      if (!throwException) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000015",
          "Entry with key [{0}] already exists.", new Object[]{entryName}, e, null, null);
      } else {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_ADD_CONFIGURATION_ENTRY_WITH_KEY,
          new Object[]{entryName}, e);
      }
    }
  }//end of addFileAsStream(Configuration configuration, String entryName, InputStream entryValue, String applicationName, boolean update)

  /**
   * @param configuration
   * @param entryName
   * @param entryValue
   * @param applicationName
   */
  public static void updateFileAsStream(Configuration configuration, String entryName, InputStream entryValue, String applicationName, boolean throwException) throws DeploymentException {
    try {
      configuration.updateFileAsStream(entryName, entryValue);
    } catch (ConfigurationException e) {
      if (!throwException) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000050",
          "Entry with key [{0}] already exists and cannot overwrite it. May be no write access.", 
          new Object[]{entryName}, e, null, null);
      } else {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_ADD_CONFIGURATION_ENTRY_WITH_KEY,
          new Object[]{entryName}, e);
      }
    }
  }//end of updateFileAsStream(Configuration configuration, String entryName, InputStream entryValue, String applicationName)

  /**
   * @param configuration
   * @param file
   * @param applicationName
   * @param update
   * @param throwException
   * @throws DeploymentException
   */
  public static void addFileEntry(Configuration configuration, File file, String applicationName, boolean update, boolean throwException) throws DeploymentException {
    try {
      configuration.addFileEntry(file);
    } catch (NameAlreadyExistsException ex) {
// $JL-EXC$
      if (update) {
        updateFile(configuration, file, applicationName, throwException);
      }
    } catch (ConfigurationException e) {
      if (!throwException) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000016",
          "Entry with key [{0}] already exists.", new Object[]{file.getName()}, e, null, null);
      } else {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_STORE_FILE_FROM_APPLICATION_IN_DATABASE,
          new Object[]{file.getPath(), applicationName}, e);
      }
    }
  }//end of addFileEntry(Configuration configuration, File file, String applicationName, boolean update, boolean throwException)

  /**
   * @param configuration
   * @param file
   * @param applicationName
   * @param throwException
   * @throws DeploymentException
   */
  public static void updateFile(Configuration configuration, File file, String applicationName, boolean throwException) throws DeploymentException {
    try {
      configuration.updateFile(file);
    } catch (ConfigurationException e) {
      if (!throwException) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000051",
          "Entry with key [{0}] already exists and cannot overwrite it. May be no write access.", 
          new Object[]{file.getName()}, e, null, null);
      } else {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_UPDATE_CONFIGURATION_ENTRY_WITH_KEY,
          new Object[]{file.getName()}, e);
      }
    }
  }//end of updateFile(Configuration configuration, File file, String applicationName, boolean throwException)

  /**
   * @param configuration
   * @param entryName
   * @param fileName
   * @param applicationName
   * @param update
   * @param throwException
   * @throws DeploymentException
   */
  public static void addFileEntryByKey(Configuration configuration, String entryName, File fileName, String applicationName, boolean update, boolean throwException) throws DeploymentException {
    try {
      configuration.addFileEntryByKey(entryName, fileName);
    } catch (NameAlreadyExistsException e) {
      if (update) {
        updateFileEntryByKey(configuration, entryName, fileName, applicationName, throwException);
      } else {
    	  //TODO:Polly check
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000017",
          "Entry with key [{0}] already exists.", new Object[]{entryName}, e, null, null);
      }
    } catch (ConfigurationException e) {
      if (!throwException) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000018",
          "Entry with key [{0}] already exists.", new Object[]{entryName}, e, null, null);
      } else {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_ADD_CONFIGURATION_ENTRY_WITH_KEY,
          new Object[]{entryName}, e);
      }
    }
  }//end of addFileEntryByKey(Configuration configuration, String entryName, File fileName, String applicationName, boolean update, boolean throwException)

  /**
   * @param configuration
   * @param entryName
   * @param fileName
   * @param applicationName
   * @param throwException
   * @throws DeploymentException
   */
  public static void updateFileEntryByKey(Configuration configuration, String entryName, File fileName, String applicationName, boolean throwException) throws DeploymentException {
    try {
      configuration.updateFileByKey(entryName, fileName);
    } catch (ConfigurationException e) {
      if (!throwException) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000052", 
          "Entry with key [{0}] already exists and cannot overwrite it. May be no write access.", 
          new Object[]{entryName}, e, null, null);
      } else {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_UPDATE_CONFIGURATION_ENTRY_WITH_KEY,
          new Object[]{entryName}, e);
      }
    }
  }//end of updateFileEntryByKey(Configuration configuration, String entryName, File fileName, String applicationName, boolean throwException)

  /**
   * @param configuration
   * @param entryName
   * @param alias
   * @return
   */
  public static InputStream getFile(Configuration configuration, String entryName, String alias) {
    InputStream is = null;
    try {
      is = configuration.getFile(entryName);
    } catch (NameNotFoundException e) {
      // $JL-EXC$
      if (traceLocation.bePath()) {
      	traceLocation.pathT("File [" + entryName + "] not found in DB for application [" + alias + "]. " + e.getMessage());
      }
    } catch (ConfigurationException e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000053",
        "Error occurs while trying to get file [{0}] for application [{1}]. ", new Object[]{entryName, alias}, e, null, null);
    }
    return is;
  }//end of getFile(Configuration configuration, String entryName, String alias)
  
  /**
   * Replaces the forbidden characters 
   * @see  com.sap.engine.frame.core.configuration.FrameUtils.FORBIDDEN_CONFIGNAME_CHARS
   * with underscore characters.
   * Note they may be name collisions after the conversions
   * @param name
   * @return
   */
  public static String convertToConfigForm(String name) {
  	String result = name;
  	for (String currentForbidden : FrameUtils.FORBIDDEN_CONFIGNAME_CHARS) {
  		result = result.replace(currentForbidden, "_");
  	}
  	return result;
  }
}//end of class
