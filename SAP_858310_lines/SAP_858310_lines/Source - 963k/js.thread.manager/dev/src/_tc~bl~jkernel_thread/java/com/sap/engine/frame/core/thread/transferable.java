/**
 * Property of SAP AG, Walldorf
 * (c) Copyright SAP AG, Walldorf, 2000-2002.
 * All rights reserved.
 */
package com.sap.engine.frame.core.thread;

/**
 * This is an interface for objects that are associated to a thread context.
 * If such an object implements this interface means that remote object
 * systems have to transfer it to the client side.
 *
 * @author Jasen Minov
 * @version 6.30
 */
public interface Transferable {

  /**
   * Return size of the object if it will be stored into a byte array. If the
   * objects is not transferable this method returns -1.
   */
  public int size();


  /**
   * Store object into byte array.
   *
   * @param    to - the byte array where to store the object
   * @param    offset - position into byte array from where to store the object
   */
  public void store(byte[] to, int offset);


  /**
   * Load internal structure of the object from a byte array
   *
   * @param    from - byte array from where to load internal data
   * @param    offset - the offset in byte array where is located the data
   */
  public void load(byte[] from, int offset);

}

