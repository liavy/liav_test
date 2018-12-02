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
public interface AuthenticationFeature {

  public static final String AUTHENTICATION_FEATURE = "http://www.sap.com/webas/630/soap/features/authentication";
  public static final String AUTHENTICATION_LEVEL = "AuthenticationLevel";
  public static final String AUTHENTICATION_METHOD = "AuthenticationMethod";
  public static final String AUTHENTICATION_MECHANISM = "AuthenticationMechanism";
  public static final String AUTHENTICATION_CREDENTIAL_USER = "AuthenticationCredentialUser";
  public static final String AUTHENTICATION_CREDENTIAL_PASSWORD = "AuthenticationCredentialPassword";
  public static final String AUTHENTICATION_CLIENT_KEYSTORE = "clientKeystore";
  public static final String AUTHENTICATION_CLIENT_CERT_LIST = "clientCertificateList";
  public static final String AUTHENTICATION_CLIENT_CERTNAME = "certName";
  public static final String AUTHENTICATION_SERVER_KEYSTORE = "serverKeystore";
  public static final String AUTHENTICATION_SERVER_CERT_LIST = "serverCertificateList";
  public static final String AUTHENTICATION_SERVER_IGNORE_CERTS ="ignoreSSLServerCertificates";
  public static final String NONE = "None";
  public static final String LEVEL_NONE = "None";
  public static final String LEVEL_BASIC = "Basic";
  public static final String LEVEL_STRONG = "Strong";
  public static final String METHOD_BASIC = "BasicAuth";
  public static final String METHOD_CERTIFICATE = "CertAuth";
  public static final String METHOD_SSO2 = "SSO2Auth";
  public static final String DYNAMIC_CLIENT = "DynamicClient";
  public static final String LOGICAL_TARGET = "LogicalTarget";
}

