/*
 * ITxManager.java
 *
 * Created on ×åòâúâòúê, 2004, Äåêåìâðè 9, 17:09
 */

package com.sap.transaction;

import javax.transaction.Synchronization;

/**
 * This interface specifies the functionality that TxManager implementations shouls support in order to
 * wrap the JTA transaction manager as specified by the new SAP transaction specification.
 */
public interface ITxManager {
     /**
     * Requires a new JTA transaction to be started or an already open
     * transaction to be joined. If there is already an open transaction
     * associated with the current thread, this transaction is joined.
     * Otherwise, if no transaction exists, a new transaction is started and
     * associated with the current thread.
     * <p>
     * The method returns a transaction ticket as a unique identifier for this
     * transaction level. Later on, this ticket has to be passed as an argument
     * to the methods <code>commitLevel()</code> and <code>leaveLevel()</code>,
     * in order to associate them with the transaction level opened by this
     * call.
     * </p>
     * <p>
     *
     * @return a transaction ticket.
     * @exception  TxException
     *                thrown if the transaction manager encounters an unexpected
     *                error situation.
     * @exception TxRollbackException
     *                thrown if there is already a transaction associated with
     *                the current thread and this transaction is marked for
     *                rollback only, for example, as a result of a
     *                <code>setRollbackOnly()</code> call.
     */
    public TransactionTicket required() throws TxException, TxRollbackException;
    
    
    /**
     * Requires a new transaction to be started. If there is already an open
     * transaction associated with the current thread, this transaction will be
     * suspended and the new transaction will be associated with the thread. The
     * suspended transaction will be resumed, when this transaction is completed
     * (either committed or rolled back).
     * <p>
     * The method returns a transaction ticket as a unique identifier for this
     * transaction level. Later on, this ticket has to be passed as an argument
     * to the methods <code>commitLevel()</code> and <code>leaveLevel()</code>,
     * in order to associate them with the transaction level opened by this
     * call.
     * </p>
     * <p>
     *
     * @return a transaction ticket.
     * @exception  TxException
     *                thrown if the transaction manager encounters an unexpected
     *                error situation.
     */
    public TransactionTicket requiresNew() throws TxException;
    
    /**
     * Marks the current JTA transaction for rollback. The rollback is performed
     * when the <code>commitLevel()</code> method is called on the transaction
     * level that has started the JTA transaction.
     * <p>
     *
     * @exception TxDemarcationException
     *                thrown if no transaction is associated with the current
     *                thread.
     * @exception  TxException
     *                thrown if the transaction manager encounters an unexpected
     *                error situation.
     */
    
    public void setRollbackOnly() throws TxException, TxDemarcationException;
    
    /**
     * Gets the status of the current transaction
     * @exception  TxException
     *              thrown if the transaction manager encounters an unexpected
     *              error situation.
     */
    public int  getStatus() throws TxException;
     /**
     * Commmits the transaction level associated with the given transaction
     * ticket.
     * <p>
     * If no JTA transaction has been started on the this transaction level,
     * then this method has no effect.
     * </p>
     * <p>
     * A <code>TxRollbackException </code> is thrown if the transaction was
     * rolled back rather than committed.
     * </p>
     * <p>
     *
     * @param ticket
     *            a transaction ticket.
     * @exception TxRollbackException
     *                thrown to indicate that the transaction has been rolled
     *                back rather than committed.
     * @exception TxDemarcationException
     *                thrown if the belance of the transaction demarcation calls
     *                has been violated.
     * @exception  TxException
     *                thrown if the transaction manager encounters an unexpected
     *                error situation.
     */
    public void commitLevel(TransactionTicket txticket) throws TxException, TxDemarcationException, TxRollbackException;
     /**
     * Finishes and leaves the transaction level associated with the given
     * transaction ticket.
     * <p>
     * If the <code>commitLevel()</code> method has already been called on the
     * transaction level identified by this ticket, this method has no further
     * effect than finishing the current transaction level. Otherwise, if the
     * <code>commitLevel()</code> method has not yet been called on this
     * transaction level, the following two cases must be distinguished:
     * </p>
     * <p>
     * <ul>
     * <li>If a JTA transaction was started on this transaction level, then it
     * will be rolled back. This also means that the
     * <code>afterCompletion()</code> methods of the registered
     * <code>Synchronization</code> objects are called immediately after the
     * rollback operation was executed.</code></li>
     * <li>Otherwise, if no JTA transaction was started on this transaction
     * level, then the joined JTA transaction will be marked for rollback only.
     * </li>
     * </ul>
     * </p>
     * <p>
     * In both cases, a <code>TxRollbackException</code> is thrown in order to
     * indicate that the current JTA transaction has been rolled back or has
     * been marked for rollback only.
     * </p>
     * <p>
     * Finally, if a JTA transaction has been suspended on this transaction
     * level because a new one was started, then this suspended transaction will
     * be resumed before this method is left.
     * <p>
     *
     * @param ticket
     *            a transaction ticket.
     * @exception TxRollbackException
     *                thrown to indicate that the transaction has been rolled
     *                back rather than committed.
     * @exception TxDemarcationException
     *                thrown if the belance of the transaction demarcation calls
     *                has been violated.
     * @exception  TxException
     *                thrown if the transaction manager encounters an unexpected
     *                error situation.
     */
    public void leaveLevel(TransactionTicket txticket) throws TxException, TxDemarcationException, TxRollbackException;
    
     /**
     * Registers the given <code>Synchronization</code> object with the
     * current transaction. The specified synchronization id (SID) is used to
     * define an order on the registered synchronization objects, thus reducing
     * the probability of database deadlocks.
     * <p>
     *
     * @param sid
     *            a synchronization identifier
     * @param sync
     *            a <code>Synchronization</code> object implementing the
     *            <code>beforeCompletion()</code> and
     *            <code>afterCompletion()</code> callbacks.
     * @exception TxSynchronizationException
     *                thrown if the given synchronization object conflicts with
     *                a another synchronization object that has already been
     *                registered under the same synchronization id before.
     * @exception TxDemarcationException
     *                thrown if no transaction is associated with the current
     *                thread.
     * @exception TxRollbackException
     *                thrown if the transaction associated with the current
     *                thread is marked for "rollback only".
     * @exception  TxException
     *                thrown if the transaction manager encounters an unexpected
     *                error situation.
     */
    public void registerSynchronization(String SID, Synchronization sync) throws TxException, TxDemarcationException, TxRollbackException, TxDuplicateOIDRegistrationException;
    
    /**
     * Gets the <code>Synchronization</code> object previously registered with
     * the current transaction under the given synchronization id (SID).
     * <p>
     *
     * @param sid
     *            a synchronization identifier
     * @return the <code>Synchronization</code> object registered with the
     *         specified SID, <code>null</code> if no
     *         <code>Synchronization</code> object has been registered under
     *         the given SID.
     * @exception TxDemarcationException
     *                thrown if no transaction is associated with the current
     *                thread.
     * @exception  TxException
     *                thrown if the transaction manager encounters an unexpected
     *                error situation.
     */
    public Synchronization getRegisteredSynchronization(String SID) throws TxException, TxDemarcationException;
    
     /**
     *  Checks whether the transaction associated with the current thread is active.
     *  @exception  TxException
     *               thrown if the transaction manager encounters an unexpected
     *               error situation.
     */
    public boolean isTxActive() throws TxException;
    
    /**
     *  Checks whether the transaction associated with the current thread is marked for rollback.
     *  @exception  TxException
     *               thrown if the transaction manager encounters an unexpected
     *               error situation.
     */
    public boolean isTxMarkedRollback() throws TxException;
    
}
