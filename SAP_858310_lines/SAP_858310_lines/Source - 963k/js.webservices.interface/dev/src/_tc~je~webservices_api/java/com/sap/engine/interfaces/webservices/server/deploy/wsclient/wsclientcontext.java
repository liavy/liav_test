package com.sap.engine.interfaces.webservices.server.deploy.wsclient;

/**
 * Title: WSClientContext.
 * Description: This context provides access to the base ws client and logical ports configuration settings.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface WSClientContext extends WSClientBaseContext {

  /**
   * Method for access to the logical ports property contexts.
   *
   * @return LPPropertyContext[]  - the logical ports property contexts.
   */

   public LPPropertyContext[] getLPPropertyContexts();

}
