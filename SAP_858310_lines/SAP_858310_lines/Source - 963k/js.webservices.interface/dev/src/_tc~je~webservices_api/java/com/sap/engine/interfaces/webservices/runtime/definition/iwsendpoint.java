/*
 * Copyright (c) 2003 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.interfaces.webservices.runtime.definition;

import com.sap.engine.interfaces.webservices.runtime.Feature;
import com.sap.engine.interfaces.webservices.runtime.OperationDefinition;

import java.io.Serializable;

/**
 * Copyright (c) 2003, SAP-AG
 * @author Alexander Zubev (alexander.zubev@sap.com)
 * @version 1.1, 2004-2-26
 */
public interface IWSEndpoint extends Serializable {
  public String getEndpointName();
  
  public String getEndpointURI();
  
  public OperationDefinition[] getWSOperations();

  public Feature[] getRuntimeFeaturesChain();

  public String getDocumentWsdlURI(String host, int port);

  public String getRpcWsdlURI(String host, int port);
}
