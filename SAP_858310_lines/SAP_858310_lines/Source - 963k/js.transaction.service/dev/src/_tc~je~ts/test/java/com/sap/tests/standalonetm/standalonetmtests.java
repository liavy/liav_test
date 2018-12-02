package com.sap.tests.standaloneTM;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionFactory;
import javax.resource.spi.ManagedConnectionFactory;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.xa.XAResource;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.fail;

import com.sap.engine.interfaces.transaction.RMContainerRegistry;
import com.sap.engine.interfaces.transaction.RMRepository;
import com.sap.engine.services.connector.ConnectorAccessFacade;
import com.sap.engine.services.connector.ResourceObjectFactory;
import com.sap.engine.services.connector.deploy.descriptor.ConnectorDescriptor;
import com.sap.engine.services.connector.deploy.util.ConnectionFactoryInfo;
import com.sap.engine.services.connector.jca.deploy.MCFWrapper;
import com.sap.engine.services.ts.TransactionAccessFacade;
import com.sap.tests.utils.standaloneTMTests.oneAndTwoPhase.DummyManagedConnectionFactory;
import com.sap.tests.utils.standaloneTMTests.oneAndTwoPhase.TestObjects;

public class StandaloneTMTests {
	
	protected static TransactionManager tm;
	
	@Test
	public void testTwoPhase() {
		// initialize testObjects
		TestObjects.start.getAndSet(0);
		TestObjects.end.getAndSet(0);
		TestObjects.prepare.getAndSet(0);
		TestObjects.rollback.getAndSet(0);
		TestObjects.commit.getAndSet(0);
		
		try {
			String resourceAdapterName1 = "StandaloneDataSource1";
			String resourceAdapterName2 = "StandaloneDataSource2";
			
		    createResourceAdapter(resourceAdapterName1, new DummyManagedConnectionFactory(), ConnectorDescriptor.XA_TRANSACTION);
		    createResourceAdapter(resourceAdapterName2, new DummyManagedConnectionFactory(), ConnectorDescriptor.XA_TRANSACTION);
		    
			ResourceObjectFactory.startConnectionFactory(resourceAdapterName1); // start RA1
			ResourceObjectFactory.startConnectionFactory(resourceAdapterName2); // start RA2
		    
		    ConnectionFactory connFactory1 = (ConnectionFactory)(new ResourceObjectFactory()).getObjectInstance(createReference(resourceAdapterName1), null, null, null); // lookup CF of RA
		    ConnectionFactory connFactory2 = (ConnectionFactory)(new ResourceObjectFactory()).getObjectInstance(createReference(resourceAdapterName2), null, null, null); // lookup CF of RA
		    
			tm.begin();
			connFactory1.getConnection();
			connFactory2.getConnection();
			tm.commit();
			
			if(TestObjects.start.get() != 2 || TestObjects.end.get() != 2 ||
					TestObjects.prepare.get() != 2 || TestObjects.commit.get() != 2) {
				fail("start : " + TestObjects.start.get() + ", end : " + TestObjects.end.get() + ", prepare : " + TestObjects.prepare.get() + ", commit : " + TestObjects.commit.get());
			}
			
			System.out.println("\n\n\n End 2 phase test." + "Finished OK");
		} catch (Exception e) {
			fail(getStackTrace(e));
		}
	}
	
	@Test
	public void testOnePhase() {
		// initialize testObjects information
		TestObjects.start.getAndSet(0);
		TestObjects.end.getAndSet(0);
		TestObjects.prepare.getAndSet(0);
		TestObjects.rollback.getAndSet(0);
		TestObjects.commit.getAndSet(0);
		
		try {
			String resourceAdapterName1 = "StandaloneDataSource1";
			
		    createResourceAdapter(resourceAdapterName1, new DummyManagedConnectionFactory(), ConnectorDescriptor.XA_TRANSACTION);
		    
			ResourceObjectFactory.startConnectionFactory(resourceAdapterName1); // start RA1
		    
		    ConnectionFactory connFactory1 = (ConnectionFactory)(new ResourceObjectFactory()).getObjectInstance(createReference(resourceAdapterName1), null, null, null); // lookup CF of RA
		    
			tm.begin();
			connFactory1.getConnection();
			tm.commit();
			
			if(TestObjects.start.get() != 1 || TestObjects.end.get() != 1 || TestObjects.commit.get() != 1) {
				fail("start : " + TestObjects.start.get() + ", end : " + TestObjects.end.get() + ", commit : " + TestObjects.commit.get());
			}
			
			//test start flag parameter
			if(TestObjects.startFlag != XAResource.TMNOFLAGS) {
				fail("The startFlag is " + TestObjects.startFlag + " but expected is " + XAResource.TMNOFLAGS);
			}
			
			System.out.println("\n\n\n End one phase test." + "Finished OK");
		} catch (Exception e) {
			fail(getStackTrace(e));
		}
	}
	
	@Test
	public void testRollbackIfExInBeforeCompletion() {
		// initialize testObjects information
		TestObjects.start.getAndSet(0);
		TestObjects.end.getAndSet(0);
		TestObjects.prepare.getAndSet(0);
		TestObjects.rollback.getAndSet(0);
		TestObjects.commit.getAndSet(0);
		
		try {
		    Synchronization s1 = new Synchronization() {
					public void beforeCompletion() {
						throw new RuntimeException("For testing purposes");
					}  
					public void afterCompletion(int i) {}
			};

			String resourceAdapterName1 = "StandaloneDataSource1";
		    createResourceAdapter(resourceAdapterName1, new DummyManagedConnectionFactory(), ConnectorDescriptor.XA_TRANSACTION);
			ResourceObjectFactory.startConnectionFactory(resourceAdapterName1); // start RA1
		    ConnectionFactory connFactory1 = (ConnectionFactory)(new ResourceObjectFactory()).getObjectInstance(createReference(resourceAdapterName1), null, null, null); // lookup CF of RA
		    
		    int i = 0;
		    try {
		    	tm.begin();
		    	connFactory1.getConnection();
		    	Transaction transaction = tm.getTransaction();
		    	transaction.registerSynchronization(s1);
		    	tm.commit();
		    } catch (RollbackException e) {
		    	i++;
				if(TestObjects.start.get() != 1 || TestObjects.end.get() != 1 || TestObjects.rollback.get() != 1 || TestObjects.commit.get() != 0) {
					fail("start : " + TestObjects.start.get() + ", end : " + TestObjects.end.get() + ", commit : " + TestObjects.commit.get() + ", rollback : " + TestObjects.rollback.get());
				} else {
					System.out.println("\n\n\n End rollback after beforeCompletion throws exception test." + "Finished OK");
				}
			}
		    
		    if(i != 1) {
		    	fail("Rollback exception is not thrown if commit is invoked despite exception thrown in beforeCompletion of registered synchronization");
		    }
		} catch (Exception e) {
			fail(getStackTrace(e));
		}
	}
	
	@Ignore
	public void testTimeout() {
		try {
			int sec = 5;
			tm.setTransactionTimeout(sec);
			System.out.println("Timeout set to " + sec + " seconds.");
			tm.begin();
			System.out.println("Transaction started.");

			int status = tm.getStatus();
			if (status != Status.STATUS_ACTIVE) {
				fail("Invalid transaction status: " + status);
			}
			System.out.println("Transaction is ACTIVE.");

			long startTime = System.currentTimeMillis();
			long waitedTime;
			do {
				Thread.sleep(200);
				waitedTime = System.currentTimeMillis() - startTime;
				status = tm.getStatus();
			} while (waitedTime < 10000
					&& status == Status.STATUS_ACTIVE);

			System.out.println("Waited time: " + waitedTime + " milis.");

			if (status == Status.STATUS_ACTIVE) {
				tm.rollback();
				fail("Transaction wouldn't rollbacked in the expected timeout(1)");
			}

			final Object monitor = this;

			synchronized (this) {
				new Thread() {
					public void run() {
						try {
							System.out.println("Child thread started.");
							tm.begin();
							System.out.println("Trasnaction started.");
							int status = tm.getStatus();
							if (status != Status.STATUS_ACTIVE) {
								fail("Invalid transaction status: " + status);
							}
							System.out.println("Transaction: ACTIVE.");
							
							long startTime = System.currentTimeMillis();
							long waitedTime;
							do {
								Thread.sleep(200);
								waitedTime = System.currentTimeMillis() - startTime;
								status = tm.getStatus();
							} while (waitedTime < 10000
									&& status == Status.STATUS_ACTIVE);
							System.out.println("Waited time: " + waitedTime + " milis.");

							if (status == Status.STATUS_ACTIVE) {
								tm.rollback();
								fail("Transaction wouldn't rollbacked in the expected timeout");
							}

							tm.setTransactionTimeout(1);
							System.out.println("Set trasnaction timeout = 1 sec (It shouldn't change parrent thread timeout)");
							System.out.println("Child thread finished.");
						} catch (Exception e) {
							fail(StandaloneTMTests.getStackTrace(e));
						}
						synchronized (monitor) {
							monitor.notify();
						}
					}
				}.start();
				
				wait();
			}

			System.out.println("Back to parent thread.");

			tm.begin();
			System.out.println("Trasnaction started.");
			status = tm.getStatus();
			if (status != Status.STATUS_ACTIVE) {
				fail("Invalid transaction status: " + status);
			}

			System.out.println("Trasnaction: ACTIVE.");

			startTime = System.currentTimeMillis();
			do {
				Thread.sleep(200);
				waitedTime = System.currentTimeMillis() - startTime;
				status = tm.getStatus();
			} while (waitedTime < 10000
					&& status == Status.STATUS_ACTIVE);

			System.out.println("Waited time: " + waitedTime + " milis");
			if (status == Status.STATUS_ACTIVE) {
				tm.rollback();
				fail("Transaction wouldn't rollbacked in the expected timeout(2)");
			}
			if (waitedTime < 4000) {
				fail("Transaction rollbacked earlier(3)");
			}

			System.out.println("\n\n\nEnd of timeout test.  Finished OK.");
		}catch (Exception e) {
			fail(getStackTrace(e));
		} finally {
			try {
				tm.setTransactionTimeout(1);
			} catch (SystemException e) {
				fail(getStackTrace(e));
			}
		}
	}

	@Test
	public void testTimeout2() {
		try {
			tm.begin();
			System.out.println("Trasnaction started.");

			int status = tm.getStatus();
			if (status != Status.STATUS_ACTIVE) {
				fail("Invalid transaction status: " + status);
			}

			System.out.println("Trasnaction: ACTIVE.");

			long startTime = System.currentTimeMillis();
			long waitedTime;
			do {
				Thread.sleep(200);
				waitedTime = System.currentTimeMillis() - startTime;
				status = tm.getStatus();
			} while (waitedTime < 5000
					&& status != Status.STATUS_NO_TRANSACTION);

			System.out.println("Waited time: " + waitedTime + " milis.");

			if (status == Status.STATUS_NO_TRANSACTION) {
				fail("Transaction rollbacked unexpectedly");
			}
			tm.rollback();
		} catch (Exception e) {
			fail(getStackTrace(e));
		} finally {
			try {
				tm.setTransactionTimeout(0);
			} catch (SystemException e) {
				fail(getStackTrace(e));
			} // for to save other tests in 6.40
			System.out.println("Set trasnaction timeout = 0 sec<BR><BR>");
		}

		System.out.println("\n\n\nEnd of timeout test2.  Finished OK.");
	}
	
	@Test
	public void testSynchronizationRegistry() {
		final TransactionSynchronizationRegistry transactionSynchronizationRegistry = TransactionAccessFacade.getTransactionSynchronizationRegistry();
	    try {
	      tm.begin();
			System.out.println("Trasnaction started.<BR><BR>");

			int status = tm.getStatus();
			if (status != Status.STATUS_ACTIVE) {
				fail("Invalid transaction status: " + status);
			}

			int statusFromtxRegistry = transactionSynchronizationRegistry.getTransactionStatus();
			if (statusFromtxRegistry != Status.STATUS_ACTIVE) {
				fail("Invalid status from TransactionSynchronizationRegistry. Status is not active: "
						+ status);
			}

			Object transactionKey = transactionSynchronizationRegistry.getTransactionKey();
			if (transactionKey == null) {
				fail("TransactionKey is null when transaction is running");
			}

			Object objectForTxRegistry = new Object();
			transactionSynchronizationRegistry.putResource(transactionKey, objectForTxRegistry);
			Object objectFromTxRegistry = transactionSynchronizationRegistry.getResource(transactionKey);
			if (objectFromTxRegistry == null || objectFromTxRegistry != objectForTxRegistry) {
				fail("Transaction object registry is not working properly");
			}

			if (transactionSynchronizationRegistry.getRollbackOnly()) {
				fail("TransactionSynchronizationRegistry.getRollbackOnly() is not working properly");
			}

			Synchronization synchObject = new Synchronization() {
				public void beforeCompletion() {
				}

				public void afterCompletion(int i) {
				}
			};
			transactionSynchronizationRegistry.registerInterposedSynchronization(synchObject);
			tm.commit();

			statusFromtxRegistry = transactionSynchronizationRegistry.getTransactionStatus();
			if (statusFromtxRegistry != Status.STATUS_NO_TRANSACTION) {
				fail("Invalid status from TxSynRegistry. Status is not STATUS_NO_TRANSACTION: " + status);
			}

			transactionKey = transactionSynchronizationRegistry.getTransactionKey();
			if (transactionKey != null) {
				fail("TransactionKey must be null when transaction is not running");
			}

			tm.begin();
			System.out.println("Second trasnaction started.");

			status = tm.getStatus();
			if (status != Status.STATUS_ACTIVE) {
				fail("Invalid transaction status: " + status);
			}

			statusFromtxRegistry = transactionSynchronizationRegistry.getTransactionStatus();
			if (statusFromtxRegistry != Status.STATUS_ACTIVE) {
				fail("Invalid status from TransactionSynchronizationRegistry. Status is not active: " + status);
			}

			transactionSynchronizationRegistry.setRollbackOnly();
			if (!transactionSynchronizationRegistry.getRollbackOnly()) {
				fail("TransactionSynchronizationRegistry.getRollbackOnly() is not working properly");
			}

			tm.rollback();
		} catch (Exception e) {
			fail(getStackTrace(e));
		}
	}
	
	@Test
	public void testSynchronization() {
		try {
			final AtomicInteger atom = new AtomicInteger(0);
			
		    Synchronization s1 = new Synchronization() {
					public void beforeCompletion() {}  
					public void afterCompletion(int i) {
						if(atom.incrementAndGet() == 1) {
							throw new RuntimeException("For testing purposes");
						}
					}
			};
			Synchronization s2 = new Synchronization() {
  					public void beforeCompletion() {}  
					public void afterCompletion(int i) {
						atom.incrementAndGet();
					}
			};
			
			tm.begin();
			Transaction transaction = tm.getTransaction();
			transaction.registerSynchronization(s1);
			transaction.registerSynchronization(s2);
			tm.commit();

			int res = atom.getAndSet(0);
			if(res != 2) {
				fail("The second afterCompletion() is not called. AfterCompletion called " + res + " times.");
			}
		} catch (Exception e) {
			fail(getStackTrace(e));
		}
	}
	
	@BeforeClass
	public static void initTMandConnector() throws IOException, Exception {
		System.out.println("@BeforeClass - init TM and Connector");
		System.out.println("TM standalone test, temp dir: " + (new File(".")).getCanonicalPath());
		// init transaction facade
		TransactionAccessFacade.init();
		// get objects from transaction facade
		tm = TransactionAccessFacade.getTransactionManagerInstance();		
		RMContainerRegistry containerRegistry = TransactionAccessFacade.getRMContainerRegistryImpl();
		RMRepository rmRepository = TransactionAccessFacade.getRMRepositoryImpl();

		// init connector facade
		ConnectorAccessFacade.init();
		// set objects to connector facade from transaction facade
		ConnectorAccessFacade.setTransactionManager(TransactionAccessFacade.getTransactionManagerExtentionInstance());
		ConnectorAccessFacade.setRmContainerRegistry(containerRegistry);
		ConnectorAccessFacade.setRMRepository(rmRepository);

		// set objects to transaction facade from connector facade
		TransactionAccessFacade.setResourceContextFactory(ConnectorAccessFacade.getResourceContextFactory());
		TransactionAccessFacade.setResourceSetFactory(ConnectorAccessFacade.getResourceSetFactory());
	}
	
	private static Reference createReference(String dsName) {
	    Reference ref = new Reference("com.sap.engine.services.dbpool.cci.ConnectionFactory", "com.sap.engine.services.connector.ResourceObjectFactory", "service:connector");
	    ref.add(new StringRefAddr("res-type", "javax.sql.DataSource"));
	    ref.add(new StringRefAddr("res-auth", "Container"));
	    ref.add(new StringRefAddr("sharing-scope", "Shareable"));
	    ref.add(new StringRefAddr("res-name", dsName));
	    return ref;
	}
	
	private static void createResourceAdapter(String resourceAdapterName,
			ManagedConnectionFactory mcf, int txType) throws ResourceException {
		
		ConnectionFactoryInfo connectionFactoryInfo = new ConnectionFactoryInfo();
		connectionFactoryInfo.setApplicationName(null);
		connectionFactoryInfo.setConnectionFactoryName(resourceAdapterName); // name of RA
		connectionFactoryInfo.setConnectionFactoryInterface("javax.sql.DataSource");
		connectionFactoryInfo.setSupportContainerAuth(false);
		connectionFactoryInfo.setAuthenticationMechanismType(null);
		connectionFactoryInfo.setStandAlone(true);
		connectionFactoryInfo.setGlobalResource(true);

		MCFWrapper wrapper = new MCFWrapper(mcf, txType, new String[] {}, 0, 10, 120, 900, 300, false); // false - no pooling
		connectionFactoryInfo.setMCFWrapper(wrapper);
		ResourceObjectFactory.addConnectionFactory(connectionFactoryInfo); // enlist maybe
	}

    protected static final String getStackTrace(Exception e) {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    (e).printStackTrace(new PrintStream(baos));
	    return baos.toString();
	}
}
