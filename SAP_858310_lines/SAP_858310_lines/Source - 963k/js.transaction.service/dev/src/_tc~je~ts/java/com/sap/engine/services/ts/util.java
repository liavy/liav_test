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
package com.sap.engine.services.ts;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.sap.tc.logging.Severity;
import com.sap.tc.logging.Location;

/**
 * Provides some utils needed by the Transaction Service classes
 *
 * @author Iliyan Nenov, ilian.nenov@sap.com
 * @version SAP J2EE Engine 6.30
 */
public class Util {

  private static final Location LOCATION = Location.getLocation(Util.class);
  /**
   * Converts javax.transaction.Status to org.omg.CosTransactions.Status
   *
   * @param jtaStatus int value representing a JTA status
   * @return OMG Status object representing OMG equivalent for the given the JTA status
   */
  public static org.omg.CosTransactions.Status jta2omgStatus(int jtaStatus) {
    switch (jtaStatus) {
      case (javax.transaction.Status.STATUS_ACTIVE): {
        return org.omg.CosTransactions.Status.StatusActive;
      }
      case (javax.transaction.Status.STATUS_MARKED_ROLLBACK): {
        return org.omg.CosTransactions.Status.StatusMarkedRollback;
      }
      case (javax.transaction.Status.STATUS_COMMITTING): {
        return org.omg.CosTransactions.Status.StatusCommitting;
      }
      case (javax.transaction.Status.STATUS_COMMITTED): {
        return org.omg.CosTransactions.Status.StatusCommitted;
      }
      case (javax.transaction.Status.STATUS_ROLLING_BACK): {
        return org.omg.CosTransactions.Status.StatusRollingBack;
      }
      case (javax.transaction.Status.STATUS_ROLLEDBACK): {
        return org.omg.CosTransactions.Status.StatusRolledBack;
      }
      case (javax.transaction.Status.STATUS_PREPARED): {
        return org.omg.CosTransactions.Status.StatusPrepared;
      }
      case (javax.transaction.Status.STATUS_PREPARING): {
        return org.omg.CosTransactions.Status.StatusPreparing;
      }
      case (javax.transaction.Status.STATUS_NO_TRANSACTION): {
        return org.omg.CosTransactions.Status.StatusNoTransaction;
      }
      case (javax.transaction.Status.STATUS_UNKNOWN):
      default: {
        return org.omg.CosTransactions.Status.StatusUnknown;
      }
    }
  }

  /**
   * Converts org.omg.CosTransactions.Status to javax.transaction.Status
   *
   * @param omgStatus OMG status object
   * @return int value representing JTA equivalent for the given the OMG status
   */
  public static int omg2jtaStatus(org.omg.CosTransactions.Status omgStatus) {
    if (omgStatus == org.omg.CosTransactions.Status.StatusActive) {
      return javax.transaction.Status.STATUS_ACTIVE;
    }

    if (omgStatus == org.omg.CosTransactions.Status.StatusMarkedRollback) {
      return javax.transaction.Status.STATUS_MARKED_ROLLBACK;
    }

    if (omgStatus == org.omg.CosTransactions.Status.StatusCommitting) {
      return javax.transaction.Status.STATUS_COMMITTING;
    }

    if (omgStatus == org.omg.CosTransactions.Status.StatusCommitted) {
      return javax.transaction.Status.STATUS_COMMITTED;
    }

    if (omgStatus == org.omg.CosTransactions.Status.StatusRollingBack) {
      return javax.transaction.Status.STATUS_ROLLING_BACK;
    }

    if (omgStatus == org.omg.CosTransactions.Status.StatusRolledBack) {
      return javax.transaction.Status.STATUS_ROLLEDBACK;
    }

    if (omgStatus == org.omg.CosTransactions.Status.StatusPrepared) {
      return javax.transaction.Status.STATUS_PREPARED;
    }

    if (omgStatus == org.omg.CosTransactions.Status.StatusPreparing) {
      return javax.transaction.Status.STATUS_PREPARING;
    }

    if (omgStatus == org.omg.CosTransactions.Status.StatusNoTransaction) {
      return javax.transaction.Status.STATUS_NO_TRANSACTION;
    }

    int st = javax.transaction.Status.STATUS_UNKNOWN;
    return st;
  }

  
  public static String getStackTrace(Throwable t) {
	  StringWriter stringWriter = new StringWriter();
	  t.printStackTrace(new PrintWriter(stringWriter, true));
	  return stringWriter.toString();
  }
  
}

