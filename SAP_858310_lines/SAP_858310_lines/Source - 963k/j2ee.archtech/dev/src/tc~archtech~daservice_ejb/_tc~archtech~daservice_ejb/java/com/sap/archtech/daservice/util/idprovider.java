package com.sap.archtech.daservice.util;

import java.sql.*;

public class IdProvider {

	private final static String INC_ID = "UPDATE BC_XMLA_MAXIDS SET MAXID = MAXID + 1 WHERE TABLENAME = ?";
	private final static String GET_ID = "SELECT MAXID FROM BC_XMLA_MAXIDS WHERE TABLENAME = ?";

	private Connection con;

	public IdProvider(Connection con) {
		this.con = con;
	}

	public long getId(String table) throws SQLException {
		int count;
		PreparedStatement pst1;
		PreparedStatement pst2;

		pst1 = con.prepareStatement(INC_ID);
		pst2 = con.prepareStatement(GET_ID);

		pst1.setString(1, table);
		pst2.setString(1, table);

		count = pst1.executeUpdate();
		pst1.close();

		if (count != 1) {
			throw new SQLException(
					"IdProvider: Unable to update DB key provider table");
		} else {
			ResultSet rs1 = pst2.executeQuery();
			rs1.next();
			long maxId = rs1.getLong("maxId");
			rs1.close();
			pst2.close();

			// DB Commit
			con.commit();

			// Return Max Id
			return maxId;
		}
	}
}
