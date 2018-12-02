/*
 * Copyright (c) 2002 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.ts.jts.ots.CosTransactions.impl;

import com.sap.engine.services.ts.Util;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * This class is implementation of JTA synchronization. It is used in the implemetation of the Control
 * when an OMG synchronization is registered to the coordinator of a JTA transaction
 * the Coordinator implementation creates a wrapper of the OMG synchronization and registeres it in the
 * JTA transaction. This class acts as a proxy to the OMG synchronization
 *
 * @author Iliyan Nenov, ilian.nenov@sap.com
 * @version SAP J2EE Engine 6.30
 */
public class SynchronizationOTSWrapper implements javax.transaction.Synchronization {

  private static final Location LOCATION = Location.getLocation(SynchronizationOTSWrapper.class);

  org.omg.CosTransactions.Synchronization synch = null;

  public SynchronizationOTSWrapper(org.omg.CosTransactions.Synchronization synchParam) {
    synch = synchParam;
  }

  /**
   * Implementation of beforeCompletion method, because this class acts as a proxy
   * to the OMG synchronization the implementation calls beforeCompetion of the OMG synchronization
   */
  public void beforeCompletion() {
    synch.before_completion();
  }

  /**
   * Implementation of afterCompletion method, because this class acts as a proxy
   * to the OMG synchronization the implementation calls afterCompetion of the OMG synchronization
   */
   public void afterCompletion(int statusParam) {
     try {
       synch.after_completion(Util.jta2omgStatus(statusParam));
     } catch (Exception e) {
         /* in some implementations when the commit phase begins Control object is disconnected from the
            ORB when we try to call after_completion we will be thrown org.omg.CORBA.OBJECT_NOT_EXIST
            so to avoid any problems we catch an exception and log it*/
        // made for the reference implementation Sun J2EE disconnects
        // the propagated transaction control
       if (LOCATION.beLogged(Severity.INFO)) {
         LOCATION.infoT("SynchronizationOTSWrapper.afterCompletion(): " + e.toString());
       }
       if (LOCATION.beLogged(Severity.DEBUG)) {
         LOCATION.traceThrowableT(Severity.DEBUG, "SynchronizationOTSWrapper.afterCompletion", e);
       }
     }
  }

}

