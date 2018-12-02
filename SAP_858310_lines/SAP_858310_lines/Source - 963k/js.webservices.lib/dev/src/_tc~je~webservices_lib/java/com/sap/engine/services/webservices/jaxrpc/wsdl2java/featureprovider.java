/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java;

import com.sap.engine.interfaces.webservices.client.ClientFeatureProvider;

/**
 * All modules that provides features (TBindings, Protocols) must implement this interface.
 * 
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */

public interface FeatureProvider extends ClientFeatureProvider {
  
  /**
   * Return true if this Protocol implements this feature.
   * When protocol writers implement some feature they must implement this method in order to runtime know they are
   * the suppoters of this feature.
   */ 
  public boolean isFeatureImplemented(String featureName, PropertyContext property);
  
  /**
   * Returns provider name. It's used as provider name in logical port. 
   * Provider name must be unique among other providers. 
   * It serves as provider id in container.
   */   
  public String getName();
  
  /**
   * Returns features that this protocol implements and supports.
   */   
  public String[] getFeatures();
  
}
