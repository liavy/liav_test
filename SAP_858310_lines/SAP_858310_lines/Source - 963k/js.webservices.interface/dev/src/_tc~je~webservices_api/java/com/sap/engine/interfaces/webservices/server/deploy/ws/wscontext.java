package com.sap.engine.interfaces.webservices.server.deploy.ws;

/**
 * Title: WSContext
 * Description: This context provides access to the base web service configuration settings.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface WSContext {

  /**
   * Method for access to the application name that the web service belongs to.
   *
   * @return String - The application name.
   */

   public String getApplicationName();

   /**
    * Method for access to the web service name.
    *
    * @return String - The web service name.
    */

   public String getWebServiceName();

   /**
    * Method for access to the web service's endpoint contexts.
    *
    * @return SEIDeployContext[] - An array of service endpoint contexts.
    */

   public SEIContext[] getSEIContexts();

   /**
    * Method for access to the web service definition file name.
    *
    * @return String - The web service definition file name.
    */

  // public String getWSDName(); /*?*/

}
