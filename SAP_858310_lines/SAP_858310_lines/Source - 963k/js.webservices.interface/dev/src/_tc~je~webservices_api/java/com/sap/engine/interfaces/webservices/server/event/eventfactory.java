package com.sap.engine.interfaces.webservices.server.event;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;

/**
 * Title: EventFactory
 * Description: Thid interface provides methods for saving/loading an event to/from DB.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface EventFactory {

  /**
   * The method returns the event identifier.
   *
   * @return String - the event identifier.
   */
  public String getEventId();

  /**
   * The method serializes the event into DB.
   *
   * @param eventConfiguration - the event configuration.
   */
  public void save(Event event, Configuration eventConfiguration) throws WSDeploymentException, WSWarningException;


 /**
  * The method loads and deserializes the event from DB.
  *
  * @param eventConfiguration - the eventconfiguration.
  */
  Event load(Configuration eventConfiguration) throws WSDeploymentException, WSWarningException;

}
