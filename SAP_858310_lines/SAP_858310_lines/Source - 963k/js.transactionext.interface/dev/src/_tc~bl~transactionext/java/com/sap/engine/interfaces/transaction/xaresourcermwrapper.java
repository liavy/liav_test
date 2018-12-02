package com.sap.engine.interfaces.transaction;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class XAResourceRMWrapper implements XAResource {

	private XAResource xaResourceInstance = null;
	private int rmID = 0;
	
	public XAResourceRMWrapper(XAResource xaResourceInstance, int rmID){
		this.rmID = rmID;
		this.xaResourceInstance = xaResourceInstance;
	}
	
	public XAResource getVendorXAResource(){
		return xaResourceInstance;		
	}
	
	public int getRMID(){
		return rmID;
	}
	
	public void commit(Xid xid, boolean onePhaseCommit) throws XAException {
		xaResourceInstance.commit(xid, onePhaseCommit);
	}

	public void end(Xid arg0, int arg1) throws XAException {
		xaResourceInstance.end(arg0, arg1);
	}

	public void forget(Xid arg0) throws XAException {
		xaResourceInstance.forget(arg0);
	}

	public int getTransactionTimeout() throws XAException {
		return xaResourceInstance.getTransactionTimeout();
	}

	public boolean isSameRM(XAResource arg0) throws XAException {
		if(arg0 == null){
			return false;
		}
		boolean result = xaResourceInstance.isSameRM(arg0);
		if(!result && arg0 instanceof XAResourceRMWrapper){
			return xaResourceInstance.isSameRM(((XAResourceRMWrapper)arg0).xaResourceInstance);
		}
		return result;
	}

	public int prepare(Xid arg0) throws XAException {
		return xaResourceInstance.prepare(arg0);
	}

	public Xid[] recover(int arg0) throws XAException {
		return xaResourceInstance.recover(arg0);
	}

	public void rollback(Xid arg0) throws XAException {
		xaResourceInstance.rollback(arg0);
	}

	public boolean setTransactionTimeout(int arg0) throws XAException {
		return xaResourceInstance.setTransactionTimeout(arg0);
	}

	public void start(Xid arg0, int arg1) throws XAException {
		xaResourceInstance.start(arg0, arg1);
	}

}
