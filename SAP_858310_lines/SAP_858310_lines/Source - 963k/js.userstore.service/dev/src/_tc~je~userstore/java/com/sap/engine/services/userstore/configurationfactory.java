/**
 *  Copyright (c) 2000 by InQMy Software AG.,
 * url: http://www.inqmy.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of InQMy Software AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with InQMy.
 */
package com.sap.engine.services.userstore;

import com.sap.engine.frame.core.configuration.*;
import com.sap.engine.services.userstore.exceptions.BaseSecurityException;

/**
 *  Pool of transactionManagers.
 *
 * @author  Jako Blagoev
 * @author  Ekaterina Zheleva
 * @version 6.30
 */
class ConfigurationFactory {
  public static final String ROOT_CONTAINER_NAME = "userstore";
  public static String SECURITY_CONFIGURATIONS_PATH = "security/configurations";

  private Configuration applicationConfiguration = null;
  protected String configPath = "";

  public ConfigurationFactory() {
  }

  public void setTransactionAttribute(Configuration config) {
    try {
      this.applicationConfiguration = config;
      if (config != null) {
        this.configPath = config.getPath();
        if (!this.configPath.endsWith("/")) {
          this.configPath += "/";
        }
      } else {
        this.configPath = "";
      }
    } catch (Exception e) {
      throw new BaseSecurityException(BaseSecurityException.SET_TRANSACTION_ATTRIBUTE_ERROR, e);
    }
  }

  public Configuration getTransactionAttribute() {
    return applicationConfiguration;
  }

  public void setPath(String path) {
    this.configPath = path;
  }

  public void initializeConfigurationPath(String alias) {
    ConfigurationHandler configHandler = HandlerPool.getFreeHandler();
    try {
      Configuration config = configHandler.openConfiguration(SECURITY_CONFIGURATIONS_PATH, ConfigurationHandler.READ_ACCESS);
      configPath = (String) config.getConfigEntry(alias);
    } catch (Exception e) {
      throw new BaseSecurityException(BaseSecurityException.INIT_CONFIGURATION_PATH_ERROR, e);
    } finally {
      try {
        configHandler.closeAllConfigurations();
        HandlerPool.freeHandler(configHandler);
      } catch (Exception ex) {
        UserStoreServiceFrame.logError("Unable to close configuration handler", ex);
      }
    }
  }

  public ConfigurationHandler getConfigurationHandler() {
    return HandlerPool.getFreeHandler();
  }

  public Configuration createRootConfiguration(ConfigurationHandler configHandler) throws Exception {
    Configuration rootConfig = null;
    try {
      if (applicationConfiguration != null && applicationConfiguration.isValid()) {
        if (applicationConfiguration.existsSubConfiguration(ROOT_CONTAINER_NAME)) {
          return applicationConfiguration.getSubConfiguration(ROOT_CONTAINER_NAME);
        }
        return  applicationConfiguration.createSubConfiguration(ROOT_CONTAINER_NAME);
      } else if (configPath.length() != 0) {
        rootConfig = configHandler.openConfiguration(configPath, ConfigurationHandler.WRITE_ACCESS);
        if (rootConfig.existsSubConfiguration(ROOT_CONTAINER_NAME)) {
          rootConfig = rootConfig.getSubConfiguration(ROOT_CONTAINER_NAME);
        } else {
          rootConfig = rootConfig.createSubConfiguration(ROOT_CONTAINER_NAME);
        }
      } else {
        if (contain(configHandler.getAllRootNames(), ROOT_CONTAINER_NAME)) {
          rootConfig = configHandler.openConfiguration(ROOT_CONTAINER_NAME, ConfigurationHandler.WRITE_ACCESS);
        } else {
          rootConfig = configHandler.createRootConfiguration(ROOT_CONTAINER_NAME);
        }
      }
    } catch (Exception e) {
      throw new BaseSecurityException(BaseSecurityException.CANNOT_CREATE_ROOT_CONFIGURATION, e);
    }
    return rootConfig;
  }

  public Configuration getRootConfiguration(ConfigurationHandler configHandler) throws Exception {
    try {
      if (applicationConfiguration != null && applicationConfiguration.isValid()) {
        return applicationConfiguration.getSubConfiguration(ROOT_CONTAINER_NAME);
      }

      if (configPath.length() > 0 && !this.configPath.endsWith("/")) {
        configPath += "/";
      }
      Configuration rootConfig = configHandler.openConfiguration(configPath + ROOT_CONTAINER_NAME, ConfigurationHandler.WRITE_ACCESS);
      return rootConfig;
    } catch (ConfigurationException e) {
      throw new BaseSecurityException(BaseSecurityException.CANNOT_GET_ROOT_CONFIGURATION, e);
    }
  }

  /**
   *  Gets TransactionManagerWrapper.
   *
   * @return  TransactionManagerWrapper
   *
   * @exception   SecurityException
   */
  protected Configuration getConfiguration(String innerConfiguration, boolean update, ConfigurationHandler handler) throws NameNotFoundException, SecurityException {
    Configuration activeContainer = null;
    try {
      if (applicationConfiguration != null && applicationConfiguration.isValid()) {
        if (applicationConfiguration.getPath().endsWith(ROOT_CONTAINER_NAME)) {
          return applicationConfiguration.getSubConfiguration(innerConfiguration);
        }
        return applicationConfiguration.getSubConfiguration(ROOT_CONTAINER_NAME + '/' + innerConfiguration);
      }

      if (configPath.length() > 0 && !this.configPath.endsWith("/")) {
        configPath += "/";
      }

      if (update) {
        activeContainer = handler.openConfiguration(configPath + ROOT_CONTAINER_NAME + '/' + innerConfiguration, ConfigurationHandler.WRITE_ACCESS);
      } else {
        activeContainer = handler.openConfiguration(configPath + ROOT_CONTAINER_NAME + '/' + innerConfiguration, ConfigurationHandler.READ_ACCESS);
      }
      return activeContainer;
    } catch (NameNotFoundException nnfe) {
      throw nnfe;
    } catch (Exception e) {
      throw new BaseSecurityException(BaseSecurityException.CANNOT_GET_CONFIGURATION , new Object[] {innerConfiguration}, e);
    }
  }

  protected void commit(Configuration config, ConfigurationHandler handler) throws SecurityException {
    try {
      if (config != null && config == applicationConfiguration) {
        return;
      }

      if (handler != null) {
        handler.commit();
        handler.closeAllConfigurations();
        HandlerPool.freeHandler(handler);
      }
    } catch (Exception ex) {
      throw new BaseSecurityException(BaseSecurityException.CANNOT_COMMIT, ex);
    }
  }

  protected void rollback(Configuration config, ConfigurationHandler handler) throws SecurityException {
    try {
      if (config != null && config == applicationConfiguration) {
        return;
      }

      if (handler != null) {
        handler.rollback();
        handler.closeAllConfigurations();
        HandlerPool.freeHandler(handler);
      }
    } catch (Exception ex) {
      throw new BaseSecurityException(BaseSecurityException.CANNOT_ROLLBACK, ex);
    }
  }

  protected void close(Configuration config, ConfigurationHandler handler) throws SecurityException {
    try {
      if (config != null && config == applicationConfiguration) {
        return;
      }

      if (handler != null) {
        handler.closeAllConfigurations();
        HandlerPool.freeHandler(handler);
      }
    } catch (Exception ex) {
      throw new BaseSecurityException(BaseSecurityException.CANNOT_FORGET, ex);
    }
  }

  public void registerConnectorConfigurationListener(ConfigurationChangedListener listener, ConfigurationHandler configHandler) {
		ConfigurationHandler handler = null;
    boolean isInternalHandler = true;
    if (configHandler != null) {
			handler = configHandler;
			isInternalHandler = false;
    } else {
			handler = HandlerPool.getFreeHandler();
    }
    handler.addConfigurationChangedListener(listener, configPath + ROOT_CONTAINER_NAME, ConfigurationChangedListener.MODE_SYNCHRONOUS);
    if (isInternalHandler) {
      try {
				handler.commit();
				handler.closeAllConfigurations();
				HandlerPool.freeHandler(handler);
      } catch (Exception e) {
        UserStoreServiceFrame.logError("Unable to close configuration handler", e);
      }
    }
  }

  public void unregisterConnectorConfigurationListener(ConfigurationChangedListener listener) {
    ConfigurationHandler handler = HandlerPool.getFreeHandler();
    handler.removeConfigurationChangedListener(listener, configPath + ROOT_CONTAINER_NAME);

    try {
      handler.closeAllConfigurations();
    } catch (Exception e) {
      UserStoreServiceFrame.logError("Unable to close configuration handler", e);
    }
  }

  public void unregisterConnectorConfigurationListener(ConfigurationChangedListener listener, Configuration transactionAttribute) {
    try {
      setTransactionAttribute(transactionAttribute);
      ConfigurationHandler handler = HandlerPool.getFreeHandler();
      handler.removeConfigurationChangedListener(listener, configPath + ROOT_CONTAINER_NAME);
      try {
        Configuration config = getRootConfiguration(handler);
        config.deleteConfiguration();
        handler.commit();
        handler.closeAllConfigurations();
      } catch (Exception e) {
        handler.rollback();
        throw e;
      }
    } catch (Exception e) {
      UserStoreServiceFrame.logError("Unable to close configuration handler", e);
    }
  }

  private boolean contain(String[] arr, String sbj) {
    if (arr == null || arr.length == 0) {
      return false;
    }
    for (int i = 0; i < arr.length; i++) {
      if (arr[i].equals(sbj)) {
        return true;
      }
    }
    return false;
  }

}