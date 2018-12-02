package com.sap.engine.interfaces.webservices.server.deploy.wsclient;

/**
 * Title: WSClientContext.
 * Description: This context provides access to the base ws client configuration settings.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface WSClientBaseContext {

  /**
   * Method for access to the application name that the web service belongs to.
   *
   * @return String - The application name.
   */

   public String getApplicationName();

   /**
    * Method for access to the ws client name.
    *
    * @return String - The ws client name.
    */

   public String getWSClientName();

  /**
   * Method for access to the ws client root jndi name.
   *
   * @return String - The ws client root jndi name.
   */

   public String getRootJndiName();

}