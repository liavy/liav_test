package com.sap.engine.services.webservices.espbase.client.dynamic.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

import com.sap.engine.services.webservices.espbase.client.dynamic.DGenericService;

public class ServiceSoftReference extends SoftReference<DGenericService> {
  
  private Object key;
  
  public ServiceSoftReference(Object key, DGenericService service, ReferenceQueue referenceQueue) {
    super(service, referenceQueue);
    this.key = key;
  }
  
  Object getKey() {
    return(key);
  }
}
