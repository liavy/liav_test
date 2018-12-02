/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.configuration.marshallers;

import com.sap.engine.services.webservices.espbase.configuration.ConfigurationMarshallerFactory;
import com.sap.engine.services.webservices.espbase.configuration.marshallers.wsa.AddressingConfigurationMarshaller;
import com.sap.engine.services.webservices.espbase.configuration.marshallers.wsrm.RMConfigurationMarshaller;

/**
 * Registry for the marshallers defined in the subpackages of this package.
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, May 17, 2006
 */
public class MarshallerRegistry {
  public static ConfigurationMarshallerFactory getInitializedFactory() {
    ConfigurationMarshallerFactory cfg = ConfigurationMarshallerFactory.newInstance();
    //register Addressing marshaller
    cfg.registerMarshaller(new AddressingConfigurationMarshaller());
    //register RM marshaller
    cfg.registerMarshaller(new RMConfigurationMarshaller());
    return cfg;
  }
}
