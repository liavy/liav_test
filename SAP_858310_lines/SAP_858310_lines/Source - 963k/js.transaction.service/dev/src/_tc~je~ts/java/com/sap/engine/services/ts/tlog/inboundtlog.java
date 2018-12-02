package com.sap.engine.services.ts.tlog;

import java.util.Map;

import javax.transaction.xa.Xid;

public interface InboundTLog extends TLog {

	/**
	 * Write inbound transaction record into this TLog
	 * 
	 * @param record
	 *            the inbound transaction record to be written
	 * @throws TLogIOException
	 *             when unexpected IOException or SQLException occurred.
	 * @throws InvalidRMIDException
	 *             when some of the provided resource manager IDs are not valid.
	 * @throws InvalidTransactionClassifierID
	 *             when the transaction classifier is not valid
	 */
	public void writeInboundTransactionRecord(InboundTransactionRecord record)
			throws TLogIOException, InvalidRMIDException,
			InvalidTransactionClassifierID;

	/**
	 * Return the external XIDs of all successfully prepared transactions
	 * 
	 * @return the external XIDs of all successfully prepared transactions
	 * @throws TLogIOException
	 *             when unexpected IOException or SQLException occurred.
	 */
	public Map<Xid, InboundTransactionRecord> recover() throws TLogIOException;

	/**
	 * 
	 * @param txSeqNumber
	 * @param heuristicOutcome
	 * @throws TLogIOException
	 */
	public void setHeuristicOutcome(long txSeqNumber, int heuristicOutcome) throws TLogIOException;
}
