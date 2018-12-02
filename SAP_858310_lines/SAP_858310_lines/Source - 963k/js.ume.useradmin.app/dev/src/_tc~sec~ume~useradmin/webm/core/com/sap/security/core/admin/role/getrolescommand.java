package com.sap.security.core.admin.role;

import java.util.HashSet;
import java.util.Iterator;
import java.util.MissingResourceException;

import com.sap.security.api.IUser;
import com.sap.security.api.UMException;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.IAccessToLogic;
import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.core.admin.permissions.UMAdminPermissions;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.InfoBean;
import com.sap.security.core.util.Message;
import com.sap.security.core.util.ResourceBean;

/**
 *  Description of the Class
 *
 *@author     Fereidoon Samii
 *@created    May 16, 2001
 */
public class GetRolesCommand implements Command
{
	private String next;

	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/role/GetRolesCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);


	/**
	 *  Constructor for the GetRolesCommand object
	 *
	 *@param  next  Description of Parameter
	 */
	public GetRolesCommand( String next )
	{
		this.next = next;
	}


	/**
	 *  Constructor for the GetRolesCommand object
	 *
	 *@param  proxy  Description of Parameter
	 *@param  key    Description of Parameter
	 */
	public GetRolesCommand(  IAccessToLogic proxy , String key )
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
				trace.infoT( "GetRolesCommand(proxy,key)", "resource" + key + " not found" );
				next = "error.jsp";
			}
		}
		catch ( MissingResourceException e )
		{
			trace.errorT( "GetRolesCommand(proxy,key)", "resource" + key + " not found", e );
			proxy.setRequestAttribute( "javax.servlet.jsp.jspException", e );
			next = "error.jsp";
		}
	}


	/**
	 *  This Method expects a ID such as company ID which is needed to find out the
	 *  roleFactory. Next it stores all the roles and the ID into the session abd
	 *  then calls the appropriate jsp file (role_management_main.jsp)
	 *
	 *@param  proxy                     Description of Parameter
	 *@return                           Description of the Returned Value
	 *@exception  RoleCommandException  Description of Exception
	 */
	public String execute( IAccessToLogic proxy ) throws RoleCommandException
	{
        final String methodName = "execute(IAccessToLogic)";
        trace.entering(methodName);
        if (trace.beInfo())
            trace.infoT( methodName, "request parameters: " + HelperClass.reportRequestParameters( proxy ) );

		// HelperClass.checkActiveUser throws UMRuntimeException if activeUser is null;
		IUser performer = HelperClass.checkActiveUser( proxy );

        String redirectURL = HelperClass.getParam( proxy, "redirectURL" );
        String searchFilter = HelperClass.getParam( proxy, "searchFilter" );
        boolean newSearchFilter = "true".equals( HelperClass.getParam(proxy, "newSearchFilter"))?true:false;
        String roleSF = (String)proxy.getSessionAttribute("roleSearchFilter");

        if (newSearchFilter)
        {
            proxy.setSessionAttribute( "roleSearchFilter", searchFilter );
        }
        else if (roleSF != null)
        {
            searchFilter = roleSF;
        }

        // checkPermission throws java.security.AccessControlException if failed.
        performer.checkPermission( new UMAdminPermissions(UserAdminHelper.VIEW_ROLES) );
        Iterator sortedObjects;
        try
        {
            if ((newSearchFilter) || (roleSF != null))
            {
                trace.debugT(methodName, "retrieving roles");
                sortedObjects = HelperClass.getVisibleRoles( proxy, searchFilter.trim(), true );
                next = "role_management_main.jsp";
            }
            else {
                sortedObjects = new HashSet().iterator();
                proxy.setRequestAttribute( InfoBean.beanId, new InfoBean( new Message( RoleAdminMessagesBean.NO_SEARCH_PERFORMED ) ) );
            }
        }
        catch (UMException ex)
        {
            trace.errorT(methodName, "No visible roles returned.", ex);
            sortedObjects = new HashSet().iterator();
        }
		proxy.setRequestAttribute( "list_roles", sortedObjects );
		proxy.setRequestAttribute( "redirectURL", redirectURL );
        proxy.setRequestAttribute( "searchFilter", searchFilter );

		trace.exiting(methodName, "returned next:" + next );
		return next;
	}
}
