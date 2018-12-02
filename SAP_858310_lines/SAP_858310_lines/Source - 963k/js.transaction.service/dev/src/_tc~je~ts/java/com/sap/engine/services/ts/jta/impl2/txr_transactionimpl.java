package com.sap.engine.services.ts.jta.impl2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.resource.ResourceException;
import javax.resource.spi.LocalTransaction;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.HeuristicCommit;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicRollback;
import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.Vote;

import com.sap.engine.interfaces.resourcecontext.ResourceContext;
import com.sap.engine.interfaces.resourcecontext.ResourceContextException;
import com.sap.engine.interfaces.resourceset.ResourceSet;
import com.sap.engine.interfaces.transaction.AfterBeginSynchronizationExtension;
import com.sap.engine.interfaces.transaction.LocalTxProvider;
import com.sap.engine.interfaces.transaction.SAPHeuristicHazardException;
import com.sap.engine.interfaces.transaction.SynchronizationExtension;
import com.sap.engine.interfaces.transaction.SynchronizationPriorityExtension;
import com.sap.engine.interfaces.transaction.XAResourceRMWrapper;
import com.sap.engine.lib.util.LinkedList;
import com.sap.engine.services.ts.Log;
import com.sap.engine.services.ts.LogUtil;
import com.sap.engine.services.ts.TransactionContextObject;
import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.Util;
import com.sap.engine.services.ts.exceptions.BaseIllegalStateException;
import com.sap.engine.services.ts.exceptions.BaseRollbackException;
import com.sap.engine.services.ts.exceptions.BaseSystemException;
import com.sap.engine.services.ts.exceptions.ExceptionConstants;
import com.sap.engine.services.ts.exceptions.TimeOutIsStoppedException;
import com.sap.engine.services.ts.facades.timer.TimeoutListener;
import com.sap.engine.services.ts.jta.TransactionInternalExtension;
import com.sap.engine.services.ts.jta.statistics.SingleTransactionStatistic;
import com.sap.engine.services.ts.jta.statistics.TransactionStatistics;
import com.sap.engine.services.ts.jts.TransactionServiceImpl;
import com.sap.engine.services.ts.jts.ots.CosTSPortability.impl.SenderReceiverImpl;
import com.sap.engine.services.ts.jts.ots.CosTransactions.impl.ControlImpl;
import com.sap.engine.services.ts.jts.ots.CosTransactions.impl.TransactionKey;
import com.sap.engine.services.ts.recovery.PTLProcessor;
import com.sap.engine.services.ts.recovery.PendingRMInfo;
import com.sap.engine.services.ts.recovery.PendingTXData;
import com.sap.engine.services.ts.tlog.InvalidRMIDException;
import com.sap.engine.services.ts.tlog.InvalidTransactionClassifierID;
import com.sap.engine.services.ts.tlog.TLog;
import com.sap.engine.services.ts.tlog.TLogFullException;
import com.sap.engine.services.ts.tlog.TLogIOException;
import com.sap.engine.services.ts.tlog.TransactionRecordImpl;
import com.sap.engine.services.ts.utils.ByteArrayUtils;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LoggingUtilities;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

public class TXR_TransactionImpl implements TransactionInternalExtension, TimeoutListener, SingleTransactionStatistic {


	private static final Location LOCATION = Location.getLocation(TXR_TransactionImpl.class);	
	private static final String UNKNOWN_LOCAL_RM_PREFIX = "Local RM "; 
	/*just a storage for objects which are associated with transaction*/
	private HashMap<Object,Object> associatedObjects = null;
    /* local transaction for this transaction */
	private LocalTransaction localTX = null;
	/* local resource reference for this transaction*/
	private LocalTxProvider localResourceReference = null;	
	/* the status of this transaction */
	protected int status = 0;
	/* used to trace invocation of setRollbackOnly()*/
	private Throwable setRollbackPosition = null;	
	/*This list keeps all wrappers od XAResource which are enlisted into this transaction*/
	protected ArrayList<XAResourceWrapper> xaResourceWrapperList = null;
	/*This is first Xid which is created for this transaction. The XID will be created during first XAResource is enlisted.*/
	private SAPXidImpl mainXID = null;
	protected long transactionSequenceNumber = -1;
	private long transactionBirthTime = 0;
	private int transactionClassifierID = 0;// 0 means that transaction classifier is not set for this transaction
	private byte[] transactionClassifierByteArray = null; // null means that transaction classifier is not set for this transaction
	private boolean rejectSetTransactionClassifier = false;// will be set to true when first XAResource is enlisted.
	private boolean transactionClassifierAlreadySet = false; //
	
	private List xaConnectionsList = new ArrayList();
    /*TransactionManager instance which is responsible for management of this Transaction.*/
	protected TXR_TransactionManagerImpl tm = null;
	/* synchronizations registered to this transaction*/
	protected ArrayList<Synchronization> synchronizations = new ArrayList<Synchronization>();
    /* this pointer is used to ensure that on each synchronization beforeCompletion or beforeRollback is called, and only one of the methods is called.*/
	private int beforeCompletionCalledPointer = 0;  
    /* Cached listeners for beforeCompletion or beforeRollback Calls*/
	private Synchronization[] listeners = null;
	private ArrayList<Synchronization> newSynchronizationListeners = null;// This list is used to call beforeCompletion() method on synchronization objects that are registered during beforeCompletion call of other synchronization objects. 
	/*
	 * Control to this transaction if it is propagated to another server
	 * this object is null if the transaction is not propagated
	 */
	protected ControlImpl txControl = null;
	/* Flag which shows if the transaction is a timeout listener */
	private boolean timeoutListener = false;
	private Object associatedObject = null;
	/*Transaction classifier*/
	private String transactionClassifier = "";
	private String resultingTxClassifier = null;
	private String dcName = null;
	private TLog tLog = null;
	protected int numberOfPreparedAndRecoverableRMs = 0;
	private boolean DeleteTxRecordAtTheEnd = false;
	private boolean removeSynchronizationStackWhenCompleted = false;
	// used for transaction statistics
	private boolean isTimeouted = false;
	private boolean isAbandoned = false;
	private boolean isHeuristicallyCompleted = false;
	protected boolean isRolledbackByApplication = false;
	private boolean isRolledbackBecauseOfRMError = false;
	private ArrayList<String> failedRMNames = new ArrayList<String>();
    private ArrayList<PendingRMInfo> rmInfos = null;
	protected String txToString;
	
	public TXR_TransactionImpl(TXR_TransactionManagerImpl tmImpl, long txSequenceNumber, boolean timeoutListener){
	    status = Status.STATUS_ACTIVE;
	    this.timeoutListener = timeoutListener;
		this.tm = tmImpl;		
		this.transactionSequenceNumber = txSequenceNumber;
		this.transactionBirthTime = System.currentTimeMillis();
	}
	
    public void associateObjectWithTransaction(Object sid, Object obj) throws SystemException{
	    if (associatedObjects == null) {
		      associatedObjects = new HashMap<Object,Object>();
	    }
	    if (associatedObjects.get(sid) != null){
//	       LOCATION.logT(Severity.DEBUG, "Object " + obj + " is already associated with current transaction.");
	       SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000179", "Object {0} is already associated with current transaction.", new Object[] {obj});
	    }
	    associatedObjects.put(sid,obj);
	}

    public Object getAssociatedObjectWithTransaction(Object sid){
	    if(associatedObjects == null){
	      return null;
	    }
	    return associatedObjects.get(sid);
	}
	
//====================== Enlistment of 1PC resources (LocalTransaction) =====================    
    
    public void enlistLocalResource(LocalTxProvider localRef) throws SystemException {
        if (LOCATION.beLogged(Severity.DEBUG)) {
//          LOCATION.logT(Severity.DEBUG, "{0} .enlistLocalResource({1})", new Object[]{toString(), Log.objectToString(localRef)});
          SimpleLogger.trace(Severity.DEBUG, LOCATION, "ASJ.trans.000180","{0} .enlistLocalResource({1})", new Object[]{toString(), Log.objectToString(localRef)});
        }
        if (localResourceReference != null) {
          if (LOCATION.beLogged(Severity.DEBUG)) {
//            LOCATION.logT(Severity.DEBUG, "{0} local resource is already enlisted into Transaction ( {1} ), second enlistLocalResource is not possible.", new Object[]{toString(), Log.objectToString(localResourceReference)});
            SimpleLogger.trace(Severity.DEBUG, LOCATION, "ASJ.trans.000181","{0} local resource is already enlisted into Transaction ( {1} ), second enlistLocalResource is not possible.", new Object[]{toString(), Log.objectToString(localResourceReference)});
          }
          throw new BaseSystemException(ExceptionConstants.Second_local_resource, toString());
        }
        localTX = (LocalTransaction)localRef.getLocalTransaction();
        try {
          localTX.begin();
        } catch (RuntimeException e) {
          if (LOCATION.beLogged(Severity.DEBUG)) {
            LOCATION.traceThrowableT(Severity.DEBUG, "{0} local resource couldn't begin successfuly.", new Object[]{localTX}, e);
          }
          throw new BaseSystemException(ExceptionConstants.Exception_local_resource_begin, toString(), e);
        } catch (ResourceException e) {
          if (LOCATION.beLogged(Severity.DEBUG)) {
            LOCATION.traceThrowableT(Severity.DEBUG, "{0} local resource couldn't begin successfuly.", new Object[]{localTX}, e);
          }
          throw new BaseSystemException(ExceptionConstants.Exception_local_resource_begin, toString(), e);
        }
        localResourceReference = localRef;
      }

	public LocalTxProvider getLocalResource() {		
		return localResourceReference;
	}    
    
  //======================= Enlistment of XAResources ======================================	
	
	public boolean enlistResource(int rmID, XAResource xaRes) throws RollbackException, IllegalStateException, SystemException {
	    if (LOCATION.beLogged(Severity.DEBUG)) {
	    	SimpleLogger.trace(Severity.ERROR, LOCATION, "enlistResource "+xaRes+" with rmID="+rmID+" into transaction:" + getTransactionIdForLogs());
        }
		
		XAResourceWrapper xaResourceWrapper = new XAResourceWrapper(xaRes, rmID);
		int countOfXAResourcewithSameRMID = 0;// used  
		
		if(xaResourceWrapperList == null){
			xaResourceWrapperList = new ArrayList<XAResourceWrapper>();
		} else {
			for(XAResourceWrapper xaWrap : xaResourceWrapperList){
				if(xaWrap.theXAResource == xaRes){
					throw new IllegalStateException("Provided XAResource instance was already enlisted into this transaction. Second enlistment is not supported.");
				}				
				if(xaWrap.rmID == rmID){//calculation of branch iterators.
					countOfXAResourcewithSameRMID++;
				}
			}
		}
		
		if(mainXID == null){//enlistment of first XAResource
			mainXID = SAPXidImpl.createNewXidWithNewGTxId(transactionClassifierByteArray, transactionBirthTime, transactionSequenceNumber, rmID);
			rejectSetTransactionClassifier = true;
			try {
				xaResourceWrapper.start(mainXID);
			} catch (RuntimeException runex){
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "RuntimeException occurred during "+xaResourceWrapper.theXAResource+".start(TMNOFLAGS). Resource is not enlisted into transaction.", runex);		    	  
				throw new BaseSystemException(ExceptionConstants.Cannot_enlist_XAEx_start_tmnoflag, xaRes, runex);				
		    } catch (XAException xae) {
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "XAException occurred during "+xaResourceWrapper.theXAResource+".start(TMNOFLAGS). Returned status is "+convertXAErrorCodeToString(xae.errorCode)+". Resource is not enlisted into transaction.", xae);
				throw new BaseSystemException(ExceptionConstants.Cannot_enlist_XAEx_start_tmnoflag, xaRes, xae);
			}
			xaResourceWrapperList.add(xaResourceWrapper);
			return true;
		}
		
		if(countOfXAResourcewithSameRMID >255){
			throw new IllegalStateException("TransactionManager does not support enlistment of mote than 255 different connections from one resource manager into one Transaction.");
		}
		
		// enlistment of second and next resources.
		try {//branch iterator for first XAResource is 0, after that 1..to 255
			if(rmID == 0){
				rmID = countOfXAResourcewithSameRMID + 32768;//only 32768 registered RM-s are allowed. Which is more then enough.  
			}
			xaResourceWrapper.start(mainXID.createXidForNewResourceManager((byte)countOfXAResourcewithSameRMID, rmID));
		} catch (RuntimeException e){
	    	  SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "RuntimeException occurred during "+xaResourceWrapper.theXAResource+".start(TMNOFLAGS). Resource is not enlisted into transaction.", e);		    	  
			  SystemException sysEx = new SystemException("Unexpected Exception occurred during enlisment of XAResource :" + xaRes);
			  sysEx.initCause(e);
			  throw sysEx;
		} catch (XAException xae) {
			SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "XAException occurred during "+xaResourceWrapper.theXAResource+".start(TMNOFLAGS). Returned status is "+convertXAErrorCodeToString(xae.errorCode)+". Resource is not enlisted into transaction.", xae);
			throw new BaseSystemException(ExceptionConstants.Cannot_enlist_XAEx_start_tmnoflag, xaRes, xae);
		}
		xaResourceWrapperList.add(xaResourceWrapper);
		return true;		
	}

	public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException, SystemException {
	    if (LOCATION.beLogged(Severity.DEBUG)) {
//	        LOCATION.logT(Severity.DEBUG, "{0} .delistResource({1}, {2})", new Object[]{toString(), Log.objectToString(xaRes), Integer.toString(flag)});
	    	SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000182", "{0} .delistResource({1}, {2})", new Object[]{toString(), Log.objectToString(xaRes), Integer.toString(flag)});
        }

	    if (xaResourceWrapperList != null) {
			for(XAResourceWrapper xaWrap : xaResourceWrapperList){
				if(xaWrap.theXAResource == xaRes || ((xaRes instanceof XAResourceRMWrapper) && (((XAResourceRMWrapper)xaRes).getVendorXAResource() == xaWrap.theXAResource))){
					try {
						xaWrap.delistResource();
				    } catch (RuntimeException e) {
		            	 SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "RuntimeException occurred during "+xaWrap.theXAResource+".end().", e);
			             throw new BaseIllegalStateException(ExceptionConstants.Exception_XAResource_End, e);
			        } catch (XAException xae) {
						SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "XAException occurred during "+xaWrap.theXAResource+".end(). Returned status is "+convertXAErrorCodeToString(xae.errorCode)+".", xae);
						throw new BaseIllegalStateException(ExceptionConstants.Exception_XAResource_End, xae);
			        }   
					return true;
				}
			}	    
	    }
	    	
	    throw new IllegalStateException("It is not possible to delist provided XAResource because it is not enlisted into transaction.");  	

	}

    public boolean enlistResource(XAResource xaRes) throws RollbackException,IllegalStateException, SystemException {
      if(LOCATION.beDebug()){
    	  SimpleLogger.trace(Severity.DEBUG, LOCATION, "Transaction:"+getTransactionIdForLogs()+" .enlistResource("+xaRes+")");
      }
      int rmID = 0;
      XAResource vendorXAResource = xaRes;
    	
      if(xaRes instanceof XAResourceRMWrapper){
    	rmID = ((XAResourceRMWrapper)xaRes).getRMID();
    	vendorXAResource = ((XAResourceRMWrapper)xaRes).getVendorXAResource();
      } else {
    	rmID = tm.getRMIdOfExternalRM(xaRes);
    	if(LOCATION.beDebug() && rmID == 0){
    		SimpleLogger.trace(Severity.DEBUG, LOCATION, "XAResource " + xaRes + " is from unknown resource manager and TransactionManager will not be able to recover it's transactions.");
    	}
      }
      return enlistResource(rmID, vendorXAResource);
    }	
	
	
	
	
//================= Methods used from connection management ===============	
	

	/**
	 * @return the transaction sequence number. This number is unique only during one TM run.   
	 */
	public long getID() {
		// trace method call
		return transactionSequenceNumber;
	}

	/**
	 * @return  the list with ManagedConnection with XAResource support which are enlisted 
	 * into this transaction. Used from connection management for implementation of connection sharing. 
	 */
	public List getXAResourceConnections(){
		  return xaConnectionsList;
	}

	/**
	 * Checks if resource with local transaction support was already enlisted into this Transaction.
	 * Used for connection management for LAO implementation. 
	 * 
	 * @return true if there is no resource with local transaction support which is already enlisted 
	 * into this transaction. Otherwise returns false. 
	 */
	public boolean isEmpty() {
	   return localTX == null;
	}


	/**
	 * This method is used only for transaction propagation vie RMI/IIOP.
	 * 
	 * @return byte aray which represents global transaction id for this transactions object. Branch iterator is 0.
	 */
	public byte[] getGlobalTxIDWith_0_BranchIterator(){
	
		rejectSetTransactionClassifier = true;
		return SAPXidImpl.createGlobalTxIDWith_0_BranchIterator(transactionClassifierByteArray, transactionBirthTime, transactionSequenceNumber);	
		
	}
	
//=============== Registration of transaction Synchronization listeners	
	
	  /**
	   * Register a synchronization object for the transaction currently
	   * associated with the calling thread. The transaction manager invokes
	   * the beforeCompletion method prior to starting the transaction
	   * commit process. After the transaction is completed, the transaction
	   * manager invokes the afterCompletion method.
	   *
	   * @param newSynchronization The Synchronization object for the transaction associated
	   *    with the target object
	   *
	   * @exception RollbackException Thrown to indicate that
	   *    the transaction has been marked for rollback only.
	   *
	   * @exception IllegalStateException Thrown if the transaction in the
	   *    target object is in prepared state or the transaction is inactive.
	   *
	   * @exception SystemException Thrown if the transaction manager
	   *    encounters an unexpected error condition
	   *
	   */
	  public void registerSynchronization(Synchronization newSynchronization) throws RollbackException, IllegalStateException, SystemException {
	    if (LOCATION.beLogged(Severity.DEBUG)) {
	    	SimpleLogger.trace(Severity.DEBUG, LOCATION, " registerSynchronization with listener "+newSynchronization+" into transaction:"+getTransactionIdForLogs());
	    }

	    if (status == Status.STATUS_MARKED_ROLLBACK) {
	    	BaseRollbackException e = new BaseRollbackException("Cannot register synchronization listener into transaction:"+getTransactionIdForLogs()+" because it is marked for rollback.");	    	
	    	if (LOCATION.beLogged(Severity.DEBUG)) {
	    	  SimpleLogger.traceThrowable(Severity.DEBUG, LOCATION, "registerSynchronization with listener "+newSynchronization+" is rejected because transaction:"+getTransactionIdForLogs()+" is marked for rollback.", e);
	    	}
	    	throw e; 
	    }

	    if (!isAlive()) {
	    	BaseRollbackException e = new BaseRollbackException("Cannot register synchronization listener into transaction:"+getTransactionIdForLogs()+" because it is not active.");
	    	if (LOCATION.beLogged(Severity.DEBUG)) {
		    	  SimpleLogger.traceThrowable(Severity.DEBUG, LOCATION, "registerSynchronization with listener "+newSynchronization+" is rejected because transaction:"+getTransactionIdForLogs()+" is not active.", e);
     	    }
	    	throw e; 
		}	    
	    
	    if (status == Status.STATUS_PREPARED) {
	    	BaseRollbackException e = new BaseRollbackException("Cannot register synchronization listener into transaction:"+getTransactionIdForLogs()+" because it is already prepared for commit.");
	    	if (LOCATION.beLogged(Severity.DEBUG)) {
		    	  SimpleLogger.traceThrowable(Severity.DEBUG, LOCATION, "registerSynchronization with listener "+newSynchronization+" is rejected because transaction:"+getTransactionIdForLogs()+" is prepared for commit.", e);
     	    }
	    	throw e; 
		}	    
	    
	    registerSynchronizationWithoutStatusChecks(newSynchronization);

	  }

	  /**
	   * Register a synchronization object for the transaction currently
	   * associated with the calling thread. The transaction manager invokes
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
	  public synchronized void registerSynchronizationWithoutStatusChecks(Synchronization newSynchronization) throws RollbackException, IllegalStateException, SystemException{

	    if (status != Status.STATUS_ACTIVE && status != Status.STATUS_MARKED_ROLLBACK) {
	      if (LOCATION.beLogged(Severity.DEBUG)) {
//	        LOCATION.logT(Severity.DEBUG, "{0} invalid status: {1}", new Object[]{toString(), Log.statusToString(status)});
	        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000183", "{0} invalid status: {1}", new Object[]{toString(), Log.statusToString(status)});
	      }
	      throw new BaseIllegalStateException(ExceptionConstants.Status_not_active, new Object[]{toString(), Log.statusToString(status)});
	    }

	    if(newSynchronization instanceof AfterBeginSynchronizationExtension){
	    	try{
	    		((AfterBeginSynchronizationExtension)newSynchronization).afterBegin();
	    	}catch (Exception e) {
	    		String exMess = "Exception : " + e + "was thrown from " + newSynchronization + ".afterBegin() method. This synchronization is not registered into transaction.";
	    	    if (LOCATION.beLogged(Severity.WARNING)) {
//	    	       LOCATION.logT(Severity.WARNING, exMess);
	    	       SimpleLogger.trace(Severity.WARNING,LOCATION, "ASJ.trans.000184", "Exception : {0} was thrown from {1}.afterBegin() method. This synchronization is not registered into transaction.",  new Object[] { e,newSynchronization});
	    	    }    		
	    		SystemException sysEx = new SystemException(exMess);
	    		sysEx.initCause(e);
				throw sysEx;
			}
	    }
	    
	    int newPriority = SynchronizationPriorityExtension.DEFAULT_PRIORITY;
	    if (newSynchronization instanceof SynchronizationPriorityExtension) {
	      newPriority = ((SynchronizationPriorityExtension)newSynchronization).getPriority();
	      if (newPriority < SynchronizationPriorityExtension.MIN_PRIORITY || newPriority > SynchronizationPriorityExtension.MAX_PRIORITY) {
	        if (LOCATION.beLogged(Severity.DEBUG)) {
//	          LOCATION.logT(Severity.DEBUG, "{0} invalid synchronization priority: {1}", new Object[]{toString(), Integer.toString(newPriority)});
	          SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000185", "{0} invalid synchronization priority: {1}", new Object[]{toString(), Integer.toString(newPriority)});
	        }
	        throw new BaseSystemException(ExceptionConstants.INVALID_PRIORITY, new Object[]{toString(), Integer.toString(newPriority)});
	      }
	    }

	    int pos = 0;
	    for (; pos < synchronizations.size(); pos++) {
	      Synchronization synchronization = synchronizations.get(pos);
	      int priority = SynchronizationPriorityExtension.DEFAULT_PRIORITY;
	      if (synchronization instanceof SynchronizationPriorityExtension) {
	        priority = ((SynchronizationPriorityExtension)synchronization).getPriority();
	      }
	      if (newPriority > priority) {
	        break;
	      }
	    }
	    synchronizations.add(pos, newSynchronization);
	    
	    if(listeners != null){// This trick is used to call beforeCompletion() method on synchronization objects that are registered during beforeCompletion call of other synchronization object.
	    	if(newSynchronizationListeners == null){
	    		newSynchronizationListeners = new ArrayList<Synchronization>();
	    	}
	    	newSynchronizationListeners.add(newSynchronization);
	    }

	  }
	
	  protected void checkIfReadyForCommit() throws RollbackException, IllegalStateException, SystemException {
		  
	      if (status == Status.STATUS_MARKED_ROLLBACK) {
		        if (LOCATION.beLogged(Severity.DEBUG)) {
		        	SimpleLogger.trace(Severity.DEBUG, LOCATION, "Cannot commit transaction:"+getTransactionIdForLogs()+" because it was marked for rollback. " +
		        			"Transaction is marked for rollback from " + Util.getStackTrace(setRollbackPosition));
		        }
		        rollback_internal(false);
		        throw new BaseRollbackException(ExceptionConstants.Transaction_marked_for_rollback, toString(), setRollbackPosition);
		      }

		      if (status != Status.STATUS_ACTIVE) {
		        if (LOCATION.beLogged(Severity.DEBUG)) {
		        	SimpleLogger.trace(Severity.DEBUG, LOCATION, "Cannot commit transaction:"+getTransactionIdForLogs()+" because its status is not 'active'. Status is " + Log.statusToString(status));
		        }
		        throw new BaseIllegalStateException(ExceptionConstants.Status_not_active, new Object[]{toString(), Log.statusToString(status)});
		      }
	  }
	  
	  protected void beforePrepare() throws RollbackException, SystemException {
		  
	      try {
		        listeners = synchronizations.toArray(new Synchronization[synchronizations.size()]);
		        for (; beforeCompletionCalledPointer < listeners.length; beforeCompletionCalledPointer++) {
		          listeners[beforeCompletionCalledPointer].beforeCompletion();
	        	  if(newSynchronizationListeners != null){
	        		  //This trick is used to call beforeCompletion() method on synchronization objects that
	        		  //are registered during beforeCompletion call of other synchronization object.
	        		for(Synchronization sync : newSynchronizationListeners){
	        			sync.beforeCompletion();
	        		}
	        		newSynchronizationListeners = null;
	        	  }
		          if (status == Status.STATUS_MARKED_ROLLBACK){
		            beforeCompletionCalledPointer++;        	
		            break;
		          }
		        }
		      } catch (RuntimeException te) {
		        if (LOCATION.beLogged(Severity.DEBUG)) {
		        	SimpleLogger.trace(Severity.DEBUG, LOCATION, "Cannot commit transaction:"+getTransactionIdForLogs()+" because of unexpected exception in beforeCompletion. Will try to rollback the transaction.", te);
		        }
		        beforeCompletionCalledPointer++;
		        rollback_internal(false);
		        throw new BaseRollbackException(ExceptionConstants.Exception_in_beforeCompletion, toString(), te);
		      }

		      // in case it is marked for rollback in beforeCompletion()
		      if (status == Status.STATUS_MARKED_ROLLBACK) {
		        if (LOCATION.beLogged(Severity.DEBUG)) {
		        	SimpleLogger.trace(Severity.DEBUG, LOCATION, "Cannot commit transaction:"+getTransactionIdForLogs()+" because it was marked for rollback during beforeCompletion callbacks." +
		        			"Transaction is marked for rollback from " + Util.getStackTrace(setRollbackPosition));	        			
		        }
		        rollback_internal(false);
		        throw new BaseRollbackException(ExceptionConstants.Transaction_marked_for_rollback, toString(), setRollbackPosition);
		      }

		      try{
		        ResourceSet resourceSet = (ResourceSet)getAssociatedObjectWithTransaction(TXR_UserTransaction.UT_RESOURCE_SET_OBJECT_KEY);
		        if(resourceSet != null){
		          resourceSet.delistAll(XAResource.TMSUCCESS);
		          associatedObjects.remove(TXR_UserTransaction.UT_RESOURCE_SET_OBJECT_KEY);// this operation will not be done in rollback.
		        }		        
		      } catch (SystemException sysEx) {
		        if (LOCATION.beLogged(Severity.DEBUG)) {
		        	SimpleLogger.trace(Severity.DEBUG, LOCATION, "Cannot commit transaction:"+getTransactionIdForLogs()+" because of unexpected exception during resource delistment.", sysEx);
		        }
		        rollback_internal(false);
		        throw new BaseRollbackException(ExceptionConstants.Commit_failed, toString(), sysEx);
		      }

		      try{
		        ResourceContext resourceContext = (ResourceContext)getAssociatedObjectWithTransaction(TXR_TransactionManagerImpl.RESOURCE_CONTEXT_OBJECT_KEY);
		        if(resourceContext != null){
		          resourceContext.exitMethod(TXR_TransactionManagerImpl.RESOURCE_CONTEXT_METHOD_NAME, true);
		          associatedObjects.remove(TXR_TransactionManagerImpl.RESOURCE_CONTEXT_OBJECT_KEY);//this operation will not be done in rollback.
		        }
		      } catch (ResourceContextException rce){
		        if (LOCATION.beLogged(Severity.DEBUG)) {
		        	SimpleLogger.trace(Severity.DEBUG, LOCATION, "Cannot commit transaction:"+getTransactionIdForLogs()+" because of unexpected exception during resource delistment.", rce);
		        }
		        rollback_internal(false);
		        throw new BaseRollbackException(ExceptionConstants.Commit_failed, toString(),rce);
		      }
	  }
	  
	  protected void prepareRMs() throws RollbackException, SystemException {
		       
		      RollbackException rollbackException = null;	      
		      boolean fatalStatus = false;

		      if (xaResourceWrapperList != null) { 
		        for(XAResourceWrapper xaWrap : xaResourceWrapperList) {
		          int prepareStatus = XAResource.TMFAIL;
		          try {
		            prepareStatus = xaWrap.prepare();
		            fatalStatus = (prepareStatus != XAResource.XA_OK && prepareStatus != XAResource.XA_RDONLY);
		            if(xaWrap.checkIfEligibleForRecovery()){
		            	numberOfPreparedAndRecoverableRMs++;
		            }
		          } catch (XAException xaE) {
		            fatalStatus = true;
		            isRolledbackBecauseOfRMError = true;
		            String messageForRollbackException = null;
			        String rmName = getRMName(xaWrap.rmID, xaWrap.theXAResource);
		            int errorCode = xaE.errorCode;
		            if (errorCode >= XAException.XA_RBBASE && errorCode <= XAException.XA_RBEND){
		            	messageForRollbackException = "Transaction:"+getTransactionIdForLogs()+" will be rolledback because resource manager "+rmName+" rolledback it's transaction branch during prepare phase and returned " + convertXAErrorCodeToString(errorCode) + " status";
		            	SimpleLogger.traceThrowable(Severity.INFO, LOCATION, messageForRollbackException, xaE);
		            } else { //XAER_RMERR, XAER_RMFAIL, OTHER
		            	messageForRollbackException = "Transaction:"+getTransactionIdForLogs()+" cannot be committed because resource manager "+rmName+" failed to prepare the transaction and returned " + convertXAErrorCodeToString(errorCode) + " status. Transaction manager will try to rolledback the transaction. ";
		            	SimpleLogger.traceThrowable(Severity.INFO, LOCATION, messageForRollbackException, xaE);
		            }
		            rollbackException = new RollbackException(messageForRollbackException);
		            rollbackException.initCause(xaE);
		          } catch (RuntimeException re) {
		            fatalStatus = true;
		            isRolledbackBecauseOfRMError = true;
	            	String messageForRollbackException = "Transaction:"+getTransactionIdForLogs()+" will be rolledback because resource manager "+getRMName(xaWrap.rmID, xaWrap.theXAResource)+" was not able to prepare its transaction branch.";
	            	SimpleLogger.traceThrowable(Severity.INFO, LOCATION, messageForRollbackException, re);	            
		            rollbackException = new RollbackException(messageForRollbackException);
		            rollbackException.initCause(re);
		          }
		          if(fatalStatus && rollbackException != null){
		        	  String messageForRollbackException = "Transaction:"+getTransactionIdForLogs()+"will be rolledback because resource manager "+getRMName(xaWrap.rmID, xaWrap.theXAResource)+" was not able to prepare its transaction branch. Returned result from prepare is " +convertXAReturnCodesToString(prepareStatus) + ".";
		        	  SimpleLogger.trace(Severity.INFO, LOCATION, messageForRollbackException);	             		
		        	  rollbackException = new RollbackException(messageForRollbackException);
		        	  break;
		          }
		        }
		      }

		      if (fatalStatus) {
		        rollback_internal(false);
		        throw rollbackException;
		      }

		      if (txControl != null) {		    	  
    		    LinkedList omgResources = txControl.getOMGResources(); // a list of OMG Resources registered to the Coordinator
		        for(int i = 0; i < omgResources.size() && !fatalStatus; i++) {
		          try {
		            int voteResult = ((Resource)omgResources.get(i)).prepare().value();
		            if (voteResult == Vote._VoteRollback) {
		              fatalStatus = true;
		              SimpleLogger.trace(Severity.INFO, LOCATION, "OMG Resource:"+ omgResources.get(i) +" voted for rollback. Transaction:"+getTransactionIdForLogs()+" will be rolledback.");
		            }
		          } catch (RuntimeException re) {
		            fatalStatus = true;
		            String errorMessage = "Unexpected exception occurred during "+omgResources.get(i)+ ".prepare(). Transaction:"+getTransactionIdForLogs()+" will be rolledback";
		            SimpleLogger.trace(Severity.INFO, LOCATION, errorMessage);
		            rollbackException = new RollbackException(errorMessage);	            
		            rollbackException.initCause(re);
		          } catch (HeuristicMixed hm) {
		            fatalStatus = true;
		            String errorMessage = "Transaction:"+getTransactionIdForLogs()+" will be rolledback because "+omgResources.get(i)+ ".prepare() returned HeuristicMixed status.";
		            SimpleLogger.trace(Severity.INFO, LOCATION, errorMessage);
		            rollbackException = new RollbackException(errorMessage);	            
		            rollbackException.initCause(hm);
		          } catch (HeuristicHazard hh) {
		            fatalStatus = true;
		            String errorMessage = "Transaction:"+getTransactionIdForLogs()+" will be rolledback because "+omgResources.get(i)+ ".prepare() returned HeuristicHazard status.";
		            SimpleLogger.trace(Severity.INFO, LOCATION, errorMessage);
		            rollbackException = new RollbackException(errorMessage);	            
		            rollbackException.initCause(hh);
		           }
		          if(fatalStatus && rollbackException != null){
		        	  rollbackException = new RollbackException("Transaction:" + getTransactionIdForLogs() + " cannot be committed and will be rolledback because resource manager "+ omgResources.get(i) + " voted for rollback.");
		        	  break;
		          }	          
		        }

		      }

		      if (fatalStatus) {
			        rollback_internal(false);
			        throw rollbackException;
			  }
	  }

	  protected void commitSecondPhase(boolean onePhaseCommitOptimization, boolean isInboundTransaction) throws RollbackException, SystemException, HeuristicMixedException,HeuristicRollbackException {
	      SAPHeuristicHazardException heuristicHazardException = null;
	      HeuristicMixedException heuristicMixedException = null;
          
	      try {
	          try{
	      	    if (localTX != null) {
	      	        localTX.commit();
	      	      }
	          } catch (ResourceException re) {
	        	  String rmName = UNKNOWN_LOCAL_RM_PREFIX + localTX.getClass().getName();	        	
		          PendingRMInfo pendingRMInfo = new PendingRMInfo(true, null, 0, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_UNEXPECTED_ERROR_FROM_LT, 
		        			getTransactionIdForLogs(), re, false, false);
		          if(rmInfos == null) rmInfos = new ArrayList<PendingRMInfo>();
		          rmInfos.add(pendingRMInfo);
		          failedRMNames.add(rmName);
		          changeDecisionToRollbackAndRemoveTransactionRecord();
			      BaseRollbackException bre = new BaseRollbackException(ExceptionConstants.Transaction_rolled_back_LT_resource_error, toString(), re);
			      rollback_internal(false);		        	
		          throw bre;      	
	          } catch (RuntimeException re) {
	        	  String rmName = UNKNOWN_LOCAL_RM_PREFIX + localTX.getClass().getName();	        	
		          PendingRMInfo pendingRMInfo = new PendingRMInfo(true, null, 0, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_UNEXPECTED_ERROR_FROM_LT, 
		        			getTransactionIdForLogs(), re, false, false);
		          if(rmInfos == null) rmInfos = new ArrayList<PendingRMInfo>();
		          rmInfos.add(pendingRMInfo);
		          failedRMNames.add(rmName);
		          changeDecisionToRollbackAndRemoveTransactionRecord();		          
		          BaseRollbackException bre = new BaseRollbackException(ExceptionConstants.Transaction_rolled_back_LT_resource_error, toString(), re);
		          rollback_internal(false);		        	
	          	  throw bre;      	
	          } 

	          
	        if (xaResourceWrapperList != null) {
	      	  if(onePhaseCommitOptimization){
	      		XAResourceWrapper xaWrap = xaResourceWrapperList.get(0);  
	  	        try {
	  		       xaWrap.commitOnePhase();
	  		    } catch (RuntimeException re) {
	            	String rmName = getRMName(xaWrap.rmID, xaWrap.theXAResource);	
	            	PendingRMInfo pendingRMInfo = new PendingRMInfo(true, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_UNEXPECTED_ERROR_FROM_XARM_1PC,
				      		getTransactionIdForLogs(), re, false, false);
	            	heuristicHazardException = new SAPHeuristicHazardException(pendingRMInfo.getMessageForTrace(), re);
	            	if(rmInfos == null) rmInfos = new ArrayList<PendingRMInfo>();
	            	rmInfos.add(pendingRMInfo);
	            	failedRMNames.add(rmName);  	            	
	  		    } catch (XAException xae) {
	  		    	int errorCode = xae.errorCode;
	  		    	String rmName = getRMName(xaWrap.rmID, xaWrap.theXAResource);
	  		    	if(rmInfos == null) rmInfos = new ArrayList<PendingRMInfo>();
	  		    	
	  		    	if(errorCode == XAException.XA_HEURCOM){
	  		    		PendingRMInfo pendingRMInfo = new PendingRMInfo(false, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_XA_HEURCOM_1PC,
			    		      		getTransactionIdForLogs(), xae, false, false);
	  		    		rmInfos.add(pendingRMInfo);
	  		       } else if(errorCode == XAException.XA_HEURHAZ){
 	            	   isHeuristicallyCompleted = true;
 	            	   PendingRMInfo pendingRMInfo = new PendingRMInfo(true, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_XA_HEURHAZ_1PC,
			    		      		getTransactionIdForLogs(), xae, false, false);
 	            	   SAPHeuristicHazardException heurHazEx = new SAPHeuristicHazardException(pendingRMInfo.getMessageForTrace(), xae);
 	            	   rmInfos.add(pendingRMInfo);
 	            	   failedRMNames.add(rmName);
 	            	   throw heurHazEx; 	            	   
	  		       } else if(errorCode == XAException.XA_HEURMIX){
	  		    	   isHeuristicallyCompleted = true;
	  		    	   PendingRMInfo pendingRMInfo = new PendingRMInfo(true, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_XA_HEURMIX_1PC,
			    		      		getTransactionIdForLogs(), xae, false, false);
	  		    	   HeuristicMixedException heurMixEx = new HeuristicMixedException(pendingRMInfo.getMessageForTrace());
	  		    	   heurMixEx.initCause(xae);
	  		    	   rmInfos.add(pendingRMInfo);
	  		    	   failedRMNames.add(rmName);
	  		    	   throw heurMixEx;
	  		       } else if(errorCode == XAException.XA_HEURRB){
	  		    	   PendingRMInfo pendingRMInfo = new PendingRMInfo(true, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_XA_HEURRB_1PC,
		    		      		getTransactionIdForLogs(), xae, false, false);
	  		    	   RollbackException rbe = new RollbackException(pendingRMInfo.getMessageForTrace());
	  		    	   rbe.initCause(xae);
	  		    	   rmInfos.add(pendingRMInfo);
	  		    	   failedRMNames.add(rmName);
	  		    	   throw rbe;
	  		       } else if((errorCode >= XAException.XA_RBBASE && errorCode <= XAException.XA_RBEND) || (errorCode == XAException.XAER_RMERR) ){
	  		    	   PendingRMInfo pendingRMInfo = new PendingRMInfo(true, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_XA_RBBASE_1PC,
		    		      		getTransactionIdForLogs(), xae, false, false);
	  		    	   RollbackException rbe = new RollbackException(pendingRMInfo.getMessageForTrace());
	  		    	   rbe.initCause(xae);
	  		    	   rmInfos.add(pendingRMInfo);
	  		    	   failedRMNames.add(rmName);
	  		    	   throw rbe;	  		    	   
	  		       } else if(errorCode == XAException.XAER_RMFAIL){
 	            	   PendingRMInfo pendingRMInfo = new PendingRMInfo(true, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_XAER_RMFAIL_1PC,
			    		      		getTransactionIdForLogs(), xae, true, false);
 	            	   SAPHeuristicHazardException heurHazEx = new SAPHeuristicHazardException(pendingRMInfo.getMessageForTrace(), xae);
 	            	   rmInfos.add(pendingRMInfo);
 	            	   failedRMNames.add(rmName);
 	            	   throw heurHazEx; 
	  		       } else {
	  		    	   PendingRMInfo pendingRMInfo = new PendingRMInfo(true, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_XA_UNKNOWN_1PC,
		    		      		getTransactionIdForLogs(), xae, false, false);
	  		    	   HeuristicMixedException heurMixEx = new HeuristicMixedException(pendingRMInfo.getMessageForTrace());
	  		    	   heurMixEx.initCause(xae);
	  		    	   rmInfos.add(pendingRMInfo);
	  		    	   failedRMNames.add(rmName);	 
	  		    	   throw heurMixEx;
	  		       }
	  		    }		          
	      	  } else {
		      		boolean firstRM = localTX == null && txControl == null;  
		  	        for(XAResourceWrapper xaWrap : xaResourceWrapperList) {
		  	          try {
		  	            xaWrap.commitTwoPhase();
		  	          } catch (RuntimeException re) {
		            	String rmName = getRMName(xaWrap.rmID, xaWrap.theXAResource);	
		            	PendingRMInfo pendingRMInfo = new PendingRMInfo(true, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_UNEXPECTED_ERROR_FROM_XARM_2PC,
					      		getTransactionIdForLogs(), re, false, false);
		            	heuristicHazardException = new SAPHeuristicHazardException(pendingRMInfo.getMessageForTrace(), re);
		            	if(rmInfos == null) rmInfos = new ArrayList<PendingRMInfo>();
		            	rmInfos.add(pendingRMInfo);
		            	failedRMNames.add(rmName);  	            	
		  	          } catch (XAException xae) {	  	        	  
			              int errorCode = xae.errorCode;
				          String rmName = getRMName(xaWrap.rmID, xaWrap.theXAResource);
				          if(rmInfos == null) rmInfos = new ArrayList<PendingRMInfo>();
				          
				          if (errorCode == XAException.XA_HEURCOM){
			            	  PendingRMInfo pendingRMInfo = new PendingRMInfo(false, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_XA_HEURCOM_2PC,
				    		      		getTransactionIdForLogs(), xae, false, !TransactionServiceFrame.callForgetAfterHeuristics); 
	            			  rmInfos.add(pendingRMInfo);            	  
			            	  xaWrap.forget();//real forget will be called only if it is configured.		  	            	
		  	            } else if (errorCode == XAException.XA_HEURHAZ){
		  	            	isHeuristicallyCompleted = true;
		  	            	PendingRMInfo pendingRMInfo = new PendingRMInfo(true, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_XA_HEURHAZ_2PC,
				    		      		getTransactionIdForLogs(), xae, false, !TransactionServiceFrame.callForgetAfterHeuristics);
		  	            	rmInfos.add(pendingRMInfo);
	 	 	  		        failedRMNames.add(rmName);		  	            		
		  	            	heuristicHazardException = new SAPHeuristicHazardException(pendingRMInfo.getMessageForTrace());
		  	            	heuristicHazardException.initCause(xae);
		  	            	xaWrap.forget();//forget method will check the settings and will call forget method on RM.
		  	            } else if (errorCode == XAException.XA_HEURMIX){
		  	            	isHeuristicallyCompleted = true;
		  	            	PendingRMInfo pendingRMInfo = new PendingRMInfo(true, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_XA_HEURMIX_2PC,
			    		      		getTransactionIdForLogs(), xae, false, !TransactionServiceFrame.callForgetAfterHeuristics);
		  	            	rmInfos.add(pendingRMInfo);
		  	            	failedRMNames.add(rmName);		  	            		
		 	  		        heuristicMixedException = new HeuristicMixedException(pendingRMInfo.getMessageForTrace());
		 	  		        heuristicMixedException.initCause(xae);
		  	            	xaWrap.forget();//forget method will check the settings and will call forget method on RM.
		  	            } else if (errorCode == XAException.XA_HEURRB){
		  	            	isHeuristicallyCompleted = true;
		  	            	if(firstRM && changeDecisionToRollbackAndRemoveTransactionRecord()){
		  	            		String messageForTrace = "Transaction:"+getTransactionIdForLogs()+" will be rolledback because first resource manager "+rmName+" rolledback heuristically its transaction branch and returned " + convertXAErrorCodeToString(errorCode)+" status";
		  	            		xaWrap.markAsRolledback();
		  	            		rollback_internal(false);
		  	            		RollbackException rbe = new RollbackException(messageForTrace);
		  	            		rbe.initCause(xae);		  	            		
		  	            		throw rbe;
		  	            	} else {
			  	            	PendingRMInfo pendingRMInfo = new PendingRMInfo(true, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_XA_HEURRB_2PC,
				    		      		getTransactionIdForLogs(), xae, false, !TransactionServiceFrame.callForgetAfterHeuristics);
			  	            	rmInfos.add(pendingRMInfo);
			  	            	failedRMNames.add(rmName);			  	            		
			 	  		        heuristicMixedException = new HeuristicMixedException(pendingRMInfo.getMessageForTrace());
			 	  		        heuristicMixedException.initCause(xae);
			  	            	xaWrap.forget();//forget method will check the settings and will call forget method on RM.		  	            		
		  	            	}
		  	            	
		  	            } else if ((errorCode >= XAException.XA_RBBASE && errorCode <= XAException.XA_RBEND) || (errorCode == XAException.XAER_RMERR)){
		  	            	if(firstRM && changeDecisionToRollbackAndRemoveTransactionRecord()){
		  	            		String messageForTrace = "Transaction:"+getTransactionIdForLogs()+" will be rolledback because first resource manager "+rmName+" rolledback heuristically its transaction branch and returned " + convertXAErrorCodeToString(errorCode)+" status";
		  	            		xaWrap.markAsRolledback();
		  	            		rollback_internal(false);
		  	            		RollbackException rbe = new RollbackException(messageForTrace);
		  	            		rbe.initCause(xae);		  	            		
		  	            		throw rbe;		  	            		
		  	            	} else {
			  	            	PendingRMInfo pendingRMInfo = new PendingRMInfo(true, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_XA_RBBASE_2PC,
				    		      		getTransactionIdForLogs(), xae, false, false);
			  	            	rmInfos.add(pendingRMInfo);
			  	            	failedRMNames.add(rmName);
			 	  		        heuristicMixedException = new HeuristicMixedException(pendingRMInfo.getMessageForTrace());
			 	  		        heuristicMixedException.initCause(xae);		  	            	      		
		  	            	}
		  	            } else if (errorCode == XAException.XA_RETRY || errorCode == XAException.XAER_RMFAIL){
		 	  		        DeleteTxRecordAtTheEnd = false; 
		  	            	PendingRMInfo pendingRMInfo = new PendingRMInfo(false, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_XA_RETRY_XAER_RMFAIL_2PC,
			    		      		getTransactionIdForLogs(), xae, true, false);
		  	            	rmInfos.add(pendingRMInfo);
		  	            	String messageForTrace = pendingRMInfo.getMessageForTrace();
		  	            	failedRMNames.add(rmName);
		  	            	heuristicHazardException = new SAPHeuristicHazardException(messageForTrace);
		  	            	heuristicHazardException.initCause(xae);
		  	            } else {
		  	            	PendingRMInfo pendingRMInfo = new PendingRMInfo(true, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_XA_RETRY_XAER_RMFAIL_2PC,//TODO new constant
			    		      		getTransactionIdForLogs(), xae, false, false);
		  	            	rmInfos.add(pendingRMInfo);
		  	            	failedRMNames.add(rmName);
		  	            	heuristicMixedException = new HeuristicMixedException(pendingRMInfo.getMessageForTrace());
		  	            	heuristicMixedException.initCause(xae);
		  	            }
		  	          } finally {
		  	        	  firstRM = false;
		  	          }
		  	        }
		      	  }
	        }
	        /* Commiting OMG resources if this transaction is propagated */
	        if (txControl != null) {
    		  LinkedList omgResources = txControl.getOMGResources(); // a list of OMG Resources registered to the Coordinator	        	
	          for(int i = 0; i < omgResources.size(); i++) {
	            try {
	              ((Resource)omgResources.get(i)).commit();
	            } catch (RuntimeException re) {	              
	        	  String rmName = "External OMGResource " + ((Resource)omgResources.get(i)).getClass().getName();
	        	  PendingRMInfo pendingRMInfo = new PendingRMInfo(true, null, 0, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_UNEXPECTED_ERROR_OMGRM,
	    		      		getTransactionIdForLogs(), re, false, false);
	        	  heuristicHazardException = new SAPHeuristicHazardException(pendingRMInfo.getMessageForTrace(), re);
	        	  if(rmInfos == null) rmInfos = new ArrayList<PendingRMInfo>();
	        	  rmInfos.add(pendingRMInfo);
	        	  failedRMNames.add(rmName);
	            } catch (HeuristicMixed hm) {
	            	String rmName = "External OMGResource " + ((Resource)omgResources.get(i)).getClass().getName();
	            	PendingRMInfo pendingRMInfo = new PendingRMInfo(true, null, 0, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_HeuristicMixed_ERROR_OMGRM,
		    		      		getTransactionIdForLogs(), hm, false, false);
	            	heuristicMixedException = new HeuristicMixedException(pendingRMInfo.getMessageForTrace());
	            	heuristicMixedException.initCause(hm);
	            	if(rmInfos == null) rmInfos = new ArrayList<PendingRMInfo>();
	            	rmInfos.add(pendingRMInfo);
	            	failedRMNames.add(rmName);
	            } catch (NotPrepared np) {
	            	String rmName = "External OMGResource " + ((Resource)omgResources.get(i)).getClass().getName();
	            	PendingRMInfo pendingRMInfo = new PendingRMInfo(true, null, 0, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_NotPrepared_ERROR_OMGRM,
		    		      		getTransactionIdForLogs(), np, false, false);
	            	heuristicMixedException = new HeuristicMixedException(pendingRMInfo.getMessageForTrace());
	            	heuristicMixedException.initCause(np);
	            	if(rmInfos == null) rmInfos = new ArrayList<PendingRMInfo>();
	            	rmInfos.add(pendingRMInfo);
	            	failedRMNames.add(rmName);	              
	            } catch (HeuristicRollback hr) {
	            	String rmName = "External OMGResource " + ((Resource)omgResources.get(i)).getClass().getName();
	            	PendingRMInfo pendingRMInfo = new PendingRMInfo(true, null, 0, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_HeuristicRollback_ERROR_OMGRM,
		    		      		getTransactionIdForLogs(), hr, false, false);
	            	heuristicMixedException = new HeuristicMixedException(pendingRMInfo.getMessageForTrace());
	            	heuristicMixedException.initCause(hr);
	            	if(rmInfos == null) rmInfos = new ArrayList<PendingRMInfo>();
	            	rmInfos.add(pendingRMInfo);
	            	failedRMNames.add(rmName);	             
	            } catch (HeuristicHazard hh) {
	            	String rmName = "External OMGResource " + ((Resource)omgResources.get(i)).getClass().getName();
	            	PendingRMInfo pendingRMInfo = new PendingRMInfo(true, null, 0, rmName, PendingRMInfo.COMMIT_FINISHED_WITH_HeuristicHazard_ERROR_OMGRM,
		    		      		getTransactionIdForLogs(), hh, false, false);
	            	heuristicHazardException = new SAPHeuristicHazardException(pendingRMInfo.getMessageForTrace(), hh);
	            	if(rmInfos == null) rmInfos = new ArrayList<PendingRMInfo>();
	            	rmInfos.add(pendingRMInfo);
	            	failedRMNames.add(rmName);	     
	            }
	          }
	        }

	      } finally {
	    	if(status != Status.STATUS_ROLLEDBACK){  
	    	    if(DeleteTxRecordAtTheEnd){
	    	    	try {// tlog is not null because record was already stored 
						tLog.removeTransactionRecordLazily(transactionSequenceNumber);
					} catch (TLogIOException e) {
			            SimpleLogger.traceThrowable(Severity.WARNING, LOCATION, "TransactionManager was not able to delete transaction log for transaction:"+getTransactionIdForLogs()+". This is not fatal problem but can have side effects during recovery", e);
					} catch (RuntimeException e) {
			            SimpleLogger.traceThrowable(Severity.WARNING, LOCATION, "TransactionManager was not able to delete transaction log for transaction:"+getTransactionIdForLogs()+". This is not fatal problem but can have side effects during recovery", e);
					} 
	    	    }
		        status = Status.STATUS_COMMITTED; // presents in version #9+ do not remove !!!
		        
		        if(rmInfos != null && rmInfos.size()>0 && !isInboundTransaction){
		        	if(onePhaseCommitOptimization){
		        		PTLProcessor.addPendingTXData(new PendingTXData(transactionSequenceNumber, transactionClassifier, rmInfos, PendingTXData.COMMIT_FAILED_1PC));
		        	} else {
		        		PTLProcessor.addPendingTXData(new PendingTXData(transactionSequenceNumber, transactionClassifier, rmInfos, PendingTXData.COMMIT_FAILED));		        		
		        	}
			    }
		        
		        if (txControl != null) {
		          try {
		            ((SenderReceiverImpl)TransactionServiceImpl.getReceiver()).getImportedTx().remove(new TransactionKey(ByteArrayUtils.convertLongToByteArr(transactionSequenceNumber)));
		            txControl.disconnectFromORB();
		          } catch (Exception e) {//almost impossible
		        	  SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "Due to unexpected exeption transaction "+getTransactionIdForLogs()+" which was propagated via RMI/IIOP cannot be removed from the cache.", e);
		          }
		        }
	
		        for(Iterator<Synchronization> iterator = synchronizations.iterator(); iterator.hasNext();) {
		        	Synchronization theListener = iterator.next();
		          try {
		            theListener.afterCompletion(Status.STATUS_COMMITTED);
		          } catch (RuntimeException te) {
		        	  if(LOCATION.beInfo()){
		        		  SimpleLogger.traceThrowable(Severity.INFO, LOCATION, "Exception is thrown from " + theListener + ".afterCompletion(Status.STATUS_COMMITTED)", te);
		        	  }
		          }
		        }
	
		        if (timeoutListener) {
		          try {
		            TransactionServiceFrame.getTimeoutManager().unregisterTimeoutListener(this);
		          } catch (TimeOutIsStoppedException e) {
		        	  SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "Exception occurred during unregistering of a transaction from timeout management. This exception is not fatal.", e);
		          }
		        }
		        
		        if(removeSynchronizationStackWhenCompleted){
		     	   Stack synchronizationsStack = TransactionContextObject.getThransactionContextObject().getSynchronizationsStack();
		     	   if(!synchronizationsStack.isEmpty()){
		     		   synchronizationsStack.pop();
		     	   }
		        }
		        TransactionStatistics.transactionCommitted(this);
		        releaseMemory();
                // only one exception will be thrown to the application but this is not a problem because all other exceptions are logged.   	
		        if(heuristicHazardException != null){
		        	isHeuristicallyCompleted = true;
		        	throw heuristicHazardException;
		        }
		        if (heuristicMixedException != null){
		        	isHeuristicallyCompleted = true;
		        	throw heuristicMixedException;
		        }		    
	    	}
	      }		
		  
	  }
	  
//============= COMMIT and ROLLBACK implementation	
	
	public synchronized void commit() throws RollbackException, HeuristicMixedException,HeuristicRollbackException, SecurityException,IllegalStateException, SystemException {
	    if (LOCATION.beLogged(Severity.DEBUG)) {
	        SimpleLogger.trace(Severity.DEBUG, LOCATION, "Start commit operation on transaction:" + getTransactionIdForLogs());
	     }
	    boolean onePhaseCommitOptimization = false;
	    try{
	      checkIfReadyForCommit();	
	      beforePrepare();
	      status = Status.STATUS_PREPARING;		
	      onePhaseCommitOptimization = (localTX == null) && (xaResourceWrapperList != null) && (xaResourceWrapperList.size() == 1) && (txControl == null);
	      
	      if(!onePhaseCommitOptimization){
	    	  prepareRMs();
	      }	      
	    } catch (RuntimeException e){
	        if (LOCATION.beLogged(Severity.DEBUG)) {
	        	SimpleLogger.trace(Severity.DEBUG, LOCATION, "Cannot commit transaction:"+getTransactionIdForLogs()+" because of unexpected exception.", e);
	        }
	        rollback_internal(false);
	        throw new BaseRollbackException(ExceptionConstants.Commit_failed, toString(), e);
	    }
	      status = Status.STATUS_PREPARED;
	      
	      if(numberOfPreparedAndRecoverableRMs >0 && xaResourceWrapperList != null && !onePhaseCommitOptimization){
	    	  try{
	        	writeTransactionRecord(); 
	    	  } catch (Exception e){
	    		  String message = "Transaction:" + getTransactionIdForLogs() + " was rolledback because of exception during write into transaction log.";
	    		  RollbackException rollbackException = new RollbackException(message);
	    		  rollbackException.initCause(e);
//	    		  SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, message, e);
	    		  SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, e, "ASJ.trans.000012", "Transaction:{0} was rolledback because of exception during write into transaction log.", new Object[] {getTransactionIdForLogs()});
	    		  rollback_internal(false);
	    		  throw rollbackException;
	    	  }
	      }
   
	      status = Status.STATUS_COMMITTING;
	      
	      commitSecondPhase(onePhaseCommitOptimization, false);
	      
	}

	  /**
	   * Rollback the transaction represented by this Transaction object.
	   *
	   * @exception IllegalStateException Thrown if the transaction in the
	   *    target object is in prepared state or the transaction is inactive.
	   *
	   * @exception SystemException Thrown if the transaction manager
	   *    encounters an unexpected error condition
	   *
	   */
	  public void rollback() throws IllegalStateException, SystemException {
		  if (LOCATION.beLogged(Severity.DEBUG)) {
			SimpleLogger.trace(Severity.DEBUG, LOCATION, "Call: " + this + ".rollback()");  
		  }		
		  isRolledbackByApplication = true;
		  rollback_internal(false);
	  }
		  
      synchronized protected void rollback_internal(boolean isInboundTransaction) throws IllegalStateException, SystemException {
		  if (LOCATION.beLogged(Severity.DEBUG)) {
				SimpleLogger.trace(Severity.DEBUG, LOCATION, "Call: " + this + ".rollback_internal()");  
		  }	
		  if (!isAlive()) {
	    	BaseIllegalStateException bilse = new BaseIllegalStateException(ExceptionConstants.No_running_transaction);
    		SimpleLogger.traceThrowable(Severity.INFO, LOCATION, "Rollback of transaction "+this+" failed because it is not alive.", bilse);
	    	throw bilse;
	      }
	    
	    Exception reasonForSystemException = null;
	    if (status != Status.STATUS_PREPARING) {
//	      beforeRollbackCalled = true;
	      if(listeners == null){
	    	listeners = synchronizations.toArray(new Synchronization[synchronizations.size()]);
	      }
	      for (; beforeCompletionCalledPointer < listeners.length; beforeCompletionCalledPointer++) {
	        Object synch = listeners[beforeCompletionCalledPointer];
	        if (synch instanceof SynchronizationExtension) {
	          try {
	            ((SynchronizationExtension)synch).beforeRollback();
	          } catch (RuntimeException re) {
	            if (LOCATION.beLogged(Severity.INFO)) {
	            	SimpleLogger.traceThrowable(Severity.INFO, LOCATION, "BeforeRollback notification called on "+synch+" failed with unexpected exception.", re);
	            }
	            reasonForSystemException = re;
	          }
	        }
	      }
	    }

	    try{
	      ResourceSet resourceSet = (ResourceSet)getAssociatedObjectWithTransaction(TXR_UserTransaction.UT_RESOURCE_SET_OBJECT_KEY);
	      if(resourceSet != null){
	        resourceSet.delistAll(XAResource.TMFAIL);
	      }
	    } catch (SystemException sysEx) {
	    	SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, sysEx, "ASJ.trans.000013", "TransactionManager was not able to delist resources from transaction because of unexpected exception. Probably rollback of the transaction will fail.");
	    }

	    try{
	      ResourceContext resourceContext = (ResourceContext)getAssociatedObjectWithTransaction(TXR_TransactionManagerImpl.RESOURCE_CONTEXT_OBJECT_KEY);
	      if(resourceContext != null){
	        resourceContext.exitMethod(TXR_TransactionManagerImpl.RESOURCE_CONTEXT_METHOD_NAME, false);
	      }
	    } catch (ResourceContextException rce){
//	    	SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "TransactionManager was not able to delist resources from transaction because of unexpected exception. Probably rollback of the transaction will fail.", rce);
	    	SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,rce, "ASJ.trans.000014", "TransactionManager was not able to delist resources from transaction because of unexpected exception. Probably rollback of the transaction will fail.");
	    }

	    status = Status.STATUS_ROLLING_BACK;

	    try {
	      if (localTX != null) {
	        try {
	          localTX.rollback();
	        } catch (RuntimeException re) {
	        	String rmName = UNKNOWN_LOCAL_RM_PREFIX + localTX.getClass().getName();	        	
	        	PendingRMInfo pendingRMInfo = new PendingRMInfo(true, null, 0, rmName, PendingRMInfo.ROLLBACK_FINISHED_WITH_UNEXPECTED_ERROR_FROM_LT, 
	        			getTransactionIdForLogs(), re, false, false);
	            reasonForSystemException = new SAPHeuristicHazardException(pendingRMInfo.getMessageForTrace(), re);
	            if(rmInfos == null) rmInfos = new ArrayList<PendingRMInfo>();
	            rmInfos.add(pendingRMInfo);
	            failedRMNames.add(rmName);
	        } catch (ResourceException re) {
	        	String rmName = UNKNOWN_LOCAL_RM_PREFIX + localTX.getClass().getName();	        	
	        	PendingRMInfo pendingRMInfo = new PendingRMInfo(true, null, 0, rmName, PendingRMInfo.ROLLBACK_FINISHED_WITH_UNEXPECTED_ERROR_FROM_LT, 
	        			getTransactionIdForLogs(), re, false, false);
	            reasonForSystemException = new SAPHeuristicHazardException(pendingRMInfo.getMessageForTrace(), re);
	            if(rmInfos == null) rmInfos = new ArrayList<PendingRMInfo>();
	            rmInfos.add(pendingRMInfo);
	            failedRMNames.add(rmName);
	        }
	      }

	      if (xaResourceWrapperList != null) {
	          for(XAResourceWrapper xaWrap: xaResourceWrapperList) {
	            try {
	              xaWrap.rollback();
	            } catch (RuntimeException re) {
	            	String rmName = getRMName(xaWrap.rmID, xaWrap.theXAResource);	
	            	PendingRMInfo pendingRMInfo = new PendingRMInfo(true, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.ROLLBACK_FINISHED_WITH_UNEXPECTED_ERROR_FROM_XARM,
				      		getTransactionIdForLogs(), re, false, false);
	            	reasonForSystemException = new SAPHeuristicHazardException(pendingRMInfo.getMessageForTrace(), re);
	            	if(rmInfos == null) rmInfos = new ArrayList<PendingRMInfo>();
	            	rmInfos.add(pendingRMInfo);
	            	failedRMNames.add(rmName);
	            } catch (XAException xae) {
	              int errorCode = xae.errorCode;
		          String rmName = getRMName(xaWrap.rmID, xaWrap.theXAResource);
		          if(rmInfos == null) rmInfos = new ArrayList<PendingRMInfo>();
	              
	              if (errorCode >= XAException.XA_RBBASE && errorCode <= XAException.XA_RBEND){
	            	  PendingRMInfo pendingRMInfo = new PendingRMInfo(false, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.ROLLBACK_FINISHED_WITH_XA_RBBASE_RBEND,
	            			  getTransactionIdForLogs(), xae, false, false);
	            	  rmInfos.add(pendingRMInfo);	            	  
	              } else if (errorCode == XAException.XA_HEURRB){	
	            	  PendingRMInfo pendingRMInfo = new PendingRMInfo(false, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.ROLLBACK_FINISHED_WITH_XA_HEURRB,
		    		      		getTransactionIdForLogs(), xae, false, !TransactionServiceFrame.callForgetAfterHeuristics);
	            	  reasonForSystemException = new HeuristicRollbackException(pendingRMInfo.getMessageForTrace());
	            	  reasonForSystemException.initCause(xae);
	    	          rmInfos.add(pendingRMInfo);            	  
	            	  xaWrap.forget();//real forget will be called only if it is configured. 
	              } else if (errorCode == XAException.XA_HEURCOM){
	            	  isHeuristicallyCompleted = true;
	            	  PendingRMInfo pendingRMInfo = new PendingRMInfo(true, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.ROLLBACK_FINISHED_WITH_XA_HEURCOM,
		    		      		getTransactionIdForLogs(), xae, false, !TransactionServiceFrame.callForgetAfterHeuristics); 
	            	  reasonForSystemException = new HeuristicMixedException(pendingRMInfo.getMessageForTrace());
	            	  reasonForSystemException.initCause(xae);
	    	          rmInfos.add(pendingRMInfo);
	            	  failedRMNames.add(rmName);	            	  
	            	  xaWrap.forget();//real forget will be called only if it is configured.	            	  
	              } else if (errorCode == XAException.XA_HEURMIX){
	            	  isHeuristicallyCompleted = true;
	            	  PendingRMInfo pendingRMInfo = new PendingRMInfo(true, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.ROLLBACK_FINISHED_WITH_XA_HEURMIX,
		    		      		getTransactionIdForLogs(), xae, false, !TransactionServiceFrame.callForgetAfterHeuristics);
	            	  reasonForSystemException = new HeuristicMixedException(pendingRMInfo.getMessageForTrace());
	            	  reasonForSystemException.initCause(xae);
	    	          rmInfos.add(pendingRMInfo);            	  
	            	  failedRMNames.add(rmName);	            	  
	            	  xaWrap.forget();//real forget will be called only if it is configured.	            	  
	              } else if (errorCode == XAException.XA_HEURHAZ){
	            	  isHeuristicallyCompleted = true;
	            	  PendingRMInfo pendingRMInfo = new PendingRMInfo(true, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.ROLLBACK_FINISHED_WITH_XA_HEURHAZ,
		    		      		getTransactionIdForLogs(), xae, false, !TransactionServiceFrame.callForgetAfterHeuristics);
	            	  reasonForSystemException = new SAPHeuristicHazardException(pendingRMInfo.getMessageForTrace());
	            	  reasonForSystemException.initCause(xae);
	            	  rmInfos.add(pendingRMInfo);
  	  		          failedRMNames.add(rmName);
  	  		          xaWrap.forget();//real forget will be called only if it is configured.
	              } else if (errorCode == XAException.XAER_RMFAIL){	            	         	  
	            	  DeleteTxRecordAtTheEnd = false;	            	  
	            	  PendingRMInfo pendingRMInfo = new PendingRMInfo(true, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.ROLLBACK_FINISHED_WITH_XAER_RMFAIL,
		    		      		getTransactionIdForLogs(), xae, true, false);
	            	  reasonForSystemException = new SAPHeuristicHazardException(pendingRMInfo.getMessageForTrace());	            	  
	            	  reasonForSystemException.initCause(xae);
	            	  rmInfos.add(pendingRMInfo);
		  		      failedRMNames.add(rmName);	            	  
	              } else if (errorCode == XAException.XAER_RMERR){
	            	  PendingRMInfo pendingRMInfo = new PendingRMInfo(true, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.ROLLBACK_FINISHED_WITH_XAER_RMERR,
		    		      		getTransactionIdForLogs(), xae, false, false);
	            	  reasonForSystemException = new SAPHeuristicHazardException(pendingRMInfo.getMessageForTrace());
	            	  reasonForSystemException.initCause(xae);
	            	  rmInfos.add(pendingRMInfo);
		  		      failedRMNames.add(rmName);	            	  	            	  
	              } else {//XA_NOTA, XA_INVAL, XA_PROTO, XA_ASYNC, UNKNOWN 
	            	  PendingRMInfo pendingRMInfo = new PendingRMInfo(true, xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.ROLLBACK_FINISHED_WITH_XA_OTHER,
		    		      		getTransactionIdForLogs(), xae, false, false);
	            	  reasonForSystemException = new SAPHeuristicHazardException(pendingRMInfo.getMessageForTrace());
	            	  reasonForSystemException.initCause(xae);
	            	  rmInfos.add(pendingRMInfo);
		  		      failedRMNames.add(rmName);	            	  	            	  
	              }
	            }
	          }
	        }

	      if (txControl != null) {
	        LinkedList omgResources = txControl.getOMGResources();
	        for(int i = 0; i < omgResources.size(); i++) {
	          try {
	            ((Resource)omgResources.get(i)).rollback();
	          } catch (RuntimeException re) {
	        	  String rmName = "External OMGResource " + ((Resource)omgResources.get(i)).getClass().getName();
	        	  PendingRMInfo pendingRMInfo = new PendingRMInfo(true, null, 0, rmName, PendingRMInfo.ROLLBACK_FINISHED_WITH_UNEXPECTED_ERROR_OMGRM,
	    		      		getTransactionIdForLogs(), re, false, false);
	        	  reasonForSystemException = new SAPHeuristicHazardException(pendingRMInfo.getMessageForTrace(), re);
	        	  if(rmInfos == null) rmInfos = new ArrayList<PendingRMInfo>();
	        	  rmInfos.add(pendingRMInfo);
	        	  failedRMNames.add(rmName);	            	
	          } catch (HeuristicMixed hm) {
	        	  String rmName = "External OMGResource " + ((Resource)omgResources.get(i)).getClass().getName();
	        	  PendingRMInfo pendingRMInfo = new PendingRMInfo(true, null, 0, rmName, PendingRMInfo.ROLLBACK_FINISHED_WITH_HEURISTIC_ERROR_OMGRM,
	    		      		getTransactionIdForLogs(), hm, false, false);
	        	  reasonForSystemException = new SAPHeuristicHazardException(pendingRMInfo.getMessageForTrace(), hm);
	        	  if(rmInfos == null) rmInfos = new ArrayList<PendingRMInfo>();
	        	  rmInfos.add(pendingRMInfo);
	        	  failedRMNames.add(rmName);	            	
	          } catch (HeuristicHazard hh) {
	        	  String rmName = "External OMGResource " + ((Resource)omgResources.get(i)).getClass().getName();
	        	  PendingRMInfo pendingRMInfo = new PendingRMInfo(true, null, 0, rmName, PendingRMInfo.ROLLBACK_FINISHED_WITH_HEURISTIC_ERROR_OMGRM,
	    		      		getTransactionIdForLogs(), hh, false, false);
	        	  reasonForSystemException = new SAPHeuristicHazardException(pendingRMInfo.getMessageForTrace(), hh);
	        	  if(rmInfos == null) rmInfos = new ArrayList<PendingRMInfo>();
	        	  rmInfos.add(pendingRMInfo);
	        	  failedRMNames.add(rmName);	            	
	          } catch (HeuristicCommit hc) {
	        	  String rmName = "External OMGResource " + ((Resource)omgResources.get(i)).getClass().getName();
	        	  PendingRMInfo pendingRMInfo = new PendingRMInfo(true, null, 0, rmName, PendingRMInfo.ROLLBACK_FINISHED_WITH_HEURISTIC_ERROR_OMGRM,
	    		      		getTransactionIdForLogs(), hc, false, false);
	        	  reasonForSystemException = new SAPHeuristicHazardException(pendingRMInfo.getMessageForTrace(), hc);
	        	  if(rmInfos == null) rmInfos = new ArrayList<PendingRMInfo>();
	        	  rmInfos.add(pendingRMInfo);
	        	  failedRMNames.add(rmName);
	        	  try{
	        		  ((Resource)omgResources.get(i)).forget();//real forget will be called only if it is configured.
	        	  } catch(Exception e){
	        		  SimpleLogger.traceThrowable(Severity.INFO, LOCATION, "Forget operaton on "+rmName+" failed. This is not fatal problem.", e);
	        	  }
	          }
	        }
	      }

	    } finally {
	      status = Status.STATUS_ROLLEDBACK;
	    //txRecordStoredInTLog must be false in 99.9999% of the cases
        //for DB TLog a notification is sent for optimization purposes. With this notification the logger knows that record with that ID will not be created.
	    // this optimization is not done  
	      if(DeleteTxRecordAtTheEnd){
	    	try {
				tLog.removeTransactionRecordLazily(transactionSequenceNumber);
			} catch (TLogIOException e) {
//				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "Due to unexpected exception TransactionManager was not able to remove transaction record for tx with ID = " + transactionSequenceNumber, e);
				SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e, "ASJ.trans.000015", "Due to unexpected exception TransactionManager was not able to remove transaction record for tx with ID = {0}",  new Object[] { transactionSequenceNumber});
			} catch (RuntimeException e) {
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, e, "ASJ.trans.000016","Due to unexpected exception TransactionManager was not able to remove transaction record for tx with ID = {0}", new Object[] { transactionSequenceNumber});
	        } 
	      }	      
	      
	      if(rmInfos != null && rmInfos.size()>0 && !isInboundTransaction){
	    	  PTLProcessor.addPendingTXData(new PendingTXData(transactionSequenceNumber, transactionClassifier, rmInfos, PendingTXData.ROLLBACK_FAILED));
	      }
	      
	      if (txControl != null) {
	        try {
	          ((SenderReceiverImpl)TransactionServiceImpl.getReceiver()).getImportedTx().remove(new TransactionKey(ByteArrayUtils.convertLongToByteArr(transactionSequenceNumber)));
	          txControl.disconnectFromORB();
	        } catch (RuntimeException e) {//almost impossible
	        	SimpleLogger.traceThrowable(Severity.DEBUG, LOCATION, "Unexpected exception occured during disconnectFromORB operation of external OMG resource manager", e);
	        }
	      }
          
	      for(Iterator<Synchronization> iterator = synchronizations.iterator(); iterator.hasNext();) {
	    	  Synchronization theListener = iterator.next(); 
	        try {
	        	theListener.afterCompletion(Status.STATUS_ROLLEDBACK);
	        } catch (RuntimeException e) {
		          if (LOCATION.beLogged(Severity.INFO)) {
		        	  SimpleLogger.traceThrowable(Severity.INFO, LOCATION, "Unexpected exception was thrown from "+theListener+" during afterCompletion notification.", e);
			      }
	        }	    	  
	      }

	      if (timeoutListener) {
	        try {
	          TransactionServiceFrame.getTimeoutManager().unregisterTimeoutListener(this);	
	        } catch (TimeOutIsStoppedException e) {
	          if (LOCATION.beLogged(Severity.DEBUG)) {
	            SimpleLogger.traceThrowable(Severity.DEBUG, LOCATION, "TransactionManager was not able to unregister transaction with id "+transactionSequenceNumber+" from timeout management.", e);
	          }
	        }
	      }
	      
	      if(removeSynchronizationStackWhenCompleted){
	    	   Stack synchronizationsStack = TransactionContextObject.getThransactionContextObject().getSynchronizationsStack();
	    	   if(!synchronizationsStack.isEmpty()){
	    		   synchronizationsStack.pop();
	    	   }
	      }	      

	      TransactionStatistics.transactionRolledback(this);
	      releaseMemory();
	      
	      if (reasonForSystemException != null) {
	        throw new BaseSystemException(ExceptionConstants.Exception_in_rollback, toString(), reasonForSystemException);
	      }
	    }
	  }

	  
	  
	  public void abandon(){
		  isAbandoned = true;
	  }
	  
//	  public Exception handleException(){
//	
//	  
//  	String rmName = getRMName(xaWrap.rmID, xaWrap.theXAResource);	
//	PendingRMInfo pendingRMInfo = new PendingRMInfo(xaWrap.xid, xaWrap.rmID, rmName, PendingRMInfo.ROLLBACK_FINISHED_WITH_UNEXPECTED_ERROR_FROM_XARM,
//      		new String[]{getTransactionIdForErrorReport(), Util.getStackTrace(re)}, false, false);
//	String messageForTrace = pendingRMInfo.getMessageForTrace();
//	SimpleLogger.traceThrowable(Severity.INFO, LOCATION, messageForTrace, re);
//	reasonForSystemException = new SAPHeuristicHazardException(messageForTrace, re);
//	if(rmInfos == null) rmInfos = new ArrayList<PendingRMInfo>();
//	rmInfos.add(pendingRMInfo);
//	failedRMNames.add(rmName);	  
//	  
//	  }
// ============= Transaction status management 	
	
	  /**
	   * Obtain the status of the transaction associated with the current thread.
	   *
	   * @return The transaction status. If no transaction is associated with
	   *    the current thread, this method returns the Status.NoTransaction
	   *    value.
	   *
	   * @exception SystemException Thrown if the transaction manager
	   *    encounters an unexpected error condition
	   *
	   */
	  public int getStatus() throws SystemException {
	    if (!isAlive()) {
	      return Status.STATUS_NO_TRANSACTION;
	    }

	    return status;
	  }

	  /**
	   * Modify the transaction associated with the current thread such that
	   * the only possible outcome of the transaction is to roll back the
	   * transaction.
	   *
	   * @exception IllegalStateException Thrown if the current thread is
	   *    not associated with any transaction.
	   *
	   * @exception SystemException Thrown if the transaction manager
	   *    encounters an unexpected error condition
	   *
	   */
	  public void setRollbackOnly() throws IllegalStateException, SystemException {
		  
		  setRollbackPosition = new Throwable(LogUtil.getFailedInComponentByCaller() + "Trace of setRollbackOnly() invocation.");
		  
		  if (LOCATION.beLogged(Severity.DEBUG)) {
	    	SimpleLogger.traceThrowable(Severity.DEBUG, LOCATION, "Ivocation of Transaction:"+getTransactionIdForLogs()+".setRollbackOnly()", setRollbackPosition);
	    }
		  if (isAlive()) {
			  status = Status.STATUS_MARKED_ROLLBACK;
		  }
	  }

	  /**
	   * Determines if the transaction is alive
	   *
	   * @return true if the transaction is still alive, else returns false
	   */
	  public boolean isAlive() {
	    if (status == Status.STATUS_NO_TRANSACTION || status == Status.STATUS_ROLLEDBACK || status == Status.STATUS_UNKNOWN || status == Status.STATUS_COMMITTED) {
	      return false;
	    } else {
	      return true;
	    }
	  }
	
  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  //                                      Used by the OTS implemetation                                   //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * This metod returns a Control object to this transaction. The Control object is
   * needed by the OTS implemetation. The method prevents the support of a hashtable
   * with relations Control -> JTATransaction
   */
  public Control getControl() {
    if (txControl == null) {
      txControl = new ControlImpl(this);
      return txControl;
    }
    return txControl;
  }	  
	  
//========== Special methods for recovery
	  
	public void writeTransactionRecord() throws SystemException{
		if(!TransactionServiceFrame.enableTransactionLogging){
			return;// to allow property change without restart of the server.
		}		
		tLog = getTLog();
		if(tLog == null){
			return;// transaction log is switched off
		}

		try {
			initAndWriteTransactionRecord(tLog);
			DeleteTxRecordAtTheEnd = true;
		} catch (TLogIOException e) {
			String message = "TransactionManager was not able to store transaction log for "
				+ getTransactionIdForLogs() + "and will rollback the transaction.";
//			SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, message, e);
			SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e, "ASJ.trans.000017", "TransactionManager was not able to store transaction log for {0} and will rollback the transaction.",  new Object[] { getTransactionIdForLogs()});
			SystemException sysEx = new SystemException(message);			
			sysEx.initCause(e);
			if((e.getCause() != null) && (e.getCause() instanceof InterruptedException)){
				try {
					tLog.removeTransactionRecordImmediately(transactionSequenceNumber);
				} catch (TLogIOException e1) {
//					SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "TransactionManager tried to write a transaction record for transaction" + getTransactionIdForLogs() +
//							"but write operation failed with InterruptedException. TransactionManager tried to remove the record but this operation also failed", e1);
					SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e1, "ASJ.trans.000018", "TransactionManager tried to write a transaction record for transaction {0} but write operation failed with InterruptedException. TransactionManager tried to remove the record but this operation also failed.",  new Object[] { getTransactionIdForLogs()});
				}
			}
			throw sysEx;			
		} catch (InvalidRMIDException e) {
			String message = "TransactionManager was not able to store transaction log for "
					+ getTransactionIdForLogs() + "and will rollback the transaction.";
//			SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, message, e);
			SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e, "ASJ.trans.000019", "TransactionManager was not able to store transaction log for {0} and will rollback the transaction.",  new Object[] { getTransactionIdForLogs()});
			SystemException sysEx = new SystemException(message);
			sysEx.initCause(e);
			throw sysEx;			
		} catch (InvalidTransactionClassifierID e) {
			String message = "TransactionManager was not able to store transaction log for "
				+ getTransactionIdForLogs() + "and will rollback the transaction.";
//			SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, message, e);
			SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e, "ASJ.trans.000020", "TransactionManager was not able to store transaction log for {0} and will rollback the transaction.",  new Object[] { getTransactionIdForLogs()});
			SystemException sysEx = new SystemException(message);
			sysEx.initCause(e);
			throw sysEx;
		}
	}

	protected TLog getTLog() {
		return TransactionServiceFrame.getTLog();
	}

	protected void initAndWriteTransactionRecord(TLog tlog) throws SystemException,
			TLogIOException, InvalidRMIDException,
			InvalidTransactionClassifierID {

		if (null != tlog ) {
			TransactionRecordImpl txRecord = new TransactionRecordImpl();
			initializeTransactionRecord(txRecord);
			tlog.writeTransactionRecord(txRecord);
		}
	}

	protected void initializeTransactionRecord(TransactionRecordImpl txRecord)
			throws SystemException {
		txRecord.setTransactionAbandonTimeout(ByteArrayUtils.getLongFromByteArray(mainXID.getGlobalTransactionId(), 15));
		txRecord.setTransactionBirthTime(transactionBirthTime);
		txRecord.setTransactionClassifierID(transactionClassifierID);
		txRecord.setTransactionSequenceNumber(transactionSequenceNumber);

		byte[] branchIterators = new byte[numberOfPreparedAndRecoverableRMs];
		int[] rmIDs = new int[numberOfPreparedAndRecoverableRMs];
		int arrayId = 0;
		for (XAResourceWrapper xaWrap : xaResourceWrapperList) {
			if (xaWrap.checkIfEligibleForRecovery()) {
				branchIterators[arrayId] = xaWrap.xid.getGlobalTransactionId()[43];
				rmIDs[arrayId] = xaWrap.rmID;
				arrayId++;
			}
		}
		if (arrayId < numberOfPreparedAndRecoverableRMs) {
			throw new SystemException(
					"TransactionManager was not able to store intofmation into transaction log due to internal error with calculation or resource managers which are eligible for recovery.");
		}

		txRecord.setBranchIterators(branchIterators);
		txRecord.setRMIDs(rmIDs);
	}

	public boolean changeDecisionToRollbackAndRemoveTransactionRecord() {
		tLog = getTLog();
		if(tLog == null){
			return true; // in this case tlog is switched off and there is no problem to change the decision
		}
		try {
			tLog.removeTransactionRecordImmediately(transactionSequenceNumber);
			DeleteTxRecordAtTheEnd = false;
			isRolledbackBecauseOfRMError = true;
			return true;
		} catch (TLogIOException e) {
			SimpleLogger.traceThrowable(Severity.WARNING, LOCATION, "Unexpected exception occurred when TransactionManager tried to remove transaction record for transaction:"+getTransactionIdForLogs()+". This exception is not fatal.", e);
			return false;
		}
	}

	public void setTransactionClassifier(String classifier) throws NotSupportedException {
		if(rejectSetTransactionClassifier){
			throw new NotSupportedException("It is not possible to set Transaction classifier because there are resources which are already enlisted into transaction.");
		}
		if(transactionClassifierAlreadySet){
			throw new NotSupportedException("It is not possible to set Transaction classifier because another classifier is already set.");			
		}
		transactionClassifier = classifier;	
		tLog = getTLog();
		if(tLog == null){//tLog id switched off.
			return;
		}
		
		try {
			transactionClassifierID = tLog.getIdForTxClassifier(classifier);
		} catch (TLogIOException e) {
			NotSupportedException nspe = new NotSupportedException("It is not possible to set transaction classifier because of unexpected exception.");
            nspe.initCause(e);
            throw nspe;
		} catch (TLogFullException e) {
			NotSupportedException nspe = new NotSupportedException("It is not possible to set transaction classifier because of unexpected exception.");
            nspe.initCause(e);
            throw nspe;
		}
		transactionClassifierByteArray = ByteArrayUtils.convertIntToByteArr(transactionClassifierID);
		transactionClassifierAlreadySet = true;// to prevent double invocation of this method. 
	}
	
	public void setDCName(String dcName) {
		if (dcName!=null && !dcName.equals("")) {
			this.dcName = dcName;
		} else {
			this.dcName = null;
		}
	}

	public String getTransactionClassifier() {
		if (null == resultingTxClassifier) {
			StringBuilder builder = new StringBuilder();
			if (transactionClassifier!=null && !transactionClassifier.equals("")) {
				builder.append(transactionClassifier);
				builder.append(" ");
			}
			if (null!=dcName) {
				builder.append("[DC Name: ");
				builder.append(dcName);
		      	String csnComponent = LoggingUtilities.getCsnComponentByDCName(dcName);
				if (csnComponent!=null && !csnComponent.equals("")) {
					builder.append(", ");
					builder.append(csnComponent);
				}
				builder.append("]");
			}
			resultingTxClassifier = builder.toString();
		}
		return resultingTxClassifier;
	}
	
	
	public void removeSynchronizationStackWhenCompleted() {
		removeSynchronizationStackWhenCompleted = true; 	
	}	
	
	protected String getTransactionIdForLogs(){
		if(transactionClassifierAlreadySet){
			return transactionClassifier + " with ID:" + transactionSequenceNumber + " ";
		} else {
			return "with ID " + transactionSequenceNumber+ " ";
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 * This method is called frequently from logging API and performance is important.
	 */
	public String toString() {		
	  if (txToString == null) {
	      StringBuffer buffer = new StringBuffer();
	      buffer.append("JTA Transaction : ");
	      buffer.append(transactionSequenceNumber);
	      txToString = buffer.toString();
	  }
	  
	  return txToString;
	}	
	
	private void releaseMemory(){
		associatedObjects = null;
		localResourceReference = null;	
		setRollbackPosition = null;
		mainXID = null;
		transactionClassifierByteArray = null;
		xaConnectionsList = null;
		synchronizations = null;
		listeners = null;
		txControl = null;
		txToString = null;		
	}
//=========== Methods inherited from TimeoutListener interface 	
	
  /**
   * If returns true <code>timeout()</code> will be called
   */
   public boolean check() {
     return true;
   }

   /**
   * This method is called from Timeout manager when active transaction timeout
   *  is reached. Transaction will be rolledback.  
   */
   public synchronized void timeout() {
     if (LOCATION.beLogged(Severity.DEBUG)) {
    	 SimpleLogger.trace(Severity.DEBUG, LOCATION, "TransactionManager will rollback transaction:"+getTransactionIdForLogs()+" because active timout is reached.");
     }
     if (status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK) {
       try {
         isTimeouted = true;
         rollback_internal(false);
       } catch (SystemException e) {
    	   SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "Exception occurrec when TransactionManager tried to rollback transaction:"+getTransactionIdForLogs()+" because of timeout.", e);
       }
     }
   }

   public void associateObject(Object obj) {
		this.associatedObject = obj;
	}

	public Object getAssociateObject() {
		return this.associatedObject;
	}
	
//============================= Utility methods =======================
   
   
   public static String convertXAErrorCodeToString(int errorCode){
	   
	   switch(errorCode){
	   case XAException.XA_HEURCOM : return "XA_HEURCOM";
	   case XAException.XA_HEURHAZ : return "XA_HEURHAZ";
	   case XAException.XA_HEURMIX : return "XA_HEURMIX";
	   case XAException.XA_HEURRB : return "XA_HEURRB"; 	   
	   case XAException.XA_NOMIGRATE : return "XA_NOMIGRATE"; 	    
	   case XAException.XA_RBCOMMFAIL : return "XA_RBCOMMFAIL"; 
	   case XAException.XA_RBDEADLOCK : return "XA_RBDEADLOCK";
	   case XAException.XA_RBINTEGRITY : return "XA_RBINTEGRITY"; 
	   case XAException.XA_RBOTHER : return "XA_RBOTHER"; 
	   case XAException.XA_RBPROTO : return "XA_RBPROTO";
	   case XAException.XA_RBROLLBACK : return "XA_RBROLLBACK";
	   case XAException.XA_RBTIMEOUT : return "XA_RBTIMEOUT";
	   case XAException.XA_RBTRANSIENT : return "XA_RBTRANSIENT";
	   case XAException.XA_RDONLY : return "XA_RDONLY";
	   case XAException.XA_RETRY : return "XA_RETRY";
	   case XAException.XAER_ASYNC : return "XAER_ASYNC";
	   case XAException.XAER_DUPID : return "XAER_DUPID";
	   case XAException.XAER_INVAL : return "XAER_INVAL";
	   case XAException.XAER_NOTA : return "XAER_NOTA";
	   case XAException.XAER_OUTSIDE : return "XAER_OUTSIDE";
	   case XAException.XAER_PROTO : return "XAER_PROTO";
	   case XAException.XAER_RMERR : return "XAER_RMERR";
	   case XAException.XAER_RMFAIL : return "XAER_RMFAIL";
 	   default : return "Unknown error code " +  errorCode + " ";  
	   }
   }

   private String convertXAReturnCodesToString(int returnCode){
	   switch(returnCode){
	   case XAResource.TMFAIL : return "TMFAIL";
	   case XAResource.TMONEPHASE : return "TMONEPHASE";
	   case XAResource.TMSUCCESS : return "TMSUCCESS";
	   case XAResource.XA_OK : return "XA_OK";
	   case XAResource.XA_RDONLY : return "XA_RDONLY";
	   default : return "Unknown XAResource code " +  returnCode + " " ;  
	   }
	   
   }

   
   //======================================== TRANSACTION STATISTICS ============================================
   public String[] getAllNames_of_FAILED_RMs() {
	return failedRMNames.toArray(new String[]{});
   }

   public String[] getAllRMNames() {
	   ArrayList<String> result = new ArrayList<String>();
	   if(xaResourceWrapperList != null) {
		   for(XAResourceWrapper xaWrap : xaResourceWrapperList) {
	         result.add(getRMName(xaWrap.rmID,xaWrap.theXAResource));
		   }
	   }
	   if(localTX != null){
		   result.add(UNKNOWN_LOCAL_RM_PREFIX + localTX.getClass().getName());
	   }
	    return result.toArray(new String[]{});
   }

   public long getCommitOrRollbackDurationInMillis() {
	// commit and rollback duration is not yet supported
	return 0;
   }

   public boolean isAbandoned() {
	return isAbandoned;
   }

   public boolean isHeuristicallyCompleted() {
	return isHeuristicallyCompleted;
   }

   public boolean isRolledbackByApplication() {	
	   return isRolledbackByApplication;
   }

   public boolean isRolledbackBecauseOfRMError() {
	   return isRolledbackBecauseOfRMError;
   }
   public boolean isTimeouted() {	
	return isTimeouted;
   }

   public int getFinishStatus() {
	return status;
   }

   private String getRMName(int rm_id, XAResource xaResourceInstance){
		 if(rm_id == 0){
			 return "Unknown XA RM " + xaResourceInstance;
		 } else {
			 TLog tLog = getTLog();
			 String rmName = null;
			 if(tLog != null){
				try {
					rmName = tLog.getRMName(rm_id);
				} catch (Exception e) {
			         if (LOCATION.beLogged(Severity.WARNING)) {
			           LOCATION.traceThrowableT(Severity.WARNING, "Unexpected exception occured during calculation of transaction statistics. It is not possible to get the name of RM with ID " + rm_id, e);
			         }					
				}				
			 }			 
			 if(rmName != null){
				 return rmName;
			 } else {
				 return "Unknown XA RM " + xaResourceInstance.getClass().getName();
			 }
		 }	   
   }
}
