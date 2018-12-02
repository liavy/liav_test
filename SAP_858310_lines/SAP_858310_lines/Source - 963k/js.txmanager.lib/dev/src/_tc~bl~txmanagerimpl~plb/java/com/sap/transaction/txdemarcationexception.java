package com.sap.transaction;


/**
 * A <code>TxStackException</code> is thrown to indicate that the 
 * <code>TxManager</code> has encountered an unbalanced sequence of transaction 
 * demarcation calls.
 */
public class TxDemarcationException extends TxException {

    public TxDemarcationException() {
        super();
    }
    
    public TxDemarcationException(String msg) {
        super(msg);
    }
}
