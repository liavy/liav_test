/*
 * Created on 2004-11-30
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.transaction;

/**
 * A TxDuplicateOIDRegistratrionException is thrown when an attempt to register a duplicate synchronization object 
 * with a different SID in a given synchtonization level is discovered. 
 * @see TxLevelSynchronizations
 * 
 */
public class TxDuplicateOIDRegistrationException extends TxException {
	public TxDuplicateOIDRegistrationException() {
		super();
	}

        public TxDuplicateOIDRegistrationException(String msg) {
		super(msg);
	}
}