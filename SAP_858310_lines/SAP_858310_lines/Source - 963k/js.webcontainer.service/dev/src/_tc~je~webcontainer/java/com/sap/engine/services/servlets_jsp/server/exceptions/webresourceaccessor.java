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
package com.sap.engine.services.servlets_jsp.server.exceptions;

import com.sap.localization.ResourceAccessor;
import com.sap.tc.logging.Location;

/*
 *
 *
 * @author Violeta Uzunova
 * @version 6.30
 */
public class WebResourceAccessor extends ResourceAccessor {

  private static String BUNDLE_NAME = "com.sap.engine.services.servlets_jsp.server.exceptions.servlets_jspServiceBundle";
  private static WebResourceAccessor resourceAccessor = null;
  public static Location location = null;

  public WebResourceAccessor() {
    super(BUNDLE_NAME);
  }

  public void init(Location l) {
    location = l;
  }

  public static synchronized WebResourceAccessor getResourceAccessor() {
    if(resourceAccessor == null) {
      resourceAccessor = new WebResourceAccessor();
    }
    return resourceAccessor;
  }


}
