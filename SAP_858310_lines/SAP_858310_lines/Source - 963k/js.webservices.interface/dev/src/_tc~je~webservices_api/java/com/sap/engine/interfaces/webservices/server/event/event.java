package com.sap.engine.interfaces.webservices.server.event;

/**
 * Title: Event
 * Description: The event is a wrapper of information of any kind.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface Event {

  /**
   * The method sets the event state stamp. It is expected to be used only by the Event System.
   *
   * @param stateStamp - the state stamp value
   */
  public void setStateStamp(String stateStamp);

  /**
   * The method returns the event state stamp.
   *
   * @return String - the event state stamp value.
   */
  public String getStateStamp();

  /**
   * The method returns the event identifier.
   *
   * @return String - the event identifier.
   */
  public String getEventId();

  /**
   * The method returns the event value.
   *
   * @return Object - the event value.
   */
  public Object getValue();

}
