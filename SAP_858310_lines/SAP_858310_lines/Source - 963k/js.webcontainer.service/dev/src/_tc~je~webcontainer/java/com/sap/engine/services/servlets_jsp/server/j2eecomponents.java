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
package com.sap.engine.services.servlets_jsp.server;

/**
 * @author Petar Petrov
 * @version 7.00
 */
public interface J2EEComponents {

  // Interfaces //

  public static final String INTERFACE_CONTAINER =           "container";
  public static final String INTERFACE_SHELL =               "shell";
  public static final String INTERFACE_RESOURCECONTEXT =     "resourcecontext_api";
  public static final String INTERFACE_WEBSERVICES =         "webservices";
  public static final String INTERFACE_WEBCONTAINER_API =    "tc~je~webcontainer~api";

  // Services //

  public static final String SERVICE_DEPLOY =                "deploy";
  public static final String SERVICE_HTTP =                  "http";
  public static final String SERVICE_MONITOR =               "monitor";
  public static final String SERVICE_SECURITY =              "security";
  public static final String SERVICE_SERVLET_JSP =           "servlet_jsp";
  public static final String SERVICE_TELNET =                "telnet";
  public static final String SERVICE_TIMEOUT =               "timeout";
  public static final String SERVICE_TS =                    "ts";
  public static final String SERVICE_JMX =                   "jmx";
  public static final String SERVICE_BASICADMIN = 			 "basicadmin";

  // Libraries //

  public static final String LIBRARY_MANAGEMENT_MODEL =      "tc/je/mmodel/lib";

}