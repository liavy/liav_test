package com.sap.engine.services.webservices.tools;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      1.0
 */

public class InstancesPool {
  //default values
  private static final int INITIAL_SIZE  =  8;
  private static final int MAX_SIZE  =  64;

  private Object[] data;
  private int maxSize;
  //current size
  private int size;

  /**
   * Constructor.
   *
   * @param   initialSize  initial size of the pool
   * @param   maxSize      maximal size of the pool
   */
  public InstancesPool(int initialSize, int maxSize) {

    if (initialSize < 0 || maxSize < 0 || initialSize > maxSize) {
      throw new IllegalStateException("The parameters must be positive and 'initialSize' > 'maxSize'.");
    }

    data = new Object[initialSize];
    this.maxSize = maxSize;
    this.size = 0;
  }

  public InstancesPool() {
    this(INITIAL_SIZE, MAX_SIZE);
  }

  /**
   * Get object from pool. If pool is empty, creates new one.
   *
   * @return     the object from pool
   */
  public synchronized Object getInstance() {
    if (size == 0) {
      return null;
    } else {
      Object obj =  data[--size];
      data[size] = null;
      return obj;
    }
  }

  /**
   * Add object to pool.
   *
   * @param   action  the object added to the pool
   */
  public synchronized void rollBackInstance(Object obj) {
    if (size == data.length) {
      if (data.length == maxSize) {
        return;
      }

      int newLength = ((data.length * 2) > maxSize) ? maxSize : data.length * 2;
      //resize
      Object[] temp = data;
      data = new Object[newLength];
      System.arraycopy(temp, 0, data, 0, temp.length);
    }

    data[size++] = obj;
  }

  public synchronized void clear() {
    //traversing all array
    for (int i = 0; i < data.length; i++) {
      data[i] = null;
    }
    size = 0;
  }

  public synchronized int size() {
    return size;
  }

  public synchronized Object get(int i) {
    if (i >= size || i < 0) {
      throw new ArrayIndexOutOfBoundsException("Current size: " + size + ". Found: " + i);
    }

    return data[i];
  }

  public synchronized Object remove(int i) {
    if (i >= size || i < 0) {
      throw new ArrayIndexOutOfBoundsException("Current size: " + size + ". Found: " + i);
    }

    Object oldValue = data[i];

    int numMoved = size - i - 1;
    if (numMoved > 0)
        System.arraycopy(data, i + 1, data, i,
             numMoved);
    data[--size] = null; // Let gc do its work

    return oldValue;
  }
}