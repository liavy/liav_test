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
 
package com.sap.engine.services.webservices.server;

import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.services.webservices.server.container.OfflineBaseServiceContext;
import com.sap.engine.services.webservices.server.container.ws.ServiceContext;
import com.sap.engine.services.webservices.server.container.wsclients.ServiceRefContext;

/**
 * Title: WebServicesContainer
 * Description: WebServicesContainer 
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class WebServicesContainer {
    
  private static ServiceContext serviceContext; 
  private static ServiceRefContext serviceRefContext; 
  private static WebServicesContainerManipulatorImpl wsManipulator; 
  private static WebServicesContainerManipulatorImpl wsClientsManipulator;   
     
  /**
   * @return ServiceContext 
   */
  public synchronized static ServiceContext getServiceContext() {
    if(serviceContext == null) {
      serviceContext = new ServiceContext(); 
    }
    
    return serviceContext;
  }
  
  /**
   * @return ServiceRefContext 
   */
  public synchronized static ServiceRefContext getServiceRefContext() {
    if(serviceRefContext == null) {
      serviceRefContext = new ServiceRefContext(); 
    }
  
    return serviceRefContext;
  }
  
  /**
   * @return OfflineBaseServiceContext 
   */
  public static OfflineBaseServiceContext getOfflineBaseServiceContext() throws ConfigurationException {     
    return WebServicesFrame.webServicesDeployManager.getOfflineServiceContext();    	 
  }
  
  public static WebServicesContainerManipulatorImpl getWSContainerManipulator() {
    if(wsManipulator == null) {
      wsManipulator = new WebServicesContainerManipulatorImpl();
    }
    
    return wsManipulator;
  }
  
  public static WebServicesContainerManipulatorImpl getWSClientsContainerManipulator() {
   if(wsClientsManipulator == null) {
     wsClientsManipulator = new WebServicesContainerManipulatorImpl();   
     wsClientsManipulator.setConsumer(true);
   }
    
   return wsClientsManipulator;
  }
  
}
