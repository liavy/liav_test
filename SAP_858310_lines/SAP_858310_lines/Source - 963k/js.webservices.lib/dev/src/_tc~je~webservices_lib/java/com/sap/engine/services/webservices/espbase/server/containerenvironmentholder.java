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

/**
 * @author Pavel Boev
 *
 */
public class ContainerEnvironmentHolder {
  private static ContainerEnvironment containerEnvironment;

  /**
   * @return the containerEnvironment
   */
  public static ContainerEnvironment getContainerEnvironment() {
    return containerEnvironment;
  }

  /**
   * @param containerEnvironment the containerEnvironment to set
   */
  public static void setContainerEnvironment(ContainerEnvironment containerEnvironment) {
    ContainerEnvironmentHolder.containerEnvironment = containerEnvironment;
  }
  
}
