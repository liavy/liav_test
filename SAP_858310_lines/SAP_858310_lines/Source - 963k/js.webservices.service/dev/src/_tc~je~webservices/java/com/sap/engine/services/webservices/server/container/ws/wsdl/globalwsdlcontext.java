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
 
package com.sap.engine.services.webservices.server.container.ws.wsdl;

import java.util.Hashtable;

/**
 * Title: GlobalWSDLContext
 * Description: GlobalWSDLContext
 * 
 * @author Dimitrina Stoyanova
 * @version
 */
public class GlobalWSDLContext {
    
  private ApplicationWSDLContext interfaceDefWSDLContextRegistry;    
  private Hashtable<String, ApplicationWSDLContext> applicationWSDLContexts;  
   
  public GlobalWSDLContext() {
	 	  
  }
  
  /**
   * @return - interface definition wsdl context registry
   */
  public synchronized ApplicationWSDLContext getInterfaceDefWSDLContextRegistry() {
    if(interfaceDefWSDLContextRegistry == null) {
      interfaceDefWSDLContextRegistry = new ApplicationWSDLContext();
    }
    return interfaceDefWSDLContextRegistry;
  } 
   
  /**
   * @return - a hashtable of application wsdl contexts
   */
  public synchronized Hashtable getApplicationWSDLContexts() {
    if(applicationWSDLContexts == null) {
      applicationWSDLContexts = new Hashtable(); 
    }
    return applicationWSDLContexts;
  }

}
