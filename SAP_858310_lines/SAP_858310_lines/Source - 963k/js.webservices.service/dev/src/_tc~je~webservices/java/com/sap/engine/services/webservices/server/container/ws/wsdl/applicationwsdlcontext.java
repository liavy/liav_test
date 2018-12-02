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

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Title: ApplicationWSDLContext
 * Description: ApplicationWSDLContext
 * 
 * @author Dimitrina Stoyanova
 * @version
 */
public class ApplicationWSDLContext {
  
  public static final String ROOT = "root";  
  public static final String BINDING = "binding";
  public static final String PORTTYPE = "porttype";    
  
  private Hashtable<String, WSDLContext> wsdlContexts; 
  
  public ApplicationWSDLContext() {
    
  }
  
  /**
   * @return - a hashtable of wsdl contexts
   */
  public synchronized Hashtable getWSDLContexts() {
    if(wsdlContexts == null) {
      wsdlContexts = new Hashtable();
    }
    return wsdlContexts;
  }  
  
  public boolean containsWSDLContextKey(String key) {    
    return getWSDLContexts().containsKey(key);
  }
  
  public boolean containsWSDL(String wsdlContextKey, String wsdlRegistryKey) {
    if(!containsWSDLContextKey(wsdlContextKey)) {
      return false; 
    }
    
    return getWSDLContext(wsdlContextKey).containsWSDLRegistryKey(wsdlRegistryKey);    
  }
  
  public void putWSDLContext(String key, WSDLContext wsdlContext) {
    if(!isValidWSDLContextKey(key)) {
      //TODO - throw exception
      return;
    }
    getWSDLContexts().put(key, wsdlContext);
  }
  
  public WSDLContext getWSDLContext(String key) {
    return (WSDLContext)getWSDLContexts().get(key);
  }
  
  public WSDLContext removeWSDLContext(String key) {
    return (WSDLContext)getWSDLContexts().remove(key);
  }

  public static boolean isValidWSDLContextKey(String key) {
    if(key.equals(ROOT)) {
      return true; 
    }
    if(key.equals(BINDING)) {
      return true; 
    }
    if(key.equals(PORTTYPE)) {
      return true; 
    }    
    return false; 
  }
  
  public String toString() {
    String resultStr = ""; 
    String nl = System.getProperty("line.separator");
    
    Hashtable wsdlContexts = getWSDLContexts(); 
    if(wsdlContexts.size() == 0) {
      return "EMPTY";
    } 
    
    Enumeration enum1 = wsdlContexts.keys();
    int i = 0;  
    while(enum1.hasMoreElements()) {
      String type = (String)enum1.nextElement(); 
      WSDLContext wsdlContext = (WSDLContext)wsdlContexts.get(type);       
      resultStr += "WSDL CONTEXT[" + type + "]: " + nl + wsdlContext.toString() + nl;      
    }    
    
    return resultStr;          
  }
        
}
