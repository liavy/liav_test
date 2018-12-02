package com.sap.security.core.admin.role;

import java.util.MissingResourceException;
import java.util.Set;
import java.util.Vector;

import com.sap.security.api.IPrincipal;
import com.sap.security.api.IRoleFactory;
import com.sap.security.api.IUser;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.IAccessToLogic;
import com.sap.security.core.admin.ListBean;
import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.core.admin.UserAdminLogic;
import com.sap.security.core.admin.util;
import com.sap.security.core.admin.permissions.UMAdminPermissions;
import com.sap.security.core.util.ErrorBean;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.Message;
import com.sap.security.core.util.ResourceBean;

/**
 *  This class is called at the role assignment page, when a user performs
 *  a user-role or group-role assignment.
 *
 *@author     Markus Liepold
 */
public class AddUsersCommand implements Command
{
	private String next;

	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/role/AddUsersCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);

	/**
	 *  Constructor for the AddUsersCommand object
	 *
	 *@param  next  Description of Parameter
	 */
	public AddUsersCommand( String next )
	{
		this.next = next;
	}


	/**
	 *  Constructor for the AddUsersCommand object
	 *
	 *@param  proxy  Description of Parameter
	 *@param  key    Description of Parameter
	 */
	public AddUsersCommand( IAccessToLogic proxy, String key )
	{
		try	{
			ResourceBean localeBean = ( ResourceBean ) proxy.getSessionAttribute( RoleAdminLocaleBean.beanId );
			if ( localeBean != null )
			{
				String next = localeBean.getPage( key );
				this.next = next;
			}
			else {
				trace.errorT( "AddUsersCommand(proxy,String)", "Resource" + key + " not found" );
				next = "error.jsp";
			}
		}
		catch ( MissingResourceException e )
		{
			trace.errorT( "AddUsersCommand(proxy,String)", "Resource" + key + " not found", e );
			proxy.setRequestAttribute( "javax.servlet.jsp.jspException", e );
			next = "error.jsp";
		}
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
		trace.infoT( methodName, "request parameters: " + HelperClass.reportRequestParameters( proxy ) );

        String principal = HelperClass.setPrincipalRequestParam(proxy);
        //hack.  This parameter needs to be a pass through of the useradmin servlet
        //responsible for searching users.  For now I set it to "user"
        if (principal.equalsIgnoreCase(""))  principal = "user";
		String rolenamesReport = HelperClass.getParam( proxy, "rolenamesReport" );
		String roleIDs = util.URLDecoder( HelperClass.getParam( proxy, "roleIDs" ) );
        String memberSF = HelperClass.getParam( proxy, "memberSF");
        
		String[] selectedMemberIDs;
		String pageName = proxy.getRequestParameter(UserAdminLogic.listPage);
		if ( (null != pageName) && (UserAdminLogic.userSearchResultPage.equals( pageName )) ) {
			ListBean userlist = (ListBean) proxy.getSessionAttribute(UserAdminLogic.srList);
			userlist.init(proxy); 
			selectedMemberIDs = util.getSelectedUniqueIDs(userlist);			
		} 
        else {
			selectedMemberIDs = util.getUniqueIDs( proxy, "selectedUsers" );
		}	

		IUser performer = HelperClass.checkActiveUser( proxy );
        // checkPermission throws java.security.AccessControlException if failed.
        performer.checkPermission( new UMAdminPermissions(UserAdminHelper.ASSIGN_ROLES) );

        IRoleFactory roleFactory = UMFactory.getRoleFactory();
		IPrincipal[] members = null;
        boolean status = true;
        // do the search:
		try
		{
            Set membersWithTheseRoles = null;
			Vector roleList = HelperClass.convertIDsToVector( roleIDs, "," );
            String[] roleArray = (String[])roleList.toArray( new String[roleList.size()] );
            RoleAssignmentFactory raFactory = RoleAssignmentFactory.getInstance();

			if (principal.equalsIgnoreCase("user"))
            {
                Set selectedMembers = HelperClass.getUsers( selectedMemberIDs );
			    // Assign the users
                status = raFactory.assignRoles( selectedMembers, roleArray, RoleAssignmentFactory.USER_RETRIEVAL);
    			membersWithTheseRoles = raFactory.getMembersWithTheseRoles( proxy, performer, roleArray, RoleAssignmentFactory.USER_RETRIEVAL, memberSF);
                proxy.setRequestAttribute( "principal","user" );
            }
            else if (principal.equalsIgnoreCase("group"))
            {
                Set selectedMembers = HelperClass.getGroups( selectedMemberIDs );
                // Assign the groups
                status = raFactory.assignRoles( selectedMembers, roleArray, RoleAssignmentFactory.GROUP_RETRIEVAL);
                membersWithTheseRoles = raFactory.getMembersWithTheseRoles( proxy, performer, roleArray, RoleAssignmentFactory.GROUP_RETRIEVAL, memberSF);
                proxy.setRequestAttribute( "principal", "group" );
            }
            else {
                trace.infoT(methodName, "Not supported principal");
            }
            // check status of the role assignment 
            if (!status)
            {
                trace.warningT( methodName, "Assignment of members to roles failed!");
                proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.EXCEPTION_ROLE_ASSIGNMENT ) ) );
            }
			members = HelperClass.getCominedMembers( null, membersWithTheseRoles );
		}
		catch ( UMException e )
		{
			trace.errorT( methodName, "(find members failed): ", e );
			proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.EXCEPTION_SEARCH_FAILED ) ) );
			throw new RoleCommandException( "AddUsersCommand:execute " + e.getMessage() );
		}

		ListBean list = HelperClass.setUpListBean( proxy, principal, members );
        if (trace.beInfo())
            trace.infoT(methodName, "nr. of members in ListBean: " + list.getTotalItems() );

		proxy.setRequestAttribute( ListBean.beanId, list );
		proxy.setRequestAttribute( "rolenamesReport", rolenamesReport );
		proxy.setRequestAttribute( "roleIDs", roleIDs );
		proxy.setRequestAttribute( "roleDescription", HelperClass.getRoleDesciption( roleFactory, roleIDs ) );
        proxy.setRequestAttribute( "memberSF", memberSF);

		trace.exiting( methodName, "returned next:" + next );
		return next;
	}

}
