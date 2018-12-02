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
package com.sap.engine.services.webservices.jaxrpc.handlers.exceptions;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-3-12
 */
public class ResourceAccessor extends com.sap.localization.ResourceAccessor {
  private static final String BUNDLE_NAME = "com.sap.engine.services.webservices.jaxrpc.handlers.exceptions.ResourceBundle";
  private static ResourceAccessor resourceAccessor;

  static {
    resourceAccessor = new ResourceAccessor();
  }

  public ResourceAccessor() {
    super(BUNDLE_NAME);
  }

  public static ResourceAccessor getResourceAccessor() {
    return resourceAccessor;
  }

}
