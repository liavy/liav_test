package com.sap.engine.services.ts.tlog;

import javax.transaction.xa.Xid;

public interface InboundTransactionRecord extends TransactionRecord {

	/**
	 * Returns the external XID for the transaction
	 * 
	 * @return the external XID for the transaction
	 */
	public Xid getExternalXID();

	/**
	 * If the transaction is heuristically completed return the heuristic
	 * outcome code and 0 otherwise
	 * 
	 * @return the heuristics outcome code with which the transaction was
	 *         completed. 0 if it haven't been heuristically completed.
	 */
	public int getHeuristicOutcome();
}
