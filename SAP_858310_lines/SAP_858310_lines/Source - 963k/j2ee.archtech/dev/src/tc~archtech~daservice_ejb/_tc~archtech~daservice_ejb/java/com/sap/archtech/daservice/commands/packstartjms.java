package com.sap.archtech.daservice.commands;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.StringTokenizer;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.CollectionData;
import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;
import com.sap.archtech.daservice.ejb.PackStatusDBLocalHome;
import com.sap.archtech.daservice.exceptions.ArchStoreConfigException;
import com.sap.archtech.daservice.exceptions.MissingParameterException;
import com.sap.archtech.daservice.exceptions.NoSuchDBObjectException;
import com.sap.archtech.daservice.exceptions.WrongArgumentException;

public class PackStartJMS extends MasterMethod {

	private final static String SEL_COL_TAB = "SELECT colId, storeId FROM BC_XMLA_COL WHERE uri = ?";
	private final static String SEL_RES_TAB = "SELECT RESID FROM BC_XMLA_RES WHERE COLID = ? AND RESNAME LIKE '%.adk'";
	private final static String COUNT_PACKRES = "SELECT COUNT(*) FROM BC_XMLA_RES WHERE colId = ? and isPacked <>'Y'";

	private java.sql.Connection connection;
	private QueueConnectionFactory queueConnectionFactory;
	private DirContext queueContext;
	private ArchStoreConfigLocalHome beanLocalHome;
	private String coll;
	private String user;
	private Timestamp ctime;

	public PackStartJMS(java.sql.Connection con, DirContext queueContext,
			QueueConnectionFactory topicConnectionFactory,
			HttpServletResponse response,
			ArchStoreConfigLocalHome beanLocalHome, String coll, String user,
			Timestamp ctime) {
		this.connection = con;
		this.queueContext = queueContext;
		this.queueConnectionFactory = topicConnectionFactory;
		this.response = response;
		this.beanLocalHome = beanLocalHome;
		this.coll = coll;
		this.user = user;
		this.ctime = ctime;
	}

	public boolean execute() throws IOException {
		PreparedStatement pst0 = null;
		PreparedStatement pst1 = null;
		PreparedStatement pst2 = null;
		CollectionData coldat;
		int packRes;

		response.setContentType("text/xml");

		try {
			pst0 = this.connection.prepareStatement(SEL_COL_TAB);
			pst1 = this.connection.prepareStatement(SEL_RES_TAB);
			pst1.setMaxRows(1);
			pst2 = this.connection.prepareStatement(COUNT_PACKRES);

			coldat = checkCollection(coll, pst0);

			// Check If Collection Is Under ILM Control
			StringTokenizer st = new StringTokenizer(coll, "/");
			String secondCol = "";
			String thirdCol = "";
			if (st.hasMoreTokens()) {
				st.nextToken();
				if (st.hasMoreTokens()) {
					secondCol = st.nextToken();
					if (st.hasMoreTokens())
						thirdCol = st.nextToken();
				}
			}
			if (secondCol.equals("ad") || thirdCol.equals("ad")
					|| thirdCol.equals("sn") || thirdCol.equals("al")
					|| thirdCol.equals("dl")) {
				this
						.reportError(HttpServletResponse.SC_FORBIDDEN,
								"PACK: Packing collections under ILM control is not allowed");
				return false;
			}

			// Check If Collection Contains At Least One ADK Resource
			if (this.isAdkResoureInCollection(coldat.getcolId(), pst1)) {
				this
						.reportError(HttpServletResponse.SC_FORBIDDEN,
								"PACK: Packing collections containing ADK resources is not allowed");
				return false;
			}

			packRes = this.countRes(coldat.getcolId(), pst2);

			// only if we found unpacked resources something is to do
			if (packRes > 0) {
				Sapxmla_Config sac = this.getArchStoreConfigObject(
						beanLocalHome, coldat.getStoreId());
				this.insPollEntry(coldat.getcolId(), packRes);

				// call PackMDBean
				this.startPackMDB(coldat, sac);
			}

		} catch (ArchStoreConfigException ascex) {
			this.reportError(DasResponse.SC_CONFIG_INCONSISTENT, "PACK: "
					+ ascex.getMessage(), ascex);
			return false;
		} catch (WrongArgumentException waex) {
			this.reportError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
					"PACK: " + waex.getMessage(), waex);
			return false;
		} catch (JMSException jmsex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "PACK: "
					+ jmsex.getMessage(), jmsex);
			return false;
		} catch (NamingException nmex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "PACK: "
					+ nmex.getMessage(), nmex);
			return false;
		} catch (SQLException sqlex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "PACK: "
					+ sqlex.getMessage(), sqlex);
			return false;
		} catch (CreateException crex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "PACK: "
					+ crex.getMessage(), crex);
			return false;
		} catch (NoSuchDBObjectException nsdbex) {
			this.reportError(DasResponse.SC_DOES_NOT_EXISTS, "PACK: "
					+ nsdbex.getMessage(), nsdbex);
			return false;
		} catch (MissingParameterException msex) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING, "PACK: "
					+ msex.getMessage(), msex);
			return false;
		} catch (EJBException ejbex) {
			this
					.reportError(
							DasResponse.SC_SQL_ERROR,
							"PACK: Failed PACK operation can only be restarted after cleanup of table BC_XMLA_POLLING",
							ejbex);
			return false;
		}
		this.response.setHeader("service_message", "Ok");
		return true;
	}

	private void startPackMDB(CollectionData coldat, Sapxmla_Config sac)
			throws JMSException, NamingException {
		Queue daqueue = (Queue) queueContext.lookup(MasterMethod.JMSQUEUE);
		loc.infoT("jms: Got Queue DAserviceQueue");
		QueueConnection con = queueConnectionFactory.createQueueConnection();
		loc.infoT("jms: QueueConnection created");
		QueueSession sess = con.createQueueSession(false,
				Session.AUTO_ACKNOWLEDGE);
		loc.infoT("jms: QueueSession created");
		QueueSender sender = sess.createSender(daqueue);
		loc.infoT("jms: Sender created");

		MapMessage msg = sess.createMapMessage();
		msg.setStringProperty("method", "PACK");
		msg.setLong("colid", coldat.getcolId());
		msg.setString("coluri", this.coll);
		msg.setString("user", this.user);
		msg.setString("sac.storeid", Long.toString(sac.store_id));
		msg.setString("sac.archive_store", sac.archive_store);
		msg.setString("sac.storage_system", sac.storage_system);
		msg.setString("sac.type", sac.type);
		msg.setString("sac.win_root", sac.win_root);
		msg.setString("sac.unix_root", sac.unix_root);
		msg.setString("sac.proxy_host", sac.proxy_host);
		msg.setInt("sac.proxy_port", sac.proxy_port);

		loc.infoT("jms: Message created");
		sender.send(msg);
		cat.infoT(loc, "PACK: JMS message successfully published");
		con.close();
		loc.infoT("jms: Connection closed");
	}

	private boolean isAdkResoureInCollection(long colId, PreparedStatement pst1)
			throws SQLException {
		int hits = 0;
		pst1.setLong(1, colId);
		ResultSet pst1Res = pst1.executeQuery();
		while (pst1Res.next()) {
			hits++;
		}
		pst1Res.close();
		if (hits == 0)
			return false;
		else
			return true;
	}

	private int countRes(long colId, PreparedStatement pst2)
			throws SQLException {

		int counter = 0;

		pst2.setLong(1, colId);
		ResultSet pst2Res = pst2.executeQuery();
		pst2Res.next();
		counter = pst2Res.getInt(1);
		pst2Res.close();

		return counter;
	}

	private void insPollEntry(long colId, int resno) throws NamingException,
			CreateException, EJBException {
		PackStatusDBLocalHome pHome;
		Context ctx = new InitialContext();
		pHome = (PackStatusDBLocalHome) ctx
				.lookup("java:comp/env/PackStatusDBBean");
		pHome.createMethod(colId, ctime, ctime, resno, 0, "STARTING");
	}
}