package com.sap.security.core.admin.group;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.sap.security.api.IGroup;
import com.sap.security.api.IGroupFactory;
import com.sap.security.api.IPrincipal;
import com.sap.security.api.IPrincipalSearchFilter;
import com.sap.security.api.ISearchAttribute;
import com.sap.security.api.ISearchResult;
import com.sap.security.api.IUser;
import com.sap.security.api.IUserFactory;
import com.sap.security.api.IUserSearchFilter;
import com.sap.security.api.NoSuchGroupException;
import com.sap.security.api.NoSuchUserException;
import com.sap.security.api.PrincipalIterator;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.IAccessToLogic;
import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.core.admin.util;
import com.sap.security.core.admin.role.HelperClass;
import com.sap.security.core.admin.role.RoleAdminMessagesBean;
import com.sap.security.core.imp.GroupFactory;
import com.sap.security.core.persistence.datasource.imp.CompanyGroups;
import com.sap.security.core.util.ErrorBean;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.Message;

/**
 *  This class implements the group to user and user to group assignments
 *
 *@author     Markus Liepold
 */
public class GroupAssignmentFactory
{
	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/group/GroupAssignmentFactory.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private final static IUMTrace trace = InternalUMFactory.getTrace( VERSIONSTRING );

	protected final static int GROUP_RETRIEVAL = 0;
	protected final static int USER_RETRIEVAL = 1;

	//  Only one instant of roleassighfactory is allowed
	private static GroupAssignmentFactory groupassignfactory = null;

    private final static int MAX_HITS = UMFactory.getProperties().getNumber("ume.admin.search_maxhits_warninglevel", 200);

	/**
	 *  Returns the members that all given groups have in common.
	 *
	 *@param  groupIDs      String[] of groupnameIDs
	 *@param  performer     currently logged in user
     *@param  type          values for different supported IPrincipals
	 *@return               Set of IPrincipal elements
	 *@exception  UMException  Description of Exception
	 */
	protected Set getMembersWithTheseGroups( IAccessToLogic proxy, IUser performer, 
                                             String[] groupIDs, int type, String memberSF )
        throws UMException
	{
		final String methodName = "getMembersWithTheseGroups(IUser,Vector,int)";
        trace.entering( methodName );

		if ((groupIDs == null) || (groupIDs.length == 0))
		{
			trace.warningT( methodName, "groupIDs passed is 'null' or empty" );
			return null;
		}
		else {
            int size = groupIDs.length;
            Set[] memberIDSets = new HashSet[size];
            Iterator memberIter = null;
			for ( int nr = 0; nr < size; nr++ )
			{
                String groupID = groupIDs[ nr ].trim();
                // invalidate group in cache to get up-to-date group members
                GroupFactory.invalidateGroupInCache( groupID );
                IGroup group = UMFactory.getGroupFactory().getGroup( groupID );

                trace.infoT( methodName, "Getting members for group: " + groupID );
                Iterator tempIter = null;
                if ( type == GROUP_RETRIEVAL )
                {
                    tempIter = group.getGroupMembers( false );
                }
                else {
                    // default: USER_RETRIEVAL
                    tempIter = group.getUserMembers( false );
                }
                // create an iterator with no existence check
                memberIter = new PrincipalIterator(tempIter, PrincipalIterator.ITERATOR_TYPE_UNIQUEIDS_NOT_CHECKED);
                // if common members for more than ome group should be got
                // -> put the memberIDs into Sets for later
                if (size > 1)
                {
                    // put all member IDs in a Set
                    Set memberIDs = new HashSet();
                    while (memberIter.hasNext())
                    {
                        memberIDs.add( (String) memberIter.next() );
                    }
                    // if current set of member ID's is empty -> return (no common members)
    				if ( memberIDs.isEmpty() )
    				{
                        return null;
                    }
                    else {
                        memberIDSets[ nr ] = memberIDs;
    				}
                }
			}
            // more than one group -> get common members of groups
            if (size > 1)
                memberIter = getMemberList( memberIDSets );

            if ( type == GROUP_RETRIEVAL )
            {
                return getVisibleGroupMembers( proxy, performer, memberIter, memberSF );
            }
            else {
                // default: USER_RETRIEVAL
                return getVisibleUserMembers( proxy, performer, memberIter, memberSF );
            }
		}
	}


	/**
	 *  Description of the Method
	 *
     *@param  proxy         Description of Parameter
	 *@param  members       Description of Parameter
	 *@param  groupList     Description of Parameter
	 *@param  value         Description of Parameter
	 *@return               Returns true if successfull, false otherwise
	 */
	protected boolean assignGroups( IAccessToLogic proxy, Set members, String[] groupList, int value )
	{
		final String methodName = "assignGroups(Set,Iterator,IGroupFactory,int)";

		boolean status = true;
		if ( !checkMember( value ) )
		{
			return false;
		}
		if ( (null == groupList) || (groupList.length == 0) || (null == members) )
		{
			trace.warningT( methodName, "No groups passed!" );
			return false;
		}
		else if ( ( members.isEmpty() ) && ( null != groupList ) )
		{
			trace.infoT( methodName, "No member is passed, returning." );
			return true;
		}
		else {
			try	{
                IGroupFactory groupFactory = UMFactory.getGroupFactory();
                int size = groupList.length;

                for (int nr=0; nr < size; nr++)
                {
					String groupID = groupList[ nr ];
                    trace.infoT( methodName, "Assigning group " + groupID + " to the selected members" );
					IGroup group = groupFactory.getMutableGroup( groupID.trim() );
					if ( !addGroupToMembers( proxy, group, members, value ) )
                    {
                        trace.warningT( methodName, "Assignment of members wasn't successful for group: " 
                                        + group.getDisplayName());
                        status = false;
                    }
				}
			}
			catch ( UMException ex )
			{
				trace.errorT( methodName, "Could not retrieve group for the principal passed" );
				status = false;
			}
		}
		trace.exiting( methodName, new Boolean(status) );
		return status;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  groupIDs         Description of Parameter
	 *@param  memberList       Description of Parameter
	 *@return                  Description of the Returned Value
	 *@exception  UMException  Description of Exception
	 */
	protected boolean removeGroupsFromMembers( String[] groupIDs, Set memberList )
        throws UMException
	{
		final String methodName = "removeGroupsFromMembers(String[],Set,int)";
		trace.entering( methodName, new Object[]{groupIDs, memberList} );

		if ((groupIDs == null) || (groupIDs.length == 0))
		{
			return false;
		}
        IGroupFactory groupfactory = UMFactory.getGroupFactory();
		int size = groupIDs.length;
		boolean status = true;
		for ( int i = 0; i < size; i++ )
		{
            String groupID = groupIDs[ i ];
            try {
    			IGroup group = groupfactory.getMutableGroup( groupID.trim() );
    			trace.infoT( methodName, "removing groupID " + groupID + " from selected members" );
                if (!removeGroupFromMembers( group, memberList ))
                {
                    trace.warningT( methodName, "Removing the assignment wasn't successful for group: " 
                                    + group.getDisplayName());
                    status = false;
                }
            }
            catch (UMException exc)
            {
                trace.warningT(methodName, "Couldn't get mutable group: " + groupID, exc);
            }
		}
		trace.exiting( methodName, status ? Boolean.TRUE : Boolean.FALSE );
		return status;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  value  Description of Parameter
	 *@return        Description of the Returned Value
	 */
	protected boolean checkMember( int value )
	{
		if ( ( value != GROUP_RETRIEVAL ) && ( value != USER_RETRIEVAL ) )
		{
			trace.infoT( "checkMember(int)", "Not supported IPrincipal object" );
			return false;
		}
		return true;
	}


	/**
	 *  Gets the Users object from the memberIDs, but only the users that are
     *  visible to the performer.
	 *
	 *@param  performer        the currently logged in user
     *@param  userIDs          Iterator with the uniqueIDs of the user members              
	 *@return                  IPrincipal[] with the principal objects for the performer
     *                          
	 *@exception  UMException  Description of Exception
	 */
	public static Set getVisibleUserMembers( IAccessToLogic proxy, IUser performer, 
                                                Iterator userIDs, String memberSF )
        throws UMException
	{
		final String methodName = "getVisibleUsersMembers(IUser,Set)";
		trace.entering( methodName, new Object[]{performer} );

        // sort uniqueID's
        TreeSet sortedIDs = new TreeSet( new IDComparator() );
        while (userIDs.hasNext())
        {
            sortedIDs.add( (String) userIDs.next() );
        }
        String[] sortedIDsArray = new String[sortedIDs.size()];
        sortedIDs.toArray( sortedIDsArray );

        int size = sortedIDsArray.length;
        int visibleMembers = 0;
        IUserFactory usf = UMFactory.getUserFactory();

        // check if a search filter for the members is given
        boolean checkSF = (memberSF != null) && (memberSF.length() > 0) && (!"*".equals(memberSF));

        if (checkSF) 
        {
            memberSF = memberSF.toLowerCase(); 
            if (trace.beInfo())
                trace.infoT( methodName, "Search filter is set; memberSF: " + memberSF);
        }
        
        Set members = new HashSet();
		for (int nr = 0; nr < size; nr++)
		{
			try	{
				IUser user = usf.getUser( sortedIDsArray[ nr ] );
                // if search filter is given -> check if member is visible
                if ((checkSF) && (!compliesSearchFilter( user.getDisplayName(), memberSF ))
                    && (!compliesSearchFilter( HelperClass.getPrincipalInfo( user ), memberSF ))) 
                {
                    // user doesn't comply the given search filter
                }
                else if (UserAdminHelper.hasAccess(performer, user, null))
                {
                    // assigned member is visible to performer
                    if (++visibleMembers <= MAX_HITS)
                    {
                        if (trace.beDebug())
                            trace.debugT( methodName, "visible user member: " + user.getUniqueID() );
                        members.add( user );
                    }
                    else {
                        trace.infoT( methodName, "Max. of visible members is exceeded: " + MAX_HITS );
                        if (proxy != null)
                        {
                            // set error message for group admin
                            proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.RESTRICT_SEARCH, 
                                    new Integer[]{new Integer(MAX_HITS)} ) ) );
                        }
                        // set error bean
                        break;
                    }
                }
			}
			catch ( NoSuchUserException exc )
			{
				trace.infoT( methodName, "User doesn't exist anymore: " + sortedIDsArray[ nr ] );
			}
		}
		if ( trace.bePath() )
			trace.exiting( methodName, members.size() + " visible user members" );

		return members;
	}


    /**
     *  Gets the group objects from the memberIDs, but only the groups that are
     *  visible to the performer.
     *
     *@param  performer        the currently logged in user
     *@param  groupIDs         Iterator with the uniqueIDs of the group members              
     *@return                  Set with the principal objects for the performer
     *                          
     *@exception  UMException  Description of Exception
     */
    public static Set getVisibleGroupMembers( IAccessToLogic proxy, IUser performer, 
                                              Iterator groupIDs, String memberSF )
        throws UMException
    {
		final String methodName = "getVisibleGroupMembers(IUser,Iterator)";
        trace.entering( methodName, new Object[]{performer} );

        boolean manageAllCompanies = UserAdminHelper.hasAccess(performer, UserAdminHelper.MANAGE_ALL_COMPANIES);
        boolean companyConcept = UserAdminHelper.isCompanyConceptEnabled();
        String companyID = performer.getCompany();

        // sort uniqueID's
        TreeSet sortedIDs = new TreeSet( new IDComparator() );
        while (groupIDs.hasNext())
        {
            sortedIDs.add( (String) groupIDs.next() );
        }
        String[] sortedIDsArray = new String[sortedIDs.size()];
        sortedIDs.toArray( sortedIDsArray );

        int size = sortedIDsArray.length;
        int visibleMembers = 0;

        // check if a search filter for the members is given
        boolean checkSF = (memberSF != null) && (memberSF.length() > 0) && (!"*".equals(memberSF));
        if (checkSF) memberSF = memberSF.toLowerCase();
        if (trace.beInfo())
            trace.infoT( methodName, "Search filter is set: " + checkSF + "; memberSF: " + memberSF);
        
        IGroupFactory groupF = UMFactory.getGroupFactory();
        Set members = new HashSet();
        for (int nr = 0; nr < size; nr++)
        {
            try {
                IGroup group = groupF.getGroup( sortedIDsArray[ nr ] );
                boolean visibleGroup = true;
                // if search filter is given -> check if member is visible
                if ((checkSF) && (!compliesSearchFilter( group.getDisplayName(), memberSF ))
                    && (!compliesSearchFilter( HelperClass.getPrincipalInfo( group ), memberSF ))) 
                {
                    visibleGroup = false;
                }
                else if (companyConcept && !manageAllCompanies)
                {
                    // if member is a company group, only show it if performer can 
                    // manage all companies or if it's the group for its company
                    String[] values = group.getAttribute(IPrincipal.DEFAULT_NAMESPACE, CompanyGroups.COMPANY_ATTRIBUTE);
                    if (!((values == null) || (values.length == 0) || 
                        util.checkNull(values[0]).equals( companyID )))
                    {
                        visibleGroup = false;
                    }
                }
                if (visibleGroup)
                {
                    if (++visibleMembers <= MAX_HITS)
                    {
                        if (trace.beDebug())
                            trace.debugT( methodName, "visible group member: " + sortedIDsArray[ nr ] );
                        members.add( group );
                    }
                    else {
                        trace.infoT( methodName, "Max. of visible members is exceeded: " + MAX_HITS );
                        if (proxy != null)
                        {
                            // set error message for group admin
                            proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.RESTRICT_SEARCH, 
                                    new Integer[]{new Integer(MAX_HITS)} ) ) );
                        }
                        break;
                    }
                }
            }
            catch ( NoSuchGroupException exc )
            {
                trace.infoT( methodName, "Group doesn't exist anymore: " + sortedIDsArray[ nr ] );
            }
        }
        if ( trace.bePath() )
            trace.exiting( methodName, members.size() + " visible group members" );

        return members;
	}

	/**
	 *  Gets the UserList attribute of the RoleAssignmentFactory object
	 *
	 *@param   memberSets      Array of Sets with uniqueID's (of members)
	 *@return                  Set with inqueID's of common members
	 */
	public static Iterator getMemberList( Set[] memberSets )
	{
		final String methodName = "getMemberList(Set[])";

		int size = memberSets.length;
		trace.infoT( methodName, "Array contains " + size + " Sets of members" );

        // no memberSets is no valid condition
        if (size == 0) return null;            

		for ( int index = 1; index < size; index++ )
		{
			if ((null != memberSets[0]) && (null != memberSets[index]))
			{
                boolean changed = memberSets[0].retainAll( memberSets[index] );
                if (trace.beDebug())
                    trace.debugT( methodName, "MemberSet was reduced: " + changed);
            }
		}
		return memberSets[0].iterator();
	}


	/**
	 *  Assigns the given members to a given group.
	 *
     *@param  proxy         
	 *@param  group         The group, the members should be added to
	 *@param  memberList    The list of members that should be added to the group
	 *@param  value         defines if the members are groups or users
	 *@return               true if successfull, false otherwise
	 */
	private boolean addGroupToMembers( IAccessToLogic proxy, IGroup group, Set memberList, int value )
	{
		final String methodName = "addGroupToMembers(IAccessToLogic,IGroup,Set,int)";
        trace.entering( methodName, new Object[]{group, memberList} );

        if ( !checkMember( value ) )
        {
            trace.infoT( methodName, value + " is no valid identifier" );
            return false;
        }
		boolean status = true;
		trace.infoT( methodName, "Adding " + memberList.size() + " Members" );
		Iterator memberIter = memberList.iterator();
        IPrincipal member = null;
		try
		{
			while ( memberIter.hasNext() )
			{
				member = ( ( IPrincipal ) memberIter.next() );
                String memberID = member.getUniqueID();
                String memberName = member.getDisplayName();
				if ( value == USER_RETRIEVAL )
				{
					if ( !group.isUserMember( memberID, false ) )
					{
                        if (trace.beInfo())
    						trace.infoT( methodName, "Adding user " + memberName + "( " +  memberID + " )" );
						group.addUserMember( memberID );
					}
				}
				else if ( value == GROUP_RETRIEVAL )
				{
					if ( !group.isGroupMember( memberID, false ) )
					{
                        String groupID = group.getUniqueID();
                        if (groupID.equals( memberID ))
                        {
                            trace.infoT( methodName, "Group wasn't added to group " + memberName + ", because they were identical");
                            proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.ERROR_ASSIGN_ITSELF ) ) );
                        }
                        else {
                            if (trace.beInfo())
                                trace.infoT( methodName, "Adding group " + memberName + "( " +  memberID + " )" );
                            group.addGroupMember( memberID );
                        }
					}
				}
			}
            group.save();
            group.commit();            
		}
		catch ( UMException ex )
		{
            group.rollback();
			trace.errorT( methodName, "Assignment of member(s) failed", ex );
            status = false;
		}
		trace.exiting( methodName, status ? Boolean.TRUE : Boolean.FALSE );
		return status;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  memberList       Set of IUser or IGroup objects
	 *@param  group            Description of Parameter
	 *@return                  returns true if successfull, false otherwise
	 *@exception  UMException  throws UMException if user or role can not be
	 *                         retrieved from database
	 */
	private boolean removeGroupFromMembers( IGroup group, Set memberList )
	{
		final String methodName = "removeGroupFromMembers(IGroup,Set)";
        trace.entering( methodName, new Object[]{group, memberList} );

        boolean status = true;
        trace.infoT( methodName, "Removing " + memberList.size() + " members" );
        Iterator memberIter = memberList.iterator();

        try {
            while ( memberIter.hasNext() )
            {
                IPrincipal member = ( IPrincipal ) memberIter.next();
                if ( null != member )
                {
                    trace.infoT( methodName, "Removing " + member.getDisplayName() );
                    group.removeMember( member.getUniqueID() );
                }
                else {
                    throw new UMException( "Member that should be removed was NULL!" );
                }
    		}
            group.save();
            group.commit();            
        }
        catch (UMException exc)
        {
            group.rollback();
            trace.errorT( methodName, "Removing of assignments failed", exc );
            status = false;
        }
        
		trace.exiting( methodName, status ? Boolean.TRUE : Boolean.FALSE );
		return status;
	}


    /**************************************************************************/
    private static boolean compliesSearchFilter(String attribute, String searchFilter)
    {
        trace.debugT("compliesSearchFilter(String,String)", "attribute: " + attribute);
        
        if (attribute == null || searchFilter == null) 
        {
        	return false;
        }
            
        boolean status = false;
        attribute = attribute.toLowerCase();
        
        int pos = searchFilter.indexOf("*");
        if (pos >= 0) 
        {
            if (pos == 0)
            {
                // search pattern: *Name*
                if (searchFilter.charAt( searchFilter.length()-1 ) == '*')
                {
                    if (attribute.indexOf( searchFilter.substring(1, searchFilter.length()-1)) >= 0)
                        status = true; 
                }
                // search pattern: *Name
                else if (attribute.endsWith( searchFilter.substring( pos+1 ) ))
                    status = true;
            }
            else if (pos == (searchFilter.length()-1))
            {
                // search pattern: Name*
                if (attribute.startsWith( searchFilter.substring(0, pos)))
                    status = true;
            }
            else {
                // search pattern: Name1*Name2
                if (attribute.startsWith( searchFilter.substring(0, pos)) &&
                    attribute.endsWith( searchFilter.substring( pos+1 ) ))
                    status = true;
            }
        }
        else {
            // check if attribute is identical with searchFilter
            if (searchFilter.equals( attribute ))
                status = true;
        }
        trace.debugT("compliesSearchFilter(String,String)", "status: " + status);
        return status;
    } 


	/**
	 *@return    only one instance of this factory
	 */
	public static GroupAssignmentFactory getInstance()
	{
		if ( groupassignfactory == null )
		{
			groupassignfactory = new GroupAssignmentFactory();
		}
		return groupassignfactory;
	}
}

/******************************************************************************/
class IDComparator implements java.util.Comparator
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
