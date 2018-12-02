/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */

package com.sap.engine.frame.core.thread;

/**
 * This is an interface for objects that are associated to a thread context.
 * If such an object implements this interface means that remote object
 * systems have to transfer it to the client side.
 *
 * @version 6.30
 */
public interface TransferableExt extends Transferable{

  /**
   * return size of the object if it will be stored into a byte array. if the
   * objects is not transferable this method returns -1.
   * this method is used when the transfer is between different clusters.
   *
   * @param    clusterid - identifier of the requesting cluster.
   */
  public int size(Object clusterid);

  /**
   * store object into byte array.
   * this method is used when the transfer is between different clusters.
   *
   * @param    to - the byte array where to store the object
   * @param    offset - position into byte array from where to store the object
   * @param    clusterid - identifier of the requesting cluster.
   */
  public void store(Object clusterid, byte[] to, int offset);

  /**
   * load internal structure of the object from a byte array.
   * this method is used when the transfer is between different clusters.
   *
   * @param    from - byte array from where to load internal data
   * @param    offset - the offset in byte array where is located the data
   * @param    clusterid - identifier of the requesting cluster.
   */
  public void load(Object clusterid, byte[] from, int offset);
}
