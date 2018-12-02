package com.sap.archtech.xmldasinit;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ReflectionException;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.security.api.UMException;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * The class <code>InitServlet</code> represents the main entry point of the
 * XML DAS Initialization Service.
 */
public class InitServlet extends HttpServlet {

	private static final long serialVersionUID = 1234567890l;
	private static final Category cat = Category.getCategory(
			Category.APPS_COMMON_ARCHIVING, "Initialization Service");
	private static final Location loc = Location
			.getLocation("com.sap.archtech.initservice");

	public void init() throws ServletException {
		loc.entering();
		try {

			// Deploy XMLDAS Administration Roles
			ActionsDescriptorBuilder.deployActions();
			cat
					.infoT(
							loc,
							"XMLDAS Administration Roles Deployment: The deployment of the XMLDAS administration roles was successful.");

			// Initialize XMLDAS Administration MBean
			MBeanInitializer mBeanInit = new MBeanInitializer();
			mBeanInit.initMBeans();
			cat
					.infoT(
							loc,
							"XMLDAS MBean Registration: SAP_ITSAMXMLDataArchivingServer MBean was registered successfully.");
		} catch (UMException e) {
			cat
					.logThrowableT(
							Severity.ERROR,
							loc,
							"XMLDAS Administration Roles Deployment: The deployment of the XMLDAS administration roles failed."
									+ e.getMessage(), e);
			throw new ServletException(e);
		} catch (UnsupportedEncodingException e) {
			cat
					.logThrowableT(
							Severity.ERROR,
							loc,
							"XMLDAS Administration Roles Deployment: The deployment of the XMLDAS administration roles failed."
									+ e.getMessage(), e);
			throw new ServletException(e);
		} catch (MalformedObjectNameException e) {
			cat
					.logThrowableT(
							Severity.ERROR,
							loc,
							"XMLDAS MBean Registration: The format of the string does not correspond to a valid object name."
									+ e.getMessage(), e);
			throw new ServletException(e);
		} catch (InstanceAlreadyExistsException e) {
			cat.logThrowableT(Severity.ERROR, loc,
					"XMLDAS MBean Registration: The MBean is already registered in the repository."
							+ e.getMessage(), e);
			throw new ServletException(e);
		} catch (MBeanRegistrationException e) {
			cat.logThrowableT(Severity.ERROR, loc,
					"XMLDAS MBean Registration: Error occurred while MBean registration."
							+ e.getMessage(), e);
			throw new ServletException(e);
		} catch (NotCompliantMBeanException e) {
			cat.logThrowableT(Severity.ERROR, loc,
					"XMLDAS MBean Registration: Registered object is not a JMX compliant MBean."
							+ e.getMessage(), e);
			throw new ServletException(e);
		} catch (NamingException e) {
			cat.logThrowableT(Severity.ERROR, loc,
					"XMLDAS MBean Registration: Error occurred while accessing the MBean server."
							+ e.getMessage(), e);
			throw new ServletException(e);
		} finally {
			loc.exiting();
		}
	}

	public void destroy() {
		loc.entering();
		try {
			MBeanInitializer mBeanInit = new MBeanInitializer();
			mBeanInit.destroyMBeans();
			cat
					.infoT(
							loc,
							"XMLDAS MBean Unregistration: SAP_ITSAMXMLDataArchivingServer MBean was unregistered successfully.");
		} catch (MalformedObjectNameException e) {
			cat
					.logThrowableT(
							Severity.ERROR,
							loc,
							"XMLDAS MBean Unregistration: The format of the string does not correspond to a valid object name."
									+ e.getMessage(), e);
		} catch (MBeanRegistrationException e) {
			cat.logThrowableT(Severity.ERROR, loc,
					"XMLDAS MBean Unregistration: Error occurred while MBean registration."
							+ e.getMessage(), e);
		} catch (InstanceNotFoundException e) {
			cat
					.logThrowableT(
							Severity.ERROR,
							loc,
							"XMLDAS MBean Unregistration: The specified MBean does not exist in the repository."
									+ e.getMessage(), e);
		} catch (NamingException e) {
			cat.logThrowableT(Severity.ERROR, loc,
					"XMLDAS MBean Unregistration: Error occurred while accessing the MBean server."
							+ e.getMessage(), e);
		} finally {
			loc.exiting();
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String info = null;
		try {
			MBeanInitializer mBeanInit = new MBeanInitializer();
			info = mBeanInit.infoMBeans();
		} catch (MalformedObjectNameException e) {
			cat
					.logThrowableT(
							Severity.ERROR,
							loc,
							"XMLDAS MBean Registration Info: The format of the string does not correspond to a valid object name."
									+ e.getMessage(), e);
			throw new ServletException(e);
		} catch (NamingException e) {
			cat
					.logThrowableT(
							Severity.ERROR,
							loc,
							"XMLDAS MBean Registration Info: Error occurred while accessing the MBean server."
									+ e.getMessage(), e);
			throw new ServletException(e);
		} catch (InstanceNotFoundException e) {
			cat
					.logThrowableT(
							Severity.ERROR,
							loc,
							"XMLDAS MBean Registration Info: The specified MBean does not exist in the MBean server. "
									+ e.getMessage(), e);
			throw new ServletException(e);
		} catch (IntrospectionException e) {
			cat.logThrowableT(Severity.ERROR, loc,
					"XMLDAS MBean Registration Info: Error occurred while introspection."
							+ e.getMessage(), e);
			throw new ServletException(e);
		} catch (ReflectionException e) {
			cat.logThrowableT(Severity.ERROR, loc,
					"XMLDAS MBean Registration Info: Error occurred while invoking MBean method."
							+ e.getMessage(), e);
			throw new ServletException(e);
		}
		cat.infoT(loc, new StringBuffer("Received HTTP GET request (Client: ")
				.append(request.getRemoteHost()).append(", User: ").append(
						request.getRemoteUser()).append(")").toString());
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String docType = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 "
				+ "Transitional//EN\">\n";
		out.println(docType + "<HTML>\n"
				+ "<HEAD><TITLE>XMLDAS Initialization Service</TITLE></HEAD>\n"
				+ "<BODY BGCOLOR=\"#FDF5E6\">\n"
				+ "<H1>XMLDAS Initialization Service - Diagnosis</H1>\n"
				+ "<PRE>");
		out.println("<H3>Results of MBean Registration:</H3>\n");
		out.println(info);
		out.println("</BODY>");
	}
}
