/*
 *  Copyright 2001
 *
 *  SAPMarkets, Inc.
 *  All rights reserved
 *  Palo Alto, California, 94304, U.S.A.
 *
 */
package com.sap.security.core.admin.group;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.ServletException;

import com.sap.security.api.IUser;
import com.sap.security.api.UMRuntimeException;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.AccessToLogicException;
import com.sap.security.core.admin.CountriesBean;
import com.sap.security.core.admin.IAccessToLogic;
import com.sap.security.core.admin.LanguagesBean;
import com.sap.security.core.admin.ServletAccessToLogic;
import com.sap.security.core.admin.TimeZonesBean;
import com.sap.security.core.admin.UserAdminCommonLogic;
import com.sap.security.core.admin.UserAdminLocaleBean;
import com.sap.security.core.admin.UserAdminLogic;
import com.sap.security.core.admin.UserAdminMessagesBean;
import com.sap.security.core.admin.util;
import com.sap.security.core.admin.role.Command;
import com.sap.security.core.admin.role.HelperClass;
import com.sap.security.core.admin.role.RoleAdminLocaleBean;
import com.sap.security.core.admin.role.RoleAdminMessagesBean;
import com.sap.security.core.admin.role.RoleCommandException;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.ResourceBean;


public class GroupAdminLogic
{
    public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/group/GroupAdminLogic.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private final static IUMTrace trace = InternalUMFactory.getTrace( VERSIONSTRING );

	private IAccessToLogic proxy;
	public final static String GROUP_ASSIGNMENT_PAGE_NAVIGATION = "group-assignment-page-navigation";
	public final static String GROUP_MANAGEMENT_MAIN = "group-management-main";
	public final static String GROUP_MANAGEMENT_ADD = "group-management-add";
	public final static String GROUP_MANAGEMENT_MODIFY = "group-management-modify";
	public final static String GROUP_MANAGEMENT_DOUPDATE = "group-management-doupdate";
	public final static String GROUP_MANAGEMENT_DELETE = "group-management-delete";
	public final static String GROUP_ASSIGNMENT = "group-assignment";
	public final static String GROUP_ASSIGNMENT_ADD_USERS = "group-assignment-add-users";
	public final static String GROUP_ASSIGNMENT_ADD_GROUPS = "group-assignment-add-groups";
	public final static String GROUP_ASSIGNMENT_REMOVE_USERS = "group-assignment-remove-users";
	public final static String GROUP_ASSIGNMENT_SEARCH_GROUPS = "group-assignment-search-groups";
    public final static String GROUP_EXPORT = "group-export";

    // group admin pages
    protected final static String groupAssignmentPage   = "groupAssignmentPage";
    protected final static String groupManagementPage   = "groupManagementPage";
    protected final static String groupManagementAddPage = "groupManagementAddPage";
    protected final static String groupSearchResultPage = "groupSearchResultPage";

    // max. length for group name and description
    public final static int MAX_GROUP_NAME_LENGTH = 40;
    public final static int MAX_GROUP_DESCRIPTION_LENGTH = 255;

	public final static String GROUP_ABORT = "group-abort";
	public final static String UM_ABORT = "um-abort";
	public final static String COMMAND = "cmd";
	protected final static String ASSIGEN_MEMBERS = "assignedMembers";

	private final static String loggedInUser = "loggedInUser";
	private final static String inPortal = "inPortal";

    public final static String alias = "/useradmin/groupAdmin";
    public final static String servlet_name = "/groupAdmin";

    public static String component_name = null;
    private static HashMap commands;


    /**
     * Constructor
     */
    public GroupAdminLogic(IAccessToLogic _proxy)
    {
        this.proxy = _proxy;
        component_name = proxy.getContextURI("com.sap.portal.usermanagement.admin.GroupAdmin");
    }


	/**
	 *  This Servlet retrieves the command handler class based on the command
	 *  identifier, next it calls the commands handler's execute method. Last it
	 *  directs the request to the destination jsp file. Creation date: (17.01.2001
	 *  11:57:02)
	 *
	 *@exception  ServletException         Description of Exception
	 *@exception  IOException              Description of Exception
	 *@exception  UMRuntimeException       Description of Exception
     *@exception  AccessToLogicException   Description of Exception
	 */
	public void doGet() throws ServletException, IOException, AccessToLogicException
	{
		final String methodName = "doGet()";
		trace.entering( methodName );

		util.checkNewUI(proxy);

        String next = null;

		try
		{
            proxy.setSessionAttribute(UserAdminLogic.currentAction, GROUP_MANAGEMENT_MAIN);
			UserAdminCommonLogic.setResponse(proxy);
			// initialize common beans if not already done
			if ( null == proxy.getSessionAttribute( loggedInUser ) || proxy.isSessionNew() )
			{
				IUser performer = proxy.getActiveUser();
				if ( null == performer )
				{
					return;
				}
				proxy.setSessionAttribute( loggedInUser, performer );
				Locale locale = proxy.getLocale();
				proxy.setSessionAttribute( "currentLocale", locale );
				inits( locale );
			}
			else {
				// Initialize role beans
				// Has to be done everytime, because locale could have changed
				trace.debugT( methodName, "Initializing role beans" );
				Locale locale = proxy.getLocale();
				proxy.setSessionAttribute( RoleAdminLocaleBean.beanId, new RoleAdminLocaleBean( locale ) );
				proxy.setSessionAttribute( RoleAdminMessagesBean.beanId, new RoleAdminMessagesBean( locale ) );
			}
			proxy.setSessionAttribute( "ignoreRedirect", "true" );

            if (proxy.getRequestParameter(UserAdminLogic.performSearchResultNavigateAction) != null)
            {
                UserAdminLogic ual = new UserAdminLogic(proxy);
                ual.performSearchResultNavigate();
                return;
            }
            trace.infoT( methodName, "Looking up command" );
			Command cmd = lookupCommand( proxy.getRequestParameter( "cmd" ) );
			next = cmd.execute( proxy );
			trace.infoT( methodName, "next = " + next );
		}
        catch ( java.security.AccessControlException acex )
        {
            UserAdminCommonLogic.gotoNoAccess(proxy, new String[]{});
            return;
        }
		catch ( Exception e )
		{
			String result = HelperClass.makeErrorReport( proxy, e );
			trace.errorT( methodName, result, e );
			next = UserAdminCommonLogic.errorPage;
		}
		if ( shouldThisRedirect() )
		{
            String redirectURL = URLDecoder.decode( HelperClass.getParam( proxy, "redirectURL" ) );

			trace.debugT( methodName, "Redirecting to : " + redirectURL );
			proxy.setRequestAttribute( "redirectURL", "" );
			proxy.setSessionAttribute( "ignoreRedirect", "false" );
			proxy.sendRedirect( redirectURL );
		}
		else
		{
            proxy.gotoPage( next );
		}
	}


	/**
	 *  see: {@link #doGet()}
	 *
	 *@exception  ServletException    Description of Exception
	 *@exception  IOException         Description of Exception
	 *@exception  UMRuntimeException  Description of Exception
     *@exception  AccessToLogicException   Description of Exception
	 */
	public void doPost()
			 throws UMRuntimeException, ServletException, IOException, AccessToLogicException
	{
		doGet();
	}


	/**
	 *  Gets the LocaleBean attribute retrieved from the proxy
	 *
	 *@return          The LocaleBean value
	 */
	protected ResourceBean getLocaleBean()
	{
		final String methodName = "getLocaleBean()";
		trace.entering( methodName );

		ResourceBean localeBean = ( ResourceBean ) proxy.getSessionAttribute( RoleAdminLocaleBean.beanId );
        if (trace.beDebug()) {
    		trace.debugT( methodName, "locale retrieved from the proxy is: ",
				new Object[]{localeBean == null ? "null" : localeBean.getLocale().getDisplayName()} );
        }
		return localeBean;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  locale         Description of Parameter
	 *@exception  Exception  Description of Exception
	 */
	private void initBeans( Locale locale ) throws Exception
	{
		final String methodName = "initBeans(Locale)";
		// getCurrentSession or create a new one
		trace.debugT( methodName, "locale and sessionid", new Object[]{locale, proxy.getSessionId()} );

		// initializing session bean objects
		proxy.setSessionAttribute( RoleAdminLocaleBean.beanId, new RoleAdminLocaleBean( locale ) );
		proxy.setSessionAttribute( RoleAdminMessagesBean.beanId, new RoleAdminMessagesBean( locale ) );

		proxy.setSessionAttribute( UserAdminLocaleBean.beanId, UserAdminLocaleBean.getInstance(locale) );
		proxy.setSessionAttribute( UserAdminMessagesBean.beanId, UserAdminMessagesBean.getInstance(locale) );
		proxy.setSessionAttribute( LanguagesBean.beanId, LanguagesBean.getInstance( locale ) );
		proxy.setSessionAttribute( CountriesBean.beanId, CountriesBean.getInstance( locale ) );
		proxy.setSessionAttribute( TimeZonesBean.beanId, TimeZonesBean.getInstance( locale ) );

		trace.debugT( methodName, "finished session objects initialization" );
	}


	/**
	 *  Description of the Method
	 *
	 *@return      Description of the Returned Value
	 */
	private boolean shouldThisRedirect()
	{
		final String methodName = "shouldThisRedirect()";
		trace.entering( methodName );

		String redirectURL = HelperClass.getParam( proxy, "redirectURL" );
		String ignoreRedirect = ( String ) proxy.getSessionAttribute( "ignoreRedirect" );
        if (trace.beDebug())
            trace.debugT( methodName, "redirectURL = " + redirectURL + ", ignoreRedirect = " + ignoreRedirect );

		if ( (null == ignoreRedirect) || (redirectURL.equals( "" )) || (ignoreRedirect.equalsIgnoreCase( "true" )) )
		{
			return false;
		}
		else {
			return true;
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  cmd                       Description of Parameter
	 *@return                           Description of the Returned Value
	 *@exception  RoleCommandException  Description of Exception
	 */
	private Command lookupCommand( String cmd ) throws RoleCommandException
	{
		final String methodName = "lookupCommand(String)";
		trace.entering( methodName );
		trace.debugT( methodName, "Checking for cmd = " + cmd );

		if ( ( cmd == null ) || cmd.equals( "" ) )
		{
			trace.warningT( methodName, "command passed was null, default command = group-management-main is used." );
			cmd = "group-management-main";
		}
		String command = cmd.toLowerCase();

		if ( commands.containsKey( command ) )
		{
			trace.debugT( methodName, "cmd is " + command );
			return ( Command ) commands.get( command );
		}
		else {
			trace.errorT( methodName, "Invalid Command Identifier: " + command );
			throw new RoleCommandException( "Invalid Command Identifier" );
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  locale         Description of Parameter
	 *@exception  Exception  Description of Exception
	 */
	private void inits( Locale locale ) throws Exception
	{
		if ( proxy instanceof ServletAccessToLogic )
		{
			proxy.setSessionAttribute( inPortal, Boolean.FALSE );
		}
		else {
			proxy.setSessionAttribute( inPortal, Boolean.TRUE );
		}
		initBeans( locale );
	}


	static
    {
		commands = new HashMap();
		//  Each command is a combination of command identifier, command handler
		//  class, and a destination screen (JSP file).

		commands.put( GROUP_ASSIGNMENT_PAGE_NAVIGATION, new GroupPageNavigationCommand( groupAssignmentPage ) );
		commands.put( GROUP_MANAGEMENT_MAIN,            new GetGroupsCommand( groupManagementPage ) );
		commands.put( GROUP_MANAGEMENT_ADD,             new AddGroupCommand( groupManagementAddPage ) );
		commands.put( GROUP_MANAGEMENT_MODIFY,          new ModifyGroupCommand( groupManagementAddPage ) );
		commands.put( GROUP_MANAGEMENT_DOUPDATE,        new UpdateGroupCommand( groupManagementPage ) );
		commands.put( GROUP_MANAGEMENT_DELETE,          new DeleteGroupCommand( groupManagementPage ) );
		commands.put( GROUP_ASSIGNMENT,                 new AssignGroupsCommand( groupAssignmentPage ) );
		commands.put( GROUP_ASSIGNMENT_ADD_USERS,       new AddUsersGroupCommand( groupAssignmentPage ) );
		commands.put( GROUP_ABORT,                      new GetGroupsCommand( groupManagementPage ) );
		commands.put( GROUP_ASSIGNMENT_REMOVE_USERS,    new RemoveGroupUsersCommand( groupAssignmentPage ) );
		commands.put( UM_ABORT,                         new GroupCancelCommand( groupManagementPage ) );
		commands.put( GROUP_ASSIGNMENT_SEARCH_GROUPS,   new SearchCommand( groupSearchResultPage ) );
		commands.put( GROUP_ASSIGNMENT_ADD_GROUPS,      new AddUsersGroupCommand( groupAssignmentPage ) );
        commands.put( GROUP_EXPORT,                     new ExportGroupsCommand() );
	}

}
