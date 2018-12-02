package com.sap.archtech.daservice.util;

import java.io.File;

import com.sap.archtech.daservice.commands.MasterMethod;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.tssap.dtr.client.lib.protocol.IResponse;
import com.tssap.dtr.client.lib.protocol.requests.http.HeadRequest;
import com.tssap.dtr.client.lib.protocol.requests.http.OptionsRequest;

public class ArchiveStoreTest {

	public static String execute(Sapxmla_Config sac) {
		if (sac.type.toUpperCase().startsWith("W")) {
			int optionsStatusCode = 0;
			String optionsReasonPhrase = "";
			boolean optionsSupportsDAV = false;
			int headStatusCode = 0;
			String headReasonPhrase = "";
			com.tssap.dtr.client.lib.protocol.IConnection conn = null;
			try {

				// Acquire DTR Connection
				conn = WebDavTemplateProvider
						.acquireWebDAVConnection(sac.store_id);

				// OPTIONS request
				OptionsRequest optionsRequest = new OptionsRequest("");
				IResponse optionsResponse = optionsRequest.perform(conn);
				optionsSupportsDAV = optionsRequest.supportsDAV();
				optionsStatusCode = optionsResponse.getStatus();
				optionsReasonPhrase = optionsResponse.getStatusDescription();

				// HEAD Request
				HeadRequest headRequest = new HeadRequest("");
				IResponse headResponse = headRequest.perform(conn);
				headStatusCode = headResponse.getStatus();
				headReasonPhrase = headResponse.getStatusDescription();
			} catch (Exception ex) {
				return "F;Archive Store " + sac.archive_store
						+ " is not running. Exception: " + ex.getMessage();
			} finally {

				// Release DTR Connection
				WebDavTemplateProvider.releaseWebDAVConnection(conn);
			}
			if ((optionsStatusCode != 200) || (optionsSupportsDAV == false)
					|| (headStatusCode != 200)) {
				return "F;Archive Store "
						+ sac.archive_store
						+ " is not running. Response from archive store: OPTIONS "
						+ optionsStatusCode + " " + optionsReasonPhrase
						+ " HEAD " + headStatusCode + " " + headReasonPhrase;
			}
		} else {
			boolean isDirectory = false;
			try {
				File f = null;

				// Unix Operation System
				if (System.getProperty("file.separator").startsWith("/")) {
					if (sac.unix_root.contains("<DIR_GLOBAL>"))
						f = new File(sac.unix_root.replace("<DIR_GLOBAL>",
								MasterMethod.GLOBAL_DIR));
					else
						f = new File(sac.unix_root);
				}

				// Windows Operating System
				else {
					if (sac.win_root.contains("<DIR_GLOBAL>"))
						f = new File(sac.win_root.replace("<DIR_GLOBAL>",
								MasterMethod.GLOBAL_DIR));
					else
						f = new File(sac.win_root);
				}

				// Check If Directory Exists
				isDirectory = f.isDirectory();
			} catch (Exception ex) {
				return "F;Archive Store " + sac.archive_store
						+ " is not running. Exception: " + ex.getMessage();
			}
			if (isDirectory == false) {
				return "F;Archive Store " + sac.archive_store
						+ " is not running";
			}
		}

		// Archive Store Test Was Successful
		return "S";
	}
}
