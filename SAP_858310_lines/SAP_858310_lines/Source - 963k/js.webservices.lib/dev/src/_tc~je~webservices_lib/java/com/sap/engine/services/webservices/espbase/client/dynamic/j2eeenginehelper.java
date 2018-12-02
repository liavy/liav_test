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
package com.sap.engine.services.webservices.espbase.client.dynamic;

import com.sap.engine.interfaces.webservices.runtime.component.ClientComponentFactory;
import com.sap.engine.services.webservices.espbase.discovery.ServiceDiscovery;

/**
 * Copyright (c) 2004, SAP-AG
 * @author Alexander Zubev
 * @version 1.1, 2005-10-4
 */
public interface J2EEEngineHelper { 
  public ServiceDiscovery getServiceDiscovery();
  public void setServiceDiscovery(ServiceDiscovery serviceDiscovery);
  public ServiceFactoryConfig getServiceFactoryConfig();
  public ClientComponentFactory getClientComponentFactory();
}
