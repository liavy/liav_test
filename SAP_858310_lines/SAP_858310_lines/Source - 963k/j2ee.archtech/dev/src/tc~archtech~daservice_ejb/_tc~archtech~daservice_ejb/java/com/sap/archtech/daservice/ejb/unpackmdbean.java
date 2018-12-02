package com.sap.archtech.daservice.ejb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import javax.ejb.FinderException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.ejb.RemoveException;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.sap.archtech.daservice.data.ResourceData;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.storage.XmlDasDelete;
import com.sap.archtech.daservice.storage.XmlDasDeleteRequest;
import com.sap.archtech.daservice.storage.XmlDasGet;
import com.sap.archtech.daservice.storage.XmlDasGetRequest;
import com.sap.archtech.daservice.storage.XmlDasGetResponse;
import com.sap.archtech.daservice.storage.XmlDasPut;
import com.sap.archtech.daservice.storage.XmlDasPutRequest;
import com.sap.archtech.daservice.storage.XmlDasPutResponse;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class UnpackMDBean implements MessageDrivenBean, MessageListener {

	private static final long serialVersionUID = 1234567890l;
	private static final int INITIAL_LIST_SIZE = 500;
	private static final Location loc = Location
			.getLocation("com.sap.archtech.daservice");
	private static final Category cat = Category.getCategory(
			Category.APPS_COMMON_ARCHIVING, "XML_DAS");
	private final static String UPD_RES = "UPDATE BC_XMLA_RES SET packName = ?, packLength = ?, offset = ?, packUser = ?, packTime = ?, isPacked = 'N' WHERE resId = ?";
	private final static String GET_RESURI_OF_COLL = "SELECT resid, resname, restype, fingerprint, packlength, offset, packname FROM BC_XMLA_RES WHERE colId = ? and isPacked ='Y' ORDER BY packName, offset";

	private transient MessageDrivenContext mdc = null;

	public void ejbCreate() {
		loc.debugT("In UnpackMDBBean.ejbCreate()");
	}

	public void ejbRemove() {
		loc.debugT("In UnpackMDBBean.ejbRemove()");
	}

	public void setMessageDrivenContext(MessageDrivenContext context) {
		loc.debugT("In UnpackMDBBean.setMessageDrivenContext()");
		this.mdc = context;
	}

	/**
	 * @see javax.jms.MessageListener#onMessage(Message)
	 */
	public void onMessage(Message inMessage) {

		MapMessage msg = null;
		long colid = -1;
		int commitcount;
		PreparedStatement pst4 = null;
		PreparedStatement pst5 = null;
		DataSource ds;
		java.sql.Connection con = null;
		ArrayList<ResourceData> reslist;

		PackStatusDBLocalHome pHome;
		PackStatusDBLocal pAccess = null;

		try {
			if (inMessage instanceof MapMessage) {
				msg = (MapMessage) inMessage;
				colid = msg.getLong("colid");
				loc.debugT("UNPACK MESSAGE BEAN: Message received");
				loc.debugT(msg.getString("coluri"));
				loc.debugT(String.valueOf(colid));
				loc.debugT(msg.getString("user"));
			} else {
				cat.errorT(loc, "Message of wrong type: "
						+ inMessage.getClass().getName());
				return;
			}
			loc.debugT("UnpackMDBean started");

			// Obtain our environment naming context
			Context initCtx = new InitialContext();
			// Look up our data source
			ds = (DataSource) initCtx.lookup("java:comp/env/SAP/BC_XMLA");
			con = ds.getConnection();
			con.setAutoCommit(false);
			pHome = (PackStatusDBLocalHome) initCtx
					.lookup("java:comp/env/ejb/PackStatusDBBean");
			pAccess = pHome.findByPrimaryKey(Long.valueOf(colid));
			loc.debugT("UnpackMDBean: Got DB connection");

			pst4 = con.prepareStatement(UPD_RES);
			pst5 = con.prepareStatement(GET_RESURI_OF_COLL);

			reslist = this.getResToUnpack(colid, pst5);
			loc.debugT("UnpackMDBean: Got list of " + reslist.size()
					+ " resources to unpack");

			Sapxmla_Config sac = new Sapxmla_Config(Long.parseLong(msg
					.getString("sac.storeid")), msg
					.getString("sac.archive_store"), msg
					.getString("sac.storage_system"),
					msg.getString("sac.type"), msg.getString("sac.win_root"),
					msg.getString("sac.unix_root"), msg
							.getString("sac.proxy_host"), msg
							.getInt("sac.proxy_port"));

			commitcount = this.calculateCommitCounter(reslist);

			loc.debugT("UnpackMDBean: Start unpacking resources");
			this.unpackFiles(msg.getString("coluri"), commitcount, reslist,
					sac, pAccess);

			loc.debugT("UnpackMDBean: unpacking finished, start updating DB");
			this.setPollMessage("UPDATING_DB", pAccess);
			this.updateResTable(reslist, msg.getString("user"), pst4);
			con.commit();

			loc.debugT("UnpackMDBean: deleting non-packed resources");
			this.setPollMessage("DELETE_RES", pAccess);
			this.deleteRes(sac, reslist, msg.getString("coluri"));

			this.deleteCount(pAccess);

			loc.debugT("UnpackMDBean: finished");
		} catch (JMSException jmsex) {
			reportBeanError(jmsex, pAccess);
			// mdc.setRollbackOnly();
			return;
		} catch (SQLException sqlex) {
			reportBeanError(sqlex, pAccess);
			return;
		} catch (FinderException finex) {
			reportBeanError(finex, pAccess);
			return;
		} catch (RemoveException rmex) {
			reportBeanError(rmex, pAccess);
			return;
		} catch (NamingException nmex) {
			reportBeanError(nmex, pAccess);
			return;
		} catch (IOException ioex) {
			reportBeanError(ioex, pAccess);
			return;
		} finally {
			// in every case close all open Statements
			try {
				if (pst4 != null)
					pst4.close();
				if (pst5 != null)
					pst5.close();
				if (con != null)
					con.close();
			} catch (SQLException sqlex) {
				// cat.errorT("UnpackMDBean: " + sqlex.getMessage());
				cat.logThrowableT(Severity.ERROR, loc, "PackBean: "
						+ "UnpackMDBean: " + sqlex.getMessage(), sqlex);

			}
		}

	}

	private void reportBeanError(Exception ex, PackStatusDBLocal pAccess) {
		cat.logThrowableT(Severity.ERROR, loc,
				"UnpackBean: " + ex.getMessage(), ex);
		pAccess.setMessage(ex.getMessage());
	}

	private ArrayList<ResourceData> getResToUnpack(long colId,
			PreparedStatement pst5) throws SQLException {
		ArrayList<ResourceData> resNames = new ArrayList<ResourceData>(
				INITIAL_LIST_SIZE);

		pst5.setLong(1, colId);
		ResultSet rs5 = pst5.executeQuery();
		while (rs5.next())
			resNames.add(new ResourceData(rs5.getLong("resId"), rs5
					.getString("resName"), rs5.getString("restype"), rs5
					.getString("fingerprint"), rs5.getInt("packLength"), rs5
					.getLong("offset"), rs5.getString("packName")));
		rs5.close();

		return resNames;
	}

	/*
	 * The commit-counter for the DB-Updates of BC_XMLA_POLLING should vary
	 * between 1 and 50 depending of the average size of the resources to
	 * archive. If the resources are 1K or smaller, a commit should occur after
	 * 50 resources are archived. If the resources are 100K or bigger, a commit
	 * should occur after ever archived resource.
	 * 
	 * Thus, we need a function ccount = a*avg_size_of_res + b
	 * 
	 * Doing some mathematics we find out that with the values given above a is
	 * -0.5 and b is 50.5. We have
	 * 
	 * ccount = -(avg_size_of_res)/2 + 50.5
	 * 
	 * This formula will be implemented here.
	 * 
	 */

	private int calculateCommitCounter(ArrayList<ResourceData> resList) {
		long sum = 0;
		int resno = resList.size();
		int ratio;
		int ccount;

		// Calculate the length of all resource to be archived
		for (int i = 0; i < resno; i++)
			sum += resList.get(i).getPackLength();

		// Convert to KByte
		sum = sum / 1024;
		// Calculate avergae size of resources (avg_size_of_res)
		ratio = (int) sum / resno;
		// Calculate ccount
		ccount = (int) (-(ratio / 2) + 50.5);
		// Ensure 1 as lower and 50 as upper limit
		if (ccount < 1)
			ccount = 1;
		if (ccount > 50)
			ccount = 50;

		loc.debugT("UnpackMDB: Processing " + resno + " resources");
		loc.debugT("UnpackMDB: Commit Counter is " + ccount);

		return ccount;
	}

	private void unpackFiles(String coluri, int commitcount,
			ArrayList<ResourceData> reslist, Sapxmla_Config sac,
			PackStatusDBLocal pAccess) throws IOException {
		int responseCode;
		ResourceData res;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		for (int i = 0; i < reslist.size(); i++) {
			res = reslist.get(i);

			// Read a packed resource
			// unpacking is done in XmlDasGet
			XmlDasGetRequest getRequest = new XmlDasGetRequest(sac, bos, coluri
					.concat(res.getPackName()), res.getOffset(), res
					.getPackLength(), null, "DELIVER", "NO", null);
			XmlDasGet get = new XmlDasGet(getRequest);
			XmlDasGetResponse getResponse = get.execute();
			responseCode = getResponse.getStatusCode();

			if (!((responseCode == HttpServletResponse.SC_OK) || (responseCode == HttpServletResponse.SC_PARTIAL_CONTENT)))
				throw new IOException(String.valueOf(responseCode) + ": "
						+ getResponse.getReasonPhrase());

			bos.flush();

			// Write the unpacked resource back
			XmlDasPutRequest putRequest = new XmlDasPutRequest(sac,
					new ByteArrayInputStream(bos.toByteArray()), coluri
							.concat(res.getResName()), res.getResType(),
					"STORE", "NO", null);
			XmlDasPut put = new XmlDasPut(putRequest);
			XmlDasPutResponse putResponse = put.execute();

			responseCode = putResponse.getStatusCode();

			if (responseCode != HttpServletResponse.SC_CREATED)
				throw new IOException(String.valueOf(responseCode) + ": "
						+ putResponse.getReasonPhrase());

			bos.flush();
			bos.reset();

			// update polling table
			if ((i % commitcount) == 0)
				this.updateCount(i, pAccess);

		}
	}

	private void updateCount(int packedres, PackStatusDBLocal pAccess) {
		pAccess.updPackStatus(packedres, new Timestamp(System
				.currentTimeMillis()), "RUNNING");
	}

	private void deleteCount(PackStatusDBLocal pAccess) throws RemoveException {
		pAccess.remove();
	}

	private void setPollMessage(String message, PackStatusDBLocal pAccess) {
		pAccess.setMessage(message);
	}

	private void updateResTable(ArrayList<ResourceData> reslist, String user,
			PreparedStatement pst4) throws SQLException {
		int updCount;
		Timestamp dateTime = new Timestamp(System.currentTimeMillis());
		ResourceData res;

		for (int i = 0; i < reslist.size(); i++) {
			res = reslist.get(i);
			pst4.setNull(1, Types.VARCHAR); // packName
			pst4.setNull(2, Types.INTEGER); // packLength
			pst4.setNull(3, Types.BIGINT); // offset
			pst4.setString(4, user.trim()); // user
			pst4.setTimestamp(5, dateTime, new GregorianCalendar(
					new SimpleTimeZone(0, "UTC")));
			pst4.setLong(6, res.getResId());
			updCount = pst4.executeUpdate();
			if (updCount != 1)
				throw new SQLException("Update of resource table failed");
		}
	}

	private void deleteRes(Sapxmla_Config sac, ArrayList<ResourceData> resList,
			String coluri) throws IOException {
		ArrayList<String> axmlFileNames = new ArrayList<String>();
		ResourceData res;

		// Determine all possible existing AXML files for this collection
		for (int i = 0; i < resList.size(); i++) {
			res = resList.get(i);
			if (!axmlFileNames.contains(res.getPackName()))
				axmlFileNames.add(res.getPackName());
		}

		// And delete all AXML-files
		for (int j = 0; j < axmlFileNames.size(); j++) {
			XmlDasDeleteRequest deleteRequest = new XmlDasDeleteRequest(sac,
					coluri.concat(axmlFileNames.get(j)), "RES");
			XmlDasDelete delete = new XmlDasDelete(deleteRequest);
			delete.execute();
		}
	}
}
