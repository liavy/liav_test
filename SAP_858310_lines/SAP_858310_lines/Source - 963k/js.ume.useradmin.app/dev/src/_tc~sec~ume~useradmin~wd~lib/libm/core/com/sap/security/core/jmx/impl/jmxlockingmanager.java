package com.sap.security.core.jmx.impl;

import java.util.Arrays;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.sap.engine.frame.core.locking.LockingConstants;
import com.sap.engine.services.applocking.LogicalLocking;
import com.sap.engine.services.applocking.LogicalLockingFactory;
import com.sap.security.api.UMException;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.locking.LockException;
import com.sap.security.core.locking.TechnicalLockException;
import com.sap.security.core.locking.imp.LockManager;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Helper class for locking of UME principals being edited in the UME identity
 * management application.
 * 
 * Each lock's lifetime is bound to the lifetime of the current user session.

 * <b>Read locks</b> are implemented as shared locks, that means several users
 * can simultaneously hold read locks for the same principal.
 * 
 * <b>Write locks</b> are implemented as exclusive cumulative locks, so you can only
 * get a write lock for a principal if no other user holds a (read or write) lock for
 * the same principal simultaneously.
 * 
 * You can hold both a read lock and a write lock for the same principal at the same
 * same time. When you have a read lock and get a write lock for the same principal,
 * the read lock is not "transformed" into a write lock, but the write lock is
 * retrieved additionally. You need to release both locks independently from each
 * other.
 * 
 * <b>Example:</b>
 * 
 * We have a principal administration application which only displays a principal at
 * first and then allows to switch to modification mode. When displaying a user, the
 * application tries to retrieve a read lock for the principal. When switching to
 * modification mode, the application retrieves an additional write lock. While
 * saving the changes or cancelling modification mode, the application needs to
 * release the write lock. When the user leaves the application or selects another
 * principal (i.e. when the first principal is no longer displayed), the application
 * also needs to release the read lock.
 * 
 */
public class JmxLockingManager {

    private static final Location LOCATION = Location.getLocation(JmxLockingManager.class);

    private static final String NAMESPACE   = "UME_Admin";
    private static final String DESCRIPTION = "UME Identity Management";

    // TODO Switch to LockingConstants.LIFETIME_TRANSACTION when using transactions in the UI?
    private static final byte LOCK_LIFETIME   = LockingConstants.LIFETIME_USERSESSION;
    private static final char LOCK_TYPE_READ  = LockingConstants.MODE_SHARED;
    private static final char LOCK_TYPE_WRITE = LockingConstants.MODE_EXCLUSIVE_CUMULATIVE;

    private static LogicalLocking _locking;

    /**
     * Initialize this class. Must be called during startup of JMX server.
     * 
     * This method builds the connection to SAP J2EE Engine's logical locking service.
     */
    public static void initialize() throws UMException {
        String method = "initialize()";

        LOCATION.infoT(method, "Initializing locking helper for UME JMX server.");

        try {
            Context context = new InitialContext();
            Object factoryObject = context.lookup("LogicalLockingFactory");

            LogicalLockingFactory lockingFactory = (LogicalLockingFactory) PortableRemoteObject.narrow(
                factoryObject, LogicalLockingFactory.class);

            // Remember the locking factory.
            _locking = lockingFactory.createLogicalLocking(NAMESPACE, DESCRIPTION);

            LOCATION.infoT(method, "Locking helper for UME JMX server successfully initialized.");
        }
        catch(Exception e) {
            LOCATION.traceThrowableT(Severity.ERROR, method,
                "Could not initialize logical locking for UME JMX server.",
                e);
            // How to handle this case? Throw a runtime exception?
            throw new UMException(e, "Initializing locking helper failed");
        }
    }

    /**
     * Lock a list of principals for reading.
     * 
     * @param principalUniqueIDs Unique IDs of all principals to lock.
     * @throws LockException
     * @throws TechnicalLockException
     */
    public static void createReadLock(String[] principalUniqueIDs) throws LockException, TechnicalLockException {
        final String method = "createReadLock(String)";

        String[] names     = getNamespaces(principalUniqueIDs.length);
        String[] arguments = LockManager.escapePrincipalIDs(principalUniqueIDs);
        char[]   modes     = getLockModes(principalUniqueIDs.length, LOCK_TYPE_READ);

        try {
            _locking.lock(LOCK_LIFETIME, names, arguments, modes);

            if(LOCATION.beInfo()) {
                LOCATION.infoT(method, "Got read locks for the following principals: {0}",
                    new Object[] { Arrays.asList(principalUniqueIDs).toString() } );
            }
        }
        catch(com.sap.engine.frame.core.locking.LockException e) {
            if(LOCATION.beInfo()) {
                LOCATION.traceThrowableT(Severity.INFO, method,
                    "Could not get read locks for the following principals because of a conflicting lock: {0}",
                    new Object[] { Arrays.asList(principalUniqueIDs).toString() }, e);
            }

            throw new LockException(e);
        }
        //catch(com.sap.engine.frame.core.locking.TechnicalLockException e) {
        //catch(IllegalArgumentException e) {
        catch(Exception e) {
            LOCATION.traceThrowableT(Severity.ERROR, method,
                "An internal error occured while locking the following principals for reading: {0}",
                new Object[] { Arrays.asList(principalUniqueIDs).toString() }, e);
            throw new TechnicalLockException(e);
        }
    }

    /**
     * Lock a list of principals for writing.
     * 
     * @param principalUniqueIDs Unique IDs of all principals to lock.
     * @throws TechnicalLockException
     * @throws com.sap.engine.frame.core.locking.LockException 
     */
    public static void createWriteLock(String[] principalUniqueIDs) throws TechnicalLockException, com.sap.engine.frame.core.locking.LockException {
        final String method = "createWriteLock(String)";

        String[] names     = getNamespaces(principalUniqueIDs.length);
        String[] arguments = LockManager.escapePrincipalIDs(principalUniqueIDs);
        char[]   modes     = getLockModes(principalUniqueIDs.length, LOCK_TYPE_WRITE);

        try {
            _locking.lock(LOCK_LIFETIME, names, arguments, modes);

            if(LOCATION.beInfo()) {
                LOCATION.infoT(method, "Got write locks for the following principals: {0}",
                    new Object[] { Arrays.asList(principalUniqueIDs).toString() } );
            }
        }
        catch(com.sap.engine.frame.core.locking.LockException e) {
            if(LOCATION.beInfo()) {
                LOCATION.traceThrowableT(Severity.INFO, method,
                    "Could not get write locks for the following principals because of a conflicting lock: {0}",
                    new Object[] { Arrays.asList(principalUniqueIDs).toString() }, e);
            }

            throw e;
        }
        //catch(com.sap.engine.frame.core.locking.TechnicalLockException e) {
        //catch(IllegalArgumentException e) {
        catch(Exception e) {
            LOCATION.traceThrowableT(Severity.ERROR, method,
                "An internal error occured while locking the following principals for writing: {0}",
                new Object[] { Arrays.asList(principalUniqueIDs).toString() }, e);
            throw new TechnicalLockException(e);
        }
    }

    /**
     * Release read locks for a list of principals.
     * 
     * @param principalUniqueIDs Unique IDs of all principals to lock.
     * @throws LockException
     * @throws TechnicalLockException
     */
    public static void releaseReadLock(String[] principalUniqueIDs) throws LockException, TechnicalLockException {
        final String method = "releaseReadLock(String)";

        String[] names     = getNamespaces(principalUniqueIDs.length);
        String[] arguments = LockManager.escapePrincipalIDs(principalUniqueIDs);
        char[]   modes     = getLockModes(principalUniqueIDs.length, LOCK_TYPE_READ);

        try {
            _locking.unlock(LOCK_LIFETIME, names, arguments, modes);

            if(LOCATION.beInfo()) {
                LOCATION.infoT(method, "Released read locks for the following principals: {0}",
                    new Object[] { Arrays.asList(principalUniqueIDs).toString() } );
            }
        }
        //catch(com.sap.engine.frame.core.locking.TechnicalLockException e) {
        //catch(IllegalArgumentException e) {
        catch(Exception e) {
            InternalUMFactory.CATEGORY.warningT(LOCATION, method,
                "An internal error occured while releasing read locks for the following principals: {0}.\n" +
                "This prevents modification of the principals in the Identity Management tool.\n" +
                "You might release the locks manually in the system's Locking Adapter with the " +
                "following lock information:\n" +
                "Name     : \"{1}\"\n" +
                "Arguments: {2}",
                new Object[] {
                    Arrays.asList(principalUniqueIDs).toString(),
                    NAMESPACE,
                    Arrays.asList(arguments)
                } );
            LOCATION.traceThrowableT(Severity.ERROR, method, e);

            throw new TechnicalLockException(e);
        }
    }

    /**
     * Release write locks for a list of principals.
     * 
     * @param principalUniqueIDs Unique IDs of all principals to lock.
     * @throws LockException
     * @throws TechnicalLockException
     */
    public static void releaseWriteLock(String[] principalUniqueIDs) throws LockException, TechnicalLockException {
        final String method = "releaseWriteLock(String)";

        String[] names     = getNamespaces(principalUniqueIDs.length);
        String[] arguments = LockManager.escapePrincipalIDs(principalUniqueIDs);
        char[]   modes     = getLockModes(principalUniqueIDs.length, LOCK_TYPE_WRITE);

        try {
            _locking.unlock(LOCK_LIFETIME, names, arguments, modes);

            if(LOCATION.beInfo()) {
                LOCATION.infoT(method, "Released write locks for the following principals: {0}",
                    new Object[] { Arrays.asList(principalUniqueIDs).toString() } );
            }
        }
        //catch(com.sap.engine.frame.core.locking.TechnicalLockException e) {
        //catch(IllegalArgumentException e) {
        catch(Exception e) {
            InternalUMFactory.CATEGORY.warningT(LOCATION, method,
                "An internal error occured while releasing write locks for the following principals: {0}.\n" +
                "This prevents modification of the principals in the Identity Management tool.\n" +
                "You might release the locks manually in the system's Locking Adapter with the " +
                "following lock information:\n" +
                "Name     : \"{1}\"\n" +
                "Arguments: {2}",
                new Object[] {
                    Arrays.asList(principalUniqueIDs).toString(),
                    NAMESPACE,
                    Arrays.asList(arguments)
                } );
            LOCATION.traceThrowableT(Severity.ERROR, method, e);

            throw new TechnicalLockException(e);
        }
    }

    private static String[] getNamespaces(int arraySize) {
        String[] namespaces = new String[arraySize];

        for(int i = 0; i < namespaces.length; i++) { namespaces[i] = NAMESPACE; }

        return namespaces;
    }

    private static char[] getLockModes(int arraySize, char lockMode) {
        char[] lockModes = new char[arraySize];

        for(int i = 0; i < lockModes.length; i++) {
            lockModes[i] = lockMode;
        }

        return lockModes;
    }

}
