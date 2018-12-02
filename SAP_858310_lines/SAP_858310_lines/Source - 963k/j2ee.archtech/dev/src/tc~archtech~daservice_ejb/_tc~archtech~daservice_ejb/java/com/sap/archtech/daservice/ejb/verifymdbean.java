package com.sap.archtech.daservice.ejb;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.sap.archtech.daservice.data.CollectionData;
import com.sap.archtech.daservice.data.ResourceData;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.storage.XmlDasGet;
import com.sap.archtech.daservice.storage.XmlDasGetRequest;
import com.sap.archtech.daservice.storage.XmlDasGetResponse;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Message Driven Bean which realizes the VERIFY INTEGRITY procedure. All
 * resources of a collection(and of all sub collections if recursive is set) are
 * read silently. Checksums are calculated again and compared to the existing
 * ones. Results are logged to special log category
 * /Applications/Common/Archiving/XML_DAS/Verification.
 */
public class VerifyMDBean implements MessageDrivenBean, MessageListener {
	
	private static final long serialVersionUID = 1234567890l;
	private static final Location loc = Location
			.getLocation("com.sap.archtech.daservice"); //$NON-NLS-1$
	private static final Category cat = Category.getCategory(
			Category.APPS_COMMON_ARCHIVING, "XML_DAS"); //$NON-NLS-1$
	private static final Location vloc = Location
			.getLocation("com.sap.archtech.daservice.commands.VerifyIntegrityMethod"); //$NON-NLS-1$
	private static final Category vcat = Category.getCategory(
			Category.APPS_COMMON_ARCHIVING, "XML_DAS/Verification"); //$NON-NLS-1$
	private final static String GET_ALL_RES = "SELECT * FROM BC_XMLA_RES WHERE colId = ?"; //$NON-NLS-1$
	private final static String GET_CHILD_COLLS = "SELECT colId, uri, creationUser, creationTime, colType, frozen FROM BC_XMLA_COL WHERE parentColId = ?"; //$NON-NLS-1$

	// For translation
	private final static String YESSTRING = VerifyIntegrityMessages
			.getString("Verify.Yes"); //$NON-NLS-1$
	private final static String NOSTRING = VerifyIntegrityMessages
			.getString("Verify.No"); //$NON-NLS-1$

	private transient MessageDrivenContext mdc = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.ejb.MessageDrivenBean#ejbRemove()
	 */
	public void ejbRemove() throws EJBException {
		loc.debugT("In VerifyMDBean.ejbRemove()"); //$NON-NLS-1$
	}

	public void ejbCreate() {
		loc.debugT("In VerifyMDBean.ejbCreate()"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.ejb.MessageDrivenBean#setMessageDrivenContext(javax.ejb.MessageDrivenContext)
	 */
	public void setMessageDrivenContext(MessageDrivenContext context)
			throws EJBException {
		loc.debugT("In VerifyMDBean.setMessageDrivenContext()"); //$NON-NLS-1$
		this.mdc = context;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message inMessage) {
		MapMessage msg = null;
		long colid;
		ResourceData resdat;
		PreparedStatement pst2 = null;
		PreparedStatement pst3 = null;
		boolean checkresult;
		long errorCounter = 0;
		long resCounter = 0;
		java.sql.Connection conn = null;
		DataSource ds;
		HashMap<Long, CollectionData> collMap = null;
		CollectionData coldat;
		Map.Entry<Long, CollectionData> mapEntry;

		try {
			if (inMessage instanceof MapMessage) {
				msg = (MapMessage) inMessage;
				loc.debugT("VERIFY MESSAGE BEAN: Message received"); //$NON-NLS-1$
				loc.debugT(msg.getString("apath")); //$NON-NLS-1$
				loc.debugT(String.valueOf(msg.getLong("colid"))); //$NON-NLS-1$
				loc.debugT(msg.getString("user")); //$NON-NLS-1$
				loc.debugT(msg.getString("recursive")); //$NON-NLS-1$
				loc.debugT(msg.getString("stopatend")); //$NON-NLS-1$
			} else {
				cat
						.errorT(
								loc,
								"Message of wrong type: " + inMessage.getClass().getName()); //$NON-NLS-1$
				return;
			}

			Sapxmla_Config sac = new Sapxmla_Config(Long.parseLong(msg
					.getString("sac.storeid")), //$NON-NLS-1$
					msg.getString("sac.archive_store"), //$NON-NLS-1$
					msg.getString("sac.storage_system"), //$NON-NLS-1$
					msg.getString("sac.type"), //$NON-NLS-1$
					msg.getString("sac.win_root"), //$NON-NLS-1$
					msg.getString("sac.unix_root"), //$NON-NLS-1$
					msg.getString("sac.proxy_host"), //$NON-NLS-1$
					msg.getInt("sac.proxy_port")); //$NON-NLS-1$

			// Obtain our environment naming context
			Context initCtx = new InitialContext();
			// Look up our data source
			ds = (DataSource) initCtx.lookup("java:comp/env/SAP/BC_XMLA"); //$NON-NLS-1$
			conn = ds.getConnection();
			conn.setAutoCommit(false);
			pst2 = conn.prepareStatement(GET_ALL_RES);
			pst3 = conn.prepareStatement(GET_CHILD_COLLS);

			// in the recursive case, collect all child collections in an
			// HashMap
			// in the non recursive case, create an empty HashMap
			if ("Y".equals(msg.getString("recursive"))) //$NON-NLS-1$ //$NON-NLS-2$
				collMap = getAllChildColls(msg.getLong("colid"), pst3); //$NON-NLS-1$
			else
				collMap = new HashMap<Long, CollectionData>();
			// add the start collection
			collMap.put(Long.valueOf(msg.getLong("colid")), new CollectionData(msg.getLong("colid"), msg.getString("apath"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			logstart(msg);
			// for all collections
			for (Iterator<Map.Entry<Long, CollectionData>> iter = collMap
					.entrySet().iterator(); iter.hasNext();) {
				mapEntry = iter.next();
				coldat = mapEntry.getValue();
				colid = mapEntry.getKey().longValue();
				Object colParam[] = { coldat.getColURI() };
				vcat.infoT(vloc, MessageFormat.format(VerifyIntegrityMessages
						.getString("Verify.Verify_coll"), colParam)); //$NON-NLS-1$
				pst2.setLong(1, colid);
				ResultSet rs2 = pst2.executeQuery();
				while (rs2.next()) {
					resdat = new ResourceData(rs2.getLong("RESID"), //$NON-NLS-1$
							rs2.getString("RESNAME"), //$NON-NLS-1$
							rs2.getString("FINGERPRINT"), //$NON-NLS-1$
							rs2.getInt("PACKLENGTH"), //$NON-NLS-1$
							rs2.getLong("OFFSET"), //$NON-NLS-1$
							rs2.getString("PACKNAME"), //$NON-NLS-1$
							rs2.getString("ISPACKED")); //$NON-NLS-1$
					checkresult = checkResource(resdat, coldat.getColURI(), sac);
					if (!checkresult)
						errorCounter++;
					if (!checkresult
							&& "Y".equalsIgnoreCase(msg.getString("stopatend"))) //$NON-NLS-1$ //$NON-NLS-2$
					{
						vcat.infoT(vloc, VerifyIntegrityMessages
								.getString("Verify.Stop_at_first")); //$NON-NLS-1$
						break;
					}
					resCounter++;
				}
			}
			logend(resCounter, errorCounter);
		} catch (JMSException jmsex) {
			cat.logThrowableT(Severity.ERROR, loc,
					"VerifyMDBean: " + jmsex.getMessage(), jmsex); //$NON-NLS-1$
			throw new EJBException(jmsex.getMessage());
		} catch (NamingException namex) {
			cat.logThrowableT(Severity.ERROR, loc,
					"VerifyMDBean: " + namex.getMessage(), namex); //$NON-NLS-1$
			throw new EJBException(namex.getMessage());
		} catch (SQLException sqlex) {
			cat.logThrowableT(Severity.ERROR, loc,
					"VerifyMDBean: " + sqlex.getMessage(), sqlex); //$NON-NLS-1$
			throw new EJBException(sqlex.getMessage());
		} catch (IOException ioex) {
			cat.logThrowableT(Severity.ERROR, loc,
					"VerifyMDBean: " + ioex.getMessage(), ioex); //$NON-NLS-1$
			throw new EJBException(ioex.getMessage());
		} finally {
			try {
				if (pst2 != null)
					pst2.close();
				if (pst3 != null)
					pst3.close();
				if (conn != null) {
					conn.commit();
					conn.close();
				}
			} catch (SQLException sqlex) {
				cat.logThrowableT(Severity.ERROR, loc,
						"VerifyMDBean: " + sqlex.getMessage(), sqlex); //$NON-NLS-1$
			}
		}
	} // ---------------

	private void logstart(MapMessage msg) throws JMSException {
		Object inputParams[] = {
				msg.getString("user"), //$NON-NLS-1$
				msg.getString("apath"), //$NON-NLS-1$
				"Y".equalsIgnoreCase(msg.getString("recursive")) ? YESSTRING : NOSTRING, //$NON-NLS-1$ //$NON-NLS-2$
				"Y".equalsIgnoreCase(msg.getString("stopatend")) ? YESSTRING : NOSTRING }; //$NON-NLS-1$ //$NON-NLS-2$

		vcat.infoT(vloc, MessageFormat.format(VerifyIntegrityMessages
				.getString("Verify.VERIFY_started"), inputParams)); //$NON-NLS-1$
		vcat.infoT(vloc, MessageFormat.format(VerifyIntegrityMessages
				.getString("Verify.Param_archive"), inputParams)); //$NON-NLS-1$
		vcat.infoT(vloc, MessageFormat.format(VerifyIntegrityMessages
				.getString("Verify.Param_recursive"), inputParams)); //$NON-NLS-1$
		vcat.infoT(vloc, MessageFormat.format(VerifyIntegrityMessages
				.getString("Verify.Param_stop"), inputParams)); //$NON-NLS-1$
		vcat.infoT(vloc, "-------------------------------------"); //$NON-NLS-1$
	}

	private void logend(long resCounter, long errorCounter) {
		Object docCounter[] = { Long.valueOf(resCounter), Long.valueOf(errorCounter) };

		vcat.infoT(vloc, VerifyIntegrityMessages
				.getString("Verify.VERIFY_finished")); //$NON-NLS-1$
		vcat.infoT(vloc, MessageFormat.format(VerifyIntegrityMessages
				.getString("Verify.documents_verified"), docCounter)); //$NON-NLS-1$
		vcat.infoT(vloc, MessageFormat.format(VerifyIntegrityMessages
				.getString("Verify.documents_with_problems"), docCounter)); //$NON-NLS-1$
		vcat.infoT(vloc, "--------------------------------"); //$NON-NLS-1$
	}

	private boolean checkResource(ResourceData resdat, String uri,
			Sapxmla_Config sac) throws IOException {

		int readsuccess;
		XmlDasGetResponse getResponse;
		XmlDasGet get;
		if (!uri.endsWith("/")) //$NON-NLS-1$
			uri = uri.concat("/"); //$NON-NLS-1$
		if (resdat.getIsPacked().equalsIgnoreCase("Y")) //$NON-NLS-1$
		{
			XmlDasGetRequest getRequest = new XmlDasGetRequest(sac, null, uri
					+ resdat.getPackName().trim(), resdat.getOffset(), resdat
					.getPackLength(), resdat.getFpdb(), "NODELIVER", //$NON-NLS-1$
					"NO", //$NON-NLS-1$
					null);
			get = new XmlDasGet(getRequest);
			getResponse = get.execute();
			readsuccess = getResponse.getStatusCode();
		} else {
			XmlDasGetRequest getRequest = new XmlDasGetRequest(sac, null, uri
					+ resdat.getResName().trim(), 0, 0, resdat.getFpdb(),
					"NODELIVER", "NO", null); //$NON-NLS-1$ //$NON-NLS-2$
			get = new XmlDasGet(getRequest);
			getResponse = get.execute();
			readsuccess = getResponse.getStatusCode();
		}
		// Error handling - check two cases:
		// 1. all "general" errors (resource does not exist, is not readable
		// etc.)
		// 2. Checksum incorrect
		// Must be checked in this order. Incorrect checksum is not delivered
		// back as a status code.
		// If a resource does not exists,
		// XmlDasGetResponse.isCheckSumIdentical() is set to false.
		Object errorParams[] = { resdat.getResName(), Integer.valueOf(readsuccess),
				getResponse.getReasonPhrase() };
		if (!(readsuccess == HttpServletResponse.SC_OK || readsuccess == HttpServletResponse.SC_PARTIAL_CONTENT)) {
			vcat.infoT(vloc, MessageFormat.format(VerifyIntegrityMessages
					.getString("Verify.Error_reading"), errorParams)); //$NON-NLS-1$
			if (getResponse.getReasonPhrase() != null
					&& getResponse.getException() != null)
				cat.logThrowableT(Severity.ERROR, loc, getResponse
						.getReasonPhrase(), getResponse.getException());
			return false;
		}

		if (!getResponse.isChecksumIdentical()) {
			vcat.infoT(vloc, MessageFormat.format(VerifyIntegrityMessages
					.getString("Verify.Incorrect_Checksum"), errorParams)); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	private HashMap<Long, CollectionData> getAllChildColls(long collId,
			PreparedStatement pst5) throws SQLException {
		HashMap<Long, CollectionData> inter = new HashMap<Long, CollectionData>();
		ResultSet res5;
		pst5.setLong(1, collId);
		res5 = pst5.executeQuery();
		while (res5.next())
			inter.put(Long.valueOf(res5.getLong("colId")), //$NON-NLS-1$
					new CollectionData(res5.getLong("colId"), //$NON-NLS-1$
							res5.getString("URI"), //$NON-NLS-1$
							res5.getString("creationUser"), //$NON-NLS-1$
							res5.getTimestamp("creationTime"), //$NON-NLS-1$
							res5.getString("colType"), //$NON-NLS-1$
							res5.getString("frozen"))); //$NON-NLS-1$
		res5.close();
		if (inter.isEmpty())
			return inter;
		else {
			// build an ArrayList with all key Values
			ArrayList<Long> atemp = new ArrayList<Long>();
			for (Iterator<Long> i = inter.keySet().iterator(); i.hasNext();)
				atemp.add(i.next());
			// iterate over the ArrayList and call this method recursively for
			// each element
			for (int j = 0; j < atemp.size(); j++)
				inter.putAll(this.getAllChildColls(atemp.get(j).longValue(),
						pst5));
		}
		return inter;
	}
}
