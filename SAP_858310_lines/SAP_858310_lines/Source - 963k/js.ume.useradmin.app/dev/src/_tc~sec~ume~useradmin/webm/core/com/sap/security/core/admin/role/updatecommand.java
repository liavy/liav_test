package com.sap.security.core.admin.role;

import java.util.*;

import com.sap.security.api.*;
import com.sap.security.core.role.imp.PermissionRoles;
import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.core.util.*;
import com.sap.security.core.role.*;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.admin.*;

/**
 *  This class is called when a user wants to save a role. It performs the
 *  creation or modification of the role.
 *
 *@author     Markus Liepold
 */
public class UpdateCommand implements Command
{
	private String next;

	public final static String VERSIONSTRING = "$Id: //shared_tc/com.sapall.security/dev/src/_useradmin/webapp/WEB-INF/java/com/sap/security/core/admin/role/UpdateCommand.java#10 $ from $DateTime: 2004/05/13 17:03:36 $ ($Change: 15940 $)";
	private final static IUMTrace trace = InternalUMFactory.getTrace( VERSIONSTRING );

    // prefix at action ID that defines an unavailable action 
    public final static String UNAV_ACTION_PREFIX = "$UA$"; 

	/**
	 *  Constructor for the UpdateCommand object
	 *
	 *@param  next  Description of Parameter
	 */
	public UpdateCommand( String next )
	{
		this.next = next;
	}


	/**
	 *  This method expects the rolename that is to be updated. It
	 *  then does the task and stores the roles and ID into the session and then
	 *  calls the appropriate jsp file (role_management_main.jsp)
	 *
	 *@param  proxy                     Description of Parameter
	 *@return                           Description of the Returned Value
	 *@exception  RoleCommandException  Description of Exception
	 */
	public String execute( IAccessToLogic proxy ) throws RoleCommandException
	{
		final String methodName = "execute(proxy)";
        trace.entering( methodName );
        trace.infoT( methodName, "request parameters: " + HelperClass.reportRequestParameters( proxy ) );

		String ID = HelperClass.getParam( proxy, "ID" );
		// HelperClass.checkActiveUser throws UMRuntimeException if activeUser is null;
		IUser performer = HelperClass.checkActiveUser( proxy );

		//  HelperClass.checkAccess throws java.security.AccessControlException if failed.
		UserAdminHelper.checkAccess( performer, ID, UserAdminHelper.MODIFY_ROLES );

		try
		{
            String roleID = "";
			// if modifying a role, modifyRole is true, when adding a role, it is false.
			String modifyRole = HelperClass.getParam( proxy, "modifyRole" );
			String roleDescription = HelperClass.getParam( proxy, "roleDescription" );
            String searchFilter = (String)proxy.getSessionAttribute("roleSearchFilter");

			if ( roleDescription.length() > RoleAdminServlet.MAX_ROLE_DESCRIPTION_LENGTH )
			{
				trace.infoT( methodName, "Truncating roleDescription, MaxRoleDescription is " + RoleAdminServlet.MAX_ROLE_DESCRIPTION_LENGTH );
				//truncate the Description field.
				roleDescription = roleDescription.substring( 0, RoleAdminServlet.MAX_ROLE_DESCRIPTION_LENGTH - 1 );
			}

			String rolename = HelperClass.getParam( proxy, "rolename" ).trim();
			if ( rolename.equals( "" ) )
            {
                proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.MISSING_ROLE ) ) );
            }
            else {
                IRoleFactory rolefactory = UMFactory.getRoleFactory();
                IRole role = null;

				if ( modifyRole.equals( "true" ) )
				{
					trace.infoT( methodName, "Modifying role" );
					roleID = util.URLDecoder( HelperClass.getParam( proxy, "roleID" ) );
				}
				else {
					trace.infoT( methodName, "Adding role" );
					try
					{
						role = rolefactory.newRole( rolename );
						role.commit();
						roleID = role.getUniqueID();
						// set searchfilter to the created role name
						searchFilter = role.getUniqueName();
						proxy.setSessionAttribute("roleSearchFilter", searchFilter);
					}
					catch ( RoleAlreadyExistsException fe )
					{
						trace.warningT( methodName, "Role " + rolename + " already exists: ", fe );
						proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.ERROR_ROLE_EXISTS ) ) );
						Iterator allActions = HelperClass.getVisibleActions(null);
						proxy.setRequestAttribute( "allActions", allActions );
						proxy.setRequestAttribute( "assignedActions", null );
						proxy.setRequestAttribute( "ID", ID );
						proxy.setRequestAttribute( "modifyRole", "false" );
						proxy.setRequestAttribute( "roleID", roleID );
						proxy.setRequestAttribute( "rolename", rolename );
                        // if role already exists -> stay on this page
                        trace.exiting( methodName, "returned next: role_management_add.jsp" );
                        return "role_management_add.jsp";
					}
				}

				try
				{
					role = rolefactory.getMutableRole( roleID.trim() );
				}
				catch ( UMException ex )
				{
					trace.errorT( methodName, "getMutableRole(roleID) threw an exception! Where roleID = " + roleID );
					proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.MISSING_ROLE ) ) );
					throw new UMRuntimeException( "getMutableRole(roleID) threw an exception! Where roleID = " + roleID );
				}

				trace.infoT( methodName, "retrieving assigned actions" );
                String actions[] = proxy.getRequestParameterValues( "assignedActions" );
				if ( null != actions )
				{
					IServiceRepository serviceRepository = InternalUMFactory.getServiceRepository();
					if ( null == serviceRepository )
					{
						trace.errorT( methodName, "IServiceRepository was null)" );
						throw new UMRuntimeException( "IServiceRepository was null)" );
					}
					Set actionList = new HashSet( actions.length );
                    Set unavActions = new HashSet();
					for ( int i = 0; i < actions.length; i++ )
					{
						String uniqueID = actions[i];
						if (uniqueID.startsWith(IActionFactory.ACTION_TYPE))
						{
							IAction action = InternalUMFactory.getActionFactory().getAction(uniqueID);
							actionList.add( action );
						}
                        else if (uniqueID.startsWith( UNAV_ACTION_PREFIX ))
                        {
                            // save uniqueID without the identifier for unav. actions
                            unavActions.add( uniqueID.substring(4) );
                        }
                        else {
    						IAction serviceAction = InternalUMFactory.getServiceRepository().getServiceActionById( uniqueID );
    						//Collect only if Actions are visible
                            //TODO serviceRepository.hasAccess needs correct "tp" param.?!
    						if ( serviceRepository.hasAccess( null, serviceAction ) )
    						{
    							actionList.add( serviceAction );
    						}
                        }
					}
					PermissionRoles.setActions( role, actionList, unavActions );
				}
				else {
					PermissionRoles.removeActions( role );
				}
				trace.infoT( methodName, "roleDescription = " + roleDescription );
				if ( !roleDescription.equals( "" ) )
				{
					trace.infoT( methodName, "Setting roleDescription = " + roleDescription );
					role.setDescription( roleDescription );
				}
				role.commit();
				proxy.setRequestAttribute( InfoBean.beanId, new InfoBean( new Message( RoleAdminMessagesBean.UPDATED_SUCCESS ) ) );
				trace.infoT( methodName, "Role was updated successfully" );
			}

            proxy.setRequestAttribute( "list_roles", HelperClass.getVisibleRoles( proxy, searchFilter, true ) );
            proxy.setRequestAttribute( "searchFilter", searchFilter );
			proxy.setRequestAttribute( "ID", ID );
			proxy.setRequestAttribute( "roleID", roleID );
			trace.exiting( methodName, "returned next:" + next );
			return next;
		}
		catch ( UMException fe )
		{
			trace.errorT( methodName, "Error Setting Actions: ", fe );
			proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.EXCEPTION_ROLE_ASSIGNMENT ) ) );
			throw new RoleCommandException( "UpdateCommand:execute " + fe.getMessage() );
		}
	}
}
