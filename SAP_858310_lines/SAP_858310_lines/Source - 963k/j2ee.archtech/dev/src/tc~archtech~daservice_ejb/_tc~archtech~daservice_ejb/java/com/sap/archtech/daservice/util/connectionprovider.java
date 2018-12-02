package com.sap.archtech.daservice.util;

import java.sql.*;
import javax.naming.NamingException;

public interface ConnectionProvider {
	public abstract Connection getConnection() throws SQLException;

	public abstract void closeConnection(Connection con) throws SQLException;

	public abstract void createConnectionPool() throws NamingException;

	public abstract void closeConnectionPool();
}
