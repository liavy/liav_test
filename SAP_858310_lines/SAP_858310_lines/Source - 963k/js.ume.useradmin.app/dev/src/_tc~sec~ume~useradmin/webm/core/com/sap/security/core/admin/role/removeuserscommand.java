package com.sap.security.core.admin.role;

import java.util.MissingResourceException;
import java.util.Set;
import java.util.Vector;

import com.sap.security.api.IPrincipal;
import com.sap.security.api.IUser;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.IAccessToLogic;
import com.sap.security.core.admin.ListBean;
import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.core.admin.util;
import com.sap.security.core.admin.permissions.UMAdminPermissions;
import com.sap.security.core.util.ErrorBean;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.Message;
import com.sap.security.core.util.ResourceBean;

/**
 *  This class is called when users or groups are removed from a role
 *
 *@author     Fereidoon Samii
 *@created    May 16, 2001
 */
public class RemoveUsersCommand implements Command
{
	private String next;

	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/role/RemoveUsersCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);


	/**
	 *  Constructor for the RemoveUsersCommand object
	 *
	 *@param  next  Description of Parameter
	 */
	public RemoveUsersCommand( String next )
	{
		this.next = next;
	}


	/**
	 *  Constructor for the RemoveUsersCommand object
	 *
	 *@param  proxy  Description of Parameter
	 *@param  key    Description of Parameter
	 */
	public RemoveUsersCommand( IAccessToLogic proxy , String key )
	{
		try
		{
			ResourceBean localeBean = ( ResourceBean )proxy.getSessionAttribute( RoleAdminLocaleBean.beanId );
			if ( localeBean != null )
			{
				String next = localeBean.getPage( key );
				this.next = next;
			}
			else {
				trace.errorT( "RemoveUsersCommand(proxy,String)", "Resource" + key + " not found" );
				next = "error.jsp";
			}
		}
		catch ( MissingResourceException e )
		{
			trace.errorT( "RemoveUsersCommand(proxy,String)", "Resource" + key + " not found", e );
			proxy.setRequestAttribute( "javax.servlet.jsp.jspException", e );
			next = "error.jsp";
		}
	}


	/**
	 *  This Method expects the roleIDs that is used to search which members have
     *  the roles in common. Next it removes the members with common roles, the
     *  remaining members, the roleIDs into the request and then calls the
     *  appropriate jsp file (role_assignment.jsp)

	 *
	 *@param  proxy                     Description of Parameter
	 *@return                           Description of the Returned Value
	 *@exception  RoleCommandException  Description of Exception
	 */
	public String execute( IAccessToLogic proxy ) throws RoleCommandException
	{
        final String methodName = "execute(proxy)";
		trace.entering(methodName);
		trace.infoT( methodName, "request parameters: " +HelperClass.reportRequestParameters(proxy) );

		//contains internalRolenames
        String[] removeMemberIDs = util.getUniqueIDs(proxy, "userIDs" );
		String rolenamesReport = HelperClass.getParam( proxy, "rolenamesReport" );
		String roleIDs = util.URLDecoder( HelperClass.getParam( proxy, "roleIDs" ) );
        String memberSF = HelperClass.getParam( proxy, "memberSF");
        String principal = HelperClass.setPrincipalRequestParam(proxy);
        proxy.setRequestAttribute( "principal", principal );

		// HelperClass.checkActiveUser throws UMRuntimeException if activeUser is null;
		IUser performer = HelperClass.checkActiveUser( proxy );
        // checkPermission throws java.security.AccessControlException if failed.
        performer.checkPermission( new UMAdminPermissions(UserAdminHelper.ASSIGN_ROLES) );

		Set membersWithTheseRoles = null;
		try
		{
            Vector roleList = HelperClass.convertIDsToVector( roleIDs, "," );
            String[] roleArray = (String[])roleList.toArray( new String[roleList.size()] );
            boolean status = true;
            RoleAssignmentFactory raFactory = RoleAssignmentFactory.getInstance();

            if (principal.equalsIgnoreCase("user"))
            {
                trace.infoT(methodName, "Removing user members from the selected roles");
                Set removeMembers = HelperClass.getUsers(removeMemberIDs);
                status = raFactory.removeRolesFromMembers(roleArray, removeMembers);
                membersWithTheseRoles = raFactory.getMembersWithTheseRoles( proxy, performer, roleArray, RoleAssignmentFactory.USER_RETRIEVAL, memberSF );
		    }
            else if (principal.equalsIgnoreCase("group"))
            {
                trace.infoT(methodName, "Removing group members from the selected roles");
                Set removeMembers = HelperClass.getGroups(removeMemberIDs);
                status = raFactory.removeRolesFromMembers(roleArray, removeMembers);
                membersWithTheseRoles = raFactory.getMembersWithTheseRoles( proxy, performer, roleArray, RoleAssignmentFactory.GROUP_RETRIEVAL, memberSF );
		    }
            else {
                trace.infoT(methodName, "Not supported principal: " + principal);
            }

            if (!status )
            {
                proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.EXCEPTION_ROLE_ASSIGNMENT ) ) );
            }
		}
		catch ( UMException e )
		{
			trace.errorT( methodName, "(find users failed): ", e );
			proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.EXCEPTION_SEARCH_FAILED ) ) );
			throw new RoleCommandException( "execute " + e.getMessage() );
		}
        IPrincipal[] assignedMembers = null;

        if (null != membersWithTheseRoles)
        {
            int size = membersWithTheseRoles.size();
            assignedMembers = new IPrincipal[size];
            membersWithTheseRoles.toArray(assignedMembers);
            trace.infoT(methodName, "nr. of assigned members: " + size);
        }
        
        ListBean list = HelperClass.setUpListBean(proxy, principal, assignedMembers);
        proxy.setRequestAttribute( ListBean.beanId, list);
		proxy.setRequestAttribute( "roleIDs", roleIDs );
		proxy.setRequestAttribute( "roleDescription", HelperClass.getRoleDesciption(UMFactory.getRoleFactory(), roleIDs) );
		proxy.setRequestAttribute( "rolenamesReport", rolenamesReport );
        proxy.setRequestAttribute( "memberSF", memberSF);

		trace.exiting( methodName, "returned next:" + next );
		return next;
	}
}
