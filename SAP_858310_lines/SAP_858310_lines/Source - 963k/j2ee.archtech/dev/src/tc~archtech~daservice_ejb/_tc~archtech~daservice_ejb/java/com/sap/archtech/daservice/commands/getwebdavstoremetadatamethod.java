package com.sap.archtech.daservice.commands;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocal;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;
import com.sap.archtech.daservice.util.WebDavTemplateProvider;
import com.tssap.dtr.client.lib.protocol.Header;
import com.tssap.dtr.client.lib.protocol.IConnection;
import com.tssap.dtr.client.lib.protocol.IResponse;
import com.tssap.dtr.client.lib.protocol.requests.http.OptionsRequest;

public class GetWebdavStoreMetaDataMethod extends MasterMethod {

	private ArchStoreConfigLocalHome beanLocalHome;
	private String archive_store;

	public GetWebdavStoreMetaDataMethod(HttpServletResponse response,
			ArchStoreConfigLocalHome beanLocalHome, String archive_store) {
		this.response = response;
		this.beanLocalHome = beanLocalHome;
		this.archive_store = archive_store;
	}

	public boolean execute() throws IOException {

		// Variables
		boolean status = false;
		boolean errorOccurred = false;
		long storeId = 0;
		Sapxmla_Config sac = null;

		// Check Request Header "archive_store"
		if (this.archive_store == null) {
			this
					.reportError(DasResponse.SC_PARAMETER_MISSING,
							"GET_WEBDAV_STORE_META_DATA: ARCHIVE_STORE missing from request header");
			return false;
		}

		// Set Response Content Type
		this.response.setContentType("text/xml");

		// Initialize Response Headers
		this.response.setHeader("dav-conformance", "");
		this.response.setHeader("ilm-conformance", "0");
		this.response.setHeader("ilm-al-conformance", "0");

		// Get Archive Store Access
		try {
			Collection<ArchStoreConfigLocal> col = beanLocalHome
					.findByArchiveStore(this.archive_store);
			Iterator<ArchStoreConfigLocal> iter = col.iterator();
			if (col.isEmpty()) {

				// Report Error
				this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
						"GET_WEBDAV_STORE_META_DATA: Archive store "
								+ this.archive_store + " is not defined");
				errorOccurred = true;
			} else {
				ArchStoreConfigLocal ascl = (ArchStoreConfigLocal) iter.next();
				storeId = ((Long) ascl.getPrimaryKey()).longValue();
			}
		} catch (Exception ex) {
			this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
					"GET_WEBDAV_STORE_META_DATA: Archive store "
							+ this.archive_store + " is not defined: "
							+ ex.getMessage(), ex);
			errorOccurred = true;
		}
		if (!errorOccurred) {
			try {

				// Get Archive Store Configuration Data
				sac = this.getArchStoreConfigObject(beanLocalHome, storeId);

			} catch (Exception ex) {
				this.reportError(DasResponse.SC_CONFIG_INCONSISTENT,
						"GET_WEBDAV_STORE_META_DATA: Archive store "
								+ this.archive_store + " not defined: "
								+ ex.getMessage(), ex);
				errorOccurred = true;
			}
			if (sac == null)
				errorOccurred = true;
			if (!errorOccurred) {
				if (sac.type.equalsIgnoreCase("W")) {
					int optionsStatusCode = 0;
					String optionsReasonPhrase = "";
					String davConformance = "";
					String ilmConformance = "";
					String ilmAlConformance = "";
					IConnection conn = null;
					try {

						// Acquire DTR Connection
						conn = WebDavTemplateProvider
								.acquireWebDAVConnection(sac.store_id);

						// Perform OPTIONS Request
						OptionsRequest optionsRequest = new OptionsRequest("");
						IResponse optionsResponse = optionsRequest
								.perform(conn);
						optionsStatusCode = optionsResponse.getStatus();
						optionsReasonPhrase = optionsResponse
								.getStatusDescription();

						// Get OPTIONS Request Header
						Header header = optionsResponse.getHeader("DAV");
						if (header != null) {
							davConformance = header.getValue();
							if (davConformance == null)
								davConformance = "";
						} else
							davConformance = "";
						header = optionsResponse
								.getHeader("SAP-ILM-Conformance");
						if (header != null) {
							ilmConformance = header.getValue();
							if (ilmConformance == null)
								ilmConformance = "0";
						} else
							ilmConformance = "0";
						header = optionsResponse
								.getHeader("SAP-ILM-AL-Conformance");
						if (header != null) {
							ilmAlConformance = header.getValue();
							if (ilmAlConformance == null)
								ilmAlConformance = "0";
						} else
							ilmAlConformance = "0";
					} catch (Exception ex) {
						this.reportError(DasResponse.SC_IO_ERROR,
								"GET_WEBDAV_STORE_META_DATA: Archive store "
										+ this.archive_store
										+ " is not accessible: "
										+ ex.getMessage(), ex);
						errorOccurred = true;
					} finally {

						// Release DTR Connection
						WebDavTemplateProvider.releaseWebDAVConnection(conn);
					}

					// Set Response Headers
					if (!errorOccurred) {
						if (optionsStatusCode == 200) {
							this.response.setHeader("dav-conformance",
									davConformance);
							this.response.setHeader("ilm-conformance",
									ilmConformance);
							this.response.setHeader("ilm-al-conformance",
									ilmAlConformance);
						} else {
							this
									.reportError(
											DasResponse.SC_IO_ERROR,
											"GET_WEBDAV_STORE_META_DATA: Archive store "
													+ this.archive_store
													+ " sent following response for the OPTIONS request: "
													+ optionsStatusCode + " "
													+ optionsReasonPhrase);
							errorOccurred = true;
						}
					}
				}
			}

			// Method Was Successful
			if (!errorOccurred) {
				this.response.setHeader("service_message", "Ok");
				status = true;
			}
		}
		return status;
	}
}