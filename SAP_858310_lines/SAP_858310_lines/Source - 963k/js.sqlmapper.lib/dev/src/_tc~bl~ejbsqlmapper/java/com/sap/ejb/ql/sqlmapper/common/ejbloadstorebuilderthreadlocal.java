package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.common.EJBLoadStoreBuilder;

import java.util.WeakHashMap;

/**
 * Provides a thread local <code>EJBLoadStoreBuilder</code> instance.
 * In order to avoid memory leaks the instances are not stored
 * in the respective thread's map.
 * <p></p>
 * Copyright (c) 2004, 2006 SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 2.0
 */

public final class EJBLoadStoreBuilderThreadLocal
{
  private WeakHashMap<Thread, EJBLoadStoreBuilder> map = new  WeakHashMap<Thread, EJBLoadStoreBuilder>();
  /**
   * Creates a new <code>EJBLoadStoreBuilder</code> object.
   */
  synchronized EJBLoadStoreBuilder get()
  {
    EJBLoadStoreBuilder elsb = this.map.get(Thread.currentThread());
    if ( elsb == null )
    {
      elsb = new EJBLoadStoreBuilder();
      this.map.put(Thread.currentThread(), elsb);
    }
    return elsb;
  }
}
