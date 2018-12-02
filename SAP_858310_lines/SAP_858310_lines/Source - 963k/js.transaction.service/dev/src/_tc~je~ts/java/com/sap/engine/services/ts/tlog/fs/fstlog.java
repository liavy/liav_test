package com.sap.engine.services.ts.tlog.fs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

import com.sap.engine.frame.core.thread.execution.Executor;
import com.sap.engine.interfaces.transaction.RMProps;
import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.exceptions.TimeOutIsStoppedException;
import com.sap.engine.services.ts.facades.timer.TimeoutListener;
import com.sap.engine.services.ts.tlog.InvalidClassifierIDException;
import com.sap.engine.services.ts.tlog.InvalidRMIDException;
import com.sap.engine.services.ts.tlog.InvalidRMKeyException;
import com.sap.engine.services.ts.tlog.InvalidTransactionClassifierID;
import com.sap.engine.services.ts.tlog.RMNameAlreadyInUseException;
import com.sap.engine.services.ts.tlog.RMPropsExtension;
import com.sap.engine.services.ts.tlog.TLog;
import com.sap.engine.services.ts.tlog.TLogFullException;
import com.sap.engine.services.ts.tlog.TLogIOException;
import com.sap.engine.services.ts.tlog.TransactionRecord;
import com.sap.engine.services.ts.tlog.util.TLogLocking;
import com.sap.engine.services.ts.tlog.util.TLogLockingInfrastructureException;
import com.sap.engine.services.ts.utils.TLogVersion;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

public class FSTLog implements TLog {

	public static final String TX_FILE_NAME_SUFFIX = "_TX";
	public static final String RM_FILE_NAME_SUFFIX = "_RM";
	public static final String CLASSIFIER_FILE_NAME_SUFFIX = "_TXClassifier";
	public static final String LOG_FILE_EXTENSION = ".tlog";
	public static final byte CLASSIFIER_ENTRY_RECORD_TYPE = 69;
	
	private static int DEFAULT_MAX_RM_ENTRIES_PER_FILE = 100;	
	
	private final TLogVersion tLogVersion;
	private final File workingDirectory;	
	private final TLogLocking tlogLock;
	static final Location LOCATION = Location.getLocation(FSTLog.class);
	protected boolean isClosed = false;
	
	private File rmEntryFile;
	private FileChannel rmChannel;

	private File clEntryFile;
	private FileChannel clChannel;

	// Transaction classifiers
	protected final ClassifiersMap classifiers;

	protected final FSTLogOptimizator tLogOptimizator;

	// registered rmids(integers) - ascending order
	private Map<Integer, RMEntryRecord> rmEntries;
	private Set<RMRemoveRecord> rmRemoveRecords;
	private final Object rmChangeLock = new Object();
	
	protected int[] rmIDs;
	private int countIds; // count only for entry records

	public void setMaxFileSize(long maxFileSize) {
		tLogOptimizator.setMaxFileSize(maxFileSize);
	}

	public long getMaxFileSize() {
		return tLogOptimizator.getMaxFileSize();
	}

	public FSTLog(TLogVersion tLogVersion, File directory,
			TLogLocking tlogLock, Executor flusherThreadExecutor,
			int bufferCapacity, long maxTLogFileSize, File[] txFiles)
			throws FileNotFoundException {

		this(tLogVersion, directory, tlogLock, flusherThreadExecutor,
				new ConcurrentHashMap<Long, ByteBuffer>(
						bufferCapacity * 2),
				new ClassifiersMap(),
				new HashMap<Integer, RMEntryRecord>(),
				new HashSet<RMRemoveRecord>(), // hashSet is for removeRecords
				new int[]{}, 0, bufferCapacity, maxTLogFileSize, txFiles, 0, 0);
	}

	public FSTLog(TLogVersion tLogVersion, File directory,
			TLogLocking tlogLock, Executor flusherThreadExecutor,
			ConcurrentHashMap<Long, ByteBuffer> activeRecords,
			ClassifiersMap classifiers, Map<Integer, RMEntryRecord> rmEntries,
			Set<RMRemoveRecord> rmRemoveRecords, int[] rmIDs, int countIds,
			int bufferCapacity, long maxTLogFileSize, File[] txFiles,
			long initialFileSize, int currentFileName)
			throws FileNotFoundException {

		this.tLogVersion = tLogVersion;
		this.workingDirectory = directory;
		this.tlogLock = tlogLock;

		this.classifiers = classifiers;
		this.rmEntries = rmEntries;
		this.rmRemoveRecords = rmRemoveRecords;
		this.rmIDs = rmIDs;
		this.countIds = countIds;

		tLogOptimizator = new FSTLogOptimizator(txFiles, maxTLogFileSize,
				flusherThreadExecutor, bufferCapacity, activeRecords,
				initialFileSize, currentFileName);

		initializeClLogFile();
		initializeRMLogFile();
	}
	
	public String getRMName(int id) throws TLogIOException, InvalidRMIDException {
		RMEntryRecord rec = getUsedRMIDsMap(true).get(id);
		if(rec == null) {
			synchronized (rmChangeLock) {
				rec = getUsedRMIDsMap(true).get(id);
				if(rec == null) {
					RMProps props = null;
					try {
						props = getRMProperties(id);
					} catch (InvalidRMKeyException e) {
						throw new InvalidRMIDException(e);
					}
					if(props == null) {
						throw new InvalidRMIDException("No rm with such id = " + id);						
					}

					rec = new RMEntryRecord(id, props.getKeyName().getBytes(),
							props.getRmContainerName().getBytes(),
							props.getSecureProperties(), props.getNonSecureProperties());
					rmEntries.put(id, rec.encrypt());
					return props.getKeyName();
				}
				return new String(rec.getName());
			}
		}

		if(rec.getClass() == RMEntryRecord.class) {
			return new String(((RMEntryRecord) rec).getName());
		} else {
			throw new TLogIOException("The record for this id="+ id +" is remove record. This id is pending to be removed.");
		}
	}

	public void rotateRM() throws TLogIOException { // Rotate is working only with tlog files
		//remove old file
		String tmpName = rmEntryFile.getAbsolutePath();
		
		String fileName = tmpName.substring(
				rmEntryFile.getAbsolutePath().lastIndexOf(File.separator) + 1,
				rmEntryFile.getAbsolutePath().lastIndexOf(LOG_FILE_EXTENSION)
		);
		File newFile = new File(
				rmEntryFile.getAbsolutePath().substring(0, rmEntryFile.getAbsolutePath().lastIndexOf(File.separator) + 1) +
				fileName + "_new_File" + LOG_FILE_EXTENSION);
		FileChannel newChannel = null;
		try {
			newChannel = new FileOutputStream(newFile, false).getChannel();
		} catch (FileNotFoundException e) {
			throw new TLogIOException("File " + newFile.getAbsolutePath() + " cannot be found(while rotating).", e);
		}

		Map<Integer, RMEntryRecord> tempRMEntries = new HashMap<Integer, RMEntryRecord>();
		Set<RMRemoveRecord> tempRMRemoveRecords = new HashSet<RMRemoveRecord>();
		
		boolean readFromFile = false;
		// if the information is not in rmEntries we read it from rmEntryFile
//		if(rmEntries == null) {
//			readFromFile = true;
//			int[] rmidsInTxArray = FSTLogReaderWriter.getAssociatedRMids(tLogOptimizator.getActiveRecords());
//
//			Set<Integer> rmidsInTxRecords = new HashSet<Integer>();
//			for (int i = 0; i < rmidsInTxArray.length; i++) {
//				rmidsInTxRecords.add(rmidsInTxArray[i]);
//			}
//			rmidsInTxArray = null;
//			
//			// write information to newFile without remove records
//			boolean exceptionThrown = false;
//			try {
//				// 4 + 8 + 1 bytes for (length + checksum + recordType)
//				ByteBuffer startBuf = ByteBuffer.allocate(13);
//				while (true) {
//					startBuf.rewind();
//					try {
//						if (rmChannel.read(startBuf) == -1) {
//							break;
//						}
//					} catch (IOException e) {
//						exceptionThrown = true;
//						throw new TLogIOException(
//								"Exception while reading from channel", e);
//					}
//					startBuf.rewind();
//	
//					int recordLen = startBuf.getInt();
//					long checksum = startBuf.getLong();
//					byte recordType = startBuf.get();
//	
//					ByteBuffer readBuf = ByteBuffer.allocate(recordLen - 1); // -1 is for record type, readBuf contains the body(without type)
//					readBuf.rewind();
//	
//					try {
//						int numRead = rmChannel.read(readBuf);
//						if (numRead < (recordLen - 1)) {
//							throw new TLogIOException("Length of record is smaller than expected.");
//						}
//					} catch (IOException e) {
//						exceptionThrown = true;
//						throw new TLogIOException(
//								"Exception while reading from channel", e);
//					}
//	
//					// checksum-------
//					readBuf.rewind();
//					byte[] data = new byte[readBuf.capacity() + 1];
//					data[0] = recordType;
//					readBuf.get(data, 1, readBuf.capacity());
//					CRC32 crc = new CRC32();
//					crc.update(data);
//					long check = crc.getValue();
//					if (check != checksum) {
//						SimpleLogger.trace(Severity.ERROR, LOCATION, "ASJ.trans.000004", "Invalid checksum in resource manager recovery record. The record will be skipped.");
//						continue;
//					}
//					// -------------end checksum
//	
//					readBuf.rewind();
//					RMRecord rec = FSTLogReaderWriter.getRMRecord(recordType);
//					if (rec.getClass() == RMEntryRecord.class) {
//						((RMEntryRecord) rec).read(readBuf, false);
//						tempRMEntries.put(rec.getRMID(), ((RMEntryRecord)rec));
//						
//					} else {
//						rec.read(readBuf);
//						tempRMRemoveRecords.add(((RMRemoveRecord)rec));
//					}
//				}
//			} finally {
//				try {
//					rmChannel.close();
//				} catch (IOException e) {
//					if (!exceptionThrown) {
//						throw new TLogIOException("Cannot close rm channel while rotating.", e);
//					}
//				}
//			}
//			
//			rotateFiles(newChannel, tempRMEntries, tempRMRemoveRecords,
//					rmidsInTxRecords);
//			
//			
//		} else {
			try {
				rmChannel.close();
			} catch (IOException e) {
				throw new TLogIOException("Cannot close rm channel while rotating.", e);
			}
			Map<Long, ByteBuffer> txRecords = tLogOptimizator.getActiveRecords();
			int[] associatedRMIDs = FSTLogReaderWriter.getAssociatedRMids(txRecords);
			Set<Integer> setRMids = new HashSet<Integer>();
			for (int i = 0; i < associatedRMIDs.length; i++) {
				setRMids.add(associatedRMIDs[i]);
			}
			rotateFiles(newChannel, rmEntries, rmRemoveRecords, setRMids);
//		}
		
		// rename new file to old name
		try {
			newChannel.close();
		} catch (IOException e) {
			throw new TLogIOException("cannot close new channel in order to rename new file to old one name", e);
		}

		// delete old file
		if(!rmEntryFile.delete()) {
			throw new TLogIOException("Cannot delete old rm file while rotating.");
		}

		newFile.renameTo(rmEntryFile);
		try {
			rmChannel = new FileOutputStream(rmEntryFile, true).getChannel();
		} catch (FileNotFoundException e) {
			throw new TLogIOException(
					"File "	+ newFile.getAbsolutePath()
					+ " cannot be found(while rotating) after old one is deleted and new is renamed.",
					e);
		}

		if (readFromFile) {
			rmEntries = tempRMEntries;
			rmRemoveRecords = tempRMRemoveRecords;	
		}
	}

	private void rotateFiles(FileChannel newChannel,
			Map<Integer, RMEntryRecord> tempRMEntries,
			Set<RMRemoveRecord> tempRMRemoveRecords,
			Set<Integer> rmidsInTxRecords) throws TLogIOException {
		Iterator<RMRemoveRecord> removeRecordsIt = tempRMRemoveRecords.iterator();
		Set<RMRemoveRecord> remRecords = new HashSet<RMRemoveRecord>();
		while(removeRecordsIt.hasNext()) {
			RMRemoveRecord next = removeRecordsIt.next();
			int removeRecordRMID = next.getRMID();
			// if this removeRecordRMID is not in any transaction record
			// we can remove entry record for it
			if(!rmidsInTxRecords.contains(removeRecordRMID)) {
				tempRMEntries.remove(removeRecordRMID);
			} else {
				remRecords.add(next);
			}
		}
		
		//write tempRMEntries into new file
		writeRMEntries(tempRMEntries, remRecords, newChannel);
	}

	private void writeRMEntries(Map<Integer, RMEntryRecord> leftRMEntries, Set<RMRemoveRecord> removeRercords,
			FileChannel newChannel) throws TLogIOException {
		Iterator<RMEntryRecord> it = null;
		Iterator<RMRemoveRecord> iter = null;
		if(leftRMEntries == null) {
			it = rmEntries.values().iterator();
		} else {
			it = leftRMEntries.values().iterator();
		}
		while(it.hasNext()) {
			writeIntoFile(it.next(), false, newChannel);
		}
		
		if(removeRercords == null) {
			iter = rmRemoveRecords.iterator();
		} else {
			iter = removeRercords.iterator();
		}
		while (iter.hasNext()) {
			writeIntoFile(iter.next(), false, newChannel);
		}
	}

	private void initializeRMLogFile() throws FileNotFoundException {
		String rmFileName = workingDirectory + File.separator + tLogVersion.toString() + RM_FILE_NAME_SUFFIX + LOG_FILE_EXTENSION; 
		
		rmEntryFile = new File(rmFileName);

		rmChannel = new FileOutputStream(rmEntryFile, true).getChannel();
	}

	private void initializeClLogFile() throws FileNotFoundException {
		String clFileName = workingDirectory + File.separator + tLogVersion.toString() + CLASSIFIER_FILE_NAME_SUFFIX + LOG_FILE_EXTENSION;
		
		clEntryFile = new File(clFileName);

		clChannel = new FileOutputStream(clEntryFile, true).getChannel();
	}
	
	public void close() throws TLogIOException {
		if (isClosed) {
			throw new TLogIOException("This tlog is already closed.");
		}
		
		IOException ex = null;
		try {
			if (tLogOptimizator!=null) {
				tLogOptimizator.close();
			}
		} catch (TLogIOException e) {
			ex = new IOException(e.getMessage());
		}
		try {
			clChannel.close();
		} catch (IOException e) {
			ex = e;
		}
		try {
			rmChannel.close();
		} catch (IOException e) {
			ex = e;
		}
		if(ex != null) {
			throw new TLogIOException("Channel cannot be closed", ex);
		}

		try {
			Iterator<TransactionRecord> tmp = getAllTransactionRecords();
			if(tmp.hasNext()) {	
				return;
			}

			boolean clFileDeleted = false;
			boolean txFileDeleted = false;
			boolean rmFileDeleted = false;
			RuntimeException runEx = null;
			try {
				txFileDeleted = tLogOptimizator.deleteLastUsedFile();
			} catch (RuntimeException e) {
				runEx = e;
			}
			try {
				clFileDeleted = clEntryFile.delete();
			} catch (RuntimeException e) {
				runEx = e;
			}
			try {
				rmFileDeleted = rmEntryFile.delete();
			} catch (RuntimeException e) {
				runEx = e;
			} 
			if(runEx != null || !clFileDeleted || !rmFileDeleted || !txFileDeleted) {
				throw new TLogIOException("File(s) cannot be deleted: " +
						(!clFileDeleted ? "transaction classifiers " : "") +
						(!rmFileDeleted ? "resource managers " : "") +
						(!txFileDeleted ? "transaction records " : "")
						, runEx);
			}
		} finally {
			try {
				tlogLock.unlockTLog(tLogVersion.toString());
				isClosed = true;
			} catch (TLogLockingInfrastructureException e) {
				throw new TLogIOException("Error during locking", e);
			}
		}
	}

	/**
	 * Stores specified classifier into Tlog if it was not stored before.
	 * 
	 * @param classifier the classifier of the transaction which will be stored
	 * @return the ID of this classifier. This ID must be positive integer if provided string is not null and not empty.
	 * @throws TLogFullException when max number of different transaction classifiers is reached. 
	 */
	public int getIdForTxClassifier(String classifier) throws TLogIOException, TLogFullException {
		if (isClosed) {
			throw new TLogIOException("This tlog is already closed.");
		}
		if (classifier == null || classifier.length() == 0) {
			return 0;
		}

		int id = classifiers.getId(classifier);
		if (-1!=id) {
			return id;
		}

		synchronized (classifiers) {
			id = classifiers.add(classifier);
			if(id == -1) { // id = -1 means that id is assigned to classifier and it is in map
				return classifiers.getId(classifier);
			} else {
				// store id and classifier into file
				ByteBuffer clBuf = ByteBuffer.allocate(1024);
				int start = clBuf.position();
				clBuf.putInt(0); // reserve 4 bytes for length
				clBuf.putLong(0); // reserve 8 bytes for checksum of the body
				clBuf.put(CLASSIFIER_ENTRY_RECORD_TYPE);
				clBuf.putInt(id); // put transaction id - counter of classifiers
				clBuf.putInt(classifier.getBytes().length); // put length of byte[] which represents this transaction classifier   
				clBuf.put(classifier.getBytes()); // put classifier name
				int end = clBuf.position();
				int dataLength = end - start;
				int bodyLength = dataLength - 4 - 8;		
				clBuf.putInt(start, bodyLength); // put length
				
				byte[] data = new byte[dataLength]; 
				clBuf.position(start);
				clBuf.get(data, 0, dataLength);
				long checksum = 0L;
				CRC32 crc32 = new CRC32();
				crc32.update(data, 4 + 8, data.length - 4 - 8);
				checksum = crc32.getValue();
				clBuf.putLong(start + 4, checksum); // put crc32
				
				try {
					clBuf.flip();
					clChannel.write(clBuf);
					clChannel.force(true);
				} catch (IOException e) {
					throw new TLogIOException("Exception while reading from channel");
				}

				return id;
			}
		}
	}
	
	public String getTxClassifierById(int id) throws TLogIOException, InvalidClassifierIDException {
		if (isClosed) {
			throw new TLogIOException("This tlog is already closed.");
		}
		if(id <= 0) {
			throw new InvalidClassifierIDException("Transaction id = " + id + " is not valid. It must be positive.");
		}
		if (classifiers.isEmpty()) {
			throw new InvalidClassifierIDException("There is no such id=" + id + " stored in TLog.");
		}
		String classifier = classifiers.getClassifier(id);
		if(classifier == null) {
			throw new InvalidClassifierIDException("There is no such id=" + id + " stored in TLog.");
		} 
		return classifier;
	}
	
	public Iterator<TransactionRecord> getAllTransactionRecords() throws TLogIOException {
		if (isClosed) {
			throw new TLogIOException("This tlog is already closed.");
		}

		Map<Long, ByteBuffer> actRecs = tLogOptimizator.getActiveRecords();
		List<TransactionRecord> result = new ArrayList<TransactionRecord>(actRecs.size());

		for (ByteBuffer buf : actRecs.values()) {
			result.add(TXEntryRecord.readRecord(buf));
		}

		return result.iterator();
	}

	public int[] getAllUsedRMIDs() throws TLogIOException {
		if (isClosed) {
			throw new TLogIOException("This tlog is already closed.");
		}
//		int[] associatedRMIDs = FSTLogReaderWriter.getAssociatedRMids(tLogOptimizator.getActiveRecords());
//		
//		Set<Integer> resultSet = new HashSet<Integer>();
//		Iterator<Integer> it = rmEntries.keySet().iterator();
//		while (it.hasNext()) {
//			resultSet.add(it.next());
//		}
//		Iterator<RMRemoveRecord> iter = rmRemoveRecords.iterator();
//		while (iter.hasNext()) {
//			resultSet.remove(iter.next().getRMID());
//		}
//		for (int i = 0; i < associatedRMIDs.length; i++) {
//			resultSet.add(associatedRMIDs[i]);
//		}
//		
//		int[] result = new int[resultSet.size()];
//		int count = 0;
//		it = resultSet.iterator();
//		while(it.hasNext()) {
//			result[count++] = it.next();
//		}
//		Arrays.sort(result);
//		return result;

		return rmIDs;
	}

	public int getRMIDByName(String keyName) throws TLogIOException, InvalidRMKeyException {
		if (isClosed) {
			throw new TLogIOException("This tlog is already closed.");
		}
		if (keyName == null || keyName.length() == 0) {
			throw new InvalidRMKeyException("The given resource manager name is null.");
		}
		
		Map<Integer, RMEntryRecord> map = getUsedRMIDsMap(true);
		Iterator<Entry<Integer, RMEntryRecord>> it = map.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Integer, RMEntryRecord> next = it.next();
			RMEntryRecord rec = null;
			if(next.getValue().getClass() == RMEntryRecord.class) {
				rec = (RMEntryRecord)next.getValue();
			}
			if(rec != null && new String(rec.getName()).equals(keyName)) {
				return next.getKey();
			}
		}
		throw new InvalidRMKeyException("There is no RM with such name = " + keyName);
	}
	
	public RMPropsExtension getRMProperties(int rmID) throws TLogIOException, InvalidRMKeyException {
		return getRMProperties(rmID, true);
	}
	
	public RMPropsExtension getRMProperties(int rmID, boolean returnDecrypted) throws TLogIOException, InvalidRMKeyException {
		if (isClosed) {
			throw new TLogIOException("This tlog is already closed.");
		}
		if(rmID <= 0) {
			throw new InvalidRMKeyException("RM ID = " + rmID + " is not supported. It must be positive");
		}
		if(Arrays.binarySearch(rmIDs, rmID) < 0) {
			throw new InvalidRMKeyException("No rm with such id = " + rmID);
		}

		RMEntryRecord rec = null;
		RMRecord recc = rmEntries.get(rmID);
		if(recc != null && recc.getClass() == RMEntryRecord.class) {
			RMEntryRecord r = null;
			try {
				r = ((RMEntryRecord) recc).clone();
			} catch (CloneNotSupportedException e) {
				throw new TLogIOException(e);
			}
			if(returnDecrypted) {
				r.setSecure(r.decrypt().getSecureProperties());	
			}
			rec = r;
		}
		if(rec == null) {
			synchronized (rmChangeLock) {
				rec = rmEntries.get(rmID);
				if(rec == null) {
					// read from file if there is such record
					rec = getRMRecordFromFile(rmID);
					if(rec == null) {
						throw new InvalidRMKeyException("No rm with such id = " + rmID);
					}
				}
			}
		}
		RMPropsExtension props = new RMPropsExtension();
		Properties nonSecure = new Properties();
		try {
			nonSecure.load(new ByteArrayInputStream(rec.getNonSecure()));
		} catch (IOException e) {
			throw new TLogIOException("Cannot load nonsecure properties", e);
		}
		props.setNonSecureProperties(nonSecure);
		props.setKeyName(new String(rec.getName()));
		props.setRmContainerName(new String(rec.getContainerName()));
		Properties secureProperties = new Properties();
		try {
			secureProperties.load(new ByteArrayInputStream(rec.getSecure()));
		} catch (IOException e) {
			throw new TLogIOException("Cannot load secure properties", e);
		}
		props.setSecureProperties(secureProperties);
		return props;
	}
	
	/**
	 * This method is used in getRMProperties in order to search in rm records file
	 * for record with given id
	 * @param rmID - id of record to be found
	 * @return rm entry record which is in file or null if there is no such record
	 * @throws TLogIOException
	 */
	private RMEntryRecord getRMRecordFromFile(int rmID) throws TLogIOException {
		try {
			ByteBuffer startBuf = ByteBuffer.allocate(13);	// 4 + 8 + 1 bytes for
															// length + checksum +
															// recordType
			while (true) {
				startBuf.rewind();
				try {
					if (rmChannel.read(startBuf) == -1) {
						break;
					}
				} catch (IOException e) {
					throw new TLogIOException(
							"Exception while reading from channel", e);
				}
				startBuf.rewind();

				int recordLen = startBuf.getInt();
				long checksum = startBuf.getLong();
				byte recordType = startBuf.get();

				ByteBuffer readBuf = ByteBuffer.allocate(recordLen - 1); // -1 is for record type, readBuf contains the body(without type)
				readBuf.rewind();

				try {
					int numRead = rmChannel.read(readBuf);
					if (numRead < (recordLen - 1)) {
						throw new TLogIOException("Length of record is smaller than expected.");
					}
				} catch (IOException e) {
					throw new TLogIOException(
							"Exception while reading from channel", e);
				}

				// checksum-------
				readBuf.rewind();
				byte[] data = new byte[readBuf.capacity() + 1];
				data[0] = recordType;
				readBuf.get(data, 1, readBuf.capacity());
				CRC32 crc = new CRC32();
				crc.update(data);
				long check = crc.getValue();
				if (check != checksum) {
					SimpleLogger.trace(Severity.ERROR, LOCATION, "ASJ.trans.000004", "Invalid checksum in resource manager recovery record. The record will be skipped.");
					continue;
				}
				// -------------end checksum

				readBuf.rewind();
				RMRecord rec = FSTLogReaderWriter.getRMRecord(recordType);
				if (rec.getClass() == RMEntryRecord.class) {
					((RMEntryRecord) rec).read(readBuf, false);
					if(rec.getRMID() == rmID) {
						RMEntryRecord tempRecord = ((RMEntryRecord)rec); 
						rmEntries.put(rmID, tempRecord);
						return tempRecord;
					}
				}
			}
		} finally {
			try {
				rmChannel.close();
			} catch (IOException e) {
				throw new TLogIOException("Cannot close rm channel while rotating.", e);
			}
		}
		
		return null;
	}

    public TLogVersion getTLogVersion(){
    	return tLogVersion;
    }
	
	public int registerNewRM(RMProps rmProps) throws TLogIOException, RMNameAlreadyInUseException {
		if (isClosed) {
			throw new TLogIOException("This tlog is already closed.");
		}
		
		if (	null == rmProps ||
				null == rmProps.getSecureProperties() ||
				null == rmProps.getNonSecureProperties() ||
				null == rmProps.getKeyName() ||
				null == rmProps.getRmContainerName()) {
			throw new IllegalArgumentException(
					"Input parameter rmProps and it's fields must not be null.");
		}
		
		synchronized (rmChangeLock) {
			Map<Integer, RMEntryRecord> map = getUsedRMIDsMap(true);
			
			Iterator<RMEntryRecord> it = map.values().iterator();
			while(it.hasNext()) {
				RMRecord next = it.next();
				if(next.getClass() == RMEntryRecord.class) {
					RMEntryRecord rec = (RMEntryRecord)next;
					if(new String(rec.getName()).equals(rmProps.getKeyName())) {
						throw new RMNameAlreadyInUseException("The name "
								+ rmProps.getKeyName() + " is already in use by other "
								+ "resource manager in this transaction log");
					}
				}
			}
			
			
			byte[] name = rmProps.getKeyName().getBytes();
			byte[] containerName = rmProps.getRmContainerName().getBytes();
			Properties nonSecure = rmProps.getNonSecureProperties();
			Properties secure = rmProps.getSecureProperties();

			int[] rmid = new int[rmIDs.length + 1];
			System.arraycopy(rmIDs, 0, rmid, 0, rmIDs.length);
			rmid[rmIDs.length] = ++countIds;
			rmIDs = rmid;
			
			RMEntryRecord value = new RMEntryRecord(countIds, name, containerName, secure, nonSecure);
			RMRecord rec = writeIntoFile(value, true);
			if(rec != null && rec.getClass() == RMEntryRecord.class) {
				rmEntries.put(countIds, ((RMEntryRecord)rec));
			} else {
				throw new TLogIOException("Cannot write resource manager record into file during registerNewRM().");
			}
			
			if(rmEntries.size() + rmRemoveRecords.size() >= DEFAULT_MAX_RM_ENTRIES_PER_FILE) {
				rotateRM();
			}
			
			return countIds;
		}
		
	}
	
	public void unregisterRM(int rmID) throws TLogIOException, InvalidRMIDException {
		if (isClosed) {
			throw new TLogIOException("This tlog is already closed.");
		}
		synchronized (rmChangeLock) {
			if(Arrays.binarySearch(getUsedRMIDsArray(false),rmID) < 0) {
				throw new InvalidRMIDException("No rm with such id = " + rmID);
			}
			
			try  {
				//countIds++;
				RMRemoveRecord value = new RMRemoveRecord(rmID);
				try {
					writeIntoFile(value, true); // write compensation record
					rmRemoveRecords.add(value);
				} catch (TLogIOException e) {
					LOCATION.traceThrowableT(Severity.INFO, "Cannot write rm record to file", e);
				}
			} finally {
				// Register listener to remove the ID
				RMListener rmListener = new RMListener(rmID);
				try{
					TransactionServiceFrame.getTimeoutManager().registerTimeoutListener(rmListener, TransactionServiceFrame.txTimeout * 2, 0);
				} catch (TimeOutIsStoppedException e){
					LOCATION.traceThrowableT(Severity.WARNING, "Resource manager with ID" +rmID+ " will be removed imediatlly because timeout manager is not available.", e);
					rmListener.timeout();
				}
			}
		}
	}

	/**
	 * Removes specified transaction record from TLog. immediately 
	 * Will be used during recovery and in all cases when logID is not known. 
	 * Physical record will be removed immediately. This method does not provide asynchronous deletion.
	 * It will be used also when transaction is prepared but commit of the first RM failed and TM decides
	 * to rollback the transaction. 
	 * 
	 * @param logID is the transaction sequence number
	 * @throws TLogIOException when unexpected IOException or SQLException occurred.
	 */
	public void removeTransactionRecordImmediately(long txSequenceNumber) throws TLogIOException {
		if (isClosed) {
			throw new TLogIOException("This tlog is already closed.");
		}
		tLogOptimizator.removeTransactionRecordImmediately(txSequenceNumber);
	}
	
	/**
	 * Removes specified transaction record from TLog. 
	 * For optimization purposes it is possible to remove physically this record later.
	 * 
	 * @param logID is the transaction sequence number
	 * @throws TLogIOException when unexpected IOException or SQLException occurred.
	 */
	public void removeTransactionRecordLazily(long txSequenceNumber) throws TLogIOException {
		if (isClosed) {
			throw new TLogIOException("This tlog is already closed.");
		}
		tLogOptimizator.removeTransactionRecordLazily(txSequenceNumber);
	}
	
	/**
	 * Method is used from TransactionManager to store a record for each transaction 
	 * which is successfully prepared.
	 * 
	 * @param transactionRecord record for one transaction which holds transaction name, global transactionID, 
	 * resource manager IDs and branch iterators.
	 * @throws TLogIOException when unexpected IOException or SQLException occurred.
	 * @throws InvalidRMIDException when some of the provided resource manager IDs are not valid.
	 */
	public void writeTransactionRecord(TransactionRecord transactionRecord)
		throws TLogIOException, InvalidRMIDException, InvalidTransactionClassifierID {
		if (isClosed) {
			throw new TLogIOException("This tlog is already closed.");
		}
		if(transactionRecord == null) {
			throw new IllegalArgumentException("Input cannot be null.");
		}

		int transactionClassifierID = transactionRecord.getTransactionClassifierID();
		if(transactionClassifierID != 0 && !classifiers.containsId(transactionClassifierID)) {
			throw new InvalidTransactionClassifierID("Invalid transaction classifier ID: " + transactionClassifierID);
		}

		int[] rmids = transactionRecord.getRMIDs();
		for (int i = 0; i < rmids.length; i++) {
			int id = rmids[i];
			if (Arrays.binarySearch(rmIDs, id) < 0) {
				throw new InvalidRMIDException("Resource manager ID " + id + " is not valid.");
			}
		}

		ByteBuffer buffer = TXEntryRecord.writeRecord(transactionRecord);
		tLogOptimizator.writeTransactionRecord(transactionRecord.getTransactionSequenceNumber(), buffer);
	}
	
	public void flushRemovedTransactionRecords() throws TLogIOException {
		if (isClosed) {
			throw new TLogIOException("This tlog is already closed.");
		}
		tLogOptimizator.flushRemovedTransactionRecords();
	}

	protected RMRecord writeIntoFile(RMRecord record, boolean encrypt)
			throws TLogIOException {
		return writeIntoFile(record, encrypt, rmChannel);
	}
	
	protected RMRecord writeIntoFile(RMRecord record, boolean encrypt, FileChannel channel)
			throws TLogIOException {
		try {
			RMRecord tempRec = null;
			ByteBuffer rmBuf = ByteBuffer.allocate(1024);
			if (record.getClass() != RMEntryRecord.class) {
				tempRec = record.write(rmBuf);
			} else {
				RMEntryRecord rec = ((RMEntryRecord) record).clone();
				if (encrypt) {
					rec.write(rmBuf);
				} else {
					rec.write(rmBuf, false);
				}
				tempRec = rec;
			}

			rmBuf.flip();

//			// registering in rmEntries
//			if (record.getClass() == RMEntryRecord.class) {
//				rmEntries.put(tempRec.getRMID(), tempRec);
//			}

			channel.write(rmBuf);
			channel.force(true);
			
			return tempRec;
		} catch (IOException e) {
			throw new TLogIOException("Error writing record " + record, e);
		} catch (CloneNotSupportedException e) {
			throw new TLogIOException(e);
		}
	}

	public void setMaxRMEntries(int value) {
		DEFAULT_MAX_RM_ENTRIES_PER_FILE = value;
	}

	public void setRMEntries(Map<Integer, RMEntryRecord> rmEntries) {
		this.rmEntries = rmEntries;
	}
	
	public Map<Integer, RMEntryRecord> getRMEntries() {
		return rmEntries;
	}

	/**
	 * Returns used rm entry records and rmids
	 * @param inTxRecords -
	 *            if it is true then rmids which are in transaction records are
	 *            counted
	 * @return map with rmids as keys and entry records as values 
	 */
	private Map<Integer, RMEntryRecord> getUsedRMIDsMap(boolean inTxRecords) {
		Map<Integer, RMEntryRecord> map = new HashMap<Integer, RMEntryRecord>();
		map.putAll(rmEntries);
		Iterator<RMRemoveRecord> iter = rmRemoveRecords.iterator();
		while (iter.hasNext()) {
			map.remove(iter.next().getRMID());
		}

		if(inTxRecords) {
			int[] rmids = FSTLogReaderWriter.getAssociatedRMids(tLogOptimizator.getActiveRecords());
			for (int i = 0; i < rmids.length; i++) {
				map.put(rmids[i], rmEntries.get(rmids[i]));
			}
		}

		return map;
	}
	
	/**
	 * Returns used rmids in array
	 * 
	 * @param inTxRecords -
	 *            if it is true then rmids which are in transaction records are
	 *            counted
	 * @return array of used rmids
	 */
	private int[] getUsedRMIDsArray(boolean inTxRecords) {
		Set<Integer> resultSet = getUsedRMIDsMap(inTxRecords).keySet();
		
		int[] result = new int[resultSet.size()];
		int count = 0;
		Iterator<Integer> it = resultSet.iterator();
		while(it.hasNext()) {
			result[count++] = it.next();
		}
		Arrays.sort(result);
		return result;
	}
	
	/**
	 * Timeout listener used to remove unregistered RM
	 * from the rmID array and from the database
	 */
	protected class RMListener  implements TimeoutListener {

		private final int rmIDToBeRemoved;
		private Object associatedObject = null;

		public RMListener(int rmID) {
			this.rmIDToBeRemoved = rmID;
		}

		public boolean check() {
			return true;
		}

		public void timeout() {
			synchronized (rmChangeLock) {
				int index = Arrays.binarySearch(rmIDs, rmIDToBeRemoved);
				if (index >= 0) {
					Map<Long, ByteBuffer> txRecords = tLogOptimizator
							.getActiveRecords();
					int[] associatedRMIDs = FSTLogReaderWriter
							.getAssociatedRMids(txRecords);
					if (Arrays.binarySearch(associatedRMIDs, rmIDToBeRemoved) < 0) {
						int[] newRmIds = new int[rmIDs.length - 1];
						System.arraycopy(rmIDs, 0, newRmIds, 0, index);
						System.arraycopy(rmIDs, index + 1, newRmIds, index,
								rmIDs.length - index - 1);
						rmIDs = newRmIds;
					}
				} else {
					if (LOCATION.beInfo()) {
						LOCATION.infoT(
								"Resource manager with ID {0} not found.",
								new Object[] { rmIDToBeRemoved });
					}
				}

				if (rmEntries.size() + rmRemoveRecords.size() >= DEFAULT_MAX_RM_ENTRIES_PER_FILE) {
					try {
						rotateRM();
					} catch (TLogIOException e) {
						LOCATION.traceThrowableT(Severity.INFO,
								"Cannot rotate file", e);
					}
				}
			}
		}

		public void associateObject(Object obj) {
			this.associatedObject = obj;
		}

		public Object getAssociateObject() {
			return this.associatedObject;
		}
	}
}