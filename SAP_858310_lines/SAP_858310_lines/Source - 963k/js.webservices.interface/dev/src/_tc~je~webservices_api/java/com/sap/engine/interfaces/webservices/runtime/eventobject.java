/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime;

/**
 *   Encapsulates event data and ID. The Runtime creates instance of
 * this class setting it some specific event data and ID. The ID
 * is used to resolve the Event. The IDs constants are listed in EventObjectIDs
 * interface.
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */
public class EventObject {

  private String eventID;
  private Object data;


  public EventObject(String eventID, Object data) {
    this.eventID = eventID;
    this.data = data;
  }

  public EventObject() {
  }

  /**
   *  Returns the Event ID
   */
  public String getEventID() {
    return eventID;
  }

  /**
   *  Returns the Event specific data
   */
  public Object getData() {
    return data;
  }

  public void setEventID(String eventID) {
    this.eventID = eventID;
  }

  public void setData(Object data) {
    this.data = data;
  }
}
