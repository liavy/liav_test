/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.interfaces.webservices.runtime;

import java.io.OutputStream;


public interface ServletsHelper {
  public static final String NAME = "ServletsHelper";

//  //Both literal and encoded portTypes and schemas will be generated
//  public static final int LITERAL_ENCODED_MODE  =  0;
//  //Only literal portTypes and schemas will be generated
//  public static final int LITERAL_MODE  =  1;
//  //Only encoded portTypes and schemas will be generated
//  public static final int ENCODED_MODE  =  2;
//  //String parameters
//  public static final String LITERAL = "literal";
//  public static final String ENCODED = "enc";
//  public static final String LITERAL_ENCODED = "literal_enc";
//
  public void sendFile(String name, String type, byte[] data) throws Exception;
//
//  public boolean fileExists(String name, String type) throws Exception;
//
//  public UDDIRegistry[] getUDDIRegistries() throws Exception;
//
//  public void addUDDIRegistry(UDDIRegistry registry) throws Exception;
//
  public void generateWSDLFromWSD(String wsdID, OutputStream result) throws Exception;

  public void generateWSDLFromVI(String viID, String wsdName, String style, OutputStream result) throws Exception;
//
//  public String[] getAddressPointsForWS(String hostName, int port, String serviceName, String applicationName, String jarName) throws Exception;
//
//  public String getUDDIKeyForWS(String serviceName, String applicationName, String jarName, UDDIRegistry registry) throws Exception;
//
//  public String[] getWSDRefereneces(String serviceName, String applicationName, String jarName, UDDIRegistry registry) throws Exception;
//
//  public void setUDDIKeyToWS(String uddiKey, String serviceName, String applicationName, String jarName, UDDIRegistry registry) throws Exception;
//
//  public WebServiceExt[] getAllWebServices();
//  
//  public WebServiceExt getWebService(String serviceName, String appName, String jarName);
//
//  public String getProxyJars();
  
  public HTTPProxyResolver getHTTPProxyResolver(); 
}
