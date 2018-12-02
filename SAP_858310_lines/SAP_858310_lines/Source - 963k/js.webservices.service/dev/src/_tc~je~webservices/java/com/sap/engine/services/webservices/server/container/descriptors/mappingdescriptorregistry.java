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

package com.sap.engine.services.webservices.server.container.descriptors;

import java.util.Enumeration;
import java.util.Hashtable;

import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.exceptions.PatternKeys;
import com.sap.engine.services.webservices.exceptions.RegistryException;

/**
 * Title: MappingDescriptorRegistry 
 * Description: MappingDescriptorRegistry 
 * 
 * @author Dimitrina Stoyanova
 * @version
 */
public class MappingDescriptorRegistry {

  private Hashtable<String, MappingRules> mappingDescriptors;
  
  public MappingDescriptorRegistry() {   
    this.mappingDescriptors = new Hashtable<String, MappingRules>(); 
  }  
  
  /**
   * @return - a hashtable of mapping descriptors
   */
  public Hashtable getMappingDescriptors() {
    if(mappingDescriptors == null) {
      mappingDescriptors = new Hashtable();
    }
    return mappingDescriptors;
  }
  
  public boolean containsMappingDescriptorID(String id) {
    return getMappingDescriptors().containsKey(id);
  }
  
  public boolean containsMappingDescriptor(MappingRules mappingDescriptor) {
    return getMappingDescriptors().contains(mappingDescriptor);
  }
  
  public void putMappingDescriptor(String id, MappingRules mappingDescriptor) throws Exception {
    if(containsMappingDescriptorID(id)) {
      //throw new RegistryException(PatternKeys.WS_DUBLICATE_ELEMENT, new Object[]{id});
    }
    getMappingDescriptors().put(id, mappingDescriptor);
  }
  
  public MappingRules getMappingDescriptor(String id) {
    return (MappingRules)getMappingDescriptors().get(id);
  }
  
  public MappingRules removeMappingDescriptor(String id) {
    return (MappingRules)getMappingDescriptors().remove(id);
  }
  
  public String toString() {
    String resultStr = ""; 
    String nl = System.getProperty("line.separator");    
    
    Hashtable services = getMappingDescriptors();
    
    if(services.size() == 0) {
      return "EMPTY";
    }
    
    Enumeration enum1 = services.keys();
    int i = 0; 
    while(enum1.hasMoreElements()) {
      resultStr += "Mapping Descriptor ID[" + i++ + "]: " +enum1.nextElement() + nl;   
    }            
    
    return resultStr;
  }

}
