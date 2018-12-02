package com.sap.security.core.admin.role;

import com.sap.security.api.*;
import com.sap.security.core.admin.*;
import com.sap.security.core.util.*;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.IUMTrace;

import java.io.IOException;
import java.net.*;
import java.util.*;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;

/**
 *  This servlet is starting point for the Administration
 *
 *@author     Fereidoon Samii
 *@created    May 16, 2001
 *@version    1.0 01.17.2001
 */

public class RoleAdminServlet extends HttpServlet
{
	private transient IAccessToLogic proxy;

	public final static String alias = "/useradmin/roleAdmin";
	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/role/RoleAdminServlet.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
	public final static String ROLE_MANAGEMENT_MAIN = "role-management-main";
	public final static String ROLE_MANAGEMENT_ADD = "role-management-add";
	public final static String ROLE_MANAGEMENT_DOUPDATE = "role-management-doupdate";
	public final static String ROLE_MANAGEMENT_DELETE = "role-management-delete";
	public final static String ROLE_MANAGEMENT_MODIFY = "role-management-modify";
	public final static String ROLE_ASSIGNMENT_ASSIGN_USERS = "role-assignment-assign-users";
	public final static String ROLE_ASSIGNMENT = "role-assignment";
	public final static String ROLE_ASSIGNMENT_REMOVE_USERS = "role-assignment-remove-users";
	public final static String ROLE_ASSIGNMENT_ADD_USERS = "role-assignment-add-users";
	public final static String ROLE_MANAGEMENT_RESTORE_ROLES = "role-management-restore-roles";
	public final static String ROLE_ASSIGNMENT_PAGE_NAVIGATION = "role-assignment-page-navigation";
	public final static String ROLE_ASSIGNMENT_SEARCH_GROUPS = "role-assignment-search-groups";
	public final static String ROLE_ASSIGNMENT_ADD_GROUPS = "role-assignment-add-groups";
	public final static String ROLE_ABORT = "abort";
	public final static String UM_ABORT = "um-abort";
	public final static String ROLE_SEARCH_ABORT = "role-abort";
    public final static String ROLE_EXPORT = "role-export";

    public static final int FULL_USAGE = 1;
    public static final int PARTLY_USAGE = 2;
    public static final int NO_USAGE = 3;

    // max. length for role name and description
    public final static int MAX_ROLE_NAME_LENGTH = 40;
    public final static int MAX_ROLE_DESCRIPTION_LENGTH = 255;

	// common parameters
	public final static String COMMAND = "cmd";
	protected final static String ASSIGNED_USERS = "assignedUsers";
	protected final static String ASSIGNED_GROUPS = "assignedGroups";
	protected final static int GROUP_RETRIEVAL = 0;
	protected final static int USER_RETRIEVAL = 1;
	private final static IUMTrace trace = InternalUMFactory.getTrace( VERSIONSTRING );
	private static HashMap commands;

	private final static String roledir = "/role/";
	private final static String loggedInUser = "loggedInUser";
	private final static String inPortal = "inPortal";

	/**
	 *  This Servlet retrieves the command handler class based on the command
	 *  identifier, next it calls the commands handler's execute method. Last it
	 *  directs the request to the destination jsp file. Creation date: (17.01.2001
	 *  11:57:02)
	 *
	 *@param  req                     Description of Parameter
	 *@param  res                     Description of Parameter
	 *@exception  ServletException    Description of Exception
	 *@exception  IOException         Description of Exception
	 *@exception  UMRuntimeException  Description of Exception
	 */
	public void doGet( HttpServletRequest req, HttpServletResponse res )
        throws UMRuntimeException, ServletException, IOException
	{
        final String methodName = "doGet(req,res)";
        trace.entering( methodName );

		String next = null;
		String redirectURL = null;

		try	{
			util.checkNewUI(proxy);
            proxy = new ServletAccessToLogic( req, res );
            proxy.setSessionAttribute(UserAdminLogic.currentAction, ROLE_MANAGEMENT_MAIN);
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
				// initialize role beans, if not already done
                if ( null == proxy.getSessionAttribute(RoleAdminLocaleBean.beanId))
                {
                    trace.debugT( methodName, "Initializing role beans");
                    Locale locale = proxy.getLocale();
					proxy.setSessionAttribute( RoleAdminLocaleBean.beanId, new RoleAdminLocaleBean( locale ) );
					proxy.setSessionAttribute( RoleAdminMessagesBean.beanId, new RoleAdminMessagesBean( locale ) );
				}
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
		catch ( Exception e )
		{
			if ( ( e instanceof RoleCommandException ) || ( e instanceof UMException ) )
			{
				String result = HelperClass.makeErrorReport( proxy, e );
				trace.errorT( methodName, result, e );
				next = UserAdminCommonLogic.errorPage;
			}
			else if ( e instanceof java.security.AccessControlException )
			{
				try {
					UserAdminCommonLogic.gotoNoAccess(proxy, new String[]{});
				} catch (AccessToLogicException ex) {
				  trace.debugT(methodName, ex);
				}
				return;
			}
			else {
				trace.errorT( methodName, "Unhandled exception in RoleAdmin", e );
				throw new UMRuntimeException( e );
			}
		}
		if ( shouldThisRedirect( req ) )
		{
			redirectURL = URLDecoder.decode( HelperClass.getParam( proxy, "redirectURL" ) );

			//String invokedURL = req.getRequestURI();
			//if (req.getQueryString() != null) {
			//invokedURL += "?" + req.getQueryString();
			trace.debugT( methodName, "Redirecting to : " + redirectURL );
			proxy.setRequestAttribute( "redirectURL", "" );
			proxy.setSessionAttribute( "ignoreRedirect", "false" );
			proxy.sendRedirect( redirectURL );
		}
		else {
            if (UserAdminCommonLogic.errorPage.equals( next ))
            {
                try {
                    proxy.gotoPage( next );
                }
                catch (AccessToLogicException exc)
                {
                    trace.errorT( methodName, "Could not forward to error page!", exc);
                }
            }
            else {
    			String dest = roledir + next;
                RequestDispatcher rd = getServletContext().getRequestDispatcher( dest );
    			trace.debugT( methodName, "Forwarding to page: " + dest );
    			rd.forward( req, res );
            }
		}
	}


	/**
	 *  see: {@link #doGet(req, res)}
	 *
	 *@param  req                     Description of Parameter
	 *@param  res                     Description of Parameter
	 *@exception  ServletException    Description of Exception
	 *@exception  IOException         Description of Exception
	 *@exception  UMRuntimeException  Description of Exception
	 */
	public void doPost( HttpServletRequest req, HttpServletResponse res )
			 throws UMRuntimeException, ServletException, IOException
	{
		doGet( req, res );
	}


	/**
	 *  Gets the LocaleBean attribute of the RoleAdminServlet object
	 *
	 *@param  session  Description of Parameter
	 *@return          The LocaleBean value
	 */
	protected ResourceBean getLocaleBean( HttpSession session )
	{
		ResourceBean localeBean = ( ResourceBean ) session.getAttribute( RoleAdminLocaleBean.beanId );
        if (trace.beDebug())
    		trace.debugT( "getLocaleBean(session)", "locale retrieved from the proxy is: ",
				new Object[]{localeBean == null ? "null" : localeBean.getLocale().getDisplayName()} );
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

        proxy.setSessionAttribute( UserAdminLocaleBean.beanId, UserAdminLocaleBean.getInstance(locale));
        proxy.setSessionAttribute( UserAdminMessagesBean.beanId, UserAdminMessagesBean.getInstance(locale));
 		proxy.setSessionAttribute( LanguagesBean.beanId, LanguagesBean.getInstance( locale ) );
		proxy.setSessionAttribute( CountriesBean.beanId, CountriesBean.getInstance( locale ) );
		proxy.setSessionAttribute( TimeZonesBean.beanId, TimeZonesBean.getInstance( locale ) );

		trace.debugT( methodName, "finished session objects initialization" );
	}


	/**
	 *  Description of the Method
	 *
	 *@param  req  Description of Parameter
	 *@return      Description of the Returned Value
	 */
	private boolean shouldThisRedirect( HttpServletRequest req )
	{
		final String methodName = "shouldThisRedirect(req)";
		trace.entering( methodName );

		HttpSession session = req.getSession();
		String redirectURL = HelperClass.getParam( proxy, "redirectURL" );
		String ignoreRedirect = ( String ) session.getAttribute( "ignoreRedirect" );
        if (trace.beDebug())
        {
            trace.debugT( methodName, "redirectURL = " + redirectURL );
    		trace.debugT( methodName, "ignoreRedirect = " + ignoreRedirect );
        }

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
		trace.entering(methodName, new Object[]{cmd});

        if ( null == util.checkEmpty(cmd) )
		{
			trace.warningT( methodName, "command passed was null, default command = role-management-main is used." );
			cmd = ROLE_MANAGEMENT_MAIN;
		}
		cmd = cmd.toLowerCase();

		if ( commands.containsKey( cmd ) )
		{
			trace.debugT( methodName, "cmd is " + cmd );
			return ( Command ) commands.get( cmd );
		}
		else {
			trace.errorT( methodName, "Invalid Command Identifier" );
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

		commands.put( ROLE_MANAGEMENT_MAIN, new GetRolesCommand( "role_management_main.jsp" ) );
		commands.put( ROLE_MANAGEMENT_RESTORE_ROLES, new GetRolesCommand( "role_management_main.jsp" ) );
		commands.put( ROLE_MANAGEMENT_ADD, new AddRoleCommand( "role_management_add.jsp" ) );
		commands.put( ROLE_MANAGEMENT_DOUPDATE, new UpdateCommand( "role_management_main.jsp" ) );
		commands.put( ROLE_MANAGEMENT_DELETE, new DeleteCommand( "role_management_main.jsp" ) );
		commands.put( ROLE_MANAGEMENT_MODIFY, new ModifyRoleCommand( "role_management_add.jsp" ) );
		commands.put( ROLE_ASSIGNMENT_ADD_USERS, new AddUsersCommand( "role_assignment.jsp" ) );
		commands.put( ROLE_ASSIGNMENT, new AssignRolesCommand( "role_assignment.jsp" ) );
		commands.put( ROLE_ASSIGNMENT_PAGE_NAVIGATION, new PageNavigationCommand( "role_assignment.jsp" ) );
		commands.put( ROLE_ASSIGNMENT_REMOVE_USERS, new RemoveUsersCommand( "role_assignment.jsp" ) );
		commands.put( ROLE_ASSIGNMENT_SEARCH_GROUPS, new GroupSearchCommand( "role_search_result.jsp" ) );
		commands.put( ROLE_ABORT, new GetRolesCommand( "role_management_main.jsp" ) );
		commands.put( UM_ABORT, new CancelCommand( "role_management_main.jsp" ) );
		commands.put( ROLE_ASSIGNMENT_ADD_GROUPS, new AddUsersCommand( "role_assignment.jsp" ) );
		commands.put( ROLE_SEARCH_ABORT, new GetRolesCommand( "role_management_main.jsp" ) );
        commands.put( ROLE_EXPORT, new ExportRolesCommand() );
	}

}
