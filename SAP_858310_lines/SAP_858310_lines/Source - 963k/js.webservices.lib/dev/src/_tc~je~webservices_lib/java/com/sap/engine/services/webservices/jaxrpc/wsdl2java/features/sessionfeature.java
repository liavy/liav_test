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
public interface SessionFeature {
                                                
  public static final String SESSION_FEATURE = "http://www.sap.com/webas/630/soap/features/session/";
  public static final String SESSION_METHOD_PROPERTY = "SessionMethod";
  public static final String HTTP_SESSION_METHOD = "httpCookies";
  public static final String HTTP_BYPASS_METHOD = "none";
  public static final String SESSION_COOKIE_PROPERTY = "SessionCoockie";
  public static final String HTTP_MAINTAIN_SESSION = "maintainSession"; // default true
  public static final String USE_SESSION_TRUE ="yes";
  public static final String USE_SESSION_FALSE ="no";
  public static final String ABAP_SESSION = "abapSession";
  public static final String HTTP_FLUSH_SESSION = "flushSession";
  public static final String FLUSH_SESSION_TRUE ="yes";
  public static final String FLUSH_SESSION_FALSE ="no";
}
