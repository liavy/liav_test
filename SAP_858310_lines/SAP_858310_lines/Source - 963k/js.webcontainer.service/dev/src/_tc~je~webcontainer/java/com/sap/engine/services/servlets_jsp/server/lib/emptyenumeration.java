/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.lib;

import java.util.Enumeration;

public class EmptyEnumeration implements Enumeration {
  public boolean hasMoreElements() {
    return false;
  }

  public Object nextElement() {
    return null;
  }
}
