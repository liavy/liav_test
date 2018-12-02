package com.sap.archtech.daservice.commands;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.ejb.FinderException;
import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocal;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;
import com.sap.archtech.daservice.exceptions.ArchStoreConfigException;
import com.sap.archtech.daservice.exceptions.InvalidNameException;
import com.sap.archtech.daservice.storage.XmlDasDelete;
import com.sap.archtech.daservice.storage.XmlDasDeleteRequest;
import com.sap.archtech.daservice.storage.XmlDasDeleteResponse;
import com.sap.archtech.daservice.storage.XmlDasLegalHold;
import com.sap.archtech.daservice.storage.XmlDasLegalHoldRequest;
import com.sap.archtech.daservice.storage.XmlDasLegalHoldResponse;
import com.sap.archtech.daservice.storage.XmlDasMaster;
import com.sap.archtech.daservice.storage.XmlDasMkcol;
import com.sap.archtech.daservice.storage.XmlDasMkcolRequest;
import com.sap.archtech.daservice.storage.XmlDasMkcolResponse;
import com.sap.archtech.daservice.storage.XmlDasPropFind;
import com.sap.archtech.daservice.storage.XmlDasPropFindRequest;
import com.sap.archtech.daservice.storage.XmlDasPropFindResponse;
import com.sap.archtech.daservice.storage.XmlDasPropPatch;
import com.sap.archtech.daservice.storage.XmlDasPropPatchRequest;
import com.sap.archtech.daservice.storage.XmlDasPropPatchResponse;
import com.sap.archtech.daservice.util.IdProvider;
import com.sap.engine.frame.core.locking.LockException;
import com.sap.engine.frame.core.locking.TechnicalLockException;
import com.sap.engine.services.applocking.TableLocking;
import com.sap.tc.logging.Severity;

public class SyncHomePathMethod extends MasterMethod {

	private final static String INS_SEL_COL_TAB1 = "SELECT COLTYPE FROM BC_XMLA_COL WHERE URI = ?";
	private final static String INS_SEL_COL_TAB2 = "SELECT URI FROM BC_XMLA_COL WHERE URI LIKE ? AND COLTYPE = 'H'";
	private final static String INS_SEL_COL_TAB3 = "SELECT COLID FROM BC_XMLA_COL WHERE URI = ?";
	private final static String INS_SEL_COL_STO = "SELECT COLID FROM BC_XMLA_COL_STORE WHERE COLID = ? AND STOREID = ?";
	private final static String INS_SEL_COL_STO1 = "SELECT STOREID FROM BC_XMLA_COL_STORE WHERE COLID = ? ORDER BY STOREID DESC";
	private final static String INS_INS_COL_STO = "INSERT INTO BC_XMLA_COL_STORE (COLID, STOREID) VALUES (?, ?)";
	private final static String INS_INS_COL_TAB1 = "INSERT INTO BC_XMLA_COL (COLID, URI, CREATIONTIME, CREATIONUSER, FROZEN, PARENTCOLID, COLTYPE) VALUES (?, ?, ?, ?, ?, ?, ?)";
	private final static String INS_INS_COL_TAB2 = "INSERT INTO BC_XMLA_COL (COLID, URI, CREATIONTIME, CREATIONUSER, FROZEN, PARENTCOLID, STOREID, COLTYPE) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	private final static String DEL_SEL_RES_TAB = "SELECT RESID FROM BC_XMLA_RES WHERE COLID = ?";
	private final static String DEL_SEL_COL_TAB1 = "SELECT COLID, PARENTCOLID, COLTYPE FROM BC_XMLA_COL WHERE URI = ?";
	private final static String DEL_SEL_COL_TAB2 = "SELECT COLID FROM BC_XMLA_COL WHERE URI LIKE ?";
	private final static String DEL_SEL_COL_TAB4 = "SELECT PARENTCOLID FROM BC_XMLA_COL WHERE COLID = ?";
	private final static String DEL_SEL_COL_TAB3 = "SELECT COLID FROM BC_XMLA_COL WHERE URI LIKE ? AND COLTYPE = 'H'";
	private final static String DEL_SEL_COL_STO1 = "SELECT STOREID FROM BC_XMLA_COL_STORE WHERE COLID = ?";
	private final static String DEL_SEL_COL_STO2 = "SELECT COLID FROM BC_XMLA_COL_STORE WHERE COLID = ? AND STOREID = ?";
	private final static String DEL_DEL_COL_STO = "DELETE FROM BC_XMLA_COL_STORE WHERE COLID = ? AND STOREID = ?";
	private final static String DEL_DEL_COL_TAB = "DELETE FROM BC_XMLA_COL WHERE COLID = ?";
	private final static String DEL_DEL_COL_PRP = "DELETE FROM BC_XMLA_COL_PROP WHERE COLID = ?";

	private Connection connection;
	private IdProvider idProvider;
	private TableLocking tlock;
	private ArchStoreConfigLocalHome beanLocalHome;
	private String home_path;
	private String action;
	private String user;
	private String context;
	private String archive_store;
	private Timestamp dateTime;
	private ArrayList<Long> alreadyAssignedColsOnCurrentArchiveStore = new ArrayList<Long>();

	public SyncHomePathMethod(HttpServletResponse response,
			Connection connection, IdProvider idProvider, TableLocking tlock,
			ArchStoreConfigLocalHome beanLocalHome, String home_path,
			String action, String user, String context, String archive_store,
			Timestamp dateTime) {
		this.response = response;
		this.connection = connection;
		this.idProvider = idProvider;
		this.tlock = tlock;
		this.beanLocalHome = beanLocalHome;
		this.home_path = home_path;
		this.action = action;
		this.user = user;
		this.context = context;
		this.archive_store = archive_store;
		this.dateTime = dateTime;
	}

	public boolean execute() throws IOException {

		// Variables
		int hits = 0;

		long colId = 0;
		long storeId = 0;

		PreparedStatement pst1 = null;
		PreparedStatement pst2 = null;
		PreparedStatement pst3 = null;
		PreparedStatement pst4 = null;
		PreparedStatement pst5 = null;
		PreparedStatement pst6 = null;
		PreparedStatement pst7 = null;
		PreparedStatement pst8 = null;
		PreparedStatement pst9 = null;
		PreparedStatement pst10 = null;
		PreparedStatement pst11 = null;
		PreparedStatement pst12 = null;
		PreparedStatement pst13 = null;
		PreparedStatement pst14 = null;
		PreparedStatement pst15 = null;
		ResultSet result = null;
		Sapxmla_Config sac = null;

		// Set 'Logical' Exclusive Table Lock For Synchronizing Home Path
		// Function
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("COLID", new Long(0));
		hm.put("RESNAME", new String("SHP"));
		for (int i = 0; i < 10; i++) {
			try {
				tlock.lock(TableLocking.LIFETIME_TRANSACTION, connection,
						"BC_XMLA_LOCKING", hm,
						TableLocking.MODE_EXCLUSIVE_NONCUMULATIVE);
				break;
			} catch (LockException lex) {

				// $JL-EXC$
				if (i == 9) {
					this
							.reportError(
									HttpServletResponse.SC_CONFLICT,
									"_SYNC_HOME_PATH: The lock for the synchronize home path function can not be granted: "
											+ lex.toString(), lex);

					return false;
				}
			} catch (TechnicalLockException tlex) {

				// $JL-EXC$
				if (i == 9) {
					this
							.reportError(
									HttpServletResponse.SC_CONFLICT,
									"_SYNC_HOME_PATH: The lock for the synchronize home path function can not be granted for technical reasons: "
											+ tlex.toString(), tlex);

					return false;
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException iex) {

				// $JL-EXC$
				MasterMethod.loc
						.infoT("_SYNC_HOME_PATH: The current thread is waiting, sleeping, or otherwise paused for a long time and another thread interrupts it.");
			}
		}

		// Check Request Header "home_path"
		if (this.home_path == null) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"_SYNC_HOME_PATH: HOME_PATH missing from request header");
			return false;
		} else {
			try {
				this.isValidName(this.home_path, true);
			} catch (InvalidNameException inex) {

				// $JL-EXC$
				this.reportError(DasResponse.SC_INVALID_CHARACTER,
						"_SYNC_HOME_PATH: " + inex.getMessage());
				return false;
			}
			this.home_path = this.home_path.toLowerCase();
			if (!(this.home_path.indexOf("//") == -1)
					|| !this.home_path.startsWith("/")
					|| !this.home_path.endsWith("/")
					|| this.home_path.length() < 3) {
				this
						.reportError(
								DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
								"_SYNC_HOME_PATH: Value "
										+ this.home_path
										+ " of request header HOME_PATH does not meet specifications");
				return false;
			}
			StringTokenizer sTokenizer = new StringTokenizer(this.home_path,
					"/");
			while (sTokenizer.hasMoreTokens()) {
				String pathSegment = sTokenizer.nextToken();
				if ((pathSegment.startsWith("_") && (pathSegment
						.endsWith(".xml")))
						|| (pathSegment.startsWith("_") && (pathSegment
								.endsWith(".bin")))
						|| (pathSegment.startsWith("__"))
						|| (pathSegment.endsWith("."))) {
					this
							.reportError(
									DasResponse.SC_INVALID_CHARACTER,
									"_SYNC_HOME_PATH: Segment "
											+ pathSegment
											+ " of request header ARCHIVE_PATH does not meet specifications");
					return false;
				}
			}
		}

		// Check Request Header "context"
		if (this.context == null)
			this.context = "";

		// Check Request Header "action"
		if (this.action == null) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"_SYNC_HOME_PATH: ACTION missing from request header");
			return false;
		} else
			this.action = this.action.toUpperCase();
		if (!(this.action.startsWith("I") || this.action.startsWith("D"))) {
			this
					.reportError(
							DasResponse.SC_KEYWORD_UNKNOWN,
							"_SYNC_HOME_PATH: Value "
									+ this.action
									+ " of request header ACTION does not meet specifications");
			return false;
		}

		// Check Request Header "user"
		if ((this.user == null) || (this.user.length() == 0)) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"_SYNC_HOME_PATH: USER missing from request header ");
			return false;
		}

		/*---------------------------/
		 / Insert New Home Collection /
		 /---------------------------*/
		if (this.action.equalsIgnoreCase("I")) {

			// Create Initial List Of Created Collections For Error Handling
			ArrayList<Sapxmla_Config> createdPaths = new ArrayList<Sapxmla_Config>();

			boolean status = false;
			boolean errorOccurred = false;
			try {

				// Check If Archive Store Is Passed
				boolean isArchiveStorePassed;
				if (this.archive_store != null
						&& this.archive_store.length() > 0)
					isArchiveStorePassed = true;
				else
					isArchiveStorePassed = false;

				// Check If The Requested Home Collection Already Exists
				pst1 = connection.prepareStatement(INS_SEL_COL_TAB3);
				pst1.setString(1, this.home_path.substring(0, this.home_path
						.length() - 1));
				result = pst1.executeQuery();
				hits = 0;
				while (result.next())
					hits++;
				result.close();
				pst1.close();
				if (hits != 0) {
					String homeCollection = this.home_path.substring(0,
							this.home_path.length() - 1);
					int lastSlashNum = homeCollection.lastIndexOf("/");
					int strLen = homeCollection.length();
					if ((lastSlashNum != -1) && (lastSlashNum < strLen))
						this.reportError(DasResponse.SC_HOME_COLLECTION_EXISTS,
								"_SYNC_HOME_PATH: Home collection "
										+ homeCollection.substring(
												lastSlashNum + 1, strLen)
										+ " already exists");
					else
						this
								.reportError(DasResponse.SC_DOES_NOT_EXISTS,
										"_SYNC_HOME_PATH: Home collection already exists");
					errorOccurred = true;
				}

				// Check If Any Home Collection Above The Requested Home
				// Collection
				// Exists
				String actualPath = "";
				if (!errorOccurred) {
					StringTokenizer st1 = new StringTokenizer(this.home_path,
							"/");
					actualPath = "/";
					pst2 = connection.prepareStatement(INS_SEL_COL_TAB1);
					while (st1.hasMoreTokens()) {
						String colType = "";
						actualPath += st1.nextToken();
						pst2.setString(1, actualPath.trim());
						result = pst2.executeQuery();
						hits = 0;
						while (result.next()) {
							colType = result.getString("COLTYPE");
							hits++;
						}
						if (colType.equalsIgnoreCase("H")) {
							this.reportError(HttpServletResponse.SC_CONFLICT,
									"_SYNC_HOME_PATH: Home collection "
											+ actualPath.substring(actualPath
													.lastIndexOf("/") + 1,
													actualPath.length())
											+ " exists in home path "
											+ this.home_path);
							errorOccurred = true;
							break;
						}
						actualPath += "/";
					}
					if (!errorOccurred) {
						result.close();
						pst2.close();
					}
				}

				// Check If Any Home Collection Below The Requested Home
				// Collection Exists
				if (!errorOccurred) {
					String uri = "";
					pst3 = connection.prepareStatement(INS_SEL_COL_TAB2);
					pst3.setString(1, this.home_path + "%");
					result = pst3.executeQuery();
					hits = 0;
					while (result.next()) {
						uri = result.getString("URI");
						hits++;
					}
					result.close();
					pst3.close();
					if (hits != 0) {
						this.reportError(HttpServletResponse.SC_CONFLICT,
								"_SYNC_HOME_PATH: Home collection "
										+ uri.substring(
												uri.lastIndexOf("/") + 1, uri
														.length())
										+ " exists below home path "
										+ this.home_path);
						errorOccurred = true;
					}
				}

				// Get Archive Store Id
				if (!errorOccurred) {
					if (isArchiveStorePassed) {
						Collection<ArchStoreConfigLocal> col = beanLocalHome
								.findByArchiveStore(this.archive_store);
						Iterator<ArchStoreConfigLocal> iter = col.iterator();
						if (col.isEmpty()) {

							// Report Error
							this.reportError(
									DasResponse.SC_CONFIG_INCONSISTENT,
									"_SYNC_HOME_PATH: Archive store "
											+ this.archive_store
											+ " not defined");
							errorOccurred = true;
						} else {
							ArchStoreConfigLocal ascl = iter.next();
							storeId = ((Long) ascl.getPrimaryKey()).longValue();
						}
					}
				}

				// Loop Downwards From Root Node To Home Collection
				if (!errorOccurred) {
					StringTokenizer st2 = new StringTokenizer(this.home_path,
							"/");
					int cntToken = st2.countTokens();
					int actualToken = 0;
					actualPath = "/";
					long parentCol = 0;
					while (st2.hasMoreTokens()) {

						if (errorOccurred)
							break;

						// Adjust Actual Path
						actualToken++;
						actualPath += st2.nextToken();

						// Adjust Boolean Variables
						boolean hasColEntry = false;
						boolean hasColStoreEntry = false;

						// Check If BC_XMLA_COL Entry Exists
						pst4 = connection.prepareStatement(INS_SEL_COL_TAB3);
						pst4.setString(1, actualPath);
						result = pst4.executeQuery();
						hits = 0;
						while (result.next()) {
							colId = result.getLong("COLID");
							hits++;
						}
						result.close();
						pst4.close();
						if (hits != 0) {
							hasColEntry = true;

							// Check If BC_XMLA_COL_STORE Entry Exists
							if (storeId != 0) {
								pst5 = connection
										.prepareStatement(INS_SEL_COL_STO);
								pst5.setLong(1, colId);
								pst5.setLong(2, storeId);
								result = pst5.executeQuery();
								hits = 0;
								while (result.next())
									hits++;
								result.close();
								pst5.close();
								if (hits != 0) {
									hasColStoreEntry = true;
									alreadyAssignedColsOnCurrentArchiveStore
											.add(new Long(colId));
								}
							}
						}

						// Determine Case For Distinction Of Cases
						int shpCase = 0;
						if (!hasColEntry && !isArchiveStorePassed)
							shpCase = 1;
						else if (!hasColEntry && isArchiveStorePassed)
							shpCase = 2;
						else if (hasColEntry && !hasColStoreEntry
								&& isArchiveStorePassed)
							shpCase = 3;

						// Distinction Of Cases
						switch (shpCase) {

						// Case 1
						case 1: {

							// Get New Collection Id
							colId = this.idProvider.getId("BC_XMLA_COL");

							// Insert New Collection Entry Into Table
							// BC_XMLA_COL
							pst6 = connection
									.prepareStatement(INS_INS_COL_TAB1);
							pst6.setLong(1, colId);
							pst6.setString(2, actualPath.trim());
							pst6.setTimestamp(3, dateTime);
							pst6.setString(4, this.user.trim());
							pst6.setString(5, "N");
							pst6.setLong(6, parentCol);
							if (actualToken != cntToken)
								pst6.setString(7, "S");
							else
								pst6.setString(7, "H");
							pst6.executeUpdate();
							pst6.close();

							// Write Successful Home Collection Creation Message
							// Into Log
							if (actualToken == cntToken) {
								int lastSlashNum = actualPath.lastIndexOf("/");
								int strLen = actualPath.length();
								if ((lastSlashNum != -1)
										&& (lastSlashNum < strLen))
									MasterMethod.cat
											.infoT(
													loc,
													"_SYNC_HOME_PATH: Home collection "
															+ actualPath
																	.substring(
																			lastSlashNum + 1,
																			strLen)
															+ " successful created on database from user "
															+ this.user);
								else
									MasterMethod.cat
											.infoT(
													loc,
													"_SYNC_HOME_PATH: Home collection "
															+ actualPath
															+ " successful created on database from user "
															+ this.user);
							}
						}
							break;

						// Case 2
						case 2: {

							// Get Archive Store Configuration Data
							sac = this.getArchStoreConfigObject(beanLocalHome,
									storeId);

							// Create Collection On Storage System
							try {
								XmlDasMkcolRequest mkcolRequest = new XmlDasMkcolRequest(
										sac, actualPath.trim());
								XmlDasMkcol mkcol = new XmlDasMkcol(
										mkcolRequest);
								XmlDasMkcolResponse mkcolResponse = mkcol
										.execute();
								if (mkcolResponse.getStatusCode() != HttpServletResponse.SC_CREATED) {
									if (mkcolResponse.getStatusCode() != HttpServletResponse.SC_METHOD_NOT_ALLOWED) {
										if (mkcolResponse.getException() == null) {
											MasterMethod.cat
													.errorT(
															loc,
															mkcolResponse
																	.getStatusCode()
																	+ " "
																	+ mkcolResponse
																			.getReasonPhrase()
																	+ " "
																	+ mkcolResponse
																			.getEntityBody());
											throw new IOException(mkcolResponse
													.getStatusCode()
													+ " "
													+ mkcolResponse
															.getReasonPhrase()
													+ " "
													+ mkcolResponse
															.getEntityBody());
										} else {
											MasterMethod.cat
													.errorT(
															loc,
															mkcolResponse
																	.getStatusCode()
																	+ " "
																	+ mkcolResponse
																			.getReasonPhrase()
																	+ " "
																	+ mkcolResponse
																			.getEntityBody()
																	+ " "
																	+ getStackTrace(mkcolResponse
																			.getException()));
											throw new IOException(mkcolResponse
													.getStatusCode()
													+ " "
													+ mkcolResponse
															.getReasonPhrase()
													+ " "
													+ mkcolResponse
															.getEntityBody()
													+ " "
													+ mkcolResponse
															.getException()
															.toString());
										}
									} else {
										MasterMethod.cat
												.errorT(
														loc,
														"_SYNC_HOME_PATH: Collection "
																+ actualPath
																		.trim()
																+ " already existed on storage system - it is now consistently created in XML DAS");
									}
								}
							} catch (IOException ioex) {

								// Report Error
								this.reportError(DasResponse.SC_IO_ERROR,
										"_SYNC_HOME_PATH: Archive Store: "
												+ sac.archive_store
												+ ", Archive Path: "
												+ XmlDasMaster.getPhysicalPath(
														sac, actualPath.trim())
												+ ", " + ioex.toString(), ioex);

								// Roll Back Already Created Collections
								this.deleteCollections(createdPaths);
								errorOccurred = true;
								break;
							}

							// Store URI For Possible Error Handling
							createdPaths.add(0, new Sapxmla_Config(
									sac.store_id, sac.archive_store, actualPath
											.trim(), sac.type, sac.win_root,
									sac.unix_root, sac.proxy_host,
									sac.proxy_port));

							// Get New Collection Id
							colId = this.idProvider.getId("BC_XMLA_COL");

							// Insert New Collection Entry Into Table
							// BC_XMLA_COL
							if (actualToken != cntToken) {
								pst6 = connection
										.prepareStatement(INS_INS_COL_TAB1);
								pst6.setLong(1, colId);
								pst6.setString(2, actualPath.trim());
								pst6.setTimestamp(3, dateTime);
								pst6.setString(4, this.user.trim());
								pst6.setString(5, "N");
								pst6.setLong(6, parentCol);
								pst6.setString(7, "S");
								pst6.executeUpdate();
								pst6.close();
							} else {
								pst6 = connection
										.prepareStatement(INS_INS_COL_TAB2);
								pst6.setLong(1, colId);
								pst6.setString(2, actualPath.trim());
								pst6.setTimestamp(3, dateTime);
								pst6.setString(4, this.user.trim());
								pst6.setString(5, "N");
								pst6.setLong(6, parentCol);
								pst6.setLong(7, storeId);
								pst6.setString(8, "H");
								pst6.executeUpdate();
								pst6.close();
							}

							// Insert BC_XMLA_COL_STORE Entry
							pst7 = connection.prepareStatement(INS_INS_COL_STO);
							pst7.setLong(1, colId);
							pst7.setLong(2, storeId);
							pst7.executeUpdate();
							pst7.close();

							// Write Successful Home Collection Creation Message
							// Into Log
							if (actualToken == cntToken) {
								int lastSlashNum = actualPath.lastIndexOf("/");
								int strLen = actualPath.length();
								if ((lastSlashNum != -1)
										&& (lastSlashNum < strLen))
									MasterMethod.cat
											.infoT(
													loc,
													"_SYNC_HOME_PATH: Home collection "
															+ actualPath
																	.substring(
																			lastSlashNum + 1,
																			strLen)
															+ " successful created on archive store "
															+ sac.archive_store
															+ " from user "
															+ this.user);
								else
									MasterMethod.cat
											.infoT(
													loc,
													"_SYNC_HOME_PATH: Home collection "
															+ actualPath
															+ " successful created on archive store "
															+ sac.archive_store
															+ " from user "
															+ this.user);
							}
						}
							break;

						// Case 3
						case 3: {

							// Get Archive Store Configuration Data
							sac = this.getArchStoreConfigObject(beanLocalHome,
									storeId);

							// Create Collection On Storage System
							try {
								XmlDasMkcolRequest mkcolRequest = new XmlDasMkcolRequest(
										sac, actualPath.trim());
								XmlDasMkcol mkcol = new XmlDasMkcol(
										mkcolRequest);
								XmlDasMkcolResponse mkcolResponse = mkcol
										.execute();
								if (mkcolResponse.getStatusCode() != HttpServletResponse.SC_CREATED) {
									if (mkcolResponse.getStatusCode() != HttpServletResponse.SC_METHOD_NOT_ALLOWED) {
										if (mkcolResponse.getException() == null) {
											MasterMethod.cat
													.errorT(
															loc,
															mkcolResponse
																	.getStatusCode()
																	+ " "
																	+ mkcolResponse
																			.getReasonPhrase()
																	+ " "
																	+ mkcolResponse
																			.getEntityBody());
											throw new IOException(mkcolResponse
													.getStatusCode()
													+ " "
													+ mkcolResponse
															.getReasonPhrase()
													+ " "
													+ mkcolResponse
															.getEntityBody());
										} else {
											MasterMethod.cat
													.errorT(
															loc,
															mkcolResponse
																	.getStatusCode()
																	+ " "
																	+ mkcolResponse
																			.getReasonPhrase()
																	+ " "
																	+ mkcolResponse
																			.getEntityBody()
																	+ " "
																	+ getStackTrace(mkcolResponse
																			.getException()));
											throw new IOException(mkcolResponse
													.getStatusCode()
													+ " "
													+ mkcolResponse
															.getReasonPhrase()
													+ " "
													+ mkcolResponse
															.getEntityBody()
													+ " "
													+ mkcolResponse
															.getException()
															.toString());
										}
									} else {
										MasterMethod.cat
												.errorT(
														loc,
														"_SYNC_HOME_PATH: Collection "
																+ actualPath
																		.trim()
																+ " already existed on storage system - it is now consistently created in XML DAS");
									}
								}
							} catch (IOException ioex) {

								// Report Error
								this.reportError(DasResponse.SC_IO_ERROR,
										"_SYNC_HOME_PATH: Archive Store: "
												+ sac.archive_store
												+ ", Archive Path: "
												+ XmlDasMaster.getPhysicalPath(
														sac, actualPath.trim())
												+ ", " + ioex.toString(), ioex);

								// Roll Back Already Created Collections
								this.deleteCollections(createdPaths);
								errorOccurred = true;
								break;
							}

							// Store URI For Possible Error Handling
							createdPaths.add(0, new Sapxmla_Config(
									sac.store_id, sac.archive_store, actualPath
											.trim(), sac.type, sac.win_root,
									sac.unix_root, sac.proxy_host,
									sac.proxy_port));

							// Insert BC_XMLA_COL_STORE Entry
							pst7 = connection.prepareStatement(INS_INS_COL_STO);
							pst7.setLong(1, colId);
							pst7.setLong(2, storeId);
							pst7.executeUpdate();
							pst7.close();

							// Write Successful Home Collection Creation Message
							// Into Log
							if (actualToken == cntToken) {
								int lastSlashNum = actualPath.lastIndexOf("/");
								int strLen = actualPath.length();
								if ((lastSlashNum != -1)
										&& (lastSlashNum < strLen))
									MasterMethod.cat
											.infoT(
													loc,
													"_SYNC_HOME_PATH: Home collection "
															+ actualPath
																	.substring(
																			lastSlashNum + 1,
																			strLen)
															+ " successful created on archive store "
															+ sac.archive_store
															+ " from user "
															+ this.user);
								else
									MasterMethod.cat
											.infoT(
													loc,
													"_SYNC_HOME_PATH: Home collection "
															+ actualPath
															+ " successful created on archive store "
															+ sac.archive_store
															+ " from user "
															+ this.user);
							}
						}
							break;

						// All Other Cases
						default:
							break;
						} // end switch

						if (errorOccurred)
							break;

						// Adjust Actual Path And Parent Collection Id
						actualPath += "/";
						parentCol = colId;
					} // end while
				} // end if

				// Set Possibly Missing Properties And Legal Holds
				if ((storeId != 0) && (sac.type.equalsIgnoreCase("W"))) {
					pst14 = connection.prepareStatement(INS_SEL_COL_TAB3);
					pst15 = connection.prepareStatement(INS_SEL_COL_STO1);
					this.setPropertiesAndLegalHolds(pst14, pst15, result,
							this.home_path, storeId, sac);
					pst14.close();
					pst15.close();
				}

				// Method Was Successful
				if (!errorOccurred) {
					this.response.setHeader("service_message", "Ok");
					status = true;
				}
			} // end try
			catch (ArchStoreConfigException ascex) {

				// Report Error
				this.reportError(DasResponse.SC_CONFIG_INCONSISTENT,
						"_SYNC_HOME_PATH: " + ascex.getMessage(), ascex);

				// Roll Back Already Created Collections
				this.deleteCollections(createdPaths);
			} catch (FinderException fex) {

				// Report Error
				this.reportError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"_SYNC_HOME_PATH: " + fex.getMessage(), fex);

				// Roll Back Already Created Collections
				this.deleteCollections(createdPaths);
			} catch (SQLException sqlex) {

				// Report Error
				this.reportError(DasResponse.SC_SQL_ERROR, "_SYNC_HOME_PATH: "
						+ sqlex.toString(), sqlex);

				// Roll Back Already Created Collections
				this.deleteCollections(createdPaths);

			} catch (Exception ex) {

				// Report Error
				this.reportError(HttpServletResponse.SC_CONFLICT,
						"_SYNC_HOME_PATH: " + ex.toString(), ex);

				// Roll Back Already Created Collections
				this.deleteCollections(createdPaths);

			} finally {
				try {
					if (result != null)
						result.close();
					if (pst1 != null)
						pst1.close();
					if (pst2 != null)
						pst2.close();
					if (pst3 != null)
						pst3.close();
					if (pst4 != null)
						pst4.close();
					if (pst5 != null)
						pst5.close();
					if (pst6 != null)
						pst6.close();
					if (pst7 != null)
						pst7.close();
					if (pst8 != null)
						pst8.close();
					if (pst9 != null)
						pst9.close();
					if (pst10 != null)
						pst10.close();
					if (pst11 != null)
						pst11.close();
					if (pst12 != null)
						pst12.close();
					if (pst13 != null)
						pst13.close();
					if (pst14 != null)
						pst14.close();
					if (pst15 != null)
						pst15.close();
				} catch (SQLException sqlex) {
					this.reportError(DasResponse.SC_SQL_ERROR,
							"_SYNC_HOME_PATH: " + sqlex.toString(), sqlex);
					status = false;
				}
			}
			return status;
		}

		/*--------------------------------/
		 / Delete Existing Home Collection /
		 /--------------------------------*/
		else {

			// Create Initial List Of Created Collections For Error Handling
			ArrayList<Sapxmla_Config> deletedPaths = new ArrayList<Sapxmla_Config>();

			boolean status = false;
			boolean errorOccurred = false;
			try {

				// Adjust Home Path
				String actualPath = this.home_path.substring(0, this.home_path
						.length() - 1);
				long parentColId = 0;
				String colType = "";

				// Check If The Requested Home Collection Exists
				pst1 = connection.prepareStatement(DEL_SEL_COL_TAB1);
				pst1.setString(1, actualPath);
				result = pst1.executeQuery();
				hits = 0;
				while (result.next()) {
					colId = result.getLong("COLID");
					parentColId = result.getLong("PARENTCOLID");
					colType = result.getString("COLTYPE");
					hits++;
				}
				result.close();
				pst1.close();
				if (hits == 0) {
					String homeCollection = actualPath;
					int lastSlashNum = homeCollection.lastIndexOf("/");
					int strLen = homeCollection.length();
					if ((lastSlashNum != -1) && (lastSlashNum < strLen))
						this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
								"_SYNC_HOME_PATH: Home collection "
										+ homeCollection.substring(
												lastSlashNum + 1, strLen)
										+ " does not exist");
					else
						this
								.reportError(DasResponse.SC_DOES_NOT_EXISTS,
										"_SYNC_HOME_PATH: Home collection does not exist");
					errorOccurred = true;
				}
				if (!colType.equalsIgnoreCase("H")) {
					this
							.reportError(HttpServletResponse.SC_CONFLICT,
									"_SYNC_HOME_PATH: Collection is not a home collection");
					errorOccurred = true;
				}

				// Check If Resources Below Requested Home Collection Exists
				if (!errorOccurred) {
					pst2 = connection.prepareStatement(DEL_SEL_RES_TAB);
					pst2.setLong(1, colId);
					result = pst2.executeQuery();
					hits = 0;
					while (result.next())
						hits++;
					result.close();
					pst2.close();
					if (hits != 0) {
						String homeCollection = actualPath;
						int lastSlashNum = homeCollection.lastIndexOf("/");
						int strLen = homeCollection.length();
						if ((lastSlashNum != -1) && (lastSlashNum < strLen))
							this
									.reportError(
											DasResponse.SC_HOME_NOT_EMPTY,
											"_SYNC_HOME_PATH: Home collection "
													+ homeCollection.substring(
															lastSlashNum + 1,
															strLen)
													+ " is not empty; it still contains resources");
						else
							this
									.reportError(DasResponse.SC_HOME_NOT_EMPTY,
											"_SYNC_HOME_PATH: Home collection is not empty; it still contains resources");
						errorOccurred = true;
					}
				}

				// Check If Collections Below Requested Home Collection Exists
				if (!errorOccurred) {
					pst3 = connection.prepareStatement(DEL_SEL_COL_TAB2);
					pst3.setString(1, this.home_path + "%");
					result = pst3.executeQuery();
					hits = 0;
					while (result.next())
						hits++;
					result.close();
					pst3.close();
					if (hits != 0) {
						String homeCollection = actualPath;
						int lastSlashNum = homeCollection.lastIndexOf("/");
						int strLen = homeCollection.length();
						if ((lastSlashNum != -1) && (lastSlashNum < strLen))
							this
									.reportError(
											DasResponse.SC_HOME_NOT_EMPTY,
											"_SYNC_HOME_PATH: Home collection "
													+ homeCollection.substring(
															lastSlashNum + 1,
															strLen)
													+ " is not empty; it still contains collections");
						else
							this
									.reportError(DasResponse.SC_HOME_NOT_EMPTY,
											"_SYNC_HOME_PATH: Home collection is not empty; it still contains collections");
						errorOccurred = true;
					}
				}

				// Check If BC_XMLA_COL_STORE Entry Exists
				if (!errorOccurred) {
					pst4 = connection.prepareStatement(DEL_SEL_COL_STO1);
					pst4.setLong(1, colId);
					result = pst4.executeQuery();
					hits = 0;
					while (result.next()) {
						storeId = result.getLong("STOREID");
						hits++;
					}
					result.close();
					pst4.close();
					if (hits != 0) {

						// Get Archive Store Configuration Data
						sac = this.getArchStoreConfigObject(beanLocalHome,
								storeId);

						// Check If Requested And Stored Archive Store Matches
						if (this.archive_store != null
								&& this.archive_store.length() > 0
								&& sac != null
								&& !this.archive_store.trim().equals(
										sac.archive_store.trim())) {

							// Report Error
							String homeCollection = actualPath;
							int lastSlashNum = homeCollection.lastIndexOf("/");
							int strLen = homeCollection.length();
							if ((lastSlashNum != -1) && (lastSlashNum < strLen))
								this
										.reportError(
												DasResponse.SC_PARAMETERS_INCONSISTENT,
												"_SYNC_HOME_PATH: Home collection "
														+ homeCollection
																.substring(
																		lastSlashNum + 1,
																		strLen)
														+ " is stored in archive store "
														+ sac.archive_store
														+ " and not in archive store "
														+ this.archive_store);
							else
								this.reportError(
										DasResponse.SC_PARAMETERS_INCONSISTENT,
										"_SYNC_HOME_PATH: Home collection is stored in archive store "
												+ sac.archive_store
												+ " and not in archive store "
												+ this.archive_store);
							errorOccurred = true;
						}

						// Delete Collection On Storage System
						if (!errorOccurred) {
							try {
								XmlDasDeleteRequest deleteRequest = new XmlDasDeleteRequest(
										sac, actualPath.trim(), "COL");
								XmlDasDelete delete = new XmlDasDelete(
										deleteRequest);
								XmlDasDeleteResponse deleteResponse = delete
										.execute();
								if (!((deleteResponse.getStatusCode() == HttpServletResponse.SC_OK)
										|| (deleteResponse.getStatusCode() == HttpServletResponse.SC_ACCEPTED) || (deleteResponse
										.getStatusCode() == HttpServletResponse.SC_NO_CONTENT))) {
									if (deleteResponse.getException() == null) {
										MasterMethod.cat
												.errorT(
														loc,
														deleteResponse
																.getStatusCode()
																+ " "
																+ deleteResponse
																		.getReasonPhrase()
																+ " "
																+ deleteResponse
																		.getEntityBody());
										throw new IOException(deleteResponse
												.getStatusCode()
												+ " "
												+ deleteResponse
														.getReasonPhrase()
												+ " "
												+ deleteResponse
														.getEntityBody());
									}

									else {
										MasterMethod.cat
												.errorT(
														loc,
														deleteResponse
																.getStatusCode()
																+ " "
																+ deleteResponse
																		.getReasonPhrase()
																+ " "
																+ deleteResponse
																		.getEntityBody()
																+ " "
																+ getStackTrace(deleteResponse
																		.getException()));
										throw new IOException(deleteResponse
												.getStatusCode()
												+ " "
												+ deleteResponse
														.getReasonPhrase()
												+ " "
												+ deleteResponse
														.getEntityBody()
												+ " "
												+ deleteResponse.getException()
														.toString());
									}
								}
							} catch (IOException ioex) {

								// Report Error
								this.reportError(DasResponse.SC_IO_ERROR,
										"_SYNC_HOME_PATH: Archive Store: "
												+ sac.archive_store
												+ ", Archive Path: "
												+ XmlDasMaster.getPhysicalPath(
														sac, actualPath.trim())
												+ ", " + ioex.toString(), ioex);
								errorOccurred = true;
							}
						}

						if (!errorOccurred) {

							// Store URI For Possible Error Handling
							deletedPaths.add(0, new Sapxmla_Config(
									sac.store_id, sac.archive_store, actualPath
											.trim(), sac.type, sac.win_root,
									sac.unix_root, sac.proxy_host,
									sac.proxy_port));

							// Delete BC_XMLA_COL_STORE Entry
							pst5 = connection.prepareStatement(DEL_DEL_COL_STO);
							pst5.setLong(1, colId);
							pst5.setLong(2, storeId);
							pst5.executeUpdate();
							pst5.close();
						}

					}
				}

				if (!errorOccurred) {
					// Delete BC_XMLA_COL Entry
					pst6 = connection.prepareStatement(DEL_DEL_COL_TAB);
					pst6.setLong(1, colId);
					pst6.executeUpdate();
					pst6.close();

					// Delete BC_XMLA_COL_PROP Entries
					pst13 = this.connection.prepareStatement(DEL_DEL_COL_PRP);
					pst13.setLong(1, colId);
					pst13.executeUpdate();
					pst13.close();

					// Loop Upwards To Root Node
					while (parentColId > 0) {

						if (errorOccurred)
							break;

						// Adjust Collection Id
						colId = parentColId;

						// Adjust Boolean Variables
						boolean hasColStoreEntry = false;
						boolean hasColDescendant = false;
						boolean colDescendantHasColStoreEntry = false;

						// Adjust Actual Path For Further Processing
						actualPath = actualPath.substring(0, actualPath
								.lastIndexOf("/"));

						// Get Parent Collection Id
						pst7 = connection.prepareStatement(DEL_SEL_COL_TAB4);
						pst7.setLong(1, colId);
						result = pst7.executeQuery();
						while (result.next())
							parentColId = result.getLong("PARENTCOLID");
						result.close();
						pst7.close();

						// Check If Collection Has An BC_XMLA_COL_STORE Entry
						pst8 = connection.prepareStatement(DEL_SEL_COL_STO2);
						pst8.setLong(1, colId);
						pst8.setLong(2, storeId);
						result = pst8.executeQuery();
						hits = 0;
						while (result.next())
							hits++;
						result.close();
						pst8.close();
						if (hits != 0)
							hasColStoreEntry = true;

						// Check If Collection Has At Least One Home Collection
						// As Descendant
						ArrayList<Long> descedantHomeCols = new ArrayList<Long>();
						pst9 = connection.prepareStatement(DEL_SEL_COL_TAB3);
						pst9.setString(1, actualPath + "/%");
						result = pst9.executeQuery();
						hits = 0;
						while (result.next()) {
							descedantHomeCols.add(new Long(result
									.getLong("COLID")));
							hits++;
						}
						result.close();
						pst9.close();
						if (hits != 0)
							hasColDescendant = true;

						Iterator<Long> it = descedantHomeCols.iterator();
						while (it.hasNext()) {

							// Check If At Least One Descendant Collection Has
							// An
							// BC_XMLA_COL_STORE Entry
							pst10 = connection
									.prepareStatement(DEL_SEL_COL_STO2);
							pst10.setLong(1, it.next().longValue());
							pst10.setLong(2, storeId);
							result = pst10.executeQuery();
							hits = 0;
							while (result.next())
								hits++;
							result.close();
							pst10.close();
							if (hits != 0) {
								colDescendantHasColStoreEntry = true;
								break;
							}
						}

						// Determine Case For Distinction Of Cases
						int shpCase = 0;
						if (!hasColDescendant && !hasColStoreEntry)
							shpCase = 1;
						else if (!hasColDescendant && hasColStoreEntry)
							shpCase = 2;
						else if (hasColDescendant
								&& !colDescendantHasColStoreEntry
								&& hasColStoreEntry)
							shpCase = 3;

						// Distinction Of Cases
						switch (shpCase) {

						// Case 1
						case 1: {

							// Delete BC_XMLA_COL Entry
							pst11 = connection
									.prepareStatement(DEL_DEL_COL_TAB);
							pst11.setLong(1, colId);
							pst11.executeUpdate();
							pst11.close();

							// Delete BC_XMLA_COL_PROP Entries
							pst13 = this.connection
									.prepareStatement(DEL_DEL_COL_PRP);
							pst13.setLong(1, colId);
							pst13.executeUpdate();
							pst13.close();

							break;
						}

							// Case 2
						case 2: {

							// Get Archive Store Configuration Data
							sac = this.getArchStoreConfigObject(beanLocalHome,
									storeId);

							// Delete Collection On Storage System
							try {
								XmlDasDeleteRequest deleteRequest = new XmlDasDeleteRequest(
										sac, actualPath.trim(), "COL");
								XmlDasDelete delete = new XmlDasDelete(
										deleteRequest);
								XmlDasDeleteResponse deleteResponse = delete
										.execute();
								if (!((deleteResponse.getStatusCode() == HttpServletResponse.SC_OK)
										|| (deleteResponse.getStatusCode() == HttpServletResponse.SC_ACCEPTED) || (deleteResponse
										.getStatusCode() == HttpServletResponse.SC_NO_CONTENT))) {
									if (deleteResponse.getException() == null) {
										MasterMethod.cat
												.errorT(
														loc,
														deleteResponse
																.getStatusCode()
																+ " "
																+ deleteResponse
																		.getReasonPhrase()
																+ " "
																+ deleteResponse
																		.getEntityBody());
										throw new IOException(deleteResponse
												.getStatusCode()
												+ " "
												+ deleteResponse
														.getReasonPhrase()
												+ " "
												+ deleteResponse
														.getEntityBody());
									} else {
										MasterMethod.cat
												.errorT(
														loc,
														deleteResponse
																.getStatusCode()
																+ " "
																+ deleteResponse
																		.getReasonPhrase()
																+ " "
																+ deleteResponse
																		.getEntityBody()
																+ " "
																+ getStackTrace(deleteResponse
																		.getException()));
										throw new IOException(deleteResponse
												.getStatusCode()
												+ " "
												+ deleteResponse
														.getReasonPhrase()
												+ " "
												+ deleteResponse
														.getEntityBody()
												+ " "
												+ deleteResponse.getException()
														.toString());
									}
								}
							} catch (IOException ioex) {

								// Report Error
								this.reportError(DasResponse.SC_IO_ERROR,
										"_SYNC_HOME_PATH: Archive Store: "
												+ sac.archive_store
												+ ", Archive Path: "
												+ XmlDasMaster.getPhysicalPath(
														sac, actualPath.trim())
												+ ", " + ioex.toString(), ioex);

								// Roll Back Already Deleted Collections
								this.createCollections(deletedPaths);
								errorOccurred = true;
								break;
							}

							// Store URI For Possible Error Handling
							deletedPaths.add(0, new Sapxmla_Config(
									sac.store_id, sac.archive_store, actualPath
											.trim(), sac.type, sac.win_root,
									sac.unix_root, sac.proxy_host,
									sac.proxy_port));

							// Delete BC_XMLA_COL_STORE Entry
							pst11 = connection
									.prepareStatement(DEL_DEL_COL_STO);
							pst11.setLong(1, colId);
							pst11.setLong(2, storeId);
							pst11.executeUpdate();
							pst11.close();

							// Delete BC_XMLA_COL Entry
							pst12 = connection
									.prepareStatement(DEL_DEL_COL_TAB);
							pst12.setLong(1, colId);
							pst12.executeUpdate();
							pst12.close();

							// Delete BC_XMLA_COL_PROP Entries
							pst13 = this.connection
									.prepareStatement(DEL_DEL_COL_PRP);
							pst13.setLong(1, colId);
							pst13.executeUpdate();
							pst13.close();

							break;
						}

							// Case 3
						case 3: {

							// Get Archive Store Configuration Data
							sac = this.getArchStoreConfigObject(beanLocalHome,
									storeId);

							// Delete Collection On Storage System
							try {
								XmlDasDeleteRequest deleteRequest = new XmlDasDeleteRequest(
										sac, actualPath.trim(), "COL");
								XmlDasDelete delete = new XmlDasDelete(
										deleteRequest);
								XmlDasDeleteResponse deleteResponse = delete
										.execute();
								if (!((deleteResponse.getStatusCode() == HttpServletResponse.SC_OK)
										|| (deleteResponse.getStatusCode() == HttpServletResponse.SC_ACCEPTED) || (deleteResponse
										.getStatusCode() == HttpServletResponse.SC_NO_CONTENT))) {
									if (deleteResponse.getException() == null) {
										MasterMethod.cat
												.errorT(
														loc,
														deleteResponse
																.getStatusCode()
																+ " "
																+ deleteResponse
																		.getReasonPhrase()
																+ " "
																+ deleteResponse
																		.getEntityBody());
										throw new IOException(deleteResponse
												.getStatusCode()
												+ " "
												+ deleteResponse
														.getReasonPhrase()
												+ " "
												+ deleteResponse
														.getEntityBody());
									} else {
										MasterMethod.cat
												.errorT(
														loc,
														deleteResponse
																.getStatusCode()
																+ " "
																+ deleteResponse
																		.getReasonPhrase()
																+ " "
																+ deleteResponse
																		.getEntityBody()
																+ " "
																+ getStackTrace(deleteResponse
																		.getException()));
										throw new IOException(deleteResponse
												.getStatusCode()
												+ " "
												+ deleteResponse
														.getReasonPhrase()
												+ " "
												+ deleteResponse
														.getEntityBody()
												+ " "
												+ deleteResponse.getException()
														.toString());
									}
								}
							} catch (IOException ioex) {

								// Report Error
								this.reportError(DasResponse.SC_IO_ERROR,
										"_SYNC_HOME_PATH: Archive Store: "
												+ sac.archive_store
												+ ", Archive Path: "
												+ XmlDasMaster.getPhysicalPath(
														sac, actualPath.trim())
												+ ", " + ioex.toString(), ioex);

								// Roll Back Already Deleted Collections
								this.createCollections(deletedPaths);
								errorOccurred = true;
								break;
							}

							// Store URI For Possible Error Handling
							deletedPaths.add(0, new Sapxmla_Config(
									sac.store_id, sac.archive_store, actualPath
											.trim(), sac.type, sac.win_root,
									sac.unix_root, sac.proxy_host,
									sac.proxy_port));

							// Delete BC_XMLA_COL_STORE Entry
							pst11 = connection
									.prepareStatement(DEL_DEL_COL_STO);
							pst11.setLong(1, colId);
							pst11.setLong(2, storeId);
							pst11.executeUpdate();
							pst11.close();

							break;
						}

							// All Other Cases
						default:
							break;

						} // end switch
					} // end while
				} // end if

				if (!errorOccurred) {
					// Write Successful Home Collection Deletion Message Into
					// Log
					actualPath = this.home_path.substring(0, this.home_path
							.length() - 1);
					int lastSlashNum = actualPath.lastIndexOf("/");
					int strLen = actualPath.length();
					if (sac == null) {
						if ((lastSlashNum != -1) && (lastSlashNum < strLen))
							MasterMethod.cat
									.infoT(
											loc,
											"_SYNC_HOME_PATH: Home collection "
													+ actualPath.substring(
															lastSlashNum + 1,
															strLen)
													+ " successful deleted on database from user "
													+ this.user);
						else
							MasterMethod.cat
									.infoT(
											loc,
											"_SYNC_HOME_PATH: Home collection "
													+ actualPath
													+ " successful deleted on database from user "
													+ this.user);
					} else {
						if ((lastSlashNum != -1) && (lastSlashNum < strLen))
							MasterMethod.cat
									.infoT(
											loc,
											"_SYNC_HOME_PATH: Home collection "
													+ actualPath.substring(
															lastSlashNum + 1,
															strLen)
													+ " successful deleted on archive store "
													+ sac.archive_store
													+ " from user " + this.user);
						else
							MasterMethod.cat
									.infoT(
											loc,
											"_SYNC_HOME_PATH: Home collection "
													+ actualPath
													+ " successful deleted on archive store "
													+ sac.archive_store
													+ " from user " + this.user);
					}

					// Method Was Successful
					this.response.setHeader("service_message", "Ok");

					// Set Status
					status = true;
				}

			} // end try
			catch (ArchStoreConfigException ascex) {

				// Report Error
				this.reportError(DasResponse.SC_CONFIG_INCONSISTENT,
						"_SYNC_HOME_PATH: " + ascex.getMessage(), ascex);

				// Roll Back Already Deleted Collections
				this.createCollections(deletedPaths);

			} catch (SQLException sqlex) {

				// Report Error
				this.reportError(DasResponse.SC_SQL_ERROR, "_SYNC_HOME_PATH: "
						+ sqlex.toString(), sqlex);

				// Roll Back Already Deleted Collections
				this.createCollections(deletedPaths);

			} catch (Exception ex) {

				// Report Error
				this.reportError(HttpServletResponse.SC_CONFLICT,
						"_SYNC_HOME_PATH: " + ex.toString(), ex);

				// Roll Back Already Deleted Collections
				this.createCollections(deletedPaths);

			} finally {
				try {
					if (result != null)
						result.close();
					if (pst1 != null)
						pst1.close();
					if (pst2 != null)
						pst2.close();
					if (pst3 != null)
						pst3.close();
					if (pst4 != null)
						pst4.close();
					if (pst5 != null)
						pst5.close();
					if (pst6 != null)
						pst6.close();
					if (pst7 != null)
						pst7.close();
					if (pst8 != null)
						pst8.close();
					if (pst9 != null)
						pst9.close();
					if (pst10 != null)
						pst10.close();
					if (pst11 != null)
						pst11.close();
					if (pst12 != null)
						pst12.close();
					if (pst13 != null)
						pst13.close();
					if (pst14 != null)
						pst14.close();
					if (pst15 != null)
						pst15.close();
				} catch (SQLException sqlex) {
					this.reportError(DasResponse.SC_SQL_ERROR,
							"_SYNC_HOME_PATH: " + sqlex.toString(), sqlex);
					status = false;
				}
			}
			return status;
		}
	}

	private void deleteCollections(ArrayList<Sapxmla_Config> createdPaths) {
		Sapxmla_Config sc = null;
		Iterator<Sapxmla_Config> it = createdPaths.iterator();
		while (it.hasNext()) {
			try {
				sc = it.next();
				XmlDasDeleteRequest deleteRequest = new XmlDasDeleteRequest(sc,
						sc.storage_system, "COL");
				XmlDasDelete delete = new XmlDasDelete(deleteRequest);
				XmlDasDeleteResponse deleteResponse = delete.execute();
				if (!((deleteResponse.getStatusCode() == HttpServletResponse.SC_OK)
						|| (deleteResponse.getStatusCode() == HttpServletResponse.SC_ACCEPTED) || (deleteResponse
						.getStatusCode() == HttpServletResponse.SC_NO_CONTENT))) {
					if (deleteResponse.getException() == null) {
						MasterMethod.cat
								.errorT(
										loc,
										"Collection "
												+ XmlDasMaster.getPhysicalPath(
														sc, sc.storage_system)
												+ " was created and cannot be accessed by SAP XML DAS. To complete the roll back process you must delete it manually from the storage system: "
												+ deleteResponse
														.getStatusCode()
												+ " "
												+ deleteResponse
														.getReasonPhrase()
												+ " "
												+ deleteResponse
														.getEntityBody());
						throw new IOException(
								"Collection "
										+ XmlDasMaster.getPhysicalPath(sc,
												sc.storage_system)
										+ " was created and cannot be accessed by SAP XML DAS. To complete the roll back process you must delete it manually from the storage system: "
										+ deleteResponse.getStatusCode() + " "
										+ deleteResponse.getReasonPhrase()
										+ " " + deleteResponse.getEntityBody());
					} else {
						MasterMethod.cat
								.errorT(
										loc,
										"Collection "
												+ XmlDasMaster.getPhysicalPath(
														sc, sc.storage_system)
												+ " was created and cannot be accessed by SAP XML DAS. To complete the roll back process you must delete it manually from the storage system: "
												+ deleteResponse
														.getStatusCode()
												+ " "
												+ deleteResponse
														.getReasonPhrase()
												+ " "
												+ deleteResponse
														.getEntityBody()
												+ " "
												+ getStackTrace(deleteResponse
														.getException()));
						throw new IOException(
								"Collection "
										+ XmlDasMaster.getPhysicalPath(sc,
												sc.storage_system)
										+ " was created and cannot be accessed by SAP XML DAS. To complete the roll back process you must delete it manually from the storage system: "
										+ deleteResponse.getStatusCode()
										+ " "
										+ deleteResponse.getReasonPhrase()
										+ " "
										+ deleteResponse.getEntityBody()
										+ " "
										+ deleteResponse.getException()
												.toString());
					}
				}
			} catch (IOException ioex) {

				// Write Exception Into Log
				MasterMethod.cat
						.logThrowableT(
								Severity.ERROR,
								loc,
								"_SYNC_HOME_PATH: User: "
										+ this.user
										+ " Following exception occurred while deleting collection "
										+ XmlDasMaster.getPhysicalPath(sc,
												sc.storage_system)
										+ " during roll back: "
										+ ioex.toString(), ioex);
				continue;
			}

			// Write Success Into Log
			MasterMethod.cat.infoT(loc, "_SYNC_HOME_PATH: User: " + this.user
					+ " Collection "
					+ XmlDasMaster.getPhysicalPath(sc, sc.storage_system)
					+ " successful deleted during roll back process");
		}
	}

	private void createCollections(ArrayList<Sapxmla_Config> deletedPaths) {
		Sapxmla_Config sc = null;
		Iterator<Sapxmla_Config> it = deletedPaths.iterator();
		while (it.hasNext()) {
			try {
				sc = it.next();
				XmlDasMkcolRequest mkcolRequest = new XmlDasMkcolRequest(sc,
						sc.storage_system);
				XmlDasMkcol mkcol = new XmlDasMkcol(mkcolRequest);
				XmlDasMkcolResponse mkcolResponse = mkcol.execute();
				if (mkcolResponse.getStatusCode() != HttpServletResponse.SC_CREATED) {
					if (mkcolResponse.getStatusCode() != HttpServletResponse.SC_METHOD_NOT_ALLOWED) {
						if (mkcolResponse.getException() == null) {
							MasterMethod.cat
									.errorT(
											loc,
											"Collection "
													+ XmlDasMaster
															.getPhysicalPath(
																	sc,
																	sc.storage_system)
													+ " was deleted and cannot be recreated by SAP XML DAS. To complete the roll back process you must create it manually on the storage system: "
													+ mkcolResponse
															.getStatusCode()
													+ " "
													+ mkcolResponse
															.getReasonPhrase()
													+ " "
													+ mkcolResponse
															.getEntityBody());
							throw new IOException(
									"Collection "
											+ XmlDasMaster.getPhysicalPath(sc,
													sc.storage_system)
											+ " was deleted and cannot be recreated by SAP XML DAS. To complete the roll back process you must create it manually on the storage system: "
											+ mkcolResponse.getStatusCode()
											+ " "
											+ mkcolResponse.getReasonPhrase()
											+ " "
											+ mkcolResponse.getEntityBody());
						} else {
							MasterMethod.cat
									.errorT(
											loc,
											"Collection "
													+ XmlDasMaster
															.getPhysicalPath(
																	sc,
																	sc.storage_system)
													+ " was deleted and cannot be recreated by SAP XML DAS. To complete the roll back process you must create it manually on the storage system: "
													+ mkcolResponse
															.getStatusCode()
													+ " "
													+ mkcolResponse
															.getReasonPhrase()
													+ " "
													+ mkcolResponse
															.getEntityBody()
													+ " "
													+ getStackTrace(mkcolResponse
															.getException()));
							throw new IOException(
									"Collection "
											+ XmlDasMaster.getPhysicalPath(sc,
													sc.storage_system)
											+ " was deleted and cannot be recreated by SAP XML DAS. To complete the roll back process you must create it manually on the storage system: "
											+ mkcolResponse.getStatusCode()
											+ " "
											+ mkcolResponse.getReasonPhrase()
											+ " "
											+ mkcolResponse.getEntityBody()
											+ " "
											+ mkcolResponse.getException()
													.toString());
						}
					} else {
						MasterMethod.cat
								.errorT(
										loc,
										"_SYNC_HOME_PATH: Collection "
												+ XmlDasMaster.getPhysicalPath(
														sc, sc.storage_system)
												+ " already existed on storage system during roll back process - it is now consistently created in XML DAS");
					}
				}
			} catch (IOException ioex) {

				// Write Exception Into Log
				MasterMethod.cat
						.logThrowableT(
								Severity.ERROR,
								loc,
								"_SYNC_HOME_PATH: User: "
										+ this.user
										+ " Following exception occurred while creating collection "
										+ XmlDasMaster.getPhysicalPath(sc,
												sc.storage_system)
										+ " during roll back: "
										+ ioex.toString(), ioex);
				continue;
			}

			// Write Success Into Log
			MasterMethod.cat.infoT(loc, "_SYNC_HOME_PATH: User: " + this.user
					+ " Collection "
					+ XmlDasMaster.getPhysicalPath(sc, sc.storage_system)
					+ " successful created during roll back process");
		}
	}

	private void setPropertiesAndLegalHolds(PreparedStatement pst1,
			PreparedStatement pst2, ResultSet result, String archive_path,
			long storeId, Sapxmla_Config sac) throws SQLException,
			FinderException, IOException {
		boolean isLegalHoldSet = false;
		short currentILMConformanceClass = 0;
		long currentColID = 0;
		String currentURI = "";
		String expirationDateMemory = "";
		String startOfRetentionMemory = "";
		Sapxmla_Config currentSac = null;

		// Loop Downwards The Archive Path Hierarchy Starting From The
		// Root
		StringTokenizer st = new StringTokenizer(archive_path, "/");
		while (st.hasMoreTokens()) {

			// Calculate Current URI
			currentURI += "/" + st.nextToken();

			// Get Current Collection ID
			pst1.setString(1, currentURI);
			result = pst1.executeQuery();
			currentColID = 0;
			while (result.next())
				currentColID = result.getLong("COLID");
			result.close();

			// Get All Other Archive Stores For Current URI
			pst2.setLong(1, currentColID);
			result = pst2.executeQuery();
			ArrayList<Long> otherStoreIds = new ArrayList<Long>();
			while (result.next()) {
				long tmpStoreId = result.getLong("STOREID");
				if (tmpStoreId != storeId)
					otherStoreIds.add(new Long(tmpStoreId));
			}
			result.close();

			// Check If Collection Is Already Assigned On Other Archive Stores
			if (otherStoreIds.isEmpty() == true)
				break;

			// Check If Collection Is Already Assigned On Current Archive Store
			if (alreadyAssignedColsOnCurrentArchiveStore.contains(new Long(
					currentColID)))
				continue;

			// Check ILM Conformance Class Of Assigned Archive Stores
			currentILMConformanceClass = 0;
			Iterator<Long> storeIter = otherStoreIds.iterator();
			Sapxmla_Config sacILMConfClas0 = null;
			Sapxmla_Config sacILMConfClas1 = null;
			while (storeIter.hasNext()) {
				currentSac = this.beanLocalHome.findByPrimaryKey(
						storeIter.next()).getSapxmla_Config();
				if (currentSac.ilm_conformance >= 2) {
					currentILMConformanceClass = 2;
					break;
				} else if (currentSac.ilm_conformance == 1) {
					if (currentILMConformanceClass == 0) {
						currentILMConformanceClass = 1;
						sacILMConfClas1 = currentSac;
						continue;
					}
				} else if (currentSac.ilm_conformance == 0) {
					if ((currentILMConformanceClass == 0)
							&& (currentSac.type.equalsIgnoreCase("W"))
							&& (sacILMConfClas0 == null)) {
						sacILMConfClas0 = currentSac;
						continue;
					}
				}
			}
			if (currentILMConformanceClass == 0)
				if (sacILMConfClas0 == null)
					break;
				else
					currentSac = sacILMConfClas0;
			else if (currentILMConformanceClass == 1)
				currentSac = sacILMConfClas1;

			// Get All Properties From Current URI Of Other Archive
			// Store
			XmlDasPropFindRequest propFindRequest = new XmlDasPropFindRequest(
					currentSac, currentURI, "*", "COL");
			XmlDasPropFind propFind = new XmlDasPropFind(propFindRequest);
			XmlDasPropFindResponse propFindResponse = propFind.execute();
			if (propFindResponse.getStatusCode() != HttpServletResponse.SC_OK) {

				// Report Error
				if (propFindResponse.getException() == null)
					throw new IOException("PropFind response for archive path "
							+ currentURI + " on archive store "
							+ currentSac.archive_store + ": "
							+ propFindResponse.getStatusCode() + " "
							+ propFindResponse.getReasonPhrase() + " "
							+ propFindResponse.getEntityBody());
				else
					throw new IOException("PropFind response for archive path "
							+ currentURI + " on archive store "
							+ currentSac.archive_store + ": "
							+ propFindResponse.getStatusCode() + " "
							+ propFindResponse.getReasonPhrase() + " "
							+ propFindResponse.getEntityBody() + " "
							+ propFindResponse.getException().toString());
			}
			String currentProperties = propFindResponse.getEntityBody();
			if (currentProperties == null)
				currentProperties = "";
			else if (currentProperties.endsWith("\r\n"))
				currentProperties = currentProperties.substring(0,
						currentProperties.length() - 2);

			// Create A Property Container
			Properties props = new Properties();
			StringTokenizer stprops = new StringTokenizer(currentProperties,
					"#");
			while (stprops.hasMoreTokens()) {
				String prop = stprops.nextToken();
				props.put(prop.substring(0, prop.indexOf("=")).toLowerCase()
						.trim(), prop.substring(prop.indexOf("=") + 1, prop
						.length()));
			}

			// Check If "origin" Property Is Set
			String properties = "";
			if (props.containsKey(ORIGIN)) {
				if (props.getProperty(ORIGIN) == null)
					properties = ORIGIN + "=";
				else
					properties = ORIGIN + "=" + props.getProperty(ORIGIN);
			}

			// Check If "compulsory_destruction_date" Property Is Set
			if (props.containsKey(COMPULSORY_DESTRUCTION_DATE)) {
				if (properties.length() == 0) {
					if (props.getProperty(COMPULSORY_DESTRUCTION_DATE) == null)
						properties = COMPULSORY_DESTRUCTION_DATE + "=";
					else
						properties = COMPULSORY_DESTRUCTION_DATE
								+ "="
								+ props
										.getProperty(COMPULSORY_DESTRUCTION_DATE);
				} else {
					if (props.getProperty(COMPULSORY_DESTRUCTION_DATE) == null)
						properties += "#" + COMPULSORY_DESTRUCTION_DATE + "=";
					else
						properties += "#"
								+ COMPULSORY_DESTRUCTION_DATE
								+ "="
								+ props
										.getProperty(COMPULSORY_DESTRUCTION_DATE);
				}
			}

			// Check If "expiration_date" And "start_of_retention"
			// Properties Are Set
			if (props.containsKey(EXPIRATION_DATE)) {
				String expi_date = props.getProperty(EXPIRATION_DATE);
				if (expi_date == null)
					expi_date = "";
				else
					expi_date = expi_date.toLowerCase();

				// Check If "expiration_date" Property Has Changed To Previous
				// One
				if (!expi_date.equalsIgnoreCase(expirationDateMemory)) {

					// Store "start_of_retention" Property For Further
					// Processing
					if (props.containsKey(START_OF_RETENTION)) {
						if (props.getProperty(START_OF_RETENTION) != null)
							startOfRetentionMemory = props
									.getProperty(START_OF_RETENTION);
						else
							startOfRetentionMemory = "";
					}

					// Property "expiration_date" Is Unspecific
					if (expi_date.startsWith(UNKNOWN)) {
						if (properties.length() == 0) {
							if (startOfRetentionMemory.length() != 0)
								properties = START_OF_RETENTION + "="
										+ startOfRetentionMemory + "#"
										+ EXPIRATION_DATE + "=" + UNKNOWN;
							else
								properties = START_OF_RETENTION + "=" + UNKNOWN
										+ "#" + EXPIRATION_DATE + "=" + UNKNOWN;
						} else {
							if (startOfRetentionMemory.length() != 0)
								properties += "#" + START_OF_RETENTION + "="
										+ startOfRetentionMemory + "#"
										+ EXPIRATION_DATE + "=" + UNKNOWN;
							else
								properties += "#" + START_OF_RETENTION + "="
										+ UNKNOWN + "#" + EXPIRATION_DATE + "="
										+ UNKNOWN;
						}
						expirationDateMemory = UNKNOWN;
					}

					// Property "expiration_date" Is Specific
					else {

						// Get Formatted Date Strings
						int currentDateAsInteger = 0;
						try {
							if (expi_date.length() >= 10)
								currentDateAsInteger = Integer
										.parseInt(expi_date.substring(0, 4)
												+ expi_date.substring(5, 7)
												+ expi_date.substring(8, 10));
						} catch (NumberFormatException nfex) {

							// $JL-EXC$
							currentDateAsInteger = 0;
						}
						if (currentDateAsInteger != 0) {
							Date date = new Date();
							SimpleDateFormat sdf1 = new SimpleDateFormat(
									"yyyyMMdd");
							sdf1.setTimeZone(TimeZone.getTimeZone("UTC"));
							SimpleDateFormat sdf2 = new SimpleDateFormat(
									"yyyy-MM-dd");
							sdf2.setTimeZone(TimeZone.getTimeZone("UTC"));
							int todayDateAsInteger = 0;
							try {
								todayDateAsInteger = Integer.parseInt(sdf1
										.format(date));
							} catch (NumberFormatException nfex) {

								// $JL-EXC$
								todayDateAsInteger = 0;
							}
							if (todayDateAsInteger != 0) {
								String current_expi_date = sdf2.format(date);

								// Check If "expiration_date" Property Has
								// Already Expired
								if (currentDateAsInteger >= todayDateAsInteger) {
									if (properties.length() == 0) {
										if (startOfRetentionMemory.length() != 0)
											properties = START_OF_RETENTION
													+ "="
													+ startOfRetentionMemory
													+ "#" + EXPIRATION_DATE
													+ "=" + expi_date;
										else
											properties = START_OF_RETENTION
													+ "=" + UNKNOWN + "#"
													+ EXPIRATION_DATE + "="
													+ expi_date;
									} else {
										if (startOfRetentionMemory.length() != 0)
											properties += "#"
													+ START_OF_RETENTION + "="
													+ startOfRetentionMemory
													+ "#" + EXPIRATION_DATE
													+ "=" + expi_date;
										else
											properties += "#"
													+ START_OF_RETENTION + "="
													+ UNKNOWN + "#"
													+ EXPIRATION_DATE + "="
													+ expi_date;
									}
									expirationDateMemory = expi_date;
								} else {
									if (properties.length() == 0) {
										if (startOfRetentionMemory.length() != 0)
											properties = START_OF_RETENTION
													+ "="
													+ startOfRetentionMemory
													+ "#" + EXPIRATION_DATE
													+ "=" + current_expi_date;
										else
											properties = START_OF_RETENTION
													+ "=" + UNKNOWN + "#"
													+ EXPIRATION_DATE + "="
													+ current_expi_date;
									} else {
										if (startOfRetentionMemory.length() != 0)
											properties += "#"
													+ START_OF_RETENTION + "="
													+ startOfRetentionMemory
													+ "#" + EXPIRATION_DATE
													+ "=" + current_expi_date;
										else
											properties += "#"
													+ START_OF_RETENTION + "="
													+ UNKNOWN + "#"
													+ EXPIRATION_DATE + "="
													+ current_expi_date;
									}
									expirationDateMemory = current_expi_date;
								}
							}
						}
					}
				}
			}

			// Set Properties
			if (properties.length() != 0) {
				XmlDasPropPatchRequest propPatchRequest = new XmlDasPropPatchRequest(
						sac, currentURI, "Set", properties, "COL");
				XmlDasPropPatch propPatch = new XmlDasPropPatch(
						propPatchRequest);
				XmlDasPropPatchResponse propPatchResponse = propPatch.execute();
				if (propPatchResponse.getStatusCode() != HttpServletResponse.SC_OK) {

					// Report Error
					if (propPatchResponse.getException() == null)
						throw new IOException(
								"PropPatch response for archive path "
										+ currentURI + " on archive store "
										+ sac.archive_store + ": "
										+ propPatchResponse.getStatusCode()
										+ " "
										+ propPatchResponse.getReasonPhrase()
										+ " "
										+ propPatchResponse.getEntityBody());
					else
						throw new IOException(
								"PropPatch response for archive path "
										+ currentURI
										+ " on archive store "
										+ sac.archive_store
										+ ": "
										+ propPatchResponse.getStatusCode()
										+ " "
										+ propPatchResponse.getReasonPhrase()
										+ " "
										+ propPatchResponse.getEntityBody()
										+ " "
										+ propPatchResponse.getException()
												.toString());
				}
			}

			// Get All Legal Holds From Current URI Of Other Archive Store
			if (isLegalHoldSet == false) {
				XmlDasLegalHoldRequest legalHoldRequest = new XmlDasLegalHoldRequest(
						currentSac, currentURI, "N", "Get", "COL");
				XmlDasLegalHold legalHold = new XmlDasLegalHold(
						legalHoldRequest);
				XmlDasLegalHoldResponse legalHoldResponse = legalHold.execute();
				if (legalHoldResponse.getStatusCode() != HttpServletResponse.SC_OK) {

					// Report Error
					if (legalHoldResponse.getException() == null)
						throw new IOException(
								"Legal Hold Get response for archive path "
										+ currentURI + " on archive store "
										+ currentSac.archive_store + ": "
										+ propFindResponse.getStatusCode()
										+ " "
										+ propFindResponse.getReasonPhrase()
										+ " "
										+ propFindResponse.getEntityBody());
					else
						throw new IOException(
								"Legal Hold Get response for archive path "
										+ currentURI
										+ " on archive store "
										+ currentSac.archive_store
										+ ": "
										+ propFindResponse.getStatusCode()
										+ " "
										+ propFindResponse.getReasonPhrase()
										+ " "
										+ propFindResponse.getEntityBody()
										+ " "
										+ propFindResponse.getException()
												.toString());
				}
				String currentLegalHolds = legalHoldResponse.getEntityBody();
				if (currentLegalHolds == null)
					currentLegalHolds = "";
				StringTokenizer stlegalholds = new StringTokenizer(
						currentLegalHolds, "\r\n");

				// Set Legal Holds
				while (stlegalholds.hasMoreTokens()) {
					legalHoldRequest = new XmlDasLegalHoldRequest(sac,
							currentURI, stlegalholds.nextToken(), "Add", "COL");
					legalHold = new XmlDasLegalHold(legalHoldRequest);
					legalHoldResponse = legalHold.execute();
					if (legalHoldResponse.getStatusCode() != HttpServletResponse.SC_OK) {

						// Report Error
						if (legalHoldResponse.getException() == null)
							throw new IOException(
									"Legal Hold Add response for archive path "
											+ currentURI
											+ " on archive store "
											+ sac.archive_store
											+ ": "
											+ propFindResponse.getStatusCode()
											+ " "
											+ propFindResponse
													.getReasonPhrase() + " "
											+ propFindResponse.getEntityBody());
						else
							throw new IOException(
									"Legal Hold Add response for archive path "
											+ currentURI
											+ " on archive store "
											+ sac.archive_store
											+ ": "
											+ propFindResponse.getStatusCode()
											+ " "
											+ propFindResponse
													.getReasonPhrase()
											+ " "
											+ propFindResponse.getEntityBody()
											+ " "
											+ propFindResponse.getException()
													.toString());
					}

					// Set Legal Hold Already Set Flag
					isLegalHoldSet = true;
				}
			}
		}
	}
}