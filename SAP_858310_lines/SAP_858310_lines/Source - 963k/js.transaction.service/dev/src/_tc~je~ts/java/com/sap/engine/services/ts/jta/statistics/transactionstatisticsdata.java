package com.sap.engine.services.ts.jta.statistics;

import com.sap.engine.services.ts.mbeans.SAP_ITSAMTransactionStatisticsData;
import com.sap.engine.services.ts.recovery.PTLProcessor;

public class TransactionStatisticsData {
	
	public TransactionStatisticsData() {	
	}

	private String name = null;

	private long totalCommittedTx = 0;

	private long totalRollbackedTx = 0;

	private long activeTxCount = 0;
	
	private long suspendedTxCount = 0;

	private long txWithHeuristicOutcomesCount = 0;

	private long txPassedAbandonTimeout = 0;

	private long txRollbackedByApplication = 0;

	private long txRollbackedBecauseRMError = 0;

	private long timeoutedTxCount = 0;
		
	private long notRecoveredTxCount = 0;

	private int averageCommitTime = -1;


	public SAP_ITSAMTransactionStatisticsData createITSAMData(){
		return new SAP_ITSAMTransactionStatisticsData(name, PTLProcessor.getPendingForCompletionTxCount(),
				totalCommittedTx + totalRollbackedTx,totalCommittedTx,
				totalRollbackedTx, activeTxCount, suspendedTxCount,
				txWithHeuristicOutcomesCount, txPassedAbandonTimeout,
				txRollbackedByApplication, txRollbackedBecauseRMError,
				timeoutedTxCount, averageCommitTime, notRecoveredTxCount,
				null, null, null);
				// TODO set correct Caption,Description, ElementName);
	}
	

    
	
	public void increaseActiveTxCount(){
		activeTxCount++;
	}
	
	public void decreaseActiveTxCount(){
	    //prevent negative value - use a snapshot instead of a synchronization
	    long localActiveTxCount = activeTxCount;
	    if (--localActiveTxCount >= 0) {
	    	activeTxCount = localActiveTxCount;
	    } else {
	    	activeTxCount = 0;
	    }		
	}
	
	public void increaseSuspendedTxCount(){
		suspendedTxCount++;
	}
	
	public void decreaseSuspendedTxCount(){
	    //prevent negative value - use a snapshot instead of a synchronization
	    long localSuspendedTxCount = suspendedTxCount;
	    if (--localSuspendedTxCount >= 0) {
	    	suspendedTxCount = localSuspendedTxCount;
	    } else {
	    	suspendedTxCount = 0;
	    }			
	}
	
	public void increaseTotalCommittedTxCount(){
		totalCommittedTx++;
	}
	
	public void increaseTotalRollbackedTxCount(){
		totalRollbackedTx++;
	}
     
	public void increaseTxWithHeuristicOutcomesCount(){
		txWithHeuristicOutcomesCount++;
	}
  
	public void increaseAbandonedTxCount(){
		txPassedAbandonTimeout++;
	}
	public void increaseTxRollbackedByApplication(){
		txRollbackedByApplication++;
	}
	
	public void increaseTxRollbackedBecauseRMError(){
		txRollbackedBecauseRMError++;
	}
	
	public void increaseTimeoutedTxCount(){
		timeoutedTxCount++;
	}
	
	public void increaseNotRecoveredTxCount(){
		notRecoveredTxCount++;
	}
	
	public void clearNotRecoveredTxCount(){
		notRecoveredTxCount = 0;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public long getRollbackTransactionsCount() {
	    return totalRollbackedTx;
	}

	public long getCommitedTransactionsCount() {
		return totalCommittedTx;
	}

	public long getTimedoutTransactionsCount() {
		return timeoutedTxCount;
	}

	public int getActiveTransactionsCount() {
		return (int)activeTxCount;
	}

	public int getSuspendedTransactionsCount() {
		return (int)suspendedTxCount;
	}
}
