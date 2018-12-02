package com.sap.security.core.jmx.impl;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.management.MBeanServer;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.jmx.ObjectNameFactory;
import com.sap.security.core.jmx.IJmxServer;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class JmxProvider extends GenericServlet {

	private static final long serialVersionUID = 4601560236052527495L;
	
	private static final String UME_JMX_SERVER_NAME = "UmeJmxServer";
	private static final String UME_JMX_SERVER_INTF = "IJmxServer";

    private static final String WD_URL_UME_ADMIN_APP = "/webdynpro/dispatcher/sap.com/tc~sec~ume~wd~umeadmin/UmeAdminApp";
    private static final String WD_URL_SELFREG_APP   = "/webdynpro/dispatcher/sap.com/tc~sec~ume~wd~enduser/SelfregApp";

	private transient Location myLoc =
		Location.getLocation(JmxProvider.class);
	private transient Category myCat =
		Category.getCategory(Category.SYS_SECURITY, "Usermanagement");

	/**
	 * @see javax.servlet.Servlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
	 */
	public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        final String method = "service(ServletRequest, ServletResponse)";

		// Redirect to the Web Dynpro application
        if(request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest  httpRequest  = (HttpServletRequest ) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // Compatibility: Support the old servlet URLs.
            // Determine the Web Dynpro application that matches the requested servlet.
            String servletPath = httpRequest.getServletPath();
            StringBuffer redirectUrl = new StringBuffer();

            if(servletPath == null) {
                redirectUrl = null;
            }
            else if(servletPath.startsWith("/userAdminServlet")) {
                redirectUrl.append(WD_URL_UME_ADMIN_APP);
            }
            else if(servletPath.startsWith("/roleAdmin")) {
                redirectUrl.append(WD_URL_UME_ADMIN_APP);
            }
            else if(servletPath.startsWith("/groupAdmin")) {
                redirectUrl.append(WD_URL_UME_ADMIN_APP);
            }
            else if(servletPath.startsWith("/selfReg")) {
                redirectUrl.append(WD_URL_SELFREG_APP);
            }
            else {
                redirectUrl = null;
            }

            if(redirectUrl != null) {
                myLoc.infoT(method, "Mapped request for servlet \"{0}\" to UME Web " +
                    "Dynpro application \"{1}\".",
                    new Object[] { servletPath, redirectUrl.toString() } );

                // Add potentially existing request parameter "redirectURL" to redirect URL.
            	String nextRedirectURL = request.getParameter("redirectURL");
                if(nextRedirectURL != null) {
                	redirectUrl.append("?redirectURL=");
                	redirectUrl.append(URLEncoder.encode(URLDecoder.decode(nextRedirectURL, "UTF-8"), "UTF-8"));
                }

                httpResponse.sendRedirect(redirectUrl.toString());
            }
            else {
                // TODO Display some error message?
                myLoc.infoT(method, "Could not map servlet path \"{0}\" to any UME Web " +
                    "Dynpro application.",
                    new Object[] { servletPath } );
            }
        }
	}

	/**
	 * @see javax.servlet.Servlet#destroy()
	 */
	public void destroy() {
		final String METHOD = "destroy()"; //$NON-NLS-1$
		try {
			if (myLoc.beDebug()){
				myLoc.debugT(METHOD, "stopping UMEADMIN provider application");				
			}
			MBeanServer mbs = (MBeanServer) new InitialContext().lookup("jmx"); //$NON-NLS-1$
			if (mbs == null) {
				throw new NamingException("MBeanServer is null, no valid object available in InitialContext");
			}
			mbs.unregisterMBean(
				ObjectNameFactory.getNameForServerChild(
					UME_JMX_SERVER_NAME,
					UME_JMX_SERVER_INTF,
					null));
			if (myLoc.beDebug()){
				myLoc.debugT(METHOD, "UMEADMIN provider application stopped");
			}
		} catch (Exception e) {
			myLoc.traceThrowableT(Severity.ERROR,
				"Unable to stop UMEADMIN Model provider (JMX Server) application",
				e);
		}
	}

	/**
	 * @see javax.servlet.GenericServlet#init()
	 */
	public void init() throws ServletException {
		final String METHOD = "init()"; //$NON-NLS-1$
		try {
            // Initialize locking helper.
            JmxLockingManager.initialize();

			if (myLoc.beDebug()){
				myLoc.debugT(METHOD, "starting UMEADMIN provider application");
		}
			MBeanServer mbs = (MBeanServer) new InitialContext().lookup("jmx"); //$NON-NLS-1$
		if (mbs == null) {
			throw new NamingException("MBeanServer is null, no valid object available in InitialContext");
		}
		IJmxServer mBean = new JmxServer();
		mbs.registerMBean(
			mBean,
			ObjectNameFactory.getNameForServerChild(
				UME_JMX_SERVER_NAME,
				UME_JMX_SERVER_INTF,
				null));
			if (myLoc.beDebug()){
				myLoc.debugT(METHOD, "UMEADMIN provider application started");
		}
		} catch (Exception e) {
			myLoc.traceThrowableT(
				Severity.ERROR,
				"Unable to start UMEADMIN model provider (JMX Server) application, "
					+ "UME WD application will not work.",
				e);
			myCat.errorT(
				myLoc,
				"Unable to start UMEADMIN model provider (JMX Server) application, "
					+ "UME WD application will not work. "
					+ "Please see SAP Note 869852 and 963174 for further notice.");
			throw new ServletException(e);
		}
	}

}
