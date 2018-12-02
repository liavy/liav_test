package com.sap.engine.services.webservices.espbase.client.dynamic.util;

import java.lang.ref.ReferenceQueue;
import java.util.Enumeration;
import java.util.Hashtable;

import com.sap.engine.services.webservices.espbase.client.dynamic.DGenericService;

public class ServiceCache {

  private Hashtable<Object, ServiceSoftReference> serviceSoftRefsHash;
  private ReferenceQueue refQueue;
  
  public ServiceCache() {
    serviceSoftRefsHash = new Hashtable();
    refQueue = new ReferenceQueue();
  }
  
  private void clearCollectedSoftReferences() {
    ServiceSoftReference serviceSoftRef = null;
    while((serviceSoftRef = (ServiceSoftReference)(refQueue.poll())) != null) {
      serviceSoftRefsHash.remove(serviceSoftRef.getKey());
    }
  }
  
  public synchronized void put(Object key, DGenericService service) {
    clearCollectedSoftReferences();
    serviceSoftRefsHash.put(key, new ServiceSoftReference(key, service, refQueue));
  }
  
  public synchronized DGenericService get(Object key) {
    clearCollectedSoftReferences();
    ServiceSoftReference serviceSoftRef = serviceSoftRefsHash.get(key);
    return(serviceSoftRef == null ? null : serviceSoftRef.get());
  }
  
  public synchronized void remove(Object key) {
    clearCollectedSoftReferences();
    serviceSoftRefsHash.remove(key);
  }
  
  public synchronized Enumeration keys() {
    clearCollectedSoftReferences();
    return(serviceSoftRefsHash.keys());
  }
  
  public synchronized void clear() {
    serviceSoftRefsHash.clear();
  }
}
