package com.sap.security.core.admin.role;

import com.sap.security.api.*;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.IAccessToLogic;
import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.core.admin.util;
import com.sap.security.core.role.imp.PermissionRoles;
import com.sap.security.core.util.*;

import java.util.*;

/**
 *  This class is called when a user wants to view/modify a role.
 *
 *@author     Markus Liepold
 */
public class ModifyRoleCommand implements Command
{
	private String next;

	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/role/ModifyRoleCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);


	/**
	 *  Constructor for the ModifyRoleCommand object
	 *
	 *@param  next  Description of Parameter
	 */
	public ModifyRoleCommand( String next )
	{
		this.next = next;
	}


	/**
	 *  This Method expects a assignedRoles such as company name which is needed to
	 *  find out the roleFactory. It also expects the rolename that is wished to be
	 *  deleted. Next it stores the role, all its action and all the availavble
	 *  actions and the assignedRoles into the session and then calls the
	 *  appropriate jsp file (role_management_add.jsp)
	 *
	 *@param  proxy                     Description of Parameter
	 *@return                           Description of the Returned Value
	 *@exception  RoleCommandException  Description of Exception
	 */
	public String execute( IAccessToLogic proxy ) throws RoleCommandException
	{
        final String methodName = "execute(proxy)";
        trace.entering(methodName);
        trace.infoT( methodName, "request parameters: " + HelperClass.reportRequestParameters( proxy ) );

		String ID = HelperClass.getParam( proxy, "ID" );
		// HelperClass.checkActiveUser throws UMRuntimeException if activeUser is null;
		IUser performer = HelperClass.checkActiveUser( proxy );

		String roleID = HelperClass.getParam( proxy, "rolename" ).trim();
        roleID = util.URLDecoder( roleID );

		if ( roleID.equals( "" ) )
		{
			trace.errorT(methodName, "Rolename is missing" );
			proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.MISSING_ROLE ) ) );
			throw new RoleCommandException( "ModifyRoleCommand:No role was passed to be modified" );
		}

        IRole role = null;
        try
        {
            role = UMFactory.getRoleFactory().getRole( roleID );
        }
        catch (UMException ex)
        {
			trace.errorT( methodName, "getRole(roleID) threw an exception! Where roleID = " + roleID );
			proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.MISSING_ROLE ) ) );
			throw new UMRuntimeException( "getRole(roleID) threw an exception! Where roleID = " + roleID );
        }
        //  HelperClass.checkAccess throws java.security.AccessControlException if failed.
        UserAdminHelper.checkAccess( performer, ID, UserAdminHelper.MODIFY_ROLES );

        //HelperClass.getVisibleActions makes sure only visible Actions are returned.
        //TODO call getVisibleActions with correct tp parameter!
        Iterator allActions = HelperClass.getVisibleActions( null );
        Set updatedActions = HelperClass.removeAssignedFromAll( allActions, PermissionRoles.getActions(role) );
        Iterator assignedActions = PermissionRoles.getActions(role);
        Iterator updatedActionsIter = updatedActions.iterator();

        String displayRoleName = role.getDisplayName();
        if (trace.beInfo())
        {
            trace.infoT( methodName, "Rolename: " + displayRoleName );
            trace.infoT( methodName, "RoleDescription:" + role.getDescription());
        }

		proxy.setRequestAttribute( "roleObj", role );
		proxy.setRequestAttribute( "allActions", updatedActionsIter );
		proxy.setRequestAttribute( "assignedActions", assignedActions );
        proxy.setRequestAttribute( "unavailableActions", PermissionRoles.getUnavailableActions(role) );
		proxy.setRequestAttribute( "ID", ID );
		proxy.setRequestAttribute( "modifyRole", "true" );
		proxy.setRequestAttribute( "ROLE_MAX_LENGTH", new Integer( RoleAdminServlet.MAX_ROLE_NAME_LENGTH ) );
		proxy.setRequestAttribute( "rolenamesReport", displayRoleName );
		proxy.setRequestAttribute( "roleFactory", UMFactory.getRoleFactory() );

		trace.exiting( methodName, "returned next:" + next );
		return next;
	}
}
