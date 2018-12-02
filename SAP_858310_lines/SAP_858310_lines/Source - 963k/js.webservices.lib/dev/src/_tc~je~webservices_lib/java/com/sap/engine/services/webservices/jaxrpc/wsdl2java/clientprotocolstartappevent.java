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

/**
 * All protocols that implement this interface will be called on application start.
 * In the property context Protocol Configurations are binded and runtime information.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public interface ClientProtocolStartAppEvent {

  /**
   * Use this constant to get runtime information in context.
   */
  public static final String RUNTIMEINFO = "RuntimeInfo";

  /**
   * This method is called on application start for every protocol usage. Use context.getProperty(RUNTIMEINFO)
   * to get runtime information and context.getSubContext(String FeatureName) to get feature config.
   * @param context
   */
  public void onStartApplication(PropertyContext context);

}
