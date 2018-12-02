package com.sap.archtech.daservice.commands;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;
import com.sap.archtech.daservice.exceptions.ArchStoreConfigException;
import com.sap.archtech.daservice.exceptions.InvalidNameException;
import com.sap.archtech.daservice.storage.XmlDasGetStream;
import com.sap.archtech.daservice.storage.XmlDasGetStreamRequest;
import com.sap.archtech.daservice.storage.XmlDasGetStreamResponse;
import com.sap.tc.logging.Severity;

public class GetStreamMethod extends MasterMethod {

	private final static String SEL_COL_TAB = "SELECT COLID, STOREID FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_RES_TAB = "SELECT RESLENGTH, ISPACKED FROM BC_XMLA_RES WHERE COLID = ? AND DELSTATUS IN ('Y', 'N', 'P') AND RESNAME = ?";

	private Connection connection;
	private ArchStoreConfigLocalHome beanLocalHome;
	private String uri;
	private String offset;
	private String length;

	public GetStreamMethod(HttpServletResponse response, Connection connection,
			ArchStoreConfigLocalHome beanLocalHome, String uri, String offset,
			String length) {
		this.response = response;
		this.connection = connection;
		this.beanLocalHome = beanLocalHome;
		this.uri = uri;
		this.offset = offset;
		this.length = length;
	}

	public boolean execute() throws IOException {

		// Variables
		long resLength = 0;
		long offsetLong = 0;
		int lengthInteger = 0;

		int hits = 0;

		// long offset = 0;
		long colId = 0;
		long storeId = 0;

		String archivePath = "";
		String resourceName = "";

		PreparedStatement pst1 = null;
		PreparedStatement pst2 = null;
		PreparedStatement pst3 = null;
		ResultSet result = null;
		Sapxmla_Config sac = null;

		// Set Response Header
		response.setHeader("service_message", "see message body");
		BufferedOutputStream bos = new BufferedOutputStream(response
				.getOutputStream());

		// Check Request Header "uri"
		if (this.uri == null) {
			this.reportStreamError(DasResponse.SC_PARAMETER_MISSING,
					"GETSTREAM: URI missing from request header", bos);
			return false;
		} else {
			try {
				this.isValidName(this.uri, true);
			} catch (InvalidNameException inex) {

				// $JL-EXC$
				this.reportStreamError(DasResponse.SC_INVALID_CHARACTER,
						"GETSTREAM: " + inex.getMessage(), bos);
				return false;
			}
			this.uri = this.uri.toLowerCase();
			if ((this.uri.indexOf("//") != -1) || !this.uri.startsWith("/")
					|| this.uri.endsWith("/") || this.uri.length() < 2) {
				this
						.reportStreamError(
								DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
								"GETSTREAM: Value "
										+ this.uri
										+ " of request header URI does not meet specifications",
								bos);
				return false;
			}
		}

		// Check Request Header "offset"
		if (this.offset == null) {
			this.reportStreamError(DasResponse.SC_PARAMETER_MISSING,
					"GETSTREAM: OFFSET missing from request header", bos);
			return false;
		} else {
			try {
				offsetLong = Long.parseLong(this.offset);
				if (offsetLong < 0) {
					this.reportStreamError(
							DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
							"GETSTREAM: Value " + offsetLong
									+ " of request header OFFSET is < 0", bos);
					return false;
				}
			} catch (NumberFormatException nfe) {

				// $JL-EXC$
				this
						.reportStreamError(
								DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
								"GETSTREAM: Value "
										+ this.offset
										+ " of request header OFFSET does not meet specifications",
								bos);
				return false;
			}
		}

		// Check Request Header "length"
		if (this.length == null) {
			this.reportStreamError(DasResponse.SC_PARAMETER_MISSING,
					"GETSTREAM: LENGTH missing from request header", bos);
			return false;
		} else {
			try {
				lengthInteger = Integer.parseInt(this.length);
				if (lengthInteger < 0) {
					this.reportStreamError(
							DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
							"GETSTREAM: Value " + lengthInteger
									+ " of request header LENGTH is < 0", bos);
					return false;
				}
			} catch (NumberFormatException nfe) {

				// $JL-EXC$
				this
						.reportStreamError(
								DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
								"GETSTREAM: Value "
										+ this.length
										+ " of request header LENGTH does not meet specifications",
								bos);
				return false;
			}
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
							"GETSTREAM: Collection "
									+ archivePath.substring(lastSlashNum + 1,
											strLen) + " does not exist", bos);
				else
					this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
							"GETSTREAM: Collection does not exist", bos);
				errorOccurred = true;
			}

			// Check If Entry In Table BC_XMLA_RES Exists
			if (!errorOccurred) {
				pst2 = connection.prepareStatement(SEL_RES_TAB);
				pst2.setLong(1, colId);
				pst2.setString(2, resourceName.trim());
				result = pst2.executeQuery();
				hits = 0;
				String isPacked = "";
				while (result.next()) {
					resLength = result.getLong("RESLENGTH");
					isPacked = result.getString("ISPACKED");
					hits++;
				}
				result.close();
				pst2.close();
				if (hits == 0) {
					this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
							"GETSTREAM: Resource " + resourceName
									+ " does not exist", bos);
					errorOccurred = true;
				}
				if (!errorOccurred) {
					if (isPacked.toUpperCase().startsWith("Y")) {
						this.reportStreamError(
								HttpServletResponse.SC_FORBIDDEN,
								"GETSTREAM: Resource " + resourceName
										+ " is packed", bos);
						errorOccurred = true;
					}
				}
			}

			if (!errorOccurred) {

				// Set Response Header
				response.setContentType(MasterMethod.contentType);

				// Calculate And Add Resource Length Response Header
				long rest = resLength - offsetLong;
				if (rest <= 0) {
					resLength = resLength - 1;
					this.reportStreamError(DasResponse.SC_WRONG_OFFSET,
							"GETSTREAM: Offset " + offsetLong
									+ " exceeds maximum offset " + resLength,
							bos);
					errorOccurred = true;
				} else {
					long delta = rest - lengthInteger;
					if (delta >= 0)
						response.setHeader("reslength", Integer
								.toString(lengthInteger));
					else
						response.setHeader("reslength", Long.toString(rest));

					// Get Archive Store Configuration Data
					sac = this.getArchStoreConfigObject(beanLocalHome, storeId);

					// Add Archive Store Response Header
					if (sac != null)
						response.setHeader("archive_store", sac.archive_store
								.trim());
				}
			}

			// Get Resource From Storage System
			XmlDasGetStreamResponse getStreamResponse = null;
			if (!errorOccurred) {
				XmlDasGetStreamRequest getStreamRequest = new XmlDasGetStreamRequest(
						sac, bos, uri, offsetLong, lengthInteger);
				XmlDasGetStream getStream = new XmlDasGetStream(
						getStreamRequest);
				getStreamResponse = getStream.execute();
				if (!((getStreamResponse.getStatusCode() == HttpServletResponse.SC_OK) || (getStreamResponse
						.getStatusCode() == HttpServletResponse.SC_PARTIAL_CONTENT))) {
					this.reportStreamError(getStreamResponse.getStatusCode(),
							"GETSTREAM: Error while accessing resource; response from archive store: "
									+ getStreamResponse.getStatusCode() + " "
									+ getStreamResponse.getReasonPhrase() + " "
									+ getStreamResponse.getEntityBody(), bos);
					errorOccurred = true;
				}
			}

			// Method Was Successful
			if (!errorOccurred) {
				this.writeStatus(bos, HttpServletResponse.SC_OK, "Ok");
				bos.flush();
				status = true;
			}
		} catch (ArchStoreConfigException ascex) {
			this.reportStreamError(DasResponse.SC_CONFIG_INCONSISTENT,
					"GETSTREAM: " + ascex.getMessage(), ascex, bos);
		} catch (IOException ioex) {
			this.reportStreamError(DasResponse.SC_IO_ERROR, "GETSTREAM: "
					+ ioex.toString(), ioex, bos);
		} catch (SQLException sqlex) {
			this.reportStreamError(DasResponse.SC_SQL_ERROR, "GETSTREAM: "
					+ sqlex.toString(), sqlex, bos);
		} finally {
			try {
				if (pst1 != null)
					pst1.close();
				if (pst2 != null)
					pst2.close();
				if (pst3 != null)
					pst3.close();
			} catch (SQLException sqlex) {
				cat.logThrowableT(Severity.WARNING, loc, "GETSTREAM: "
						+ sqlex.getMessage(), sqlex);
			}
			if (bos != null) {
				bos.close();
			}
		}
		return status;
	}
}