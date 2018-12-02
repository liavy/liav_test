package com.sap.archtech.daservice;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.TimeZone;

import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import javax.xml.validation.Schema;

import com.sap.archtech.daservice.commands.AssignArchiveStoresMethod;
import com.sap.archtech.daservice.commands.CheckMethod;
import com.sap.archtech.daservice.commands.ColSearchMethod;
import com.sap.archtech.daservice.commands.DefineArchiveStoresMethod;
import com.sap.archtech.daservice.commands.DeleteMethod;
import com.sap.archtech.daservice.commands.DeletionMarkMethod;
import com.sap.archtech.daservice.commands.FreezeMethod;
import com.sap.archtech.daservice.commands.GetMethod;
import com.sap.archtech.daservice.commands.GetStreamMethod;
import com.sap.archtech.daservice.commands.GetWebdavStoreMetaDataMethod;
import com.sap.archtech.daservice.commands.HeadMethod;
import com.sap.archtech.daservice.commands.IndexCreateMethod;
import com.sap.archtech.daservice.commands.IndexDescribeMethod;
import com.sap.archtech.daservice.commands.IndexDropMethod;
import com.sap.archtech.daservice.commands.IndexExistsMethod;
import com.sap.archtech.daservice.commands.IndexGetMethod;
import com.sap.archtech.daservice.commands.IndexInsertMethod;
import com.sap.archtech.daservice.commands.InfoMethod;
import com.sap.archtech.daservice.commands.InvalidUriGetMethod;
import com.sap.archtech.daservice.commands.InvalidUriSetMethod;
import com.sap.archtech.daservice.commands.LegalHoldAddMethod;
import com.sap.archtech.daservice.commands.LegalHoldGetMethod;
import com.sap.archtech.daservice.commands.LegalHoldRemoveMethod;
import com.sap.archtech.daservice.commands.ListArchivePathsMethod;
import com.sap.archtech.daservice.commands.ListArchiveStoresMethod;
import com.sap.archtech.daservice.commands.ListAssignedArchivePathsMethod;
import com.sap.archtech.daservice.commands.ListMethod;
import com.sap.archtech.daservice.commands.MasterMethod;
import com.sap.archtech.daservice.commands.MkcolMethod;
import com.sap.archtech.daservice.commands.ModifyPathMethod;
import com.sap.archtech.daservice.commands.OriginListMethod;
import com.sap.archtech.daservice.commands.OriginSearchMethod;
import com.sap.archtech.daservice.commands.PackStartJMS;
import com.sap.archtech.daservice.commands.PackStatusMethod;
import com.sap.archtech.daservice.commands.PickMethod;
import com.sap.archtech.daservice.commands.PropertyGetMethod;
import com.sap.archtech.daservice.commands.PropertySetMethod;
import com.sap.archtech.daservice.commands.PutMethod;
import com.sap.archtech.daservice.commands.ResetDelstatMethod;
import com.sap.archtech.daservice.commands.SelectMethod;
import com.sap.archtech.daservice.commands.SyncHomePathMethod;
import com.sap.archtech.daservice.commands.UnpackStartJMS;
import com.sap.archtech.daservice.commands.VerifyIntegrityMethod;
import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;
import com.sap.archtech.daservice.storage.XmlDasPut;
import com.sap.archtech.daservice.util.IdProvider;
import com.sap.archtech.daservice.util.WebDavTemplateProvider;
import com.sap.archtech.daservice.util.XmlSchemaProvider;
import com.sap.engine.services.applocking.TableLocking;
import com.sap.engine.services.configuration.appconfiguration.ApplicationConfigHandlerFactory;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.tssap.dtr.client.lib.protocol.pool.ConnectionPool;
import com.tssap.dtr.client.lib.protocol.pool.SynchronizedConnectionPool;
import com.tssap.dtr.client.lib.protocol.templates.SimpleTemplateProvider;

public class DASmain extends HttpServlet implements IDASMethods {

	public static final long serialVersionUID = 1234567890l;
	private static final Category cat = Category.getCategory(
			Category.APPS_COMMON_ARCHIVING, "XML_DAS");
	private static final Location loc = Location
			.getLocation("com.sap.archtech.daservice");
	public static final String XMLDAS = "XML Data Archiving Service";

	public static ArchStoreConfigLocalHome beanLocalHome;
	private HashMap<String, Integer> dbColumnLimits;
	private DataSource ds;
	private String instanceName;
	private HashMap<String, Integer> methodInts;
	private QueueConnectionFactory queueConnectionFactory;
	private ArrayList<String> resTypeList;
	private TableLocking tlock;
	private DirContext queueContext;

	// Servlet Initialization
	public void init() throws ServletException {

		// Write Initialize Log
		loc.entering("init() DASmain");
		java.sql.Connection connection = null;

		// Initialize MethodInt-Values In Hash map
		this.initMethodInts();

		// Get WebDAV Basic Authentication Data Settings, WebDAV Timeouts And
		// Connection Pool Limit From Configuration Manager
		Properties props = getConfigurationManagerEntries();
		WebDavTemplateProvider.WEBDAVCLIENTUSR = props
				.getProperty("WEBDAVCLIENTUSR");
		WebDavTemplateProvider.WEBDAVCLIENTPWD = props
				.getProperty("WEBDAVCLIENTPWD");
		WebDavTemplateProvider.WEBDAVREADTIMEOUT = Integer.parseInt(props
				.getProperty("WEBDAVREADTIMEOUT"));
		WebDavTemplateProvider.WEBDAVCONNECTTIMEOUT = Integer.parseInt(props
				.getProperty("WEBDAVCONNECTTIMEOUT"));
		WebDavTemplateProvider.WEBDAVEXPIRATIONTIMEOUT = Integer.parseInt(props
				.getProperty("WEBDAVEXPIRATIONTIMEOUT"));
		WebDavTemplateProvider.WEBDAVCONNECTIONTEMPLATELIMIT = Integer
				.parseInt(props.getProperty("WEBDAVCONNECTIONTEMPLATELIMIT"));

		// Resolve Global Directory Path
		getGlobalDir();

		// Get File System Synchronization Information From Configuration
		// Manager
		String fileSystemSync = props.getProperty("FILESYSTEMSYNC");
		if (fileSystemSync != null
				&& (fileSystemSync.startsWith("Y") || fileSystemSync
						.startsWith("y")))
			XmlDasPut.FILESYSTEMSYNC = true;
		else
			XmlDasPut.FILESYSTEMSYNC = false;

		try {
			// Get Reference to Table Locking API (application locking service)
			this.initLocking();

			// Initialize Connection Pool
			this.initConnectionPool();

			// Get Archive Store Configuration Home Reference
			this.getArchStoreConfigHomeRef();

			// Initialize WebDAV Connection Pool
			WebDavTemplateProvider.webDAVTemplates = new SimpleTemplateProvider();
			WebDavTemplateProvider.webDAVConnectionPool = new ConnectionPool(
					WebDavTemplateProvider.webDAVTemplates);
			WebDavTemplateProvider.webDAVSyncConnectionPool = new SynchronizedConnectionPool(
					WebDavTemplateProvider.webDAVConnectionPool);
			WebDavTemplateProvider.webDAVTemplateHashTable = new Hashtable<Long, Integer>();

			// Initialize XML Schema Cache
			XmlSchemaProvider.xmlSchemaMap = new HashMap<Long, Schema>();
			XmlSchemaProvider.xmlSchemaAccessList = new ArrayList<Long>();

			// Initialize JMS Stuff
			this.initJMS();
			connection = ds.getConnection();
			connection.setAutoCommit(false);

			// Read Allowed Resource Types
			this.readResTypes(connection);

			// Read Dynamically Length Of Some DB-Columns For Later Checks
			// NOTE: DatabaseMetaData.getColumn() is not supported by Open SQL
			// for Java!
			// Therefore, readDBLimits does not work
			// this.readDBLimits(connection);
			// As long as this does not work, hard-code the column lengths
			this.setDBLimits();

			// Get VSI References
			MasterMethod.getVsiService();

			// Synchronize System Landscape Directory Entries
			MasterMethod.synchronizeSystemLandscapeDirectory();

			// Write Exit Log
			connection.commit();
			loc.exiting();
		} catch (SQLException sqlex) {
			cat.logThrowableT(Severity.FATAL, loc,
					"Unable to get a DB connection: " + sqlex.getMessage(),
					sqlex);
			throw new ServletException("Unable to get a DB connection", sqlex);
		} catch (NamingException namex) {
			cat.logThrowableT(Severity.FATAL, loc,
					"Unable to lookup JMS connection factory: "
							+ namex.getMessage(), namex);
			throw new ServletException(
					"Unable to lookup JMS connection factory", namex);
		} catch (JMSException jmsex) {
			cat.logThrowableT(Severity.FATAL, loc,
					"Unable to create JMS topic: " + jmsex.getMessage(), jmsex);
			throw new ServletException("Unable to create JMS topic", jmsex);
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException sqlex) {
				cat.logThrowableT(Severity.FATAL, loc,
						"Unable to return DB connection to DB Pool: "
								+ sqlex.getMessage(), sqlex);
			}
		}
	}

	// HTTP Get Request
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		session.setAttribute("isUserValid", "x");
		session
				.setAttribute("AuthRequHead", request
						.getHeader("Authorization"));
		session.setAttribute("SessionUser", request.getRemoteUser());
		RequestDispatcher dispatcher = this.getServletContext()
				.getRequestDispatcher("/index.jsp");
		dispatcher.forward(request, response);
	}

	// HTTP Post Request
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Get HTTP Session
		HttpSession session = request.getSession(false);

		// Set Response Header "datetime"
		Timestamp dateTime = new Timestamp(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		response.setHeader("datetime", sdf.format(new java.util.Date(dateTime
				.getTime()
				+ (dateTime.getNanos() / 1000000))));

		// Get XML DAS URL
		if (MasterMethod.xmldasURL == null)
			MasterMethod.xmldasURL = request.getRequestURL().toString();

		// Define Variables
		MasterMethod archcomm = null;
		java.sql.Connection connection = null;
		java.sql.Connection connIdProv = null;
		boolean methodResult = false;
		IdProvider idProvider = null;

		// Find The Appropriate Method Integer Value
		String method = request.getHeader("method");
		if (method == null) {
			response.sendError(DasResponse.SC_PARAMETER_MISSING,
					"METHOD missing from request header");
			return;
		}
		if (method.equalsIgnoreCase("INDEX")
				|| method.equalsIgnoreCase("INVALIDURI")
				|| method.equalsIgnoreCase("PROPERTY")
				|| method.equalsIgnoreCase("LEGALHOLD")) {
			String submethod = request.getHeader("submethod");
			if (submethod == null) {
				response.sendError(DasResponse.SC_PARAMETER_MISSING,
						"SUBMETHOD missing from request header");
				return;
			} else
				method = method.concat(submethod);
		}
		method = method.toUpperCase();

		// Check Method Name
		Integer i = (Integer) this.methodInts.get(method);
		int command = 0;
		if (i != null)
			command = i.intValue();

		// Get One Or Two Connections
		try {
			try {
				connection = ds.getConnection();
				connection.setAutoCommit(false);
				if (method.startsWith("INDEX") || method.equals("MKCOL")
						|| method.equals("MODIFYPATH") || method.equals("PUT")
						|| method.equals("_DEFINE_ARCHIVE_STORES")
						|| method.equals("_SYNC_HOME_PATH")) {
					connIdProv = ds.getConnection();
					connIdProv.setAutoCommit(false);
					idProvider = new IdProvider(connIdProv);
				}
			} catch (SQLException sqlex) {
				cat.logThrowableT(Severity.ERROR, loc,
						"Unable to get a DB connection: " + sqlex.getMessage(),
						sqlex);
				response.sendError(DasResponse.SC_SQL_ERROR,
						"Database connection lost: " + sqlex.getMessage());
				return;
			}
			switch (command) {
			case CHECK:
				archcomm = new CheckMethod(response, connection, beanLocalHome,
						request.getHeader("uri"), request
								.getHeader("check_level"));
				break;
			case COLSEARCH:
				archcomm = new ColSearchMethod(request, response, connection,
						request.getHeader("colname_only"), request
								.getHeader("search_root"));
				break;
			case DELETE:
				archcomm = new DeleteMethod(response, connection,
						beanLocalHome, request.getHeader("uri"), request
								.getHeader("delete_range"));
				break;
			case DELETIONMARK:
				archcomm = new DeletionMarkMethod(response, connection, request
						.getHeader("archive_path"));
				break;
			case FREEZE:
				archcomm = new FreezeMethod(response, connection, request
						.getHeader("archive_path"));
				break;
			case GET:
				archcomm = new GetMethod(response, connection, beanLocalHome,
						request.getHeader("uri"),
						request.getHeader("checksum"), request
								.getHeader("mode"), request
								.getHeader("check_level"), request
								.getHeader("filename_win"), request
								.getHeader("filename_unx"));
				break;
			case GETSTREAM:
				archcomm = new GetStreamMethod(response, connection,
						beanLocalHome, request.getHeader("uri"), request
								.getHeader("offset"), request
								.getHeader("length"));
				break;
			case GET_WEBDAV_STORE_META_DATA:
				archcomm = new GetWebdavStoreMetaDataMethod(response,
						beanLocalHome, request.getHeader("archive_store"));
				break;
			case HEAD:
				archcomm = new HeadMethod(response, connection, request
						.getHeader("uri"));
				break;
			case INDEXCREATE:
				archcomm = new IndexCreateMethod(connection, idProvider,
						response, request, request.getHeader("index_name"),
						this.dbColumnLimits, request
								.getHeader("multi_val_support"));
				break;
			case INDEXDESCRIBE:
				archcomm = new IndexDescribeMethod(connection, response,
						request.getHeader("index_name"));
				break;
			case INDEXEXISTS:
				archcomm = new IndexExistsMethod(connection, response, request
						.getHeader("index_name"));
				break;
			case INDEXDROP:
				archcomm = new IndexDropMethod(connection, response, request
						.getHeader("index_name"));
				break;
			case INDEXGET:
				archcomm = new IndexGetMethod(connection, response, request
						.getHeader("index_name"), request.getHeader("uri"),
						request.getHeader("User-Agent"));
				break;
			case INDEXINSERT:
				archcomm = new IndexInsertMethod(request, response, connection,
						idProvider, request.getHeader("archive_path"), request
								.getHeader("resource_name"), request
								.getHeader("index_count"));
				break;
			case INFO:
				archcomm = new InfoMethod(response, connection, beanLocalHome,
						request.getHeader("archive_path"));
				break;
			case INVALIDURIGET:
				archcomm = new InvalidUriGetMethod(response, connection,
						request.getHeader("uri"));
				break;
			case INVALIDURISET:
				archcomm = new InvalidUriSetMethod(response, connection,
						request.getHeader("uri"), request.getHeader("invalid"),
						request.getHeader("user"), request.getHeader("reason"));
				break;
			case LEGALHOLDADD:
				archcomm = new LegalHoldAddMethod(request, response,
						connection, beanLocalHome, request
								.getHeader("ilm_case"), request
								.getHeader("att_col"));
				break;
			case LEGALHOLDGET:
				archcomm = new LegalHoldGetMethod(response, connection,
						beanLocalHome, request.getHeader("uri"));
				break;
			case LEGALHOLDREMOVE:
				archcomm = new LegalHoldRemoveMethod(request, response,
						connection, beanLocalHome, request
								.getHeader("ilm_case"), request
								.getHeader("att_col"));
				break;
			case LIST:
				archcomm = new ListMethod(connection, response, request,
						resTypeList, request.getHeader("archive_path"), request
								.getHeader("list_range"), request
								.getHeader("recursive"), request
								.getHeader("type"), request
								.getHeader("index_name"), request
								.getHeader("property_name"), request
								.getHeader("provide_resourcedata"), request
								.getHeader("provide_nr_of_hits"));
				break;
			case MKCOL:
				archcomm = new MkcolMethod(response, connection, idProvider,
						tlock, beanLocalHome,
						request.getHeader("archive_path"), request
								.getHeader("collection_name"), request
								.getHeader("user"), dateTime);
				break;
			case MODIFYPATH:
				archcomm = new ModifyPathMethod(response, connection,
						idProvider, tlock, beanLocalHome, request
								.getHeader("archive_path"), request
								.getHeader("user"), dateTime);
				break;
			case ORIGINLIST:
				archcomm = new OriginListMethod(request, response, connection);
				break;
			case ORIGINSEARCH:
				archcomm = new OriginSearchMethod(request, response,
						connection, request.getHeader("search_root"));
				break;
			case PACK:
				archcomm = new PackStartJMS(connection, queueContext,
						queueConnectionFactory, response, beanLocalHome,
						request.getHeader("archive_path"), request
								.getHeader("user"), dateTime);
				break;
			case PACKSTATUS:
				archcomm = new PackStatusMethod(connection, response, request
						.getHeader("archive_path"));
				break;
			case PICK:
				archcomm = new PickMethod(response, request, connection,
						request.getHeader("archive_path"), tlock, beanLocalHome);
				break;
			case PROPERTYGET:
				archcomm = new PropertyGetMethod(response, connection,
						beanLocalHome, request.getHeader("uri"), request
								.getHeader("property_name"), request
								.getHeader("suppress_log"));
				break;
			case PROPERTYSET:
				archcomm = new PropertySetMethod(request, response, connection,
						beanLocalHome, request.getHeader("uri"), request
								.getHeader("att_col"));
				break;
			case PUT:
				archcomm = new PutMethod(request, response, connection,
						idProvider, tlock, beanLocalHome, resTypeList, request
								.getHeader("type"), request
								.getHeader("archive_path"), request
								.getHeader("resource_name"), request
								.getHeader("user"), request.getHeader("mode"),
						request.getHeader("check_level"), request
								.getHeader("index_count"), request
								.getHeader("checksum"), dateTime, request
								.getHeader("filename_win"), request
								.getHeader("filename_unx"));
				break;
			case SELECT:
				archcomm = new SelectMethod(connection, response, request,
						request.getHeader("archive_path"), request
								.getHeader("recursive"), request
								.getHeader("maxhits"));
				break;
			case SESSIONINVALIDATE:
				if (session != null)
					session.invalidate();
				methodResult = true;
				response.setHeader("service_message", "Ok");
				break;
			case UNPACK:
				archcomm = new UnpackStartJMS(connection, queueContext,
						queueConnectionFactory, response, beanLocalHome,
						request.getHeader("archive_path"), request
								.getHeader("user"), dateTime);
				break;
			case _ASSIGN_ARCHIVE_STORES:
				archcomm = new AssignArchiveStoresMethod(response, connection,
						tlock, beanLocalHome, MasterMethod.xmldasURL, request
								.getHeader("action"),
						request.getHeader("user"), request
								.getHeader("archive_path"), request
								.getHeader("archive_store"));
				break;
			case _DEFINE_ARCHIVE_STORES:
				archcomm = new DefineArchiveStoresMethod(response, connection,
						idProvider, beanLocalHome, MasterMethod.xmldasURL,
						request.getHeader("action"), request.getHeader("user"),
						request.getHeader("archive_store"), request
								.getHeader("storage_system"), request
								.getHeader("store_type"), request
								.getHeader("win_root"), request
								.getHeader("unix_root"), request
								.getHeader("destination"), request
								.getHeader("proxy_host"), request
								.getHeader("proxy_port"), request
								.getHeader("is_default"));
				break;
			case _LIST_ARCHIVE_PATHS:
				archcomm = new ListArchivePathsMethod(response, connection,
						beanLocalHome, request.getHeader("type"), request
								.getHeader("user"), request
								.getHeader("archive_path"));
				break;
			case _LIST_ARCHIVE_STORES:
				archcomm = new ListArchiveStoresMethod(response, beanLocalHome,
						request.getHeader("type"), request.getHeader("user"));
				break;
			case _LIST_ASSIGNED_ARCHIVE_PATHS:
				archcomm = new ListAssignedArchivePathsMethod(response,
						connection, beanLocalHome, request.getHeader("user"),
						request.getHeader("archive_store"));
				break;
			case _RESET_DELSTAT:
				archcomm = new ResetDelstatMethod(response, connection, request
						.getHeader("archive_path"), tlock);
				break;
			case _SYNC_HOME_PATH:
				archcomm = new SyncHomePathMethod(response, connection,
						idProvider, tlock, beanLocalHome, request
								.getHeader("home_path"), request
								.getHeader("action"),
						request.getHeader("user"),
						request.getHeader("context"), request
								.getHeader("archive_store"), dateTime);
				break;
			case VERIFY_INTEGRITY:
				archcomm = new VerifyIntegrityMethod(response, connection,
						request.getHeader("archive_path"), request
								.getHeader("recursive"), request
								.getHeader("stop_at_end"), request
								.getHeader("user"), beanLocalHome,
						queueContext, queueConnectionFactory);
				break;

			// Invalid Method
			default:
				response.setContentType("text/xml");
			response
			.setHeader(
					"Allow",
					"Allow: CHECK, COLSEARCH, DELETE, DELETIONMARK, FREEZE, GET, GETSTREAM, GET_WEBDAV_STORE_META_DATA, HEAD, INDEX CREATE, INDEX DESCRIBE, INDEX DROP, INDEX EXISTS, INDEX GET, INDEX INSERT, INFO, INVALIDURI GET, INVALIDURI SET, LEGALHOLD ADD, LEGALHOLD GET, LEGALHOLD REMOVE, LIST, MKCOL, MODIFYPATH, ORIGINLIST, ORIGINSEARCH, PACK, PACKSTATUS, PICK, PROPERTY GET, PROPERTY SET, PUT, SELECT, SESSIONINVALIDATE, UNPACK, VERIFY_INTEGRITY, _ASSIGN_ARCHIVE_STORES, _DEFINE_ARCHIVE_STORES, _LIST_ARCHIVE_PATHS, _LIST_ARCHIVE_STORES, _LIST_ASSIGNED_ARCHIVE_PATHS, _RESET_DELSTAT, _SYNC_HOME_PATH");
				response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
						"Unknown DataArchivingService method");
				break;
			}

			// Execute The Command
			if (archcomm != null)
				methodResult = archcomm.execute();
		} finally {

			// Commit DB Connections -> Note: connIdProv Is Committed In
			// IdProvider
			try {
				if (methodResult)
					connection.commit();
				else
					connection.rollback();
			} catch (SQLException sqlex) {
				if (methodResult) {
					cat.logThrowableT(Severity.ERROR, loc,
							"Unable to commit the XML DAS DB transaction: "
									+ sqlex.getMessage(), sqlex);
					response.sendError(DasResponse.SC_SQL_ERROR,
							"Unable to commit the XML DAS DB transaction: "
									+ sqlex.getMessage());
				} else {
					cat.logThrowableT(Severity.ERROR, loc,
							"Unable to rollback the XML DAS DB transaction: "
									+ sqlex.getMessage(), sqlex);
					response.sendError(DasResponse.SC_SQL_ERROR,
							"Unable to rollback the XML DAS DB transaction: "
									+ sqlex.getMessage());
				}
			} finally {

				// Close DB Connections
				try {
					if (connection != null)
						connection.close();
					if (connIdProv != null)
						connIdProv.close();
				} catch (SQLException sqlex) {
					cat.logThrowableT(Severity.ERROR, loc,
							"Unable to close a DB connection: "
									+ sqlex.getMessage(), sqlex);
				}
			}
		}
	}

	// Servlet Destruction
	public void destroy() {
	}

	// Get Archive Store Configuration Home Reference
	private void getArchStoreConfigHomeRef() throws NamingException {
		Context ctx = new InitialContext();
		DASmain.beanLocalHome = (ArchStoreConfigLocalHome) ctx
				.lookup("java:comp/env/ArchStoreConfigBean");
	}

	// Initialize DB Connection Pool
	private void initConnectionPool() throws ServletException {
		try {

			// Obtain Our Environment Naming Context
			Context initCtx = new InitialContext();

			// Look Up Our Data Source
			this.ds = (DataSource) initCtx.lookup("java:comp/env/SAP/BC_XMLA");
		} catch (NamingException namex) {
			cat.logThrowableT(Severity.FATAL, loc,
					"Unable to create Connection Pool:" + namex.getMessage(),
					namex);
			throw new ServletException("Unable to create Connection Pool");
		}
	}

	// Initialize JMS
	private void initJMS() throws NamingException, JMSException {
		InitialContext initCon = new InitialContext();
		instanceName = getInitParameter("instance");
		if (instanceName == null) {
			instanceName = "default";
			queueConnectionFactory = (QueueConnectionFactory) initCon
					.lookup("jmsfactory/" + instanceName
							+ "/DAserviceQueueFactory");
		} else
			queueConnectionFactory = (QueueConnectionFactory) initCon
					.lookup("jmsfactory/" + instanceName
							+ "/DAserviceQueueFactory");

		queueContext = (DirContext) initCon.lookup("jmsqueues/" + instanceName);
	}

	private void initLocking() throws NamingException {
		Context initCtx = new InitialContext();
		tlock = (TableLocking) initCtx.lookup(TableLocking.JNDI_NAME);
	}

	// Assign Integer Number To Each Method
	private void initMethodInts() {
		this.methodInts = new HashMap<String, Integer>();
		this.methodInts.put("CHECK", new Integer(CHECK));
		this.methodInts.put("COLSEARCH", new Integer(COLSEARCH));
		this.methodInts.put("DELETE", new Integer(DELETE));
		this.methodInts.put("DELETIONMARK", new Integer(DELETIONMARK));
		this.methodInts.put("FREEZE", new Integer(FREEZE));
		this.methodInts.put("GET", new Integer(GET));
		this.methodInts.put("GETSTREAM", new Integer(GETSTREAM));
		this.methodInts.put("GET_WEBDAV_STORE_META_DATA", new Integer(
				GET_WEBDAV_STORE_META_DATA));
		this.methodInts.put("HEAD", new Integer(HEAD));
		this.methodInts.put("INDEXCREATE", new Integer(INDEXCREATE));
		this.methodInts.put("INDEXDELETE", new Integer(INDEXDELETE));
		this.methodInts.put("INDEXDESCRIBE", new Integer(INDEXDESCRIBE));
		this.methodInts.put("INDEXDROP", new Integer(INDEXDROP));
		this.methodInts.put("INDEXEXISTS", new Integer(INDEXEXISTS));
		this.methodInts.put("INDEXGET", new Integer(INDEXGET));
		this.methodInts.put("INDEXINSERT", new Integer(INDEXINSERT));
		this.methodInts.put("INFO", new Integer(INFO));
		this.methodInts.put("INVALIDURIGET", new Integer(INVALIDURIGET));
		this.methodInts.put("INVALIDURISET", new Integer(INVALIDURISET));
		this.methodInts.put("LEGALHOLDADD", new Integer(LEGALHOLDADD));
		this.methodInts.put("LEGALHOLDGET", new Integer(LEGALHOLDGET));
		this.methodInts.put("LEGALHOLDREMOVE", new Integer(LEGALHOLDREMOVE));
		this.methodInts.put("LIST", new Integer(LIST));
		this.methodInts.put("MKCOL", new Integer(MKCOL));
		this.methodInts.put("MODIFYPATH", new Integer(MODIFYPATH));
		this.methodInts.put("ORIGINLIST", new Integer(ORIGINLIST));
		this.methodInts.put("ORIGINSEARCH", new Integer(ORIGINSEARCH));
		this.methodInts.put("PACK", new Integer(PACK));
		this.methodInts.put("PACKSTATUS", new Integer(PACKSTATUS));
		this.methodInts.put("PICK", new Integer(PICK));
		this.methodInts.put("PROPERTYGET", new Integer(PROPERTYGET));
		this.methodInts.put("PROPERTYSET", new Integer(PROPERTYSET));
		this.methodInts.put("PUT", new Integer(PUT));
		this.methodInts.put("SELECT", new Integer(SELECT));
		this.methodInts
		.put("SESSIONINVALIDATE", new Integer(SESSIONINVALIDATE));
		this.methodInts.put("UNPACK", new Integer(UNPACK));
		this.methodInts.put("VERIFY_INTEGRITY", new Integer(VERIFY_INTEGRITY));
		this.methodInts.put("_ASSIGN_ARCHIVE_STORES", new Integer(
				_ASSIGN_ARCHIVE_STORES));
		this.methodInts.put("_DEFINE_ARCHIVE_STORES", new Integer(
				_DEFINE_ARCHIVE_STORES));
		this.methodInts.put("_LIST_ARCHIVE_PATHS", new Integer(
				_LIST_ARCHIVE_PATHS));
		this.methodInts.put("_LIST_ARCHIVE_STORES", new Integer(
				_LIST_ARCHIVE_STORES));
		this.methodInts.put("_LIST_ASSIGNED_ARCHIVE_PATHS", new Integer(
				_LIST_ASSIGNED_ARCHIVE_PATHS));
		this.methodInts.put("_RESET_DELSTAT", new Integer(_RESET_DELSTAT));
		this.methodInts.put("_SYNC_HOME_PATH", new Integer(_SYNC_HOME_PATH));
	}

	// Get Data From Configuration Manager
	private Properties getConfigurationManagerEntries() throws ServletException {
		try {

			// Obtain JNDI Context
			Context ctx = new InitialContext();

			// Get ApplicationConfigHandlerFactory Instance From JNDI
			ApplicationConfigHandlerFactory appCfgHdlFctry = (ApplicationConfigHandlerFactory) ctx
					.lookup("ApplicationConfiguration");

			// Get Properties From sap.application.global.properties
			Properties appProps = appCfgHdlFctry.getApplicationProperties();

			if (appProps == null) {
				ServletException svlex = new ServletException(
						"Property file is empty");
				cat.logThrowableT(Severity.ERROR, loc, svlex.getMessage(),
						svlex);
				throw svlex;
			} else
				return appProps;
		} catch (NamingException nex) {
			cat.logThrowableT(Severity.ERROR, loc,
					"Unable to lookup Configuration Manager: "
							+ nex.getMessage(), nex);
			throw new ServletException(
					"Unable to lookup Configuration Manager: ", nex);
		}
	}

	// Get Global Directory
	private void getGlobalDir() throws ServletException {
		try {
			Context ctx = new InitialContext();
			ApplicationConfigHandlerFactory appCfgHdl = (ApplicationConfigHandlerFactory) ctx
					.lookup("ApplicationConfiguration");
			MasterMethod.GLOBAL_DIR = appCfgHdl.getSystemProfile().getProperty(
					"SYS_GLOBAL_DIR");
		} catch (NamingException nex) {
			cat.logThrowableT(Severity.ERROR, loc,
					"Unable to lookup Configuration Manager: "
							+ nex.getMessage(), nex);
			throw new ServletException(
					"Unable to lookup Configuration Manager: ", nex);
		}
	}

	private void readObject(ObjectInputStream oin)
			throws ClassNotFoundException, IOException {
		// $JL-LOG_AND_TRACE$ ignored
		cat.errorT(loc, "Trying to deserialize DAS, this will not work.");
		throw new IOException("Error trying to deserialize DASmain.");
	}

	// Obtain Possible Resource Types
	private void readResTypes(java.sql.Connection conn) throws SQLException {
		this.resTypeList = new ArrayList<String>();
		Statement stmt = conn.createStatement();
		ResultSet rst = stmt.executeQuery("SELECT * FROM BC_XMLA_RESTYPES");
		while (rst.next())
			resTypeList.add(rst.getString("RESTYPE"));
		rst.close();
		stmt.close();
	}

	// Set DB Limitations
	private void setDBLimits() {
		dbColumnLimits = new HashMap<String, Integer>();
		dbColumnLimits.put("index_dict.indexName", new Integer(255));
		dbColumnLimits.put("index_cols.propName", new Integer(30));
		dbColumnLimits.put("res.resName", new Integer(100));
	}

	/*
	 * Serialization Problem This class extends HttpServlet which implements
	 * java.io.Serializable. The following instance variables are not
	 * serializable: DataSource ds SynResPicker synrespicker DirContext
	 * topicContext QueueConnectionFactory queueConnectionFactory
	 * 
	 * It does not make sense to make them transient. It is not clear (for me)
	 * if and when serialization of a servlet is needed. By implementing the two
	 * methods below (originally in ObjectOutputStream and ObjectInputStream) an
	 * Exception is thrown and a log message is created if there is an attempt
	 * to (de)serialize this servlet. The methods are called "by magic" (not
	 * declared in any Interface but called automatically by the JVM). JLin
	 * Serialization check errors are avoided.
	 * 
	 */
	private void writeObject(ObjectOutputStream oout) throws IOException {
		// $JL-LOG_AND_TRACE$ ignored
		cat.errorT(loc, "Trying to serialize DAS, this will not work.");
		throw new IOException("Error trying to serialize DASmain.");
	}
}