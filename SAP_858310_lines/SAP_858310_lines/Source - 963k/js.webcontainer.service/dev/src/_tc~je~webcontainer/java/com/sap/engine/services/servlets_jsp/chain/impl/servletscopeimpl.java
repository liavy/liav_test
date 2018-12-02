package com.sap.engine.services.servlets_jsp.chain.impl;

import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.httpserver.interfaces.RequestPathMappings;
import com.sap.engine.services.servlets_jsp.chain.ServletScope;

public class ServletScopeImpl implements ServletScope {
  private RequestPathMappings mappings;
  private String servletName;
  
  public ServletScopeImpl(HttpParameters params) {
    this.mappings = params.getRequestPathMappings();
    if (mappings.getServletName() == null) {
      servletName = null;
    } else {
      servletName = mappings.getServletName().toString();
    }  
  }

  public String getServletName() {
    return servletName;
  }

  public String getServletClassName() {
    return null;
  }

  public String[] getFilterChain() {
    return mappings.getFilterChain();
  }

}
