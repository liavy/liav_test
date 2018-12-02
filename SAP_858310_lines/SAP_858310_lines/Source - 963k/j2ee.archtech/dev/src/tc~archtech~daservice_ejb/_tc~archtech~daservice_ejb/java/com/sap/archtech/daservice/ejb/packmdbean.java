package com.sap.archtech.daservice.ejb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
import com.sap.archtech.daservice.storage.PackResInputStream;
import com.sap.archtech.daservice.storage.XmlDasDelete;
import com.sap.archtech.daservice.storage.XmlDasDeleteRequest;
import com.sap.archtech.daservice.storage.XmlDasPut;
import com.sap.archtech.daservice.storage.XmlDasPutRequest;
import com.sap.archtech.daservice.storage.XmlDasPutResponse;
import com.sap.archtech.daservice.util.PackResInStreamEnumeration;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class PackMDBean implements MessageDrivenBean, MessageListener {

	private static final long serialVersionUID = 1234567890l;

	private final static String UPD_RES = "UPDATE BC_XMLA_RES SET packName = ?, offset = ?, packLength = ?, packTime = ?, packUser = ?, isPacked = 'Y' WHERE resId = ?";
	private final static String GET_RESURI_OF_COLL = "SELECT resId, resName, resType, resLength FROM BC_XMLA_RES WHERE colId = ? and resType = ? and isPacked <>'Y'";

	private final static byte[] HEADERID = { 0x41, 0x58, 0x4D, 0x4C };
	private final static byte COMPRESSION_ALG = 0x08;
	private final static byte ENCRYPTION = 0x00;
	private final static String RELEASE = "01.0";

	private static final Location loc = Location
			.getLocation("com.sap.archtech.daservice");
	private static final Category cat = Category.getCategory(
			Category.APPS_COMMON_ARCHIVING, "XML_DAS");

	// ------------------
	// Instance Variables ------------------------------------------------------
	// ------------------

	private transient MessageDrivenContext mdc = null;
	private long colid;
	private int listsize;

	// --------------
	// Public Methods ----------------------------------------------------------
	// --------------

	public void onMessage(Message inMessage) {
		MapMessage msg = null;
		DataSource ds;
		PreparedStatement pst4 = null;
		PreparedStatement pst5 = null;
		java.sql.Connection con = null;
		ArrayList<ResourceData> reslist;
		long startoffset;
		String axmlfile;
		int commitcount;
		PackStatusDBLocalHome pHome;
		PackStatusDBLocal pAccess = null;
		PackResInStreamEnumeration stenum;

		this.colid = -1;

		try {
			if (inMessage instanceof MapMessage) {
				msg = (MapMessage) inMessage;
				colid = msg.getLong("colid");
				loc.debugT("PACK MESSAGE BEAN: Message received");
				loc.debugT(msg.getString("coluri"));
				loc.debugT(String.valueOf(colid));
				loc.debugT(msg.getString("user"));
			} else {
				cat.errorT(loc, "Message of wrong type: "
						+ inMessage.getClass().getName());
				return;
			}

			loc.debugT("PackMDBean started");

			// Obtain our environment naming context
			Context initCtx = new InitialContext();
			// Look up our data source
			ds = (DataSource) initCtx.lookup("java:comp/env/SAP/BC_XMLA");
			con = ds.getConnection();
			con.setAutoCommit(false);
			// get PackStatusDBHome via EJB-local-ref
			pHome = (PackStatusDBLocalHome) initCtx
					.lookup("java:comp/env/ejb/PackStatusDBBean");
			pAccess = pHome.findByPrimaryKey(Long.valueOf(colid));
			loc.debugT("PackMDBean: Got DB connection");

			pst4 = con.prepareStatement(UPD_RES);
			pst5 = con.prepareStatement(GET_RESURI_OF_COLL);

			reslist = this.getResNamesIds(colid, pst5);
			this.listsize = reslist.size();
			loc.debugT("PackMDBean: Got list of " + this.listsize
					+ " resources to pack");

			commitcount = this.calculateCommitCounter(reslist);

			Sapxmla_Config sac = new Sapxmla_Config(Long.parseLong(msg
					.getString("sac.storeid")), msg
					.getString("sac.archive_store"), msg
					.getString("sac.storage_system"),
					msg.getString("sac.type"), msg.getString("sac.win_root"),
					msg.getString("sac.unix_root"), msg
							.getString("sac.proxy_host"), msg
							.getInt("sac.proxy_port"));

			axmlfile = this.createFileName();
			stenum = new PackResInStreamEnumeration(commitcount, pAccess);
			loc.debugT("PackMDBean: Start packing resources");

			startoffset = this.packResources(msg.getString("coluri"), reslist,
					sac, axmlfile, stenum);

			this.setPollMessage("UPDATING_DB", pAccess);
			this.updateResTable(reslist, startoffset, msg.getString("user"),
					axmlfile, pst4);
			con.commit();

			this.setPollMessage("DELETE_RES", pAccess);
			this.deleteRes(sac, reslist, msg.getString("coluri"));

			this.deleteCount(pAccess);

			loc.debugT("PackMDBean: finished");
		} catch (JMSException jmsex) {
			reportBeanError(jmsex, pAccess);
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
			try {
				if (pst4 != null)
					pst4.close();
				if (pst5 != null)
					pst5.close();
				if (con != null)
					con.close();
			} catch (SQLException sqlex) {
				cat.errorT(loc, "PackMDBean: " + sqlex.getMessage());
			}
		}
	}

	public void ejbCreate() {
		loc.debugT("In PackMDBBean.ejbCreate()");
	}

	public void ejbRemove() {
		loc.debugT("In PackMDBBean.ejbRemove()");
	}

	public void setMessageDrivenContext(MessageDrivenContext context) {
		loc.debugT("In PackMDBBean.setMessageDrivenContext()");
		this.mdc = context;
	}

	// ---------------
	// Private Methods
	// ----------------------------------------------------------
	// ---------------

	private ArrayList<ResourceData> getResNamesIds(long colId, PreparedStatement pst2)
			throws SQLException {
		/*
		 * We need to sort the entries in the following order: XSD, XSL, XML,
		 * BIN. Other resource types are not accepted.
		 */
		ArrayList<ResourceData> resNames = new ArrayList<ResourceData>(500);

		pst2.setLong(1, colId);
		pst2.setString(2, "XSD");
		ResultSet xsdRes = pst2.executeQuery();
		while (xsdRes.next())
			resNames.add(new ResourceData(xsdRes.getLong("resId"), xsdRes
					.getString("resName"), xsdRes.getString("resType"), xsdRes
					.getLong("reslength")));
		xsdRes.close();

		pst2.setString(2, "XSL");
		ResultSet xslRes = pst2.executeQuery();
		while (xslRes.next())
			resNames.add(new ResourceData(xslRes.getLong("resId"), xslRes
					.getString("resName"), xslRes.getString("resType"), xslRes
					.getLong("reslength")));
		xslRes.close();

		pst2.setString(2, "XML");
		ResultSet xmlRes = pst2.executeQuery();
		while (xmlRes.next())
			resNames.add(new ResourceData(xmlRes.getLong("resId"), xmlRes
					.getString("resName"), xmlRes.getString("resType"), xmlRes
					.getLong("reslength")));
		xmlRes.close();

		pst2.setString(2, "BIN");
		ResultSet binRes = pst2.executeQuery();
		while (binRes.next())
			resNames.add(new ResourceData(binRes.getLong("resId"), binRes
					.getString("resName"), binRes.getString("resType"), binRes
					.getLong("reslength")));
		binRes.close();

		return resNames;
	}

	/**
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
			sum += ((ResourceData) resList.get(i)).getLength();

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

		loc.debugT("PackMDB: Processing " + resno + " resources");
		loc.debugT("PackMDB: Commit Counter is " + ccount);

		return ccount;
	}

	private long packResources(String coluri, ArrayList<ResourceData> reslist,
			Sapxmla_Config sac, String axmlFile,
			PackResInStreamEnumeration stenum) throws IOException {

		ByteArrayOutputStream doc_headerout;
		ByteArrayInputStream doc_headerin;
		SequenceInputStream ppack;
		ResourceData res;
		int responseCode;
		long offset = 0;

		// Prepare: add 0-byte at the end
		ByteArrayOutputStream bosZIPdummy;
		ByteArrayInputStream binZIPdummy;

		// Create AXML file-header (once)
		doc_headerout = new ByteArrayOutputStream();
		offset += this.writeFileHeader(doc_headerout, coluri + axmlFile);
		doc_headerin = new ByteArrayInputStream(doc_headerout.toByteArray());
		stenum.addElement(doc_headerin);

		// Add packed resource
		for (int i = 0; i < reslist.size(); i++) {
			res = (ResourceData) reslist.get(i);
			stenum.addElement(new PackResInputStream(coluri, res, sac));

			// Add a 0 byte add the end of each packed resource. This is needed
			// for the ZLIB native library. See javadoc for Inflater.
			bosZIPdummy = new ByteArrayOutputStream(1);
			bosZIPdummy.write(0x00);
			binZIPdummy = new ByteArrayInputStream(bosZIPdummy.toByteArray());

			stenum.addElement(binZIPdummy);
		}
		ppack = new SequenceInputStream(stenum);

		XmlDasPutRequest putRequest = new XmlDasPutRequest(sac, ppack, coluri
				+ axmlFile, "BIN", "STORE", "NO", null);
		XmlDasPut put = new XmlDasPut(putRequest);
		XmlDasPutResponse putResponse = put.execute();

		responseCode = putResponse.getStatusCode();

		if (responseCode != HttpServletResponse.SC_CREATED) {
			cat.errorT(loc, "PackMDBean: PUT returned statuscode "
					+ responseCode + " and message "
					+ putResponse.getReasonPhrase());
			throw new IOException("PACK: " + putResponse.getReasonPhrase());
		}

		return offset;
	}

	private String createFileName() {
		// the long and difficult way

		java.util.Date currDate = new java.util.Date(System.currentTimeMillis());
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(currDate);
		SimpleDateFormat fmt = new SimpleDateFormat();
		fmt.applyPattern("yyyyMMdd");
		String currDateString = fmt.format(cal.getTime());
		GregorianCalendar gregCal = new GregorianCalendar();
		gregCal.setTime(currDate);
		SimpleDateFormat form = new SimpleDateFormat();
		form.applyPattern("HHmmssSSS");
		String currTimeString = form.format(gregCal.getTime());

		return (currDateString + "_" + currTimeString + ".axml");
	}

	/**
	 * Description of the Method
	 * 
	 * @param bos
	 *            Description of the Parameter
	 * @param axmlURI
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 * @exception IOException
	 *                Description of the Exception
	 */
	private int writeFileHeader(ByteArrayOutputStream bos, String axmlURI)
			throws IOException {

		bos.write(HEADERID);
		bos.write(COMPRESSION_ALG);
		bos.write(ENCRYPTION);
		bos.write(RELEASE.getBytes("UTF-8"));
		int urilength = axmlURI.length();
		bos.write(toFourBytes(urilength));
		bos.write(axmlURI.getBytes("UTF-8"));

		return HEADERID.length + 1 + 1 + RELEASE.length() + 4 + urilength;
	}

	/**
	 * Description of the Method
	 * 
	 * @param n
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	private byte[] toFourBytes(int n) {
		byte[] b = new byte[4];
		b[3] = (byte) (n);
		n >>>= 8;
		b[2] = (byte) (n);
		n >>>= 8;
		b[1] = (byte) (n);
		n >>>= 8;
		b[0] = (byte) (n);

		return b;
	}

	private void deleteRes(Sapxmla_Config sac, ArrayList<ResourceData> resList, String coluri)
			throws IOException {
		for (int j = 0; j < resList.size(); j++) {
			XmlDasDeleteRequest deleteRequest = new XmlDasDeleteRequest(
					sac,
					coluri.concat(((ResourceData) resList.get(j)).getResName()),
					"RES");
			XmlDasDelete delete = new XmlDasDelete(deleteRequest);
			delete.execute();
		}
	}

	private void deleteCount(PackStatusDBLocal pAccess) throws RemoveException {
		pAccess.remove();
	}

	private void setPollMessage(String message, PackStatusDBLocal pAccess) {
		// Update database
		pAccess.setMessage(message);
	}

	private void updateResTable(ArrayList<ResourceData> reslist, long totaloffset,
			String user, String axmlFile, PreparedStatement pst4)
			throws SQLException {
		Timestamp dateTime;
		ResourceData res;
		int updcount;

		dateTime = new Timestamp(System.currentTimeMillis());
		for (int i = 0; i < reslist.size(); i++) {
			res = (ResourceData) reslist.get(i);

			// Add 1 to the resource length because of the 0 byte
			// for the ZIP algorithm
			res.setLength(res.getLength() + 1);

			totaloffset += res.getOffset();
			pst4.setString(1, axmlFile);
			pst4.setLong(2, totaloffset);
			pst4.setInt(3, (int) res.getLength());
			pst4.setTimestamp(4, dateTime, new GregorianCalendar(
					new SimpleTimeZone(0, "UTC")));
			pst4.setString(5, user);
			pst4.setLong(6, res.getResId());
			updcount = pst4.executeUpdate();
			if (updcount != 1)
				throw new SQLException(
						"PackBean: Update of resource table failed");
			totaloffset += res.getLength();
		}
	}

	private void reportBeanError(Exception ex, PackStatusDBLocal pAccess) {
		cat.logThrowableT(Severity.ERROR, loc, "PackBean: " + ex.getMessage(),
				ex);
		pAccess.setMessage(ex.getMessage());
	}
}
