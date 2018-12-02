package com.sap.engine.services.ts.tlog;

import java.util.Arrays;

import com.sap.engine.services.ts.jta.impl2.SAPXidImpl;

/**
 * @author I024163
 *
 */
public class TransactionRecordImpl implements TransactionRecord {

	
	private long transactionAbandonTimeout = SAPXidImpl.defaultTxAbandonTimeout;
	private long transactionBirthTime = -1;
	private int transactionClassifierID = 0;//0 means that transaction has no specified classifier.
	private long transactionSequenceNumber = -1;
	private byte[] branchIterators = null;
	private int[] rmIDs = null; 

	public TransactionRecordImpl(){
	}

	public long getTransactionAbandonTimeout() {
		return transactionAbandonTimeout;      
	}
	
	public void setTransactionAbandonTimeout(long txAbandonTimeout){
		this.transactionAbandonTimeout = txAbandonTimeout;
	}

	public long getTransactionBirthTime() {
		return transactionBirthTime;
	}

	public void setTransactionBirthTime(long txBirthTime){
		this.transactionBirthTime = txBirthTime;
	}
	
	public int getTransactionClassifierID() {
		return transactionClassifierID;
	}
	
	public void setTransactionClassifierID(int txClassifierID){
		this.transactionClassifierID = txClassifierID;		
	}

	public long getTransactionSequenceNumber() {
		return transactionSequenceNumber;
	}
	
	public void setTransactionSequenceNumber(long txSequenceNumber){
		this.transactionSequenceNumber = txSequenceNumber;
	}

	public byte[] getBranchIterators() {		
		return branchIterators;
	}

	public void setBranchIterators(byte[] brIterators){
		this.branchIterators = brIterators;
	}
	
	public int[] getRMIDs() {
		return rmIDs;
	}
	
	public void setRMIDs(int[] rmIDs){
		this.rmIDs = rmIDs;
	}
	
	public int hashCode(){
		return System.identityHashCode(this);// just for JLin compliance. This class will not be used in hashtables. 
	}	
	
    public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof TransactionRecordImpl)) {
			return false;
		}
		TransactionRecordImpl otherTxRecord = (TransactionRecordImpl) obj;

		return transactionAbandonTimeout == otherTxRecord.transactionAbandonTimeout
				&& transactionBirthTime == otherTxRecord.transactionBirthTime
				&& transactionClassifierID == otherTxRecord.transactionClassifierID
				&& transactionSequenceNumber == otherTxRecord.transactionSequenceNumber
				&& Arrays.equals(branchIterators, otherTxRecord.branchIterators)
				&& Arrays.equals(rmIDs, otherTxRecord.rmIDs);
	}			
	
}
