package com.sap.archtech.daservice.beanfacade;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jms.QueueConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.sql.DataSource;

import com.sap.archtech.archconn.values.ArchiveStoreData;
import com.sap.archtech.daservice.commands.AssignArchiveStoresMethod;
import com.sap.archtech.daservice.commands.DefineArchiveStoresMethod;
import com.sap.archtech.daservice.commands.InfoMethod;
import com.sap.archtech.daservice.commands.ListArchivePathsMethod;
import com.sap.archtech.daservice.commands.MasterMethod;
import com.sap.archtech.daservice.commands.PackStartJMS;
import com.sap.archtech.daservice.commands.PackStatusMethod;
import com.sap.archtech.daservice.commands.SyncHomePathMethod;
import com.sap.archtech.daservice.commands.UnpackStartJMS;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;
import com.sap.archtech.daservice.util.IdProvider;
import com.sap.engine.services.applocking.TableLocking;
import com.sap.sldserv.exception.SldServiceRuntimeException;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class CommandBean implements SessionBean {
		
	private static final long serialVersionUID = 1234567890l;
		private static final Location loc = Location
			.getLocation("com.sap.archtech.daservice.beanfacade.CommandBean");
	private static final Category cat = Category.getCategory(
			Category.APPS_COMMON_ARCHIVING, "XML_DAS");
	
	private transient DataSource dataSource;
	private transient ArchStoreConfigLocalHome archStoreConfigBeanHome;
	private transient QueueConnectionFactory queueConnectionFactory;
	private transient DirContext queueContext;
	private transient SessionContext sessionContext;

	public void setSessionContext(SessionContext sessionContext) {
		this.sessionContext = sessionContext;
	}

	public void ejbCreate() throws CreateException {
		try {
			onBeanLoad();
		} catch (NamingException e) {
			throw new CreateException(e.getMessage());
		}
	}

	public void ejbRemove() {
	}

	public void ejbActivate() throws EJBException {
		try {
			onBeanLoad();
		} catch (NamingException e) {
			throw new EJBException(e);
		}
	}

	public void ejbPassivate() {
	}

	public ServiceResponse checkServiceConnection() throws IOException,
			SQLException {

		// Synchronize SLD
		MasterMethod.synchronizeSystemLandscapeDirectory();

		Connection connection = getConnection();
		CommandResponse cmdResponse = new CommandResponse();
		// delegate to INFO command
		executeCommand(connection, null, new InfoMethod(cmdResponse,
				connection, archStoreConfigBeanHome, null));
		return createServiceResponse(cmdResponse, true);
	}

	public ObjectListResponse getArchiveStores() throws IOException,
			SQLException {
		Connection connection = getConnection();
		CommandResponse cmdResponse = new CommandResponse();
		Connection connIdProvider = getConnection();
		IdProvider idProvider = new IdProvider(connIdProvider);
		String user = sessionContext.getCallerPrincipal().getName();
		// delegate to DEFINE_ARCHIVE_STORES command
		DefineArchiveStoresMethod method = new DefineArchiveStoresMethod(
				cmdResponse, connection, idProvider, archStoreConfigBeanHome,
				"", "L", user, "", "", "", "", "", "", "", "", "");
		executeCommand(connection, connIdProvider, method);
		return createObjectListResponse(cmdResponse, false);
	}

	public ServiceResponse defineArchiveStore(ArchiveStoreData archiveStore)
			throws IOException, SQLException {
		return defineArchStore(archiveStore).getServiceResponse();
	}

	public ObjectListResponse defineArchStore(ArchiveStoreData archiveStore)
			throws IOException, SQLException {
		Connection connection = getConnection();
		CommandResponse cmdResponse = new CommandResponse();
		Connection connIdProvider = getConnection();
		IdProvider idProvider = new IdProvider(connIdProvider);
		String user = sessionContext.getCallerPrincipal().getName();
		// delegate to DEFINE_ARCHIVE_STORES command
		if (MasterMethod.xmldasURL == null)
			MasterMethod.xmldasURL = MasterMethod.getXmlDasUrl();
		DefineArchiveStoresMethod method = new DefineArchiveStoresMethod(
				cmdResponse, connection, idProvider, archStoreConfigBeanHome,
				MasterMethod.xmldasURL, "I", user, archiveStore
						.getArchiveStore(), archiveStore.getStorageSystem(),
				archiveStore.getStoreType(), archiveStore.getWinRoot(),
				archiveStore.getUnixRoot(), archiveStore.getDestination(),
				archiveStore.getProxyHost(), Integer.valueOf(archiveStore.getProxyPort()).toString()
					, archiveStore.getIsDefault());
		try {
			executeCommand(connection, connIdProvider, method);
		} catch (SldServiceRuntimeException e) {
			cat
					.logThrowableT(
							Severity.WARNING,
							loc,
							"Archive store definition: Failed to update System Landscape Directory",
							e);
		}
		return createObjectListResponse(cmdResponse, false);
	}

	public ServiceResponse updateArchiveStore(ArchiveStoreData archiveStore)
			throws IOException, SQLException {
		return updateArchStore(archiveStore).getServiceResponse();
	}

	public ObjectListResponse updateArchStore(ArchiveStoreData archiveStore)
			throws IOException, SQLException {
		Connection connection = getConnection();
		CommandResponse cmdResponse = new CommandResponse();
		Connection connIdProvider = getConnection();
		IdProvider idProvider = new IdProvider(connIdProvider);
		String user = sessionContext.getCallerPrincipal().getName();
		// delegate to DEFINE_ARCHIVE_STORES command
		DefineArchiveStoresMethod method = new DefineArchiveStoresMethod(
				cmdResponse, connection, idProvider, archStoreConfigBeanHome,
				"", "U", user, archiveStore.getArchiveStore(), archiveStore
						.getStorageSystem(), archiveStore.getStoreType(),
				archiveStore.getWinRoot(), archiveStore.getUnixRoot(),
				archiveStore.getDestination(), archiveStore.getProxyHost(),
				Integer.valueOf(archiveStore.getProxyPort()).toString(),
				archiveStore.getIsDefault());
		try {
			executeCommand(connection, connIdProvider, method);
		} catch (SldServiceRuntimeException e) {
			cat
					.logThrowableT(
							Severity.WARNING,
							loc,
							"Update of archive store definition: Failed to update System Landscape Directory",
							e);
		}
		return createObjectListResponse(cmdResponse, false);
	}

	public ServiceResponse deleteArchiveStore(String archStoreName)
			throws IOException, SQLException {
		Connection connection = getConnection();
		CommandResponse cmdResponse = new CommandResponse();
		Connection connIdProvider = getConnection();
		IdProvider idProvider = new IdProvider(connIdProvider);
		String user = sessionContext.getCallerPrincipal().getName();
		// delegate to DEFINE_ARCHIVE_STORES command
		DefineArchiveStoresMethod method = new DefineArchiveStoresMethod(
				cmdResponse, connection, idProvider, archStoreConfigBeanHome,
				"", "D", user, archStoreName, "", "", "", "", "", "", "", "");
		try {
			executeCommand(connection, connIdProvider, method);
		} catch (SldServiceRuntimeException e) {
			cat
					.logThrowableT(
							Severity.WARNING,
							loc,
							"Deletion of archive store definition: Failed to update System Landscape Directory",
							e);
		}
		return createServiceResponse(cmdResponse, false);
	}

	public ObjectListResponse testArchiveStore(String archStoreName)
			throws IOException, SQLException {
		Connection connection = getConnection();
		CommandResponse cmdResponse = new CommandResponse();
		Connection connIdProvider = getConnection();
		IdProvider idProvider = new IdProvider(connIdProvider);
		String user = sessionContext.getCallerPrincipal().getName();
		// delegate to DEFINE_ARCHIVE_STORES command
		DefineArchiveStoresMethod method = new DefineArchiveStoresMethod(
				cmdResponse, connection, idProvider, archStoreConfigBeanHome,
				"", "T", user, archStoreName, "", "", "", "", "", "", "", "");
		try {
			executeCommand(connection, connIdProvider, method);
		} catch (SldServiceRuntimeException e) {
			cat
					.logThrowableT(
							Severity.WARNING,
							loc,
							"Test of archive store definition: Failed to update System Landscape Directory",
							e);
		}
		return createObjectListResponse(cmdResponse, false);
	}

	public ObjectListResponse getArchivePaths() throws IOException,
			SQLException, NamingException {
		Connection connection = getConnection();
		CommandResponse cmdResponse = new CommandResponse();
		String user = sessionContext.getCallerPrincipal().getName();
		AssignArchiveStoresMethod method = new AssignArchiveStoresMethod(
				cmdResponse, connection, getLockingObject(),
				archStoreConfigBeanHome, "", "L", user, null, "");
		executeCommand(connection, null, method);
		return createObjectListResponse(cmdResponse, false);
	}

	public ObjectListResponse assignArchiveStore(String archStoreName,
			String archivePath) throws IOException, SQLException,
			NamingException {
		Connection connection = getConnection();
		CommandResponse cmdResponse = new CommandResponse();
		String user = sessionContext.getCallerPrincipal().getName();
		AssignArchiveStoresMethod method = new AssignArchiveStoresMethod(
				cmdResponse, connection, getLockingObject(),
				archStoreConfigBeanHome, MasterMethod.getXmlDasUrl(), "A",
				user, archivePath, archStoreName);
		executeCommand(connection, null, method);
		return createObjectListResponse(cmdResponse, false);
	}

	public ObjectListResponse unassignArchiveStore(String archivePath)
			throws IOException, SQLException, NamingException {
		Connection connection = getConnection();
		CommandResponse cmdResponse = new CommandResponse();
		String user = sessionContext.getCallerPrincipal().getName();
		AssignArchiveStoresMethod method = new AssignArchiveStoresMethod(
				cmdResponse, connection, getLockingObject(),
				archStoreConfigBeanHome, "", "U", user, archivePath, "");
		executeCommand(connection, null, method);
		return createObjectListResponse(cmdResponse, false);
	}

	public ObjectListResponse getArchivePaths(String archivePathType,
			String rootArchivePath) throws IOException, SQLException {
		Connection connection = getConnection();
		CommandResponse cmdResponse = new CommandResponse();
		String user = sessionContext.getCallerPrincipal().getName();
		ListArchivePathsMethod method = new ListArchivePathsMethod(cmdResponse,
				connection, archStoreConfigBeanHome, archivePathType, user,
				rootArchivePath);
		executeCommand(connection, null, method);
		return createObjectListResponse(cmdResponse, false);
	}

	public ServiceResponse packResources(String archivePath)
			throws IOException, SQLException {
		Connection connection = getConnection();
		CommandResponse cmdResponse = new CommandResponse();
		String user = sessionContext.getCallerPrincipal().getName();
		PackStartJMS method = new PackStartJMS(connection, queueContext,
				queueConnectionFactory, cmdResponse, archStoreConfigBeanHome,
				archivePath, user, new Timestamp(System.currentTimeMillis()));
		executeCommand(connection, null, method);
		return createServiceResponse(cmdResponse, false);
	}

	public ServiceResponse unpackResources(String archivePath)
			throws IOException, SQLException {
		Connection connection = getConnection();
		CommandResponse cmdResponse = new CommandResponse();
		String user = sessionContext.getCallerPrincipal().getName();
		UnpackStartJMS method = new UnpackStartJMS(connection, queueContext,
				queueConnectionFactory, cmdResponse, archStoreConfigBeanHome,
				archivePath, user, new Timestamp(System.currentTimeMillis()));
		executeCommand(connection, null, method);
		return createServiceResponse(cmdResponse, false);
	}

	public PackStatusResponse getPackStatus(String archivePath)
			throws IOException, SQLException {
		Connection connection = getConnection();
		CommandResponse cmdResponse = new CommandResponse();
		PackStatusMethod method = new PackStatusMethod(connection, cmdResponse,
				archivePath);
		executeCommand(connection, null, method);
		return createPackStatusResponse(cmdResponse, false);
	}

	public ServiceResponse insertHomepath(String homePath,
			String archStoreName, String context) throws IOException,
			SQLException, NamingException {
		Connection connection = getConnection();
		CommandResponse cmdResponse = new CommandResponse();
		Connection connIdProvider = getConnection();
		IdProvider idProvider = new IdProvider(connIdProvider);
		String user = sessionContext.getCallerPrincipal().getName();
		SyncHomePathMethod method = new SyncHomePathMethod(cmdResponse,
				connection, idProvider, getLockingObject(),
				archStoreConfigBeanHome, homePath, "I", user, context,
				archStoreName, new Timestamp(System.currentTimeMillis()));
		executeCommand(connection, connIdProvider, method);
		return createServiceResponse(cmdResponse, false);
	}

	public ServiceResponse deleteHomepath(String homePath, String context)
			throws IOException, SQLException, NamingException {
		Connection connection = getConnection();
		CommandResponse cmdResponse = new CommandResponse();
		Connection connIdProvider = getConnection();
		IdProvider idProvider = new IdProvider(connIdProvider);
		String user = sessionContext.getCallerPrincipal().getName();
		SyncHomePathMethod method = new SyncHomePathMethod(cmdResponse,
				connection, idProvider, getLockingObject(),
				archStoreConfigBeanHome, homePath, "D", user, context, "",
				new Timestamp(System.currentTimeMillis()));
		executeCommand(connection, connIdProvider, method);
		return createServiceResponse(cmdResponse, false);
	}

	private void onBeanLoad() throws NamingException {
		Context initCtx = null;
		try {
			initCtx = new InitialContext();
			dataSource = (DataSource) initCtx
					.lookup("java:comp/env/SAP/BC_XMLA");
			archStoreConfigBeanHome = (ArchStoreConfigLocalHome) initCtx
					.lookup("java:comp/env/ArchStoreConfigBean");
			queueConnectionFactory = (QueueConnectionFactory) initCtx
					.lookup("jmsfactory/default/DAserviceQueueFactory");
			queueContext = (DirContext) initCtx.lookup("jmsqueues/default");
		} finally {
			if (initCtx != null) {
				initCtx.close();
			}
		}
	}

	private Connection getConnection() throws SQLException {
		Connection connection = dataSource.getConnection();
		connection.setAutoCommit(false);
		return connection;
	}

	private void executeCommand(final Connection connection,
			final Connection idProvConnection, final MasterMethod command)
			throws IOException, SQLException {
		try {
			boolean isOK = command.execute();
			if (isOK) {
				connection.commit();
			} else {
				connection.rollback();
			}
		} finally {
			connection.close();
			if (idProvConnection != null) {
				idProvConnection.close();
			}
		}
	}

	private ServiceResponse createServiceResponse(CommandResponse cmdResponse,
			boolean addOptionalProps) throws IOException {
		// commit the response
		cmdResponse.flushBuffer();
		int status = getStatusCode(cmdResponse);
		String serviceMessage = cmdResponse
				.getHeaderProperty("service_message");
		String protocolMessage = getProtocolMessage(cmdResponse);
		// optional properties
		if (addOptionalProps) {
			String xmldasName = cmdResponse.getHeaderProperty("xmldas_name");
			String xmldasRelease = cmdResponse.getHeaderProperty("release");
			return new ServiceResponse(status, serviceMessage, protocolMessage,
					xmldasName, xmldasRelease);
		}
		return new ServiceResponse(status, serviceMessage, protocolMessage);
	}

	private String getProtocolMessage(CommandResponse cmdResponse)
			throws IOException {
		try {
			String protMsg = ((CommandResponseSerializer) cmdResponse
					.getOutputStream()).getProtocolMessage(cmdResponse
					.getStatus());
			if (protMsg == null) {
				protMsg = "";
			}
			return protMsg;
		} catch (ClassNotFoundException e) {
			throw new IOException(e.getMessage());
		}
	}

	private int getStatusCode(CommandResponse cmdResponse) throws IOException {
		try {
			int statusCode = ((CommandResponseSerializer) cmdResponse
					.getOutputStream()).getResponseStreamStatus(cmdResponse
					.getStatus());
			if (statusCode == -1) {
				// fall back: use status code stored in response header
				statusCode = cmdResponse.getStatus();
			}
			return statusCode;
		} catch (ClassNotFoundException e) {
			throw new IOException(e.getMessage());
		}
	}

	private ObjectListResponse createObjectListResponse(
			CommandResponse cmdResponse, boolean addOptionalProps)
			throws IOException {
		ServiceResponse serviceResponse = createServiceResponse(cmdResponse,
				addOptionalProps);
		List<Object> objectList = getObjectList(cmdResponse);
		return new ObjectListResponse(serviceResponse, objectList);
	}

	private ArrayList<Object> getObjectList(CommandResponse cmdResponse)
			throws IOException {
		try {
			Object deserializedObject = ((CommandResponseSerializer) cmdResponse
					.getOutputStream()).getSerializedObjectData(cmdResponse
					.getStatus());
			// deserialized object is expected to be an ArrayList
			if (deserializedObject == null) {
				return new ArrayList<Object>(0);
			}
			return (ArrayList<Object>) deserializedObject;
		} catch (ClassNotFoundException e) {
			throw new IOException(e.getMessage());
		} catch (ClassCastException e) {
			// no array list !!
			throw new IOException(e.getMessage());
		}
	}

	private PackStatusResponse createPackStatusResponse(
			CommandResponse cmdResponse, boolean addOptionalProps)
			throws IOException {
		ServiceResponse serviceResponse = createServiceResponse(cmdResponse,
				addOptionalProps);
		String packStatus = cmdResponse.getHeaderProperty("packstatus");
		return new PackStatusResponse(serviceResponse, packStatus);
	}

	private TableLocking getLockingObject() throws NamingException {
		Context initCtx = new InitialContext();
		return (TableLocking) initCtx.lookup(TableLocking.JNDI_NAME);
	}
}
