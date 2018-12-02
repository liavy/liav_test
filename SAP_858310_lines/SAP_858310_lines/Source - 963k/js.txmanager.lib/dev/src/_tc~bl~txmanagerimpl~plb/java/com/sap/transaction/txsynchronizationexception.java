package com.sap.transaction;

/**
 * A <code>TxSynchronizationException</code> is thrown when a passed
 * <code>Synchronization</code> object conflicts with another
 * <code>Synchronization</code> object that has already been registered
 * under the same synchronization id.
 */
public class TxSynchronizationException extends TxException {

    public TxSynchronizationException() {
        super();
    }
    
    public TxSynchronizationException(String msg) {
        super(msg);
    }
    
}
