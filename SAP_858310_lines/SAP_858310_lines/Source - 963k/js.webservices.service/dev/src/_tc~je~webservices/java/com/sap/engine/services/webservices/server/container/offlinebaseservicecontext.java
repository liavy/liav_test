/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 *This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */

package com.sap.engine.services.webservices.server.container;

import java.util.Iterator;
import java.util.Map.Entry;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.frame.core.configuration.ConfigurationHandler;
import com.sap.engine.frame.core.configuration.ConfigurationLockedException;
import com.sap.engine.frame.core.configuration.NameNotFoundException;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.server.container.metadata.InterfaceDefMetaData;
import com.sap.engine.services.webservices.server.container.metadata.InterfaceDefMetaDataRegistry;

/**
 * Title: OfflineBaseServiceContext 
 * Description: OfflineBaseServiceContext
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class OfflineBaseServiceContext {
  
  public static final String APPS_CONFIGURATION_PATH    = "webservices/apps"; 
  public static final String WS_CONFIGURATION_NAME      = "ws";
  public static final String INTERFACE_DEFINITIONS_NAME = "interface_definitions"; 
	
  private InterfaceDefMetaDataRegistry interfaceDefMetaDataRegistry; 

  public synchronized InterfaceDefMetaDataRegistry getInterfaceDefMetaDataRegistry() {
    if(interfaceDefMetaDataRegistry == null) {
      interfaceDefMetaDataRegistry = new InterfaceDefMetaDataRegistry();	
    }
    
    return interfaceDefMetaDataRegistry;
  } 
  
  public InterfaceDefMetaData getInterfaceDefMetaData(String interfaceDefinitionId) {
    return getInterfaceDefMetaDataRegistry().getInterfaceDefMetaData(interfaceDefinitionId); 	  
  } 
    
}
