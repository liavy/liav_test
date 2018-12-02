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

public class MkcolMethod extends MasterMethod {

	private final static int MAXRESOURCENAMELENGTH = 100;
	private final static int MAXCOLLECTIONURILENGTH = 255;
	private final static String SEL_COL_TAB1 = "SELECT * FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_COL_TAB2 = "SELECT COLID FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_RES_TAB = "SELECT RESID FROM BC_XMLA_RES WHERE COLID = ? AND DELSTATUS IN ('Y', 'N', 'P') AND RESNAME = ?";
	private final static String INS_COL_TAB = "INSERT INTO BC_XMLA_COL (COLID, URI, CREATIONTIME, CREATIONUSER, FROZEN, PARENTCOLID, STOREID, COLTYPE) VALUES (?, ?, ?, ?, ? ,? ,? ,?)";
	private final static String SEL_COL_IDX = "SELECT INDEXID FROM BC_XMLA_COL_INDEX WHERE COLID = ?";
	private final static String INS_COL_IDX = "INSERT INTO BC_XMLA_COL_INDEX (INDEXID, COLID) VALUES (?, ?)";
	private final static String INS_COL_STO = "INSERT INTO BC_XMLA_COL_STORE (COLID, STOREID) VALUES (?, ?)";

	private Connection connection;
	private IdProvider idProvider;
	private TableLocking tlock;
	private ArchStoreConfigLocalHome beanLocalHome;
	private String archive_path;
	private String collection_name;
	private String user;
	private Timestamp dateTime;

	public MkcolMethod(HttpServletResponse response, Connection connection,
			IdProvider idProvider, TableLocking tlock,
			ArchStoreConfigLocalHome beanLocalHome, String archive_path,
			String collection_name, String user, Timestamp dateTime) {
		this.response = response;
		this.connection = connection;
		this.idProvider = idProvider;
		this.tlock = tlock;
		this.beanLocalHome = beanLocalHome;
		this.archive_path = archive_path;
		this.collection_name = collection_name;
		this.user = user;
		this.dateTime = dateTime;
	}

	public boolean execute() throws IOException {

		// Variables
		long colId = 0;
		long parentColId = 0;
		long storeId = 0;
		long parentStoreId = 0;
		long parentNextStoreId = 0;

		String parentFrozen = "";
		String parentColType = "";

		PreparedStatement pst0 = null;
		PreparedStatement pst1 = null;
		PreparedStatement pst2 = null;
		PreparedStatement pst3 = null;
		PreparedStatement pst4 = null;
		PreparedStatement pst5 = null;
		PreparedStatement pst6 = null;
		ResultSet result = null;
		Sapxmla_Config sac = null;

		// Check Request Header "archive_path"
		if (this.archive_path == null) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"MKCOL: ARCHIVE_PATH missing from request header");
			return false;
		} else {
			try {
				this.isValidName(this.archive_path, true);
			} catch (InvalidNameException inex) {

				// $JL-EXC$
				this.reportError(DasResponse.SC_INVALID_CHARACTER, "MKCOL: "
						+ inex.getMessage());
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
								"MKCOL: Value "
										+ this.archive_path
										+ " of request header ARCHIVE_PATH does not meet specifications");
				return false;
			}
		}

		// Check Request Header "collection_name"
		if (this.collection_name == null) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"MKCOL: COLLECTION_NAME missing from request header ");
			return false;
		} else {
			try {
				this.isValidName(this.collection_name, false);
			} catch (InvalidNameException inex) {

				// $JL-EXC$
				this.reportError(DasResponse.SC_INVALID_CHARACTER, "MKCOL: "
						+ inex.getMessage());
				return false;
			}
			this.collection_name = this.collection_name.toLowerCase();
			if (this.collection_name.length() == 0) {
				this
						.reportError(
								DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
								"MKCOL: Value "
										+ this.collection_name
										+ " of request header COLLECTION_NAME does not meet specifications");
				return false;
			}
			if ((this.collection_name.startsWith("_") && (this.collection_name
					.endsWith(".xml")))
					|| (this.collection_name.startsWith("_") && (this.collection_name
							.endsWith(".bin")))
					|| (this.collection_name.startsWith("__"))
					|| (this.collection_name.endsWith("."))) {
				this
						.reportError(
								DasResponse.SC_INVALID_CHARACTER,
								"MKCOL: Value "
										+ this.collection_name
										+ " of request header COLLECTION_NAME does not meet specifications");
				return false;
			}
		}

		// Check If Collection URI Length Exceeds Maximum Length
		if ((this.archive_path.length() + this.collection_name.length()) > MAXCOLLECTIONURILENGTH) {
			this.reportError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
					"MKCOL: Length of collection URI " + this.archive_path
							+ this.collection_name + " exceeds limit of "
							+ MAXCOLLECTIONURILENGTH);
			return false;
		}

		// Check Request Header "user"
		if ((this.user == null) || (this.user.length() == 0)) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"MKCOL: USER missing from request header ");
			return false;
		}

		boolean errorOccurred = false;
		try {

			// Adjust Archive Path For Further Processing
			this.archive_path = this.archive_path.substring(0,
					this.archive_path.length() - 1);

			// Check If Parent Collection Exists
			pst0 = connection.prepareStatement(SEL_COL_TAB1);
			pst0.setString(1, this.archive_path.trim());
			result = pst0.executeQuery();
			int hits = 0;
			while (result.next()) {
				parentColId = result.getLong("COLID");
				parentFrozen = result.getString("FROZEN");
				parentStoreId = result.getLong("STOREID");
				parentNextStoreId = result.getLong("NEXTSTOREID");
				parentColType = result.getString("COLTYPE");
				hits++;
			}
			result.close();
			pst0.close();

			if (hits == 0) {
				int lastSlashNum = this.archive_path.lastIndexOf("/");
				int strLen = this.archive_path.length();
				if ((lastSlashNum != -1) && (lastSlashNum < strLen))
					this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
							"MKCOL: Parent collection "
									+ this.archive_path.substring(
											lastSlashNum + 1, strLen)
									+ " does not exist");
				else
					this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
							"MKCOL: Parent collection does not exist");

				errorOccurred = true;
			}

			// Check If Parent Collection Is Frozen
			if (!errorOccurred) {
				if (!parentFrozen.equalsIgnoreCase("N")) {
					int lastSlashNum = this.archive_path.lastIndexOf("/");
					int strLen = this.archive_path.length();
					if ((lastSlashNum != -1) && (lastSlashNum < strLen))
						this.reportError(DasResponse.SC_COLLECTION_FROZEN,
								"MKCOL: Parent collection "
										+ this.archive_path.substring(
												lastSlashNum + 1, strLen)
										+ " is frozen");
					else
						this.reportError(DasResponse.SC_COLLECTION_FROZEN,
								"MKCOL: Parent collection is frozen");
					errorOccurred = true;
				}
			}

			// Check If Parent Collection Is A Home Or Application Collection
			if (!errorOccurred) {
				if (!(parentColType.equalsIgnoreCase("H") || parentColType
						.equalsIgnoreCase("A"))) {
					int lastSlashNum = this.archive_path.lastIndexOf("/");
					int strLen = this.archive_path.length();
					if ((lastSlashNum != -1) && (lastSlashNum < strLen))
						this
								.reportError(
										DasResponse.SC_SYSTEM_COLLECTION,
										"MKCOL: Parent collection "
												+ this.archive_path.substring(
														lastSlashNum + 1,
														strLen)
												+ " is a not a home or application collection");
					else
						this
								.reportError(DasResponse.SC_SYSTEM_COLLECTION,
										"MKCOL: Parent collection is not a home or application collection");
					errorOccurred = true;
				}
			}

			// Set 'Logical' Exclusive Table Lock
			if (!errorOccurred) {
				try {
					HashMap<String, Object> primKeyMap = new HashMap<String, Object>();
					primKeyMap.put("COLID", new Long(parentColId));
					primKeyMap.put("RESNAME", new String(this.collection_name
							.trim()));
					tlock.lock(TableLocking.LIFETIME_TRANSACTION, connection,
							"BC_XMLA_LOCKING", primKeyMap,
							TableLocking.MODE_EXCLUSIVE_NONCUMULATIVE);
				} catch (LockException lex) {
					int lastSlashNum = this.archive_path.lastIndexOf("/");
					int strLen = this.archive_path.length();
					if ((lastSlashNum != -1) && (lastSlashNum < strLen))
						this.reportError(HttpServletResponse.SC_CONFLICT,
								"MKCOL: The lock for the parent collection "
										+ this.archive_path.substring(
												lastSlashNum + 1, strLen)
										+ " and collection "
										+ this.collection_name
										+ " cannot be granted", lex);
					else
						this.reportError(HttpServletResponse.SC_CONFLICT,
								"MKCOL: The lock for the parent collection id "
										+ parentColId + " and collection "
										+ this.collection_name
										+ " cannot be granted", lex);
					errorOccurred = true;
				} catch (TechnicalLockException tlex) {
					int lastSlashNum = this.archive_path.lastIndexOf("/");
					int strLen = this.archive_path.length();
					if ((lastSlashNum != -1) && (lastSlashNum < strLen))
						this.reportError(HttpServletResponse.SC_CONFLICT,
								"MKCOL: A technical error occurred while locking parent collection "
										+ this.archive_path.substring(
												lastSlashNum + 1, strLen)
										+ " and collection "
										+ this.collection_name, tlex);
					else
						this.reportError(HttpServletResponse.SC_CONFLICT,
								"MKCOL: A technical error occurred while locking parent collection id "
										+ parentColId + " and collection "
										+ this.collection_name, tlex);
					errorOccurred = true;
				}
			}

			// Check If Collection Already Exists
			if (!errorOccurred) {
				pst1 = connection.prepareStatement(SEL_COL_TAB2);
				pst1.setString(1, this.archive_path.trim() + "/"
						+ this.collection_name.trim());
				result = pst1.executeQuery();
				hits = 0;
				while (result.next())
					hits++;
				result.close();
				pst1.close();
				if (hits != 0) {
					this.reportError(HttpServletResponse.SC_CONFLICT,
							"MKCOL: Collection " + this.collection_name.trim()
									+ " already exists");
					errorOccurred = true;
				}
			}

			// Check If A Resource With Identical Name As Collection Exists
			if (!errorOccurred) {
				if (this.collection_name.trim().length() <= MAXRESOURCENAMELENGTH) {
					pst2 = connection.prepareStatement(SEL_RES_TAB);
					pst2.setLong(1, parentColId);
					pst2.setString(2, this.collection_name.trim());
					result = pst2.executeQuery();
					hits = 0;
					while (result.next())
						hits++;
					result.close();
					pst2.close();
					if (hits != 0) {
						this
								.reportError(
										HttpServletResponse.SC_CONFLICT,
										"MKCOL: Name "
												+ this.collection_name
												+ " already being used for an existing resource");
						errorOccurred = true;
					}
				}
			}

			if (!errorOccurred) {
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
							sac, this.archive_path.trim() + "/"
									+ this.collection_name.trim());
					XmlDasMkcol mkcol = new XmlDasMkcol(mkcolRequest);
					XmlDasMkcolResponse mkcolResponse = mkcol.execute();
					if (mkcolResponse.getStatusCode() != HttpServletResponse.SC_CREATED) {
						if (mkcolResponse.getStatusCode() != HttpServletResponse.SC_METHOD_NOT_ALLOWED) {
							if (mkcolResponse.getException() == null)
								this
										.reportError(
												DasResponse.SC_IO_ERROR,
												"MKCOL: Error while creating collection "
														+ this.collection_name
																.trim()
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
												"MKCOL: Error while creating collection "
														+ this.collection_name
																.trim()
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
						} else {
							MasterMethod.cat
									.errorT(
											loc,
											"MKCOL: Collection "
													+ this.collection_name
															.trim()
													+ " already existed on storage system - it is now consistently created in XML DAS");
						}
					}
				}
			}

		} catch (ArchStoreConfigException ascex) {
			this.reportError(DasResponse.SC_CONFIG_INCONSISTENT, "MKCOL: "
					+ ascex.getMessage(), ascex);
			errorOccurred = true;
		} catch (IOException ioex) {
			this.reportError(DasResponse.SC_IO_ERROR, "MKCOL: "
					+ ioex.toString(), ioex);
			errorOccurred = true;
		} catch (SQLException sqlex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "MKCOL: "
					+ sqlex.toString(), sqlex);
			errorOccurred = true;
		} finally {
			try {
				if (pst0 != null)
					pst0.close();
				if (pst1 != null)
					pst1.close();
				if (pst2 != null)
					pst2.close();
			} catch (SQLException sqlex) {
				this.reportError(DasResponse.SC_SQL_ERROR, "MKCOL: "
						+ sqlex.toString(), sqlex);
				errorOccurred = true;
			}
		}
		if (errorOccurred)
			return false;

		boolean status = false;
		try {

			// Get New Collection Id
			colId = this.idProvider.getId("BC_XMLA_COL");

			// Insert New Collection Entry Into Table BC_XMLA_COL
			pst3 = connection.prepareStatement(INS_COL_TAB);
			pst3.setLong(1, colId);
			pst3.setString(2, this.archive_path.trim() + "/"
					+ this.collection_name.trim());
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

			// Set Response Header Fields
			response.setContentType("text/xml");

			// Method Was Successful
			this.response.setHeader("service_message", "Ok");

			// Set Status
			status = true;
		} catch (SQLException sqlex) {

			// Delete Already Created Collection Because SQL Exception Occurred
			String errorMessage = "";
			if (storeId != 0) {
				try {
					XmlDasDeleteRequest deleteRequest = new XmlDasDeleteRequest(
							sac, this.archive_path.trim() + "/"
									+ this.collection_name.trim(), "COL");
					XmlDasDelete delete = new XmlDasDelete(deleteRequest);
					XmlDasDeleteResponse deleteResponse = delete.execute();
					if (!(deleteResponse.getStatusCode() == HttpServletResponse.SC_OK
							|| deleteResponse.getStatusCode() == HttpServletResponse.SC_ACCEPTED || deleteResponse
							.getStatusCode() == HttpServletResponse.SC_NO_CONTENT))
						throw new IOException(deleteResponse.getStatusCode()
								+ " " + deleteResponse.getReasonPhrase() + " "
								+ deleteResponse.getEntityBody());
				} catch (IOException ioex) {
					MasterMethod.cat.errorT(loc, getStackTrace(ioex));
					errorMessage = ioex.toString();
				}
			}
			if (errorMessage.length() == 0)
				this.reportError(DasResponse.SC_SQL_ERROR, "MKCOL: Collection "
						+ this.collection_name.trim() + " was not created: "
						+ sqlex.toString(), sqlex);
			else
				this
						.reportError(
								DasResponse.SC_SQL_ERROR,
								"MKCOL: Collection "
										+ this.collection_name.trim()
										+ " was created and cannot be accessed by SAP XML DAS. To complete the roll back process you must delete it manually from the storage system: "
										+ sqlex.toString()
										+ " IOException when deleting "
										+ this.collection_name.trim()
										+ " in "
										+ XmlDasMaster.getPhysicalPath(sac,
												this.archive_path) + ": "
										+ errorMessage, sqlex);
		} finally {
			try {
				if (pst3 != null)
					pst3.close();
				if (pst4 != null)
					pst4.close();
				if (pst5 != null)
					pst5.close();
				if (pst6 != null)
					pst6.close();
			} catch (SQLException sqlex) {
				this.reportError(DasResponse.SC_SQL_ERROR, "MKCOL: "
						+ sqlex.toString(), sqlex);
				status = false;
			}
		}
		return status;
	}
}