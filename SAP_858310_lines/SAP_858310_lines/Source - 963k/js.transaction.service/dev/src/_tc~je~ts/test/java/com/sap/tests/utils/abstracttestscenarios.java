package com.sap.tests.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.transaction.xa.Xid;

import org.junit.Before;
import org.junit.Test;

import com.sap.engine.interfaces.transaction.RMProps;
import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.facades.crypter.ServerCrypter;
import com.sap.engine.services.ts.facades.timer.SimpleTimeoutManager;
import com.sap.engine.services.ts.tlog.InboundTLog;
import com.sap.engine.services.ts.tlog.InboundTransactionRecord;
import com.sap.engine.services.ts.tlog.InboundTransactionRecordImpl;
import com.sap.engine.services.ts.tlog.InvalidClassifierIDException;
import com.sap.engine.services.ts.tlog.InvalidRMIDException;
import com.sap.engine.services.ts.tlog.InvalidRMKeyException;
import com.sap.engine.services.ts.tlog.InvalidTransactionClassifierID;
import com.sap.engine.services.ts.tlog.RMNameAlreadyInUseException;
import com.sap.engine.services.ts.tlog.TLog;
import com.sap.engine.services.ts.tlog.TLogAlreadyExistException;
import com.sap.engine.services.ts.tlog.TLogFullException;
import com.sap.engine.services.ts.tlog.TLogIOException;
import com.sap.engine.services.ts.tlog.TLogReaderWriter;
import com.sap.engine.services.ts.tlog.TransactionRecord;
import com.sap.engine.services.ts.tlog.TransactionRecordImpl;
import com.sap.engine.services.ts.utils.TLogVersion;
import com.sap.engine.services.ts.utils.XidImpl;
import com.sap.security.core.server.secstorefs.FileExistsException;
import com.sap.security.core.server.secstorefs.FileIOException;
import com.sap.security.core.server.secstorefs.FileInvalidException;
import com.sap.security.core.server.secstorefs.FileMissingException;
import com.sap.security.core.server.secstorefs.InvalidStateException;
import com.sap.security.core.server.secstorefs.NoEncryptionException;
import com.sap.security.core.server.secstorefs.NoHashException;
import com.sap.security.core.server.secstorefs.NoKeyRequiredException;
import com.sap.security.core.server.secstorefs.NoSIDException;
import com.sap.security.core.server.secstorefs.SecStoreFS;

public abstract class AbstractTestScenarios {
	
	protected final static String SEC_STORE_FN = "SecStore.properties"; 
	protected final static String SEC_STORE_KEY_FN = "SecStore.key"; 

	protected final static String SEC_STORE_BAK_FN = "SecStore.bak"; 
	protected final static String REENCRYPT_KEY_FN = "Reencrypt.key"; 

	protected TLogReaderWriter writer = null;
	protected TLog tlog = null;
	
	public AbstractTestScenarios() {		
		super();
		TransactionServiceFrame.txTimeout = 200;
	}

	static {
		try {
			new File(SEC_STORE_FN).delete();
			new File(SEC_STORE_KEY_FN).delete();
			new File(SEC_STORE_BAK_FN).delete();
			new File(REENCRYPT_KEY_FN).delete();
			SecStoreFS.setDefaultFilenames(SEC_STORE_FN, SEC_STORE_KEY_FN);
			SecStoreFS.setSID("LKG");
			SecStoreFS ss = new SecStoreFS();
			ss.createStoreWithEncryption("A sample key phrase", true);
			TransactionServiceFrame.setCrypter(new ServerCrypter());
		} catch (InvalidStateException e) {
			fail(getStackTrace(e));
		} catch (FileIOException e) {
			fail(getStackTrace(e));
		} catch (FileMissingException e) {
			fail(getStackTrace(e));
		} catch (FileExistsException e) {
			fail(getStackTrace(e));
		} catch (FileInvalidException e) {
			fail(getStackTrace(e));
		} catch (NoEncryptionException e) {
			fail(getStackTrace(e));
		} catch (NoHashException e) {
			fail(getStackTrace(e));
		} catch (NoSIDException e) {
			fail(getStackTrace(e));
		} catch (NoKeyRequiredException e) {
			fail(getStackTrace(e));
		}
	}

	protected static void beforeClass() {
		TransactionServiceFrame.setTimeoutManager(new SimpleTimeoutManager());
	}

	@Before
	public abstract void init();

	@Test
	public void scenario1() {
		try {
			assertTrue(tlog.getAllUsedRMIDs().length == 0);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}	
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
			tlog.registerNewRM(props1);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (RMNameAlreadyInUseException e) {
			fail(getStackTrace(e));
		}
		//register rm2
		try {
			tlog.registerNewRM(props2);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (RMNameAlreadyInUseException e) {
			fail(getStackTrace(e));
		}
		
		int[] usedRMids = null;
		try {
			usedRMids = tlog.getAllUsedRMIDs();
			assertEquals("Check for how many rm are into tlog", 2, usedRMids.length);
			assertTrue("Check if id1 is for rm1",
					((tlog.getRMProperties(usedRMids[0])).equals(props1) && (tlog.getRMProperties(usedRMids[1])).equals(props2))
					||
					((tlog.getRMProperties(usedRMids[1])).equals(props1) && (tlog.getRMProperties(usedRMids[0])).equals(props2))
			);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMKeyException e) {
			fail(getStackTrace(e));
		}
		
		//remove rm1
		boolean temp = false;
		try {
			temp = tlog.getRMProperties(usedRMids[0]).equals(props1);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMKeyException e) {
			fail(getStackTrace(e));
		}
		int tmp = (temp) ? usedRMids[0] : usedRMids[1];
		try {
			tlog.unregisterRM(tmp);
			try {
				Thread.sleep(TransactionServiceFrame.txTimeout*3);
			} catch (InterruptedException e) {}
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMIDException e) {
			fail(getStackTrace(e));
		}
		
		//assert if the second rm is remaining , but register another rm to force database
		int[] usedRMids2 = null;
		try {
			try { // wait to unregister
				Thread.sleep(TransactionServiceFrame.txTimeout*4);
			} catch (InterruptedException e) {}
			usedRMids2 = tlog.getAllUsedRMIDs();
			assertEquals("Check for how many rm are into tlog", 1, usedRMids2.length);
			assertTrue("Check if id is for rm2", (tlog.getRMProperties(usedRMids2[0])).equals(props2));
			assertTrue("Check(by name) if rm2 is remaining", tlog.getRMIDByName("ResourceManager2") == usedRMids2[0]);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMKeyException e) {
			fail(getStackTrace(e));
		}
		
		int magicKey = 19;
		while(magicKey-- > 0) {
			
			int txClassifierId = 0;
			try {
				txClassifierId = tlog.getIdForTxClassifier("Pe6o"+magicKey);
			} catch (TLogIOException e1) {
				fail(getStackTrace(e1));
			} catch (TLogFullException e1) {
				fail(getStackTrace(e1));
			}

			long nowa = System.currentTimeMillis();
			TransactionRecordImpl rec1 = new TransactionRecordImpl();
			rec1.setTransactionAbandonTimeout(nowa + 1000000);
			rec1.setTransactionBirthTime(nowa);
			rec1.setTransactionClassifierID(txClassifierId);
			rec1.setTransactionSequenceNumber((long)magicKey);

			int id = -1;
			try {
				id = tlog.getRMIDByName("ResourceManager2");
			} catch (InvalidRMKeyException e) {
				fail(getStackTrace(e));
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} // transaction record to 1st resource manager
			int[] ids = new int[1];
			ids[0] = id;
			rec1.setRMIDs(ids);
			rec1.setBranchIterators(new byte[] {0});
			TransactionRecord rec = rec1;
			
			
			assertEquals("Abandon timeout is wrong", nowa + 1000000, rec.getTransactionAbandonTimeout());
			assertEquals("Birth time is wrong", nowa, rec.getTransactionBirthTime());
			assertEquals("ClassifierID is wrong", txClassifierId, rec.getTransactionClassifierID());
			assertEquals("ASequence Number is wrong", (long)magicKey, rec.getTransactionSequenceNumber());
			
			try {
				tlog.writeTransactionRecord(rec);
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} catch (InvalidRMIDException e) {
				fail(getStackTrace(e));
			} catch (InvalidTransactionClassifierID e) {
				fail(getStackTrace(e));
			}
			try {
				tlog.removeTransactionRecordLazily(rec.getTransactionSequenceNumber());
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
		}
		
		Iterator<TransactionRecord> it = null;
		try{
			it = tlog.getAllTransactionRecords();
		} catch(TLogIOException e) {
			fail(getStackTrace(e));
		}
		int count = 0;
		while(it.hasNext()) {
			count++;
			it.next();
		}
		assertTrue("Check if remaining transactions are less than 500", count < 10);
		try{
			tlog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		
		try{
			assertNull("orphaned tlog should be null", writer.lockOrphanedTLog());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
	}

	@Test
	public void scenario2() {
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
		
		int rmId1 = 0;
		//register rm1
		try {
			rmId1 = tlog.registerNewRM(props1);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (RMNameAlreadyInUseException e) {
			fail(getStackTrace(e));
		}
		//register rm2
		try {
			tlog.registerNewRM(props2);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (RMNameAlreadyInUseException e) {
			fail(getStackTrace(e));
		}
		
		int classifier=0;
		try {
			classifier = tlog.getIdForTxClassifier("classifier");
		} catch (TLogIOException e1) {
			fail(getStackTrace(e1));
		} catch (TLogFullException e1) {
			fail(getStackTrace(e1));
		}
		try {
			String classifierForId = tlog.getTxClassifierById(classifier);
			assertEquals("classifier", classifierForId);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidClassifierIDException e) {
			fail(getStackTrace(e));
		}
		/// testing negative classifier id
		try {
			tlog.getTxClassifierById(-2);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidClassifierIDException e) {
			assertTrue(true);
		}
		/// testing not real classifier id
		try {
			tlog.getTxClassifierById(800);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidClassifierIDException e) {
			assertTrue(true);
		}
		TransactionRecordImpl rec = new TransactionRecordImpl();
		initTransactionRecord(rec, classifier, 6);
		rec.setRMIDs(new int[] {rmId1});
		rec.setBranchIterators(new byte[] {0});
	
		try {
			tlog.writeTransactionRecord(rec);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMIDException e) {
			fail(getStackTrace(e));
		} catch (InvalidTransactionClassifierID e) {
			fail(getStackTrace(e));
		}
		
		int[] usedRMids = null;
		try{
			usedRMids = tlog.getAllUsedRMIDs();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		//unregister rm - s
		try {
			tlog.unregisterRM(usedRMids[0]);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMIDException e) {
			fail(getStackTrace(e));
		}
		try {
			tlog.unregisterRM(usedRMids[1]);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMIDException e) {
			fail(getStackTrace(e));
		}
		
		int[] rmid = null;
		try{
			props2.setKeyName("fake resource manager");
			try {
				try {
					Thread.sleep(TransactionServiceFrame.txTimeout*4);
				} catch (InterruptedException e) {}
				tlog.registerNewRM(props2);
			} catch (RMNameAlreadyInUseException e) {
				fail(getStackTrace(e));
			}
			try {
				Thread.sleep(TransactionServiceFrame.txTimeout*4);
			} catch (InterruptedException e) {}
			rmid = tlog.getAllUsedRMIDs();
			assertEquals("there is more than 1 or 0 rm into tlog", 2, rmid.length);
			assertTrue("Check if id is for rm1", tlog.getRMProperties(rmid[0]).equals(props1));
			assertTrue("Check if id is for rm3", tlog.getRMProperties(rmid[1]).equals(props2));
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMKeyException e) {
			fail(getStackTrace(e));
		}
		
		try{
			tlog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		
		try{
			tlog = writer.lockOrphanedTLog();
			assertTrue(null!=tlog);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		int[] rmids = null;
		Iterator<TransactionRecord> it = null;
		try{
			rmids = tlog.getAllUsedRMIDs();
			it = tlog.getAllTransactionRecords();
			//assert that resource manager remaining is 1
			assertEquals("There is more than 1 or 0 rm into tlog", 2, rmids.length);
			//assert that resource manager remaining is the same
			assertTrue("Check if id is for rm2", tlog.getRMProperties(rmid[0]).equals(props1));
			assertTrue("Check if id is for rm2", tlog.getRMProperties(rmid[1]).equals(props2));
			//assert that the transaction record in the rm is the same
			assertTrue("If the transaction record is the same", it.next().equals(rec));
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMKeyException e) {
			fail(getStackTrace(e));
		}
		
		try{
			tlog.removeTransactionRecordLazily(rec.getTransactionSequenceNumber());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		try{
			tlog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		try{
			assertNull("orphaned tlog should be null", writer.lockOrphanedTLog());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
	}

	@Test
	public void scenario3() {
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
		
		//register rm1
		int rmID=0;
		try {
			rmID = tlog.registerNewRM(props1);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (RMNameAlreadyInUseException e) {
			fail(getStackTrace(e));
		}
		
		// write transaction record to rm1!!!
		// set properties of transaction record
		int classifier=0;
		try {
			classifier = tlog.getIdForTxClassifier("classifier");
		} catch (TLogIOException e1) {
			fail(getStackTrace(e1));
		} catch (TLogFullException e1) {
			fail(getStackTrace(e1));
		}
		
		TransactionRecordImpl rec1 = new TransactionRecordImpl();
		initTransactionRecord(rec1, classifier, 6);
		int idd = -1;
		try {
			idd = tlog.getRMIDByName("ResourceManager1");
		} catch (InvalidRMKeyException e) {
			fail(getStackTrace(e));
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} // transaction record to 1st resource manager
		int[] ids = new int[1];
		ids[0] = idd;
		rec1.setRMIDs(ids);
		rec1.setBranchIterators(new byte[] {0});
		TransactionRecord rec = rec1;
	
		try {
			tlog.writeTransactionRecord(rec);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMIDException e) {
			fail(getStackTrace(e));
		} catch (InvalidTransactionClassifierID e) {
			fail(getStackTrace(e));
		}
		
		int id = Integer.MIN_VALUE;
		try {
			id = tlog.getRMIDByName("ResourceManager1");
			tlog.unregisterRM(id);
		} catch(TLogIOException e) {
			fail(getStackTrace(e));
		} catch(InvalidRMIDException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMKeyException e) {
			fail(getStackTrace(e));
		}
		
		try {
			assertTrue("get rm1 != null", tlog.getRMProperties(id).equals(props1));
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMKeyException e) {
			fail(getStackTrace(e));
		}
		
		try {
			tlog.removeTransactionRecordLazily(rec.getTransactionSequenceNumber());
		} catch (TLogIOException e1) {
			fail(getStackTrace(e1));
		}
	
		try {
			Thread.sleep(TransactionServiceFrame.txTimeout*4);
		} catch (InterruptedException e1) {}
		
		try {
			props1.setKeyName("new resource manager");
			tlog.registerNewRM(props1);
		} catch (TLogIOException e1) {
			fail(getStackTrace(e1));
		} catch (RMNameAlreadyInUseException e1) {
			fail(getStackTrace(e1));
		}
		
		RMProps props = null;
		try {
			props = tlog.getRMProperties(rmID);
		} catch (TLogIOException e1) {
			fail(getStackTrace(e1));
		} catch (InvalidRMKeyException e1) {
			assertNull(props);
		}
	
		try{
			tlog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
	
		try{
			assertNull("orphaned tlog should be null", writer.lockOrphanedTLog());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
	}

	@Test
	public void scenario4() {
		// record with fake rm id
		TransactionRecordImpl impl = new TransactionRecordImpl();
		impl.setTransactionAbandonTimeout(
				System.currentTimeMillis() + TransactionServiceFrame.txTimeout);
		impl.setTransactionBirthTime(System.currentTimeMillis());
		impl.setTransactionSequenceNumber(4);
		int[] a = new int[1];
		a[0] = 1000;
		impl.setRMIDs(a);
		impl.setBranchIterators(new byte[] {1});
		try{
			tlog.writeTransactionRecord(impl);
			fail("Exception not thrown");
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMIDException e) {
			assertTrue(true);
		} catch (InvalidTransactionClassifierID e) {
			fail(getStackTrace(e));
		}
		
		try{
			tlog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		try{
			assertNull("orphaned tlog should be null", writer.lockOrphanedTLog());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
	}

	@Test
	public void scenario5() {
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
		
		//register rm1
		int rmID=0;
		try {
			rmID = tlog.registerNewRM(props1);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (RMNameAlreadyInUseException e) {
			fail(getStackTrace(e));
		}
		
		int classifier=0;
		try {
			classifier = tlog.getIdForTxClassifier("classifier");
		} catch (TLogIOException e1) {
			fail(getStackTrace(e1));
		} catch (TLogFullException e1) {
			fail(getStackTrace(e1));
		}
	
		TransactionRecordImpl rec1 = createRecord(classifier,6, rmID);
		TransactionRecordImpl rec2 = createRecord(classifier,7, rmID);
		TransactionRecordImpl rec3 = createRecord(classifier,8, rmID);
		
		// set properties of transaction record
		int classifier1=0;
		try {
			classifier1 = tlog.getIdForTxClassifier("classifier1");
		} catch (TLogIOException e1) {
			fail(getStackTrace(e1));
		} catch (TLogFullException e1) {
			fail(getStackTrace(e1));
		}
		TransactionRecord rec4 = createRecord(classifier1, 9, rmID);
		TransactionRecord rec5 = createRecord(classifier1, 20, rmID);
		
		
		int classifier2=0;
		try {
			classifier2 = tlog.getIdForTxClassifier("classifier2");
		} catch (TLogIOException e1) {
			fail(getStackTrace(e1));
		} catch (TLogFullException e1) {
			fail(getStackTrace(e1));
		}
		int classifier3=0;
		try {
			classifier3 = tlog.getIdForTxClassifier("classifier1");
		} catch (TLogIOException e1) {
			fail(getStackTrace(e1));
		} catch (TLogFullException e1) {
			fail(getStackTrace(e1));
		}
	
		assertTrue(classifier1==classifier3);
		assertTrue(classifier!=classifier1);
		assertTrue(classifier!=classifier2);
		assertTrue(classifier!=classifier3);
	
		// with different names
		TransactionRecord rec6 = createRecord(classifier2, 21, rmID);
		TransactionRecord rec7 = createRecord(classifier3, 11, rmID);
		
		try {
			tlog.writeTransactionRecord(rec1);
			tlog.writeTransactionRecord(rec2);
			tlog.writeTransactionRecord(rec3);
			tlog.writeTransactionRecord(rec4);
			tlog.writeTransactionRecord(rec5);
			tlog.writeTransactionRecord(rec6);
			tlog.writeTransactionRecord(rec7);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMIDException e) {
			fail(getStackTrace(e));
		} catch (InvalidTransactionClassifierID e) {
			fail(getStackTrace(e));
		}
		
		Iterator<TransactionRecord> it = null;
		try {
			it = tlog.getAllTransactionRecords();
			assertTrue("Tlog doesn't contains rec 1", iteratorContainsRecord(it, rec1));
			it = tlog.getAllTransactionRecords();
			assertTrue("Tlog doesn't contains rec 2", iteratorContainsRecord(it, rec2));
			it = tlog.getAllTransactionRecords();
			assertTrue("Tlog doesn't contains rec 3", iteratorContainsRecord(it, rec3));
			it = tlog.getAllTransactionRecords();
			assertTrue("Tlog doesn't contains rec 4", iteratorContainsRecord(it, rec4));
			it = tlog.getAllTransactionRecords();
			assertTrue("Tlog doesn't contains rec 5", iteratorContainsRecord(it, rec5));
			it = tlog.getAllTransactionRecords();
			assertTrue("Tlog doesn't contains rec 6", iteratorContainsRecord(it, rec6));
			it = tlog.getAllTransactionRecords();
			assertTrue("Tlog doesn't contains rec 7", iteratorContainsRecord(it, rec7));
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		try{
			tlog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		try {
			tlog = writer.lockOrphanedTLog();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		try {
			tlog.removeTransactionRecordLazily(rec1.getTransactionSequenceNumber());
			tlog.removeTransactionRecordLazily(rec2.getTransactionSequenceNumber());
			tlog.removeTransactionRecordLazily(rec3.getTransactionSequenceNumber());
			tlog.removeTransactionRecordLazily(rec4.getTransactionSequenceNumber());
			tlog.removeTransactionRecordLazily(rec5.getTransactionSequenceNumber());
			tlog.removeTransactionRecordLazily(rec6.getTransactionSequenceNumber());
			tlog.removeTransactionRecordLazily(rec7.getTransactionSequenceNumber());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		try {
			tlog.flushRemovedTransactionRecords();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		try{
			tlog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		try {
			assertNull("orphaned tlog should be null", writer.lockOrphanedTLog());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
	}

	@Test
	public void scenario6() {
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
		
		//register rm1
		try {
			tlog.registerNewRM(props1);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (RMNameAlreadyInUseException e) {
			fail(getStackTrace(e));
		}
		
		writeDelete(2);
		writeDelete(3);
		writeDelete(4);
		writeDelete(5);
		writeDelete(6);
	
		try {
			Iterator<TransactionRecord> recs = tlog.getAllTransactionRecords();
			assertTrue(!recs.hasNext());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		try{
			tlog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		try{
			assertNull("orphaned tlog should be null", writer.lockOrphanedTLog());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
	}

	@Test
	public void scenario7() {
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
		
		//register rm1
		try {
			tlog.registerNewRM(props1);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (RMNameAlreadyInUseException e) {
			fail(getStackTrace(e));
		}
		
		int classifier=0;
		try {
			classifier = tlog.getIdForTxClassifier("classifier");
		} catch (TLogIOException e1) {
			fail(getStackTrace(e1));
		} catch (TLogFullException e1) {
			fail(getStackTrace(e1));
		}

		TransactionRecordImpl rec1 = new TransactionRecordImpl();
		initTransactionRecord(rec1, classifier, 6);
		int id = -1;
		try {
			id = tlog.getRMIDByName("ResourceManager1");
		} catch (InvalidRMKeyException e) {
			fail(getStackTrace(e));
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} // transaction record to 1st resource manager
		int[] ids = new int[1];
		ids[0] = id;
		rec1.setRMIDs(ids);
		rec1.setBranchIterators(new byte[] {5});
		TransactionRecord rec = rec1;
		
		try {
			tlog.writeTransactionRecord(rec);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMIDException e) {
			fail(getStackTrace(e));
		} catch (InvalidTransactionClassifierID e) {
			fail(getStackTrace(e));
		}
		
		try{
			tlog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		try{
			tlog = writer.lockOrphanedTLog();
			assertNull("orphaned tlog should be null", writer.lockOrphanedTLog());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		assertTrue(tlog != null);
		
		try {
			tlog.removeTransactionRecordImmediately(rec.getTransactionSequenceNumber());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		try{
			tlog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		try{
			assertNull("orphaned tlog should be null", writer.lockOrphanedTLog());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
	}

	@Test
	public void scenario8() {
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
			
			//register rm1
			try {
				tlog.registerNewRM(props1);
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} catch (RMNameAlreadyInUseException e) {
				fail(getStackTrace(e));
			}
			
			// write transaction record to rm1!!!
			TransactionRecordImpl rec = new TransactionRecordImpl();
			initTransactionRecord(rec, 0, 6);
			int id = -1;
			try {
				id = tlog.getRMIDByName(props1.getKeyName());
			} catch (InvalidRMKeyException e) {
				fail(getStackTrace(e));
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} // transaction record to 1st resource manager
			int[] ids = new int[1];
			ids[0] = id;
			rec.setRMIDs(ids);
			rec.setBranchIterators(new byte[] {5});
			// set properties of transaction record
			try {
				tlog.writeTransactionRecord(rec);
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} catch (InvalidRMIDException e) {
				fail(getStackTrace(e));
			} catch (InvalidTransactionClassifierID e) {
				fail(getStackTrace(e));
			}
			
			try{
				tlog.close();
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
			
			//create new tlog
			
			TLogVersion ver = new TLogVersion("APV".getBytes(), 9, System.currentTimeMillis());
			TLog tlog2 = null;
			try {
				tlog2 = writer.createNewTLog(ver.getTLogVersion());
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} catch (TLogAlreadyExistException e) {
				fail(getStackTrace(e));
			}
			
			
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
			
			//register rm2
			int rm2ID=0;
			try {
				rm2ID = tlog2.registerNewRM(props2);
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} catch (RMNameAlreadyInUseException e) {
				fail(getStackTrace(e));
			}
			
			int classifier=0;
			try {
				classifier = tlog2.getIdForTxClassifier("classifier");
			} catch (TLogIOException e1) {
				fail(getStackTrace(e1));
			} catch (TLogFullException e1) {
				fail(getStackTrace(e1));
			}
			
			TransactionRecordImpl rec1 = new TransactionRecordImpl();
			initTransactionRecord(rec1, classifier, 6);
			rec1.setRMIDs(new int[] {rm2ID});
			rec1.setBranchIterators(new byte[] {0});
			try {
				tlog2.getRMIDByName(props2.getKeyName());
			} catch (InvalidRMKeyException e) {
				fail(getStackTrace(e));
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} // transaction record to 1st resource manager
			TransactionRecord rec2 = rec1;
			try {
				tlog2.writeTransactionRecord(rec2);
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} catch (InvalidRMIDException e) {
				fail(getStackTrace(e));
			} catch (InvalidTransactionClassifierID e) {
				fail(getStackTrace(e));
			}
			
			try{
				tlog2.close();
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
			
			try{
				tlog = writer.lockOrphanedTLog();
				tlog2 = writer.lockOrphanedTLog();
				assertNull("orphaned tlog should be null", writer.lockOrphanedTLog());
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
			
			Iterator<TransactionRecord> it1 = null;
			Iterator<TransactionRecord> it2 = null;
			try {
				it1 = tlog.getAllTransactionRecords();
				it2 = tlog2.getAllTransactionRecords();
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
			
			TransactionRecord red1 = it1.next();
			TransactionRecord red2 = it2.next();
			boolean tmp = false;
			try {
				tlog.removeTransactionRecordImmediately(red1.getTransactionSequenceNumber());
			} catch (TLogIOException e) {
				tmp = true;
				try {
					tlog.removeTransactionRecordImmediately(red2.getTransactionSequenceNumber());
				} catch (TLogIOException e1) {
					fail(getStackTrace(e));
				}
			}
			try{
				tlog.close();
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
			
			try {
				assertNull("orphaned tlog should be null", writer.lockOrphanedTLog());
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
			//// end tlog
			
			
			try {
				if(tmp) {
					tlog2.removeTransactionRecordImmediately(red1.getTransactionSequenceNumber());
				} else {
					tlog2.removeTransactionRecordImmediately(red2.getTransactionSequenceNumber());
				}
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
			try{
				tlog2.close();
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
			
			try {
				assertNull("orphaned tlog should be null", writer.lockOrphanedTLog());
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
			
		}

	@Test
	public void scenario9() {
		try {
			assertTrue(! (tlog == null));
			assertTrue("There should not have any records", !tlog.getAllTransactionRecords().hasNext());
			tlog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		try{
			assertNull("orphaned tlog should be null", writer.lockOrphanedTLog());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
	}

	@Test
	public void scenario10() {
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
		
		//register rm1
		try {
			tlog.registerNewRM(props1);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (RMNameAlreadyInUseException e) {
			fail(getStackTrace(e));
		}
		
		int classifier=0;
		try {
			classifier = tlog.getIdForTxClassifier("classifier");
		} catch (TLogIOException e1) {
			fail(getStackTrace(e1));
		} catch (TLogFullException e1) {
			fail(getStackTrace(e1));
		}

		TransactionRecordImpl rec1 = new TransactionRecordImpl();
		initTransactionRecord(rec1, classifier, 6666);

		int id = -1;
		try {
			id = tlog.getRMIDByName("ResourceManager1");
		} catch (InvalidRMKeyException e) {
			fail(getStackTrace(e));
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} // transaction record to 1st resource manager
		int[] ids = new int[1];
		ids[0] = id;
		rec1.setRMIDs(ids);
		rec1.setBranchIterators(new byte[] {0});
		TransactionRecord rec = rec1;
		
		try {
			tlog.writeTransactionRecord(rec);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMIDException e) {
			fail(getStackTrace(e));
		} catch (InvalidTransactionClassifierID e) {
			fail(getStackTrace(e));
		}
		
		try {
			tlog.removeTransactionRecordImmediately(rec.getTransactionSequenceNumber());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		try{
			tlog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		//--------------------------------------
		try {
			tlog.close();
			fail("Tlog is already deleted");
		} catch (TLogIOException e) {
			assertTrue(true);
		}	
		try {
			tlog.getAllTransactionRecords();
			fail("Tlog is already deleted");
		} catch (TLogIOException e) {
			assertTrue(true);
		}
		
		try {
			tlog.getAllUsedRMIDs();
			fail("Tlog is already deleted");
		} catch (TLogIOException e) {
			assertTrue(true);
		}
		
		try {
			tlog.getIdForTxClassifier("classifier");
			fail("Tlog is already deleted");
		} catch (TLogFullException e) {
		} catch (TLogIOException e) {
		}
		try {
			tlog.getRMIDByName("ResourceManager1");
			fail("Tlog is already deleted");
		} catch (TLogIOException e) {
		} catch (InvalidRMKeyException e) {
		}
		try {
			tlog.getRMProperties(tlog.getAllUsedRMIDs()[0]);
			fail("Tlog is already deleted");
		} catch (TLogIOException e) {
		} catch (InvalidRMKeyException e) {
			fail(getStackTrace(e));
		}
		try {
			tlog.registerNewRM(new RMProps());
			fail("Tlog is already deleted");
		} catch (TLogIOException e) {
		} catch (RMNameAlreadyInUseException e) {
			fail(getStackTrace(e));
		}
		try {
			tlog.removeTransactionRecordImmediately(rec.getTransactionSequenceNumber());
			fail("Tlog is already deleted");
		} catch (TLogIOException e) {
		}
		try {
			tlog.removeTransactionRecordLazily(rec.getTransactionSequenceNumber());
			fail("Tlog is already deleted");
		} catch (TLogIOException e) {
		}
		try {
			tlog.unregisterRM(tlog.getAllUsedRMIDs()[0]);
			fail("Tlog is already deleted");
		} catch (TLogIOException e) {
		} catch (InvalidRMIDException e) {
		}
		try {
			tlog.writeTransactionRecord(rec);
			fail("Tlog is already deleted");
		} catch (TLogIOException e) {
		} catch (InvalidRMIDException e) {
		} catch (InvalidTransactionClassifierID e) {
		}
		
		try {
			tlog.flushRemovedTransactionRecords();
		} catch (TLogIOException e) {
			assertTrue(true);
		}		
		//------
		try{
			assertNull("orphaned tlog should be null", writer.lockOrphanedTLog());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
	}

	@Test
	public void scenario11() {
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
		
		//register rm1
		int rmID = 0;
		try {
			rmID = tlog.registerNewRM(props1);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (RMNameAlreadyInUseException e) {
			fail(getStackTrace(e));
		}
		
		// write transaction record to rm1!!!
		TransactionRecordImpl rec = new TransactionRecordImpl();
		rec.setBranchIterators(new byte[] {0});
		rec.setRMIDs(new int[] {rmID});
		rec.setTransactionBirthTime(System.currentTimeMillis());
		rec.setTransactionAbandonTimeout(System.currentTimeMillis()+TransactionServiceFrame.txTimeout);
		rec.setTransactionClassifierID(0);
		rec.setTransactionSequenceNumber(1);
		// set properties of transaction record
		try {
			tlog.writeTransactionRecord(rec);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMIDException e) {
			fail(getStackTrace(e));
		} catch (InvalidTransactionClassifierID e) {
			fail(getStackTrace(e));
		}
		
		try{
			tlog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		TLogVersion ver = new TLogVersion("APM".getBytes(), 1, System.currentTimeMillis());
		TLog tlog2 = null;
		try {
			tlog2 = writer.createNewTLog(ver.getTLogVersion());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (TLogAlreadyExistException e) {
			fail(getStackTrace(e));
		}
	
		try {
			tlog = writer.lockOrphanedTLog();
		} catch (TLogIOException e) {
			fail("Cannot get orpaned tlog");
		}
		
		Iterator<TransactionRecord> it = null;
		try{
			it = tlog.getAllTransactionRecords();
			//assert that the transaction record in the rm is the same
			assertTrue("If the transaction record is the same", it.next().equals(rec));
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		try {
			tlog.removeTransactionRecordLazily(rec.getTransactionSequenceNumber());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		try{
			tlog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
	
		try{
			tlog2.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		try{
			assertNull("orphaned tlog should be null", writer.lockOrphanedTLog());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
	}

	@Test
	public void scenario12() {
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
		
		//register rm1
		try {
			tlog.registerNewRM(props1);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (RMNameAlreadyInUseException e) {
			fail(getStackTrace(e));
		}
		
		//register rm1 again should result to exception
		try {
			tlog.registerNewRM(props1);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (RMNameAlreadyInUseException e) {
			assertTrue(true);
		}
		
		try {
			tlog.getRMIDByName("Wrong Name");
			fail("Cannot get id for wrong name");
		} catch (InvalidRMKeyException e) {
			assertTrue(true); // if it is here it means that the exception is thrown for searching for wrong rm name
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} 
		
		try {
			assertTrue(tlog.getRMIDByName(props1.getKeyName()) >= 0);
		} catch (InvalidRMKeyException e) {
			fail(getStackTrace(e));
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
		try{
			tlog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
	
		try{
			assertNull("orphaned tlog should be null", writer.lockOrphanedTLog());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
	}

	@Test
	public void scenario13() {
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
		
		//register rm1
		int rmID = 0;
		try {
			rmID = tlog.registerNewRM(props1);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (RMNameAlreadyInUseException e) {
			fail(getStackTrace(e));
		}
		
		int classifier=0;
		try {
			classifier = tlog.getIdForTxClassifier("classifier");
		} catch (TLogIOException e1) {
			fail(getStackTrace(e1));
		} catch (TLogFullException e1) {
			fail(getStackTrace(e1));
		}
		
		TransactionRecordImpl rec1 = new TransactionRecordImpl();
		initTransactionRecord(rec1, classifier, 1000000);

		rec1.setBranchIterators(new byte[] {0});
		int id = -1;
		try {
			id = tlog.getRMIDByName("ResourceManager1");
			assertTrue(id == rmID);
		} catch (InvalidRMKeyException e) {
			fail(getStackTrace(e));
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} // transaction record to 1st resource manager
		int[] ids = new int[1];
		ids[0] = id;
		rec1.setRMIDs(ids);
		TransactionRecord rec = rec1;
		
		try {
			tlog.writeTransactionRecord(rec);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMIDException e) {
			fail(getStackTrace(e));
		} catch (InvalidTransactionClassifierID e) {
			fail(getStackTrace(e));
		}
		
		try {
			int a = tlog.getIdForTxClassifier("some name 1");
			int b = tlog.getIdForTxClassifier("some name 2");
			assertTrue(tlog.getIdForTxClassifier(null) == 0);
			assertTrue(tlog.getIdForTxClassifier("") == 0);
			assertTrue(a > 0);
			assertTrue(b > 0);
			assertTrue(a != b);
		} catch (TLogFullException e) {
			fail(getStackTrace(e));
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
	
		try{
			tlog.removeTransactionRecordLazily(rec1.getTransactionSequenceNumber());
			tlog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
	
		try{
			assertNull("orphaned tlog should be null", writer.lockOrphanedTLog());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
	}

	@Test
	public void scenario14() { // rm id-s should be positive and increasing???
		int i = 1;
		int[] temp = new int[11];
		temp[0] = Integer.MIN_VALUE;
		while(i < 10) {
			
			//create rm1
			RMProps props1 = new RMProps();
			props1.setKeyName("ResourceManager" + i);
			props1.setRmContainerName("Container1");
			Properties secure = new Properties();
			secure.setProperty("one", "string1");
			secure.setProperty("two", "string2");
			Properties unsecure = new Properties();
			unsecure.setProperty("qwe", "unsecure1");
			unsecure.setProperty("rty", "unsecure2");
			props1.setNonSecureProperties(unsecure);
			props1.setSecureProperties(secure);
			
			//register rm1
			try {
				tlog.registerNewRM(props1);
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} catch (RMNameAlreadyInUseException e) {
				fail(getStackTrace(e));
			}
			
			try {
				int id = tlog.getRMIDByName(props1.getKeyName());
				assertTrue("RM id is not positive", id > 0);
				temp[i++] = id;
				assertTrue("RM id is not increasing", id > temp[i-2]);
				tlog.unregisterRM(id);
				try {
					Thread.sleep(TransactionServiceFrame.txTimeout*3);
				} catch (InterruptedException e) {}
			} catch (InvalidRMKeyException e) {
				fail(getStackTrace(e));
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} catch (InvalidRMIDException e) {
				fail(getStackTrace(e));
			}
			
		}
	
		try{
			tlog.close();
			assertNull("orphaned tlog should be null", writer.lockOrphanedTLog());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
		
	}
	
	/**
	 * 1) Register RM
	 * 2) Write two records
	 * 3) Delete one record lazily
	 * 4) Close, open TLog
	 * 5) Check that there is only one record and it's the right one
	 * 6) Delete record
	 * 7) Close, check there are no TLog(s)
	 */
	@Test
	public void Scenario15() {  
		RMProps props = makeRM("NewRM", "Container",
				new String [] {"sec1", "pr2", "pass"},
				new String [] {"prop1", "prop2", "prop3", "prop5"});

		int RMID = 0;
		try {
			RMID = tlog.registerNewRM(props);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (RMNameAlreadyInUseException e) {
			fail(getStackTrace(e));
		}

		TransactionRecord recDelete = createRecord(0, 1,  RMID);
		TransactionRecord rec = createRecord(0, 2,  RMID);

		try {
			tlog.writeTransactionRecord(recDelete);
			tlog.writeTransactionRecord(rec);
			tlog.removeTransactionRecordLazily(recDelete.getTransactionSequenceNumber());
			tlog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMIDException e) {
			fail(getStackTrace(e));
		} catch (InvalidTransactionClassifierID e) {
			fail(getStackTrace(e));
		}

		try {
			tlog = writer.lockOrphanedTLog();

			Iterator<TransactionRecord> it = tlog.getAllTransactionRecords();
			assertTrue(it.hasNext());
			TransactionRecord r = it.next();
			assertTrue(rec.equals(r));
			assertFalse(recDelete.equals(r));
			assertFalse(it.hasNext());

			tlog.removeTransactionRecordLazily(r.getTransactionSequenceNumber());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}

		try {
			tlog.close();
			assertNull(writer.lockOrphanedTLog());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
	}

	
	@Test
	public void testCrypting() {
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

		try {
			tlog.registerNewRM(props1);
			Properties secProps = getPlainSecureProps();
			assertFalse(props1.getSecureProperties().equals(secProps));
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (RMNameAlreadyInUseException e) {
			fail(getStackTrace(e));
		} finally {
			try {
				tlog.close();
				assertNull(writer.lockOrphanedTLog());
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
		}
	}

	@Test
	public void testInbound0() {
		TLogVersion ver = new TLogVersion("TST".getBytes(), 25488451, 0);
		InboundTLog tLog = beginInboundTest(ver);
		try {
			RMProps rmProps = makeRM("RM", "container", new String [] {"prop1", "prop2"}, new String[] {"shash", "hasha"});
//			int rmId = 0;
			try {
				/*rmId =*/ tLog.registerNewRM(rmProps);
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} catch (RMNameAlreadyInUseException e) {
				fail(getStackTrace(e));
			}
	
//			int clId = 0;
			try {
				/*clId =*/ tLog.getIdForTxClassifier("classssssssifier");
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} catch (TLogFullException e) {
				fail(getStackTrace(e));
			}

			tLog.close();
			tLog = writer.getInboundTLog(ver);
			tLog.close();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}
	}

	@Test
	public void testInbound1() {
		TLogVersion ver = new TLogVersion("TST".getBytes(), 25488451, 0);
		InboundTLog tLog = beginInboundTest(ver);

		try {
			RMProps rmProps = makeRM("RM", "container", new String [] {"prop1", "prop2"}, new String[] {"shash", "hasha"});
			int rmId = 0;
			try {
				rmId = tLog.registerNewRM(rmProps);
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} catch (RMNameAlreadyInUseException e) {
				fail(getStackTrace(e));
			}
	
			int clId = 0;
			try {
				clId = tLog.getIdForTxClassifier("classssssssifier");
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} catch (TLogFullException e) {
				fail(getStackTrace(e));
			}
	
			byte [] branchIterCl = new byte [] {5,1,5,6,21,6,4,4,6,2,62,46,42,6,111};
			byte [] globalIdCl = new byte [] {37,73,73,2,44,61,35,61,3,4,4,5};
			Xid xidCl = new XidImpl(6, branchIterCl, globalIdCl);
			InboundTransactionRecordImpl recCl = makeInboundTxRec(1, clId, xidCl, 0, new int [] {rmId}, new byte[] {0});
	
			byte [] branchIter = new byte [] {5,1,7,6,81,6,4,4,22,22,52,46,42,6,111,6,6,2,4,6};
			byte [] globalId = new byte [] {37,76,73,2,46,61,35,64,3,4,4,5,35,3,46};
			Xid xid = new XidImpl(111, branchIter, globalId);
			InboundTransactionRecord rec = makeInboundTxRec(10, 0, xid, 0, new int [] {rmId}, new byte[] {0});
	
			try {
				tLog.writeTransactionRecord(rec);
				fail("writeTransactionRecord should not work");
			} catch (Exception e) {
				// this is okay :)
			}
	
			try {
				tLog.writeInboundTransactionRecord(recCl);
				tLog.writeInboundTransactionRecord(rec);
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} catch (InvalidRMIDException e) {
				fail(getStackTrace(e));
			} catch (InvalidTransactionClassifierID e) {
				fail(getStackTrace(e));
			}
	
			try {
				tLog.close();
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
	
			try {
				tLog = writer.getInboundTLog(ver);
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
	
			try {
				recCl.setHeuristicOutcome(25);
				tLog.setHeuristicOutcome(recCl.getTransactionSequenceNumber(), 25);
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
	
			try {
				Map<Xid, InboundTransactionRecord> r = tLog.recover();
				InboundTransactionRecord rec1 = r.get(recCl.getExternalXID());
				assertTrue(rec1.equals(recCl));
				assertTrue(rec1.getHeuristicOutcome() == recCl.getHeuristicOutcome());
				assertTrue(rec1.getHeuristicOutcome() == 25);
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
	
			try {
				tLog.close();
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
	
			try {
				tLog = writer.getInboundTLog(ver);
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
	
			try {
				Map<Xid, InboundTransactionRecord> r = tLog.recover();
				InboundTransactionRecord rec1 = r.get(recCl.getExternalXID());
				assertTrue(rec1.equals(recCl));
				assertTrue(rec1.getHeuristicOutcome() == recCl.getHeuristicOutcome());
				assertTrue(rec1.getHeuristicOutcome() == 25);
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
	
			try {
				Map<Xid, InboundTransactionRecord> r = tLog.recover();
				assertTrue(r.size() == 2);
				for (Map.Entry<Xid, InboundTransactionRecord> e : r.entrySet()) {
					if (e.getKey().equals(xid)) {
						assertTrue(e.getValue().equals(rec));
					} else if (e.getKey().equals(xidCl)) {
						assertTrue(e.getValue().equals(recCl));
					} else {
						fail("Unknown XID after recover.");
					}
				}
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
	
			try {
				tLog.removeTransactionRecordLazily(rec.getTransactionSequenceNumber());
				tLog.removeTransactionRecordLazily(recCl.getTransactionSequenceNumber());
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
	
			try {
				assertTrue(tLog.getRMProperties(rmId).equals(rmProps));
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} catch (InvalidRMKeyException e) {
				fail(getStackTrace(e));
			}

		} finally {
			try {
				tLog.close();
				assertNull(writer.lockOrphanedTLog());
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
		}
	}

	@Test
	public void testInbound2() {
		TLogVersion ver = new TLogVersion("T02".getBytes(), 25488452, 0);
		InboundTLog tLog = beginInboundTest(ver);

		try {
			RMProps rmProps = makeRM("RM", "container", new String [] {"prop1", "prop2"}, new String[] {"shash", "hasha"});
			int rm1Id = 0;
			try {
				rm1Id = tLog.registerNewRM(rmProps);
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} catch (RMNameAlreadyInUseException e) {
				fail(getStackTrace(e));
			}
	
			RMProps rm2Props = makeRM("RM2", "container2", new String [] {"paffahafh", "tahha"}, new String[] {"wehwah", "srhas", "ahshasha"});
			int rm2Id = 0;
			try {
				rm2Id = tLog.registerNewRM(rm2Props);
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} catch (RMNameAlreadyInUseException e) {
				fail(getStackTrace(e));
			}
	
			byte [] branchIter = new byte [] {5,1,7,6,81,6,4,4,-22,22,52,46,42,6,-2,6,6,2,-4,6};
			byte [] globalId = new byte [] {37,76,-73,2,46,-61,35,-64,3,4,4,5,35,3,-46};
			Xid xid = new XidImpl(111, branchIter, globalId);
			InboundTransactionRecord rec = makeInboundTxRec(10, 0, xid, 0, new int [] {rm1Id, rm2Id}, new byte[] {0,0});
	
			try {
				tLog.writeInboundTransactionRecord(rec);
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} catch (InvalidRMIDException e) {
				fail(getStackTrace(e));
			} catch (InvalidTransactionClassifierID e) {
				fail(getStackTrace(e));
			}
	
			try {
				tLog.close();
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
	
			try {
				tLog = writer.getInboundTLog(ver);
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
	
			try {
				Map<Xid, InboundTransactionRecord> r = tLog.recover();
				assertTrue(r.size() == 1);
				for (Map.Entry<Xid, InboundTransactionRecord> e : r.entrySet()) {
					if (e.getKey().equals(xid)) {
						assertTrue(e.getValue().equals(rec));
					} else {
						fail("Unknown XID after recover.");
					}
				}
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
	
			try {
				assertTrue(tLog.getRMProperties(rm1Id).equals(rmProps));
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} catch (InvalidRMKeyException e) {
				fail(getStackTrace(e));
			}
	
			try {
				assertTrue(tLog.getRMProperties(rm2Id).equals(rm2Props));
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			} catch (InvalidRMKeyException e) {
				fail(getStackTrace(e));
			}
	
			try {
				tLog.removeTransactionRecordImmediately(rec.getTransactionSequenceNumber());
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}

		} finally {
			try {
				tLog.close();
				assertNull(writer.lockOrphanedTLog());
			} catch (TLogIOException e) {
				fail(getStackTrace(e));
			}
		}
	}

//	@Test
//	public void testInbound3() {
//		InboundTLog tLog = beginInboundTest(new TLogVersion("TST".getBytes(), 25488451, 0));
//	}
//
//	@Test
//	public void testInbound4() {
//		InboundTLog tLog = beginInboundTest(new TLogVersion("ZZZ".getBytes(), 25488454, 0));
//	}
//
//	@Test
//	public void testInbound5() {
//		InboundTLog tLog = beginInboundTest(new TLogVersion("TST".getBytes(), 25488451, 0));
//	}

	private InboundTLog beginInboundTest(TLogVersion ver) {
		try {
			tlog.close();
			assertNull(writer.lockOrphanedTLog());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}

		InboundTLog res = null;
		try {
			res = writer.getInboundTLog(ver);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}

		return res;
	}

	private InboundTransactionRecordImpl makeInboundTxRec(
			long txSequenceNumber,
			int txClassifierID,
			Xid externalXID,
			int heuristicOutcome,
			int[] rmIDs,
			byte[] brIterators) {

		InboundTransactionRecordImpl rec = new InboundTransactionRecordImpl();
		rec.setTransactionBirthTime(System.currentTimeMillis());
		rec.setTransactionAbandonTimeout(System.currentTimeMillis()+1000000);
		rec.setTransactionClassifierID(txClassifierID);
		rec.setTransactionSequenceNumber(txSequenceNumber);
		rec.setRMIDs(rmIDs);
		rec.setBranchIterators(brIterators);
		rec.setExternalXID(externalXID);
		rec.setHeuristicOutcome(heuristicOutcome);
		return rec;
	}

	private RMProps makeRM(String keyName, String container, String[] secProps, String[] unsecProps) {
		RMProps props1 = new RMProps();
		props1.setKeyName(keyName);
		props1.setRmContainerName(container);
		Properties secure = new Properties();
		for (String s : secProps) {
			secure.setProperty(s, s+"Value");
		}
		Properties unsecure = new Properties();
		for (String s : unsecProps) {
			unsecure.setProperty(s, s+"Value");
		}
		props1.setNonSecureProperties(unsecure);
		props1.setSecureProperties(secure);
		return props1;
	}

	/**
	 * @param classifier
	 * @return
	 */
	private TransactionRecordImpl createRecord(int classifier, long txId, int rmID) {
		TransactionRecordImpl rec1 = new TransactionRecordImpl();
		initTransactionRecord(rec1, classifier, txId);
		rec1.setRMIDs(new int[]{rmID} );
		rec1.setBranchIterators(new byte[] {2});
		return rec1;
	}

	/**
	 * @param i = classifier
	 * @return byte array from which we can create transaction record
	 */
	protected void initTransactionRecord(TransactionRecordImpl rec, int classifier, long seqNum) {
		long now = System.currentTimeMillis();
		rec.setTransactionAbandonTimeout(now + 1000000);
		rec.setTransactionBirthTime(now);
		rec.setTransactionClassifierID(classifier);
		rec.setTransactionSequenceNumber(seqNum);
	}

	private void writeDelete(int num) {
		int tmp = num+1;
		int classifier=0;
		try {
			classifier = tlog.getIdForTxClassifier("classifier " + num);
		} catch (TLogIOException e1) {
			fail(getStackTrace(e1));
		} catch (TLogFullException e1) {
			fail(getStackTrace(e1));
		}
		while(--tmp > 0) {
			
			TransactionRecordImpl rec1 = new TransactionRecordImpl();
			initTransactionRecord(rec1, classifier, tmp);
			int id = -1;
			try {
				id = tlog.getRMIDByName("ResourceManager1");
			} catch (InvalidRMKeyException e) {
				fail("fail to get id for given name");
			} catch (TLogIOException e) {
				fail("fail to get id by name");
			} // transaction record to 1st resource manager
			int[] ids = new int[1];
			ids[0] = id;
			rec1.setRMIDs(ids);
			rec1.setBranchIterators(new byte[] {0});
			TransactionRecord rec = rec1;
			
			try {
				tlog.writeTransactionRecord(rec);
			} catch (TLogIOException e) {
				fail("Cannot write record");
			} catch (InvalidRMIDException e) {
				fail("Invalid rm id in transaction record");
			} catch (InvalidTransactionClassifierID e) {
				fail("Invalid transaction classifier");
			}
		}
		Iterator<TransactionRecord> it = null;
		try {
			it = tlog.getAllTransactionRecords();
		} catch (TLogIOException e) {
			fail("Fail to get records");
		}
	
		
		int count = 0;
		while(it.hasNext()) {
			try {
				count++;
				TransactionRecord r = it.next();
				tlog.removeTransactionRecordImmediately(r.getTransactionSequenceNumber());
			} catch (TLogIOException e) {
				fail("Cannot remove transaction immediately");
			}
		}
	
		assertTrue(count == num);
	}

	private boolean iteratorContainsRecord(Iterator<TransactionRecord> it, TransactionRecord rec) {
		while(it.hasNext()) {
			if(it.next().equals(rec))
				return true;
		}
		return false;
	}

	/**
	 * get all secure properties without decryption
	 * @return get all secure properties without decryption
	 */
	protected abstract Properties getPlainSecureProps();

    protected static final String getStackTrace(Throwable e) {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    (e).printStackTrace(new PrintStream(baos));
	    return baos.toString();
	}	
}