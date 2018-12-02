/*
 * Copyright (c) 2002 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.timeout;

import com.sap.engine.frame.ApplicationServiceContext;
import com.sap.engine.frame.ApplicationServiceFrame;
import com.sap.engine.frame.ServiceException;
import com.sap.engine.frame.container.registry.ObjectRegistry;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

public class TimeoutFrame implements ApplicationServiceFrame {

  private ObjectRegistry objectRegistry = null;
  private ApplicationServiceContext applicationServiceContext = null;
  private TimeoutManagerImpl timeout = null;

  /**
   * Category used for logging
   */
  public static Category category = Category.SYS_SERVER;

  /**
   * Location used for logging
   */
  public static Location location = Location.getLocation(TimeoutFrame.class);

  public void start(ApplicationServiceContext serviceContext) throws ServiceException {
    try {
      this.applicationServiceContext = serviceContext;
      objectRegistry = serviceContext.getContainerContext().getObjectRegistry();
      timeout = new TimeoutManagerImpl(serviceContext.getServiceState().getProperties(), serviceContext.getCoreContext().getThreadSystem());
      TimeoutManager tm = timeout;
      objectRegistry.registerInterface(tm);
      serviceContext.getServiceState().registerManagementInterface(timeout);
    } catch (Exception e) {
      SimpleLogger.traceThrowable(Severity.ERROR, location,
          "ASJ.timeout.000001",
          "Exception while starting the Timeout service", e);
      throw new ServiceException(e);
    }
    SimpleLogger.trace(Severity.PATH, location, "ASJ.timeout.000017",
        "Timeout service started");
  }

  public void stop() {
    try {
      timeout.stop();
      applicationServiceContext.getServiceState().unregisterManagementInterface();
    } finally {
      objectRegistry.unregisterInterface();
    }
    SimpleLogger.trace(Severity.PATH, location, "ASJ.timeout.000018",
        "Timeout service stopped");
  }

}

