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

import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;

/**
 * Title: OperationMappingRegistry 
 * Description: OperationMappingRegistry is a registry for operation mappings
 * 
 * @author Dimitrina Stoyanova
 * @version
 */
public class OperationMappingRegistry {
  
  private Hashtable<String, OperationMapping> operationMappings; 
  
  public OperationMappingRegistry() {
    this.operationMappings = new Hashtable<String, OperationMapping>();
  }    

  /**
   * @return - a hashtable of operation mappings
   */
  public Hashtable getOperationMappings() {
    if(operationMappings == null) {   
      operationMappings = new Hashtable();
    }
    return operationMappings;
  }
  
  public boolean containsOperationMappingID(String id) {
    return getOperationMappings().containsKey(id);    
  }
  
  public boolean containsOperationMapping(OperationMapping operationMapping) {
    return getOperationMappings().contains(operationMapping);
  }
  
  public void putOperationMapping(String id, OperationMapping operationMapping) {
    getOperationMappings().put(id, operationMapping); 
  }
  
  public OperationMapping getOperationMapping(String id) {    
    return (OperationMapping)getOperationMappings().get(id);
  }

  public OperationMapping removeOperationMapping(String id) {    
    return (OperationMapping)getOperationMappings().remove(id);
  }
  
  public String toString() {
    String resultStr = ""; 
    String nl = System.getProperty("line.separator");    

    Hashtable operationMappings = getOperationMappings();

    if(operationMappings.size() == 0) {
      return "EMPTY";
    }

    Enumeration enum1 = operationMappings.keys();
    int i = 0;     
    while(enum1.hasMoreElements()) {           
      resultStr += "Operation[" + i++ + "]: " +enum1.nextElement() + nl;      
    }            

    return resultStr;
  }
   
}
