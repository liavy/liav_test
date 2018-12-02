package com.sap.engine.services.ts.recovery;

import com.sap.engine.services.ts.utils.TLogVersion;

public class PendingTXDataKey{
	
	protected long transactionSequenceNumber = 0;
	protected TLogVersion tLogVersion = null;	
	

	public PendingTXDataKey(){
		//fields are set from child class
	}
	
	public PendingTXDataKey(byte[] systemId,int nodeId,	long tmStartupTime, long transactionSequenceNumber){
		this.transactionSequenceNumber = transactionSequenceNumber;
		this.tLogVersion = new TLogVersion(systemId, nodeId, tmStartupTime);
	}
	
    public int hashCode(){
    	return (int)transactionSequenceNumber;
    }

    public boolean equals(Object obj) {
    	if(!(obj instanceof PendingTXDataKey)){
    		return false;
    	}
    	PendingTXDataKey otherPendingTXDataKey = (PendingTXDataKey)obj;
    	
    	if(otherPendingTXDataKey.tLogVersion.equals(tLogVersion) &&
    			otherPendingTXDataKey.transactionSequenceNumber == transactionSequenceNumber){
    	   return true;	
    	}
    	return false;
    }	
    
}
