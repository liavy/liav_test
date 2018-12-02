	package com.sap.engine.services.ts.recovery;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.transaction.xa.XAException;

import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.exceptions.TimeOutIsStoppedException;
import com.sap.engine.services.ts.facades.timer.TimeoutListener;
import com.sap.engine.services.ts.mbeans.SAP_ITSAMPendingTransactionData;
import com.sap.engine.services.ts.tlog.TLog;
import com.sap.engine.services.ts.tlog.TLogIOException;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;


public class PTLProcessor implements Runnable, TimeoutListener {
	
	private static Hashtable<PendingTXDataKey, PendingTXData> pendingTransactions = new Hashtable<PendingTXDataKey, PendingTXData>();  
	private static final Location LOCATION = Location.getLocation(PTLProcessor.class);	
	
	private static PTLProcessor ptlProcessorInstance = null;
	private boolean registeredAsTimeoutListener = false;
	private long registeredTimeout = 0;
	
	private Object associatedObject = null;
	
	public synchronized static void addPendingTXData(PendingTXData pendingTx){	
		PendingTXData prevPendingTXData = pendingTransactions.put(pendingTx, pendingTx);
		if(prevPendingTXData != null){
			throw new IllegalArgumentException("Due to internal error one transaction was added twice into pending transaction list");
		} 	
		if(pendingTx.isOperationForRetry()){
			PTLProcessor.getPTLInstance().registerAsTimeoutListener();
		}
	}
	
	public synchronized static SAP_ITSAMPendingTransactionData[] getPendingTransactions(){
		ArrayList<SAP_ITSAMPendingTransactionData> result = new ArrayList<SAP_ITSAMPendingTransactionData>();
		for(PendingTXData pendingTxData : pendingTransactions.values()){
			result.add(pendingTxData.getITSAMPendingTxData());			
		}
		return result.toArray(new SAP_ITSAMPendingTransactionData[]{});
	}	
	
	public synchronized static PendingTXData getPendingTxInfo(byte[] systemId,
			int nodeId,	long tmStartupTime,long transactionSeqNumber){
		PendingTXDataKey key = new PendingTXDataKey(systemId, nodeId, tmStartupTime, transactionSeqNumber);
		return pendingTransactions.get(key);
	}
	
	public synchronized static int getPendingForCompletionTxCount(){
		int result = 0;
		for(PendingTXData pendingTxData : pendingTransactions.values()){
			if(pendingTxData.isOperationForRetry()){
				result ++;
			}			
		}
		return result;
	}
	
	//TODO register in timeout for retry or start a new thread if timeout is not available 
	
	public synchronized static boolean completePendingTransactions() {
		TLog tLog = TransactionServiceFrame.getTLog();
		if(tLog == null){
			SimpleLogger.trace(Severity.INFO, LOCATION, "Transaction log is not available and pending transactions cannot be completed.");
			return false;			
		}
		boolean result = true;
		Hashtable<PendingTXDataKey, PendingTXData> pendingTransactionsCopy = (Hashtable<PendingTXDataKey, PendingTXData>)pendingTransactions.clone();
		for(PendingTXData pendingTxData : pendingTransactionsCopy.values()){			
			if(pendingTxData.isOperationForRetry()){
				boolean operationResult = false;
				try {
					operationResult = pendingTxData.finishTransaction(tLog);
				} catch (XAException e) {
					SimpleLogger.traceThrowable(Severity.INFO, LOCATION, "Exception was thrown when TransactionManager tried to complete pending transactions.", e);
				} catch (RuntimeException e) {
					SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "Unexcpected exception was thrown when TransactionManager tried to complete pending transactions.", e);					
				}
				if(operationResult){
					pendingTransactions.remove(pendingTxData);
				}
				result = result && operationResult;
			}					
		}	
		return result;
	} 
	
	public synchronized static boolean abandonTransaction(byte[] systemId,
			int nodeId,	long tmStartupTime,long transactionSeqNumber){
		PendingTXDataKey key = new PendingTXDataKey(systemId, nodeId, tmStartupTime, transactionSeqNumber);
		
		PendingTXData pendingTXData = pendingTransactions.remove(key);		
		TLog tLog = TransactionServiceFrame.getTLog();
		if(tLog != null && pendingTXData != null && pendingTXData.isOperationForRetry()){
			try {
				tLog.removeTransactionRecordLazily(pendingTXData.transactionSequenceNumber);
			} catch (TLogIOException e) {
				SimpleLogger.trace(Severity.ERROR,LOCATION,"Unexpected exception occurred when TransactionManager tried to remove transaction record for a transaction which was pending.");
			}			
		}
		return pendingTXData != null;
	}
	
	public synchronized static boolean forgetTransaction(byte[] systemId,
			int nodeId,	long tmStartupTime,long transactionSeqNumber){
		TLog tLog = TransactionServiceFrame.getTLog();
		if(tLog == null){
			SimpleLogger.trace(Severity.INFO, LOCATION, "Operation 'forgetTransaction' cannot complete successfully because transaction log is not available.");
			return false;			
		}		
		PendingTXDataKey key = new PendingTXDataKey(systemId, nodeId, tmStartupTime, transactionSeqNumber);
		PendingTXData pendingTXData = pendingTransactions.get(key);	
		if(pendingTXData != null){
			pendingTXData.forgetTransaction(tLog);
			pendingTransactions.remove(key);
			return true;
		}
		return false;
	}

	private PTLProcessor(){
		
	}
	
	private static PTLProcessor getPTLInstance(){
		if(ptlProcessorInstance != null){
			return ptlProcessorInstance;
		}
		synchronized(PTLProcessor.class){
			if(ptlProcessorInstance != null){
				return ptlProcessorInstance;
			}
			ptlProcessorInstance = new PTLProcessor();
			return ptlProcessorInstance;
		}
	}
	
	public synchronized void run() {
		boolean success = false; 
		do {
			try {
				Thread.sleep(TransactionServiceFrame.recoveryRetryInterval * 1000);
			} catch (InterruptedException e) {
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "Exception occurred during Thread.sleep between recovery retry.", e);
			}
			success = completePendingTransactions();
				
		} while (success);
		registeredAsTimeoutListener = false;
	}

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
		registeredAsTimeoutListener = false;
		try{
			if(!completePendingTransactions()){
				registerAsTimeoutListener();
			}
		}catch (Exception e){
			SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "Unexpected exception occurred when TransactionManager tried to complete pending transactions.", e);
			registerAsTimeoutListener();
		}
	}

	public void associateObject(Object obj) {
		this.associatedObject = obj;
	}

	public Object getAssociateObject() {
		return this.associatedObject;
	}
	
	private synchronized void registerAsTimeoutListener(){
		if(registeredAsTimeoutListener && (registeredTimeout <= TransactionServiceFrame.recoveryRetryInterval)){
			return;
		}
        try {
           registeredTimeout = TransactionServiceFrame.recoveryRetryInterval;
           if(registeredAsTimeoutListener){
        	   TransactionServiceFrame.getTimeoutManager().unregisterTimeoutListener(this);
           }
           TransactionServiceFrame.getTimeoutManager().registerTimeoutListener(this, registeredTimeout*1000, 0);
     } catch (TimeOutIsStoppedException e) {        	
   	 	if (LOCATION.beLogged(Severity.WARNING)) {
             SimpleLogger.trace(Severity.WARNING, LOCATION, "TimeoutManager is stopped and will start new Thread for recovery retry.", e);
        }
        // This is extremely exceptional case 
        (new Thread(this)).start();
     }
     registeredAsTimeoutListener = true;
   }
	     
	
	//TODO abandon(forget) transactions which are for abandon after some time

}
