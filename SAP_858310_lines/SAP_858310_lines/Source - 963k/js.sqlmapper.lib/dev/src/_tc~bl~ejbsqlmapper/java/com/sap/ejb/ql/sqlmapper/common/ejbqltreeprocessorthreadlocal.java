package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.common.EJBQLTreeProcessor;

import java.util.WeakHashMap;

/**
 * Provides a thread local <code>EJBQLTreeProcessor</code> instance.
 * In order to avoid memory leaks the instances are not stored
 * in the respective thread's map.
 * <p></p>
 * Copyright (c) 2004, 2006, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 2.0
 */

public final class EJBQLTreeProcessorThreadLocal
{
  private WeakHashMap<Thread, EJBQLTreeProcessor> map = new WeakHashMap<Thread, EJBQLTreeProcessor>();

  /**
   * Creates a new <code>EJBQLTreeProcessor</code> object.
   */
  synchronized EJBQLTreeProcessor get()
  {
    EJBQLTreeProcessor etp = this.map.get(Thread.currentThread());
    if ( etp == null )
    {
      etp = new EJBQLTreeProcessor();
      this.map.put(Thread.currentThread(), etp);
    }
    return etp;
  }
}
