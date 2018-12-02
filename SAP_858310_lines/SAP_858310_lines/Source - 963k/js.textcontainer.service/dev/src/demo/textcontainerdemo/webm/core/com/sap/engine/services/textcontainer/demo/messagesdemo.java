/*
 * Created on Feb 1, 2006
 */
package com.sap.engine.services.textcontainer.demo;

import java.util.Locale;
import java.util.MissingResourceException;

import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.engine.interfaces.textcontainer.TextContainerManager;
import com.sap.engine.services.textcontainer.runtime.SAPResourceBundle;
import com.sap.security.api.IUser;
import com.sap.security.api.UMFactory;

/**
 * @author d029702
 */
public class MessagesDemo
{
	private MessagesDemo()
	{
	}

	public static void checkLogin(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		IUser user = UMFactory.getAuthenticator().getLoggedInUser(request, response);

		if (null == user)
		{
			UMFactory.getAuthenticator().forceLoggedInUser(request, response);
		}
	}

	public static void setIndustry(String industry) throws Exception
	{
		InitialContext ctx = new InitialContext();
		TextContainerManager tci = (TextContainerManager) ctx.lookup("interfaces/textcontainer_api");

		tci.setSystemContext(industry, "", "");
	}

	public static String getIndustry() throws Exception
	{
		InitialContext ctx = new InitialContext();
		TextContainerManager tci = (TextContainerManager) ctx.lookup("interfaces/textcontainer_api");

		return tci.getSystemContextIndustry();
	}

	public static String getString(String key)
	{
		try
		{
//			return SAPResourceBundle.getBundle("View").getString(key);
			return SAPResourceBundle.getBundle("View", new Locale("en"), MessagesDemo.class).getString(key);
//			return SAPResourceBundle.getBundle("View", null, MessagesDemo.class.getClassLoader()).getString(key);
		}
		catch (MissingResourceException e)
		{
		    // $JL-EXC$
			return "Text with key '" + key + "' not found!";
		}
	}
}