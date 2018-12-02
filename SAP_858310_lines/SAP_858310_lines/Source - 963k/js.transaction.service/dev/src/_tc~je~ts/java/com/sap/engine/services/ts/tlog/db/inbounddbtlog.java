package com.sap.engine.services.ts.tlog.db;

import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.INBOUND_TX;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.INBOUND_TX_EXTERNAL_XID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.INBOUND_TX_HEURISTIC_OUTCOME;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TX_RECORDS_BRANCH_IDS_LIST;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TX_RECORDS_BRANCH_ITER_LIST;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TX_RECORDS_TLOG_ID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TX_RECORDS_TX_ABANDON_TIME;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TX_RECORDS_TX_BIRTH_TIME;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TX_RECORDS_TX_CLASSIFIER;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TX_RECORDS_TX_NUMBER;
import static java.util.Arrays.binarySearch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.transaction.xa.Xid;

import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.facades.timer.TimeoutManager;
import com.sap.engine.services.ts.tlog.InboundTLog;
import com.sap.engine.services.ts.tlog.InboundTransactionRecord;
import com.sap.engine.services.ts.tlog.InboundTransactionRecordImpl;
import com.sap.engine.services.ts.tlog.InvalidRMIDException;
import com.sap.engine.services.ts.tlog.InvalidTransactionClassifierID;
import com.sap.engine.services.ts.tlog.TLogIOException;
import com.sap.engine.services.ts.tlog.TransactionRecord;
import com.sap.engine.services.ts.tlog.util.TLogLocking;
import com.sap.engine.services.ts.utils.ByteArrayUtils;
import com.sap.engine.services.ts.utils.TLogVersion;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

public class InboundDBTLog extends DBTLog implements InboundTLog {

	// Prepared statements
	/**
	 * Insert one transaction record.
	 * The arguments are as follows:
	 * 1 - Transaction Number
	 * 2 - TLog ID
	 * 3 - Transaction Birth Time
	 * 4 - Transaction Abandon Time
	 * 5 - Transaction Classifier
	 * 6 - Resource Managers IDs
	 * 7 - Branch Iterators
	 * 8 - External XID
	 * 9 - Heuristic outcome
	 */
	protected static final String INSERT_INBOUND_TRANSACTION_RECORD_STATEMENT =
		"INSERT INTO " + INBOUND_TX + " ( " +
		TX_RECORDS_TX_NUMBER + ", " +
		TX_RECORDS_TLOG_ID + ", " +
		TX_RECORDS_TX_BIRTH_TIME + ", " +
		TX_RECORDS_TX_ABANDON_TIME + ", " +
		TX_RECORDS_TX_CLASSIFIER + ", " +
		TX_RECORDS_BRANCH_IDS_LIST + ", " +
		TX_RECORDS_BRANCH_ITER_LIST + ", " +
		INBOUND_TX_EXTERNAL_XID + ", " +
		INBOUND_TX_HEURISTIC_OUTCOME + " ) " +
		"VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? )";

	/**
	 * Select all transaction records for a TLog and return all of
	 * their fields.
	 * Parameters:
	 * 1 - TLog ID
	 * Results:
	 * 1 - Transaction Number
	 * 2 - Transaction Birth Time
	 * 3 - Transaction Abandon Time
	 * 4 - Transaction Classifier
	 * 5 - Resource Managers IDs
	 * 6 - Branch Iterators
	 * 7 - External XID
	 * 8 - Heuristic outcome
	 */
	protected static final String GET_ALL_INBOUND_TRANSACTION_RECORDS_STATEMENT =
		"SELECT " + TX_RECORDS_TX_NUMBER + ", " +
		TX_RECORDS_TX_BIRTH_TIME + ", " +
		TX_RECORDS_TX_ABANDON_TIME + ", " +
		TX_RECORDS_TX_CLASSIFIER + ", " +
		TX_RECORDS_BRANCH_IDS_LIST + ", " +
		TX_RECORDS_BRANCH_ITER_LIST + ", " +
		INBOUND_TX_EXTERNAL_XID + ", " +
		INBOUND_TX_HEURISTIC_OUTCOME +
		" FROM " + INBOUND_TX +
		" WHERE " + TX_RECORDS_TLOG_ID + " = ?";

	/**
	 * Set the heuristic outcome of a transaction to a given value
	 * Parameters:
	 * 1 - Heuristic Outcome
	 * 2 - TLog ID
	 * 3 - Transaction Number
	 */
	protected static final String SET_HEURISTIC_OUTCOME_STATEMENT =
		"UPDATE " + INBOUND_TX +
		" SET " + INBOUND_TX_HEURISTIC_OUTCOME + " = ?" +
		" WHERE " + TX_RECORDS_TLOG_ID + " = ?" +
		" AND " + TX_RECORDS_TX_NUMBER + " = ?";


	// Class/instance fields
	private static final Location LOCATION = Location.getLocation(InboundDBTLog.class);


	// Constructors
	public InboundDBTLog(int tLogID, TLogVersion tLogVersion,
			TLogLocking tLogLock) {
		super(tLogID, tLogVersion, tLogLock);
		this.txManager = new DBTransactionRecordsManager(INBOUND_TX, tLogID);
	}

	/**
	 * You must call getAllUsedRMIDs() to initialize the rmIDs array
	 */
	public InboundDBTLog(int tLogID, TLogVersion tLogVersion,
			TLogLocking tLogLock, Map<String, Integer> transactionClassifiers,
			int maxTxClassifierID) {
		super(tLogID, tLogVersion, tLogLock, transactionClassifiers,
				maxTxClassifierID);
		this.txManager = new DBTransactionRecordsManager(INBOUND_TX, tLogID);
	}


	// InboundTLog interface functions
	public Map<Xid, InboundTransactionRecord> recover() throws TLogIOException {
		if (isClosed) {
			throw new TLogIOException("This transaction log is already closed.");
		}

		boolean exceptionThrown = false;
		Connection con = null;
		try {
			con = TransactionServiceFrame.getDataSourceForDBTlog().getConnection();
			txManager.flushRemovedTransactionRecords(con);

			PreparedStatement stmn = con
					.prepareStatement(GET_ALL_INBOUND_TRANSACTION_RECORDS_STATEMENT);
			try {
				stmn.setInt(1, tLogID);
				ResultSet resSet = stmn.executeQuery();

				HashMap<Xid, InboundTransactionRecord> res = new HashMap<Xid, InboundTransactionRecord>();

				while (resSet.next()) {
					InboundTransactionRecordImpl record = new InboundTransactionRecordImpl();

					// 1 - Transaction Number
					record.setTransactionSequenceNumber(resSet.getLong(1));
					// 2 - Transaction Birth Time
					record.setTransactionBirthTime(resSet.getLong(2));
					// 3 - Transaction Abandon Time
					record.setTransactionAbandonTimeout(resSet.getLong(3));
					// 4 - Transaction Classifier
					record.setTransactionClassifierID(resSet.getInt(4));
					// 5 - Resource Managers IDs
					int[] resMngs = ByteArrayUtils.bytesToInts(resSet.getBytes(5));
					record.setRMIDs(resMngs);
					// 6 - Branch Iterators
					record.setBranchIterators(resSet.getBytes(6));
					// 7 - External XID
					Xid xid = ByteArrayUtils.getXidFromByteArr(resSet.getBytes(7), 0);
					record.setExternalXID(xid);
					// 8 - Heuristic outcome
					record.setHeuristicOutcome(resSet.getInt(8));

					res.put(xid, record);
				}

				return res;
			} finally {
				stmn.close();
			}

		} catch (SQLException e) {
			exceptionThrown = true;
			throw new TLogIOException("An SQL error occured", e);

		} finally {
			if (null!=con) {
				try {
					con.close();
				} catch (SQLException e) {
					String msg = "Could not close DB connection because of an SQL exception";
					if (false == exceptionThrown) {
						throw new TLogIOException(msg, e);
					} else {
						SimpleLogger.traceThrowable(Severity.DEBUG, LOCATION, msg, e);
					}
				}
			}
		}
	}

	// performance critical function
	public void writeInboundTransactionRecord(InboundTransactionRecord record)
			throws TLogIOException, InvalidRMIDException,
			InvalidTransactionClassifierID {

		if (isClosed) {
			throw new TLogIOException("This transaction log is already closed.");
		}

		if (null==record) {
			throw new IllegalArgumentException("Input parameter transactionRecord cannot be null.");
		}

		boolean exceptionThrown = false;
		Connection con = null;
		try {
			con = TransactionServiceFrame.getDataSourceForDBTlog().getConnection();
			PreparedStatement stmn = con.prepareStatement(INSERT_INBOUND_TRANSACTION_RECORD_STATEMENT);
			// 1 - Transaction Number
			stmn.setLong(1, record.getTransactionSequenceNumber());
			// 2 - TLog ID
			stmn.setInt(2, tLogID);
			// 3 - Transaction Birth Time
			stmn.setLong(3, record.getTransactionBirthTime());
			// 4 - Transaction Abandon Time
			stmn.setLong(4, record.getTransactionAbandonTimeout());

			// 5 - Transaction Classifier
			int tcID = record.getTransactionClassifierID();
			if (tcID!=0 && !transactionClassifiers.containsValue(tcID)) {
				exceptionThrown = true;
				throw new InvalidTransactionClassifierID("Invalid transaction classifier ID: " + tcID);
			}
			stmn.setInt(5, tcID);

			// convert the array of the RM IDs to byte array
			// and check the RM IDs are valid
			int[] resManagers = record.getRMIDs();
			byte[] resManBA = new byte[resManagers.length*4];

			for (int i=0; i<resManagers.length; ++i) {
				int id = resManagers[i];
				if (binarySearch(rmIDs, id) < 0) {
					exceptionThrown = true;
					throw new InvalidRMIDException("Resource manager ID " + id + " is not valid.");
				}
				ByteArrayUtils.addIntInByteArray(id, resManBA, i*4);
			}
			// 6 - Resource Managers IDs
			stmn.setBytes(6, resManBA);
			// 7 - Branch Iterators
			stmn.setBytes(7, record.getBranchIterators());
			// 8 - External XID
			byte[] xid = ByteArrayUtils.convertXidToByteArray(record.getExternalXID());
			stmn.setBytes(8, xid);
			// 9 - Heuristic outcome
			stmn.setInt(9, record.getHeuristicOutcome());

			stmn.executeUpdate();
			stmn.close();

		} catch (SQLException e) {
			exceptionThrown = true;
			throw new TLogIOException("An SQL error occured", e);

		} finally {
			if (null!=con) {
				try {
					con.close();
				} catch (SQLException e) {
					String msg = "Could not close DB connection because of an SQL exception";
					if (false == exceptionThrown) {
						throw new TLogIOException(msg, e);
					} else {
						SimpleLogger.traceThrowable(Severity.DEBUG, LOCATION, msg, e);
					}
				}
			}
		}
	}

	public void setHeuristicOutcome(long txSeqNumber, int heuristicOutcome) throws TLogIOException {
		if (isClosed) {
			throw new TLogIOException("This transaction log is already closed.");
		}

		boolean exceptionThrown = false;
		Connection con = null;
		try {
			con = TransactionServiceFrame.getDataSourceForDBTlog().getConnection();
			PreparedStatement stmn = con.prepareStatement(SET_HEURISTIC_OUTCOME_STATEMENT);
			// 1 - Heuristic Outcome
			stmn.setInt(1, heuristicOutcome);
			// 2 - TLog ID
			stmn.setInt(2, tLogID);
			// 3 - Transaction Number
			stmn.setLong(3, txSeqNumber);

			/*int numAffectedColumns =*/ stmn.executeUpdate();
			// TODO check the numAffectedColumns to be one ?

			stmn.close();

		} catch (SQLException e) {
			exceptionThrown = true;
			throw new TLogIOException("An SQL error occured", e);

		} finally {
			if (null!=con) {
				try {
					con.close();
				} catch (SQLException e) {
					String msg = "Could not close DB connection because of an SQL exception";
					if (false == exceptionThrown) {
						throw new TLogIOException(msg, e);
					} else {
						SimpleLogger.traceThrowable(Severity.DEBUG, LOCATION, msg, e);
					}
				}
			}
		}
	}

	@Override
	public void writeTransactionRecord(TransactionRecord transactionRecord) {
		// This function is not supported for the InboundDBTLog. Use
		// writeInboundTransactionRecords instead.
		throw new RuntimeException(
				"Internal error: Unsupported function in InboundDBTLog. "
				+ "Use writeInboundTransactionRecord() instead.");
	}


	// helpful methods
	@Override
	protected Iterator<TransactionRecord> getAllTransactionRecords(
			Connection con) throws SQLException {

		txManager.flushRemovedTransactionRecords(con);
		PreparedStatement stmn = con.prepareStatement(
				GET_ALL_INBOUND_TRANSACTION_RECORDS_STATEMENT);

		try {
			stmn.setInt(1, tLogID);
			ResultSet resSet = stmn.executeQuery();

			ArrayList<TransactionRecord> res = new ArrayList<TransactionRecord>();

			while (resSet.next()) {
				InboundTransactionRecordImpl record = new InboundTransactionRecordImpl();

				// 1 - Transaction Number
				record.setTransactionSequenceNumber(resSet.getLong(1));
				// 2 - Transaction Birth Time
				record.setTransactionBirthTime(resSet.getLong(2));
				// 3 - Transaction Abandon Time
				record.setTransactionAbandonTimeout(resSet.getLong(3));
				// 4 - Transaction Classifier
				record.setTransactionClassifierID(resSet.getInt(4));
				// 5 - Resource Managers IDs
				int[] resMngs = ByteArrayUtils.bytesToInts(resSet.getBytes(5));
				record.setRMIDs(resMngs);
				// 6 - Branch Iterators
				record.setBranchIterators(resSet.getBytes(6));
				// 7 - External XID
				Xid xid = ByteArrayUtils.getXidFromByteArr(resSet.getBytes(7), 0);
				record.setExternalXID(xid);
				// 8 - Heuristic outcome
				record.setHeuristicOutcome(resSet.getInt(8));

				res.add(record);
			}

			return res.iterator();

		} finally {
			stmn.close();
		}
	}
}
