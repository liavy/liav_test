package com.sap.tests.utils.standaloneTMTests.oneAndTwoPhase;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class DummyXAResource implements XAResource{

	public void commit(Xid xid, boolean flag) throws XAException {		
		System.out.println(this + ".commit(" + xid +", " + flag +")");
		TestObjects.commit.incrementAndGet();
	}

	public void end(Xid xid, int flag) throws XAException {
		System.out.println(this + ".end(" + xid +", " + flag +")");
		TestObjects.end.incrementAndGet();
	}

	public void forget(Xid xid) throws XAException {
		System.out.println(this + ".forget(" + xid +")");	}

	public int getTransactionTimeout() throws XAException {
		System.out.println(this + ".getTransactionTimeout()");
		return 0;
	}

	public boolean isSameRM(XAResource xaResource) throws XAException {
		System.out.println(this + ".isSameRM(" + xaResource +")");
		return false;
	}

	public int prepare(Xid xid) throws XAException {
		System.out.println(this + ".prepare(" + xid + ")");
		TestObjects.prepare.incrementAndGet();
		return XAResource.XA_OK;
	}

	public Xid[] recover(int flag) throws XAException {
		System.out.println(this + ".recover(" + flag +")");
		return null;
	}

	public void rollback(Xid xid) throws XAException {
		System.out.println(this + ".rollback(" + xid + ")");
		TestObjects.rollback.incrementAndGet();
	}

	public boolean setTransactionTimeout(int flag) throws XAException {
		System.out.println(this + ".setTransactionTimeout(" + flag +")");
		return false;
	}

	public void start(Xid xid, int flag) throws XAException {
		System.out.println(this + ".start(" + xid +", " + flag +")");
		TestObjects.start.incrementAndGet();
		TestObjects.startFlag = flag;
	}

}
