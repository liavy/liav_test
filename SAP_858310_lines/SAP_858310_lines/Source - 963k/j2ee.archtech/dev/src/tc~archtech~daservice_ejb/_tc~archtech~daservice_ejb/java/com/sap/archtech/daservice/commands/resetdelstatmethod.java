package com.sap.archtech.daservice.commands;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.CollectionData;
import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.exceptions.MissingParameterException;
import com.sap.archtech.daservice.exceptions.NoSuchDBObjectException;
import com.sap.archtech.daservice.exceptions.WrongArgumentException;
import com.sap.engine.frame.core.locking.LockException;
import com.sap.engine.frame.core.locking.TechnicalLockException;
import com.sap.engine.services.applocking.TableLocking;

public class ResetDelstatMethod extends MasterMethod {

	private final static String SEL_COL_TAB = "SELECT colId, storeId FROM BC_XMLA_COL WHERE uri = ?";
	private final static String UPD_COL_DEL = "UPDATE BC_XMLA_RES SET delstatus = ? WHERE colId = ? AND delstatus = ?";

	private Connection connection;
	private String uri;
	private TableLocking tlock;

	public ResetDelstatMethod(HttpServletResponse response,
			Connection connection, String uri, TableLocking tlock) {
		this.response = response;
		this.connection = connection;
		this.uri = uri.toLowerCase();
		this.tlock = tlock;
	}

	public boolean execute() throws IOException {
		int resetcounter;
		CollectionData cdat;
		PreparedStatement pst1 = null;
		PreparedStatement pst2 = null;
		HashMap<String, Long> pkMap = new HashMap<String, Long>();
		boolean status = false;

		try {
			pst1 = connection.prepareStatement(SEL_COL_TAB);
			pst2 = connection.prepareStatement(UPD_COL_DEL);
			cdat = this.checkCollection(this.uri, pst1);

			pkMap.clear();
			pkMap.put("COLID", new Long(cdat.getcolId()));
			try {
				tlock.lock(TableLocking.LIFETIME_TRANSACTION, connection,
						"BC_XMLA_COL", pkMap,
						TableLocking.MODE_EXCLUSIVE_NONCUMULATIVE);
			} catch (LockException loex) {

				// $JL-EXC$
				// the update is already running - nothing to do
				return true;
			} catch (TechnicalLockException tlex) {
				// Generate an error message but return with true so that
				// all locks are released during commit
				this.reportError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"RESETDELSTAT: " + tlex.getMessage(), tlex);
				return true;
			}

			resetcounter = this.update_collection(cdat, pst2);

			this.response.setHeader("service_message", "Ok");
			this.response.setHeader("resetcount", String.valueOf(resetcounter));
			status = true;
		} catch (WrongArgumentException waex) {
			this.reportError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
					"RESETDELSTAT: " + waex.getMessage(), waex);
		} catch (SQLException sqlex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "RESETDELSTAT: "
					+ sqlex.getMessage(), sqlex);
		} catch (NoSuchDBObjectException nsdbex) {
			this.reportError(DasResponse.SC_DOES_NOT_EXISTS, "RESETDELSTAT: "
					+ nsdbex.getMessage(), nsdbex);
		} catch (MissingParameterException msex) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING, "RESETDELSTAT: "
					+ msex.getMessage(), msex);
		} finally {
			try {
				if (pst1 != null)
					pst1.close();
				if (pst2 != null)
					pst2.close();
			} catch (SQLException sqlex) {
				this.reportError(DasResponse.SC_SQL_ERROR, "RESETDELSTAT: "
						+ sqlex.getMessage(), sqlex);
				status = false;
			}
		}
		return status;
	}

	private int update_collection(CollectionData cdat, PreparedStatement pst2)
			throws SQLException {
		int counter;

		pst2.setString(1, "N");
		pst2.setLong(2, cdat.getcolId());
		pst2.setString(3, "P");
		counter = pst2.executeUpdate();
		return counter;
	}
}
