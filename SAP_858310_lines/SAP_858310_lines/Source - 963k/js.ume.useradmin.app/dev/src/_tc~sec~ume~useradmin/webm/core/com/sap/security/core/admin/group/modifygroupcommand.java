package com.sap.security.core.admin.group;

import com.sap.security.api.*;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.core.admin.*;
import com.sap.security.core.admin.permissions.UMAdminPermissions;
import com.sap.security.core.admin.role.*;
import com.sap.security.core.imp.GroupFactory;
import com.sap.security.core.util.*;

/**
 *  This class is called when a user wants to view/modify a group.
 *
 *@author     Markus Liepold
 */
public class ModifyGroupCommand implements Command
{
	private String next;

	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/group/ModifyGroupCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);


	/**
	 *  Constructor for the ModifyRoleCommand object
	 *
	 *@param  next  Description of Parameter
	 */
	public ModifyGroupCommand( String next )
	{
		this.next = next;
	}


	/**
	 *  This method expects the group name that is wished to be modified. 
     *  Next it stores some group information into the session and then calls the
	 *  appropriate jsp file (group_management_add.jsp)
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
        // checkAccess throws java.security.AccessControlException if failed.
        performer.checkPermission( new UMAdminPermissions(UserAdminHelper.MODIFY_GROUPS) );

		String groupID = HelperClass.getParam( proxy, "groupname" ).trim();
        groupID = util.URLDecoder( groupID );
        IGroup group = null;
        boolean readOnly = false;
        String displayGroupName = null;
        String modifyGroup = "true";
        try {
            // invalidate group in cache to get a up-to-date object
            GroupFactory.invalidateGroupInCache(groupID);
            group = UMFactory.getGroupFactory().getGroup(groupID);

            if (!UMFactory.getPrincipalFactory().isPrincipalAttributeModifiable(groupID, 
                IPrincipal.DEFAULT_NAMESPACE, IPrincipal.DESCRIPTION))
            {
                trace.infoT(methodName, "Can't update group, because it is read/only!");
                proxy.setRequestAttribute( InfoBean.beanId, new InfoBean( new Message( RoleAdminMessagesBean.READONLY_GROUP_MODIFY ) ) );
                readOnly = true;
            }

            displayGroupName = group.getDisplayName();
            if (trace.beInfo())
                trace.infoT(methodName, "Groupname: " + displayGroupName + ", GroupDescription:" + group.getDescription());
        }
        catch (NoSuchGroupException ex)
        {
            trace.warningT(methodName, "Group ID missing or group not found; groupID = " + groupID, ex );
            // if group doesn't exist (anymore), show error message and create new group
            proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.MISSING_GROUP ) ) );
            modifyGroup = "false";
        }
        catch (UMException ex)
        {
			trace.errorT( methodName, "groupID = " + groupID, ex );
			proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.GENERAL_EXCEPTION ) ) );
			throw new RoleCommandException( "ModifyGroupCommand: Unexpected exception " + ex.getMessage() );
        }

		proxy.setRequestAttribute( "groupObj", group );
		proxy.setRequestAttribute( "modifyGroup", modifyGroup );
        proxy.setRequestAttribute( "readOnly", new Boolean( readOnly ) );
		proxy.setRequestAttribute( "GROUP_MAX_LENGTH", new Integer( GroupAdminLogic.MAX_GROUP_NAME_LENGTH ) );
		proxy.setRequestAttribute( "groupNamesReport", displayGroupName );
		trace.exiting( methodName, "returned next:" + next );
		return next;
	}
}
