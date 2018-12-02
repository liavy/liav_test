package com.sap.archtech.daservice.commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.archconn.values.ArchiveStoreData;
import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocal;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;
import com.sap.archtech.daservice.util.Director;
import com.sap.archtech.daservice.util.IdProvider;
import com.sap.archtech.daservice.util.WebDavTemplateProvider;
import com.sap.security.core.server.destinations.api.Destination;
import com.sap.security.core.server.destinations.api.DestinationException;
import com.sap.security.core.server.destinations.api.DestinationService;
import com.sap.security.core.server.destinations.api.HTTPDestination;
import com.sap.sld.api.builder.InvalidDataException;
import com.sap.sld.api.util.SystemExplorer;
import com.sap.sld.api.wbem.exception.CIMException;
import com.sap.tc.logging.Severity;
import com.tssap.dtr.client.lib.protocol.Header;
import com.tssap.dtr.client.lib.protocol.IConnectionTemplate;
import com.tssap.dtr.client.lib.protocol.IResponse;
import com.tssap.dtr.client.lib.protocol.requests.dav.Depth;
import com.tssap.dtr.client.lib.protocol.requests.dav.PropfindOption;
import com.tssap.dtr.client.lib.protocol.requests.dav.PropfindRequest;
import com.tssap.dtr.client.lib.protocol.requests.http.HeadRequest;
import com.tssap.dtr.client.lib.protocol.requests.http.OptionsRequest;

public class DefineArchiveStoresMethod extends MasterMethod {

	private final static String DEL_SEL_COL_TAB = "SELECT URI FROM BC_XMLA_COL WHERE STOREID = ? OR NEXTSTOREID = ?";

	private short ilm_conformance_class;
	private int proxy_port_int;
	private String action;
	private String archive_store;
	private String destination;
	private String is_default;
	private String proxy_host;
	private String proxy_port;
	private String storage_system;
	private String store_type;
	private String unix_root;
	private String user;
	private String url;
	private String win_root;
	private ArchStoreConfigLocalHome beanLocalHome;
	private BufferedWriter bwout;
	private Connection connection;
	private IdProvider idProvider;
	private ArrayList<ArchiveStoreData> archiveStores;
	private ObjectOutputStream oos;

	public DefineArchiveStoresMethod(HttpServletResponse response,
			Connection connection, IdProvider idProvider,
			ArchStoreConfigLocalHome beanLocalHome, String url, String action,
			String user, String archive_store, String storage_system,
			String store_type, String win_root, String unix_root,
			String destination, String proxy_host, String proxy_port,
			String is_default) {
		this.response = response;
		this.connection = connection;
		this.idProvider = idProvider;
		this.beanLocalHome = beanLocalHome;
		this.url = url;
		this.action = action;
		this.user = user;
		this.archive_store = archive_store;
		this.storage_system = storage_system;
		this.store_type = store_type;
		this.win_root = win_root;
		this.unix_root = unix_root;
		this.destination = destination;
		this.proxy_host = proxy_host;
		this.proxy_port = proxy_port;
		this.is_default = is_default;
	}

	public boolean execute() throws IOException {

		// Get Servlet Output Stream
		bwout = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream(), "UTF8"));

		// Initialize Response Array List
		archiveStores = new ArrayList<ArchiveStoreData>();

		// Set Response Header
		response.setContentType(MasterMethod.contentType);
		response.setHeader("service_message", "see message body");

		// Check Request Header "action"
		if (this.action == null) {
			this
					.reportStreamError(
							DasResponse.SC_PARAMETER_MISSING,
							"_DEFINE_ARCHIVE_STORES: ACTION missing from request header",
							bwout);
			return false;
		} else {
			this.action = this.action.toUpperCase();
			if (!(this.action.equalsIgnoreCase("L")
					|| this.action.equalsIgnoreCase("T")
					|| this.action.equalsIgnoreCase("I")
					|| this.action.equalsIgnoreCase("U") || this.action
					.equalsIgnoreCase("D"))) {
				this
						.reportStreamError(
								DasResponse.SC_KEYWORD_UNKNOWN,
								"_DEFINE_ARCHIVE_STORES: Value "
										+ this.action
										+ " of request header ACTION does not meet specifications",
								bwout);
				return false;
			}
		}

		// Check Request Header "user"
		if ((this.user == null) || (this.user.length() == 0)) {
			this.reportStreamError(DasResponse.SC_PARAMETER_MISSING,
					"_DEFINE_ARCHIVE_STORES: USER missing from request header",
					bwout);
			return false;
		}

		// Check Request Header "archive_store"
		if (this.action.equalsIgnoreCase("T")
				|| this.action.equalsIgnoreCase("I")
				|| this.action.equalsIgnoreCase("U")
				|| this.action.equalsIgnoreCase("D")) {
			if (this.archive_store != null && this.archive_store.length() > 0) {
				this.archive_store = this.archive_store.trim().toUpperCase();
			} else {
				this
						.reportStreamError(
								DasResponse.SC_PARAMETER_MISSING,
								"_DEFINE_ARCHIVE_STORES: ARCHIVE_STORE missing from request header",
								bwout);
				return false;
			}
		}

		// Execute The Requested Action
		try {
			if (this.action.equalsIgnoreCase("I") && !this.insert())
				return false;
			else if (this.action.equalsIgnoreCase("U") && !this.update())
				return false;
			else if (this.action.equalsIgnoreCase("D") && !this.delete())
				return false;

			// Send Response Body
			boolean sendResponseWasSuccessFul = true;
			if (this.action.equalsIgnoreCase("L")
					|| this.action.equalsIgnoreCase("T"))
				sendResponseWasSuccessFul = this.createResponse();
			if (sendResponseWasSuccessFul == true) {
				this.writeStatus(bwout, HttpServletResponse.SC_OK, "Ok");
				bwout.flush();
			} else
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

	private boolean insert() throws IOException {

		// Check Request Header "storage_system"
		if (this.storage_system != null && this.storage_system.length() != 0) {
			this.storage_system = this.storage_system.trim();
		} else {
			this
					.reportStreamError(
							DasResponse.SC_PARAMETER_MISSING,
							"_DEFINE_ARCHIVE_STORES: STORAGE_SYSTEM missing from request header",
							bwout);
			return false;
		}

		// Check Request Header "store_type"
		if (this.store_type != null && this.store_type.length() != 0) {
			if (!(this.store_type.equalsIgnoreCase("W") || this.store_type
					.equalsIgnoreCase("F"))) {
				this
						.reportStreamError(
								DasResponse.SC_KEYWORD_UNKNOWN,
								"_DEFINE_ARCHIVE_STORES: Value "
										+ this.store_type
										+ " of request header STORE_TYPE does not meet specifications",
								bwout);
				return false;
			}
		} else {
			this
					.reportStreamError(
							DasResponse.SC_PARAMETER_MISSING,
							"_DEFINE_ARCHIVE_STORES: STORE_TYPE missing from request header",
							bwout);
			return false;
		}

		// Check Request Header "win_root" And "unix_root"
		if (this.store_type.equalsIgnoreCase("F")) {
			if ((this.win_root != null) && (this.win_root.length() != 0)) {
				this.win_root = this.win_root.trim();
				if ((this.win_root.endsWith("\\"))
						|| (this.win_root.endsWith("/"))) {
					this
							.reportStreamError(
									DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
									"_DEFINE_ARCHIVE_STORES: Value "
											+ this.win_root
											+ " of request header WIN_ROOT must not end with a slash or backslash",
									bwout);
					return false;
				}
			}
			if ((this.unix_root != null) && (this.unix_root.length() != 0)) {
				this.unix_root = this.unix_root.trim();
				if ((this.unix_root.endsWith("\\"))
						|| (this.unix_root.endsWith("/"))) {
					this
							.reportStreamError(
									DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
									"_DEFINE_ARCHIVE_STORES: Value "
											+ this.unix_root
											+ " of request header UNIX_ROOT must not end with a slash or backslash",
									bwout);
					return false;
				}
			}
			if (!(((this.win_root != null) && (this.win_root.length() != 0)) || ((this.unix_root != null) && (this.unix_root
					.length() != 0)))) {
				this
						.reportStreamError(
								DasResponse.SC_PARAMETER_MISSING,
								"_DEFINE_ARCHIVE_STORES: WIN_ROOT or UNIX_ROOT missing from request header",
								bwout);
				return false;
			}
		}

		// Check Request Header "destination", "proxy_host" And "proxy_port"
		else {
			if ((this.destination != null) && (this.destination.length() != 0)) {
				try {
					DestinationService destService = (DestinationService) new InitialContext()
							.lookup(DestinationService.JNDI_KEY);
					if (destService == null) {
						this
								.reportStreamError(
										HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
										"_DEFINE_ARCHIVE_STORES: Destination Service is not available",
										bwout);
						return false;
					}
					Destination dest = destService.getDestination("HTTP",
							this.destination);
					if (dest.getName() == null) {
						this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
								"_DEFINE_ARCHIVE_STORES: Destination "
										+ this.destination
										+ " not found in Destination Service",
								bwout);
						return false;
					}
				} catch (NamingException nex) {
					this.reportStreamError(
							HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"_DEFINE_ARCHIVE_STORES: " + nex.toString(), nex,
							bwout);
					return false;
				} catch (DestinationException dex) {
					this.reportStreamError(
							HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"_DEFINE_ARCHIVE_STORES: " + dex.toString(), dex,
							bwout);
					return false;
				}
			} else {
				this
						.reportStreamError(
								DasResponse.SC_PARAMETER_MISSING,
								"_DEFINE_ARCHIVE_STORES: DESTINATION missing from request header",
								bwout);
				return false;
			}
			if ((this.proxy_host != null) && (this.proxy_host.length() != 0)) {
				this.proxy_host = this.proxy_host.trim();
			}
			if (this.proxy_port != null && this.proxy_port.length() != 0) {
				this.proxy_port = this.proxy_port.trim();
				try {
					this.proxy_port_int = Integer.parseInt(this.proxy_port);
				} catch (NumberFormatException nfex) {

					// $JL-EXC$
					this
							.reportStreamError(
									DasResponse.SC_PARAMETER_MISSING,
									"_DEFINE_ARCHIVE_STORES: Value "
											+ this.proxy_port
											+ " of request header PROXY_PORT does not meet specifications",
									bwout);
					return false;
				}
				if ((this.proxy_port_int < 0) || (this.proxy_port_int > 65536)) {
					this
							.reportStreamError(
									DasResponse.SC_PARAMETER_MISSING,
									"_DEFINE_ARCHIVE_STORES: Value "
											+ this.proxy_port_int
											+ " of request header PROXY_PORT does not meet specifications",
									bwout);
					return false;
				}
			}
			if ((this.proxy_host != null) && (this.proxy_host.length() != 0)
					&& (this.proxy_port_int == 0)) {
				this
						.reportStreamError(
								DasResponse.SC_PARAMETER_MISSING,
								"_DEFINE_ARCHIVE_STORES: PROXY_PORT missing from request header",
								bwout);
				return false;
			}
		}

		// Check Request Header "is_default"
		if (this.is_default != null && this.is_default.length() != 0) {
			this.is_default = this.is_default.toUpperCase().trim();
			if (!(this.is_default.equalsIgnoreCase("Y") || this.is_default
					.equalsIgnoreCase("N"))) {
				this
						.reportStreamError(
								DasResponse.SC_KEYWORD_UNKNOWN,
								"_DEFINE_ARCHIVE_STORES: Value "
										+ this.is_default
										+ " of request header IS_DEFAULT does not meet specifications",
								bwout);
				return false;
			}
		} else {
			this.is_default = "N";
		}

		// Check If Archive Store Already Exists
		try {
			Collection<ArchStoreConfigLocal> col = beanLocalHome
					.findByArchiveStore(this.archive_store.toUpperCase());
			if (!col.isEmpty()) {
				this
						.reportStreamError(HttpServletResponse.SC_CONFLICT,
								"_DEFINE_ARCHIVE_STORES: Archive Store "
										+ this.archive_store
										+ " already exists", bwout);
				return false;
			}
		} catch (FinderException fex) {
			this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
					"_DEFINE_ARCHIVE_STORES: " + fex.getMessage(), bwout);
			return false;
		}

		// Check If There Is A Conflict With Already Existing Archive Stores
		Sapxmla_Config sac = new Sapxmla_Config(this.archive_store,
				this.storage_system, this.store_type, this.win_root,
				this.unix_root, this.destination, this.proxy_host,
				this.proxy_port_int);
		if (this.checkIfNoConflictExists(sac) == false)
			return false;

		// Check If Archive Store Is Running
		try {
			this.testNewArchiveStore(sac);
		} catch (NamingException nex) {
			this.reportStreamError(HttpServletResponse.SC_CONFLICT,
					"_DEFINE_ARCHIVE_STORES: " + nex.getMessage(), bwout);
			return false;
		} catch (IOException ioex) {
			this.reportStreamError(DasResponse.SC_IO_ERROR,
					"_DEFINE_ARCHIVE_STORES: " + ioex.getMessage(), bwout);
			return false;
		}

		// Reset Currently Default Archive Store
		if (is_default.equalsIgnoreCase("Y")) {
			try {
				this.resetExistingDefaultArchiveStore();
			} catch (FinderException fex) {
				MasterMethod.isSLDoutOfSync = true;

				// $JL-EXC$
				MasterMethod.cat.logThrowableT(Severity.ERROR, loc,
						"_DEFINE_ARCHIVE_STORES: " + fex.getMessage(), fex);
			} catch (NamingException nex) {
				MasterMethod.isSLDoutOfSync = true;

				// $JL-EXC$
				MasterMethod.cat.logThrowableT(Severity.ERROR, loc,
						"_DEFINE_ARCHIVE_STORES: " + nex.getMessage(), nex);
			} catch (CIMException cimex) {
				MasterMethod.isSLDoutOfSync = true;

				// $JL-EXC$
				MasterMethod.cat.logThrowableT(Severity.ERROR, loc,
						"_DEFINE_ARCHIVE_STORES: " + cimex.getMessage(), cimex);
			} catch (InvalidDataException idex) {
				MasterMethod.isSLDoutOfSync = true;

				// $JL-EXC$
				MasterMethod.cat.logThrowableT(Severity.ERROR, loc,
						"_DEFINE_ARCHIVE_STORES: " + idex.getMessage(), idex);
			}
		}

		// Create New Archive Store Configuration Entry
		if (win_root != null && win_root.length() == 0)
			win_root = null;
		if (unix_root != null && unix_root.length() == 0)
			unix_root = null;
		if (destination != null && destination.length() == 0)
			destination = null;
		if (proxy_host != null && proxy_host.length() == 0)
			proxy_host = null;
		try {
			beanLocalHome.create(new Long(this.idProvider
					.getId("BC_XMLA_CONFIG")), archive_store, storage_system,
					store_type, win_root, unix_root, destination, proxy_host,
					proxy_port_int, ilm_conformance_class, is_default);
		} catch (CreateException cex) {
			this.reportStreamError(HttpServletResponse.SC_CONFLICT,
					"_DEFINE_ARCHIVE_STORES: " + cex.getMessage(), bwout);
			return false;
		} catch (SQLException sqlex) {
			this.reportStreamError(DasResponse.SC_SQL_ERROR,
					"_DEFINE_ARCHIVE_STORES: " + sqlex.getMessage(), bwout);
			return false;
		}

		// Add Archive Store Configuration To Response Object
		ArchiveStoreData archiveStoreData = new ArchiveStoreData();
		archiveStoreData.setArchiveStore(archive_store);
		archiveStoreData.setStorageSystem(storage_system);
		archiveStoreData.setStoreType(store_type);
		archiveStoreData.setWinRoot(win_root);
		archiveStoreData.setUnixRoot(unix_root);
		archiveStoreData.setDestination(destination);
		archiveStoreData.setProxyHost(proxy_host);
		archiveStoreData.setProxyPort(proxy_port_int);
		archiveStoreData.setIlmConformance(ilm_conformance_class);
		archiveStoreData.setIsDefault(is_default);
		archiveStoreData.setStoreStatus("S");

		// Add Archive Store Entry To Response Array List
		archiveStores.add(archiveStoreData);

		// Send Array List With One Entry
		if (!this.sendResponse())
			return false;

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
			enginePropValues[7] = this.url;
			String archiveName = this.archive_store
					+ ".XMLDASName.xmldas.SystemName."
					+ SystemExplorer.getJ2EEClusterEngineName();
			String[] archivePropValues = new String[12];
			archivePropValues[0] = this.storage_system;
			archivePropValues[1] = this.archive_store;
			archivePropValues[2] = this.storage_system;
			archivePropValues[3] = this.destination;
			archivePropValues[4] = "SAP standard";
			if (this.store_type.equalsIgnoreCase("W")) {
				archivePropValues[5] = "WebDAV";
				archivePropValues[6] = "";
				try {
					archivePropValues[7] = getWebDavRoot(this.destination);
				} catch (Exception ex) {

					// $JL-EXC$
					archivePropValues[7] = "";
				}
				archivePropValues[8] = "";
			} else {
				archivePropValues[5] = "Filesystem";
				archivePropValues[6] = this.unix_root;
				archivePropValues[7] = "";
				archivePropValues[8] = this.win_root;
			}
			archivePropValues[9] = "xmldas.SystemName."
					+ SystemExplorer.getJ2EEClusterEngineName();
			archivePropValues[10] = Short.toString(this.ilm_conformance_class);
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
			MasterMethod.cat.errorT(loc,
					"_DEFINE_ARCHIVE_STORES: JNDI error while creating WBEM client: "
							+ nex.toString());
		} catch (CIMException cimex) {
			MasterMethod.isSLDoutOfSync = true;

			// $JL-EXC$
			MasterMethod.cat.errorT(loc,
					"_DEFINE_ARCHIVE_STORES:  CIM error condition occurred: "
							+ cimex.toString());

		} catch (InvalidDataException idex) {
			MasterMethod.isSLDoutOfSync = true;

			// $JL-EXC$
			MasterMethod.cat.errorT(loc,
					"_DEFINE_ARCHIVE_STORES: Invalid input data: "
							+ idex.toString());
		} catch (Exception ex) {
			MasterMethod.isSLDoutOfSync = true;

			// $JL-EXC$
			MasterMethod.cat.errorT(loc,
					"_DEFINE_ARCHIVE_STORES: Exception occurred: "
							+ ex.toString());
		}

		// Write Successful Archive Store Creation Message Into Log
		MasterMethod.cat.infoT(loc, "_DEFINE_ARCHIVE_STORES: Archive store "
				+ archive_store + " successfully created");

		// Insert Method Was Successful
		return true;
	}

	private boolean update() throws IOException {

		// Get Archive Store Configuration
		int hits = 0;
		Sapxmla_Config sac = null;
		try {
			Iterator<ArchStoreConfigLocal> iter = this.beanLocalHome
					.findByArchiveStore(this.archive_store).iterator();
			while (iter.hasNext()) {
				ArchStoreConfigLocal ascl = iter.next();
				sac = ascl.getSapxmla_Config();
				hits++;
			}
		} catch (FinderException fex) {
			this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
					"_DEFINE_ARCHIVE_STORES: " + fex.getMessage(), bwout);
			return false;
		}
		if (hits != 1) {
			this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
					"_DEFINE_ARCHIVE_STORES: Archive Store "
							+ this.archive_store + " is not defined", bwout);
			return false;
		}

		// Check Request Header "storage_system"
		if ((this.storage_system != null)
				&& (this.storage_system.length() != 0))
			sac.storage_system = this.storage_system;

		// Check Request Header "store_type"
		if ((this.store_type != null) && (this.store_type.length() != 0)) {
			if (!(this.store_type.equalsIgnoreCase("W") || this.store_type
					.equalsIgnoreCase("F"))) {
				this
						.reportStreamError(
								DasResponse.SC_KEYWORD_UNKNOWN,
								"_DEFINE_ARCHIVE_STORES: Value "
										+ this.store_type
										+ " of request header STORE_TYPE does not meet specifications",
								bwout);
				return false;
			}
			sac.type = this.store_type;
		}

		// Check Request Header "win_root" And "unix_root"
		if (sac.type.equalsIgnoreCase("F")) {
			if ((this.win_root != null) && (this.win_root.length() != 0)) {
				this.win_root = this.win_root.trim();
				if ((this.win_root.endsWith("\\"))
						|| (this.win_root.endsWith("/"))) {
					this
							.reportStreamError(
									DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
									"_DEFINE_ARCHIVE_STORES: Value "
											+ this.win_root
											+ " of request header WIN_ROOT must not end with a slash or backslash",
									bwout);
					return false;
				}
				sac.win_root = this.win_root;
			}
			if ((this.unix_root != null) && (this.unix_root.length() != 0)) {
				this.unix_root = this.unix_root.trim();
				if ((this.unix_root.endsWith("\\"))
						|| (this.unix_root.endsWith("/"))) {
					this
							.reportStreamError(
									DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
									"_DEFINE_ARCHIVE_STORES: Value "
											+ this.unix_root
											+ " of request header UNIX_ROOT must not end with a slash or backslash",
									bwout);
					return false;
				}
				sac.unix_root = this.unix_root;
			}
			if (!(((sac.win_root != null) && (sac.win_root.length() != 0)) || ((sac.unix_root != null) && (sac.unix_root
					.length() != 0)))) {
				this
						.reportStreamError(
								DasResponse.SC_PARAMETER_MISSING,
								"_DEFINE_ARCHIVE_STORES: WIN_ROOT or UNIX_ROOT missing from request header",
								bwout);
				return false;
			}
			sac.destination = null;
			sac.proxy_host = null;
			sac.proxy_port = 0;
		}

		// Check Request Header "destination", "proxy_host" And "proxy_port"
		else {
			if ((this.destination != null) && (this.destination.length() != 0)) {
				try {
					DestinationService destService = (DestinationService) new InitialContext()
							.lookup(DestinationService.JNDI_KEY);
					if (destService == null) {
						this
								.reportStreamError(
										HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
										"_DEFINE_ARCHIVE_STORES: Destination Service is not available",
										bwout);
						return false;
					}
					Destination dest = destService.getDestination("HTTP",
							this.destination);
					if (dest.getName() == null) {
						this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
								"_DEFINE_ARCHIVE_STORES: Destination "
										+ this.destination
										+ " not found in Destination Service",
								bwout);
						return false;
					}
				} catch (NamingException nex) {
					this.reportStreamError(
							HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"_DEFINE_ARCHIVE_STORES: " + nex.toString(), nex,
							bwout);
					return false;
				} catch (DestinationException dex) {
					this.reportStreamError(
							HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"_DEFINE_ARCHIVE_STORES: " + dex.toString(), dex,
							bwout);
					return false;
				}
				sac.destination = this.destination;
			}
			if (!((sac.destination != null) && (sac.destination.length() != 0))) {
				this
						.reportStreamError(
								DasResponse.SC_PARAMETER_MISSING,
								"_DEFINE_ARCHIVE_STORES: DESTINATION missing from request header",
								bwout);
				return false;
			}
			if ((this.proxy_host != null) && (this.proxy_host.length() != 0)) {
				sac.proxy_host = this.proxy_host;
			}
			if (this.proxy_port != null && this.proxy_port.length() != 0) {
				this.proxy_port = this.proxy_port.trim();
				try {
					sac.proxy_port = Integer.parseInt(this.proxy_port);
				} catch (NumberFormatException nfex) {
					this
							.reportStreamError(
									DasResponse.SC_PARAMETER_MISSING,
									"_DEFINE_ARCHIVE_STORES: Value "
											+ this.proxy_port
											+ " of request header PROXY_PORT does not meet specifications",
									bwout);
					return false;
				}
				if ((sac.proxy_port < 0) || (sac.proxy_port > 65536)) {
					this
							.reportStreamError(
									DasResponse.SC_PARAMETER_MISSING,
									"_DEFINE_ARCHIVE_STORES: Value "
											+ sac.proxy_port
											+ " of request header PROXY_PORT does not meet specifications",
									bwout);
					return false;
				}
			}
			if ((sac.proxy_host != null) && (sac.proxy_host.length() != 0)
					&& (sac.proxy_port == 0)) {
				this
						.reportStreamError(
								DasResponse.SC_PARAMETER_MISSING,
								"_DEFINE_ARCHIVE_STORES: PROXY_PORT missing from request header",
								bwout);
				return false;
			}
			sac.win_root = null;
			sac.unix_root = null;
		}

		// Check Request Header "is_default"
		if ((this.is_default != null) && (this.is_default.length() != 0)) {
			this.is_default = this.is_default.toUpperCase().trim();
			if (!(this.is_default.equalsIgnoreCase("Y") || this.is_default
					.equalsIgnoreCase("N"))) {
				this
						.reportStreamError(
								DasResponse.SC_KEYWORD_UNKNOWN,
								"_DEFINE_ARCHIVE_STORES: Value "
										+ this.is_default
										+ " of request header IS_DEFAULT does not meet specifications",
								bwout);
				return false;
			}
			sac.is_default = this.is_default;
		}

		// Check If There Is A Conflict With Already Existing Archive Stores
		if (this.checkIfNoConflictExists(sac) == false)
			return false;

		// Check If Archive Store Is Running
		try {
			this.testUpdateArchiveStore(sac);
		} catch (NamingException nex) {
			this.reportStreamError(HttpServletResponse.SC_CONFLICT,
					"_DEFINE_ARCHIVE_STORES: " + nex.getMessage(), bwout);
			return false;
		} catch (IOException ioex) {
			this.reportStreamError(DasResponse.SC_IO_ERROR,
					"_DEFINE_ARCHIVE_STORES: " + ioex.getMessage(), bwout);
			return false;
		}

		// Reset Currently Default Archive Store
		if (sac.is_default.equalsIgnoreCase("Y")) {
			try {
				this.resetExistingDefaultArchiveStore();
			} catch (FinderException fex) {
				MasterMethod.isSLDoutOfSync = true;

				// $JL-EXC$
				MasterMethod.cat.logThrowableT(Severity.ERROR, loc,
						"_DEFINE_ARCHIVE_STORES: " + fex.getMessage(), fex);
			} catch (NamingException nex) {
				MasterMethod.isSLDoutOfSync = true;

				// $JL-EXC$
				MasterMethod.cat.logThrowableT(Severity.ERROR, loc,
						"_DEFINE_ARCHIVE_STORES: " + nex.getMessage(), nex);
			} catch (CIMException cimex) {
				MasterMethod.isSLDoutOfSync = true;

				// $JL-EXC$
				MasterMethod.cat.logThrowableT(Severity.ERROR, loc,
						"_DEFINE_ARCHIVE_STORES: " + cimex.getMessage(), cimex);
			} catch (InvalidDataException idex) {
				MasterMethod.isSLDoutOfSync = true;

				// $JL-EXC$
				MasterMethod.cat.logThrowableT(Severity.ERROR, loc,
						"_DEFINE_ARCHIVE_STORES: " + idex.getMessage(), idex);
			}
		}

		// Persist Updated Archive Store Configuration
		try {
			if (sac.win_root != null && sac.win_root.length() == 0)
				sac.win_root = null;
			if (sac.unix_root != null && sac.unix_root.length() == 0)
				sac.unix_root = null;
			if (sac.destination != null && sac.destination.length() == 0)
				sac.destination = null;
			if (sac.proxy_host != null && sac.proxy_host.length() == 0)
				sac.proxy_host = null;
			ArchStoreConfigLocal ascl = beanLocalHome
					.findByPrimaryKey(new Long(sac.store_id));
			ascl.setStoragesystem(sac.storage_system);
			ascl.setStoretype(sac.type);
			ascl.setWinroot(sac.win_root);
			ascl.setUnixroot(sac.unix_root);
			ascl.setDestination(sac.destination);
			ascl.setProxyhost(sac.proxy_host);
			ascl.setProxyport(sac.proxy_port);
			ascl.setIlmconform(this.ilm_conformance_class);
			ascl.setIsdefault(sac.is_default);
		} catch (FinderException fex) {
			this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
					"_DEFINE_ARCHIVE_STORES: " + fex.getMessage(), bwout);
			return false;
		}

		// Add Archive Store Configuration To Response Object
		ArchiveStoreData archiveStoreData = new ArchiveStoreData();
		archiveStoreData.setArchiveStore(sac.archive_store);
		archiveStoreData.setStorageSystem(sac.storage_system);
		archiveStoreData.setStoreType(sac.type);
		archiveStoreData.setWinRoot(sac.win_root);
		archiveStoreData.setUnixRoot(sac.unix_root);
		archiveStoreData.setDestination(sac.destination);
		archiveStoreData.setProxyHost(sac.proxy_host);
		archiveStoreData.setProxyPort(sac.proxy_port);
		archiveStoreData.setIlmConformance(this.ilm_conformance_class);
		archiveStoreData.setIsDefault(sac.is_default);
		archiveStoreData.setStoreStatus("S");

		// Add Archive Store Entry To Response Array List
		archiveStores.add(archiveStoreData);

		// Send Empty Array List
		if (!this.sendResponse())
			return false;

		// Remove HTTP Connection From HTTP Connection Pool In Case The
		// Destination Was Updated
		WebDavTemplateProvider.webDAVTemplateHashTable.remove(new Long(
				sac.store_id));

		// Update SLD Archive Store Instance
		try {
			String archiveName = this.archive_store
					+ ".XMLDASName.xmldas.SystemName."
					+ SystemExplorer.getJ2EEClusterEngineName();
			String[] archivePropValues = new String[12];
			archivePropValues[0] = sac.storage_system;
			archivePropValues[1] = sac.archive_store;
			archivePropValues[2] = sac.storage_system;
			if (this.destination == null)
				archivePropValues[3] = "";
			else
				archivePropValues[3] = sac.destination;
			archivePropValues[4] = "SAP standard";
			if (sac.type.equalsIgnoreCase("W")) {
				archivePropValues[5] = "WebDAV";
				archivePropValues[6] = "";
				if (sac.destination == null)
					archivePropValues[7] = "";
				else {
					try {
						archivePropValues[7] = getWebDavRoot(sac.destination);
					} catch (Exception ex) {

						// $JL-EXC$
						archivePropValues[7] = "";
					}
				}
				archivePropValues[8] = "";
			} else {
				archivePropValues[5] = "Filesystem";
				if (sac.unix_root == null)
					archivePropValues[6] = "";
				else
					archivePropValues[6] = sac.unix_root;
				archivePropValues[7] = "";
				if (sac.win_root == null)
					archivePropValues[8] = "";
				else
					archivePropValues[8] = sac.win_root;
			}
			archivePropValues[9] = "xmldas.SystemName."
					+ SystemExplorer.getJ2EEClusterEngineName();
			archivePropValues[10] = Short.toString(this.ilm_conformance_class);
			if (sac.is_default.equalsIgnoreCase("Y"))
				archivePropValues[11] = "true";
			else
				archivePropValues[11] = "false";
			Director director = new Director();

			director.updateSldInstance(archiveName, archivePropValues);
		} catch (NamingException nex) {
			MasterMethod.isSLDoutOfSync = true;

			// $JL-EXC$
			MasterMethod.cat.errorT(loc,
					"_DEFINE_ARCHIVE_STORES: JNDI error while creating WBEM client: "
							+ nex.toString());
		} catch (CIMException cimex) {
			MasterMethod.isSLDoutOfSync = true;

			// $JL-EXC$
			MasterMethod.cat.errorT(loc,
					"_DEFINE_ARCHIVE_STORES:  CIM error condition occurred: "
							+ cimex.toString());
		} catch (InvalidDataException idex) {
			MasterMethod.isSLDoutOfSync = true;

			// $JL-EXC$
			MasterMethod.cat.errorT(loc,
					"_DEFINE_ARCHIVE_STORES: Invalid input data: "
							+ idex.toString());
		} catch (Exception ex) {
			MasterMethod.isSLDoutOfSync = true;

			// $JL-EXC$
			MasterMethod.cat.errorT(loc,
					"_DEFINE_ARCHIVE_STORES: Exception occurred: "
							+ ex.toString());
		}

		// Write Successful Archive Store Update Message Into Log
		MasterMethod.cat.infoT(loc, "_DEFINE_ARCHIVE_STORES: Archive store "
				+ sac.archive_store + " successful updated. Storage System: "
				+ sac.storage_system + ", Store Type: " + sac.type
				+ ", Win Root: " + sac.win_root + ", Unix Root: "
				+ sac.unix_root + ", Destination: " + sac.destination
				+ ", Proxy Host: " + sac.proxy_host + ", Proxy Port: "
				+ sac.proxy_port + ", ILM Conformance: " + sac.ilm_conformance
				+ ", Default Archive Store: " + sac.is_default);

		// Update Method Was Successful
		return true;
	}

	private boolean delete() throws IOException {

		// Get Archive Store Configuration
		int hits = 0;
		Sapxmla_Config sac = null;
		try {
			Iterator<ArchStoreConfigLocal> iter = this.beanLocalHome
					.findByArchiveStore(this.archive_store).iterator();
			while (iter.hasNext()) {
				ArchStoreConfigLocal ascl = iter.next();
				sac = ascl.getSapxmla_Config();
				hits++;
			}
		} catch (FinderException fex) {
			this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
					"_DEFINE_ARCHIVE_STORES: " + fex.getMessage(), bwout);
			return false;
		}
		if (hits != 1) {
			this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
					"_DEFINE_ARCHIVE_STORES: Archive Store "
							+ this.archive_store + " is not defined", bwout);
			return false;
		}

		// Get All Assigned Archive Stores
		String uri = "";
		PreparedStatement pst1 = null;
		ResultSet result = null;
		boolean errorOccurred = false;
		try {
			pst1 = connection.prepareStatement(DEL_SEL_COL_TAB);
			pst1.setLong(1, sac.store_id);
			pst1.setLong(2, sac.store_id);
			result = pst1.executeQuery();
			hits = 0;
			while (result.next()) {
				uri = result.getString("URI");
				hits++;
			}
			result.close();
			pst1.close();
		} catch (SQLException sqlex) {
			this.reportStreamError(DasResponse.SC_SQL_ERROR,
					"_DEFINE_ARCHIVE_STORES: " + sqlex.getMessage(), bwout);
			errorOccurred = true;
		} finally {

			// Close Prepared Statements
			try {
				if (result != null)
					result.close();
				if (pst1 != null)
					pst1.close();
			} catch (SQLException sqlex) {
				this.reportStreamError(DasResponse.SC_SQL_ERROR,
						"_DEFINE_ARCHIVE_STORES: " + sqlex.getMessage(), bwout);
				errorOccurred = true;
			}
		}
		if (errorOccurred)
			return false;

		// Check If Any Collection Is Still Assigned To Archive Store
		if (hits != 0) {
			this.reportStreamError(HttpServletResponse.SC_CONFLICT,
					"_DEFINE_ARCHIVE_STORES: Cannot delete archive store "
							+ archive_store
							+ " because at least collection "
							+ uri.substring(uri.lastIndexOf("/") + 1, uri
									.length()) + " is still assigned", bwout);
			return false;
		}

		// Delete Archive Store Configuration
		try {
			this.beanLocalHome.remove(new Long(sac.store_id));
		} catch (RemoveException rex) {
			this.reportStreamError(HttpServletResponse.SC_CONFLICT,
					"_DEFINE_ARCHIVE_STORES: " + rex.getMessage(), bwout);
			return false;
		}

		// Send Empty Array List
		if (!this.sendResponse())
			return false;

		// Remove HTTP Connection From HTTP Connection Pool
		WebDavTemplateProvider.webDAVTemplateHashTable.remove(new Long(
				sac.store_id));

		// Delete SLD Archive Store Instance And If Necessary Xml Archive Server
		// Instance
		try {
			String archiveName = this.archive_store
					+ ".XMLDASName.xmldas.SystemName."
					+ SystemExplorer.getJ2EEClusterEngineName();
			Director director = new Director();
			Collection<ArchStoreConfigLocal> coll = this.beanLocalHome
					.findAll();
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
			MasterMethod.cat.errorT(loc,
					"_DEFINE_ARCHIVE_STORES: Failure while finding the requested EJB objects: "
							+ fex.toString());

		} catch (NamingException nex) {
			MasterMethod.isSLDoutOfSync = true;

			// $JL-EXC$
			MasterMethod.cat.errorT(loc,
					"_DEFINE_ARCHIVE_STORES: JNDI error while creating WBEM client: "
							+ nex.toString());
		} catch (CIMException cimex) {
			MasterMethod.isSLDoutOfSync = true;

			// $JL-EXC$
			MasterMethod.cat.errorT(loc,
					"_DEFINE_ARCHIVE_STORES:  CIM error condition occurred: "
							+ cimex.toString());
		} catch (InvalidDataException idex) {
			MasterMethod.isSLDoutOfSync = true;

			// $JL-EXC$
			MasterMethod.cat.errorT(loc,
					"_DEFINE_ARCHIVE_STORES: Invalid input data: "
							+ idex.toString());
		} catch (Exception ex) {
			MasterMethod.isSLDoutOfSync = true;

			// $JL-EXC$
			MasterMethod.cat.errorT(loc,
					"_DEFINE_ARCHIVE_STORES: Exception occurred: "
							+ ex.toString());
		}

		// Write Successful Archive Store Deletion Message Into Log
		MasterMethod.cat.infoT(loc, "_DEFINE_ARCHIVE_STORES: Archive store "
				+ archive_store + " successfully deleted");

		// Delete Method Was Successful
		return true;
	}

	private boolean createResponse() throws IOException {
		try {
			DestinationService destService = null;
			Iterator<ArchStoreConfigLocal> iter = null;
			if (this.action.equalsIgnoreCase("T"))
				iter = beanLocalHome.findByArchiveStore(this.archive_store)
						.iterator();
			else {
				iter = beanLocalHome.findAll().iterator();

				// Get Destination Service
				destService = (DestinationService) new InitialContext()
						.lookup(DestinationService.JNDI_KEY_LOCAL);
				if (destService == null)
					throw new NamingException(
							"Destination Service is not available");
			}

			// Loop All Archive Stores
			while (iter.hasNext()) {

				// Get Archive Store Configuration
				ArchStoreConfigLocal ascl = iter.next();
				Sapxmla_Config sac = ascl.getSapxmla_Config();

				// Add Archive Store Configuration To Response Object
				ArchiveStoreData archiveStoreData = new ArchiveStoreData();
				archiveStoreData.setArchiveStore(sac.archive_store);
				archiveStoreData.setStorageSystem(sac.storage_system);
				archiveStoreData.setStoreType(sac.type);
				archiveStoreData.setWinRoot(sac.win_root);
				archiveStoreData.setUnixRoot(sac.unix_root);
				archiveStoreData.setDestination(sac.destination);
				archiveStoreData.setProxyHost(sac.proxy_host);
				archiveStoreData.setProxyPort(sac.proxy_port);
				archiveStoreData.setIlmConformance(sac.ilm_conformance);
				archiveStoreData.setIsDefault(sac.is_default);
				if (this.action.equalsIgnoreCase("T"))
					archiveStoreData.setStoreStatus(this
							.testExistingArchiveStore(sac));
				else {

					// Workaround To Avoid ArchiveStoreData Bean Interface
					// Change
					if (sac.type.equalsIgnoreCase("W")) {

						// After Destination Service Usage
						if ((sac.destination != null)
								&& (sac.destination.trim().length() != 0)) {
							HTTPDestination httpDest = (HTTPDestination) destService
									.getDestination("HTTP", sac.destination);
							archiveStoreData.setStoreStatus(httpDest.getUrl());
						}

						// Before Destination Service Usage
						else {
							archiveStoreData.setStoreStatus(sac.win_root);
						}
					} else {
						archiveStoreData.setStoreStatus(null);
					}
				}

				// Add Archive Store Entry To Response Array List
				archiveStores.add(archiveStoreData);
			}
		} catch (FinderException fex) {
			this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
					"_DEFINE_ARCHIVE_STORES: " + fex.getMessage(), bwout);
			return false;
		} catch (NamingException nex) {
			this.reportStreamError(HttpServletResponse.SC_CONFLICT,
					"_DEFINE_ARCHIVE_STORES: " + nex.getMessage(), bwout);
			return false;
		} catch (DestinationException dex) {
			this.reportStreamError(HttpServletResponse.SC_CONFLICT,
					"_DEFINE_ARCHIVE_STORES: " + dex.getMessage(), bwout);
			return false;
		}

		// Send Created Response
		if (!this.sendResponse())
			return false;

		// Method Was Successful
		return true;
	}

	private boolean sendResponse() throws IOException {
		try {

			// Write Serialized Object Into Servlet Output Stream
			oos = new ObjectOutputStream(response.getOutputStream());
			oos.writeObject(archiveStores);
		} catch (IOException ioex) {
			this.reportStreamError(DasResponse.SC_IO_ERROR,
					"_DEFINE_ARCHIVE_STORES: " + ioex.getMessage(), bwout);
			return false;
		} finally {

			// Flush Object Output Stream
			oos.flush();
		}

		// Method Was Successful
		return true;
	}

	private String testExistingArchiveStore(Sapxmla_Config sac) {
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
				return "F;Testing the archive store " + archive_store
						+ " failed. Reason: " + ex.getMessage();
			} finally {

				// Release DTR Connection
				WebDavTemplateProvider.releaseWebDAVConnection(conn);
			}
			if ((optionsStatusCode != 200) || (optionsSupportsDAV == false)) {
				if (optionsSupportsDAV == false) {
					return "F;Testing the archive store "
							+ archive_store
							+ " failed. Reason: The response of the OPTIONS request is "
							+ optionsStatusCode + " " + optionsReasonPhrase
							+ ". The WebDAV protocol is not supported.";
				} else {
					return "F;Testing the archive store "
							+ archive_store
							+ " failed. Reason: The response of the OPTIONS request is "
							+ optionsStatusCode + " " + optionsReasonPhrase
							+ ".";
				}
			}
			if (headStatusCode != 200) {
				return "F;Testing the archive store "
						+ archive_store
						+ " failed. Reason: The response of the HEAD request is "
						+ headStatusCode + " " + headReasonPhrase + ".";
			}
		} else {
			boolean isDirectory = false;
			File f = null;
			try {

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
				return "F;Testing the archive store " + archive_store
						+ " failed. Reason: " + ex.getMessage() + ".";
			}
			if (isDirectory == false) {
				return "F;Testing the archive store " + archive_store
						+ " failed. Reason: The directory " + f.getName()
						+ " does not exist or " + f.getName()
						+ " is not a directory.";
			}
		}

		// Archive Store Test Was Successful
		return "S";
	}

	private void testNewArchiveStore(Sapxmla_Config sac)
			throws NamingException, IOException {

		// Check If Archive Store Is Running
		if (sac.type.toUpperCase().startsWith("W")) {
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
						.getDestination("HTTP", sac.destination);
				IConnectionTemplate connTemplate = httpDest
						.getHTTPConnectionTemplate();
				com.tssap.dtr.client.lib.protocol.Connection conn = new com.tssap.dtr.client.lib.protocol.Connection(
						connTemplate);
				if (sac.proxy_host == null || sac.proxy_host.length() == 0) {
					conn.setUseProxy(false);
				} else {
					conn.setProxyHost(sac.proxy_host);
					conn.setProxyPort(sac.proxy_port);
					conn.setUseProxy(true);
				}
				conn
						.setSocketReadTimeout(WebDavTemplateProvider.WEBDAVREADTIMEOUT);
				conn
						.setSocketConnectTimeout(WebDavTemplateProvider.WEBDAVCONNECTTIMEOUT);
				conn
						.setSocketExpirationTimeout(WebDavTemplateProvider.WEBDAVEXPIRATIONTIMEOUT);
				conn.setUserAgent(WebDavTemplateProvider.WEBDAVUSERAGENT);

				// OPTIONS request
				OptionsRequest optionsRequest = new OptionsRequest("");
				IResponse optionsResponse = optionsRequest.perform(conn);
				optionsSupportsDAV = optionsRequest.supportsDAV();
				optionsStatusCode = optionsResponse.getStatus();
				optionsReasonPhrase = optionsResponse.getStatusDescription();
				if (optionsStatusCode != 200)
					throw new Exception("OPTIONS: " + optionsStatusCode + " "
							+ optionsReasonPhrase);

				// Get ILM Conformance Class From Archive Store
				this.ilm_conformance_class = 0;
				Header header = optionsResponse
						.getHeader("SAP-ILM-Conformance");
				if (header != null) {
					String ilmValue = header.getValue();
					if (ilmValue != null && ilmValue.length() > 0) {
						this.ilm_conformance_class = Short.parseShort(ilmValue);
					} else {
						this.ilm_conformance_class = 0;
					}

				} else {
					this.ilm_conformance_class = 0;
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
				propfindStatusCode = propfindResponse.getStatus();
				propfindReasonPhrase = propfindResponse.getStatusDescription();
				if (propfindStatusCode == 207)
					propfindCountResources = propfindRequest.countResources();

				// Close DTR Connection
				conn.close();
			} catch (Exception ex) {

				// $JL-EXC$
				throw new IOException("Testing the archive store "
						+ archive_store + " failed. Reason: " + ex.getMessage()
						+ ".");
			}

			// Evaluate If Check Was Successful
			if ((optionsStatusCode != 200) || (optionsSupportsDAV == false)) {
				if (optionsSupportsDAV == false)
					throw new IOException(
							"Testing the archive store "
									+ archive_store
									+ " failed. Reason: The response of the OPTIONS request is "
									+ optionsStatusCode + " "
									+ optionsReasonPhrase
									+ ". The WebDAV protocol is not supported.");
				else
					throw new IOException(
							"Testing the archive store "
									+ archive_store
									+ " failed. Reason: The response of the OPTIONS request is "
									+ optionsStatusCode + " "
									+ optionsReasonPhrase + ".");
			}
			if (headStatusCode != 200) {
				throw new IOException(
						"Testing the archive store "
								+ archive_store
								+ " failed. Reason: The response of the HEAD request is "
								+ headStatusCode + " " + headReasonPhrase + ".");
			}
			if (((propfindStatusCode != 207)) || (propfindCountResources != 1)) {
				if (propfindCountResources != 1)
					throw new IOException(
							"Testing the archive store "
									+ archive_store
									+ " failed. Reason: The response of the PROPFIND request is "
									+ propfindStatusCode
									+ " "
									+ propfindReasonPhrase
									+ ". The WebDAV root collection is not empty.");
				else
					throw new IOException(
							"Testing the archive store "
									+ archive_store
									+ " failed. Reason: The response of the PROPFIND request is "
									+ propfindStatusCode + " "
									+ propfindReasonPhrase + ".");
			}
		} else {
			boolean isDirectory = false;
			boolean canRead = false;
			boolean canWrite = false;
			boolean hasChildren = false;
			File f = null;
			try {

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

				// Check If Directory Can Be Read
				canRead = f.canRead();

				// Check If Directory Can Be Written
				canWrite = f.canWrite();

				// Check If Directory Contains Directories Or Files
				File[] fl = f.listFiles();
				if (fl != null) {
					if (fl.length == 0)
						hasChildren = false;
					else
						hasChildren = true;
				}
			} catch (Exception ex) {

				// $JL-EXC$
				throw new IOException("Testing the archive store "
						+ archive_store + " failed. Reason: " + ex.getMessage()
						+ ".");
			}

			// Evaluate If Check Was Successful
			if (isDirectory == false) {
				throw new IOException("Testing the archive store "
						+ archive_store + " failed. Reason: The directory "
						+ f.getName() + " does not exist or " + f.getName()
						+ " is not a directory.");
			}
			if (canRead == false) {
				throw new IOException("Testing the archive store "
						+ archive_store + " failed. Reason: The directory "
						+ f.getName() + " is not readable.");
			}
			if (canWrite == false) {
				throw new IOException("Testing the archive store "
						+ archive_store + " failed. Reason: The directory "
						+ f.getName() + " is not writable.");
			}
			if (hasChildren == true) {
				throw new IOException("Testing archive store " + archive_store
						+ " failed. Reason: The directory " + f.getName()
						+ " is not empty.");
			}
		}
	}

	private void testUpdateArchiveStore(Sapxmla_Config sac)
			throws NamingException, IOException {

		// Check If Archive Store Is Running
		if (sac.type.toUpperCase().startsWith("W")) {
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
						.getDestination("HTTP", sac.destination);
				IConnectionTemplate connTemplate = httpDest
						.getHTTPConnectionTemplate();
				com.tssap.dtr.client.lib.protocol.Connection conn = new com.tssap.dtr.client.lib.protocol.Connection(
						connTemplate);
				if (sac.proxy_host == null || sac.proxy_host.length() == 0) {
					conn.setUseProxy(false);
				} else {
					conn.setProxyHost(sac.proxy_host);
					conn.setProxyPort(sac.proxy_port);
					conn.setUseProxy(true);
				}
				conn
						.setSocketReadTimeout(WebDavTemplateProvider.WEBDAVREADTIMEOUT);
				conn
						.setSocketConnectTimeout(WebDavTemplateProvider.WEBDAVCONNECTTIMEOUT);
				conn
						.setSocketExpirationTimeout(WebDavTemplateProvider.WEBDAVEXPIRATIONTIMEOUT);
				conn.setUserAgent(WebDavTemplateProvider.WEBDAVUSERAGENT);

				// OPTIONS request
				OptionsRequest optionsRequest = new OptionsRequest("");
				IResponse optionsResponse = optionsRequest.perform(conn);
				optionsSupportsDAV = optionsRequest.supportsDAV();
				optionsStatusCode = optionsResponse.getStatus();
				optionsReasonPhrase = optionsResponse.getStatusDescription();

				// Get ILM Conformance Class From Archive Store
				this.ilm_conformance_class = 0;
				Header header = optionsResponse
						.getHeader("SAP-ILM-Conformance");
				if (header != null) {
					String ilmValue = header.getValue();
					if (ilmValue != null && ilmValue.length() > 0) {
						this.ilm_conformance_class = Short.parseShort(ilmValue);
					} else {
						this.ilm_conformance_class = 0;
					}

				} else {
					this.ilm_conformance_class = 0;
				}

				// HEAD Request
				HeadRequest headRequest = new HeadRequest("");
				IResponse headResponse = headRequest.perform(conn);
				headStatusCode = headResponse.getStatus();
				headReasonPhrase = headResponse.getStatusDescription();

				// Close DTR Connection
				conn.close();
			} catch (Exception ex) {

				// $JL-EXC$
				throw new IOException("Testing the archive store "
						+ archive_store + " failed. Reason: " + ex.getMessage()
						+ ".");
			}

			// Evaluate If Check Was Successful
			if ((optionsStatusCode != 200) || (optionsSupportsDAV == false)) {
				if (optionsSupportsDAV == false)
					throw new IOException(
							"Testing the archive store "
									+ archive_store
									+ " failed. Reason: The response of the OPTIONS request is "
									+ optionsStatusCode + " "
									+ optionsReasonPhrase
									+ ". The WebDAV protocol is not supported.");
				else
					throw new IOException(
							"Testing the archive store "
									+ archive_store
									+ " failed. Reason: The response of the OPTIONS request is "
									+ optionsStatusCode + " "
									+ optionsReasonPhrase + ".");
			}
			if (headStatusCode != 200) {
				throw new IOException(
						"Testing the archive store "
								+ archive_store
								+ " failed. Reason: The response of the HEAD request is "
								+ headStatusCode + " " + headReasonPhrase + ".");
			}
		} else {
			boolean isDirectory = false;
			boolean canRead = false;
			boolean canWrite = false;
			File f = null;
			try {

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

				// Check If Directory Can Be Read
				canRead = f.canRead();

				// Check If Directory Can Be Written
				canWrite = f.canWrite();
			} catch (Exception ex) {

				// $JL-EXC$
				throw new IOException("Testing the archive store "
						+ archive_store + " failed. Reason: " + ex.getMessage()
						+ ".");
			}

			// Evaluate If Check Was Successful
			if (isDirectory == false) {
				throw new IOException("Testing the archive store "
						+ archive_store + " failed. Reason: The directory "
						+ f.getName() + " does not exist or " + f.getName()
						+ " is not a directory.");
			}
			if (canRead == false) {
				throw new IOException("Testing the archive store "
						+ archive_store + " failed. Reason: The directory "
						+ f.getName() + " is not readable.");
			}
			if (canWrite == false) {
				throw new IOException("Testing the archive store "
						+ archive_store + " failed. Reason: The directory "
						+ f.getName() + " is not writable.");
			}
		}
	}

	private void resetExistingDefaultArchiveStore() throws FinderException,
			NamingException, CIMException, InvalidDataException {

		// Loop Over All Currently Existing Archive Stores
		Iterator<ArchStoreConfigLocal> iter = this.beanLocalHome.findAll()
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

	private boolean checkIfNoConflictExists(Sapxmla_Config thissac)
			throws IOException {
		if (thissac.type.equalsIgnoreCase("W"))// WebDAV System
		{
			try {
				Collection<ArchStoreConfigLocal> c = beanLocalHome.findAll();
				for (Iterator<ArchStoreConfigLocal> iter = c.iterator(); iter
						.hasNext();) {
					ArchStoreConfigLocal element = iter.next();
					Sapxmla_Config sac = element.getSapxmla_Config();
					if (!(this.action.equalsIgnoreCase("U") && thissac.archive_store
							.equalsIgnoreCase(sac.archive_store))) {

						// Note: There Is Not Check For Non Destination Archive
						// Stores
						if ((thissac.destination != null)
								&& ((sac.destination != null) && (sac.destination
										.trim().length() != 0))) {
							if (thissac.destination.equals(sac.destination)) {
								this
										.reportStreamError(
												HttpServletResponse.SC_CONFLICT,
												"_DEFINE_ARCHIVE_STORES: Destination "
														+ thissac.destination
														+ " already exists in archive store "
														+ sac.archive_store,
												bwout);
								return false;
							}
							DestinationService destService = (DestinationService) new InitialContext()
									.lookup(DestinationService.JNDI_KEY);
							if (destService == null) {
								this
										.reportStreamError(
												HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
												"_DEFINE_ARCHIVE_STORES: Destination Service is not available",
												bwout);
								return false;
							}
							HTTPDestination thisDest = (HTTPDestination) destService
									.getDestination("HTTP", thissac.destination);
							HTTPDestination sacDest = (HTTPDestination) destService
									.getDestination("HTTP", sac.destination);
							if ((thisDest == null) || (sacDest == null)) {
								if (thisDest == null)
									this
											.reportStreamError(
													DasResponse.SC_DOES_NOT_EXISTS,
													"_DEFINE_ARCHIVE_STORES: Destination "
															+ thissac.destination
															+ " not found in Destination Service",
													bwout);
								else
									this
											.reportStreamError(
													DasResponse.SC_DOES_NOT_EXISTS,
													"_DEFINE_ARCHIVE_STORES: Destination "
															+ sac.destination
															+ " not found in Destination Service",
													bwout);
								return false;
							}
							String thisURLString = thisDest.getUrl();
							String sacURLString = sacDest.getUrl();
							if ((thisURLString == null)
									|| (sacURLString == null)) {
								if (thisURLString == null)
									this
											.reportStreamError(
													DasResponse.SC_DOES_NOT_EXISTS,
													"_DEFINE_ARCHIVE_STORES: No URL for destination "
															+ thissac.destination
															+ " found in Destination Service",
													bwout);
								else
									this
											.reportStreamError(
													DasResponse.SC_DOES_NOT_EXISTS,
													"_DEFINE_ARCHIVE_STORES: No URL for destination "
															+ sac.destination
															+ " found in Destination Service",
													bwout);
								return false;
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
								this
										.reportStreamError(
												HttpServletResponse.SC_CONFLICT,
												"_DEFINE_ARCHIVE_STORES: URL "
														+ thisNormalizedURLString
														+ " of destination "
														+ thissac.destination
														+ " already exists in destination "
														+ sac.destination
														+ " of archive store "
														+ sac.archive_store,
												bwout);
								return false;
							}
							if (thisNormalizedURLString
									.startsWith(sacNormalizedURLString)) {
								if (thisNormalizedURLString.replace(
										sacNormalizedURLString, "").startsWith(
										"/")) {
									this
											.reportStreamError(
													HttpServletResponse.SC_CONFLICT,
													"_DEFINE_ARCHIVE_STORES: URL "
															+ thisNormalizedURLString
															+ " of destination "
															+ thissac.destination
															+ " is a child node of URL "
															+ sacNormalizedURLString
															+ " of destination "
															+ sac.destination
															+ " that already exists in archive store "
															+ sac.archive_store,
													bwout);
									return false;
								}
							}
							if (sacNormalizedURLString
									.startsWith(thisNormalizedURLString)) {
								if (sacNormalizedURLString.replace(
										thisNormalizedURLString, "")
										.startsWith("/")) {
									this
											.reportStreamError(
													HttpServletResponse.SC_CONFLICT,
													"_DEFINE_ARCHIVE_STORES: URL "
															+ thisNormalizedURLString
															+ " of destination "
															+ thissac.destination
															+ " is a parent node of URL "
															+ sacNormalizedURLString
															+ " of destination "
															+ sac.destination
															+ " that already exists in archive store "
															+ sac.archive_store,
													bwout);
									return false;
								}
							}
						}
					}
				}
			} catch (FinderException fex) {
				this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
						"_DEFINE_ARCHIVE_STORES: " + fex.getMessage(), bwout);
				return false;
			} catch (NamingException nex) {
				this
						.reportStreamError(
								HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
								"_DEFINE_ARCHIVE_STORES: " + nex.toString(),
								nex, bwout);
				return false;
			} catch (DestinationException dex) {
				this
						.reportStreamError(
								HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
								"_DEFINE_ARCHIVE_STORES: " + dex.toString(),
								dex, bwout);
				return false;
			}
		} else// File System
		{
			try {
				Collection<ArchStoreConfigLocal> c = beanLocalHome.findAll();
				for (Iterator<ArchStoreConfigLocal> iter = c.iterator(); iter
						.hasNext();) {
					ArchStoreConfigLocal element = iter.next();
					Sapxmla_Config sac = element.getSapxmla_Config();
					if (!(this.action.equalsIgnoreCase("U") && thissac.archive_store
							.equalsIgnoreCase(sac.archive_store))) {
						if ((thissac.win_root != null)
								&& (sac.win_root != null)) {
							if (thissac.win_root.equals(sac.win_root)) {
								this
										.reportStreamError(
												HttpServletResponse.SC_CONFLICT,
												"_DEFINE_ARCHIVE_STORES: Windows root "
														+ thissac.win_root
														+ " already exists in archive store "
														+ sac.archive_store,
												bwout);
								return false;
							}
							if (thissac.win_root.startsWith(sac.win_root)) {
								if ((thissac.win_root.replace(sac.win_root, "")
										.startsWith("\\"))) {
									this
											.reportStreamError(
													HttpServletResponse.SC_CONFLICT,
													"_DEFINE_ARCHIVE_STORES: Windows root "
															+ thissac.win_root
															+ " is a child node of Windows root "
															+ sac.win_root
															+ " that already exists in archive store "
															+ sac.archive_store,
													bwout);
									return false;
								}
							}
							if (sac.win_root.startsWith(thissac.win_root)) {
								if ((sac.win_root.replace(thissac.win_root, "")
										.startsWith("\\"))) {
									this
											.reportStreamError(
													HttpServletResponse.SC_CONFLICT,
													"_DEFINE_ARCHIVE_STORES: Windows root "
															+ thissac.win_root
															+ " is a parent node of Windows root "
															+ sac.win_root
															+ " that already exists in archive store "
															+ sac.archive_store,
													bwout);
									return false;
								}
							}
						}
						if ((thissac.unix_root != null)
								&& (sac.unix_root != null)) {
							if (thissac.unix_root.equals(sac.unix_root)) {
								this
										.reportStreamError(
												HttpServletResponse.SC_CONFLICT,
												"_DEFINE_ARCHIVE_STORES: Unix root "
														+ thissac.unix_root
														+ " already exists in archive store "
														+ sac.archive_store,
												bwout);
								return false;
							}
							if (thissac.unix_root.startsWith(sac.unix_root)) {
								if ((thissac.unix_root.replace(sac.unix_root,
										"").startsWith("/"))) {
									this
											.reportStreamError(
													HttpServletResponse.SC_CONFLICT,
													"_DEFINE_ARCHIVE_STORES: Unix root "
															+ thissac.unix_root
															+ " is a child node of Unix root "
															+ sac.unix_root
															+ " that already exists in archive store "
															+ sac.archive_store,
													bwout);
									return false;
								}
							}
							if (sac.unix_root.startsWith(thissac.unix_root)) {
								if ((sac.unix_root.replace(thissac.unix_root,
										"").startsWith("/"))) {
									this
											.reportStreamError(
													HttpServletResponse.SC_CONFLICT,
													"_DEFINE_ARCHIVE_STORES: Unix root "
															+ thissac.unix_root
															+ " is a parent node of Unix root "
															+ sac.unix_root
															+ " that already exists in archive store "
															+ sac.archive_store,
													bwout);
									return false;
								}
							}
						}
					}
				}
			} catch (FinderException fex) {
				this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
						"_DEFINE_ARCHIVE_STORES: " + fex.getMessage(), bwout);
				return false;
			}
		}
		return true;
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