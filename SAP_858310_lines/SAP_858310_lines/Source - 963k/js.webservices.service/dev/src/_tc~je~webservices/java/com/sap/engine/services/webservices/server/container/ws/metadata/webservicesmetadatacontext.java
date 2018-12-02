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
 
package com.sap.engine.services.webservices.server.container.ws.metaData;

import java.util.Enumeration;
import java.util.Hashtable;

import com.sap.engine.services.webservices.server.container.metadata.InterfaceDefMetaDataRegistry;
import com.sap.engine.services.webservices.server.container.metadata.service.BindingDataMetaDataRegistry;

/**
 * Title: MetaDataContext 
 * Description: MetaDataContext is a context for application and service metadata
 * 
 * @author Dimitrina Stoyanova
 * @version
 */
public class WebServicesMetaDataContext {
  
  private InterfaceDefMetaDataRegistry interfaceDefMetaDataRegistry;
  private Hashtable<String, WSApplicationMetaDataContext> wsApplicationMetaDataContexts;  
  private BindingDataMetaDataRegistry globalBindingDataMetaDataRegistry;
   
  public WebServicesMetaDataContext() {
	  
  }

  /**
   * @return InterfaceDefMetaDataRegistry
   */
  public synchronized InterfaceDefMetaDataRegistry getInterfaceDefMetaDataRegistry() {
    if(interfaceDefMetaDataRegistry == null) {
      interfaceDefMetaDataRegistry = new InterfaceDefMetaDataRegistry(); 
    }
    return interfaceDefMetaDataRegistry;
  }

  /**
   * @return - a hashtable of application metadata contexts
   */
  public synchronized Hashtable getWSApplicationMetaDataContexts() {
    if(wsApplicationMetaDataContexts == null) {
      wsApplicationMetaDataContexts = new Hashtable();
    }
    return wsApplicationMetaDataContexts;
  }  
  
  /**
  * @return BindingDataMetaDataRegistry  
  */  
  public synchronized BindingDataMetaDataRegistry getGlobalBindingDataMetaDataRegistry() {
    if(globalBindingDataMetaDataRegistry == null) {
      globalBindingDataMetaDataRegistry = new BindingDataMetaDataRegistry(); 
    }   
    return globalBindingDataMetaDataRegistry;
  }
  
  public String toString() {
    String resultStr = "";         
    String nl = System.getProperty("line.separator");    
            
    resultStr += "APPLICATION META DATA CONTEXTS: " + nl + applicationConfigurationContextsToString() + nl;
    resultStr += "BINDING DATA META DATA REGISTRY: " + nl + globalBindingDataMetaDataRegistry.toString() + nl; 
     
    return resultStr;             
  }
  
  public String applicationConfigurationContextsToString() {
    String resultStr = "";
    String nl = System.getProperty("line.separator");
    
    Hashtable applicationMetaDataContexts = getWSApplicationMetaDataContexts(); 
    if(applicationMetaDataContexts.size() == 0) {
      return "EMPTY";
    }
        
    Enumeration enum1 = applicationMetaDataContexts.keys();
    int i = 0; 
    while(enum1.hasMoreElements()) {
      String applicationName = (String)enum1.nextElement(); 
      WSApplicationMetaDataContext applicationMetaDataContext = (WSApplicationMetaDataContext)applicationMetaDataContexts.get(applicationName);
      resultStr += "Application[" + i + "]: " + applicationName + nl + applicationMetaDataContext.toString() + nl;  
    } 
    
    return resultStr;     
  }

}
