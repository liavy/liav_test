package com.sap.archtech.daservice.commands;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
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
import com.sap.tc.logging.Severity;

public class GetMethod extends MasterMethod {

	private final static String SEL_COL_TAB = "SELECT COLID, STOREID FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_RES_TAB = "SELECT * FROM BC_XMLA_RES WHERE COLID = ? AND DELSTATUS IN ('Y', 'N', 'P') AND RESNAME = ?";
	private final static String SEL_RES_SCH = "SELECT * FROM BC_XMLA_RES WHERE COLID = ? AND RESTYPE = 'XSD' ORDER BY RESID";

	private Connection connection;
	private ArchStoreConfigLocalHome beanLocalHome;
	private String uri;
	private String checksum;
	private String mode;
	private String check_level;
	private String filename_win;
	private String filename_unx;

	public GetMethod(HttpServletResponse response, Connection connection,
			ArchStoreConfigLocalHome beanLocalHome, String uri,
			String checksum, String mode, String check_level,
			String filename_win, String filename_unx) {
		this.response = response;
		this.connection = connection;
		this.beanLocalHome = beanLocalHome;
		this.uri = uri;
		this.checksum = checksum;
		this.mode = mode;
		this.check_level = check_level;
		this.filename_win = filename_win;
		this.filename_unx = filename_unx;
	}

	public boolean execute() throws IOException {

		// Variables
		int hits = 0;
		long resLength = 0;
		int packLength = 0;

		long offset = 0;
		long colId = 0;
		long storeId = 0;

		String archivePath = "";
		String resourceName = "";

		BufferedOutputStream sos = null;
		BufferedOutputStream bos = null;
		PreparedStatement pst1 = null;
		PreparedStatement pst2 = null;
		PreparedStatement pst3 = null;
		ResultSet result = null;
		Sapxmla_Config sac = null;
		String fpdb = null;
		String isPacked = null;
		String packName = null;

		// Set Response Header
		response.setHeader("service_message", "see message body");
		sos = new BufferedOutputStream(response.getOutputStream());

		// Check Request Header "uri"
		if (this.uri == null) {
			this.reportStreamError(DasResponse.SC_PARAMETER_MISSING,
					"GET: URI missing from request header", sos);
			return false;
		} else {
			try {
				this.isValidName(this.uri, true);
			} catch (InvalidNameException inex) {

				// $JL-EXC$
				this.reportStreamError(DasResponse.SC_INVALID_CHARACTER,
						"GET: " + inex.getMessage(), sos);
				return false;
			}
			this.uri = this.uri.toLowerCase();
			if ((this.uri.indexOf("//") != -1) || !this.uri.startsWith("/")
					|| this.uri.endsWith("/") || this.uri.length() < 2) {
				this
						.reportStreamError(
								DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
								"GET: Value "
										+ this.uri
										+ " of request header URI does not meet specifications",
								sos);
				return false;
			}
		}

		// Check Request Header "checksum"
		if (this.checksum == null) {
			this.checksum = "N";
		} else if (!(this.checksum.equalsIgnoreCase("Y") || this.checksum
				.equalsIgnoreCase("N"))) {
			this
					.reportStreamError(
							DasResponse.SC_KEYWORD_UNKNOWN,
							"GET: Value "
									+ this.checksum
									+ " of request header CHECKSUM does not meet specifications",
							sos);
			return false;
		}

		// Check Request Header "mode"
		if (this.mode == null)
			this.mode = "DELIVER";
		else if (!((this.mode.equalsIgnoreCase("DELIVER")) || (this.mode
				.equalsIgnoreCase("NODELIVER")))) {
			this
					.reportStreamError(
							DasResponse.SC_KEYWORD_UNKNOWN,
							"GET: Value "
									+ this.mode
									+ " of request header MODE does not meet specifications",
							sos);
			return false;
		}

		// Check Request Header "check_level"
		if (this.check_level == null)
			this.check_level = "NO";
		else if (!((this.check_level.equalsIgnoreCase("NO"))
				|| (this.check_level.equalsIgnoreCase("PARSE")) || (this.check_level
				.equalsIgnoreCase("VALIDATE")))) {
			this
					.reportStreamError(
							DasResponse.SC_KEYWORD_UNKNOWN,
							"GET: Value "
									+ this.check_level
									+ " of request header CHECK_LEVEL does not meet specifications",
							sos);
			return false;
		}

		// Check Request Headers "filename_win" And "filename_unx"
		if ((this.filename_win != null) || (this.filename_unx != null)) {
			String filename = "";

			// Unix Operation System
			if (System.getProperty("file.separator").startsWith("/")) {
				if (this.filename_unx == null) {
					this.reportStreamError(DasResponse.SC_IO_ERROR,
							"GET: Unix file name is missing for Windows file name "
									+ this.filename_win, sos);
					return false;
				} else {
					filename = this.filename_unx;
				}
			}

			// Windows Operating System
			else {
				if (this.filename_win == null) {
					this.reportStreamError(DasResponse.SC_IO_ERROR,
							"GET: Windows file name is missing for Unix file name "
									+ this.filename_unx, sos);
					return false;
				} else {

					filename = this.filename_win;
				}
			}

			// Get Target File Stream
			if (this.mode.equalsIgnoreCase("DELIVER"))
				bos = new BufferedOutputStream(new FileOutputStream(filename));
			else
				bos = new BufferedOutputStream(new ByteArrayOutputStream());
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
					this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
							"GET: Collection "
									+ archivePath.substring(lastSlashNum + 1,
											strLen) + " does not exist", sos);
				else
					this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
							"GET: Collection does not exist", sos);
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
					resLength = result.getLong("RESLENGTH");
					fpdb = result.getString("FINGERPRINT");
					isPacked = result.getString("ISPACKED");
					packName = result.getString("PACKNAME");
					offset = result.getLong("OFFSET");
					packLength = result.getInt("PACKLENGTH");
					hits++;
				}
				result.close();
				pst2.close();
				if (hits == 0) {
					this
							.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
									"GET: Resource " + resourceName
											+ " does not exist", sos);
					errorOccurred = true;
				}
			}

			String uriParam = null;
			long offsetParam = 0;
			int lengthParam = 0;
			String checksumParam = null;
			Schema schema = null;
			if (!errorOccurred) {

				// Set Response Header
				response.setContentType(MasterMethod.contentType);

				// Add Resource Length Response Header
				response.setHeader("reslength", Long.toString(resLength));

				// Get Archive Store Configuration Data
				sac = this.getArchStoreConfigObject(beanLocalHome, storeId);

				// Add Archive Store Response Header
				if (sac != null)
					response.setHeader("archive_store", sac.archive_store
							.trim());

				// Prepare Case Distinction
				if (isPacked.equalsIgnoreCase("Y"))
					uriParam = archivePath.trim() + "/" + packName.trim();
				else
					uriParam = uri.trim();

				if (packLength > 0) {
					offsetParam = offset;
					lengthParam = packLength;
				}

				if (checksum.equalsIgnoreCase("Y"))
					checksumParam = fpdb;

				if (check_level.equalsIgnoreCase("VALIDATE")) {
					String error = null;

					// Check If XML Schema Object Is Already In Schema Pool
					schema = XmlSchemaProvider.getSchemaObject(colId);

					// Get XML Schema From Archive Store And Add To Schema Pool
					if (schema == null) {
						ArrayList<ResourceData> schemaList = new ArrayList<ResourceData>();

						// Get All XML Schemas Entries From Table BC_XMLA_RES
						pst3 = connection.prepareStatement(SEL_RES_SCH);
						pst3.setLong(1, colId);
						result = pst3.executeQuery();
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
						pst3.close();
						if (hits == 0) {
							error = "GET: VALIDATE Error: Collection contains no XML Schema for validation";
						} else {

							// Get All XML Schemas From Archive Store
							Source[] schemaFiles = new StreamSource[schemaList
									.size()];
							Iterator<ResourceData> schemaIter = schemaList
									.iterator();
							int schemaNumber = 0;
							while (schemaIter.hasNext()) {
								ByteArrayOutputStream schemaContainer = new ByteArrayOutputStream();
								ResourceData resourceData = (ResourceData) schemaIter
										.next();
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
									error = "GET: VALIDATE Error while getting XML Schema "
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
											+ getResponse.getReasonPhrase();
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
								// Load Schema Array
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
									error = "GET: Error while creating schema object: "
											+ sex.toString();
								}
							}
						}
					}
					if (error != null) {
						this.reportStreamError(DasResponse.SC_CHECK_FAILED,
								error, sos);
						errorOccurred = true;
					}
				}
			}

			// Get Resource From Storage System
			XmlDasGetResponse getResponse = null;
			if (!errorOccurred) {
				XmlDasGetRequest getRequest = null;
				if ((this.filename_win != null) || (this.filename_unx != null))

					// File Output Stream
					getRequest = new XmlDasGetRequest(sac, bos, uriParam,
							offsetParam, lengthParam, checksumParam, mode,
							check_level, schema);
				else

					// Servlet Output Stream
					getRequest = new XmlDasGetRequest(sac, sos, uriParam,
							offsetParam, lengthParam, checksumParam, mode,
							check_level, schema);
				XmlDasGet get = new XmlDasGet(getRequest);
				getResponse = get.execute();
				if (!((getResponse.getStatusCode() == HttpServletResponse.SC_OK) || (getResponse
						.getStatusCode() == HttpServletResponse.SC_PARTIAL_CONTENT))) {
					this.reportStreamError(getResponse.getStatusCode(),
							"GET: Error while accessing resource; response from archive store: "
									+ getResponse.getStatusCode() + " "
									+ getResponse.getReasonPhrase() + " "
									+ getResponse.getEntityBody(), sos);
					errorOccurred = true;
				}
			}

			if (!errorOccurred) {
				if (getResponse.isChecksumIdentical() == false
						&& checksum.equalsIgnoreCase("Y")) {
					this.reportStreamError(DasResponse.SC_CHECKSUM_INCORRECT,
							"GET: Check sum " + checksumParam
									+ " not matched by resource " + uriParam,
							sos);
					errorOccurred = true;
				}
			}

			// Method Was Successful
			if (!errorOccurred) {
				this.writeStatus(sos, HttpServletResponse.SC_OK, "Ok");
				sos.flush();
				status = true;
			}
		} catch (ArchStoreConfigException ascex) {
			this.reportStreamError(DasResponse.SC_CONFIG_INCONSISTENT, "GET: "
					+ ascex.getMessage(), ascex, sos);
		} catch (IOException ioex) {
			this.reportStreamError(DasResponse.SC_IO_ERROR, "GET: "
					+ ioex.toString(), ioex, sos);
		} catch (SQLException sqlex) {
			this.reportStreamError(DasResponse.SC_SQL_ERROR, "GET: "
					+ sqlex.toString(), sqlex, sos);
		} finally {
			try {
				if (pst1 != null)
					pst1.close();
				if (pst2 != null)
					pst2.close();
				if (pst3 != null)
					pst3.close();
			} catch (SQLException sqlex) {
				cat.logThrowableT(Severity.WARNING, loc, "GET: "
						+ sqlex.getMessage(), sqlex);
			}
			if (sos != null) {
				sos.close();
			}
			if (bos != null) {
				bos.flush();
				bos.close();
			}
		}
		return status;
	}
}