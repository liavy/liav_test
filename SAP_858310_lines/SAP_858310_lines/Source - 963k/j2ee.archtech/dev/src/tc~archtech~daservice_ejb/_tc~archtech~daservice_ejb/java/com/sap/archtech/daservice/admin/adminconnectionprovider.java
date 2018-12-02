package com.sap.archtech.daservice.admin;

import java.sql.*;
import javax.sql.*;
import javax.naming.*;

public class AdminConnectionProvider {
	public Connection getConnection() throws SQLException, NamingException {

		// Obtain Our Environment Naming Context
		Context initCtx = new InitialContext();

		// Look Up Our Data Source
		DataSource ds = (DataSource) initCtx
				.lookup("java:comp/env/SAP/BC_XMLA");

		// Get DB Connection
		Connection con = ds.getConnection();
		con.setAutoCommit(false);

		// Return DB Connection
		return con;
	}
}
