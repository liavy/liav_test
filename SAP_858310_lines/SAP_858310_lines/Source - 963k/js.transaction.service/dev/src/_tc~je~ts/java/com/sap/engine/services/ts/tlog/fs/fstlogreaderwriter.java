package com.sap.engine.services.ts.tlog.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

import com.sap.engine.frame.core.thread.execution.Executor;
import com.sap.engine.services.ts.facades.timer.TimeoutManager;
import com.sap.engine.services.ts.tlog.InboundTLog;
import com.sap.engine.services.ts.tlog.TLog;
import com.sap.engine.services.ts.tlog.TLogAlreadyExistException;
import com.sap.engine.services.ts.tlog.TLogFullException;
import com.sap.engine.services.ts.tlog.TLogIOException;
import com.sap.engine.services.ts.tlog.TLogReaderWriter;
import com.sap.engine.services.ts.tlog.util.TLogLocking;
import com.sap.engine.services.ts.tlog.util.TLogLockingException;
import com.sap.engine.services.ts.tlog.util.TLogLockingInfrastructureException;
import com.sap.engine.services.ts.utils.TLogVersion;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

public class FSTLogReaderWriter implements TLogReaderWriter {
	
	File workingDirectory;
	protected final int bufferCapacity;
	protected long maxTLogFileSize;

	protected final TLogLocking tlogLock;
	protected final Executor executor;
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.ts.tlog.fs.FSTLogReaderWriter");

	/**
	 * 
	 * @param workingDirectory
	 * @param tlogLock
	 * @param timeoutManager
	 *            it must be null for productive use and some fake
	 *            implementation for testing purposes
	 * @param executor
	 *            it must be real executor the FSTLogReaderWriter is
	 *            instantiated by the transaction service and null for test
	 *            purposes. When it's null the tread will be created with
	 *            "new Thread(...)"
	 * @param bufferCapacity
	 * @param maxTLogFileSize
	 */
	public FSTLogReaderWriter(File workingDirectory, TLogLocking tlogLock,
			Executor executor, int bufferCapacity, long maxTLogFileSize) {
		this.workingDirectory = workingDirectory;
		this.tlogLock = tlogLock;
		this.executor = executor;
		this.bufferCapacity = bufferCapacity;
		this.maxTLogFileSize = maxTLogFileSize;
	}

	public TLog createNewTLog(byte tLogVersion[]) throws TLogIOException, TLogAlreadyExistException {
		TLogVersion ver = new TLogVersion(tLogVersion);
		File files[] = workingDirectory.listFiles(
			new FilenameFilter	() {
				public boolean accept(File dir, String filename) {
					return filename.endsWith(FSTLog.LOG_FILE_EXTENSION);
				}
			}
		);
		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			if (name.startsWith(ver.toString())) {
				throw new TLogAlreadyExistException("Tlog with given tlogVersion already exist");
			}
		}

		try {
			tlogLock.lockTLog(ver.toString());
		} catch (TLogLockingException e) {
			throw new TLogIOException("Locking error occurred", e);
		} catch (TLogLockingInfrastructureException e) {
			throw new TLogIOException("Infrastrucure exception", e);
		}

		String tx = FSTLog.TX_FILE_NAME_SUFFIX + FSTLog.LOG_FILE_EXTENSION;
		File tx0File = new File(workingDirectory + File.separator + ver + "_0" + tx);
		File tx1File = new File(workingDirectory + File.separator + ver + "_1" + tx);
		File txFiles[] = new File[] {tx0File, tx1File};

		try {
			return new FSTLog(ver, workingDirectory, tlogLock,
					executor, bufferCapacity, maxTLogFileSize, txFiles);
		} catch (FileNotFoundException e) {
			try {
				tlogLock.unlockTLog(ver.toString());
			} catch (TLogLockingInfrastructureException e1) {
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "ASJ.trans.000312", "Infrastrucure exception", e1);
			}
			throw new TLogIOException("File not found", e);
		}
	}

	public TLog lockOrphanedTLog() throws TLogIOException {
		// search for unlocked TLog
		TLogVersion tLogVer = null;
		File files[] = workingDirectory.listFiles(
			new FilenameFilter	() {
				public boolean accept(File dir, String filename) {
					return filename.endsWith(FSTLog.LOG_FILE_EXTENSION);
				}
			}
		);
		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			if (!isTLogFile(name)) {
				continue;	// this is not TLog file
			}

			TLogVersion ver = new TLogVersion(name.substring(0, name.indexOf("_")));
			if (0 == ver.getTmStartupTime()) {
				continue;	// ensure this is not inbound TLog file
			}

			try {
				tlogLock.lockTLog(ver.toString());
			} catch (TLogLockingException e) {
				//$JL-EXC$
				continue;
			} catch (TLogLockingInfrastructureException e) {
				throw new TLogIOException("Locking error occurred", e);
			}

			tLogVer = ver;
			break;
		}

		if (null==tLogVer) {
			// there is no orphaned TLog
			return null;
		}

		try {
			return initializeTLog(tLogVer, false);
		} catch (TLogIOException e) {
			try {
				tlogLock.unlockTLog(tLogVer.toString());
			} catch (TLogLockingInfrastructureException e1) {
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, e1, "ASJ.trans.000002", "Couldn't release lock: {0}", new Object[]{ tLogVer.toString()});
			}
			throw e;
		}
	}

	private TLog initializeTLog(TLogVersion tLogVer, boolean isInboundTLog) throws TLogIOException {
		// "_TX.tlog";
		String tx = FSTLog.TX_FILE_NAME_SUFFIX + FSTLog.LOG_FILE_EXTENSION;
		File tx0File = new File(workingDirectory + File.separator + tLogVer + "_0" + tx);
		File tx1File = new File(workingDirectory + File.separator + tLogVer + "_1" + tx);
		File txFiles[] = new File[] {tx0File, tx1File};

		// "_RM.tlog";
		String rm = workingDirectory + File.separator + tLogVer
				+ FSTLog.RM_FILE_NAME_SUFFIX + FSTLog.LOG_FILE_EXTENSION;
		File rmFile = new File(rm);

		// "_TXClassifier.tlog";
		String cl = workingDirectory + File.separator + tLogVer
				+ FSTLog.CLASSIFIER_FILE_NAME_SUFFIX
				+ FSTLog.LOG_FILE_EXTENSION;
		File clFile = new File(cl);

		if (!allFilesExist(tx0File, tx1File, rmFile, clFile, tLogVer)) {
			throw new TLogIOException(
					"There are missing TLog files for TLog with version: "
					+ tLogVer.toString());
		}

		// reading file.... and update information
		ConcurrentHashMap<Long, ByteBuffer> activeRecords = new ConcurrentHashMap<Long, ByteBuffer>();
		int initialTLogFileSize = readTXLogFiles(txFiles, activeRecords);
		int currentFile;
		if (tx0File.exists()) {
			currentFile = 0;
		} else {
			currentFile = 1;
		}

		Map<Integer, RMEntryRecord> rmEntries = new HashMap<Integer, RMEntryRecord>();
		Set<RMRemoveRecord> rmRemoveRecords = new HashSet<RMRemoveRecord>();
		int[] rmIDs = readRMLogFile(rmFile, rmEntries, rmRemoveRecords, activeRecords);

		ClassifiersMap classifiers = new ClassifiersMap();
		readTCLogFile(clFile, classifiers);

		try {
			if (!isInboundTLog) {
				return new FSTLog(tLogVer, workingDirectory, tlogLock,
						executor, activeRecords, classifiers,
						rmEntries, rmRemoveRecords, rmIDs,
						0 == rmIDs.length ? 1 : rmIDs[rmIDs.length - 1] + 1,
						bufferCapacity, maxTLogFileSize, txFiles,
						initialTLogFileSize, currentFile);
			} else {
				return new InboundFSTLog(tLogVer, workingDirectory, tlogLock,
						executor, activeRecords, classifiers,
						rmEntries, rmRemoveRecords, rmIDs,
						0 == rmIDs.length ? 1 : rmIDs[rmIDs.length - 1] + 1,
						bufferCapacity, maxTLogFileSize, txFiles,
						initialTLogFileSize, currentFile);

			}
		} catch (FileNotFoundException e) {
			throw new TLogIOException("Cannot open FS TLog", e);
		}
	}

	/**
	 * @param recordType
	 * @throws TLogIOException
	 */
	static RMRecord getRMRecord(byte recordType) throws TLogIOException {
		if(recordType == RMRecord.RM_ENTRY_RECORD_TYPE) {
			return new RMEntryRecord();
		} else if(recordType == RMRecord.RM_REMOVE_RECORD_TYPE) {
			return new RMRemoveRecord();
		} else {
			throw new TLogIOException("IO Exception");
		}
	}

	private boolean isTLogFile(String name) {
		if (name.endsWith(FSTLog.TX_FILE_NAME_SUFFIX + FSTLog.LOG_FILE_EXTENSION))
			return true;
		if (name.endsWith(FSTLog.RM_FILE_NAME_SUFFIX + FSTLog.LOG_FILE_EXTENSION))
			return true;
		return name.endsWith(FSTLog.CLASSIFIER_FILE_NAME_SUFFIX + FSTLog.LOG_FILE_EXTENSION);
	}


	public InboundTLog getInboundTLog(TLogVersion ver) throws TLogIOException {
		if (null==ver || ver.getTmStartupTime() != 0) {
			throw new IllegalArgumentException(
					"Input parameter TLogVersion must be not null.");
		}
		
		File files[] = workingDirectory.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.endsWith(FSTLog.LOG_FILE_EXTENSION);
			}
		});

		InboundTLog resultLog = null;
		
		try {
			tlogLock.lockTLog(ver.toString());
		} catch (TLogLockingException e) {
			// the caller must ensure that no one else has
			// locked this TLog so this is runtime error
			throw new IllegalStateException("Internal error.", e);
		} catch (TLogLockingInfrastructureException e) {
			throw new TLogIOException("Locking error occurred", e);
		}

		try {
			// check if TLog exists
			for (int i = 0; i < files.length; i++) {
				String name = files[i].getName();
				if (name.startsWith(ver.toString())) {
					// TLog exists => get it
					resultLog = (InboundTLog)initializeTLog(ver, true);
					break;
				}
			}
	
			if (null==resultLog) {
				// TLog don't exist
				try {
					String tx = FSTLog.TX_FILE_NAME_SUFFIX + FSTLog.LOG_FILE_EXTENSION;
					File tx0File = new File(workingDirectory + File.separator + ver + "_0" + tx);
					File tx1File = new File(workingDirectory + File.separator + ver + "_1" + tx);
					File txFiles[] = new File[] {tx0File, tx1File};
	
					resultLog = new InboundFSTLog(ver, workingDirectory, tlogLock,
							executor, bufferCapacity, maxTLogFileSize, txFiles);
				} catch (FileNotFoundException e) {
					throw new TLogIOException("File not found", e);
				}
			}
		} catch (TLogIOException e) {
			try {
				tlogLock.unlockTLog(ver.toString());
			} catch (TLogLockingInfrastructureException e1) {
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "ASJ.trans.000311", "Locking error occurred.", e1);
			}
			throw e;
		}
		
		return resultLog;
	}


	private int readTXLogFiles(File txFiles[], Map<Long, ByteBuffer> txRecords) throws TLogIOException {
		File f0 = txFiles[0];
		File f1 = txFiles[1];

		if (f0.exists() && !(f1.exists())) {
			return readTXTLogFile(txFiles[0], txRecords);

		} else if (f1.exists() && !(f0.exists())) {
			return readTXTLogFile(txFiles[1], txRecords);

		} else if (f0.exists() && f1.exists()) {
			// if you have two files then first read the older one and
			// then the newer one.
			int firstFile = (f0.lastModified() < f1.lastModified()) ? 0 : 1;
			int secondFile = (1 + firstFile) & 1;
			readTXTLogFile(txFiles[firstFile], txRecords);
			readTXTLogFile(txFiles[secondFile], txRecords);

			// write transaction records into temporary file
			int initialFileSize = 0;
			ByteBuffer[] actRecs = new ByteBuffer[txRecords.size()];
			int curPos = 0;
			for (ByteBuffer buf : txRecords.values()) {
				buf.position(buf.limit());
				buf.flip();
				actRecs[curPos++] = buf;

				// update the excepted file size
				initialFileSize += buf.limit();
			}

			File tmpFile = new File("txRecordsTempFile");
			// just in case
			tmpFile.delete();

			try {
				FileChannel currentFile = new FileOutputStream(tmpFile).getChannel();
				// write the transaction records
				currentFile.write(actRecs);
				// force the file no matter what
				currentFile.force(true);
				currentFile.close();
			} catch (IOException e) {
				throw new TLogIOException("Unexpected IO exception occurred.", e);
			}

			// delete the oldest file
			if (!txFiles[firstFile].delete()) {
				throw new TLogIOException("Transaction log file cannot be deleted.");
			}

			// rename temporary file to transaction records file
			if (!tmpFile.renameTo(txFiles[firstFile])) {
				throw new TLogIOException("Transaction log file cannot be renamed.");
			}

			// delete the second file
			if (!txFiles[secondFile].delete()) {
				throw new TLogIOException("Transaction log file cannot be deleted.");
			}

			return initialFileSize;

		} else {
			throw new TLogIOException("The tlog transactions file is missing.");
		}
	}

	/**
	 * 
	 * @param fileName
	 *            the file which we want to read from
	 * @param txRecords
	 *            a map which the method will update according to the file
	 *            content
	 * @return the position in the file at which the read stops, e.g. the size
	 *         of the file
	 * @throws TLogIOException
	 *             in case of IOException or other problems
	 */
	private static int readTXTLogFile(File txFile,
			Map<Long, ByteBuffer> txRecords) throws TLogIOException {

		FileChannel channel;
		try {
			channel = new RandomAccessFile(txFile, "rw").getChannel();
		} catch (FileNotFoundException e) {
			throw new TLogIOException("Cannot open file for reading", e);
		}

		boolean exceptionThrown = false;
		int initialTLogFileSize = 0;

		try {
			ByteBuffer beginBuf = ByteBuffer.allocate(4);
			while (true) {
				beginBuf.rewind();
				try {
					if (channel.read(beginBuf) == -1) {
						break;
					}
				} catch (IOException e) {
					exceptionThrown = true;
					throw new TLogIOException(
							"Exception while reading from channel", e);
				}

				beginBuf.rewind();

				int recordLen = beginBuf.getInt();

				int fullRecordSize = 4 + 8 + recordLen;
				initialTLogFileSize += fullRecordSize;
				ByteBuffer readBuf = ByteBuffer.allocate(fullRecordSize);
				readBuf.rewind();
				readBuf.putInt(recordLen);
				try {
					int numRead = channel.read(readBuf);
					if (numRead < (8 + recordLen)) {
						// incomplete record  -- may be exception can be thrown or something should be logged
						int numMissingBytes = (8 + recordLen) - numRead;
						writeZeroesForRecordCompletition(channel, numMissingBytes);
						break;
					}
				} catch (IOException e) {
					exceptionThrown = true;
					throw new TLogIOException(
							"Exception while reading from channel", e);
				}

				// checksum
				readBuf.rewind();
				// skip the record length
				readBuf.getInt();
				long checksum = readBuf.getLong();

				byte[] data = new byte[fullRecordSize - 8 - 4];
				readBuf.get(data);
				CRC32 crc = new CRC32();
				crc.update(data);
				long check = crc.getValue();
				if (check != checksum) {
					SimpleLogger.trace(Severity.ERROR, LOCATION, "ASJ.trans.000003", "Invalid checksum in transaction recovery record. The record will be skipped.");
					continue;
				}
				// -------------end checksum

				byte recordType = data[0];
				if (	recordType == TXRecord.TX_ENTRY_RECORD_TYPE ||
						recordType == TXRecord.INBOUND_TX_ENTRY_RECORD_TYPE) {
					long txSeqNum = readBuf.getLong(TXEntryRecord.SEQUENCE_NUMBER_INDEX);
					readBuf.limit(fullRecordSize);
					txRecords.put(txSeqNum, readBuf);

				} else if (recordType == TXRecord.TX_REMOVE_RECORD_TYPE) {
					long[] removedRecs = TXRemoveRecord.read(readBuf);
					for (long rec : removedRecs) {
						txRecords.remove(rec);
					}
				}
			}
		} finally {
			try {
				channel.close();
			} catch (IOException e2) {
				if (false == exceptionThrown) {
					throw new TLogIOException("Cannot close channel", e2);
				} else {
					SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "ASJ.trans.000313", "Cannot close channel", e2);
				}
			}
		}

		return initialTLogFileSize;
	}

	private static void writeZeroesForRecordCompletition(FileChannel channel,
			int numMissingBytes) throws IOException {

		byte[] bytes = new byte[numMissingBytes];
		Arrays.fill(bytes, (byte) 0);

		ByteBuffer fakeBufferForRecordFinishing = ByteBuffer
				.allocate(numMissingBytes);
		fakeBufferForRecordFinishing.put(bytes);
		fakeBufferForRecordFinishing.flip();

		channel.write(fakeBufferForRecordFinishing);
		channel.force(false);
	}

	private static int[] readRMLogFile(File rmFile,
			Map<Integer, RMEntryRecord> rmEntries,
			Set<RMRemoveRecord> rmRemoveRecords,
			Map<Long, ByteBuffer> txRecords)
			throws TLogIOException {

		FileChannel channel;
		try {
			channel = new RandomAccessFile(rmFile, "rw").getChannel();
		} catch (FileNotFoundException e) {
			throw new TLogIOException("Cannot open file for reading", e);
		}

		boolean exceptionThrown = false;
		try {
			ByteBuffer startBuf = ByteBuffer.allocate(13);	// 4 + 8 + 1 bytes for
															// length + checksum +
															// recordType
			while (true) {
				startBuf.rewind();
				try {
					if (channel.read(startBuf) == -1) {
						break;
					}
				} catch (IOException e) {
					exceptionThrown = true;
					throw new TLogIOException(
							"Exception while reading from channel", e);
				}
				startBuf.rewind();

				int recordLen = startBuf.getInt();
				long checksum = startBuf.getLong();
				byte recordType = startBuf.get();

				ByteBuffer readBuf = ByteBuffer.allocate(recordLen - 1);// -1 is for record type
				readBuf.rewind();

				try {
					int numRead = channel.read(readBuf);
					if (numRead < (recordLen - 1)) {// incomplete record
						writeZeroesForRecordCompletition(channel, (recordLen - 1) - numRead);
						break;
					}
				} catch (IOException e) {
					exceptionThrown = true;
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
				RMRecord rec = getRMRecord(recordType);
				if (rec.getClass() == RMEntryRecord.class) {
					((RMEntryRecord) rec).read(readBuf, false);
				} else {
					rec.read(readBuf);
				}
				
				//count++;
				//rmEntries.put(rec.getRMID(), rec);
				if (rec.getClass() == RMRemoveRecord.class) {
					rmRemoveRecords.add(((RMRemoveRecord)rec));
				}
				else if (rec.getClass() == RMEntryRecord.class) {
					rmEntries.put(rec.getRMID(), ((RMEntryRecord)rec));
				}
			}
		} finally {
			try {
				channel.close();
			} catch (IOException e2) {
				if (false == exceptionThrown) {
					throw new TLogIOException("Cannot close channel", e2);
				}
			}
		}
		
		Set<Integer> rmidsInTxRecords = new HashSet<Integer>();
		int[] rmidsInTxArray = FSTLogReaderWriter.getAssociatedRMids(txRecords);
		for (int i = 0; i < rmidsInTxArray.length; i++) {
			rmidsInTxRecords.add(rmidsInTxArray[i]);
		}
		
		Iterator<RMRemoveRecord> removeRecordsIt = rmRemoveRecords.iterator();
		while(removeRecordsIt.hasNext()) {
			int removeRecordRMID = removeRecordsIt.next().getRMID();
			// if this removeRecordRMID is not in any transaction record
			// we can remove entry record for it
			if(!rmidsInTxRecords.contains(removeRecordRMID)) {
				rmEntries.remove(removeRecordRMID);
			}
		}
		
		Integer[] rmIDsObj = (Integer[])rmEntries.keySet().toArray(new Integer[rmEntries.size()]);
		int[] rmIDs = new int[rmEntries.size()];
		for (int i = 0; i < rmIDsObj.length; i++) {
			rmIDs[i] = rmIDsObj[i];
		}
		
		Arrays.sort(rmIDs);
		return rmIDs;
	}

	private void readTCLogFile(File clFile, ClassifiersMap classifiers) throws TLogIOException {
		FileChannel channel;
		try {
			channel = new RandomAccessFile(clFile, "rw").getChannel();
		} catch (FileNotFoundException e) {
			throw new TLogIOException("Cannot open file for reading", e);
		}

		boolean exceptionThrown = false;
		try {
			// 4 + 8 + 1 bytes for length + checksum + type
			ByteBuffer startBuf = ByteBuffer.allocate(13);

			while (true) {
				startBuf.rewind();
				try {
					if (channel.read(startBuf) == -1) {
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

				ByteBuffer readBuf = ByteBuffer.allocate(recordLen - 1);
				readBuf.rewind();
				try {
					int numRead = channel.read(readBuf);
					if (numRead < recordLen - 1) {
						// incomplete record
						writeZeroesForRecordCompletition(channel, (recordLen - 1) - numRead);
						break;
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
					SimpleLogger.trace(Severity.ERROR, LOCATION, "ASJ.trans.000005", "Invalid checksum in transaction classifier recovery record. The record will be skipped.");
					continue;
				}
				// -------------end checksum

				readBuf.rewind();
				int id = readBuf.getInt();
				int classifierLen = readBuf.getInt();
				byte[] classifier = new byte[classifierLen];
				readBuf.get(classifier, 0, classifierLen);
				try {
					classifiers.add(new String(classifier), id);
				} catch (TLogFullException e) {
					throw new TLogIOException(
							"Error in reading classifier record", e);
				}
			}
		} finally {
			try {
				channel.close();
			} catch (IOException e2) {
				if (false == exceptionThrown) {
					throw new TLogIOException("Cannot close channel", e2);
				}
			}
		}
	}

	private boolean allFilesExist(File tx0File, File tx1File, File rmFile,
			File clFile, TLogVersion tLogVer) {

		if (   (tx0File.exists() || tx1File.exists()) &&
				clFile.exists()  &&
				rmFile.exists()  ) {
			// all is okay
			return true;
		}

		// some file don't exist ... delete all files and throw exception
		SimpleLogger.trace(Severity.WARNING, LOCATION,
				"ASJ.trans.000006", "Some file for TLog {0} doesn't exist. All existing file will be delete and TLog will be skipped",
				new Object[] { tLogVer.toString() });

		if (tx0File.exists()) {
			if (!tx0File.delete()) {
				SimpleLogger.trace(Severity.WARNING, LOCATION,
						"ASJ.trans.000007",
						"Cannot delete transaction log file");
			}
		}

		if (tx1File.exists()) {
			if (!tx1File.delete()) {
				SimpleLogger.trace(Severity.WARNING, LOCATION,
						"ASJ.trans.000007",
						"Cannot delete transaction log file");
			}
		}

		if (rmFile.exists()) {
			if (!rmFile.delete()) {
				SimpleLogger.trace(Severity.WARNING, LOCATION,
						"ASJ.trans.000008",
						"Cannot delete resource managers log file");
			}
		}

		if (clFile.exists()) {
			if (!clFile.delete()) {
				SimpleLogger.trace(Severity.WARNING, LOCATION,
						"ASJ.trans.000009",
						"Cannot delete classifiers log file");
			}
		}

		return false;
	}

	static int[] getAssociatedRMids(Map<Long, ByteBuffer> txRecords) {
		Set<Integer> rms = new TreeSet<Integer>();
		for (ByteBuffer b : txRecords.values()) {
			b.position(TXEntryRecord.RM_IDS_COUNT_INDEX);
			int c = b.getInt();
			for (int q = 0; q < c; ++q) {
				rms.add(b.getInt());
			}
		}
		int[] associatedRMids = new int[rms.size()];
		int counter = 0;
		for (Integer integ : rms) {
			associatedRMids[counter++] = integ;
		}
		return associatedRMids;
	}
}
