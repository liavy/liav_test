package com.sap.engine.services.ts.tlog.db;

import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TLOG;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TLOG_ID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TLOG_NODE_ID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TLOG_SYSTEM_ID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TLOG_TM_STARTUP_TIME;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TX_CLASSIFIER;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TX_CLASSIFIER_CLASSIFIER;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TX_CLASSIFIER_TLOG_ID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TX_CLASSIFIER_TX_CLASSIFIER_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import com.sap.engine.services.ts.facades.timer.TimeoutManager;
import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.tlog.InboundTLog;
import com.sap.engine.services.ts.tlog.TLog;
import com.sap.engine.services.ts.tlog.TLogAlreadyExistException;
import com.sap.engine.services.ts.tlog.TLogIOException;
import com.sap.engine.services.ts.tlog.TLogReaderWriter;
import com.sap.engine.services.ts.tlog.util.TLogLocking;
import com.sap.engine.services.ts.tlog.util.TLogLockingException;
import com.sap.engine.services.ts.tlog.util.TLogLockingInfrastructureException;
import com.sap.engine.services.ts.utils.TLogVersion;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

public class DBTLogReaderWriter implements TLogReaderWriter {

	// Prepared statements
	/**
	 * Query to get TLog ID by the TLog version
	 * Parameters:
	 * 1 - System ID
	 * 2 - Node ID
	 * 3 - Transaction Manager Startup Time
	 * Result:
	 * 1 - TLog ID
	 */
	protected static final String GET_TLOG_ID_BY_VERSION_STATEMENT =
		"SELECT " + TLOG_ID +
		" FROM " + TLOG +
		" WHERE " + TLOG_SYSTEM_ID + " = ?" +
		" AND " + TLOG_NODE_ID + " = ?" +
		" AND " + TLOG_TM_STARTUP_TIME + " = ?";

	/**
	 * Query to get the TLog ID with the maximal TLog ID number.
	 */
	protected static final String GET_MAX_TLOG_ID_STATEMENT =
		"SELECT MAX ( " + TLOG_ID + " )" +
		" FROM " + TLOG;

	/**
	 * Insert record for one TLog in TLOG_ID DB table
	 * Parameters:
	 * 1 - TLog ID
	 * 2 - System ID
	 * 3 - Node ID
	 * 4 - Transaction Manager Startup Time
	 */
	protected static final String INSERT_TLOG_STATEMENT =
		"INSERT INTO " + TLOG + " ( " +
		TLOG_ID + ", " +
		TLOG_SYSTEM_ID + ", " +
		TLOG_NODE_ID + ", " +
		TLOG_TM_STARTUP_TIME + " ) " +
		"VALUES ( ?, ?, ?, ? )";

	/**
	 * Return all TLog records in the TLOG table
	 * Results:
	 * 1 - TLog ID
	 * 2 - System ID
	 * 3 - Node ID
	 * 4 - Transaction Manager Startup Time
	 */
	protected static final String GET_ALL_TLOG_RECORDS_STATEMENT =
		"SELECT " + TLOG_ID + ", " +
		TLOG_SYSTEM_ID + ", " +
		TLOG_NODE_ID + ", " +
		TLOG_TM_STARTUP_TIME +
		" FROM " + TLOG;

	/**
	 * Return all transaction classifiers for a TLog
	 * Parameters:
	 * 1 - TLog ID
	 * Results:
	 * 1 - Transaction Classifier ID
	 * 2 - Transaction Classifier
	 */
	protected static final String GET_ALL_TRANSACTION_IDENTIFIERS_FOR_TLOG_STATEMENT =
		"SELECT " + TX_CLASSIFIER_TX_CLASSIFIER_ID + ", " +
		TX_CLASSIFIER_CLASSIFIER +
		" FROM " + TX_CLASSIFIER + 
		" WHERE " + TX_CLASSIFIER_TLOG_ID + " = ?";


	// Class/instance fields
	private static final Location LOCATION = Location.getLocation(DBTLogReaderWriter.class);

	/**
	 * This value is used to lock the TLog table in the DB and must never be
	 * changed.
	 */
	protected static final String TLOG_TABLE_LOCK = "SAPTLOGDBTABLELOCK";

	protected final TLogLocking tLogLock;


	// Constructor
	public DBTLogReaderWriter(TLogLocking lock) {
		this.tLogLock = lock;
	}


	// TLogReaderWriter methods
	public TLog createNewTLog(byte[] TLogVersion) throws TLogIOException, TLogAlreadyExistException {
		if (null==TLogVersion || TLogVersion.length!=15) {
			throw new IllegalArgumentException("Input parametar TLogVersion must be not null and must be 15 bytes long.");
		}

		boolean exceptionThrown = false;
		TLogVersion version = new TLogVersion(TLogVersion);
		Connection con = null;
		try {
			con = TransactionServiceFrame.getDataSourceForDBTlog().getConnection();
			con.setAutoCommit(false);
			// obtain TLog table lock
			tLogLock.obtainTLogLock(TLOG_TABLE_LOCK);

			// check if TLog exist
			if (0 != getTLogID(con, version)) {
				exceptionThrown = true;
				throw new TLogAlreadyExistException("Transaction log with system ID: " +
						version.getSystemID() + " , node ID: " +
						version.getNodeID() + " and transaction server startup time: " +
						version.getTmStartupTime() + " already exist.");

			}

			// generate new TLog ID
			int tLogID = generateTLogID(con);

			// Lock the TLog
			try {
				tLogLock.lockTLog(version.toString());
			} catch (TLogLockingException e1) {
				// the lock must not exist because no one else can
				// create the same log so this is runtime error
				exceptionThrown = true;
				throw new IllegalStateException("Internal error.", e1);
			}
			try {
				// Insert TLog
				insertTLog(con, tLogID, version);

				// Commit
				con.commit();

				return new DBTLog(tLogID, version, tLogLock);
			} catch (SQLException e) {
				try {
					tLogLock.unlockTLog(version.toString());
				} catch (TLogLockingInfrastructureException e1) {
					SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "ASJ.trans.000310", "Locking error occurred.", e1);
				}
				throw e;
			}

		} catch (SQLException e) {
			if (null!=con) {
				try {
					con.rollback();
				} catch (SQLException ex) {
					SimpleLogger.traceThrowable(
							Severity.DEBUG,
							LOCATION,
							"Cound not rollback DB transaction because of an SQL exception",
							ex);
				}
			}
			exceptionThrown = true;
			throw new TLogIOException("An SQL error occurred", e);

		} catch (TLogLockingInfrastructureException e) {
			if (null!=con) {
				try {
					con.rollback();
				} catch (SQLException ex) {
					SimpleLogger.traceThrowable(
							Severity.DEBUG,
							LOCATION,
							"Cound not rollback DB transaction because of an SQL exception",
							ex);
				}
			}
			exceptionThrown = true;
			throw new TLogIOException("Locking error occurred", e);

		} catch (TimeoutException e) {
			if (null!=con) {
				try {
					con.rollback();
				} catch (SQLException ex) {
					SimpleLogger.traceThrowable(
							Severity.DEBUG,
							LOCATION,
							"Cound not rollback DB transaction because of an SQL exception",
							ex);
				}
			}
			exceptionThrown = true;
			throw new TLogIOException("Could not obtain the " + TLOG + " DB table lock", e);

		} finally {
			try {
				// release table lock
				tLogLock.unlockTLog(TLOG_TABLE_LOCK);
			} catch (TLogLockingInfrastructureException lockerr) {
				String msg = "Locking error occurred";
				if (false == exceptionThrown) {
					exceptionThrown = true;
					throw new TLogIOException(msg, lockerr);
				} else {
					SimpleLogger.traceThrowable(Severity.DEBUG, LOCATION, msg, lockerr);
				}
			} finally {
				if (null!=con) {
					try {
						con.close();
					} catch (SQLException e) {
						String msg = "Could not close DB connection because of an SQL exception";
						if (false == exceptionThrown) {
							exceptionThrown = true;
							throw new TLogIOException(msg, e);
						} else {
							SimpleLogger.traceThrowable(Severity.DEBUG, LOCATION, msg, e);
						}
					}
				}
			}
		}
	}

	public TLog lockOrphanedTLog() throws TLogIOException {
		boolean exceptionThrown = false;
		Connection con = null;
		try {
			con = TransactionServiceFrame.getDataSourceForDBTlog().getConnection();

			int tLogID=0;
			TLogVersion tLogVer = null;
			{
				// Read TLog table
				PreparedStatement stmn = con.prepareStatement(GET_ALL_TLOG_RECORDS_STATEMENT);
				ResultSet res = stmn.executeQuery();
				// 1 - TLog ID
				// 2 - System ID
				// 3 - Node ID
				// 4 - Transaction Manager Startup Time

				// Search for unlocked TLog
				while (res.next()) {
					TLogVersion ver = new TLogVersion(res.getBytes(2),
							res.getInt(3), res.getLong(4));
					if (ver.getTmStartupTime()==0) {
						continue;
					}
					try {
						tLogLock.lockTLog(ver.toString());
					} catch (TLogLockingException e) {
						//$JL-EXC$
						continue;
					}
					tLogVer = ver;
					tLogID = res.getInt(1);
					break;
				}
			}

			DBTLog resultTLog = null;
			// If orphaned lock is found - create and initialize
			// DBTLog instance for it
			if (null!=tLogVer) {
				Map<String, Integer> txClassifiers = new ConcurrentHashMap<String, Integer>(
						TransactionServiceFrame.maxTransactionClassifiers);
				int maxTxClassifierID = readTxClassifierForTLog(con, tLogID, txClassifiers);

				resultTLog = new DBTLog(tLogID, tLogVer, tLogLock,
						txClassifiers, maxTxClassifierID);
			}

			return resultTLog;

		} catch (SQLException e) {
			exceptionThrown = true;
			throw new TLogIOException("An SQL error occurred", e);

		} catch (TLogLockingInfrastructureException e) {
			exceptionThrown = true;
			throw new TLogIOException("Locking error occurred", e);

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

	public InboundTLog getInboundTLog(TLogVersion ver) throws TLogIOException {
		if (null==ver || ver.getTmStartupTime() != 0) {
			throw new IllegalArgumentException(
					"Input parameter TLogVersion must be not null.");
		}

		boolean exceptionThrown = false;
		Connection con = null;

		// Lock the TLog
		try {
			tLogLock.lockTLog(ver.toString());
		} catch (TLogLockingException e) {
			// the caller must ensure that no one else has
			// locked this TLog so this is runtime error
			exceptionThrown = true;
			throw new IllegalStateException("Internal error.", e);
		} catch (TLogLockingInfrastructureException e) {
			throw new TLogIOException("Locking error occurred", e);
		}

		try {
			con = TransactionServiceFrame.getDataSourceForDBTlog().getConnection();
			boolean newTLog = false;
			int tLogID;
			Map<String, Integer> txClassifiers = new ConcurrentHashMap<String, Integer>(
					TransactionServiceFrame.maxTransactionClassifiers);
			int maxTxClassifierID = 0;

			// obtain TLog table lock
			tLogLock.obtainTLogLock(TLOG_TABLE_LOCK);
			try {
				tLogID = getTLogID(con, ver);
				if (0 == tLogID) {
					newTLog = true;
					tLogID = generateTLogID(con);
					insertTLog(con, tLogID, ver);
				}
			} finally {
				// release table lock
				tLogLock.unlockTLog(TLOG_TABLE_LOCK);
			}

			if (!newTLog) {
				maxTxClassifierID = readTxClassifierForTLog(con, tLogID, txClassifiers);
			}

			return new InboundDBTLog(tLogID, ver, tLogLock, txClassifiers,
					maxTxClassifierID);

		} catch (SQLException e) {
			exceptionThrown = true;
			try {
				tLogLock.unlockTLog(ver.toString());
			} catch (TLogLockingInfrastructureException e1) {
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "ASJ.trans.000310", "Locking error occurred.", e1);
			}
			throw new TLogIOException("An SQL error occurred", e);

		} catch (TLogLockingInfrastructureException e) {
			exceptionThrown = true;
			try {
				tLogLock.unlockTLog(ver.toString());
			} catch (TLogLockingInfrastructureException e1) {
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "ASJ.trans.000310", "Locking error occurred.", e1);
			}
			throw new TLogIOException("Locking error occurred", e);

		} catch (TimeoutException e) {
			exceptionThrown = true;
			try {
				tLogLock.unlockTLog(ver.toString());
			} catch (TLogLockingInfrastructureException e1) {
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "ASJ.trans.000310", "Locking error occurred.", e1);
			}
			throw new TLogIOException("Could not obtain the " + TLOG + " DB table lock", e);

		} finally {
			if (null != con) {
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


	// helpful methods
	/**
	 * Return the TLog ID for the TLog with the specified version. If a TLog
	 * with the specified version doesn't exists then return 0.
	 */
	protected int getTLogID(Connection con, TLogVersion version)
			throws SQLException {

		PreparedStatement stmn = con.prepareStatement(GET_TLOG_ID_BY_VERSION_STATEMENT);

		try {
			// 1 - System ID
			stmn.setBytes(1, version.getSystemID());
			// 2 - Node ID
			stmn.setInt(2, version.getNodeID());
			// 3 - Transaction Manager Startup Time
			stmn.setLong(3, version.getTmStartupTime());
			ResultSet res = stmn.executeQuery();

			if (res.next()) {
				return res.getInt(1);
			} else {
				return 0;
			}

		} finally {
			stmn.close();
		}
	}

	/**
	 * PRECONDITION: Hold the TLOG_TABLE_LOCK lock
	 * 
	 * @param con
	 *            a connection to the data source which we use
	 * @return the generated ID for the TLog
	 * @throws SQLException
	 *             if SQLException occurs
	 */
	protected int generateTLogID(Connection con) throws SQLException {
		PreparedStatement stmn = con.prepareStatement(GET_MAX_TLOG_ID_STATEMENT);
		try {
			ResultSet res = stmn.executeQuery();
			if (res.next()) {
				return 1 + res.getInt(1);
			} else {
				return 1;
			}
		} finally {
			stmn.close();
		}
	}

	/**
	 * PRECONDITION: Hold the TLOG_TABLE_LOCK lock
	 * 
	 * @param con
	 *            a connection to the data source which we use
	 * @param tLogID
	 *            the TLog ID for the TLog which we are inserting
	 * @param version
	 *            the verstion of the TLog which we are inserting
	 * @throws SQLException
	 *             if SQLException occurs
	 */
	protected void insertTLog(Connection con, int tLogID, TLogVersion version)
			throws SQLException {

		PreparedStatement stmn = con.prepareStatement(INSERT_TLOG_STATEMENT);
		try {
			// 1 - TLog ID
			stmn.setInt(1, tLogID);
			// 2 - System ID
			stmn.setBytes(2, version.getSystemID());
			// 3 - Node ID
			stmn.setInt(3, version.getNodeID());
			// 4 - Transaction Manager Startup Time
			stmn.setLong(4, version.getTmStartupTime());
			stmn.executeUpdate();
		} finally {
			stmn.close();
		}
	}

	/**
	 * Read BC_JTA_TX_CLASS table for a specified TLog ID, put all record in a
	 * Map<String, Integer> and return the maximal ID
	 * 
	 * @return the maximal transaction classifier ID
	 */
	protected int readTxClassifierForTLog(Connection con, int tLogID,
			Map<String, Integer> txClassifiers) throws SQLException {

		PreparedStatement stmn = con.prepareStatement(
				GET_ALL_TRANSACTION_IDENTIFIERS_FOR_TLOG_STATEMENT);

		try {
			// 1 - TLog ID
			stmn.setInt(1, tLogID);
			ResultSet res = stmn.executeQuery();

			int maxTxClassifierID  = 0;
			while (res.next()) {
				// 1 - Transaction Classifier ID
				// 2 - Transaction Classifier
				int id = res.getInt(1);
				txClassifiers.put(res.getString(2), id);
				if (id > maxTxClassifierID)
					maxTxClassifierID = id;
			}

			return maxTxClassifierID;

		} finally {
			stmn.close();
		}
	}
}
