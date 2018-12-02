package com.sap.engine.services.ts.tlog.fs;

import java.nio.ByteBuffer;

import com.sap.engine.services.ts.tlog.InboundTransactionRecord;
import com.sap.engine.services.ts.tlog.InboundTransactionRecordImpl;
import com.sap.engine.services.ts.utils.ByteArrayUtils;

public class InboundTXEntryRecord extends TXEntryRecord {
	public static final int VAR_SIZE_PER_RM = 5;
	public static final int CONST_SIZE_PER_TX_REC = 4 + 8 + 1 + 8 + CRLF.length + 8 + 8 + 4 + 4	// standard record length
									+ 134	// external XID
									+ 4;	// heuristic outcome

	public static final int HEURISTIC_OUTCOME_OFFSET = 4 + 8 + 1 + 8 + CRLF.length + 8 + 8 + 4 + 4	// standard record length
									+ 134;	// external XID

	protected static int calculateTxRecordSize(InboundTransactionRecord rec) {
		return CONST_SIZE_PER_TX_REC + (rec.getRMIDs().length * VAR_SIZE_PER_RM);
	}

	public static ByteBuffer writeInboundRecord(InboundTransactionRecord txRecord) {
		int txRecSize = InboundTXEntryRecord.calculateTxRecordSize(txRecord);
		ByteBuffer buffer = ByteBuffer.allocate(txRecSize);

		int start = 0;

		TXEntryRecord.writeTXRecordIntoByteBuffer(txRecord, buffer, TXRecord.INBOUND_TX_ENTRY_RECORD_TYPE);
		buffer.put(ByteArrayUtils.convertXidToByteArray(txRecord.getExternalXID()));
		buffer.putInt(txRecord.getHeuristicOutcome());

		//get length
		int end = buffer.position();

		//finish
		Record.completeData(buffer, start, end);

		buffer.flip();
		return buffer;
	}

	public static InboundTransactionRecord readInboundRecord(ByteBuffer buf) {
		InboundTransactionRecordImpl rec = new InboundTransactionRecordImpl();

		TXEntryRecord.readTXRecordFromByteBuffer(buf, rec, TXRecord.INBOUND_TX_ENTRY_RECORD_TYPE);

		byte[] xid = new byte[134];
		buf.get(xid);
		rec.setExternalXID(ByteArrayUtils.getXidFromByteArr(xid, 0));

		int heuristicOutcome = buf.getInt();
		rec.setHeuristicOutcome(heuristicOutcome);

		return rec;
	}
}
