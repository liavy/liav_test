/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.interfaces.webservices.uddi4j;

import java.io.Serializable;

/**
 * This class repesents a UDDI Registry Operator.
 */
public class UDDIRegistry implements Serializable {
  /**
   * The name of the UDDI Registry Operator
   */
  private String name;

  /**
   * The inquiry URL of the UDDI Registry Operator
   */
  private String inquiryURL;

  /**
   * The publish URL of the UDDI Registry Operator
   */
  private String publishURL;
  
  /**
   * The URL for the security API of the UDDI Registry Operator
   */
  private String securityURL;
  
  /**
   * The URL for the subsricption API of the UDDI Registry Operator
   */
  private String subscriptionURL;
  
  /**
   * The URL for the monitoring API of the UDDI Registry Operator
   */
  private String monitoringURL;

  /**
   * Proxy hostname or <code>null</code> if no proxy is used.
   */
  private String proxyHost;

  /**
   * Proxy port number
   */
  private int proxyPort;

  /**
   * Constructs a new UDDI Registry Operator with the specifies parameters
   * @param name The name of the UDDI Registry Operator
   * @param inquiryURL The inquiry URL of the UDDI Registry Operator
   * @param publishURL The publish URL of the UDDI Registry Operator
   */
  public UDDIRegistry(String name, String inquiryURL, String publishURL) {
    this.name = name;
    this.inquiryURL = inquiryURL;
    this.publishURL = publishURL;
  }

  /**
   * Constructs an empty UDDI Registry Operator
   */
  public UDDIRegistry() {
  }
  
  /**
   * A method for getting the name of the operator
   * @return The name of the UDDI Registry Operator
   */
  public String getName() {
    return name;
  }

  /**
   * A method for getting the name of the operator
   * @return The name of the UDDI Registry Operator
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * A method for getting the inquiry URL of the operator
   * @return The inquiry URL of the UDDI Registry Operator
   */
  public String getInquiryURL() {
    return inquiryURL;
  }

  /**
   * A method for getting the inquiry URL of the operator
   * @param The inquiry URL of the UDDI Registry Operator
   */
  public void setInquiryURL(String inquiryURL) {
    this.inquiryURL = inquiryURL;
  }

  /**
   * A method for getting the publish URL of the operator
   * @return The publish URL of the UDDI Registry Operator
   */
  public String getPublishURL() {
    return publishURL;
  }

  /**
   * A method for getting the publish URL of the operator
   * @param The publish URL of the UDDI Registry Operator
   */
  public void setPublishURL(String publishURL) {
    this.publishURL = publishURL;
  }

  /**
   * A method for getting the proxy hostname
   * @return The proxyhost name or <code>null</code> if no proxy is used.
   * @deprecated Use an HTTPProxyResolver
   */
  public String getProxyHost() {
    return proxyHost;
  }

  /**
   * A method for setting the proxy host name
   * @param proxyHost The proxy host name
   */
  public void setProxyHost(String proxyHost) {
    this.proxyHost = proxyHost;
  }

  /**
   * A method for getting the proxy port.
   * @return The proxy port number
   * @deprecated Use an HTTPProxyResolver
   */
  public int getProxyPort() {
    return proxyPort;
  }

  /**
   * A method for setting the proxy port number
   * @param proxyPort The port number
   */
  public void setProxyPort(int proxyPort) {
    this.proxyPort = proxyPort;
  }

  public String toString() {
    if (name == null) {
      return "com.sap.engine.interfaces.webservices.uddi4j.UDDIRegistry: name is null";
    }
    return name;
  }

  /**
   * @return The URL to the security API
   */
  public String getSecurityURL() {
    return securityURL;
  }

  /**
   * @param securityURL - The URL to the security API
   */
  public void setSecurityURL(String securityURL) {
    this.securityURL = securityURL;
  }

  /**
   * @return The URL to the subscription API
   */
  public String getSubscriptionURL() {
    return subscriptionURL;
  }

  /**
   * @param subscriptionURL - The URL to the subscription API
   */
  public void setSubscriptionURL(String subscriptionURL) {
    this.subscriptionURL = subscriptionURL;
  }

/**
 * @return The URL to the monitoring API
 */
public String getMonitoringURL() {
	return monitoringURL;
}
/**
 * @param monitoringURL - The URL to the monitoring API
 */
public void setMonitoringURL(String monitoringURL) {
	this.monitoringURL = monitoringURL;
}
}
