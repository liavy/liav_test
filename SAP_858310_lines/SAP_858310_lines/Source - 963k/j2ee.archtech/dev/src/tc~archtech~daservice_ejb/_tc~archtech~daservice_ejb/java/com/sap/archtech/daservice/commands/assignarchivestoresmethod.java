package com.sap.archtech.daservice.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.ejb.FinderException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.archconn.values.ArchivePathData;
import com.sap.archtech.daservice.data.CollectionData;
import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocal;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;
import com.sap.archtech.daservice.exceptions.ArchStoreConfigException;
import com.sap.archtech.daservice.exceptions.InvalidNameException;
import com.sap.archtech.daservice.storage.XmlDasDelete;
import com.sap.archtech.daservice.storage.XmlDasDeleteRequest;
import com.sap.archtech.daservice.storage.XmlDasDeleteResponse;
import com.sap.archtech.daservice.storage.XmlDasLegalHold;
import com.sap.archtech.daservice.storage.XmlDasLegalHoldRequest;
import com.sap.archtech.daservice.storage.XmlDasLegalHoldResponse;
import com.sap.archtech.daservice.storage.XmlDasMaster;
import com.sap.archtech.daservice.storage.XmlDasMkcol;
import com.sap.archtech.daservice.storage.XmlDasMkcolRequest;
import com.sap.archtech.daservice.storage.XmlDasMkcolResponse;
import com.sap.archtech.daservice.storage.XmlDasPropFind;
import com.sap.archtech.daservice.storage.XmlDasPropFindRequest;
import com.sap.archtech.daservice.storage.XmlDasPropFindResponse;
import com.sap.archtech.daservice.storage.XmlDasPropPatch;
import com.sap.archtech.daservice.storage.XmlDasPropPatchRequest;
import com.sap.archtech.daservice.storage.XmlDasPropPatchResponse;
import com.sap.archtech.daservice.util.Director;
import com.sap.engine.frame.core.locking.LockException;
import com.sap.engine.frame.core.locking.TechnicalLockException;
import com.sap.engine.services.applocking.TableLocking;
import com.sap.security.core.server.destinations.api.DestinationException;
import com.sap.security.core.server.destinations.api.DestinationService;
import com.sap.security.core.server.destinations.api.HTTPDestination;
import com.sap.sld.api.builder.InvalidDataException;
import com.sap.sld.api.util.SystemExplorer;
import com.sap.sld.api.wbem.exception.CIMException;
import com.sap.tc.logging.Severity;

public class AssignArchiveStoresMethod extends MasterMethod {

	private final static String ALL_SEL_RES_TAB1 = "SELECT RESID FROM BC_XMLA_RES WHERE COLID = ?";
	private final static String ALL_SEL_COL_TAB1 = "SELECT COLID, PARENTCOLID, STOREID FROM BC_XMLA_COL WHERE URI = ?";
	private final static String ALL_SEL_COL_TAB2 = "SELECT * FROM BC_XMLA_COL WHERE COLTYPE = 'H' OR COLTYPE = 'A' ORDER BY URI";
	private final static String ALL_SEL_COL_TAB3 = "SELECT STOREID FROM BC_XMLA_COL WHERE COLID = ?";
	private final static String ALL_SEL_COL_TAB4 = "SELECT COLID, STOREID FROM BC_XMLA_COL WHERE PARENTCOLID = ?";
	private final static String ALL_SEL_COL_TAB5 = "SELECT * FROM BC_XMLA_COL WHERE COLID = ?";
	private final static String INS_SEL_COL_TAB = "SELECT COLID FROM BC_XMLA_COL WHERE URI = ?";
	private final static String INS_SEL_COL_TAB1 = "SELECT COLID, URI FROM BC_XMLA_COL WHERE PARENTCOLID = ?";
	private final static String INS_SEL_COL_STO = "SELECT COLID FROM BC_XMLA_COL_STORE WHERE COLID = ? AND STOREID = ?";
	private final static String INS_SEL_COL_STO1 = "SELECT STOREID FROM BC_XMLA_COL_STORE WHERE COLID = ? ORDER BY STOREID DESC";
	private final static String INS_UPD_COL_TAB = "UPDATE BC_XMLA_COL SET STOREID = ? WHERE COLID = ?";
	private final static String INS_INS_COL_STO = "INSERT INTO BC_XMLA_COL_STORE (COLID, STOREID) VALUES (?, ?)";
	private final static String DEL_DEL_COL_STO = "DELETE FROM BC_XMLA_COL_STORE WHERE COLID = ? AND STOREID = ?";
	private final static String DEL_SEL_COL_TAB1 = "SELECT PARENTCOLID FROM BC_XMLA_COL WHERE COLID = ?";
	private final static String DEL_SEL_COL_TAB2 = "SELECT COLID FROM BC_XMLA_COL WHERE URI LIKE ?";
	private final static String DEL_SEL_COL_TAB3 = "SELECT URI FROM BC_XMLA_COL WHERE COLID = ?";
	private final static String DEL_SEL_COL_TAB4 = "SELECT COLID, URI, STOREID FROM BC_XMLA_COL WHERE PARENTCOLID = ?";
	private final static String DEL_SEL_COL_STO1 = "SELECT COLID FROM BC_XMLA_COL_STORE WHERE COLID = ? AND STOREID = ?";
	private final static String DEL_SEL_COL_STO2 = "SELECT STOREID FROM BC_XMLA_COL_STORE WHERE COLID = ?";
	private final static String DEL_UPD_COL_TAB = "UPDATE BC_XMLA_COL SET STOREID = ? WHERE COLID = ?";
	private final static String DEL_DEL_COL_PRP = "DELETE FROM BC_XMLA_COL_PROP WHERE COLID = ?";

	private int hits = 0;
	private long storeId = 0;
	private long colId = 0;
	private long colIdBackup = 0;
	private long parentColId = 0;
	private String uri = "";
	private String action;
	private String user;
	private String url;
	private String archive_path;
	private String archive_store;
	private Sapxmla_Config sac = null;
	private BufferedWriter bwout = null;
	private ArchStoreConfigLocalHome beanLocalHome;
	private TableLocking tlock;
	private Connection connection = null;
	private ResultSet result = null;
	private PreparedStatement pst = null;
	private PreparedStatement pst0 = null;
	private PreparedStatement pst1 = null;
	private PreparedStatement pst2 = null;
	private PreparedStatement pst3 = null;
	private PreparedStatement pst4 = null;
	private PreparedStatement pst5 = null;
	private PreparedStatement pst6 = null;
	private PreparedStatement pst7 = null;
	private PreparedStatement pst8 = null;
	private PreparedStatement pst9 = null;
	private PreparedStatement pst10 = null;
	private PreparedStatement pst11 = null;
	private XmlDasMkcolRequest mkcolRequest = null;
	private XmlDasMkcol mkcol = null;
	private XmlDasMkcolResponse mkcolResponse = null;
	private XmlDasDeleteRequest deleteRequest = null;
	private XmlDasDelete delete = null;
	private XmlDasDeleteResponse deleteResponse = null;
	private ArrayList<Sapxmla_Config> createdPaths = new ArrayList<Sapxmla_Config>();
	private ArrayList<Sapxmla_Config> deletedPaths = new ArrayList<Sapxmla_Config>();
	private ArrayList<Long> alreadyAssignedColsOnCurrentArchiveStore = new ArrayList<Long>();

	public AssignArchiveStoresMethod(HttpServletResponse response,
			Connection connection, TableLocking tlock,
			ArchStoreConfigLocalHome beanLocalHome, String url, String action,
			String user, String archive_path, String archive_store) {
		this.response = response;
		this.connection = connection;
		this.beanLocalHome = beanLocalHome;
		this.url = url;
		this.action = action;
		this.user = user;
		this.archive_path = archive_path;
		this.archive_store = archive_store;
		this.tlock = tlock;
	}

	public boolean execute() throws IOException {

		// Get Servlet Output Stream
		bwout = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream(), "UTF8"));

		// Set Response Header
		response.setContentType(MasterMethod.contentType);
		response.setHeader("service_message", "see message body");

		// Set 'Logical' Exclusive Table Lock For Assigning Archive Stores
		// Function
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("COLID", new Long(0));
		hm.put("RESNAME", new String("AAS"));
		for (int i = 0; i < 10; i++) {
			try {
				tlock.lock(TableLocking.LIFETIME_TRANSACTION, connection,
						"BC_XMLA_LOCKING", hm,
						TableLocking.MODE_EXCLUSIVE_NONCUMULATIVE);
				break;
			} catch (LockException lex) {

				// $JL-EXC$
				if (i == 9) {
					this
							.reportStreamError(
									HttpServletResponse.SC_CONFLICT,
									"_ASSIGN_ARCHIVE_STORES: The lock for the assign archive store function can not be granted: "
											+ lex.toString(), bwout);

					return false;
				}
			} catch (TechnicalLockException tlex) {

				// $JL-EXC$
				if (i == 9) {
					this
							.reportStreamError(
									HttpServletResponse.SC_CONFLICT,
									"_ASSIGN_ARCHIVE_STORES: The lock for the assign archive store function can not be granted for technical reasons: "
											+ tlex.toString(), bwout);

					return false;
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException iex) {

				// $JL-EXC$
				MasterMethod.loc
						.infoT("_ASSIGN_ARCHIVE_STORES: The current thread is waiting, sleeping, or otherwise paused for a long time and another thread interrupts it.");
			}
		}

		// Check Request Header "action"
		if (this.action == null) {
			this
					.reportStreamError(
							DasResponse.SC_PARAMETER_MISSING,
							"_ASSIGN_ARCHIVE_STORES: ACTION missing from request header",
							bwout);
			return false;
		} else {
			this.action = this.action.toUpperCase();
			if (!(this.action.startsWith("L") || this.action.startsWith("A") || this.action
					.startsWith("U"))) {
				this
						.reportStreamError(
								DasResponse.SC_KEYWORD_UNKNOWN,
								"_ASSIGN_ARCHIVE_STORES: Value "
										+ this.action
										+ " of request header ACTION does not meet specifications",
								bwout);
				return false;
			}
		}

		// Check Request Header "user"
		if ((this.user == null) || (this.user.length() == 0)) {
			this.reportStreamError(DasResponse.SC_PARAMETER_MISSING,
					"_ASSIGN_ARCHIVE_STORES: USER missing from request header",
					bwout);
			return false;
		}

		// Check Request Header "archive_path"
		if (this.archive_path == null) {
			if (!action.startsWith("L")) {
				this
						.reportStreamError(
								DasResponse.SC_PARAMETER_MISSING,
								"_ASSIGN_ARCHIVE_STORES: ARCHIVE_PATH missing from request header",
								bwout);
			}
		} else {
			try {
				this.isValidName(this.archive_path, true);
			} catch (InvalidNameException inex) {

				// $JL-EXC$
				this.reportStreamError(DasResponse.SC_INVALID_CHARACTER,
						"_ASSIGN_ARCHIVE_STORES: " + inex.getMessage(), bwout);
				return false;
			}
			this.archive_path = this.archive_path.toLowerCase();
			if (!(this.archive_path.indexOf("//") == -1)
					|| !this.archive_path.startsWith("/")
					|| !this.archive_path.endsWith("/")
					|| this.archive_path.length() < 3) {
				this
						.reportStreamError(
								DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
								"_ASSIGN_ARCHIVE_STORES: Value "
										+ this.archive_path
										+ " of request header ARCHIVE_PATH does not meet specifications",
								bwout);
				return false;
			}
		}

		// Check Request Header "archive_store"
		if ((this.action.startsWith("A"))
				&& ((this.archive_store == null) || ((this.archive_store != null) && (this.archive_store
						.length() == 0)))) {
			this
					.reportStreamError(
							DasResponse.SC_PARAMETER_MISSING,
							"_ASSIGN_ARCHIVE_STORES: ARCHIVE_STORE missing from request header",
							bwout);
			return false;
		}

		ObjectOutputStream oos = null;
		boolean status = false;
		boolean errorOccurred = false;
		try {
			if (action.startsWith("A") || action.startsWith("U")) {

				// Get Collection Properties
				uri = this.archive_path.substring(0,
						this.archive_path.length() - 1);
				pst = connection.prepareStatement(ALL_SEL_COL_TAB1);
				pst.setString(1, uri);
				result = pst.executeQuery();
				hits = 0;
				while (result.next()) {
					colId = result.getLong("COLID");
					colIdBackup = colId;
					parentColId = result.getLong("PARENTCOLID");
					storeId = result.getLong("STOREID");
					hits++;
				}
				result.close();
				pst.close();
				if (hits != 1) {
					this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
							"_ASSIGN_ARCHIVE_STORES: Archive path "
									+ this.archive_path + " is not defined",
							bwout);
					errorOccurred = true;
				}

				// Check If Assign/Unassign Action Is Allowed At All
				if (!errorOccurred) {
					if (this.action.equalsIgnoreCase("A")) {
						if ((storeId != 0)
								|| (this.hasAssignedDescendants(colId))) {
							this
									.reportStreamError(
											HttpServletResponse.SC_FORBIDDEN,
											"_ASSIGN_ARCHIVE_STORES: Assigning archive path "
													+ this.archive_path
													+ " is not allowed because of already assigned ancestors or descendants",
											bwout);
							errorOccurred = true;
						}
					} else {
						if ((storeId == 0)
								|| (this.hasAssignedAncestor(parentColId))
								|| (this
										.hasAssignedDescendantsContainingResources(colId))) {
							this
									.reportStreamError(
											HttpServletResponse.SC_FORBIDDEN,
											"_ASSIGN_ARCHIVE_STORES: Unassigning archive path "
													+ this.archive_path
													+ " is not allowed because of still assigned ancestors or descendants",
											bwout);
							errorOccurred = true;
						}
					}
				}

				// Assign A Collection
				if (!errorOccurred) {
					if (this.action.equalsIgnoreCase("A")
							&& !this.assignArchiveStores())
						errorOccurred = true;
				}

				// Unassign A Collection
				if (!errorOccurred) {
					if (this.action.equalsIgnoreCase("U")
							&& !this.unassignArchiveStores())
						errorOccurred = true;
				}
			}

			if (!errorOccurred) {

				// Write Serialized Object Into Servlet Output Stream
				oos = new ObjectOutputStream(response.getOutputStream());
				if (action.startsWith("A") || action.startsWith("U"))
					oos.writeObject(this.listArchivePath());
				else
					oos.writeObject(this.listArchivePaths());
				oos.flush();

				// Write Success To Servlet Output Stream
				this.writeStatus(bwout, HttpServletResponse.SC_OK, "Ok");
				bwout.flush();
				status = true;
			}
		} catch (ArchStoreConfigException ascex) {

			// Write Message Into Log
			MasterMethod.cat.logThrowableT(Severity.ERROR, loc,
					"_ASSIGN_ARCHIVE_STORES: " + ascex.getMessage(), ascex);

			// Write Message Into Servlet Output Stream
			this.reportStreamError(DasResponse.SC_CONFIG_INCONSISTENT,
					"_ASSIGN_ARCHIVE_STORES: " + ascex.getMessage(), bwout);
		} catch (IOException ioex) {

			// Write Message Into Log
			MasterMethod.cat.logThrowableT(Severity.ERROR, loc,
					"_ASSIGN_ARCHIVE_STORES: " + ioex.getMessage(), ioex);

			// Write Message Into Servlet Output Stream
			this.reportStreamError(DasResponse.SC_IO_ERROR,
					"_ASSIGN_ARCHIVE_STORES: " + ioex.getMessage(), bwout);
		} catch (SQLException sqlex) {

			// Write Message Into Log
			MasterMethod.cat.logThrowableT(Severity.ERROR, loc,
					"_ASSIGN_ARCHIVE_STORES: " + sqlex.getMessage(), sqlex);

			// Write Message Into Servlet Output Stream
			this.reportStreamError(DasResponse.SC_SQL_ERROR,
					"_ASSIGN_ARCHIVE_STORES: " + sqlex.getMessage(), bwout);

		} catch (Exception ex) {

			// Write Message Into Log
			MasterMethod.cat.logThrowableT(Severity.ERROR, loc,
					"_ASSIGN_ARCHIVE_STORES: " + ex.getMessage(), ex);

			// Write Message Into Servlet Output Stream
			this.reportStreamError(
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"_ASSIGN_ARCHIVE_STORES: " + ex.getMessage(), bwout);
		} finally {

			// Close Prepared Statements
			try {
				if (result != null)
					result.close();
				if (pst != null)
					pst.close();
				if (pst0 != null)
					pst0.close();
				if (pst1 != null)
					pst1.close();
				if (pst2 != null)
					pst2.close();
				if (pst3 != null)
					pst3.close();
				if (pst4 != null)
					pst4.close();
				if (pst5 != null)
					pst5.close();
				if (pst6 != null)
					pst6.close();
				if (pst7 != null)
					pst7.close();
				if (pst8 != null)
					pst8.close();
				if (pst9 != null)
					pst9.close();
				if (pst10 != null)
					pst10.close();
				if (pst11 != null)
					pst11.close();
			} catch (SQLException sqlex) {
				cat.logThrowableT(Severity.WARNING, loc,
						"_ASSIGN_ARCHIVE_STORES: " + sqlex.getMessage(), sqlex);
			}

			// Close Servlet Output Stream
			if (bwout != null) {
				bwout.close();
			}
		}
		return status;
	}

	private boolean assignArchiveStores() throws IOException {
		try {

			// Get Archive Store Id
			try {
				Collection<ArchStoreConfigLocal> col = beanLocalHome
						.findByArchiveStore(this.archive_store);
				Iterator<ArchStoreConfigLocal> iter = col.iterator();
				if (col.isEmpty()) {

					// Write Message Into Log
					MasterMethod.cat.errorT(loc,
							"_ASSIGN_ARCHIVE_STORES: Archive store "
									+ this.archive_store + " is not defined");

					// Write Message Into Servlet Output Stream
					this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
							"_ASSIGN_ARCHIVE_STORES: Archive store "
									+ this.archive_store + " is not defined",
							bwout);
					return false;
				} else {
					ArchStoreConfigLocal ascl = iter.next();
					storeId = ((Long) ascl.getPrimaryKey()).longValue();
					sac = ascl.getSapxmla_Config();
				}
			} catch (FinderException fex) {

				// $JL-EXC$

				// Write Message Into Log
				MasterMethod.cat.errorT(loc,
						"_ASSIGN_ARCHIVE_STORES: Archive store "
								+ this.archive_store + " is not defined");

				// Write Message Into Servlet Output Stream
				this
						.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
								"_ASSIGN_ARCHIVE_STORES: Archive store "
										+ this.archive_store
										+ " is not defined", bwout);
				return false;
			}

			// Assign All Ancestors
			pst1 = connection.prepareStatement(INS_SEL_COL_TAB);
			pst2 = connection.prepareStatement(INS_SEL_COL_STO);
			pst3 = connection.prepareStatement(INS_INS_COL_STO);
			pst4 = connection.prepareStatement(INS_UPD_COL_TAB);
			this.assignAllAncestors();
			pst1.close();
			pst2.close();
			pst3.close();
			pst4.close();

			// Assign Recursive All Descendants
			pst5 = connection.prepareStatement(INS_SEL_COL_TAB1);
			pst6 = connection.prepareStatement(INS_INS_COL_STO);
			pst7 = connection.prepareStatement(INS_UPD_COL_TAB);
			this.assignRecursiveAllDescendants(colIdBackup);
			pst5.close();
			pst6.close();
			pst7.close();

			// Set Possibly Missing Properties And Legal Holds
			if (sac.type.equalsIgnoreCase("W")) {
				pst8 = connection.prepareStatement(INS_SEL_COL_TAB);
				pst9 = connection.prepareStatement(INS_SEL_COL_STO1);
				this.setPropertiesAndLegalHolds();
				pst8.close();
				pst9.close();
			}

			// Only For Test Archive Store: Insert SLD Archive Store Instance
			// And Xml Archive Server Instance
			if (this.archive_store.equalsIgnoreCase("_TEST_ARCHIVE_STORE")) {
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
					enginePropValues[6] = SystemExplorer
							.getJ2EEClusterEngineName();
					enginePropValues[7] = this.url;
					String archiveName = this.archive_store
							+ ".XMLDASName.xmldas.SystemName."
							+ SystemExplorer.getJ2EEClusterEngineName();
					String[] archivePropValues = new String[12];
					archivePropValues[0] = sac.storage_system;
					archivePropValues[1] = this.archive_store;
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
					archivePropValues[10] = "0";
					archivePropValues[11] = "false";
					Director director = new Director();

					director.insertSldInstance(engineName, enginePropValues,
							archiveName, archivePropValues);
				} catch (NamingException nex) {
					MasterMethod.isSLDoutOfSync = true;

					// $JL-EXC$
					MasterMethod.cat.errorT(loc,
							"_ASSIGN_ARCHIVE_STORES: JNDI error while creating WBEM client: "
									+ nex.toString());
				} catch (CIMException cimex) {
					MasterMethod.isSLDoutOfSync = true;

					// $JL-EXC$
					MasterMethod.cat.errorT(loc,
							"_ASSIGN_ARCHIVE_STORES:  CIM error condition occurred: "
									+ cimex.toString());

				} catch (InvalidDataException idex) {
					MasterMethod.isSLDoutOfSync = true;

					// $JL-EXC$
					MasterMethod.cat.errorT(loc,
							"_ASSIGN_ARCHIVE_STORES: Invalid input data: "
									+ idex.toString());
				} catch (Exception ex) {
					MasterMethod.isSLDoutOfSync = true;

					// $JL-EXC$
					MasterMethod.cat.errorT(loc,
							"_ASSIGN_ARCHIVE_STORES: Exception occurred: "
									+ ex.toString());
				}
			}

			// Write Successful Archive Store Assignment Into Log
			int lastSlashNum = uri.lastIndexOf("/");
			int strLen = uri.length();
			if ((lastSlashNum != -1) && (lastSlashNum < strLen))
				MasterMethod.cat.infoT(loc,
						"_ASSIGN_ARCHIVE_STORES: Collection "
								+ uri.substring(lastSlashNum + 1, strLen)
								+ " successfully assigned to archive store "
								+ this.archive_store);
			else
				MasterMethod.cat.infoT(loc,
						"_ASSIGN_ARCHIVE_STORES: Collection " + uri
								+ " successfully assigned to archive store "
								+ this.archive_store);
		} catch (FinderException fex) {

			// Roll Back Already Created Collections
			this.deleteCollections(createdPaths);

			// Write Message Into Log
			MasterMethod.cat.logThrowableT(Severity.ERROR, loc,
					"_ASSIGN_ARCHIVE_STORES: " + fex.getMessage(), fex);

			// Write Message Into Servlet Output Stream
			this.reportStreamError(
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"_ASSIGN_ARCHIVE_STORES: " + fex.getMessage(), bwout);
			return false;
		} catch (SQLException sqlex) {

			// Roll Back Already Created Collections
			this.deleteCollections(createdPaths);

			// Write Message Into Log
			MasterMethod.cat.logThrowableT(Severity.ERROR, loc,
					"_ASSIGN_ARCHIVE_STORES: " + sqlex.getMessage(), sqlex);

			// Write Message Into Servlet Output Stream
			this.reportStreamError(DasResponse.SC_SQL_ERROR,
					"_ASSIGN_ARCHIVE_STORES: " + sqlex.getMessage(), bwout);
			return false;
		} catch (IOException ioex) {

			// Roll Back Already Created Collections
			this.deleteCollections(createdPaths);

			// Write Message Into Log
			MasterMethod.cat.logThrowableT(Severity.ERROR, loc,
					"_ASSIGN_ARCHIVE_STORES: " + ioex.getMessage(), ioex);

			// Write Message Into Servlet Output Stream
			this.reportStreamError(DasResponse.SC_IO_ERROR,
					"_ASSIGN_ARCHIVE_STORES: " + ioex.getMessage(), bwout);
			return false;
		}

		// Method Was Successful
		return true;
	}

	private boolean unassignArchiveStores() throws IOException {
		try {

			// Get All Archive Store Parameters
			try {
				sac = beanLocalHome.findByPrimaryKey(new Long(storeId))
						.getSapxmla_Config();
			} catch (FinderException fex) {

				// $JL-EXC$

				// Write Message Into Servlet Output Stream
				this.reportStreamError(HttpServletResponse.SC_CONFLICT,
						"_ASSIGN_ARCHIVE_STORES: Archive path "
								+ this.archive_path
								+ " is not assigned to any archive store",
						bwout);
				return false;
			}

			// Unassign Recursive All Descendants And Selected Collection
			pst1 = connection.prepareStatement(DEL_SEL_COL_TAB3);
			pst2 = connection.prepareStatement(DEL_SEL_COL_TAB4);
			pst3 = connection.prepareStatement(DEL_DEL_COL_STO);
			pst4 = connection.prepareStatement(DEL_UPD_COL_TAB);
			pst11 = connection.prepareStatement(DEL_DEL_COL_PRP);
			this.unassignRecursiveAllDescendants(colId);
			pst1.close();
			pst2.close();
			pst3.close();
			pst4.close();
			pst11.close();

			// Unassign All Ancestors
			pst5 = connection.prepareStatement(DEL_SEL_COL_TAB1);
			pst6 = connection.prepareStatement(DEL_SEL_COL_TAB2);
			pst7 = connection.prepareStatement(DEL_SEL_COL_STO1);
			pst8 = connection.prepareStatement(DEL_DEL_COL_STO);
			pst9 = connection.prepareStatement(DEL_SEL_COL_STO2);
			pst10 = this.connection.prepareStatement(DEL_DEL_COL_PRP);
			this.unassignAllAncestors();
			pst5.close();
			pst6.close();
			pst7.close();
			pst8.close();
			pst9.close();
			pst10.close();

			// Write Successful Archive Store Deletion Message Into Log
			int lastSlashNum = uri.lastIndexOf("/");
			int strLen = uri.length();
			if ((lastSlashNum != -1) && (lastSlashNum < strLen))
				MasterMethod.cat
						.infoT(
								loc,
								"_ASSIGN_ARCHIVE_STORES: Collection "
										+ uri.substring(lastSlashNum + 1,
												strLen)
										+ " successfully unassigned from archive store "
										+ sac.archive_store);
			else
				MasterMethod.cat
						.infoT(
								loc,
								"_ASSIGN_ARCHIVE_STORES: Collection "
										+ uri
										+ " successfully unassigned from archive store "
										+ sac.archive_store);
		} catch (SQLException sqlex) {

			// Roll Back Already Deleted Collections
			this.createCollections(deletedPaths);

			// Write Message Into Log
			MasterMethod.cat.logThrowableT(Severity.ERROR, loc,
					"_ASSIGN_ARCHIVE_STORES: " + sqlex.getMessage(), sqlex);

			// Write Message Into Servlet Output Stream
			this.reportStreamError(DasResponse.SC_SQL_ERROR,
					"_ASSIGN_ARCHIVE_STORES: " + sqlex.getMessage(), bwout);
			return false;
		} catch (IOException ioex) {

			// Roll Back Already Deleted Collections
			this.createCollections(deletedPaths);

			// Write Message Into Log
			MasterMethod.cat.logThrowableT(Severity.ERROR, loc,
					"_ASSIGN_ARCHIVE_STORES: " + ioex.getMessage(), ioex);

			// Write Message Into Servlet Output Stream
			this.reportStreamError(DasResponse.SC_IO_ERROR,
					"_ASSIGN_ARCHIVE_STORES: " + ioex.getMessage(), bwout);
			return false;
		}

		// Method Was Successful
		return true;
	}

	private void deleteCollections(ArrayList<Sapxmla_Config> createdPaths) {
		Sapxmla_Config sc = null;
		Iterator<Sapxmla_Config> it = createdPaths.iterator();
		while (it.hasNext()) {
			try {
				sc = it.next();
				XmlDasDeleteRequest deleteRequest = new XmlDasDeleteRequest(sc,
						sc.storage_system, "COL");
				XmlDasDelete delete = new XmlDasDelete(deleteRequest);
				XmlDasDeleteResponse deleteResponse = delete.execute();
				if (!((deleteResponse.getStatusCode() == HttpServletResponse.SC_OK)
						|| (deleteResponse.getStatusCode() == HttpServletResponse.SC_ACCEPTED) || (deleteResponse
						.getStatusCode() == HttpServletResponse.SC_NO_CONTENT))) {
					if (deleteResponse.getException() == null) {
						MasterMethod.cat
								.errorT(
										loc,
										"Collection "
												+ XmlDasMaster.getPhysicalPath(
														sc, sc.storage_system)
												+ " was created and cannot be accessed by SAP XML DAS. To complete the rollback process you must delete it manually from the storage system: "
												+ deleteResponse
														.getStatusCode()
												+ " "
												+ deleteResponse
														.getReasonPhrase()
												+ " "
												+ deleteResponse
														.getEntityBody());
						throw new IOException(
								"Collection "
										+ XmlDasMaster.getPhysicalPath(sc,
												sc.storage_system)
										+ " was created and cannot be accessed by SAP XML DAS. To complete the rollback process you must delete it manually from the storage system: "
										+ deleteResponse.getStatusCode() + " "
										+ deleteResponse.getReasonPhrase()
										+ " " + deleteResponse.getEntityBody());
					} else {
						MasterMethod.cat
								.errorT(
										loc,
										"Collection "
												+ XmlDasMaster.getPhysicalPath(
														sc, sc.storage_system)
												+ " was created and cannot be accessed by SAP XML DAS. To complete the rollback process you must delete it manually from the storage system: "
												+ deleteResponse
														.getStatusCode()
												+ " "
												+ deleteResponse
														.getReasonPhrase()
												+ " "
												+ deleteResponse
														.getEntityBody()
												+ " "
												+ getStackTrace(deleteResponse
														.getException()));
						throw new IOException(
								"Collection "
										+ XmlDasMaster.getPhysicalPath(sc,
												sc.storage_system)
										+ " was created and cannot be accessed by SAP XML DAS. To complete the rollback process you must delete it manually from the storage system: "
										+ deleteResponse.getStatusCode()
										+ " "
										+ deleteResponse.getReasonPhrase()
										+ " "
										+ deleteResponse.getEntityBody()
										+ " "
										+ deleteResponse.getException()
												.toString());
					}
				}
			} catch (IOException ioex) {

				// Write Exception Into Log
				MasterMethod.cat
						.logThrowableT(
								Severity.ERROR,
								loc,
								"_ASSIGN_ARCHIVE_STORES: Following exception occured while deleting collection "
										+ XmlDasMaster.getPhysicalPath(sc,
												sc.storage_system)
										+ " during rollback: "
										+ ioex.toString(), ioex);
				continue;
			}

			// Write Success Into Log
			MasterMethod.cat.infoT(loc, "_ASSIGN_ARCHIVE_STORES: Collection "
					+ XmlDasMaster.getPhysicalPath(sc, sc.storage_system)
					+ " successfully deleted during rollback process");
		}
	}

	private void createCollections(ArrayList<Sapxmla_Config> deletedPaths) {
		Sapxmla_Config sc = null;
		Iterator<Sapxmla_Config> it = deletedPaths.iterator();
		while (it.hasNext()) {
			try {
				sc = it.next();
				XmlDasMkcolRequest mkcolRequest = new XmlDasMkcolRequest(sc,
						sc.storage_system);
				XmlDasMkcol mkcol = new XmlDasMkcol(mkcolRequest);
				XmlDasMkcolResponse mkcolResponse = mkcol.execute();
				if (mkcolResponse.getStatusCode() != HttpServletResponse.SC_CREATED) {
					if (mkcolResponse.getStatusCode() != HttpServletResponse.SC_METHOD_NOT_ALLOWED) {
						if (mkcolResponse.getException() == null) {
							MasterMethod.cat
									.errorT(
											loc,
											"Collection "
													+ XmlDasMaster
															.getPhysicalPath(
																	sc,
																	sc.storage_system)
													+ " was deleted and cannot be recreated by SAP XML DAS. To complete the roll back process you must create it manually on the storage system: "
													+ mkcolResponse
															.getStatusCode()
													+ " "
													+ mkcolResponse
															.getReasonPhrase()
													+ " "
													+ mkcolResponse
															.getEntityBody());
							throw new IOException(
									"Collection "
											+ XmlDasMaster.getPhysicalPath(sc,
													sc.storage_system)
											+ " was deleted and cannot be recreated by SAP XML DAS. To complete the roll back process you must create it manually on the storage system: "
											+ mkcolResponse.getStatusCode()
											+ " "
											+ mkcolResponse.getReasonPhrase()
											+ " "
											+ mkcolResponse.getEntityBody());
						} else {
							MasterMethod.cat
									.errorT(
											loc,
											"Collection "
													+ XmlDasMaster
															.getPhysicalPath(
																	sc,
																	sc.storage_system)
													+ " was deleted and cannot be recreated by SAP XML DAS. To complete the roll back process you must create it manually on the storage system: "
													+ mkcolResponse
															.getStatusCode()
													+ " "
													+ mkcolResponse
															.getReasonPhrase()
													+ " "
													+ mkcolResponse
															.getEntityBody()
													+ " "
													+ getStackTrace(mkcolResponse
															.getException()));
							throw new IOException(
									"Collection "
											+ XmlDasMaster.getPhysicalPath(sc,
													sc.storage_system)
											+ " was deleted and cannot be recreated by SAP XML DAS. To complete the roll back process you must create it manually on the storage system: "
											+ mkcolResponse.getStatusCode()
											+ " "
											+ mkcolResponse.getReasonPhrase()
											+ " "
											+ mkcolResponse.getEntityBody()
											+ " "
											+ mkcolResponse.getException()
													.toString());
						}
					} else {
						MasterMethod.cat
								.errorT(
										loc,
										"_ASSIGN_ARCHIVE_STORES: Collection "
												+ XmlDasMaster.getPhysicalPath(
														sc, sc.storage_system)
												+ " already existed on storage system during roll back process - it is now consistently created in XML DAS");
					}
				}
			} catch (IOException ioex) {

				// Write Exception Into Log
				MasterMethod.cat
						.logThrowableT(
								Severity.ERROR,
								loc,
								"_ASSIGN_ARCHIVE_STORES: Following exception occured while creating collection "
										+ XmlDasMaster.getPhysicalPath(sc,
												sc.storage_system)
										+ " during rollback: "
										+ ioex.toString(), ioex);
				continue;
			}

			// Write Success Into Log
			MasterMethod.cat.infoT(loc, "_ASSIGN_ARCHIVE_STORES: Collection "
					+ XmlDasMaster.getPhysicalPath(sc, sc.storage_system)
					+ " successfully created during rollback process");
		}
	}

	private boolean hasAssignedDescendants(long colId) throws SQLException {
		boolean isAssigned = false;
		PreparedStatement pst1 = null;
		try {
			pst1 = connection.prepareStatement(ALL_SEL_COL_TAB4);
			pst1.setLong(1, colId);
			ResultSet result1 = pst1.executeQuery();
			long cId = 0;
			long sId = 0;
			ArrayList<CollectionData> al = new ArrayList<CollectionData>();
			while (result1.next()) {
				cId = result1.getLong("COLID");
				sId = result1.getLong("STOREID");
				if (sId != 0) {
					isAssigned = true;
					break;
				}
				al.add(new CollectionData(cId, sId));
			}
			result1.close();
			pst1.close();
			if (isAssigned == true)
				return true;
			for (Iterator<CollectionData> iter = al.iterator(); iter.hasNext();) {
				CollectionData element = iter.next();
				if (this.hasAssignedDescendants(element.getcolId()))
					return true;
			}
			return false;
		} finally {
			if (pst1 != null)
				pst1.close();
		}
	}

	private boolean hasAssignedDescendantsContainingResources(long colId)
			throws SQLException {
		boolean hasResource = false;
		PreparedStatement pst1 = null, pst2 = null;
		try {
			int hits = 0;
			pst1 = connection.prepareStatement(ALL_SEL_RES_TAB1);
			pst1.setLong(1, colId);
			pst1.setMaxRows(1);
			ResultSet result1 = pst1.executeQuery();
			while (result1.next())
				hits++;
			result1.close();
			pst1.close();
			if (hits != 0)
				return true;
			pst2 = connection.prepareStatement(ALL_SEL_COL_TAB4);
			pst2.setLong(1, colId);
			ResultSet result2 = pst2.executeQuery();
			while (result2.next()) {
				if (this.hasAssignedDescendantsContainingResources(result2
						.getLong("COLID"))) {
					hasResource = true;
					break;
				}
			}
			result2.close();
			pst2.close();
			if (hasResource == true)
				return true;
			else
				return false;
		} finally {
			if (pst1 != null)
				pst1.close();
			if (pst2 != null)
				pst2.close();
		}
	}

	private boolean hasAssignedAncestor(long parentColId) throws SQLException {
		PreparedStatement pst1 = null;
		try {
			pst1 = connection.prepareStatement(ALL_SEL_COL_TAB3);
			pst1.setLong(1, parentColId);
			ResultSet result1 = pst1.executeQuery();
			long storeId = 0;
			while (result1.next())
				storeId = result1.getLong("STOREID");
			result1.close();
			pst1.close();
			if (storeId == 0)
				return false;
			else
				return true;
		} finally {
			if (pst1 != null)
				pst1.close();
		}
	}

	private void assignAllAncestors() throws IOException, SQLException {

		// Loop Downwards From Root Node To Collection
		StringTokenizer st = new StringTokenizer(uri, "/");
		int actualToken = 0;
		String actualPath = "/";
		while (st.hasMoreTokens()) {

			// Adjust Actual Path
			actualToken++;
			actualPath += st.nextToken();

			// Get Collection Id
			pst1.setString(1, actualPath);
			result = pst1.executeQuery();
			while (result.next())
				colId = result.getLong("COLID");
			result.close();

			// Check If BC_XMLA_COL_STORE Entry Exists
			pst2.setLong(1, colId);
			pst2.setLong(2, storeId);
			result = pst2.executeQuery();
			hits = 0;
			while (result.next())
				hits++;
			result.close();
			if (hits == 0) {

				// Create Collection On Storage System
				mkcolRequest = new XmlDasMkcolRequest(sac, actualPath.trim());
				mkcol = new XmlDasMkcol(mkcolRequest);
				mkcolResponse = mkcol.execute();
				if (mkcolResponse.getStatusCode() != HttpServletResponse.SC_CREATED) {
					if (mkcolResponse.getStatusCode() != HttpServletResponse.SC_METHOD_NOT_ALLOWED) {
						if (mkcolResponse.getException() == null) {
							MasterMethod.cat
									.errorT(
											loc,
											"_ASSIGN_ARCHIVE_STORES: I/O-Error while creating collection "
													+ actualPath
															.substring(
																	actualPath
																			.lastIndexOf("/") + 1,
																	actualPath
																			.length())
													+ ": Archive store returned following response: "
													+ mkcolResponse
															.getStatusCode()
													+ " "
													+ mkcolResponse
															.getReasonPhrase()
													+ " "
													+ mkcolResponse
															.getEntityBody());
							throw new IOException(
									"_ASSIGN_ARCHIVE_STORES: I/O-Error while creating collection "
											+ actualPath.substring(actualPath
													.lastIndexOf("/") + 1,
													actualPath.length())
											+ ": Archive store returned following response: "
											+ mkcolResponse.getStatusCode()
											+ " "
											+ mkcolResponse.getReasonPhrase()
											+ " "
											+ mkcolResponse.getEntityBody());
						} else {
							MasterMethod.cat
									.logThrowableT(
											Severity.ERROR,
											loc,
											"_ASSIGN_ARCHIVE_STORES: I/O-Error while creating collection "
													+ actualPath
															.substring(
																	actualPath
																			.lastIndexOf("/") + 1,
																	actualPath
																			.length())
													+ ": Archive store returned following response: "
													+ mkcolResponse
															.getStatusCode()
													+ " "
													+ mkcolResponse
															.getReasonPhrase()
													+ " "
													+ mkcolResponse
															.getEntityBody()
													+ " "
													+ getStackTrace(mkcolResponse
															.getException()),
											mkcolResponse.getException());
							throw new IOException(
									"_ASSIGN_ARCHIVE_STORES: I/O-Error while creating collection "
											+ actualPath.substring(actualPath
													.lastIndexOf("/") + 1,
													actualPath.length())
											+ ": Archive store returned following response: "
											+ mkcolResponse.getStatusCode()
											+ " "
											+ mkcolResponse.getReasonPhrase()
											+ " "
											+ mkcolResponse.getEntityBody()
											+ " "
											+ mkcolResponse.getException()
													.toString());
						}
					} else {
						MasterMethod.cat
								.errorT(
										loc,
										"_ASSIGN_ARCHIVE_STORES: Collection "
												+ actualPath
														.substring(
																actualPath
																		.lastIndexOf("/") + 1,
																actualPath
																		.length())
												+ " already existed on storage system - it is now consistently created in XML DAS");
					}
				}

				// Store URI For Possible Error Handling
				createdPaths.add(0, new Sapxmla_Config(sac.store_id,
						sac.archive_store, actualPath.trim(), sac.type,
						sac.win_root, sac.unix_root, sac.proxy_host,
						sac.proxy_port));

				// Insert BC_XMLA_COL_STORE Entry
				pst3.setLong(1, colId);
				pst3.setLong(2, storeId);
				pst3.executeUpdate();
			} else {
				alreadyAssignedColsOnCurrentArchiveStore.add(new Long(colId));
			}

			// Adjust Actual Path And Parent Collection Id
			actualPath += "/";
		} // end while

		// Update Collection Entry
		pst4.setLong(1, storeId);
		pst4.setLong(2, colIdBackup);
		pst4.executeUpdate();
	}

	private void assignRecursiveAllDescendants(long parentColId)
			throws IOException, SQLException {
		ArrayList<CollectionData> al = new ArrayList<CollectionData>();
		pst5.setLong(1, parentColId);
		result = pst5.executeQuery();
		while (result.next())
			al.add(new CollectionData(result.getLong("COLID"), result
					.getString("URI")));
		result.close();

		if (al.isEmpty())
			return;

		for (Iterator<CollectionData> iter = al.iterator(); iter.hasNext();) {
			CollectionData collData = iter.next();

			// Create Collection On Storage System
			mkcolRequest = new XmlDasMkcolRequest(sac, collData.getColURI());
			mkcol = new XmlDasMkcol(mkcolRequest);
			mkcolResponse = mkcol.execute();
			if (mkcolResponse.getStatusCode() != HttpServletResponse.SC_CREATED) {
				if (mkcolResponse.getStatusCode() != HttpServletResponse.SC_METHOD_NOT_ALLOWED) {
					if (mkcolResponse.getException() == null) {
						MasterMethod.cat
								.errorT(
										loc,
										"_ASSIGN_ARCHIVE_STORES: I/O-Error while creating collection "
												+ collData
														.getColURI()
														.substring(
																collData
																		.getColURI()
																		.lastIndexOf(
																				"/") + 1,
																collData
																		.getColURI()
																		.length())
												+ ": Archive store returned following response: "
												+ mkcolResponse.getStatusCode()
												+ " "
												+ mkcolResponse
														.getReasonPhrase()
												+ " "
												+ mkcolResponse.getEntityBody());
						throw new IOException(
								"_ASSIGN_ARCHIVE_STORES: I/O-Error while creating collection "
										+ collData.getColURI().substring(
												collData.getColURI()
														.lastIndexOf("/") + 1,
												collData.getColURI().length())
										+ ": Archive store returned following response: "
										+ mkcolResponse.getStatusCode() + " "
										+ mkcolResponse.getReasonPhrase() + " "
										+ mkcolResponse.getEntityBody());
					} else {
						MasterMethod.cat
								.logThrowableT(
										Severity.ERROR,
										loc,
										"_ASSIGN_ARCHIVE_STORES: I/O-Error while creating collection "
												+ collData
														.getColURI()
														.substring(
																collData
																		.getColURI()
																		.lastIndexOf(
																				"/") + 1,
																collData
																		.getColURI()
																		.length())
												+ ": Archive store returned following response: "
												+ mkcolResponse.getStatusCode()
												+ " "
												+ mkcolResponse
														.getReasonPhrase()
												+ " "
												+ mkcolResponse.getEntityBody()
												+ " "
												+ getStackTrace(mkcolResponse
														.getException()),
										mkcolResponse.getException());
						throw new IOException(
								"_ASSIGN_ARCHIVE_STORES: I/O-Error while creating collection "
										+ collData.getColURI().substring(
												collData.getColURI()
														.lastIndexOf("/") + 1,
												collData.getColURI().length())
										+ ": Archive store returned following response: "
										+ mkcolResponse.getStatusCode()
										+ " "
										+ mkcolResponse.getReasonPhrase()
										+ " "
										+ mkcolResponse.getEntityBody()
										+ " "
										+ mkcolResponse.getException()
												.toString());
					}
				} else {
					MasterMethod.cat
							.errorT(
									loc,
									"_ASSIGN_ARCHIVE_STORES: Collection "
											+ collData
													.getColURI()
													.substring(
															collData
																	.getColURI()
																	.lastIndexOf(
																			"/") + 1,
															collData
																	.getColURI()
																	.length())
											+ " already existed on storage system - it is now consistently created in XML DAS");
				}
			}

			// Store URI For Possible Error Handling
			createdPaths.add(0,
					new Sapxmla_Config(sac.store_id, sac.archive_store,
							collData.getColURI(), sac.type, sac.win_root,
							sac.unix_root, sac.proxy_host, sac.proxy_port));

			// Insert BC_XMLA_COL_STORE Entry
			pst6.setLong(1, collData.getcolId());
			pst6.setLong(2, storeId);
			pst6.executeUpdate();

			// Update Collection Entry
			pst7.setLong(1, storeId);
			pst7.setLong(2, collData.getcolId());
			pst7.executeUpdate();

			// Recursive Call
			this.assignRecursiveAllDescendants(collData.getcolId());
		}
		return;
	}

	private void setPropertiesAndLegalHolds() throws SQLException,
			FinderException, IOException {
		boolean isLegalHoldSet = false;
		short currentILMConformanceClass = 0;
		long currentColID = 0;
		String currentURI = "";
		String expirationDateMemory = "";
		String startOfRetentionMemory = "";
		Sapxmla_Config currentSac = null;

		// Loop Downwards The Archive Path Hierarchy Starting From The
		// Root
		StringTokenizer st = new StringTokenizer(this.archive_path, "/");
		while (st.hasMoreTokens()) {

			// Calculate Current URI
			currentURI += "/" + st.nextToken();

			// Get Current Collection ID
			this.pst8.setString(1, currentURI);
			this.result = this.pst8.executeQuery();
			currentColID = 0;
			while (this.result.next())
				currentColID = this.result.getLong("COLID");
			this.result.close();

			// Get All Other Archive Stores For Current URI
			this.pst9.setLong(1, currentColID);
			this.result = pst9.executeQuery();
			ArrayList<Long> otherStoreIds = new ArrayList<Long>();
			while (this.result.next()) {
				long tmpStoreId = this.result.getLong("STOREID");
				if (tmpStoreId != this.storeId)
					otherStoreIds.add(new Long(tmpStoreId));
			}
			this.result.close();

			// Check If Collection Is Already Assigned On Other Archive Stores
			if (otherStoreIds.isEmpty() == true)
				break;

			// Check If Collection Is Already Assigned On Current Archive Store
			if (alreadyAssignedColsOnCurrentArchiveStore.contains(new Long(
					currentColID)))
				continue;

			// Check ILM Conformance Class Of Assigned Archive Stores
			currentILMConformanceClass = 0;
			Iterator<Long> storeIter = otherStoreIds.iterator();
			Sapxmla_Config sacILMConfClas0 = null;
			Sapxmla_Config sacILMConfClas1 = null;
			while (storeIter.hasNext()) {
				currentSac = this.beanLocalHome.findByPrimaryKey(
						storeIter.next()).getSapxmla_Config();
				if (currentSac.ilm_conformance >= 2) {
					currentILMConformanceClass = 2;
					break;
				} else if (currentSac.ilm_conformance == 1) {
					if (currentILMConformanceClass == 0) {
						currentILMConformanceClass = 1;
						sacILMConfClas1 = currentSac;
						continue;
					}
				} else if (currentSac.ilm_conformance == 0) {
					if ((currentILMConformanceClass == 0)
							&& (currentSac.type.equalsIgnoreCase("W"))
							&& (sacILMConfClas0 == null)) {
						sacILMConfClas0 = currentSac;
						continue;
					}
				}
			}
			if (currentILMConformanceClass == 0)
				if (sacILMConfClas0 == null)
					break;
				else
					currentSac = sacILMConfClas0;
			else if (currentILMConformanceClass == 1)
				currentSac = sacILMConfClas1;

			// Get All Properties From Current URI Of Other Archive
			// Store
			XmlDasPropFindRequest propFindRequest = new XmlDasPropFindRequest(
					currentSac, currentURI, "*", "COL");
			XmlDasPropFind propFind = new XmlDasPropFind(propFindRequest);
			XmlDasPropFindResponse propFindResponse = propFind.execute();
			if (propFindResponse.getStatusCode() != HttpServletResponse.SC_OK) {

				// Report Error
				if (propFindResponse.getException() == null)
					throw new IOException("PropFind response for archive path "
							+ currentURI + " on archive store "
							+ currentSac.archive_store + ": "
							+ propFindResponse.getStatusCode() + " "
							+ propFindResponse.getReasonPhrase() + " "
							+ propFindResponse.getEntityBody());
				else
					throw new IOException("PropFind response for archive path "
							+ currentURI + " on archive store "
							+ currentSac.archive_store + ": "
							+ propFindResponse.getStatusCode() + " "
							+ propFindResponse.getReasonPhrase() + " "
							+ propFindResponse.getEntityBody() + " "
							+ propFindResponse.getException().toString());
			}
			String currentProperties = propFindResponse.getEntityBody();
			if (currentProperties == null)
				currentProperties = "";
			else if (currentProperties.endsWith("\r\n"))
				currentProperties = currentProperties.substring(0,
						currentProperties.length() - 2);

			// Create A Property Container
			Properties props = new Properties();
			StringTokenizer stprops = new StringTokenizer(currentProperties,
					"#");
			while (stprops.hasMoreTokens()) {
				String prop = stprops.nextToken();
				props.put(prop.substring(0, prop.indexOf("=")).toLowerCase()
						.trim(), prop.substring(prop.indexOf("=") + 1, prop
						.length()));
			}

			// Check If "origin" Property Is Set
			String properties = "";
			if (props.containsKey(ORIGIN)) {
				if (props.getProperty(ORIGIN) == null)
					properties = ORIGIN + "=";
				else
					properties = ORIGIN + "=" + props.getProperty(ORIGIN);
			}

			// Check If "compulsory_destruction_date" Property Is Set
			if (props.containsKey(COMPULSORY_DESTRUCTION_DATE)) {
				if (properties.length() == 0) {
					if (props.getProperty(COMPULSORY_DESTRUCTION_DATE) == null)
						properties = COMPULSORY_DESTRUCTION_DATE + "=";
					else
						properties = COMPULSORY_DESTRUCTION_DATE
								+ "="
								+ props
										.getProperty(COMPULSORY_DESTRUCTION_DATE);
				} else {
					if (props.getProperty(COMPULSORY_DESTRUCTION_DATE) == null)
						properties += "#" + COMPULSORY_DESTRUCTION_DATE + "=";
					else
						properties += "#"
								+ COMPULSORY_DESTRUCTION_DATE
								+ "="
								+ props
										.getProperty(COMPULSORY_DESTRUCTION_DATE);
				}
			}

			// Check If "expiration_date" And "start_of_retention"
			// Properties Are Set
			if (props.containsKey(EXPIRATION_DATE)) {
				String expi_date = props.getProperty(EXPIRATION_DATE);
				if (expi_date == null)
					expi_date = "";
				else
					expi_date = expi_date.toLowerCase();

				// Check If "expiration_date" Property Has Changed To Previous
				// One
				if (!expi_date.equalsIgnoreCase(expirationDateMemory)) {

					// Store "start_of_retention" Property For Further
					// Processing
					if (props.containsKey(START_OF_RETENTION)) {
						if (props.getProperty(START_OF_RETENTION) != null)
							startOfRetentionMemory = props
									.getProperty(START_OF_RETENTION);
						else
							startOfRetentionMemory = "";
					}

					// Property "expiration_date" Is Unspecific
					if (expi_date.startsWith(UNKNOWN)) {
						if (properties.length() == 0) {
							if (startOfRetentionMemory.length() != 0)
								properties = START_OF_RETENTION + "="
										+ startOfRetentionMemory + "#"
										+ EXPIRATION_DATE + "=" + UNKNOWN;
							else
								properties = START_OF_RETENTION + "=" + UNKNOWN
										+ "#" + EXPIRATION_DATE + "=" + UNKNOWN;
						} else {
							if (startOfRetentionMemory.length() != 0)
								properties += "#" + START_OF_RETENTION + "="
										+ startOfRetentionMemory + "#"
										+ EXPIRATION_DATE + "=" + UNKNOWN;
							else
								properties += "#" + START_OF_RETENTION + "="
										+ UNKNOWN + "#" + EXPIRATION_DATE + "="
										+ UNKNOWN;
						}
						expirationDateMemory = UNKNOWN;
					}

					// Property "expiration_date" Is Specific
					else {

						// Get Formatted Date Strings
						int currentDateAsInteger = 0;
						try {
							if (expi_date.length() >= 10)
								currentDateAsInteger = Integer
										.parseInt(expi_date.substring(0, 4)
												+ expi_date.substring(5, 7)
												+ expi_date.substring(8, 10));
						} catch (NumberFormatException nfex) {

							// $JL-EXC$
							currentDateAsInteger = 0;
						}
						if (currentDateAsInteger != 0) {
							Date date = new Date();
							SimpleDateFormat sdf1 = new SimpleDateFormat(
									"yyyyMMdd");
							sdf1.setTimeZone(TimeZone.getTimeZone("UTC"));
							SimpleDateFormat sdf2 = new SimpleDateFormat(
									"yyyy-MM-dd");
							sdf2.setTimeZone(TimeZone.getTimeZone("UTC"));
							int todayDateAsInteger = 0;
							try {
								todayDateAsInteger = Integer.parseInt(sdf1
										.format(date));
							} catch (NumberFormatException nfex) {

								// $JL-EXC$
								todayDateAsInteger = 0;
							}
							if (todayDateAsInteger != 0) {
								String current_expi_date = sdf2.format(date);

								// Check If "expiration_date" Property Has
								// Already Expired
								if (currentDateAsInteger >= todayDateAsInteger) {
									if (properties.length() == 0) {
										if (startOfRetentionMemory.length() != 0)
											properties = START_OF_RETENTION
													+ "="
													+ startOfRetentionMemory
													+ "#" + EXPIRATION_DATE
													+ "=" + expi_date;
										else
											properties = START_OF_RETENTION
													+ "=" + UNKNOWN + "#"
													+ EXPIRATION_DATE + "="
													+ expi_date;
									} else {
										if (startOfRetentionMemory.length() != 0)
											properties += "#"
													+ START_OF_RETENTION + "="
													+ startOfRetentionMemory
													+ "#" + EXPIRATION_DATE
													+ "=" + expi_date;
										else
											properties += "#"
													+ START_OF_RETENTION + "="
													+ UNKNOWN + "#"
													+ EXPIRATION_DATE + "="
													+ expi_date;
									}
									expirationDateMemory = expi_date;
								} else {
									if (properties.length() == 0) {
										if (startOfRetentionMemory.length() != 0)
											properties = START_OF_RETENTION
													+ "="
													+ startOfRetentionMemory
													+ "#" + EXPIRATION_DATE
													+ "=" + current_expi_date;
										else
											properties = START_OF_RETENTION
													+ "=" + UNKNOWN + "#"
													+ EXPIRATION_DATE + "="
													+ current_expi_date;
									} else {
										if (startOfRetentionMemory.length() != 0)
											properties += "#"
													+ START_OF_RETENTION + "="
													+ startOfRetentionMemory
													+ "#" + EXPIRATION_DATE
													+ "=" + current_expi_date;
										else
											properties += "#"
													+ START_OF_RETENTION + "="
													+ UNKNOWN + "#"
													+ EXPIRATION_DATE + "="
													+ current_expi_date;
									}
									expirationDateMemory = current_expi_date;
								}
							}
						}
					}
				}
			}

			// Set Properties
			if (properties.length() != 0) {
				XmlDasPropPatchRequest propPatchRequest = new XmlDasPropPatchRequest(
						this.sac, currentURI, "Set", properties, "COL");
				XmlDasPropPatch propPatch = new XmlDasPropPatch(
						propPatchRequest);
				XmlDasPropPatchResponse propPatchResponse = propPatch.execute();
				if (propPatchResponse.getStatusCode() != HttpServletResponse.SC_OK) {

					// Report Error
					if (propPatchResponse.getException() == null)
						throw new IOException(
								"PropPatch response for archive path "
										+ currentURI + " on archive store "
										+ this.sac.archive_store + ": "
										+ propPatchResponse.getStatusCode()
										+ " "
										+ propPatchResponse.getReasonPhrase()
										+ " "
										+ propPatchResponse.getEntityBody());
					else
						throw new IOException(
								"PropPatch response for archive path "
										+ currentURI
										+ " on archive store "
										+ this.sac.archive_store
										+ ": "
										+ propPatchResponse.getStatusCode()
										+ " "
										+ propPatchResponse.getReasonPhrase()
										+ " "
										+ propPatchResponse.getEntityBody()
										+ " "
										+ propPatchResponse.getException()
												.toString());
				}
			}

			// Get All Legal Holds From Current URI Of Other Archive Store
			if (isLegalHoldSet == false) {
				XmlDasLegalHoldRequest legalHoldRequest = new XmlDasLegalHoldRequest(
						currentSac, currentURI, "N", "Get", "COL");
				XmlDasLegalHold legalHold = new XmlDasLegalHold(
						legalHoldRequest);
				XmlDasLegalHoldResponse legalHoldResponse = legalHold.execute();
				if (legalHoldResponse.getStatusCode() != HttpServletResponse.SC_OK) {

					// Report Error
					if (legalHoldResponse.getException() == null)
						throw new IOException(
								"Legal Hold Get response for archive path "
										+ currentURI + " on archive store "
										+ currentSac.archive_store + ": "
										+ propFindResponse.getStatusCode()
										+ " "
										+ propFindResponse.getReasonPhrase()
										+ " "
										+ propFindResponse.getEntityBody());
					else
						throw new IOException(
								"Legal Hold Get response for archive path "
										+ currentURI
										+ " on archive store "
										+ currentSac.archive_store
										+ ": "
										+ propFindResponse.getStatusCode()
										+ " "
										+ propFindResponse.getReasonPhrase()
										+ " "
										+ propFindResponse.getEntityBody()
										+ " "
										+ propFindResponse.getException()
												.toString());
				}
				String currentLegalHolds = legalHoldResponse.getEntityBody();
				if (currentLegalHolds == null)
					currentLegalHolds = "";
				StringTokenizer stlegalholds = new StringTokenizer(
						currentLegalHolds, "\r\n");

				// Set Legal Holds
				while (stlegalholds.hasMoreTokens()) {
					legalHoldRequest = new XmlDasLegalHoldRequest(this.sac,
							currentURI, stlegalholds.nextToken(), "Add", "COL");
					legalHold = new XmlDasLegalHold(legalHoldRequest);
					legalHoldResponse = legalHold.execute();
					if (legalHoldResponse.getStatusCode() != HttpServletResponse.SC_OK) {

						// Report Error
						if (legalHoldResponse.getException() == null)
							throw new IOException(
									"Legal Hold Add response for archive path "
											+ currentURI
											+ " on archive store "
											+ this.sac.archive_store
											+ ": "
											+ propFindResponse.getStatusCode()
											+ " "
											+ propFindResponse
													.getReasonPhrase() + " "
											+ propFindResponse.getEntityBody());
						else
							throw new IOException(
									"Legal Hold Add response for archive path "
											+ currentURI
											+ " on archive store "
											+ this.sac.archive_store
											+ ": "
											+ propFindResponse.getStatusCode()
											+ " "
											+ propFindResponse
													.getReasonPhrase()
											+ " "
											+ propFindResponse.getEntityBody()
											+ " "
											+ propFindResponse.getException()
													.toString());
					}

					// Set Legal Hold Already Set Flag
					isLegalHoldSet = true;
				}
			}
		}
	}

	private void unassignRecursiveAllDescendants(long parentColId)
			throws IOException, SQLException {
		String backupUri = "";
		pst1.setLong(1, parentColId);
		result = pst1.executeQuery();
		while (result.next())
			backupUri = result.getString("URI");
		result.close();

		ArrayList<CollectionData> al = new ArrayList<CollectionData>();
		pst2.setLong(1, parentColId);
		result = pst2.executeQuery();
		while (result.next())
			al.add(new CollectionData(result.getLong("COLID"), result
					.getString("URI"), result.getLong("STOREID")));
		result.close();

		if (al.isEmpty()) {

			// Delete Collection On Storage System
			deleteRequest = new XmlDasDeleteRequest(sac, backupUri, "COL");
			delete = new XmlDasDelete(deleteRequest);
			deleteResponse = delete.execute();
			if (!((deleteResponse.getStatusCode() == HttpServletResponse.SC_OK)
					|| (deleteResponse.getStatusCode() == HttpServletResponse.SC_ACCEPTED) || (deleteResponse
					.getStatusCode() == HttpServletResponse.SC_NO_CONTENT))) {
				if (deleteResponse.getException() == null) {
					MasterMethod.cat
							.errorT(
									loc,
									"_ASSIGN_ARCHIVE_STORES: I/O-Error while deleting collection "
											+ backupUri.substring(backupUri
													.lastIndexOf("/") + 1,
													backupUri.length())
											+ ": Archive store returned following response: "
											+ deleteResponse.getStatusCode()
											+ " "
											+ deleteResponse.getReasonPhrase()
											+ " "
											+ deleteResponse.getEntityBody());
					throw new IOException(
							"_ASSIGN_ARCHIVE_STORES: I/O-Error while deleting collection "
									+ backupUri.substring(backupUri
											.lastIndexOf("/") + 1, backupUri
											.length())
									+ ": Archive store returned following response: "
									+ deleteResponse.getStatusCode() + " "
									+ deleteResponse.getReasonPhrase() + " "
									+ deleteResponse.getEntityBody());
				} else {
					MasterMethod.cat
							.logThrowableT(
									Severity.ERROR,
									loc,
									"_ASSIGN_ARCHIVE_STORES: I/O-Error while deleting collection "
											+ backupUri.substring(backupUri
													.lastIndexOf("/") + 1,
													backupUri.length())
											+ ": Archive store returned following response: "
											+ deleteResponse.getStatusCode()
											+ " "
											+ deleteResponse.getReasonPhrase()
											+ " "
											+ deleteResponse.getEntityBody()
											+ " "
											+ getStackTrace(deleteResponse
													.getException()),
									deleteResponse.getException());
					throw new IOException(
							"_ASSIGN_ARCHIVE_STORES: I/O-Error while deleting collection "
									+ backupUri.substring(backupUri
											.lastIndexOf("/") + 1, backupUri
											.length())
									+ ": Archive store returned following response: "
									+ deleteResponse.getStatusCode() + " "
									+ deleteResponse.getReasonPhrase() + " "
									+ deleteResponse.getEntityBody() + " "
									+ deleteResponse.getException().toString());
				}
			}

			// Store URI For Possible Error Handling
			deletedPaths.add(0,
					new Sapxmla_Config(sac.store_id, sac.archive_store,
							backupUri.trim(), sac.type, sac.win_root,
							sac.unix_root, sac.proxy_host, sac.proxy_port));

			// Delete BC_XMLA_COL_STORE Entry
			pst3.setLong(1, parentColId);
			pst3.setLong(2, storeId);
			pst3.executeUpdate();

			// Delete BC_XMLA_COL_PROP Entry
			pst11.setLong(1, parentColId);
			pst11.executeUpdate();

			// Update BC_XMLA_COL Entry
			pst4.setNull(1, Types.NULL);
			pst4.setLong(2, parentColId);
			pst4.executeUpdate();

			return;
		}

		for (Iterator<CollectionData> iter = al.iterator(); iter.hasNext();) {
			CollectionData collData = iter.next();
			unassignRecursiveAllDescendants(collData.getcolId());
		}

		// Delete Collection On Storage System
		deleteRequest = new XmlDasDeleteRequest(sac, backupUri, "COL");
		delete = new XmlDasDelete(deleteRequest);
		deleteResponse = delete.execute();
		if (!((deleteResponse.getStatusCode() == HttpServletResponse.SC_OK)
				|| (deleteResponse.getStatusCode() == HttpServletResponse.SC_ACCEPTED) || (deleteResponse
				.getStatusCode() == HttpServletResponse.SC_NO_CONTENT))) {
			if (deleteResponse.getException() == null) {
				MasterMethod.cat
						.errorT(
								loc,
								"_ASSIGN_ARCHIVE_STORES: I/O-Error while deleting collection "
										+ backupUri.substring(backupUri
												.lastIndexOf("/") + 1,
												backupUri.length())
										+ ": Archive store returned following response: "
										+ deleteResponse.getStatusCode() + " "
										+ deleteResponse.getReasonPhrase()
										+ " " + deleteResponse.getEntityBody());
				throw new IOException(
						"_ASSIGN_ARCHIVE_STORES: I/O-Error while deleting collection "
								+ backupUri.substring(backupUri
										.lastIndexOf("/") + 1, backupUri
										.length())
								+ ": Archive store returned following response: "
								+ deleteResponse.getStatusCode() + " "
								+ deleteResponse.getReasonPhrase() + " "
								+ deleteResponse.getEntityBody());
			} else {
				MasterMethod.cat
						.logThrowableT(
								Severity.ERROR,
								loc,
								"_ASSIGN_ARCHIVE_STORES: I/O-Error while deleting collection "
										+ backupUri.substring(backupUri
												.lastIndexOf("/") + 1,
												backupUri.length())
										+ ": Archive store returned following response: "
										+ deleteResponse.getStatusCode()
										+ " "
										+ deleteResponse.getReasonPhrase()
										+ " "
										+ deleteResponse.getEntityBody()
										+ " "
										+ getStackTrace(deleteResponse
												.getException()),
								deleteResponse.getException());
				throw new IOException(
						"_ASSIGN_ARCHIVE_STORES: I/O-Error while deleting collection "
								+ backupUri.substring(backupUri
										.lastIndexOf("/") + 1, backupUri
										.length())
								+ ": Archive store returned following response: "
								+ deleteResponse.getStatusCode() + " "
								+ deleteResponse.getReasonPhrase() + " "
								+ deleteResponse.getEntityBody() + " "
								+ deleteResponse.getException().toString());
			}
		}

		// Store URI For Possible Error Handling
		deletedPaths.add(0, new Sapxmla_Config(sac.store_id, sac.archive_store,
				backupUri.trim(), sac.type, sac.win_root, sac.unix_root,
				sac.proxy_host, sac.proxy_port));

		// Delete BC_XMLA_COL_STORE Entry
		pst3.setLong(1, parentColId);
		pst3.setLong(2, storeId);
		pst3.executeUpdate();

		// Delete BC_XMLA_COL_PROP Entry
		pst11.setLong(1, parentColId);
		pst11.executeUpdate();

		// Update BC_XMLA_COL Entry
		pst4.setNull(1, Types.NULL);
		pst4.setLong(2, parentColId);
		pst4.executeUpdate();

		return;
	}

	private void unassignAllAncestors() throws IOException, SQLException {
		// Loop Upwards From Selected Collection To Root Collection
		String actualPath = uri;
		while (parentColId > 0) {
			// Adjust Collection Id
			colId = parentColId;

			// Adjust Boolean Variables
			boolean colDescendantHasColStoreEntry = false;

			// Adjust Actual Path For Further Processing
			actualPath = actualPath.substring(0, actualPath.lastIndexOf("/"));

			// Get Parent Collection Id
			pst5.setLong(1, colId);
			result = pst5.executeQuery();
			while (result.next())
				parentColId = result.getLong("PARENTCOLID");
			result.close();

			// Check If Collection Has At Least One Home Collection As
			// Descendant
			ArrayList<Long> descendantHomeCols = new ArrayList<Long>();
			pst6.setString(1, actualPath + "/%");
			result = pst6.executeQuery();
			hits = 0;
			while (result.next()) {
				descendantHomeCols.add(new Long(result.getLong("COLID")));
				hits++;
			}
			result.close();
			if (hits != 0) {

				Iterator<Long> it = descendantHomeCols.iterator();
				while (it.hasNext()) {

					// Check If At Least One Descendant Collection Has An
					// BC_XMLA_COL_STORE Entry
					pst7.setLong(1, it.next().longValue());
					pst7.setLong(2, storeId);
					result = pst7.executeQuery();
					hits = 0;
					while (result.next())
						hits++;
					result.close();
					if (hits != 0) {
						colDescendantHasColStoreEntry = true;
						break;
					}
				}
			}

			// Delete System Collection On Archive Store
			if (colDescendantHasColStoreEntry == false) {
				// Delete Collection On Storage System
				deleteRequest = new XmlDasDeleteRequest(sac, actualPath.trim(),
						"COL");
				delete = new XmlDasDelete(deleteRequest);
				deleteResponse = delete.execute();
				if (!((deleteResponse.getStatusCode() == HttpServletResponse.SC_OK)
						|| (deleteResponse.getStatusCode() == HttpServletResponse.SC_ACCEPTED) || (deleteResponse
						.getStatusCode() == HttpServletResponse.SC_NO_CONTENT))) {
					if (deleteResponse.getException() == null) {
						MasterMethod.cat
								.errorT(
										loc,
										"_ASSIGN_ARCHIVE_STORES: I/O-Error while deleting collection "
												+ actualPath
														.substring(
																actualPath
																		.lastIndexOf("/") + 1,
																actualPath
																		.length())
												+ ": Archive store returned following response: "
												+ deleteResponse
														.getStatusCode()
												+ " "
												+ deleteResponse
														.getReasonPhrase()
												+ " "
												+ deleteResponse
														.getEntityBody());
						throw new IOException(
								"_ASSIGN_ARCHIVE_STORES: I/O-Error while deleting collection "
										+ actualPath.substring(actualPath
												.lastIndexOf("/") + 1,
												actualPath.length())
										+ ": Archive store returned following response: "
										+ deleteResponse.getStatusCode() + " "
										+ deleteResponse.getReasonPhrase()
										+ " " + deleteResponse.getEntityBody());
					} else {
						MasterMethod.cat
								.logThrowableT(
										Severity.ERROR,
										loc,
										"_ASSIGN_ARCHIVE_STORES: I/O-Error while deleting collection "
												+ actualPath
														.substring(
																actualPath
																		.lastIndexOf("/") + 1,
																actualPath
																		.length())
												+ ": Archive store returned following response: "
												+ deleteResponse
														.getStatusCode()
												+ " "
												+ deleteResponse
														.getReasonPhrase()
												+ " "
												+ deleteResponse
														.getEntityBody()
												+ " "
												+ getStackTrace(deleteResponse
														.getException()),
										deleteResponse.getException());
						throw new IOException(
								"_ASSIGN_ARCHIVE_STORES: I/O-Error while deleting collection "
										+ actualPath.substring(actualPath
												.lastIndexOf("/") + 1,
												actualPath.length())
										+ ": Archive store returned following response: "
										+ deleteResponse.getStatusCode()
										+ " "
										+ deleteResponse.getReasonPhrase()
										+ " "
										+ deleteResponse.getEntityBody()
										+ " "
										+ deleteResponse.getException()
												.toString());
					}
				}

				// Store URI For Possible Error Handling
				deletedPaths.add(0, new Sapxmla_Config(sac.store_id,
						sac.archive_store, actualPath.trim(), sac.type,
						sac.win_root, sac.unix_root, sac.proxy_host,
						sac.proxy_port));

				// Delete BC_XMLA_COL_STORE Entry
				pst8.setLong(1, colId);
				pst8.setLong(2, storeId);
				pst8.executeUpdate();

				// Check If BC_XMLA_COL_PROP Entry Could Be Deleted
				pst9.setLong(1, colId);
				result = pst9.executeQuery();
				hits = 0;
				while (result.next())
					if (result.getLong("STOREID") != storeId)
						hits++;
				result.close();

				// Delete BC_XMLA_COL_PROP Entry
				if (hits == 0) {
					pst10.setLong(1, colId);
					pst10.executeUpdate();
				}
			}
		} // end while
	}

	private ArrayList<ArchivePathData> listArchivePath() throws SQLException,
			ArchStoreConfigException {
		ArrayList<ArchivePathData> archivePath = new ArrayList<ArchivePathData>();
		ArchivePathData archivePathData = new ArchivePathData();
		pst0 = connection.prepareStatement(ALL_SEL_COL_TAB5);
		pst0.setLong(1, this.colIdBackup);
		result = pst0.executeQuery();
		while (result.next()) {
			archivePathData.setArchivePath(result.getString("URI"));
			archivePathData.setCollectionType(result.getString("COLTYPE"));
			archivePathData
					.setCreationTime(result.getTimestamp("CREATIONTIME"));
			archivePathData.setCreationUser(result.getString("CREATIONUSER"));
			long storeId = result.getLong("STOREID");
			if (storeId > 0) {
				Sapxmla_Config sac = this.getArchStoreConfigObject(
						beanLocalHome, storeId);
				archivePathData.setArchiveStore(sac.archive_store);
				archivePathData.setStorageSystem(sac.storage_system);
			}
			if (result.getString("FROZEN").startsWith("Y"))
				archivePathData.setFrozen(true);
			else
				archivePathData.setFrozen(false);
			archivePathData.setColId(this.colId);
			archivePath.add(archivePathData);
		}
		result.close();
		pst0.close();
		return archivePath;
	}

	private ArrayList<ArchivePathData> listArchivePaths() throws SQLException,
			ArchStoreConfigException {
		ArrayList<ArchivePathData> archivePaths = new ArrayList<ArchivePathData>();
		pst0 = connection.prepareStatement(ALL_SEL_COL_TAB2);
		result = pst0.executeQuery();
		while (result.next()) {
			long colid = result.getLong("COLID");
			long storeid = result.getLong("STOREID");
			long parentcolid = result.getLong("PARENTCOLID");
			if ((this.hasAssignedAncestor(parentcolid))
					|| (storeid == 0 && this.hasAssignedDescendants(colid))
					|| (storeid != 0 && this
							.hasAssignedDescendantsContainingResources(colid)))
				continue;
			else {
				ArchivePathData archivePathData = new ArchivePathData();
				archivePathData.setArchivePath(result.getString("URI"));
				archivePathData.setCollectionType(result.getString("COLTYPE"));
				archivePathData.setCreationTime(result
						.getTimestamp("CREATIONTIME"));
				archivePathData.setCreationUser(result
						.getString("CREATIONUSER"));
				long storeId = result.getLong("STOREID");
				if (storeId > 0) {
					Sapxmla_Config sac = this.getArchStoreConfigObject(
							beanLocalHome, storeId);
					archivePathData.setArchiveStore(sac.archive_store);
					archivePathData.setStorageSystem(sac.storage_system);
				}
				if (result.getString("FROZEN").startsWith("Y"))
					archivePathData.setFrozen(true);
				else
					archivePathData.setFrozen(false);
				archivePaths.add(archivePathData);
			}
		}
		result.close();
		pst0.close();
		return archivePaths;
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