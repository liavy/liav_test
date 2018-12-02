package com.sap.engine.services.servlets_jsp;

/**
 * This interface is used in order DSR service to register real implementation for the Web Container instrumentation
 * The instrumented point is: 
 * - entry point when the request goes into Web Container
 * 
 * @author simeon-s
 *
 */
public interface DSRWebContainer {
  
  /**
   * This method is invoked when request goes into Web Container
   * 
   * @param appName - requested application
   * @param appAlias - alias of the application of the current HTTP call
   * @param resource - servlet or JSP of the current HTTP call
   * @param sessionId - sessionId if any
   */
  void containerStart(String appName, String appAlias, String resource, String sessionId);
  
  /**
   * This method is invoked when the request gous out of Web container
   */ 
  void containerEnd();
  
  
}
