package com.sap.security.core.admin.group;

import java.util.*;
import com.sap.security.api.*;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.IAccessToLogic;
import com.sap.security.core.admin.ListBean;
import com.sap.security.core.admin.UserAdminCommonLogic;
import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.core.admin.util;
import com.sap.security.core.admin.role.*;
import com.sap.security.core.admin.permissions.UMAdminPermissions;
import com.sap.security.core.util.*;

/**
 *  Description of the Class
 *
 *@author     Fereidoon Samii
 *@created    May 16, 2001
 */
public class RemoveGroupUsersCommand implements Command
{
	private String next;

	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/group/RemoveGroupUsersCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);


	/**
	 *  Constructor for the RemoveGroupUsersCommand object
	 *
	 *@param  next  Description of Parameter
	 */
	public RemoveGroupUsersCommand( String next )
	{
		this.next = next;
	}


	/**
	 *  Constructor for the RemoveGroupUsersCommand object
	 *
	 *@param  req  Description of Parameter
	 *@param  key  Description of Parameter
	 */
	public RemoveGroupUsersCommand( IAccessToLogic proxy , String key )
	{
        final String methodName = "RemoveGroupUsersCommand(proxy,String)";
		try	{
			ResourceBean localeBean = ( ResourceBean )proxy.getSessionAttribute( RoleAdminLocaleBean.beanId );
			if ( localeBean != null )
			{
				String next = localeBean.getPage( key );
				this.next = next;
			}
			else {
				trace.errorT( methodName, "Resource" + key + " not found" );
				next = UserAdminCommonLogic.errorPage;
			}
		}
		catch ( MissingResourceException e )
		{
			trace.errorT( methodName, "Resource" + key + " not found", e );
			proxy.setRequestAttribute( "javax.servlet.jsp.jspException", e );
			next = UserAdminCommonLogic.errorPage;
		}
	}


	/**
	 *  This Method expects the groupIDs that is used to search which members have
	 *  the groups in common. Next it removes the members with common groups, the
	 *  remaining members, the groupIDs into the request and then calls the
	 *  appropriate jsp file (group_assignment_role_user.jsp)
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
    		trace.infoT( methodName, "request parameters: " + HelperClass.reportRequestParameters(proxy) );

        String[] removeMemberIDs = util.getUniqueIDs(proxy, "userIDs" );
		String groupnamesReport = HelperClass.getParam( proxy, "groupnamesReport" );
		String groupIDs = HelperClass.getParam( proxy, "groupIDs" );
		String[] _groupIDs = (String[]) proxy.getSessionAttribute("_groupIDs");
        String memberSF = HelperClass.getParam( proxy, "memberSF");
        String principal = HelperClass.setPrincipalRequestParam(proxy);

		// HelperClass.checkActiveUser throws UMRuntimeException if activeUser is null;
		IUser performer = HelperClass.checkActiveUser( proxy );

		// checkPermission throws java.security.AccessControlException if failed.
        performer.checkPermission( new UMAdminPermissions(UserAdminHelper.VIEW_GROUPS) );

		Set membersWithTheseGroups = null;
        IGroupFactory groupFactory = UMFactory.getGroupFactory();
		try	{
            // Now make this persistent.
            boolean status = true;
            Set removeMembers;
            GroupAssignmentFactory gaFactory = GroupAssignmentFactory.getInstance();
            if (principal.equalsIgnoreCase("user"))
            {
                trace.infoT(methodName, "Removing user members from the selected groups");
                removeMembers = HelperClass.getUsers(removeMemberIDs);
                // success of method "removeGroupsFromMembers" is not checked yet
                status = gaFactory.removeGroupsFromMembers(_groupIDs, removeMembers);
                membersWithTheseGroups = gaFactory.getMembersWithTheseGroups( proxy, performer, _groupIDs, GroupAssignmentFactory.USER_RETRIEVAL, memberSF );
		    }
            else if (principal.equalsIgnoreCase("group"))
            {
                trace.infoT(methodName, "Removing group members from the selected groups");
                removeMembers = GroupUtil.getGroups(removeMemberIDs);
                // success of method "removeGroupsFromMembers" is not checked yet
                status = gaFactory.removeGroupsFromMembers(_groupIDs,removeMembers);
                membersWithTheseGroups = gaFactory.getMembersWithTheseGroups( proxy, performer, _groupIDs, GroupAssignmentFactory.GROUP_RETRIEVAL, memberSF );
		    }
            else {
                trace.infoT(methodName, "Not supported principal: " + principal);
            }
            if (!status)
                proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.EXCEPTION_GROUP_ASSIGNMENT ) ) );
        }
		catch ( UMException e )
		{
			trace.errorT( methodName, "(find principal failed): ", e );
			proxy.setRequestAttribute( InfoBean.beanId, new InfoBean( new Message( RoleAdminMessagesBean.EXCEPTION_SEARCH_FAILED ) ) );
			throw new RoleCommandException( "execute " + e.getMessage() );
		}

        IPrincipal[] assignedMembers = null;
        if (null != membersWithTheseGroups)
        {
            int size = membersWithTheseGroups.size();
            assignedMembers = new IPrincipal[size];
            membersWithTheseGroups.toArray(assignedMembers);
            trace.infoT(methodName, "nr. of assignedMembers: " + size);
        }
        
        ListBean list = HelperClass.setUpListBean(proxy,principal,assignedMembers);
        proxy.setRequestAttribute( ListBean.beanId, list);
		proxy.setRequestAttribute( "groupIDs", groupIDs );
		proxy.setRequestAttribute( "groupDescription", GroupUtil.getGroupDesciption(groupFactory, _groupIDs) );
		proxy.setRequestAttribute( "groupnamesReport", groupnamesReport );
        proxy.setRequestAttribute( "memberSF", memberSF);

		trace.exiting( methodName, "returned next:" + next );
		return next;
	}
}
