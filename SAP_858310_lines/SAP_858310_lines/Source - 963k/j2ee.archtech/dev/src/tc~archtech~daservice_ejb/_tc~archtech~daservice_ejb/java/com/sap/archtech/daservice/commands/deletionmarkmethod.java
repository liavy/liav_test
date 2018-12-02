package com.sap.archtech.daservice.commands;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.CollectionData;
import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.exceptions.MissingParameterException;
import com.sap.archtech.daservice.exceptions.NoSuchDBObjectException;
import com.sap.archtech.daservice.exceptions.WrongArgumentException;

public class DeletionMarkMethod extends MasterMethod {

	private final static String SEL_COL_TAB = "SELECT colId, storeId FROM BC_XMLA_COL WHERE uri = ?";
	private final static String UPD_COL_DEL = "UPDATE BC_XMLA_RES SET delstatus = ? WHERE colId = ? AND restype <> 'XSD'";

	private Connection connection;
	private String uri;

	public DeletionMarkMethod(HttpServletResponse response,
			Connection connection, String uri) {
		this.response = response;
		this.connection = connection;
		this.uri = uri.toLowerCase();
	}

	public boolean execute() throws IOException {
		int delcounter;
		CollectionData cdat;
		PreparedStatement pst1 = null;
		PreparedStatement pst2 = null;
		boolean status = false;

		try {
			pst1 = connection.prepareStatement(SEL_COL_TAB);
			pst2 = connection.prepareStatement(UPD_COL_DEL);
			cdat = this.checkCollection(this.uri, pst1);
			delcounter = this.update_collection(cdat, pst2);

			this.response.setHeader("service_message", "Ok");
			this.response
					.setHeader("deletioncount", String.valueOf(delcounter));
			status = true;
		} catch (WrongArgumentException waex) {
			this.reportError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
					"DELETIONMARK: " + waex.getMessage(), waex);
		} catch (SQLException sqlex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "DELETIONMARK: "
					+ sqlex.getMessage(), sqlex);
		} catch (NoSuchDBObjectException nsdbex) {
			this.reportError(DasResponse.SC_DOES_NOT_EXISTS, "DELETIONMARK: "
					+ nsdbex.getMessage(), nsdbex);
		} catch (MissingParameterException msex) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING, "DELETIONMARK: "
					+ msex.getMessage(), msex);
		} finally {
			try {
				if (pst1 != null)
					pst1.close();
				if (pst2 != null)
					pst2.close();
			} catch (SQLException sqlex) {
				this.reportError(DasResponse.SC_SQL_ERROR, "DELETIONMARK: "
						+ sqlex.getMessage(), sqlex);
				status = false;
			}
		}

		return status;
	}

	private int update_collection(CollectionData cdat, PreparedStatement pst2)
			throws SQLException {
		int counter;

		pst2.setString(1, "Y");
		pst2.setLong(2, cdat.getcolId());
		counter = pst2.executeUpdate();
		return counter;
	}
}
