/*
 * Copyright (c) 2002 by SAP Labs Bulgaria AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.ts.exceptions;

import com.sap.localization.ResourceAccessor;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 *
 *
 * @author Krasimir Semerdzhiev (krasimir.semerdzhiev@sap.com)
 * @version 6.30
 */
public class TSResourceAccessor extends ResourceAccessor {

  static final long serialVersionUID = 1586885401919813242L;
  private static final String BUNDLE_NAME = "com.sap.engine.services.ts.exceptions.TSResourceBundle";
  private static final String LOCATION_PATH = "com.sap.engine.services.ts";
  private static final TSResourceAccessor RESOURCE_ACCESSOR = new TSResourceAccessor();
  public static final Category category = Category.SYS_SERVER;
  public static final Location location = Location.getLocation(LOCATION_PATH);

  private TSResourceAccessor() {
    super(BUNDLE_NAME);
  }

  public static TSResourceAccessor getResourceAccessor() {
    return RESOURCE_ACCESSOR;
  }

}
