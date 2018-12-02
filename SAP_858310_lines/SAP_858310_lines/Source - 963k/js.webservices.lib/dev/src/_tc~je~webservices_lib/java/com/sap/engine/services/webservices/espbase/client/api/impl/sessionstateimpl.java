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
package com.sap.engine.services.webservices.espbase.client.api.impl;

import java.io.Serializable;
import java.util.HashMap;

import com.sap.engine.services.webservices.espbase.client.api.SessionState;

public class SessionStateImpl implements SessionState,Serializable {

  private static final long serialVersionUID = -2360182579779444325L;
  public HashMap<String,String> persistableContext;
  public HashMap<String,Object> dynamicContext;
  
  public SessionStateImpl() {
    this.dynamicContext = new HashMap<String,Object>();
    this.persistableContext = new HashMap<String,String>();
  }
  
}
