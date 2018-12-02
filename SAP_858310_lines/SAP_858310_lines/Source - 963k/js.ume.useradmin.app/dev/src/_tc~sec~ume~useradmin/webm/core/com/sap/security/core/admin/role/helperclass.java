package com.sap.security.core.admin.role;

import com.sap.security.api.*;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.persistence.datasource.imp.CompanyGroups;
import com.sap.security.core.admin.*;
import com.sap.security.core.role.*;
import com.sap.security.core.role.imp.PermissionRoles;
import com.sap.security.core.util.*;
import com.sapmarkets.tpd.master.PartnerID;
import com.sapmarkets.tpd.util.TpdException;
import com.sapmarkets.tpd.master.TradingPartnerInterface;
import com.sapmarkets.tpd.master.TradingPartnerDirectoryInterface;

import java.util.*;
import javax.servlet.http.*;

/**
 *  Description of the Class
 *
 *@author     Fereidoon Samii
 *@created    May 16, 2001
 */
public class HelperClass
{
	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/role/HelperClass.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);


	/**
	 *  Gets the Checked attribute of the HelperClass object
	 *
	 *@param  request  Description of Parameter
	 *@param  param    Description of Parameter
	 *@param  test     Description of Parameter
	 *@return          The Checked value
	 */
	protected String isChecked( IAccessToLogic proxy, String param, String test )
	{
		if ( requestContains( proxy, param, test ) )
		{
			return "checked";
		}
		else {
			return "";
		}
	}


	/**
	 *  Gets the Selected attribute of the HelperClass object
	 *
	 *@param  request  Description of Parameter
	 *@param  param    Description of Parameter
	 *@param  test     Description of Parameter
	 *@return          The Selected value
	 */
	protected String isSelected( IAccessToLogic proxy , String param, String test )
	{
		if ( requestContains( proxy, param, test ) )
		{
			return "selected";
		}
		else {
			return "";
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  roles  Description of Parameter
	 *@param  role   Description of Parameter
	 *@return        Description of the Returned Value
	 */
	protected boolean containsRole( Iterator roles, IRole role )
	{
		if ( null == roles )
		{
			return false;
		}
		if ( null == role )
		{
			return true;
		}

		Set allRoles = new HashSet();

		while ( roles.hasNext() )
		{
			allRoles.add( roles.next() );
		}
		if ( allRoles.contains( role ) )
		{
			return true;
		}
		else {
			return false;
		}
	}


	/**
	 *  Gets the Param attribute of the HelperClass class
	 *
	 *@param  request  Description of Parameter
	 *@param  param    Description of Parameter
	 *@return          The Param value
	 */
	public static String getParam( IAccessToLogic proxy, String param )
	{
		String paramName = proxy.getRequestParameter( param );
        if ( paramName != null )
		{
			return paramName.trim();
		}
		else {
			return "";
		}
	}


	/**
	 *  Gets the Attr attribute of the HelperClass class
	 *
	 *@param  request  Description of Parameter
	 *@param  param    Description of Parameter
	 *@return          The Attr value
	 */
	public static java.lang.Object getAttr( IAccessToLogic proxy, String param )
	{
		Object attr = proxy.getRequestAttribute( param );
        if ( null != attr )
		{
			return attr;
		}
		else {
			return "";
		}
	}


	/**
	 *  Gets the ParamValues attribute of the HelperClass class
	 *
	 *@param  request  Description of Parameter
	 *@param  param    Description of Parameter
	 *@return          The ParamValues value
	 */
	public static String getParamValues( HttpServletRequest request, String param )
	{
		String values[] = request.getParameterValues( param );
		if ( values == null )
		{
			return "";
		}
		int count = values.length;
		switch ( count )
		{
			case 1:
				return values[0];
			default:
				StringBuffer result = new StringBuffer( values[0] );
				int stop = count - 1;
				if ( stop > 0 )
				{
					result.append( ", " );
				}
				for ( int i = 1; i < stop; ++i )
				{
					result.append( values[i] );
					result.append( ", " );
				}
				result.append( values[stop] );
				return result.toString();
		}
	}


	/**
	 *  Gets the AssignedRoles attribute of the HelperClass class
	 *
	 *@param  performer        activeUser (logged in user)
	 *@param  userID           userID
	 *@return                  The AssignedRoles value (contains IRole element)
	 *@exception  RoleCommandException  Description of Exception
	 */
	public static Set getAssignedRoles( IUser performer, String userID )
        throws RoleCommandException, UMException
	{
        final String methodName = "getAssignedRoles(IUser,String)";
        trace.entering( methodName, new Object[]{userID} );

		try
		{
			if ( userID.equals( "" ) )
			{
				trace.errorT( methodName, "No userID was passed" );
				throw new RoleCommandException( RoleAdminMessagesBean.MISSING_USER );
			}

			IUser user = UMFactory.getUserFactory().getUser( userID );
			if ( null == user )
			{
				trace.errorT( methodName, "UserFactory.getUser returned null for userID " + userID );
				throw new RoleCommandException( "Error in getUser(userID)" );
			}
            // checkAccess throws java.security.AccessControlException if failed.
            UserAdminHelper.checkAccess( performer, user.getCompany(), UserAdminHelper.VIEW_ROLES );
		}
		catch ( UMException e )
		{
			trace.errorT( methodName, "Couldn't get user for userID: " + userID, e );
			throw new RoleCommandException( RoleAdminMessagesBean.MISSING_USER );
		}

        IRoleFactory rolefactory = UMFactory.getRoleFactory();
        //TODO this method returns all roles instead of one users role!!!
		//Iterator assignedRoles = rolefactory.getRoles( user );
        ISearchResult assignedRoles = rolefactory.searchRoles(rolefactory.getRoleSearchFilter());

		Set list = new HashSet();
		while ( assignedRoles.hasNext() )
		{
			list.add( (IRole) assignedRoles.next() );
		}

		trace.exiting( methodName );
		return list;
	}


	/**
	 *  Gets the AvailableRoles attribute of the HelperClass class
	 *
	 *@param  performer        activeUser (logged in user)
	 *@param  userID           userID
	 *@return                  The AvailableRoles value (contains IRole element)
	 *@exception  RoleCommandException  Description of Exception
	 */
	public static Set getAvailableRoles( IUser performer, String userID )
        throws RoleCommandException, UMException
	{
        final String methodName = "getAvailableRoles(IUser,String)";
        trace.entering( methodName, new Object[]{userID} );

		try
		{
			if ( userID.equals( "" ) )
			{
				trace.errorT( methodName, "No userID was passed" );
				throw new RoleCommandException( RoleAdminMessagesBean.MISSING_USER );
			}
            IUser user = UMFactory.getUserFactory().getUser( userID );
            // checkAccess throws java.security.AccessControlException if failed.
            UserAdminHelper.checkAccess( performer, user.getCompany(), UserAdminHelper.VIEW_ROLES );
		}
		catch ( UMException e )
		{
			trace.errorT( methodName, "Couldn't get user for userID: " + userID, e );
			throw new RoleCommandException( RoleAdminMessagesBean.MISSING_USER );
		}

        IRoleFactory rolefactory = UMFactory.getRoleFactory();
        ISearchResult assignedRoles = rolefactory.searchRoles(rolefactory.getRoleSearchFilter());
		// HelperClass.getVisibleRoles makes sure te role is visible.
		Iterator sortedRoles = HelperClass.getVisibleRoles();
		Set updatedRoles = HelperClass.removeAssignedFromAll( sortedRoles, assignedRoles );

		trace.exiting( methodName);
		return updatedRoles;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  str  Description of Parameter
	 *@return      Description of the Returned Value
	 */
	public static String checkNull( String str )
	{
		return str == null ? "" : str;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  performer                 activeUser (logged in user)
	 *@param  userID                    userID
	 *@param  assignedRoleIDs           String[] of roleIDs
	 *@return                           true if successfull
	 *@exception  RoleCommandException  Description of Exception
	 */
	public static boolean assignRolestoUser( IUser performer, String userID, String[] assignedRoleIDs )
        throws RoleCommandException, UMException
	{
        final String methodName = "assignRolestoUser(IUser,userID,String[]))";
        trace.entering( methodName);
        //TODO is this method really doing something?
        
		try
		{
			if ( userID.equals( "" ) )
			{
				trace.errorT( methodName, "No userID was passed" );
				throw new RoleCommandException( RoleAdminMessagesBean.MISSING_USER );
			}
			trace.infoT( methodName, "Retrieving user " + userID );
            IUser user = UMFactory.getUserFactory().getUser(userID);
            // checkAccess throws java.security.AccessControlException if failed.
            UserAdminHelper.checkAccess( performer, user.getCompany(), UserAdminHelper.ASSIGN_ROLES );
		}
		catch ( UMException e )
		{
			trace.errorT( methodName, "Couldn't get user for userID: " + userID, e );
			throw new RoleCommandException( RoleAdminMessagesBean.MISSING_USER + e.getMessage() );
		}
		//Assigned roles
		Vector rolenames = new Vector();
		Set list = new HashSet();
		if ( null != assignedRoleIDs )
		{
            IRoleFactory rolefactory = UMFactory.getRoleFactory();
			String roleID = null;
			for ( int i = 0; i < assignedRoleIDs.length; i++ )
			{
				roleID = assignedRoleIDs[i].trim();
				trace.infoT( methodName, "Retrieving roleID " + roleID );
				IRole role = ( IRole ) rolefactory.getRole( roleID );
				if ( null == role )
				{
					trace.errorT( methodName, "rolefactory.getRoleByID(" + assignedRoleIDs[i].trim() + ") returned null" );
					throw new UMRuntimeException( "getRole called from assignRolestoUser returned null!" );
				}
				String rolename = role.getDisplayName();
				list.add( role );
				rolenames.add( rolename );
			}
		}
		trace.exiting( methodName );
		return true;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  tp  Description of Parameter
	 *@return     Description of the Returned Value
	 */
	public static Iterator getVisibleActions( TradingPartnerInterface tp )
	{
	    return PermissionRoles.getVisibleActions(tp).iterator();
	}


    /**
     *  Gets all roles that are visible
     *
     *@return                  all visible roles
     *@exception  UMException  Description of Exception
     */
    protected static Iterator getVisibleRoles() 
        throws UMException
    {
        return getVisibleRoles( null, "*", false );
    }

    /**
     *  Gets the roles that match the search filter 
     *
     *@param    searchFilter   the role search filter string
     *@param    checkMaxHits   defines if only the defined max. hits should be returned
     *@return                  the visible roles
     *@exception  UMException  Description of Exception
     */
    protected static Iterator getVisibleRoles(IAccessToLogic proxy, String searchFilter, 
                                              boolean checkMaxHits) 
        throws UMException
	{
		final String methodName = "getVisibleRoles(IAccessToLogic,String,boolean)";
        trace.entering( methodName );

		IRoleFactory rolefactory = UMFactory.getRoleFactory();
        IRoleSearchFilter sf = rolefactory.getRoleSearchFilter ();
        // default search filter is "*"
        if ((searchFilter == null) || "".equals(searchFilter) || "*".equals(searchFilter) )
        {
            sf.setUniqueName( "*", com.sap.security.api.ISearchAttribute.LIKE_OPERATOR, false );
        }
        else {
            int operator = com.sap.security.api.ISearchAttribute.EQUALS_OPERATOR;            
            if ( searchFilter.indexOf("*") != -1 )
            {
                operator = com.sap.security.api.ISearchAttribute.LIKE_OPERATOR;
            }                       
            sf.setSearchAttribute(IPrincipal.DEFAULT_NAMESPACE, IPrincipal.DISPLAYNAME, searchFilter, operator, false );
            sf.setUniqueName( searchFilter, operator, false );
            sf.setSearchMethod(IPrincipalSearchFilter.SEARCHMETHOD_OR);
        }

        ISearchResult roles = rolefactory.searchRoles (sf);
        // sort found role IDs
        TreeSet sortedIDs = new TreeSet( new UniqueIDComparator() );
        while (roles.hasNext())
        {
            sortedIDs.add( (String) roles.next() );
        }
        String[] sortedIDsArray = new String[sortedIDs.size()];
        sortedIDs.toArray( sortedIDsArray );

        int maxHits = UMFactory.getProperties().getNumber("ume.admin.search_maxhits_warninglevel", 200);
        int size = roles.size();
        // don't return more roles as defined as max. search result
        if ((checkMaxHits) && (size > maxHits))
        {
            if (proxy != null)
            {
                // set error message for role admin
                proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.SEARCH_RESULT_BEYOND_MAXHITS, 
                        new Integer[]{new Integer(size), new Integer(maxHits)} ) ) );
            }
            size = maxHits;
        }

        TreeSet visibleRoles = new TreeSet( new RoleNameComparator() );
        for (int i = 0; i < size; i++)
        {
            String uniqueIdOfRole = sortedIDsArray[i];
            IRole role = rolefactory.getRole(uniqueIdOfRole);
    		visibleRoles.add( role );
    		trace.debugT( methodName, "This Role is visible --> " + role.getDisplayName() );
        }
		trace.exiting( methodName );
		return visibleRoles.iterator();
	}


	/**
	 *  Gets the TP attribute of the HelperClass class
	 *
	 *@param  orgID                     Description of Parameter
	 *@return                           The TP value
	 *@exception  RoleCommandException  Description of Exception
	 */
	public static TradingPartnerInterface getTP( String orgID ) throws RoleCommandException
	{
        final String methodName = "getTP(String orgID)";
        trace.entering( methodName, new Object[]{"Getting company for companyID " + orgID} );

		if ( null == util.checkEmpty(orgID) )
		{
			//Individual User
			trace.infoT( methodName, "Individual user" );
			return null;
		}
		try	{
            TradingPartnerDirectoryInterface tpd = com.sapmarkets.tpd.TradingPartnerDirectoryCommon.getTPD();
			PartnerID mpid = PartnerID.instantiatePartnerID( orgID );
			trace.infoT( methodName, "This is a Company User", new Object[]{mpid} );
			return tpd.getPartner( mpid );
		}
		catch ( TpdException e )
		{
			String msg = "Please check if sapmbd_tpd.jar is configured and connection to the TPD DB is OK. ";
			trace.errorT( methodName, msg, e );
			throw new RoleCommandException( "(TPD Error) " + msg + e.getMessage() );
		}
	}


	/**
	 *  Gets the ActiveUser attribute of the HelperClass class
	 *
	 *@param   proxy   Description of Parameter
	 *@return  The ActiveUser value
	 */
	protected static IUser getActiveUser( IAccessToLogic proxy )
	{
		return proxy.getActiveUser();
	}


	/**
	 *  Gets the Desciption attribute of the HelperClass class
	 *
	 *@param  rolefactory  Description of Parameter
	 *@param  roleIDs      Description of Parameter
	 *@return              The Desciption value
	 */
	protected static String getRoleDesciption( IRoleFactory rolefactory, String roleIDs )
	{
        final String methodName = "getDescription(IRoleFactory,String)";
		trace.entering( methodName, new Object[]{rolefactory,roleIDs} );

		if ( ( null == roleIDs ) || ( null == rolefactory ) )
		{
			return "";
		}

		StringBuffer roleDescription = new StringBuffer();
		String description = null;
		StringTokenizer t = new StringTokenizer( roleIDs, "," );
		int count = 0;
		while ( t.hasMoreTokens() )
		{
			String roleID = t.nextToken();
            try
            {
    			IRole role = rolefactory.getRole( roleID.trim() );
                count++;
                description = role.getDescription();
                if ( null != description )
                {
                    roleDescription.append( description );
                    roleDescription.append( ", " );
                }
            }
            catch (UMException ex)
            {
 				trace.warningT( methodName, "role not found for roleID= " + roleID, ex );
            }
		}
		description = roleDescription.toString();
		trace.infoT( methodName, "description(before): " + description );
		if ( count > 1 )
		{
			return "MultiRole";
		}
		int index = description.lastIndexOf( ',' );
		trace.infoT( methodName, "pos: " + index );
		if ( index <= 0 )
		{
			return "";
		}
		description = description.substring( 0, index );
		trace.infoT( methodName, "description(after): " + description );
		trace.exiting( methodName+ ": " + description);
		return description;
	}


	/**
	 *  Gets the Users attribute of the HelperClass class
	 *
	 *@param  allUserIDs                   Description of Parameter
	 *@return                              The Users value
	 *@exception  UMException  Description of Exception
	 */
	public static IUser[] getUsers( String allUserIDs ) throws UMException
	{
		final String methodName = "getUsers(String)";
        trace.entering(methodName, new Object[] {allUserIDs});

        if ( null == allUserIDs )
		{
			return null;
		}
		Vector allUsers = convertIDsToVector( allUserIDs, "," );
		Vector resultedUsers = new Vector();
		int size = allUsers.size();
		for ( int i = 0; i < size; i++ )
		{
			String userID = (String) allUsers.elementAt( i );
			IUser user = UMFactory.getUserFactory().getUser(userID);
            //TODO how to react if getUser throws exception?
			trace.infoT( methodName , user.getDisplayName() );
			resultedUsers.add( user );
		}
		size = resultedUsers.size();
		IUser[] assignedUsers = new IUser[size];
		resultedUsers.toArray( assignedUsers );

        trace.exiting(methodName);
		return assignedUsers;
	}


	/**
	 *  Gets the Users attribute of the HelperClass class
	 *
	 *@param  allUserIDs                   Description of Parameter
	 *@return                              The Users value
	 *@exception  UMException  Description of Exception
	 */
	public static Set getUsers( String[] allUserIDs ) throws UMException
	{
		final String methodName = "getUsers(String[])";
		trace.entering( methodName );

		HashSet userSet = new HashSet();
		if ( null == allUserIDs )
		{
			return userSet;
		}
		for ( int i = 0; i < allUserIDs.length; i++ )
		{
			String userID = allUserIDs[i];
			trace.infoT(methodName, "Retrieving User Object for userID = " + userID);
            IUser user = UMFactory.getUserFactory().getUser( userID );
            //TODO how to react if getUser throws exception?
			trace.infoT( methodName, "getting " + user.getDisplayName() );
			userSet.add( user );
		}
		trace.exiting( methodName );
		return userSet;
	}


	/**
	 *  Gets the CompanyName attribute of the HelperClass class
	 *
	 *@param  tp  Description of Parameter
	 *@return     The CompanyName value
	 */
	public static String getCompanyName( TradingPartnerInterface tp )
	{
        final String methodName = "getCompanyName(tp)";
		if ( null != tp )
		{
            String coName = tp.getDisplayName();
            trace.infoT(methodName, coName);
			return coName;
		}
		else {
			return "guest User";
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  proxy  Description of Parameter
	 *@param  e      Description of Parameter
	 *@return        Description of the Returned Value
	 */
	public static String makeErrorReport( IAccessToLogic proxy, Throwable e )
	{
        final String methodName = "makeErrorReport(proxy,Throwable)";
		StringBuffer buffer = new StringBuffer();
		//reportRequest( buffer, proxy );
		reportParameters( buffer, proxy );
		//reportHeaders( buffer, req );
		//reportCookies( buffer, req );
		reportException( buffer, e );
		String result = buffer.toString();
		trace.infoT( methodName, result );

		return result;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  buffer  Description of Parameter
	 *@param  e       Description of Parameter
	 */
	protected static void reportException( StringBuffer buffer, Throwable e )
	{
        //Commented out below three lines since caused a bug in InQmy
		//The problem was that a forward was called after outputing something to html.
        //This is not allowed: If you do a forward, you must not get a printwriter before.

		//StringWriter writer = new StringWriter();
        //e.printStackTrace( new PrintWriter( writer ) );
		//buffer.append( writer.getBuffer() );
		buffer.append( e.toString() );
		buffer.append( '\n' );
	}


	/**
	 *  Description of the Method
	 *
	 *@param  buffer  Description of Parameter
	 *@param  req     Description of Parameter
	 */
	/*
    protected static void reportRequest( StringBuffer buffer, HttpServletRequest req )
	{
		buffer.append( "Request: " );
		buffer.append( req.getMethod() );
		buffer.append( ' ' );
		buffer.append( HttpUtils.getRequestURL( req ) );
		String queryString = req.getQueryString();
		if ( queryString != null )
		{
			buffer.append( '?' );
			buffer.append( queryString );
		}
		buffer.append( "\nSession ID: " );
		String sessionId = req.getRequestedSessionId();
		if ( sessionId == null )
		{
			buffer.append( "none" );
		}
		else if ( req.isRequestedSessionIdValid() )
		{
			buffer.append( sessionId );
			buffer.append( " (from " );
			if ( req.isRequestedSessionIdFromCookie() )
			{
				buffer.append( "cookie)\n" );
			}
			else if ( req.isRequestedSessionIdFromURL() )
			{
				buffer.append( "url)\n" );
			}
			else
			{
				buffer.append( "unknown)\n" );
			}
		}
		else
		{
			buffer.append( "invalid\n" );
		}
	}
    */

	/**
	 *  Description of the Method
	 *
	 *@param  buffer  Description of Parameter
	 *@param  req     Description of Parameter
	 */
	protected static void reportParameters( StringBuffer buffer, IAccessToLogic proxy )
	{
		Enumeration names = proxy.getRequestParameterNames();
		if ( names.hasMoreElements() )
		{
			buffer.append( "Parameters:\n" );
			while ( names.hasMoreElements() )
			{
				String name = ( String ) names.nextElement();
				String[] values = proxy.getRequestParameterValues( name );
				for ( int i = 0; i < values.length; ++i )
				{
					buffer.append( "    " ).append( name );
					buffer.append( " = " ).append( values[i] );
					buffer.append( '\n' );
				}
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  proxy  Description of Parameter
	 *@return        Description of the Returned Value
	 */
	public static String reportRequestParameters( IAccessToLogic proxy)
	{
		StringBuffer buffer = new StringBuffer();
		reportParameters( buffer, proxy );
		return buffer.toString();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  buffer  Description of Parameter
	 *@param  req     Description of Parameter
	 */
	/*
    protected static void reportHeaders( StringBuffer buffer, HttpServletRequest req )
	{
		Enumeration names = req.getHeaderNames();
		if ( names.hasMoreElements() )
		{
			buffer.append( "Headers:\n" );
			while ( names.hasMoreElements() )
			{
				String name = ( String ) names.nextElement();
				String value = ( String ) req.getHeader( name );
				buffer.append( "    " );
				buffer.append( name );
				buffer.append( ": " );
				buffer.append( value );
				buffer.append( '\n' );
			}
		}
	}

    */
	/**
	 *  Description of the Method
	 *
	 *@param  buffer  Description of Parameter
	 *@param  req     Description of Parameter
	 */
	/*
    protected static void reportCookies( StringBuffer buffer, HttpServletRequest req )
	{
		Cookie[] cookies = req.getCookies();
		int l = cookies.length;
		if ( l > 0 )
		{
			buffer.append( "Cookies:\n" );
			for ( int i = 0; i < l; ++i )
			{
				Cookie cookie = cookies[i];
				buffer.append( "    " );
				buffer.append( cookie.getName() );
				buffer.append( " = " );
				buffer.append( cookie.getValue() );
				buffer.append( '\n' );
			}
		}
	}

*/
	/**
	 *  Description of the Method
	 *
	 *@param  request  Description of Parameter
	 *@param  param    Description of Parameter
	 *@param  test     Description of Parameter
	 *@return          Description of the Returned Value
	 */
	protected static boolean requestContains( IAccessToLogic proxy, String param, String test )
	{
		String rp[] = proxy.getRequestParameterValues( param );
		if ( rp == null )
		{
			return false;
		}
		for ( int i = 0; i < rp.length; i++ )
		{
			if ( test.equals( rp[i] ) )
			{
				return true;
			}
		}
		return false;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  values  Description of Parameter
	 *@return         Description of the Returned Value
	 */
	protected static String toString( String[] values )
	{
		if ( ( values == null ) || ( values.length == 0 ) )
		{
			return "";
		}
		int count = values.length;
		switch ( count )
		{
			case 1:
				return values[0];
			default:
				StringBuffer result = new StringBuffer( values[0] );
				int stop = count - 1;
				if ( stop > 0 )
				{
					result.append( ", " );
				}
				for ( int i = 1; i < stop; ++i )
				{
					result.append( values[i] );
					result.append( ", " );
				}
				result.append( values[stop] );
				return result.toString();
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  values  Description of Parameter
	 *@return         Description of the Returned Value
	 */
	protected static String toString( Vector values )
	{
		if ( ( values == null ) || ( values.size() == 0 ) )
		{
			return "";
		}
		int count = values.size();
		switch ( count )
		{
			case 1:
				return ( String ) values.elementAt( 0 );
			default:
				StringBuffer result = new StringBuffer( ( String ) values.elementAt( 0 ) );
				int stop = count - 1;
				if ( stop > 0 )
				{
					result.append( ", " );
				}
				for ( int i = 1; i < stop; ++i )
				{
					result.append( ( String ) values.elementAt( i ) );
					result.append( ", " );
				}
				result.append( ( String ) values.elementAt( stop ) );
				return result.toString();
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  req  Description of Parameter
	 *@param  res  Description of Parameter
	 *@return      Description of the Returned Value
	 */
	protected static IUser forceLoggedInUser( HttpServletRequest req, HttpServletResponse res )
	{
		IAuthentication authentication = UMFactory.getAuthenticator();
		IUser activeUser = authentication.forceLoggedInUser( req, res );
		return activeUser;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  all       Description of Parameter
	 *@param  assigned  Description of Parameter
	 *@return           Description of the Returned Value
	 */
	protected static Set removeAssignedFromAll( Iterator all, Iterator assigned )
	{
		final String methodName = "removeAssignedFromAll(Iterator,Iterator)";
        trace.entering(methodName);

        Set allActions = new HashSet();
		Set assignedActions = new HashSet();

		while ( all.hasNext() )
		{
			allActions.add( all.next() );
		}
		while ( assigned.hasNext() )
		{
			assignedActions.add( assigned.next() );
		}
		allActions.removeAll( assignedActions );

		trace.exiting(methodName);
        return allActions;
	}

    /**************************************************************************/
	protected static Set removeAssignedFromAll( Iterator all, ISearchResult assigned )
	{
		String methodName = "removeAssignedFromAll(Iterator all,Iterator assigned)";
        trace.entering(methodName);

        Set allActions = new HashSet();
		Set assignedActions = new HashSet();

		while ( all.hasNext() )
		{
			allActions.add( all.next() );
		}
		while ( assigned.hasNext() )
		{
			assignedActions.add( assigned.next() );
		}
		allActions.removeAll( assignedActions );
		trace.exiting(methodName);
        return allActions;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  param1  Description of Parameter
	 *@param  param2  Description of Parameter
	 *@return         Description of the Returned Value
	 */
	protected static Set removeAssignedFromAll( Iterator param1, Set param2 )
	{
		if ( null == param1 )
		{
			return null;
		}

		HashSet list = new HashSet();
		while ( param1.hasNext() )
		{
			list.add( param1.next() );
		}

		if ( ( null == param2 ) || ( param2.size() == 0 ) )
		{
			return list;
		}

		list.removeAll( param2 );
		return list;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  proxy  Description of Parameter
	 *@return        Description of the Returned Value
	 */
	public static IUser checkActiveUser( IAccessToLogic proxy )
	{
        final String methodName = "checkActiveUser(proxy)";

		IUser performer = getActiveUser( proxy );
		if ( null == performer )
		{
			trace.errorT( methodName, "ActiveUser was null" );
			proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.MISSING_USER ) ) );
			throw new UMRuntimeException( "ActiveUser was null" );
		}
		else {
			trace.infoT( methodName, "Active User = " + performer.getDisplayName() );
			return performer;
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  req    Description of Parameter
	 *@param  param  Description of Parameter
	 *@return        Description of the Returned Value
	 */
	protected static String constructProtocolAndDomain( HttpServletRequest req, String param )
	{
		StringBuffer reqUtilURL = javax.servlet.http.HttpUtils.getRequestURL( req );
		String callingURL = reqUtilURL.toString();
		int index = callingURL.indexOf( "//" );
		int endindex = callingURL.indexOf( "/", index + 2 );
		String protocol = null;
		String domainport = null;
		if ( index >= 0 )
		{
			protocol = callingURL.substring( 0, index );
			domainport = callingURL.substring( index, endindex );
		}
		String invokedURL = protocol + domainport + param;
		return invokedURL;
	}


	/**
	 *  rolenames passed are roleIDs. This routine constructs displayable rolenames
	 *  out of roleIDs i.e "role1 , role2 , role3"
	 *
	 *@param  roleFactory                  Description of Parameter
	 *@param  rolenames                    are roleIDs
	 *@return                              String of roles separated by ","
	 *@exception  UMException  Description of Exception
	 */
	protected static String makeRoleNames( IRoleFactory roleFactory, String[] rolenames )
        throws UMException
	{
		final String methodName = "makeRoleNames(IRoleFactory,String[])";
		trace.entering(methodName);

        int count = rolenames.length;
		if ( 0 == count )
		{
			return "";
		}
		String[] roles = new String[count];
		for ( int i = 0; i < rolenames.length; i++ )
		{
			IRole role = roleFactory.getRole( rolenames[i].trim() );
			if ( null == role )
			{
				trace.errorT( methodName, "roleFactory.getRole(" + rolenames[i].trim() + ") returned null" );
				throw new UMRuntimeException();
			}
			roles[i] = role.getDisplayName();
            if (trace.beInfo()) 
            {
    			trace.infoT( methodName, "roleID: " + rolenames[i] );
    			trace.infoT( methodName, "roleName: " + roles[i] );
            }
		}
        trace.exiting(methodName);
		return makeString( count, roles );
	}


	/**
	 *  rolenames passed are roleIDs. This routine constructs roleIDs seperated
	 *  by ',' i.e "xxxxx1 , xxxxx2 , xxxxx3"
	 *
	 *@param   names           Description of Parameter
	 *@return                  Description of the Returned Value
	 *@exception  UMException  Description of Exception
	 */
	public static String makeIDs( String[] names ) throws UMException
	{
		int count = names.length;
		if ( 0 == count )
		{
			return "";
		}
		return makeString( count, names );
	}


	/**
	 *  Description of the Method
	 *
	 *@param  count   Description of Parameter
	 *@param  values  Description of Parameter
	 *@return         Description of the Returned Value
	 */
	public static String makeString( int count, String[] values )
	{
		switch ( count )
		{
			case 1:
				return values[0];
			default:
				StringBuffer result = new StringBuffer( values[0] );
				int stop = count - 1;
				if ( stop > 0 )
				{
					result.append( ", " );
				}
				for ( int i = 1; i < stop; ++i )
				{
					result.append( values[i] );
					result.append( ", " );
				}
				result.append( values[stop] );
				return result.toString();
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  param1  largest set of users
	 *@param  param2  remove this from first param
	 *@return         returns the result set of users
	 */
	protected static IUser[] removeAssignedUsersFromUsers( IUser[] param1, IUser[] param2 )
	{
		if ( null == param2 )
		{
			return param1;
		}
		if ( ( null == param1 ) || ( param1.length == 0 ) )
		{
			return null;
		}

		Vector List1 = new Vector();
		Vector List2 = new Vector();

		for ( int i = 0; i < param1.length; i++ )
		{
			List1.addElement( param1[i] );
		}

		for ( int i = 0; i < param2.length; i++ )
		{
			List2.addElement( param2[i] );
		}

		List1.removeAll( List2 );
		IUser[] result = new IUser[ List1.size() ];
		List1.copyInto( result );
		return result;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  users                        Description of Parameter
	 *@param  userIDs                      Description of Parameter
	 *@return                              Description of the Returned Value
	 *@exception  UMException  Description of Exception
	 */
	protected static IUser[] removeUsers( IUser[] users, String[] userIDs )
        throws UMException
	{
        final String methodName = "removeUsers(IUser[],String[])";
        trace.entering(methodName);

		if ( ( null == userIDs ) || ( userIDs.length == 0 ) )
		{
			trace.warningT( methodName, "No userIDs " );
			return users;
		}
		if ( ( null == users ) || ( users.length == 0 ) )
		{
			trace.warningT( methodName, "users passed were null" );
			return null;
		}

		Vector allUsers = new Vector();
		Vector removeUsers = new Vector();

		for ( int i = 0; i < users.length; i++ )
		{
			allUsers.add( users[i] );
		}
		for ( int i = 0; i < userIDs.length; i++ )
		{
			IUser user = UMFactory.getUserFactory().getUser( userIDs[i] );
			if ( null == user )
			{
				trace.errorT( methodName, "UserFactory.getUser returned null for userID " + userIDs[i] );
			}
			else {
				trace.infoT( methodName, "removing " + user.getDisplayName() );
				removeUsers.add( user );
			}
		}
		allUsers.removeAll( removeUsers );
		IUser[] assignedUsers = new IUser[ allUsers.size() ];
		allUsers.toArray( assignedUsers );

        trace.exiting(methodName);
		return assignedUsers;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  allUserIDs                   Description of Parameter
	 *@param  removeUserIDs                Description of Parameter
	 *@return                              Description of the Returned Value
	 *@exception  UMException  Description of Exception
	 */
	protected static IUser[] removeUsers( String[] allUserIDs, String[] removeUserIDs )
        throws UMException
	{
        final String methodName = "removeUsers(String[],String[])";
        trace.entering(methodName);

		if ( ( null == allUserIDs ) || ( allUserIDs.length == 0 ) )
		{
			return null;
		}

		Vector allUsers = new Vector();
		Vector removeUsers = new Vector();

		for ( int i = 0; i < allUserIDs.length; i++ )
		{
			allUsers.add( allUserIDs[i] );
		}
		for ( int i = 0; i < removeUserIDs.length; i++ )
		{
			removeUsers.add( removeUserIDs[i] );
		}
		allUsers.removeAll( removeUsers );
		Vector resultedUsers = new Vector();
		int size = allUsers.size();
		for ( int i = 0; i < size; i++ )
		{
			String userID = (String) allUsers.elementAt( i );
			IUser user = UMFactory.getUserFactory().getUser( userID );
			if ( null == user )
			{
				trace.errorT( methodName, "UserFactory.getUser returned null for userID " + userID );
			}
			else {
				trace.infoT( methodName, "removing " + user.getDisplayName() );
				resultedUsers.add( user );
			}
		}
		IUser[] assignedUsers = new IUser[ resultedUsers.size() ];
		resultedUsers.toArray( assignedUsers );
        trace.exiting(methodName);
		return assignedUsers;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  IDs    IDs, separated by token
	 *@param  token  token
	 *@return        Returns a set containing UserIDs
	 */
	protected static Set convertIDsToSet( String IDs, String token )
	{
        final String methodName = "convertIDsToSet(String,String)";
        trace.entering( methodName, new Object[]{IDs, token} );

		Set myList = new HashSet();
		StringTokenizer t = new StringTokenizer( IDs, token );
		while ( t.hasMoreTokens() )
		{
			IDs = t.nextToken();
			myList.add( IDs );
			trace.infoT(methodName, "IDs =" + IDs );
		}
		trace.exiting( methodName );
		return myList;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  IDs  Description of Parameter
	 *@return      Description of the Returned Value
	 */
	public static Vector convertStringArrayToVector( String[] IDs )
	{
        final String methodName = "convertStringArrayToVector(String[])";
		trace.entering( methodName, new Object[]{IDs} );

		Vector myList = new Vector();
		if ( null == IDs )
		{
			trace.warningT( methodName, "ID passed was null" );
			return myList;
		}
		for ( int i = 0; i < IDs.length; i++ )
		{
			myList.add( IDs[i] );
			trace.infoT( methodName, "Adding " + IDs[i] + " to the vector" );
		}
		trace.exiting( methodName );
		return myList;
	}


    /**
     *  Description of the Method
     *
     *@param  proxy                   Description of Parameter
     *@param  principal               Description of Parameter
     *@return                         Description of the Returned Value
     */
    public static ListBean getListBean(IAccessToLogic proxy, String principal)
    {
        final String methodName = "getListBean(IAccessToLogic,String)";
        trace.entering(methodName);

        IPrincipal[] members = null;
        if ( principal.equalsIgnoreCase("user"))
        {
            members = (IPrincipal[])proxy.getSessionAttribute(RoleAdminServlet.ASSIGNED_USERS);
        }
        else if ( principal.equalsIgnoreCase("group"))
        {
            members = (IPrincipal[])proxy.getSessionAttribute(RoleAdminServlet.ASSIGNED_GROUPS);
        }

        return getListBean( proxy, members );
    }


    /**
     *  Description of the Method
     *
     *@param  proxy                 Description of Parameter
     *@param  members               Description of Parameter
     *@return                       Description of the Returned Value
     */
    public static ListBean getListBean(IAccessToLogic proxy, IPrincipal[] members)
    {
        final String methodName = "getListBean(IAccessToLogic,IPrincipal[])";
        trace.entering(methodName);

        ListBean list = null;
        if (null != members)
        {
            list = new ListBean(proxy, members);
            trace.infoT(methodName, "nr. of assignedMembers: " + members.length);
        }
        trace.exiting(methodName);
        return list;
    }

    /**
     *  Description of the Method
     *
     *@param  proxy                   Description of Parameter
     *@param  principal               Description of Parameter
     *@param  members                 Description of Parameter
     *@return                         Description of the Returned Value
     */
    public static ListBean setUpListBean(IAccessToLogic proxy, String principal, IPrincipal[] members)
    {
        final String methodName = "setUpListBeanistBean(proxy,principal,members)";
        trace.entering(methodName);

        ListBean list = null;
        if (members == null)
        {
            trace.warningT(methodName, "IPrincipal[] passed was null, returning null");
            trace.exiting(methodName);
            return null;
        }
        if ( principal.equalsIgnoreCase("user"))
        {
            members = sortByDisplayName(members);
            proxy.removeSessionAttribute( RoleAdminServlet.ASSIGNED_USERS );
            proxy.setSessionAttribute(RoleAdminServlet.ASSIGNED_USERS, members);
            list = getListBean(members);
        }
        else if ( principal.equalsIgnoreCase("group"))
        {
            members = sortByDisplayName(members);
            proxy.removeSessionAttribute( RoleAdminServlet.ASSIGNED_GROUPS );
            proxy.setSessionAttribute(RoleAdminServlet.ASSIGNED_GROUPS, members);
            list = getListBean(members);
        }
        else {
            trace.infoT(methodName, "Not supported principal");
        }
        trace.exiting(methodName);
        return list;
    }


    /**
     *  Description of the Method
     *
     *@param  IDs                 Description of Parameter
     *@param  token               Description of Parameter
     *@return                     Description of the Returned Value
     */
	public static Vector convertIDsToVector( String IDs, String token )
	{
        final String methodName = "convertIDsToVector(IDs,token)";
        trace.entering( methodName, new Object[]{IDs, token} );

		Vector myVector = new Vector();
		StringTokenizer t = new StringTokenizer( IDs, token );
		while ( t.hasMoreTokens() )
		{
			IDs = t.nextToken();
			myVector.add( IDs );
			trace.infoT( methodName, "IDs =" + IDs );
		}
		trace.exiting( methodName);
		return myVector;
	}

    /**************************************************************************/
    public static IPrincipal[] sortByDisplayName(IPrincipal[] users)
    {
	    final String methodName = "sortByDisplayName(IPrincipal)";
        trace.entering(methodName);

        TreeSet sortedUsers = new TreeSet( new UserNameComparator() );
        if (null != users)
        {
            for (int i=0; i < users.length; i++)
            {
                sortedUsers.add(users[i]);
            }
        }
        IPrincipal[] sorteUsersdArray = new IPrincipal[sortedUsers.size()];
        sortedUsers.toArray(sorteUsersdArray);

        trace.exiting(methodName);
        return sorteUsersdArray;
    }


	/**
	 *  Gets the CombinedUsers attribute of the AddUsersCommand object
	 *
	 *@param  selectedUsers                Set of IUser
	 *@param  existingUsers                Set of IUser
	 *@return                              The CominedUsers value
	 *@exception  UMException  Description of Exception
	 */
	public static IPrincipal[] getCominedMembers( Set selectedMembers, Set existingMembers )
        throws UMException
	{
		final String methodName = "getCominedMembers(selectedMembers,existingMembers)";
        trace.entering( methodName);

		if ( (null == selectedMembers) && (null == existingMembers) )
		{
			return new IPrincipal[0];
		}

        HashMap memberMap = new HashMap();
        
        if (existingMembers != null)
        {
            trace.infoT(methodName, "existingMembers");
    		Iterator memberIter = existingMembers.iterator();
    		while (memberIter.hasNext())
    		{
    			IPrincipal member = (IPrincipal) memberIter.next();
    			String memberUniqueID = member.getUniqueID();
    			memberMap.put(memberUniqueID, member);
    			trace.infoT(methodName, memberUniqueID);
    		}
        }
		        
        if (selectedMembers != null)
        {
            trace.infoT(methodName, "selectedMembers");
            Iterator memberIter = selectedMembers.iterator();
            while (memberIter.hasNext())
            {
                IPrincipal member = (IPrincipal) memberIter.next();
                String memberUniqueID = member.getUniqueID();
                memberMap.put(memberUniqueID, member);
                trace.infoT(methodName, memberUniqueID);
            }
        }

        Iterator memberIter = memberMap.values().iterator();
   		IPrincipal[] members = new IPrincipal[ memberMap.size() ];
        int i=0;
        while( memberIter.hasNext())
        {
	        members[i++] = (IPrincipal) memberIter.next();
		}
		return members;
	}

    /**
     *  Description of the Method
     *
     *@param  proxy               Description of Parameter
     *@return                     Description of the Returned Value
     */
	public static String setPrincipalRequestParam( IAccessToLogic proxy )
	{
        final String methodName = "setPrincipalRequestParam(proxy)";
        trace.entering(methodName);

        String principal = HelperClass.getParam( proxy, "principal" ).trim();
		if ( principal.equals( "user" ) || principal.equals( "group" ) )
		{
			proxy.setRequestAttribute( "principal", principal );
            trace.infoT(methodName, "Principal is " + principal);
		}
        else {
            trace.infoT(methodName, "not supported principal");
        }

        trace.exiting(methodName);
        return principal;
	}

    /**
     *  Description of the Method
     *
     *@param  principal           Description of Parameter
     *@return                     Description of the Returned Value
     */
    public static String getPrincipalInfo(IPrincipal principal)
    {
        final String methodName = "getPrincipalInfo(IPrincipal)";
        trace.entering(methodName);

        String info = "";
        try 
        {
            if (principal instanceof IUser)
            {
                IUser user = (IUser) principal;
                IUserAccount[] uas = user.getUserAccounts();
                if (uas.length > 0)
                {
                    info = uas[0].getLogonUid();
                    if (null == info)
                    {
                        trace.warningT(methodName, "user.getuseraccount()[0].getLogonUid() returned null, will use uniqueID instead");
                        info = principal.getUniqueID();
                    }
                }
                else {
                    trace.warningT(methodName, "user doesn't have a user account, will use uniqueName instead");
                    info = user.getUniqueName();
                }
            }
            else if (principal instanceof IGroup)
            {
                IGroup group = (IGroup) principal;
                info = group.getDescription();
            }
            else {
                trace.infoT(methodName, "The principal passed was not supported, returning uniqueID");
                info = principal.getUniqueID();
            }
        }
        catch (UMException ex)
        {
            trace.warningT(methodName, "Exception occurred, returning uniqueID");
            info = principal.getUniqueID();
        }
        trace.exiting(methodName,info);
        return info;
    }

    /**
     *  Description of the Method
     *
     *@param  principalVec           Description of Parameter
     *@return                     Description of the Returned Value
     */
    public static IPrincipal[] getPrincipals(Vector principalVec)
    {
        final String methodName = "getPrincipals(Vector)";
        trace.entering(methodName);

   		IPrincipal assignedMember = (IPrincipal) principalVec.elementAt(0);
		if (assignedMember instanceof IGroup)
		{
            trace.infoT(methodName," returning IGroup[]");
		    IGroup[] assignedMembers = new IGroup[principalVec.size()];
            trace.exiting(methodName);
		    return assignedMembers = (IGroup[]) principalVec.toArray(assignedMembers);
		}
        else if (assignedMember instanceof IRole)
        {
            trace.infoT(methodName," returning IRole[]");
            IRole[] assignedMembers = new IRole[principalVec.size()];
            trace.exiting(methodName);
            return assignedMembers = (IRole[]) principalVec.toArray(assignedMembers);
        }
		else if (assignedMember instanceof IUser)
		{
            trace.infoT(methodName," returning IUser[]");
		    IUser[] assignedMembers = new IUser[principalVec.size()];
            trace.exiting(methodName);
		    return assignedMembers = (IUser[]) principalVec.toArray(assignedMembers);
		}
		else {
            trace.infoT(methodName, "not supported principal, returning principal[]");
		    IPrincipal[] assignedMembers = new IPrincipal[principalVec.size()];
            trace.exiting(methodName);
		    return assignedMembers = (IPrincipal[]) principalVec.toArray(assignedMembers);
		}
    }

    /**************************************************************************/
	protected static Set getGroups( String[] allGroupIDs ) throws UMException
	{
		final String methodName = "getGroups(String[])";
		trace.entering( methodName );

		HashSet groupSet = new HashSet();
		if ( null == allGroupIDs )
		{
			return groupSet;
		}
		for ( int i = 0; i < allGroupIDs.length; i++ )
		{
			String groupID = allGroupIDs[i];
			trace.infoT( methodName, "Retrieving User Object for groupID = " + groupID );
			IGroup group = UMFactory.getGroupFactory().getGroup( groupID );
            //TODO how to react if getGroup throws exception?
			trace.infoT( methodName, "getting " + group.getDisplayName() );
			groupSet.add( group );
		}
		trace.exiting( methodName );
		return groupSet;
	}
    
    
	/**
	 *  Gets the visible groups for the active user
	 *
	 *@return                  The visible groups
	 *@exception  UMException  Description of Exception
	 */
    protected static Iterator getVisibleGroups(IUser performer) throws UMException
    {
        final String methodName = "getVisibleGroups(IUser)";
        trace.entering( methodName );

        IGroupFactory groupfactory = UMFactory.getGroupFactory();
        IGroupSearchFilter sf = groupfactory.getGroupSearchFilter();
        sf.setUniqueName( "*", com.sap.security.api.ISearchAttribute.LIKE_OPERATOR, true );

        ISearchResult groups = groupfactory.searchGroups( sf );
        TreeSet visibleGroups = new TreeSet( new RoleNameComparator() );
        
        boolean manageAllCompanies = UserAdminHelper.hasAccess(performer, UserAdminHelper.MANAGE_ALL_COMPANIES);
        boolean companyConcept = UserAdminHelper.isCompanyConceptEnabled();
        String companyID = performer.getCompany();

        if (companyConcept && !manageAllCompanies)
        {
            trace.infoT(methodName, "Company groups are not visible to this user!");
        }
        
        while ( groups.hasNext() )
        {
            String uniqueIdOfGroup = ( String ) groups.next();
            IGroup group = groupfactory.getGroup( uniqueIdOfGroup );
            // only show company groups if performer is allowed to manage all companies
            if (companyConcept && !manageAllCompanies)
            {
                String[] values = group.getAttribute(IPrincipal.DEFAULT_NAMESPACE, CompanyGroups.COMPANY_ATTRIBUTE);
                if ((values == null) || (values.length == 0) || 
                    checkNull(values[0]).equals( companyID ))
                {
                    visibleGroups.add( group );
                    trace.debugT( methodName, "This Group is visible: " + uniqueIdOfGroup );
                }
            }
            else {
                visibleGroups.add( group );
                trace.debugT( methodName, "This Group is visible: " + uniqueIdOfGroup );
            }
        }
        trace.exiting( methodName );
        return visibleGroups.iterator();
    }
    
	private static ListBean getListBean(IPrincipal[] members)
	{
		final String methodName = "getListBean(IAccessToLogic,IPrincipal[])";
		trace.entering(methodName);

		ListBean list = null;
		if (null != members)
		{
			list = new ListBean(members);
			trace.infoT(methodName, "nr. of assignedMembers: " + members.length);
		}
		trace.exiting(methodName);
		return list;
	}  // getListBean(IPrincipal[] members) 
}

/******************************************************************************/
class UniqueIDComparator implements java.util.Comparator
{
    /**
     *  Compares two objects based on their uniqueID.
     *
     *@param  o1  uniqueID1
     *@param  o2  uniqueID2
     *@return     Description of the Returned Value
     */
    public int compare( Object o1, Object o2 )
    {
        String uniqueID1 = ( String ) o1;
        String uniqueID2 = ( String ) o2;

        int res = uniqueID1.compareToIgnoreCase( uniqueID2 );
        if ( 0 == res ) res = -1;
        return res;
    }
}
