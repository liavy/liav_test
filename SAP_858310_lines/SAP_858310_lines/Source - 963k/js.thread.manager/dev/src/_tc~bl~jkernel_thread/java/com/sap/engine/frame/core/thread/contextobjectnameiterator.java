/**
 * Property of SAP AG, Walldorf
 * (c) Copyright SAP AG, Walldorf, 2000-2002.
 * All rights reserved.
 */
package com.sap.engine.frame.core.thread;

/**
 * This is iterator for context object names.
 *
 * @author Jasen Minov
 * @version 6.30
 */
public interface ContextObjectNameIterator {

  /**
   *  Returns true if there are more cotext objects
   */
  public boolean hasNext();


  /**
   *  Returns the name of next context object
   */
  public String nextName();


  /**
   *  Release iterator
   */
  public void releaseIterator();

}

