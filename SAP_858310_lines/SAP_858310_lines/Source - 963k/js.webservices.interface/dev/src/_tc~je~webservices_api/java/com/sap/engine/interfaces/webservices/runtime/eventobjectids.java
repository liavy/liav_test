/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime;

/**
 *   Holds the Event IDs.
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */

public interface EventObjectIDs {

  //indicates stopping of application
  public static final String STOP_APPLICATION  =  "http://www.sap.com/webas/630/soap/features/java/implevent/stop-application";
  //indicates invalidating of session
  public static final String INVALIDATE_SESSION  =  "http://www.sap.com/webas/630/soap/features/java/implevent/invalidate-session";
  //indicates external exception
  public static final String UBNORMAL_RUNTIME_PROCESSING  =  "http://www.sap.com/webas/630/soap/features/java/implevent/ubnormal-processing";
}
