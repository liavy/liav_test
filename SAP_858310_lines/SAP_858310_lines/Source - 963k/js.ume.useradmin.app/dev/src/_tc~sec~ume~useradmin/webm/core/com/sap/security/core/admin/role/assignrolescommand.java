package com.sap.security.core.admin.role;

import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Set;

import com.sap.security.api.IPrincipal;
import com.sap.security.api.IRoleFactory;
import com.sap.security.api.IUser;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.IAccessToLogic;
import com.sap.security.core.admin.ListBean;
import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.core.admin.util;
import com.sap.security.core.admin.permissions.UMAdminPermissions;
import com.sap.security.core.util.ErrorBean;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.Message;
import com.sap.security.core.util.ResourceBean;

/**
 *  This class is called before the role assignment screen is shown.
 *  It gets all common members of the selected role(s): 
 *
 *@author     Markus Liepold
 */
public class AssignRolesCommand implements Command
{
	private String next;

	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/role/AssignRolesCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);

	/**
	 *  Constructor for the AssignRolesCommand object
	 *
	 *@param  next  Description of Parameter
	 */
	public AssignRolesCommand( String next )
	{
		this.next = next;
	}


	/**
	 *  Constructor for the AssignRolesCommand object
	 *
	 *@param  proxy  Description of Parameter
	 *@param  key    Description of Parameter
	 */
	public AssignRolesCommand( IAccessToLogic proxy , String key )
	{
        final String methodName = "AssignRolesCommand(proxy,key)";
		try
		{
			ResourceBean localeBean = ( ResourceBean ) proxy.getSessionAttribute( RoleAdminLocaleBean.beanId );
			if ( localeBean != null )
			{
				String next = localeBean.getPage( key );
				this.next = next;
			}
			else {
				trace.errorT( methodName, "Resource" + key + " not found" );
				next = "error.jsp";
			}
		}
		catch ( MissingResourceException e )
		{
			trace.errorT( methodName, "Resource" + key + " not found", e );
			proxy.setRequestAttribute( "javax.servlet.jsp.jspException", e );
			next = "error.jsp";
		}
	}


	/**
	 *  This Method expects a ID such as company name which is needed to find out
	 *  the roleFactory. It also expects the rolename(s) that is used to search
	 *  which users of the company have in common. Next it stores the users with
	 *  common roles, the remaining users, the rolenames and the ID into the
	 *  session and then calls the appropriate jsp file
	 *  (role_assignment_role_user.jsp)
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
            trace.infoT( methodName, "request parameters: " + HelperClass.reportRequestParameters(proxy) );

		//contains internalRolenames(i.e roleIDs)
		String rolenames[] = util.getUniqueIDs( proxy,  "rolename" );
        if (rolenames == null)
        {
            rolenames = (String[]) proxy.getSessionAttribute( "_roleIDs" );
        }
        String memberSF = HelperClass.getParam( proxy, "memberSF");
        String principal = HelperClass.setPrincipalRequestParam(proxy);

		// HelperClass.checkActiveUser throws UMRuntimeException if activeUser is null;
		IUser performer = HelperClass.checkActiveUser( proxy );
        // checkPermission throws java.security.AccessControlException if failed.
        performer.checkPermission( new UMAdminPermissions(UserAdminHelper.VIEW_ROLES) );

		Set memberList = new HashSet();
        IRoleFactory roleFactory = UMFactory.getRoleFactory();
        String rolenamesReport = "";
        String rolenameIDs = "";
        try
		{
            if ( null != rolenames )
            {
                rolenamesReport = HelperClass.makeRoleNames( roleFactory, rolenames );
                rolenameIDs = HelperClass.makeIDs( rolenames );
                if (trace.beInfo()) 
                {
                    trace.infoT( methodName, "role names: " + rolenamesReport );
                    trace.infoT( methodName, "role IDs: " + rolenameIDs );
                }
            }
            RoleAssignmentFactory raFactory = RoleAssignmentFactory.getInstance();

            if (principal.equalsIgnoreCase("user"))
            {
                memberList = raFactory.getMembersWithTheseRoles( proxy, performer, rolenames, RoleAssignmentFactory.USER_RETRIEVAL, memberSF );
            }
            else if (principal.equalsIgnoreCase("group"))
            {
                memberList = raFactory.getMembersWithTheseRoles( proxy, performer, rolenames, RoleAssignmentFactory.GROUP_RETRIEVAL, memberSF );
            }
            else {
                trace.infoT(methodName," Not supported principal" );
            }
 		}
		catch ( UMException e )
		{
			trace.errorT( methodName, e );
			proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.EXCEPTION_SEARCH_FAILED ) ) );
			throw new RoleCommandException( "AssignRolesCommand:execute " + e.getMessage() );
		}

        IPrincipal[] assignedMembers = null;
        if ((memberList != null) && (!memberList.isEmpty() ))
		{
            int size = memberList.size();
			assignedMembers = new IPrincipal[size];
            trace.infoT(methodName, size + " members found:");
			memberList.toArray( assignedMembers );
		}

        ListBean list = HelperClass.setUpListBean(proxy, principal, assignedMembers);
        proxy.setRequestAttribute( ListBean.beanId, list);
		proxy.setRequestAttribute( RoleAdminServlet.ASSIGNED_USERS, assignedMembers );
		proxy.setRequestAttribute( "rolenamesReport", rolenamesReport );
		proxy.setRequestAttribute( "roleIDs", rolenameIDs );
        proxy.setSessionAttribute( "_roleIDs", rolenames);
		proxy.setRequestAttribute( "roleDescription", HelperClass.getRoleDesciption(roleFactory, rolenameIDs) );
        proxy.setRequestAttribute( "memberSF", memberSF);

		trace.exiting( methodName, "returned next:" + next );
		return next;
	}
}
