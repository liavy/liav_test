/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.frame.client;

import java.util.*;

import com.sap.engine.frame.core.thread.ClientThreadContext;
import com.sap.engine.frame.core.thread.ContextObject;
import com.sap.engine.frame.core.thread.ContextObjectNameIterator;
import com.sap.engine.frame.core.thread.Transferable;
import com.sap.engine.lib.lang.Monitor;
import com.sap.engine.lib.util.ConcurrentHashMapObjectObject;

/**
 * Client Thread Context implementation.
 *
 * @author Krasimir Semerdzhiev (krasimir.semerdzhiev@sap.com)
 * @version 6.30
 */
public class ClientThreadContextImpl extends InheritableThreadLocal implements ClientThreadContext, ClientThreadContextFactory {

  private ConcurrentHashMapObjectObject coHash = new ConcurrentHashMapObjectObject();
  private Monitor synch = new Monitor();
  private HashSet transferables = new HashSet();
  private static ClientThreadContextImpl current = new ClientThreadContextImpl();
  private static ConcurrentHashMapObjectObject staticCoHash = new ConcurrentHashMapObjectObject();

  public ClientThreadContextImpl() {
    super();
  }

  /**
   * Get ContextObject instance that is connected to the current thread by name.
   * If such an object doesn't exists return <source> null <source>
   *
   * @param   name - the name of context object
   *
   */
  public ContextObject getContextObject(String name) {
    ContextObject result = (ContextObject)coHash.get(name);
    if (result == null) {
      synchronized (staticCoHash) {
        result = (ContextObject)staticCoHash.get(name);
        if (result != null) {
          result = result.getInitialValue();
          setContextObject(name, result);
        }
      }
    }
    return result;
  }

  /**
   * Get names of all context objects that are registered.
   *
   */
  public ContextObjectNameIterator getContextObjectNames() {
    return new ContextObjectNameIteratorImpl(coHash.getAllKeys());
  }

  /**
   * Get names of all context objects that are registered and are transferable
   * If one object is transferrable then remote protocols will try to keep it
   * into thread context during remote calls. Such an objects are session,
   * transactions, etc...
   *
   */
  public ContextObjectNameIterator getTransferableContextObjectNames() {
    return new ContextObjectNameIteratorImpl(transferables.toArray());
  }

  /**
   * Set ContextObject instance for current thread.
   *
   * @param   name - name of context object
   * @param   object - context object
   *
   */
  public void setContextObject(String name, ContextObject object) {
    if ((coHash.get(name) == null) && (staticCoHash.get(name) == null)) {
      staticCoHash.put(name, object.getInitialValue());
    }

    coHash.put(name, object);

    if (object instanceof Transferable) {
      transferables.add(name);
    }
  }

  /**
   * Unregister object that is attached to a thread.
   *
   * @param   name - the name of context object
   *
   */
  public void unregisterContextObject(String name) {
    synchronized (synch) {
      coHash.remove(name);
      transferables.remove(name);
    }
  }

  /**
   * Returns the calling thread's initial value for this ThreadLocal
   * variable. This method will be called once per accessing thread for
   * each ThreadLocal, the first time each thread accesses the variable
   * with get or set.  If the programmer desires ThreadLocal variables
   * to be initialized to some value other than null, ThreadLocal must
   * be subclassed, and this method overridden.  Typically, an anonymous
   * inner class will be used.  Typical implementations of initialValue
   * will call an appropriate constructor and return the newly constructed
   * object.
   *
   * @return the initial value for this ThreadLocal
   */
  protected Object initialValue() {
    return new ClientThreadContextImpl();
  }

  /**
   * Computes the child's initial value for this InheritableThreadLocal
   * as a function of the parent's value at the time the child Thread is
   * created.  This method is called from within the parent thread before
   * the child is started.
   * <p>
   * This method merely returns its input argument, and should be overridden
   * if a different behavior is desired.
   *
   * @param parentValue the parent thread's value
   * @return the child thread's initial value
   */
  protected Object childValue(Object parentValue) {
    ClientThreadContextImpl child = new ClientThreadContextImpl();
    ClientThreadContextImpl parent = (ClientThreadContextImpl) parentValue;
    synchronized (parent.synch) {
      child.childCoHash(parent.coHash);
      child.transferables = (HashSet) parent.transferables.clone();
    }
    return child;
  }

  private void childCoHash(ConcurrentHashMapObjectObject pCoHash) {
    if (coHash != null) {
      coHash.clear();
    } else {
      coHash = new ConcurrentHashMapObjectObject(pCoHash.size());
    }

    Enumeration enumHashKeys = pCoHash.keys();

    while (enumHashKeys.hasMoreElements()) {
      String temp = (String) enumHashKeys.nextElement();
      ContextObject tempCo = (ContextObject) pCoHash.get(temp);
      ContextObject tempNew = null;
      tempNew = tempCo.childValue(tempCo, tempNew);
      if (tempNew == null) {
        Thread.dumpStack();
      }
      coHash.put(temp, tempNew);
    }
  }

  public ClientThreadContext getThreadContext() {
    return (ClientThreadContext) current.get();
  }

}

