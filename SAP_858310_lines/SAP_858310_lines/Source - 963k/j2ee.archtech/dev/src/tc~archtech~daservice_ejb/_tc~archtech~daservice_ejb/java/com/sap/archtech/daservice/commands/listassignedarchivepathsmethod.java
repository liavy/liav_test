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
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.FinderException;
import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.archconn.values.ArchivePathData;
import com.sap.archtech.daservice.data.CollectionData;
import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocal;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;

public class ListAssignedArchivePathsMethod extends MasterMethod {

	private final static String SEL_COL_TAB = "SELECT * FROM BC_XMLA_COL WHERE PARENTCOLID = ?";

	private Connection connection;
	private ArchStoreConfigLocalHome beanLocalHome;
	private String user;
	private String archive_store;
	private String storage_system;
	private ArrayList<ArchivePathData> archivePathList = new ArrayList<ArchivePathData>();;
	private long storeId;

	public ListAssignedArchivePathsMethod(HttpServletResponse response,
			Connection connection, ArchStoreConfigLocalHome beanLocalHome,
			String user, String archive_store) {
		this.response = response;
		this.connection = connection;
		this.beanLocalHome = beanLocalHome;
		this.user = user;
		this.archive_store = archive_store;
	}

	public boolean execute() throws IOException {

		// Initialize Status Flag
		boolean status = false;

		// Get Servlet Output Stream
		BufferedWriter bwout = new BufferedWriter(new OutputStreamWriter(
				response.getOutputStream(), "UTF8"));

		try {

			// Set Response Header
			response.setContentType(MasterMethod.contentType);
			response.setHeader("service_message", "see message body");

			// Check Request Header "archive_store"
			if ((this.archive_store == null)
					|| (this.archive_store.length() == 0)) {
				this
						.reportStreamError(
								DasResponse.SC_PARAMETER_MISSING,
								"_LIST_ASSIGNED_ARCHIVE_PATHS: ARCHIVE_STORE missing from request header",
								bwout);
				return status;
			} else {
				this.archive_store = this.archive_store.trim().toUpperCase();
			}

			// Check Request Header "user"
			if ((this.user == null) || (this.user.length() == 0)) {
				this
						.reportStreamError(
								DasResponse.SC_PARAMETER_MISSING,
								"_LIST_ASSIGNED_ARCHIVE_PATHS: USER missing from request header",
								bwout);
				return status;
			}

			// Get Archive Store ID
			Collection<ArchStoreConfigLocal> col = beanLocalHome
					.findByArchiveStore(this.archive_store);
			Iterator<ArchStoreConfigLocal> iter = col.iterator();
			if (col.isEmpty()) {

				// Report Error
				this.reportStreamError(DasResponse.SC_CONFIG_INCONSISTENT,
						"_LIST_ASSIGNED_ARCHIVE_PATHS: Archive store "
								+ this.archive_store + " not defined", bwout);
				return status;
			} else {
				ArchStoreConfigLocal ascl = (ArchStoreConfigLocal) iter.next();
				storeId = ((Long) ascl.getPrimaryKey()).longValue();
				storage_system = ascl.getStoragesystem();
			}

			// Recursive Search Archive Paths Starting With Root Archive Path
			this.traverse(0);

			// Write Serialized Object Into Servlet Output Stream
			ObjectOutputStream oos = new ObjectOutputStream(response
					.getOutputStream());
			oos.writeObject(this.archivePathList);
			oos.flush();

			// Write Success To Servlet Output Stream
			this.writeStatus(bwout, HttpServletResponse.SC_OK, "Ok");
			bwout.flush();

			// Method Was Successful
			status = true;
		} catch (FinderException fex) {
			// Report Error
			this.reportStreamError(DasResponse.SC_CONFIG_INCONSISTENT,
					"_LIST_ASSIGNED_ARCHIVE_PATHS: " + fex.toString(), bwout);
			return status;
		} catch (SQLException sqlex) {
			// Report Error
			this.reportStreamError(DasResponse.SC_CONFIG_INCONSISTENT,
					"_LIST_ASSIGNED_ARCHIVE_PATHS: " + sqlex.toString(), bwout);
			return status;
		} finally {

			// Close Servlet Output Stream
			if (bwout != null) {
				bwout.close();
			}
		}
		return status;
	}

	private void traverse(long parentColId) throws SQLException {
		PreparedStatement pst1 = null;
		try {
			pst1 = connection.prepareStatement(SEL_COL_TAB);
			pst1.setLong(1, parentColId);
			ResultSet result1 = pst1.executeQuery();
			long cId = 0;
			long sId = 0;
			ArrayList<CollectionData> al = new ArrayList<CollectionData>();
			while (result1.next()) {
				cId = result1.getLong("COLID");
				sId = result1.getLong("STOREID");
				if (sId == 0) {
					al.add(new CollectionData(cId, sId));
				} else {
					if (sId == this.storeId) {
						ArchivePathData element = new ArchivePathData();
						element.setColId(cId);
						element.setArchivePath(result1.getString("URI"));
						element.setCreationTime(result1
								.getTimestamp("CREATIONTIME"));
						element.setCreationUser(result1
								.getString("CREATIONUSER"));
						if (result1.getString("FROZEN").startsWith("Y"))
							element.setFrozen(true);
						else
							element.setFrozen(false);
						element.setArchiveStore(this.archive_store);
						element.setStorageSystem(this.storage_system);
						element.setCollectionType(result1.getString("COLTYPE"));
						this.archivePathList.add(element);
					}
				}
			}
			result1.close();
			pst1.close();
			for (Iterator<CollectionData> iter = al.iterator(); iter.hasNext();) {
				CollectionData element = (CollectionData) iter.next();
				this.traverse(element.getcolId());
			}
			return;
		} finally {
			if (pst1 != null)
				pst1.close();
		}
	}
}