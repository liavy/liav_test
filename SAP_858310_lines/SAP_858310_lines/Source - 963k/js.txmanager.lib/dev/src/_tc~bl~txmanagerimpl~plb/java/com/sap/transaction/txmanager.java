/*
 * TxManager.java
 *
 * 
 */

package com.sap.transaction;

/**
 * The <code>TxManager</code> class provides a set of static methods that
 * allow applications to handle JTA transactions in a convenient and controlled
 * way. The implementation of the <code>TxManager</code> methods is completely
 * based on the JTA transaction API as defined by the J2EE standard. So, this
 * class is simply a convenience facade to the JTA implementation provided by
 * the J2EE server.
 * <p>
 * The transaction control methods provided by this class essentially emulate
 * the two basic transaction control properties of EJB methods: <i>required </i>
 * and <i>requiresNew </i>. The <i>required </i> attribute means that the EJB
 * method needs to be executed under transaction control. If there is already an
 * open JTA transaction associated with the current thread, then this one will
 * be "joined". Otherwise, if no open JTA transaction exists, a new one will be
 * started. Contrary to this, the <i>requiresNew </i> attribute indicates that a
 * new and independent JTA transaction must be started to execute the EJB
 * method, even if there is already an open JTA transaction associated with the
 * current thread. Because the JTA concept does not support nested transactions,
 * there can be only one JTA transaction associated with a thread at one time.
 * This means, that an open transaction must be suspended before a new
 * transaction can be started. The suspended transaction is resumed after the
 * new transaction has been completed (either committed or rolled back).
 * </p>
 * <p>
 * The <code>TxManager</code> offers an analogous transaction control
 * mechanism as the required/requiresNew attributes as a programmatic API for
 * non-EJB components. But in contrast to the descriptive specification of
 * transaction attributes for EJB methods, which are implictly controlled by the
 * EJB container, explicit transaction demarcation involves the risk that an
 * error in control flow can cause mismatches between the calls that start a
 * transaction and those that complete a transaction. Therefore, the
 * <code>TxManager</code> enforces the balancing of transaction demarcation
 * calls by returning a transaction ticket (an instance of the class
 * {@link TxTicketImpl}) when a transaction is started (either by a call of
 * {@link #required()}or {@link #requiresNew()}method). This ticket uniquely
 * identifies the "transaction level" and it must be passed as an argument to
 * the respective calls that complete the transaction on this level (
 * {@link #commitLevel(TxTicket)}and {@link #leaveLevel(TxTicket)}).
 * </p>
 * <p>
 * Note that the "transaction levels" identified by transaction tickets does not
 * necessarily correspond to as many levels of JTA transactions. Because the
 * {@link #required()}method simply joins an already open JTA transaction,
 * there might be several "transaction levels" in the <code>TxManager</code>
 * sense referring to the same JTA transaction.
 * </p>
 * <p>
 * In order to support applications with high performance requirements, which
 * may have the need to collect modifications on persistent objects first in a
 * cache (instead of writing them directly to the database) and then write them
 * more efficiently (for example using batch operations) at the very end of a
 * JTA transaction, the <code>TxManager</code> provides a method
 * {@link #registerSynchronization(String, javax.transaction.Synchronization)}
 * to register application specific synchronization objects with a transaction.
 * The <code>javax.transaction.Synchronization</code> interface has two
 * callback methods: <code>beforeCompletion()</code> and
 * <code>afterCompletion()</code>. The <code>beforeCompletion()</code>
 * methods are called by the <code>TxManager</code> immediately before the JTA
 * transaction commits, whereas the <code>afterCompletion()</code> methods are
 * called when the commit processing has been finished. It is important to note,
 * that the synchronization objects are registered with the current JTA
 * transaction and not with the transaction levels controlled by the
 * <code>TxManager</code>. This means, that a call of method
 * <code>commitLevel()</code> does not necessarily cause the invocation of the
 * synchronization callbacks, but only if the current JTA transaction has been
 * started on this transaction level. In contrast to the corresponding method in
 * the <code>javax.transaction.Transaction</code> interface, the
 * <code>TxManager.registerSynchronization()</code> method requires an
 * synchronization identifier as an additional argument. This synchronization
 * identifier is used by the <code>TxManager</code> to invoke the
 * synchronization callbacks in a well-defined order, thus reducing the
 * likelyhood of deadlocks.
 * </p>
 * <p>
 * Method {@link #getRegisteredSynchronization(String)}can be used to retrieve
 * a registered synchronization object from the current JTA transaction. This
 * method might be useful, if an application wants to register a persistence
 * manager as a synchronization object with the current JTA transaction. By
 * this, the application has always access to its persistent manager while the
 * transaction is running.
 * </p>
 * <p>
 * If an application encounters an error situation that requires the current JTA
 * transaction to be rolled back, then it should call method
 * {@link #setRollbackOnly()}which marks the transaction for rollback only.
 * Marking a JTA transaction for rollback only means, that later on, when the
 * transaction is about to be committed, the commit will be turned into a
 * rollback.
 * </p>
 * <p>
 * Here is a code fragment that demonstrates a typical use-case of the
 * <code>TxManager</code> methods:
 * </p>
 *
 * <pre>
 *
 *
 *
 *
 *
 *        import com.sap.engine.services.ts.transaction.*;
 *
 *
 *
 *        public void aDatabaseAccessMethod(...) {
 *            TransactionTicket ticket = null;
 *            try {
 *                // join the current JTA transaction or start a new one if no
 *                // transaction exists
 *                ticket = TxManager.required();
 *                ...
 *                if (someConsistencyCheckFailed()) {
 *                    // mark the current transaction for rollback only
 *                    TxManager.setRollbackOnly();
 *                }
 *                ...
 *                // if everything is fine, commit the database modifications on
 *                // this transaction level; note, that this might be a noop here, if
 *                // the previous call of required() has not started its own JTA
 *                // transaction but only joined an existing one
 *                TxManager.commitLevel(ticket);
 *
 *            } catch (TxRollbackException e) {
 *                // The commit has turned into a rollback; maybe there are some
 *                // cleanups necessary, otherwise you can omit this catch block
 *                ...
 *
 *            } catch (TxException e) {
 *                // The transaction manager has encountered an unexpected error
 *                // situation; turn this into an according application exception
 *                throw new ApplicationException(...);
 *
 *            } finally {
 *                // Complete and leave the current transaction level; if the
 *                // commitLevel() operation has not been executed because some
 *                // application error ocurred, then the virtual transaction will be
 *                // rolled back implicitly by the leaveLevel() method (either by
 *                // directly executing a rollback operation, if the transaction was
 *                // started on this level, or indirectly by marking the current
 *                // transaction for rollback only).
 *                TxManager.leaveLevel(ticket);
 *            }
 *        }
 *
 *
 *
 *
 *
 *
 *
 * </pre>
 *
 * <p>
 * Some remarks on possible error situations: <br>
 * <ul>
 * <li>Explicit transaction control through JDBC connections is not allowed in
 * the context of a <code>TxManager</code> controlled transaction. The
 * following methods are forbidden: <code>setAutoCommit(false),
 *     commit() and rollback()</code>.
 * Setting the transaction isolation level is only possible immediately after a
 * JTA transaction has been started. This means, that
 * <code>setTransactionIsolation()</code> may only be called on a connection
 * object if a new JTA transaction has been started immediately before through a
 * <code>TxManager.requiresNew()</code> call.</li>
 * <li>Intermixing the transaction demaraction calls of the
 * <code>TxManager</code> with explict transaction control calls via the J2EE
 * server's JTA transaction manager may lead to unbalanced transaction levels
 * from the <code>TxManager's</code> point of view. For example, if a JTA
 * transaction has been started through a call of <code>required()</code> or
 * <code>requiresNew()</code>, then it cannot be committed or rolled back
 * through the commit/rollback methods provided by the JTA interfaces
 * <code>Transaction</code>,<code>TransactionManager</code> or
 * <code>UserTransaction</code>. Such an error situation will be detected by
 * the <code>TxManager</code> and the commit will fail with a
 * <code>RollbackException</code>.</li>
 * <li>It is essential that the transaction demarcation calls of the
 * <code>TxManager</code> are correctly balanced. This requires a strong code
 * discipline and thus it is strictly recommended to put the
 * <code>leaveLevel()</code> method in the finally-block of the try-statement
 * where method <code>required()</code>/<code>requiresNew()</code> has
 * been called.</li>
 * </ul>
 *
 * @see TxTicketImpl
 *
 */
public final class TxManager {
    
    private static ITxManager txMgr = null;
    
    public static void setTxManagerImpl(ITxManager mgr){
        txMgr = mgr;
    }
    
    /** Creates a new instance of TxManager */
    private TxManager() {
    }
    
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
    public static void commitLevel(TransactionTicket txticket) throws TxException, TxDemarcationException, TxRollbackException{
        if(txMgr==null){
            throw new TxException("Transaction service is not ready");
        }
        
        txMgr.commitLevel(txticket);
    }
    
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
    public static javax.transaction.Synchronization getRegisteredSynchronization(String SID) throws TxException, TxDemarcationException{
        if(txMgr==null){
            throw new TxException("Transaction service is not ready");
        }
        return txMgr.getRegisteredSynchronization(SID);
    }
    
    private  static int getStatus() throws TxException {
        if(txMgr==null){
            throw new TxException("Transaction service is not ready");
        }
        return txMgr.getStatus();
    }
    
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
    public static void registerSynchronization(String SID, javax.transaction.Synchronization sync) throws TxException, TxDemarcationException, TxRollbackException, TxDuplicateOIDRegistrationException {
        if(txMgr==null){
            throw new TxException("Transaction service is not ready");
        }
        txMgr.registerSynchronization(SID,sync);
    }
    
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
    public static TransactionTicket required() throws TxException, TxRollbackException {
        if(txMgr==null){
            throw new TxException("Transaction service is not ready");
        }
        return txMgr.required();
    }
    
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
    public static TransactionTicket requiresNew() throws TxException {
        if(txMgr==null){
            throw new TxException("Transaction service is not ready");
        }
        return txMgr.requiresNew();
    }
    
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
    public static void setRollbackOnly() throws TxException {
        if(txMgr==null){
            throw new TxException("Transaction service is not ready");
        }
        txMgr.setRollbackOnly();
    }
    
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
     * Finally, if a JTA transaction has been suspended on this transaction
     * level because a new one was started, then this suspended transaction will
     * be resumed before this method is left.
     * <p>
     *
     * @param ticket
     *            a transaction ticket.
     * @exception TxRollbackException
     *                thrown to indicate problems during rollback of the transaction.
     * @exception TxDemarcationException
     *                thrown if the belance of the transaction demarcation calls
     *                has been violated.
     * @exception  TxException
     *                thrown if the transaction manager encounters an unexpected
     *                error situation.
     */
    public static void leaveLevel(TransactionTicket ticket) throws TxException,  TxDemarcationException, TxRollbackException{
        if(txMgr==null){
            throw new TxException("Transaction service is not ready");
        }
        txMgr.leaveLevel(ticket);
        
    }
    
    /**
     *  Checks whether the transaction associated with the current thread is active.
     *  @exception  TxException
     *               thrown if the transaction manager encounters an unexpected
     *               error situation.
     */
    public static boolean isTxActive() throws TxException {
        return txMgr.isTxActive();
    }
    /**
     *  Checks whether the transaction associated with the current thread is marked for rollback.
     *  @exception  TxException
     *               thrown if the transaction manager encounters an unexpected
     *               error situation.
     */
    public static boolean isTxMarkedRollback() throws TxException {
        return txMgr.isTxMarkedRollback();
    }
    
}
