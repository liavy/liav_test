package com.sap.security.core.admin.role;

import com.sap.security.api.*;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.IAccessToLogic;
import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.core.util.IUMTrace;

import java.util.*;

/**
 *  Description of the Class
 *
 *@author     Fereidoon Samii
 *@created    May 16, 2001
 */
public class AddRoleCommand implements Command
{
	private String next;

	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/role/AddRoleCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);


	/**
	 *  Constructor for the AddRoleCommand object
	 *
	 *@param  next  Description of Parameter
	 */
	public AddRoleCommand( String next )
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
        final String methodName = "execute(proxy)";
		trace.entering( methodName );
		trace.infoT( methodName, "request parameters: " + HelperClass.reportRequestParameters(proxy) );

		// HelperClass.checkActiveUser throws UMRuntimeException if activeUser is null;
		IUser performer = HelperClass.checkActiveUser( proxy );
		String ID = HelperClass.getParam( proxy, "ID" );

		//  HelperClass.checkAccess throws java.security.AccessControlException if failed.
		UserAdminHelper.checkAccess( performer, ID, UserAdminHelper.ADD_ROLES );

        writeRoleAttr(proxy, UMFactory.getRoleFactory(), RoleAdminServlet.MAX_ROLE_NAME_LENGTH, ID);

		trace.exiting( methodName, "returned next:" + next );
		return next;
	}

    /**************************************************************************/
    private  void writeRoleAttr(IAccessToLogic proxy, IRoleFactory rolefactory, 
                                int RoleMaxLength, String ID)
    {
        // allActions will contain IServiceAction elements
        //TODO call getVisibleActions with correct tp parameter!
        Iterator allActions = HelperClass.getVisibleActions(null);
		proxy.setRequestAttribute( "roleObj", null );
		proxy.setRequestAttribute( "allActions", allActions );
		proxy.setRequestAttribute( "assignedActions", null );
		proxy.setRequestAttribute( "ID", ID );
		proxy.setRequestAttribute( "modifyRole", "false" );
		proxy.setRequestAttribute( "ROLE_MAX_LENGTH", new Integer(RoleMaxLength) );
		proxy.setRequestAttribute( "roleFactory", rolefactory );
    }

}
