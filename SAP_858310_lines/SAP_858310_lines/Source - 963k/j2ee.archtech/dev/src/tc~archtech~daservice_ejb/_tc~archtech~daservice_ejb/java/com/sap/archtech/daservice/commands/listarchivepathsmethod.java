package com.sap.archtech.daservice.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.archconn.values.ArchivePathData;
import com.sap.archtech.daservice.data.CollectionData;
import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;
import com.sap.archtech.daservice.exceptions.ArchStoreConfigException;
import com.sap.archtech.daservice.exceptions.InvalidNameException;
import com.sap.tc.logging.Severity;

public class ListArchivePathsMethod extends MasterMethod {

	private final static String SEL_RES_TAB1 = "SELECT RESID FROM BC_XMLA_RES WHERE COLID = ?";
	private final static String SEL_COL_TAB1 = "SELECT * FROM BC_XMLA_COL WHERE COLTYPE = ? ORDER BY URI";
	private final static String SEL_COL_TAB2 = "SELECT COLID, COLTYPE FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_COL_TAB3 = "SELECT * FROM BC_XMLA_COL WHERE PARENTCOLID = ?";
	private final static String SEL_COL_TAB4 = "SELECT COLID, STOREID FROM BC_XMLA_COL WHERE PARENTCOLID = ?";
	private final static String SEL_COL_TAB5 = "SELECT STOREID FROM BC_XMLA_COL WHERE COLID = ?";
	private final static String SEL_COL_TAB6 = "SELECT COLID, PARENTCOLID, STOREID FROM BC_XMLA_COL WHERE URI = ?";

	private Connection connection;
	private ArchStoreConfigLocalHome beanLocalHome;
	private String type;
	private String user;
	private String archive_path;

	public ListArchivePathsMethod(HttpServletResponse response,
			Connection connection, ArchStoreConfigLocalHome beanLocalHome,
			String type, String user, String archive_path) {
		this.response = response;
		this.connection = connection;
		this.beanLocalHome = beanLocalHome;
		this.type = type;
		this.user = user;
		this.archive_path = archive_path;
	}

	public boolean execute() throws IOException {

		// Variables
		PreparedStatement pst = null;
		ResultSet result = null;

		// Get Servlet Output Stream
		BufferedWriter bwout = new BufferedWriter(new OutputStreamWriter(
				response.getOutputStream(), "UTF8"));

		// Set Response Header
		response.setContentType(MasterMethod.contentType);
		response.setHeader("service_message", "see message body");

		// Check Request Header "type"
		if (this.type == null) {
			this.reportStreamError(DasResponse.SC_PARAMETER_MISSING,
					"_LIST_ARCHIVE_PATHS: TYPE missing from request header",
					bwout);
			return false;
		} else {
			this.type = this.type.toUpperCase();
			if (!(this.type.startsWith("H") || this.type.startsWith("A")
					|| this.type.startsWith("S") || this.type.startsWith("X"))) {
				this
						.reportStreamError(
								DasResponse.SC_KEYWORD_UNKNOWN,
								"_LIST_ARCHIVE_PATHS: Value "
										+ this.type
										+ " of request header TYPE does not meet specifications",
								bwout);
				return false;
			}
		}

		// Check Request Header "user"
		if ((this.user == null) || (this.user.length() == 0)) {
			this.reportStreamError(DasResponse.SC_PARAMETER_MISSING,
					"_LIST_ARCHIVE_PATHS: USER missing from request header",
					bwout);
			return false;
		}

		// Check Request Header "archive_path"
		if (this.type.startsWith("A") || this.type.startsWith("S")
				|| this.type.startsWith("X")) {
			if (this.archive_path != null) {
				try {
					this.isValidName(this.archive_path, true);
				} catch (InvalidNameException inex) {

					// $JL-EXC$
					this.reportStreamError(DasResponse.SC_INVALID_CHARACTER,
							"_LIST_ARCHIVE_PATHS: " + inex.getMessage(), bwout);
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
									"_LIST_ARCHIVE_PATHS: Value "
											+ this.archive_path
											+ " of request header ARCHIVE_PATH does not meet specifications",
									bwout);
					return false;
				}
			} else {
				this
						.reportStreamError(
								DasResponse.SC_PARAMETER_MISSING,
								"_LIST_ARCHIVE_PATHS: ARCHIVE_PATH missing from request header",
								bwout);
				return false;
			}
		}

		ObjectOutputStream oos = null;
		boolean status = false;
		long colId = 0;
		long parentColId = 0;
		long storeId = 0;
		int hits = 0;
		String colType = "";
		try {

			// Get Archive Path Entries
			ArrayList<ArchivePathData> archivePaths = new ArrayList<ArchivePathData>();

			if (this.type.startsWith("X")) {

				// Get Collection Id
				pst = connection.prepareStatement(SEL_COL_TAB6);
				pst.setString(1, this.archive_path.substring(0,
						this.archive_path.length() - 1));
				result = pst.executeQuery();
				while (result.next()) {
					colId = result.getLong("COLID");
					parentColId = result.getLong("PARENTCOLID");
					storeId = result.getLong("STOREID");
					hits++;
				}
				result.close();
				pst.close();
				if (hits != 1) {
					this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
							"_LIST_ARCHIVE_PATHS: Archive Path "
									+ this.archive_path + " does not exist",
							bwout);
					return false;
				}

				// Check If Collection Is Assignable / Unassignable
				ArchivePathData archivePathData = new ArchivePathData();
				if ((this.hasAssignedAncestor(parentColId))
						|| (storeId == 0 && this.hasAssignedDescendants(colId))
						|| (storeId != 0 && this
								.hasAssignedDescendantsContainingResources(colId))) {
					archivePathData.setHasChildren(true);

				} else {
					archivePathData.setHasChildren(false);
				}
				archivePathData.setColId(storeId);

				// Add Archive Store Entry To Response Array List
				archivePaths.add(archivePathData);
			} else if (this.type.startsWith("S")) {

				// Get Collection Id
				pst = connection.prepareStatement(SEL_COL_TAB6);
				pst.setString(1, this.archive_path.substring(0,
						this.archive_path.length() - 1));
				result = pst.executeQuery();
				while (result.next()) {
					colId = result.getLong("COLID");
					hits++;
				}
				result.close();
				pst.close();
				if (hits != 1) {
					this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
							"_LIST_ARCHIVE_PATHS: Archive Path "
									+ this.archive_path + " does not exist",
							bwout);
					return false;
				}

				// Check If At Least One Assigned Descendant Exists
				ArchivePathData archivePathData = new ArchivePathData();
				if (hasAssignedDescendants(colId)) {
					archivePathData.setHasChildren(true);
				} else {
					archivePathData.setHasChildren(false);
				}

				// Add Archive Store Entry To Response Array List
				archivePaths.add(archivePathData);
			} else {

				if (this.type.startsWith("H")) {
					pst = connection.prepareStatement(SEL_COL_TAB1);
					pst.setString(1, this.type);
				} else {

					// Get Collection Id
					pst = connection.prepareStatement(SEL_COL_TAB2);
					pst.setString(1, this.archive_path.substring(0,
							this.archive_path.length() - 1));
					result = pst.executeQuery();
					while (result.next()) {
						colId = result.getLong("COLID");
						colType = result.getString("COLTYPE");
						hits++;
					}
					result.close();
					pst.close();					
					if (hits != 1) {
						this
								.reportStreamError(
										DasResponse.SC_DOES_NOT_EXISTS,
										"_LIST_ARCHIVE_PATHS: Archive Path "
												+ this.archive_path
												+ " does not exist", bwout);
						return false;
					}
					if (colType.equalsIgnoreCase("S")) {
						this
								.reportStreamError(
										DasResponse.SC_PARAMETERS_INCONSISTENT,
										"_LIST_ARCHIVE_PATHS: Archive Path "
												+ this.archive_path
												+ " is not an home or application path",
										bwout);
						return false;
					}

					// Get Direct Descendant Archive Paths
					pst = connection.prepareStatement(SEL_COL_TAB3);
					pst.setLong(1, colId);
				}
				result = pst.executeQuery();
				while (result.next()) {

					// Add Archive Store Configuration To Response Object
					ArchivePathData archivePathData = new ArchivePathData();
					archivePathData.setColId(result.getLong("COLID"));
					archivePathData.setArchivePath(result.getString("URI"));
					archivePathData.setCollectionType(result
							.getString("COLTYPE"));
					archivePathData.setCreationTime(result
							.getTimestamp("CREATIONTIME"));
					archivePathData.setCreationUser(result
							.getString("CREATIONUSER"));
					storeId = result.getLong("STOREID");
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

					// Add Archive Store Entry To Response Array List
					archivePaths.add(archivePathData);
				}
				result.close();
				pst.close();				

				// Check If Archive Paths Have Children
				Iterator<ArchivePathData> iter = archivePaths.iterator();
				pst = connection.prepareStatement(SEL_COL_TAB3);
				while (iter.hasNext()) {
					ArchivePathData archivePathData = iter.next();
					colId = archivePathData.getColId();
					hits = 0;
					pst.setLong(1, colId);
					result = pst.executeQuery();
					while (result.next())
						hits++;
					if (hits == 0)
						archivePathData.setHasChildren(false);
					else
						archivePathData.setHasChildren(true);
				}
				result.close();
				pst.close();
			}

			// Write Serialized Object Into Servlet Output Stream
			oos = new ObjectOutputStream(response.getOutputStream());
			oos.writeObject(archivePaths);
			oos.flush();

			// Write Success To Servlet Output Stream
			this.writeStatus(bwout, HttpServletResponse.SC_OK, "Ok");
			bwout.flush();

			// Set Status
			status = true;
		} catch (ArchStoreConfigException ascex) {

			// Report Error
			this.reportStreamError(DasResponse.SC_CONFIG_INCONSISTENT,
					"_LIST_ARCHIVE_PATHS: " + ascex.getMessage(), ascex, bwout);
		} catch (SQLException sqlex) {

			// Report Error
			this.reportStreamError(DasResponse.SC_SQL_ERROR,
					"_LIST_ARCHIVE_PATHS: " + sqlex.getMessage(), sqlex, bwout);
		} catch (IOException ioex) {

			// Report Error
			this.reportStreamError(DasResponse.SC_IO_ERROR,
					"_LIST_ARCHIVE_PATHS: " + ioex.getMessage(), ioex, bwout);
		} finally {

			// Close Prepared Statements
			try {
				if (result != null)
					result.close();
				if (pst != null)
					pst.close();
			} catch (SQLException sqlex) {
				cat.logThrowableT(Severity.WARNING, loc,
						"_LIST_ARCHIVE_STORES: " + sqlex.getMessage(), sqlex);
			}

			// Close Servlet Output Stream
			if (bwout != null) {
				bwout.close();
			}
		}

		// Method Was Successful
		return status;
	}

	private boolean hasAssignedDescendants(long colId) throws SQLException {
		boolean isAssigned = false;
		PreparedStatement pst1 = null;
		try {
			pst1 = connection.prepareStatement(SEL_COL_TAB4);
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
			pst1 = connection.prepareStatement(SEL_RES_TAB1);
			pst1.setLong(1, colId);
			pst1.setMaxRows(1);
			ResultSet result1 = pst1.executeQuery();
			while (result1.next())
				hits++;
			result1.close();
			pst1.close();
			if (hits != 0)
				return true;
			pst2 = connection.prepareStatement(SEL_COL_TAB4);
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
			pst1 = connection.prepareStatement(SEL_COL_TAB5);
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
}