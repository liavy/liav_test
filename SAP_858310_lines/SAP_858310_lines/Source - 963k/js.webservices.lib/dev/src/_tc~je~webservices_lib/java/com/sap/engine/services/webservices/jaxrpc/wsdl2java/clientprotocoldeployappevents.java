package com.sap.engine.services.webservices.jaxrpc.wsdl2java;

import com.sap.engine.services.webservices.jaxrpc.wsdl2java.PropertyContext;


/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2003
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */
public interface ClientProtocolDeployAppEvents {

   /**
   * Use this constant to get runtime information in context.
   */
  public static final String RUNTIMEINFO = "RuntimeInfo";

  /**
   * This method is called on application deploy for every protocol usage. Use context.getProperty(RUNTIMEINFO)
   * to get runtime information and context.getSubContext(String FeatureName) to get feature config.
   * @param context
   */
  public void onDeployApplication(PropertyContext context);

  /**
   * This method is called on application remove for every protocol usage.
   * @param appName
   */
  public void onRemoveApplication(String appName);

}
