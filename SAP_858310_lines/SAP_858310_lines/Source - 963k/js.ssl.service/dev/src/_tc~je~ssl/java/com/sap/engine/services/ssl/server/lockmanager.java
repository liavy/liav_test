/**
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.ssl.server;

import com.sap.engine.frame.core.locking.LockingContext;
import com.sap.engine.frame.core.locking.ServerInternalLocking;
import com.sap.engine.frame.core.locking.TechnicalLockException;
import com.sap.engine.frame.core.locking.LockException;

/**
 * @author Ilia Kacarov
 *         Copyright (c) 2002, SAP-AG
 */
public class LockManager {
  private static String LOCK_NAME = null;
  private static LockingContext lockingContext = null;
  private static ServerInternalLocking lock = null;
  private static int groupID = -1;
  
  public static final synchronized void setCurrentGroupID(int group_ID) {
    //ServerService.dump("setCurrentGroupID: " + group_ID);
    groupID = group_ID;   
  }
  
  public static final synchronized void setLockContext(LockingContext locking_Context) {
    ServerService.dump("setLockContext: " + locking_Context);
    lockingContext = locking_Context;
  }

  public static final synchronized void start() throws TechnicalLockException {
    if (lockingContext == null || groupID == -1) {
      throw new RuntimeException("ClusterLockManager not initialized");
    }
    
    LOCK_NAME = "$service.ssl.icm_" + groupID;
    lock = lockingContext.createServerInternalLocking(LOCK_NAME, "ICP PSE update synch");
  }
  
  public static final void stop() {
    try {
      lock.unlockAll();
    } catch (TechnicalLockException e) {
      ServerService.dump("cannot unlock all existing locks: ", e);
    }

  }
  
  public static final void getClusterLock() throws LockException, TechnicalLockException {
    ServerService.dump(" >>>>> got cluster lock: " + LOCK_NAME);
    lock.lock(LOCK_NAME, "", ServerInternalLocking.MODE_EXCLUSIVE_NONCUMULATIVE);
  }

  public static final void releaseClusterLock() throws TechnicalLockException {
    ServerService.dump(" <<<<< released cluster lock: " + LOCK_NAME);
    lock.unlock(LOCK_NAME, "", ServerInternalLocking.MODE_EXCLUSIVE_NONCUMULATIVE);
  }
}
