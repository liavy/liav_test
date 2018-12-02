/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server;

import java.util.*;
import java.io.IOException;

import com.sap.engine.frame.core.configuration.*;
import com.sap.engine.frame.core.locking.LockException;
import com.sap.engine.frame.core.locking.TechnicalLockException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIOException;
import com.sap.tc.logging.Location;

public class ConfigurationAccessor {

  private ConfigurationHandler handler = null;
  private ServiceContext serviceContext = null;

  public ConfigurationAccessor(ConfigurationHandlerFactory factory, ServiceContext serviceContext) throws java.io.IOException {
    try {
      if (factory == null) {
        throw new WebIOException(WebIOException.Cannot_create_configuration_CHF_NOT_FOUND);
      }
      if (factory != null) {
        handler = factory.getConfigurationHandler();
      }
      this.serviceContext = serviceContext;
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable ce) {
      throw new WebIOException(WebIOException.Cannot_create_configuration, ce);
    }
  }

  public ConfigurationHandler getConfigurationHandler() {
    return handler;
  }
}
