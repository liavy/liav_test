package com.sap.engine.services.ts.tlog.fs;

import java.nio.ByteBuffer;

import com.sap.engine.services.ts.tlog.TLogIOException;

public class RMRemoveRecord extends RMRecord {
	
	private int rmID;
	
	private final byte type = RM_REMOVE_RECORD_TYPE;
	
	//Empty Constructor
	public RMRemoveRecord() {
	}
	
	// Constructor
	public RMRemoveRecord(int rmID) {
		super();
		this.rmID = rmID;
	}

	@Override
	protected RMRemoveRecord read(ByteBuffer buf) throws TLogIOException {
		int rmid = buf.getInt();
		
		this.rmID = rmid;
		return this;
	}

	@Override
	protected RMRemoveRecord write(ByteBuffer buf)  throws TLogIOException {
		int start = buf.position();
		
		buf.putInt(0); // reserved for length
		
		buf.putLong(0); // reserved for checksum - crc32
		
		buf.put(type); // record type
		
		buf.putInt(rmID); // resource manager id
		
		int end = buf.position();
		
		Record.completeData(buf, start, end);
		return this;
	}
	
	public int getRMID() {
		return rmID;
	}
	
}
