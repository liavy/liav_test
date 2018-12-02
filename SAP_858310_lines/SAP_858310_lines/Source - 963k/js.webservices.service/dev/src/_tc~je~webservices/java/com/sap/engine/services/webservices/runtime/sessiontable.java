package com.sap.engine.services.webservices.runtime;

import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

import javax.ejb.EJBObject;
import javax.servlet.http.HttpSessionBindingEvent;
import java.util.Enumeration;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class SessionTable implements javax.servlet.http.HttpSessionBindingListener {

  private HashMapObjectObject hashTable = new HashMapObjectObject();

  public synchronized InstanceWrapper registerInstanceInuse(InstanceWrapper instanceWrapper) throws ServerRuntimeProcessException {
    String id = instanceWrapper.getCacheKey();
    instanceWrapper.setInuse(true);

    InstanceWrapper prevInstance = (InstanceWrapper) hashTable.put(id, instanceWrapper);
    if (prevInstance != null) { //has sesssion with such key
      hashTable.put(id, prevInstance);
      throw new ServerRuntimeProcessException(RuntimeExceptionConstants.CONCURENT_STATEFULBEAN_USE, new Object[]{id});
    }

    return instanceWrapper;
  }

  public synchronized InstanceWrapper getInstance(String id) throws ServerRuntimeProcessException {
    InstanceWrapper instance = (InstanceWrapper) hashTable.get(id);

    if (instance == null) { //no instance for ID
      return null;
    }

    if (instance.isInuse()) { //concurent call to stateful instance
      throw new ServerRuntimeProcessException(RuntimeExceptionConstants.CONCURENT_STATEFULBEAN_USE, new Object[]{id});
    }
    instance.setInuse(true);

    return instance;
  }

  public synchronized void rollbackInstance(String sessionID, InstanceWrapper instance) throws ServerRuntimeProcessException {
    if (instance == null) { //this is done to mark the session in case of exception in the RuntimeProcessor
      instance = getInstance(sessionID);
      //this is new session and no bean is mapped to it
      if (instance == null) {
        return;
      }
    }

    //instance.setLastActiveTime(System.currentTimeMillis());
    instance.setInuse(false);
  }

  public synchronized void removeInstance(String id) {
    InstanceWrapper instance = (InstanceWrapper) hashTable.remove(id);
//    System.out.println("SesionTable instance: " + instance);
//    if (instance != null) {
//      System.out.println("SesionTable instance.isInuse(): " + instance.isInuse());
//    }
    if (instance != null && !instance.isInuse()) {

       removeInstance(instance);
    }
  }

  public synchronized Enumeration keys() {
    return this.hashTable.keys();
  }

//  public synchronized void timeout() {
//    //!!!!!while cleaning is performed the table is locked.
//    InstanceWrapper instWrapper;
//    long endTime = System.currentTimeMillis();
//    Object[] keys = hashTable.getAllKeys();
//
//    for (int i = 0; i < keys.length; i++) {
//      instWrapper = (InstanceWrapper) hashTable.get(keys[i]);
//      if ((! instWrapper.isInuse()) && (endTime - instWrapper.getLastActiveTime()) > instWrapper.getSessionTimeout()) {
//        //removing it from the cache
//        hashTable.remove(keys[i]);
//        removeInstance(instWrapper);
//      }
//    }
//  }

//  public boolean check() {
//    return true;
//  }

  public void valueBound(HttpSessionBindingEvent event) {
  }

  public synchronized void valueUnbound(HttpSessionBindingEvent event) {
    String bindID = event.getName(); //takes the value under which this SessionTable is bound into HTTPSession
    removeInstance(bindID);
  }

  private void removeInstance(InstanceWrapper instance) {
    Location loc = (instance.logLocation != null) ? instance.logLocation : Location.getLocation(WSLogging.RUNTIME_LOCATION);
    try {
      //closing sessoin
      Object obj = instance.getInstance();
      loc.logT(Severity.PATH, "Removing object: " + obj);
      if (obj instanceof EJBObject) {
        ((EJBObject) obj).remove();
      }
    } catch (Exception e) {
      loc.catching("Exception in removing session object: [" + instance.getInstance() + "]", e);
    }
  }

}