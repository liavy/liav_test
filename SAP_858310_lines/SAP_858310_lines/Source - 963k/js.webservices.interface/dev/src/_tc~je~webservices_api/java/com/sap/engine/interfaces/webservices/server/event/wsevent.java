package com.sap.engine.interfaces.webservices.server.event;

/**
 * Title: WSEvent
 * Description: This interface should be implemented by all events, that contains a web service change.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface WSEvent extends Event {

  /**
   * The method returns the application name that the event is associated to.
   *
   * @return String - the application name.
   */
  public String getApplicationName();

  /**
   * The method returns the web service name that the event is associated to.
   *
   * @return String - the web servcie name.
   */
  public String getWebServiceName();

  /**
   * The method set the web service configuration name. It is expected to be used only by the Event System.
   *
   * @param webServiceConfigName - the web service configuration name.
   */
  public void setWSConfigName(String webServiceConfigName);

  /**
   * The method returns the web service configuration name. It is expected to be used only by the Event System.
   *
   * @return String - the web service configuration name.
   */
  public String getWSConfigName();

}
