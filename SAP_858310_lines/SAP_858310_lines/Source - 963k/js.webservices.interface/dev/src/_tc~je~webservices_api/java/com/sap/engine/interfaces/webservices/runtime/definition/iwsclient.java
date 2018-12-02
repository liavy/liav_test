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

/**
 * Copyright (c) 2003, SAP-AG
 * @author Alexander Zubev (alexander.zubev@sap.com)
 * @version 1.1, 2004-2-5
 */
public interface IWSClient extends IWSComponent {
  public Object[] getLogicalPorts() throws Exception;
  public WSClientIdentifier getWsClientId();
  public String getServiceInterfaceName();
  public Object getWSDLDefinitions() throws Exception;
  public String getLogicalPortsFullFileName();
  public Object getServiceImpl() throws Exception;
}
