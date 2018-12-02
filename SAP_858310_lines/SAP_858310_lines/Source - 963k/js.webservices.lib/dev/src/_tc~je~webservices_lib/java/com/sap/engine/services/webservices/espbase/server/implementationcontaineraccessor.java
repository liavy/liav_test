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
package com.sap.engine.services.webservices.espbase.server;

import com.sap.engine.interfaces.webservices.esp.ImplementationContainer;

/**
 * This interface is supposed to be implemented by the implementation container
 * registry into which the different implementation container instances are registered.
 * Via this interface the runtime gains access to a concrete implemenetation container instance.
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-10-5
 */
public interface ImplementationContainerAccessor {
  /**
   * @param containerId implementation container identifier
   * @return the implementation container instance registered under <code>containerID</code> or null 
   *         if nothing is registered for this <code>containerID</code>.
   */
  public ImplementationContainer getImplementationContainer(String containerId);
}
