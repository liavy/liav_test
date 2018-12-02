package com.sap.tests.utils;

import javax.transaction.xa.XAResource;

import com.sap.engine.interfaces.transaction.RMContainer;
import com.sap.engine.interfaces.transaction.RMProps;
import com.sap.engine.interfaces.transaction.RMUnavailableException;

public class MyRMContainer implements RMContainer {

	public XAResource createXAResource(RMProps arg0) throws RMUnavailableException {
		return null;
	}

	public void closeXAResource(XAResource xaResource)
			throws RMUnavailableException {
		// TODO Auto-generated method stub
		
	}

}
