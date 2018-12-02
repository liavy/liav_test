package com.sap.transaction;

/**
 * A <code>TxRollbackException</code> is thrown when a transaction is marked
 * for rollback only or when it has been rolled back instead of committed.
 */
public class TxRollbackException extends TxException {
    
    public TxRollbackException() {
        super();
    }
    
    public TxRollbackException(String msg) {
        super(msg);
    }

}
