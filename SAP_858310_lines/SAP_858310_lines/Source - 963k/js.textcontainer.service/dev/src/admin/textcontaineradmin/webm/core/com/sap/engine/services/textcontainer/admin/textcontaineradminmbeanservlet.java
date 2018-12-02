package com.sap.engine.services.textcontainer.admin;


import java.io.IOException;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.jmx.ObjectNameFactory;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Servlet implementation class for Servlet: TextContainerAdminMBeanServlet
 *
 */
public class TextContainerAdminMBeanServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet
{
	public TextContainerAdminMBeanServlet()
	{
		super();
	}   	

	public void init(ServletConfig cfg) throws ServletException
	{
		super.init(cfg);

		try
		{
			InitialContext initCtx = new InitialContext();

			mbs = (MBeanServer) initCtx.lookup("jmx");

			localMBeanName = ObjectNameFactory.getNameForApplicationResourcePerNode("TextContainerAdminMBean", "sap.com/textcontainer~admin", ObjectNameFactory.EMPTY_VALUE, ObjectNameFactory.EMPTY_VALUE);

			TextContainerAdminMBean myMBean = new TextContainerAdmin();

			mbs.registerMBean(myMBean, localMBeanName);
		}
		catch (MalformedObjectNameException e)
		{
    		CATEGORY.logThrowableT(Severity.FATAL, LOCATION, "MalformedObjectNameException", e);
		}
		catch (InstanceAlreadyExistsException e)
		{
    		CATEGORY.logThrowableT(Severity.FATAL, LOCATION, "InstanceAlreadyExistsException", e);
		}
		catch (MBeanRegistrationException e)
		{
    		CATEGORY.logThrowableT(Severity.FATAL, LOCATION, "MBeanRegistrationException", e);
		}
		catch (NotCompliantMBeanException e)
		{
    		CATEGORY.logThrowableT(Severity.FATAL, LOCATION, "NotCompliantMBeanException", e);
		}
		catch (NamingException e)
		{
    		CATEGORY.logThrowableT(Severity.FATAL, LOCATION, "NamingException", e);
		}
		catch (Exception e)
		{
    		CATEGORY.logThrowableT(Severity.FATAL, LOCATION, "Exception", e);
		}
	}

	public void destroy()
	{
		super.destroy();

		try
		{
			mbs.unregisterMBean(localMBeanName);
		}
		catch (InstanceNotFoundException e)
		{
    		CATEGORY.logThrowableT(Severity.FATAL, LOCATION, "InstanceNotFoundException", e);
		}
		catch (MBeanRegistrationException e)
		{
    		CATEGORY.logThrowableT(Severity.FATAL, LOCATION, "MBeanRegistrationException", e);
		}
		catch (Exception e)
		{
    		CATEGORY.logThrowableT(Severity.FATAL, LOCATION, "Exception", e);
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
	}   	  	    

	private static MBeanServer mbs;

	private static ObjectName localMBeanName;

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.admin.TextContainerAdminMBeanServlet");
	private static final Category CATEGORY = Category.APPLICATIONS;

}