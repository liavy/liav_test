package com.sap.engine.frame.core.thread;

import com.sap.localization.ResourceAccessor;

/**
 * The ResourceAccessor for the software component "kernel".
 */
public class ThreadResourceAccessor extends ResourceAccessor
{
  private static final String BUNDLE_NAME = "com.sap.engine.frame.core.thread.ThreadResourceBundle";
  private static final ThreadResourceAccessor instance = new ThreadResourceAccessor();

  private ThreadResourceAccessor() {
    super(BUNDLE_NAME);
  }

  public static ThreadResourceAccessor getInstance() {
    return instance;
  }
}
