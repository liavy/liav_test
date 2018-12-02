package com.sap.security.core.admin.group;

import java.util.*;
import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.core.admin.ListBean;
import com.sap.security.api.*;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.admin.*;
import com.sap.security.core.admin.role.*;
import com.sap.security.core.admin.permissions.UMAdminPermissions;
import com.sap.security.core.util.*;

/**
 *  This class is called when you want to assign a group to a role/group.
 *  It searches for the groups that should be assigned.
 *
 *@author     Fereidoon Samii
 *@created    May 16, 2001
 */
public class SearchCommand implements Command
{
	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/group/SearchCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
	private final static IUMTrace trace = InternalUMFactory.getTrace( VERSIONSTRING );

    private String next;


	/**
	 *  Constructor for the SearchCommand object
	 *
	 *@param  next  Description of Parameter
	 */
	public SearchCommand( String next )
	{
		this.next = next;
	}


	/**
	 *  This Method expects a ID such as company name which is needed to find out
	 *  the roleFactory. It also expects the rolename(s) that is used to search
	 *  which users of the company have in common. Next it stores the users with
	 *  common roles, the remaining users, the rolenames and the ID into the
	 *  session and then calls the appropriate jsp file
	 *  (role_assignment_role_user.jsp)
	 *
	 *@param  proxy                     Description of Parameter
	 *@return                           Description of the Returned Value
	 *@exception  RoleCommandException  Description of Exception
	 */
	public String execute( IAccessToLogic proxy ) throws RoleCommandException
	{
		final String methodName = "execute(proxy)";
		trace.entering( methodName );
        if (trace.beInfo())
    		trace.infoT( methodName, "request parameters: " + HelperClass.reportRequestParameters( proxy ) );

		// HelperClass.checkActiveUser throws UMRuntimeException if activeUser is null;
		IUser performer = HelperClass.checkActiveUser( proxy );

		String redirectURL = HelperClass.getParam( proxy, "redirectURL" );
		String groupIDs = HelperClass.getParam( proxy, "groupIDs" );
		String groupnamesReport = HelperClass.getParam( proxy, "groupnamesReport" );
        String groupDescription = HelperClass.getParam( proxy, "groupDescription" );
        String searchFilter = (String) proxy.getRequestParameter( "searchFilter" );
        String memberSF = HelperClass.getParam( proxy, "memberSF");

		//  HelperClass.checkAccess throws java.security.AccessControlException if failed.
        performer.checkPermission( new UMAdminPermissions(UserAdminHelper.VIEW_GROUPS) );
		Iterator sortedObjects;
		try
		{
            if (searchFilter != null)
            {
                trace.debugT(methodName, "searching groups with searchFilter: " + searchFilter);
                sortedObjects = GroupUtil.getVisibleGroups( proxy, performer, searchFilter.trim(), true );
            }
            else {
                sortedObjects = new HashSet().iterator();
                proxy.setRequestAttribute( InfoBean.beanId, new InfoBean( new Message( RoleAdminMessagesBean.NO_SEARCH_PERFORMED ) ) );
            }
		}
		catch ( UMException ex )
		{
			trace.errorT( methodName, "No visible groups returned." );
            proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.EXCEPTION_GETTING_GROUPS ) ) );
			sortedObjects = new HashSet().iterator();
		}

		Vector principalVec = new Vector();
		while ( sortedObjects.hasNext() )
		{
			IPrincipal principal = ( IPrincipal ) ( sortedObjects.next() );
			principalVec.add( principal );
			trace.debugT( methodName, "search returned groupID: " + principal.getUniqueID() );
		}

		int size = principalVec.size();
		IPrincipal[] assignedMembers = new IPrincipal[size];
		principalVec.toArray( assignedMembers );

        ListBean list = HelperClass.setUpListBean(proxy, "group", assignedMembers);
		trace.infoT( methodName, "nr. of members in ListBean: " + list.getTotalItems() );
        proxy.setRequestAttribute( ListBean.beanId, list );
		proxy.setRequestAttribute( "redirectURL", redirectURL );
		proxy.setRequestAttribute( "groupIDs", groupIDs );
		proxy.setRequestAttribute( "principal", "group" );
        proxy.setRequestAttribute( "groupnamesReport", groupnamesReport );
        proxy.setRequestAttribute( "groupDescription", groupDescription );
        proxy.setRequestAttribute( "searchFilter", searchFilter );
        proxy.setRequestAttribute( "memberSF", memberSF);

		trace.exiting( methodName, "returned next:" + next );
		return next;
	}
    
}
