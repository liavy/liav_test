package com.sap.engine.services.ts.utils;

import java.io.Serializable;
import java.util.Arrays;

import javax.transaction.xa.Xid;

public final class XidImpl implements Xid, Serializable {

	private static final long serialVersionUID = 4332071607559102158L;

	private final int formatId;
	private final byte[] branchQualifier;
	private final byte[] globalTransactionId;

	public XidImpl(int formatId, byte[] branchQualifier, byte[] globalTransactionId) {
		this.formatId = formatId;
		this.branchQualifier = branchQualifier;
		this.globalTransactionId = globalTransactionId;
	}

	public byte[] getBranchQualifier() {
		return branchQualifier;
	}

	public int getFormatId() {
		return formatId;
	}

	public byte[] getGlobalTransactionId() {
		return globalTransactionId;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.globalTransactionId);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;

		if (!(other instanceof Xid)) {
			return false;
		}

		Xid o = (Xid)other;
		return this.formatId == o.getFormatId() &&
			Arrays.equals(this.globalTransactionId, o.getGlobalTransactionId()) &&
			Arrays.equals(this.branchQualifier, o.getBranchQualifier());
	}
}
