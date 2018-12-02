package com.sap.archtech.daservice.util;

import com.sap.archtech.daservice.data.*;
import com.sap.archtech.daservice.ejb.*;
import com.sap.security.core.server.destinations.api.*;
import com.sap.tc.logging.*;
import com.tssap.dtr.client.lib.protocol.*;
import com.tssap.dtr.client.lib.protocol.pool.*;
import com.tssap.dtr.client.lib.protocol.session.*;
import com.tssap.dtr.client.lib.protocol.templates.*;
import java.util.*;
import javax.naming.*;

public class WebDavTemplateProvider {

	private static final Location loc = Location
			.getLocation("com.sap.archtech.daservice");
	private static final Category cat = Category.getCategory(
			Category.APPS_COMMON_ARCHIVING, "XML_DAS");
	public static final int WEBDAVACQUIRECONNECTIONWAITLOOPS = 100;
	public static final int WEBDAVACQUIRECONNECTIONWAITTIME = 100;
	public static final String WEBDAVCLIENTAUTHSCHEME = "Basic";
	public static final String WEBDAVUSERAGENT = "SAP XML DAS";

	public static String WEBDAVCLIENTPWD;
	public static String WEBDAVCLIENTUSR;
	public static ConnectionPool webDAVConnectionPool;
	public static int WEBDAVCONNECTIONTEMPLATELIMIT;
	public static int WEBDAVCONNECTTIMEOUT;
	public static int WEBDAVEXPIRATIONTIMEOUT;
	public static int WEBDAVREADTIMEOUT;
	public static SynchronizedConnectionPool webDAVSyncConnectionPool;
	public static Hashtable<Long, Integer> webDAVTemplateHashTable;
	public static ITemplateProvider webDAVTemplates;

	// Acquire WebDAV Connection From WebDAV Connection Pool
	public static IConnection acquireWebDAVConnection(long storeId)
			throws Exception {

		// Convert Store Id
		Long storeIdLong = new Long(storeId);

		// Check If WebDAV Connection To Archive Store Already Exist In WebDAV
		// Connection Pool
		if (!WebDavTemplateProvider.webDAVTemplateHashTable
				.containsKey(storeIdLong))
			WebDavTemplateProvider.addTemplate(storeIdLong);

		// Acquire WebDAV Connection
		int templateId = ((Integer) WebDavTemplateProvider.webDAVTemplateHashTable
				.get(storeIdLong)).intValue();

		// Try Multiple Times To Acquire A WebDAV Connection From WebDAV
		// Connection Pool
		for (int loopcounter = 0; loopcounter < WebDavTemplateProvider.WEBDAVACQUIRECONNECTIONWAITLOOPS; loopcounter++) {
			try {
				return WebDavTemplateProvider.webDAVSyncConnectionPool
						.acquireConnection(templateId);
			} catch (OutOfConnectionsException oocex) {

				// $JL-EXC$
				Thread
						.sleep(WebDavTemplateProvider.WEBDAVACQUIRECONNECTIONWAITTIME);
				cat.infoT(loc, "Waiting for WebDAV connection");
			}
		}
		return WebDavTemplateProvider.webDAVSyncConnectionPool
				.acquireConnection(templateId);
	}

	// Release WebDAV Connection To Connection Pool
	public static void releaseWebDAVConnection(IConnection conn) {
		if (conn != null)
			WebDavTemplateProvider.webDAVSyncConnectionPool.releaseConnection(
					conn, false);
	}

	// Add WebDAV Connection To WebDAV Connection Pool
	public synchronized static void addTemplate(Long storeIdLong)
			throws Exception {

		// Check Again If WebDAV Connection To Archive Store Already Exist In
		// WebDAV Connection Pool
		if (!WebDavTemplateProvider.webDAVTemplateHashTable
				.containsKey(storeIdLong)) {

			// Get Archive Store Settings
			Connection conn = null;
			ArchStoreConfigLocal ascl = ((ArchStoreConfigLocalHome) new InitialContext()
					.lookup("java:comp/env/ArchStoreConfigBean"))
					.findByPrimaryKey(storeIdLong);
			Sapxmla_Config sac = ascl.getSapxmla_Config();

			// After Destination Service Usage
			if ((sac.destination != null)
					&& (sac.destination.trim().length() != 0)) {
				DestinationService destService = (DestinationService) new InitialContext()
						.lookup(DestinationService.JNDI_KEY);
				if (destService == null)
					throw new NamingException(
							"Destination Service is not available");
				HTTPDestination httpDest = (HTTPDestination) destService
						.getDestination("HTTP", sac.destination);
				IConnectionTemplate connTemplate = httpDest
						.getHTTPConnectionTemplate();
				conn = new Connection(connTemplate);
			}

			// Before Destination Service Usage
			else {
				conn = new Connection(sac.win_root);
				conn.setSessionContext(new SessionContext(
						WebDavTemplateProvider.WEBDAVCLIENTUSR,
						WebDavTemplateProvider.WEBDAVCLIENTPWD,
						WebDavTemplateProvider.WEBDAVCLIENTAUTHSCHEME));
			}

			// Set Possible Proxy Settings
			if (sac.proxy_host != null) {
				conn.setProxyHost(sac.proxy_host);
				conn.setProxyPort(sac.proxy_port);
				conn.setUseProxy(true);
			} else {
				conn.setUseProxy(false);
			}

			// Set Socket And WebDAV Timeouts
			conn.setSocketReadTimeout(WebDavTemplateProvider.WEBDAVREADTIMEOUT);
			conn
					.setSocketConnectTimeout(WebDavTemplateProvider.WEBDAVCONNECTTIMEOUT);
			conn
					.setSocketExpirationTimeout(WebDavTemplateProvider.WEBDAVEXPIRATIONTIMEOUT);

			// Set User Agent
			conn.setUserAgent(WebDavTemplateProvider.WEBDAVUSERAGENT);

			// Add WebDAV Connection To WebDAV Connection Pool
			int templateId = WebDavTemplateProvider.webDAVTemplates
					.addConnectionTemplate(conn);
			WebDavTemplateProvider.webDAVTemplateHashTable.put(new Long(
					sac.store_id), new Integer(templateId));
			WebDavTemplateProvider.webDAVConnectionPool.setLimit(templateId,
					WebDavTemplateProvider.WEBDAVCONNECTIONTEMPLATELIMIT);
		}
	}
}
