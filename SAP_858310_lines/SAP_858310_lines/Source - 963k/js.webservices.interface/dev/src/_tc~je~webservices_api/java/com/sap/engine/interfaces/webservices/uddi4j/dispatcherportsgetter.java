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
package com.sap.engine.interfaces.webservices.uddi4j;

/**
 * Copyright (c) 2003, SAP-AG
 * @author Alexander Zubev (alexander.zubev@sap.com)
 * @version 1.1, 2004-3-31
 */
public interface DispatcherPortsGetter {
  /**
   * @return an array of objects: Object[0] is java.net.InetAddres object of the dispatcher node. Object[1] is java.lang.Integer value for the HTTP port. If no HTTP port is available <null> is returned.
   */
  public Object[] getHTTPPort();

  /**
   * @return an array of objects: Object[0] is java.net.InetAddres object of the dispatcher node. Object[1] is java.lang.Integer value for the HTTPS port. If no HTTPS port is available <null> is returned.
   */
  public Object[] getHTTPSPort();
}
