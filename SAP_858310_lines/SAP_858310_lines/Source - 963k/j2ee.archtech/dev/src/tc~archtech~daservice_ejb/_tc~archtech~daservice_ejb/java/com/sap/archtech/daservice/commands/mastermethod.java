package com.sap.archtech.daservice.commands;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;

import javax.ejb.FinderException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.CollectionData;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocal;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;
import com.sap.archtech.daservice.exceptions.ArchStoreConfigException;
import com.sap.archtech.daservice.exceptions.InvalidNameException;
import com.sap.archtech.daservice.exceptions.MissingParameterException;
import com.sap.archtech.daservice.exceptions.NoSuchDBObjectException;
import com.sap.archtech.daservice.exceptions.WrongArgumentException;
import com.sap.archtech.daservice.util.Director;
import com.sap.jmx.ObjectNameFactory;
import com.sap.security.core.server.destinations.api.DestinationException;
import com.sap.security.core.server.destinations.api.DestinationService;
import com.sap.security.core.server.destinations.api.HTTPDestination;
import com.sap.security.core.server.vsi.api.VSIService;
import com.sap.sld.api.builder.InvalidDataException;
import com.sap.sld.api.util.SystemExplorer;
import com.sap.sld.api.wbem.cim.CIMElementList;
import com.sap.sld.api.wbem.cim.CIMInstance;
import com.sap.sld.api.wbem.cim.CIMNamespace;
import com.sap.sld.api.wbem.cim.CIMReference;
import com.sap.sld.api.wbem.cim.CIM_Constants;
import com.sap.sld.api.wbem.client.WBEMClient;
import com.sap.sld.api.wbem.client.WBEMClientUtil;
import com.sap.sld.api.wbem.exception.CIMException;
import com.sap.sld.api.wbem.sap.SLDElementNames;
import com.sap.sldserv.SldApplicationServiceInterface;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public abstract class MasterMethod {

	public static final String DASVERSION = "7.3.0.00.01";
	public static final String JMSQUEUE = "DAserviceQueue";
	protected static final Location loc = Location
			.getLocation("com.sap.archtech.daservice");
	protected static final Category cat = Category.getCategory(
			Category.APPS_COMMON_ARCHIVING, "XML_DAS");
	protected static final String contentType = "application/octet-stream";
	private static final String SPECIALCHARS_RES = "_-.!~";
	private static final String SPECIALCHARS_URI = "_-.!~/";
	public final static String ORIGIN = "origin";
	public final static String COMPULSORY_DESTRUCTION_DATE = "compulsory_destruction_date";
	public final static String START_OF_RETENTION = "start_of_retention";
	public final static String EXPIRATION_DATE = "expiration_date";
	public final static String UNKNOWN = "unknown";

	public static String xmldasURL;
	public static boolean isSLDoutOfSync = true;
	public static VSIService vsiService;

	protected HttpServletResponse response;
	protected SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");
	public static String GLOBAL_DIR = "";

	public abstract boolean execute() throws IOException;

	protected void reportError(int errorCode, String message, Exception ex)
			throws IOException {
		MasterMethod.cat.logThrowableT(Severity.ERROR, loc, errorCode + " "
				+ message + ": " + ex.getMessage(), ex);
		this.response.setStatus(errorCode);
		String newmessage = message.replace('\n', ' ');
		this.response.setHeader("service_message", newmessage);
	}

	protected void reportError(int errorCode, String message)
			throws IOException {
		MasterMethod.cat.errorT(loc, errorCode + " " + message);
		this.response.setStatus(errorCode);
		String newmessage = message.replace('\n', ' ');
		this.response.setHeader("service_message", newmessage);
	}

	protected void reportStreamError(int errorCode, String message,
			BufferedWriter bw) throws IOException {
		MasterMethod.cat.errorT(loc, errorCode + " " + message);
		if (!response.isCommitted())
			this.response.setStatus(errorCode);
		this.writeStatus(bw, errorCode, message);
		bw.flush();
		bw.close();
	}

	protected void reportStreamError(int errorCode, String message,
			BufferedOutputStream bos) throws IOException {
		MasterMethod.cat.errorT(loc, errorCode + " " + message);
		if (!response.isCommitted()) {
			this.response.setStatus(errorCode);
		}
		this.writeStatus(bos, errorCode, message);
		bos.flush();
		bos.close();
	}

	protected void reportStreamError(int errorCode, String message,
			Exception ex, BufferedWriter bw) throws IOException {
		this.reportStreamError(errorCode, message, bw);
		MasterMethod.cat.logThrowableT(Severity.ERROR, loc, errorCode + " "
				+ message + ": " + ex.getMessage(), ex);
	}

	protected void reportStreamError(int errorCode, String message,
			Exception ex, BufferedOutputStream bos) throws IOException {
		this.reportStreamError(errorCode, message, bos);
		MasterMethod.cat.logThrowableT(Severity.ERROR, loc, errorCode + " "
				+ message + ": " + ex.getMessage(), ex);
	}

	protected void writeStatus(BufferedOutputStream bos, int errorCode,
			String message) throws IOException {
		String code = new Integer(errorCode).toString() + " ";
		if (message.length() > 500)
			message = message.substring(0, 499);
		bos.write(0x1F);
		bos.write(code.getBytes("UTF-8"));
		bos.write(message.getBytes("UTF-8"));
	}

	protected void writeStatus(BufferedWriter bw, int errorCode, String message)
			throws IOException {
		if (message.length() > 500)
			message = message.substring(0, 499);
		String code = new Integer(errorCode).toString() + " ";
		bw.write(0x1F);
		bw.write(code);
		bw.write(message);
	}

	protected void reportInfo(int errorCode, String message) {
		this.response.setStatus(errorCode);
		cat.infoT(loc, message);
		String newmessage = message.replace('\n', ' ');
		this.response.setHeader("service_message", newmessage);
	}

	protected void reportInfoTrace(int errorCode, String message) {
		this.response.setStatus(errorCode);
		loc.infoT(message);
		String newmessage = message.replace('\n', ' ');
		this.response.setHeader("service_message", newmessage);
	}

	protected void reportStreamInfo(int errorCode, String message,
			BufferedWriter bw) throws IOException {
		MasterMethod.cat.infoT(loc, errorCode + " " + message);
		if (!response.isCommitted())
			this.response.setStatus(errorCode);
		this.writeStatus(bw, errorCode, message);
		bw.flush();
		bw.close();
	}

	public static String getStackTrace(Throwable aThrowable) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}

	protected CollectionData checkCollection(String uri, PreparedStatement pst1)
			throws SQLException, MissingParameterException,
			WrongArgumentException, NoSuchDBObjectException {
		CollectionData coldat;
		long colId;
		long storeId;

		// check archive_path parameter
		if (uri == null || uri.equals("")) {
			MissingParameterException mpex = new MissingParameterException(
					"An existing archive path must be specified in archive_path");
			MasterMethod.cat.logThrowableT(Severity.ERROR, loc, mpex
					.getMessage(), mpex);
			throw mpex;
		}
		if (!(uri.indexOf("//") == -1) || !(uri.indexOf("\\") == -1)
				|| !uri.startsWith("/") || !uri.endsWith("/")) {
			WrongArgumentException waex = new WrongArgumentException(
					"Archive Path " + uri + " does not meet specifications");
			MasterMethod.cat.logThrowableT(Severity.ERROR, loc, waex
					.getMessage(), waex);
			throw waex;
		}
		// cut the last slash in the collection String
		if (!"/".equals(uri))
			uri = uri.substring(0, uri.length() - 1).toLowerCase();

		// check if collection exists
		// we need to fetch colId and storeId
		pst1.setString(1, uri.trim());
		ResultSet rs1 = pst1.executeQuery();
		if (rs1.next()) {
			colId = rs1.getLong("colId");
			storeId = rs1.getLong("storeId");
			coldat = new CollectionData(colId, storeId);
			rs1.close();
		} else {
			NoSuchDBObjectException nsoex = new NoSuchDBObjectException(
					"Collection " + uri + " does not exist");
			MasterMethod.cat.logThrowableT(Severity.ERROR, loc, nsoex
					.getMessage(), nsoex);
			throw nsoex;
		}
		return coldat;
	}

	protected void isValidName(String name, boolean isuri)
			throws InvalidNameException {
		char z;

		for (int i = 0; i < name.length(); i++) {
			z = name.charAt(i);
			if (!(Character.isLetterOrDigit(z))) {
				if (isuri) {
					if (SPECIALCHARS_URI.indexOf(z) == -1) {
						InvalidNameException ivex = new InvalidNameException(
								"Invalid Character " + z + " on position "
										+ (i + 1));
						MasterMethod.cat.logThrowableT(Severity.ERROR, loc,
								ivex.getMessage(), ivex);
						throw ivex;
					}
				} else {
					if (SPECIALCHARS_RES.indexOf(z) == -1) {
						InvalidNameException ivex = new InvalidNameException(
								"Invalid Character " + z + " on position "
										+ (i + 1));
						MasterMethod.cat.logThrowableT(Severity.ERROR, loc,
								ivex.getMessage(), ivex);
						throw ivex;
					}
				}
			}
		}
	}

	protected String getUTCString(Timestamp ts) {
		String utcdate;
		this.sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		utcdate = sdf.format(new java.util.Date(ts.getTime()
				+ (ts.getNanos() / 1000000)));
		return utcdate;
	}

	public Sapxmla_Config getArchStoreConfigObject(
			ArchStoreConfigLocalHome beanLocalHome, long id)
			throws ArchStoreConfigException {
		try {
			return beanLocalHome.findByPrimaryKey(new Long(id))
					.getSapxmla_Config();
		} catch (FinderException fiex) {
			MasterMethod.cat.logThrowableT(Severity.ERROR, loc,
					"Exception during Archive Store Configuration Object Search: "
							+ fiex.getMessage(), fiex);
			throw new ArchStoreConfigException(
					"Exception during Archive Store Configuration Object Search:"
							+ fiex.getMessage());
		}
	}

	public static String getXmlDasVersion() {
		return MasterMethod.DASVERSION;
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

	public static String getXmlDasUrl() {
		Context ctx = null;
		MBeanServer mbeanServer = null;
		try {
			ctx = new InitialContext();
			mbeanServer = (MBeanServer) ctx.lookup("jmx");
			if (mbeanServer == null) {
				throw new NamingException(
						"Access to MBean Server is not available");
			}
		} catch (NamingException nex) {
			cat
					.logThrowableT(
							Severity.WARNING,
							loc,
							"MasterMethod: Could not determine server name and port via MBean",
							nex);
			cat
					.infoT(loc,
							"MasterMethod: Default values will be used for building the URL");
		} finally {
			if (ctx != null) {
				try {
					ctx.close();
				} catch (NamingException e) {
					cat
							.logThrowableT(
									Severity.WARNING,
									loc,
									"MasterMethod: Could not determine server name and port via MBean",
									e);
					cat
							.infoT(loc,
									"MasterMethod: Default values will be used for building the URL");
				}
			}
		}

		// Set Default Values
		String host = "localhost";
		Integer port = Integer.valueOf(56000);
		if (mbeanServer != null) {
			try {

				// Lookup Local Instance Mbean
				ObjectName localInstancePattern = new ObjectName(
						new StringBuffer("*:").append(
								ObjectNameFactory.J2EETYPE_KEY).append('=')
								.append(ObjectNameFactory.SAP_J2EEInstance)
								.append(',').append(ObjectNameFactory.NAME_KEY)
								.append('=').append("local").append(",*")
								.toString());
				Set<ObjectName> result = mbeanServer.queryNames(
						localInstancePattern, null);
				if (!result.isEmpty()) {

					// Local Instance Mbean Found -> Read Its Attributes
					ObjectName localInstanceON = result.iterator().next();
					host = (String) mbeanServer.getAttribute(localInstanceON,
							"Host");
					port = (Integer) mbeanServer.getAttribute(localInstanceON,
							"HttpPort");
				} else {
					throw new RuntimeException(
							"MBean not found - the query was: "
									+ localInstancePattern);
				}
			} catch (Exception ex) {
				cat
						.logThrowableT(
								Severity.WARNING,
								loc,
								"MasterMethod: Could not determine server name and port via MBean",
								ex);
				cat
						.infoT(loc,
								"MasterMethod: Default values will be used for building the URL");
			}
		}
		return new StringBuffer("http://").append(host).append(':')
				.append(port).append("/DataArchivingService/DAS").toString();
	}

	public static void getVsiService() {

		// Check If Virus Scan Profile Is Active
		try {

			// Get Virus Scan Service
			vsiService = (VSIService) new InitialContext()
					.lookup(VSIService.JNDI_NAME);
		} catch (Exception ex) {
			// $JL-EXC$
			vsiService = null;
		}
	}

	synchronized public static void synchronizeSystemLandscapeDirectory() {

		// Check If Synchronization Is Necessary
		if (MasterMethod.isSLDoutOfSync == false)
			return;
		else
			MasterMethod.isSLDoutOfSync = false;

		// Declarations
		Context ctx = null;
		WBEMClient wbemClient = null;
		try {

			// Write Start Of SLD Synchronization Into Log
			MasterMethod.cat.infoT(loc,
					"MasterMethod: SLD synchronization was started");

			// Get Initial Context
			ctx = new InitialContext();

			// Determine Current XML DAS Name
			String xmldasName = "xmldas.SystemName."
					+ SystemExplorer.getJ2EEClusterEngineName();

			// Check If XML DAS Is Already Registered At SLD
			SldApplicationServiceInterface srvContext = (SldApplicationServiceInterface) (ctx
					.lookup(SldApplicationServiceInterface.KEY));
			wbemClient = srvContext.getWbemClient();
			wbemClient.setTargetNamespace(new CIMNamespace(
					CIM_Constants.DEFAULT_NAMESPACE.toString()));
			CIMElementList<CIMReference> xmldasServerList = wbemClient
					.enumerateInstanceNames(SLDElementNames.C_SAP_XMLDataArchivingServer);
			boolean isRegistered = false;
			for (Iterator<CIMReference> iter = xmldasServerList.iterator(); iter
					.hasNext();) {
				CIMReference ref = iter.next();
				if ((ref != null)
						&& (xmldasName.equals(ref
								.getValue(SLDElementNames.P_Name))))
					isRegistered = true;
			}

			// Check If Archive Stores Are Already Defined
			ArchStoreConfigLocalHome beanLocalHome = (ArchStoreConfigLocalHome) ctx
					.lookup("java:comp/env/ArchStoreConfigBean");
			Collection<ArchStoreConfigLocal> c = beanLocalHome.findAll();
			boolean hasArchiveStores = false;
			if (!c.isEmpty())
				hasArchiveStores = true;

			// Case Distinction
			if (hasArchiveStores == true) {

				// Insert Necessary XMLDataArchivingServer And ArchiveStore SLD
				// Entries
				if (MasterMethod.xmldasURL == null)
					MasterMethod.xmldasURL = MasterMethod.getXmlDasUrl();
				Director director = new Director();
				for (Iterator<ArchStoreConfigLocal> iter = c.iterator(); iter
						.hasNext();) {
					ArchStoreConfigLocal element = iter.next();
					Sapxmla_Config sac = element.getSapxmla_Config();
					String[] enginePropValues = new String[8];
					enginePropValues[0] = "XML Data Archiving Server";
					enginePropValues[1] = "tc/TechSrv/XML_DAS";
					enginePropValues[2] = "J2EE";
					enginePropValues[3] = "XML Data Archiving Server";
					enginePropValues[4] = "SAP standard";
					enginePropValues[5] = "SAP_J2EEEngineCluster";
					enginePropValues[6] = SystemExplorer
							.getJ2EEClusterEngineName();
					enginePropValues[7] = MasterMethod.xmldasURL;
					String archiveName = sac.archive_store
							+ ".XMLDASName.xmldas.SystemName."
							+ SystemExplorer.getJ2EEClusterEngineName();
					String[] archivePropValues = new String[12];
					archivePropValues[0] = sac.storage_system;
					archivePropValues[1] = sac.archive_store;
					archivePropValues[2] = sac.storage_system;
					archivePropValues[3] = sac.destination;
					archivePropValues[4] = "SAP standard";
					if (sac.type.equalsIgnoreCase("W")) {
						archivePropValues[5] = "WebDAV";
						archivePropValues[6] = "";
						try {
							archivePropValues[7] = getWebDavRoot(sac.destination);
						} catch (Exception ex) {

							// $JL-EXC$
							archivePropValues[7] = "";
						}
						archivePropValues[8] = "";
					} else {
						archivePropValues[5] = "Filesystem";
						archivePropValues[6] = sac.unix_root;
						archivePropValues[7] = "";
						archivePropValues[8] = sac.win_root;
					}
					archivePropValues[9] = "xmldas.SystemName."
							+ SystemExplorer.getJ2EEClusterEngineName();
					archivePropValues[10] = Short.toString(sac.ilm_conformance);
					if (sac.is_default.equalsIgnoreCase("Y"))
						archivePropValues[11] = "true";
					else
						archivePropValues[11] = "false";
					director.insertSldInstance(xmldasName, enginePropValues,
							archiveName, archivePropValues);

					// Write Action Into Log
					MasterMethod.cat.infoT(loc,
							"MasterMethod: SLD synchronization: SAP_XMLDataArchivingServer instance "
									+ xmldasName
									+ " and SAP_ArchiveStore instance "
									+ archiveName + " successfully updated");
				}

				// Delete Unnecessary ArchiveStore SLD Entries
				CIMElementList<CIMReference> archiveStoreList = wbemClient
						.enumerateInstanceNames(SLDElementNames.C_SAP_ArchiveStore);
				for (Iterator<CIMReference> iter = archiveStoreList.iterator(); iter
						.hasNext();) {
					CIMReference ref = iter.next();
					CIMInstance inst = WBEMClientUtil.getInstanceComplete(
							wbemClient, ref);
					if ((inst != null)
							&& (xmldasName.equals(inst.getProperty(
									SLDElementNames.P_XMLDASName).getValue()))) {
						c = beanLocalHome.findByArchiveStore(inst.getProperty(
								SLDElementNames.P_ArchiveStore).getValue());
						if (c.isEmpty()) {
							director.deleteSldInstance(null, ref
									.getValue(SLDElementNames.P_Name));

							// Write Action Into Log
							MasterMethod.cat
									.infoT(
											loc,
											"MasterMethod: SLD synchronization: SAP_ArchiveStore instance "
													+ ref
															.getValue(SLDElementNames.P_Name)
													+ " successfully deleted");
						}
					}
				}
			} else if (isRegistered == true) {

				// Delete Unnecessary XMLDataArchivingServer SLD Entry
				Director director = new Director();
				director.deleteSldInstance(xmldasName, null);

				// Write Action Into Log
				MasterMethod.cat.infoT(loc,
						"MasterMethod: SLD synchronization: SAP_XMLDataArchivingServer instance "
								+ xmldasName + " successfully deleted");
			}

			// Write Success Ending Of SLD Synchronization Into Log
			MasterMethod.cat.infoT(loc,
					"MasterMethod: SLD synchronization finished successfully");
		} catch (NamingException nex) {
			MasterMethod.isSLDoutOfSync = true;
			MasterMethod.cat
					.logThrowableT(
							Severity.WARNING,
							loc,
							"MasterMethod: An error occurred during the System Landscape Directory synchronization: "
									+ nex.getMessage(), nex);
		} catch (FinderException fex) {
			MasterMethod.isSLDoutOfSync = true;
			MasterMethod.cat
					.logThrowableT(
							Severity.WARNING,
							loc,
							"MasterMethod: An error occurred during the System Landscape Directory synchronization: "
									+ fex.getMessage(), fex);
		} catch (InvalidDataException idex) {
			MasterMethod.isSLDoutOfSync = true;
			MasterMethod.cat
					.logThrowableT(
							Severity.WARNING,
							loc,
							"MasterMethod: An error occurred during the System Landscape Directory synchronization: "
									+ idex.getMessage(), idex);
		} catch (CIMException cimex) {
			MasterMethod.isSLDoutOfSync = true;
			MasterMethod.cat
					.logThrowableT(
							Severity.WARNING,
							loc,
							"MasterMethod: An error occurred during the System Landscape Directory synchronization: "
									+ cimex.getMessage(), cimex);
		} catch (Exception ex) {
			MasterMethod.isSLDoutOfSync = true;
			MasterMethod.cat
					.logThrowableT(
							Severity.WARNING,
							loc,
							"MasterMethod: An error occurred during the System Landscape Directory synchronization: "
									+ ex.getMessage(), ex);
		} finally {

			// Disconnect WBEM Client
			if (wbemClient != null)
				wbemClient.disconnect();

			// Close Initial Context
			if (ctx != null) {
				try {
					ctx.close();
				} catch (NamingException nex) {
					cat.logThrowableT(Severity.WARNING, loc,
							"MasterMethod: Error occurred while closing initial context: "
									+ nex.getMessage(), nex);
				}
			}
		}
	}
}
