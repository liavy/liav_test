package com.sap.engine.services.ts.inboundtx;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.sap.engine.interfaces.transaction.LocalTxProvider;
import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.exceptions.BaseSystemException;
import com.sap.engine.services.ts.exceptions.ExceptionConstants;
import com.sap.engine.services.ts.jta.impl2.TXR_TransactionImpl;
import com.sap.engine.services.ts.jta.impl2.TXR_TransactionManagerImpl;
import com.sap.engine.services.ts.jta.impl2.XAResourceWrapper;
import com.sap.engine.services.ts.tlog.InboundTLog;
import com.sap.engine.services.ts.tlog.InboundTransactionRecordImpl;
import com.sap.engine.services.ts.tlog.InvalidRMIDException;
import com.sap.engine.services.ts.tlog.InvalidTransactionClassifierID;
import com.sap.engine.services.ts.tlog.TLog;
import com.sap.engine.services.ts.tlog.TLogIOException;
import com.sap.engine.services.ts.tlog.TransactionRecordImpl;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

/**
 * 
 * JCATransaction class represent a single JCA transaction throughout it's
 * life-cycle except the time after a server crash when a prepared JCA
 * transaction is represented by InboundTransactionRecord
 * 
 * @author Dimitar Iv. Dimitrov
 * @version SAP NetWeaver 7.11 SP0
 */
public class JCATransaction extends TXR_TransactionImpl {

	private static final Location LOCATION = Location.getLocation(JCATransaction.class);		

	private final Xid externalXID;
	private int heuristicOutcome = XAResource.XA_OK;
	private final AtomicBoolean activeInThread = new AtomicBoolean(true);

	public JCATransaction(Xid externalXID, boolean timeoutListener,
			long txSequenceNumber,
			TXR_TransactionManagerImpl tmImpl) {
		super(tmImpl, txSequenceNumber, timeoutListener);
		this.externalXID = externalXID;
	}

	public int prepare() throws XAException {
		
		try {
			checkIfReadyForCommit();
			beforePrepare();
			status = Status.STATUS_PREPARING;
			prepareRMs();
			status = Status.STATUS_PREPARED;
			
			if(numberOfPreparedAndRecoverableRMs >0 && xaResourceWrapperList != null){
				try {
					writeTransactionRecord();
				} catch (Exception e) {
					String message = "Transaction:"
							+ getTransactionIdForLogs()
							+ " was rolledback because of exception during write into transaction log.";
					RollbackException rollbackException = new RollbackException(
							message);
					rollbackException.initCause(e);
//					SimpleLogger.traceThrowable(Severity.ERROR, LOCATION,
//							message, e);
					SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e, "ASJ.trans.000108", "Transaction:{0} was rolledback because of exception during write into transaction log.",  new Object[] { getTransactionIdForLogs()});
					rollback_internal(true);
					throw rollbackException;
				}
				return XAResource.XA_OK;
			} else {
				return XAResource.XA_RDONLY;
			}		
		} catch (IllegalStateException e) {			
			SimpleLogger.traceThrowable(Severity.ERROR, LOCATION,e,"ASJ.trans.000109", "Inbound TransactionManager was not able to prepare transaction {0} because it was committed or rolled back.", new Object[] {getTransactionIdForLogs()});
			XAException xae = new XAException(XAException.XAER_NOTA);
			xae.initCause(e);
			throw xae;
		} catch (RollbackException e) {
			SimpleLogger. traceThrowable(Severity.ERROR, LOCATION,e,"ASJ.trans.000110", "Inbound TransactionManager directly rolled back transaction {0} instead of preparing it.", new Object[] {getTransactionIdForLogs()});
			XAException xae = new XAException(XAException.XA_RBROLLBACK);
			xae.initCause(e);
			throw xae;
		} catch (SystemException e) {
			SimpleLogger.traceThrowable(Severity.ERROR, LOCATION,e,"ASJ.trans.000111", "Inbound TransactionManager was not able o prepare transaction {0}. ", new Object[] {getTransactionIdForLogs()});
			XAException xae = new XAException(XAException.XAER_RMERR);
			xae.initCause(e);
			throw xae;
		}
	}

    public void enlistLocalResource(LocalTxProvider localRef) throws SystemException {
		  if(TransactionServiceFrame.enableLocalResourceInOTS){
		     super.enlistLocalResource(localRef); 
		  } else {
		     throw new BaseSystemException(ExceptionConstants.Enlist_of_local_resource_in_OTS_Transaction_Not_Allowed, "ENABLE_LOCAL_RESOURCE_IN_OTS");
		  }
	  }

    public boolean enlistResource(int rmID, XAResource xaRes)
			throws RollbackException, IllegalStateException, SystemException {

    	int mappedID = tm.getXATerminator().getRMIDsMapper()
				.mapOutboundRMIDToInboundRMID(rmID);
		return super.enlistResource(mappedID, xaRes);
    }

	public void commit(boolean onePhase) throws RollbackException,
			HeuristicMixedException, HeuristicRollbackException,
			SecurityException, IllegalStateException, SystemException {
		if(onePhase){
			super.commit();			
		} else {	
			commitSecondPhase(false, true);
		}
	}

	public void rollback() throws IllegalStateException, SystemException {
		  if (LOCATION.beLogged(Severity.DEBUG)) {
			SimpleLogger.trace(Severity.DEBUG, LOCATION, "Call: " + this + ".rollback()");  
		  }		
		  isRolledbackByApplication = true;
		  rollback_internal(true);
	}	
	
	protected void forget() throws XAException {
        if (xaResourceWrapperList != null) {        	
            for(XAResourceWrapper xaWrap : xaResourceWrapperList) {
            	xaWrap.forceForget();
            }
        }
	}
	
	@Override
	protected InboundTLog getTLog() {
		return tm.getXATerminator().getInboundTLog();
	}

	@Override
	protected void initAndWriteTransactionRecord(TLog tlog) throws SystemException,
			TLogIOException, InvalidRMIDException,
			InvalidTransactionClassifierID {

		if (null != tlog) {
			InboundTransactionRecordImpl txRecord = new InboundTransactionRecordImpl();
			initializeTransactionRecord(txRecord);
			((InboundTLog)tlog).writeInboundTransactionRecord(txRecord);
		}
	}

	@Override
	protected void initializeTransactionRecord(TransactionRecordImpl txRecord)
			throws SystemException {

		if (!(txRecord instanceof InboundTransactionRecordImpl)) {
			throw new RuntimeException("Invalid input parameter " + txRecord);
		}

		super.initializeTransactionRecord(txRecord);
		InboundTransactionRecordImpl rec = (InboundTransactionRecordImpl)txRecord;
		rec.setExternalXID(externalXID);
		rec.setHeuristicOutcome(heuristicOutcome);
	}

	/**
	 * Mark this JCA transaction as active in a thread
	 * 
	 * @return return true if the activation has succeed
	 */
	public boolean activateInThread() {
		return activeInThread.compareAndSet(false, true);
	}

	/**
	 * Mark this JCA transaction as inactive in a thread
	 */
	public void inactivateInThread() {
		activeInThread.set(false);
	}

	@Override
	public String toString() {
		  if (txToString == null) {
		      StringBuffer buffer = new StringBuffer();
		      buffer.append("Inbound JCA Transaction : ");
		      buffer.append(transactionSequenceNumber);
		      txToString = buffer.toString();
		  }
		  
		  return txToString;
	}
}
