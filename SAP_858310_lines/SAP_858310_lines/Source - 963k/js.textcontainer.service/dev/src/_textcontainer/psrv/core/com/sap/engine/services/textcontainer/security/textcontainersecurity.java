/*
 * Created on Mar 6, 2006
 */
package com.sap.engine.services.textcontainer.security;

import java.security.Permission;

import com.sap.engine.interfaces.security.SecurityContextObject;
import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.engine.services.textcontainer.TextContainerService;
import com.sap.security.api.IUser;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * @author d029702
 */
public class TextContainerSecurity
{

	static public void checkAdministrationPermission() throws TextContainerException
	{
    	try
		{
    		SecurityContextObject sco = (SecurityContextObject) TextContainerService.getServiceContext().getCoreContext().getThreadSystem().getThreadContext().getContextObject(SecurityContextObject.NAME);

    		String userName = sco.getSession().getPrincipal().getName();

    		IUser user = UMFactory.getUserFactory().getUserByUniqueName(userName);

    		Permission permission = new TextContainerPermission("administration", "change");

    		if (!user.hasPermission(permission))
    		{
    			throw new TextContainerException("User " + userName + " does not have permission " + permission.getClass().getName() + " with parameters " + permission.toString());
    		}
		}
    	catch (UMException e)
    	{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "checkAdministrationPermission", e);
    		throw new TextContainerException("UMException", e);
    	}
	}

	static public boolean checkAdministrationPermission2() throws TextContainerException
	{
    	try
		{
    		SecurityContextObject sco = (SecurityContextObject) TextContainerService.getServiceContext().getCoreContext().getThreadSystem().getThreadContext().getContextObject(SecurityContextObject.NAME);

    		String userName = sco.getSession().getPrincipal().getName();

    		IUser user = UMFactory.getUserFactory().getUserByUniqueName(userName);

    		Permission permission = new TextContainerPermission("administration", "change");

    		return user.hasPermission(permission);
		}
    	catch (UMException e)
    	{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "checkAdministrationPermission2", e);
    		throw new TextContainerException("UMException", e);
    	}
	}

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.security.TextContainerSecurity");
	private static final Category CATEGORY = Category.SYS_SERVER;
}
