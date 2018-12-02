/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sap.engine.services.webservices.espbase.configuration.marshallers.DefaultConfigurationMarshaller;

/**
 * Factory for configuration marshallers instances.
 * 
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-4-12
 */
public class ConfigurationMarshallerFactory {
  
  private List marshallers = new ArrayList();
  private Set marshallerClasses = new HashSet();
  
  private ConfigurationMarshallerFactory() {
    
  }
  
  /**
   * Returns unmodified list of IConfiguraionMarshaller objects.
   */
  public List getMarshallers() {
    return Collections.unmodifiableList(marshallers);
  }
  /**
   * Adds <code>marshaller</code> to the internal list of marshallers. As a key the
   * marshaller class name is used, based on the assumption that one marshaller
   * handles exactly one domain. 
   */
  public void registerMarshaller(IConfigurationMarshaller marshaller) {
    if (marshaller == null) {
      throw new IllegalArgumentException("Cannot register null marshaller");
    }
    String clsName = marshaller.getClass().getName();
    if (! marshallerClasses.contains(clsName)) {
      this.marshallers.add(marshaller);
      this.marshallerClasses.add(clsName);
    }
  }
  /**
   * Returns configuration marshaller factory, containing default configuration marshaller.
   */
  public static ConfigurationMarshallerFactory newInstance() {    
    ConfigurationMarshallerFactory f = new ConfigurationMarshallerFactory();
    f.marshallers.add(new DefaultConfigurationMarshaller()); 
    return f;
  }  
}
