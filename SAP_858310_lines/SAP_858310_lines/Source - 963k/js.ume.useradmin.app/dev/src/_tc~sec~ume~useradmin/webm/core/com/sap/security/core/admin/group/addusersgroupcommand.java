package com.sap.security.core.admin.group;

import java.util.*;
import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.api.UMFactory;
import com.sap.security.core.admin.ListBean;
import com.sap.security.core.util.*;
import com.sap.security.api.*;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.admin.*;
import com.sap.security.core.admin.role.*;
import com.sap.security.core.admin.permissions.UMAdminPermissions;

/**
 *  This class is called at the group assignment page, when a user performs
 *  a user-group or group-group assignment.
 *
 *@author     Markus Liepold
 */
public class AddUsersGroupCommand implements Command
{
	private String next;

	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/group/AddUsersGroupCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);

	/**
	 *  Constructor for the AddUsersGroupCommand object
	 *
	 *@param  next  Description of Parameter
	 */
	public AddUsersGroupCommand( String next )
	{
		this.next = next;
	}


	/**
	 *  Constructor for the AddUsersGroupCommand object
	 *
	 *@param  req  Description of Parameter
	 *@param  key  Description of Parameter
	 */
	public AddUsersGroupCommand( IAccessToLogic proxy, String key )
	{
		try
		{
			ResourceBean localeBean = ( ResourceBean ) proxy.getSessionAttribute( RoleAdminLocaleBean.beanId );
			if ( localeBean != null )
			{
				String next = localeBean.getPage( key );
				this.next = next;
			}
			else {
				trace.errorT( "AddUsersGroupCommand(proxy,String)", "Resource" + key + " not found" );
				next = UserAdminCommonLogic.errorPage;
			}
		}
		catch ( MissingResourceException e )
		{
			trace.errorT( "AddUsersGroupCommand(proxy,String)", "Resource" + key + " not found", e );
			proxy.setRequestAttribute( "javax.servlet.jsp.jspException", e );
			next = UserAdminCommonLogic.errorPage;
		}
	}


	/**
	 *  This method expects the group name(s) for that the assignments should be
	 *  done, performs the assignment and then calls the appropriate jsp file
	 *  (group_assignment.jsp)
	 *
	 *@param      proxy                 Description of Parameter
	 *@return                           Description of the Returned Value
	 *@exception  RoleCommandException  Description of Exception
	 */
	public String execute( IAccessToLogic proxy ) throws RoleCommandException
	{
        final String methodName = "execute(proxy)";
		trace.entering( methodName );
        if (trace.beInfo())
    		trace.infoT( methodName, "request parameters: " + HelperClass.reportRequestParameters( proxy ) );

        String principal = HelperClass.setPrincipalRequestParam(proxy);
        //hack.  This parameter needs to be a pass through of the useradmin servlet
        //responsible for searching users.  For now I set it to "user"
        if  (principal.equals(""))  principal = "user";
		String groupnamesReport = HelperClass.getParam( proxy, "groupnamesReport" );
		String groupIDs = HelperClass.getParam( proxy, "groupIDs" );
        String memberSF = HelperClass.getParam( proxy, "memberSF");

		String[] _groupIDs = (String[])  proxy.getSessionAttribute("_groupIDs");
        // if session timed out -> return to group main page
        if (_groupIDs == null)
        {
            trace.exiting( methodName, "returned next:" + GroupAdminLogic.groupManagementPage );
            return GroupAdminLogic.groupManagementPage;
        }
        		
		String[] selectedMemberIDs;
		String pageName = proxy.getRequestParameter(UserAdminLogic.listPage);
		if ( (null != pageName) && (UserAdminLogic.userSearchResultPage.equals( pageName )) ) 
        {
			ListBean userlist = (ListBean) proxy.getSessionAttribute(UserAdminLogic.srList);
			userlist.init(proxy); 
			selectedMemberIDs = util.getSelectedUniqueIDs(userlist);			
		} 
        else {
			selectedMemberIDs = util.getUniqueIDs( proxy, "selectedUsers" );
		}	

		IUser performer = HelperClass.checkActiveUser( proxy );
    	IGroupFactory groupFactory = UMFactory.getGroupFactory();
		GroupAssignmentFactory gaFactory = GroupAssignmentFactory.getInstance();

		// checkPermission throws java.security.AccessControlException if failed.
		performer.checkPermission( new UMAdminPermissions(UserAdminHelper.ASSIGN_GROUPS) );


		IPrincipal[] members = null;
        boolean status = true;
		// do the search:
		try {
            Set selectedMembers = null;
            Set membersWithTheseGroups = null;

			if (principal.equalsIgnoreCase("user"))
            {
                selectedMembers = HelperClass.getUsers( selectedMemberIDs );
                // Assign the groups.
                status = gaFactory.assignGroups( proxy, selectedMembers, _groupIDs, GroupAssignmentFactory.USER_RETRIEVAL );
                membersWithTheseGroups = gaFactory.getMembersWithTheseGroups( proxy, performer, _groupIDs, GroupAssignmentFactory.USER_RETRIEVAL, memberSF );
                proxy.setRequestAttribute( "principal", "user" );
            }
            else if (principal.equalsIgnoreCase("group"))
            {
                selectedMembers = GroupUtil.getGroups( selectedMemberIDs );
                // Assign the groups.
                status = gaFactory.assignGroups( proxy, selectedMembers, _groupIDs, GroupAssignmentFactory.GROUP_RETRIEVAL );
                membersWithTheseGroups = gaFactory.getMembersWithTheseGroups( proxy, performer, _groupIDs, GroupAssignmentFactory.GROUP_RETRIEVAL, memberSF );
                proxy.setRequestAttribute( "principal", "group" );
            }
            else {
                trace.infoT(methodName, "Not supported principal");
            }
            // if assignments failed, don't get combined members
            if (!status)
            {
                selectedMembers = new HashSet();
                trace.warningT( methodName, "Assignment of members to groups failed!");
                proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.EXCEPTION_GROUP_ASSIGNMENT ) ) );
            }
            members = HelperClass.getCominedMembers( null, membersWithTheseGroups );
		}
		catch ( UMException e )
		{
			trace.errorT( methodName, "(find members failed): ", e );
			proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.EXCEPTION_SEARCH_FAILED ) ) );
			throw new RoleCommandException( "AddUsersGroupCommand:execute " + e.getMessage() );
		}

		ListBean list = HelperClass.setUpListBean( proxy, principal, members );
        if (trace.beInfo())
    		trace.infoT(methodName, "nr. of members in ListBean: " + list.getTotalItems() );

        proxy.setRequestAttribute( ListBean.beanId, list );
		proxy.setRequestAttribute( "groupnamesReport", groupnamesReport );
		proxy.setRequestAttribute( "groupIDs", groupIDs );
		proxy.setRequestAttribute( "groupDescription", GroupUtil.getGroupDesciption( groupFactory, _groupIDs ) );
        proxy.setRequestAttribute( "memberSF", memberSF);

		trace.exiting( methodName, "returned next:" + next );
		return next;
	}
}
