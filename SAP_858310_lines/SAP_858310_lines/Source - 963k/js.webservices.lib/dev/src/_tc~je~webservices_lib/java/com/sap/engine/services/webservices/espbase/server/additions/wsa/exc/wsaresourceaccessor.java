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
package com.sap.engine.services.webservices.espbase.server.additions.wsa.exc;

import com.sap.localization.ResourceAccessor;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-12-7
 */
public class WSAResourceAccessor extends ResourceAccessor {

  private static final String BUNDLE_NAME = "com.sap.engine.services.webservices.espbase.server.additions.wsa.exc.WSAResourceBundle";
  private static WSAResourceAccessor resourceAccessor;

  static {
    resourceAccessor = new WSAResourceAccessor();
  }

  public WSAResourceAccessor() {
    super(BUNDLE_NAME);
  }

  public static WSAResourceAccessor getResourceAccessor() {
    return resourceAccessor;
  }
  
}
