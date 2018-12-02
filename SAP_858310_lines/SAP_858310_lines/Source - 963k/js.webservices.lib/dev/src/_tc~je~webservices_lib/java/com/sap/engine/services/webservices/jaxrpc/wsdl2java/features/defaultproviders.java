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

import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.builtin.SessionProtocol;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.builtin.SoapHeadersProtocol;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.AuthenticationFeature;

import java.util.*;

/**
 * 
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class DefaultProviders {

  public static final Hashtable defaults = new Hashtable();
  public static final Properties defaultImplementations = new Properties();

  static {
    initDefault();
  }
  /**
   * Sets default
   */
  private static synchronized void initDefault() {
    defaults.put(SessionFeature.SESSION_FEATURE,SessionProtocol.NAME);
    defaults.put(HeadersFeature.NAME,SoapHeadersProtocol.NAME);
    defaults.put(AuthenticationFeature.AUTHENTICATION_FEATURE, "SecurityProtocol");
    defaults.put(MessageIdFeature.MESSAGEIDFEATURE,MessageIdFeature.DEFAULTNAME);
    defaultImplementations.put(SessionProtocol.NAME, SessionProtocol.class.getName());
    defaultImplementations.put(SoapHeadersProtocol.NAME, SoapHeadersProtocol.class.getName());
    defaultImplementations.put("SecurityProtocol","com.sap.security.core.client.ws.SecurityProtocol");
    defaultImplementations.put(MessageIdFeature.DEFAULTNAME,"com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.builtin.MessageIdProtocol");
  }

  public static synchronized String getProvider(String featureName) {
    return (String) defaults.get(featureName);
  }

  public static synchronized Properties getProtocolImplementations() {
    return defaultImplementations;
  }
}
