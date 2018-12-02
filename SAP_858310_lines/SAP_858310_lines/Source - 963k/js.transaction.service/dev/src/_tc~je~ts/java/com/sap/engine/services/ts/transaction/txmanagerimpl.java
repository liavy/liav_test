package com.sap.engine.services.ts.transaction;

import java.util.Stack;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import com.sap.engine.frame.core.load.Component;
import com.sap.engine.interfaces.resourcecontext.ResourceContext;
import com.sap.engine.interfaces.resourcecontext.ResourceContextException;
import com.sap.engine.interfaces.resourcecontext.ResourceContextFactory;
import com.sap.engine.interfaces.resourceset.ResourceSet;
import com.sap.engine.interfaces.resourceset.ResourceSetFactory;
import com.sap.engine.interfaces.transaction.TransactionExtension;
import com.sap.engine.services.ts.LogUtil;
import com.sap.engine.services.ts.TransactionContextObject;
import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.exceptions.ExceptionConstants;
import com.sap.engine.services.ts.jta.TransactionInternalExtension;
import com.sap.engine.services.ts.jta.impl2.TXR_TransactionImpl;
import com.sap.engine.services.ts.jta.impl2.TXR_TransactionManagerImpl;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;
import com.sap.transaction.ITxManager;
import com.sap.transaction.TransactionTicket;
import static com.sap.engine.services.ts.jta.impl2.TXR_TransactionManagerImpl.RESOURCE_CONTEXT_METHOD_NAME;;


/**
 * The <code>TxManager</code> class provides a set of  methods that
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
public final class TxManagerImpl implements ITxManager{
    
    private static final Location LOCATION = Location.getLocation(TxManagerImpl.class);    
    private static final TxManagerImpl instance = new TxManagerImpl();
    
    private  TransactionManager jtaTxMgr = null;
    
    
    /**
     * prevent others from creating TxManager instance.
     */
    private TxManagerImpl() {
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
    public TransactionTicket required() throws TxException,  TxRollbackException{
        if (LOCATION.beLogged(Severity.DEBUG)) {
//            LOCATION.logT(Severity.DEBUG, "TxManager.required()");
            SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000263", "TxManager.required()");
            LOCATION.traceThrowableT(Severity.DEBUG,"TxManager.required() trace", new Exception("TxManager.required() trace"));            
        }
        if(jtaTxMgr == null){
            throw new TxException(ExceptionConstants.TX_SERVICE_NOT_READY);
        }
        Transaction tx = null;
        boolean started = false;
        
                /*
                 * Get transaction status and check if there is alread a JTA transaction
                 * associated with the current thread or not. In the first case, the
                 * existing transaction is joined and nothing else has to be done.
                 * Otherwise, a new JTA transaction is started.
                 */
        try{
            switch (jtaTxMgr.getStatus()) {
                
                case Status.STATUS_ACTIVE:{
                    tx = jtaTxMgr.getTransaction();
                    started = false;
                    break;
                }
                case Status.STATUS_NO_TRANSACTION:{
                    jtaTxMgr.begin();
                    tx = jtaTxMgr.getTransaction();
                    if(((TransactionExtension)tx).getAssociatedObjectWithTransaction(TXR_TransactionManagerImpl.RESOURCE_CONTEXT_OBJECT_KEY) == null){
                    	ResourceSet resourceSet =null;
                    	ResourceSetFactory rsf = TransactionServiceFrame.getResourceSetFactory();
                    	if (rsf != null) {
                    		resourceSet = rsf.getCurrentResourceSet();
                    	}
                      String appName = null;
                      String jndi_offset = "";
                      if(resourceSet != null){
                      	appName = resourceSet.getApplicationName();
                      	jndi_offset = resourceSet.getComponentName();
                      } else {
                    	Component component = Component.getCallerComponent();
                    	if (null!=component) {
                            appName = component.getName();
                    	}
                      }     
                        ResourceContextFactory rcf = TransactionServiceFrame.getResourceContextFactory();
                        if (null!=rcf) {
    	   	                ResourceContext resourceContext = rcf.createContext(appName, jndi_offset, true);
    	                    resourceContext.enterMethod(RESOURCE_CONTEXT_METHOD_NAME);  
    	                    ((TransactionExtension)tx).associateObjectWithTransaction(TXR_TransactionManagerImpl.RESOURCE_CONTEXT_OBJECT_KEY, resourceContext);
                        }

	                    TXR_TransactionImpl t = (TXR_TransactionImpl)tx;
	                    t.setDCName(appName);
                    }
                    started = true;
                    break;
                }
                case Status.STATUS_MARKED_ROLLBACK:{
                    //check spec for this??
                    //throw new TxRollbackException();
                    throw new TxRollbackException();
                }
                default:
                    throw new TxException(ExceptionConstants.UNEXPECTED_STATUS_ERROR);
            }
            
        }catch(SystemException e){
            if (LOCATION.beLogged(Severity.DEBUG)) {
                LOCATION.traceThrowableT(Severity.DEBUG, "Failed to start a new transaction.", e);
            }
            try{
              jtaTxMgr.rollback();
            } catch (Exception ex){
              throw new TxException(ExceptionConstants.BEGIN_TRANSACTION_ERROR,ex);
            }                        
            throw new TxException(ExceptionConstants.BEGIN_TRANSACTION_ERROR,e);
        }catch(NotSupportedException e){
            if (LOCATION.beLogged(Severity.DEBUG)) {
                LOCATION.traceThrowableT(Severity.DEBUG, "Failed to start a new transaction.", e);
            }
            try{
              jtaTxMgr.rollback();
            } catch (Exception ex){
              throw new TxException(ExceptionConstants.BEGIN_TRANSACTION_ERROR,ex);
            }                        
            throw new TxException(ExceptionConstants.BEGIN_TRANSACTION_ERROR,e);
        }catch(ResourceContextException e){
            if (LOCATION.beLogged(Severity.DEBUG)) {
                LOCATION.traceThrowableT(Severity.DEBUG, "Failed to start a new transaction.", e);
            }
            try{
              jtaTxMgr.rollback();
            } catch (Exception ex){
              throw new TxException(ExceptionConstants.Exception_in_begin_of_transaction, ex);
            }            
            throw new TxException(ExceptionConstants.Exception_in_begin_of_transaction, e);
        }catch(RuntimeException e){
            if (LOCATION.beLogged(Severity.DEBUG)) {
                LOCATION.traceThrowableT(Severity.DEBUG, "Failed to start a new transaction.", e);
            }
            try{
              jtaTxMgr.rollback();
            } catch (Exception ex){
              throw new TxException(ExceptionConstants.Exception_in_begin_of_transaction, ex);
            }            
            throw new TxException(ExceptionConstants.Exception_in_begin_of_transaction, e);
        }
        
        TxTicketImpl result = null;
        
        if(started){//transaction was started from TxManager
        	addTxSynchronizationLevel(tx);
            result = new TxTicketImpl(tx, started, false);
        } else if (getTransactionContextObject().getSynchronizationsStack().isEmpty()){//Transaction was started before from UserTransaction or TransactionManager
        	addTxSynchronizationLevel(tx);
        	result = new TxTicketImpl(tx, started, true);
        } else {//Transaction was started before from TxManager.required() or TxManager.requiresNew()
        	result = new TxTicketImpl(tx, started, false);
        }
        
        getTransactionContextObject().getTicketStack().push(result);
        
        return (TransactionTicket)result;
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
    public  TransactionTicket requiresNew() throws TxException {
        
        if (LOCATION.beLogged(Severity.DEBUG)) {
//            LOCATION.logT(Severity.DEBUG, "TxManager.requiresNew()");
            SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000264", "TxManager.requiresNew()");
            LOCATION.traceThrowableT(Severity.DEBUG,"TxManager.requiresNew() trace", new Exception("TxManager.requiresNew() trace"));            
        }
        
        if(jtaTxMgr == null){
            throw new TxException(ExceptionConstants.TX_SERVICE_NOT_READY);
        }
        
        Transaction suspendedTransaction = null;
        
                /*
                 * Suspend the active transaction if there is one. Note: suspend()
                 * method returns "null" if there is no transaction associated with the
                 * calling thread.
                 */
        try {
            //@todo no need to suspend if no upper level tx exists
            if (jtaTxMgr.getStatus() != Status.STATUS_NO_TRANSACTION && jtaTxMgr.getStatus() != Status.STATUS_UNKNOWN) {
                suspendedTransaction = jtaTxMgr.suspend();
            }
        } catch (SystemException e) {
            if (LOCATION.beLogged(Severity.DEBUG)) {
                LOCATION.traceThrowableT(Severity.DEBUG, "Failed to suspend current transaction", e);
            }
            throw new TxException(ExceptionConstants.SUSPEND_TRANSACTION_ERROR, e);
        }
        
        Transaction tx = null;
        
        try{
            jtaTxMgr.begin();
            tx =jtaTxMgr.getTransaction();
            if(((TransactionExtension)tx).getAssociatedObjectWithTransaction(TXR_TransactionManagerImpl.RESOURCE_CONTEXT_OBJECT_KEY) == null){
            	ResourceSetFactory rsf = TransactionServiceFrame.getResourceSetFactory();
            	ResourceSet resourceSet = null;
            	if (rsf != null) {
            		resourceSet = rsf.getCurrentResourceSet();
            	}
              String appName = null;
              String jndi_offset = "";
              if(resourceSet != null){
               	appName = resourceSet.getApplicationName();
               	jndi_offset = resourceSet.getComponentName();
              } else {
            	Component component = Component.getCallerComponent();
            	if (null!=component) {
                    appName = component.getName();
            	}
              }                      
              ResourceContextFactory rcf = TransactionServiceFrame.getResourceContextFactory();
              if (rcf != null) {
                  ResourceContext resourceContext = rcf.createContext(appName, jndi_offset, true);
                  resourceContext.enterMethod(RESOURCE_CONTEXT_METHOD_NAME);  
    	            ((TransactionExtension)tx).associateObjectWithTransaction(TXR_TransactionManagerImpl.RESOURCE_CONTEXT_OBJECT_KEY, resourceContext);
              }

              TXR_TransactionImpl t = (TXR_TransactionImpl)tx;
              t.setDCName(appName);
            }                     
        } catch(SystemException e){
            if (LOCATION.beLogged(Severity.DEBUG)) {
                LOCATION.traceThrowableT(Severity.DEBUG, "Failed to start a new transaction.",e);
            }
            try{
              jtaTxMgr.rollback();
            } catch (Exception ex){
              throw new TxException(ExceptionConstants.BEGIN_TRANSACTION_ERROR,e);
            }            
            throw new TxException(ExceptionConstants.BEGIN_TRANSACTION_ERROR,e);
        } catch(NotSupportedException e){
            if (LOCATION.beLogged(Severity.DEBUG)) {
                LOCATION.traceThrowableT(Severity.DEBUG, "Failed to start a new transaction.", e);
            }
            try{
              jtaTxMgr.rollback();
            } catch (Exception ex){
              throw new TxException(ExceptionConstants.BEGIN_TRANSACTION_ERROR,ex);
            }                        
            throw new TxException(ExceptionConstants.BEGIN_TRANSACTION_ERROR,e);
        } catch (RuntimeException e){
            if (LOCATION.beLogged(Severity.DEBUG)) {
                LOCATION.traceThrowableT(Severity.DEBUG, "Failed to start a new transaction.", e);
            }
            try{
              jtaTxMgr.rollback();
            } catch (Exception ex){
              throw new TxException(ExceptionConstants.Exception_in_begin_of_transaction, ex);
            }            
            throw new TxException(ExceptionConstants.Exception_in_begin_of_transaction, e);
        } catch (ResourceContextException e){
            if (LOCATION.beLogged(Severity.DEBUG)) {
                LOCATION.traceThrowableT(Severity.DEBUG, "Failed to start a new transaction.", e);
            }
            try{
              jtaTxMgr.rollback();
            } catch (Exception ex){
              throw new TxException(ExceptionConstants.Exception_in_begin_of_transaction, ex);
            }            
            throw new TxException(ExceptionConstants.Exception_in_begin_of_transaction, e);
        }
        
        addTxSynchronizationLevel(tx);
        TxTicketImpl result = new TxTicketImpl(tx, suspendedTransaction);
        
        getTransactionContextObject().getTicketStack().push(result);
        
        return (TransactionTicket)result;
        
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
    public  void commitLevel(TransactionTicket ticket) throws TxException, TxDemarcationException, TxRollbackException{
        
        if (LOCATION.beLogged(Severity.DEBUG)) {
//            LOCATION.logT(Severity.DEBUG, "TxManager.commitLevel("+ticket+")");
            SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000265", "TxManager.commitLevel({0})",  new Object[] { ticket});
        }
        
        if(ticket == null){
            throw new TxException(ExceptionConstants.TICKET_IS_NULL);
        }
        if(jtaTxMgr == null){
            throw new TxException(ExceptionConstants.TX_SERVICE_NOT_READY);
        }
        
        TransactionContextObject txContextObject = getTransactionContextObject();
        
        if (txContextObject.getTicketStack().isEmpty()
        || ticket != (TransactionTicket) txContextObject.getTicketStack().peek()) {
            /* Stack discipline violated */
            throw new TxDemarcationException();
        }
        
                /*
                 * Check that the JTA transaction referred to by the given ticket is the
                 * same as that associated with the current thread.
                 */
        Transaction tx = null;
        try{
            tx = jtaTxMgr.getTransaction();
        }catch(SystemException e){
            if (LOCATION.beLogged(Severity.DEBUG)) {
                LOCATION.traceThrowableT(Severity.DEBUG, "Failed to get the transaction associated with the current thread.", e);
            }
            throw new TxException(ExceptionConstants.GET_TRANSACTION_ERROR,e);
        }
        if (tx != ((TxTicketImpl) ticket).getActiveTransaction()) {
            throw new TxDemarcationException();
        }
        
        try {
            if (ticket.wasStarted()) {
                /*
                 * The JTA transaction referred to by the ticket has been
                 * started on this transaction level. In this case, the
                 * transaction must be committed now; note that this commit-call
                 * might be turned into a rollback by the transaction manager if
                 * the the transaction has been marked for rollback; in this
                 * case the commit() method throws a RollbackException which
                 * will be turned into a TxRollbackException.
                 */
                try {
                    jtaTxMgr.commit();
                }
                catch (RollbackException e) {
                    if (LOCATION.beLogged(Severity.DEBUG)) {
                        LOCATION.traceThrowableT(Severity.DEBUG, "Current transaction is marked for rollback.", e);
                    }
                    Throwable cause = e.getCause();
                    if(cause == null){//transaction is marked for rollback and there is no exception from beforeCompletion
                    	cause = e;
                    }
                    /* the commit has been turned into a rollback */
                    throw new TxRollbackException(ExceptionConstants.MARKED_FOR_ROLLBACK,cause);
                }
                
                catch (HeuristicRollbackException e) {
                    if (LOCATION.beLogged(Severity.DEBUG)) {
                        LOCATION.traceThrowableT(Severity.DEBUG, "Failed to commit the current transaction.", e);
                    }
                    /* the commit has been turned into a rollback */
                    throw new TxException(ExceptionConstants.COMMIT_TRANSACTION_ERROR,e);
                } catch (HeuristicMixedException e) {
                    if (LOCATION.beLogged(Severity.DEBUG)) {
                        LOCATION.traceThrowableT(Severity.DEBUG, "Failed to commit the current transaction.", e);
                    }
                    throw new TxException(ExceptionConstants.COMMIT_TRANSACTION_ERROR,
                    e);
                }
                catch (SystemException e) {
                    if (LOCATION.beLogged(Severity.DEBUG)) {
                        LOCATION.traceThrowableT(Severity.DEBUG, "Failed to commit the current transaction.", e);
                    }
                    throw new TxException(ExceptionConstants.COMMIT_TRANSACTION_ERROR,
                    e);
                }
                
                
            }
        } finally {
            ((TxTicketImpl)ticket).setCompleted();
        }
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
    
    public  void leaveLevel(TransactionTicket ticket) throws TxException,  TxDemarcationException, TxRollbackException{
        
        if (LOCATION.beLogged(Severity.DEBUG)) {
            LOCATION.logT(Severity.DEBUG, "TxManager.leaveLevel("+ticket+")");
        }
        
        if(ticket == null){
            throw new TxException(ExceptionConstants.TICKET_IS_NULL);
        }
        if(jtaTxMgr == null){
            throw new TxException(ExceptionConstants.TX_SERVICE_NOT_READY);
        }
        
        TransactionContextObject txContextObject = getTransactionContextObject();
        
        TxDemarcationException demarcationExceptionToThrowAtTheEnd = null;
        
        if (txContextObject.getTicketStack().isEmpty()
        || ticket != (TransactionTicket) txContextObject.getTicketStack().peek()) {
            /* Stack discipline violated */
            demarcationExceptionToThrowAtTheEnd = new TxDemarcationException();
        }
        
        /*
         * Remove the TxSynchronization object registered with
         * the current JTA transaction from the synchronizations
         * stack
         */
        if(!txContextObject.getTicketStack().isEmpty()){
        	txContextObject.getTicketStack().pop();
        }
        
        
        if (ticket.wasStarted()) {
        	
        	if(!txContextObject.getSynchronizationsStack().isEmpty()){
        		txContextObject.getSynchronizationsStack().pop();
        	}

            try{
                if (!((TxTicketImpl)ticket).wasCompleted()){
                    /*rollback*/
                    try {
                        jtaTxMgr.rollback();
                    }catch (SystemException e) {
                        if (LOCATION.beLogged(Severity.DEBUG)) {
                            LOCATION.traceThrowableT(Severity.DEBUG, "Failed to rollback the current transaction.", e);
                        }
                        throw new TxException(ExceptionConstants.ROLLBACK_TRANSACTION_ERROR,e);
                    }
                }
            }finally{
                /*
                 * If the owner of the ticket has suspended a transaction, then
                 * it has to be resumed now
                 */
                Transaction suspendedTransaction = ((TxTicketImpl) ticket).getSuspendedTransaction();
                if (suspendedTransaction != null) {
                    try {
                        jtaTxMgr.resume(suspendedTransaction);
                    } catch (IllegalStateException e) {
                        throw new TxDemarcationException(ExceptionConstants.RESUME_TRANSACTION_ERROR,e);
                    } catch (InvalidTransactionException e) {
                        throw new TxDemarcationException(ExceptionConstants.RESUME_TRANSACTION_ERROR,e);
                    }
                    catch (SystemException e) {
                        if (LOCATION.beLogged(Severity.DEBUG)) {
                            LOCATION.traceThrowableT(Severity.DEBUG, "Failed to resume suspended transaction.", e);
                        }
                        throw new TxException(
                        ExceptionConstants.RESUME_TRANSACTION_ERROR, e);
                    }
                }
            }
            
        } else {
//    synchronization stack will be cleared when transaction is completed or when a new transaction is started. Required in CSN 3415669 2008        	
        	if (((TxTicketImpl)ticket).wasTxStartedFromJTAapis()){
        		((TransactionInternalExtension)((TxTicketImpl)ticket).getActiveTransaction()).removeSynchronizationStackWhenCompleted();
        	}
        	
//        	if (((TxTicketImpl)ticket).wasTxStartedFromJTAapis() && !txContextObject.getSynchronizationsStack().isEmpty()){
//        		txContextObject.getSynchronizationsStack().pop();
//        	}        	
        	
            if (!((TxTicketImpl)ticket).wasCompleted()) {
                setRollbackOnly();
            }
        }
        
        if(demarcationExceptionToThrowAtTheEnd != null){
        	throw demarcationExceptionToThrowAtTheEnd;        	
        }
        
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
    public  void setRollbackOnly() throws TxException, TxDemarcationException {
        
        if (LOCATION.beLogged(Severity.DEBUG)) {
//            LOCATION.logT(Severity.DEBUG, "TxManager.setRollbackOnly()");
            SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000266", "TxManager.setRollbackOnly()");
        }
        
        if(jtaTxMgr == null){
            throw new TxException(ExceptionConstants.TX_SERVICE_NOT_READY);
        }
        /* Execute on TxManager instance */
        try {
            jtaTxMgr.setRollbackOnly();
        } catch (IllegalStateException ex) {
            /* no transaction associated with the current thread */
            throw new TxDemarcationException(
            		ExceptionConstants.Thread_is_not_associated_with_transaction,
            		new Object[] {LogUtil.getDCNameByCaller(), LogUtil.getCSNComponentByCaller()},
            		ex);
        } catch (SystemException e) {
            if (LOCATION.beLogged(Severity.DEBUG)) {
                LOCATION.traceThrowableT(Severity.DEBUG, "Failed to mark transaction for rollback only.", e);
            }
            throw new TxException(ExceptionConstants.SET_ROLLBACK_ONLY_ERROR, e);
        }
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
    public  void registerSynchronization(String sid, Synchronization sync) throws TxException, TxDemarcationException, TxRollbackException, TxDuplicateOIDRegistrationException, TxSynchronizationException {
        
        if (LOCATION.beLogged(Severity.DEBUG)) {
//            LOCATION.logT(Severity.DEBUG, "TxManager.registerSynchronization("+sid+","+sync+")");
            SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000267", "TxManager.registerSynchronization({0},{1})",  new Object[] { sid,sync});
        }
        
        if(jtaTxMgr == null){
            throw new TxException(ExceptionConstants.TX_SERVICE_NOT_READY);
        }
        try{
            switch (jtaTxMgr.getStatus()) {
                case Status.STATUS_ACTIVE:
                    break;
                case Status.STATUS_NO_TRANSACTION:
                    throw new TxDemarcationException();
                case Status.STATUS_MARKED_ROLLBACK:
                    //???? check JTA spec
                    throw new TxRollbackException();
                default:
                    throw new TxException(ExceptionConstants.UNEXPECTED_STATUS_ERROR);
            }
        }catch(SystemException e){
            if (LOCATION.beLogged(Severity.DEBUG)) {
                LOCATION.traceThrowableT(Severity.DEBUG, "Failed to retrieve the status of the current transaction.", e);
            }
            throw new TxException(ExceptionConstants.GET_STATUS_ERROR,e);
        }
        
                /*
                 * Add the given Synchronization object to the list of synchronizations
                 * being stored in the TxSynchronization object associated with the
                 * current JTA transaction (= top of synchronizationsStack).
                 */
        if (getTransactionContextObject().getSynchronizationsStack().isEmpty()) {
            throw new TxDemarcationException();
        }
        
        TxLevelSynchronizations txSync = (TxLevelSynchronizations) getTransactionContextObject().getSynchronizationsStack().peek();
        txSync.addSynchronization(sid, sync);
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
    public  Synchronization getRegisteredSynchronization(String sid) throws TxException,  TxDemarcationException{
        
        if (LOCATION.beLogged(Severity.DEBUG)) {
//            LOCATION.logT(Severity.DEBUG, "TxManager.getRegisteredSynchronization("+sid+")");
            SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000268", "TxManager.getRegisteredSynchronization({0})",  new Object[] { sid});
        }
        
        if(jtaTxMgr == null){
            throw new TxException(ExceptionConstants.TX_SERVICE_NOT_READY);
        }
        
		    /*
		     * Get the TxSynchronization object registered with the current JTA
		     * transaction (= top of the synchronisationsStack) and search for a
		     * Synchronization with the specified SID
		     */
		     
		    Stack synchronizationStack = getTransactionContextObject().getSynchronizationsStack();
		    if (synchronizationStack.isEmpty()) {
		     	// stack is empty. This means that there is no transaction and no synchronizations
		    return null;
		    }
		        
		    return ((TxLevelSynchronizations)synchronizationStack.peek()).getSynchronization(sid);
		    
    }
    
    /**
     *  Checks whether the transaction associated with the current thread is active.
     *  @exception  TxException
     *               thrown if the transaction manager encounters an unexpected
     *               error situation.
     */
    
    public  boolean isTxActive() throws TxException {
        if(jtaTxMgr == null){
            throw new TxException(ExceptionConstants.TX_SERVICE_NOT_READY);
        }
        try {
            if (jtaTxMgr.getStatus() == Status.STATUS_ACTIVE) {
                return true;
            }
        } catch (SystemException e) {
            if (LOCATION.beLogged(Severity.DEBUG)) {
                LOCATION.traceThrowableT(Severity.DEBUG, "Failed to retrieve the status of the current transaction.", e);
            }
            throw new TxException(ExceptionConstants.GET_STATUS_ERROR, e);
        }
        return false;
    }
    /**
     *  Checks whether the transaction associated with the current thread is marked for rollback.
     *  @exception  TxException
     *               thrown if the transaction manager encounters an unexpected
     *               error situation.
     */
    public  boolean isTxMarkedRollback() throws TxException {
        if(jtaTxMgr == null){
            throw new TxException(ExceptionConstants.TX_SERVICE_NOT_READY);
        }
        try {
            if (jtaTxMgr.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                return true;
            }
        } catch (SystemException e) {
            if (LOCATION.beLogged(Severity.DEBUG)) {
                LOCATION.traceThrowableT(Severity.DEBUG, "Failed to retrieve the status of the current transaction.", e);
            }
            throw new TxException(ExceptionConstants.GET_STATUS_ERROR, e);
        }
        return false;
    }
    /*
     *  Checks whether the transaction associated with the current thread is alive.
     *  @exception  TxException
     *               thrown if the transaction manager encounters an unexpected
     *               error situation.
     */
    private  boolean isTxAlive() throws TxException {
        if(jtaTxMgr == null){
            throw new TxException(ExceptionConstants.TX_SERVICE_NOT_READY);
        }
        try {
            int status = jtaTxMgr.getStatus();
            if (status != Status.STATUS_ROLLING_BACK
            || status != Status.STATUS_ROLLEDBACK
            || status != Status.STATUS_NO_TRANSACTION
            || status != Status.STATUS_MARKED_ROLLBACK) {
                return true;
            }
        } catch (SystemException e) {
            throw new TxException(ExceptionConstants.GET_STATUS_ERROR, e);
        }
        return false;
    }
    
    /**
     * Creates a new <code>TxSynchronizationLevel</code> object and registers it
     * with the given JTA transaction.
     * <p>
     *
     * @param tx
     *            a <code>Transaction</code> object representing the JTA
     *            transaction associated with the current thread.
     * @exception  TxException
     *                thrown if the JTA transaction manager has encountered an
     *                unexpected error situation.
     */
    private  void addTxSynchronizationLevel(Transaction tx) throws TxException {
        if (LOCATION.beLogged(Severity.DEBUG)) {
//            LOCATION.logT(Severity.DEBUG, "TxManager.addTxSynchronizationLevel("+tx+")");
            SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000269", "TxManager.addTxSynchronizationLevel({0})",  new Object[] { tx});
        }
        if(jtaTxMgr == null){
            throw new TxException(ExceptionConstants.TX_SERVICE_NOT_READY);
        }
        try {
            
            TxLevelSynchronizations txLevelSync = new TxLevelSynchronizations(tx);
            
            getTransactionContextObject().getSynchronizationsStack().push(txLevelSync);
            
        } catch (RuntimeException e) {
            
            if (LOCATION.beLogged(Severity.DEBUG)) {
                LOCATION.traceThrowableT(Severity.DEBUG, "Failed to register a synchronization object with the current transaction.", e);
            }
            throw new TxException(ExceptionConstants.REGISTER_SYNCHRONIZATION_ERROR, e);
        }
    }
    
    /**
     * this method sets the JTA transaction manager. Used by TransactionServiceFrame.
     */
    public  void setTransactionManager(TransactionManager _tm) {
        if (LOCATION.beLogged(Severity.DEBUG)) {
//            LOCATION.logT(Severity.DEBUG, "TxManager.setTransactionManager("+_tm+")");
            SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000270", "TxManager.setTransactionManager({0})",  new Object[] { _tm});
        }
        jtaTxMgr = _tm;
    }
    
    private  TransactionContextObject getTransactionContextObject() {
        
        TransactionContextObject obj =  TransactionContextObject.getThransactionContextObject();
        if (LOCATION.beLogged(Severity.DEBUG)) {
//        	LOCATION.logT(Severity.DEBUG, ""+obj+" TxManager.getTransactionContextObject()");
        	SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000271", "{0} TxManager.getTransactionContextObject()",  new Object[] { obj});
        }
        return obj;
    }
    /**
     * Gets the status of the current transaction
     * @exception  TxException
     *              thrown if the transaction manager encounters an unexpected
     *              error situation.
     */
    public  int getStatus() throws TxException{
        if (LOCATION.beLogged(Severity.DEBUG)) {
//            LOCATION.logT(Severity.DEBUG, "TxManager.getStatus()");
            SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000272", "TxManager.getStatus()");
        }
        try{
            return jtaTxMgr.getStatus();
        }catch(SystemException e){
            if (LOCATION.beLogged(Severity.DEBUG)) {
                LOCATION.traceThrowableT(Severity.DEBUG, "Failed to retrieve the status of the current transaction.", e);
            }
            throw new TxException(ExceptionConstants.Exception_in_get_status_of_transaction,e);
        }
    }
    
    
    public static TxManagerImpl getInstance(){
        return instance;
    }
    
}