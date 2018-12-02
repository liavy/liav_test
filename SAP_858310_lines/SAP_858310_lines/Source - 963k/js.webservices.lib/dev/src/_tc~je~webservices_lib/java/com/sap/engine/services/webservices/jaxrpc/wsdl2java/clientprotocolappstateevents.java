package com.sap.engine.services.webservices.jaxrpc.wsdl2java;

/**
 * Title: ClientProtocolAppStateEvents
 * Description: All protocols that implement this interface will be called on application start/stop.
 * In the property context protocol configurations are bound and also runtime information.
 * Copyright: Copyright (c) 2003
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface ClientProtocolAppStateEvents {

  /**
   * Use this constant to get runtime information in context.
   */
  public static final String RUNTIMEINFO = "RuntimeInfo";

  /**
   * This method is called on application start for every protocol usage. Use context.getProperty(RUNTIMEINFO)
   * to get runtime information and context.getSubContext(String FeatureName) to get feature config.
   * @param context
   */
  public void onStartApplication(PropertyContext context);

  /**
     * This method is called on application stop for every protocol usage. Use context.getProperty(RUNTIMEINFO)
     * to get runtime information and context.getSubContext(String FeatureName) to get feature config.
     * @param context
     */
  public void onStopApplication(PropertyContext context);

}

