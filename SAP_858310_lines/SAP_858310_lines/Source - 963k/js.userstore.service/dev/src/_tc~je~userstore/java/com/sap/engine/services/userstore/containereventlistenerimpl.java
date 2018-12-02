/**
 * Copyright (c) 2002 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.userstore;

import com.sap.engine.frame.container.event.ContainerEventListener;
import com.sap.engine.interfaces.keystore.KeystoreManager;

import java.util.Properties;

/**
 *
 *
 * @version 6.30
 */
public class ContainerEventListenerImpl implements ContainerEventListener {

  public void containerStarted() {

  }

  public void beginContainerStop() {

  }

  public void serviceStarted(String serviceName, Object serviceInterface) {
//    if (serviceName.equals("keystore")) {
//      UserStoreServiceFrame.setKeystore((KeystoreManager) serviceInterface);
//    }
  }

  public void serviceNotStarted(String serviceName) {

  }

  public void beginServiceStop(String serviceName) {

  }

  public void serviceStopped(String serviceName) {
//    if (serviceName.equals("keystore")) {
//      UserStoreServiceFrame.setKeystore(null);
//    }
  }

  public void interfaceAvailable(String interfaceName, Object interfaceImpl) {
    if (interfaceName.equals("log")) {
      UserStoreServiceFrame.useCategory_LocationLogging(true);
    } else if (interfaceName.equals("keystore_api")) {
      UserStoreServiceFrame.setKeystore((KeystoreManager) interfaceImpl);
    }
  }

  public void interfaceNotAvailable(String interfaceName) {
    if (interfaceName.equals("log")) {
      UserStoreServiceFrame.useCategory_LocationLogging(false);
    } else if (interfaceName.equals("keystore_api")) {
      UserStoreServiceFrame.setKeystore(null);
    }
  }

  public void markForShutdown(long timeout) {
  }

  public boolean setServiceProperty(String key, String value) {
    return false;
  }

  public boolean setServiceProperties(Properties serviceProperties) {
    return false;
  }
}

