package com.sap.engine.services.ts.utils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class TLogVersion {

	private final byte[] tLogVersion;
	private final byte[] systemID;
	private final int nodeID;
	private final long tmStartupTime;

	/**
	 * Construct by TLog version array
	 * @param TLogVersion must be 15 bytes long
	 */
	public TLogVersion(byte[] TLogVersion) {
		this.tLogVersion = TLogVersion;

		systemID = new byte[3];
		System.arraycopy(TLogVersion, 0, systemID, 0, 3);

		nodeID = ByteArrayUtils.getIntFromByteArray(TLogVersion, 3);
		tmStartupTime = ByteArrayUtils.getLongFromByteArray(TLogVersion, 7);
	}

	/**
	 * Construct by TLog version components
	 * @param systemID must be 3 bytes long
	 * @param nodeID
	 * @param tmStartupTime
	 */
	public TLogVersion(byte[] systemID, int nodeID, long tmStartupTime) {
		this.systemID = systemID;
		this.nodeID = nodeID;
		this.tmStartupTime = tmStartupTime;

		tLogVersion = new byte[15];
		System.arraycopy(systemID, 0, tLogVersion, 0, 3);
		ByteArrayUtils.addIntInByteArray(nodeID, tLogVersion, 3);
		ByteArrayUtils.addLongInByteArray(tmStartupTime, tLogVersion, 7);
	}

	public byte[] getSystemID() {
		return systemID;
	}

	public String getSystemIDAsString() {
		try {
			return new String(systemID, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Internal error.", e);
		}
	}

	public int getNodeID() {
		return nodeID;
	}

	public long getTmStartupTime() {
		return tmStartupTime;
	}

	public byte[] getTLogVersion() {
		return tLogVersion;
	}
	
	public String toString() {
		try {
			return new String(systemID, "US-ASCII")
				+ "-" + Integer.toString(nodeID, 16) + "-" + Long.toString(tmStartupTime,16);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Internal error.", e);
		}
	}

	/**
	 * Construct by TLog version String
	 * @param the String must be in format as toString method converts it
	 */
	public TLogVersion(String s) {
		int pos = s.indexOf("-");
		int pos2 = s.indexOf("-", pos+1);
		try {
			this.systemID = s.substring(0, pos).getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Internal error.", e);
		}
		this.nodeID = Integer.valueOf(s.substring(pos+1, pos2), 16);
		this.tmStartupTime = Long.valueOf(s.substring(pos2+1), 16);

		this.tLogVersion = new byte[15];
		System.arraycopy(systemID, 0, tLogVersion, 0, 3);
		ByteArrayUtils.addIntInByteArray(nodeID, tLogVersion, 3);
		ByteArrayUtils.addLongInByteArray(tmStartupTime, tLogVersion, 7);
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TLogVersion other = (TLogVersion) obj;
		if (!Arrays.equals(tLogVersion, other.tLogVersion)) {
			return false;
		}
		return true;
	}
	
}