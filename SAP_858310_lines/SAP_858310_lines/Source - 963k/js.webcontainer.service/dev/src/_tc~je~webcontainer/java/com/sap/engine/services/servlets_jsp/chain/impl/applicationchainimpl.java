package com.sap.engine.services.servlets_jsp.chain.impl;

import java.util.Iterator;

import com.sap.engine.services.httpserver.chain.AbstractChain;
import com.sap.engine.services.httpserver.chain.Filter;
import com.sap.engine.services.httpserver.chain.HostChain;
import com.sap.engine.services.httpserver.chain.HostScope;
import com.sap.engine.services.httpserver.chain.ServerScope;
import com.sap.engine.services.servlets_jsp.chain.ApplicationScope;
import com.sap.engine.services.servlets_jsp.chain.ServletScope;
import com.sap.engine.services.servlets_jsp.chain.WebContainerScope;

public class ApplicationChainImpl extends AbstractChain implements
    ServletChain {
  private WebContainerScope webScope;
  private ApplicationScope appScope;
  private ServletScope servletScope;

  public ApplicationChainImpl(HostChain hostChain, Iterator<Filter> filters,
      WebContainerScope webContainerScope) {
    super(hostChain, filters);
    this.webScope = webContainerScope;
  }
  
  public ServerScope getServerScope() {
    return ((HostChain)chain).getServerScope();
  }

  public HostScope getHostScope() {
    return ((HostChain)chain).getHostScope();
  }

  public WebContainerScope getWebContainerScope() {
    return webScope;
  }
  
  public ApplicationScope getApplicationScope() {
    return appScope;
  }

  public void setApplicationScope(ApplicationScope scope) {
    this.appScope = scope;
  }

  public ServletScope getServletScope() {
    return servletScope;
  }

  public void setServletScope(ServletScope servletScope) {
    this.servletScope = servletScope;
  }
}
