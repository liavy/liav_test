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
import com.sap.archtech.daservice.storage.XmlDasMaster;
import com.sap.archtech.daservice.storage.XmlDasPropFind;
import com.sap.archtech.daservice.storage.XmlDasPropFindRequest;
import com.sap.archtech.daservice.storage.XmlDasPropFindResponse;

public class PropertyGetMethod extends MasterMethod {

	private final static String SEL_COL_TAB = "SELECT COLID, STOREID FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_RES_TAB = "SELECT RESID FROM BC_XMLA_RES WHERE RESNAME = ? AND COLID = ?";
	private final static String SEL_COL_STO_TAB = "SELECT STOREID FROM BC_XMLA_COL_STORE WHERE COLID = ? ORDER BY STOREID";
	private final static String SEL_CFG_TAB = "SELECT STORETYPE FROM BC_XMLA_CONFIG WHERE STOREID = ?";
	private final static String SEL_COL_PRP = "SELECT * FROM BC_XMLA_COL_PROP WHERE COLID = ? AND COLPROPNAME = ?";

	private Connection connection;
	private ArchStoreConfigLocalHome beanLocalHome;
	private String uri;
	private String property_name;
	private String suppress_log;

	public PropertyGetMethod(HttpServletResponse response,
			Connection connection, ArchStoreConfigLocalHome beanLocalHome,
			String uri, String property_name, String suppress_log) {
		this.response = response;
		this.connection = connection;
		this.beanLocalHome = beanLocalHome;
		this.uri = uri;
		this.property_name = property_name;
		this.suppress_log = suppress_log;
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
		Sapxmla_Config sac;
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
					"PROPERTY GET: URI missing from request header", bw);
			return false;
		} else {
			this.uri = this.uri.toLowerCase();
			try {
				this.isValidName(this.uri, true);
			} catch (InvalidNameException inex) {

				// $JL-EXC$
				this.reportStreamError(DasResponse.SC_INVALID_CHARACTER,
						"PROPERTY GET: " + inex.getMessage(), bw);
				return false;
			}
			if ((this.uri.indexOf("//") != -1) || !this.uri.startsWith("/")
					|| this.uri.endsWith("/") || this.uri.length() < 2) {
				this.reportStreamError(
						DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
						"PROPERTY GET: Value " + this.uri
								+ " of request header "
								+ "URI does not meet specifications", bw);
				return false;
			}
		}

		// Check Request Header "property_name"
		if (this.property_name == null) {
			this.reportStreamError(DasResponse.SC_PARAMETER_MISSING,
					"PROPERTY GET: PROPERTY_NAME missing from request header",
					bw);
			return false;
		} else {
			this.property_name = this.property_name.toLowerCase().trim();
			if (this.property_name.startsWith("legal_hold")) {
				this.reportStreamError(HttpServletResponse.SC_FORBIDDEN,
						"PROPERTY GET: Property 'legal_hold' must not be get",
						bw);
				return false;
			}
		}

		// Check Request Header "suppress_log"
		if (this.suppress_log == null) {
			this.suppress_log = "N";
		} else {
			this.suppress_log = this.suppress_log.toUpperCase();
			if (this.suppress_log.startsWith("Y"))
				this.suppress_log = "Y";
			else
				this.suppress_log = "N";
		}

		// Check If Properties Should Be Get For A Collection Or A Resource
		try {
			pst = connection.prepareStatement(SEL_COL_TAB);
			pst.setString(1, this.uri);
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
							"PROPERTY GET: URI " + uri + " not found", bw);
					return false;
				}

				pst = connection.prepareStatement(SEL_COL_TAB);
				pst.setString(1, this.uri.substring(0, this.uri
						.lastIndexOf("/")));
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
					resName = this.uri.substring(this.uri.lastIndexOf("/") + 1);
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
										"PROPERTY GET: URI "
												+ this.uri
												+ " is not assigned to a collection or resource",
										bw);
						return false;
					}

				} else {
					this
							.reportStreamError(
									HttpServletResponse.SC_CONFLICT,
									"PROPERTY GET: URI "
											+ this.uri
											+ " is not assigned to a collection or resource",
									bw);

					return false;
				}
			}

			// Check If "origin" Property Is Existing For Performance Reasons
			if (this.property_name.equalsIgnoreCase("origin")) {
				pst = connection.prepareStatement(SEL_COL_PRP);
				pst.setLong(1, colId);
				pst.setString(2, "origin");
				result = pst.executeQuery();
				hits = 0;
				while (result.next())
					hits++;
				result.close();
				pst.close();
				if (hits == 0) {
					if (this.suppress_log.equalsIgnoreCase("Y")) {
						this.response.setStatus(DasResponse.SC_DOES_NOT_EXISTS);
						this.writeStatus(bw, DasResponse.SC_DOES_NOT_EXISTS,
								"PROPERTY GET: Property " + this.property_name
										+ " not found for URI " + this.uri);
						bw.flush();
						bw.close();
					} else {
						this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
								"PROPERTY GET: Property " + this.property_name
										+ " not found for URI " + this.uri, bw);
					}
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
									"PROPERTY GET: URI "
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
									"PROPERTY GET: Getting properties is only allowed for resources or collections on WebDAV archive stores",
									bw);
					return false;
				}
			}
		} catch (SQLException sqlex) {
			this.reportStreamError(DasResponse.SC_SQL_ERROR, "PROPERTY GET: "
					+ sqlex.toString(), sqlex, bw);
			return false;
		} finally {
			try {
				if (pst != null)
					pst.close();
			} catch (SQLException sqlex) {
				this.reportStreamError(DasResponse.SC_SQL_ERROR,
						"PROPERTY GET: " + sqlex.toString(), sqlex, bw);
				errorOccurred = true;
			}
		}

		// Check If Error Occurred
		if (errorOccurred == false) {

			// Get Archive Store Configuration Data
			try {

				// Get Properties Of Collection Or Resource From First Archive
				// Store
				sac = this.getArchStoreConfigObject(beanLocalHome,
						((Long) storeIdList.get(0)).longValue());
			} catch (ArchStoreConfigException ascex) {
				this.reportStreamError(DasResponse.SC_CONFIG_INCONSISTENT,
						"PROPERTY GET: " + ascex.getMessage(), ascex, bw);
				return false;
			}

			// Check If A WebDAV System With Correct ILM Conformance Class Is
			// Selected
			if (!sac.type.equalsIgnoreCase("W")) {
				this.reportStreamError(HttpServletResponse.SC_CONFLICT,
						"PROPERTY GET: URI " + this.uri
								+ " is not assigned to a WebDAV archive store",
						bw);
				return false;
			}
			if (sac.ilm_conformance == 0) {
				cat
						.warningT(
								loc,
								"PROPERTY GET: URI "
										+ uri
										+ " is not assigned to a WebDAV archive store with a correct ILM conformance class");
			}

			// Get Properties On WebDAV System
			XmlDasPropFindRequest propFindRequest = new XmlDasPropFindRequest(
					sac, uri, property_name, range);
			XmlDasPropFind propFind = new XmlDasPropFind(propFindRequest);
			XmlDasPropFindResponse propFindResponse = propFind.execute();
			if (propFindResponse.getStatusCode() != HttpServletResponse.SC_OK) {
				if (propFindResponse.getStatusCode() == HttpServletResponse.SC_NOT_FOUND) {
					if (this.suppress_log.equalsIgnoreCase("Y")) {
						this.response.setStatus(DasResponse.SC_DOES_NOT_EXISTS);
						this.writeStatus(bw, DasResponse.SC_DOES_NOT_EXISTS,
								"PROPERTY GET: Property " + this.property_name
										+ " not found for URI " + this.uri);
						bw.flush();
						bw.close();
					} else {
						this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
								"PROPERTY GET: Property " + this.property_name
										+ " not found for URI " + this.uri, bw);
					}
				} else {
					if (this.suppress_log.equalsIgnoreCase("Y")) {
						this.response
								.setStatus(XmlDasMaster.SC_CANNOT_READ_WEBDAV_PROPERTY);
						this.writeStatus(bw,
								XmlDasMaster.SC_CANNOT_READ_WEBDAV_PROPERTY,
								"PROPERTY GET: Error occurred while getting following properties for URI "
										+ this.uri + ": "
										+ propFindResponse.getEntityBody());
						bw.flush();
						bw.close();
					} else {
						this.reportStreamError(
								XmlDasMaster.SC_CANNOT_READ_WEBDAV_PROPERTY,
								"PROPERTY GET: Error occurred while getting following properties for URI "
										+ this.uri + ": "
										+ propFindResponse.getEntityBody(), bw);
					}
				}
				return false;
			}

			// Prepare For Sending The Result
			try {
				bw.write(propFindResponse.getEntityBody());
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
