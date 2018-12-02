package com.sap.archtech.daservice.storage;

import javax.naming.InitialContext;

import com.sap.archtech.daservice.commands.MasterMethod;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.security.core.server.destinations.api.DestinationService;
import com.sap.security.core.server.destinations.api.HTTPDestination;

public class XmlDasMaster {

	public final static int SC_OK = 200;
	public final static String RP_OK = "OK";
	public final static int SC_CREATED = 201;
	public final static String RP_CREATED = "Created";
	public final static int SC_PARTIAL_CONTENT = 206;
	public final static String RP_PARTIAL_CONTENT = "Partial Content";
	public final static int SC_NOT_FOUND = 404;
	public final static String RP_NOT_FOUND = "Not found";
	public final static int SC_LOCKED = 423;
	public final static String RP_LOCKED = "Locked";
	public final static int SC_CHECK_FAILED = 493;
	public final static String RP_CHECK_FAILED = "Check failed";
	public final static int SC_INTERNAL_SERVER_ERROR = 500;
	public final static String RP_INTERNAL_SERVER_ERROR = "Internal Server Error";
	public final static int SC_VIRUS_SCAN_ERROR = 590;
	public final static String RP_VIRUS_SCAN_ERROR = "Virus Scan Error";
	public final static int SC_CANNOT_UPDATE_WEBDAV_PROPERTY = 592;
	public final static String RP_CANNOT_UPDATE_WEBDAV_PROPERTY = "Can not update WEBDAV property";
	public final static int SC_CANNOT_READ_WEBDAV_PROPERTY = 591;
	public final static String RP_CANNOT_READ_WEBDAV_PROPERTY = "Can not read WEBDAV property";
	public final static int SC_IO_ERROR = 598;
	public final static String RP_IO_ERROR = "I/O Error";

	protected Sapxmla_Config sac;

	public static String getPhysicalPath(Sapxmla_Config sac, String archivePath) {

		try {
			// WebDAV System
			if (sac.type.equalsIgnoreCase("W")) {

				// After Destination Service Usage
				if ((sac.destination != null)
						&& (sac.destination.trim().length() != 0)) {
					DestinationService destService = (DestinationService) new InitialContext()
							.lookup(DestinationService.JNDI_KEY);
					HTTPDestination httpDest = (HTTPDestination) destService
							.getDestination("HTTP", sac.destination);
					String destUrl = httpDest.getUrl();
					if (destUrl.endsWith("/"))
						destUrl = destUrl.substring(0, destUrl.length() - 1);
					return destUrl + archivePath.trim();
				}

				// Before Destination Service Usage
				else {
					return sac.win_root.trim() + archivePath.trim();
				}
			}

			// File System
			else {

				// Unix Operation System
				if (System.getProperty("file.separator").startsWith("/")) {
					if (sac.unix_root.contains("<DIR_GLOBAL>"))
						return sac.unix_root.replace("<DIR_GLOBAL>",
								MasterMethod.GLOBAL_DIR)
								+ archivePath.trim();
					else
						return sac.unix_root.trim() + archivePath.trim();
				}

				// Windows Operating System
				else {
					if (sac.win_root.contains("<DIR_GLOBAL>"))
						return sac.win_root.replace("<DIR_GLOBAL>",
								MasterMethod.GLOBAL_DIR)
								+ archivePath.replace('/', '\\').trim();
					else
						return sac.win_root.trim()
								+ archivePath.replace('/', '\\').trim();
				}
			}
		} catch (Exception ex) {

			// $JL-EXC$
			return "";
		}
	}
}
