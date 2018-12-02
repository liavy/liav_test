/**
 * Property of SAP AG, Walldorf
 * (c) Copyright SAP AG, Walldorf, 2000-2002.
 * All rights reserved.
 */
package com.sap.engine.frame.core.thread;

/**
 * This is connection from the client side to the thread context.
 *
 * @author Jasen Minov
 * @version 6.30
 */
public interface ClientThreadContext {

  /**
   * Unregister object that is attached to a thread.
   *
   * @param   name - the name of context object
   *
   */
  public void unregisterContextObject(String name);


  /**
   * Get ContextObject instance that is connected to the current thread by name.
   * If such an object doesn't exists return <source> null <source>
   *
   * @param   name - the name of context object
   *
   */
  public ContextObject getContextObject(String name);


  /**
   * Set ContextObject instance for current thread.
   *
   * @param   id - the id of context object
   *
   */
  public void setContextObject(String name, ContextObject object);


  /**
   * Get names of all context objects that are registered.
   *
   */
  public ContextObjectNameIterator getContextObjectNames();


  /**
   * Get names of all context objects that are registered and are transferable
   * If one object is transferrable then remote protocols will try to keep it
   * into thread context during remote calls. Such an objects are session,
   * transactions, etc...
   *
   */
  public ContextObjectNameIterator getTransferableContextObjectNames();

}

