package com.sap.archtech.daservice.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.exceptions.InvalidNameException;

public class HeadMethod extends MasterMethod {

	private final static String SEL_COL_TAB1 = "SELECT COLID, CREATIONTIME, CREATIONUSER, FROZEN, COLTYPE FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_COL_TAB2 = "SELECT COLID FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_RES_TAB = "SELECT * FROM BC_XMLA_RES WHERE COLID = ? AND DELSTATUS IN ('Y', 'N', 'P') AND RESNAME = ?";
	private final static String SEL_RES_CNT = "SELECT count(*) AS CT from BC_XMLA_RES WHERE COLID = ?";

	private Connection connection;
	private String uri;

	public HeadMethod(HttpServletResponse response, Connection connection,
			String uri) {
		this.response = response;
		this.connection = connection;
		this.uri = uri;
	}

	public boolean execute() throws IOException {

		// Variables
		int hits = 0;
		int packLength = 0;

		long offSet = 0;
		long colId = 0;
		long resLength = 0;

		String type = " ";
		String creationUTC = " ";
		String creationUser = " ";
		String frozen = " ";
		String colType = " ";
		String checkStatus = " ";
		String responseBody = "";
		String isPacked = " ";
		String packUTC = " ";
		String packName = " ";
		String packUser = " ";

		Timestamp creationTimeStamp = null;
		Timestamp packTimeStamp = null;
		PreparedStatement pst1 = null;
		PreparedStatement pst2 = null;
		PreparedStatement pst3 = null;
		PreparedStatement pst4 = null;
		ResultSet result = null;
		BufferedWriter bw = null;

		// Check Request Header "uri"
		if (this.uri == null) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"HEAD: URI missing from request header");
			return false;
		} else {
			try {
				this.isValidName(this.uri, true);
			} catch (InvalidNameException inex) {

				// $JL-EXC$
				this.reportError(DasResponse.SC_INVALID_CHARACTER, "HEAD: "
						+ inex.getMessage());
				return false;
			}
			this.uri = this.uri.toLowerCase();
			if ((this.uri.indexOf("//") != -1) || !this.uri.startsWith("/")
					|| this.uri.endsWith("/") || this.uri.length() < 2) {
				this
						.reportError(
								DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
								"HEAD: Value "
										+ this.uri
										+ " of request header URI does not meet specifications");
				return false;
			}
		}

		// Get Time Stamp Formatter
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		boolean status = false;
		boolean errorOccurred = false;
		try {

			// Check If URI Is A Collection Or A Resource
			pst1 = connection.prepareStatement(SEL_COL_TAB1);
			pst1.setString(1, this.uri.trim());
			result = pst1.executeQuery();
			hits = 0;
			while (result.next()) {
				colId = result.getLong("COLID");
				type = "COL";
				creationTimeStamp = result.getTimestamp("CREATIONTIME");
				creationUser = result.getString("CREATIONUSER");
				frozen = result.getString("FROZEN");
				colType = result.getString("COLTYPE");
				hits++;
			}
			result.close();
			pst1.close();

			// URI Is A Resource
			if (hits == 0) {
				String pathName = this.uri.substring(0, this.uri
						.lastIndexOf("/"));
				if (pathName == null || (pathName.length() < 1)) {
					// Error
					this.reportInfoTrace(DasResponse.SC_DOES_NOT_EXISTS,
							"HEAD: URI " + this.uri + " does not exist");
					return false;
				}
				pst2 = connection.prepareStatement(SEL_COL_TAB2);
				pst2.setString(1, pathName);
				result = pst2.executeQuery();
				hits = 0;
				while (result.next()) {
					colId = result.getLong("COLID");
					hits++;
				}
				result.close();
				pst2.close();

				if (hits == 0) {
					// Error
					this.reportInfoTrace(DasResponse.SC_DOES_NOT_EXISTS,
							"HEAD: URI " + this.uri + " does not exist");
					errorOccurred = true;
				}

				String resourceName = "";
				if (!errorOccurred) {
					resourceName = this.uri.substring(
							this.uri.lastIndexOf("/") + 1, this.uri.length());
					if (resourceName == null || (resourceName.length() < 1)) {
						// Error
						this.reportInfoTrace(DasResponse.SC_DOES_NOT_EXISTS,
								"HEAD: URI " + this.uri + " does not exist");
						errorOccurred = true;
					}
				}

				if (!errorOccurred) {
					pst3 = connection.prepareStatement(SEL_RES_TAB);
					pst3.setLong(1, colId);
					pst3.setString(2, resourceName);
					result = pst3.executeQuery();
					hits = 0;
					while (result.next()) {
						type = result.getString("RESTYPE");
						resLength = result.getLong("RESLENGTH");
						creationTimeStamp = result.getTimestamp("CREATIONTIME");
						creationUser = result.getString("CREATIONUSER");
						packName = result.getString("PACKNAME");
						offSet = result.getLong("OFFSET");
						packLength = result.getInt("PACKLENGTH");
						packTimeStamp = result.getTimestamp("PACKTIME");
						packUser = result.getString("PACKUSER");
						checkStatus = result.getString("CHECKSTATUS");
						isPacked = result.getString("ISPACKED");
						hits++;
					}
					result.close();
					pst3.close();
					if (hits == 0) {
						// Error
						this.reportInfoTrace(DasResponse.SC_DOES_NOT_EXISTS,
								"HEAD: URI " + this.uri + " does not exist");
						errorOccurred = true;
					}
				}
			}

			if (!errorOccurred) {
				// Format Creation UTC Time Stamp
				creationUTC = sdf.format(new java.util.Date(creationTimeStamp
						.getTime()
						+ (creationTimeStamp.getNanos() / 1000000)));

				// Set Response Header Fields
				response.setContentType(MasterMethod.contentType);
				this.response.setHeader("service_message", "Ok");

				// Create Response Body
				if (type.equalsIgnoreCase("COL")) {
					// Get Number Of Resources
					pst4 = connection.prepareStatement(SEL_RES_CNT);
					pst4.setLong(1, colId);
					result = pst4.executeQuery();
					result.next();
					int res_ctr = result.getInt("CT");
					result.close();
					pst4.close();

					// Create Response String
					responseBody = type + ";" + creationUTC + ";"
							+ creationUser + ";" + frozen + ";" + colType + ";"
							+ this.uri + ";" + res_ctr;
				} else if (isPacked.equalsIgnoreCase("Y")) {

					// Format Pack UTC Time Stamp
					packUTC = sdf.format(new java.util.Date(packTimeStamp
							.getTime()
							+ (packTimeStamp.getNanos() / 1000000)));

					// Create Response String
					responseBody = type + ";" + creationUTC + ";"
							+ creationUser + ";" + resLength + ";"
							+ checkStatus + ";" + isPacked + ";" + packName
							+ ";" + offSet + ";" + packLength + ";" + packUTC
							+ ";" + packUser + ";" + this.uri;
				} else

					// Create Response String
					responseBody = type + ";" + creationUTC + ";"
							+ creationUser + ";" + resLength + ";"
							+ checkStatus + ";N; ; ; ; ; ;" + this.uri;

				// Send Response Body
				bw = new BufferedWriter(new OutputStreamWriter(response
						.getOutputStream(), "UTF8"));
				bw.write(responseBody);

				// Method Was Successful
				status = true;
			}
		} catch (IOException ioex) {
			this.reportError(DasResponse.SC_IO_ERROR, "HEAD: "
					+ ioex.toString(), ioex);
		} catch (SQLException sqlex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "HEAD: "
					+ sqlex.toString(), sqlex);
		} finally {
			try {
				if (bw != null) {
					bw.flush();
					bw.close();
				}
			} catch (IOException ioex) {
				this.reportError(DasResponse.SC_IO_ERROR, "HEAD: "
						+ ioex.toString(), ioex);
				status = false;
			}
			try {
				if (pst1 != null)
					pst1.close();
				if (pst2 != null)
					pst2.close();
				if (pst3 != null)
					pst3.close();
				if (pst4 != null)
					pst4.close();
			} catch (SQLException sqlex) {
				this.reportError(DasResponse.SC_SQL_ERROR, "HEAD: "
						+ sqlex.toString(), sqlex);
				status = false;
			}
		}
		return status;
	}
}