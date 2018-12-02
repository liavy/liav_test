package com.sap.archtech.daservice.commands;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.ejb.CreateException;
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

public class UnpackStartJMS extends MasterMethod {

	private final static String SEL_COL_TAB = "SELECT colId, storeId FROM BC_XMLA_COL WHERE uri = ?";
	private final static String GET_RES_OF_COLL = "SELECT count(*) FROM BC_XMLA_RES WHERE colId = ? and isPacked = 'Y'";

	private java.sql.Connection connection;
	private QueueConnectionFactory queueConnectionFactory;
	private DirContext queueContext;
	private ArchStoreConfigLocalHome beanLocalHome;
	private String coll;
	private String user;
	private Timestamp ctime;

	public UnpackStartJMS(java.sql.Connection connection,
			DirContext queueContext,
			QueueConnectionFactory topicConnectionFactory,
			HttpServletResponse response,
			ArchStoreConfigLocalHome beanLocalHome, String coll, String user,
			Timestamp ctime) {
		this.connection = connection;
		this.queueContext = queueContext;
		this.queueConnectionFactory = topicConnectionFactory;
		this.response = response;
		this.beanLocalHome = beanLocalHome;
		this.coll = coll;
		this.user = user;
		this.ctime = ctime;
	}

	public boolean execute() throws IOException {
		CollectionData startColl;
		PreparedStatement pst1 = null, pst2 = null;
		int unpackRes;

		response.setContentType("text/xml");

		try {

			pst1 = this.connection.prepareStatement(SEL_COL_TAB);
			pst2 = this.connection.prepareStatement(GET_RES_OF_COLL);

			// check if collection exists
			startColl = checkCollection(coll, pst1);
			// determine number of resources to unpack
			unpackRes = this.getResNamesIds(startColl.getcolId(), pst2);
			// only if we found packed resources something is to do
			if (unpackRes > 0) {
				Sapxmla_Config sac = this.getArchStoreConfigObject(
						beanLocalHome, startColl.getStoreId());

				this.insPollEntry(startColl.getcolId(), unpackRes);

				// call PackMDBean
				this.startUnpackMDB(startColl, sac);
			}
		} catch (ArchStoreConfigException ascex) {
			this.reportError(DasResponse.SC_CONFIG_INCONSISTENT, "UNPACK: "
					+ ascex.getMessage(), ascex);
			return false;
		} catch (SQLException sqlex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "UNPACK: "
					+ sqlex.getMessage(), sqlex);
			return false;
		} catch (WrongArgumentException waex) {
			this.reportError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
					"UNPACK: " + waex.getMessage(), waex);
			return false;
		} catch (CreateException crex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "UNPACK: "
					+ crex.getMessage(), crex);
			return false;
		} catch (NoSuchDBObjectException nsdbex) {
			this.reportError(DasResponse.SC_DOES_NOT_EXISTS, "UNPACK: "
					+ nsdbex.getMessage(), nsdbex);
			return false;
		} catch (MissingParameterException msex) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING, "UNPACK: "
					+ msex.getMessage(), msex);
			return false;
		} catch (JMSException jmsex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "UNPACK: "
					+ jmsex.getMessage(), jmsex);
			return false;
		} catch (NamingException nmex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "UNPACK: "
					+ nmex.getMessage(), nmex);
			return false;
		}
		this.response.setHeader("service_message", "Ok");
		return true;
	}

	private void startUnpackMDB(CollectionData coldat, Sapxmla_Config sac)
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
		msg.setStringProperty("method", "UNPACK");
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
		cat.infoT(loc, "UNPACK: JMS message successfully published");
		con.close();
		loc.infoT("jms: Connection closed");
	}

	private int getResNamesIds(long colId, PreparedStatement pst2)
			throws SQLException {
		int counter;

		pst2.setLong(1, colId);
		ResultSet pst2Res = pst2.executeQuery();
		pst2Res.next();
		counter = pst2Res.getInt(1);

		pst2Res.close();
		return counter;
	}

	protected void insPollEntry(long colId, int resno) throws NamingException,
			CreateException {
		PackStatusDBLocalHome pHome;
		Context ctx = new InitialContext();
		pHome = (PackStatusDBLocalHome) ctx
				.lookup("java:comp/env/PackStatusDBBean");
		pHome.createMethod(colId, ctime, ctime, resno, 0, "STARTING");

	}
}