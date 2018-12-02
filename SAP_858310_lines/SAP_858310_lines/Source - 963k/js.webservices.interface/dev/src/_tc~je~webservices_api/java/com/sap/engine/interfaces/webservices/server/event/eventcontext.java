package com.sap.engine.interfaces.webservices.server.event;

/**
 * Title: EventContext
 * Description: This interface provides methods for registering/unregistering event factories and listeners.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface EventContext {

  /**
   * The method checks if an event factory has already been registered for an event.
   *
   * @param eventId - the event identifier, that the factory refers to.
   * @return boolean
   */
  public boolean isRegisteredEventFactory(String eventId);

  /**
   * The method registers an event factory for an event.
   *
   * @param eventId - the event identifier, that the factory refers to.
   * @param eventFactory - the event factory.
   */
  public void registerEventFactory(String eventId, EventFactory eventFactory);

  /**
   * The method unregisters an event factory.
   *
   * @param eventId - the event identifier, that the factory refers to.
   */
  public void unregisterEventFactory(String eventId);

  /**
   * The method checks if an event listener has already been registered for an event.
   *
   * @param eventId - the event identifier, that the factory refers to.
   * @param listenerId - the listener identifier.
   * @return boolean
   */
  public boolean isRegisteredEventListener(String eventId, String listenerId);

  /**
   * The method registers an event listener for an event.
   *
   * @param eventId - the event identifier, that the listeners refers to.
   * @param eventListener - the event listener.
   */
  public void registerEventListener(String eventId, EventListener eventListener);

  /**
   * The method unregisters an event listener.
   *
   * @param eventId - the event identifier, that the listener refers to.
   */
  public void unregisterEventListener(String eventId);

}
