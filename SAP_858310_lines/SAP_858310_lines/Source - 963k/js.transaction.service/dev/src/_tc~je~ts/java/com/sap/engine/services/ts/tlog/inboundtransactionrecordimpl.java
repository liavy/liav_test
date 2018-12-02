package com.sap.engine.services.ts.tlog;

import javax.transaction.xa.Xid;

public class InboundTransactionRecordImpl extends TransactionRecordImpl implements InboundTransactionRecord {

	private Xid externalXID;
	private int heuristicOutcome = 0;

	public void setExternalXID(Xid externalXID) {
		this.externalXID = externalXID;
	}

	public Xid getExternalXID() {
		return externalXID;
	}

	public void setHeuristicOutcome(int heuristicOutcome) {
		this.heuristicOutcome = heuristicOutcome;
	}

	public int getHeuristicOutcome() {
		return heuristicOutcome;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;

		if (!(other instanceof InboundTransactionRecordImpl)) {
			return false;
		}

		InboundTransactionRecordImpl o = (InboundTransactionRecordImpl)other;

		return super.equals(other) &&
			this.externalXID.equals(o.externalXID) &&
			this.heuristicOutcome == o.heuristicOutcome;
	}

	public int hashCode(){
		return System.identityHashCode(this);// just for JLin compliance. This class will not be used in hashtables. 
	}	
}
