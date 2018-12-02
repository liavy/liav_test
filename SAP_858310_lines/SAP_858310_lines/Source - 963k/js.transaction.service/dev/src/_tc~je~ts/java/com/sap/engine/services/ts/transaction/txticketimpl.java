package com.sap.engine.services.ts.transaction;

import javax.transaction.Transaction;
import com.sap.transaction.TransactionTicket;
/**
 * An instance of this class represents a "transaction ticket" that uniquely
 * identifies a certain transaction level controlled by the {@link TxManager}.
 * <p>
 * Transaction tickets are created by the <code>TxManager </code> when a 
 * "virtual" transaction is started through one of the methods 
 * {@link TxManager#required()} or {@link TxManager#requiresNew()}. The notion
 * "virtual" transaction should indicate that a transaction demarcated by
 * the <code>TxManager</code> methods do not necessarily correspond to 
 * a "real" JTA transaction. This is due to the fact that the 
 * <code>required()</code> method does not start a new JTA transaction if
 * there is already an open transaction associated with the current thread.
 * This means that there could be several levels of "virtual" transactions
 * started by the <code>required()</code> method, all of them referring to
 * one JTA transaction.</p>
 * <p>
 * The <code>TxTicket</code> class has no public constructors nor methods and
 * its instances are only created and consumed by the respective
 * <code>TxManager </code> methods.</p>
 * <p>
 * @see TxManager
 */
public class TxTicketImpl implements TransactionTicket {

    // -------------------
    // Instance Attributes -----------------------------------------------------
    // -------------------
    
    /** 
     * The JTA transaction associated with the transaction level identified by 
     * this ticket.
     */
    private final Transaction activeTransaction;
    
    /** 
     * The JTA transaction that has been suspended on the transaction level 
     * identified by this ticket (might be null).
     */
    private final Transaction suspendedTransaction;

    /**
     * Indicates whether a new JTA transaction has been started on this ticket's
     * transaction level or if an existing one has been joined.
     */
    private final boolean wasStarted;
    
    /**
     * Indicates whether the JTA transaction associated with this ticket
     * has already been completed, i.e. committed or rolled back.
     */
    private boolean wasCompleted = false;
    
    /**
     * Indicates whether the JTA transaction associated with this ticket
     * has been started from UserTransaction or TransactionManager
     */    
    private boolean wasTxStartedFromJTAapis = false;
     // ------------
    // Constructors ------------------------------------------------------------
    // ------------
        
    /**
     * Creates a new <code>TxTicket</code> that is associated with the
     * given JTA transaction. The <code>wasStarted</code> flag indicates
     * whether the JTA transaction was started on this transaction level or
     * if it was joined.
     * <p>
     * @param activeTransaction
     *     the JTA transaction associated with the new ticket.
     * @param wasStarted
     *     has the JTA transaction been started on this transaction level?
     */
    public TxTicketImpl(Transaction activeTransaction, boolean wasStarted, boolean wasTxStartedFromJTAapis) {
        this.activeTransaction = activeTransaction;
        this.suspendedTransaction = null;
        this.wasTxStartedFromJTAapis = wasTxStartedFromJTAapis;
        this.wasStarted = wasStarted;
    }

    /**
     * This constructor is to be used to create a new ticket for the
     * <code>TxManager.requiresNew()</code> method. Besides the new JTA
     * transaction the created ticket also stores a reference to the
     * suspended transaction (if there is one).
     * <p>
     * @param activeTransaction
     *     the JTA transaction associated with the new ticket.
     * @param suspendedTransaction
     *     the JTA transaction that has been suspended on this transaction 
     *     level or <code>null</code> if no transaction had to be suspended.
     */    
    public TxTicketImpl(Transaction activeTransaction, Transaction suspendedTransaction) {
        this.activeTransaction = activeTransaction;
        this.suspendedTransaction = suspendedTransaction;
        this.wasStarted = true;
    }

    // ---------------
    // Package Methods ---------------------------------------------------------
    // ---------------
    
    /**
     * Gets  the JTA transaction associated with this ticket.
     * <p>
     * @return 
     *     a <code>Transaction</code> object.
     */
    public Transaction getActiveTransaction() {
        return this.activeTransaction;
    }

    /**
     * Gets  the JTA transaction that has been suspended on the transaction
     * level associated with this ticket.
     * <p>
     * @return 
     *     a <code>Transaction</code> object representing a suspended
     *     transaction (might be <code>null</code>).
     */    
    public Transaction getSuspendedTransaction() {
        return this.suspendedTransaction;
    }
    
    /**
     * Has the JTA transaction been started on the transaction level associated
     * with this ticket?
     * <p>
     * @return
     *     <code>true</code> if the JTA transaction was started on this
     *     ticket's transaction level, <code>false</code> otherwise.
     */
    public boolean wasStarted() {
        return this.wasStarted;
    }
    
    public void test(){
    }
    
    /**
     * Sets the completed flag for this ticket.
     * <p>
     */
    void setCompleted() {
        this.wasCompleted = true;
    }
    
    /**
     * Has the JTA transaction associated with this ticket alread been
     * completed, i.e. committed or rolled back.
     * <p>
     * @return
     *     <code>true</code> if the JTA transaction associated with this 
     *     ticket has already been completed, <code>false</code> otherwise.
     */
    boolean wasCompleted() {
        return this.wasCompleted;
    }
    
    /**
     * Has the JTA transaction associated with this ticket was started from
     * UserTransaction or TransactionManager
     * <p>
     * @return
     *     <code>true</code> if the JTA transaction associated with this 
     *     ticket was started from UserTransaction or TransactionManager, <code>false</code> otherwise.
     */     
    boolean wasTxStartedFromJTAapis(){
    	return this.wasTxStartedFromJTAapis;
    } 
}
