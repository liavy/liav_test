package com.sap.security.core.admin.role;

import java.util.MissingResourceException;

import com.sap.security.api.IUser;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.IAccessToLogic;
import com.sap.security.core.admin.ListBean;
import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.core.admin.permissions.UMAdminPermissions;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.ResourceBean;

/**
 *  Description of the Class
 *
 *@author     Fereidoon Samii
 *@created    May 16, 2001
 */
public class PageNavigationCommand implements Command
{
	private String next;

	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/role/PageNavigationCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);


	/**
	 *  Constructor for the PageNavigationCommand object
	 *
	 *@param  next  Description of Parameter
	 */
	public PageNavigationCommand( String next )
	{
		this.next = next;
	}


	/**
	 *  Constructor for the PageNavigationCommand object
	 *
	 *@param  proxy  Description of Parameter
	 *@param  key    Description of Parameter
	 */
	public PageNavigationCommand( IAccessToLogic proxy, String key )
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
				trace.errorT( "PageNavigationCommand(proxy,String)", "Resource" + key + " not found" );
				next = "error.jsp";
			}
		}
		catch ( MissingResourceException e )
		{
			trace.errorT( "PageNavigationCommand(proxy,String)", "Resource" + key + " not found", e );
			proxy.setRequestAttribute( "javax.servlet.jsp.jspException", e );
			next = "error.jsp";
		}
	}


	/**
	 *  This Method expects a ID such as company name which is needed to find out
	 *  the roleFactory. It also expects the rolename(s) that is used to search
	 *  which users of the company have in common. Next it stores the users with
	 *  common roles, the remaining users, the rolenames and the ID into the
	 *  session and then calls the appropriate jsp file (role_assignment.jsp)
	 *
	 *@param  proxy                     Description of Parameter
	 *@return                           Description of the Returned Value
	 *@exception  RoleCommandException  Description of Exception
	 */
	public String execute( IAccessToLogic proxy  ) throws RoleCommandException
	{
        final String methodName = "execute(proxy)";
		trace.entering(methodName);
        if (trace.beInfo())
		  trace.infoT( methodName, "request parameters: " +HelperClass.reportRequestParameters(proxy) );

        String principal = HelperClass.setPrincipalRequestParam(proxy);
		String rolenamesReport = HelperClass.getParam( proxy, "rolenamesReport" );
        String roleDescription = HelperClass.getParam( proxy, "roleDescription" );
        String roleIDs = HelperClass.getParam( proxy, "roleIDs" );
        String memberSF = HelperClass.getParam( proxy, "memberSF");
        String searchFilter = HelperClass.getParam( proxy, "searchFilter");

		// HelperClass.checkActiveUser throws UMRuntimeException if activeUser is null;
		IUser performer = HelperClass.checkActiveUser( proxy );
        // checkPermission throws java.security.AccessControlException if failed.
        performer.checkPermission( new UMAdminPermissions(UserAdminHelper.VIEW_GROUPS) );
        
        proxy.setRequestAttribute( ListBean.beanId, HelperClass.getListBean(proxy,principal));
		proxy.setRequestAttribute( "rolenamesReport", rolenamesReport );
		proxy.setRequestAttribute( "roleIDs", roleIDs );
		proxy.setRequestAttribute( "roleDescription", roleDescription );
        proxy.setRequestAttribute( "memberSF", memberSF);
        proxy.setRequestAttribute( "searchFilter", searchFilter);

		String searching = HelperClass.getParam(proxy, "searching");
        if (searching.equalsIgnoreCase("true"))
        {
            next = "role_search_result.jsp";
        }
        else {
            next = "role_assignment.jsp";
        }
        trace.exiting( methodName, "returned next:" + next );
        return next;
	}

}
