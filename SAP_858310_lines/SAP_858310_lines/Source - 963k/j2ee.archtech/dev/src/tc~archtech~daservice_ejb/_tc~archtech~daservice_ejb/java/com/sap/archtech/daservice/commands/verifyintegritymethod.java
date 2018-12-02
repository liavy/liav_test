package com.sap.archtech.daservice.commands;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.CollectionData;
import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;
import com.sap.archtech.daservice.exceptions.ArchStoreConfigException;
import com.sap.archtech.daservice.exceptions.MissingParameterException;
import com.sap.archtech.daservice.exceptions.NoSuchDBObjectException;
import com.sap.archtech.daservice.exceptions.WrongArgumentException;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

public class VerifyIntegrityMethod extends MasterMethod {

	protected static final Location vloc = Location
			.getLocation("com.sap.archtech.daservice.commands.VerifyIntegrityMethod");
	protected static final Category vcat = Category.getCategory(
			Category.APPS_COMMON_ARCHIVING, "XML_DAS/Verification");
	private final static String GET_COLL = "SELECT * FROM BC_XMLA_COL WHERE uri = ?";

	private java.sql.Connection conn;
	private String apath;
	private String recursive;
	private String stopatend;
	private String user;
	private ArchStoreConfigLocalHome beanLocalHome;
	private QueueConnectionFactory queueConnectionFactory;
	private DirContext queueContext;

	public VerifyIntegrityMethod(HttpServletResponse response, Connection conn,
			String apath, String recursive, String stopatend, String user,
			ArchStoreConfigLocalHome beanLocalHome, DirContext queueContext,
			QueueConnectionFactory queueConnectionFactory) throws IOException {
		this.response = response;
		this.conn = conn;
		this.apath = apath.toLowerCase();
		this.recursive = recursive;
		this.stopatend = stopatend;
		this.user = user;
		this.beanLocalHome = beanLocalHome;
		this.queueContext = queueContext;
		this.queueConnectionFactory = queueConnectionFactory;
	}

	public boolean execute() throws IOException {
		CollectionData coldat;
		PreparedStatement pst1 = null;
		Sapxmla_Config sac = null;
		boolean status = false;

		try {
			pst1 = conn.prepareStatement(GET_COLL);
			coldat = this.checkParams(pst1);
			sac = this.getArchStoreConfigObject(beanLocalHome, coldat
					.getStoreId());

			this.startVerifyMDB(coldat, sac);

			this.response.setHeader("service_message", "Ok");
			status = true;
		} catch (SQLException sqlex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "VERIFY_INTEGRITY: "
					+ sqlex.getMessage(), sqlex);
		} catch (WrongArgumentException waex) {
			this.reportError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
					"VERIFY_INTEGRITY: " + waex.getMessage(), waex);
		} catch (NoSuchDBObjectException nsdbex) {
			this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
					"VERIFY_INTEGRITY: " + nsdbex.getMessage(), nsdbex);
		} catch (MissingParameterException mpex) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"VERIFY_INTEGRITY: " + mpex.getMessage(), mpex);
		} catch (ArchStoreConfigException ascex) {
			this.reportError(DasResponse.SC_CONFIG_INCONSISTENT,
					"VERIFY_INTEGRITY: " + ascex.getMessage(), ascex);
		} catch (NamingException namex) {
			this.reportError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"VERIFY_INTEGRITY: " + namex.getMessage(), namex);
		} catch (JMSException jmsex) {
			this.reportError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"VERIFY_INTEGRITY: " + jmsex.getMessage(), jmsex);
		} finally {
			try {
				if (pst1 != null)
					pst1.close();
			} catch (SQLException sqlex) {
				this.reportError(DasResponse.SC_SQL_ERROR, "VERIFY_INTEGRITY: "
						+ sqlex.getMessage(), sqlex);
				status = false;
			}
		}
		return status;
	}

	private CollectionData checkParams(PreparedStatement pst1)
			throws WrongArgumentException, SQLException,
			NoSuchDBObjectException, MissingParameterException {
		CollectionData coldat = null;

		// check recursive flag
		if (this.recursive == null)
			this.recursive = "N";
		if (!(this.recursive.equalsIgnoreCase("Y") || this.recursive
				.equalsIgnoreCase("N"))) {
			throw new MissingParameterException("Recursive Setting "
					+ this.recursive
					+ " not supported in SELECT; specify Y or N");
		}

		// check user
		if (this.user == null)
			throw new MissingParameterException("User not specified");

		// check stopatend
		if (this.stopatend == null)
			this.stopatend = "Y";
		if (!(this.stopatend.equalsIgnoreCase("Y") || this.stopatend
				.equalsIgnoreCase("N"))) {
			throw new MissingParameterException("Recursive Setting "
					+ this.recursive
					+ " not supported in SELECT; specify Y or N");
		}

		// check collection
		coldat = checkCollection(this.apath, pst1);
		return coldat;
	}

	private void startVerifyMDB(CollectionData coldat, Sapxmla_Config sac)
			throws JMSException, NamingException {
		Queue daqueue = (Queue) queueContext.lookup(MasterMethod.JMSQUEUE);
		loc.debugT("jms: Got Queue DAserviceQueue");
		QueueConnection con = queueConnectionFactory.createQueueConnection();
		loc.debugT("jms: QueueConnection created");
		QueueSession sess = con.createQueueSession(false,
				Session.AUTO_ACKNOWLEDGE);
		loc.debugT("jms: QueueSession created");
		QueueSender sender = sess.createSender(daqueue);
		loc.debugT("jms: Sender created");

		MapMessage msg = sess.createMapMessage();
		msg.setStringProperty("method", "VERIFY");
		msg.setLong("colid", coldat.getcolId());
		msg.setString("apath", this.apath);
		msg.setString("user", this.user);
		msg.setString("recursive", this.recursive);
		msg.setString("stopatend", this.stopatend);
		msg.setString("sac.storeid", Long.toString(sac.store_id));
		msg.setString("sac.archive_store", sac.archive_store);
		msg.setString("sac.storage_system", sac.storage_system);
		msg.setString("sac.type", sac.type);
		msg.setString("sac.win_root", sac.win_root);
		msg.setString("sac.unix_root", sac.unix_root);
		msg.setString("sac.proxy_host", sac.proxy_host);
		msg.setInt("sac.proxy_port", sac.proxy_port);

		loc.debugT("jms: Message created");
		sender.send(msg);
		loc.debugT("jms: Message published");
		con.close();
		loc.debugT("jms: Connection closed");
	}
}
