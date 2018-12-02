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
package com.sap.engine.services.webservices.espbase.client.bindings.exceptions;

import com.sap.localization.ResourceAccessor;

/**
 * Client Transport Binding resourse accessor.
 * @version 1.0
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class TBResourceAccessor extends com.sap.localization.ResourceAccessor {

  private static String BUNDLE_NAME = "com.sap.engine.services.webservices.espbase.client.bindings.exceptions.TransportBindingBundle";
  private static final ResourceAccessor resourceAccessor;
 
  public TBResourceAccessor() {
    super(BUNDLE_NAME);
  }

  public static final ResourceAccessor getResourceAccessor() {
    return resourceAccessor;
  }

  static  {
    resourceAccessor = new TBResourceAccessor();
  }
  

}
