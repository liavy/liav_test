/**
 * Copyright:    2002 by SAP AG
 * Company:      SAP AG, http://www.sap.com
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the license
 * agreement you entered into with SAP.
 */
package com.sap.engine.services.userstore;

import java.util.Properties;
import com.sap.engine.frame.core.configuration.*;
import com.sap.engine.services.userstore.exceptions.BaseSecurityException;
import com.sap.engine.services.userstore.filter.FilterStorage;
import com.sap.engine.services.userstore.filter.FilterUsernameImpl;
import com.sap.engine.services.userstore.filter.FilterPasswordImpl;

/**
 *  Implementation of the UserContextSpi for the needs of JCA security.
 *
 * @author  Ekaterina Zheleva
 * @version 6.30
 */
public class ConnectorContextImpl extends UserContextImpl {

  public ConnectorContextImpl() {
    super();
  }

  public ConnectorContextImpl(Configuration configuration) {
    this();
    if (configuration != null) {
      factory.setTransactionAttribute(configuration);
    }
  }

  public synchronized void enginePropertiesChanged(Properties newProps) {
    String userstoreName = newProps.getProperty("CONNECTOR");

    this.retrievePasswords = true;

    ConfigurationHandler handler = factory.getConfigurationHandler();

    if (factory.getTransactionAttribute() != null && factory.getTransactionAttribute().isValid()) {
      Configuration userstoreConfig = null;
      setLocker(userstoreName);
      UserStoreServiceFrame.lock(getLocker());
      UserStoreServiceFrame.lock(getLocker() + USERS_CONFIG_PATH);

      try {
        userstoreConfig = factory.createRootConfiguration(handler);
        userstoreConfig.createSubConfiguration(USERS_CONFIG_PATH);
        Configuration filterConfiguration = userstoreConfig.createSubConfiguration(FilterStorage.FILTER_CONFIG_PATH);
        new FilterUsernameImpl(filterConfiguration.createSubConfiguration(FilterStorage.USER_FILTER_CONFIG_PATH.substring(FilterStorage.USER_FILTER_CONFIG_PATH.indexOf("/") + 1)), true);
        new FilterPasswordImpl(filterConfiguration.createSubConfiguration(FilterStorage.PASSWORD_FILTER_CONFIG_PATH.substring(FilterStorage.PASSWORD_FILTER_CONFIG_PATH.indexOf("/") + 1)), true);
      } catch (Exception e) {
        throw new BaseSecurityException(BaseSecurityException.CANNOT_INITIALIZE_CONNECTOR_USERSTORE, e);
      } finally {
        HandlerPool.freeHandler(handler);
        UserStoreServiceFrame.releaseLock(getLocker() + USERS_CONFIG_PATH);
        UserStoreServiceFrame.releaseLock(getLocker());
      }
    } else {
      factory.initializeConfigurationPath(userstoreName);
//      Configuration rootConfiguration = null;
//      String locker = (factory.configPath.endsWith("/")) ? factory.configPath : factory.configPath + "/";
//      UserStoreServiceFrame.lock(locker);
//      try {
//        rootConfiguration = factory.createRootConfiguration(handler);
//        UserStoreConverter.transferUserstoreToNewStorage(rootConfiguration);
//        factory.commit(rootConfiguration, handler);
//      } catch (InternalException ie) {
//        try {
//          if (ie.toCommit()) {
//            throw new BaseSecurityException(BaseSecurityException.UNEXPECTED_CONNECTOR_USERSTORE_EXCEPTION);
//          }
//        } finally {
//          factory.rollback(rootConfiguration, handler);
//        }
//      } catch (Exception _) {
//        factory.rollback(rootConfiguration, handler);
//        UserStoreServiceFrame.logWarning("Cannot initialize connector UserContextSpi implementation.", _);
//      } finally {
//        UserStoreServiceFrame.releaseLock(locker);
//      }
    }
  }

  public void registerConnectorConfigurationListener(ConfigurationChangedListener listener, ConfigurationHandler configHandler) {
    factory.registerConnectorConfigurationListener(listener, configHandler);
  }

  public void unregisterConnectorConfigurationListener(ConfigurationChangedListener listener) {
    factory.unregisterConnectorConfigurationListener(listener);
  }

  public void unregisterConnectorConfigurationListener(ConfigurationChangedListener listener, Configuration transactionAttribute) {
    factory.unregisterConnectorConfigurationListener(listener, transactionAttribute);
  }

  public void setTransactionAttribute(Configuration configuration) {  
    if (configuration != null) {
      factory.setTransactionAttribute(configuration);
    }
  }

}