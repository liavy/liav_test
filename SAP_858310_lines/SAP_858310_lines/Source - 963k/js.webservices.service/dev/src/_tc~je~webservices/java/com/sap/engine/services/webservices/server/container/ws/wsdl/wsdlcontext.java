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
 * Title: WSDLContext
 * Description: WSDLContext is a context for wsdl registries
 * 
 * @author Dimitrina Stoyanova
 * @version
 */
public class WSDLContext {
  
  public static final String DOCUMENT = "document";
  public static final String RPC = "rpc";
  public static final String RPC_ENC = "rpc_enc";
  public static final String DEFAULT = "default";
      
  private Hashtable<String, WSDLRegistry> wsdlRegistries; 
  
  public WSDLContext() {   
    
  }      
    
  /**
   * @return - a hashtable of wsdl registries
   */
  public synchronized Hashtable getWsdlRegistries() {
    if(wsdlRegistries == null) {
      wsdlRegistries = new Hashtable();
    }
    return wsdlRegistries;
  }
  
  public boolean containsWSDLRegistryKey(String key) {
    return getWsdlRegistries().containsKey(key);
  } 
 
  public void putWSDLRegistry(String key, WSDLRegistry wsdlRegistry) {
    if(!isValidWSDLRegistryKey(key)) {
      //TODO - throw exception
      return;
    }     
    getWsdlRegistries().put(key, wsdlRegistry);
  }
  
  public WSDLRegistry getWSDLRegistry(String key) {
    return (WSDLRegistry)getWsdlRegistries().get(key);
  }
  
  public WSDLRegistry removeWSDLRegistry(String key) {
    return (WSDLRegistry)getWsdlRegistries().remove(key);
  }
    
  public boolean isValidWSDLRegistryKey(String key) {
    if(key.equals(DOCUMENT)) {
      return true; 
    }    
    if(key.equals(RPC)) {
      return true; 
    }    
    if(key.equals(RPC_ENC)) {
      return true; 
    }    
    if(key.equals(DEFAULT)) {
      return true; 
    }
    return false;      
  }   
   
  public String toString() {
    String resultStr = ""; 
    String nl = System.getProperty("line.separator");
    
    Hashtable wsdlRegistries = getWsdlRegistries(); 
    if(wsdlRegistries.size() == 0) {
      return "EMPTY";
    } 
    
    Enumeration enum1 = wsdlRegistries.keys();
    int i = 0;  
    while(enum1.hasMoreElements()) {
      String use = (String)enum1.nextElement(); 
      WSDLRegistry wsdlRegistry = (WSDLRegistry)wsdlRegistries.get(use);       
      resultStr += "WSDL REGISTRY[" + use + "]: " + nl + wsdlRegistry.toString() + nl ;      
    }    
    
    return resultStr;          
  } 
     
}
