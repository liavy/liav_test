package com.sap.security.core.sapmimp.logon;

import java.net.URLDecoder;
import java.net.URLEncoder;

import com.sap.security.api.IUser;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.api.logon.ILoginConstants;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.permissions.UMAdminPermissions;
import com.sap.security.core.logon.imp.AnonymousUser;
import com.sap.security.core.logonadmin.IAccessToLogic;
import com.sap.security.core.persistence.IPrincipalDatabag;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.UMEPermission;
import com.sap.security.core.util.imp.LogonUtils;

/**
 * Title: UM3 Authentication Description: UM3 Authentication Copyright:
 * Copyright (c) 2001 Company: SAPMarkets, Inc
 * 
 * @author Tim Tianwen Feng
 * @created April 26, 2001
 * @version 1.0
 */
public class LogonBean {

	public final static String VERSIONSTRING = "$Id: //engine/j2ee.ume/dev/src/com.sap.security.core.admin/_tc~sec~ume~logon/webapp/WEB-INF/java/com/sap/security/core/sapmimp/logon/LogonBean.java#3 $ from $DateTime$ ($Change$)";

	private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);

	public final static String beanId = "LogonBean";

	public final static String LONGUID = ILoginConstants.LOGON_USER_ID;

	public final static String PASSWORD = ILoginConstants.LOGON_PWD_ALIAS;

	public final static String REDIRECTURL = ILoginConstants.REDIRECT_PARAMETER;

	private String logoffRedirect = null;

	/**
	 * Constructor for the LogonBean object
	 */
	public LogonBean() {
		//
	}

	/**
	 * Gets the UID attribute of the LogonBean class
	 * 
	 * @param proxy
	 *            Description of Parameter
	 * @return The UID value
	 */
	public String getUid(IAccessToLogic proxy) {
		String uid = null;

		try {
			uid = proxy.getRequestParameter("uid");

			if (uid != null) {
				uid = uid.trim();
			}
		} catch (Exception e) {
			trace.errorT("getUid", "Exception occured when getUID().  Message: uid is null");
		}

		if (trace.beDebug()) {
			trace.debugT("getUid", "uid = " + uid);
		}

		return uid;
	}

	public String getLongUid(IAccessToLogic proxy) {
		String uid = null;

		try {
			uid = proxy.getRequestParameter(ILoginConstants.LOGON_USER_ID);

			if (uid != null) {
				uid = uid.trim();
			}
		} catch (Exception e) {
			trace.errorT("getLongUid", "Exception occured- Message: LongUid is null", e);
		}

		if (trace.beDebug()) {
			trace.debugT("getLongUid", "uid = " + uid);
		}

		return uid;
	}

	/**
	 * Gets the LogonURL attribute of the LogonBean class
	 * 
	 * @param proxy
	 *            Description of Parameter
	 * @param params
	 *            Description of Parameter
	 * @return The LogonURL value
	 */
	public String getLogonURL(IAccessToLogic proxy, String params) {
		String logonURL = SAPMLogonLogic.alias(proxy);
		String schema = proxy.getRequestParameter("schema");

		if (schema == null) {
			schema = (String) proxy.getRequestAttribute("schema");
		}

		String paramURL = null;

		if (schema != null) {
			paramURL = "schema=" + encode(schema);
		}

		if (!(params == null || params.equals(""))) {
			if (paramURL == null) {
				paramURL = params;
			} else {
				paramURL += "&" + params;
			}
		}

		String redirectURL = proxy.getRequestParameter(ILoginConstants.REDIRECT_PARAMETER);

		if (redirectURL != null) {
			if (paramURL == null) {
				paramURL = ILoginConstants.REDIRECT_PARAMETER + "=" + encode(redirectURL);
			} else {
				paramURL = paramURL + "&" + ILoginConstants.REDIRECT_PARAMETER + "=" + encode(redirectURL);
			}
		}

		if (paramURL != null) {
			if (logonURL.indexOf("?") >= 0) {
				logonURL = logonURL + "&" + paramURL;
			} else {
				logonURL = logonURL + "?" + paramURL;
			}
		}

		if (trace.beDebug()) {
			trace.debugT("getLogonURL", "logonURL = " + logonURL);
		}

		return logonURL;
	}

	public String getLogonCertURL(IAccessToLogic proxy, String params) {
		String schema = proxy.getRequestParameter("schema");

		if (schema == null) {
			schema = (String) proxy.getRequestAttribute("schema");
		}

		String paramURL = null;

		if (schema != null) {
			paramURL = "schema=" + encode(schema);
		}

		if (!(params == null || params.equals(""))) {
			if (paramURL == null) {
				paramURL = encode(params);
			} else {
				paramURL += "&" + encode(params);
			}
		}

		String logonURL = SAPMLogonCertLogic.alias(proxy);
		String query = proxy.getQueryString();

		if (!(paramURL == null || paramURL.equals(""))) {
			query = paramURL + "&" + encode(query);
		}

		logonURL = logonURL + "?" + encode(query);

		if (trace.beDebug()) {
			trace.debugT("getLogonCertURL", "logonURL = " + logonURL);
		}

		return logonURL;
	}

	public String getHttpsCertURL(IAccessToLogic proxy, String params) {
		String httpsline = "https://" + proxy.getServerName();
		String https_port = UMFactory.getProperties().get(LogonUtils.UM_HTTPS_PORT);

		if (https_port != null) {
			httpsline = httpsline + ":" + https_port;
		}

		httpsline = httpsline + getLogonCertURL(proxy, params);

		if (trace.beDebug()) {
			trace.debugT("getHttpsCertURL", "SSL URL=" + httpsline);
		}

		return httpsline;
	}

	/*
	 * //WAS NOT USED!! private String formatParms(Hashtable keyvalue) { String
	 * queryString = "";
	 * 
	 * if (!keyvalue.isEmpty()) { Enumeration keys = keyvalue.keys();
	 * 
	 * while (keys.hasMoreElements()) { String key = (String)
	 * keys.nextElement(); queryString = queryString + "&" + key + "=" +
	 * keyvalue.get(key); } // get rid of the initial "&" queryString =
	 * queryString.substring(1); }
	 * 
	 * return queryString; }
	 */
	public void setLogoffRedirect(IAccessToLogic proxy) {
		logoffRedirect = encode(proxy.getRequestParameter(ILoginConstants.REDIRECT_PARAMETER));

		if (trace.beDebug()) {
			trace.debugT("setLogoffRedirect", "Set logoffRedirect to " + logoffRedirect);
		}
	}

	public String getLogoffRedirect() {
		return logoffRedirect;
	}

	public boolean getSelfReg() {
		IUser user = null;
		// no specific user, use anonymous
		try {
			user = AnonymousUser.getInstance().getAnonymousUser();

		} catch (UMException umex) {
			return false;
		}

		if (user != null) {
			// check if user has permission
			UMEPermission perm = new UMEPermission(IPrincipalDatabag.USER_TYPE, UMEPermission.ACTION_CREATE,
					UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID, null, null, user.getUniqueID());

			return user.hasPermission(perm);
		}

		return false;
	}

	public boolean getAllowCertLogon() {
		return UMFactory.getProperties().getBoolean(LogonUtils.ALLOW_CERT_LOGON, false);
	}

	public boolean getLogonHelp() {
		IUser user = null;
		// no specific user, use anonymous
		try {
			user = AnonymousUser.getInstance().getAnonymousUser();

		} catch (UMException umex) {
			return false;
		}

		if (user != null) {
			// check if user has permission
			UMAdminPermissions umaperm = new UMAdminPermissions("LOGON_HELP");

			return user.hasPermission(umaperm);
		}

		return false;
	}

	public boolean getPasswordReset() {
		return UMFactory.getProperties().getBoolean(LogonUtils.UM_ADMIN_LOGON_PWD_RESET, true);
	}

	private String encode(String str) {
		return URLEncoder.encode(URLDecoder.decode(str));
	}
}
