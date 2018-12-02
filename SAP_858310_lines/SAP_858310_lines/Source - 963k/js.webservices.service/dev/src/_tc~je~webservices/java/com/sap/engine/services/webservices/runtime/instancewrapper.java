package com.sap.engine.services.webservices.runtime;

import java.lang.reflect.Method;

import com.sap.engine.services.webservices.tools.InstancesPool;
import com.sap.tc.logging.Location;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class InstanceWrapper {

  private Object instance;
  private Class instanceClass;
  private Method[] instanceMethods;

  //cache info
  private String cacheKey;
  private InstancesPool ownerPool;
  private long lastActiveTime;  //the time at which the session was used last
  private long sessionTimeout;
  private boolean isInuse;

  Location logLocation; //used in the Session/StatelessTable for logging an tracing in the webservices location
  boolean isNew = true; //used for EJBImplementationContainer cache errors

  public InstanceWrapper(Object instance, String cacheKey, InstancesPool ownerPool) {
    this.instance = instance;
    this.instanceClass = instance.getClass();
    this.instanceMethods = instanceClass.getMethods();
    this.ownerPool = ownerPool;
    this.cacheKey = cacheKey;
  }

  long getLastActiveTime() {
    return lastActiveTime;
  }

  void setLastActiveTime(long lastActiveTime) {
    this.lastActiveTime = lastActiveTime;
  }

  long getSessionTimeout() {
    return sessionTimeout;
  }

  /**
   * Timeout in millies.
   */
  void setSessionTimeout(long sessionTimeout) {
    this.sessionTimeout = sessionTimeout;
  }

  Object getInstance() {
    return instance;
  }

  void setInstance(Object instance) {
    this.instance = instance;
  }

  Class getInstanceClass() {
    return instanceClass;
  }

  void setInstanceClass(Class instanceClass) {
    this.instanceClass = instanceClass;
  }

  Method[] getInstanceMethods() {
    return instanceMethods;
  }

  void setInstanceMethods(Method[] instanceMethods) {
    this.instanceMethods = instanceMethods;
  }

  String getCacheKey() {
    return cacheKey;
  }

  void setCacheKey(String cacheKey) {
    this.cacheKey = cacheKey;
  }

  InstancesPool getOwnerPool() {
    return ownerPool;
  }

  void setOwnerPool(InstancesPool ownerPool) {
    this.ownerPool = ownerPool;
  }

  boolean isInuse() {
    return isInuse;
  }

  void setInuse(boolean inuse) {
    isInuse = inuse;
  }
}