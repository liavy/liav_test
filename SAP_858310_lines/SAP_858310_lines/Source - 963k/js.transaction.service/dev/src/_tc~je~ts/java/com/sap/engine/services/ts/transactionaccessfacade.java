package com.sap.engine.services.ts;

import java.io.File;

import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;

import com.sap.engine.interfaces.resourcecontext.ResourceContextFactory;
import com.sap.engine.interfaces.resourceset.ResourceSetFactory;
import com.sap.engine.interfaces.transaction.TransactionManagerExtension;
import com.sap.engine.services.ts.facades.crypter.StandaloneCrypter;
import com.sap.engine.services.ts.facades.timer.SimpleTimeoutManager;
import com.sap.engine.services.ts.jta.impl.AppTransactionManager;
import com.sap.engine.services.ts.jta.impl.TransactionSynchronizationRegistryImpl;
import com.sap.engine.services.ts.jta.impl2.SAPXidImpl;
import com.sap.engine.services.ts.jta.impl2.TXR_TransactionManagerImpl;
import com.sap.engine.services.ts.jta.impl2.TXR_UserTransaction;
import com.sap.engine.services.ts.recovery.RMContainerRegistryImpl;
import com.sap.engine.services.ts.recovery.RMRepositoryImpl;
import com.sap.engine.services.ts.tlog.fs.FSTLogReaderWriter;
import com.sap.engine.services.ts.tlog.util.TLogLockingImplFS;
import com.sap.engine.services.ts.utils.TLogVersion;

public class TransactionAccessFacade {
    
	private static boolean initialized = false;

	private static TXR_TransactionManagerImpl masterTxManager = null;
	private static TransactionManager transactionManager = null;
	private static UserTransaction userTransaction = null;
	private static TransactionSynchronizationRegistry transactionSynchronizationRegistry = null;
	private static RMRepositoryImpl rmRepository = null;
	private static RMContainerRegistryImpl rmContainerRegistryImpl = null;  
	
	public static synchronized void init() throws Exception {// TODO provide all needed properties for initialization
		// Forr example folder for transaction log is needed and etc. 
		
//		Executor executor = threadSystem.createCleanThreadExecutor(
//				"FSTLog flusher thread.",
//				Executor.MAX_CONCURRENCY_ALLOWED, 10,
//				Executor.IMMEDIATE_START_POLICY);
//		tLogReaderWriter = new FSTLogReaderWriter(fsTLogFolder,
//				locking, null, executor, tLogBufferCapacity, maxTLogFileSize);
//		
//		tLogVersion = new TLogVersion(serviceContext.getClusterContext().getClusterMonitor().getClusterName().getBytes(),
//        		serviceContext.getClusterContext().getClusterMonitor().getCurrentParticipant().getClusterId(),
//        		System.currentTimeMillis());
//		Executor executor = threadSystem.createCleanThreadExecutor(
//				"FSTLog flusher thread.",
//				Executor.MAX_CONCURRENCY_ALLOWED, 10,
//				Executor.IMMEDIATE_START_POLICY);
//		tLogReaderWriter = new FSTLogReaderWriter(fsTLogFolder,
//				locking, null, executor, tLogBufferCapacity, maxTLogFileSize);
//		

		if (!initialized) {
			TLogVersion tLogVersion = new TLogVersion("LOG".getBytes("UTF-8"), 0, System.currentTimeMillis());
			TransactionServiceFrame.setTLogVersion(tLogVersion);
			TransactionServiceFrame.setTLogReaderWriter(new FSTLogReaderWriter( new File("."), TLogLockingImplFS.getInstance(2000), null, 100, 8*1024*1024));
			TransactionServiceFrame.setTimeoutManager(new SimpleTimeoutManager());
			TransactionServiceFrame.setCrypter(new StandaloneCrypter());
			TransactionServiceFrame.isDBTLog = false;
			
			       
		  	SAPXidImpl.initializeStaticValues(tLogVersion);		
			
			masterTxManager = new TXR_TransactionManagerImpl();
	        transactionManager = new AppTransactionManager(masterTxManager);
	        userTransaction = new TXR_UserTransaction(masterTxManager);
	        transactionSynchronizationRegistry =  new TransactionSynchronizationRegistryImpl(masterTxManager);
	        
	        rmRepository = new RMRepositoryImpl(masterTxManager);
	        rmContainerRegistryImpl = new RMContainerRegistryImpl();
	        initialized = true;
		}

	}
	
	public static  TransactionManager getTransactionManagerInstance(){
		if (initialized) {
			return transactionManager;
		} else {
			return null;
		}
	}

	public static  TransactionManagerExtension getTransactionManagerExtentionInstance(){
		if (initialized) {
			return masterTxManager;
		} else {
			return null;
		}
	}
	
	public static UserTransaction getUserTransactionInstance(){
		if (initialized) {
			return userTransaction;
		} else {
			return null;
		}
	}
	
	public static TransactionSynchronizationRegistry getTransactionSynchronizationRegistry(){
		if (initialized) {
			return transactionSynchronizationRegistry;
		} else {
			return null;
		}
	}
	
	public static RMRepositoryImpl getRMRepositoryImpl(){
		if (initialized) {
			return rmRepository;
		} else {
			return null;
		}
	}
	
	public static RMContainerRegistryImpl getRMContainerRegistryImpl(){
		if (initialized) {
			return rmContainerRegistryImpl;
		} else {
			return null;
		}
	}

	public static void setResourceContextFactory(ResourceContextFactory resourceContextFactory) {
		TransactionServiceFrame.setResourceContextFactory(resourceContextFactory);
	}

	public static void setResourceSetFactory(ResourceSetFactory resourceSetFactory) {
		TransactionServiceFrame.setResourceSetFactory(resourceSetFactory);
	}
}
