package com.sap.tests.utils.standaloneTMTests.oneAndTwoPhase;

import javax.transaction.xa.XAResource;

import com.sap.engine.interfaces.transaction.RMContainer;
import com.sap.engine.interfaces.transaction.RMProps;
import com.sap.engine.interfaces.transaction.RMUnavailableException;

public class DummyRMContainerImpl implements RMContainer{

	public void closeXAResource(XAResource xaResource) throws RMUnavailableException {
		// TODO Auto-generated method stub
		
	}

	public XAResource createXAResource(RMProps rmProps)	throws RMUnavailableException {
		// TODO Auto-generated method stub
		return null;
	}

}
