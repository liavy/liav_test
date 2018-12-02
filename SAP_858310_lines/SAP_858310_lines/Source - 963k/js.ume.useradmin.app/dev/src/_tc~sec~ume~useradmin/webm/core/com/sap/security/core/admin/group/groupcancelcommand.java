/*
 *  Copyright 2001
 *
 *  SAPMarkets, Inc.
 *  All rights reserved
 *
 * @author  Fereidoon Samii
 */
package com.sap.security.core.admin.group;

import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.admin.*;
import com.sap.security.core.admin.role.*;

/**
 *  Description of the Class
 *
 *@author     Fereidoon Samii
 *@created    May 16, 2001
 */
public class GroupCancelCommand implements Command
{
	private String next;

	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/group/GroupCancelCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);


	/**
	 *  Constructor for the CancelCommand object
	 *
	 *@param  next  Description of Parameter
	 */
	public GroupCancelCommand( String next )
	{
		this.next = next;
	}


	/**
	 *  This method is used to support cancel button to redirect the page to the
	 *  caller
	 *
	 *@param  req                       Description of Parameter
	 *@return                           Description of the Returned Value
	 *@exception  RoleCommandException  Description of Exception
	 */
	public String execute( IAccessToLogic proxy ) throws RoleCommandException
	{
        final String methodName = "execute(proxy)";
		trace.entering( methodName );
        if (trace.beInfo())
    		trace.infoT( methodName, "request parameters: " + HelperClass.reportRequestParameters( proxy ) );

		String redirectURL = HelperClass.getParam( proxy, "redirectURL" );
		//Setting this flag to false means to call the redirectURL and redirect to that after
		//this command is exceuted.
		if ( null != proxy.getSession() )
		{
			proxy.setSessionAttribute("ignoreRedirect", "false" );
			trace.infoT( methodName, "cancel was hit to go back to UM url" );
		}

		proxy.setRequestAttribute("redirectURL", redirectURL );
		trace.exiting( methodName, "returned next: " + next );
		return next;
	}
}
