package com.sap.security.core.admin.role;

import java.util.HashSet;
import java.util.Iterator;

import com.sap.security.api.IPrincipalFactory;
import com.sap.security.api.IRole;
import com.sap.security.api.IRoleFactory;
import com.sap.security.api.IUser;
import com.sap.security.api.NoSuchRoleException;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.IAccessToLogic;
import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.core.admin.util;
import com.sap.security.core.admin.permissions.UMAdminPermissions;
import com.sap.security.core.util.ErrorBean;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.InfoBean;
import com.sap.security.core.util.Message;

/**
 *  This class is called when a user wants do delete a role.
 *
 *@author     Markus Liepold
 */
public class DeleteCommand implements Command
{
	private String next;

	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/role/DeleteCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);


	/**
	 *  Constructor for the DeleteCommand object
	 *
	 *@param  next  Description of Parameter
	 */
	public DeleteCommand( String next )
	{
		this.next = next;
	}


	/**
	 *  This Method expects a rolename(s) to be deleted and a namespace such as
	 *  company name which is needed to find out the roleFactory. It then deletes
	 *  the role and calls the appropriate jsp file (role_management_main.jsp)
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
            trace.infoT( methodName, "request parameters: " +HelperClass.reportRequestParameters(proxy) );

		// HelperClass.checkActiveUser throws UMRuntimeException if activeUser is null;
		IUser performer = HelperClass.checkActiveUser( proxy );
        String searchFilter = (String)proxy.getSessionAttribute("roleSearchFilter");
        //  checkPermission throws java.security.AccessControlException if failed.
        performer.checkPermission( new UMAdminPermissions(UserAdminHelper.DELETE_ROLES) );

		String roleIDs[] = util.getUniqueIDs( proxy, "rolename" );
        // get number if group ID's
        int count = (roleIDs==null) ? 0 : roleIDs.length;
   
        if ( (count == 0) || ((count == 1) && ("".equals( roleIDs[0] ))) ) 
        {
            trace.infoT(methodName, "Could not delete a role -> roleID(s) were empty");
            proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.MISSING_ROLE ) ) );
        }
        else {
            IPrincipalFactory pFactory = UMFactory.getPrincipalFactory();

            int errorCode = 0;
    		for ( int i = 0; i < count; i++ )
    		{
    			String roleID = roleIDs[i].trim();
    			if ( !"".equals( roleID ) )
    			{
                    try
                    {
                        if (pFactory.isPrincipalDeletable( roleID ))
                        {
                            try {
                                // check existence of role before its deletion
                                IRoleFactory rolefactory = UMFactory.getRoleFactory();
                                IRole role = rolefactory.getRole( roleID );
                                trace.infoT( methodName, "Deleting role: " + role.getUniqueName() );
                                rolefactory.deleteRole( roleID );
    				        }
                            catch (NoSuchRoleException exc)
                            {
                                trace.infoT( methodName, "The role doesn't exist anymore: " + roleID);
                            }
                        }
                        else {
                            trace.infoT( methodName, "The role is read-only: " + roleID );
                            if (errorCode < 1) errorCode = 1;
                        }
                    }
                    catch (UMException ex)
                    {
                        errorCode = 2;
                        trace.warningT( methodName, "Could not delete role: " + roleID, ex );
                    }
                }
    		}
            switch (errorCode) {
                case 1: // some roles were read/only
                   proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.ERROR_DELETE ) ) );
                   break;
                case 2: // exception occured during deletion
                   proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.GENERAL_EXCEPTION ) ) );
                   break;
                default: // success
                proxy.setRequestAttribute( InfoBean.beanId, new InfoBean( new Message( RoleAdminMessagesBean.ALL_DELETE_SUCCESS ) ) );
            }
        }

		// HelperClass.getVisibleRoles makes sure te role is visible.
		Iterator sortedRoles;
        try {
            sortedRoles = HelperClass.getVisibleRoles( proxy, searchFilter, true);
        }
        catch (UMException ex)
        {
            trace.errorT(methodName, "No visible roles returned.");
            sortedRoles = new HashSet().iterator();
        }
		proxy.setRequestAttribute( "list_roles", sortedRoles );
        proxy.setRequestAttribute( "searchFilter", searchFilter );

		trace.exiting( methodName, "returned next:" + next );
		return next;
	}

}
