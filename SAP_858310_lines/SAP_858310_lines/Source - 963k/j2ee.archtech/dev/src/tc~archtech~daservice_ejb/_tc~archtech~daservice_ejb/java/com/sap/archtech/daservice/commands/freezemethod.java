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

public class FreezeMethod extends MasterMethod {

	private final static String SEL_COL_TAB1 = "SELECT COLID FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_COL_TAB2 = "SELECT COLID FROM BC_XMLA_COL WHERE PARENTCOLID = ?";
	private final static String UPD_COL_TAB = "UPDATE BC_XMLA_COL SET FROZEN = 'Y' WHERE COLID = ?";

	private Connection connection;
	private String archive_path;
	private PreparedStatement pst1 = null;
	private PreparedStatement pst2 = null;
	private PreparedStatement pst3 = null;

	public FreezeMethod(HttpServletResponse response, Connection connection,
			String archive_path) {
		this.response = response;
		this.connection = connection;
		this.archive_path = archive_path;
	}

	public boolean execute() throws IOException {

		// Variables
		int hits = 0;
		long colId = 0;

		ResultSet result = null;

		// Check Request Header "archive_path"
		if (this.archive_path == null) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"FREEZE: ARCHIVE_PATH missing from request header");
			return false;
		} else {
			try {
				this.isValidName(this.archive_path, true);
			} catch (InvalidNameException inex) {

				// $JL-EXC$
				this.reportError(DasResponse.SC_INVALID_CHARACTER, "FREEZE: "
						+ inex.getMessage());
				return false;
			}
			this.archive_path = this.archive_path.toLowerCase();
			if (!(this.archive_path.indexOf("//") == -1)
					|| !this.archive_path.startsWith("/")
					|| !this.archive_path.endsWith("/")
					|| this.archive_path.length() < 3) {
				this
						.reportError(
								DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
								"FREEZE: Value "
										+ this.archive_path
										+ " of request header ARCHIVE_PATH does not meet specifications");
				return false;
			}
		}

		boolean status = false;
		boolean errorOccurred = false;
		try {

			// Prepare Statements
			this.pst1 = this.connection.prepareStatement(SEL_COL_TAB1);
			this.pst2 = this.connection.prepareStatement(SEL_COL_TAB2);
			this.pst3 = this.connection.prepareStatement(UPD_COL_TAB);

			// Adjust Archive Path For Further Processing
			this.archive_path = this.archive_path.substring(0,
					this.archive_path.length() - 1).trim();

			// Get Collection Id
			pst1.setString(1, this.archive_path.trim());
			result = pst1.executeQuery();
			hits = 0;
			while (result.next()) {
				colId = result.getLong("COLID");
				hits++;
			}
			result.close();
			pst1.close();
			if (hits == 0) {
				int lastSlashNum = this.archive_path.lastIndexOf("/");
				int strLen = this.archive_path.length();
				if ((lastSlashNum != -1) && (lastSlashNum < strLen))
					this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
							"FREEZE: Collection "
									+ this.archive_path.substring(
											lastSlashNum + 1, strLen)
									+ " does not exist");
				else
					this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
							"FREEZE: Collection does not exist");
				errorOccurred = true;
			}

			if (!errorOccurred) {

				// Recursive Freeze Entries in Table BC_XMLA_COL
				this.traverse(colId);
				if (this.pst2 != null)
					this.pst2.close();
				if (this.pst3 != null)
					this.pst3.close();

				// Set Response Header Fields
				response.setContentType("text/xml");

				// Method Was Successful
				this.response.setHeader("service_message", "Ok");
				status = true;
			}
		} catch (SQLException sqlex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "FREEZE: "
					+ sqlex.toString(), sqlex);
		} finally {
			try {
				if (this.pst1 != null)
					this.pst1.close();
				if (this.pst2 != null)
					this.pst2.close();
				if (this.pst3 != null)
					this.pst3.close();
			} catch (SQLException sqlex) {
				this.reportError(DasResponse.SC_SQL_ERROR, "FREEZE: "
						+ sqlex.toString(), sqlex);
				status = false;
			}
		}
		return status;
	}

	private void traverse(long colId) throws SQLException {
		ResultSet result = null;
		ArrayList<Long> childrenNodesList = new ArrayList<Long>();

		// Get All Children Nodes
		this.pst2.setLong(1, colId);
		result = this.pst2.executeQuery();
		while (result.next()) {
			childrenNodesList.add(new Long(result.getLong("COLID")));
		}
		result.close();

		// Update BC_XMLA_COL Entry
		this.pst3.setLong(1, colId);
		this.pst3.executeUpdate();

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