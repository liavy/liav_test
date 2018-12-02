package com.sap.archtech.daservice.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;

import javax.ejb.FinderException;
import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocal;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;

public class ListArchiveStoresMethod extends MasterMethod {

	private final static char SEPARATOR = 0x1E;
	private final static String CRLF = "\r\n";

	private ArchStoreConfigLocalHome beanLocalHome;
	private String type;
	private String user;

	public ListArchiveStoresMethod(HttpServletResponse response,
			ArchStoreConfigLocalHome beanLocalHome, String type, String user) {
		this.response = response;
		this.beanLocalHome = beanLocalHome;
		this.type = type;
		this.user = user;
	}

	public boolean execute() throws IOException {

		// Get Servlet Output Stream
		BufferedWriter bwout = new BufferedWriter(new OutputStreamWriter(
				response.getOutputStream(), "UTF8"));

		// Set Response Header
		response.setContentType(MasterMethod.contentType);
		response.setHeader("service_message", "see message body");

		// Check Request Header "type"
		if (this.type == null) {
			this.reportStreamError(DasResponse.SC_PARAMETER_MISSING,
					"_LIST_ARCHIVE_STORES: TYPE missing from request header",
					bwout);
			return false;
		} else {
			this.type = this.type.toUpperCase();
			if (!(this.type.startsWith("L"))) {
				this
						.reportStreamError(
								DasResponse.SC_KEYWORD_UNKNOWN,
								"_LIST_ARCHIVE_STORES: Value "
										+ this.type
										+ " of request header TYPE does not meet specifications",
								bwout);
				return false;
			}
		}

		// Check Request Header "user"
		if ((this.user == null) || (this.user.length() == 0)) {
			this.reportStreamError(DasResponse.SC_PARAMETER_MISSING,
					"_LIST_ARCHIVE_STORES: USER missing from request header",
					bwout);
			return false;
		}

		// Get All Archive Store Informations
		try {
			Iterator<ArchStoreConfigLocal> iter = this.beanLocalHome.findAll()
					.iterator();
			StringBuffer sb = null;
			while (iter.hasNext()) {

				// Get Archive Store Configuration
				ArchStoreConfigLocal ascl = iter.next();
				Sapxmla_Config sac = ascl.getSapxmla_Config();

				// Create Response Data
				sb = new StringBuffer();
				if (sac.archive_store != null)
					sb.append(sac.archive_store);
				sb.append(SEPARATOR);
				if (sac.storage_system != null)
					sb.append(sac.storage_system);
				sb.append(SEPARATOR);
				sb.append(sac.type);
				sb.append(SEPARATOR);
				if (sac.win_root != null)
					sb.append(sac.win_root);
				sb.append(SEPARATOR);
				if (sac.unix_root != null)
					sb.append(sac.unix_root);
				sb.append(SEPARATOR);
				if (sac.destination != null)
					sb.append(sac.destination);
				sb.append(SEPARATOR);
				if (sac.proxy_host != null)
					sb.append(sac.proxy_host);
				sb.append(SEPARATOR);
				if (sac.proxy_port != 0)
					sb.append(sac.proxy_port);
				sb.append(SEPARATOR);
				sb.append(sac.ilm_conformance);
				sb.append(SEPARATOR);
				sb.append(sac.is_default);
				sb.append(CRLF);

				// Write Response Data
				bwout.write(sb.toString());
			}

			// Write Success To Servlet Output Stream
			this.writeStatus(bwout, HttpServletResponse.SC_OK, "Ok");
			bwout.flush();
		} catch (FinderException fex) {

			// $JL-EXC$
			this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
					"_LIST_ARCHIVE_STORES: " + fex.getMessage(), bwout);
			return false;
		} finally {

			// Close Servlet Output Stream
			if (bwout != null) {
				bwout.close();
			}
		}

		// Method Was Successful
		return true;
	}
}
