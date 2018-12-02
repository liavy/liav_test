package com.sap.engine.services.servlets_jsp.chain;

import com.sap.engine.services.httpserver.chain.Scope;

public interface ServletScope extends Scope {
  public String getServletName();
  
  public String getServletClassName();
  
  public String[] getFilterChain();
}
