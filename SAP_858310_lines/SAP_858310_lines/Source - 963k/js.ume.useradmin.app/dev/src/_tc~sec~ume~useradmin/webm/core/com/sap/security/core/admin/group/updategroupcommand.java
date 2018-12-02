package com.sap.security.core.admin.group;

import com.sap.security.api.*;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.*;
import com.sap.security.core.admin.role.*;
import com.sap.security.core.admin.permissions.UMAdminPermissions;
import com.sap.security.core.persistence.datasource.PersistenceException;
import com.sap.security.core.util.*;

/**
 *  This class is called when a user wants to save a group. It performs the
 *  creation or modification of the group.
 *
 *@author     Markus Liepold
 */
public class UpdateGroupCommand implements Command
{
	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/group/UpdateGroupCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);

    private String next;


	/**
	 *  Constructor for the UpdateCommand object
	 *
	 *@param  next  Description of Parameter
	 */
	public UpdateGroupCommand( String next )
	{
		this.next = next;
	}


	/**
	 *  This method expects the group name that is to be updated. It
	 *  then does the task and stores the group and ID into the session and then
	 *  calls the appropriate jsp file (group_management_main.jsp)
	 *
	 *@param      proxy                 Description of Parameter
	 *@return                           Description of the Returned Value
	 *@exception  RoleCommandException  Description of Exception
	 */
	public String execute( IAccessToLogic proxy ) throws RoleCommandException
	{
        final String methodName = "execute(proxy)";
        trace.entering(methodName);
        if (trace.beDebug())
            trace.debugT( methodName, "request parameters: " + HelperClass.reportRequestParameters( proxy ) );

		// HelperClass.checkActiveUser throws UMRuntimeException if activeUser is null;
		IUser performer = HelperClass.checkActiveUser( proxy );
		// checkPermission throws java.security.AccessControlException if failed.
        performer.checkPermission( new UMAdminPermissions(UserAdminHelper.MODIFY_GROUPS) );

		// if modifying a group, modifyGroup is true, when adding a group, it is false.
		String modifyGroup = HelperClass.getParam( proxy, "modifyGroup" );
        String groupname = HelperClass.getParam( proxy, "groupname" );
		String groupDescription = HelperClass.getParam( proxy, "groupDescription" );
        String searchFilter = (String)proxy.getSessionAttribute("groupSearchFilter");

        if (groupDescription.length() > GroupAdminLogic.MAX_GROUP_DESCRIPTION_LENGTH)
        {
            trace.infoT(methodName, "Truncating groupDescription, MaxGroupDescription is " + 
                        GroupAdminLogic.MAX_GROUP_DESCRIPTION_LENGTH);
            // truncate the Description field.
            groupDescription = groupDescription.substring(0, GroupAdminLogic.MAX_GROUP_DESCRIPTION_LENGTH - 1);
        }

        try {
            String groupID = "";

			if ( groupname.equals("") )
			{
                trace.infoT(methodName, "No group created, because no group name was passed!");
                proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.MISSING_GROUP ) ) );
            }
            else {
                IGroupFactory groupfactory = UMFactory.getGroupFactory();
                IGroup group = null;
				if ( !modifyGroup.equalsIgnoreCase("true" ) )
				{
					trace.infoT(methodName, "Adding group");
					try	{
                        group = groupfactory.newGroup(groupname);
                        group.setDescription(groupDescription);
                        group.commit();
						// set searchfilter to the created group name
						searchFilter = group.getUniqueName();
						proxy.setSessionAttribute("groupSearchFilter", searchFilter);
                        groupID = group.getUniqueID();

                        trace.infoT( methodName, "Group was created successfully" );
                        proxy.setRequestAttribute( InfoBean.beanId, new InfoBean( new Message( RoleAdminMessagesBean.UPDATED_SUCCESS ) ) );
					}
					catch (GroupAlreadyExistsException fe)
					{
						trace.warningT( methodName, "Group " + groupname + " already exists: ", fe );
						proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.ERROR_GROUP_EXISTS ) ) );
						proxy.setRequestAttribute( "modifyGroup", "false" );
						proxy.setRequestAttribute( "groupID", groupID );
						proxy.setRequestAttribute( "groupname", groupname );
                        // if group already exists -> stay on this page
                        trace.exiting( methodName, "returned next: " + GroupAdminLogic.groupManagementAddPage );
                        return GroupAdminLogic.groupManagementAddPage;
					}
                    catch (PersistenceException pe)
                    {
                        trace.warningT( methodName, "Creation of groups is not possible. Adapter is configured read/only!", pe);
                        proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.ERROR_GROUP_CREATE ) ) );
                    }
				}
                else {
                    trace.infoT(methodName, "Modifying Group");
                    groupID = util.URLDecoder( HelperClass.getParam( proxy, "groupID" ) );

                    try {
                        if (!groupDescription.equals(""))
                        {
                            group = groupfactory.getMutableGroup(groupID);
                            trace.infoT( methodName, "Setting groupDescription = " + groupDescription);
                            group.setDescription(groupDescription);
                            group.commit();
                        }
                        proxy.setRequestAttribute( InfoBean.beanId, new InfoBean( new Message( RoleAdminMessagesBean.UPDATED_SUCCESS ) ) );
                        trace.infoT( methodName, "Group was updated successfully" );
                    }
                    catch (NoSuchGroupException ex)
                    {
                        trace.warningT( methodName, "Group doesn't exist anymore; groupID = " + groupID);
                        proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.MISSING_GROUP ) ) );
                    }
                }
			}
            // HelperClass.getVisibleGroups makes sure the group is visible.
			proxy.setRequestAttribute( "list_groups", GroupUtil.getVisibleGroups( proxy, performer, searchFilter, true ) );
            proxy.setRequestAttribute( "searchFilter", searchFilter );
			proxy.setRequestAttribute( "groupID", groupID );
		    trace.exiting( methodName, "returned next: " + next );
			return next;
		}
		catch ( UMException fe )
		{
			trace.warningT(methodName, "Exception occured while updating/creation group: ", fe );
			proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.GENERAL_EXCEPTION ) ) );
			throw new RoleCommandException( "UpdateGroupCommand:execute " + fe.getMessage() );
		}
	}
}
