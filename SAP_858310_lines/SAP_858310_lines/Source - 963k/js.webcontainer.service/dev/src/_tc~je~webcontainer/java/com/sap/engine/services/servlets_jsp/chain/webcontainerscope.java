package com.sap.engine.services.servlets_jsp.chain;

import com.sap.engine.services.httpserver.chain.Scope;
import com.sap.engine.services.httpserver.interfaces.HttpHandler;
import com.sap.engine.services.servlets_jsp.server.WebContainerProperties;
import com.sap.engine.services.servlets_jsp.server.WebMonitoring;

public interface WebContainerScope extends Scope {
  public abstract WebContainerProperties getWebContainerProperties();
  
  public abstract WebMonitoring getWebMonitoring();
  
  /**
   * TODO: Remove it when web container integration finish
   * @deprecated
   */
  public HttpHandler getHttpHandler();
}
