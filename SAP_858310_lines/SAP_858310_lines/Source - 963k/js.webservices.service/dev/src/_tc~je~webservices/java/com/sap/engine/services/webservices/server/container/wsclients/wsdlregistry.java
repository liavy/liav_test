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
 
package com.sap.engine.services.webservices.server.container.wsclients;

import java.util.Hashtable;

/**
 * Title: WSDLRegistry 
 * Description: WSDLRegistry
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class WSDLRegistry {

  private Hashtable wsdlRegistry; 
   
  private Hashtable wsdlDescriptors;
   
  public WSDLRegistry() {    
  }
//   
//  /**
//   * @return - a hashtable of wsdl descriptors
//   */
//  public Hashtable getWsdlDescriptors() {
//    if(wsdlDescriptors == null) {
//      wsdlDescriptors = new Hashtable();
//    }
//    return wsdlDescriptors;
//  }
//    
//  public boolean containsWsdlDescriptorID(String id) {
//    return getWsdlDescriptors().containsKey(id);
//  }
//    
//  public boolean containsWSDLDescriptor(WsdlType wsdlDescriptor) {
//    return getWsdlDescriptors().contains(wsdlDescriptor);
//  } 
//    
//  public void putWsdlDescriptor(String id, WsdlType wsdlDescriptor) {
//    getWsdlDescriptors().put(id, wsdlDescriptor);
//  } 
//    
//  public WsdlType getWsdlDescriptor(String id) {
//    return (WsdlType)getWsdlDescriptors().get(id);
//  }
//    
//  public WsdlType removeWsdlDescriptor(String id) {
//    return (WsdlType)getWsdlDescriptors().remove(id);
//  }
//    
//  public String toString() {
//    String resultStr = ""; 
//    String nl = System.getProperty("line.separator");
//      
//    Hashtable wsdlDescriptors = getWsdlDescriptors(); 
//    if(wsdlDescriptors.size() == 0) {
//      return "EMPTY";
//    } 
//      
//    Enumeration enum1 = wsdlDescriptors.keys();
//    int i = 0;  
//    while(enum1.hasMoreElements()) {
//      String serviceName = (String)enum1.nextElement(); 
//      WsdlType wsdlDescriptor = (WsdlType)wsdlDescriptors.get(serviceName);       
//      resultStr += "Service[" + i++  + "]: " + serviceName + nl;
//      resultStr += "WSDL: " + wsdlDescriptor.get_value().trim() + nl ;  
//    }    
//      
//    return resultStr;          
//  }
  
}
