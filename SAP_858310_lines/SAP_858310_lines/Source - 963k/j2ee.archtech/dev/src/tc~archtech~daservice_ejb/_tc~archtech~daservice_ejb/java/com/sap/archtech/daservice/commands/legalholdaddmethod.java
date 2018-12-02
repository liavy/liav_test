package com.sap.archtech.daservice.commands;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;
import com.sap.archtech.daservice.exceptions.ArchStoreConfigException;
import com.sap.archtech.daservice.exceptions.InvalidNameException;
import com.sap.archtech.daservice.storage.XmlDasLegalHold;
import com.sap.archtech.daservice.storage.XmlDasLegalHoldRequest;
import com.sap.archtech.daservice.storage.XmlDasLegalHoldResponse;
import com.sap.archtech.daservice.util.ArchiveStoreTest;

public class LegalHoldAddMethod extends MasterMethod {

	private final static int READBUFFER = 256;
	private final static String SEL_COL_TAB = "SELECT COLID, STOREID FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_RES_TAB = "SELECT RESID FROM BC_XMLA_RES WHERE RESNAME = ? AND COLID = ?";
	private final static String SEL_COL_STO_TAB = "SELECT STOREID FROM BC_XMLA_COL_STORE WHERE COLID = ? ORDER BY STOREID";
	private final static String SEL_CFG_TAB = "SELECT STORETYPE FROM BC_XMLA_CONFIG WHERE STOREID = ?";

	private HttpServletRequest request;
	private Connection connection;
	private ArchStoreConfigLocalHome beanLocalHome;
	private String ilm_case;
	private String att_col;
	private ArrayList<Sapxmla_Config> rollbackAttachProps = new ArrayList<Sapxmla_Config>();
	private ArrayList<Sapxmla_Config> rollbackXmldasProps = new ArrayList<Sapxmla_Config>();

	public LegalHoldAddMethod(HttpServletRequest request,
			HttpServletResponse response, Connection connection,
			ArchStoreConfigLocalHome beanLocalHome, String ilm_case,
			String att_col) {
		this.request = request;
		this.response = response;
		this.connection = connection;
		this.beanLocalHome = beanLocalHome;
		this.ilm_case = ilm_case;
		this.att_col = att_col;
	}

	public boolean execute() throws IOException {

		// Variables
		boolean errorOccurred = false;
		boolean rollbackFailed = false;
		int hits = 0;
		long colId = 0;
		long storeId = 0;
		String range = "";
		String resName = "";
		String attachment_uri = "";
		PreparedStatement pst = null;
		ResultSet result = null;
		Sapxmla_Config sac = null;
		BufferedWriter bw;
		ArrayList<Long> storeIdList = new ArrayList<Long>();
		ArrayList<Sapxmla_Config> storeList = new ArrayList<Sapxmla_Config>();

		// Set Response Header Fields
		response.setContentType("text/xml");
		response.setHeader("service_message", "see message body");

		// Prepare Sending Response Body
		bw = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream(), "UTF8"));

		// Check Request Header "ilm_case"
		if (this.ilm_case == null) {
			this.reportStreamError(DasResponse.SC_PARAMETER_MISSING,
					"LEGALHOLD ADD: ILM_CASE missing from request header", bw);
			return false;
		} else {
			this.ilm_case = this.ilm_case.trim();
			if (this.ilm_case.length() == 0) {
				this.reportStreamError(
						DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
						"LEGALHOLD ADD: Request header ILM_CASE is empty", bw);
				return false;
			}
		}

		// Check Request Header "att_col"
		if (this.att_col == null) {
			this.att_col = "N";
		} else {
			this.att_col = this.att_col.toUpperCase();
			if (!(this.att_col.equalsIgnoreCase("Y") || this.att_col
					.equalsIgnoreCase("N"))) {
				this.reportStreamError(
						DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
						"LEGALHOLD ADD: Value " + this.att_col
								+ " of request header "
								+ "ATT_COL does not meet specifications", bw);
				return false;
			}
		}

		// Loop All URI´s
		ByteArrayOutputStream baos = null;
		int size = 0;
		boolean furtherLoop = true;
		String uri = "";
		do {

			// Get URI From Request Body
			baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[READBUFFER];
			int counter = 0;
			while ((counter = request.getInputStream().readLine(buffer, 0,
					READBUFFER)) > 0) {
				baos.write(buffer, 0, counter);
				if (buffer[counter - 1] == 10)
					break;
			}
			size = baos.size();
			if (size <= 2) {
				furtherLoop = false;
			} else {
				byte[] idxBuffer = new byte[size];
				idxBuffer = baos.toByteArray();
				uri = new String(idxBuffer, "UTF-8");
			}
			baos.close();
			if (!furtherLoop)
				break;

			// Check URI
			if (!uri.endsWith("\r\n")) {
				this.reportStreamError(DasResponse.SC_BODY_FORMAT_CORRUPT,
						"LEGALHOLD ADD: URI " + uri
								+ " does not meet specifications", bw);
				return false;
			} else {

				// Cut Line Feed And Carriage Return
				int crIdx = uri.lastIndexOf(13);
				int lfIdx = uri.lastIndexOf(10);
				StringBuffer sbuf = new StringBuffer(uri);
				if (crIdx != -1)
					sbuf.deleteCharAt(crIdx);
				if (lfIdx != -1)
					sbuf.deleteCharAt(crIdx);
				uri = sbuf.toString();
				uri = uri.toLowerCase();
				try {
					this.isValidName(uri, true);
				} catch (InvalidNameException inex) {

					// $JL-EXC$
					this.reportStreamError(DasResponse.SC_INVALID_CHARACTER,
							"LEGALHOLD ADD: " + inex.getMessage(), bw);
					return false;
				}
				if ((uri.indexOf("//") != -1) || !uri.startsWith("/")
						|| uri.endsWith("/") || uri.length() < 2) {
					this.reportStreamError(
							DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
							"LEGALHOLD ADD: Value " + uri
									+ " of request header "
									+ "URI does not meet specifications", bw);
					return false;
				}
			}

			// Check If Legal Hold Case Should Be Added For A Collection Or A
			// Resource
			try {
				pst = connection.prepareStatement(SEL_COL_TAB);
				pst.setString(1, uri);
				result = pst.executeQuery();
				hits = 0;
				while (result.next()) {
					colId = result.getLong("COLID");
					storeId = result.getLong("STOREID");
					hits++;
				}
				result.close();
				pst.close();
				if (hits == 1) {
					range = "COL";
				} else {
					if (uri.indexOf('/') == uri.lastIndexOf('/')) {
						this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
								"LEGALHOLD ADD: URI " + uri + " not found", bw);
						return false;
					}

					pst = connection.prepareStatement(SEL_COL_TAB);
					pst.setString(1, uri.substring(0, uri.lastIndexOf("/")));
					result = pst.executeQuery();
					hits = 0;
					while (result.next()) {
						colId = result.getLong("COLID");
						storeId = result.getLong("STOREID");
						hits++;
					}
					result.close();
					pst.close();
					if (hits == 1) {
						resName = uri.substring(uri.lastIndexOf("/") + 1);
						pst = connection.prepareStatement(SEL_RES_TAB);
						pst.setString(1, resName);
						pst.setLong(2, colId);
						result = pst.executeQuery();
						hits = 0;
						while (result.next()) {
							hits++;
						}
						result.close();
						pst.close();
						if (hits == 1) {
							range = "RES";
						} else {
							this
									.reportStreamError(
											HttpServletResponse.SC_CONFLICT,
											"LEGALHOLD ADD: URI "
													+ uri
													+ " is not assigned to a collection or resource",
											bw);
							return false;
						}

					} else {
						this
								.reportStreamError(
										HttpServletResponse.SC_CONFLICT,
										"LEGALHOLD ADD: URI "
												+ uri
												+ " is not assigned to a collection or resource",
										bw);

						return false;
					}
				}

				// Get All Archive Stores For Requested URI
				if (storeId == 0) {
					pst = connection.prepareStatement(SEL_COL_STO_TAB);
					pst.setLong(1, colId);
					result = pst.executeQuery();
					hits = 0;
					while (result.next()) {
						storeIdList
								.add(Long.valueOf(result.getLong("STOREID")));
						hits++;
					}
					result.close();
					pst.close();
					if (hits == 0) {
						this
								.reportStreamError(
										DasResponse.SC_STORE_NOT_ASSIGNED,
										"LEGALHOLD ADD: URI "
												+ uri
												+ " is either not yet assigned or not uniquely assigned to an archive store",
										bw);
						return false;
					}
				} else {
					storeIdList.add(Long.valueOf(storeId));
				}

				// Check If All Archive Stores Are WebDAV Types
				String storeType = "";
				for (Iterator<Long> iter = storeIdList.iterator(); iter
						.hasNext();) {
					pst = connection.prepareStatement(SEL_CFG_TAB);
					pst.setLong(1, iter.next().longValue());
					result = pst.executeQuery();
					while (result.next()) {
						storeType = result.getString("STORETYPE");
					}
					result.close();
					pst.close();
					if (!storeType.equalsIgnoreCase("W")) {
						this
								.reportStreamError(
										HttpServletResponse.SC_FORBIDDEN,
										"LEGALHOLD ADD: Adding legal hold cases is only allowed for resources or collections on WebDAV archive stores",
										bw);
						return false;
					}
				}
			} catch (SQLException sqlex) {
				this.reportStreamError(DasResponse.SC_SQL_ERROR,
						"LEGALHOLD ADD: " + sqlex.toString(), sqlex, bw);
				return false;
			} finally {
				try {
					if (pst != null)
						pst.close();
				} catch (SQLException sqlex) {
					this.reportStreamError(DasResponse.SC_SQL_ERROR,
							"LEGALHOLD ADD: " + sqlex.toString(), sqlex, bw);
					errorOccurred = true;
				}
			}

			// Check If Error Occurred
			if (errorOccurred == true)
				break;

			// Check If All Archive Stores Are Running
			String testResult = "";
			for (Iterator<Long> iter = storeIdList.iterator(); iter.hasNext();) {

				// Get Configuration Data For All Archive Stores
				try {
					sac = this.getArchStoreConfigObject(beanLocalHome, iter
							.next().longValue());
					storeList.add(sac);

					// Check If A WebDAV System With Correct ILM Conformance
					// Class Is Selected
					if (sac.ilm_conformance == 0) {
						cat
								.warningT(
										loc,
										"LEGALHOLD ADD: URI "
												+ uri
												+ " is not assigned to a WebDAV archive store with a correct ILM conformance class");
					}
				} catch (ArchStoreConfigException ascex) {
					this.reportStreamError(DasResponse.SC_CONFIG_INCONSISTENT,
							"LEGALHOLD ADD: " + ascex.getMessage(), ascex, bw);
					return false;
				}

				// Test Archive Store
				testResult = ArchiveStoreTest.execute(sac);
				if (!testResult.startsWith("S")) {
					this.reportStreamError(HttpServletResponse.SC_CONFLICT,
							"LEGALHOLD ADD: " + testResult.substring(2), bw);
					return false;
				}
			}

			if (this.att_col.equalsIgnoreCase("Y")) {

				// Check If Setting Legal Hold Cases For Attachments Is Allowed
				if (range.equalsIgnoreCase("COL")) {
					this
							.reportStreamError(
									HttpServletResponse.SC_FORBIDDEN,
									"LEGALHOLD ADD: Adding legal hold cases is not allowed for an attachment collection URI",
									bw);
					return false;
				}

				// Check If Naming For Attachment Collection Is Correct
				if (!(resName.startsWith("_") && (resName.endsWith(".xml")))) {
					this
							.reportStreamError(
									HttpServletResponse.SC_FORBIDDEN,
									"LEGALHOLD ADD: Name pattern for attachment collection does not meet specifications",
									bw);
					return false;
				}

				// Build Uri For Attachment Collection
				StringBuffer sb = new StringBuffer();
				sb.append(uri.substring(0, uri.lastIndexOf("/") + 1));
				sb.append("_");
				sb.append(resName.substring(0, resName.length() - 4));
				attachment_uri = sb.toString();
			}

			// Add Legal Hold On All Archive Stores
			XmlDasLegalHoldRequest legalHoldAttRequest = null;
			XmlDasLegalHoldRequest legalHoldRequest = null;
			XmlDasLegalHold legalHoldAtt = null;
			XmlDasLegalHold legalHold = null;
			XmlDasLegalHoldResponse legalHoldAttResponse = null;
			XmlDasLegalHoldResponse legalHoldResponse = null;
			for (Iterator<Sapxmla_Config> iter = storeList.iterator(); iter
					.hasNext();) {

				// Get Archive Store Data
				sac = iter.next();

				// Add Legal Hold For Attachment Collection
				if (this.att_col.equalsIgnoreCase("Y")) {
					legalHoldAttRequest = new XmlDasLegalHoldRequest(sac,
							attachment_uri, ilm_case, "Add", range);
					legalHoldAtt = new XmlDasLegalHold(legalHoldAttRequest);
					legalHoldAttResponse = legalHoldAtt.execute();
					if ((legalHoldAttResponse.getStatusCode() == HttpServletResponse.SC_OK)) {

						// Write Roll Back Information
						rollbackAttachProps.add(sac);
					} else if (legalHoldAttResponse.getStatusCode() != HttpServletResponse.SC_NOT_FOUND) {

						// Error Occurred
						errorOccurred = true;
						break;
					}
				}

				// Add Legal Hold For XMLDAS Collection Or Resource
				legalHoldRequest = new XmlDasLegalHoldRequest(sac, uri,
						ilm_case, "Add", range);
				legalHold = new XmlDasLegalHold(legalHoldRequest);
				legalHoldResponse = legalHold.execute();
				if (legalHoldResponse.getStatusCode() == HttpServletResponse.SC_OK) {

					// Write Roll Back Information
					rollbackXmldasProps.add(sac);
				} else {

					// Error Occurred
					errorOccurred = true;
					break;
				}
			} // end for loop

			// Check If Error Occurred
			if (errorOccurred == true) {

				// Roll Back Legal Hold Cases For Current Uri On All Archive
				// Stores
				rollbackFailed = rollback(attachment_uri, uri, range);

				// Write Log Informations
				if (rollbackFailed == true) {
					if (legalHoldResponse == null)
						this
								.reportStreamError(
										DasResponse.SC_IO_ERROR,
										"LEGALHOLD ADD: Error occurred while adding legal hold case for URI "
												+ uri
												+ " on archive store "
												+ sac.archive_store
												+ ". The automatic roll back of the added legal hold case failed - see XMLDAS log for details",
										bw);
					else
						this
								.reportStreamError(
										DasResponse.SC_IO_ERROR,
										"LEGALHOLD ADD: Error occurred while adding legal hold case for URI "
												+ uri
												+ " on archive store "
												+ sac.archive_store
												+ ". The automatic roll back of the added legal hold case failed - see XMLDAS log for details: "
												+ legalHoldResponse
														.getStatusCode()
												+ " "
												+ legalHoldResponse
														.getReasonPhrase()
												+ " "
												+ legalHoldResponse
														.getEntityBody(), bw);
				} else {
					if (legalHoldResponse == null)
						this.reportStreamError(DasResponse.SC_IO_ERROR,
								"LEGALHOLD ADD: Error occurred while adding legal hold case for URI "
										+ uri + " on archive store "
										+ sac.archive_store, bw);
					else
						this
								.reportStreamError(DasResponse.SC_IO_ERROR,
										"LEGALHOLD ADD: Error occurred while adding legal hold case for URI "
												+ uri
												+ " on archive store "
												+ sac.archive_store
												+ ": "
												+ legalHoldResponse
														.getStatusCode()
												+ " "
												+ legalHoldResponse
														.getReasonPhrase()
												+ " "
												+ legalHoldResponse
														.getEntityBody(), bw);
				}
				break;
			} else {

				// Write Result Into Response Body
				if (this.att_col.equalsIgnoreCase("Y")) {
					bw.write(attachment_uri + "\r\n");
				}
				bw.write(uri + "\r\n");

				// Clear Roll Back Lists For Current Uri
				rollbackAttachProps.clear();
				rollbackXmldasProps.clear();
			}
		} while (furtherLoop); // end do-while loop

		// Check If Error Occurred
		if (errorOccurred == true)
			return false;

		// Request Was Successful
		try {
			this.writeStatus(bw, HttpServletResponse.SC_OK, "Ok");
			bw.flush();
			bw.close();
		} catch (IOException ioex) {
			MasterMethod.cat.errorT(loc, getStackTrace(ioex));
			throw new IOException(ioex.toString());
		}
		return true;
	}

	private boolean rollback(String attachment_uri, String uri, String range) {

		// Set Status
		boolean errorOccurred = false;

		// Roll Back Attachment Legal Hold Cases
		Sapxmla_Config sac;
		XmlDasLegalHoldRequest legalHoldRequest;
		XmlDasLegalHold legalHold;
		XmlDasLegalHoldResponse legalHoldResponse;
		for (Iterator<Sapxmla_Config> iter = this.rollbackAttachProps
				.iterator(); iter.hasNext();) {
			sac = iter.next();
			legalHoldRequest = new XmlDasLegalHoldRequest(sac, attachment_uri,
					ilm_case, "Remove", range);
			legalHold = new XmlDasLegalHold(legalHoldRequest);
			legalHoldResponse = legalHold.execute();
			if (legalHoldResponse.getStatusCode() != HttpServletResponse.SC_OK) {
				cat.errorT(loc,
						"LEGALHOLD ADD: Error occurred while removing legal hold case for URI "
								+ attachment_uri + " on archive store "
								+ sac.archive_store + ": "
								+ legalHoldResponse.getStatusCode() + " "
								+ legalHoldResponse.getReasonPhrase() + " "
								+ legalHoldResponse.getEntityBody());
				errorOccurred = true;
			}
		}

		// Roll Back Xmldas Legal Hold Cases
		for (Iterator<Sapxmla_Config> iter = this.rollbackXmldasProps
				.iterator(); iter.hasNext();) {
			sac = iter.next();
			legalHoldRequest = new XmlDasLegalHoldRequest(sac, uri, ilm_case,
					"Remove", range);
			legalHold = new XmlDasLegalHold(legalHoldRequest);
			legalHoldResponse = legalHold.execute();
			if (legalHoldResponse.getStatusCode() != HttpServletResponse.SC_OK) {
				cat.errorT(loc,
						"LEGALHOLD ADD: Error occurred while removing legal hold case for URI "
								+ uri + " on archive store "
								+ sac.archive_store + ": "
								+ legalHoldResponse.getStatusCode() + " "
								+ legalHoldResponse.getReasonPhrase() + " "
								+ legalHoldResponse.getEntityBody());
				errorOccurred = true;
			}
		}

		// Return Status
		return errorOccurred;
	}
}
