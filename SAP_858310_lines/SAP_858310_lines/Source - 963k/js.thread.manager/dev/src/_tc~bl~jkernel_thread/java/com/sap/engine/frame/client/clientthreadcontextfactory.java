/*
 * ...Copyright...
 */
package com.sap.engine.frame.client;

import com.sap.engine.frame.core.thread.ClientThreadContext;

public interface ClientThreadContextFactory {

  public ClientThreadContext getThreadContext();

}

