package com.sap.engine.interfaces.webservices.server.accessors;

import com.sap.localization.ResourceAccessor;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSInterfaceResourceAccessor extends ResourceAccessor {

  private static String BUNDLE_NAME = "com.sap.engine.interfaces.webservices.server.accessors.WSInterfaceBundle";
  private static WSInterfaceResourceAccessor wsResourceAccessor = null;

  public WSInterfaceResourceAccessor() {
    super(BUNDLE_NAME);
  }

  public static synchronized WSInterfaceResourceAccessor getResourceAccessor() {
    if (wsResourceAccessor == null) {
      wsResourceAccessor = new WSInterfaceResourceAccessor();
    }
    return wsResourceAccessor;
  }

}
