package com.sap.archtech.daservice.commands;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;
import com.sap.archtech.daservice.exceptions.ArchStoreConfigException;
import com.sap.archtech.daservice.exceptions.InvalidNameException;
import com.sap.archtech.daservice.storage.XmlDasMaster;
import com.sap.archtech.daservice.util.StringFilter;
import com.sap.sld.api.util.SystemExplorer;

public class InfoMethod extends MasterMethod {

	private final static String SEL_COL_TAB = "SELECT STOREID FROM BC_XMLA_COL WHERE URI = ?";

	private Connection connection;
	private ArchStoreConfigLocalHome beanLocalHome;
	private String archive_path;

	public InfoMethod(HttpServletResponse response, Connection connection,
			ArchStoreConfigLocalHome beanLocalHome, String archive_path) {
		this.response = response;
		this.connection = connection;
		this.beanLocalHome = beanLocalHome;
		this.archive_path = archive_path;
	}

	public boolean execute() throws IOException {

		// Variables
		int hits = 0;
		long storeId = 0;
		PreparedStatement pst = null;
		ResultSet result = null;
		Sapxmla_Config sac = null;

		// Check Request Header "archive_path"
		if (this.archive_path != null) {
			try {
				this.isValidName(this.archive_path, true);
			} catch (InvalidNameException inex) {

				// $JL-EXC$
				this.reportError(DasResponse.SC_INVALID_CHARACTER, "INFO: "
						+ inex.getMessage());
				return false;
			}
			this.archive_path = this.archive_path.toLowerCase();
			if ((this.archive_path.indexOf("//") != -1)
					|| !this.archive_path.startsWith("/")
					|| !this.archive_path.endsWith("/")
					|| this.archive_path.length() < 3) {
				this
						.reportError(
								DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
								"INFO: Value "
										+ this.archive_path
										+ " of request header ARCHIVE_PATH does not meet specifications");
				return false;
			}
		}

		boolean status = false;
		boolean errorOccurred = false;
		try {

			// Set Response Content Type
			this.response.setContentType("text/xml");

			// Set DAS Version
			this.response.setHeader("release", MasterMethod.getXmlDasVersion());

			// Set Database And JDBC Product Name And Product Version
			DatabaseMetaData dbmd = this.connection.getMetaData();

			String DBProductName = dbmd.getDatabaseProductName();
			if (DBProductName != null)
				this.response.setHeader("dbproductname", StringFilter
						.filterResponseHeaderField(DBProductName));
			else
				this.response.setHeader("dbproductname", "");

			String DBProductVersion = dbmd.getDatabaseProductVersion();
			if (DBProductVersion != null)
				this.response.setHeader("dbproductversion", StringFilter
						.filterResponseHeaderField(DBProductVersion));
			else
				this.response.setHeader("dbproductversion", "");

			String JDBCDriverName = dbmd.getDriverName();
			if (JDBCDriverName != null)
				this.response.setHeader("jdbcdrivername", StringFilter
						.filterResponseHeaderField(JDBCDriverName));
			else
				this.response.setHeader("jdbcdrivername", "");

			String JDBCDriverVersion = dbmd.getDriverVersion();
			if (JDBCDriverVersion != null)
				this.response.setHeader("jdbcdriverversion", StringFilter
						.filterResponseHeaderField(JDBCDriverVersion));
			else
				this.response.setHeader("jdbcdriverversion", "");

			// Set Operation System Informations
			String OSName = System.getProperty("os.name");
			if (OSName != null)
				this.response.setHeader("osname", StringFilter
						.filterResponseHeaderField(OSName));
			else
				this.response.setHeader("osname", "");

			String OSVersion = System.getProperty("os.version");
			if (OSVersion != null)
				this.response.setHeader("osversion", StringFilter
						.filterResponseHeaderField(OSVersion));
			else
				this.response.setHeader("osversion", "");

			// Set Java Informations
			String JavaVendor = System.getProperty("java.vendor");
			if (JavaVendor != null)
				this.response.setHeader("javavendor", StringFilter
						.filterResponseHeaderField(JavaVendor));
			else
				this.response.setHeader("javavendor", "");

			String JavaVersion = System.getProperty("java.version");
			if (JavaVersion != null)
				this.response.setHeader("javaversion", StringFilter
						.filterResponseHeaderField(JavaVersion));
			else
				this.response.setHeader("javaversion", "");

			// Set Java VM Informations
			String JavaVMVendor = System.getProperty("java.vm.vendor");
			if (JavaVMVendor != null)
				this.response.setHeader("javavmvendor", StringFilter
						.filterResponseHeaderField(JavaVMVendor));
			else
				this.response.setHeader("javavmvendor", "");

			String JavaVMName = System.getProperty("java.vm.name");
			if (JavaVMName != null)
				this.response.setHeader("javavmname", StringFilter
						.filterResponseHeaderField(JavaVMName));
			else
				this.response.setHeader("javavmname", "");

			String JavaVMVersion = System.getProperty("java.vm.version");
			if (JavaVMVersion != null)
				this.response.setHeader("javavmversion", StringFilter
						.filterResponseHeaderField(JavaVMVersion));
			else
				this.response.setHeader("javavmversion", "");

			// Set XML Data Archiving Server Name
			try {
				String XMLDataArchivingServer = "xmldas.SystemName."
						+ SystemExplorer.getJ2EEClusterEngineName();
				if (XMLDataArchivingServer != null)
					this.response.setHeader("xmldas_name",
							XMLDataArchivingServer);
				else
					this.response.setHeader("xmldas_name", "");
			} catch (Exception ex) {

				// $JL-EXC$
				this.response.setHeader("xmldas_name", "");
			}

			// Set SAP System Name
			String sapSID = System.getProperty("SAPSYSTEMNAME");
			if (sapSID != null)
				this.response.setHeader("sap_sid", sapSID);

			// Set Archive Path
			if (this.archive_path != null) {

				// Adjust Archive Path For Further Processing
				this.archive_path = this.archive_path.substring(0,
						this.archive_path.length() - 1).trim();

				// Get Collection Id and Archive Store
				pst = connection.prepareStatement(SEL_COL_TAB);
				pst.setString(1, this.archive_path.trim());
				result = pst.executeQuery();
				hits = 0;
				while (result.next()) {
					storeId = result.getLong("STOREID");
					hits++;
				}
				result.close();
				pst.close();
				if (hits == 0) {
					int lastSlashNum = this.archive_path.lastIndexOf("/");
					int strLen = this.archive_path.length();
					if ((lastSlashNum != -1) && (lastSlashNum < strLen))
						this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
								"INFO: Collection "
										+ this.archive_path.substring(
												lastSlashNum + 1, strLen)
										+ " does not exist");
					else
						this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
								"INFO: Collection does not exist");
					errorOccurred = true;
				}

				// Check If Selected Archive Store Is Assigned
				if (!errorOccurred) {
					if (storeId > 0) {

						// Get Archive Store Configuration Data
						sac = this.getArchStoreConfigObject(beanLocalHome,
								storeId);

						// Set Physical Path
						this.response.setHeader("archive_store",
								sac.archive_store);
						this.response.setHeader("physical_path", XmlDasMaster
								.getPhysicalPath(sac, this.archive_path.trim()
										+ "/"));

						// Set ILM Conformance Class
						this.response.setHeader("ilm_conformance_class", Short
								.toString(sac.ilm_conformance));
						// Set archive store type
						this.response.setHeader("store_type", sac.type.trim());
					} else {

						// Set Physical Path
						this.response.setHeader("archive_store", "");
						this.response.setHeader("physical_path", "");

						// Set ILM Conformance Class
						this.response.setHeader("ilm_conformance_class", "0");
						// Set archive store type
						this.response.setHeader("store_type", "");
					}
				}
			}

			// Method Was Successful
			if (!errorOccurred) {
				this.response.setHeader("service_message", "Ok");
				status = true;
			}

		} catch (ArchStoreConfigException ascex) {
			this.reportError(DasResponse.SC_CONFIG_INCONSISTENT, "INFO: "
					+ ascex.getMessage(), ascex);
		} catch (SQLException sqlex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "INFO: "
					+ sqlex.toString(), sqlex);
		} finally {
			try {
				if (pst != null)
					pst.close();
			} catch (SQLException sqlex) {
				this.reportError(DasResponse.SC_SQL_ERROR, "INFO: "
						+ sqlex.toString(), sqlex);
				status = false;
			}
		}
		return status;
	}
}