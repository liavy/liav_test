package com.sap.engine.services.ts.tlog.db;

import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TX_RECORDS_TLOG_ID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TX_RECORDS_TX_NUMBER;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

public class DBTransactionRecordsManager {

	/**
	 * Delete all transaction records between two transaction
	 * numbers for a specific transaction log.
	 * Parameters:
	 * 1 - First Transaction Number
	 * 2 - Second Transaction Number
	 */
	protected final String removeTxRecordsBetweenStatement;

	protected static final int TRANSACTION_RECORDS_TO_BE_REMOVED_CHACHE_MAX_SIZE = 256;

	protected volatile long[] txRecordsToBeRemovedChache =
		new long[TRANSACTION_RECORDS_TO_BE_REMOVED_CHACHE_MAX_SIZE];
	protected volatile int txRecordsToBeRemovedChacheSize = 0;


	public DBTransactionRecordsManager(String txRecordsTableName, int tLogId) {
		removeTxRecordsBetweenStatement =
			"DELETE FROM " + txRecordsTableName +
			" WHERE " + TX_RECORDS_TLOG_ID + " = " + tLogId +
			" AND " + TX_RECORDS_TX_NUMBER + " BETWEEN ? AND ?";
	}


	// performance critical function
	public void removeTransactionRecordLazily(Connection con, long txSequenceNumber)
				throws SQLException {
		long[] t1 = null;
		int t2 = 0;

		synchronized (this) {
			txRecordsToBeRemovedChache[txRecordsToBeRemovedChacheSize++] = txSequenceNumber;
			if (txRecordsToBeRemovedChacheSize == TRANSACTION_RECORDS_TO_BE_REMOVED_CHACHE_MAX_SIZE) {
				t1 = txRecordsToBeRemovedChache;
				t2 = txRecordsToBeRemovedChacheSize;
				txRecordsToBeRemovedChache = new long[TRANSACTION_RECORDS_TO_BE_REMOVED_CHACHE_MAX_SIZE];
				txRecordsToBeRemovedChacheSize = 0;
			}
		}

		if (t1 != null) {
			flushRemovedTransactionRecords(con, t1, t2);
		}
	}

	// performance critical function
	public void removeTransactionRecordImmediately(Connection con, long txSequenceNumber)
				throws SQLException {
		long[] t1 = null;
		int t2 = 0;

		synchronized (this) {
			txRecordsToBeRemovedChache[txRecordsToBeRemovedChacheSize++] = txSequenceNumber;
			t1 = txRecordsToBeRemovedChache;
			t2 = txRecordsToBeRemovedChacheSize;
			txRecordsToBeRemovedChache = new long[TRANSACTION_RECORDS_TO_BE_REMOVED_CHACHE_MAX_SIZE];
			txRecordsToBeRemovedChacheSize = 0;
		}

		flushRemovedTransactionRecords(con, t1, t2);
	}

	// performance critical function
	public void flushRemovedTransactionRecords(Connection con) throws SQLException {
		long[] t1 = null;
		int t2 = 0;

		synchronized (this) {
			t1 = txRecordsToBeRemovedChache;
			t2 = txRecordsToBeRemovedChacheSize;
			txRecordsToBeRemovedChache = new long[TRANSACTION_RECORDS_TO_BE_REMOVED_CHACHE_MAX_SIZE];
			txRecordsToBeRemovedChacheSize = 0;
		}

		flushRemovedTransactionRecords(con, t1, t2);
	}

	// performance critical function
	protected void flushRemovedTransactionRecords(Connection con, long[] txRecordsToFlush, int size)
				throws SQLException {
		if (size==0)
			return;

		// Sort the array for easier search of sequences
		Arrays.sort(txRecordsToFlush, 0, size);

		PreparedStatement stmn = con.prepareStatement(removeTxRecordsBetweenStatement);

		// Search for sequences and delete them
		for (int i=0; i<size; ) {
			long first = txRecordsToFlush[i++];
			for (; i<size; ++i) {
				// Check if the current element is the
				// '+1 element' for the previous element
				if (txRecordsToFlush[i-1] + 1 != txRecordsToFlush[i]) {
					break;
				}
			}

			// Delete between first and txRecordsToFlush[i-1]
			// because txRecordsToFlush[i] is not the '+1 element'
			// for txRecordsToFlush[i-1] element or it is the
			// 'size' element
			// 1 - First Transaction Number
			stmn.setLong(1, first);
			// 2 - Second Transaction Number
			stmn.setLong(2, txRecordsToFlush[i-1]);
			stmn.executeUpdate();
		}

		stmn.close();
	}
}
