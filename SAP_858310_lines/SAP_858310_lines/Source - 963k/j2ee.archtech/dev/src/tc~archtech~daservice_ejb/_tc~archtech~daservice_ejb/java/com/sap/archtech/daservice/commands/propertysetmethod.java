package com.sap.archtech.daservice.commands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;
import com.sap.archtech.daservice.exceptions.ArchStoreConfigException;
import com.sap.archtech.daservice.exceptions.InvalidNameException;
import com.sap.archtech.daservice.storage.XmlDasMaster;
import com.sap.archtech.daservice.storage.XmlDasPropPatch;
import com.sap.archtech.daservice.storage.XmlDasPropPatchRequest;
import com.sap.archtech.daservice.storage.XmlDasPropPatchResponse;
import com.sap.archtech.daservice.util.ArchiveStoreTest;

public class PropertySetMethod extends MasterMethod {

	private final static int READBUFFER = 128;
	private final static String SEL_COL_TAB = "SELECT COLID, STOREID FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_RES_TAB = "SELECT RESID FROM BC_XMLA_RES WHERE RESNAME = ? AND COLID = ?";
	private final static String SEL_COL_STO_TAB = "SELECT STOREID FROM BC_XMLA_COL_STORE WHERE COLID = ? ORDER BY STOREID";
	private final static String SEL_CFG_TAB = "SELECT STORETYPE FROM BC_XMLA_CONFIG WHERE STOREID = ?";
	private final static String SEL_COL_PRP = "SELECT COLPROPVALUE FROM BC_XMLA_COL_PROP WHERE COLID = ? AND COLPROPNAME = ?";
	private final static String INS_COL_PRP = "INSERT INTO BC_XMLA_COL_PROP (COLID, COLPROPNAME, COLPROPVALUE, COLNAME) VALUES (?, ?, ?, ?)";
	private final static String UPD_COL_PRP = "UPDATE BC_XMLA_COL_PROP SET COLPROPVALUE = ? WHERE COLID = ? AND COLPROPNAME = ?";

	private HttpServletRequest request;
	private Connection connection;
	private ArchStoreConfigLocalHome beanLocalHome;
	private String uri;
	private String att_col;
	private String attachment_uri = "";
	private String properties = "";
	private ArrayList<Sapxmla_Config> rollbackAttachProps = new ArrayList<Sapxmla_Config>();
	private ArrayList<Sapxmla_Config> rollbackXmldasProps = new ArrayList<Sapxmla_Config>();

	public PropertySetMethod(HttpServletRequest request,
			HttpServletResponse response, Connection connection,
			ArchStoreConfigLocalHome beanLocalHome, String uri, String att_col) {
		this.request = request;
		this.response = response;
		this.connection = connection;
		this.beanLocalHome = beanLocalHome;
		this.uri = uri;
		this.att_col = att_col;
	}

	public boolean execute() throws IOException {

		// Variables
		boolean errorOccurred = false;
		boolean multiStatusErrorOccurred = false;
		boolean rollbackFailed = false;
		boolean containsOriginProperty = false;
		int hits = 0;
		long colId = 0;
		long storeId = 0;
		String range = "";
		String resName = "";
		String colPropName = "";
		String colPropValue = "";
		String colName = "";
		String dbcolPropValue = "";
		String multiStatusErrorString = "";
		PreparedStatement pst = null;
		ResultSet result = null;
		Sapxmla_Config sac = null;
		ArrayList<Long> storeIdList = new ArrayList<Long>();
		ArrayList<Sapxmla_Config> storeList = new ArrayList<Sapxmla_Config>();

		// Set Response Header Fields
		response.setContentType("text/xml");

		// Check Request Header "uri"
		if (this.uri == null) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"PROPERTY SET: URI missing from request header");
			return false;
		} else {
			this.uri = this.uri.toLowerCase();
			try {
				this.isValidName(this.uri, true);
			} catch (InvalidNameException inex) {

				// $JL-EXC$
				this.reportError(DasResponse.SC_INVALID_CHARACTER,
						"PROPERTY SET: " + inex.getMessage());
				return false;
			}
			if ((this.uri.indexOf("//") != -1) || !this.uri.startsWith("/")
					|| this.uri.endsWith("/") || this.uri.length() < 2) {
				this.reportError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
						"PROPERTY SET: Value " + this.uri
								+ " of request header "
								+ "URI does not meet specifications");
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
				this.reportError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
						"PROPERTY SET: Value " + this.att_col
								+ " of request header "
								+ "ATT_COL does not meet specifications");
				return false;
			}
		}

		// Get Properties From Request Body
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[READBUFFER];
		int counter = 0;
		while ((counter = request.getInputStream().readLine(buffer, 0,
				READBUFFER)) > 0) {
			baos.write(buffer, 0, counter);
		}
		byte[] idxBuffer = new byte[baos.size()];
		idxBuffer = baos.toByteArray();
		properties = new String(idxBuffer, "UTF-8");
		baos.close();

		// Cut Line Feed And Carriage Return
		int crIdx = properties.lastIndexOf(13);
		int lfIdx = properties.lastIndexOf(10);
		StringBuffer sbuf = new StringBuffer(properties);
		if (crIdx != -1)
			sbuf.deleteCharAt(crIdx);
		if (lfIdx != -1)
			sbuf.deleteCharAt(crIdx);
		properties = sbuf.toString();

		// Check Properties Syntax
		StringTokenizer st = new StringTokenizer(properties, "#");
		int propertyCounter = 0;
		while (st.hasMoreTokens()) {
			String tokenCaseSensitive = (String) st.nextToken();
			String token = tokenCaseSensitive.toLowerCase();
			if (token.startsWith("legal_hold=")) {
				this.reportError(HttpServletResponse.SC_FORBIDDEN,
						"PROPERTY SET: Property 'legal_hold' must not be set");
				return false;
			}
			int index = token.indexOf("=");
			if (index == 0) {
				this.reportError(DasResponse.SC_BODY_FORMAT_CORRUPT,
						"PROPERTY SET: Property name missing in request body");
				return false;
			}
			if (index == -1) {
				this
						.reportError(DasResponse.SC_BODY_FORMAT_CORRUPT,
								"PROPERTY SET: Separation character '=' missing in request body");
				return false;
			}
			if (token.startsWith("origin=")) {
				containsOriginProperty = true;
				colPropName = ORIGIN;
				colPropValue = tokenCaseSensitive.substring(index + 1,
						tokenCaseSensitive.length());
				if (colPropValue.length() == 0)
					colPropValue = " ";
				colName = this.uri.substring(this.uri.lastIndexOf("/") + 1,
						this.uri.length());
			}
			propertyCounter++;
		}

		try {

			// Check If Properties Should Be Set For A Collection Or A Resource
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
					this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
							"PROPERTY SET: URI " + uri + " not found");
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
								.reportError(
										HttpServletResponse.SC_CONFLICT,
										"PROPERTY SET: URI "
												+ this.uri
												+ " is not assigned to a collection or resource");
						return false;
					}
				} else {
					this
							.reportError(
									HttpServletResponse.SC_CONFLICT,
									"PROPERTY SET: URI "
											+ this.uri
											+ " is not assigned to a collection or resource");
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
							.reportError(
									DasResponse.SC_STORE_NOT_ASSIGNED,
									"PROPERTY SET: URI "
											+ this.uri
											+ " is either not yet assigned or not uniquely assigned to an archive store");
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
							.reportError(
									HttpServletResponse.SC_FORBIDDEN,
									"PROPERTY SET: Setting properties is only allowed for resources or collections on WebDAV archive stores");
					return false;
				}
			}

			// Origin Property Special Treatment
			if ((containsOriginProperty == true)
					&& (range.equalsIgnoreCase("COL"))) {

				// Check If Origin Property Is Already Set
				hits = 0;
				pst = connection.prepareStatement(SEL_COL_PRP);
				pst.setLong(1, colId);
				pst.setString(2, ORIGIN);
				result = pst.executeQuery();
				while (result.next()) {
					dbcolPropValue = result.getString("COLPROPVALUE");
					hits++;
				}
				result.close();
				pst.close();
				if (hits > 1) {
					this
							.reportError(DasResponse.SC_SQL_ERROR,
									"PROPERTY SET: Database table BC_XMLA_COL_PROP has an inconsistent state");
					return false;
				}

				// Case Distinction
				if (hits == 0) {
					try {

						// Insert Origin Property Into Database
						pst = connection.prepareStatement(INS_COL_PRP);
						pst.setLong(1, colId);
						pst.setString(2, colPropName);
						pst.setString(3, colPropValue);
						pst.setString(4, colName);
						pst.executeUpdate();
						pst.close();
					} catch (SQLException sqlex) {

						// $JL-EXC$
						if (propertyCounter == 1) {

							// Nothing To Do -> Exit Property Set Method
							this.response.setHeader("service_message", "Ok");
							return true;
						} else {

							// Cut Origin Property from Property String
							StringTokenizer st1 = new StringTokenizer(
									properties, "#");
							StringBuffer sb = new StringBuffer();
							while (st1.hasMoreTokens()) {
								String tokenCaseSensitive = st1.nextToken();
								String token = tokenCaseSensitive.toLowerCase();
								if (!token.startsWith("origin="))
									if (sb.length() == 0)
										sb.append(tokenCaseSensitive);
									else {
										sb.append("#");
										sb.append(tokenCaseSensitive);
									}
							}
							properties = sb.toString();
						}
					}
				} else {
					if (dbcolPropValue.equals(colPropValue)) {
						if (propertyCounter == 1) {

							// Nothing To Do -> Exit Property Set Method
							this.response.setHeader("service_message", "Ok");
							return true;
						} else {

							// Cut Origin Property from Property String
							StringTokenizer st1 = new StringTokenizer(
									properties, "#");
							StringBuffer sb = new StringBuffer();
							while (st1.hasMoreTokens()) {
								String tokenCaseSensitive = st1.nextToken();
								String token = tokenCaseSensitive.toLowerCase();
								if (!token.startsWith("origin="))
									if (sb.length() == 0)
										sb.append(tokenCaseSensitive);
									else {
										sb.append("#");
										sb.append(tokenCaseSensitive);
									}
							}
							properties = sb.toString();
						}
					} else {

						// Update Origin Property Into Database
						pst = connection.prepareStatement(UPD_COL_PRP);
						pst.setString(1, colPropValue);
						pst.setLong(2, colId);
						pst.setString(3, colPropName);
						pst.executeUpdate();
						pst.close();
					}
				}
			}
		} catch (SQLException sqlex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "PROPERTY SET: "
					+ sqlex.toString(), sqlex);
			return false;
		} finally {
			try {
				if (result != null)
					result.close();
				if (pst != null)
					pst.close();
			} catch (SQLException sqlex) {
				this.reportError(DasResponse.SC_SQL_ERROR, "PROPERTY SET: "
						+ sqlex.toString(), sqlex);
				errorOccurred = true;
			}
		}

		if (errorOccurred == false) {

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
										"PROPERTY SET: URI "
												+ uri
												+ " is not assigned to a WebDAV archive store with a correct ILM conformance class");
					}
				} catch (ArchStoreConfigException ascex) {
					this.reportError(DasResponse.SC_CONFIG_INCONSISTENT,
							"PROPERTY SET: " + ascex.getMessage(), ascex);
					return false;
				}

				// Test Archive Store
				testResult = ArchiveStoreTest.execute(sac);
				if (!testResult.startsWith("S")) {
					this.reportError(HttpServletResponse.SC_CONFLICT,
							"PROPERTY SET: " + testResult.substring(2));
					return false;
				}
			}

			if (this.att_col.equalsIgnoreCase("Y")) {

				// Check If Setting Attachment Properties Is Allowed
				if (range.equalsIgnoreCase("COL")) {
					this
							.reportError(
									HttpServletResponse.SC_FORBIDDEN,
									"PROPERTY SET: Setting attachment properties is not allowed for a collection URI");
					return false;
				}

				// Check If Naming For Attachment Collection Is Correct
				if (!(resName.startsWith("_") && (resName.endsWith(".xml")))) {
					this
							.reportError(
									HttpServletResponse.SC_FORBIDDEN,
									"PROPERTY SET: Name pattern for attachment collection does not meet specifications");
					return false;
				}

				// Build Uri For Attachment Collection
				StringBuffer sb = new StringBuffer();
				sb.append(this.uri.substring(0, this.uri.lastIndexOf("/") + 1));
				sb.append("_");
				sb.append(resName.substring(0, resName.length() - 4));
				attachment_uri = sb.toString();
			}

			// Set Properties On All Archive Stores
			XmlDasPropPatchRequest propPatchAttRequest, propPatchRequest;
			XmlDasPropPatch propPatchAtt, propPatch;
			XmlDasPropPatchResponse propPatchAttResponse, propPatchResponse;
			for (Iterator<Sapxmla_Config> iter = storeList.iterator(); iter
					.hasNext();) {

				// Get Archive Store Data
				sac = iter.next();

				// Set Properties For Attachment Collection
				if (this.att_col.equalsIgnoreCase("Y")) {
					propPatchAttRequest = new XmlDasPropPatchRequest(sac,
							attachment_uri, "Set", properties, range);
					propPatchAtt = new XmlDasPropPatch(propPatchAttRequest);
					propPatchAttResponse = propPatchAtt.execute();
					if (!((propPatchAttResponse.getStatusCode() == HttpServletResponse.SC_OK) || (propPatchAttResponse
							.getStatusCode() == HttpServletResponse.SC_NOT_FOUND))) {
						if (propPatchAttResponse.getStatusCode() == XmlDasMaster.SC_CANNOT_UPDATE_WEBDAV_PROPERTY) {
							multiStatusErrorOccurred = true;
							multiStatusErrorString = propPatchAttResponse
									.getEntityBody();
						}
						rollbackFailed = rollback(range);
						errorOccurred = true;
						break;
					} else {
						rollbackAttachProps.add(sac);
					}
				}

				// Set Properties For XMLDAS Collection Or Resource
				propPatchRequest = new XmlDasPropPatchRequest(sac, uri, "Set",
						properties, range);
				propPatch = new XmlDasPropPatch(propPatchRequest);
				propPatchResponse = propPatch.execute();

				// Evaluate Response
				if (propPatchResponse.getStatusCode() != HttpServletResponse.SC_OK) {
					if (propPatchResponse.getStatusCode() == XmlDasMaster.SC_CANNOT_UPDATE_WEBDAV_PROPERTY) {
						multiStatusErrorOccurred = true;
						multiStatusErrorString = propPatchResponse
								.getEntityBody();
					} else {
						if (propPatchResponse.getException() != null)
							multiStatusErrorString = propPatchResponse
									.getStatusCode()
									+ " "
									+ propPatchResponse.getReasonPhrase()
									+ " "
									+ propPatchResponse.getEntityBody()
									+ " "
									+ propPatchResponse.getException()
											.toString();
						else
							multiStatusErrorString = propPatchResponse
									.getStatusCode()
									+ " "
									+ propPatchResponse.getReasonPhrase()
									+ " " + propPatchResponse.getEntityBody();
					}
					rollbackFailed = rollback(range);
					errorOccurred = true;
					break;
				} else {
					rollbackXmldasProps.add(sac);
				}
			} // end for loop

			if (errorOccurred == false) {

				// Method Was Successful
				this.response.setHeader("service_message", "Ok");

				// Return Status
				return true;
			} else if (multiStatusErrorOccurred == true) {
				if (rollbackFailed == true)
					this
							.reportError(
									XmlDasMaster.SC_CANNOT_UPDATE_WEBDAV_PROPERTY,
									"PROPERTY SET: Error occurred while setting properties for URI "
											+ uri
											+ " on archive store "
											+ sac.archive_store
											+ ". The automatic roll back of the properties failed - see XMLDAS log for details: "
											+ multiStatusErrorString);
				else
					this.reportError(
							XmlDasMaster.SC_CANNOT_UPDATE_WEBDAV_PROPERTY,
							"PROPERTY SET: Error occurred while setting properties for URI "
									+ uri + " on archive store "
									+ sac.archive_store + ": "
									+ multiStatusErrorString);
				return false;
			} else {
				if (rollbackFailed == true)
					this
							.reportError(
									DasResponse.SC_IO_ERROR,
									"PROPERTY SET: Error occurred while setting properties for URI "
											+ uri
											+ " on archive store "
											+ sac.archive_store
											+ ". The automatic roll back of the properties failed - see XMLDAS log for details: "
											+ multiStatusErrorString);
				else
					this.reportError(DasResponse.SC_IO_ERROR,
							"PROPERTY SET: Error occurred while setting properties for URI "
									+ uri + " on archive store "
									+ sac.archive_store + ": "
									+ multiStatusErrorString);
				return false;
			}
		} else
			return false;
	}

	private boolean rollback(String range) {

		// Set Status
		boolean errorOccurred = false;

		// Roll Back Attachment Properties
		Sapxmla_Config sac;
		XmlDasPropPatchRequest propPatchRequest;
		XmlDasPropPatch propPatch;
		XmlDasPropPatchResponse propPatchResponse;
		if (this.att_col.equalsIgnoreCase("Y")) {

			for (Iterator<Sapxmla_Config> iter = this.rollbackAttachProps
					.iterator(); iter.hasNext();) {
				sac = iter.next();
				propPatchRequest = new XmlDasPropPatchRequest(sac,
						attachment_uri, "Remove", properties, range);
				propPatch = new XmlDasPropPatch(propPatchRequest);
				propPatchResponse = propPatch.execute();
				if (propPatchResponse.getStatusCode() != HttpServletResponse.SC_OK) {
					cat.errorT(loc,
							"PROPERTY SET: Error occurred while removing properties for URI "
									+ attachment_uri + " on archive store "
									+ sac.archive_store);
					errorOccurred = true;
				}
			}
		}

		// Roll Back Xmldas Properties
		for (Iterator<Sapxmla_Config> iter = this.rollbackXmldasProps
				.iterator(); iter.hasNext();) {
			sac = iter.next();
			propPatchRequest = new XmlDasPropPatchRequest(sac, uri, "Remove",
					properties, range);
			propPatch = new XmlDasPropPatch(propPatchRequest);
			propPatchResponse = propPatch.execute();
			if (propPatchResponse.getStatusCode() != HttpServletResponse.SC_OK) {
				cat.errorT(loc,
						"PROPERTY SET: Error occurred while removing properties for URI "
								+ uri + " on archive store "
								+ sac.archive_store);
				errorOccurred = true;
			}
		}

		// Return Status
		return errorOccurred;
	}
}
