/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.wsdl.exceptions;

import com.sap.localization.ResourceAccessor;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-21
 */
public class WSDLResourceAccessor extends ResourceAccessor {
  private static String BUNDLE_NAME = "com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLResourceBundle";
  private static final WSDLResourceAccessor resourceAccessor;
 
	public WSDLResourceAccessor() {
		super(BUNDLE_NAME);
	}

  public static final WSDLResourceAccessor getResourceAccessor() {
    return resourceAccessor;
  }

  static  {
    resourceAccessor = new WSDLResourceAccessor();
  }
}
