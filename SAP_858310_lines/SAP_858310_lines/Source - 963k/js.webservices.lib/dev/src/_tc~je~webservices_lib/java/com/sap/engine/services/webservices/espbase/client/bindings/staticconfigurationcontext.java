/*
 * Copyright (c) 2003 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.client.bindings;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceData;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;

/**
 * Configuration context that containes external persistend information that is not to
 * be changed by the framework or protocols.
 * @version 1.0
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public interface StaticConfigurationContext extends ConfigurationContext {
  
  /**
   * Returns endpoint designtime configuration.
   * @return
   */
  public InterfaceData getDTConfig();
  
  /**
   * Returns endpoint runtime configuration. 
   * @return
   */
  public BindingData getRTConfig();
  
  /**
   * Returns endpoint interface mapping data.
   * @return
   */
  public InterfaceMapping getInterfaceData();  
  
}
