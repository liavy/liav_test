package com.sap.security.core.admin.group;

import java.util.*;
import com.sap.security.api.*;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.IAccessToLogic;
import com.sap.security.core.admin.UserAdminCommonLogic;
import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.core.admin.permissions.UMAdminPermissions;
import com.sap.security.core.admin.role.*;
import com.sap.security.core.util.*;

/**
 *  This class is called when a user clicks on the group administration.
 *  It gets all visible groups for this user.
 *
 *@author     Markus Liepold
 */
public class GetGroupsCommand implements Command
{
	private String next;

	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/group/GetGroupsCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);


	/**
	 *  Constructor for the GetGroupsCommand object
	 *
	 *@param  next  Description of Parameter
	 */
	public GetGroupsCommand( String next )
	{
		this.next = next;
	}


	/**
	 *  Constructor for the GetGroupsCommand object
	 *
	 *@param  proxy  Description of Parameter
	 *@param  key    Description of Parameter
	 */
	public GetGroupsCommand( IAccessToLogic proxy , String key )
	{
        final String methodName = "GetGroupsCommand(proxy,key)";
		try
		{
			ResourceBean localeBean = ( ResourceBean ) proxy.getSessionAttribute( RoleAdminLocaleBean.beanId );
			if ( localeBean != null )
			{
				String next = localeBean.getPage( key );
				this.next = next;
			}
			else {
				trace.entering( methodName, new Object[]{"resource" + key + " not found"} );
				next = UserAdminCommonLogic.errorPage;
			}
		}
		catch ( MissingResourceException e )
		{
			trace.errorT( methodName, "resource" + key + " not found", e );
			proxy.setRequestAttribute( "javax.servlet.jsp.jspException", e );
			next = UserAdminCommonLogic.errorPage;
		}
	}


	/**
	 *  This Method stores all the groups into the session and
	 *  then calls the appropriate jsp file (group_management_main.jsp)
	 *
	 *@param  proxy                     Description of Parameter
	 *@return                           Description of the Returned Value
	 *@exception  RoleCommandException  Description of Exception
	 */
	public String execute( IAccessToLogic proxy ) throws RoleCommandException
	{
        final String methodName = "execute(proxy)";
		trace.entering(methodName);
        if (trace.beInfo())
    		trace.infoT( methodName, "request parameters: " + HelperClass.reportRequestParameters( proxy ) );

		// HelperClass.checkActiveUser throws UMRuntimeException if activeUser is null;
		IUser performer = HelperClass.checkActiveUser( proxy );

		String redirectURL = HelperClass.getParam( proxy, "redirectURL" );
        String searchFilter = HelperClass.getParam( proxy, "searchFilter" );
        boolean newSearchFilter = "true".equals( HelperClass.getParam(proxy, "newSearchFilter"))?true:false;
        String groupSF = (String)proxy.getSessionAttribute("groupSearchFilter");

        if (newSearchFilter)
        {
            proxy.setSessionAttribute( "groupSearchFilter", searchFilter );
        }
        else if (groupSF != null)
        {
            searchFilter = groupSF;
        }
            
		// checkPermission throws java.security.AccessControlException if failed.
		performer.checkPermission( new UMAdminPermissions(UserAdminHelper.VIEW_GROUPS) );
        Iterator sortedObjects;
        try
        {
            if ((newSearchFilter) || (groupSF != null))
            {
                trace.debugT(methodName, "retrieving groups");
                sortedObjects = GroupUtil.getVisibleGroups( proxy, performer, searchFilter.trim(), true );
                next = GroupAdminLogic.groupManagementPage;
            }
            else {
                sortedObjects = new HashSet().iterator();
                proxy.setRequestAttribute( InfoBean.beanId, new InfoBean( new Message( RoleAdminMessagesBean.NO_SEARCH_PERFORMED ) ) );
            }
        }
        catch (UMException ex)
        {
            trace.errorT(methodName, "No visible groups returned.");
            proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.EXCEPTION_GETTING_GROUPS ) ) );
            sortedObjects = new HashSet().iterator();
        }

		proxy.setRequestAttribute( "list_groups", sortedObjects );
		proxy.setRequestAttribute( "redirectURL", redirectURL );
        proxy.setRequestAttribute( "searchFilter", searchFilter );

		trace.exiting( methodName, "returned next:" + next );
		return next;
	}
}
