package com.sap.engine.services.webservices.server;

import com.sap.engine.interfaces.ejb.monitor.EJBManager;

/**
 * Title: InterfaceContext
 * Description: This context is a storage for all interfaces, provided by the server.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class InterfaceContext {

  EJBManager ejbManager = null; 

  public InterfaceContext() {

  }

  public EJBManager getEjbManager() {
    return ejbManager;
  }

  public void setEjbManager(EJBManager ejbManager) {
    this.ejbManager = ejbManager;
  }

}
