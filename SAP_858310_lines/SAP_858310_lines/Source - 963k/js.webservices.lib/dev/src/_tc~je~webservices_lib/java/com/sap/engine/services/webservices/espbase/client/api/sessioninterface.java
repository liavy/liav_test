package com.sap.engine.services.webservices.espbase.client.api;

import java.rmi.RemoteException;

/**
 * HTTP Session control interface. The HTTP Sessions are server initiated not client initiated.
 * closeSession just discards the current cookies.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public interface SessionInterface {

  /**
   * Returns the state of the current maintain session property.
   * When session with the server is open the isMaintainSession returns true.
   * @return
   */
  public boolean isMaintainSession();

  /**
   * Closes the current session by discarding the cookies.
   */
  public void closeSession();

  /**
   * Special SAP Specific extension that might be called by the client to
   * notify the server to release session related resources after closeSession is called.
   * Otherwise the server will wait the client session to timeout.
   */
  public void releaseServerResources() throws RemoteException;

}
