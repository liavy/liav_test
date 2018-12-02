package com.sap.engine.services.ts.tlog.fs;

import java.nio.ByteBuffer;

import com.sap.engine.services.ts.tlog.TLogIOException;

public abstract class RMRecord extends Record {

	public static final byte RM_REMOVE_RECORD_TYPE = 82;//RemoveRecord
	public static final byte RM_ENTRY_RECORD_TYPE = 69;//Entry - Resource manager record
	
	protected abstract RMRecord write(ByteBuffer buf) throws TLogIOException;
	
	protected abstract RMRecord read(ByteBuffer buf) throws TLogIOException;
	
	public abstract int getRMID();
	
}
