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

/**
 * @author Alexander Zubev (alexander.zubev@sap.com)
 */
public class UDDIPublicationExt {
  private String inquiryURL;
  private String publishURL;
  private String serviceKey;
  
  /**
   * @return The Inquiry URL of the UDDI Registry, in which the WS has been published
   */
  public String getInquiryURL() {
    return inquiryURL;
  }

  /**
   * @return The Publish URL of the UDDI Registry, in which the WS has been published
   */
  public String getPublishURL() {
    return publishURL;
  }

  /**
   * @return The Service Key of the Business Service that represents the Web Service in the appropriate UDDI Registry
   */
  public String getServiceKey() {
    return serviceKey;
  }

  /**
   * @param inquiryURL The Inquiry URL of the UDDI Registry, in which the WS has been published
   */
  public void setInquiryURL(String inquiryURL) {
    this.inquiryURL = inquiryURL;
  }

  /**
   * @param publishURL The Publish URL of the UDDI Registry, in which the WS has been published
   */
  public void setPublishURL(String publishURL) {
    this.publishURL = publishURL;
  }

  /**
   * @param serviceKey The Service Key of the Business Service that represents the Web Service in the appropriate UDDI Registry
   */
  public void setServiceKey(String serviceKey) {
    this.serviceKey = serviceKey;
  }

}
