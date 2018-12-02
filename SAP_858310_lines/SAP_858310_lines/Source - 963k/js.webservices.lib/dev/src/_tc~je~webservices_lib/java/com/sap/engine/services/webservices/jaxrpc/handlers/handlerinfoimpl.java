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
package com.sap.engine.services.webservices.jaxrpc.handlers;

import javax.xml.rpc.handler.HandlerInfo;

/**
 * Extends <code>javax.xml.rpc.handler.HandlerInfo</code> with some additional data.
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-3-12
 */
public class HandlerInfoImpl extends HandlerInfo {
  private String handlerName; //handler name
  private String handlerClassName; //handler class name
  private String[] roles; //handler roles
  
  public HandlerInfoImpl(String hName, String hClassName, String[] hRoles) {
    this.handlerName = hName;
    this.handlerClassName = hClassName;
    this.roles = hRoles;
  }
  
  public String getHandlerName() {
    return handlerName;
  }

  public String[] getRoles() {
    return roles;
  }

  public void setHandlerName(String string) {
    handlerName = string;
  }

  public void setRoles(String[] strings) {
    roles = strings;
  }
  
  public String getHandlerClassName() {
    return this.handlerClassName;
  }
}
