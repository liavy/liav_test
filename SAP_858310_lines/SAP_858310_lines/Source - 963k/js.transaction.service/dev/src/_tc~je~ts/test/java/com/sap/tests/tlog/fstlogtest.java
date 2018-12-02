package com.sap.tests.tlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.engine.interfaces.transaction.RMProps;
import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.facades.crypter.CrypterException;
import com.sap.engine.services.ts.tlog.InvalidRMIDException;
import com.sap.engine.services.ts.tlog.InvalidRMKeyException;
import com.sap.engine.services.ts.tlog.RMNameAlreadyInUseException;
import com.sap.engine.services.ts.tlog.TLog;
import com.sap.engine.services.ts.tlog.TLogAlreadyExistException;
import com.sap.engine.services.ts.tlog.TLogFullException;
import com.sap.engine.services.ts.tlog.TLogIOException;
import com.sap.engine.services.ts.tlog.TransactionRecord;
import com.sap.engine.services.ts.tlog.TransactionRecordImpl;
import com.sap.engine.services.ts.tlog.fs.FSTLog;
import com.sap.engine.services.ts.tlog.fs.FSTLogOptimizator;
import com.sap.engine.services.ts.tlog.fs.FSTLogReaderWriter;
import com.sap.engine.services.ts.facades.timer.SimpleTimeoutManager;
import com.sap.engine.services.ts.tlog.util.TLogLocking;
import com.sap.engine.services.ts.utils.TLogVersion;
import com.sap.security.core.server.secstorefs.FileIOException;
import com.sap.security.core.server.secstorefs.FileMissingException;
import com.sap.security.core.server.secstorefs.InvalidStateException;
import com.sap.security.core.server.secstorefs.SecStoreFS;
import com.sap.security.core.server.secstorefs.SecStoreFSException;
import com.sap.tests.utils.AbstractTestScenarios;
import com.sap.tests.utils.TLogLockingImplFS;


public class FSTLogTest extends AbstractTestScenarios {

	private static int NUMBER_WRITES = 100;
	private static long TIME_TO_WAIT_BETWEEN_WRITE_AND_DELETE = 5;

	private static final int NUMBER_WRITING_THREADS = 50;

	private final static int MAX_TLOG_FILE_SIZE = 2 * 1024 * 1024;
	private final static int TLOG_BUFFER_CAPACITY = NUMBER_WRITING_THREADS + 5;

	// for the FSTLogOptimizator test
	private Object lock = new Object();
	private int numThreadReady = 0;
	private long txSeqNum = 1;
	private int id1 = -1, id2 = -1;
	int classifier=0;
	// end

	@BeforeClass
	public static void initFS() {
		AbstractTestScenarios.beforeClass();
		deleteTlogFilesLeft();
	}
	
	/**
	 * Create TLog
	 */
	@Before
	@Override
	public void init() {
		String dir = System.getProperty("java.io.tmpdir");
		
		writer = new FSTLogReaderWriter(
				new File(dir), 
				com.sap.engine.services.ts.tlog.util.TLogLockingImplFS.getInstance(2000),
				null,
				TLOG_BUFFER_CAPACITY,
				MAX_TLOG_FILE_SIZE
		);
		TLogVersion ver = new TLogVersion("APM".getBytes(), 1, System.currentTimeMillis());
		try {
			tlog = writer.createNewTLog(ver.getTLogVersion());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (TLogAlreadyExistException e) {
			fail(getStackTrace(e));
		}
		assertTrue(tlog.getTLogVersion().equals(ver));
		
		((FSTLog)tlog).setMaxRMEntries(10);
	}

	@Test
	public void testReencryption() {
		// in init we have created tlog and we should close it
		try{
			tlog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		RMProps props1 = new RMProps();
		props1.setKeyName("ResourceManager1");
		props1.setRmContainerName("Container1");
		Properties secure = new Properties();
		secure.setProperty("one", "string1");
		secure.setProperty("two", "string2");
		Properties unsecure = new Properties();
		unsecure.setProperty("qwe", "unsecure1");
		unsecure.setProperty("rty", "unsecure2");
		props1.setNonSecureProperties(unsecure);
		props1.setSecureProperties(secure);
		
		// create some tlog files(write some rm to them) and give it to Crypter.reencryptFSTLog
		String directory = System.getProperty("java.io.tmpdir") + File.separator + "TestDirectory";
		File dir = new File(directory);
		dir.mkdir();
		// locking... should be only one instance
		TLogLocking testLocking = TLogLockingImplFS.getInstance(2000);
		com.sap.engine.services.ts.tlog.util.TLogLockingImplFS realLocking = com.sap.engine.services.ts.tlog.util.TLogLockingImplFS.getInstance(2000);
		writer = new FSTLogReaderWriter(dir, realLocking,
				null,
				TLOG_BUFFER_CAPACITY,
				MAX_TLOG_FILE_SIZE);
		TLogVersion ver1 = new TLogVersion("ACD".getBytes(), 1, System.currentTimeMillis());
		TLogVersion ver2 = new TLogVersion("DFG".getBytes(), 1, System.currentTimeMillis());
		TLog tlog2 = null;
		SecStoreFS ss = null;
		try {
			tlog = writer.createNewTLog(ver1.getTLogVersion());
			tlog2 = writer.createNewTLog(ver2.getTLogVersion());
			
			int rmid1 = tlog.registerNewRM(props1);
			int rmid2 = tlog2.registerNewRM(props1);
			
			// write transaction record to rm1!!!
			// set properties of transaction record
			int classifier = tlog.getIdForTxClassifier("classifier");
			int classifier1 = tlog2.getIdForTxClassifier("classifier");

			TransactionRecordImpl rec = new TransactionRecordImpl();
			TransactionRecordImpl rec1 = new TransactionRecordImpl();

			super.initTransactionRecord(rec, classifier, 6);
			super.initTransactionRecord(rec1, classifier1, 6);

			int idd = tlog.getRMIDByName("ResourceManager1");
			int iddd = tlog2.getRMIDByName("ResourceManager1");
			int[] ids = new int[] {idd};
			int[] ids1 = new int[] {iddd};
			rec.setRMIDs(ids);
			rec1.setRMIDs(ids1);
			rec.setBranchIterators(new byte[] {0});
			rec1.setBranchIterators(new byte[] {0});
			TransactionRecord recc = rec;
			TransactionRecord recc1 = rec1;
		
			tlog.writeTransactionRecord(recc);
			tlog2.writeTransactionRecord(recc1);
			
			RMProps p = tlog.getRMProperties(rmid1);
			RMProps pp = ((com.sap.engine.services.ts.tlog.fs.FSTLog) tlog).getRMProperties(rmid1, false);
			assertFalse(p.equals(pp));
			
			tlog.close();
			tlog2.close();
			
			ss = new SecStoreFS();
			ss.openExistingStore();
			ss.migrateEncryptionKey("A new key phrase!!!", true);
			
			TransactionServiceFrame.getCrypter().reencryptFSTLog(directory, testLocking, TLOG_BUFFER_CAPACITY, MAX_TLOG_FILE_SIZE);
			
			tlog = writer.lockOrphanedTLog();
			tlog2 = writer.lockOrphanedTLog();
			
			RMProps props = ((FSTLog)tlog).getRMProperties(rmid1,true);
			RMProps props2 = ((FSTLog)tlog2).getRMProperties(rmid2,true);
			assertEquals(props1, props);
			assertEquals(props1, props2);
			
			Iterator<TransactionRecord> it = tlog.getAllTransactionRecords();
			while(it.hasNext()) {
				tlog.removeTransactionRecordImmediately(it.next().getTransactionSequenceNumber());
			}
			it = tlog2.getAllTransactionRecords();
			while(it.hasNext()) {
				tlog2.removeTransactionRecordImmediately(it.next().getTransactionSequenceNumber());
			}
			try {
				tlog.close();
				tlog2.close();
				assertNull(writer.lockOrphanedTLog());
			} catch(TLogIOException e) {
				fail(getStackTrace(e));
			}
		} catch (Exception e) {
			fail(getStackTrace(e));
		} finally {
			try {
				if(ss != null) {
					ss.deleteBackupFiles();
				}
			} catch (FileMissingException e) {
				fail(getStackTrace(e));
			} catch (InvalidStateException e) {
				fail(getStackTrace(e));
			} catch (FileIOException e) {
				fail(getStackTrace(e));
			} catch (SecStoreFSException e) {
				fail(getStackTrace(e));
			}
		}
		
	}

	protected Properties getPlainSecureProps() {
		// should understand how to get secure properties from file (how they are encrypted to file)
		
		int[] rmids = null;
		try {
			rmids = tlog.getAllUsedRMIDs();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		
		RMProps prop = null;
		
		try {
			prop = tlog.getRMProperties(rmids[0]);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMKeyException e) {
			fail(getStackTrace(e));
		}
		
		Properties secure = prop.getSecureProperties();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		// secure props
		try {
			secure.store(out, null);
		} catch (IOException e) {
			fail(getStackTrace(e));
		}
		byte[] arr = out.toByteArray();
		try {
			arr = TransactionServiceFrame.getCrypter().encryptBytes(arr);
		} catch (CrypterException e) {
			fail(getStackTrace(e));
		}
		
		Properties secProps = new Properties();
		try {
			secProps.load(new ByteArrayInputStream(arr));
		} catch (IOException e) {
			fail(getStackTrace(e));
		}
		
		return secProps;
	}

	@AfterClass
	public static void afterClass() {
		new File(SEC_STORE_FN).deleteOnExit();
		new File(SEC_STORE_KEY_FN).deleteOnExit();
		new File(SEC_STORE_BAK_FN).deleteOnExit();
		new File(REENCRYPT_KEY_FN).deleteOnExit();
		new File(System.getProperty("java.io.tmpdir") + File.separator + "TestDirectory").deleteOnExit();
		deleteTlogFilesLeft();
	}
	
	private static void deleteTlogFilesLeft() {
		String dir = System.getProperty("java.io.tmpdir");
		File[] files = new File(dir).listFiles(new FilenameFilter	() {
				public boolean accept(File dir, String filename) {
					return filename.endsWith(FSTLog.LOG_FILE_EXTENSION);
				}
			});
		for (File f : files) {
			f.delete();
		}
	}

	@Test
	public void testFSTLogOptimizator() {
		//set properties
		((FSTLog)tlog).setMaxFileSize(2 * MAX_TLOG_FILE_SIZE);
		FSTLogOptimizator.setWaitBetweenFlushes(25);
		
		assertTrue( ((FSTLog)tlog).getMaxFileSize() == 2 * MAX_TLOG_FILE_SIZE);

		//create rm1
		RMProps props1 = new RMProps();
		props1.setKeyName("ResourceManager1");
		props1.setRmContainerName("Container1");
		Properties secure = new Properties();
		secure.setProperty("one", "string1");
		secure.setProperty("two", "string2");
		Properties unsecure = new Properties();
		unsecure.setProperty("qwe", "unsecure1");
		unsecure.setProperty("rty", "unsecure2");
		props1.setNonSecureProperties(unsecure);
		props1.setSecureProperties(secure);
		// create rm2
		RMProps props2 = new RMProps();
		props2.setKeyName("ResourceManager2");
		props2.setRmContainerName("Container2");
		Properties secure2 = new Properties();
		secure2.setProperty("ones", "string111");
		secure2.setProperty("twos", "string22");
		Properties unsecure2 = new Properties();
		unsecure2.setProperty("qwer", "unsecure11");
		unsecure2.setProperty("rtyu", "unsecure22");
		props2.setNonSecureProperties(unsecure2);
		props2.setSecureProperties(secure2);
		
		//register rm1
		try {
			id1 = tlog.registerNewRM(props1);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (RMNameAlreadyInUseException e) {
			fail(getStackTrace(e));
		}
		//register rm2
		try {
			id2 = tlog.registerNewRM(props2);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (RMNameAlreadyInUseException e) {
			fail(getStackTrace(e));
		}
		
		try {
			classifier = tlog.getIdForTxClassifier("Classifier 1");
		} catch (TLogIOException e1) {
			fail(getStackTrace(e1));
		} catch (TLogFullException e1) {
			fail(getStackTrace(e1));
		}

		// write transactions
		ExecutorService executor = Executors
				.newFixedThreadPool(NUMBER_WRITING_THREADS + 5);

		Collection<Callable<Writer>> writers = new ArrayList<Callable<Writer>>(
				NUMBER_WRITING_THREADS);
		for (int i = 0; i < NUMBER_WRITING_THREADS; ++i) {
			writers.add(Executors.callable(new Writer(), new Writer()));
		}

		try {
			synchronized (lock) {
				numThreadReady = 0;
			}

			executor.invokeAll(writers);
			synchronized (lock) {
				while (numThreadReady < NUMBER_WRITING_THREADS) {
					lock.wait();
				}
			}
		} catch (InterruptedException e) {
			fail(getStackTrace(e));
		}
		
		// read 1..5000, e.g. check
		try {
			Iterator<TransactionRecord> it = tlog.getAllTransactionRecords();
			int tmp = NUMBER_WRITES * NUMBER_WRITING_THREADS;
			try {
				while(tmp-- > 0) {
					it.next();
				}
			} catch (Exception e) {
				failMissingRecords(tmp, e);
			}

			assertFalse(it.hasNext());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}

		// close
		try {
			tlog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}

		// lock orphe...
		try {
			tlog = writer.lockOrphanedTLog();
			assertNull(writer.lockOrphanedTLog());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		// read 1..5000, e.g. check
		try {
			Iterator<TransactionRecord> it = tlog.getAllTransactionRecords();
			int tmp = NUMBER_WRITES * NUMBER_WRITING_THREADS;
			try {
				while(tmp-- > 0) {
					it.next();
				}
			} catch (Exception e) {
				failMissingRecords(tmp, e);
			}

			assertFalse(it.hasNext());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		// remove 1..5000
		for (int i=1; i <=NUMBER_WRITES * NUMBER_WRITING_THREADS; ++i) {
			if(i%1017  == 0) {
				try {
					tlog.removeTransactionRecordImmediately(i);
				} catch (TLogIOException e) {
					fail(getStackTrace(e));
				}
			} else {
				try {
					tlog.removeTransactionRecordLazily(i);
				} catch (TLogIOException e) {
					fail(getStackTrace(e));
				}
			}
		}
		
		// close()
		try {
			tlog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		// lockOrphaned == null
		try {
			assertNull(writer.lockOrphanedTLog());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
	}
	
	@Test
	public void testRotate() {
		FSTLog log = (FSTLog)tlog;
		log.setMaxRMEntries(2);
		
		//create rm1
		RMProps props1 = new RMProps();
		props1.setKeyName("ResourceManager1");
		props1.setRmContainerName("Container1");
		Properties secure = new Properties();
		secure.setProperty("one", "string1");
		secure.setProperty("two", "string2");
		Properties unsecure = new Properties();
		unsecure.setProperty("qwe", "unsecure1");
		unsecure.setProperty("rty", "unsecure2");
		props1.setNonSecureProperties(unsecure);
		props1.setSecureProperties(secure);
		// create rm2
		RMProps props2 = new RMProps();
		props2.setKeyName("ResourceManager2");
		props2.setRmContainerName("Container2");
		Properties secure2 = new Properties();
		secure2.setProperty("ones", "string111");
		secure2.setProperty("twos", "string22");
		Properties unsecure2 = new Properties();
		unsecure2.setProperty("qwer", "unsecure11");
		unsecure2.setProperty("rtyu", "unsecure22");
		props2.setNonSecureProperties(unsecure2);
		props2.setSecureProperties(secure2);
		
		//register rm1
		try {
			id1 = tlog.registerNewRM(props1);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (RMNameAlreadyInUseException e) {
			fail(getStackTrace(e));
		}
		//register rm2
		try {
			id2 = tlog.registerNewRM(props2);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (RMNameAlreadyInUseException e) {
			fail(getStackTrace(e));
		}
		
		try {
			tlog.unregisterRM(id1);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMIDException e) {
			fail(getStackTrace(e));
		}
		try {
			tlog.unregisterRM(id2);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMIDException e) {
			fail(getStackTrace(e));
		}
		
		try {
			tlog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
	}
	
	private void failMissingRecords(int tmp, Exception e)
			throws TLogIOException {
		Iterator<TransactionRecord> iter = tlog.getAllTransactionRecords();
		long[] ids = new long[NUMBER_WRITES * NUMBER_WRITING_THREADS];
		Arrays.fill(ids, 0);
		int i = 0;
		while (iter.hasNext()) {
			ids[i++] = iter.next().getTransactionSequenceNumber();
		}
		Arrays.sort(ids, 0, i);

		StringBuilder missingIDs = new StringBuilder();
		missingIDs.append("Minimal ID = ").append(ids[0]).append("\n");
		missingIDs.append("Maximal ID = ").append(ids[i - 1]).append("\n");

		missingIDs.append("Missing values in the range [").append(ids[0])
				.append(", ").append(ids[i - 1]).append("] ID:\n");

		for (long expextedValue = ids[0], j = 0; j < i; ++j, ++expextedValue) {
			while (expextedValue != ids[(int) j]) {
				missingIDs.append(expextedValue++).append(' ');
			}
		}
		missingIDs.append('\n');

		int iteration = NUMBER_WRITES * NUMBER_WRITING_THREADS - tmp;

		fail("Fail at " + iteration + " iteration with:\n" + getStackTrace(e)
				+ "\n\n" + missingIDs.toString());
	}


	class Writer implements Runnable {
		public void run() {
			try {
				for (int i = 0; i < NUMBER_WRITES; ++i) {
					try {
						TransactionRecordImpl rec = new TransactionRecordImpl();
						rec.setBranchIterators(new byte[] { 0, 0});
						rec.setRMIDs(new int[] { id1, id2 });
						rec.setTransactionAbandonTimeout(10000);
						rec.setTransactionBirthTime(40000);
						rec.setTransactionClassifierID(classifier);
						synchronized (lock) {
							rec.setTransactionSequenceNumber(txSeqNum++);
						}
						tlog.writeTransactionRecord(rec);
	
						synchronized (this) {
							wait(TIME_TO_WAIT_BETWEEN_WRITE_AND_DELETE);
						}
					} catch (Exception e) {
						fail(getStackTrace(e));
					}
				}
			} finally {
				synchronized (lock) {
					numThreadReady++;
					lock.notifyAll();
				}
			}
		}
	}

}
