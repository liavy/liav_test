package com.sap.dictionary.database.veris;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

import com.sap.devtest.util.TestEnvironment;
import com.sap.dictionary.database.dbs.ExType;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.sql.connect.OpenSQLDataSource;
import com.sap.sql.jdbc.internal.SAPDataSource;

import junit.framework.*;

public class VeriStarter extends TestCase {
	/**
	 * Name of the JVEer parameter specifying the test data source.
	 */
	private static final String DATASOURCE_PARAMETER_NAME = "database.datasource";

	public static Test suite() {
		TestSuite suite = new TestSuite();
		//suite.addTestSuite(VeriOpenFriendTools.class);
		suite.addTestSuite(VeriCommon.class);
		return suite;
	}

	private static final String[] DATABASE_PROPERTY_NAMES = new String[] {
			"database.db2_os390", "database.db2_as400", "database.db2_udb",
			"database.mss_ddd", "database.mss", "database.mysql",
			"database.oracle", "database.sapdb", "database.javadb", };

	protected final String getDatasourceParameter() throws JddException {
		String propertyFileName = null;
		for (final String databasePropertyName : DATABASE_PROPERTY_NAMES) {
			propertyFileName = TestEnvironment.instance()
					.getValueOfRequiredProperty(databasePropertyName);
			if (propertyFileName != null) {
				break;
			}
		}
		if (propertyFileName == null) {
			// for JVerPlugin in eclipse: use System.Property named
			// database.local
			propertyFileName = System.getProperty("database.local");
		}
		if (propertyFileName == null) {
			propertyFileName = "examples/local.properties";
		}
		if (propertyFileName == null || propertyFileName.length() == 0) {
			throw new JddException("Failed to obtain property file name");
		}
		final Properties properties = new Properties();
		try {
			final InputStream input = new FileInputStream(propertyFileName);
			try {
				properties.load(input);
			} finally {
				input.close();
			}
		} catch (IOException e) {
			throw new JddException(ExType.IO_FAILURE,
					"Failed to obtain property file name", e);
		}
		return properties.getProperty(DATASOURCE_PARAMETER_NAME);
	}

	protected final Connection getConnection() throws Exception {

		OpenSQLDataSource ds = SAPDataSource.getOpenSQLDataSource(getDatasourceParameter());
		ds.setConnectionType(OpenSQLDataSource.CONN_TYPE_OPEN_SQL);
		return ds.getConnection();
	}
}
