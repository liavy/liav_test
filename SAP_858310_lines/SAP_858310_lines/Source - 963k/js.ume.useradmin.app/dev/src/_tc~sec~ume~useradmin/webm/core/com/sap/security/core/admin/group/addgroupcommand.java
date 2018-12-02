package com.sap.security.core.admin.group;

import com.sap.security.api.*;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.IUMTrace;

import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.api.UMFactory;
import com.sap.security.core.admin.*;
import com.sap.security.core.admin.role.Command;
import com.sap.security.core.admin.role.*;
import com.sap.security.core.admin.permissions.UMAdminPermissions;

/**
 *  Description of the Class
 *
 *@author     Fereidoon Samii
 *@created    May 16, 2001
 */
public class AddGroupCommand implements Command
{
	private String next;

	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/group/AddGroupCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);


	/**
	 *  Constructor for the AddGroupCommand object
	 *
	 *@param  next  Description of Parameter
	 */
	public AddGroupCommand( String next )
	{
		this.next = next;
	}


	/**
	 *  This method expects a rolename. It then stores all the sreviceActions that
	 *  this role has under assignedActions and all the available actions under
	 *  allActions which are Iterators that contain IServiceAction element. It then
	 *  calls the appropriate jsp file. (role_management_add.jsp)
	 *
	 *@param  proxy                     Description of Parameter
	 *@return                           Description of the Returned Value
	 *@exception  RoleCommandException  Description of Exception
	 */
	public String execute(IAccessToLogic proxy) throws RoleCommandException
	{
        final String methodName = "execute(req)";

		trace.entering( methodName );
        if (trace.beInfo())
    		trace.infoT( methodName, "request parameters: " +HelperClass.reportRequestParameters(proxy) );
		// HelperClass.checkActiveUser throws UMRuntimeException if activeUser is null;
		IUser performer = HelperClass.checkActiveUser( proxy );

		//  checkAccess throws java.security.AccessControlException if failed.
		performer.checkPermission( new UMAdminPermissions(UserAdminHelper.ADD_GROUPS) );

        proxy.setRequestAttribute( "groupObj", null );
        proxy.setRequestAttribute( "modifyGroup", "false" );
        proxy.setRequestAttribute( "GROUP_MAX_LENGTH", new Integer( GroupAdminLogic.MAX_GROUP_NAME_LENGTH ) );
        proxy.setRequestAttribute( "groupFactory", UMFactory.getGroupFactory() );
		trace.exiting( methodName, "returned next:" + next );
		return next;
	}

}
