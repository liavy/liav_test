package com.sap.security.core.admin.group;

import java.util.*;
import com.sap.security.api.*;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.*;
import com.sap.security.core.admin.role.*;
import com.sap.security.core.admin.permissions.UMAdminPermissions;
import com.sap.security.core.util.*;
import com.sap.security.core.util.IUMTrace;


/**
 *  Description of the Class
 *
 *@author     Fereidoon Samii
 *@created    May 16, 2001
 */
public class GroupPageNavigationCommand implements Command
{
	private String next;

	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/group/GroupPageNavigationCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);


	/**
	 *  Constructor for the PageNavigationCommand object
	 *
	 *@param  next  Description of Parameter
	 */
	public GroupPageNavigationCommand( String next )
	{
		this.next = next;
	}


	/**
	 *  Constructor for the PageNavigationCommand object
	 *
	 *@param  proxy  Description of Parameter
	 *@param  key    Description of Parameter
	 */
	public GroupPageNavigationCommand( IAccessToLogic proxy, String key )
	{
		try	{
			ResourceBean localeBean = ( ResourceBean ) proxy.getSessionAttribute( RoleAdminLocaleBean.beanId );
			if ( localeBean != null )
			{
				String next = localeBean.getPage( key );
				this.next = next;
			}
			else {
				trace.errorT( "GroupPageNavigationCommand(proxy,key)", "Resource" + key + " not found" );
				next = UserAdminCommonLogic.errorPage;
			}
		}
		catch ( MissingResourceException e )
		{
			trace.errorT( "GroupPageNavigationCommand(proxy,key)", "Resource" + key + " not found", e );
			proxy.setRequestAttribute( "javax.servlet.jsp.jspException", e );
			next = UserAdminCommonLogic.errorPage;
		}
	}


	/**
	 *  This Method expects a ID such as company name which is needed to find out
	 *  the groupFactory. It also expects the rolename(s) that is used to search
	 *  which users of the company have in common. Next it stores the users with
	 *  common roles, the remaining users, the rolenames and the ID into the
	 *  session and then calls the appropriate jsp file
	 *  (role_assignment_role_user.jsp)
	 *
	 *@param  proxy                     Description of Parameter
	 *@return                           Description of the Returned Value
	 *@exception  RoleCommandException  Description of Exception
	 */
	public String execute( IAccessToLogic proxy  ) throws RoleCommandException
	{
        final String methodName = "execute(proxy)";
		trace.entering(methodName);
        if (trace.beInfo())
    		trace.infoT( methodName, "request parameters: " + HelperClass.reportRequestParameters(proxy) );

		IGroupFactory groupFactory = UMFactory.getGroupFactory();
        String principal = HelperClass.setPrincipalRequestParam(proxy);
		String groupnamesReport = HelperClass.getParam( proxy, "groupnamesReport" );
		String groupIDs = HelperClass.getParam( proxy, "groupIDs" );
		String[] _groupIDs = (String[]) proxy.getSessionAttribute( "_groupIDs" );
        String readOnlyGroups = HelperClass.getParam( proxy, "readOnlyGroups" );
        String memberSF = HelperClass.getParam( proxy, "memberSF");
        String searchFilter = HelperClass.getParam( proxy, "searchFilter");

		// HelperClass.checkActiveUser throws UMRuntimeException if activeUser is null;
		IUser performer = HelperClass.checkActiveUser( proxy );

		// checkPermission throws java.security.AccessControlException if failed.
        performer.checkPermission( new UMAdminPermissions(UserAdminHelper.VIEW_GROUPS) );

        ListBean list = HelperClass.getListBean(proxy,principal);
        proxy.setRequestAttribute(ListBean.beanId, list);
		proxy.setRequestAttribute( "groupnamesReport", groupnamesReport );
		proxy.setRequestAttribute( "groupIDs", groupIDs );
		proxy.setRequestAttribute( "groupDescription", GroupUtil.getGroupDesciption (groupFactory, _groupIDs) );
        proxy.setRequestAttribute( "readOnlyGroups", new Boolean ("true".equalsIgnoreCase(readOnlyGroups)) );
        proxy.setRequestAttribute( "memberSF", memberSF);
        proxy.setRequestAttribute( "searchFilter", searchFilter);
		String searching = HelperClass.getParam(proxy,"searching");
        if (searching.equalsIgnoreCase("true"))
        {
            next = GroupAdminLogic.groupSearchResultPage;
        }
        else {
            next = GroupAdminLogic.groupAssignmentPage;
        }
        trace.exiting( methodName, "returned next: " + next );
        return next;
	}
}
