package com.sap.engine.services.webservices.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.lang.ref.SoftReference;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Copyright (c) 2008 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Ivaylo Zlatanov
 * @version      1.0
 */

public class SoftReferenceInstancesPool <I> {
  
  private static final int DEF_MAX_SIZE  =  64;
  private static final int INITIAL_SIZE  =  8;
  private final int maxSize;
 
  private SoftReference<I>[] data;
  
  //current size
  private int size;


  public SoftReferenceInstancesPool(int initialSize, int maxSize) {
    if (maxSize < 1){
      throw new IllegalArgumentException("max size must be positive");
    }
    if (initialSize < 1){
      throw new IllegalArgumentException("initial size must be positive");
    }
    if (initialSize > maxSize) {
      throw new IllegalArgumentException("initial size must not exceed max size.");
    }

    data = new SoftReference[initialSize];
    this.maxSize = maxSize;
    this.size = 0;
  }

  public SoftReferenceInstancesPool() {
    this(INITIAL_SIZE, DEF_MAX_SIZE);
  }


  /**
   * Add object to pool.
   *
   * @param   instance  the object added to the pool
   */
  public synchronized boolean rollBackInstance(I instance) {
    if (size == data.length) {
      if (data.length == maxSize) {
        return false;
      }

      int newLength = ((data.length * 2) > maxSize) ? maxSize : data.length * 2;
      //resize
      SoftReference<I>[] temp = data;
      data = new SoftReference[newLength];
      System.arraycopy(temp, 0, data, 0, temp.length);
    }

    data[size++] = new SoftReference<I>(instance);
    return true;
  }

  /**
   * Get object from pool. If pool is empty, return null.
   *
   * @return     the object from pool
   */
  public synchronized I getInstance() {
    
    
    if (size == 0) {
      return null;
    } else {
      SoftReference<I> r = null;
      I instance = null;
      do {
        r = data[--size];
        data[size] = null;
        if (r == null){ // must not happen
          continue; 
        }
        instance = r.get();
      } while(instance == null && size > 0);

      if (r == null){
        return null;
      }
      return instance;
      
    }
  }

  public synchronized void clear() {
    for (int i = 0; i < data.length; i++) {
      data[i] = null;
    }
    size = 0;  
  }

  public synchronized int size() {
    return size;
  }

  public int maxSize(){
    return maxSize;
  }
  
  public synchronized int sizeAlive(){
    int alive = size;
    if (alive > 0){
      SoftReference<I> r = null;
      for (int i = 0; i < size; i ++){
        r = data[i];
        if (r.get() == null){
          alive--;
        }
      }
    }
    return alive;
  }
}