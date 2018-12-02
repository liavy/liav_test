package com.sap.engine.services.servlets_jsp.chain.impl;

import com.sap.engine.services.servlets_jsp.chain.ApplicationChain;
import com.sap.engine.services.servlets_jsp.chain.ServletScope;

public interface ServletChain extends ApplicationChain {
  public ServletScope getServletScope();
}
