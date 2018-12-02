package com.sap.archtech.daservice.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;
import com.sap.archtech.daservice.exceptions.ArchStoreConfigException;
import com.sap.archtech.daservice.exceptions.InvalidNameException;
import com.sap.archtech.daservice.storage.XmlDasLegalHold;
import com.sap.archtech.daservice.storage.XmlDasLegalHoldRequest;
import com.sap.archtech.daservice.storage.XmlDasLegalHoldResponse;

public class LegalHoldGetMethod extends MasterMethod {

	private final static String SEL_COL_TAB = "SELECT COLID, STOREID FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_RES_TAB = "SELECT RESID FROM BC_XMLA_RES WHERE RESNAME = ? AND COLID = ?";
	private final static String SEL_COL_STO_TAB = "SELECT STOREID FROM BC_XMLA_COL_STORE WHERE COLID = ? ORDER BY STOREID";
	private final static String SEL_CFG_TAB = "SELECT STORETYPE FROM BC_XMLA_CONFIG WHERE STOREID = ?";

	private Connection connection;
	private ArchStoreConfigLocalHome beanLocalHome;
	private String uri;

	public LegalHoldGetMethod(HttpServletResponse response,
			Connection connection, ArchStoreConfigLocalHome beanLocalHome,
			String uri) {
		this.response = response;
		this.connection = connection;
		this.beanLocalHome = beanLocalHome;
		this.uri = uri;
	}

	public boolean execute() throws IOException {

		// Variables
		boolean errorOccurred = false;
		int hits = 0;
		long colId = 0;
		long storeId = 0;
		String range = "";
		String resName = "";
		PreparedStatement pst = null;
		ResultSet result = null;
		Sapxmla_Config sac = null;
		BufferedWriter bw;
		ArrayList<Long> storeIdList = new ArrayList<Long>();

		// Set Response Header Fields
		response.setContentType("text/xml");
		response.setHeader("service_message", "see message body");

		// Prepare Sending Response Body
		bw = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream(), "UTF8"));

		// Check Request Header "uri"
		if (this.uri == null) {
			this.reportStreamError(DasResponse.SC_PARAMETER_MISSING,
					"LEGALHOLD GET: URI missing from request header", bw);
			return false;
		} else {
			this.uri = this.uri.toLowerCase();
			try {
				this.isValidName(this.uri, true);
			} catch (InvalidNameException inex) {

				// $JL-EXC$
				this.reportStreamError(DasResponse.SC_INVALID_CHARACTER,
						"LEGALHOLD GET: " + inex.getMessage(), bw);
				return false;
			}
			if ((this.uri.indexOf("//") != -1) || !this.uri.startsWith("/")
					|| this.uri.endsWith("/") || this.uri.length() < 2) {
				this.reportStreamError(
						DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
						"LEGALHOLD GET: Value " + this.uri
								+ " of request header "
								+ "URI does not meet specifications", bw);
				return false;
			}
		}

		// Check If Legal Hold Case Should Be Get For A Collection Or A Resource
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
							"LEGALHOLD GET: URI " + uri + " not found", bw);
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
										"LEGALHOLD GET: URI "
												+ uri
												+ " is not assigned to a collection or resource",
										bw);
						return false;
					}
				} else {
					this
							.reportStreamError(
									HttpServletResponse.SC_CONFLICT,
									"LEGALHOLD GET: URI "
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
					storeIdList.add(Long.valueOf(result.getLong("STOREID")));
					hits++;
				}
				result.close();
				pst.close();
				if (hits == 0) {
					this
							.reportStreamError(
									DasResponse.SC_STORE_NOT_ASSIGNED,
									"LEGALHOLD GET: URI "
											+ this.uri
											+ " is either not yet assigned or not uniquely assigned to an archive store",
									bw);
					return false;
				}
			} else {
				storeIdList.add(Long.valueOf(storeId));
			}

			// Check If All Archive Stores Are WebDAV Types
			String storeType = "";
			for (Iterator<Long> iter = storeIdList.iterator(); iter.hasNext();) {
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
									"LEGALHOLD GET: Getting legal hold cases is only allowed for resources or collections on WebDAV archive stores",
									bw);
					return false;
				}
			}
		} catch (SQLException sqlex) {
			this.reportStreamError(DasResponse.SC_SQL_ERROR, "LEGALHOLD ADD: "
					+ sqlex.toString(), sqlex, bw);
			return false;
		} finally {
			try {
				if (pst != null)
					pst.close();
			} catch (SQLException sqlex) {
				this.reportStreamError(DasResponse.SC_SQL_ERROR,
						"LEGALHOLD GET: " + sqlex.toString(), sqlex, bw);
				errorOccurred = true;
			}
		}

		// Check If Error Occurred
		if (errorOccurred == false) {

			// Get Properties Of Collection Or Resource From First Archive Store
			try {
				sac = this.getArchStoreConfigObject(beanLocalHome,
						((Long) storeIdList.get(0)).longValue());
			} catch (ArchStoreConfigException ascex) {
				this.reportStreamError(DasResponse.SC_CONFIG_INCONSISTENT,
						"LEGALHOLD GET: " + ascex.getMessage(), ascex, bw);
				return false;
			}

			// Check If A WebDAV System With Correct ILM Conformance Class Is
			// Selected
			if (!sac.type.equalsIgnoreCase("W")) {
				this.reportStreamError(HttpServletResponse.SC_FORBIDDEN,
						"LEGALHOLD GET: URI " + uri
								+ " is not assigned to a WebDAV archive store",
						bw);
				return false;
			}
			if (sac.ilm_conformance == 0) {
				cat
						.warningT(
								loc,
								"LEGALHOLD GET: URI "
										+ uri
										+ " is not assigned to a WebDAV archive store with a correct ILM conformance class ");
			}

			// Get Legal Hold Cases For XMLDAS Collection Or Resource On WebDAV
			// System
			XmlDasLegalHoldRequest legalHoldRequest = new XmlDasLegalHoldRequest(
					sac, uri, "N", "Get", range);
			XmlDasLegalHold legalHold = new XmlDasLegalHold(legalHoldRequest);
			XmlDasLegalHoldResponse legalHoldResponse = legalHold.execute();

			// Evaluate Response
			if (legalHoldResponse.getStatusCode() == HttpServletResponse.SC_OK) {
				if (legalHoldResponse.getEntityBody() != null)
					bw.write(legalHoldResponse.getEntityBody());
			} else {
				if (legalHoldResponse.getException() == null) {
					this.reportStreamError(legalHoldResponse.getStatusCode(),
							"LEGALHOLD GET: Error occurred while getting legal hold cases for uri "
									+ this.uri + ": "
									+ legalHoldResponse.getStatusCode() + " "
									+ legalHoldResponse.getReasonPhrase() + " "
									+ legalHoldResponse.getEntityBody(), bw);
				} else {
					this.reportStreamError(legalHoldResponse.getStatusCode(),
							"LEGALHOLD GET: Error occurred while getting legal hold cases for uri "
									+ this.uri + ": "
									+ legalHoldResponse.getStatusCode() + " "
									+ legalHoldResponse.getReasonPhrase() + " "
									+ legalHoldResponse.getEntityBody(),
							legalHoldResponse.getException(), bw);
				}
				return false;
			}
			try {
				this.writeStatus(bw, HttpServletResponse.SC_OK, "Ok");
				bw.flush();
				bw.close();
			} catch (IOException ioex) {
				MasterMethod.cat.errorT(loc, getStackTrace(ioex));
				throw new IOException(ioex.toString());
			}
			return true;
		} else
			return false;
	}
}
