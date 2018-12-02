package com.sap.engine.services.webservices.webservices630.server.deploy.ws;

import com.sap.engine.services.webservices.runtime.definition.ServiceEndpointDefinition;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;

/**
 * Title: WSDeploySettingsProvider
 * Description: The interface provides access and implementation of functionality, that is deployment usage dependent.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface WSDeploySettingsProvider {

  public String getClassPath();

  public void defineImplLink(ServiceEndpointDefinition serviceEndpointDefinition) throws WSDeploymentException;

}
