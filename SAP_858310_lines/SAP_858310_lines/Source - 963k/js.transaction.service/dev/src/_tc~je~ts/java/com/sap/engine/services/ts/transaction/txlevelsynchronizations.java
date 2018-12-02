/*
 * Created on 2004-12-2
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.engine.services.ts.transaction;

import java.util.HashMap;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;
import com.sap.engine.services.ts.exceptions.ExceptionConstants;

/**
 * The TxLevelSynchronizations class acts like a holder for synchronization objects related to a given transaction.
 * Synchronization objects are kept in an ordered TreeMap accessible by SID (synchronization id).
 * The TxLevelSynchronizations class does not allow duplicate SIDs or same objects associated with different SIDs 
 * to be added in the same synchronization level.
 */
public class TxLevelSynchronizations {
    
    private static final Location LOCATION = Location.getLocation(TxLevelSynchronizations.class);
    
    private Transaction tx;
    
    /**
     * A map containing all registered synchronizations sorted by OIDs. The
     * sorting guarantees that the synchronization methods are executed in a
     * well-defined order.
     */
    private HashMap registeredSynchronizations;
    
    
    /**
     * Creates a new synchronization object for the specified JTA transaction.
     * <p>
     *
     * @param tx
     *            a <code>Transaction</code> object that represents a JTA
     *            transaction.
     */
    public TxLevelSynchronizations(Transaction tx) {
        if (LOCATION.beLogged(Severity.DEBUG)) {
//                LOCATION.logT(Severity.DEBUG, "TxLevelSynchronizations.TxLevelSynchronizations("+tx+")");
                SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000258", "TxLevelSynchronizations.TxLevelSynchronizations({0})",  new Object[] { tx});
        }
        this.tx = tx;
        this.registeredSynchronizations = new HashMap();
    }
    
    /**
     * Gets the <code>Synchronization</code> object registered with the
     * specified synchronization id.
     * <p>
     *
     * @param sid
     *            a synchronization identifier.
     * @return a <code>Synchronization</code> object or <code>null</code> if
     *         no <code>Synchronization</code> object is registered with the
     *         given sid.
     */
    public Synchronization getSynchronization(String sid) {
        Synchronization sync = (Synchronization) registeredSynchronizations.get(sid);
        if (LOCATION.beLogged(Severity.DEBUG)) {
//                LOCATION.logT(Severity.DEBUG, ""+sync+" TxLevelSynchronizations.getSynchronization("+sid+")");
                SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000259", "{0} TxLevelSynchronizations.getSynchronization({1})",  new Object[] { sync,sid});
        }
        return sync;
    }
    
    /**
     * Adds a new <code>Synchronization</code> object identified by the given
     * synchronization id (SID) to this <code>TxSynchronizationLevel</code>
     * instance.
     * <p>
     *
     * @param sid
     *            a synchronization id
     * @param sync
     *            a <code>Synchronization</code> object
     * @exception TxSynchronizationException
     *                thrown if another synchronization object has already been
     *                registered with the same synchronization id as the given
     *                one.
     * @exception TxDuplicateOIDRegistrationException
     *                thrown if a duplicate object is registered with different SID
     *
     * @exception TxException
     *                thrown when registration of the synchronization object to the current transaction fails 
     *                or when there is failure getting the transaction context
     */
    public void addSynchronization(String sid, Synchronization sync) throws TxException, TxDuplicateOIDRegistrationException, TxSynchronizationException{
        
        if (LOCATION.beLogged(Severity.DEBUG)) {
//                LOCATION.logT(Severity.DEBUG, "TxLevelSynchronizations.addSynchronization("+sid+","+sync+")");
                SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000260", "TxLevelSynchronizations.addSynchronization({0},{1})",  new Object[] { sync,sid});
        }
        
        try {
            /*
             * Checks for duplicate SIDs or synchronization objects in the entire Stack
             */
            if (registeredSynchronizations.containsValue(sync)){
              throw new TxDuplicateOIDRegistrationException();
            }
            if (registeredSynchronizations.containsKey(sid)){
              throw new TxSynchronizationException();
            }
            
            tx.registerSynchronization(sync);                        
            registeredSynchronizations.put(sid, sync);
            
        } 
        catch (RollbackException e) {
            if (LOCATION.beLogged(Severity.DEBUG)) {
//                LOCATION.logT(Severity.DEBUG, ExceptionConstants.ROLLBACK_TRANSACTION_ERROR);
                SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000261","{0}", new Object[]{ExceptionConstants.ROLLBACK_TRANSACTION_ERROR});
            }
            throw new TxException(ExceptionConstants.ROLLBACK_TRANSACTION_ERROR,e);
        }
        catch(SystemException e){
            if (LOCATION.beLogged(Severity.DEBUG)) {
//                LOCATION.logT(Severity.DEBUG, e.toString());
                SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000262","{0}", new Object[]{e.toString()});
            }
            throw new RuntimeException(e.toString(), e);
        }
        
    }
    
}