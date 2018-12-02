package com.sap.archtech.daservice.commands;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;
import com.sap.archtech.daservice.exceptions.ArchStoreConfigException;
import com.sap.archtech.daservice.exceptions.InvalidNameException;
import com.sap.archtech.daservice.storage.XmlDasDelete;
import com.sap.archtech.daservice.storage.XmlDasDeleteRequest;
import com.sap.archtech.daservice.storage.XmlDasDeleteResponse;
import com.sap.archtech.daservice.storage.XmlDasMaster;
import com.sap.archtech.daservice.storage.XmlDasMkcol;
import com.sap.archtech.daservice.storage.XmlDasMkcolRequest;
import com.sap.archtech.daservice.storage.XmlDasMkcolResponse;
import com.sap.archtech.daservice.util.IdProvider;
import com.sap.engine.frame.core.locking.LockException;
import com.sap.engine.frame.core.locking.TechnicalLockException;
import com.sap.engine.services.applocking.TableLocking;

public class ModifyPathMethod extends MasterMethod {

	private final static int LOCKTIMEOUT = 10000; // Lock Timeout in ms
	private final static int MAXRESOURCENAMELENGTH = 100;
	private final static int MAXCOLLECTIONURILENGTH = 255;
	private final static String SEL_COL_TAB = "SELECT COLTYPE FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_COL_TAB1 = "SELECT * FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_COL_TAB2 = "SELECT COLID FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_RES_TAB = "SELECT RESID FROM BC_XMLA_RES WHERE RESNAME = ? AND COLID = ?";
	private final static String INS_COL_TAB = "INSERT INTO BC_XMLA_COL (COLID, URI, CREATIONTIME, CREATIONUSER, FROZEN, PARENTCOLID, STOREID, COLTYPE) VALUES (?, ?, ?, ?, ? ,? ,? ,?)";
	private final static String SEL_COL_IDX = "SELECT INDEXID FROM BC_XMLA_COL_INDEX WHERE COLID = ?";
	private final static String INS_COL_IDX = "INSERT INTO BC_XMLA_COL_INDEX (INDEXID, COLID) VALUES (?, ?)";
	private final static String INS_COL_STO = "INSERT INTO BC_XMLA_COL_STORE (COLID, STOREID) VALUES (?, ?)";

	private Connection connection;
	private IdProvider idProvider;
	private TableLocking tlock;
	private ArchStoreConfigLocalHome beanLocalHome;
	private String archive_path;
	private String user;
	private Timestamp dateTime;

	public ModifyPathMethod(HttpServletResponse response,
			Connection connection, IdProvider idProvider, TableLocking tlock,
			ArchStoreConfigLocalHome beanLocalHome, String archive_path,
			String user, Timestamp dateTime) {
		this.response = response;
		this.connection = connection;
		this.idProvider = idProvider;
		this.tlock = tlock;
		this.beanLocalHome = beanLocalHome;
		this.archive_path = archive_path;
		this.user = user;
		this.dateTime = dateTime;
	}

	public boolean execute() throws IOException {

		// Variables
		int hits = 0;
		int createdColsCounter = 0;

		long colId = 0;
		long parentColId = 0;
		long storeId = 0;
		long parentStoreId = 0;
		long parentNextStoreId = 0;

		String parentFrozen = "";
		String parentColType = "";
		String collPath = "";
		String collName = "";

		PreparedStatement pst = null;
		PreparedStatement pst0 = null;
		PreparedStatement pst1 = null;
		PreparedStatement pst2 = null;
		PreparedStatement pst3 = null;
		PreparedStatement pst4 = null;
		PreparedStatement pst5 = null;
		PreparedStatement pst6 = null;
		ResultSet result = null;
		Sapxmla_Config sac = null;
		ArrayList<Sapxmla_Config> rollbackCols = new ArrayList<Sapxmla_Config>();

		// Set 'Logical' Exclusive Table Lock For Synchronizing Home Path
		// Function
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("COLID", new Long(0));
		hm.put("RESNAME", new String("MPH"));
		for (int i = 0; i < 10; i++) {
			try {
				tlock.lock(TableLocking.LIFETIME_TRANSACTION, connection,
						"BC_XMLA_LOCKING", hm,
						TableLocking.MODE_EXCLUSIVE_NONCUMULATIVE);
				break;
			} catch (LockException lex) {

				// $JL-EXC$
				if (i == 9) {
					this.reportError(HttpServletResponse.SC_CONFLICT,
							"MODIFYPATH: The lock for the modify path function can not be granted: "
									+ lex.toString(), lex);

					return false;
				}
			} catch (TechnicalLockException tlex) {

				// $JL-EXC$
				if (i == 9) {
					this
							.reportError(
									HttpServletResponse.SC_CONFLICT,
									"MODIFYPATH: The lock for the modify path function can not be granted for technical reasons: "
											+ tlex.toString(), tlex);

					return false;
				}
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException iex) {

				// $JL-EXC$
				MasterMethod.loc
						.infoT("MODIFYPATH: The current thread is waiting, sleeping, or otherwise paused for a long time and another thread interrupts it.");
			}
		}

		// Check Request Header "archive_path"
		if (this.archive_path == null) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"MODIFYPATH: ARCHIVE_PATH missing from request header");
			return false;
		} else {
			try {
				this.isValidName(this.archive_path, true);
			} catch (InvalidNameException inex) {

				// $JL-EXC$
				this.reportError(DasResponse.SC_INVALID_CHARACTER,
						"MODIFYPATH: " + inex.getMessage());
				return false;
			}
			this.archive_path = this.archive_path.toLowerCase();
			if (!(this.archive_path.indexOf("//") == -1)
					|| !this.archive_path.startsWith("/")
					|| !this.archive_path.endsWith("/")
					|| this.archive_path.length() < 3) {
				this
						.reportError(
								DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
								"MODIFYPATH: Value "
										+ this.archive_path
										+ " of request header ARCHIVE_PATH does not meet specifications");
				return false;
			}
			StringTokenizer sTokenizer = new StringTokenizer(this.archive_path,
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
									"MODIFYPATH: Segment "
											+ pathSegment
											+ " of request header ARCHIVE_PATH does not meet specifications");
					return false;
				}
			}
		}

		// Check If Collection URI Length Exceeds Maximum Length
		if ((this.archive_path.length() - 1) > MAXCOLLECTIONURILENGTH) {
			this.reportError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
					"MODIFYPATH: Length of collection URI "
							+ this.archive_path.substring(0, this.archive_path
									.length() - 1) + " exceeds limit of "
							+ MAXCOLLECTIONURILENGTH);
			return false;
		}

		// Check Request Header "user"
		if ((this.user == null) || (this.user.length() == 0)) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"MODIFYPATH: USER missing from request header ");
			return false;
		}

		// Determine Home Path and Logical Path
		String homePath = "";
		String logPath = "/";
		boolean errorOccurred = false;
		try {
			StringTokenizer st1 = new StringTokenizer(this.archive_path, "/");
			String actualPath = "/";
			pst0 = connection.prepareStatement(SEL_COL_TAB);
			boolean isHomeColReached = false;
			while (st1.hasMoreTokens()) {
				String nextToken = st1.nextToken();
				String colType = "";
				if (isHomeColReached == false) {
					actualPath += nextToken;
					pst0.setString(1, actualPath.trim());
					result = pst0.executeQuery();
					hits = 0;
					while (result.next()) {
						colType = result.getString("COLTYPE");
						hits++;
					}
					if (hits != 1) {
						this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
								"MODIFYPATH: Collection "
										+ actualPath.substring(actualPath
												.lastIndexOf("/") + 1,
												actualPath.length())
										+ " does not exist in any home path");
						errorOccurred = true;
						break;
					}
				} else {
					logPath += nextToken + "/";
				}
				if (colType.equalsIgnoreCase("H")) {
					homePath = actualPath + "/";
					isHomeColReached = true;
				}
				actualPath += "/";
			}
			if (!errorOccurred) {
				result.close();
				pst0.close();
			}
		} catch (SQLException sqlex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "MODIFYPATH: "
					+ sqlex.toString(), sqlex);
			errorOccurred = true;
		} finally {
			try {
				if (pst0 != null)
					pst0.close();
			} catch (SQLException sqlex) {
				this.reportError(DasResponse.SC_SQL_ERROR, "MODIFYPATH: "
						+ sqlex.toString(), sqlex);
				errorOccurred = true;
			}
		}
		if (errorOccurred)
			return false;

		// Check And Create If Necessary Collections Downwards From Home Path
		StringTokenizer st2 = new StringTokenizer(logPath, "/");
		String uri = homePath;
		while (st2.hasMoreTokens()) {

			if (errorOccurred)
				break;

			// Determine Archive Path And Collection Name
			uri += st2.nextToken();
			collPath = uri.substring(0, uri.lastIndexOf("/"));
			collName = uri.substring(uri.lastIndexOf("/") + 1, uri.length());

			try {

				// Check If Parent Collection Exists
				pst1 = connection.prepareStatement(SEL_COL_TAB1);
				pst1.setString(1, collPath.trim());
				result = pst1.executeQuery();
				hits = 0;
				while (result.next()) {
					parentColId = result.getLong("COLID");
					parentFrozen = result.getString("FROZEN");
					parentStoreId = result.getLong("STOREID");
					parentNextStoreId = result.getLong("NEXTSTOREID");
					parentColType = result.getString("COLTYPE");
					hits++;
				}
				result.close();
				pst1.close();

				if (hits == 0) {
					int lastSlashNum = collPath.lastIndexOf("/");
					int strLen = collPath.length();
					if ((lastSlashNum != -1) && (lastSlashNum < strLen))
						this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
								"MODIFYPATH: Parent collection "
										+ collPath.substring(lastSlashNum + 1,
												strLen) + " does not exist");
					else
						this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
								"MODIFYPATH: Parent collection does not exist");
					errorOccurred = true;
					break;
				}

				// Check If Parent Collection Is Frozen
				if (!parentFrozen.equalsIgnoreCase("N")) {
					int lastSlashNum = collPath.lastIndexOf("/");
					int strLen = collPath.length();
					if ((lastSlashNum != -1) && (lastSlashNum < strLen))
						this.reportError(DasResponse.SC_COLLECTION_FROZEN,
								"MODIFYPATH: Parent collection "
										+ collPath.substring(lastSlashNum + 1,
												strLen) + " is frozen");
					else
						this.reportError(DasResponse.SC_COLLECTION_FROZEN,
								"MODIFYPATH: Parent collection is frozen");
					errorOccurred = true;
					break;
				}

				// Check If Parent Collection Is A Home Or Application
				// Collection
				if (!(parentColType.equalsIgnoreCase("H") || parentColType
						.equalsIgnoreCase("A"))) {
					int lastSlashNum = collPath.lastIndexOf("/");
					int strLen = collPath.length();
					if ((lastSlashNum != -1) && (lastSlashNum < strLen))
						this
								.reportError(
										DasResponse.SC_SYSTEM_COLLECTION,
										"MODIFYPATH: Parent collection "
												+ collPath.substring(
														lastSlashNum + 1,
														strLen)
												+ " is a not a home or application collection");
					else
						this
								.reportError(DasResponse.SC_SYSTEM_COLLECTION,
										"MODIFYPATH: Parent collection is not a home or application collection");
					errorOccurred = true;
					break;
				}

				// Set 'Logical' Exclusive Table Lock
				HashMap<String, Object> primKeyMap = new HashMap<String, Object>();
				try {
					primKeyMap.put("COLID", new Long(parentColId));
					primKeyMap.put("RESNAME", new String(collName.trim()));
					tlock.lock(TableLocking.LIFETIME_TRANSACTION, connection,
							"BC_XMLA_LOCKING", primKeyMap,
							TableLocking.MODE_EXCLUSIVE_NONCUMULATIVE,
							LOCKTIMEOUT);
				} catch (LockException lex) {
					int lastSlashNum = collPath.lastIndexOf("/");
					int strLen = collPath.length();
					if ((lastSlashNum != -1) && (lastSlashNum < strLen))
						this.reportError(HttpServletResponse.SC_CONFLICT,
								"MODIFYPATH: The lock for the parent collection "
										+ collPath.substring(lastSlashNum + 1,
												strLen) + " and collection "
										+ collName.trim()
										+ " cannot be granted", lex);
					else
						this.reportError(HttpServletResponse.SC_CONFLICT,
								"MODIFYPATH: The lock for the parent collection id "
										+ parentColId + " and collection "
										+ collName.trim()
										+ " cannot be granted", lex);
					errorOccurred = true;
					break;
				} catch (TechnicalLockException tlex) {
					int lastSlashNum = collPath.lastIndexOf("/");
					int strLen = collPath.length();
					if ((lastSlashNum != -1) && (lastSlashNum < strLen))
						this.reportError(HttpServletResponse.SC_CONFLICT,
								"MODIFYPATH: A technical error occurred while locking parent collection "
										+ collPath.substring(lastSlashNum + 1,
												strLen) + " and collection "
										+ collName.trim(), tlex);
					else
						this.reportError(HttpServletResponse.SC_CONFLICT,
								"MODIFYPATH: A technical error occurred while locking parent collection id "
										+ parentColId + " and collection "
										+ collName.trim(), tlex);
					errorOccurred = true;
					break;
				}

				// Check If Collection Already Exists
				pst2 = connection.prepareStatement(SEL_COL_TAB2);
				pst2.setString(1, collPath.trim() + "/" + collName.trim());
				result = pst2.executeQuery();
				hits = 0;
				while (result.next())
					hits++;
				result.close();
				pst2.close();
				if (hits != 0) {
					uri += "/";
					try {
						tlock.unlock(TableLocking.LIFETIME_TRANSACTION,
								connection, "BC_XMLA_LOCKING", primKeyMap,
								TableLocking.MODE_EXCLUSIVE_NONCUMULATIVE);
					} catch (TechnicalLockException tlex) {
						int lastSlashNum = collPath.lastIndexOf("/");
						int strLen = collPath.length();
						if ((lastSlashNum != -1) && (lastSlashNum < strLen))
							this.reportError(HttpServletResponse.SC_CONFLICT,
									"MODIFYPATH: A technical error occurred while unlocking parent collection "
											+ collPath.substring(
													lastSlashNum + 1, strLen)
											+ " and collection "
											+ collName.trim(), tlex);
						else
							this.reportError(HttpServletResponse.SC_CONFLICT,
									"MODIFYPATH: A technical error occurred while unlocking parent collection id "
											+ parentColId + " and collection "
											+ collName.trim(), tlex);
						errorOccurred = true;
						break;
					}
					continue;
				}

				// Check If A Resource With Identical Name As Collection Exists
				if (collName.trim().length() <= MAXRESOURCENAMELENGTH) {
					pst = connection.prepareStatement(SEL_RES_TAB);
					pst.setString(1, collName.trim());
					pst.setLong(2, parentColId);
					result = pst.executeQuery();
					hits = 0;
					while (result.next())
						hits++;
					result.close();
					pst.close();
					if (hits != 0) {
						this
								.reportError(
										HttpServletResponse.SC_CONFLICT,
										"MODIFYPATH: Name "
												+ collName
												+ " already being used for an existing resource");
						errorOccurred = true;
						break;
					}
				}

				// Determine Correct Archive Store For New Collection
				if (parentNextStoreId == 0)
					// Current Archive Store
					storeId = parentStoreId;
				else
					// New Archive Store
					storeId = parentNextStoreId;

				// Check If An Archive Store Is Assigned
				if (storeId != 0) {

					// Get Archive Store Configuration Data
					sac = this.getArchStoreConfigObject(beanLocalHome, storeId);

					// Create Collection
					XmlDasMkcolRequest mkcolRequest = new XmlDasMkcolRequest(
							sac, collPath.trim() + "/" + collName.trim());
					XmlDasMkcol mkcol = new XmlDasMkcol(mkcolRequest);
					XmlDasMkcolResponse mkcolResponse = mkcol.execute();
					if ((mkcolResponse.getStatusCode() != HttpServletResponse.SC_CREATED)
							&& (mkcolResponse.getStatusCode() != HttpServletResponse.SC_METHOD_NOT_ALLOWED)) {
						if (mkcolResponse.getException() == null)
							this
									.reportError(
											DasResponse.SC_IO_ERROR,
											"MODIFYPATH: Error while creating collection "
													+ collName.trim()
													+ "; response from storage system: "
													+ mkcolResponse
															.getStatusCode()
													+ " "
													+ mkcolResponse
															.getReasonPhrase()
													+ " "
													+ mkcolResponse
															.getEntityBody());
						else
							this
									.reportError(
											DasResponse.SC_IO_ERROR,
											"MODIFYPATH: Error while creating collection "
													+ collName.trim()
													+ "; response from storage system: "
													+ mkcolResponse
															.getStatusCode()
													+ " "
													+ mkcolResponse
															.getReasonPhrase()
													+ " "
													+ mkcolResponse
															.getEntityBody(),
											mkcolResponse.getException());
						errorOccurred = true;
						break;
					} else {

						// Store URI For Possible Error Handling
						sac.storage_system = collPath.trim() + "/"
								+ collName.trim();
						rollbackCols.add(0, sac);
						if (mkcolResponse.getStatusCode() == HttpServletResponse.SC_METHOD_NOT_ALLOWED)
							MasterMethod.cat
									.errorT(
											loc,
											"MODIFYPATH: Collection "
													+ collName.trim()
													+ " already existed on storage system - it is now consistently created in XML DAS");
					}
				}

				// Get New Collection Id
				colId = this.idProvider.getId("BC_XMLA_COL");

				// Check If An Archive Store Is Assigned
				// Insert New Collection Entry Into Table BC_XMLA_COL
				pst3 = connection.prepareStatement(INS_COL_TAB);
				pst3.setLong(1, colId);
				pst3.setString(2, collPath.trim() + "/" + collName.trim());
				pst3.setTimestamp(3, dateTime);
				pst3.setString(4, this.user);
				pst3.setString(5, "N");
				pst3.setLong(6, parentColId);
				if (storeId != 0)
					pst3.setLong(7, storeId);
				else
					pst3.setNull(7, Types.NULL);
				pst3.setString(8, "A");
				pst3.executeUpdate();
				pst3.close();

				// Insert Index Range Into Table BC_XMLA_COL_INDEX
				ArrayList<Long> al = new ArrayList<Long>();
				pst4 = connection.prepareStatement(SEL_COL_IDX);
				pst4.setLong(1, parentColId);
				result = pst4.executeQuery();
				while (result.next())
					al.add(new Long(result.getLong("INDEXID")));
				result.close();
				pst4.close();
				pst5 = connection.prepareStatement(INS_COL_IDX);
				for (int i = 0; i < al.size(); i++) {
					pst5.setLong(1, ((Long) al.get(i)).longValue());
					pst5.setLong(2, colId);
					pst5.executeUpdate();
				}
				pst5.close();

				// Insert Entry Into Table BC_XMLA_COL_STORE
				if (storeId != 0) {
					pst6 = connection.prepareStatement(INS_COL_STO);
					pst6.setLong(1, colId);
					pst6.setLong(2, storeId);
					pst6.executeUpdate();
					pst6.close();
				}

				// Commit Current Collection And Clear Roll Back List
				connection.commit();
				rollbackCols.clear();
			} catch (ArchStoreConfigException ascex) {
				this.reportError(DasResponse.SC_CONFIG_INCONSISTENT,
						"MODIFYPATH: " + ascex.getMessage(), ascex);

				// Roll Back Already Created Collections
				this.deleteCollections(rollbackCols);
				errorOccurred = true;
			} catch (IOException ioex) {
				this.reportError(DasResponse.SC_IO_ERROR, "MODIFYPATH: "
						+ ioex.toString(), ioex);

				// Roll Back Already Created Collections
				this.deleteCollections(rollbackCols);
				errorOccurred = true;
			} catch (SQLException sqlex) {
				this.reportError(DasResponse.SC_SQL_ERROR, "MODIFYPATH: "
						+ sqlex.toString(), sqlex);

				// Roll Back Already Created Collections
				this.deleteCollections(rollbackCols);
				errorOccurred = true;
			} finally {
				try {
					if (pst != null)
						pst.close();
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
				} catch (SQLException sqlex) {
					this.reportError(DasResponse.SC_SQL_ERROR, "MODIFYPATH: "
							+ sqlex.toString(), sqlex);
					errorOccurred = true;
				}
			}
			if (errorOccurred)
				return false;

			// Adjust Archive Path And Collection Counter
			uri += "/";
			createdColsCounter++;

		} // end while
		if (errorOccurred)
			return false;

		// Method Was Successful
		this.response.setContentType("text/xml");
		this.response.setHeader("created_cols", Integer
				.toString(createdColsCounter));
		this.response.setHeader("service_message", "Ok");

		// Set Status
		return true;
	}

	private void deleteCollections(ArrayList<Sapxmla_Config> createdCols) {
		Sapxmla_Config sc = null;
		Iterator<Sapxmla_Config> it = createdCols.iterator();
		while (it.hasNext()) {
			try {
				sc = it.next();
				XmlDasDeleteRequest deleteRequest = new XmlDasDeleteRequest(sc,
						sc.storage_system, "COL");
				XmlDasDelete delete = new XmlDasDelete(deleteRequest);
				XmlDasDeleteResponse deleteResponse = delete.execute();
				if (!(deleteResponse.getStatusCode() == HttpServletResponse.SC_OK
						|| deleteResponse.getStatusCode() == HttpServletResponse.SC_ACCEPTED || deleteResponse
						.getStatusCode() == HttpServletResponse.SC_NO_CONTENT))
					throw new IOException(deleteResponse.getStatusCode() + " "
							+ deleteResponse.getReasonPhrase() + " "
							+ deleteResponse.getEntityBody());
			} catch (IOException ioex) {

				// Write Exception Into Log
				MasterMethod.loc
						.errorT("MODIFYPATH: Collection "
								+ XmlDasMaster.getPhysicalPath(sc,
										sc.storage_system)
								+ " was created and cannot be accessed by SAP XML DAS. To complete the roll back process you must delete it manually from the storage system: "
								+ getStackTrace(ioex));
				continue;
			}

			// Write Success Into Log
			MasterMethod.cat.infoT(loc, "MODIFYPATH: User: " + this.user
					+ " Collection "
					+ XmlDasMaster.getPhysicalPath(sc, sc.storage_system)
					+ " successful deleted during roll back process");
		}
	}
}