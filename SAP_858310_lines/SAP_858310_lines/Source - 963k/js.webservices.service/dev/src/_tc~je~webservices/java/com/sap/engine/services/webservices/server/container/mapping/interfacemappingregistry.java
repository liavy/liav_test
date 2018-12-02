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
 
package com.sap.engine.services.webservices.server.container.mapping;

import java.util.Enumeration;
import java.util.Hashtable;

import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;

/**
 * Title: InterfaceMappingRegistry  
 * Description: InterfaceMappingRegistry is a registry for interface mappings
 *
 * @author Dimitrina Stoyanova
 * @version
 */
public class InterfaceMappingRegistry {
  
  private Hashtable<String, InterfaceMapping> interfaceMappings; 
  
  public InterfaceMappingRegistry() {  
    this.interfaceMappings = new Hashtable<String, InterfaceMapping>();
  }
    
  /**
   * @return - a hashtable of interface mappings
   */
  public Hashtable getInterfaceMappings() {
    if(interfaceMappings == null) {
      interfaceMappings = new Hashtable();
    }
    return interfaceMappings;
  }
     
  public boolean containsInterfaceMappingID(String id) {
    return getInterfaceMappings().containsKey(id);
  }
  
  public boolean containsInterfaceMapping(InterfaceMapping interfaceMapping) {
    return getInterfaceMappings().contains(interfaceMapping);  
  }
  
  public void putInterfaceMapping(String id, InterfaceMapping interfaceMapping) {
    getInterfaceMappings().put(id, interfaceMapping);   
  }
  
  public InterfaceMapping getInterfaceMapping(String id) {
    return (InterfaceMapping)getInterfaceMappings().get(id);
  }
  
  public InterfaceMapping removeInterfaceMapping(String id) {   
    return (InterfaceMapping)getInterfaceMappings().remove(id);
  }
  
  public String toString() {
    String resultStr = ""; 
    String nl = System.getProperty("line.separator");    

    Hashtable interfaceMappings = getInterfaceMappings();

    if(interfaceMappings.size() == 0) {
      return "EMPTY";
    }

    Enumeration enum1 = interfaceMappings.keys();
    int i = 0;     
    while(enum1.hasMoreElements()) {           
      resultStr += "Interface[" + i++ + "]: " +enum1.nextElement() + nl;      
    }            

    return resultStr;
  }

}
