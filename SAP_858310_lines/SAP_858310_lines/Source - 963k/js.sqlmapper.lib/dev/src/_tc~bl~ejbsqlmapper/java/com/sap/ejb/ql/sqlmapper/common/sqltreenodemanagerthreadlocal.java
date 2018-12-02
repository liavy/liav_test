package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager;
import java.util.WeakHashMap;

/**
 * Provides a thread local <code>SQLTreeNodeManager</code> instance.
 * In order to avoid memory leaks the instances are not stored
 * in the respective thread's map.
 * <p></p>
 * Copyright (c) 2004, 2006, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 2.0
 */

public final class SQLTreeNodeManagerThreadLocal
{
  private WeakHashMap<Thread, SQLTreeNodeManager> map = new WeakHashMap<Thread, SQLTreeNodeManager>();

  /**
   * Creates a new <code>SQLTreeNodeManager</code> object.
   */
  synchronized SQLTreeNodeManager get()
  {
    SQLTreeNodeManager stnm = this.map.get(Thread.currentThread());
    if ( stnm == null )
    {
      stnm = new SQLTreeNodeManager();
      this.map.put(Thread.currentThread(), stnm);
    }
    return stnm;
  }
}
