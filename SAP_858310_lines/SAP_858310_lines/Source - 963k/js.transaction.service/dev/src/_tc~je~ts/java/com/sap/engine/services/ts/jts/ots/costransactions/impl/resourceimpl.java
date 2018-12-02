/*
 * Copyright (c) 2002 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */

package com.sap.engine.services.ts.jts.ots.CosTransactions.impl;

import com.sap.engine.interfaces.transaction.LocalTxProvider;
import com.sap.engine.services.ts.Log;
import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.exceptions.BaseIllegalStateException;
import com.sap.engine.services.ts.exceptions.ExceptionConstants;
import com.sap.engine.services.ts.exceptions.BaseSystemException;
import com.sap.engine.services.ts.jts.RMMap;
import com.sap.engine.services.ts.jts.ots.OTSTransaction;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

import org.omg.CosTransactions.*;

import javax.resource.ResourceException;
import javax.resource.spi.LocalTransaction;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import java.util.Enumeration;

/**
 * This class is implemetation of OMG Resource,
 * ResourceImpl packs all XAResources enlisted to the OTSTransaction in a special datastructure
 * and only one OMG Resource is registered in the coordinator. This is possible because the SAP J2EE Engine
 * Transaction service implementation uses last resource optimization. The other possible option for this class
 * is to act as a wrapper to a single XAResource but this would bring along superfluous communication. That's why
 * ResourceImpl acts as a packet of XAResources
 *
 * @author Iliyan Nenov, ilian.nenov@sap.com
 * @version SAP J2EE Engine 6.30
 */
public class ResourceImpl extends ResourceImplBase {//$JL-SER$

  private static final Location LOCATION = Location.getLocation(ResourceImpl.class);

  private RMMap rmMap = null;
  private byte[] tidBytes = new byte[16];
  private int status = 0;
  /* This is used if the property is enabled*/
  private LocalTxProvider localResourceReference = null;
  /* This is used if the property is enabled*/
  private LocalTransaction localTX = null;
  /* used only in beforeRollback calling*/
  private OTSTransaction otsTx = null;

  private String txToString;
  private Object[] txToObjectArray;

  /**
   * Creates a new ResourceImpl with a given tidBytes array and an OTSTransaction
   * the reference to the transaction is used when calling beforeRollback method
   *
   * @param _tidBytes
   * @param ots_Tx
   */
  public ResourceImpl(byte[] _tidBytes, OTSTransaction ots_Tx) {
    tidBytes = _tidBytes;
    otsTx = ots_Tx;
  }

  ///////////////////////////////////////////////////////////////////////////////////////////
  //                           Implementation specific methods                             //
  ///////////////////////////////////////////////////////////////////////////////////////////
  /**
   * Enlists a ResourceReference in this OMG Resource.
   * A new LocalTransaction is begun when the method is called
   *
   * @param localRef
   * @throws SystemException
   */
  public void enlistLocalResource(LocalTxProvider localRef) throws SystemException {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "{0} .enlistLocalResource({1})", new Object[]{toString(), objectToString(localRef)});
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000212", "{0} .enlistLocalResource({1})", new Object[]{toString(), objectToString(localRef)});
    }
    if (localResourceReference != null) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
//        LOCATION.logT(Severity.DEBUG, "{0} local resource is already enlisted into Transaction ( {1} ), second enlistLocalResource is not possible.", new Object[]{toString(), objectToString(localResourceReference)});
        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000213", "{0} local resource is already enlisted into Transaction ( {1} ), second enlistLocalResource is not possible.", new Object[]{toString(), objectToString(localResourceReference)});
      }
      throw new BaseSystemException(ExceptionConstants.Second_local_resource, this.toString());
    }
    localTX = (javax.resource.spi.LocalTransaction)localRef.getLocalTransaction();

    try {
      localTX.begin();
    } catch (RuntimeException e) {
      if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "{0} local resource couldn't begin successfuly.", toObjectArray(), e);
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e, "ASJ.trans.000028", "{0} local resource couldn't begin successfully.",  new Object[] { toObjectArray()});
      }
      throw new BaseSystemException(ExceptionConstants.Exception_local_resource_begin, toString(), e);
    } catch (ResourceException e) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
        LOCATION.traceThrowableT(Severity.DEBUG, "{0} local resource couldn't begin successfuly.", toObjectArray(), e);
      }
      throw new BaseSystemException(ExceptionConstants.Exception_local_resource_begin, toString(), e);
    }
    localResourceReference = localRef;     
  }

  /**
   * Returns the local resource
   * Note: works if EnlistLocalResourceInOTS is enabled if not returns null
   *
   * @return an instance to the local resource reference that is enlisted in the transaction
   */
  public LocalTxProvider getLocalResource() {
    if (!TransactionServiceFrame.enableLocalResourceInOTS) {
      return null;
    }
    return localResourceReference;
  }

  /**
   * Enlists a local Resource
   *
   * @param xaRes XAResource instance to enlist
   * @return true if enlisting was successful else returns false
   */
  public boolean enlistResource(XAResource xaRes) {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "{0} .enlistResource({1})", new Object[]{toString(), Log.objectToString(xaRes)});
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000214", "{0} .enlistResource({1})", new Object[]{toString(), Log.objectToString(xaRes)});
    }
    if (rmMap == null) {
      rmMap = new RMMap(tidBytes);
    }
    try {
       return rmMap.enlistResource(xaRes);
    } catch (RuntimeException re) {
        if (LOCATION.beLogged(Severity.WARNING)) {
          LOCATION.traceThrowableT(Severity.WARNING, "{0} unexpected exception in xa start.", toObjectArray(), re);
        }
        return false;
    } catch (XAException xae) {
        if (LOCATION.beLogged(Severity.DEBUG)) {
          LOCATION.traceThrowableT(Severity.DEBUG, "{0} exception in xa start.", toObjectArray(), xae);
        }
        return false;
    }
  } // enlistResource()

  /**
   * Delists a XAResource form this transaction
   *
   * @param xaRes XAResource instance to delist
   * @param flag flag to use in delisting
   * @return returns true if delisting was successful else retrns false
   */
  public boolean delistResource(XAResource xaRes, int flag) {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "{0} .delistResource({1}, {2})", new Object[]{toString(), Log.objectToString(xaRes), Integer.toString(flag)});
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000215", "{0} .delistResource({1}, {2})", new Object[]{toString(), Log.objectToString(xaRes), Integer.toString(flag)});
    }

    if (rmMap != null) {

        try {
            return rmMap.delistResource(xaRes);                 
        } catch (RuntimeException re) {
            if (LOCATION.beLogged(Severity.WARNING)) {
              LOCATION.traceThrowableT(Severity.WARNING, "{0} unexpected exception in xa end.", toObjectArray(), re);
            }
            throw new BaseIllegalStateException(ExceptionConstants.Exception_XAResource_End, re);
        } catch (XAException xae) {
            if (LOCATION.beLogged(Severity.DEBUG)) {
              LOCATION.traceThrowableT(Severity.DEBUG, "{0} exception in xa end.", toObjectArray(), xae);
            }
            throw new BaseIllegalStateException(ExceptionConstants.Exception_XAResource_End, xae);
        }
    }
    throw new BaseIllegalStateException(ExceptionConstants.XAResource_manager_not_found);
  } // delistResource()

  ///////////////////////////////////////////////////////////////////////////////////////////
  //                       Methods from org.omg.Resource interface                         //
  ///////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Prepares the XAResources, first phase of two phase commit protocol
   *
   * @return Vote object that tells the result of preparation Vote.VoteRollback is
   * returned if a XAException occured during the preparation
   * @throws HeuristicMixed
   * @throws HeuristicHazard
   */
  public Vote prepare() throws HeuristicMixed, HeuristicHazard {

    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "{0} .prepare()", toObjectArray());
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000216", "{0} .prepare()", toObjectArray());
    }

    boolean fatalStatus = false;
    boolean xaReadOnly = true;

    if (rmMap != null) {
      for(int i = 0; i < rmMap.getXAResourceCount() && !fatalStatus; i++) {
        try {
          int prepareStatus = rmMap.getResources(i).prepare();
          fatalStatus = (prepareStatus != XAResource.XA_OK && prepareStatus != XAResource.XA_RDONLY);

          if (xaReadOnly && prepareStatus != javax.transaction.xa.XAResource.XA_RDONLY) {
            xaReadOnly = false;
          }
          fatalStatus = (prepareStatus != javax.transaction.xa.XAResource.XA_OK && prepareStatus != javax.transaction.xa.XAResource.XA_RDONLY);
        } catch (RuntimeException re) {
          if (LOCATION.beLogged(Severity.WARNING)) {
            LOCATION.traceThrowableT(Severity.WARNING, "{0} unexpected exception in xa prepare.", toObjectArray(), re);
          }
          return Vote.VoteRollback;
        } catch (javax.transaction.xa.XAException xaE) {
          if (LOCATION.beLogged(Severity.DEBUG)) {
            LOCATION.traceThrowableT(Severity.DEBUG, "{0} exception xa prepare.", toObjectArray(), xaE);
          }
          return Vote.VoteRollback;
        }
      }
    }

    if (fatalStatus) {
      throw new HeuristicMixed("SAP J2EE ENGINE | TRANSACTION SERVICE | detected a Resource Manager ERROR | TRANSACTION [{0}] WAS ROLLED BACK: In prepare phase of two phase commit protocol one or more XAResource returned unspecified in the JTA1.0.1 specification prepare status! Check the Resource Manager!");
    }

    if (xaReadOnly) {
      return Vote.VoteReadOnly;
    }

    return Vote.VoteCommit;

  }

  /**
   * Rollback the XAResources
   *
   * @throws HeuristicCommit
   * @throws HeuristicMixed
   * @throws HeuristicHazard
   */
  public void rollback() throws HeuristicCommit, HeuristicMixed, HeuristicHazard {

    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "{0} .rollback()", toObjectArray());
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000217", "{0} .rollback()", toObjectArray());
    }


    if (status != Status.STATUS_PREPARING) {
      for(Enumeration enumeration = otsTx.getSynchronizations().elements(); enumeration.hasMoreElements();) {
        Object synch = enumeration.nextElement();
        if (synch instanceof SynchronizationJTAWrapper) {
          try {
            ((SynchronizationJTAWrapper)synch).beforeRollback();
          } catch (RuntimeException re) {
            if (LOCATION.beLogged(Severity.WARNING)) {
              LOCATION.traceThrowableT(Severity.WARNING, "{0} unxpected exception in beforeRollback.", toObjectArray(), re);
            }
          }
        }
      }
    }

    status = Status.STATUS_ROLLING_BACK;
    Exception exception = null;
    try {
      if (localTX != null) {
        try {
          localTX.rollback();
        } catch (RuntimeException re) {
          if (LOCATION.beLogged(Severity.WARNING)) {
            LOCATION.traceThrowableT(Severity.WARNING, "{0} unxpected exception in local resource rollback.", toObjectArray(), re);
          }
          exception = re;
        } catch (ResourceException re) {
          if (LOCATION.beLogged(Severity.DEBUG)) {
            LOCATION.traceThrowableT(Severity.DEBUG, "{0} exception in local resource rollback.", toObjectArray(), re);
          }
          exception = re;
        }
      }

      if (rmMap != null) {
        for(int rmIndex = 0; rmIndex < rmMap.getXAResourceCount(); rmIndex++) {
          try {
            rmMap.getResources(rmIndex).rollback();
          } catch (RuntimeException re) {
            if (LOCATION.beLogged(Severity.WARNING)) {
              LOCATION.traceThrowableT(Severity.WARNING, "{0} unxpected exception in xa resource rollback.", toObjectArray(), re);
            }
            exception = re;
          } catch (XAException xae) {
            if (LOCATION.beLogged(Severity.DEBUG)) {
              LOCATION.traceThrowableT(Severity.DEBUG, "{0} exception in xa resource rollback.", toObjectArray(), xae);
            }
            exception = xae;
          }
        }
      }
    } finally {
      status = Status.STATUS_ROLLEDBACK;
    }

    if (exception != null) {
      throw new HeuristicMixed("Exception in rollback : " + exception.toString());
    }

  }

  /**
   * Commits the XAResources
   *
   * @throws NotPrepared
   * @throws HeuristicRollback
   * @throws HeuristicMixed
   * @throws HeuristicHazard
   */
  public void commit() throws NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard {

    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "{0} .commit()", toObjectArray());
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000218", "{0} .commit()", toObjectArray());
    }

    status = Status.STATUS_COMMITTING;
    Exception exception = null;
    try {
      if (localTX != null && TransactionServiceFrame.enableLocalResourceInOTS) {
        localTX.commit();
      }

      if (rmMap != null) {
        for(int i = 0; i < rmMap.getXAResourceCount(); i++) {
          try {
            rmMap.getResources(i).commitTwoPhase();
          } catch (RuntimeException re) {
            if (LOCATION.beLogged(Severity.ERROR)) {
//              LOCATION.traceThrowableT(Severity.ERROR, "{0} xa resource couldn't commit successfuly.", toObjectArray(), re);
              SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,re, "ASJ.trans.000029", "{0} xa resource couldn't commit successfully.",  toObjectArray());
            }
            exception = re;
          } catch (XAException xaE) {
            if (LOCATION.beLogged(Severity.ERROR)) {
//              LOCATION.traceThrowableT(Severity.ERROR, "{0} xa resource couldn't commit successfuly.", toObjectArray(), xaE);
              SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,xaE, "ASJ.trans.000053", "{0} xa resource couldn't commit successfully.");
            }
            exception = xaE;
          }
        }
      }
    } catch (RuntimeException re) {
      if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "{0} local resource couldn't commit successfuly.", toObjectArray(), re);
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,re, "ASJ.trans.000054", "{0} local resource couldn't commit successfully.", toObjectArray());
      }
      throw new HeuristicMixed("commit of javax.resource.spi.LocalTrancaction fail " + re.toString());
    } catch (javax.resource.ResourceException re) {
      if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "{0} local resource couldn't commit successfuly.", toObjectArray(), re);
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,re, "ASJ.trans.000055", "{0} local resource couldn't commit successfully.",  toObjectArray());
      }
      throw new HeuristicMixed("commit of javax.resource.spi.LocalTrancaction fail " + re.toString());
    } finally {
      status = Status.STATUS_COMMITTED; // presents in version #9+ do not remove !!!
      if (exception != null) {
        throw new HeuristicMixed("One or more XAResources were not committed because of an XAException in the second commit phase. XAException is: " + exception.getMessage());
      }
    }
  }

  /**
   * Commits in one phase all resources without preparation
   *
   * @throws HeuristicHazard
   */
  public void commit_one_phase() throws HeuristicHazard {

    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "{0} .commit_one_phase()", toObjectArray());
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000219", "{0} .commit_one_phase()", toObjectArray());
    }

    if (localTX != null) {
      try {
        localTX.commit();
      } catch (RuntimeException e) {
        if (LOCATION.beLogged(Severity.ERROR)) {
//          LOCATION.traceThrowableT(Severity.ERROR, "{0} local resource couldn't commit successfuly.", toObjectArray(), e);
          SimpleLogger.traceThrowable(Severity.WARNING,LOCATION,e, "ASJ.trans.000056", "{0} local resource couldn't commit successfully.", toObjectArray());
        }
      } catch (ResourceException e) {
        if (LOCATION.beLogged(Severity.ERROR)) {
//          LOCATION.traceThrowableT(Severity.ERROR, "{0} local resource couldn't commit successfully.", toObjectArray(), e);
          SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e, "ASJ.trans.000057", "{0} local resource couldn't commit successfully.", toObjectArray());
        }
      }
    }

    if (rmMap != null) {
      if (rmMap.getXAResourceCount() != 1) {
        try {
          rmMap.getResources(0).commitOnePhase();
        } catch (RuntimeException re) {
          if (LOCATION.beLogged(Severity.ERROR)) {
//            LOCATION.traceThrowableT(Severity.ERROR, "{0} xa resource couldn't commit successfuly.", toObjectArray(), re);
            SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,re, "ASJ.trans.000115", "{0} xa resource couldn't commit successfully.", toObjectArray());
          }
          throw new HeuristicHazard("SAP J2EE ENGINE|TRANSACTION SERVICE| XAException occured during commit_one_phase on an OMG Resource internal message: " + re.getMessage());
        } catch (XAException xae) {
          if (LOCATION.beLogged(Severity.ERROR)) {
//            LOCATION.traceThrowableT(Severity.ERROR, "{0} xa resource couldn't commit successfuly.", toObjectArray(), xae);
            SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,xae, "ASJ.trans.000116", "{0} xa resource couldn't commit successfully.", toObjectArray());
          }
          throw new HeuristicHazard("SAP J2EE ENGINE|TRANSACTION SERVICE| XAException occured during commit_one_phase on an OMG Resource internal message: " + xae.getMessage());
        }
      } else {
        if (LOCATION.beLogged(Severity.ERROR)) {
//          LOCATION.logT(Severity.ERROR, "{0} commit_one_phase on an OMG Resource is not alowed if there are more than one resource manager.", toObjectArray());
          SimpleLogger.trace(Severity.ERROR,LOCATION,"ASJ.trans.000117", "{0} commit_one_phase on an OMG Resource is not alowed if there are more than one resource manager.", toObjectArray());
        }
        throw new HeuristicHazard("SAP J2EE ENGINE|TRANSACTION SERVICE| commit_one_phase on an OMG Resource is not alowed if there are more than one resource manager");
      }
    }
  }

  /**
   * Forgets the XAResources which are enlisted here
   */
  public void forget() {

    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "{0} .forget()", toObjectArray());
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000220", "{0} .forget()", toObjectArray());
    }

    if (rmMap != null) {
      for(int i = 0; i < rmMap.getXAResourceCount(); i++) {
        try {
          rmMap.getResources(i).forget();
        } catch (RuntimeException re) {
          if (LOCATION.beLogged(Severity.ERROR)) {
//            LOCATION.traceThrowableT(Severity.ERROR, "{0} xa resource couldn't forget successfully.", toObjectArray(), re);
            SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,re, "ASJ.trans.000120", "{0} xa resource couldn't forget successfully.",toObjectArray());
          }
        } catch (XAException xae) {
          if (LOCATION.beLogged(Severity.ERROR)) {
//            LOCATION.traceThrowableT(Severity.ERROR, "{0} xa resource couldn't forget successfully.", toObjectArray(), xae);
            SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,xae, "ASJ.trans.000121", "{0} xa resource couldn't forget successfully.", toObjectArray());
          }
        }
      }
    }
  }

  public String toString() {
    if (txToString == null) {
      StringBuffer buffer = new StringBuffer();
      buffer.append("SAP J2EE Engine OTS Transaction : [");
      for(int i = 8; i < tidBytes.length; i++) {
        buffer.append(Integer.toHexString(tidBytes[i]));
      }
      buffer.append("]");
      txToString = buffer.toString();
    }
    return txToString;
  }

  private static final String objectToString(Object o) {
    return o.getClass().getName() + "@" + Integer.toHexString(o.hashCode());
  }

  private Object[] toObjectArray() {
    if (txToObjectArray == null) {
      txToObjectArray = new Object[1];
      txToObjectArray[0] = toString();
    }
    return txToObjectArray;
  }

}
