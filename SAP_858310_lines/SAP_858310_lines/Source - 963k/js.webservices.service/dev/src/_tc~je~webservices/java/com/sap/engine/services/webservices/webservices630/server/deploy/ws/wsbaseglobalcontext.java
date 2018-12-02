package com.sap.engine.services.webservices.webservices630.server.deploy.ws;

import java.util.Hashtable;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSBaseGlobalContext {

  private Hashtable contexts = new Hashtable();

  public WSBaseGlobalContext() {
  }

  public WSBaseGlobalContext(Hashtable contexts) {
    this.contexts = contexts;
  }

  public Hashtable getContexts() {
    return contexts;
  }

  public void setContexts(Hashtable contexts) {
    this.contexts = contexts;
  }

  public boolean containsContext(String applicationName) {
    return contexts.containsKey(applicationName);
  }

  public void putContext(String applicationName, WSBaseContext wsBaseContext) {
    contexts.put(applicationName, wsBaseContext);
  }

  public WSBaseContext getContext(String applicationName) {
    return (WSBaseContext)contexts.get(applicationName);
  }

  public WSBaseContext removeContext(String applicationName) {
    return (WSBaseContext)contexts.remove(applicationName);
  }

}
