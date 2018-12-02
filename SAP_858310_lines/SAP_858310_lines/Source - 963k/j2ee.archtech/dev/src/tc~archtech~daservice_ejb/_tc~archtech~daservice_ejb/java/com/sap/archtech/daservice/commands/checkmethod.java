package com.sap.archtech.daservice.commands;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.data.ResourceData;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;
import com.sap.archtech.daservice.exceptions.ArchStoreConfigException;
import com.sap.archtech.daservice.exceptions.InvalidNameException;
import com.sap.archtech.daservice.storage.ParserErrorHandler;
import com.sap.archtech.daservice.storage.XmlDasGet;
import com.sap.archtech.daservice.storage.XmlDasGetRequest;
import com.sap.archtech.daservice.storage.XmlDasGetResponse;
import com.sap.archtech.daservice.util.XmlSchemaProvider;

public class CheckMethod extends MasterMethod {

	private final static String SEL_COL_TAB = "SELECT COLID, STOREID FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_RES_TAB = "SELECT * FROM BC_XMLA_RES WHERE COLID = ? AND DELSTATUS IN ('Y', 'N', 'P') AND RESNAME = ?";
	private final static String UPD_RES_TAB = "UPDATE BC_XMLA_RES SET CHECKSTATUS = ? WHERE RESID = ?";
	private final static String SEL_RES_SCH = "SELECT * FROM BC_XMLA_RES WHERE COLID = ? AND RESTYPE = 'XSD' ORDER BY RESID";

	private Connection connection;
	private ArchStoreConfigLocalHome beanLocalHome;
	private String uri;
	private String check_level;

	public CheckMethod(HttpServletResponse response, Connection connection,
			ArchStoreConfigLocalHome beanLocalHome, String uri,
			String check_level) {
		this.response = response;
		this.connection = connection;
		this.beanLocalHome = beanLocalHome;
		this.uri = uri;
		this.check_level = check_level;
	}

	public boolean execute() throws IOException {

		// Variables
		int hits = 0;
		int packLength = 0;
		long offset = 0;
		long resId = 0;
		long colId = 0;
		long storeId = 0;

		String archivePath = "";
		String checkStatus = "";
		String error = null;
		String isPacked = "";
		String packName = "";
		String resourceName = "";
		String validateStatus = "";

		PreparedStatement pst1 = null;
		PreparedStatement pst2 = null;
		PreparedStatement pst3 = null;
		PreparedStatement pst4 = null;
		PreparedStatement pst5 = null;
		ResultSet result = null;
		Sapxmla_Config sac = null;

		// Check Request Header "uri"
		if (this.uri == null) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"CHECK: URI missing from request header");
			return false;
		} else {
			try {
				this.isValidName(this.uri, true);
			} catch (InvalidNameException inex) {

				// $JL-EXC$
				this.reportError(DasResponse.SC_INVALID_CHARACTER, "CHECK: "
						+ inex.getMessage());
				return false;
			}
			this.uri = this.uri.toLowerCase();
			if ((this.uri.indexOf("//") != -1) || !this.uri.startsWith("/")
					|| this.uri.endsWith("/") || this.uri.length() < 2) {
				this
						.reportError(
								DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
								"CHECK: Value "
										+ this.uri
										+ " of request header URI does not meet specifications");
				return false;
			}
		}

		// Check Request Header "check_level"
		if (this.check_level == null) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"CHECK: CHECK_LEVEL missing from request header");
			return false;
		} else
			this.check_level = this.check_level.toUpperCase();
		if (!((this.check_level.equalsIgnoreCase("PARSE")) || (this.check_level
				.equalsIgnoreCase("VALIDATE")))) {
			this
					.reportError(
							DasResponse.SC_KEYWORD_UNKNOWN,
							"CHECK: Value "
									+ this.check_level
									+ " of request header CHECK_LEVEL does not meet specifications");
			return false;
		}

		// Split URI Into Archive Path And Resource Name
		archivePath = this.uri.substring(0, this.uri.lastIndexOf("/")).trim();
		resourceName = this.uri.substring(this.uri.lastIndexOf("/") + 1,
				this.uri.length()).trim();
		boolean status = false;
		boolean errorOccurred = false;
		try {

			// Get Collection Id And Archive Store
			pst1 = connection.prepareStatement(SEL_COL_TAB);
			pst1.setString(1, archivePath.trim());
			result = pst1.executeQuery();
			hits = 0;
			while (result.next()) {
				colId = result.getLong("COLID");
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
							"CHECK: Collection "
									+ archivePath.substring(lastSlashNum + 1,
											strLen) + " does not exist");

				else
					this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
							"CHECK: Collection does not exist");
				errorOccurred = true;
			}

			// Check If Entry In Table BC_XMLA_RES Exists
			if (!errorOccurred) {
				pst2 = connection.prepareStatement(SEL_RES_TAB);
				pst2.setLong(1, colId);
				pst2.setString(2, resourceName.trim());
				result = pst2.executeQuery();
				hits = 0;
				while (result.next()) {
					resId = result.getLong("RESID");
					packName = result.getString("PACKNAME");
					offset = result.getLong("OFFSET");
					packLength = result.getInt("PACKLENGTH");
					checkStatus = result.getString("CHECKSTATUS").toUpperCase();
					isPacked = result.getString("ISPACKED");
					hits++;
				}
				result.close();
				pst2.close();
				if (hits == 0) {
					this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
							"CHECK: Resource " + resourceName
									+ " does not exist");
					errorOccurred = true;
				}
			}

			if (!errorOccurred) {
				if (!(checkStatus.equalsIgnoreCase("W") || checkStatus
						.equalsIgnoreCase("V")))
					checkStatus = "X";

				// Get Archive Store Configuration Data
				sac = this.getArchStoreConfigObject(beanLocalHome, storeId);

				// SAX2-Parser: Parse Document According To Well-Formed
				if (this.check_level.equalsIgnoreCase("PARSE")) {
					XmlDasGetRequest getRequest = null;
					if (isPacked.equalsIgnoreCase("Y"))
						getRequest = new XmlDasGetRequest(sac, null,
								archivePath.trim() + "/" + packName.trim(),
								offset, packLength, null, "NODELIVER", "PARSE",
								null);
					else
						getRequest = new XmlDasGetRequest(sac, null,
								archivePath.trim() + "/" + resourceName.trim(),
								0, 0, null, "NODELIVER", "PARSE", null);
					XmlDasGet get = new XmlDasGet(getRequest);
					XmlDasGetResponse getResponse = get.execute();
					if (!((getResponse.getStatusCode() == HttpServletResponse.SC_OK) || (getResponse
							.getStatusCode() == HttpServletResponse.SC_PARTIAL_CONTENT))) {
						if (getResponse.getException() == null) {
							error = "CHECK: PARSE Error: "
									+ +getResponse.getStatusCode() + " "
									+ getResponse.getReasonPhrase() + " "
									+ getResponse.getEntityBody();
							MasterMethod.cat.errorT(loc, error);
						} else {
							error = "CHECK: PARSE Error: "
									+ +getResponse.getStatusCode() + " "
									+ getResponse.getReasonPhrase() + " "
									+ getResponse.getEntityBody() + " "
									+ getResponse.getException().toString();
							MasterMethod.cat
									.errorT(loc, "CHECK: PARSE Error: "
											+ +getResponse.getStatusCode()
											+ " "
											+ getResponse.getReasonPhrase()
											+ " "
											+ getResponse.getEntityBody()
											+ " "
											+ getStackTrace(getResponse
													.getException()));
						}
					}

					// Set Check Status
					if (error == null)
						validateStatus = "W";
					else
						validateStatus = "N";

				}
				// SAX2-Parser: Validate Document According To XML Schema(s)
				else {

					// Check If XML Schema Object Is Already In Schema Pool
					Schema schema = XmlSchemaProvider.getSchemaObject(colId);

					// Get XML Schema From Archive Store And Add To Schema Pool
					if (schema == null) {
						ArrayList<ResourceData> schemaList = new ArrayList<ResourceData>();

						// Get All XML Schemas Entries From Table BC_XMLA_RES
						pst4 = connection.prepareStatement(SEL_RES_SCH);
						pst4.setLong(1, colId);
						result = pst4.executeQuery();
						hits = 0;
						while (result.next()) {
							if (result.getString("ISPACKED").equalsIgnoreCase(
									"Y"))
								schemaList.add(new ResourceData(result
										.getLong("RESID"), archivePath.trim()
										+ "/" + result.getString("PACKNAME"),
										null, result.getInt("PACKLENGTH"),
										result.getLong("OFFSET"), result
												.getString("PACKNAME"), "Y"));
							else
								schemaList.add(new ResourceData(result
										.getLong("RESID"), archivePath.trim()
										+ "/" + result.getString("RESNAME"),
										null, 0, 0, null, "N"));
							hits++;
						}
						result.close();
						pst4.close();
						if (hits == 0) {
							error = "CHECK: VALIDATE Error: Collection contains no XML Schema for validation";
							MasterMethod.cat.errorT(loc, error);
						} else {

							// Get All XML Schemas From Archive Store
							Source[] schemaFiles = new StreamSource[schemaList
									.size()];
							Iterator<ResourceData> schemaIter = schemaList
									.iterator();
							int schemaNumber = 0;
							while (schemaIter.hasNext()) {
								ByteArrayOutputStream schemaContainer = new ByteArrayOutputStream();
								ResourceData resourceData = schemaIter.next();
								XmlDasGetRequest getRequest = null;
								if (resourceData.getIsPacked()
										.equalsIgnoreCase("Y"))
									getRequest = new XmlDasGetRequest(sac,
											schemaContainer, resourceData
													.getResName(), resourceData
													.getOffset(), resourceData
													.getPackLength(), null,
											"DELIVER", "NO", null);
								else
									getRequest = new XmlDasGetRequest(sac,
											schemaContainer, resourceData
													.getResName(), 0, 0, null,
											"DELIVER", "NO", null);
								XmlDasGet get = new XmlDasGet(getRequest);
								XmlDasGetResponse getResponse = get.execute();
								if (!((getResponse.getStatusCode() == HttpServletResponse.SC_OK) || (getResponse
										.getStatusCode() == HttpServletResponse.SC_PARTIAL_CONTENT))) {
									if (getResponse.getException() == null) {
										error = "CHECK: VALIDATE Error while getting XML Schema "
												+ resourceData
														.getResName()
														.substring(
																resourceData
																		.getResName()
																		.lastIndexOf(
																				"/") + 1,
																resourceData
																		.getResName()
																		.length())
												+ " from archive store; Response: "
												+ getResponse.getStatusCode()
												+ " "
												+ getResponse.getReasonPhrase()
												+ " "
												+ getResponse.getEntityBody();
										MasterMethod.cat.errorT(loc, error);
									} else {
										error = "CHECK: VALIDATE Error while getting XML Schema "
												+ resourceData
														.getResName()
														.substring(
																resourceData
																		.getResName()
																		.lastIndexOf(
																				"/") + 1,
																resourceData
																		.getResName()
																		.length())
												+ " from archive store; Response: "
												+ getResponse.getStatusCode()
												+ " "
												+ getResponse.getReasonPhrase()
												+ " "
												+ getResponse.getEntityBody()
												+ " "
												+ getResponse.getException()
														.toString();
										MasterMethod.cat
												.errorT(
														loc,
														"CHECK: VALIDATE Error while getting XML Schema "
																+ resourceData
																		.getResName()
																		.substring(
																				resourceData
																						.getResName()
																						.lastIndexOf(
																								"/") + 1,
																				resourceData
																						.getResName()
																						.length())
																+ " from archive store; Response: "
																+ getResponse
																		.getStatusCode()
																+ " "
																+ getResponse
																		.getReasonPhrase()
																+ " "
																+ getResponse
																		.getEntityBody()
																+ " "
																+ getStackTrace(getResponse
																		.getException()));
									}
									break;
								}
								if (error == null) {
									schemaFiles[schemaNumber] = new StreamSource(
											new ByteArrayInputStream(
													schemaContainer
															.toByteArray()));
									schemaNumber++;
								}
							}

							if (error == null) {
								try {

									// Create Schema Object
									SchemaFactory schemaFactory = SchemaFactory
											.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
									schemaFactory
											.setErrorHandler(new ParserErrorHandler());
									schema = schemaFactory
											.newSchema(schemaFiles);

									// Add Schema Object To Schema Pool
									if (!XmlSchemaProvider.addSchemaObject(
											colId, schema))
										schema = XmlSchemaProvider
												.getSchemaObject(colId);
								} catch (SAXException sex) {

									// $JL-EXC$
									error = "CHECK: Error while creating schema object: "
											+ sex.toString();
									MasterMethod.cat.errorT(loc, error);
								}
							}
						}
					}

					// Get Resource From Storage System
					XmlDasGetRequest getRequest = null;
					if (isPacked.equalsIgnoreCase("Y"))
						getRequest = new XmlDasGetRequest(sac, null,
								archivePath.trim() + "/" + packName.trim(),
								offset, packLength, null, "NODELIVER",
								"VALIDATE", schema);
					else
						getRequest = new XmlDasGetRequest(sac, null,
								archivePath.trim() + "/" + resourceName.trim(),
								0, 0, null, "NODELIVER", "VALIDATE", schema);
					XmlDasGet get = new XmlDasGet(getRequest);
					XmlDasGetResponse getResponse = get.execute();
					if (!((getResponse.getStatusCode() == HttpServletResponse.SC_OK) || (getResponse
							.getStatusCode() == HttpServletResponse.SC_PARTIAL_CONTENT))) {
						if (getResponse.getException() == null) {
							error = "CHECK: VALIDATE Error: "
									+ +getResponse.getStatusCode() + " "
									+ getResponse.getReasonPhrase() + " "
									+ getResponse.getEntityBody();
							MasterMethod.cat.errorT(loc, error);
						} else {
							error = "CHECK: VALIDATE Error: "
									+ +getResponse.getStatusCode() + " "
									+ getResponse.getReasonPhrase() + " "
									+ getResponse.getEntityBody() + " "
									+ getResponse.getException().toString();
							MasterMethod.cat.errorT(loc,
									"CHECK: VALIDATE Error: "
											+ +getResponse.getStatusCode()
											+ " "
											+ getResponse.getReasonPhrase()
											+ " "
											+ getResponse.getEntityBody()
											+ " "
											+ getStackTrace(getResponse
													.getException()));
						}
					}

					// Set Check Status
					if (error == null)
						validateStatus = "V";
					else
						validateStatus = "I";
				}
			}

			if (!errorOccurred) {
				// Adjust Check Status In Table BC_XMLA_RES
				if (validateStatus.equalsIgnoreCase("I")
						|| validateStatus.equalsIgnoreCase("N")
						|| checkStatus.equalsIgnoreCase("X")
						|| (checkStatus.equalsIgnoreCase("W") && validateStatus
								.equalsIgnoreCase("V"))) {
					pst5 = connection.prepareStatement(UPD_RES_TAB);
					pst5.setString(1, validateStatus.trim());
					pst5.setLong(2, resId);
					hits = pst5.executeUpdate();
					pst5.close();
					if (hits != 1) {
						this
								.reportError(DasResponse.SC_SQL_ERROR,
										"CHECK: Update of attribute CHECKSTATUS in table BC_XMLA_RES failed");
						errorOccurred = true;
					}
				}
			}

			// Set Response Header Fields
			if (!errorOccurred) {
				response.setContentType("text/xml");
				response.setHeader("status", validateStatus);
				if (error == null)
					response.setHeader("message", "");
				else
					response.setHeader("message", error);

				// Method Was Successful
				this.response.setHeader("service_message", "Ok");
				status = true;
			}
		} catch (ArchStoreConfigException ascex) {
			this.reportError(DasResponse.SC_CONFIG_INCONSISTENT, "CHECK: "
					+ ascex.getMessage(), ascex);
		} catch (IOException ioex) {
			this.reportError(DasResponse.SC_IO_ERROR, "CHECK: "
					+ ioex.toString(), ioex);
		} catch (SQLException sqlex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "CHECK: "
					+ sqlex.toString(), sqlex);
		} finally {
			try {
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
			} catch (SQLException sqlex) {
				this.reportError(DasResponse.SC_SQL_ERROR, "CHECK: "
						+ sqlex.toString(), sqlex);
				status = false;
			}
		}
		return status;
	}
}