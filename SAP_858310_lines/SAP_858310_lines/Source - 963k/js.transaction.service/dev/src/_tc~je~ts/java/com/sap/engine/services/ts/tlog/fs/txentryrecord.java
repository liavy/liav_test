package com.sap.engine.services.ts.tlog.fs;

import java.nio.ByteBuffer;

import com.sap.engine.services.ts.tlog.TransactionRecord;
import com.sap.engine.services.ts.tlog.TransactionRecordImpl;

public class TXEntryRecord extends TXRecord {
	public static final int VAR_SIZE_PER_RM = 5;
	public static final int CONST_SIZE_PER_TX_REC = 4 + 8 + 1 + 8 + CRLF.length + 8 + 8 + 4 + 4;

	public static final int SEQUENCE_NUMBER_INDEX = 4 + 8 + 1;
	public static final int RM_IDS_COUNT_INDEX = 4 + 8 + 1 + 8 + CRLF.length + 8 + 8 + 4;

	protected static int calculateTxRecordSize(TransactionRecord rec) {
		return CONST_SIZE_PER_TX_REC + (rec.getRMIDs().length * VAR_SIZE_PER_RM);
	}

	public static ByteBuffer writeRecord(TransactionRecord txRecord) {
		int txRecSize = TXEntryRecord.calculateTxRecordSize(txRecord);
		ByteBuffer buffer = ByteBuffer.allocate(txRecSize);

		int start = 0;

		TXEntryRecord.writeTXRecordIntoByteBuffer(txRecord, buffer, TXRecord.TX_ENTRY_RECORD_TYPE);

		//get length
		int end = buffer.position();

		//finish
		Record.completeData(buffer, start, end);

		buffer.flip();
		return buffer;
	}

	public static TransactionRecord readRecord(ByteBuffer buf) {
		TransactionRecordImpl rec = new TransactionRecordImpl();
		TXEntryRecord.readTXRecordFromByteBuffer(buf, rec, TXRecord.TX_ENTRY_RECORD_TYPE);
		return rec;
	}


	protected static void readTXRecordFromByteBuffer(ByteBuffer buf, TransactionRecordImpl rec, byte txRecType) {
		// skip the buffer header
		buf.rewind();	// go to the beginning of the buffer
		buf.getInt();	// size of the record... we don't need it here
		buf.getLong();	// checksum ... must be checked during reading in the ByteBuffer

		byte type = buf.get();
		if(type != txRecType) {
			throw new RuntimeException("Error in determine the type of TXRecord.");
		}

		// read the body of record
		long txSequenceNumber = buf.getLong();
		buf.get(); buf.get(); // for crlf
		
		long txBirthTime = buf.getLong();
		
		long txAbandonTimeout = buf.getLong();
		
		int txClassifierID = buf.getInt();
		
		int rmCount = buf.getInt();
		
		int[] rmIDs = new int[rmCount];
		for (int i = 0; i < rmIDs.length; i++) {
			rmIDs[i] = buf.getInt();
		}
		
		byte[] brIterators = new byte[rmCount];
		for (int i = 0; i < brIterators.length; i++) {
			brIterators[i] = buf.get();
		}

		rec.setBranchIterators(brIterators);
		rec.setRMIDs(rmIDs);
		rec.setTransactionAbandonTimeout(txAbandonTimeout);
		rec.setTransactionBirthTime(txBirthTime);
		rec.setTransactionClassifierID(txClassifierID);
		rec.setTransactionSequenceNumber(txSequenceNumber);
	}

	protected static void writeTXRecordIntoByteBuffer(
			TransactionRecord txRecord, ByteBuffer buffer,
			byte txRecType) {
		//put length of body
		buffer.putInt(0);

		//reserved for checksum
		buffer.putLong(0);
		
		buffer.put(txRecType);
		
		//put tsn
		buffer.putLong(txRecord.getTransactionSequenceNumber());
		
		//to be human readable in editor?
		for (byte b : CRLF) {
			buffer.put(b);
		}
		
		//put tx birth time
		buffer.putLong(txRecord.getTransactionBirthTime());
		
		//put tx abandon time
		buffer.putLong(txRecord.getTransactionAbandonTimeout());
		
		//put global tx classifier
		buffer.putInt(txRecord.getTransactionClassifierID());
		
		//put no. of resources and branch iterators count
		int[] rmIds = txRecord.getRMIDs();

		buffer.putInt(rmIds.length);

		for (int i = 0; i < rmIds.length; i++) {
			buffer.putInt(rmIds[i]);
		}
		
		byte[] branchIterators = txRecord.getBranchIterators();
		
		for (int i = 0; i < branchIterators.length; i ++) {
			buffer.put(branchIterators[i]);
		}
	}
}
