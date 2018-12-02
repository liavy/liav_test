package com.sap.engine.services.ts.tlog.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.xa.Xid;

import com.sap.engine.frame.core.thread.execution.Executor;
import com.sap.engine.services.ts.facades.timer.TimeoutManager;
import com.sap.engine.services.ts.tlog.InboundTLog;
import com.sap.engine.services.ts.tlog.InboundTransactionRecord;
import com.sap.engine.services.ts.tlog.InvalidRMIDException;
import com.sap.engine.services.ts.tlog.InvalidTransactionClassifierID;
import com.sap.engine.services.ts.tlog.TLogIOException;
import com.sap.engine.services.ts.tlog.TransactionRecord;
import com.sap.engine.services.ts.tlog.util.TLogLocking;
import com.sap.engine.services.ts.utils.TLogVersion;

public class InboundFSTLog extends FSTLog implements InboundTLog {

	public InboundFSTLog(TLogVersion tLogVersion, File directory,
			TLogLocking tlogLock,
			Executor flusherThreadExecutor, int bufferCapacity,
			long maxTLogFileSize, File[] txFiles) throws FileNotFoundException {

		super(tLogVersion, directory, tlogLock,
				flusherThreadExecutor, bufferCapacity, maxTLogFileSize, txFiles);
	}

	public InboundFSTLog(TLogVersion tLogVersion, File directory,
			TLogLocking tlogLock,
			Executor flusherThreadExecutor,
			ConcurrentHashMap<Long, ByteBuffer> activeRecords,
			ClassifiersMap classifiers, Map<Integer, RMEntryRecord> rmEntries,
			Set<RMRemoveRecord> rmRemoveRecords, int[] rmIDs, int countIds,
			int bufferCapacity, long maxTLogFileSize, File[] txFiles,
			long initialFileSize, int currentFileName)
			throws FileNotFoundException {
		super(tLogVersion, directory, tlogLock,
				flusherThreadExecutor, activeRecords, classifiers, rmEntries,
				rmRemoveRecords, rmIDs, countIds, bufferCapacity,
				maxTLogFileSize, txFiles, initialFileSize, currentFileName);
	}


	public Map<Xid, InboundTransactionRecord> recover() throws TLogIOException {
		if (isClosed) {
			throw new TLogIOException("This tlog is already closed.");
		}

		Map<Long, ByteBuffer> actRecs = tLogOptimizator.getActiveRecords();
		Map<Xid, InboundTransactionRecord> result =
			new HashMap<Xid, InboundTransactionRecord>(actRecs.size());

		for (ByteBuffer buf : actRecs.values()) {
			InboundTransactionRecord r = InboundTXEntryRecord.readInboundRecord(buf);
			result.put(r.getExternalXID(), r);
		}

		return result;
	}

	public void setHeuristicOutcome(long txSeqNumber, int heuristicOutcome)
			throws TLogIOException {
		if (isClosed) {
			throw new TLogIOException("This tlog is already closed.");
		}

		ByteBuffer rec = tLogOptimizator.getActiveRecords().get(txSeqNumber);
		if (null==rec) {
			throw new TLogIOException("Invalid transaction sequence number: "
					+ txSeqNumber);
		}

		int rmCount = rec.getInt(TXEntryRecord.RM_IDS_COUNT_INDEX);
		int hoo = (rmCount * InboundTXEntryRecord.VAR_SIZE_PER_RM)
					+ InboundTXEntryRecord.HEURISTIC_OUTCOME_OFFSET;
		rec.putInt(hoo, heuristicOutcome);
		Record.completeData(rec, 0, rec.limit());
		rec.flip();
		tLogOptimizator.writeTransactionRecord(txSeqNumber, rec);
	}

	public void writeInboundTransactionRecord(InboundTransactionRecord record)
			throws TLogIOException, InvalidRMIDException,
			InvalidTransactionClassifierID {
		if (isClosed) {
			throw new TLogIOException("This tlog is already closed.");
		}
		if(record == null) {
			throw new IllegalArgumentException("Input cannot be null.");
		}

		int transactionClassifierID = record.getTransactionClassifierID();
		if(transactionClassifierID != 0 && !classifiers.containsId(transactionClassifierID)) {
			throw new InvalidTransactionClassifierID("Invalid transaction classifier ID: " + transactionClassifierID);
		}

		int[] rmids = record.getRMIDs();
		for (int i = 0; i < rmids.length; i++) {
			int id = rmids[i];
			if (Arrays.binarySearch(rmIDs, id) < 0) {
				throw new InvalidRMIDException("Resource manager ID " + id + " is not valid.");
			}
		}

		ByteBuffer buffer = InboundTXEntryRecord.writeInboundRecord(record);
		tLogOptimizator.writeTransactionRecord(record.getTransactionSequenceNumber(), buffer);
	}

	@Override
	public Iterator<TransactionRecord> getAllTransactionRecords() throws TLogIOException {
		if (isClosed) {
			throw new TLogIOException("This tlog is already closed.");
		}

		Map<Long, ByteBuffer> actRecs = tLogOptimizator.getActiveRecords();
		List<TransactionRecord> result = new ArrayList<TransactionRecord>(actRecs.size());

		for (ByteBuffer buf : actRecs.values()) {
			result.add(InboundTXEntryRecord.readInboundRecord(buf));
		}

		return result.iterator();
	}

	@Override
	public void writeTransactionRecord(TransactionRecord transactionRecord) {
		// This function is not supported for the InboundFSTLog. Use
		// writeInboundTransactionRecords instead.
		throw new RuntimeException(
				"Internal error: Unsupported function in InboundFSTLog. "
				+ "Use writeInboundTransactionRecord() instead.");
	}
}
