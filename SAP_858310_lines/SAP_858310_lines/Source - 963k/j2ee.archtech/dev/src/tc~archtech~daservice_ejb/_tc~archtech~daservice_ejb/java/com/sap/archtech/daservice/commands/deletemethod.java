package com.sap.archtech.daservice.commands;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;
import com.sap.archtech.daservice.exceptions.ArchStoreConfigException;
import com.sap.archtech.daservice.exceptions.InvalidNameException;
import com.sap.archtech.daservice.storage.XmlDasDelete;
import com.sap.archtech.daservice.storage.XmlDasDeleteRequest;
import com.sap.archtech.daservice.storage.XmlDasDeleteResponse;
import com.sap.archtech.daservice.util.ArchiveStoreTest;

public class DeleteMethod extends MasterMethod {

	private final static String SEL_COL_TAB1 = "SELECT * FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_COL_TAB2 = "SELECT COLID FROM BC_XMLA_COL WHERE PARENTCOLID = ?";
	private final static String SEL_COL_TAB3 = "SELECT PARENTCOLID, STOREID FROM BC_XMLA_COL WHERE COLID = ?";
	private final static String SEL_COL_TAB4 = "SELECT COLID FROM BC_XMLA_COL WHERE URI LIKE ?";
	private final static String SEL_COL_TAB5 = "SELECT PARENTCOLID FROM BC_XMLA_COL WHERE COLID = ?";
	private final static String SEL_RES_TAB1 = "SELECT RESID, PACKNAME, ISPACKED FROM BC_XMLA_RES WHERE COLID = ? AND DELSTATUS IN ('Y', 'N', 'P') AND RESNAME = ?";
	private final static String SEL_RES_TAB2 = "SELECT RESID FROM BC_XMLA_RES WHERE COLID = ?";
	private final static String SEL_RES_CNT = "SELECT RESID FROM BC_XMLA_RES WHERE RESID = ? AND EXISTS (SELECT * FROM BC_XMLA_RES WHERE COLID = ? AND RESID <> ? AND PACKNAME = ?)";
	private final static String SEL_IDX_DIC = "SELECT D.INDEXTABLE FROM BC_XMLA_INDEX_DICT D INNER JOIN BC_XMLA_COL_INDEX I ON I.INDEXID = D.INDEXID WHERE I.COLID = ?";
	private final static String SEL_COL_STO = "SELECT STOREID FROM BC_XMLA_COL_STORE WHERE COLID = ? ORDER BY STOREID";
	private final static String SEL_COL_STO1 = "SELECT COLID FROM BC_XMLA_COL_STORE WHERE COLID = ? AND STOREID = ?";
	private final static String DEL_RES_TAB = "DELETE FROM BC_XMLA_RES WHERE COLID = ?";
	private final static String DEL_RES_TAB1 = "DELETE FROM BC_XMLA_RES WHERE RESID = ?";
	private final static String DEL_COL_TAB = "DELETE FROM BC_XMLA_COL WHERE COLID = ?";
	private final static String DEL_COL_STO = "DELETE FROM BC_XMLA_COL_STORE WHERE COLID = ?";
	private final static String DEL_COL_STO1 = "DELETE FROM BC_XMLA_COL_STORE WHERE COLID = ? AND STOREID = ?";
	private final static String DEL_COL_IDX = "DELETE FROM BC_XMLA_COL_INDEX WHERE COLID = ?";
	private final static String UPD_COL_TAB = "UPDATE BC_XMLA_COL SET STOREID = ? WHERE COLID = ?";
	private final static String DEL_COL_PRP = "DELETE FROM BC_XMLA_COL_PROP WHERE COLID = ?";

	private boolean isFailureStatus = false;
	private int failureStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
	private String uri = "";
	private String delete_range = "";
	private ArchStoreConfigLocalHome beanLocalHome = null;
	private Sapxmla_Config sac = null;
	private Connection connection = null;
	private ArrayList<Sapxmla_Config> storeList = new ArrayList<Sapxmla_Config>();
	private HashSet<Long> notDeletedColIds = new HashSet<Long>();
	private HashSet<Long> notDeletedResIds = new HashSet<Long>();
	private HashMap<Long, HashSet<Long>> notDeletedColStoreIds = new HashMap<Long, HashSet<Long>>();

	public DeleteMethod(HttpServletResponse response, Connection connection,
			ArchStoreConfigLocalHome beanLocalHome, String uri,
			String delete_range) {
		this.response = response;
		this.connection = connection;
		this.beanLocalHome = beanLocalHome;
		this.uri = uri;
		this.delete_range = delete_range;
	}

	public boolean execute() throws IOException {

		// Variables
		boolean successfulDeletion;

		// Check Request Header "uri"
		if (this.uri == null) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"DELETE: URI missing from request header");
			return false;
		} else {
			try {
				this.isValidName(this.uri, true);
			} catch (InvalidNameException inex) {

				// $JL-EXC$
				this.reportError(DasResponse.SC_INVALID_CHARACTER, "DELETE: "
						+ inex.getMessage());
				return false;
			}
			this.uri = this.uri.toLowerCase();
			if ((this.uri.indexOf("//") != -1) || !this.uri.startsWith("/")
					|| this.uri.endsWith("/") || this.uri.length() < 2) {
				this.reportError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
						"DELETE: Value " + this.uri + " of request header "
								+ "URI does not meet specifications");
				return false;
			}
		}

		// Check Request Header "range"
		if (this.delete_range == null) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"DELETE: RANGE missing from request header ");
			return false;
		} else
			this.delete_range = this.delete_range.toUpperCase();
		if (!(this.delete_range.equalsIgnoreCase("COL") || this.delete_range
				.equalsIgnoreCase("RES"))) {
			this.reportError(DasResponse.SC_KEYWORD_UNKNOWN, "DELETE: Value "
					+ this.delete_range + " of request header "
					+ "DELETE_RANGE does not meet specifications");
			return false;
		}

		// Delete single resource
		if (this.delete_range.equalsIgnoreCase("RES"))
			successfulDeletion = this.deleteResource();

		// Delete complete collection with all internal members
		else
			successfulDeletion = this.deleteCollection();

		// Return Status
		if (successfulDeletion == true) {

			// Set Response Header Fields
			response.setContentType("text/xml");

			// Method Was Successful
			this.response.setHeader("service_message", "Ok");
			this.response.setHeader("not_deleted_col", String
					.valueOf(this.notDeletedColIds.size()));
			this.response.setHeader("not_deleted_res", String
					.valueOf(this.notDeletedResIds.size()));

		}
		return successfulDeletion;
	}

	private boolean deleteResource() throws IOException {

		// Variables
		PreparedStatement pst1 = null;
		PreparedStatement pst2 = null;
		PreparedStatement pst3 = null;
		PreparedStatement pst4 = null;
		PreparedStatement pst5 = null;
		PreparedStatement pst6 = null;
		ResultSet result = null;

		try {

			// Split URI Into Archive Path And Resource Name
			String archivePath = this.uri.substring(0,
					this.uri.lastIndexOf("/")).trim();
			String resourceName = this.uri.substring(
					this.uri.lastIndexOf("/") + 1, this.uri.length()).trim();

			// Get Collection Id and Archive Store
			pst1 = this.connection.prepareStatement(SEL_COL_TAB1);
			pst1.setString(1, archivePath.trim());
			result = pst1.executeQuery();
			int hits = 0;
			long storeId = 0;
			long colId = 0;
			String isFrozen = "";
			while (result.next()) {
				colId = result.getLong("COLID");
				isFrozen = result.getString("FROZEN");
				storeId = result.getLong("STOREID");
				hits++;
			}
			result.close();
			pst1.close();
			if (hits == 0) {
				int lastSlashNum = archivePath.lastIndexOf("/");
				int strLen = archivePath.length();
				if ((lastSlashNum != -1) && (lastSlashNum < strLen))
					this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
							"DELETE: Collection "
									+ archivePath.substring(lastSlashNum + 1,
											strLen) + " does not exist");
				else
					this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
							"DELETE: Collection does not exist");
				return false;
			}

			// Check If Collection Is Frozen
			if (!isFrozen.equalsIgnoreCase("N")) {
				int lastSlashNum = archivePath.lastIndexOf("/");
				int strLen = archivePath.length();
				if ((lastSlashNum != -1) && (lastSlashNum < strLen))
					this.reportError(DasResponse.SC_COLLECTION_FROZEN,
							"DELETE: Collection "
									+ archivePath.substring(lastSlashNum + 1,
											strLen) + " is frozen");
				else
					this.reportError(DasResponse.SC_COLLECTION_FROZEN,
							"DELETE: Collection is frozen");
				return false;
			}

			// Check If Entry In Table BC_XMLA_RES Exists
			String packName = "";
			String isPacked = "";
			pst2 = this.connection.prepareStatement(SEL_RES_TAB1);
			pst2.setLong(1, colId);
			pst2.setString(2, resourceName.trim());
			result = pst2.executeQuery();
			hits = 0;
			long resId = 0;
			while (result.next()) {
				resId = result.getLong("RESID");
				packName = result.getString("PACKNAME");
				isPacked = result.getString("ISPACKED");
				hits++;
			}
			result.close();
			pst2.close();
			if (hits == 0) {
				this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
						"DELETE: Resource " + resourceName + " does not exist");
				return false;
			}

			// When Packed: Check If All Other Resources In This AXML File
			// Are Already Deleted
			boolean morePackedRes = false;
			if (isPacked.equalsIgnoreCase("Y")) {
				pst3 = this.connection.prepareStatement(SEL_RES_CNT);
				pst3.setLong(1, resId);
				pst3.setLong(2, colId);
				pst3.setLong(3, resId);
				pst3.setString(4, packName.trim());
				result = pst3.executeQuery();
				if (result.next())
					morePackedRes = true;
				result.close();
				pst3.close();
				if (!morePackedRes)
					resourceName = packName.trim();
			}

			// Delete Entry In Table BC_XMLA_RES
			pst4 = this.connection.prepareStatement(DEL_RES_TAB1);
			pst4.setLong(1, resId);
			hits = pst4.executeUpdate();
			pst4.close();
			if (hits != 1) {
				this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
						"DELETE: Error while deleting resource " + resourceName
								+ " from table BC_XMLA_RES");
				return false;
			}

			// Get All Belonging Index Tables
			ArrayList<String> indexTabList = new ArrayList<String>();
			pst5 = this.connection.prepareStatement(SEL_IDX_DIC);
			pst5.setLong(1, colId);
			result = pst5.executeQuery();
			while (result.next())
				indexTabList.add(result.getString("INDEXTABLE"));
			result.close();
			pst5.close();

			// Delete All Possible Entries In All Index Tables
			for (Iterator<String> iter = indexTabList.iterator(); iter
					.hasNext();) {
				pst6 = this.connection.prepareStatement("DELETE FROM "
						+ ((String) iter.next()).trim() + " WHERE RESID = ?");
				pst6.setLong(1, resId);
				pst6.executeUpdate();
				pst6.close();
			}

			// Delete Resource On Storage System, When Resource Is Not Packed Or
			// Resource Is Packed And All Other Resources In The AXML File
			// Are Already Deleted
			if (!morePackedRes) {

				// Get Archive Store Configuration Data
				sac = this.getArchStoreConfigObject(beanLocalHome, storeId);

				// Delete Resource
				XmlDasDeleteRequest deleteRequest = new XmlDasDeleteRequest(
						sac, archivePath.trim() + "/" + resourceName.trim(),
						"RES");
				XmlDasDelete delete = new XmlDasDelete(deleteRequest);
				XmlDasDeleteResponse deleteResponse = delete.execute();

				// Delete Data From Storage System
				if (!(deleteResponse.getStatusCode() == HttpServletResponse.SC_OK
						|| deleteResponse.getStatusCode() == HttpServletResponse.SC_ACCEPTED || deleteResponse
						.getStatusCode() == HttpServletResponse.SC_NO_CONTENT)) {
					if (deleteResponse.getStatusCode() == DasResponse.SC_MULTI_STATUS) {
						String entityBody = deleteResponse.getEntityBody();
						if (entityBody == null)
							entityBody = String
									.valueOf(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
						if (!(entityBody.startsWith(String
								.valueOf(HttpServletResponse.SC_OK))
								|| entityBody
										.startsWith(String
												.valueOf(HttpServletResponse.SC_ACCEPTED)) || entityBody
								.startsWith(String
										.valueOf(HttpServletResponse.SC_NO_CONTENT)))) {
							isFailureStatus = true;
							try {
								failureStatusCode = Integer.parseInt(entityBody
										.substring(0, 3));
								if (failureStatusCode < HttpServletResponse.SC_BAD_REQUEST)
									failureStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
							} catch (NumberFormatException nfex) {
								failureStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
							}
							throw new IOException(
									"DELETE: Error while deleting resource "
											+ resourceName
											+ ": Archive store returned following response: "
											+ deleteResponse.getStatusCode()
											+ " "
											+ deleteResponse.getReasonPhrase()
											+ " "
											+ deleteResponse.getEntityBody());
						}
					} else if (deleteResponse.getStatusCode() < HttpServletResponse.SC_BAD_REQUEST) {
						isFailureStatus = true;
						failureStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
						throw new IOException(
								"DELETE: Error while deleting resource "
										+ resourceName
										+ ": Archive store returned following response: "
										+ deleteResponse.getStatusCode() + " "
										+ deleteResponse.getReasonPhrase()
										+ " " + deleteResponse.getEntityBody());

					} else {
						isFailureStatus = true;
						failureStatusCode = deleteResponse.getStatusCode();
						if (deleteResponse.getException() == null) {
							MasterMethod.cat
									.errorT(
											loc,
											"DELETE: Error while deleting resource "
													+ resourceName
													+ ": Archive store returned following response: "
													+ deleteResponse
															.getStatusCode()
													+ " "
													+ deleteResponse
															.getReasonPhrase()
													+ " "
													+ deleteResponse
															.getEntityBody());
							throw new IOException(
									"DELETE: Error while deleting resource "
											+ resourceName
											+ ": Archive store returned following response: "
											+ deleteResponse.getStatusCode()
											+ " "
											+ deleteResponse.getReasonPhrase()
											+ " "
											+ deleteResponse.getEntityBody());
						} else {
							MasterMethod.cat
									.errorT(
											loc,
											"DELETE: Error while deleting resource "
													+ resourceName
													+ ": Archive store returned following response: "
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
									"DELETE: Error while deleting resource "
											+ resourceName
											+ ": Archive store returned following response: "
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
				}
			}

			// Return Status
			return true;
		} catch (IOException ioex) {
			if (isFailureStatus == true)
				this.reportError(failureStatusCode, "DELETE: "
						+ ioex.toString(), ioex);
			else
				this.reportError(DasResponse.SC_IO_ERROR, "DELETE: "
						+ ioex.toString(), ioex);
			return false;
		} catch (SQLException sqlex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "DELETE: "
					+ sqlex.toString(), sqlex);
			return false;
		} catch (ArchStoreConfigException ascex) {
			this.reportError(DasResponse.SC_CONFIG_INCONSISTENT, "DELETE: "
					+ ascex.getMessage(), ascex);
			return false;
		} finally {

			// Close All Prepared Statements
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
			} catch (SQLException sqlex) {

				// $JL-EXC$
				cat.infoT(loc, "DELETE: " + sqlex.toString());
			}
		}
	}

	private boolean deleteCollection() throws IOException {

		// Variables
		long parentColId = 0;
		PreparedStatement pst1 = null;
		PreparedStatement pst2 = null;
		ResultSet result = null;
		try {

			// Get Collection Attributes
			pst1 = this.connection.prepareStatement(SEL_COL_TAB1);
			pst1.setString(1, this.uri.trim());
			result = pst1.executeQuery();
			int hits = 0;
			long colId = 0;
			String isFrozen = "";
			String colType = "";
			while (result.next()) {
				colId = result.getLong("COLID");
				isFrozen = result.getString("FROZEN");
				parentColId = result.getLong("PARENTCOLID");
				colType = result.getString("COLTYPE");
				hits++;
			}
			result.close();
			pst1.close();
			if (hits == 0) {
				int lastSlashNum = this.uri.lastIndexOf("/");
				int strLen = this.uri.length();
				if ((lastSlashNum != -1) && (lastSlashNum < strLen))
					this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
							"DELETE: Collection "
									+ this.uri.substring(lastSlashNum + 1,
											strLen) + " does not exist");
				else
					this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
							"DELETE: Collection does not exist");
				return false;
			}

			// Check If Collection Is From Type Application
			if (!colType.equalsIgnoreCase("A")) {
				int lastSlashNum = this.uri.lastIndexOf("/");
				int strLen = this.uri.length();
				if ((lastSlashNum != -1) && (lastSlashNum < strLen))
					this.reportError(DasResponse.SC_SYSTEM_COLLECTION,
							"DELETE: Collection "
									+ this.uri.substring(lastSlashNum + 1,
											strLen)
									+ " is a system or home collection");
				else
					this
							.reportError(DasResponse.SC_SYSTEM_COLLECTION,
									"DELETE: Collection is a system or home collection");
				return false;
			}

			// Check If Collection Is Frozen
			if (!isFrozen.equalsIgnoreCase("N")) {
				int lastSlashNum = this.uri.lastIndexOf("/");
				int strLen = this.uri.length();
				if ((lastSlashNum != -1) && (lastSlashNum < strLen))
					this.reportError(DasResponse.SC_COLLECTION_FROZEN,
							"DELETE: Collection "
									+ this.uri.substring(lastSlashNum + 1,
											strLen) + " is frozen");
				else
					this.reportError(DasResponse.SC_COLLECTION_FROZEN,
							"DELETE: Collection is frozen");
				return false;
			}

			// Get All Archive Stores
			ArrayList<Long> storeIdList = new ArrayList<Long>();
			pst2 = this.connection.prepareStatement(SEL_COL_STO);
			pst2.setLong(1, colId);
			result = pst2.executeQuery();
			while (result.next())
				storeIdList.add(new Long(result.getLong("STOREID")));
			result.close();
			pst2.close();

			// Check If All Archive Stores Are Running
			String testResult = "";
			for (Iterator<Long> iter = storeIdList.iterator(); iter.hasNext();) {

				// Get Configuration Data For All Archive Stores
				Sapxmla_Config archiveStoreConfig = this
						.getArchStoreConfigObject(beanLocalHome, ((Long) iter
								.next()).longValue());
				storeList.add(archiveStoreConfig);

				// Test Archive Store
				testResult = ArchiveStoreTest.execute(archiveStoreConfig);
				if (!testResult.startsWith("S")) {
					this.reportError(HttpServletResponse.SC_CONFLICT,
							"DELETE: " + testResult.substring(2));
					return false;
				}
			}

			// IO Delete Collection From All Archive Stores
			this.deleteCollectionFromAllArchiveStores(parentColId);

			// Delete Recursive All Entries in Database Collection Tables
			this.deleteCollectionInDatabase(colId, parentColId);

			// CleanUp Collections Above
			this.deleteCollectionCleanup(parentColId);

			// Return Status
			return true;
		} catch (IOException ioex) {
			if (isFailureStatus == true)
				this.reportError(failureStatusCode, "DELETE: "
						+ ioex.toString(), ioex);
			else
				this.reportError(DasResponse.SC_IO_ERROR, "DELETE: "
						+ ioex.toString(), ioex);
			return false;
		} catch (SQLException sqlex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "DELETE: "
					+ sqlex.toString(), sqlex);
			return false;
		} catch (ArchStoreConfigException ascex) {
			this.reportError(DasResponse.SC_CONFIG_INCONSISTENT, "DELETE: "
					+ ascex.getMessage(), ascex);
			return false;
		} finally {

			// Close All Prepared Statements
			try {
				if (result != null)
					result.close();
				if (pst1 != null)
					pst1.close();
				if (pst2 != null)
					pst2.close();
			} catch (SQLException sqlex) {

				// $JL-EXC$
				cat.infoT(loc, "DELETE: " + sqlex.toString());
			}
		}
	}

	private void deleteCollectionFromAllArchiveStores(long parentColId)
			throws SQLException, IOException {

		// Loop Over All Archive Stores
		XmlDasDeleteRequest deleteRequest;
		XmlDasDelete delete;
		XmlDasDeleteResponse deleteResponse;
		Sapxmla_Config archiveStoreConfig;
		for (Iterator<Sapxmla_Config> iter = storeList.iterator(); iter
				.hasNext();) {

			// Get Archive Store Configuration Data
			archiveStoreConfig = (Sapxmla_Config) iter.next();

			// Create New Hash Set For Current Archive Store
			notDeletedColStoreIds.put(new Long(archiveStoreConfig.store_id),
					new HashSet<Long>());

			// Delete Collection
			deleteRequest = new XmlDasDeleteRequest(archiveStoreConfig,
					this.uri.trim(), "COL", notDeletedColIds, notDeletedResIds,
					notDeletedColStoreIds, connection);
			delete = new XmlDasDelete(deleteRequest);
			deleteResponse = delete.execute();
			if (!(deleteResponse.getStatusCode() == HttpServletResponse.SC_OK
					|| deleteResponse.getStatusCode() == HttpServletResponse.SC_ACCEPTED || deleteResponse
					.getStatusCode() == HttpServletResponse.SC_NO_CONTENT)) {
				if (deleteResponse.getStatusCode() == DasResponse.SC_MULTI_STATUS) {
					if (deleteResponse.getException() != null) {
						isFailureStatus = true;
						failureStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
						MasterMethod.cat
								.errorT(
										loc,
										"DELETE: Error while deleting collection "
												+ this.uri.substring(this.uri
														.lastIndexOf("/") + 1,
														this.uri.length())
												+ ": Multi-status response was empty. Archive store returned following response: "
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
								"DELETE: Error while deleting collection "
										+ this.uri.substring(this.uri
												.lastIndexOf("/") + 1, this.uri
												.length())
										+ ": Multi-status response was empty. Archive store returned following response: "
										+ deleteResponse.getStatusCode()
										+ " "
										+ deleteResponse.getReasonPhrase()
										+ " "
										+ deleteResponse.getEntityBody()
										+ " "
										+ deleteResponse.getException()
												.toString());
					}
				} else if (deleteResponse.getStatusCode() < HttpServletResponse.SC_BAD_REQUEST) {
					isFailureStatus = true;
					failureStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
					MasterMethod.cat
							.errorT(
									loc,
									"DELETE: Error while deleting collection "
											+ this.uri.substring(this.uri
													.lastIndexOf("/") + 1,
													this.uri.length())
											+ ": Archive store returned following response: "
											+ deleteResponse.getStatusCode()
											+ " "
											+ deleteResponse.getReasonPhrase()
											+ " "
											+ deleteResponse.getEntityBody());
					throw new IOException(
							"DELETE: Error while deleting collection "
									+ this.uri.substring(this.uri
											.lastIndexOf("/") + 1, this.uri
											.length())
									+ ": Archive store returned following response: "
									+ deleteResponse.getStatusCode() + " "
									+ deleteResponse.getReasonPhrase() + " "
									+ deleteResponse.getEntityBody());
				} else {
					isFailureStatus = true;
					failureStatusCode = deleteResponse.getStatusCode();
					if (deleteResponse.getException() == null) {
							MasterMethod.cat
									.errorT(
											loc,
											"DELETE: Error while deleting collection "
													+ this.uri
															.substring(
																	this.uri
																			.lastIndexOf("/") + 1,
																	this.uri
																			.length())
													+ ": Archive store returned following response: "
													+ deleteResponse
															.getStatusCode()
													+ " "
													+ deleteResponse
															.getReasonPhrase()
													+ " "
													+ deleteResponse
															.getEntityBody());
							throw new IOException(
									"DELETE: Error while deleting collection "
											+ this.uri.substring(this.uri
													.lastIndexOf("/") + 1,
													this.uri.length())
											+ ": Archive store returned following response: "
											+ deleteResponse.getStatusCode()
											+ " "
											+ deleteResponse.getReasonPhrase()
											+ " "
											+ deleteResponse.getEntityBody());
					} else {
						MasterMethod.cat
								.errorT(
										loc,
										"DELETE: Error while deleting collection "
												+ this.uri.substring(this.uri
														.lastIndexOf("/") + 1,
														this.uri.length())
												+ ": Archive store returned following response: "
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
								"DELETE: Error while deleting collection "
										+ this.uri.substring(this.uri
												.lastIndexOf("/") + 1, this.uri
												.length())
										+ ": Archive store returned following response: "
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
			}
		} // end for loop
	}

	private void deleteCollectionInDatabase(long colId, long parentColId)
			throws SQLException {
		if (notDeletedResIds.isEmpty() && notDeletedColIds.isEmpty())

			// 200 Ok Response Io Case
			this.deleteCollectionInDatabaseOkCase(colId);
		else

			// 207 Multi-Status Response Io Case
			this.deleteCollectionInDatabaseMultiStatusCase(colId, parentColId);
	}

	private void deleteCollectionInDatabaseOkCase(long colId)
			throws SQLException {

		// Variables
		long resId;
		ResultSet result = null;
		PreparedStatement pst1 = null;
		PreparedStatement pst2 = null;
		PreparedStatement pst3 = null;
		PreparedStatement pst4 = null;
		PreparedStatement pst5 = null;
		PreparedStatement pst6 = null;
		PreparedStatement pst7 = null;
		PreparedStatement pst8 = null;
		PreparedStatement pst9 = null;
		ArrayList<Long> childrenNodesList = new ArrayList<Long>();
		ArrayList<String> indexTableList = new ArrayList<String>();

		try {

			// Get All Children Nodes
			pst1 = this.connection.prepareStatement(SEL_COL_TAB2);
			pst1.setLong(1, colId);
			result = pst1.executeQuery();
			while (result.next())
				childrenNodesList.add(new Long(result.getLong("COLID")));
			result.close();
			pst1.close();

			// Delete BC_XMLA_COL entries
			pst2 = this.connection.prepareStatement(DEL_COL_TAB);
			pst2.setLong(1, colId);
			pst2.executeUpdate();
			pst2.close();

			// Delete BC_XMLA_COL_STORE entries
			pst3 = this.connection.prepareStatement(DEL_COL_STO);
			pst3.setLong(1, colId);
			pst3.executeUpdate();
			pst3.close();

			// Delete BC_XMLA_COL_PROP Entries
			pst4 = this.connection.prepareStatement(DEL_COL_PRP);
			pst4.setLong(1, colId);
			pst4.executeUpdate();
			pst4.close();

			// Get All Belonging Index Tables
			pst5 = this.connection.prepareStatement(SEL_IDX_DIC);
			pst5.setLong(1, colId);
			result = pst5.executeQuery();
			while (result.next())
				indexTableList.add(result.getString("INDEXTABLE"));
			result.close();
			pst5.close();

			// Delete BC_XMLA_COL_INDEX entries
			pst6 = this.connection.prepareStatement(DEL_COL_IDX);
			pst6.setLong(1, colId);
			pst6.executeUpdate();
			pst6.close();

			// Select All BC_XMLA_RES entries
			pst7 = this.connection.prepareStatement(SEL_RES_TAB2);
			pst7.setLong(1, colId);
			result = pst7.executeQuery();
			while (result.next()) {
				resId = result.getLong("RESID");

				// Delete All Possible Entries In All Index Tables
				for (Iterator<String> iter = indexTableList.iterator(); iter
						.hasNext();) {
					pst8 = this.connection.prepareStatement("DELETE FROM "
							+ ((String) iter.next()).trim()
							+ " WHERE RESID = ?");
					pst8.setLong(1, resId);
					pst8.executeUpdate();
					pst8.close();
				}
			}
			result.close();
			pst7.close();

			// Delete BC_XMLA_RES entry
			pst9 = this.connection.prepareStatement(DEL_RES_TAB);
			pst9.setLong(1, colId);
			pst9.executeUpdate();
			pst9.close();

			// No More Leaf Nodes Exists
			if (childrenNodesList.size() == 0) {
				return;
			}

			// More Leaf Nodes Exists
			else {
				for (int i = 0; i < childrenNodesList.size(); i++)
					this
							.deleteCollectionInDatabaseOkCase(((Long) childrenNodesList
									.get(i)).longValue());
			}
		} catch (SQLException sqlex) {
			throw sqlex;
		} finally {
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
		}
	}

	private void deleteCollectionInDatabaseMultiStatusCase(long colId,
			long parentColId) throws SQLException {
		PreparedStatement pst = null;
		ResultSet result = null;
		try {

			// Add Parent Collection Ids For All Collections To COL Set
			HashSet<Long> additionalColIds = new HashSet<Long>();
			long cid = 0;
			for (Iterator<Long> iter = notDeletedColIds.iterator(); iter
					.hasNext();) {
				cid = ((Long) iter.next()).longValue();
				boolean status = false;
				do {
					pst = connection.prepareStatement(SEL_COL_TAB5);
					pst.setLong(1, cid);
					result = pst.executeQuery();
					while (result.next()) {
						cid = result.getLong("PARENTCOLID");
					}
					result.close();
					pst.close();
					if (cid <= parentColId) {
						status = true;
					} else {
						additionalColIds.add(new Long(cid));
					}
				} while (status == false);
			}
			for (Iterator<Long> iter = additionalColIds.iterator(); iter
					.hasNext();) {
				notDeletedColIds.add((Long) iter.next());
			}

			// Add Parent Collection Ids For All Collections To COLSTORE Sets
			Sapxmla_Config archiveStoreConfig;
			HashSet<Long> hs;
			for (Iterator<Sapxmla_Config> iter = storeList.iterator(); iter
					.hasNext();) {
				archiveStoreConfig = (Sapxmla_Config) iter.next();
				hs = (HashSet<Long>) notDeletedColStoreIds.get(new Long(
						archiveStoreConfig.store_id));
				HashSet<Long> additionalColStoreIds = new HashSet<Long>();
				for (Iterator<Long> iterat = hs.iterator(); iterat.hasNext();) {
					cid = ((Long) iterat.next()).longValue();
					boolean status = false;
					do {
						pst = connection.prepareStatement(SEL_COL_TAB5);
						pst.setLong(1, cid);
						result = pst.executeQuery();
						while (result.next()) {
							cid = result.getLong("PARENTCOLID");
						}
						result.close();
						pst.close();
						if (cid <= parentColId) {
							status = true;
						} else {
							additionalColStoreIds.add(new Long(cid));
						}
					} while (status == false);
				}
				for (Iterator<Long> iterator = additionalColStoreIds.iterator(); iterator
						.hasNext();) {
					hs.add((Long) iterator.next());
				}
			}
		} catch (SQLException sqlex) {
			throw sqlex;
		} finally {
			if (result != null)
				result.close();
			if (pst != null)
				pst.close();
		}

		// Loop Over All Collections
		this.traverseCollectionsMultiStatusCase(colId);
	}

	public void traverseCollectionsMultiStatusCase(long colId)
			throws SQLException {
		PreparedStatement pst = null;
		ResultSet result = null;
		ArrayList<Long> children = new ArrayList<Long>();
		try {

			// Get All Child Collections
			pst = this.connection.prepareStatement(SEL_COL_TAB2);
			pst.setLong(1, colId);
			result = pst.executeQuery();
			while (result.next())
				children.add(new Long(result.getLong("COLID")));
			result.close();
			pst.close();
			for (Iterator<Long> iter = children.iterator(); iter.hasNext();) {
				traverseCollectionsMultiStatusCase(((Long) iter.next())
						.longValue());
			}

			// Delete All Necessary Database Entries
			deleteDatabaseInMultiStatusCase(colId);
		} catch (SQLException sqlex) {
			throw sqlex;
		} finally {
			if (result != null)
				result.close();
			if (pst != null)
				pst.close();
		}
	}

	private void deleteDatabaseInMultiStatusCase(long colId)
			throws SQLException {

		// Variables
		long resId;
		ResultSet result = null;
		PreparedStatement pst1 = null;
		PreparedStatement pst2 = null;
		PreparedStatement pst3 = null;
		PreparedStatement pst4 = null;
		PreparedStatement pst5 = null;
		PreparedStatement pst6 = null;
		PreparedStatement pst7 = null;
		PreparedStatement pst8 = null;
		ArrayList<String> indexTableList = new ArrayList<String>();
		try {

			// Check If Collection Could Be Deleted
			boolean doNotDelete = this.notDeletedColIds
					.contains(new Long(colId));
			if (!doNotDelete) {

				// Delete BC_XMLA_COL entries
				pst1 = this.connection.prepareStatement(DEL_COL_TAB);
				pst1.setLong(1, colId);
				pst1.executeUpdate();
				pst1.close();

				// Delete BC_XMLA_COL_PROP Entries
				pst2 = this.connection.prepareStatement(DEL_COL_PRP);
				pst2.setLong(1, colId);
				pst2.executeUpdate();
				pst2.close();
			}

			// Delete BC_XMLA_COL_STORE entries
			Sapxmla_Config archiveStoreConfig;
			HashSet<Long> hs;
			for (Iterator<Sapxmla_Config> iter = storeList.iterator(); iter
					.hasNext();) {
				archiveStoreConfig = (Sapxmla_Config) iter.next();
				hs = (HashSet<Long>) notDeletedColStoreIds.get(new Long(
						archiveStoreConfig.store_id));

				// Check If Collection On Current Archive Store Could Be Deleted
				if (!(hs.contains(new Long(colId)))) {
					pst3 = this.connection.prepareStatement(DEL_COL_STO1);
					pst3.setLong(1, colId);
					pst3.setLong(2, archiveStoreConfig.store_id);
					pst3.executeUpdate();
					pst3.close();
				}
			}

			// Get All Belonging Index Tables
			pst4 = this.connection.prepareStatement(SEL_IDX_DIC);
			pst4.setLong(1, colId);
			result = pst4.executeQuery();
			while (result.next())
				indexTableList.add(result.getString("INDEXTABLE"));
			result.close();
			pst4.close();
			if (!doNotDelete) {

				// Delete BC_XMLA_COL_INDEX entries
				pst5 = this.connection.prepareStatement(DEL_COL_IDX);
				pst5.setLong(1, colId);
				pst5.executeUpdate();
				pst5.close();
			}

			// Select All BC_XMLA_RES entries
			pst6 = this.connection.prepareStatement(SEL_RES_TAB2);
			pst6.setLong(1, colId);
			result = pst6.executeQuery();
			while (result.next()) {
				resId = result.getLong("RESID");

				// Check If Resource Could Be Deleted
				if (this.notDeletedResIds.contains(new Long(resId)) == false) {

					// Delete All Possible Entries In All Index Tables
					for (Iterator<String> iter = indexTableList.iterator(); iter
							.hasNext();) {
						pst7 = this.connection.prepareStatement("DELETE FROM "
								+ ((String) iter.next()).trim()
								+ " WHERE RESID = ?");
						pst7.setLong(1, resId);
						pst7.executeUpdate();
						pst7.close();
					}

					// Delete BC_XMLA_RES entry
					pst8 = this.connection.prepareStatement(DEL_RES_TAB1);
					pst8.setLong(1, resId);
					pst8.executeUpdate();
					pst8.close();
				}
			}
			result.close();
			pst6.close();

		} catch (SQLException sqlex) {
			throw sqlex;
		} finally {
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
		}
	}

	private void deleteCollectionCleanup(long parentColId) {

		// Local Variables
		long colId = 0;
		ResultSet result = null;
		PreparedStatement pst1 = null;
		PreparedStatement pst2 = null;
		PreparedStatement pst3 = null;
		PreparedStatement pst4 = null;
		PreparedStatement pst5 = null;
		PreparedStatement pst6 = null;
		XmlDasDeleteRequest deleteRequest = null;
		XmlDasDelete delete = null;
		XmlDasDeleteResponse deleteResponse = null;
		try {

			// Loop All Archive Stores
			Sapxmla_Config archiveStoreConfig = null;
			for (Iterator<Sapxmla_Config> iter = storeList.iterator(); iter
					.hasNext();) {

				// Get Archive Store Id
				archiveStoreConfig = (Sapxmla_Config) iter.next();

				// Loop Upwards From Selected Collection To Root Collection
				String actualPath = this.uri;
				long loopColId = parentColId;
				while (loopColId > 0) {

					// Adjust Collection Id
					colId = loopColId;

					// Adjust Boolean Variables
					boolean colDescendantHasColStoreEntry = false;

					// Adjust Actual Path For Further Processing
					actualPath = actualPath.substring(0, actualPath
							.lastIndexOf("/"));

					// Get Parent Collection Id And Check If Parent Collection
					// Is Assigned
					long parentStoreId = 0;
					// String parentColType = "";
					pst1 = this.connection.prepareStatement(SEL_COL_TAB3);
					pst1.setLong(1, colId);
					result = pst1.executeQuery();
					while (result.next()) {
						loopColId = result.getLong("PARENTCOLID");
						parentStoreId = result.getLong("STOREID");
					}
					result.close();
					pst1.close();
					if (parentStoreId != 0)
						break;

					// Check If Collection Has At Least One Descendant
					// Collection
					ArrayList<Long> descedantHomeCols = new ArrayList<Long>();
					pst2 = this.connection.prepareStatement(SEL_COL_TAB4);
					pst2.setString(1, actualPath + "/%");
					result = pst2.executeQuery();
					int hits = 0;
					while (result.next()) {
						descedantHomeCols
								.add(new Long(result.getLong("COLID")));
						hits++;
					}
					result.close();
					pst2.close();

					// Check If At Least One Descendant Collection Has An
					// BC_XMLA_COL_STORE Entry
					if (hits != 0) {
						Iterator<Long> it = descedantHomeCols.iterator();
						while (it.hasNext()) {
							pst3 = this.connection
									.prepareStatement(SEL_COL_STO1);
							pst3.setLong(1, ((Long) it.next()).longValue());
							pst3.setLong(2, archiveStoreConfig.store_id);
							result = pst3.executeQuery();
							hits = 0;
							while (result.next())
								hits++;
							result.close();
							pst3.close();
							if (hits != 0) {
								colDescendantHasColStoreEntry = true;
								break;
							}
						}
					}

					// Delete Collection On Archive Store
					if (colDescendantHasColStoreEntry == false) {

						// Delete Collection On Storage System
						deleteRequest = new XmlDasDeleteRequest(
								archiveStoreConfig, actualPath.trim(), "COL");
						delete = new XmlDasDelete(deleteRequest);
						deleteResponse = delete.execute();
						if (!(deleteResponse.getStatusCode() == HttpServletResponse.SC_OK
								|| deleteResponse.getStatusCode() == HttpServletResponse.SC_ACCEPTED || deleteResponse
								.getStatusCode() == HttpServletResponse.SC_NO_CONTENT)) {
							if (deleteResponse.getException() == null) {
								MasterMethod.cat
										.errorT(
												loc,
												"DELETE: Error while deleting collection "
														+ actualPath
																.substring(
																		actualPath
																				.lastIndexOf("/") + 1,
																		actualPath
																				.length())
														+ ": Archive store returned following response: "
														+ deleteResponse
																.getStatusCode()
														+ " "
														+ deleteResponse
																.getReasonPhrase()
														+ " "
														+ deleteResponse
																.getEntityBody());
								throw new IOException(
										"DELETE: Error while deleting collection "
												+ actualPath
														.substring(
																actualPath
																		.lastIndexOf("/") + 1,
																actualPath
																		.length())
												+ ": Archive store returned following response: "
												+ deleteResponse
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
												"DELETE: Error while deleting collection "
														+ actualPath
																.substring(
																		actualPath
																				.lastIndexOf("/") + 1,
																		actualPath
																				.length())
														+ ": Archive store returned following response: "
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
										"DELETE: Error while deleting collection "
												+ actualPath
														.substring(
																actualPath
																		.lastIndexOf("/") + 1,
																actualPath
																		.length())
												+ ": Archive store returned following response: "
												+ deleteResponse
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

						// Delete BC_XMLA_COL_STORE Entry
						pst4 = this.connection.prepareStatement(DEL_COL_STO1);
						pst4.setLong(1, colId);
						pst4.setLong(2, archiveStoreConfig.store_id);
						pst4.executeUpdate();
						pst4.close();

						// Delete BC_XMLA_COL_PROP Entry
						pst5 = this.connection.prepareStatement(DEL_COL_PRP);
						pst5.setLong(1, colId);
						pst5.executeUpdate();
						pst5.close();

						// Update BC_XMLA_COL Entry
						pst6 = this.connection.prepareStatement(UPD_COL_TAB);
						pst6.setNull(1, Types.NULL);
						pst6.setLong(2, colId);
						pst6.executeUpdate();
						pst6.close();
					}
				} // end while loop
			} // end for loop
		} catch (SQLException sqlex) {

			// $JL-EXC$
			cat.infoT(loc, "DELETE: " + sqlex.toString());
		} catch (IOException ioex) {

			// $JL-EXC$
			cat.infoT(loc, "DELETE: " + ioex.toString());
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
			} catch (SQLException sqlex) {

				// $JL-EXC$
				cat.infoT(loc, "DELETE: " + sqlex.toString());
			}
		}
	}
}