package com.sap.engine.services.webservices.jaxr;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.JAXRResponse;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Vector;

public class BulkResponseImpl extends JAXRResponseImpl implements BulkResponse {
  private boolean partial;
  private ArrayList objects;
  private ArrayList exceptions;
  
//  public BulkResponseImpl(Collection objects, Collection exceptions) {
//    this(false, objects, exceptions);
//  }

  public BulkResponseImpl() {
    partial = false;
    objects = new ArrayList();
    exceptions = new ArrayList();
    setRequestId(generateUUID());
  }
  
  public BulkResponseImpl(boolean partial, Collection objects, Collection exceptions) {
    this.partial = partial;
    if (partial == false) {
      setStatus(JAXRResponse.STATUS_WARNING);
    }
    if (objects == null) {
      this.objects = new ArrayList();
    } else {
      this.objects = new ArrayList(objects);
    }
    if (exceptions == null) {
      this.exceptions = new ArrayList();
    } else {
      this.exceptions = new ArrayList(exceptions);
    }
    setRequestId(generateUUID());
  }
  
  public void addException(Throwable thr) {
    exceptions.add(thr);
  }
  
  public void addObject(Object obj) {
    objects.add(obj);
  }
  
  public void addObjects(Collection objects) {
    this.objects.addAll(objects);
  }
  
  public Collection getCollection() throws JAXRException {
    return objects;
  }
  
  public Collection getExceptions() throws JAXRException {
    if (exceptions.size() == 0) {
      return null;
    }
    return exceptions;
  }
  
  public boolean isPartialResponse() throws JAXRException {
    return partial;
  }
  
  public void setPartialResponse(boolean partial) {
    this.partial = partial;
  }
  
  public void addAnotherBulkResponse(BulkResponse response) throws JAXRException {
    if (response.isPartialResponse()) {
      partial = true;
    }
    Vector temp = new Vector();
    temp.addAll(response.getCollection());
    objects.addAll(response.getCollection());
    if (response.getExceptions() != null) {
      exceptions.addAll(response.getExceptions());
    }
    setStatus(response.getStatus());
  }

  public String generateUUID() {
    String s = null;
    try {
      s = /*java.net.InetAddress.getLocalHost() + */(new java.rmi.server.UID()).toString();
    } catch (Exception unknownhostexception) {
      throw new RuntimeException(unknownhostexception.toString());
    }
    return s;
  }
}