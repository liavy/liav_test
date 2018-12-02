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
package com.sap.engine.services.webservices.jaxrpc.wsdl2java.features;

/**
 * 
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public interface ProxyFeature {

  public static final String PROXY_FEATURE = "http://www.sap.com/webas/630/soap/features/proxy";
  public static final String PROXY_HOST_PROPERTY = "proxyHost";
  public static final String PROXY_PORT_PROPERTY = "proxyPort";
  public static final String PROXY_USERNAME = "proxyUser";
  public static final String PROXY_PASSWORD = "proxyPassword";
    
}
