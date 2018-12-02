package com.sap.engine.services.ts.tlog.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.sap.engine.frame.core.thread.execution.Executor;
import com.sap.engine.services.ts.tlog.TLogIOException;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

public class FSTLogOptimizator {

	// constants
	/**
	 * The maximal number of empty cycles the flusherThread will do before it
	 * stops itself. Empty cycle is considered every cycle that we flush noting
	 * or just one transaction.
	 */
	private static final int MAX_NUMBER_EMPTY_CYCLES = 10;

	/**
	 * The maximal number of RM IDs that will be removed together lazily
	 */
	private static final int MAX_LAZILY_REMOVED_TX_ENTRIES = 1000;

	// synchronizers and other variables
	/**
	 * Used to have single thread that write/flush/rotate at a time
	 */
	private final AtomicInteger writeLock = new AtomicInteger(0);

	/**
	 * Lock for flush and modification of the buffer. Because the state of the
	 * flusherThread is dependent on the buffer state the state
	 * (stopped/started) of the flusherThread is also updated on this lock.
	 */
	private final Object flushOrModifyBufferLock = new Object();

	/**
	 * This lock is used by the flusherThread to wait other thread to write in
	 * the buffer. Other threads use it to notify the flusherThread if they are
	 * okay to flush at a given point or if the buffer is full.
	 */
	private final Object flusherWaitLock = new Object();

	/**
	 * true if there is flusherThread is started. This field is modified in
	 * flushOrModifyBufferLock because it's (flusherThread) state is related to
	 * the buffer state.
	 */
	private volatile boolean flusherThreadStarted = false;

	private final FSTLogBuffer buffer;

	/**
	 * The time that the flusherThread (if such) will wait between too flushes
	 * if it's not notified in advance
	 */
	private static volatile long waitBetweenFlushes = 25;

	/**
	 * Every Buffer will add it's size to this variable so when we are going to
	 * write to the file we will now if with the current buffer(s) it will
	 * exceed the maximal file size. All operations on this field must be
	 * synchronized on activeRecords.
	 */
	private long expectedFileSize = 0;

	/**
	 * The maximal file size
	 */
	private volatile long maxFileSize;

	/**
	 * Two files which TLog will swap with every file rotate
	 */
	private final File[] files;

	/**
	 * The current file index in the files array
	 */
	private int currentFileName;

	/**
	 * The file channel in which we currently write
	 */
	private FileChannel currentFile;

	/**
	 * Contains the current active records, e.g. which are not removed. All
	 * operations over this field, removedRecords and currentPosInRemovedRecords
	 * operations must be synchronized on this object.
	 */
	private final Map<Long, ByteBuffer> activeRecords;

	/**
	 * Holds the sequence numbers of the records that are prepared to remove but
	 * are still not removed. All operations on this field must be synchronized
	 * on activeRecords.
	 */
	private final long[] removedRecords = new long[MAX_LAZILY_REMOVED_TX_ENTRIES];

	/**
	 * The position on which we will add the next record in removedRecords. All
	 * operations on this field must be synchronized on activeRecords.
	 */
	private int curPosInRemovedRecords = 0;

	/**
	 * The number of elements in the buffer on which is consider okay to flush.
	 * minimal value = 1 maximal value = capacity
	 * 
	 * The new value is recalculated after each flush: currentNotifyPoint =
	 * currentFlushPoint + (currentFlushPoint-lastFlushPoint)
	 * 
	 * NOTE: all operation must be synchronized on flushOrModifyBufferLock
	 */
	private int currentNotifyPoint;

	/**
	 * The place on which we will add next time add is called
	 */
	private final AtomicInteger numberWritingThreads = new AtomicInteger(0);

	/**
	 * Used by flusherThread to report IO exceptions to the writing threads
	 *  
	 * NOTE: all changing operation must be synchronized on flushOrModifyBufferLock
	 */
	private volatile TLogIOException lastIOOperationException = null;

	/**
	 * Used to start the flusherThread. If it's null then the flusherThread is
	 * started with "new Thread(...)"
	 */
	private final Executor flusherThreadExecutor;

	// constructor(s)
	public FSTLogOptimizator(File[] files, long maxFileSize,
			Executor flusherThreadExecutor, int bufferCapacity)
			throws FileNotFoundException {
		this(files, maxFileSize, flusherThreadExecutor, bufferCapacity,
				new ConcurrentHashMap<Long, ByteBuffer>(bufferCapacity * 2), 0, 0);
	}

	public FSTLogOptimizator(File[] files, long maxFileSize,
			Executor flusherThreadExecutor, int bufferCapacity,
			ConcurrentHashMap<Long, ByteBuffer> activeRecords, long initialFileSize,
			int currentFileName) throws FileNotFoundException {

		this.files = files;
		this.currentFile = new FileOutputStream(this.files[currentFileName], true)
				.getChannel();

		this.expectedFileSize = initialFileSize;
		this.currentFileName = currentFileName;

		this.maxFileSize = maxFileSize;

		this.flusherThreadExecutor = flusherThreadExecutor;

		this.buffer = new FSTLogBuffer(bufferCapacity);
		this.activeRecords = activeRecords;
	}

	// public ("interface") method(s)
	public void close() throws TLogIOException {
		flushRemovedTransactionRecords();
		if (currentFile != null) {
			try {
				currentFile.close();
			} catch (IOException e) {
				throw new TLogIOException("Cannot close FSTLog optimizer.", e);
			}
		}
	}

	/**
	 * Must be called after close
	 */
	public boolean deleteLastUsedFile() {
		return files[currentFileName].delete();
	}

	public static void setWaitBetweenFlushes(long waitBetweenFlushes) {
		FSTLogOptimizator.waitBetweenFlushes = waitBetweenFlushes;
	}

	public void setMaxFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public long getMaxFileSize() {
		return this.maxFileSize;
	}

	public Map<Long, ByteBuffer> getActiveRecords() {
		return Collections.unmodifiableMap(activeRecords);
	}

	public void writeTransactionRecord(long txSeqNum, ByteBuffer rec) throws TLogIOException {
		numberWritingThreads.incrementAndGet();

		synchronized (activeRecords) {
			activeRecords.put(txSeqNum, rec);
			expectedFileSize += rec.limit();
		}

		write(rec, true);
	}

	public void removeTransactionRecordImmediately(long txRecSeqNum) throws TLogIOException {
		numberWritingThreads.incrementAndGet();

		ByteBuffer buffer;
		synchronized (activeRecords) {
			activeRecords.remove(txRecSeqNum);
			removedRecords[curPosInRemovedRecords++] = txRecSeqNum;

			buffer = TXRemoveRecord.writeRecord(removedRecords,
					curPosInRemovedRecords);

			expectedFileSize += buffer.limit();
			curPosInRemovedRecords = 0;
		}

		write(buffer, true);
	}

	public void removeTransactionRecordLazily(long txRecSeqNum) throws TLogIOException {
		boolean doWrite;
		ByteBuffer buffer = null;
		synchronized (activeRecords) {
			activeRecords.remove(txRecSeqNum);
			removedRecords[curPosInRemovedRecords++] = txRecSeqNum;

			doWrite = (curPosInRemovedRecords == MAX_LAZILY_REMOVED_TX_ENTRIES);

			if (doWrite) {
				numberWritingThreads.incrementAndGet();
				buffer = TXRemoveRecord.writeRecord(removedRecords,
						curPosInRemovedRecords);

				expectedFileSize += buffer.limit();
				curPosInRemovedRecords = 0;
			}
		}

		if (doWrite) {
			write(buffer, false);
		}
	}

	public void flushRemovedTransactionRecords() throws TLogIOException {
		ByteBuffer buffer;
		synchronized (activeRecords) {
			if(curPosInRemovedRecords==0) {
				return;
			} else {
				numberWritingThreads.incrementAndGet();
				buffer = TXRemoveRecord.writeRecord(removedRecords,
						curPosInRemovedRecords);

				expectedFileSize += buffer.limit();
				curPosInRemovedRecords = 0;
			}
		}
		write(buffer, true);
	}

	// helpful (private) method(s)
	private void write(ByteBuffer record, boolean waitForFlush)
			throws TLogIOException {
		// try to acquire the writeLock if the flusherThread is not started
		if ((!flusherThreadStarted) && (writeLock.compareAndSet(0, 1))) {
			// if write lock is available and the flusherThread is
			// not started write and force ourselves
			try {
				numberWritingThreads.decrementAndGet();
				writeOrRotate(waitForFlush, 1, record);
			} finally {
				// release the lock
				writeLock.set(0);
			}

		} else { // if write lock is not available or flusherThread is started
			// we add the record in queue and wait queue to be forced
			try {
				synchronized (flushOrModifyBufferLock) {
					while (!buffer.add(record)) {
						synchronized (flusherWaitLock) {
							flusherWaitLock.notifyAll();
						}
						flushOrModifyBufferLock.wait();
					}
					numberWritingThreads.decrementAndGet();

					// ensure there is a flusherThread running
					if (!flusherThreadStarted) {
						currentNotifyPoint = 2;

						if (null!=flusherThreadExecutor) {
							flusherThreadExecutor
									.executeMonitored(new FSTLogOptimizatorFlusher());
						} else {
							// for test purposes
							Thread flusher = new Thread(
									new FSTLogOptimizatorFlusher());
							flusher.setDaemon(true);
							flusher.start();
						}

						flusherThreadStarted = true;
					}

					if (waitForFlush) {
						// if there are no writing threads and
						// if we have reached a notify point notify the
						// flusherThread that it's okay to flush
						if ((numberWritingThreads.get() == 0)
								&& (currentNotifyPoint <= buffer.position())) {
							synchronized (flusherWaitLock) {
								flusherWaitLock.notifyAll();
							}
						}

						flushOrModifyBufferLock.wait();
					}
				}

				if (waitForFlush) {
					// only if we wait the actual write and flush we must
					// check the IO result set by the flusherThread
					if (null != lastIOOperationException) {
						throw lastIOOperationException;
					}
				}
			} catch (InterruptedException e) {
				String msg = "The write operation may not be flushed successfully because the current thread was interrupted.";
				SimpleLogger.traceThrowable(Severity.ERROR, FSTLog.LOCATION,e, "ASJ.trans.000080","The write operation may not be flushed successfully because the current thread was interrupted.");
				throw new TLogIOException(msg, e);
			}
		}
	}

	/**
	 * PRECONDITION the caller must hold the writeLock
	 * 
	 * @param force
	 *            if true we will force the record(s)
	 * @param records
	 *            the record(s) to be written
	 * @throws TLogIOException
	 *             if there are IO problems during IO operation(s)
	 */
	private void writeOrRotate(boolean force, int bufSize,
			ByteBuffer... records) throws TLogIOException {
		try {

			if (expectedFileSize > maxFileSize) {
				// rotate
				// 1) rotate is based on active records - we need its copy to
				// proceed with the rotate. NOTE: THIS CAN LEAD TO DOUBLED
				// ACTIVE RECORDS IN THE FILE WITH ONE COMPENSATION RECORDS
				ByteBuffer[] actRecs;
				synchronized (activeRecords) {
					// initialize the excepted file size
					expectedFileSize = 0;

					actRecs = new ByteBuffer[activeRecords.size()];
					int curPos = 0;
					for (ByteBuffer buf : activeRecords.values()) {
						buf.position(buf.limit());
						buf.flip();
						actRecs[curPos++] = buf;

						// update the excepted file size
						expectedFileSize += buf.limit();
					}

					// empty removed records
					curPosInRemovedRecords = 0;
				}

				// change the file channel
				currentFile.close();
				File oldFile = files[currentFileName];
				currentFileName = ((++currentFileName) % 2);
				currentFile = new FileOutputStream(files[currentFileName])
						.getChannel();

				// write the active records copy
				currentFile.write(actRecs);
				// force the file no matter what
				currentFile.force(true);

				// delete the old file
				if (!oldFile.delete()) {
					throw new IOException(
							"Transaction log file cannot be deleted.");
				}

			} else { // write
				currentFile.write(records, 0, bufSize);
				if (force) {
					currentFile.force(false);
				}
			}

		} catch (IOException e) {
			throw new TLogIOException("Unexpected IO exception occurred.", e);
		}
	}

	/**
	 * FSTLogOptimizatorFlusher is executed in background daemon thread
	 * (flusherThread) to batch multiple write/flush operations into one
	 * 
	 * @author Dimitar Iv. Dimitrov
	 * @version SAP NetWeaver 7.20
	 */
	private class FSTLogOptimizatorFlusher implements Runnable {
		public void run() {
			/*
			 * The current number of empty cycles the flusherThread have done at
			 * any given moment. Empty cycle is considered every cycle that we
			 * flush noting or just one transaction.
			 */
			int numEmptyCycles = 0;

			/*
			 * The size of the buffer on which we have the last flush
			 * minimal value = 0
			 * maximal value = capacity
			 */
			int lastFlushPoint = 1;

			boolean exit = false;

			// acquire write lock
			while (!writeLock.compareAndSet(0, 1)) {
			}

			try {
				do {
					// wait
					try {
						synchronized (flusherWaitLock) {
							flusherWaitLock.wait(waitBetweenFlushes);
						}
					} catch (InterruptedException e) {
						SimpleLogger.traceThrowable(Severity.ERROR, FSTLog.LOCATION,"ASJ.trans.000102","Thread which is responsible to flush data into file system transaction log was interrupted. Retrying the flush.",
								e);
					}

					// flush
					synchronized (flushOrModifyBufferLock) {
						// get the current buffer size
						int bufSize = buffer.position();

						// this function will clear the buffer state
						ByteBuffer[] buf = buffer.getForFlush();

						// recalculate currentNotifyPoint
						currentNotifyPoint = bufSize
								+ (bufSize - lastFlushPoint);
						currentNotifyPoint = Math.min(Math.max(1,
								currentNotifyPoint), buffer.capacity());

						lastFlushPoint = bufSize;

						try {
							// flush if not empty
							if (bufSize > 0) {
								try {
									writeOrRotate(true, bufSize, buf);
									// mark IO result as successful
									lastIOOperationException = null;
								} catch (TLogIOException e) {
									// if there is exception during IO let the
									// writing threads know about it
									lastIOOperationException = e;
								}
							}

							// check if the cycle is empty or not
							if (bufSize > 1) {
								numEmptyCycles = 0;
							} else {
								if (++numEmptyCycles > MAX_NUMBER_EMPTY_CYCLES) {
									flusherThreadStarted = false;
									exit = true;
								}
							}
						} finally {
							flushOrModifyBufferLock.notifyAll();
						}
					}

				} while (!exit);

			} finally {
				// just in case
				flusherThreadStarted = false;

				// release write lock
				writeLock.set(0);
			}
		}
	}

	/**
	 * 
	 * FSTLogBuffer provide buffer functionality. All operations on the buffer must
	 * be synchronized on flushOrModifyBufferLock because it has no self
	 * synchronization and it will be redundant to have two synchronizations -
	 * an outer used by the caller and inner used by the buffer.
	 * 
	 * @author Dimitar Iv. Dimitrov
	 * @version SAP NetWeaver 7.20
	 */
	private static class FSTLogBuffer {

		// data
		/**
		 * The data storage
		 */
		private final ByteBuffer[] buffer;

		/**
		 * The capacity of the data storage
		 */
		private final int capacity;

		/**
		 * The next write position in the buffer
		 */
		private int position;

		FSTLogBuffer(int capacity) {
			this.capacity = capacity;
			this.buffer = new ByteBuffer[capacity];
			this.position = 0;
		}

		/**
		 * Add value to the buffer if there is a place.
		 * 
		 * @param value
		 *            the value to be added
		 * @return true if the value is added
		 */
		boolean add(ByteBuffer value) {
			if (position == capacity) {
				return false;
			} else {
				buffer[position++] = value;
				return true;
			}
		}

		/**
		 * Returns the inner array to be flushed. The array must be flushed from
		 * position 0 to position returned by position() before call this method
		 * because it changes internal state.
		 * 
		 * NOTE: This method will clear the internal state of the buffer so you
		 * must call empty(), position() before you call this method.
		 * 
		 * @return the inner array to be flushed
		 */
		ByteBuffer[] getForFlush() {
			position = 0;
			return buffer;
		}

		/**
		 * true if the buffer is empty
		 * 
		 * @return true if the buffer is empty
		 */
		boolean empty() {
			return position == 0;
		}

		/**
		 * The position in the buffer. Used from the flusherThread to determine
		 * if he flushes only one record per flush and to determine the position
		 * to which it must flush. Must not flush if the returned value is zero
		 * (meaning the array is empty).
		 * 
		 * @return The current point in the the buffer
		 */
		int position() {
			return position;
		}

		/**
		 * The capacity of the buffer
		 * 
		 * @return the capacity of the buffer
		 */
		int capacity() {
			return capacity;
		}
	}
}
