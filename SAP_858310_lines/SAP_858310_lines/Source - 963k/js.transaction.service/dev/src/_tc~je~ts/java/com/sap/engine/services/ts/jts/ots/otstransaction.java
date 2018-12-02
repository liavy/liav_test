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

package com.sap.engine.services.ts.jts.ots;

import com.sap.engine.interfaces.transaction.AfterBeginSynchronizationExtension;
import com.sap.engine.interfaces.transaction.LocalTxProvider;
import com.sap.engine.interfaces.transaction.TransactionExtension;
import com.sap.engine.lib.lang.Convert;
import com.sap.engine.services.ts.Log;
import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.Util;
import com.sap.engine.services.ts.exceptions.BaseSystemException;
import com.sap.engine.services.ts.exceptions.ExceptionConstants;
import com.sap.engine.services.ts.exceptions.BaseIllegalStateException;
import com.sap.engine.services.ts.jta.impl2.TXR_TransactionManagerImpl;
import com.sap.engine.services.ts.jta.statistics.TransactionStatistics;
import com.sap.engine.services.ts.jts.ots.CosTransactions.impl.ResourceImpl;
import com.sap.engine.services.ts.jts.ots.CosTransactions.impl.SynchronizationJTAWrapper;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.Inactive;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.SynchronizationUnavailable;
import org.omg.CosTransactions.Terminator;
import org.omg.CosTransactions.otid_t;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.xa.XAResource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.zip.CRC32;
import java.util.HashMap;


/**
 * This class is a representation of propagated transaction from another server
 * It implements com.sap.engine.interfaces.transaction.TransactionExtension interface and is inconspicuous
 * for the containers. It supports the full functionality of a JTA transaction even enlisting
 * local resource but in fact this class acts as a proxy to the Control that created this transaction
 *
 * @author Iliyan Nenov, ilian.nenov@sap.com
 * @version SAP J2EE Engine 6.30
 */
public class OTSTransaction implements TransactionExtension {

  private static final Location LOCATION = Location.getLocation(OTSTransaction.class);


  private String txToString;
  private byte[] tidBytes = null;
  private ResourceImpl omgResource = null;
  private List xaList = new ArrayList();
  public PropagationContext pgContext = null;
  private Terminator terminator = null;
  private Coordinator coordinator = null;
  private otid_t otid = null;
  private int status = 0;
  private Vector syns = new Vector();
  private Object[] txToObjectArray;
  /*just a storage for objects*/
  private HashMap associatedObjects = null;


  /**
   * Constructs a new OTSTransaction
   *
   * @param pgContextParam a propagation context
   */
  public OTSTransaction(PropagationContext pgContextParam) {

    pgContext = pgContextParam;

    if (pgContext.current == null) {
//      LOCATION.logT(Severity.ERROR, "OTSTransaction object in transaction service is created with null \"current\" in the PropagationContext.");
      SimpleLogger.trace(Severity.ERROR,LOCATION, "ASJ.trans.000052", "OTSTransaction object in transaction service is created with null \"current\" in the PropagationContext.");
      throw new BaseIllegalStateException(ExceptionConstants.Current_Is_Null);
    }

    terminator = pgContext.current.term;
    coordinator = pgContext.current.coord;
    otid = pgContext.current.otid;
    tidBytes = new byte[otid.tid.length];
    for(int i = 0; i < otid.tid.length; i++) {
      tidBytes[i] = otid.tid[i];
    }
    status = javax.transaction.Status.STATUS_ACTIVE;
  }

  /**
   * Commits this OTS Transaction the implementation calls commit() method of the terminator
   *
   * @throws RollbackException never thrown by this implementation because it
   * @throws HeuristicMixedException thrown by terminator.commit()
   * @throws HeuristicRollbackException thrown by terminator.commit()
   * @throws java.lang.SecurityException never thrown by this implementation
   * @throws SystemException never thrown by this implementation
   */
  public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, java.lang.SecurityException, SystemException {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "{0}.commit", toObjectArray());
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000238", "{0}.commit", toObjectArray());
    }

    status = javax.transaction.Status.STATUS_COMMITTED;
    try {
      terminator.commit(false); // the transaction is not a local one. terminator is a Stub
    } catch (HeuristicMixed hm) {
      if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "{0}", toObjectArray(), hm);
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,hm, "ASJ.trans.000058", "{0}", toObjectArray());
      }
    } catch (HeuristicHazard hh) {
      if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "{0}", toObjectArray(), hh);     
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,hh, "ASJ.trans.000059", "{0}", toObjectArray());
      }
    } finally {
    	TransactionStatistics.transactionCommitted();
    }
  }

  /**
   * Delists a XAResource to this OTS Transaction
   *
   * @param xaRes a reference to XAResource
   * @param flag flag used in delist
   * @return true if the XAResource is ended otherwise false or an exception will be thrown
   */
  public boolean delistResource(XAResource xaRes, int flag) {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "{0}.delistResource({1}, {2})", new Object[]{toString(), Log.objectToString(xaRes), Integer.toString(flag)});
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000239", "{0}.delistResource({1}, {2})", new Object[]{toString(), Log.objectToString(xaRes), Integer.toString(flag)});
    }
    return omgResource.delistResource(xaRes, flag);
  }

  /**
   * Enlists a XAResource
   *
   * @param xaRes a reference to the XAResource
   * @return true if the XAResource is started else returns false
   */
  public boolean enlistResource(XAResource xaRes) {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "{0}.enlistResource({1})", new Object[]{toString(), Log.objectToString(xaRes)});
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000240", "{0}.enlistResource({1})", new Object[]{toString(), Log.objectToString(xaRes)});
    }
    if (omgResource == null) {
      omgResource = new ResourceImpl(tidBytes, this);
      try {
        boolean res = omgResource.enlistResource(xaRes);
        coordinator.register_resource(omgResource); // <- for this purpose OTSTransaction MUST implement org.omg.Resource
        return res;
      } catch (Inactive i) {
        if (LOCATION.beLogged(Severity.ERROR)) {
//          LOCATION.traceThrowableT(Severity.ERROR, "{0}", toObjectArray(), i);
          SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,i, "ASJ.trans.000060", "{0}", toObjectArray());
        }
      }
    }

    return omgResource.enlistResource(xaRes);
  }

  public boolean enlistResource(int rmID, XAResource xaRes) throws RollbackException, IllegalStateException, SystemException{
		// TODO implement. This is just for compilation.
		return enlistResource(xaRes);
  }
  
  /**
   * Returns the status of this transaction
   *
   * @return status as int value
   */
  public int getStatus() {
    if (status == javax.transaction.Status.STATUS_ROLLEDBACK || status == javax.transaction.Status.STATUS_COMMITTED) {
      return javax.transaction.Status.STATUS_NO_TRANSACTION;
    }
    return status;
  }
  
  /**
   * Register a synchronization object for the transaction currently
   * associated with the calling thread. The transction manager invokes
   * the beforeCompletion method prior to starting the transaction
   * commit process. After the transaction is completed, the transaction
   * manager invokes the afterCompletion method. This method does not check 
   * if transaction is marked for rollback. This method can be used when 
   * synchronization must be registered into transaction that is marked for rollback
   *
   * @param newSynchronization The Synchronization object for the transaction associated
   *    with the target object
   *
   * @exception RollbackException  this exception is not thrown
   *
   * @exception IllegalStateException Thrown if the transaction in the
   *    target object is in prepared state or the transaction is inactive.
   *
   * @exception SystemException Thrown if the transaction manager
   *    encounters an unexpected error condition
   *
   */
  public void registerSynchronizationWithoutStatusChecks(Synchronization newSynchronization) throws RollbackException, IllegalStateException, SystemException{  
  	 registerSynchronization(newSynchronization);  
  }
  
  /**
   * Registers a JTA synchronization to this OTS Transaction.
   * The synchronization is a local object, a wrapper of the JTA synchronization is created and
   * registered to the Coordinator of the transaction.
   *
   * @param sync a reference to the synchronization
   *   
   * @exception RollbackException Thrown to indicate that
   *    the transaction has been marked for rollback only.
   *
   * @exception IllegalStateException Thrown if the transaction in the
   *    target object is in prepared state or the transaction is inactive.
   *
   * @exception SystemException Thrown if the transaction manager
   *    encounters an unexpected error condition   * 
   */
  public void registerSynchronization(Synchronization sync) throws RollbackException, IllegalStateException, SystemException {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "{0}.registerSynchronization({1})", new Object[]{toString(), Log.objectToString(sync)});
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000241", "{0}.registerSynchronization({1})", new Object[]{toString(), Log.objectToString(sync)});
    }
    
    if(sync instanceof AfterBeginSynchronizationExtension){
    	try{
    		((AfterBeginSynchronizationExtension)sync).afterBegin();
    	}catch (Exception e) {
    		String exMess = "Exception : " + e + "was thrown from " + sync + ".afterBegin() method. This synchronization is not registered into transaction.";
    	    if (LOCATION.beLogged(Severity.WARNING)) {
//    	       LOCATION.logT(Severity.WARNING, exMess);
    	       SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000242", "Exception : {0}was thrown from {1}.afterBegin() method. This synchronization is not registered into transaction.",  new Object[] { e,sync});
    	    }    		
    		SystemException sysEx = new SystemException(exMess);
    		sysEx.initCause(e);
			throw sysEx;
		}
    }    
    
    try {
      SynchronizationJTAWrapper synchWrap = new SynchronizationJTAWrapper(sync);
      coordinator.register_synchronization(synchWrap);
      syns.add(synchWrap);
    } catch (Inactive i) {
      if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "{0}", toObjectArray(), i);
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,i, "ASJ.trans.000061", "{0}", toObjectArray());
      }
    } catch (SynchronizationUnavailable su) {
      if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "{0}", toObjectArray(), su);
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,su, "ASJ.trans.000062", "{0}", toObjectArray());
      }
    }
  }

  /**
   * Rollback this transaction. Calls rollback method of the terminator
   */
  public void rollback() {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "{0}.rollback", toObjectArray());
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000243", "{0}.rollback");
    }
    status = javax.transaction.Status.STATUS_ROLLEDBACK;
    try{
      terminator.rollback();
    } finally {
    	TransactionStatistics.transactionRolledback();

    }
  }

  /**
   * Sets the status of the or
   */
  public void setRollbackOnly() {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "{0}.setRollbackOnly", toObjectArray());
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000244", "{0}.setRollbackOnly");
    }
    status = Status.STATUS_MARKED_ROLLBACK;
    try {
      coordinator.rollback_only();
    } catch (Inactive i) {
      if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "{0}", toObjectArray(), i);
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,i, "ASJ.trans.000063", "{0}",toObjectArray());
      }
    }
  }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                               From com.sap.engine.interfaces.transaction.TransactionExtension interface                  //
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * This method checks if there are any OMG resources registered to this
   * OTS Transaction
   *
   * @return true if there are any else returns false
   */
  public boolean isEmpty() {
    if (omgResource != null && omgResource.getLocalResource() != null) {
      return false;
    }
    return true;
  }

  /**
   * This is our "Precious". You have a Transaction which is propagated from another server
   * SAP J2EE Engine created an instance of OTSTransaction which allows you to enlist a LocalResource
   * when a commit request is recieved this local resource will be commited too.
   *
   * @param localRef a reference to the local resource
   * @throws SystemException if the coordinator of the transaction is inactive
   */
  public void enlistLocalResource(LocalTxProvider localRef) throws SystemException {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "{0}.enlistLocalResource({1})", new Object[]{toString(), Log.objectToString(localRef)});
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000245", "{0}.enlistLocalResource({1})", new Object[]{toString(), Log.objectToString(localRef)});
    }
    if (TransactionServiceFrame.enableLocalResourceInOTS) {
    	boolean toRegisterResource = false;
      if (omgResource == null) {
        omgResource = new ResourceImpl(tidBytes, this);
        toRegisterResource = true;
      }
      omgResource.enlistLocalResource(localRef);
      if(toRegisterResource){
	      try {
	        coordinator.register_resource(omgResource);
	      } catch (Inactive i) {
	        if (LOCATION.beLogged(Severity.ERROR)) {
//	          LOCATION.traceThrowableT(Severity.ERROR, "{0}", toObjectArray(), i);
	          SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,i, "ASJ.trans.000064", "{0}", toObjectArray());
	        }
	        throw new SystemException(i.getMessage());
	      }
      }
    } else {
//      LOCATION.logT(Severity.ERROR, "{0} enlisting local resources in OTS transaction is not alowed. Check {1} property of Transaction Service.", new Object[]{toString(), "ENABLE_LOCAL_RESOURCE_IN_OTS"});
      SimpleLogger.trace(Severity.ERROR,LOCATION, "ASJ.trans.000065", "{0} enlisting local resources in OTS transaction is not alowed. Check {1} property of Transaction Service.", new Object[]{toString(), "ENABLE_LOCAL_RESOURCE_IN_OTS"});
      throw new BaseSystemException(ExceptionConstants.Enlist_of_local_resource_in_OTS_Transaction_Not_Allowed, "ENABLE_LOCAL_RESOURCE_IN_OTS");
    }
  }

  /**
   * Returns a reference to local resource
   *
   * @return reference to the local resource
   */
  public LocalTxProvider getLocalResource() {
    if (omgResource != null) {
      return omgResource.getLocalResource();
    }
    return null;
  }

  
  public List getXAResourceConnections(){
		return xaList;	  
  }  
  
  /**
   * Checks if this transaction is alive
   * Transaction is not alive iff it has one of these statuses
   * STATUS_NO_TRANSACTION, STATUS_COMMITTED, STATUS_ROLLEDBACK, STATUS_UNKNOWN
   *
   * @return true if is alive else false
   */
  public boolean isAlive() {
    if (status == Status.STATUS_NO_TRANSACTION || status == Status.STATUS_COMMITTED || status == Status.STATUS_ROLLEDBACK || status == Status.STATUS_UNKNOWN) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Retuerns a long representation of last 8 bytes of tidBytes identificator of the transaction.
   * tidBytes is an unique byte array for each transaction generated by the Transaction Manager.
   * tidBytes is complicately generated. It guarantees uniqueness of the transaction even if
   * the server is rebooted.
   *
   * @return
   */
  public long getID() {
    return Convert.byteArrToLong(tidBytes, 8);//return id;
  }

  /**
   * Returns otid object which is received with the propagation context that this transaction was creqated
   *
   * @return otid object
   */
  public otid_t get_otid() {
    return otid;
  }

  /**
   * Returns tidBytes identificator of this transaction
   *
   * @return the byte array that identifies this transaction
   */
  public byte[] getTIDbytes() {
    return tidBytes;
  }
  
  public void associateObjectWithTransaction(Object sid, Object obj) throws SystemException{
  	if (associatedObjects == null) {
  	  associatedObjects = new HashMap();
  	}  	
    if (associatedObjects.get(sid) != null){
      throw new BaseSystemException(ExceptionConstants.Associated_object_alwready_exist, toString()); 
    }    
    associatedObjects.put(sid,obj);
  }
  
  public Object getAssociatedObjectWithTransaction(Object sid){
    if(associatedObjects == null){
      return null;
    }    
    return associatedObjects.get(sid);
  }
  
  
  /**
   * Checks for equivalence this transaction and an object passed as paramenter
   * overrides equal method in java.lang.Object
   *
   * @param obj an object to test with
   * @return true if the parameter is an instance of OTSTransaction nad its tidBytes
   * is the same with this transaction own tidBytes else returns false
   */
  public boolean equals(Object obj) {
    if (obj instanceof OTSTransaction) {
      return Arrays.equals(((OTSTransaction)obj).tidBytes, tidBytes);
    } else {
      return false;
    }
  }

  /**
   * Overiden java.lang.Object hashCode method
   * the method creates a CRC32 object with the tidBytes and returns the lower 4 bytes of crc.gerValue() as int
   *
   * @return an int which guarantees low colision
   */
  public int hashCode() {
    CRC32 crc = new CRC32();
    crc.update(tidBytes);
    return (int)crc.getValue();
  }

  public Vector getSynchronizations() {
    return syns;
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

  private Object[] toObjectArray() {
    if (txToObjectArray == null) {
      txToObjectArray = new Object[1];
      txToObjectArray[0] = toString();
    }
    return txToObjectArray;
  }

}
