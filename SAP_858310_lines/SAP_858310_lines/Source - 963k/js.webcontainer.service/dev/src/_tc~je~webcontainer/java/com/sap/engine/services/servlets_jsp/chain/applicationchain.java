package com.sap.engine.services.servlets_jsp.chain;

import com.sap.engine.services.httpserver.chain.HostChain;



public interface ApplicationChain extends HostChain {
  /**
   * Gives access to this chain <code>WebContainerScope</code>
   * 
   * @return
   * the <code>WebContainerScope</code> of this chain
   */
  public abstract WebContainerScope getWebContainerScope();
  
  /**
   * Gives access to this chain <code>ApplicationScope</code>
   * 
   * @return
   * the <code>ApplicationScope</code> of this chain
   */
  public abstract ApplicationScope getApplicationScope();
}
