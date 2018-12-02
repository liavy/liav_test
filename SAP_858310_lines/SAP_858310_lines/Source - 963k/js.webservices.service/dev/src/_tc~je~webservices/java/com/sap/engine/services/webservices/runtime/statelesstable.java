package com.sap.engine.services.webservices.runtime;

import java.util.Enumeration;

import javax.ejb.EJBObject;

import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.services.timeout.TimeoutListener;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.tools.InstancesPool;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class StatelessTable implements TimeoutListener {

  private HashMapObjectObject hashTable = new HashMapObjectObject();

  public synchronized InstancesPool getPool(String key) {
    InstancesPool pool = (InstancesPool) hashTable.get(key);

    if (pool != null) {
      return pool;
    } else {
      pool = new InstancesPool();
      hashTable.put(key, pool);
      return pool;
    }
  }

  public void rollBackInstance(InstanceWrapper instance) {
    //in case exception has occured before obtaining instance
    if (instance == null) {
      return;
    }
    InstancesPool pool = instance.getOwnerPool();
    pool.rollBackInstance(instance);
  }

  public synchronized Enumeration keys() {
    return this.hashTable.keys();
  }

  public synchronized InstancesPool get(String key) {
    return (InstancesPool) this.hashTable.get(key);
  }

  public boolean check() {
    return true;
  }

  public void timeout() {

    InstanceWrapper instWrapper;
    InstancesPool pool;
    Object[] keys;

    long endTime = System.currentTimeMillis();

    synchronized  (this) {
      keys = hashTable.getAllKeys(); //the currect key content
    }

    //!!!!It is not synchnized to hashtable. The new added entries while onTime() goes won't be process
    for (int i = 0; i < keys.length; i++) {

      pool = this.get((String) keys[i]);

      synchronized (pool) { //locking the pool while is checking not to be used
        for (int j = 0; j < pool.size(); j++) {
          instWrapper = (InstanceWrapper) pool.get(j);
          if ( (endTime - instWrapper.getLastActiveTime()) > instWrapper.getSessionTimeout()) {
            //removing it from the pool
            Location loc = (instWrapper.logLocation != null) ? instWrapper.logLocation : Location.getLocation(WSLogging.RUNTIME_LOCATION);
            try {
              pool.remove(j--);
              //removing object
              Object obj = instWrapper.getInstance();
              if (obj instanceof EJBObject) {
                ((EJBObject) obj).remove();
              }
              loc.logT(Severity.PATH, "Removing stateless instance: " + instWrapper.getInstance() + " from pool " + instWrapper.getCacheKey());
            } catch (Exception e) {
              loc.catching("Exception in removing stateless object: [" + instWrapper.getInstance() + "]", e);
            }
          }
        }
      }
    } //end for
  }
}