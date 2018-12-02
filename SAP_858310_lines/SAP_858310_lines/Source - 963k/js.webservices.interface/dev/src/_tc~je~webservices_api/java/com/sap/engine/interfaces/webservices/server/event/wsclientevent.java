package com.sap.engine.interfaces.webservices.server.event;

/**
 * Title: WSClientEvent
 * Description: This interface should be implemented by all events, that contains a web service change.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface WSClientEvent extends Event {

  /**
   * The method returns the application name that the event is associated to.
   *
   * @return String - the application name.
   */
  public String getApplicationName();

  /**
   * The method returns the ws client name that the event is associated to.
   *
   * @return String - the ws client name.
   */
  public String getWSClientName();

  /**
   * The method set the ws client configuration name. It is expected to be used only by the Event System.
   *
   * @param wsClientConfigName - the ws client configuration name.
   */
  public void setWSClientConfigName(String wsClientConfigName);

  /**
   * The method returns the ws client configuration name. It is expected to be used only by the Event System.
   *
   * @return String - the ws client configuration name.
   */
  public String getWSClientConfigName();

}
