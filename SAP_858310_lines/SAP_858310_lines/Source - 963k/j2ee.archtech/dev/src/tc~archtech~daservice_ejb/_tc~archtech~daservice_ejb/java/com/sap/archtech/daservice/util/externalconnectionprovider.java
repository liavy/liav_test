package com.sap.archtech.daservice.util;

import java.sql.*;
import javax.naming.*;
import javax.sql.*;

public class ExternalConnectionProvider implements ConnectionProvider {

	private DataSource ds;

	public ExternalConnectionProvider() {
	}

	public Connection getConnection() throws SQLException {
		Connection con = ds.getConnection();
		con.setAutoCommit(false);
		return con;
	}

	public void closeConnectionPool() {
		// nothing to do
	}

	public void closeConnection(Connection con) throws SQLException {
		con.close();
	}

	public void createConnectionPool() throws NamingException {

		// Obtain our environment naming context
		Context initCtx = new InitialContext();
		// Look up our data source
		this.ds = (DataSource) initCtx.lookup("java:comp/env/SAP/BC_XMLA");
	}
}
