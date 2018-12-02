package com.sap.engine.services.webservices.additions.soaphttp.exceptions;

import com.sap.localization.ResourceAccessor;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */
public class TBindingResourceAccessor extends ResourceAccessor {

  private static final String BUNDLE_NAME = "com.sap.engine.services.webservices.additions.soaphttp.exceptions.TBindingBundle";
  private static TBindingResourceAccessor tBindingResourceAccessor;

  static {
    tBindingResourceAccessor = new TBindingResourceAccessor();
  }

  public TBindingResourceAccessor() {
    super(BUNDLE_NAME);
  }

  public static TBindingResourceAccessor getResourceAccessor() {
    return tBindingResourceAccessor;
  }

}
