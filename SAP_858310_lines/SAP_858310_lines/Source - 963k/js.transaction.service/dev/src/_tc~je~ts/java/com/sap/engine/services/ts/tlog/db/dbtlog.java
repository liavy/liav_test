package com.sap.engine.services.ts.tlog.db;

import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RES_MANAGERS;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RES_MANAGERS_RM_CONTAINER_NAME;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RES_MANAGERS_RM_ID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RES_MANAGERS_RM_NAME;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RES_MANAGERS_STATUS;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RES_MANAGERS_STATUS_ACTIVE;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RES_MANAGERS_STATUS_MARKED_FOR_DELETE;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RES_MANAGERS_TLOG_ID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RM_PROPS;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RM_PROPS_NON_SECURE_PROPERTY;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RM_PROPS_PROPERTY_NAME;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RM_PROPS_PROPERTY_TYPE;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RM_PROPS_PROPERTY_VALUE;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RM_PROPS_RM_ID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RM_PROPS_SECURE_PROPERTY;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RM_PROPS_TLOG_ID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TLOG;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TLOG_ID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TX_CLASSIFIER;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TX_CLASSIFIER_CLASSIFIER;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TX_CLASSIFIER_TLOG_ID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TX_CLASSIFIER_TX_CLASSIFIER_ID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TX_RECORDS;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.engine.interfaces.transaction.RMProps;
import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.exceptions.TimeOutIsStoppedException;
import com.sap.engine.services.ts.facades.crypter.CrypterException;
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
import com.sap.engine.services.ts.tlog.TransactionRecordImpl;
import com.sap.engine.services.ts.tlog.util.TLogLocking;
import com.sap.engine.services.ts.tlog.util.TLogLockingInfrastructureException;
import com.sap.engine.services.ts.utils.ByteArrayUtils;
import com.sap.engine.services.ts.utils.TLogVersion;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

public class DBTLog implements TLog {
	
	// Prepared statements
	/**
	 * Query to used to retrieved all resource manager IDs.
	 * The parameter is the TLog ID for which we want the
	 * resource manager IDs.
	 * NOTE: The IDs must be in ascending order because
	 * of implementation specifics.
	 */
	protected static final String GET_ALL_USED_RM_IDS_STATEMENT =
		"SELECT " + RES_MANAGERS_RM_ID + " , " +
		RES_MANAGERS_STATUS +
		" FROM " + RES_MANAGERS +
		" WHERE " + RES_MANAGERS_TLOG_ID + " = ?" +
		" ORDER BY " + RES_MANAGERS_RM_ID + " ASC";

	/**
	 * Query used to get the RM ID with the maximal value. The parameter is the
	 * TLog ID.
	 */
	protected static final String GET_MAX_RM_ID_STATEMENT =
		"SELECT MAX ( " + RES_MANAGERS_RM_ID + " )" +
		" FROM " + RES_MANAGERS +
		" WHERE " + RES_MANAGERS_TLOG_ID + " = ?";

	protected static final String GET_RM_COUNT_STATEMENT =
		"SELECT COUNT (*)" +
		" FROM " + RES_MANAGERS +
		" WHERE " + RES_MANAGERS_TLOG_ID + " = ?";

	protected static final String GET_ALL_RM_IDS_WITH_GIVEN_STATUS_STATEMENT =
		"SELECT " + RES_MANAGERS_RM_ID +
		" FROM " + RES_MANAGERS +
		" WHERE " + RES_MANAGERS_TLOG_ID + " = ?" +
		" AND " + RES_MANAGERS_STATUS + " = ?";

	protected static final String REMOVE_RM_PROPERTIES_STATEMENT =
		"DELETE FROM " + RM_PROPS +
		" WHERE " + RM_PROPS_TLOG_ID + " = ?" +
		" AND " + RM_PROPS_RM_ID + " = ?";

	protected static final String REMOVE_RM_STATEMENT =
		"DELETE FROM " + RES_MANAGERS +
		" WHERE " + RES_MANAGERS_TLOG_ID + " = ?" +
		" AND " + RES_MANAGERS_RM_ID + " = ?";

	/**
	 * Query used to get resource manager ID by RM name.
	 * Parameters:
	 * 1 - TLog ID
	 * 2 - RM Name
	 * 3 - RM Status
	 * Result:
	 * 1 - RM ID of the RM with the given name
	 */
	protected static final String GET_RM_ID_BY_NAME_STATEMENT =
		"SELECT " + RES_MANAGERS_RM_ID +
		" FROM " + RES_MANAGERS +
		" WHERE " + RES_MANAGERS_TLOG_ID + " = ?" +
		" AND " + RES_MANAGERS_RM_NAME + " LIKE ?" +
		" AND " + RES_MANAGERS_STATUS + " = ?";

	/**
	 * Query to get properties of a resource manager.
	 * Parameters:
	 * 1 - RM ID
	 * 2 - TLog ID
	 * Result:
	 * 1 - RM Name
	 * 2 - RM Container Name
	 * 3 - RM Property Type
	 * 4 - RM Property Name
	 * 5 - RM Property Value
	 */
	protected static final String GET_RM_PROPERTIES_STATEMENT =
		"SELECT rm." + RES_MANAGERS_RM_NAME +
		", rm." + RES_MANAGERS_RM_CONTAINER_NAME +
		", rmp." + RM_PROPS_PROPERTY_TYPE +
		", rmp." + RM_PROPS_PROPERTY_NAME +
		", rmp." +  RM_PROPS_PROPERTY_VALUE +
		" FROM " + RES_MANAGERS + " AS rm LEFT OUTER JOIN " + RM_PROPS + " AS rmp" +
		" ON rm." + RES_MANAGERS_RM_ID + " = rmp." + RM_PROPS_RM_ID +
		" AND rm." + RES_MANAGERS_TLOG_ID + " = rmp." + RM_PROPS_TLOG_ID +
		" WHERE rm." + RES_MANAGERS_RM_ID + " = ?" +
		" AND rm." + RES_MANAGERS_TLOG_ID + " = ?";

	/**
	 * Insert one record in the resource managers table
	 * The arguments are as follows:
	 * 1 - RM ID
	 * 2 - TLog ID
	 * 3 - RM Name
	 * 4 - RM Container Name
	 * 5 - RM Initial Status
	 */
	protected static final String INSERT_RM_STATEMENT =
		"INSERT INTO " + RES_MANAGERS + " ( " +
		RES_MANAGERS_RM_ID + ", " +
		RES_MANAGERS_TLOG_ID + ", " +
		RES_MANAGERS_RM_NAME + ", " +
		RES_MANAGERS_RM_CONTAINER_NAME + ", " +
		RES_MANAGERS_STATUS + " ) " +
		"VALUES ( ?, ?, ?, ?, ? )";

	/**
	 * Insert one record in the resource manager properties
	 * table. The arguments are as follows:
	 * 1 - RM ID
	 * 2 - TLog ID
	 * 3 - Property Type
	 * 4 - Property Name
	 * 5 - Property Value
	 */
	protected static final String INSERT_RM_PROPERTIES_STATEMENT =
		"INSERT INTO " + RM_PROPS + " ( " +
		RM_PROPS_RM_ID + ", " +
		RM_PROPS_TLOG_ID + ", " +
		RM_PROPS_PROPERTY_TYPE + ", " +
		RM_PROPS_PROPERTY_NAME + ", " +
		RM_PROPS_PROPERTY_VALUE + " ) " +
		"VALUES ( ?, ?, ?, ?, ? )";

	/**
	 * Mark RM as unregistered by marking it's with minus
	 * The arguments are as follows:
	 * 1 - New RM Status
	 * 2 - RM ID
	 * 3 - TLogID
	 */
	protected static final String CHANGE_RM_STATUS_STATEMENT =
		"UPDATE " + RES_MANAGERS +
		" SET " + RES_MANAGERS_STATUS + " = ? " +
		" WHERE " + RES_MANAGERS_RM_ID + " = ?" +
		" AND " + RM_PROPS_TLOG_ID + " = ?";

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
	 */
	protected static final String INSERT_TRANSACTION_RECORD_STATEMENT =
		"INSERT INTO " + TX_RECORDS + " ( " +
		TX_RECORDS_TX_NUMBER + ", " +
		TX_RECORDS_TLOG_ID + ", " +
		TX_RECORDS_TX_BIRTH_TIME + ", " +
		TX_RECORDS_TX_ABANDON_TIME + ", " +
		TX_RECORDS_TX_CLASSIFIER + ", " +
		TX_RECORDS_BRANCH_IDS_LIST + ", " +
		TX_RECORDS_BRANCH_ITER_LIST + " ) " +
		"VALUES ( ?, ?, ?, ?, ?, ?, ? )";

	/**
	 * Select all transaction records for a TLog and return all of
	 * their fields. Parameters:
	 * 1 - TLog ID
	 * Results:
	 * 1 - Transaction Number
	 * 2 - Transaction Birth Time
	 * 3 - Transaction Abandon Time
	 * 4 - Transaction Classifier
	 * 5 - Resource Managers IDs
	 * 6 - Branch Iterators
	 */
	protected static final String GET_ALL_TRANSACTION_RECORDS_STATEMENT =
		"SELECT " + TX_RECORDS_TX_NUMBER + ", " +
		TX_RECORDS_TX_BIRTH_TIME + ", " +
		TX_RECORDS_TX_ABANDON_TIME + ", " +
		TX_RECORDS_TX_CLASSIFIER + ", " +
		TX_RECORDS_BRANCH_IDS_LIST + ", " +
		TX_RECORDS_BRANCH_ITER_LIST +
		" FROM " + TX_RECORDS +
		" WHERE " + TX_RECORDS_TLOG_ID + " = ?";

	/**
	 * Insert one transaction classifier in the TX_CLASSIFIER
	 * table. The arguments are as follows:
	 * 1 - Transaction Classifier ID
	 * 2 - TLog ID
	 * 3 - Transaction Classifier
	 */
	protected static final String INSERT_TX_CLASSIFIER_STATEMENT =
		"INSERT INTO " + TX_CLASSIFIER + " ( " +
		TX_CLASSIFIER_TX_CLASSIFIER_ID + ", " +
		TX_CLASSIFIER_TLOG_ID + ", " +
		TX_CLASSIFIER_CLASSIFIER + " ) " +
		"VALUES ( ?, ?, ? )";

	protected static final String REMOVE_ALL_TX_CLASSIFIERS_FOR_TX_LOG_STATEMENT =
		"DELETE FROM " + TX_CLASSIFIER +
		" WHERE " + TX_CLASSIFIER_TLOG_ID + " = ?";

	protected static final String REMOVE_ALL_RM_PROPERTIES_FOR_TX_LOG_STATEMENT =
		"DELETE FROM " + RM_PROPS +
		" WHERE " + RM_PROPS_TLOG_ID + " = ?";

	protected static final String REMOVE_ALL_RM_FOR_TX_LOG_STATEMENT =
		"DELETE FROM " + RES_MANAGERS +
		" WHERE " + RES_MANAGERS_TLOG_ID + " = ?";

	protected static final String REMOVE_TLOG_STATEMENT =
		"DELETE FROM " + TLOG +
		" WHERE " + TLOG_ID + " = ?";


	// Class/instance fields
	private static final Location LOCATION = Location.getLocation(DBTLog.class);

	/**
	 * This field must be initialized correctly from every constructor
	 */
	protected DBTransactionRecordsManager txManager;

	protected final int tLogID;	
	/**
	 * Used by close() method to release the TLog lock
	 */
	protected final TLogVersion tLogVersion;
	/**
	 * Used by close() method to release the TLog lock
	 */
	protected final TLogLocking tLogLock;

	/**
	 * This array contains all registered RM IDs.
	 * NOTE: The IDs must be in ascending order because
	 * of implementation specifics.
	 */
	protected int[] rmIDs = new int[] {};

	/**
	 * Map transaction classifier string to transaction
	 * classifier ID. When constructing the class
	 * you must guarantee that this map is consistent
	 * with BC_JTA_TX_CLASS table in the database.
	 * When constructing new TLog this is an empty map.
	 * When locking an orphaned TLog you must read the
	 * BC_JTA_TX_CLASS DB table for the appropriate
	 * TLogID and put it in a consistent Map.
	 */
	protected final Map<String, Integer> transactionClassifiers;
	/**
	 * This is the maximal used transaction classifier ID
	 * by this TLog. When constructing the class you must
	 * guarantee that this number is equal or greater than
	 * the biggest transaction classifier ID used in the
	 * TLog.
	 */
	protected int maxTxClassifierID;// TODO
//	/**
//	 * This is the maximum number of transaction classifiers
//	 * which will be stored by this DBTLog.
//	 */
//	protected final int maxNumberOfTxClassifiers;

	protected volatile boolean isClosed = false;

	protected final Map<Integer, RMProps> rmEntries = new ConcurrentHashMap<Integer, RMProps>(); 

	// Constructors
	public DBTLog(int tLogID, TLogVersion tLogVersion, TLogLocking tLogLock) {

		this.tLogID = tLogID;

		this.tLogVersion = tLogVersion;
		this.tLogLock = tLogLock;

		this.transactionClassifiers = new ConcurrentHashMap<String, Integer>(
				TransactionServiceFrame.maxTransactionClassifiers);
		this.maxTxClassifierID = 0;

		this.txManager = new DBTransactionRecordsManager(TX_RECORDS, tLogID);
	}

	/**
	 * You must call getAllUsedRMIDs() to initialize the rmIDs array
	 */
	public DBTLog(int tLogID, TLogVersion tLogVersion, TLogLocking tLogLock,
			Map<String, Integer> transactionClassifiers, int maxTxClassifierID) {

		this.tLogID = tLogID;

		this.tLogVersion = tLogVersion;
		this.tLogLock = tLogLock;

		this.transactionClassifiers = transactionClassifiers;
		this.maxTxClassifierID = maxTxClassifierID;

		this.txManager = new DBTransactionRecordsManager(TX_RECORDS, tLogID);
	}

	public String getRMName(int id) throws TLogIOException, InvalidRMIDException {
		RMProps props = rmEntries.get(id);

		if(props == null) {
			synchronized (rmEntries) {
				props = rmEntries.get(id);
				if (props == null) {
					try {
						props = getRMProperties(id);
					} catch (InvalidRMKeyException e) {
						throw new InvalidRMIDException(e);
					}
					rmEntries.put(id, props);
				}
			}
		}

		return props.getKeyName();
	}

	// TLog interface functions
	/**
	 * This method will get all RM IDs registered in the
	 * current TLog and will update the local field with
	 * the RM IDs according to the DB
	 */
	public int[] getAllUsedRMIDs() throws TLogIOException {
		if (isClosed) {
			throw new TLogIOException("This transaction log is already closed.");
		}

		boolean exceptionThrown = false;

		ArrayList<Integer> activeIDs = new ArrayList<Integer>(
				rmIDs.length == 0 ? 10 : rmIDs.length);
		ArrayList<Integer> allIDs = new ArrayList<Integer>(
				rmIDs.length == 0 ? 10 : rmIDs.length);
		Connection con = null;

		synchronized (this) {
			try {
				con = TransactionServiceFrame.getDataSourceForDBTlog().getConnection();
				removeUnregistratedRMs(con);
				PreparedStatement stmn = con.prepareStatement(GET_ALL_USED_RM_IDS_STATEMENT);
				// set the TLogID for which you want to get the RM IDs
				stmn.setInt(1, tLogID);
				ResultSet res = stmn.executeQuery();

				while (res.next()) {
					int id = res.getInt(1);
					allIDs.add(id);
					if (RES_MANAGERS_STATUS_ACTIVE == res.getBytes(2)[0]) {
						activeIDs.add(id);
					}
				}

				stmn.close();

			} catch (SQLException e) {
				String msg = "An SQL error occurred";
				if (false == exceptionThrown) {
					exceptionThrown = true;
					throw new TLogIOException(msg, e);
				} else {
					SimpleLogger.traceThrowable(Severity.DEBUG, LOCATION, msg, e);
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

			// update rmIDs
			if (activeIDs.isEmpty()) {
				rmIDs = new int[] {};
			} else {
				int postiveIDsNum = activeIDs.size();
				rmIDs = new int[postiveIDsNum];
				for (int i = 0; i < postiveIDsNum; ++i) {
					rmIDs[i] = activeIDs.get(i).intValue();
				}
			}

			// return RM IDs
			if (allIDs.isEmpty()) {
				return new int[] {};
			} else {
				int allIDsNum = allIDs.size();
				int[] resRMIDs = new int[allIDsNum];
				for (int i = 0; i < allIDsNum; ++i) {
					resRMIDs[i] = allIDs.get(i).intValue();
				}
				return resRMIDs;
			}
		}
	}

	public int getRMIDByName(String keyName) throws TLogIOException,
			InvalidRMKeyException {

		if (isClosed) {
			throw new TLogIOException("This transaction log is already closed.");
		}

		if (null==keyName || keyName.length()==0) {
			throw new InvalidRMKeyException("The resource manager name must not be null or empty.");
		}

		boolean exceptionThrown = false;

		Connection con = null;
		try {
			con = TransactionServiceFrame.getDataSourceForDBTlog().getConnection();
			ResultSet res = executeGetRMIDByNameStatement(con, tLogID, keyName);

			if (res.next()) {
				return res.getInt(1);
			} else {
				// There are no rows selected so there is no
				// resource manager with the specified name
				String msg = "No such resource manager name in this TLog.";
				exceptionThrown = true;
				throw new InvalidRMKeyException(msg);
			}

		} catch (SQLException e) {
			String msg = "An SQL error occurred";
			exceptionThrown = true;
			throw new TLogIOException(msg, e);

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

	public RMPropsExtension getRMProperties(int rmID) throws TLogIOException,
				InvalidRMKeyException {
		if (isClosed) {
			throw new TLogIOException("This transaction log is already closed.");
		}

		boolean exceptionThrown = false;

		Connection con = null;
		try {
			con = TransactionServiceFrame.getDataSourceForDBTlog().getConnection();
			PreparedStatement stmn = con.prepareStatement(GET_RM_PROPERTIES_STATEMENT);
			// set the RM ID
			stmn.setInt(1, rmID);
			// set the TLogID
			stmn.setInt(2, tLogID);
			ResultSet res = stmn.executeQuery();

			RMPropsExtension resProps;
			if (res.next()) {
				resProps = getRMPropsFromResultSet(res);
			} else {
				// There are no rows selected so there is no
				// resource manager with the specified ID
				exceptionThrown = true;
				throw new InvalidRMKeyException("No such resource manager ID in this TLog.");
			}
			stmn.close();
			return resProps;

		} catch (SQLException e) {
			String msg = "An SQL error occurred";
			exceptionThrown = true;
			throw new TLogIOException(msg, e);

		} catch (CrypterException e) {
			String msg = "Cannot decrypt secure properties.";
			exceptionThrown = true;
			throw new TLogIOException(msg, e);

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

	public TLogVersion getTLogVersion(){
		return tLogVersion;		
	}

	public int registerNewRM(RMProps rmProps) throws TLogIOException,
				RMNameAlreadyInUseException {
		if (isClosed) {
			throw new TLogIOException("This transaction log is already closed.");
		}

		if (	null == rmProps ||
				null == rmProps.getSecureProperties() ||
				null == rmProps.getNonSecureProperties() ||
				null == rmProps.getKeyName() ||
				null == rmProps.getRmContainerName()) {
			throw new IllegalArgumentException(
					"Input parameter rmProps and it's fields must not be null.");
		}

		boolean exceptionThrown = false;

		Connection con = null;
		try {
			int rmID;
			con = TransactionServiceFrame.getDataSourceForDBTlog().getConnection();

			synchronized (this) {
				{	// Check that the name of RM is unique in this TLog
					ResultSet res = executeGetRMIDByNameStatement(con, tLogID, rmProps.getKeyName());
					if (res.next()) {
						// If there is an RM with the specified name in 
						// the current TLog throw an exception
						exceptionThrown = true;
						throw new RMNameAlreadyInUseException("The name "
								+ rmProps.getKeyName() + " is already in use by other "
								+ "resource manager in this transaction log");
					}
				}

				con.setAutoCommit(false);
				{	// Generate next ID
					PreparedStatement stmn = con.prepareStatement(GET_MAX_RM_ID_STATEMENT);
					stmn.setInt(1, tLogID);
					ResultSet res = stmn.executeQuery();
					if (res.next())
						rmID = 1 + res.getInt(1);
					else
						rmID = 1;
					stmn.close();
				}
				int[] newRMIDs = new int[rmIDs.length+1];
				System.arraycopy(rmIDs, 0, newRMIDs, 0, rmIDs.length);
				newRMIDs[newRMIDs.length-1] = rmID;

				// Insert RM
				insertRM(con, tLogID, rmID, rmProps);

				// Insert RM properties
				// 1) Secure
				insertRMProperties(con, tLogID, rmID,
						rmProps.getSecureProperties(),
						RM_PROPS_SECURE_PROPERTY);
				// 2) Non secure
				insertRMProperties(con, tLogID, rmID,
						rmProps.getNonSecureProperties(),
						RM_PROPS_NON_SECURE_PROPERTY);
				
				con.commit();

				// If everything is okay then update the
				// rmIDs array. In all exception cases the
				// rmIDs won't be changed.
				rmIDs = newRMIDs;
			}

			con.setAutoCommit(true);
			removeUnregistratedRMs(con);

			rmEntries.put(rmID, rmProps);//TODO
			return rmID;

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
			String msg = "SQL error occurred during registration of a resource manager.";
			exceptionThrown = true;
			throw new TLogIOException(msg, e);

		} catch (CrypterException e) {
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
			String msg = "Encryption error occurred during registration of a resource manager.";
			exceptionThrown = true;
			throw new TLogIOException(msg, e);

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

	public void unregisterRM(int rmID) throws TLogIOException,
			InvalidRMIDException {
		if (isClosed) {
			throw new TLogIOException("This transaction log is already closed.");
		}

		// Check if the rmID exist.
		if (binarySearch(rmIDs, rmID) < 0) {
			throw new InvalidRMIDException("Invalid RM ID specified. RM ID is " + rmID);
		}

		// mark the RM in the DB as unregistered (marked for delete)
		boolean exceptionThrown = false;
		Connection con = null;
		try {
			con = TransactionServiceFrame.getDataSourceForDBTlog().getConnection();
			PreparedStatement stmn = con.prepareStatement(CHANGE_RM_STATUS_STATEMENT);
			try {
				// 1 - New RM Status
				stmn.setBytes(1, new byte[] {RES_MANAGERS_STATUS_MARKED_FOR_DELETE});
				// 2 - RM ID
				stmn.setInt(2, rmID);
				// 3 - TLogID
				stmn.setInt(3, tLogID);
				stmn.executeUpdate();
			} finally {
				stmn.close();
			}

		} catch (SQLException e) {
			exceptionThrown = true;
			throw new TLogIOException(
					"Could not unregister resource manager with ID " + rmID + " because of an SQL exception.",
					e);
		} finally {

			try {
				// Register listener to remove the ID
				RMRemover rmRemover = new RMRemover(rmID);
				
				try{
					TransactionServiceFrame.getTimeoutManager().registerTimeoutListener(rmRemover, TransactionServiceFrame.txTimeout * 2, 0);
				} catch (TimeOutIsStoppedException e){
					LOCATION.traceThrowableT(Severity.WARNING, "Resource manager with ID" +rmID+ " will be removed imediatlly because timeout manager is not available.", e);
					rmRemover.timeout();
				}

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
	}

	// Performance critical function
	public void writeTransactionRecord(TransactionRecord transactionRecord)
			throws TLogIOException, InvalidRMIDException, InvalidTransactionClassifierID {
		if (isClosed) {
			throw new TLogIOException("This transaction log is already closed.");
		}

		if (null==transactionRecord) {
			throw new IllegalArgumentException("Input parameter transactionRecord cannot be null.");
		}

		boolean exceptionThrown = false;

		Connection con = null;
		try {
			con = TransactionServiceFrame.getDataSourceForDBTlog().getConnection();
			PreparedStatement stmn = con.prepareStatement(INSERT_TRANSACTION_RECORD_STATEMENT);
			// 1 - Transaction Number
			stmn.setLong(1, transactionRecord.getTransactionSequenceNumber());
			// 2 - TLog ID
			stmn.setInt(2, tLogID);
			// 3 - Transaction Birth Time
			stmn.setLong(3, transactionRecord.getTransactionBirthTime());
			// 4 - Transaction Abandon Time
			stmn.setLong(4, transactionRecord.getTransactionAbandonTimeout());

			// 5 - Transaction Classifier
			int tcID = transactionRecord.getTransactionClassifierID();
			if (tcID!=0 && !transactionClassifiers.containsValue(tcID)) {
				exceptionThrown = true;
				throw new InvalidTransactionClassifierID("Invalid transaction classifier ID: " + tcID);
			}
			stmn.setInt(5, tcID);

			// convert the array of the RM IDs to byte array
			// and check the RM IDs are valid
			int[] resManagers = transactionRecord.getRMIDs();
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
			stmn.setBytes(7, transactionRecord.getBranchIterators());

			stmn.executeUpdate();
			stmn.close();

		} catch (SQLException e) {
			String msg = "An SQL error occurred";
			if (false == exceptionThrown) {
				exceptionThrown = true;
				throw new TLogIOException(msg, e);
			} else {
				SimpleLogger.traceThrowable(Severity.DEBUG, LOCATION, msg, e);
			}

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
	public void removeTransactionRecordLazily(long txSequenceNumber)
			throws TLogIOException {
		if (isClosed) {
			throw new TLogIOException("This transaction log is already closed.");
		}

		boolean exceptionThrown = false;
		Connection con = null;

		try {
			con = TransactionServiceFrame.getDataSourceForDBTlog().getConnection();
			txManager.removeTransactionRecordLazily(con, txSequenceNumber);

		} catch (SQLException e) {
			String msg = "An SQL error occurred";
			exceptionThrown = true;
			throw new TLogIOException(msg, e);

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
	public void removeTransactionRecordImmediately(long txSequenceNumber)
			throws TLogIOException {
		if (isClosed) {
			throw new TLogIOException("This transaction log is already closed.");
		}

		boolean exceptionThrown = false;
		Connection con = null;
		try {
			con = TransactionServiceFrame.getDataSourceForDBTlog().getConnection();
			txManager.removeTransactionRecordImmediately(con, txSequenceNumber);

		} catch (SQLException e) {
			String msg = "An SQL error occurred";
			exceptionThrown = true;
			throw new TLogIOException(msg, e);

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
	public void flushRemovedTransactionRecords() throws TLogIOException {
		if (isClosed) {
			throw new TLogIOException("This transaction log is already closed.");
		}

		boolean exceptionThrown = false;
		Connection con = null;
		try {
			con = TransactionServiceFrame.getDataSourceForDBTlog().getConnection();
			txManager.flushRemovedTransactionRecords(con);

		} catch (SQLException e) {
			String msg = "An SQL error occurred";
			exceptionThrown = true;
			throw new TLogIOException(msg, e);

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

	public Iterator<TransactionRecord> getAllTransactionRecords()
			throws TLogIOException {
		if (isClosed) {
			throw new TLogIOException("This transaction log is already closed.");
		}

		boolean exceptionThrown = false;
		Connection con = null;
		try {
			con = TransactionServiceFrame.getDataSourceForDBTlog().getConnection();
			return getAllTransactionRecords(con);

		} catch (SQLException e) {
			String msg = "An SQL error occurred";
			exceptionThrown = true;
			throw new TLogIOException(msg, e);

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

	public String getTxClassifierById(int id) throws TLogIOException, InvalidClassifierIDException {
		if(id <= 0 || transactionClassifiers.isEmpty()) {
			throw new InvalidClassifierIDException("There is no such id=" + id + " stored in TLog or it is not positive");
		}

		// if id is in TLog the classifier for it will be returned
		Iterator<Entry<String, Integer>> iterator = transactionClassifiers.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, Integer> entry = iterator.next();
			if(entry.getValue() == id) {
				return entry.getKey();
			}
		}
		// if id is not in TLog the exception will be thrown
		throw new InvalidClassifierIDException("The given id is not stored in TLog");
	}

	public int getIdForTxClassifier(String classifier)
				throws TLogIOException, TLogFullException {
		if (isClosed) {
			throw new TLogIOException("This transaction log is already closed.");
		}

		// If the classifier is null or empty return 0
		// which means "no classifier available"
		if (null==classifier || 0==classifier.length()) {
			return 0;
		}

		{	// unsynchronized check if the classifier already exist
			Integer value = transactionClassifiers.get(classifier);
			if (value!=null) {
				return value;
			}
		}

		synchronized (transactionClassifiers) {
			// Check again if someone has inserted the classifier
			Integer value = transactionClassifiers.get(classifier);
			if (value!=null) {
				return value;
			}

			// If transaction classifier don't exist check if
			// there is room for it
			if (transactionClassifiers.size() >= TransactionServiceFrame.maxTransactionClassifiers) {
				throw new TLogFullException("There is no room for more transaction classifiers.");
			}
			// If there is room generate ID and insert the classifier
			int id = maxTxClassifierID+1;

			boolean exceptionThrown = false;
			Connection con = null;
			try {
				con = TransactionServiceFrame.getDataSourceForDBTlog().getConnection();
				PreparedStatement stmn = con.prepareStatement(INSERT_TX_CLASSIFIER_STATEMENT);
				// 1 - Transaction Classifier ID
				stmn.setInt(1, id);
				// 2 - TLog ID
				stmn.setInt(2, tLogID);
				// 3 - Transaction Classifier
				stmn.setString(3, classifier);
				stmn.executeUpdate();

				// if there are no problems update the set too
				transactionClassifiers.put(classifier, id);
				// mark the id as used
				maxTxClassifierID++;

			} catch (SQLException e) {
				String msg = "An SQL error occurred";
				exceptionThrown = true;
				throw new TLogIOException(msg, e);

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

			// return the generated ID
			return id;
		}
	}

	public void close() throws TLogIOException {
		if (isClosed) {
			throw new TLogIOException("This transaction log is already closed.");
		}

		boolean exceptionThrown = false;
		Connection con = null;
		try {
			con = TransactionServiceFrame.getDataSourceForDBTlog().getConnection();
			// Check if there are active transaction records left
			Iterator<TransactionRecord> txRecords = getAllTransactionRecords(con);
			if (txRecords.hasNext()) {
				// If there are transaction records do nothing
				return;
			}

			// If there are no transaction records for this TLog delete it's
			// RMs, RMs properties, transaction classifiers, and the log itself
			{	// Delete all transaction classifiers for this TLog
				PreparedStatement stmn = con.prepareStatement(REMOVE_ALL_TX_CLASSIFIERS_FOR_TX_LOG_STATEMENT);
				stmn.setInt(1, tLogID);
				stmn.executeUpdate();
				stmn.close();
			}
			{	// Delete all resource manager's properties for this TLog
				PreparedStatement stmn = con.prepareStatement(REMOVE_ALL_RM_PROPERTIES_FOR_TX_LOG_STATEMENT);
				stmn.setInt(1, tLogID);
				stmn.executeUpdate();
				stmn.close();
			}
			{	// Delete all resource managers for this TLog
				PreparedStatement stmn = con.prepareStatement(REMOVE_ALL_RM_FOR_TX_LOG_STATEMENT);
				stmn.setInt(1, tLogID);
				stmn.executeUpdate();
				stmn.close();
			}
			{	// Delete TLog
				PreparedStatement stmn = con.prepareStatement(REMOVE_TLOG_STATEMENT);
				stmn.setInt(1, tLogID);
				stmn.executeUpdate();
				stmn.close();
			}

		} catch (SQLException e) {
			String msg = "An SQL error occurred";
			exceptionThrown = true;
			throw new TLogIOException(msg, e);

		} finally {
			try {
				// release TLog lock
				tLogLock.unlockTLog(tLogVersion.toString());
				isClosed = true;
			} catch (TLogLockingInfrastructureException e1) {
				String msg = "Locking error occurred";
				if (false == exceptionThrown) {
					exceptionThrown = true;
					throw new TLogIOException(msg, e1);
				} else {
					SimpleLogger.traceThrowable(Severity.DEBUG, LOCATION, msg, e1);
				}

			} finally {
				if (null!=con) {
					try {
						con.close();
					} catch (SQLException e2) {
						String msg = "Could not close DB connection because of an SQL exception";
						if (false == exceptionThrown) {
							exceptionThrown = true;
							throw new TLogIOException(msg, e2);
						} else {
							SimpleLogger.traceThrowable(Severity.DEBUG, LOCATION, msg, e2);
						}
					}
				}
			}
		}
	}

	// helpful functions
	/**
	 * This method is used to parse the result set returned
	 * from the getRMPropertiesStatement statement and
	 * return it as RMPropsExtension object. The cursor must
	 * be positioned at the first row. The ResultSet can
	 * be TYPE_FORWARD_ONLY.
	 * 
	 * @throws SQLException if there are problems when working
	 * with ReslutSet
	 * @throws CrypterException 
	 */
	protected static RMPropsExtension getRMPropsFromResultSet(ResultSet res)
				throws SQLException, CrypterException {

		RMPropsExtension p = new RMPropsExtension();

		// Set the RM name and RM container name. They are common
		// for all rows so we take them only from the first
		p.setKeyName(res.getString(1));
		p.setRmContainerName(res.getString(2));

		// Take properties if some
		Properties securProps = new Properties();
		Properties nonSecurProps = new Properties();

		res.getBytes(3);
		// If the PROPERTY_TYPE of the first record
		// is sql null the RM has no properties.
		if (!res.wasNull()) {
			do {
				byte type = (res.getBytes(3))[0];
  				String propName = res.getString(4);
  				byte[] propValue = res.getBytes(5);
  				switch (type) {
  				case RM_PROPS_SECURE_PROPERTY: {
  					String pv = TransactionServiceFrame.getCrypter().decryptString(propValue);
  					securProps.put(propName, pv);
  					break;
  				}
  				case RM_PROPS_NON_SECURE_PROPERTY: {
  					String pv = ByteArrayUtils.convertByteArrayToString(propValue);
  					nonSecurProps.put(propName, pv);
  					break;
  				}

  				default:
  					throw new IllegalStateException("Internal error.");
  				}
			} while (res.next());
		}

		p.setSecureProperties(securProps);
		p.setNonSecureProperties(nonSecurProps);
		return p;
	}

	/**
	 * Get a result set of all RM from one TLog that have
	 * name <i>keyName</i>. The ResultSet is TYPE_FORWARD_ONLY
	 * 
	 * @param con the connection to be used
	 * @param tLogID the ID of the TLog
	 * @param keyName the name of the RM we are searching for
	 * @return a result set of all RMs that have name <i>keyName</i>
	 * @throws SQLException if SQL exception occur
	 */
	protected static ResultSet executeGetRMIDByNameStatement(Connection con, int tLogID,
			String keyName) throws SQLException {
		PreparedStatement stmn = con.prepareStatement(GET_RM_ID_BY_NAME_STATEMENT);
		// set the TLogID for which you want to get the RM ID
		stmn.setInt(1, tLogID);
		// set the RM name
		stmn.setString(2, keyName);
		// set the desired RM status
		stmn.setBytes(3, new byte[] {RES_MANAGERS_STATUS_ACTIVE} );
		ResultSet res = stmn.executeQuery();
		return res;
	}

	/**
	 * Inserts records for all properties in p object for one RM
	 * 
	 * @param rmID the ID of the RM
	 * @param con connection to be used for the insert
	 * @param p the properties to be inserted (not RMProps)
	 * @param propType RM_PROPS_SECURE_PROPERTY or RM_PROPS_NON_SECURE_PROPERTY
	 * @throws SQLException if SQL exception occur
	 * @throws CrypterException 
	 */
	protected static void insertRMProperties(Connection con, int tLogID,
			int rmID, Properties p, byte propType) throws SQLException,
			CrypterException {
		PreparedStatement stmn = con.prepareStatement(INSERT_RM_PROPERTIES_STATEMENT);
		 // 1 - RM ID
		stmn.setInt(1, rmID);
		 // 2 - TLog ID
		stmn.setInt(2, tLogID);
		 // 3 - Property Type
		stmn.setBytes(3, new byte[] { propType } );

		for (Object key : p.keySet()) {
			 // 4 - Property Name
			String name = (String)key;
			stmn.setString(4, name);

			// 5 - Property Value
			String value = p.getProperty(name);
			byte[] v;
			if (RM_PROPS_SECURE_PROPERTY == propType) {
				v = TransactionServiceFrame.getCrypter().encryptString(value);
			} else {
				v = ByteArrayUtils.convertStringToByteArray(value);
			}
			stmn.setBytes(5, v);

			stmn.executeUpdate();
		}
		stmn.close();
	}

	/**
	 *  Insert record for one resource manager into RES_MANAGERS table
	 *  
	 * @param rmProps the properties of the RM to be inserted
	 * @param rmID the ID of the RM
	 * @param con connection to be used for the insert
	 * @throws SQLException if SQL exception occur
	 */
	protected static void insertRM(Connection con, int tLogID,
			int rmID, RMProps rmProps) throws SQLException {
		PreparedStatement stmn = con.prepareStatement(INSERT_RM_STATEMENT);
		try {
			 // 1 - RM ID
			stmn.setInt(1, rmID);
			 // 2 - TLog ID
			stmn.setInt(2, tLogID);
			 // 3 - RM Name
			stmn.setString(3, rmProps.getKeyName());
			 // 4 - RM Container Name
			stmn.setString(4, rmProps.getRmContainerName());
			 // 5 - RM Initial Status
			stmn.setBytes(5, new byte[] {RES_MANAGERS_STATUS_ACTIVE} );
			stmn.executeUpdate();
		} finally {
			stmn.close();
		}
	}

	protected void removeUnregistratedRMs(Connection con) {
		try {
			Set<Integer> rmIDsToBeRemoved = new HashSet<Integer>();
			{
				// Get all marked for delete (unregistrated) RMs
				PreparedStatement stm = con.prepareStatement(GET_ALL_RM_IDS_WITH_GIVEN_STATUS_STATEMENT);
				stm.setInt(1, tLogID);
				stm.setBytes(2, new byte [] {RES_MANAGERS_STATUS_MARKED_FOR_DELETE});
				ResultSet r = stm.executeQuery();
				while (r.next()) {
					rmIDsToBeRemoved.add(r.getInt(1));
				}
				stm.close();
			}

			if (!rmIDsToBeRemoved.isEmpty()) {
				// Get all active transaction records
				Iterator<TransactionRecord> txRecords = getAllTransactionRecords(con);
				// Check which RM are still in use by transactions
				while (txRecords.hasNext()) {
					int[] ids = txRecords.next().getRMIDs();
					for (int id : ids) {
						rmIDsToBeRemoved.remove(new Integer(id));
					}
				}

				// Remove all RMs that are marked as unregistered and
				// there are no records for them (if some)
				if (!rmIDsToBeRemoved.isEmpty()) {
					PreparedStatement removeRM = con.prepareStatement(REMOVE_RM_STATEMENT);
					removeRM.setInt(1, tLogID);
					PreparedStatement removeRMProps = con.prepareStatement(REMOVE_RM_PROPERTIES_STATEMENT);
					removeRMProps.setInt(1, tLogID);

					for (int id : rmIDsToBeRemoved) {
						removeRMProps.setInt(2, id);
						removeRMProps.executeUpdate();
						removeRM.setInt(2, id);
						removeRM.executeUpdate();
					}

					removeRM.close();
					removeRMProps.close();
				}
			}

		} catch (SQLException e) {
			LOCATION.traceThrowableT(Severity.INFO,
					"Could not unregister one or more resource managers " +
					"because of an SQL exception.", e);
		}
	}

	protected Iterator<TransactionRecord> getAllTransactionRecords (Connection con)
			throws SQLException {

		txManager.flushRemovedTransactionRecords(con);
		PreparedStatement stmn = con.prepareStatement(
				GET_ALL_TRANSACTION_RECORDS_STATEMENT);

		try {
			stmn.setInt(1, tLogID);
			ResultSet resSet = stmn.executeQuery();

			ArrayList<TransactionRecord> res =
				new ArrayList<TransactionRecord>();

			while (resSet.next()) {
				TransactionRecordImpl record = new TransactionRecordImpl();

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

				res.add(record);
			}

			return res.iterator();

		} finally {
			stmn.close();
		}
	}


	/**
	 * Timeout listener used to remove unregistered RM from the rmID array and
	 * from the database
	 */
	protected class RMRemover implements TimeoutListener {

		private final int rmIDToBeRemoved;
		private Object associatedObject = null;

		public RMRemover(int rmID) {
			this.rmIDToBeRemoved = rmID;
		}

		public boolean check() {
			return true;
		}

		public void timeout() {
			synchronized (DBTLog.this) {
				// find the RM ID
				int index = binarySearch(rmIDs, rmIDToBeRemoved);
				if (index >= 0) {
					// update rmIDs array
					int tmp[] = new int[rmIDs.length - 1];
					System.arraycopy(rmIDs, 0, tmp, 0, index);
					System.arraycopy(rmIDs, index + 1, tmp, index, tmp.length
							- index);
					rmIDs = tmp;

					rmEntries.remove(rmIDToBeRemoved);
				} else {
					if (LOCATION.beInfo()) {
						LOCATION.infoT("Resource manager with ID {0} not found.",
								new Object[] { rmIDToBeRemoved });
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
