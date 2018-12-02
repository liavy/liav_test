/**
 * Property of SAP AG, Walldorf
 * (c) Copyright SAP AG, Walldorf, 2000-2002.
 * All rights reserved.
 */
package com.sap.engine.frame.core.thread;

/**
 * This is an interface for objects that are associated to a thread context.
 * By default this objects are inherritable - when you start a new thread
 * all object from the parent are copied to the child.
 *
 * @author Jasen Minov
 * @version 6.30
 */
public interface ContextObject {

  /**
   * Through this method you can very fast fill internal datastructure of the
   * object. This method is called from the system to copy context object of
   * the parent thread to this one associated with the new thread
   *
   * @param    form - context object of a parent thread
   * @param    isSystem - true if new thread is system thread
   */
  public ContextObject childValue(ContextObject parent, ContextObject child);


  /**
   * Create new context object (or get it from the pool).
   */
  public ContextObject getInitialValue();


  /**
   * Release context object (or return it to the pool).
   */
  public void empty();

}

