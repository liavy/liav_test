/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.interfaces.webservices.dynamic;

/**
 * This class can be used in order to get a Web Service implementation
 *
 * @author Alexander Zubev
 */
public interface DynamicServiceFactory {
  /**
   * A relative name to the web service context under which the DynamicServiceFactory is bound in the naming
   */
  public static final String NAME = "DynamicServiceFactory";

  /**
   * A method for getting WS Implementation
   * @param wsInterface The interface class for a generated WebService Interface
   * @param wsclientsDescriptorName The name of the ws-clients-deployment-descriptor.
   * The descriptor should be located in /META-INF/ws-clients-descriptors/
   * @return The Web Service Implementation
   * @throws Exception if the web service implementation cannot be created
   */
  public Object getWebService(Class wsInterface, String wsclientsDescriptorName) throws Exception;

  /**
   * A method for getting WS Implementation
   * @param wsInterface The interface class for a generated WebService Interface
   * @param wsclientsDescriptorName The name of the ws-clients-deployment-descriptor.
   * The descriptor should be located in /META-INF/ws-clients-descriptors/
   * @param componentName The name of the component that invokes this method.
   * @return The Web Service Implementation
   * @throws Exception if the web service implementation cannot be created
   */
  public Object getWebService(Class wsInterface, String wsclientsDescriptorName, String componentName) throws Exception;
}
