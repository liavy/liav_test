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

import java.rmi.RemoteException;

/**
 * @author Alexander Zubev (alexander.zubev@sap.com)
 */
public interface HTTPProxyResolver {
  /**
   * Getting HTTP Proxy for the specified host if needed.
   * @param host Host name or IP Address
   * @return HTTPProxy or <code>null</code> if http proxy is not needed. 
   */
  public HTTPProxy getHTTPProxyForHost(String host) throws RemoteException;
}
