package com.sap.engine.services.servlets_jsp;

/**
 * This interface is deprecated.
 * It was added only to prevent NoClassDefFoundError during upgrade from 640 to NY.
 * It will be removed in the future.
 *
 * @author Violeta Georgieva
 * @version 7.1
 * @deprecated use WebContainerInterface interface
 */
public interface ServletAndJspRuntimeInterface {
  /**
   * @param listener
   * @param debugParamName
   * @deprecated use WebContainerInterface.registerHttpSessionDebugListener(HttpSessionDebugListener listener, String debugParamName)
   */
  public void registerHttpSessionDebugListener(HttpSessionDebugListener listener, String debugParamName);

  /**
   * @deprecated use WebContainerInterface.unregisterHttpSessionDebugListener()
   */
  public void unregisterHttpSessionDebugListener();
}
