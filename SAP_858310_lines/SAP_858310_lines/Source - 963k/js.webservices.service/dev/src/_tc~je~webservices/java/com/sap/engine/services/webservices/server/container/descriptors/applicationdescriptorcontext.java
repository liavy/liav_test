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

/**
 * Title: ApplicationDescriptorContext
 * Description: ApplicationDescriptorContext
 * 
 * @author Dimitrina Stoyanova
 * @version
 */
public class ApplicationDescriptorContext {

  private ConfigurationDescriptorRegistry configurationDescriptorRegistry;  
  private MappingDescriptorRegistry mappingDescriptorRegistry; 
  
  public ApplicationDescriptorContext() {
  
  }  
  
  /**
   * @return ConfigurationDescriptorRegistry 
   */
  public ConfigurationDescriptorRegistry getConfigurationDescriptorRegistry() {
    if(configurationDescriptorRegistry == null) {
      configurationDescriptorRegistry = new ConfigurationDescriptorRegistry(); 
    }
    return configurationDescriptorRegistry;
  }

  /**
   * @return MappingDescriptorRegistry
   */
  
  public MappingDescriptorRegistry getMappingDescriptorRegistry() {
    if(mappingDescriptorRegistry == null) {
      mappingDescriptorRegistry = new MappingDescriptorRegistry(); 
    }
    return mappingDescriptorRegistry;
  }
  
}
