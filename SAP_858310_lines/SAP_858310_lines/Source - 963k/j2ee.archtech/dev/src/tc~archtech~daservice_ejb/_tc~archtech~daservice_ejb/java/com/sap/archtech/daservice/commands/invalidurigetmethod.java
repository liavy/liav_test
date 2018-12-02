package com.sap.archtech.daservice.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.exceptions.InvalidNameException;

public class InvalidUriGetMethod extends MasterMethod {

	private final static String SEL_COL_TAB = "SELECT COLID FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_COL_TAB2 = "SELECT COLID FROM BC_XMLA_COL WHERE PARENTCOLID = ?";
	private final static String SEL_COL_TAB3 = "SELECT URI FROM BC_XMLA_COL WHERE COLID = ?";
	private final static String SEL_RES_TAB = "SELECT INVALID FROM BC_XMLA_RES WHERE COLID = ? AND RESNAME = ?";
	private final static String SEL_RES_TAB2 = "SELECT RESNAME FROM BC_XMLA_RES WHERE COLID = ? AND INVALID = 'Y'";

	private Connection connection;
	private String uri;

	PreparedStatement pst1 = null;
	PreparedStatement pst2 = null;
	PreparedStatement pst3 = null;
	BufferedWriter bw;

	public InvalidUriGetMethod(HttpServletResponse response,
			Connection connection, String uri) {
		this.response = response;
		this.connection = connection;
		this.uri = uri;
	}

	public boolean execute() throws IOException {

		// Variables
		int hits = 0;
		long colId = 0;
		String range = "";
		String resName = "";
		String invalid = "";
		PreparedStatement pst = null;
		ResultSet result = null;

		// Set Response Header Fields
		response.setContentType("text/xml");
		response.setHeader("service_message", "see message body");

		// Prepare Sending Response Body
		bw = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream(), "UTF8"));

		// Check Request Header "uri"
		if (this.uri == null) {
			this.reportStreamError(DasResponse.SC_PARAMETER_MISSING,
					"INVALIDURI GET: URI missing from request header", bw);
			return false;
		} else {
			this.uri = this.uri.toLowerCase();
			try {
				this.isValidName(this.uri, true);
			} catch (InvalidNameException inex) {

				// $JL-EXC$
				this.reportStreamError(DasResponse.SC_INVALID_CHARACTER,
						"INVALIDURI GET: " + inex.getMessage(), bw);
				return false;
			}
			if ((this.uri.indexOf("//") != -1) || !this.uri.startsWith("/")
					|| this.uri.endsWith("/") || this.uri.length() < 2) {

				// $JL-EXC$
				this.reportStreamError(
						DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
						"INVALIDURI GET: Value " + this.uri
								+ " of request header "
								+ "URI does not meet specifications", bw);
				return false;
			}
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
							"INVALIDURI GET: URI " + uri + " not found");
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
						invalid = result.getString("INVALID");
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
										"INVALIDURI GET: URI "
												+ uri
												+ " is not assigned to a collection or resource");
						return false;
					}
				} else {
					this
							.reportError(
									HttpServletResponse.SC_CONFLICT,
									"INVALIDURI GET: URI "
											+ uri
											+ " is not assigned to a collection or resource");
					return false;
				}
			}

			// URI Points To A Resource
			if (range.equalsIgnoreCase("RES")) {
				if (invalid.equals("Y"))
					bw.write(uri + "\r\n");
			}

			// URI Points To A Collection
			else {

				// Loop Top Down All Collections
				pst1 = connection.prepareStatement(SEL_COL_TAB2);
				pst2 = connection.prepareStatement(SEL_COL_TAB3);
				pst3 = connection.prepareStatement(SEL_RES_TAB2);
				this.traverse(colId);
				pst1.close();
				pst2.close();
				pst3.close();
			}

			// Successful
			this.writeStatus(bw, HttpServletResponse.SC_OK, "Ok");
			bw.flush();
			bw.close();
			return true;
		} catch (SQLException sqlex) {
			this.reportStreamError(DasResponse.SC_SQL_ERROR, "INVALIDURI GET: "
					+ sqlex.getMessage(), sqlex, bw);
			return false;
		} catch (IOException ioex) {
			this.reportStreamError(DasResponse.SC_IO_ERROR, "INVALIDURI GET: "
					+ ioex.getMessage(), ioex, bw);
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
				if (pst3 != null)
					pst3.close();
			} catch (SQLException sqlex) {

				// $JL-EXC$
				MasterMethod.cat.errorT(loc, "INVALIDURI GET: "
						+ getStackTrace(sqlex));
			}
		}
	}

	private void traverse(long colId) throws SQLException, IOException {
		ArrayList<Long> childrenNodesList = new ArrayList<Long>();

		// Get All Children Nodes
		ResultSet result = null;
		pst1.setLong(1, colId);
		result = pst1.executeQuery();
		while (result.next()) {
			childrenNodesList.add(new Long(result.getLong("COLID")));
		}
		result.close();

		// Get Collection URI
		String uri = "";
		pst2.setLong(1, colId);
		result = pst2.executeQuery();
		while (result.next()) {
			uri = result.getString("URI");
		}
		result.close();

		// Get All Resource Names
		pst3.setLong(1, colId);
		result = pst3.executeQuery();
		while (result.next()) {
			bw.write(uri + "/" + result.getString("RESNAME") + "\r\n");
		}
		result.close();

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
