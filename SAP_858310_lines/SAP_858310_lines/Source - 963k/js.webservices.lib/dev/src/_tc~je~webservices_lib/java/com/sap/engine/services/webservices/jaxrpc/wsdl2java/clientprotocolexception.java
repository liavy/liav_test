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
 * 
 *
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class ClientProtocolException extends Exception {
  
  public ClientProtocolException(String msg) {
    super(msg);
  }
  
  public ClientProtocolException() {
    super();
  }
  

  public ClientProtocolException(String msg, Throwable e) {
    super(msg, e);
  }

}
