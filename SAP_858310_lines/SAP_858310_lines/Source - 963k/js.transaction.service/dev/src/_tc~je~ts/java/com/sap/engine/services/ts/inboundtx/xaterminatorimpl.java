package com.sap.engine.services.ts.inboundtx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.resource.spi.XATerminator;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.sap.engine.interfaces.transaction.TransactionExtension;
import com.sap.engine.services.ts.Log;
import com.sap.engine.services.ts.LogUtil;
import com.sap.engine.services.ts.TransactionContextObject;
import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.exceptions.BaseNotSupportedException;
import com.sap.engine.services.ts.exceptions.ExceptionConstants;
import com.sap.engine.services.ts.exceptions.TimeOutIsStoppedException;
import com.sap.engine.services.ts.jta.impl2.SAPXidImpl;
import com.sap.engine.services.ts.jta.impl2.TXR_TransactionImpl;
import com.sap.engine.services.ts.jta.impl2.TXR_TransactionManagerImpl;
import com.sap.engine.services.ts.recovery.RMContainerRegistryImpl;
import com.sap.engine.services.ts.tlog.InboundTLog;
import com.sap.engine.services.ts.tlog.InboundTransactionRecord;
import com.sap.engine.services.ts.tlog.InvalidRMKeyException;
import com.sap.engine.services.ts.tlog.RMPropsExtension;
import com.sap.engine.services.ts.tlog.TLogIOException;
import com.sap.engine.services.ts.tlog.TLogReaderWriter;
import com.sap.engine.services.ts.utils.ByteArrayUtils;
import com.sap.engine.services.ts.utils.TLogVersion;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

/**
 * 
 * XATerminator implementation which supports recover
 * 
 * @author Dimitar Iv. Dimitrov
 * @version SAP NetWeaver 7.11 SP0
 */
public class XATerminatorImpl implements XATerminator {

	private static final Location LOCATION = Location.getLocation(XATerminatorImpl.class);

	private final AtomicLong txSequenceNumberCounter = new AtomicLong(0);
	private final TXR_TransactionManagerImpl txManager;

	private final RMIDsMapper rmIDsMapper;

	private final Map<Xid, JCATransaction> transactionsRegistry =
		new ConcurrentHashMap<Xid, JCATransaction>();

	private Map<Xid, InboundTransactionRecord> pendingXIDs = null;

	private InboundTLog tLog;
	private final TLogVersion tLogVersion;


	// constructor(s)
	public XATerminatorImpl(TXR_TransactionManagerImpl txManager) {
		this.txManager = txManager;

		byte[] sysId = TransactionServiceFrame.serviceContext
				.getClusterContext().getClusterMonitor().getClusterName()
				.getBytes();
		int nodeId = TransactionServiceFrame.serviceContext.getClusterContext()
				.getClusterMonitor().getCurrentParticipant().getClusterId();
		this.tLogVersion = new TLogVersion(sysId, nodeId, 0);

		rmIDsMapper = new RMIDsMapper(getInboundTLog());
	}


	// interface method(s)
	public void commit(Xid xid, boolean onePhase) throws XAException {
		JCATransaction tx = transactionsRegistry.get(xid);
		if (null!=tx) {
			try {
				commit(xid, onePhase, tx);

			} finally {
				synchronized (this) {
					if (pendingXIDs!=null) {
						pendingXIDs.remove(xid);
					}
				}
			}

		} else {
			InboundTransactionRecord rec = null;

			synchronized (this) {
				if (pendingXIDs == null) {
					initPendingTx();
				}
				rec = pendingXIDs.remove(xid);
			}

			if (null == rec) {
				throw new XAException(XAException.XAER_NOTA);
			}

			completeTransaction(rec, true, false);//First true means that commit will be called, second false means to not call forget.
		}
	}

	public void forget(Xid xid) throws XAException {
		JCATransaction tx = transactionsRegistry.get(xid);
		if (null!=tx) {
			try {
				forget(xid, tx);

			} finally {
				synchronized (this) {
					if (pendingXIDs!=null) {
						pendingXIDs.remove(xid);
					}
				}
			}

		} else {
			InboundTransactionRecord rec = null;

			synchronized (this) {
				if (pendingXIDs == null) {
					initPendingTx();
				}
				rec = pendingXIDs.remove(xid);
			}

			if (null == rec) {
				throw new XAException(XAException.XAER_NOTA);
			}

			forget(rec);
		}
	}

	public int prepare(Xid xid) throws XAException {
		JCATransaction tx = transactionsRegistry.get(xid);

		if (null==tx || !tx.isAlive()) {
			throw new XAException(XAException.XAER_NOTA);
		}

		if (!tx.activateInThread()) {
	        throw new XAException(XAException.XAER_PROTO);
		}

		try {
			return tx.prepare();
		} finally {
			tx.inactivateInThread();
		}
	}

	public Xid[] recover(int flag) throws XAException {
		// flag parameter is ignored and in all cases we will return all pending xid from transaction logs. 

		synchronized (this) {
			initPendingTx();
		}		
		rollbackNotPreparedBranches(pendingXIDs.values().toArray(new InboundTransactionRecord[]{}));
		return pendingXIDs.keySet().toArray(new Xid[pendingXIDs.size()]);
	}

	public void rollback(Xid xid) throws XAException {
		JCATransaction tx = transactionsRegistry.get(xid);
		if (null!=tx) {
			try {
				rollback(xid, tx);

			} finally {
				synchronized (this) {
					if (pendingXIDs!=null) {
						pendingXIDs.remove(xid);
					}
				}
			}

		} else {
			InboundTransactionRecord rec = null;

			synchronized (this) {
				if (pendingXIDs == null) {
					initPendingTx();
				}
				rec = pendingXIDs.remove(xid);
			}

			if (null == rec) {
				throw new XAException(XAException.XAER_NOTA);
			}

			completeTransaction(rec, false, false);//false means rollback, second false means to not call forget.			
		}
	}

	public void beginJCATransaction(Xid xid, long timeout)
			throws NotSupportedException, SystemException {

		if (LOCATION.beLogged(Severity.DEBUG)) {
			SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000118", "XATerminatorImpl.beginJCATransaction({0}, {1})",new Object[] { Log.objectToString(xid),Long.toString(timeout) });
		}

		TransactionContextObject transactionContextObject = TransactionContextObject
				.getThransactionContextObject();
		TransactionExtension txOld = transactionContextObject.getTransaction();
		JCATransaction jcaTx = transactionsRegistry.get(xid);

		if (txOld != null && txOld.isAlive()) {
			// this thread already has an exception. This case should never happen
			if (LOCATION.beLogged(Severity.DEBUG)) {
				SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000122", "Thread already associated with transaction.");
			}
			throw new BaseNotSupportedException(
					ExceptionConstants.Thread_already_has_transaction,
					new Object[] {LogUtil.getDCNameByCaller(), LogUtil.getCSNComponentByCaller()});

		} else if (jcaTx != null) {
			// activate the transaction
			if (!jcaTx.activateInThread()) {
				throw new NotSupportedException(
				        LogUtil.getFailedInComponentByCaller() + "Transaction with same XID already active in other thread. XID: "
						+ xid.toString());
			}

		} else {
			if (timeout > 0) { // CTS 1.4 for JCA 1.5 registers with timeout =
				// -1
				jcaTx = new JCATransaction(xid, true,
						txSequenceNumberCounter.incrementAndGet(),
						txManager);
				try {
					TransactionServiceFrame.getTimeoutManager()
							.registerTimeoutListener(jcaTx, timeout * 1000, 0);
				} catch (TimeOutIsStoppedException e) {
					if (LOCATION.beLogged(Severity.WARNING)) {
						SimpleLogger.trace(Severity.WARNING,LOCATION, "ASJ.trans.000123", "TransactionManagerImpl.beginJCATransaction timeout service is not started. There is no transaction timeout.");
					}
					if (LOCATION.beLogged(Severity.DEBUG)) {
						LOCATION.traceThrowableT(Severity.DEBUG,
								"Full stacktrace: ", e);
					}
				}
			} else {
				jcaTx = new JCATransaction(xid, false,
						txSequenceNumberCounter.incrementAndGet(),
						txManager);
			}

			transactionsRegistry.put(xid, jcaTx);
		}

		transactionContextObject.setTransaction(jcaTx);
	}

	public RMIDsMapper getRMIDsMapper() {
		return rmIDsMapper;
	}

	public InboundTLog getInboundTLog() {
		if (tLog != null) {
			return tLog;
		}

		if (!TransactionServiceFrame.enableTransactionLogging) {

			return null; // there is no transaction log in this case
		}

		TLogReaderWriter tLogReaderWriter = TransactionServiceFrame
				.getTLogReaderWriter();
		if (null == tLogReaderWriter) {
			return null;
		}

		synchronized (XATerminatorImpl.class) {
			if (null == tLog) {
				try {
					tLog = tLogReaderWriter.getInboundTLog(tLogVersion);
//					tLog.getAllUsedRMIDs();
				} catch (TLogIOException e) {
					SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e,"ASJ.trans.000113","Inbound transaction log cannot be created because of unexpected exception.");
				}
			}
		}

		return tLog;
	}


	// helpful method(s)
	private void commit(Xid xid, boolean onePhase, JCATransaction tx)
			throws XAException {
		if (!tx.isAlive()) {
			throw new XAException(XAException.XAER_NOTA);
		}

		if (!tx.activateInThread()) {
			throw new XAException(XAException.XAER_PROTO);
		}

		try {
			tx.commit(onePhase);

		} catch (SecurityException e) {
			XAException xaEx = new XAException(XAException.XAER_RMFAIL);
			xaEx.initCause(e);
			throw xaEx;

		} catch (IllegalStateException e) {
			XAException xaEx = new XAException(XAException.XAER_NOTA);
			xaEx.initCause(e);
			throw xaEx;

		} catch (RollbackException e) {
			XAException xaEx = new XAException(XAException.XA_RBROLLBACK);
			xaEx.initCause(e);
			throw xaEx;

		} catch (HeuristicMixedException e) {
			XAException xaEx = new XAException(XAException.XA_HEURMIX);
			xaEx.initCause(e);
			throw xaEx;

		} catch (HeuristicRollbackException e) {
			XAException xaEx = new XAException(XAException.XA_HEURRB);
			xaEx.initCause(e);
			throw xaEx;

		} catch (SystemException e) {
			XAException xaEx = new XAException(XAException.XAER_RMFAIL);
			xaEx.initCause(e);
			throw xaEx;

		} finally {
			tx.inactivateInThread();
			transactionsRegistry.remove(xid);
		}
	}

	private void completeTransaction(InboundTransactionRecord rec, boolean isCommit, boolean isForget) throws XAException {
		XAException xaExceptionToThrow = null;// only one exception will be thrown
		int[] rmIDs = rec.getRMIDs();
		byte[] branchIterators = rec.getBranchIterators();
		
		RMContainerRegistryImpl rmContainerRegistryImpl = TransactionServiceFrame.getRmContainerRegistryImpl();
		if(rmContainerRegistryImpl == null){
			XAException xae = new XAException("Registry for containers that are responsible for resource managers is not initialized.");
			xae.errorCode = XAException.XA_RETRY;
			throw xae;
		}		
		
		for(int i=0; i<rmIDs.length; i++){
			int rmID = rmIDs[i];
			RMPropsExtension rmProps = null;
			try {
				rmProps = tLog.getRMProperties(rmID);
			} catch (InvalidRMKeyException e) {
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "Resource manager with id="+rmID+" is not available into inbound transaction log. Inbound transaction cannot be completed. ", e);
				xaExceptionToThrow = new XAException(XAException.XAER_RMFAIL);
			} catch (TLogIOException e) {
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "Cannot complete inbound transactions because of unexpected exception from inbound transaction log.", e);
				xaExceptionToThrow = new XAException(XAException.XAER_RMFAIL);
			}

			rmContainerRegistryImpl.fillRMPropertiesWithXAResources(new RMPropsExtension[]{rmProps});
			if(rmProps.getXAResource() != null){
				try{
					if(isForget){
						rmProps.forget(createXid(rec, rmID, branchIterators[i]));
					} else if(isCommit){
						rmProps.commitPendingTx(createXid(rec, rmID, branchIterators[i]), false);
					} else {
						rmProps.rollbackPendingTx(createXid(rec, rmID, branchIterators[i]));
					}
				} catch (RuntimeException e){
					SimpleLogger.traceThrowable(Severity.INFO, LOCATION, "TransactionManager tried to complete pending transaction but Exception was thrown.", e);
					xaExceptionToThrow = new XAException(XAException.XAER_RMFAIL);
					xaExceptionToThrow.initCause(e);
				} catch (XAException xaEx){
					int errorCode = xaEx.errorCode;
					// implementation is not precise in case of retry return codes. Retry will not be done and branch will be pending. It will be rolled back during next recovery call.
					SimpleLogger.traceThrowable(Severity.INFO, LOCATION, "TransactionManager tried to complete pending transaction but XAException was thrown with error code " + TXR_TransactionImpl.convertXAErrorCodeToString(errorCode), xaEx);
					if(xaExceptionToThrow != null || rmIDs.length>1){// we have a mixture of problems
						XAException prevEx = xaExceptionToThrow;
						xaExceptionToThrow = new XAException(XAException.XA_HEURHAZ);
						if(prevEx != null){
							xaExceptionToThrow.initCause(prevEx);
						}
					} else {						
						xaExceptionToThrow = xaEx;
					}
				}
			} else {
				xaExceptionToThrow = new XAException("Transaction cannot be completed because resource manager "+rmProps.getKeyName()+" is temporary unavailable.");
			}
		}
			
		try {
			tLog.removeTransactionRecordLazily(rec.getTransactionSequenceNumber());
		} catch (TLogIOException e) {
			// rare case. Transaction is committed and this is not fatal exception.
			SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "Unexpected exception occurred when inbound TransactionManager tried to remove transaction record for inbound transaction.", e);
		}
		
		if(xaExceptionToThrow != null){
			throw xaExceptionToThrow;
		}
	}

	private void rollbackNotPreparedBranches(InboundTransactionRecord[] inboundRecords) throws XAException{
		
		XAException xaExceptionToThrow = null;
		
		RMContainerRegistryImpl rmContainerRegistryImpl = TransactionServiceFrame.getRmContainerRegistryImpl();
		if(rmContainerRegistryImpl == null){
			XAException xae = new XAException("Registry for containers that are responsible for resource managers is not initialized.");
			xae.errorCode = XAException.XA_RETRY;
			throw xae;
		}		
		
		Xid[] preparedXids = new Xid[inboundRecords.length];
		ArrayList<Integer> allRmIds = new ArrayList<Integer>();
		
		for(int i = 0; i<preparedXids.length; i++){
			preparedXids[i] = createXid(inboundRecords[i], 0, (byte)0);
			int[] rmIds = inboundRecords[i].getRMIDs();
			for(int rmId: rmIds){
				if(!allRmIds.contains(rmId)){
					allRmIds.add(rmId);
				}
			}
		}
		
		for(int rmID : allRmIds){
			RMPropsExtension rmProps = null;
			try {
				rmProps = tLog.getRMProperties(rmID);
			} catch (InvalidRMKeyException e) {
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "Resource manager with id="+rmID+" is not available into inbound transaction log. Some pending inbound transaction branches cannot be rolled back. ", e);
				xaExceptionToThrow = new XAException(XAException.XAER_RMFAIL);
				xaExceptionToThrow.initCause(e);
			} catch (TLogIOException e) {
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "Some pending inbound transaction branches cannot be rolled because of following exception.", e);
				xaExceptionToThrow = new XAException(XAException.XAER_RMFAIL);
				xaExceptionToThrow.initCause(e);
			}

			rmContainerRegistryImpl.fillRMPropertiesWithXAResources(new RMPropsExtension[]{rmProps});
			try{
				if(rmProps.getXAResource() != null){
					Xid[] pendingRMXids = rmProps.getXAResource().recover(XAResource.TMSTARTRSCAN | XAResource.TMENDRSCAN);
					for(Xid pendingRMXid : pendingRMXids){
						boolean found = false;
						for (Xid preparedXid : preparedXids){
							if(pendingRMXid.getFormatId() == preparedXid.getFormatId() &&
									pendingRMXid.getGlobalTransactionId() != null && pendingRMXid.getGlobalTransactionId().length > 43 &&
									preparedXid.getGlobalTransactionId() != null && preparedXid.getGlobalTransactionId().length > 43){
								//have to ignore branch iterators in comparison.
							    byte[] preparedTxid = new byte[43];
							    System.arraycopy(preparedXid.getGlobalTransactionId(), 0, preparedTxid, 0, 43);
							    byte[] pendingTxid = new byte[43];
							    System.arraycopy(pendingRMXid.getGlobalTransactionId(), 0, pendingTxid, 0, 43);
							    if(Arrays.equals(preparedTxid, pendingTxid)){
							    	found = true;
							    	break;
							    }
							}
						}
						if(!found){ // Xid is not prepared and we have to rollback it
							rmProps.rollbackPendingTx(pendingRMXid);						
						}
					}
					
				} else {
					SimpleLogger.trace(Severity.ERROR, LOCATION, "Cannot rollback pending inbound transaction branches because resource manager "+rmProps.getKeyName()+" is not available.");
				}
			} catch (XAException e){
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "Cannot rollback pending inbound transaction branches because resource manager "+
						rmProps.getKeyName()+" throws XAException with status " + TXR_TransactionImpl.convertXAErrorCodeToString(e.errorCode), e);
			} catch (RuntimeException e){
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "Cannot rollback pending inbound transaction branches because resource manager "+
						rmProps.getKeyName()+" throws unexpected exception.", e);
			}
		}
	}
	
	private void forget(Xid xid, JCATransaction tx) throws XAException {
		//Heuristic result from commit or rollback operation will be returned only if there is only one RM. 
		//Forget method will be called in rare cases.
		if (!tx.isAlive()) {
			throw new XAException(XAException.XAER_NOTA);
		}

		if (!tx.activateInThread()) {
	        throw new XAException(XAException.XAER_PROTO);
		}

		try {
			tx.forget();
			InboundTLog tlog = getInboundTLog();
			if (null != tlog) {
				tlog.removeTransactionRecordImmediately(tx.getID());
			}

		} catch (TLogIOException e) {
			XAException xaEx = new XAException(XAException.XAER_RMERR);
			xaEx.initCause(e);
			throw xaEx;

		} finally {
			tx.inactivateInThread();
			transactionsRegistry.remove(xid);
		}
	}

	private void forget(InboundTransactionRecord rec) throws XAException {
		
		completeTransaction(rec, false, true);// this method will call forget on all XAResources. 

		try {
			InboundTLog tlog = getInboundTLog();
			if (null != tlog) {
				tlog.removeTransactionRecordImmediately(rec.getTransactionSequenceNumber());
			}
		} catch (TLogIOException e) {
			XAException xaEx = new XAException(XAException.XAER_RMERR);
			xaEx.initCause(e);
			throw xaEx;
		}
	}

	private void rollback(Xid xid, JCATransaction tx) throws XAException {
		if (!tx.isAlive()) {
			throw new XAException(XAException.XAER_NOTA);
		}

		if (!tx.activateInThread()) {
	        throw new XAException(XAException.XAER_PROTO);
		}

		try {
			tx.rollback();

		} catch (IllegalStateException e) {
			XAException xaEx = new XAException(XAException.XAER_NOTA);
			xaEx.initCause(e);
			throw xaEx;

		} catch (SystemException e) {
			XAException xaEx = new XAException(XAException.XAER_RMFAIL);
			xaEx.initCause(e);
			throw xaEx;

		} finally {
			tx.inactivateInThread();
			transactionsRegistry.remove(xid);
		}

	}

	/**
	 * Initialize pendingXIDs instance field according to the TLog if it's
	 * enabled or with empty HashMap if TLog is disabled.
	 * 
	 * PRECONDITION: hold this instance monitor (synchronized (this))
	 * 
	 * @throws XAException
	 *             if TLog throw exception
	 */
	private void initPendingTx() throws XAException {
		try {
			InboundTLog inboundTLog = getInboundTLog();
			if (null != inboundTLog) {
				pendingXIDs = inboundTLog.recover();

			} else { // no recovery
				pendingXIDs = new HashMap<Xid, InboundTransactionRecord>();
			}
		} catch (TLogIOException e) {
			XAException xaEx = new XAException(XAException.XAER_RMFAIL);
			xaEx.initCause(e);
			throw xaEx;
		}
	}
	
	private Xid createXid(InboundTransactionRecord rec, int rmID, byte branchIterator){
		
		byte[] globalTransactionID = new byte[44];
		byte[] tlogVersion = tLog.getTLogVersion().getTLogVersion();//15
		System.arraycopy(tlogVersion, 0, globalTransactionID, 0, 15);
		ByteArrayUtils.addLongInByteArray(rec.getTransactionAbandonTimeout(), globalTransactionID, 15);//8 abandon timeout
		ByteArrayUtils.addIntInByteArray(rec.getTransactionClassifierID(), globalTransactionID, 23);//23 = 15+8; 4 classifier ID
		ByteArrayUtils.addLongInByteArray(rec.getTransactionBirthTime(), globalTransactionID, 27);//27= 23+4;  8 bytes for tx birth time
		ByteArrayUtils.addLongInByteArray(rec.getTransactionSequenceNumber(), globalTransactionID, 35);// 35=27+8; 8 bytes for tx sequence number
		globalTransactionID[43] = branchIterator;// 1 byte for branch iterator 35+8+1 = 44					
		
		byte[] branchQualifier = new byte[2];					
		branchQualifier[0] = (byte)(rmID >> 8);
		branchQualifier[1] = (byte)rmID;	
		
		return new SAPXidImpl(globalTransactionID, branchQualifier);
	}
	
}
