package com.sap.archtech.daservice.commands;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.ejb.FinderException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.CollectionData;
import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.data.PollStatus;
import com.sap.archtech.daservice.ejb.PackStatusDBLocal;
import com.sap.archtech.daservice.ejb.PackStatusDBLocalHome;
import com.sap.archtech.daservice.exceptions.MissingParameterException;
import com.sap.archtech.daservice.exceptions.NoSuchDBObjectException;
import com.sap.archtech.daservice.exceptions.WrongArgumentException;

public class PackStatusMethod extends MasterMethod {

	private final static String SEL_COL_TAB = "SELECT colId, storeId FROM BC_XMLA_COL WHERE uri = ?";

	private Connection connection;
	private String coll;

	public PackStatusMethod(Connection connection,
			HttpServletResponse response, String coll) {
		this.connection = connection;
		this.response = response;
		this.coll = coll;
	}

	public boolean execute() throws IOException {
		PreparedStatement pst1 = null;
		CollectionData colldat;
		PackStatusDBLocalHome pHome;
		boolean status = false;

		try {
			Context initCtx = new InitialContext();
			pst1 = connection.prepareStatement(SEL_COL_TAB);
			colldat = this.checkCollection(this.coll, pst1);
			PollStatus polStat = null;
			pHome = (PackStatusDBLocalHome) initCtx
					.lookup("java:comp/env/PackStatusDBBean");

			if (pHome != null)
				polStat = this.readStatus(colldat.getcolId(), pHome);

			if (polStat == null)
				response.setHeader("packstatus", "FINISHED");
			else
				response.setHeader("packstatus", polStat.getMessage() + " "
						+ polStat.getPackedres() + "/" + polStat.getPackres());

			this.response.setHeader("service_message", "Ok");
			status = true;
		} catch (WrongArgumentException waex) {
			this.reportError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
					"PACKSTATUS: " + waex.getMessage(), waex);
		} catch (NoSuchDBObjectException nsdbex) {
			this.reportError(DasResponse.SC_DOES_NOT_EXISTS, "PACKSTATUS: "
					+ nsdbex.getMessage(), nsdbex);
		} catch (MissingParameterException msex) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING, "PACKSTATUS: "
					+ msex.getMessage(), msex);
		} catch (NamingException nmex) {
			this.reportError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"PACKSTATUS: " + nmex.getMessage(), nmex);
		} catch (SQLException sqlex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "PACKSTATUS: "
					+ sqlex.getMessage(), sqlex);
		} finally {
			// in every case close all open Statements
			try {
				if (pst1 != null)
					pst1.close();
			} catch (SQLException sqlex) {
				this.reportError(DasResponse.SC_SQL_ERROR, "PACKSTATUS: "
						+ sqlex.getMessage(), sqlex);
				status = false;
			}
		}
		return status;
	}

	private PollStatus readStatus(long colId, PackStatusDBLocalHome pHome) {
		PollStatus status = null;
		PackStatusDBLocal pAccess = null;

		try {
			pAccess = pHome.findByPrimaryKey(Long.valueOf(colId));
		} catch (FinderException fnex) {
			// $JL-EXC$ if entity bean is not found, null should be returned
			return null;
		}

		status = pAccess.getStatus();
		return status;

	}
}
