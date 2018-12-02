package com.sap.engine.services.webservices.exceptions.accessors;

import com.sap.localization.ResourceAccessor;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class ServerResourceAccessor extends ResourceAccessor {

  private static String BUNDLE_NAME = "com.sap.engine.services.webservices.exceptions.accessors.WebServicesBundle";
  private static ServerResourceAccessor serverResourceAccessor = null;

  public ServerResourceAccessor() {
    super(BUNDLE_NAME);
  }

  public static synchronized ServerResourceAccessor getResourceAccessor() {
    if (serverResourceAccessor == null) {
      serverResourceAccessor = new ServerResourceAccessor();
    }
    return serverResourceAccessor;
  }

}