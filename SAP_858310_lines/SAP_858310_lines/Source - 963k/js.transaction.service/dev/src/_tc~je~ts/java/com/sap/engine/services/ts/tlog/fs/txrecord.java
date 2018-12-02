package com.sap.engine.services.ts.tlog.fs;


public abstract class TXRecord extends Record {

	public static final byte TX_REMOVE_RECORD_TYPE = 88;//Remove record - 'X'
	public static final byte TX_ENTRY_RECORD_TYPE = 84;//Transaction record 'T'

	public static final byte INBOUND_TX_ENTRY_RECORD_TYPE = 73;//Inbound transaction record 'I'

}
