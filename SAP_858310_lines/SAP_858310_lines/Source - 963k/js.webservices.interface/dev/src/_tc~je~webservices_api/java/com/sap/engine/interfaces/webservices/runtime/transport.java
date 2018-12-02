/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime;

/**
 *   This object encapsulates the transport specific
 * data which is needed by the Protocols, TransportBindings ...
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */
public abstract class Transport {

  private String entryPointID;

  protected Transport() {
    this(null);
  }

  /**
   * Invoked by the servlet to set the ID of the
   * entry point.
   */
  protected Transport(String entryPointID) {
    this.entryPointID = entryPointID;
  }

  /**
   * Invoked by the servlet to set the ID of the
   * entry point.
   */
  public void setEntryPointID(String entryPointID) {
    this.entryPointID = entryPointID;
  }

  /**
   * Invoked by the runtime to find
   * the correct configuration.
   */
  public String getEntryPointID() {
    return entryPointID;
  }

  /**
   * Invoked by the servlet to reuse this obect
   */
  public void clear() {
    this.entryPointID = null;
  }

  /**
   * Invoked by the runtime to send error in
   * transport dependent way. Invoked if RuntimeTransportBinding
   * method sendServerError throws an exception.
   */
  public abstract boolean sendServerError(Throwable thr);

  /**
   * Returns the transportID
   */
  public abstract String getTransportID();

}

