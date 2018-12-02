package com.sap.archtech.daservice.admin;

import java.io.File;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.FinderException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;

import com.sap.archtech.daservice.commands.MasterMethod;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocal;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;
import com.sap.archtech.daservice.util.Director;
import com.sap.archtech.daservice.util.WebDavTemplateProvider;
import com.sap.security.core.server.destinations.api.DestinationException;
import com.sap.security.core.server.destinations.api.DestinationService;
import com.sap.security.core.server.destinations.api.HTTPDestination;
import com.sap.sld.api.builder.InvalidDataException;
import com.sap.sld.api.util.SystemExplorer;
import com.sap.sld.api.wbem.exception.CIMException;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.tssap.dtr.client.lib.protocol.Header;
import com.tssap.dtr.client.lib.protocol.IConnectionTemplate;
import com.tssap.dtr.client.lib.protocol.IResponse;
import com.tssap.dtr.client.lib.protocol.requests.dav.Depth;
import com.tssap.dtr.client.lib.protocol.requests.dav.PropfindOption;
import com.tssap.dtr.client.lib.protocol.requests.dav.PropfindRequest;
import com.tssap.dtr.client.lib.protocol.requests.http.HeadRequest;
import com.tssap.dtr.client.lib.protocol.requests.http.OptionsRequest;

public class ConfigureArchiveStores {

	protected static final Location loc = Location
			.getLocation("com.sap.archtech.daservice");
	protected static final Category cat = Category.getCategory(
			Category.APPS_COMMON_ARCHIVING, "XML_DAS");
	private final static String INS_SEL_MAX_TAB = "SELECT MAXID FROM BC_XMLA_MAXIDS WHERE TABLENAME = 'BC_XMLA_CONFIG'";
	private final static String INS_UPD_MAX_TAB = "UPDATE BC_XMLA_MAXIDS SET MAXID = ? WHERE TABLENAME = 'BC_XMLA_CONFIG'";
	private final static String DEL_SEL_COL_TAB = "SELECT URI FROM BC_XMLA_COL WHERE STOREID = ? OR NEXTSTOREID = ?";

	private HttpServletRequest request;

	public ConfigureArchiveStores(HttpServletRequest request) {
		this.request = request;
	}

	public void insert() throws Exception {

		int hits = 0;

		long storeId = 0;

		PreparedStatement pst1 = null;
		PreparedStatement pst2 = null;
		ResultSet result = null;
		Connection connection = null;

		String archiveStore = request.getParameter("archivestore").trim()
				.toUpperCase();
		String storageSystem = request.getParameter("storagesystem").trim();
		if (storageSystem.length() < 1) // When Empty Set A Blank Character
			storageSystem = " ";
		String type = request.getParameter("type");
		String winRoot = request.getParameter("winroot").trim();
		String unixRoot = request.getParameter("unixroot").trim();
		String destination = request.getParameter("destination").trim();
		String proxyHost = request.getParameter("proxyhost").trim();
		String proxyPort = request.getParameter("proxyport").trim();
		int proxyPortint;
		if (proxyPort.length() == 0)
			proxyPortint = 0;
		else
			proxyPortint = Integer.parseInt(proxyPort);
		short ilm_conformance_class = 0;
		String is_default = request.getParameter("isdefault").trim();
		if ((is_default == null) || (is_default.length() == 0))
			is_default = "N";
		try {

			// Get DB Connection
			AdminConnectionProvider acp = new AdminConnectionProvider();
			connection = acp.getConnection();

			// Check If There Is A Conflict With Already Existing Archive Stores
			if (type.equalsIgnoreCase("W"))// WebDAV System
			{
				try {
					Context ctx = new InitialContext();
					ArchStoreConfigLocalHome beanLocalHome = (ArchStoreConfigLocalHome) ctx
							.lookup("java:comp/env/ArchStoreConfigBean");
					Collection<ArchStoreConfigLocal> c = beanLocalHome
							.findAll();
					for (Iterator<ArchStoreConfigLocal> iter = c.iterator(); iter
							.hasNext();) {
						ArchStoreConfigLocal element = iter.next();
						Sapxmla_Config sac = element.getSapxmla_Config();

						// Note: There Is Not Check For Non Destination Archive
						// Stores
						if ((destination != null)
								&& ((sac.destination != null) && (sac.destination
										.trim().length() != 0))) {
							if (destination.equals(sac.destination)) {
								ConfigureArchiveStores.cat
										.errorT(
												loc,
												"DEFINE_ARCHIVE_STORES: Destination "
														+ destination
														+ " already exists in archive store "
														+ sac.archive_store);
								throw new Exception(
										"DEFINE_ARCHIVE_STORES: Destination "
												+ destination
												+ " already exists in archive store "
												+ sac.archive_store);
							}
							DestinationService destService = (DestinationService) new InitialContext()
									.lookup(DestinationService.JNDI_KEY);
							if (destService == null) {
								ConfigureArchiveStores.cat
										.errorT(loc,
												"DEFINE_ARCHIVE_STORES: Destination Service is not available");
								throw new Exception(
										"DEFINE_ARCHIVE_STORES: Destination Service is not available");
							}
							HTTPDestination thisDest = (HTTPDestination) destService
									.getDestination("HTTP", destination);
							HTTPDestination sacDest = (HTTPDestination) destService
									.getDestination("HTTP", sac.destination);
							if ((thisDest == null) || (sacDest == null)) {
								if (thisDest == null) {
									ConfigureArchiveStores.cat
											.errorT(
													loc,
													"DEFINE_ARCHIVE_STORES: Destination "
															+ destination
															+ " not found in Destination Service");
									throw new Exception(
											"DEFINE_ARCHIVE_STORES: Destination "
													+ destination
													+ " not found in Destination Service");
								} else {
									ConfigureArchiveStores.cat
											.errorT(
													loc,
													"DEFINE_ARCHIVE_STORES: Destination "
															+ sac.destination
															+ " not found in Destination Service");
									throw new Exception(
											"DEFINE_ARCHIVE_STORES: Destination "
													+ sac.destination
													+ " not found in Destination Service");
								}
							}
							String thisURLString = thisDest.getUrl();
							String sacURLString = sacDest.getUrl();
							if ((thisURLString == null)
									|| (sacURLString == null)) {
								if (thisURLString == null) {
									ConfigureArchiveStores.cat
											.errorT(
													loc,
													"DEFINE_ARCHIVE_STORES: No URL for destination "
															+ destination
															+ " found in Destination Service");
									throw new Exception(
											"DEFINE_ARCHIVE_STORES: No URL for destination "
													+ destination
													+ " found in Destination Service");
								} else {
									ConfigureArchiveStores.cat
											.errorT(
													loc,
													"DEFINE_ARCHIVE_STORES: No URL for destination "
															+ sac.destination
															+ " found in Destination Service");
									throw new Exception(
											"DEFINE_ARCHIVE_STORES: No URL for destination "
													+ sac.destination
													+ " found in Destination Service");
								}
							}
							URL thisURL = new URL(thisURLString);
							URL sacURL = new URL(sacURLString);
							InetAddress thisInet = Inet6Address
									.getByName(thisURL.getHost());
							InetAddress sacInet = Inet6Address.getByName(sacURL
									.getHost());
							String thisNormalizedURLString = thisURL
									.getProtocol()
									+ "://"
									+ thisInet.getHostAddress()
									+ ":"
									+ thisURL.getPort() + thisURL.getPath();
							String sacNormalizedURLString = sacURL
									.getProtocol()
									+ "://"
									+ sacInet.getHostAddress()
									+ ":"
									+ sacURL.getPort() + sacURL.getPath();
							if (thisNormalizedURLString
									.equals(sacNormalizedURLString)) {
								ConfigureArchiveStores.cat
										.errorT(
												loc,
												"DEFINE_ARCHIVE_STORES: URL "
														+ thisNormalizedURLString
														+ " of destination "
														+ destination
														+ " already exists in destination "
														+ sac.destination
														+ " of archive store "
														+ sac.archive_store);
								throw new Exception(
										"DEFINE_ARCHIVE_STORES: URL "
												+ thisNormalizedURLString
												+ " of destination "
												+ destination
												+ " already exists in destination "
												+ sac.destination
												+ " of archive store "
												+ sac.archive_store);
							}
							if (thisNormalizedURLString
									.startsWith(sacNormalizedURLString)) {
								if ((thisNormalizedURLString.replace(
										sacNormalizedURLString, "")
										.startsWith("/"))) {
									ConfigureArchiveStores.cat
											.errorT(
													loc,
													"DEFINE_ARCHIVE_STORES: URL "
															+ thisNormalizedURLString
															+ " of destination "
															+ destination
															+ " is a child node of URL "
															+ sacNormalizedURLString
															+ " of destination "
															+ sac.destination
															+ " that already exists in archive store "
															+ sac.archive_store);
									throw new Exception(
											"DEFINE_ARCHIVE_STORES: URL "
													+ thisNormalizedURLString
													+ " of destination "
													+ destination
													+ " is a child node of URL "
													+ sacNormalizedURLString
													+ " of destination "
													+ sac.destination
													+ " that already exists in archive store "
													+ sac.archive_store);
								}
							}
							if (sacNormalizedURLString
									.startsWith(thisNormalizedURLString)) {
								if ((sacNormalizedURLString.replace(
										thisNormalizedURLString, "")
										.startsWith("/"))) {
									ConfigureArchiveStores.cat
											.errorT(
													loc,
													"DEFINE_ARCHIVE_STORES: URL "
															+ thisNormalizedURLString
															+ " of destination "
															+ destination
															+ " is a parent node of URL "
															+ sacNormalizedURLString
															+ " of destination "
															+ sac.destination
															+ " that already exists in archive store "
															+ sac.archive_store);
									throw new Exception(
											"DEFINE_ARCHIVE_STORES: URL "
													+ thisNormalizedURLString
													+ " of destination "
													+ destination
													+ " is a parent node of URL "
													+ sacNormalizedURLString
													+ " of destination "
													+ sac.destination
													+ " that already exists in archive store "
													+ sac.archive_store);
								}
							}
						}
					}
				} catch (FinderException fex) {
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: " + fex.getMessage());
					throw new Exception("DEFINE_ARCHIVE_STORES: "
							+ fex.getMessage());
				} catch (NamingException nex) {
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: " + nex.toString());
					throw new Exception("DEFINE_ARCHIVE_STORES: "
							+ nex.toString());
				} catch (DestinationException dex) {
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: " + dex.toString());
					throw new Exception("DEFINE_ARCHIVE_STORES: "
							+ dex.toString());
				}
			} else// File System
			{
				try {
					Context ctx = new InitialContext();
					ArchStoreConfigLocalHome beanLocalHome = (ArchStoreConfigLocalHome) ctx
							.lookup("java:comp/env/ArchStoreConfigBean");
					Collection<ArchStoreConfigLocal> c = beanLocalHome
							.findAll();
					for (Iterator<ArchStoreConfigLocal> iter = c.iterator(); iter
							.hasNext();) {
						ArchStoreConfigLocal element = iter.next();
						Sapxmla_Config sac = element.getSapxmla_Config();
						if ((winRoot != null) && (sac.win_root != null)) {
							if (winRoot.equals(sac.win_root)) {
								ConfigureArchiveStores.cat
										.errorT(
												loc,
												"DEFINE_ARCHIVE_STORES: Windows root "
														+ winRoot
														+ " already exists in archive store "
														+ sac.archive_store);
								throw new Exception(
										"DEFINE_ARCHIVE_STORES: Windows root "
												+ winRoot
												+ " already exists in archive store "
												+ sac.archive_store);
							}
							if (winRoot.startsWith(sac.win_root)) {
								if ((winRoot.replace(sac.win_root, "")
										.startsWith("\\"))) {
									ConfigureArchiveStores.cat
											.errorT(
													loc,
													"DEFINE_ARCHIVE_STORES: Windows root "
															+ winRoot
															+ " is a child node of Windows root "
															+ sac.win_root
															+ " that already exists in archive store "
															+ sac.archive_store);
									throw new Exception(
											"DEFINE_ARCHIVE_STORES: Windows root "
													+ winRoot
													+ " is a child node of Windows root "
													+ sac.win_root
													+ " that already exists in archive store "
													+ sac.archive_store);
								}
							}
							if (sac.win_root.startsWith(winRoot)) {
								if ((sac.win_root.replace(winRoot, "")
										.startsWith("\\"))) {
									ConfigureArchiveStores.cat
											.errorT(
													loc,
													"DEFINE_ARCHIVE_STORES: Windows root "
															+ winRoot
															+ " is a parent node of Windows root "
															+ sac.win_root
															+ " that already exists in archive store "
															+ sac.archive_store);
									throw new Exception(
											"DEFINE_ARCHIVE_STORES: Windows root "
													+ winRoot
													+ " is a parent node of Windows root "
													+ sac.win_root
													+ " that already exists in archive store "
													+ sac.archive_store);
								}
							}
						}
						if ((unixRoot != null) && (sac.unix_root != null)) {
							if (unixRoot.equals(sac.unix_root)) {
								ConfigureArchiveStores.cat
										.errorT(
												loc,
												"DEFINE_ARCHIVE_STORES: Unix root "
														+ unixRoot
														+ " already exists in archive store "
														+ sac.archive_store);
								throw new Exception(
										"DEFINE_ARCHIVE_STORES: Unix root "
												+ unixRoot
												+ " already exists in archive store "
												+ sac.archive_store);
							}
							if (unixRoot.startsWith(sac.unix_root)) {
								if ((unixRoot.replace(sac.unix_root, "")
										.startsWith("/"))) {
									ConfigureArchiveStores.cat
											.errorT(
													loc,
													"DEFINE_ARCHIVE_STORES: Unix root "
															+ unixRoot
															+ " is a child node of Unix root "
															+ sac.unix_root
															+ " that already exists in archive store "
															+ sac.archive_store);
									throw new Exception(
											"DEFINE_ARCHIVE_STORES: Unix root "
													+ unixRoot
													+ " is a child node of Unix root "
													+ sac.unix_root
													+ " that already exists in archive store "
													+ sac.archive_store);
								}
							}
							if (sac.unix_root.startsWith(unixRoot)) {
								if ((sac.unix_root.replace(unixRoot, "")
										.startsWith("/"))) {
									ConfigureArchiveStores.cat
											.errorT(
													loc,
													"DEFINE_ARCHIVE_STORES: Unix root "
															+ unixRoot
															+ " is a parent node of Unix root "
															+ sac.unix_root
															+ " that already exists in archive store "
															+ sac.archive_store);
									throw new Exception(
											"DEFINE_ARCHIVE_STORES: Unix root "
													+ unixRoot
													+ " is a parent node of Unix root "
													+ sac.unix_root
													+ " that already exists in archive store "
													+ sac.archive_store);
								}
							}
						}
					}
				} catch (FinderException fex) {
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: " + fex.getMessage());
					throw new Exception("DEFINE_ARCHIVE_STORES: "
							+ fex.getMessage());
				}
			}

			// Check If Archive Store Is Running
			if (type.toUpperCase().startsWith("W")) {
				int optionsStatusCode = 0;
				String optionsReasonPhrase = "";
				boolean optionsSupportsDAV = false;
				int headStatusCode = 0;
				String headReasonPhrase = "";
				int propfindStatusCode = 0;
				String propfindReasonPhrase = "";
				int propfindCountResources = 0;
				try {

					// At This Time The New Archive Store Is Not Accessible From
					// HTTP Connection Pool
					DestinationService destService = (DestinationService) new InitialContext()
							.lookup(DestinationService.JNDI_KEY);
					if (destService == null)
						throw new NamingException(
								"Destination Service is not available");
					HTTPDestination httpDest = (HTTPDestination) destService
							.getDestination("HTTP", destination);
					IConnectionTemplate connTemplate = httpDest
							.getHTTPConnectionTemplate();
					com.tssap.dtr.client.lib.protocol.Connection conn = new com.tssap.dtr.client.lib.protocol.Connection(
							connTemplate);
					if (proxyHost == null || proxyHost.length() == 0) {
						conn.setUseProxy(false);
					} else {
						conn.setProxyHost(proxyHost);
						conn.setProxyPort(proxyPortint);
						conn.setUseProxy(true);
					}
					conn
							.setSocketReadTimeout(WebDavTemplateProvider.WEBDAVREADTIMEOUT);
					conn
							.setSocketConnectTimeout(WebDavTemplateProvider.WEBDAVCONNECTTIMEOUT);
					conn
							.setSocketExpirationTimeout(WebDavTemplateProvider.WEBDAVEXPIRATIONTIMEOUT);

					// OPTIONS request
					OptionsRequest optionsRequest = new OptionsRequest("");
					IResponse optionsResponse = optionsRequest.perform(conn);
					optionsSupportsDAV = optionsRequest.supportsDAV();
					optionsStatusCode = optionsResponse.getStatus();
					optionsReasonPhrase = optionsResponse
							.getStatusDescription();

					// Get ILM Conformance Class From Archive Store
					Header header = optionsResponse
							.getHeader("SAP-ILM-Conformance");
					if (header != null) {
						String ilmValue = header.getValue();
						if (ilmValue != null && ilmValue.length() > 0) {
							ilm_conformance_class = Short.parseShort(ilmValue);
						} else {
							ilm_conformance_class = 0;
						}

					} else {
						ilm_conformance_class = 0;
					}

					// HEAD Request
					HeadRequest headRequest = new HeadRequest("");
					IResponse headResponse = headRequest.perform(conn);
					headStatusCode = headResponse.getStatus();
					headReasonPhrase = headResponse.getStatusDescription();

					// PROPFIND Request
					PropfindRequest propfindRequest = new PropfindRequest("");
					propfindRequest.setOption(PropfindOption.ALL_PROPERTIES);
					propfindRequest.setDepth(Depth.DEPTH_1);
					IResponse propfindResponse = propfindRequest.perform(conn);
					propfindCountResources = propfindRequest.countResources();
					propfindStatusCode = propfindResponse.getStatus();
					propfindReasonPhrase = propfindResponse
							.getStatusDescription();

					conn.close();
				} catch (Exception ex) {
					ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR,
							loc,
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore + " failed. Reason: "
									+ ex.getMessage() + ".", ex);
					throw new IOException(
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore + " failed. Reason: "
									+ ex.getMessage() + ".");
				}

				// Evaluate If Check Was Successful
				if ((optionsStatusCode != 200) || (optionsSupportsDAV == false)) {
					if (optionsSupportsDAV == false) {
						ConfigureArchiveStores.cat
								.errorT(
										loc,
										"DEFINE_ARCHIVE_STORES: Testing the archive store "
												+ archiveStore
												+ " failed. Reason: The response of the OPTIONS request is "
												+ optionsStatusCode
												+ " "
												+ optionsReasonPhrase
												+ ". The WebDAV protocol is not supported.");
						throw new IOException(
								"DEFINE_ARCHIVE_STORES: Testing the archive store "
										+ archiveStore
										+ " failed. Reason: The response of the OPTIONS request is "
										+ optionsStatusCode
										+ " "
										+ optionsReasonPhrase
										+ ". The WebDAV protocol is not supported.");
					} else {
						ConfigureArchiveStores.cat
								.errorT(
										loc,
										"DEFINE_ARCHIVE_STORES: Testing the archive store "
												+ archiveStore
												+ " failed. Reason: The response of the OPTIONS request is "
												+ optionsStatusCode + " "
												+ optionsReasonPhrase + ".");
						throw new IOException(
								"DEFINE_ARCHIVE_STORES: Testing the archive store "
										+ archiveStore
										+ " failed. Reason: The response of the OPTIONS request is "
										+ optionsStatusCode + " "
										+ optionsReasonPhrase + ".");
					}
				}
				if (headStatusCode != 200) {
					ConfigureArchiveStores.cat
							.errorT(
									loc,
									"DEFINE_ARCHIVE_STORES: Testing the archive store "
											+ archiveStore
											+ " failed. Reason: The response of the HEAD request is "
											+ headStatusCode + " "
											+ headReasonPhrase + ".");
					throw new IOException(
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore
									+ " failed. Reason: The response of the HEAD request is "
									+ headStatusCode + " " + headReasonPhrase
									+ ".");
				}
				if (((propfindStatusCode != 207))
						|| (propfindCountResources != 1)) {
					if (propfindCountResources != 1) {
						ConfigureArchiveStores.cat
								.errorT(
										loc,
										"DEFINE_ARCHIVE_STORES: Testing the archive store "
												+ archiveStore
												+ " failed. Reason: The response of the PROPFIND request is "
												+ propfindStatusCode
												+ " "
												+ propfindReasonPhrase
												+ ". The WebDAV root collection is not empty.");
						throw new IOException(
								"DEFINE_ARCHIVE_STORES: Testing the archive store "
										+ archiveStore
										+ " failed. Reason: The response of the PROPFIND request is "
										+ propfindStatusCode
										+ " "
										+ propfindReasonPhrase
										+ ". The WebDAV root collection is not empty.");
					} else {
						ConfigureArchiveStores.cat
								.errorT(
										loc,
										"DEFINE_ARCHIVE_STORES: Testing the archive store "
												+ archiveStore
												+ " failed. Reason: The response of the PROPFIND request is "
												+ propfindStatusCode + " "
												+ propfindReasonPhrase + ".");
						throw new IOException(
								"DEFINE_ARCHIVE_STORES: Testing the archive store "
										+ archiveStore
										+ " failed. Reason: The response of the PROPFIND request is "
										+ propfindStatusCode + " "
										+ propfindReasonPhrase + ".");
					}
				}
			} else {
				String archiveRoot = "";

				// Unix Operation System
				if (System.getProperty("file.separator").startsWith("/")) {
					if (unixRoot.contains("<DIR_GLOBAL>"))
						archiveRoot = unixRoot.replace("<DIR_GLOBAL>",
								MasterMethod.GLOBAL_DIR);
					else
						archiveRoot = unixRoot;
				}

				// Windows Operating System
				else {
					if (winRoot.contains("<DIR_GLOBAL>"))
						archiveRoot = winRoot.replace("<DIR_GLOBAL>",
								MasterMethod.GLOBAL_DIR);
					else
						archiveRoot = winRoot;
				}

				boolean isDirectory;
				boolean canRead = false;
				boolean canWrite = false;
				boolean hasChildren = false;
				File f = null;
				try {
					f = new File(archiveRoot);
					isDirectory = f.isDirectory();
					canRead = f.canRead();
					canWrite = f.canWrite();
					File[] fl = f.listFiles();
					if (fl != null) {
						if (fl.length == 0)
							hasChildren = false;
						else
							hasChildren = true;
					}
				} catch (Exception ex) {
					ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR,
							loc,
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore + " failed. Reason: "
									+ ex.getMessage() + ".", ex);
					throw new IOException(
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore + " failed. Reason: "
									+ ex.getMessage() + ".");
				}

				// Evaluate If Check Was Successful
				if (isDirectory == false) {
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore
									+ " failed. Reason: The directory "
									+ f.getName() + " does not exist or "
									+ f.getName() + " is not a directory.");
					throw new IOException(
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore
									+ " failed. Reason: The directory "
									+ f.getName() + " does not exist or "
									+ f.getName() + " is not a directory.");
				}
				if (canRead == false) {
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore
									+ " failed. Reason: The directory "
									+ f.getName() + " is not readable.");
					throw new IOException(
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore
									+ " failed. Reason: The directory "
									+ f.getName() + " is not readable.");
				}
				if (canWrite == false) {
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore
									+ " failed. Reason: The directory "
									+ f.getName() + " is not writable.");
					throw new IOException(
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore
									+ " failed. Reason: The directory "
									+ f.getName() + " is not writable.");
				}
				if (hasChildren == true) {
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: Testing archive store "
									+ archiveStore
									+ " failed. Reason: The directory "
									+ f.getName() + " is not empty.");
					throw new IOException(
							"DEFINE_ARCHIVE_STORES: Testing archive store "
									+ archiveStore
									+ " failed. Reason: The directory "
									+ f.getName() + " is not empty.");
				}
			}

			// Get Archive Store Id
			pst1 = connection.prepareStatement(INS_SEL_MAX_TAB);
			result = pst1.executeQuery();
			hits = 0;
			while (result.next()) {
				storeId = result.getLong("MAXID");
				hits++;
			}
			result.close();
			pst1.close();
			if (hits == 0) {
				ConfigureArchiveStores.cat
						.errorT(loc,
								"DEFINE_ARCHIVE_STORES: Missing Entry BC_XMLA_CONFIG In Table BC_XMLA_MAXIDS");
				throw new Exception(
						"DEFINE_ARCHIVE_STORES: Missing Entry BC_XMLA_CONFIG In Table BC_XMLA_MAXIDS");
			}

			// Increase Archive Store Id
			storeId++;
			pst2 = connection.prepareStatement(INS_UPD_MAX_TAB);
			pst2.setLong(1, storeId);
			pst2.executeUpdate();
			pst2.close();

			// Reset Currently Default Archive Store
			if (is_default.equalsIgnoreCase("Y")) {
				try {
					this.resetExistingDefaultArchiveStore();
				} catch (FinderException fex) {
					MasterMethod.isSLDoutOfSync = true;

					// $JL-EXC$
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: FinderException: "
									+ fex.toString());
				} catch (NamingException nex) {
					MasterMethod.isSLDoutOfSync = true;

					// $JL-EXC$
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: NamingException: "
									+ nex.toString());
				} catch (CIMException cimex) {
					MasterMethod.isSLDoutOfSync = true;

					// $JL-EXC$
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: CIMException: "
									+ cimex.toString());
				} catch (InvalidDataException idex) {
					MasterMethod.isSLDoutOfSync = true;

					// $JL-EXC$
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: InvalidDataException: "
									+ idex.toString());
				}
			}

			// Insert Archive Store Entry
			if (winRoot.length() == 0)
				winRoot = null;
			if (unixRoot.length() == 0)
				unixRoot = null;
			if (destination.length() == 0)
				destination = null;
			if (proxyHost.length() == 0)
				proxyHost = null;

			if (type.toUpperCase().startsWith("W")) {
				winRoot = null;
				unixRoot = null;
			} else {
				destination = null;
				proxyHost = null;
				proxyPortint = 0;
			}

			Context ctx = new InitialContext();
			ArchStoreConfigLocalHome beanLocalHome = (ArchStoreConfigLocalHome) ctx
					.lookup("java:comp/env/ArchStoreConfigBean");
			beanLocalHome.create(new Long(storeId), archiveStore,
					storageSystem, type, winRoot, unixRoot, destination,
					proxyHost, proxyPortint, ilm_conformance_class, is_default);

			// Insert SLD Archive Store Instance And Xml Archive Server Instance
			try {
				String engineName = "xmldas.SystemName."
						+ SystemExplorer.getJ2EEClusterEngineName();
				String[] enginePropValues = new String[8];
				enginePropValues[0] = "XML Data Archiving Server";
				enginePropValues[1] = "tc/TechSrv/XML_DAS";
				enginePropValues[2] = "J2EE";
				enginePropValues[3] = "XML Data Archiving Server";
				enginePropValues[4] = "SAP standard";
				enginePropValues[5] = "SAP_J2EEEngineCluster";
				enginePropValues[6] = SystemExplorer.getJ2EEClusterEngineName();
				String reqURL = request.getRequestURL().toString();
				String refContextPath1 = "/DataArchivingService/";
				String refContextPath2 = "DAS";
				if ((reqURL != null) && (reqURL.length() != 0)) {
					int position = reqURL.lastIndexOf(refContextPath1);
					if (position != 0) {
						position += refContextPath1.length();
						reqURL = reqURL.substring(0, position)
								+ refContextPath2;
					}
				} else {
					reqURL = "";
				}
				enginePropValues[7] = reqURL;
				String archiveName = archiveStore
						+ ".XMLDASName.xmldas.SystemName."
						+ SystemExplorer.getJ2EEClusterEngineName();
				String[] archivePropValues = new String[12];
				archivePropValues[0] = storageSystem;
				archivePropValues[1] = archiveStore;
				archivePropValues[2] = storageSystem;
				archivePropValues[3] = destination;
				archivePropValues[4] = "SAP standard";
				if (type.equalsIgnoreCase("W")) {
					archivePropValues[5] = "WebDAV";
					archivePropValues[6] = "";
					try {
						archivePropValues[7] = getWebDavRoot(destination);
					} catch (Exception ex) {

						// $JL-EXC$
						archivePropValues[7] = "";
					}
					archivePropValues[8] = "";
				} else {
					archivePropValues[5] = "Filesystem";
					archivePropValues[6] = unixRoot;
					archivePropValues[7] = "";
					archivePropValues[8] = winRoot;
				}
				archivePropValues[9] = "xmldas.SystemName."
						+ SystemExplorer.getJ2EEClusterEngineName();
				archivePropValues[10] = Short.toString(ilm_conformance_class);
				if (is_default.equalsIgnoreCase("Y"))
					archivePropValues[11] = "true";
				else
					archivePropValues[11] = "false";
				Director director = new Director();

				director.insertSldInstance(engineName, enginePropValues,
						archiveName, archivePropValues);
			} catch (NamingException nex) {
				MasterMethod.isSLDoutOfSync = true;

				// $JL-EXC$
				ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR, loc,
						"DEFINE_ARCHIVE_STORES: NamingException: "
								+ nex.getMessage(), nex);
			} catch (CIMException cimex) {
				MasterMethod.isSLDoutOfSync = true;

				// $JL-EXC$
				ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR, loc,
						"DEFINE_ARCHIVE_STORES: CIMException: "
								+ cimex.getMessage(), cimex);
			} catch (InvalidDataException idex) {
				MasterMethod.isSLDoutOfSync = true;

				// $JL-EXC$
				ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR, loc,
						"DEFINE_ARCHIVE_STORES: InvalidDataException: "
								+ idex.getMessage(), idex);
			} catch (Exception ex) {
				MasterMethod.isSLDoutOfSync = true;

				// $JL-EXC$
				ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR, loc,
						"DEFINE_ARCHIVE_STORES: Exception: " + ex.getMessage(),
						ex);
			}

			// Write Successful Archive Store Creation Message Into Log
			ConfigureArchiveStores.cat.infoT(loc,
					"DEFINE_ARCHIVE_STORES: Archive store " + archiveStore
							+ " successfully created");

			// Commit Database
			connection.commit();

		} // end try
		catch (SQLException sqlex) {

			// Write SQLException Into Log
			ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR, loc,
					"DEFINE_ARCHIVE_STORES: SQLException: "
							+ sqlex.getMessage(), sqlex);

			// Roll Back Database
			connection.rollback();

			// Forward SQLException
			throw sqlex;
		} catch (Exception ex) {

			// Write Exception Into Log
			ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR, loc,
					"DEFINE_ARCHIVE_STORES: Exception: " + ex.getMessage(), ex);

			// Roll Back Database
			connection.rollback();

			// Forward Exception
			throw ex;
		} finally {

			try {

				// Close Prepared Statements
				if (pst1 != null)
					pst1.close();
				if (pst2 != null)
					pst2.close();

				// Close DB Connection
				if (connection != null)
					connection.close();

			} catch (SQLException sqlex) {

				// Write Exception Into Log
				ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR, loc,
						"DEFINE_ARCHIVE_STORES: SQLException: "
								+ sqlex.getMessage(), sqlex);

				// Forward Exception
				throw sqlex;
			}
		}
	}

	public void update() throws Exception {

		long storeId = Long.parseLong(request.getParameter("storeid"));
		String archiveStore = request.getParameter("archivestore").trim();
		String storageSystem = request.getParameter("storagesystem").trim();
		if (storageSystem.length() < 1) // When Empty Set A Blank Character
			storageSystem = " ";
		String type = request.getParameter("type");
		String winRoot = request.getParameter("winroot").trim();
		String unixRoot = request.getParameter("unixroot").trim();
		String destination = request.getParameter("destination").trim();
		String proxyHost = request.getParameter("proxyhost").trim();
		String proxyPort = request.getParameter("proxyport").trim();
		int proxyPortint;
		if (proxyPort.length() == 0)
			proxyPortint = 0;
		else
			proxyPortint = Integer.parseInt(proxyPort);
		short ilm_conformance_class = 0;
		String is_default = request.getParameter("isdefault").trim();
		if ((is_default == null) || (is_default.length() == 0))
			is_default = "N";
		try {

			// Check If There Is A Conflict With Already Existing Archive Stores
			if (type.equalsIgnoreCase("W"))// WebDAV System
			{
				try {
					Context ctx = new InitialContext();
					ArchStoreConfigLocalHome beanLocalHome = (ArchStoreConfigLocalHome) ctx
							.lookup("java:comp/env/ArchStoreConfigBean");
					Collection<ArchStoreConfigLocal> c = beanLocalHome
							.findAll();
					for (Iterator<ArchStoreConfigLocal> iter = c.iterator(); iter
							.hasNext();) {
						ArchStoreConfigLocal element = iter.next();
						Sapxmla_Config sac = element.getSapxmla_Config();
						if (!archiveStore.equalsIgnoreCase(sac.archive_store)) {

							// Note: There Is Not Check For Non Destination
							// Archive Stores
							if ((destination != null)
									&& ((sac.destination != null) && (sac.destination
											.trim().length() != 0))) {
								if (destination.equals(sac.destination)) {
									ConfigureArchiveStores.cat
											.errorT(
													loc,
													"DEFINE_ARCHIVE_STORES: Destination "
															+ destination
															+ " already exists in archive store "
															+ sac.archive_store);
									throw new Exception(
											"DEFINE_ARCHIVE_STORES: Destination "
													+ destination
													+ " already exists in archive store "
													+ sac.archive_store);
								}
								DestinationService destService = (DestinationService) new InitialContext()
										.lookup(DestinationService.JNDI_KEY);
								if (destService == null) {
									ConfigureArchiveStores.cat
											.errorT(loc,
													"DEFINE_ARCHIVE_STORES: Destination Service is not available");
									throw new Exception(
											"DEFINE_ARCHIVE_STORES: Destination Service is not available");
								}
								HTTPDestination thisDest = (HTTPDestination) destService
										.getDestination("HTTP", destination);
								HTTPDestination sacDest = (HTTPDestination) destService
										.getDestination("HTTP", sac.destination);
								if ((thisDest == null) || (sacDest == null)) {
									if (thisDest == null) {
										ConfigureArchiveStores.cat
												.errorT(
														loc,
														"DEFINE_ARCHIVE_STORES: Destination "
																+ destination
																+ " not found in Destination Service");
										throw new Exception(
												"DEFINE_ARCHIVE_STORES: Destination "
														+ destination
														+ " not found in Destination Service");
									} else {
										ConfigureArchiveStores.cat
												.errorT(
														loc,
														"DEFINE_ARCHIVE_STORES: Destination "
																+ sac.destination
																+ " not found in Destination Service");
										throw new Exception(
												"DEFINE_ARCHIVE_STORES: Destination "
														+ sac.destination
														+ " not found in Destination Service");
									}
								}
								String thisURLString = thisDest.getUrl();
								String sacURLString = sacDest.getUrl();
								if ((thisURLString == null)
										|| (sacURLString == null)) {
									if (thisURLString == null) {
										ConfigureArchiveStores.cat
												.errorT(
														loc,
														"DEFINE_ARCHIVE_STORES: No URL for destination "
																+ destination
																+ " found in Destination Service");
										throw new Exception(
												"DEFINE_ARCHIVE_STORES: No URL for destination "
														+ destination
														+ " found in Destination Service");
									} else {
										ConfigureArchiveStores.cat
												.errorT(
														loc,
														"DEFINE_ARCHIVE_STORES: No URL for destination "
																+ sac.destination
																+ " found in Destination Service");
										throw new Exception(
												"DEFINE_ARCHIVE_STORES: No URL for destination "
														+ sac.destination
														+ " found in Destination Service");
									}
								}
								URL thisURL = new URL(thisURLString);
								URL sacURL = new URL(sacURLString);
								InetAddress thisInet = Inet6Address
										.getByName(thisURL.getHost());
								InetAddress sacInet = Inet6Address
										.getByName(sacURL.getHost());
								String thisNormalizedURLString = thisURL
										.getProtocol()
										+ "://"
										+ thisInet.getHostAddress()
										+ ":"
										+ thisURL.getPort()
										+ thisURL.getPath();
								String sacNormalizedURLString = sacURL
										.getProtocol()
										+ "://"
										+ sacInet.getHostAddress()
										+ ":"
										+ sacURL.getPort()
										+ sacURL.getPath();
								if (thisNormalizedURLString
										.equals(sacNormalizedURLString)) {
									ConfigureArchiveStores.cat
											.errorT(
													loc,
													"DEFINE_ARCHIVE_STORES: URL "
															+ thisNormalizedURLString
															+ " of destination "
															+ destination
															+ " already exists in destination "
															+ sac.destination
															+ " of archive store "
															+ sac.archive_store);
									throw new Exception(
											"DEFINE_ARCHIVE_STORES: URL "
													+ thisNormalizedURLString
													+ " of destination "
													+ destination
													+ " already exists in destination "
													+ sac.destination
													+ " of archive store "
													+ sac.archive_store);
								}
								if (thisNormalizedURLString
										.startsWith(sacNormalizedURLString)) {
									if ((thisNormalizedURLString.replace(
											sacNormalizedURLString, "")
											.startsWith("/"))) {
										ConfigureArchiveStores.cat
												.errorT(
														loc,
														"DEFINE_ARCHIVE_STORES: URL "
																+ thisNormalizedURLString
																+ " of destination "
																+ destination
																+ " is a child node of URL "
																+ sacNormalizedURLString
																+ " of destination "
																+ sac.destination
																+ " that already exists in archive store "
																+ sac.archive_store);
										throw new Exception(
												"DEFINE_ARCHIVE_STORES: URL "
														+ thisNormalizedURLString
														+ " of destination "
														+ destination
														+ " is a child node of URL "
														+ sacNormalizedURLString
														+ " of destination "
														+ sac.destination
														+ " that already exists in archive store "
														+ sac.archive_store);
									}
								}
								if (sacNormalizedURLString
										.startsWith(thisNormalizedURLString)) {
									if ((sacNormalizedURLString.replace(
											thisNormalizedURLString, "")
											.startsWith("/"))) {
										ConfigureArchiveStores.cat
												.errorT(
														loc,
														"DEFINE_ARCHIVE_STORES: URL "
																+ thisNormalizedURLString
																+ " of destination "
																+ destination
																+ " is a parent node of URL "
																+ sacNormalizedURLString
																+ " of destination "
																+ sac.destination
																+ " that already exists in archive store "
																+ sac.archive_store);
										throw new Exception(
												"DEFINE_ARCHIVE_STORES: URL "
														+ thisNormalizedURLString
														+ " of destination "
														+ destination
														+ " is a parent node of URL "
														+ sacNormalizedURLString
														+ " of destination "
														+ sac.destination
														+ " that already exists in archive store "
														+ sac.archive_store);
									}
								}
							}
						}
					}
				} catch (FinderException fex) {
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: " + fex.getMessage());
					throw new Exception("DEFINE_ARCHIVE_STORES: "
							+ fex.getMessage());
				} catch (NamingException nex) {
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: " + nex.toString());
					throw new Exception("DEFINE_ARCHIVE_STORES: "
							+ nex.toString());
				} catch (DestinationException dex) {
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: " + dex.toString());
					throw new Exception("DEFINE_ARCHIVE_STORES: "
							+ dex.toString());
				}
			} else// File System
			{
				try {
					Context ctx = new InitialContext();
					ArchStoreConfigLocalHome beanLocalHome = (ArchStoreConfigLocalHome) ctx
							.lookup("java:comp/env/ArchStoreConfigBean");
					Collection<ArchStoreConfigLocal> c = beanLocalHome
							.findAll();
					for (Iterator<ArchStoreConfigLocal> iter = c.iterator(); iter
							.hasNext();) {
						ArchStoreConfigLocal element = iter.next();
						Sapxmla_Config sac = element.getSapxmla_Config();
						if (!archiveStore.equalsIgnoreCase(sac.archive_store)) {
							if ((winRoot != null) && (sac.win_root != null)) {
								if (winRoot.equals(sac.win_root)) {
									ConfigureArchiveStores.cat
											.errorT(
													loc,
													"DEFINE_ARCHIVE_STORES: Windows root "
															+ winRoot
															+ " already exists in archive store "
															+ sac.archive_store);
									throw new Exception(
											"DEFINE_ARCHIVE_STORES: Windows root "
													+ winRoot
													+ " already exists in archive store "
													+ sac.archive_store);
								}
								if (winRoot.startsWith(sac.win_root)) {
									if ((winRoot.replace(sac.win_root, "")
											.startsWith("\\"))) {
										ConfigureArchiveStores.cat
												.errorT(
														loc,
														"DEFINE_ARCHIVE_STORES: Windows root "
																+ winRoot
																+ " is a child node of Windows root "
																+ sac.win_root
																+ " that already exists in archive store "
																+ sac.archive_store);
										throw new Exception(
												"DEFINE_ARCHIVE_STORES: Windows root "
														+ winRoot
														+ " is a child node of Windows root "
														+ sac.win_root
														+ " that already exists in archive store "
														+ sac.archive_store);
									}
								}
								if (sac.win_root.startsWith(winRoot)) {
									if ((sac.win_root.replace(winRoot, "")
											.startsWith("\\"))) {
										ConfigureArchiveStores.cat
												.errorT(
														loc,
														"DEFINE_ARCHIVE_STORES: Windows root "
																+ winRoot
																+ " is a parent node of Windows root "
																+ sac.win_root
																+ " that already exists in archive store "
																+ sac.archive_store);
										throw new Exception(
												"DEFINE_ARCHIVE_STORES: Windows root "
														+ winRoot
														+ " is a parent node of Windows root "
														+ sac.win_root
														+ " that already exists in archive store "
														+ sac.archive_store);
									}
								}
							}
							if ((unixRoot != null) && (sac.unix_root != null)) {
								if (unixRoot.equals(sac.unix_root)) {
									ConfigureArchiveStores.cat
											.errorT(
													loc,
													"DEFINE_ARCHIVE_STORES: Unix root "
															+ unixRoot
															+ " already exists in archive store "
															+ sac.archive_store);
									throw new Exception(
											"DEFINE_ARCHIVE_STORES: Unix root "
													+ unixRoot
													+ " already exists in archive store "
													+ sac.archive_store);
								}
								if (unixRoot.startsWith(sac.unix_root)) {
									if ((unixRoot.replace(sac.unix_root, "")
											.startsWith("/"))) {
										ConfigureArchiveStores.cat
												.errorT(
														loc,
														"DEFINE_ARCHIVE_STORES: Unix root "
																+ unixRoot
																+ " is a child node of Unix root "
																+ sac.unix_root
																+ " that already exists in archive store "
																+ sac.archive_store);
										throw new Exception(
												"DEFINE_ARCHIVE_STORES: Unix root "
														+ unixRoot
														+ " is a child node of Unix root "
														+ sac.unix_root
														+ " that already exists in archive store "
														+ sac.archive_store);
									}
								}
								if (sac.unix_root.startsWith(unixRoot)) {
									if ((sac.unix_root.replace(unixRoot, "")
											.startsWith("/"))) {
										ConfigureArchiveStores.cat
												.errorT(
														loc,
														"DEFINE_ARCHIVE_STORES: Unix root "
																+ unixRoot
																+ " is a parent node of Unix root "
																+ sac.unix_root
																+ " that already exists in archive store "
																+ sac.archive_store);
										throw new Exception(
												"DEFINE_ARCHIVE_STORES: Unix root "
														+ unixRoot
														+ " is a parent node of Unix root "
														+ sac.unix_root
														+ " that already exists in archive store "
														+ sac.archive_store);
									}
								}
							}
						}
					}
				} catch (FinderException fex) {
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: " + fex.getMessage());
					throw new Exception("DEFINE_ARCHIVE_STORES: "
							+ fex.getMessage());
				}
			}

			// Check If Archive Store Is Running
			if (type.toUpperCase().startsWith("W")) {
				int optionsStatusCode = 0;
				String optionsReasonPhrase = "";
				boolean optionsSupportsDAV = false;
				int headStatusCode = 0;
				String headReasonPhrase = "";
				try {

					// At This Time The New Archive Store Is Not Accessible From
					// HTTP Connection Pool
					DestinationService destService = (DestinationService) new InitialContext()
							.lookup(DestinationService.JNDI_KEY);
					if (destService == null)
						throw new NamingException(
								"Destination Service is not available");
					HTTPDestination httpDest = (HTTPDestination) destService
							.getDestination("HTTP", destination);
					IConnectionTemplate connTemplate = httpDest
							.getHTTPConnectionTemplate();
					com.tssap.dtr.client.lib.protocol.Connection conn = new com.tssap.dtr.client.lib.protocol.Connection(
							connTemplate);
					if (proxyHost == null || proxyHost.length() == 0) {
						conn.setUseProxy(false);
					} else {
						conn.setProxyHost(proxyHost);
						conn.setProxyPort(proxyPortint);
						conn.setUseProxy(true);
					}
					conn
							.setSocketReadTimeout(WebDavTemplateProvider.WEBDAVREADTIMEOUT);
					conn
							.setSocketConnectTimeout(WebDavTemplateProvider.WEBDAVCONNECTTIMEOUT);
					conn
							.setSocketExpirationTimeout(WebDavTemplateProvider.WEBDAVEXPIRATIONTIMEOUT);

					// OPTIONS request
					OptionsRequest optionsRequest = new OptionsRequest("");
					IResponse optionsResponse = optionsRequest.perform(conn);
					optionsSupportsDAV = optionsRequest.supportsDAV();
					optionsStatusCode = optionsResponse.getStatus();
					optionsReasonPhrase = optionsResponse
							.getStatusDescription();

					// Get ILM Conformance Class From Archive Store
					Header header = optionsResponse
							.getHeader("SAP-ILM-Conformance");
					if (header != null) {
						String ilmValue = header.getValue();
						if (ilmValue != null && ilmValue.length() > 0) {
							ilm_conformance_class = Short.parseShort(ilmValue);
						} else {
							ilm_conformance_class = 0;
						}

					} else {
						ilm_conformance_class = 0;
					}

					// HEAD Request
					HeadRequest headRequest = new HeadRequest("");
					IResponse headResponse = headRequest.perform(conn);
					headStatusCode = headResponse.getStatus();
					headReasonPhrase = headResponse.getStatusDescription();

					conn.close();
				} catch (Exception ex) {
					ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR,
							loc,
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore + " failed. Reason: "
									+ ex.getMessage() + ".", ex);
					throw new IOException(
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore + " failed. Reason: "
									+ ex.getMessage() + ".");
				}

				// Evaluate If Check Was Successful
				if ((optionsStatusCode != 200) || (optionsSupportsDAV == false)) {
					if (optionsSupportsDAV == false) {
						ConfigureArchiveStores.cat
								.errorT(
										loc,
										"DEFINE_ARCHIVE_STORES: Testing the archive store "
												+ archiveStore
												+ " failed. Reason: The response of the OPTIONS request is "
												+ optionsStatusCode
												+ " "
												+ optionsReasonPhrase
												+ ". The WebDAV protocol is not supported.");
						throw new IOException(
								"DEFINE_ARCHIVE_STORES: Testing the archive store "
										+ archiveStore
										+ " failed. Reason: The response of the OPTIONS request is "
										+ optionsStatusCode
										+ " "
										+ optionsReasonPhrase
										+ ". The WebDAV protocol is not supported.");
					} else {
						ConfigureArchiveStores.cat
								.errorT(
										loc,
										"DEFINE_ARCHIVE_STORES: Testing the archive store "
												+ archiveStore
												+ " failed. Reason: The response of the OPTIONS request is "
												+ optionsStatusCode + " "
												+ optionsReasonPhrase + ".");
						throw new IOException(
								"DEFINE_ARCHIVE_STORES: Testing the archive store "
										+ archiveStore
										+ " failed. Reason: The response of the OPTIONS request is "
										+ optionsStatusCode + " "
										+ optionsReasonPhrase + ".");
					}
				}
				if (headStatusCode != 200) {
					ConfigureArchiveStores.cat
							.errorT(
									loc,
									"DEFINE_ARCHIVE_STORES: Testing the archive store "
											+ archiveStore
											+ " failed. Reason: The response of the HEAD request is "
											+ headStatusCode + " "
											+ headReasonPhrase + ".");
					throw new IOException(
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore
									+ " failed. Reason: The response of the HEAD request is "
									+ headStatusCode + " " + headReasonPhrase
									+ ".");
				}
			} else {
				String archiveRoot = "";

				// Unix Operation System
				if (System.getProperty("file.separator").startsWith("/")) {
					if (unixRoot.contains("<DIR_GLOBAL>"))
						archiveRoot = unixRoot.replace("<DIR_GLOBAL>",
								MasterMethod.GLOBAL_DIR);
					else
						archiveRoot = unixRoot;
				}

				// Windows Operating System
				else {
					if (winRoot.contains("<DIR_GLOBAL>"))
						archiveRoot = winRoot.replace("<DIR_GLOBAL>",
								MasterMethod.GLOBAL_DIR);
					else
						archiveRoot = winRoot;
				}

				boolean isDirectory;
				boolean canRead = false;
				boolean canWrite = false;
				File f = null;
				try {
					f = new File(archiveRoot);
					isDirectory = f.isDirectory();
					canRead = f.canRead();
					canWrite = f.canWrite();
				} catch (Exception ex) {
					ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR,
							loc,
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore + " failed. Reason: "
									+ ex.getMessage() + ".", ex);
					throw new IOException(
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore + " failed. Reason: "
									+ ex.getMessage() + ".");
				}

				// Evaluate If Check Was Successful
				if (isDirectory == false) {
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore
									+ " failed. Reason: The directory "
									+ f.getName() + " does not exist or "
									+ f.getName() + " is not a directory.");
					throw new IOException(
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore
									+ " failed. Reason: The directory "
									+ f.getName() + " does not exist or "
									+ f.getName() + " is not a directory.");
				}
				if (canRead == false) {
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore
									+ " failed. Reason: The directory "
									+ f.getName() + " is not readable.");
					throw new IOException(
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore
									+ " failed. Reason: The directory "
									+ f.getName() + " is not readable.");
				}
				if (canWrite == false) {
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore
									+ " failed. Reason: The directory "
									+ f.getName() + " is not writable.");
					throw new IOException(
							"DEFINE_ARCHIVE_STORES: Testing the archive store "
									+ archiveStore
									+ " failed. Reason: The directory "
									+ f.getName() + " is not writable.");
				}
			}

			// Reset Currently Default Archive Store
			if (is_default.equalsIgnoreCase("Y")) {
				try {
					this.resetExistingDefaultArchiveStore();
				} catch (FinderException fex) {
					MasterMethod.isSLDoutOfSync = true;

					// $JL-EXC$
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: FinderException: "
									+ fex.toString());
				} catch (NamingException nex) {
					MasterMethod.isSLDoutOfSync = true;

					// $JL-EXC$
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: NamingException: "
									+ nex.toString());
				} catch (CIMException cimex) {
					MasterMethod.isSLDoutOfSync = true;

					// $JL-EXC$
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: CIMException: "
									+ cimex.toString());
				} catch (InvalidDataException idex) {
					MasterMethod.isSLDoutOfSync = true;

					// $JL-EXC$
					ConfigureArchiveStores.cat.errorT(loc,
							"DEFINE_ARCHIVE_STORES: InvalidDataException: "
									+ idex.toString());
				}
			}

			// Update BC_XMLA_CONFIG Entry
			if (winRoot.length() == 0)
				winRoot = null;
			if (unixRoot.length() == 0)
				unixRoot = null;
			if (destination.length() == 0)
				destination = null;
			if (proxyHost.length() == 0)
				proxyHost = null;
			Context ctx = new InitialContext();
			ArchStoreConfigLocalHome beanLocalHome = (ArchStoreConfigLocalHome) ctx
					.lookup("java:comp/env/ArchStoreConfigBean");
			ArchStoreConfigLocal ascl = beanLocalHome
					.findByPrimaryKey(new Long(storeId));
			ascl.setStoragesystem(storageSystem);
			ascl.setWinroot(winRoot);
			ascl.setUnixroot(unixRoot);
			ascl.setDestination(destination);
			ascl.setProxyhost(proxyHost);
			ascl.setProxyport(proxyPortint);
			ascl.setIlmconform(ilm_conformance_class);
			ascl.setIsdefault(is_default);

			// Update SLD Archive Store Instance
			try {
				String archiveName = archiveStore
						+ ".XMLDASName.xmldas.SystemName."
						+ SystemExplorer.getJ2EEClusterEngineName();
				String[] archivePropValues = new String[12];
				archivePropValues[0] = storageSystem;
				archivePropValues[1] = archiveStore;
				archivePropValues[2] = storageSystem;
				if (destination == null)
					archivePropValues[3] = "";
				else
					archivePropValues[3] = destination;
				archivePropValues[4] = "SAP standard";
				if (type.equalsIgnoreCase("W")) {
					archivePropValues[5] = "WebDAV";
					archivePropValues[6] = "";
					if (destination == null)
						archivePropValues[7] = "";
					else {
						try {
							archivePropValues[7] = getWebDavRoot(destination);
						} catch (Exception ex) {

							// $JL-EXC$
							archivePropValues[7] = "";
						}
					}
					archivePropValues[8] = "";
				} else {
					archivePropValues[5] = "Filesystem";
					if (unixRoot == null)
						archivePropValues[6] = "";
					else
						archivePropValues[6] = unixRoot;
					archivePropValues[7] = "";
					if (winRoot == null)
						archivePropValues[8] = "";
					else
						archivePropValues[8] = winRoot;
				}
				archivePropValues[9] = "xmldas.SystemName."
						+ SystemExplorer.getJ2EEClusterEngineName();
				archivePropValues[10] = Short.toString(ilm_conformance_class);
				if (is_default.equalsIgnoreCase("Y"))
					archivePropValues[11] = "true";
				else
					archivePropValues[11] = "false";
				Director director = new Director();
				director.updateSldInstance(archiveName, archivePropValues);
			} catch (NamingException nex) {
				MasterMethod.isSLDoutOfSync = true;

				// $JL-EXC$
				ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR, loc,
						"DEFINE_ARCHIVE_STORES: NamingException: "
								+ nex.getMessage(), nex);
			} catch (CIMException cimex) {
				MasterMethod.isSLDoutOfSync = true;

				// $JL-EXC$
				ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR, loc,
						"DEFINE_ARCHIVE_STORES: CIMException: "
								+ cimex.getMessage(), cimex);
			} catch (InvalidDataException idex) {
				MasterMethod.isSLDoutOfSync = true;

				// $JL-EXC$
				ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR, loc,
						"DEFINE_ARCHIVE_STORES: InvalidDataException: "
								+ idex.getMessage(), idex);
			} catch (Exception ex) {
				MasterMethod.isSLDoutOfSync = true;

				// $JL-EXC$
				ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR, loc,
						"DEFINE_ARCHIVE_STORES: Exception: " + ex.getMessage(),
						ex);
			}

			// Write Successful Archive Store Update Message Into Log
			ConfigureArchiveStores.cat.infoT(loc,
					"DEFINE_ARCHIVE_STORES: Archive store " + archiveStore
							+ " successful updated. Storage System: "
							+ storageSystem + ", Windows Root: " + winRoot
							+ ", Unix Root: " + unixRoot + ", Destination: "
							+ destination + ", Proxy Host: " + proxyHost
							+ ", Proxy Port: " + proxyPort
							+ ", ILM Conformance: " + ilm_conformance_class
							+ ", Default Archive Store: " + is_default);

		} catch (Exception ex) {

			// Write Exception Into Log
			ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR, loc,
					"DEFINE_ARCHIVE_STORES: Exception: " + ex.getMessage(), ex);

			// Forward Exception
			throw ex;
		}
	}

	public void delete() throws Exception {

		int hits = 0;

		long storeId = Long.parseLong(request.getParameter("storeid"));
		String archiveStore = request.getParameter("archivestore").trim();

		PreparedStatement pst1 = null;
		ResultSet result = null;
		Connection connection = null;

		try {

			// Get DB connection
			AdminConnectionProvider acp = new AdminConnectionProvider();
			connection = acp.getConnection();

			// Check If Any Collection Is Still Assigned To Archive Store
			pst1 = connection.prepareStatement(DEL_SEL_COL_TAB);
			pst1.setLong(1, storeId);
			pst1.setLong(2, storeId);
			result = pst1.executeQuery();
			hits = 0;
			String uri = "";
			while (result.next()) {
				uri = result.getString("URI");
				hits++;
			}
			result.close();
			pst1.close();
			if (hits != 0) {
				ConfigureArchiveStores.cat.errorT(loc,
						"DEFINE_ARCHIVE_STORES: Cannot delete archive store "
								+ archiveStore
								+ " because at least collection "
								+ uri.substring(uri.lastIndexOf("/") + 1, uri
										.length()) + " is still assigned");
				throw new Exception(
						"DEFINE_ARCHIVE_STORES: Cannot delete archive store "
								+ archiveStore
								+ " because at least collection "
								+ uri.substring(uri.lastIndexOf("/") + 1, uri
										.length()) + " is still assigned");
			}

			// Delete BC_XMLA_CONFIG Entry
			Context ctx = new InitialContext();
			ArchStoreConfigLocalHome beanLocalHome = (ArchStoreConfigLocalHome) ctx
					.lookup("java:comp/env/ArchStoreConfigBean");
			beanLocalHome.remove(new Long(storeId));

			// Delete SLD Archive Store Instance And If Necessary Xml Archive
			// Server Instance
			try {
				String archiveName = archiveStore
						+ ".XMLDASName.xmldas.SystemName."
						+ SystemExplorer.getJ2EEClusterEngineName();
				Director director = new Director();
				Collection<ArchStoreConfigLocal> coll = beanLocalHome.findAll();
				if (coll.isEmpty()) {
					String engineName = "xmldas.SystemName."
							+ SystemExplorer.getJ2EEClusterEngineName();
					director.deleteSldInstance(engineName, archiveName);
				} else {
					director.deleteSldInstance(null, archiveName);
				}
			} catch (FinderException fex) {
				MasterMethod.isSLDoutOfSync = true;

				// $JL-EXC$
				ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR, loc,
						"DEFINE_ARCHIVE_STORES: FinderException: "
								+ fex.getMessage(), fex);
			} catch (NamingException nex) {
				MasterMethod.isSLDoutOfSync = true;

				// $JL-EXC$
				ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR, loc,
						"DEFINE_ARCHIVE_STORES: NamingException: "
								+ nex.getMessage(), nex);
			} catch (CIMException cimex) {
				MasterMethod.isSLDoutOfSync = true;

				// $JL-EXC$
				ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR, loc,
						"DEFINE_ARCHIVE_STORES: CIMException: "
								+ cimex.getMessage(), cimex);
			} catch (InvalidDataException idex) {
				MasterMethod.isSLDoutOfSync = true;

				// $JL-EXC$
				ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR, loc,
						"DEFINE_ARCHIVE_STORES: InvalidDataException: "
								+ idex.getMessage(), idex);
			} catch (Exception ex) {
				MasterMethod.isSLDoutOfSync = true;

				// $JL-EXC$
				ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR, loc,
						"DEFINE_ARCHIVE_STORES: Exception: " + ex.getMessage(),
						ex);
			}

			// Write Successful Archive Store Deletion Message Into Log
			ConfigureArchiveStores.cat.infoT(loc,
					"DEFINE_ARCHIVE_STORES: Archive store " + archiveStore
							+ " successfully deleted");

			// Commit Database
			connection.commit();
		} catch (SQLException sqlex) {

			// Write SQLException Into Log
			ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR, loc,
					"DEFINE_ARCHIVE_STORES: SQLException: "
							+ sqlex.getMessage(), sqlex);

			// Roll Back Database
			connection.rollback();

			// Forward SQLException
			throw sqlex;
		} catch (Exception ex) {

			// Write Exception Into Log
			ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR, loc,
					"DEFINE_ARCHIVE_STORES: Exception: " + ex.getMessage(), ex);

			// Roll Back Database
			connection.rollback();

			// Forward Exception
			throw ex;
		} finally {
			try {

				// Close Prepared Statements
				if (pst1 != null)
					pst1.close();

				// Close DB Connection
				if (connection != null)
					connection.close();
			} catch (SQLException sqlex) {

				// Write Exception Into Log
				ConfigureArchiveStores.cat.logThrowableT(Severity.ERROR, loc,
						"DEFINE_ARCHIVE_STORES: SQLException: "
								+ sqlex.getMessage(), sqlex);

				// Forward Exception
				throw sqlex;
			}
		}
	}

	private void resetExistingDefaultArchiveStore() throws FinderException,
			NamingException, CIMException, InvalidDataException {

		// Loop Over All Currently Existing Archive Stores
		Context ctx = new InitialContext();
		ArchStoreConfigLocalHome beanLocalHome = (ArchStoreConfigLocalHome) ctx
				.lookup("java:comp/env/ArchStoreConfigBean");

		Iterator<ArchStoreConfigLocal> iter = beanLocalHome.findAll()
				.iterator();
		ArchStoreConfigLocal ascl = null;
		Sapxmla_Config sac = null;
		while (iter.hasNext()) {

			// Get Archive Store Configuration
			ascl = iter.next();
			sac = ascl.getSapxmla_Config();
			if (sac.is_default.equalsIgnoreCase("Y")) {

				// Change IsDefault Value In Entity Bean To "N"
				ascl = beanLocalHome.findByPrimaryKey(new Long(sac.store_id));
				ascl.setIsdefault("N");

				// Change IsDefault Value In SLD To "N"
				try {
					String archiveName = sac.archive_store
							+ ".XMLDASName.xmldas.SystemName."
							+ SystemExplorer.getJ2EEClusterEngineName();
					String[] archivePropValues = new String[1];
					archivePropValues[0] = "false";
					Director director = new Director();
					director.updateResetDefaultSldInstance(archiveName,
							archivePropValues);
				} catch (Exception ex) {
					throw new CIMException(ex);
				}
			}
		}
	}

	public static String getWebDavRoot(String destinationName)
			throws NamingException, RemoteException, DestinationException {
		DestinationService destService = (DestinationService) new InitialContext()
				.lookup(DestinationService.JNDI_KEY);
		if (destService == null)
			throw new NamingException("Destination Service is not available");
		HTTPDestination httpDest = (HTTPDestination) destService
				.getDestination("HTTP", destinationName);
		return httpDest.getUrl();
	}
}
