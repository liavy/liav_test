/*
 * Created on 23.05.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.sap.engine.services.webservices.jaxm.soap;

import iaik.security.ssl.DefaultSessionManager;
import iaik.security.ssl.SSLTransport;
import iaik.security.ssl.Session;

/**
 * This is a dummy implementation for SSL session handling.
 * Currently it does not do any session handling to prevent reuse of previously authenticated SSL sessions
 * @author Martijn de Boer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SSLWSSessionManager extends DefaultSessionManager {

	public SSLWSSessionManager() {
		super();
	}
	/* (non-Javadoc)
	 * @see iaik.security.ssl.SessionManager#cacheSession(iaik.security.ssl.SSLTransport, iaik.security.ssl.Session)
	 */
	protected synchronized void cacheSession(SSLTransport arg0, Session arg1) {
	}

	/* (non-Javadoc)
	 * @see iaik.security.ssl.SessionManager#getSession(iaik.security.ssl.SSLTransport, java.lang.Object)
	 */
	protected synchronized Session getSession(SSLTransport arg0, Object arg1) {
		return null;
	}

}
