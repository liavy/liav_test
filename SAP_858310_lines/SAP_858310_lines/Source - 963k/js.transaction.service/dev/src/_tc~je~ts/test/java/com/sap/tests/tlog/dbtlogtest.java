package com.sap.tests.tlog;

import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.INBOUND_TX;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.INBOUND_TX_EXTERNAL_XID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.INBOUND_TX_HEURISTIC_OUTCOME;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RES_MANAGERS;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RES_MANAGERS_RM_CONTAINER_NAME;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RES_MANAGERS_RM_ID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RES_MANAGERS_RM_NAME;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RES_MANAGERS_STATUS;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RES_MANAGERS_TLOG_ID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RM_PROPS;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RM_PROPS_PROPERTY_NAME;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RM_PROPS_PROPERTY_TYPE;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RM_PROPS_PROPERTY_VALUE;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RM_PROPS_RM_ID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RM_PROPS_SECURE_PROPERTY;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RM_PROPS_TLOG_ID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TLOG;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TLOG_ID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TLOG_NODE_ID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TLOG_SYSTEM_ID;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.TLOG_TM_STARTUP_TIME;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.engine.interfaces.transaction.RMProps;
import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.facades.crypter.CrypterException;
import com.sap.engine.services.ts.tlog.InvalidRMIDException;
import com.sap.engine.services.ts.tlog.RMNameAlreadyInUseException;
import com.sap.engine.services.ts.tlog.TLogAlreadyExistException;
import com.sap.engine.services.ts.tlog.TLogIOException;
import com.sap.engine.services.ts.tlog.db.DBTLogReaderWriter;
import com.sap.engine.services.ts.facades.timer.SimpleTimeoutManager;
import com.sap.engine.services.ts.tlog.util.TLogLockingImplFS;
import com.sap.engine.services.ts.utils.ByteArrayUtils;
import com.sap.engine.services.ts.utils.TLogVersion;
import com.sap.security.core.server.secstorefs.FileIOException;
import com.sap.security.core.server.secstorefs.FileMissingException;
import com.sap.security.core.server.secstorefs.InvalidStateException;
import com.sap.security.core.server.secstorefs.SecStoreFS;
import com.sap.security.core.server.secstorefs.SecStoreFSException;
import com.sap.tests.utils.AbstractTestScenarios;

public class DBTLogTest extends AbstractTestScenarios {

	@BeforeClass
	public static void initDBAndDBTalbes() {
		AbstractTestScenarios.beforeClass();

		EmbeddedDataSource ds = getDS(true);
		TransactionServiceFrame.setDataSourceForDBTlog(ds);

		Connection con = null;
		try {
			con = ds.getConnection();
			Statement stmn = con.createStatement();

			// drop tables if there are already tables with their names
			String s = "DROP TABLE \"" + RES_MANAGERS + "\"";
			try {
				stmn.execute(s);
			} catch (SQLException e) {
				// $JL-EXC$
			}
			s = "DROP TABLE \"" + TX_RECORDS + "\"";
			try {
				stmn.execute(s);
			} catch (SQLException e) {
				// $JL-EXC$
			}
			s = "DROP TABLE \"" + TX_CLASSIFIER + "\"";
			try {
				stmn.execute(s);
			} catch (SQLException e) {
				// $JL-EXC$
			}
			s = "DROP TABLE \"" + TLOG + "\"";
			try {
				stmn.execute(s);
			} catch (SQLException e) {
				// $JL-EXC$
			}
			s = "DROP TABLE \"" + RM_PROPS + "\"";
			try {
				stmn.execute(s);
			} catch (SQLException e) {
				// $JL-EXC$
			}
			s = "DROP TABLE \"" + INBOUND_TX + "\"";
			try {
				stmn.execute(s);
			} catch (SQLException e) {
				// $JL-EXC$
			}

			// create the tables
			s = "CREATE TABLE \"" + RES_MANAGERS + "\" " + "(" + "\""
					+ RES_MANAGERS_RM_ID + "\" INTEGER NOT NULL, " + "\""
					+ RES_MANAGERS_TLOG_ID + "\" INTEGER NOT NULL, " + "\""
					+ RES_MANAGERS_RM_CONTAINER_NAME
					+ "\" VARCHAR(254)  NOT NULL, " + "\""
					+ RES_MANAGERS_RM_NAME + "\" VARCHAR(254)  NOT NULL, "
					+ "\"" + RES_MANAGERS_STATUS
					+ "\" CHAR(1) FOR BIT DATA NOT NULL)";
			stmn.execute(s);
			s = "ALTER TABLE \"" + RES_MANAGERS + "\" " + "ADD PRIMARY KEY "
					+ "(" + "\"" + RES_MANAGERS_RM_ID + "\", " + "\""
					+ RES_MANAGERS_TLOG_ID + "\")";
			stmn.execute(s);

			s = "CREATE TABLE \"" + TX_RECORDS + "\" " + "(" + "\""
					+ TX_RECORDS_TX_NUMBER + "\" BIGINT NOT NULL, " + "\""
					+ TX_RECORDS_TLOG_ID + "\" INTEGER NOT NULL, " + "\""
					+ TX_RECORDS_TX_BIRTH_TIME + "\" BIGINT NOT NULL, " + "\""
					+ TX_RECORDS_TX_ABANDON_TIME + "\" BIGINT NOT NULL, "
					+ "\"" + TX_RECORDS_TX_CLASSIFIER + "\" INTEGER, " + "\""
					+ TX_RECORDS_BRANCH_IDS_LIST
					+ "\" VARCHAR(128) FOR BIT DATA NOT NULL, " + "\""
					+ TX_RECORDS_BRANCH_ITER_LIST
					+ "\" VARCHAR(32) FOR BIT DATA NOT NULL)";
			stmn.execute(s);
			s = "ALTER TABLE \"" + TX_RECORDS + "\" " + "ADD PRIMARY KEY ( \""
					+ TX_RECORDS_TX_NUMBER + "\", \"" + TX_RECORDS_TLOG_ID
					+ "\" )";
			stmn.execute(s);

			s = "CREATE TABLE \"" + INBOUND_TX + "\" " + "(" + "\""
					+ TX_RECORDS_TX_NUMBER + "\" BIGINT NOT NULL, " + "\""
					+ TX_RECORDS_TLOG_ID + "\" INTEGER NOT NULL, " + "\""
					+ TX_RECORDS_TX_BIRTH_TIME + "\" BIGINT NOT NULL, " + "\""
					+ TX_RECORDS_TX_ABANDON_TIME + "\" BIGINT NOT NULL, "
					+ "\"" + TX_RECORDS_TX_CLASSIFIER + "\" INTEGER, " + "\""
					+ TX_RECORDS_BRANCH_IDS_LIST
					+ "\" VARCHAR(128) FOR BIT DATA NOT NULL, " + "\""
					+ TX_RECORDS_BRANCH_ITER_LIST
					+ "\" VARCHAR(32) FOR BIT DATA NOT NULL, " + "\""
					+ INBOUND_TX_EXTERNAL_XID
					+ "\" CHAR(134) FOR BIT DATA NOT NULL, " + "\""
					+ INBOUND_TX_HEURISTIC_OUTCOME + "\" INTEGER NOT NULL)";
			stmn.execute(s);
			s = "ALTER TABLE \"" + INBOUND_TX + "\" " + "ADD PRIMARY KEY ( \""
					+ TX_RECORDS_TX_NUMBER + "\", \"" + TX_RECORDS_TLOG_ID
					+ "\" )";
			stmn.execute(s);

			s = "CREATE TABLE \"" + TX_CLASSIFIER + "\" (" + "\""
					+ TX_CLASSIFIER_TX_CLASSIFIER_ID + "\" INTEGER NOT NULL, "
					+ "\"" + TX_CLASSIFIER_TLOG_ID + "\" INTEGER NOT NULL, "
					+ "\"" + TX_CLASSIFIER_CLASSIFIER
					+ "\" VARCHAR(128) NOT NULL)";
			stmn.execute(s);
			s = "ALTER TABLE \"" + TX_CLASSIFIER + "\" "
					+ "ADD PRIMARY KEY ( \"" + TX_CLASSIFIER_TX_CLASSIFIER_ID
					+ "\", \"" + TX_CLASSIFIER_TLOG_ID + "\" )";
			stmn.execute(s);

			s = "CREATE TABLE \"" + TLOG + "\" (" + "\"" + TLOG_ID
					+ "\" INTEGER NOT NULL, " + "\"" + TLOG_SYSTEM_ID
					+ "\" CHAR(3) FOR BIT DATA NOT NULL, " + "\""
					+ TLOG_NODE_ID + "\" INTEGER NOT NULL, " + "\""
					+ TLOG_TM_STARTUP_TIME + "\" BIGINT NOT NULL)";
			stmn.execute(s);
			s = "ALTER TABLE \"" + TLOG + "\" " + "ADD PRIMARY KEY ( \""
					+ TLOG_ID + "\" )";
			stmn.execute(s);

			s = "CREATE TABLE \"" + RM_PROPS + "\" (" + "\"" + RM_PROPS_RM_ID
					+ "\" INTEGER NOT NULL, " + "\"" + RM_PROPS_TLOG_ID
					+ "\" INTEGER NOT NULL, " + "\"" + RM_PROPS_PROPERTY_TYPE
					+ "\" CHAR(1) FOR BIT DATA NOT NULL, " + "\""
					+ RM_PROPS_PROPERTY_NAME + "\" VARCHAR(254) NOT NULL, "
					+ "\"" + RM_PROPS_PROPERTY_VALUE
					+ "\" VARCHAR(1024) FOR BIT DATA NOT NULL)";
			stmn.execute(s);
		} catch (SQLException e) {
			fail(getStackTrace(e));
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					fail(getStackTrace(e));
				}
			}
		}
	}

	@Before
	@Override
	public void init() {
		writer = new DBTLogReaderWriter(TLogLockingImplFS.getInstance(10000));
		TLogVersion ver = new TLogVersion(new byte[] { 'S', 'A', 'P' }, 4,
				5000000);
		try {
			tlog = writer.createNewTLog(ver.getTLogVersion());
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (TLogAlreadyExistException e) {
			fail(getStackTrace(e));
		}
		assertTrue(ver.equals(tlog.getTLogVersion()));
	}

	protected void reencrypt() {
		EmbeddedDataSource ds = getDS(false);
		Connection con = null;
		try {
			con = ds.getConnection();
			TransactionServiceFrame.getCrypter().reencryptDBTLog(con);
		} catch (SQLException e) {
			fail(getStackTrace(e));
		} catch (CrypterException e) {
			fail(getStackTrace(e));
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					fail(getStackTrace(e));
				}
			}
		}
	}

	@Test
	public void testReencryption() {
		// create rm1
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
		SecStoreFS ss = new SecStoreFS();
		try {
			int rmID = tlog.registerNewRM(props1);
			ss.openExistingStore();
			ss.migrateEncryptionKey("A new key phrase for reencryption", true);
			reencrypt();
			RMProps props = tlog.getRMProperties(rmID);
			assertTrue(props1.equals(props));

		} catch (Exception e) {
			fail(getStackTrace(e));
		} finally {
			try {
				ss.deleteBackupFiles();
			} catch (FileMissingException e) {
				fail(getStackTrace(e));
			} catch (InvalidStateException e) {
				fail(getStackTrace(e));
			} catch (FileIOException e) {
				fail(getStackTrace(e));
			} catch (SecStoreFSException e) {
				fail(getStackTrace(e));
			} finally {
				try {
					tlog.close();
					assertNull(writer.lockOrphanedTLog()); // / should see
															// creation of tlogs
															// and which files
															// lasts
				} catch (TLogIOException e) {
					fail(getStackTrace(e));
				}
			}
		}
	}

	protected Properties getPlainSecureProps() {
		Properties p = new Properties();

		EmbeddedDataSource ds = getDS(false);
		Connection con = null;
		try {
			con = ds.getConnection();
			PreparedStatement stmn = con.prepareStatement("SELECT "
					+ RM_PROPS_PROPERTY_NAME + ", " + RM_PROPS_PROPERTY_VALUE
					+ " FROM " + RM_PROPS + " WHERE " + RM_PROPS_PROPERTY_TYPE
					+ " = ?");
			stmn.setBytes(1, new byte[] { RM_PROPS_SECURE_PROPERTY });
			ResultSet res = stmn.executeQuery();
			while (res.next()) {
				p.setProperty(res.getString(1), ByteArrayUtils
						.convertByteArrayToString(res.getBytes(2)));
			}
		} catch (SQLException e) {
			fail(getStackTrace(e));
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					fail(getStackTrace(e));
				}
			}
		}

		return p;
	}

	private static EmbeddedDataSource getDS(boolean create) {
		EmbeddedDataSource ds = new EmbeddedDataSource();
		if (create) {
			ds.setCreateDatabase("create");
		}
		ds.setDatabaseName("TestDataBase");
		ds.setPassword("APP");
		ds.setUser("APP");
		return ds;
	}

	@Test
	public void testRMPropertiesDeletetion() {
		// rm properties
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

		// register rm
		int rmID = 0;
		try {
			rmID = tlog.registerNewRM(props1);
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (RMNameAlreadyInUseException e) {
			fail(getStackTrace(e));
		}

		// remove rm
		try {
			tlog.unregisterRM(rmID);
			try {
				Thread.sleep(TransactionServiceFrame.txTimeout * 3);
			} catch (InterruptedException e) {
			}
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		} catch (InvalidRMIDException e) {
			fail(getStackTrace(e));
		}

		try {
			// execute it to delete physically the RM
			tlog.getAllUsedRMIDs();
		} catch (TLogIOException e) {
			fail(getStackTrace(e));
		}

		EmbeddedDataSource ds = getDS(true);
		Connection con = null;
		try {
			con = ds.getConnection();

			// get rm properties
			// since there are no other TLogs we don't need to specify the TLog
			// ID
			PreparedStatement stmn = con.prepareStatement("SELECT "
					+ RM_PROPS_RM_ID + " FROM " + RM_PROPS + " WHERE "
					+ RM_PROPS_RM_ID + " = ?");
			stmn.setInt(1, rmID); // set rm id

			ResultSet set = stmn.executeQuery();

			boolean hasNext = set.next();
			assertFalse(hasNext); // assert if properties are deleted
			stmn.close();
		} catch (SQLException e) {
			fail(getStackTrace(e));
		} catch (Exception e) {
			fail(getStackTrace(e));
		} finally {
			if (null != con) {
				try {
					con.close();
				} catch (SQLException e) {
					fail(getStackTrace(e));
				} finally {
					try {
						tlog.close();
						assertNull(writer.lockOrphanedTLog()); // / should see
																// creation of
																// tlogs and
																// which files
																// lasts
					} catch (TLogIOException e1) {
						fail(getStackTrace(e1));
					}
				}
			}
		}
	}

}
