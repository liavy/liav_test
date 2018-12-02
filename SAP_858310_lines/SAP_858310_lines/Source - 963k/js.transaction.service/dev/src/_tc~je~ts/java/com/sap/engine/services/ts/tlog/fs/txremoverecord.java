package com.sap.engine.services.ts.tlog.fs;

import java.nio.ByteBuffer;

public class TXRemoveRecord extends TXRecord {
	private TXRemoveRecord() {
	}

	private static final int VAR_SIZE_PER_RM = 8;
	private static final int CONST_SIZE_PER_TX_REC = 4 + 8 + 1 + 4 + CRLF.length;

	protected static int calculateTxRecordSize(int compensationRecords) {
		return CONST_SIZE_PER_TX_REC + (compensationRecords * VAR_SIZE_PER_RM);
	}

	public static ByteBuffer writeRecord(long[] txRecords, int size) {
		int txRecSize = TXRemoveRecord.calculateTxRecordSize(size);
		ByteBuffer buffer = ByteBuffer.allocate(txRecSize);

		int start = 0;
		
		//put length
		buffer.putInt(0);
		
		//reserved for checksum
		buffer.putLong(0);
		
		buffer.put(TX_REMOVE_RECORD_TYPE);
	
		//put number of records
		buffer.putInt(size);

		for (int i = 0; i < size; i ++) {
			buffer.putLong(txRecords[i]);
		}

		buffer.put(CRLF);

		//get length
		int end = buffer.position();

		Record.completeData(buffer, start, end);

		buffer.flip();
		return buffer;
	}

	public static long[] read(ByteBuffer buf) {
		// skip the header
		buf.rewind();	// go to the beginning of the buffer
		buf.getInt();	// size of the record... we don't need it here
		buf.getLong();	// checksum ... must be checked during reading in the ByteBuffer

		byte type = buf.get();
		// TODO check the type is compensation record
		if(type != TXRecord.TX_REMOVE_RECORD_TYPE) {
			throw new RuntimeException("Error in determine the type of TXRecord.");
		}

		// read the body
		int txSeqNumCount = buf.getInt();
		long[] txSeqNums = new long[txSeqNumCount];
		for (int i = 0; i < txSeqNums.length; i++) {
			txSeqNums[i] = buf.getLong();
		}
		return txSeqNums;
	}
}
