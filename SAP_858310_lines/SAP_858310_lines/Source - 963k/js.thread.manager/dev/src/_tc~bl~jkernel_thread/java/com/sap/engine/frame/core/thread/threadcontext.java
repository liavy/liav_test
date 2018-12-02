/**
 * Property of SAP AG, Walldorf
 * (c) Copyright SAP AG, Walldorf, 2000-2002.
 * All rights reserved.
 */
package com.sap.engine.frame.core.thread;

/**
 * TODO: Describe why this class is introduced
 * 
 * This is connection to internal thread system. There are many reasons that a
 * service has to use this system instead of directly creating java threads.
 * Using this system it gets performance, thread related data consistency,
 * thread resource loadbalancing and thread resource protection.
 *
 * @author Jasen Minov
 * @version 6.30
 */
public interface ThreadContext extends ClientThreadContext {

  /**
   * Return the id of context object and -1 if it doesn't exist.
   *
   * @param    name - the name of the object
   */
  public int getContextObjectId(String name);


  /**
   * Get ContextObject instance that is connected to the current thread by id.
   * If such an object doesn't exists return <source> null <source>
   *
   * @param   id - the id of context object
   */
  public ContextObject getContextObject(int id);


  /**
   * Set ContextObject instance for current thread.
   *
   * @param   id - the id of context object
   */
  public void setContextObject(int id, ContextObject object);

  public boolean isSystem();
  
  /**
   * @deprecated
   */
  public void empty();

}

