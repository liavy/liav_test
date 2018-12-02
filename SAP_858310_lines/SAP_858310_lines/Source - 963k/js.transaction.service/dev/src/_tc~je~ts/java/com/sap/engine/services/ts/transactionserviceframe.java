/*
 * Copyright (c) 2002 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */

package com.sap.engine.services.ts;

import java.io.File;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.ContextNotEmptyException;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.sql.DataSource;
import javax.transaction.SystemException;

import com.sap.engine.admin.model.ManagementModelManager;
import com.sap.engine.admin.model.jsr77.JSR77ObjectNameFactory;
import com.sap.engine.admin.model.jsr77.JTAResource;
import com.sap.engine.frame.ApplicationServiceContext;
import com.sap.engine.frame.ApplicationServiceFrame;
import com.sap.engine.frame.ServiceConfigurationException;
import com.sap.engine.frame.ServiceException;
import com.sap.engine.frame.cluster.ClusterElement;
import com.sap.engine.frame.cluster.event.ClusterEventListener;
import com.sap.engine.frame.cluster.message.ListenerAlreadyRegisteredException;
import com.sap.engine.frame.container.event.ContainerEventListenerAdapter;
import com.sap.engine.frame.core.CoreContext;
import com.sap.engine.frame.core.locking.ServerInternalLocking;
import com.sap.engine.frame.core.locking.TechnicalLockException;
import com.sap.engine.frame.core.thread.ThreadSystem;
import com.sap.engine.frame.core.thread.execution.Executor;
import com.sap.engine.interfaces.resourcecontext.ResourceContextFactory;
import com.sap.engine.interfaces.resourceset.ResourceSetFactory;
import com.sap.engine.interfaces.shell.Command;
import com.sap.engine.interfaces.shell.ShellInterface;
import com.sap.engine.interfaces.transaction.RMRepository;
import com.sap.engine.services.ts.command.AbandonTransaction;
import com.sap.engine.services.ts.command.ClearTransactionStatistics;
import com.sap.engine.services.ts.command.ForgetTransaction;
import com.sap.engine.services.ts.command.ListPendingTransactions;
import com.sap.engine.services.ts.command.ListTxStats;
import com.sap.engine.services.ts.exceptions.BaseServiceException;
import com.sap.engine.services.ts.exceptions.BaseSystemException;
import com.sap.engine.services.ts.exceptions.ExceptionConstants;
import com.sap.engine.services.ts.exceptions.TimeOutIsStoppedException;
import com.sap.engine.services.ts.facades.crypter.AbstractCrypter;
import com.sap.engine.services.ts.facades.crypter.ServerCrypter;
import com.sap.engine.services.ts.facades.timer.ServerTimeoutManager;
import com.sap.engine.services.ts.facades.timer.TimeoutManager;
import com.sap.engine.services.ts.jmx.JTAResourceImpl;
import com.sap.engine.services.ts.jta.impl.AppTransactionManager;
import com.sap.engine.services.ts.jta.impl.TransactionSynchronizationRegistryImpl;
import com.sap.engine.services.ts.jta.impl2.SAPXidImpl;
import com.sap.engine.services.ts.jta.impl2.TXR_TransactionManagerImpl;
import com.sap.engine.services.ts.jta.impl2.TXR_UserTransaction;
import com.sap.engine.services.ts.jts.TransactionServiceImpl;
import com.sap.engine.services.ts.mbeans.TransactionServiceMBeansRegistrator;
import com.sap.engine.services.ts.recovery.RMContainerRegistryImpl;
import com.sap.engine.services.ts.recovery.RMRepositoryImpl;
import com.sap.engine.services.ts.recovery.RecoveryTask;
import com.sap.engine.services.ts.tlog.TLog;
import com.sap.engine.services.ts.tlog.TLogAlreadyExistException;
import com.sap.engine.services.ts.tlog.TLogIOException;
import com.sap.engine.services.ts.tlog.TLogReaderWriter;
import com.sap.engine.services.ts.tlog.db.DBTLogReaderWriter;
import com.sap.engine.services.ts.tlog.fs.FSTLogOptimizator;
import com.sap.engine.services.ts.tlog.fs.FSTLogReaderWriter;
import com.sap.engine.services.ts.tlog.util.TLogLocking;
import com.sap.engine.services.ts.tlog.util.TLogLockingImpl;
import com.sap.engine.services.ts.transaction.TxManagerImpl;
import com.sap.engine.services.ts.utils.TLogVersion;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

/**
 * This class supports the frame for Transaction Service.
 *
 * @author : Iliyan Nenov, ilian.nenov@sap.com
 * @version 1.0
 */
public class TransactionServiceFrame extends ContainerEventListenerAdapter implements ApplicationServiceFrame, ClusterEventListener {

    private static final Location LOCATION = Location.getLocation(TransactionServiceFrame.class);

    private static final int MASK = MASK_INTERFACE_AVAILABLE | MASK_INTERFACE_NOT_AVAILABLE | MASK_SERVICE_STARTED | MASK_SERVICE_STOPPED;
 
    private static final int DEFAULT_TRANSACTION_TIMEOUT = 86400; // 86400 seconds equivalent to 24 hours   
    private static final long DEFAULT_RECOVERY_RETRY_INTERVAL = 30;// in seconds 
    private static final long DEFAULT_ABANDON_TIMEOUT = 86400;// in seconds = 24h    
    
    private static TimeoutManager timeoutManager = null;
    private TXR_TransactionManagerImpl tManager = null;
    private AppTransactionManager appTransactionManager = null;
    private TransactionSynchronizationRegistryImpl transactionSynchronizationRegistry = null;
    private TXR_UserTransaction userTransaction = null;
    private TransactionServiceImpl corbaTS = null;
    private Context naming = null;
    public static ThreadSystem threadSystem = null;
    public static int txContextID = -1; 
    public static ApplicationServiceContext serviceContext = null;
    private static ResourceSetFactory resourceSetFactory = null;
    private static ResourceContextFactory resourceContextFactory = null;
    private ManagementModelManager mModelManager = null;
    private Set<String> neededInterfaces;
    private static RMRepository rmRepository = null;
    private static RMContainerRegistryImpl rmContainerRegistryImpl = null;    
    private static TLogReaderWriter tLogReaderWriter = null;
    private static DataSource dataSourceForDBTLog = null;
    private static TLogVersion tLogVersion = null;
	private static TLog tLog = null;
	private int commandsID = 0;
	Command[] commands = null;
	private ShellInterface shell = null;
	private static AbstractCrypter crypter = null;
	
	// Service properties    
    public static boolean enableLocalResourceInOTS = false;//online modifiable
    public static int txTimeout = DEFAULT_TRANSACTION_TIMEOUT;//online modifiable 
    public static int lockingTimeout = 60000;//timeout is in milliseconds. Default max wait is 60 seconds. online modifiable
    public static boolean isDBTLog = true;// default is DB TLog which is stored into system DB, NOT online modifiable
    public static int maxTransactionClassifiers = 1000;// 1000 is default value, online modifiable TODO only increased value is possible
    private static File fsTLogFolder = new File(".");// NOT online modifiable 
    private static String dataSourceLookupName = "SAP/BC_TRANSACTION_LOG";// NOT online modifiable	
    public static boolean enableTransactionLogging = true;// online modifiable   
    public static boolean enableDetailedTransactionStatistics = true;// online modifiable    
    public static long recoveryRetryInterval = DEFAULT_RECOVERY_RETRY_INTERVAL;//in seconds ;TODO use it ;; online modifiable
    private static long abandonTimeout = DEFAULT_ABANDON_TIMEOUT;//in seconds ;TODO use it;; online modifiable
    public static boolean callForgetAfterHeuristics = true;// online modifiable    
    public static boolean commitHeuristicallyInboundTx = false;
    public static long abandonTimeoutForInboundTx = DEFAULT_ABANDON_TIMEOUT;// online modifiable. in seconds
    public static long waitBetweenFlushesForOptimizer = 25; // in milliseconds. online modifiable
    public static long maxTLogFileSize = 8*1024*1024; // in bytes. online modifiable
    public static int tLogBufferCapacity = 100; // recommended to be the number of application threads. In number of elements into the buffer. not online modifiable
    public static int retryAttemptsWhenRMisUnreachable = 100;
    
    
    public TransactionServiceFrame() {
        neededInterfaces = new HashSet<String>(6);
        neededInterfaces.add("resourceset_api");
        neededInterfaces.add("resourcecontext_api");
        neededInterfaces.add("timeout");
        neededInterfaces.add("basicadmin");
        neededInterfaces.add("jmx");
        neededInterfaces.add("shell");
    }

    /**
     * The start method of transaction service. Starts the service
     *
     * @exception ServiceException thrown when the ServiceManager fails to load this service
     */
    public void start(ApplicationServiceContext serviceContext) throws ServiceException {
        this.serviceContext = serviceContext;
        Properties props = serviceContext.getServiceState().getProperties();

        try{
        	initializeServiceProperties(props, false);
        } catch (Exception e){
        	SimpleLogger.traceThrowable(Severity.WARNING, LOCATION, "Transaction service properties are not correctly set.", e);
        	//TODO better logging and Exception
        }
        
        threadSystem = serviceContext.getCoreContext().getThreadSystem();
        txContextID = threadSystem.registerContextObject(TransactionContextObject.NAME, new TransactionContextObject());
        try {
			ServerInternalLocking serverInternalLocking = serviceContext.getCoreContext().getLockingContext().createServerInternalLocking(TLogLocking.LOCKING_NAMESPACE, "Transaction log locking");
			TLogLocking locking = new TLogLockingImpl(serverInternalLocking);			
			if(isDBTLog){
				tLogReaderWriter = new DBTLogReaderWriter(locking);
				// TODO timeout manager
			} else {
				if(fsTLogFolder == null){
					throw new ServiceConfigurationException(LOCATION, new Exception("Transaction service is configured to store transaction logs on a file system but log location is not specified."));
				}
				if(!fsTLogFolder.exists()){
					throw new ServiceConfigurationException(LOCATION, new Exception("Transaction service is configured to store transaction logs on a file system but specified log location folder does not exist."));
				}

				Executor executor = threadSystem.createCleanThreadExecutor(
						"FSTLog flusher thread.",
						Executor.MAX_CONCURRENCY_ALLOWED, 10,
						Executor.IMMEDIATE_START_POLICY);
				tLogReaderWriter = new FSTLogReaderWriter(fsTLogFolder,
						locking, executor, tLogBufferCapacity, maxTLogFileSize);
				// to do timeout manager 
			}
		} catch (TechnicalLockException e) {
			throw new ServiceConfigurationException(LOCATION, e);
        	//TODO better logging and Exception
		} catch (IllegalArgumentException e) {
			throw new ServiceConfigurationException(LOCATION, e);
        	//TODO better logging and Exception
		}
       
        tLogVersion = new TLogVersion(serviceContext.getClusterContext().getClusterMonitor().getClusterName().getBytes(),
        		serviceContext.getClusterContext().getClusterMonitor().getCurrentParticipant().getClusterId(),
        		System.currentTimeMillis());        
	  	SAPXidImpl.initializeStaticValues(tLogVersion);		
		
        // creating ts objects
        tManager = new TXR_TransactionManagerImpl();
        appTransactionManager = new AppTransactionManager(tManager);
        TxManagerImpl.getInstance().setTransactionManager(tManager);
        com.sap.transaction.TxManager.setTxManagerImpl(TxManagerImpl.getInstance());

        //initialize RMRepositoryImpl
        rmRepository = new RMRepositoryImpl((TXR_TransactionManagerImpl)tManager);
        rmContainerRegistryImpl = new RMContainerRegistryImpl();
        
        // we can make these stuff below to take advantage of the static tManager:
        TransactionServiceManagementImpl tsManagement = new TransactionServiceManagementImpl(tManager);
        userTransaction = new TXR_UserTransaction(tManager);
        //userTransaction = new UserTransaction();
        corbaTS = new TransactionServiceImpl(tManager);

        transactionSynchronizationRegistry =  new TransactionSynchronizationRegistryImpl(tManager);
        

        try {
            naming = new InitialDirContext();
            naming.rebind("UserTransaction", userTransaction);
            naming.rebind("TransactionManager", appTransactionManager);
            naming.rebind("TransactionService", corbaTS);
            rebind(naming, "java:comp/TransactionSynchronizationRegistry", transactionSynchronizationRegistry);
        } catch (NamingException ne) {
        	LOCATION.traceThrowableT(Severity.ERROR, "Transaction Service cannot getInitialContext. Transaction Service can not start correctly.", ne);
        	SimpleLogger.log(Severity.ERROR, Category.SYS_SERVER, LOCATION, "ASJ.trans.000000",
        			"Transaction management will not work because transaction service cannot start. Check traces for more information.");
            throw new BaseServiceException(ExceptionConstants.Exception_TS_Cannot_start, ne);
        }
        // registers transaction context object
        serviceContext.getContainerContext().getObjectRegistry().registerInterface(appTransactionManager);
        // registers management interface
        serviceContext.getServiceState().registerManagementInterface(tsManagement);
        
        serviceContext.getContainerContext().getObjectRegistry().registerInterfaceProvider("transactionext", tManager);
        serviceContext.getContainerContext().getObjectRegistry().registerInterfaceProvider(RMContainerRegistryImpl.RMCONTAINER_REGISTRY_INTERFACE_NAME, rmContainerRegistryImpl);
        serviceContext.getContainerContext().getObjectRegistry().registerInterfaceProvider(RMRepositoryImpl.RMREPOSITORY_INTERFACE_NAME, rmRepository);

        serviceContext.getServiceState().registerContainerEventListener(MASK, neededInterfaces, this);
        
        serviceContext.getServiceState().registerRuntimeConfiguration(new ServicePropertyChanger());
        try {
        	serviceContext.getServiceState().registerClusterEventListener(this);
        } catch (ListenerAlreadyRegisteredException e) {
            SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, e, "ASJ.trans.000309", "Transaction recovery on the fly will not work because transaction service was not able to register as ClusterEventListener.", new Object[]{});
        }
        serviceContext.getCoreContext().getThreadSystem().startCleanThread(new RecoveryTask(tLogReaderWriter, rmContainerRegistryImpl), true);// true means that a new system thread will be started.
    }

    /**
     * Starts transaction service without transaction recovery support
     * 
     * @param serviceContext
     * @throws ServiceException
     */
//    public void start_without_recovery(ApplicationServiceContext serviceContext) throws ServiceException {
//    	throw new ServiceException("It is not possible to start transaction service without transaction recovery support.");
//    	
//        this.serviceContext = serviceContext;
//        Properties props = serviceContext.getServiceState().getProperties();
//
//        // reading properties
//        String result = props.getProperty("EnableLocalResourceInOTS");
//        if (result != null) {
//            enableLocalResourceInOTS = result.equalsIgnoreCase("true") || result.equalsIgnoreCase("enable") || result.equalsIgnoreCase("on") || result.equalsIgnoreCase("yes");
//        }
//
//        result = props.getProperty("TransactionTimeout");
//        if (result != null) {
//            txTimeout = (new Integer(result)).intValue();
//        }
//
//        threadSystem = serviceContext.getCoreContext().getThreadSystem();
//        txContextID = threadSystem.registerContextObject(TransactionContextObject.NAME, new TransactionContextObject());
//
//        //TODO initialize TLog
//        
//        // creating ts objects
//        tManager = new TransactionManagerImpl(serviceContext);
//        appTransactionManager = new AppTransactionManager(tManager);
//        TxManagerImpl.getInstance().setTransactionManager(tManager);
//        com.sap.transaction.TxManager.setTxManagerImpl(TxManagerImpl.getInstance());
//
//        // we can make these stuff below to take advantage of the static tManager:
//        TransactionServiceManagementImpl tsManagement = new TransactionServiceManagementImpl(tManager);
//        userTransaction = new UserTransaction(tManager);
//        //userTransaction = new UserTransaction();
//        corbaTS = new TransactionServiceImpl(tManager);
//
//        transactionSynchronizationRegistry =  new TransactionSynchronizationRegistryImpl(tManager);
//
//        try {
//            naming = new InitialDirContext();
//            naming.rebind("UserTransaction", userTransaction);
//            naming.rebind("TransactionManager", appTransactionManager);
//            naming.rebind("TransactionService", corbaTS);
//            rebind(naming, "java:comp/TransactionSynchronizationRegistry", transactionSynchronizationRegistry);
//        } catch (NamingException ne) {
//        	LOCATION.traceThrowableT(Severity.ERROR, "Transaction Service cannot getInitialContext. Transaction Service can not start correctly.", ne);
//        	SimpleLogger.log(Severity.ERROR, Category.SYS_SERVER, LOCATION, "ASJ.trans.000000",
//        			"Transaction management will not work because transaction service cannot start. Check traces for more information.");
//            throw new BaseServiceException(ExceptionConstants.Exception_TS_Cannot_start, ne);
//        }
//        // registers transaction context object
//        serviceContext.getContainerContext().getObjectRegistry().registerInterface(appTransactionManager);
//        // registers management interface
//        serviceContext.getServiceState().registerManagementInterface(tsManagement);
//        serviceContext.getContainerContext().getObjectRegistry().registerInterfaceProvider("transactionext", tManager);
//
//        serviceContext.getServiceState().registerContainerEventListener(MASK, neededInterfaces, this);
//    }
    
    
    /**
     * The stop method of this service. Stops the service
     */
    public void stop() {
        serviceContext.getContainerContext().getObjectRegistry().unregisterInterface();
        serviceContext.getServiceState().unregisterManagementInterface();
        serviceContext.getContainerContext().getObjectRegistry().unregisterInterfaceProvider("transactionext");
        threadSystem.unregisterContextObject(TransactionContextObject.NAME);
        txContextID = -1;
        
        ManagementModelManager myMModelManager = mModelManager;
    	try {
    	  if (myMModelManager != null) {
    	    myMModelManager.unregisterManagedObject(createJTAResourceObjectName(JSR77ObjectNameFactory.getJTAResourceName(JTAResourceImpl.TRANSACTION_MANAGER)));
    	  }
    	} catch(Exception exc) {
          if (LOCATION.beLogged(Severity.WARNING)) {
            LOCATION.traceThrowableT(Severity.WARNING, "Cannot unregister MBean for TransactionManager.", exc);
          }
    	}
        try {
            naming.unbind("UserTransaction");
            naming.unbind("TransactionManager");
            naming.unbind("TransactionService");
            unbind(naming, "java:comp/TransactionSynchronizationRegistry");
        } catch (NamingException ne) {
            if (LOCATION.beLogged(Severity.WARNING)) {
                LOCATION.traceThrowableT(Severity.WARNING, "UserTransaction and TransactionManager could not be unbound during transaction service stop.", ne);
            }
        }
        
        serviceContext.getServiceState().unregisterContainerEventListener();
        
        //log.unregister(logId);
        naming = null;
        //log = null;
        tManager = null;
        userTransaction = null;
        corbaTS = null;
        mModelManager = null;
        if(tLog != null){
        	try {
				tLog.flushRemovedTransactionRecords();
				tLog.close();
			} catch (TLogIOException e) {
	            if (LOCATION.beLogged(Severity.WARNING)) {
	                LOCATION.traceThrowableT(Severity.WARNING, "Transaction log was not closed propetly during server shutdown because of unexpected exception ", e);
	            }				
			}
        }
    }

    /**
     * This method is invoked for changing service properties
     *
     * @return true if the properties are changed, if something fails returns false
     */
    public boolean changeProperties(Properties new_properties) throws IllegalArgumentException {

      int new_txTimeout = txTimeout;

      // reading properties
      String result = new_properties.getProperty("EnableLocalResourceInOTS");
      if (result != null) {
          enableLocalResourceInOTS = result.equalsIgnoreCase("true") || result.equalsIgnoreCase("enable") || result.equalsIgnoreCase("on") || result.equalsIgnoreCase("yes");
      }

      result = new_properties.getProperty("TransactionTimeout");
      if (result != null) {
          new_txTimeout = (new Integer(result)).intValue();
          if(new_txTimeout >= 0){
            txTimeout = new_txTimeout;
          } else {// negative
        	return false;  
          }
      }

      return true;
    }

    /**
     * Returns the service interface. In this case it is the TransactionManager
     *
     * @return a reference to the ThransactionManager for the transaction service
     */
    public Object getServiceInterface() {
        return tManager;
    }

    public static ResourceSetFactory getResourceSetFactory() throws SystemException {
        ResourceSetFactory result = resourceSetFactory;
        if (result == null && LOCATION.beLogged(Severity.WARNING)) {
        	SimpleLogger.trace(Severity.WARNING,LOCATION, "ASJ.trans.000274", "Transaction service : Service Connector not available, trying to work with it.");
        }
        return result;
    }

    /**
	 * This method is used from the standalone transaction manager scenario to
	 * set ResourceSetFactory. It should not be called in server scenario.
	 * 
	 * @param resourceSetFactory the ResourceSetFactory
	 */
    public static void setResourceSetFactory(ResourceSetFactory resourceSetFactory){
    	TransactionServiceFrame.resourceSetFactory = resourceSetFactory;
    }

    public static ResourceContextFactory getResourceContextFactory() throws SystemException {
      ResourceContextFactory result = resourceContextFactory;
      if (result == null && LOCATION.beLogged(Severity.WARNING)) {
    	  SimpleLogger.trace(Severity.WARNING,LOCATION, "ASJ.trans.000104", "Transaction service : Service Connector not available, trying to work without it.");
      }
      return result;
    }

    /**
	 * This method is used from the standalone transaction manager scenario to
	 * set ResourceContextFactory. It should not be called in server scenario.
	 * 
	 * @param resourceContextFactory the ResourceContextFactory
	 */
    public static void setResourceContextFactory(ResourceContextFactory resourceContextFactory){
    	TransactionServiceFrame.resourceContextFactory = resourceContextFactory;
    }

    public static TimeoutManager getTimeoutManager(){
    	TimeoutManager localTimeoutManager = timeoutManager;
    	if(localTimeoutManager == null){
    		throw new TimeOutIsStoppedException();    		
    	}
    	return localTimeoutManager;
    }

	/**
	 * This method is used from the standalone transaction manager scenario to
	 * set timeoutManager. It should not be called in server scenario.
	 * 
	 * @param timeoutManager the TimeoutManager
	 */
    public static void setTimeoutManager(TimeoutManager timeoutManager){
    	TransactionServiceFrame.timeoutManager = timeoutManager;
    }

    public static DataSource getDataSourceForDBTlog() throws SQLException{
    	if(TransactionServiceFrame.dataSourceForDBTLog != null){
    		return TransactionServiceFrame.dataSourceForDBTLog;    		
    	}
    	
    	synchronized(TransactionServiceFrame.class){
	    	if(TransactionServiceFrame.dataSourceForDBTLog == null){    
	    		try {
					TransactionServiceFrame.dataSourceForDBTLog = (DataSource)(new InitialContext()).lookup("jdbc/notx/" + dataSourceLookupName);
				} catch (NamingException e1) {
					SQLException sqlException = new SQLException("DataSource which is used from TransactionManager for transaction logs is not available.");
					sqlException.initCause(e1);
					throw sqlException;
				}	    		
	    	}
	    	return TransactionServiceFrame.dataSourceForDBTLog;
    	}    	
    	
    }
    
   public static RMRepository getRmRepository(){
	   return rmRepository;
   }
    
   public static RMContainerRegistryImpl getRmContainerRegistryImpl(){
	   return rmContainerRegistryImpl;
   }

    
    /**
     * Will be used only from unit tests
     * @param dataSourceForDBTLog the DataSource instance which will be used from DB Tlogger
     */
    public static void setDataSourceForDBTlog(DataSource dataSourceForDBTLog){
    	TransactionServiceFrame.dataSourceForDBTLog = dataSourceForDBTLog;
    }    
    
    public static TLog getTLog(){
    	if(tLog != null){
    		return tLog;
    	}
    	if(!enableTransactionLogging){
    		return null; // there is no transaction log in this case
    	}
    	synchronized(TransactionServiceFrame.class){
	    	if(tLog == null){    		
	    		try {
					tLog = tLogReaderWriter.createNewTLog(tLogVersion.getTLogVersion());
				} catch (TLogIOException e) {
//					LOCATION.traceThrowableT(Severity.ERROR, "Transaction log will not be created for one or more tasactions because of unexpected exception.", e);
					SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e, "ASJ.trans.000105", "Transaction log will not be created for one or more tasactions because of unexpected exception.");
				} catch (TLogAlreadyExistException e) {
//					LOCATION.traceThrowableT(Severity.ERROR, "Transaction log will not be created for one or more tasactions because of unexpected exception.", e);
					SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e, "ASJ.trans.000106", "Transaction log will not be created for one or more tasactions because of unexpected exception.");
				}
	    	}
	    	if(tLog == null){// almost imposible case
//	    		LOCATION.logT(Severity.WARNING, "Transaction log will not be created for one or more tasactions because of unknow reason. Check traces for other exceptions.");
	    		SimpleLogger.trace(Severity.WARNING,LOCATION, "ASJ.trans.000275", "Transaction log will not be created for one or more tasactions because of unknow reason. Check traces for other exceptions.");
	    	}
	    	return tLog;
    	}
    }
    /**
     * Method invoked when a needed interface is available
     *
     * @param interfaceName the name of the interface
     * @param interfaceImpl the implementation of the interface
     */
    public void interfaceAvailable(String interfaceName, Object interfaceImpl) {
        if (interfaceName.equals("resourceset_api")) {
            resourceSetFactory = (ResourceSetFactory)interfaceImpl;
        } else if (interfaceName.equals("resourcecontext_api")) {
            resourceContextFactory = (ResourceContextFactory)interfaceImpl;
        } else if (interfaceName.equals("shell")) {
        	shell = (ShellInterface) interfaceImpl;
        	commands = new Command[5];
        	commands[0] = new ListPendingTransactions();
        	commands[1] = new AbandonTransaction();
        	commands[2] = new ClearTransactionStatistics();
        	commands[3] = new ForgetTransaction();
        	commands[4] = new ListTxStats();
        	commandsID = shell.registerCommands(commands);
        }
    }

    /**
     * Method invoked when a needed interface is not available
     *
     * @param interfaceName the name of the interface
     */
    public void interfaceNotAvailable(String interfaceName) {
        if (interfaceName.equals("resourceset_api")) {
            resourceSetFactory = null;
        } else if (interfaceName.equals("resourcecontext_api")) {
            resourceContextFactory = null;
        } else if (interfaceName.equals("shell")) {
        	shell.unregisterCommands(commandsID);
        	shell = null;
        }
    }

    public void serviceStarted(String serviceName, Object serviceInterface) {
        if ("timeout".equals(serviceName)) {
            timeoutManager = new ServerTimeoutManager(serviceInterface);
        } else if ("basicadmin".equals(serviceName)) {
          mModelManager = (ManagementModelManager)serviceInterface;
          String internalName = JSR77ObjectNameFactory.getJTAResourceName(JTAResourceImpl.TRANSACTION_MANAGER);
          JTAResourceImpl jtaResource = new JTAResourceImpl(internalName, JTAResourceImpl.TRANSACTION_MANAGER);
          try {
            mModelManager.registerManagedObject(jtaResource, JTAResource.class);
		  } catch (Exception exc) {
//		    LOCATION.traceThrowableT(Severity.ERROR, "TransactionServiceFrame.serviceStarted(), Cannot register MBean for TransactionManager, reason: ", exc);
		    SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,exc, "ASJ.trans.000107", "TransactionServiceFrame.serviceStarted(), Cannot register MBean for TransactionManager, reason: ");
		  }

		  int nodeId = serviceContext.getClusterContext().getClusterMonitor()
					.getCurrentParticipant().getClusterId();
		  MBeanServer jmx = (MBeanServer) serviceContext.getContainerContext().
		  			getObjectRegistry().getServiceInterface("jmx");
		  TransactionServiceMBeansRegistrator
		  			.registerTransactionServiceMBeans(jmx, nodeId);
        }
    }

    public void serviceStopped(String serviceName) {
        if ("timeout".equals(serviceName)) {
            timeoutManager = null;
        } else if ("basicadmin".equals(serviceName)) {
        	mModelManager = null;

  		  int nodeId = serviceContext.getClusterContext().getClusterMonitor()
					.getCurrentParticipant().getClusterId();
  		  MBeanServer jmx = (MBeanServer) serviceContext
					.getContainerContext().getObjectRegistry().getServiceInterface("jmx");
  		  TransactionServiceMBeansRegistrator
					.unregisterTransactionServiceMBeans(jmx, nodeId);
        }
    }

    public static ThreadSystem getThreadSystem() {
    	if(serviceContext != null){
    		CoreContext cc = serviceContext.getCoreContext();
    		return cc!=null ? cc.getThreadSystem() : null;
    	} else {
    		return null;
    	}
    }
    
  protected static void initializeServiceProperties(Properties props, boolean isRuntimeChange) throws ServiceException{
	  
  	Iterator<Entry<Object, Object>> iterProperties = props.entrySet().iterator();
  	
  	while (iterProperties.hasNext()) {
	  
		Entry<Object, Object> entryProperties = iterProperties.next();
  		String key = ((String)entryProperties.getKey()).trim();
  		String value = ((String)entryProperties.getValue()).trim();
  		
  		if(value == null){
  			value = "";// to ignore NullpointerExceptions. 
  		}
  		
  		if(key.equals("EnableLocalResourceInOTS")){//online
  			enableLocalResourceInOTS = "true".equalsIgnoreCase(value) || "enable".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
  		} else if(key.equals("TransactionTimeout")){//online
  	   	    try{
    		  txTimeout = Integer.parseInt(value);
    	    } catch (NumberFormatException e){
    	      if(isRuntimeChange){
    	    	 throw new ServiceException("TransactionTimeout was not set correctly and default value " + txTimeout + " seconds will be used. Reason for the exception :" + e.toString(), e);
    	      } else {
//    	    	LOCATION.logT(Severity.WARNING, "TransactionTimeout was not set correctly and default value " + txTimeout + " seconds will be used. Reason for the exception :" + e.toString());
    	    	SimpleLogger.trace(Severity.WARNING,LOCATION, "ASJ.trans.000276", "ransactionTimeout was not set correctly and default value {0} seconds will be used. Reason for the exception :{1}",  new Object[] { txTimeout,e.toString()});
    	      }
    	    }  			
  		} else if(key.equals("LockingTimeout")){//online
      	  try{
    		  lockingTimeout = Integer.parseInt(value);
    	  } catch (NumberFormatException e){
    		  if(isRuntimeChange){
    			  throw new ServiceException("LockingTimeout for transaction logs was not set correctly and default value " + lockingTimeout/1000 + " seconds will be used. Reason for the exception :" + e.toString(), e);
    		  } else {
//    			  LOCATION.logT(Severity.WARNING, "LockingTimeout for transaction logs was not set correctly and default value " + lockingTimeout/1000 + " seconds will be used. Reason for the exception :" + e.toString());
    			  SimpleLogger.trace(Severity.WARNING,LOCATION, "ASJ.trans.000277", "LockingTimeout for transaction logs was not set correctly and default value {0} seconds will be used. Reason for the exception :{1}",  new Object[] { lockingTimeout/1000,e.toString()});
    		  }
    	  }            			
  		} else if(key.equals("TLogStorageType")){//offline
  			if(isRuntimeChange){
  				throw new ServiceException("TLogStorageType property can be modified only offline.");
  			} else {
  				isDBTLog = "DB".equalsIgnoreCase(value);
  			}
  		} else if(key.equals("MaxTransactionClassifiersCount")){//online
      	  try{
    		  maxTransactionClassifiers = Integer.parseInt(value);
    	  } catch (NumberFormatException e){
    		  if(isRuntimeChange){
    			  throw new ServiceException("MaxTransactionClassifiersCount property of transaction service was not set correctly and default value " + maxTransactionClassifiers + " will be used. Reason for the exception :" + e.toString(), e);
    		  } else {
//    			  LOCATION.logT(Severity.WARNING, "MaxTransactionClassifiersCount property of transaction service was not set correctly and default value " + maxTransactionClassifiers + " will be used. Reason for the exception :" + e.toString());
    			  SimpleLogger.trace(Severity.WARNING,LOCATION, "ASJ.trans.000278", "MaxTransactionClassifiersCount property of transaction service was not set correctly and default value {0} will be used. Reason for the exception :{1}",  new Object[] { maxTransactionClassifiers,e.toString()});
    		  }
    	  }                    
  		} else if(key.equals("FSTLogLocation")){//offline
  			if(isRuntimeChange){
  				throw new ServiceException("FSTLogLocation property can be modified only offline.");
  			} else {
  				fsTLogFolder = new File(value);
  			}  		
  		} else if(key.equals("DataSourceName")){//offline
  			if(isRuntimeChange){
  			  throw new ServiceException("DataSourceName property can be modified only offline.");
  			} else {
  	    	  if(!"".equals(value)){
  	            dataSourceLookupName = value;
  	     	  }				
  			}
  		} else if(key.equals("EnableTransactionLogging")){//online
  			enableTransactionLogging = "TRUE".equalsIgnoreCase(value) || "YES".equalsIgnoreCase(value);
  		} else if(key.equals("EnableDetailedTransactionStatistics")){//online
  			enableDetailedTransactionStatistics = "TRUE".equalsIgnoreCase(value) || "YES".equalsIgnoreCase(value);  			
  		} else if(key.equals("RecoveryRetryInterval")){//online
      	    try{
    		  recoveryRetryInterval = Long.parseLong(value);
    	    } catch (NumberFormatException e){
    	    	if(isRuntimeChange){
    	    	  throw new ServiceException("RecoveryRetryInterval property of transaction service was not set correctly and default value " + recoveryRetryInterval + " seconds will be used. Reason for the exception :" + e.toString(), e);
    	    	} else {
//    	    	  LOCATION.logT(Severity.WARNING, "RecoveryRetryInterval property of transaction service was not set correctly and default value " + recoveryRetryInterval + " seconds will be used. Reason for the exception :" + e.toString());
    	    	  SimpleLogger.trace(Severity.WARNING,LOCATION, "ASJ.trans.000279", "RecoveryRetryInterval property of transaction service was not set correctly and default value {0} seconds will be used. Reason for the exception :{1}",  new Object[] { recoveryRetryInterval,e.toString()});
    	    	}
    	    }     			
  		} else if(key.equals("AbandonTimeout")){//online
      	  try{
    		  abandonTimeout = Long.parseLong(value);
    	  } catch (NumberFormatException e){
    		  if(isRuntimeChange){
    			  throw new ServiceException("AbandonTimeout property of transaction service was not set correctly and default value " + abandonTimeout + " seconds will be used. Reason for the exception :" + e.toString(), e);
    		  } else {
//    			  LOCATION.logT(Severity.WARNING, "AbandonTimeout property of transaction service was not set correctly and default value " + abandonTimeout + " seconds will be used. Reason for the exception :" + e.toString());
    			  SimpleLogger.trace(Severity.WARNING,LOCATION, "ASJ.trans.000280", "AbandonTimeout property of transaction service was not set correctly and default value {0} seconds will be used. Reason for the exception :{1}", new Object[] {abandonTimeout, e.toString()});
    		  }
    	  }     			
 		} else if(key.equals("CallForgetAfterHeuristicDecisions")){//online
 			callForgetAfterHeuristics = "TRUE".equalsIgnoreCase(value) || "YES".equalsIgnoreCase(value);
  		} else if(key.equals("CommitHeuristicallyInboundTx")){//online
  			commitHeuristicallyInboundTx = "TRUE".equalsIgnoreCase(value) || "YES".equalsIgnoreCase(value);
  		} else if(key.equals("RetryAttemptsWhenRMisUnreachable")){
        	  try{
        		  retryAttemptsWhenRMisUnreachable = Integer.parseInt(value);
        	  } catch (NumberFormatException e){
        		  if(isRuntimeChange){
        			  throw new ServiceException("RetryAttemptsWhenRMisUnreachable property of transaction service was not set correctly and default value " + retryAttemptsWhenRMisUnreachable + " will be used. Reason for the exception :" + e.toString(), e);
        		  } else {
        			  SimpleLogger.trace(Severity.WARNING,LOCATION, "ASJ.trans.000010", "RetryAttemptsWhenRMisUnreachable property of transaction service was not set correctly and default value {0} will be used. Reason for the exception :{1}", new Object[] {retryAttemptsWhenRMisUnreachable, e.toString()});
        		  }
        	  }     			
  		} else if(key.equals("AbandonTimeoutForInboundTx")){ //online
  			try {
  				abandonTimeoutForInboundTx = Long.parseLong(value);
  			} catch(NumberFormatException e) {
  				if(isRuntimeChange) {
  					throw new ServiceException("AbandonTimeoutForInboundTx property of transaction service was not set correctly and default value " + abandonTimeoutForInboundTx + " seconds will be used. Reason for the exception :" + e.toString(),e);
  				} else {
//  					LOCATION.logT(Severity.WARNING, "AbandonTimeoutForInboundTx property of transaction service was not set correctly and default value " + abandonTimeoutForInboundTx + " seconds will be used. Reason for the exception :" + e.toString());
  					SimpleLogger.trace(Severity.WARNING,LOCATION, "ASJ.trans.000281", "AbandonTimeoutForInboundTx property of transaction service was not set correctly and default value {0} seconds will be used. Reason for the exception :{1}",  new Object[] { abandonTimeoutForInboundTx,e.toString()});
  				}
  			}
  		} else if(key.equals("WaitBetweenFlushesForOptimizer")){ // online
  			try {
  				waitBetweenFlushesForOptimizer = Long.parseLong(value);
  				FSTLogOptimizator.setWaitBetweenFlushes(waitBetweenFlushesForOptimizer);
  			} catch (NumberFormatException e) {
				if(isRuntimeChange) {
					throw new ServiceException("WaitBetweenFlushesForOptimizer property of transaction service was not set correctly and default value " + waitBetweenFlushesForOptimizer + " seconds will be used. Reason for the exception :" + e.toString(),e); 
				} else {
//					LOCATION.logT(Severity.WARNING, "WaitBetweenFlushesForOptimizer property of transaction service was not set correctly and default value " + waitBetweenFlushesForOptimizer + " seconds will be used. Reason for the exception :" + e.toString());
					SimpleLogger.trace(Severity.WARNING,LOCATION, "ASJ.trans.000282", "WaitBetweenFlushesForOptimizer property of transaction service was not set correctly and default value {0} seconds will be used. Reason for the exception :{1}",  new Object[] { waitBetweenFlushesForOptimizer,e.toString()});
				}
			}
  		} else if(key.equals("MaxTLogFileSize")){ // online
  			try {
  				maxTLogFileSize = Long.parseLong(value) * 1024;
  			} catch (NumberFormatException e) {
  				if(isRuntimeChange) {
  					throw new ServiceException("MaxTLogFileSize property of transaction service was not set correctly and default value " + maxTLogFileSize + " seconds will be used. Reason for the exception :" + e.toString(),e);
  				} else {
//  					LOCATION.logT(Severity.WARNING, "MaxTLogFileSize property of transaction service was not set correctly and default value " + maxTLogFileSize + " seconds will be used. Reason for the exception :" + e.toString());
  					SimpleLogger.trace(Severity.WARNING,LOCATION, "ASJ.trans.000283", "MaxTLogFileSize property of transaction service was not set correctly and default value {0} seconds will be used. Reason for the exception :{1}",  new Object[] { maxTLogFileSize,e.toString()});
  				}
			}
  		} else if(key.equals("TLogBufferCapacity")){// offline
  			if(isRuntimeChange) {
  				throw new ServiceException("TLogBufferCapacity property can be modified only offline.");
  			} else {
  				tLogBufferCapacity = Integer.parseInt(value);
  			}
  		} else {
  			if(isRuntimeChange){
  				throw new ServiceException("Property " + key + " is not supported from transaction service.");
  			} else {
//  				LOCATION.logT(Severity.WARNING, "Property " + key + " is not supported from transaction service and will be ignored.");
  				SimpleLogger.trace(Severity.ERROR,LOCATION, "ASJ.trans.000284", "Property {0} is not supported from transaction service and will be ignored.",  new Object[] { key});
  			}
  		}
  	}
  }  
    
  private void rebind(Context root, String name, Object obj) throws NamingException {
    Context ic = root;
    Context current;

    for (StringTokenizer st = new StringTokenizer(name, "/");;) {
      String s = st.nextToken();

      if (st.hasMoreTokens()) {
        try {
          current = (Context) ic.lookup(s);
        } catch (NamingException nex) {
          try {
            current = ic.createSubcontext(s);
          } catch (NameAlreadyBoundException nabe) {
            current = (Context) ic.lookup(s);
          }
        }
        ic = current;
      } else {
        ic.rebind(s, obj);
        break;
      }
    }
  }

  private void unbind(Context root, String name) throws NamingException {
    int index = name.lastIndexOf('/');

    if (index == -1) {
      root.unbind("/" + name);
    } else {
      ((Context) root.lookup(name.substring(0, index))).unbind("/" + name.substring(index + 1));
      destroyContext(root, name.substring(0, index));
    }
  }

  private void destroyContext(Context root, String contextName) throws NamingException {
    if (contextName.equals("")) {
      return;
    }
    int index = contextName.indexOf('/');
    try {
      if (index == -1) {
        root.destroySubcontext(contextName);
      } else {
        String s = contextName.substring(0, index);
        destroyContext((Context) root.lookup(contextName.substring(0, index)), contextName.substring(index + 1));
        root.destroySubcontext(s);
      }
    } catch (ContextNotEmptyException cne) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
        LOCATION.traceThrowableT(Severity.DEBUG, "TransactionServiceFrame.destroyContext(): Cannot destroy context '" + contextName + "'. Full stacktrace: ", cne);
      }
    }
  }
  
  private ObjectName createJTAResourceObjectName(String internalName) throws Exception {
	  return new ObjectName(":"+internalName); 
  }

  public static TLogReaderWriter getTLogReaderWriter() {
	  return tLogReaderWriter;
  }

  public static void setTLogReaderWriter(TLogReaderWriter logRW) {
	  tLogReaderWriter = logRW;
  }
  
  public static void setTLogVersion(TLogVersion tVer){
	  tLogVersion = tVer;
  }
  public void elementJoin(ClusterElement arg0) {
	//This event is not used.
  }

  public void elementLoss(ClusterElement clusterElement) {
	// This event is used for implementation of recovery on the fly  
	if(serviceContext != null && tLogReaderWriter != null && rmContainerRegistryImpl != null){
		serviceContext.getCoreContext().getThreadSystem().startCleanThread(new RecoveryTask(tLogReaderWriter, rmContainerRegistryImpl), true);// true means that a new system thread will be started.
	}	
  }

  public void elementStateChanged(ClusterElement arg0, byte arg1) {
	  //This event is not used.
  }

	public static AbstractCrypter getCrypter() {
		if (TransactionServiceFrame.crypter!=null) {
			return TransactionServiceFrame.crypter;
		}
		synchronized (TransactionServiceFrame.class) {
			if (TransactionServiceFrame.crypter!=null) {
				return TransactionServiceFrame.crypter;
			}
			TransactionServiceFrame.crypter = new ServerCrypter();
			return TransactionServiceFrame.crypter;
		}
	}

	public static void setCrypter(AbstractCrypter crypter) {
		synchronized (TransactionServiceFrame.class) {
			TransactionServiceFrame.crypter = crypter;
		}
	}

}