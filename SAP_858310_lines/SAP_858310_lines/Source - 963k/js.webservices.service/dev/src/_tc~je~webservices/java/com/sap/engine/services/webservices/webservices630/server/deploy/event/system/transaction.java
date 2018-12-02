package com.sap.engine.services.webservices.webservices630.server.deploy.event.system;

import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface Transaction {

  public void make() throws WSDeploymentException, WSWarningException;

  public void commit() throws WSWarningException;

  public void rollback() throws WSWarningException;

  public void makeRemote() throws WSWarningException;

}
