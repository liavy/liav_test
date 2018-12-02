package com.sap.archtech.daservice.commands;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.exceptions.InvalidNameException;

public class InvalidUriSetMethod extends MasterMethod {

	private final static String SEL_COL_TAB = "SELECT COLID FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_COL_TAB2 = "SELECT COLID FROM BC_XMLA_COL WHERE PARENTCOLID = ?";
	private final static String SEL_RES_TAB = "SELECT RESID FROM BC_XMLA_RES WHERE COLID = ? AND RESNAME = ?";
	private final static String UPD_RES_TAB = "UPDATE BC_XMLA_RES SET INVALID = ? WHERE RESID = ?";
	private final static String UPD_RES_TAB2 = "UPDATE BC_XMLA_RES SET INVALID = ? WHERE COLID = ?";

	private Connection connection;
	private String uri;
	private String invalid;
	private String user;
	private String reason;

	PreparedStatement pst1 = null;
	PreparedStatement pst2 = null;

	public InvalidUriSetMethod(HttpServletResponse response,
			Connection connection, String uri, String invalid, String user,
			String reason) {
		this.response = response;
		this.connection = connection;
		this.uri = uri;
		this.invalid = invalid;
		this.user = user;
		this.reason = reason;
	}

	public boolean execute() throws IOException {

		// Variables
		int hits = 0;
		long colId = 0;
		long resId = 0;
		String range = "";
		String resName = "";
		PreparedStatement pst = null;
		ResultSet result = null;

		// Set Response Header Fields
		response.setContentType("text/xml");

		// Check Request Header "uri"
		if (this.uri == null) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"INVALIDURI SET: URI missing from request header");
			return false;
		} else {
			this.uri = this.uri.toLowerCase();
			try {
				this.isValidName(this.uri, true);
			} catch (InvalidNameException inex) {

				// $JL-EXC$
				this.reportError(DasResponse.SC_INVALID_CHARACTER,
						"INVALIDURI SET: " + inex.getMessage());
				return false;
			}
			if ((this.uri.indexOf("//") != -1) || !this.uri.startsWith("/")
					|| this.uri.endsWith("/") || this.uri.length() < 2) {

				// $JL-EXC$
				this.reportError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
						"INVALIDURI SET: Value " + this.uri
								+ " of request header "
								+ "URI does not meet specifications");
				return false;
			}
		}

		// Check Request Header "invalid"
		if (this.invalid == null) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"INVALIDURI SET: INVALID missing from request header");
			return false;
		} else if (!(this.invalid.equalsIgnoreCase("Y") || this.invalid
				.equalsIgnoreCase("N"))) {
			this
					.reportError(
							DasResponse.SC_KEYWORD_UNKNOWN,
							"INVALIDURI SET: Value "
									+ this.invalid
									+ " of request header INVALID does not meet specifications");
			return false;
		}

		// Check Request Header "user"
		if ((this.user == null) || (this.user.length() == 0)) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"INVALIDURI SET: USER missing from request header ");
			return false;
		}

		try {

			// Determine If URI Points To A Resource Or A Collection
			pst = connection.prepareStatement(SEL_COL_TAB);
			pst.setString(1, uri);
			result = pst.executeQuery();
			hits = 0;
			while (result.next()) {
				colId = result.getLong("COLID");
				hits++;
			}
			result.close();
			pst.close();
			if (hits == 1) {
				range = "COL";
			} else {
				if (uri.indexOf('/') == uri.lastIndexOf('/')) {
					this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
							"INVALIDURI SET: URI " + uri + " not found");
					return false;
				}

				pst = connection.prepareStatement(SEL_COL_TAB);
				pst.setString(1, uri.substring(0, uri.lastIndexOf("/")));
				result = pst.executeQuery();
				hits = 0;
				while (result.next()) {
					colId = result.getLong("COLID");
					hits++;
				}
				result.close();
				pst.close();
				if (hits == 1) {
					resName = uri.substring(uri.lastIndexOf("/") + 1);
					pst = connection.prepareStatement(SEL_RES_TAB);
					pst.setLong(1, colId);
					pst.setString(2, resName);
					result = pst.executeQuery();
					hits = 0;
					while (result.next()) {
						resId = result.getLong("RESID");
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
										"INVALIDURI SET: URI "
												+ uri
												+ " is not assigned to a collection or resource");
						return false;
					}

				} else {
					this
							.reportError(
									HttpServletResponse.SC_CONFLICT,
									"INVALIDURI SET: URI "
											+ uri
											+ " is not assigned to a collection or resource");

					return false;
				}
			}

			// URI Points To A Resource
			if (range.equalsIgnoreCase("RES")) {

				// Update Resource Table Entry
				pst = connection.prepareStatement(UPD_RES_TAB);
				pst.setString(1, invalid);
				pst.setLong(2, resId);
				pst.executeUpdate();
				pst.close();
			}

			// URI Points To A Collection
			else {

				// Loop Top Down All Collections
				pst1 = connection.prepareStatement(SEL_COL_TAB2);
				pst2 = connection.prepareStatement(UPD_RES_TAB2);
				this.traverse(colId);
				pst1.close();
				pst2.close();
			}

			// Success
			if (this.invalid.equalsIgnoreCase("Y")) {
				if ((this.reason != null) && (this.reason.length() != 0))
					MasterMethod.cat.infoT(loc, "INVALIDURI SET: " + this.user
							+ " invalidated " + this.uri + ". Reason: "
							+ this.reason);
				else
					MasterMethod.cat.infoT(loc, "INVALIDURI SET: " + this.user
							+ " invalidated " + this.uri);
			} else {
				if ((this.reason != null) && (this.reason.length() != 0))
					MasterMethod.cat.infoT(loc, "INVALIDURI SET: " + this.user
							+ " canceled the invalidity status of " + this.uri
							+ ". Reason: " + this.reason);
				else
					MasterMethod.cat.infoT(loc, "INVALIDURI SET: " + this.user
							+ " canceled the invalidity status of " + this.uri);
			}
			this.response.setHeader("service_message", "Ok");
			return true;
		} catch (SQLException sqlex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "INVALIDURI SET: "
					+ sqlex.toString(), sqlex);
			return false;
		} finally {
			try {
				if (result != null)
					result.close();
				if (pst != null)
					pst.close();
				if (pst1 != null)
					pst1.close();
				if (pst2 != null)
					pst2.close();
			} catch (SQLException sqlex) {

				// $JL-EXC$
				MasterMethod.cat.errorT(loc, "INVALIDURI SET: "
						+ getStackTrace(sqlex));
			}
		}
	}

	private void traverse(long colId) throws SQLException {
		ArrayList<Long> childrenNodesList = new ArrayList<Long>();

		// Get All Children Nodes
		ResultSet result = null;
		pst1.setLong(1, colId);
		result = pst1.executeQuery();
		while (result.next()) {
			childrenNodesList.add(new Long(result.getLong("COLID")));
		}
		result.close();

		// Update Resource Table Entries
		pst2.setString(1, invalid);
		pst2.setLong(2, colId);
		pst2.executeUpdate();

		// No More Leaf Nodes Exists
		if (childrenNodesList.size() == 0) {
			return;
		}

		// More Leaf Nodes Exists
		else {
			for (int i = 0; i < childrenNodesList.size(); i++)
				this.traverse(((Long) childrenNodesList.get(i)).longValue());
		}
	}
}
