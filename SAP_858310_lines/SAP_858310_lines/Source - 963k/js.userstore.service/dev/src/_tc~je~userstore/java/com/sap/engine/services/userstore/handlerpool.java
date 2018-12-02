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

import com.sap.engine.frame.core.configuration.ConfigurationHandlerFactory;
import com.sap.engine.frame.core.configuration.ConfigurationHandler;

import java.util.*;

class HandlerPool {

  private static List freeHandlers = new LinkedList();
  static ConfigurationHandlerFactory configHandlerFactory = null;

  static synchronized  ConfigurationHandler getFreeHandler() {
    try {
      return (ConfigurationHandler) freeHandlers.remove(0);
    } catch (Exception e) {
      try {
        return configHandlerFactory.getConfigurationHandler();
      } catch (Exception ex) {
        throw new SecurityException(ex.getMessage());
      }
    }
  }

  static synchronized void freeHandler(ConfigurationHandler handler) {
    freeHandlers.add(handler);
  }
}