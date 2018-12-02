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
package com.sap.engine.services.webservices.espbase.configuration;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.xi.XIFrameworkConstants;

/**
 * This interface contains predefined constants, relevant for configuration of both
 * consumer and provider sides.
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov 
 * @version 1.0, 2005-3-24
 */
public interface BuiltInConfigurationConstants {
  /**
   * Constant denoting default namespace for the built-in properties.
   */
  public static final String DEFAULT_PROPERTIES_NS  =  "http://www.sap.com/webas/java/webservices/framework/";
  /**
   * Constant denoting 'protocol-order' property name.
   */
  public static final String PROTOCOL_ORDER_PROPERTY  =  "protocol-order";
  
  public static final QName PROTOCOL_ORDER_PROPERTY_QNAME = new QName(DEFAULT_PROPERTIES_NS,PROTOCOL_ORDER_PROPERTY);
  /**
   * Constant denoting 'jax-rpc-handlers-config' property name.
   */
  public static final String JAXRPC_HANDLERS_CONFIG_PROPERTY  =  "jax-rpc-handlers-config";
  /**
   * Constant denoting 'jax-ws-handlers-config' property name.
   */
  public static final String JAXWS_HANDLERS_CONFIG_PROPERTY  =  "jax-ws-handlers-config";
  /**
   * Default port flag - if this flag is set then the port is considered default.
   */
  public static final String DEFAULT_LP_FLAG = "default-lp";
  /**
   * QName denoting 'SOAPApplication' property
   */
  public static final QName SOAPAPPLICATION_PROPERTY_QNAME = new QName("http://www.sap.com/webas/630/soap/features/runtime/interface/", "SOAPApplication");
  /**
   * 'SOAPApplication' property value for standard proxies. 
   */
  public static final String SOAPAPPLICATION_CLIENT_DEFAULT_VALUE = "URN:SAP-COM:SOAP:RUNTIME:APPLICATION:CLIENT";
  /**
   * 'SOAPApplication' property value for proxies using WSRM. 
   */
  public static final String SOAPAPPLICATION_CLIENT_EXTENDED_VALUE = "URN:SAP-COM:SOAP:RUNTIME:APPLICATION:CLIENT:EXTENDED";
  /**
   * 'SOAPApplication' property value for JAXWS client proxies. 
   */
  public static final String SOAPAPPLICATION_CLIENT_JAXWS_VALUE = "URN:SAP-COM:SOAP:RUNTIME:APPLICATION:CLIENT:JAXWS";
  /**
   * 'SOAPApplication' property value for standard web services 
   */
  public static final String SOAPAPPLICATION_SERVICE_DEFAULT_VALUE = "URN:SAP-COM:SOAP:RUNTIME:APPLICATION:SERVICE";
  /**
   * 'SOAPApplication' property value for web services with WS-RM 
   */
  public static final String SOAPAPPLICATION_SERVICE_EXTENDED_VALUE = "URN:SAP-COM:SOAP:RUNTIME:APPLICATION:SERVICE:EXTENDED";
  /**
   * 'SOAPApplication' property value for JAXWS web services with configured handlers.
   */
  public static final String SOAPAPPLICATION_SERVICE_JAXWS_HANDLERS_VALUE = "URN:SAP-COM:SOAP:RUNTIME:APPLICATION:SERVICE:JAXWS:HANDLERS";
  /**
   * 'SOAPApplication' property value for JAXWS web services without handlers.
   */
  public static final String SOAPAPPLICATION_SERVICE_JAXWS_DEFAULT_VALUE = "URN:SAP-COM:SOAP:RUNTIME:APPLICATION:SERVICE:JAXWS:DEFAULT";
  /**
   * 'SOAPApplication' property value for XI web services.
   */
  public static final String SOAPAPPLICATION_SERVICE_XI_VALUE = "URN:SAP-COM:SOAP:XMS:APPLICATION:XIP";
}
