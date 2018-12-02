package com.sap.security.core.admin.group;

import java.util.HashSet;
import java.util.Iterator;

import com.sap.security.api.IGroup;
import com.sap.security.api.IGroupFactory;
import com.sap.security.api.IPrincipal;
import com.sap.security.api.IPrincipalFactory;
import com.sap.security.api.IUser;
import com.sap.security.api.NoSuchGroupException;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.IAccessToLogic;
import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.core.admin.util;
import com.sap.security.core.admin.permissions.UMAdminPermissions;
import com.sap.security.core.admin.role.Command;
import com.sap.security.core.admin.role.HelperClass;
import com.sap.security.core.admin.role.RoleAdminMessagesBean;
import com.sap.security.core.admin.role.RoleCommandException;
import com.sap.security.core.persistence.datasource.imp.CompanyGroups;
import com.sap.security.core.util.ErrorBean;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.InfoBean;
import com.sap.security.core.util.Message;

/**
 *  This class is called when a user wants do delete a group.
 *
 *@author     Markus Liepold
 */
public class DeleteGroupCommand implements Command
{
	private String next;

	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/group/DeleteGroupCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);


	/**
	 *  Constructor for the DeleteCommand object
	 *
	 *@param  next  Description of Parameter
	 */
	public DeleteGroupCommand( String next )
	{
		this.next = next;
	}


	/**
	 *  This method expects groupname(s) to be deleted. It then deletes the
	 *  group(s) and calls the appropriate jsp file (group_management_main.jsp)
	 *
	 *@param  proxy                     Description of Parameter
	 *@return                           Description of the Returned Value
	 *@exception  RoleCommandException  Description of Exception
	 */
	public String execute( IAccessToLogic proxy ) throws RoleCommandException
	{
        final String methodName = "execute(proxy)";
		trace.entering(methodName);
        if (trace.beDebug())
    		trace.debugT( methodName, "request parameters: " + HelperClass.reportRequestParameters(proxy) );

		// HelperClass.checkActiveUser throws UMRuntimeException if activeUser is null;
		IUser performer = HelperClass.checkActiveUser( proxy );
        //  checkPermission throws java.security.AccessControlException if failed.
        performer.checkPermission( new UMAdminPermissions(UserAdminHelper.DELETE_GROUPS) );

        String searchFilter = (String)proxy.getSessionAttribute("groupSearchFilter");
		String groupIDs[] = util.getUniqueIDs( proxy, "groupname" );
        // get number if group ID's
        int count = (groupIDs==null) ? 0 : groupIDs.length;

		if ( (count == 0) || ((count == 1) && ("".equals( groupIDs[0] ))) ) 
		{
            trace.infoT(methodName, "Could not delete a group -> groupID(s) were empty");
			proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.MISSING_GROUP ) ) );
		}
		else {
            IGroupFactory groupfactory = UMFactory.getGroupFactory();
            IPrincipalFactory pFactory = UMFactory.getPrincipalFactory();
            
            int errorCode = 0;
            for ( int i = 0; i < count; i++ )
            {
                String groupID = groupIDs[i].trim();
                if ( !"".equals( groupID ) )
                {
                    try {
                        if (pFactory.isPrincipalDeletable( groupID ))
                        {
                            try {
                                // check existence of group before its deletion
                                IGroup group = groupfactory.getGroup( groupID );
                                // company groups must be checked separately
                                if (groupID.startsWith(CompanyGroups.IDPREFIX))
                                {
                                    trace.infoT( methodName, "Company groups can't be deleted: " + groupID );
                                    if (errorCode < 1) errorCode = 1;
                                }
                                else {
                                    trace.infoT( methodName, "Deleting group: " + groupID );
                                    groupfactory.deleteGroup( groupID );
                                }
                            }
                            catch (NoSuchGroupException exc)
                            {
                                trace.infoT( methodName, "The group doesn't exist anymore: " + groupID);
                            }
                        }
                        else {
                            trace.infoT( methodName, "The group is read/only: " + groupID );
                            if (errorCode < 1) errorCode = 1;
                        }
                    }
                    catch (UMException ex)
                    {
                        errorCode = 2;
                        trace.warningT( methodName, "Could not delete group: " + groupID, ex );
                    }
                }
            }
            switch (errorCode) {
                case 1: // some groups were read/only
                    proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.ERROR_GROUP_DELETE ) ) );
                    break;
                case 2: // exception occured during deletion
                    proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.GENERAL_EXCEPTION ) ) );
                    break;
                default:    // success
                    proxy.setRequestAttribute( InfoBean.beanId, new InfoBean( new Message( RoleAdminMessagesBean.GROUPS_ALL_DELETE_SUCCESS ) ) );
            }            
		}

		// HelperClass.getVisibleGroups makes sure the group is visible.
		Iterator sortedGroups;
        try {
            sortedGroups = GroupUtil.getVisibleGroups( proxy, performer, searchFilter, true );
        }
        catch (UMException ex)
        {
            trace.errorT(methodName, "No visible groups returned.", ex);
            sortedGroups = new HashSet().iterator();
        }

		proxy.setRequestAttribute( "list_groups", sortedGroups );
        proxy.setRequestAttribute( "searchFilter", searchFilter );

		trace.exiting( methodName, "returned next:" + next );
		return next;
	}
}
