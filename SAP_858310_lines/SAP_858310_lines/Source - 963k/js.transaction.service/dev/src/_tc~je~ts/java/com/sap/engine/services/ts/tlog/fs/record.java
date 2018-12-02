package com.sap.engine.services.ts.tlog.fs;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class Record {

	public static final int LENGTH_SIZE = 4;
	public static final int CHECKSUM_SIZE = 8;
	protected final static byte[] CRLF = "\r\n".getBytes();

	private static long checksum(byte[] barr, int startPos, int length) {
		CRC32 crc32 = new CRC32();
		crc32.update(barr, startPos, length);
		long checksum = crc32.getValue();
		return checksum;
	}

	public Record() {
		super();
	}

	public static void completeData(ByteBuffer buf, int start, int end) {
		int dataLength = end - start;

		int bodyLength = dataLength - LENGTH_SIZE - CHECKSUM_SIZE;		

		buf.putInt(start, bodyLength);

		long checksum = checksum(buf.array(), start + LENGTH_SIZE + CHECKSUM_SIZE, bodyLength);

		buf.putLong(start + LENGTH_SIZE, checksum);
		buf.position(end);
	}

}