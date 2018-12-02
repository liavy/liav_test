package com.sap.engine.services.webservices.exceptions.lib.accessors;

import com.sap.localization.ResourceAccessor;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class LibraryResourceAccessor extends ResourceAccessor {

  private static String BUNDLE_NAME = "com.sap.engine.services.webservices.exceptions.lib.accessors.WebServicesBundle";
  private static LibraryResourceAccessor serverResourceAccessor = null;


  public LibraryResourceAccessor() {
    super(BUNDLE_NAME);
  }

  public static synchronized LibraryResourceAccessor getResourceAccessor() {
    if (serverResourceAccessor == null)
      serverResourceAccessor = new LibraryResourceAccessor();
    return serverResourceAccessor;
  }

}