
package com.sap.security.core.admin.role;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.sap.security.api.IPrincipal;
import com.sap.security.api.IRole;
import com.sap.security.api.IRoleFactory;
import com.sap.security.api.IUser;
import com.sap.security.api.PrincipalIterator;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.IAccessToLogic;
import com.sap.security.core.admin.group.GroupAssignmentFactory;
import com.sap.security.core.imp.RoleFactory;
import com.sap.security.core.util.IUMTrace;

/**
 *  This class implements the role to user/group assignments
 *
 *@author     d038377
 *@created    14.05.2004
 *@version    2.0
 */

public class RoleAssignmentFactory
{
	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/role/RoleAssignmentFactory.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
	private final static IUMTrace trace = InternalUMFactory.getTrace( VERSIONSTRING );

	// only one instant of roleassignfactory is allowed
	private static RoleAssignmentFactory roleassignfactory = null;

	protected final static int GROUP_RETRIEVAL = 0;
	protected final static int USER_RETRIEVAL = 1;

    /**
     *  Returns the members that all given roles have in common.
     *
     *@param  roleIDs       String[] of rolenameIDs
     *@param  performer     currently logged in user
     *@param  type          values for different supported IPrincipals
     *@return               Set of IPrincipal elements
     *@exception  UMException  Description of Exception
     */
    protected Set getMembersWithTheseRoles( IAccessToLogic proxy, IUser performer, 
                                            String[] roleIDs, int type, String memberSF )
        throws UMException
    {
        final String methodName = "getMembersWithTheseRoles(IUser,Vector,int)";
        trace.entering( methodName );

        if ((roleIDs == null) || (roleIDs.length == 0))
        {
            trace.warningT( methodName, "roleIDs passed is 'null' or empty" );
            return null;
        }
        else {
            int size = roleIDs.length;
            Set[] memberIDSets = new HashSet[size];
            Iterator memberIter = null;
            for ( int nr = 0; nr < size; nr++ )
            {
                String roleID = roleIDs[ nr ].trim();
                // invalidate role in cache to get up-to-date role members
                RoleFactory.invalidateRoleInCache( roleID );
                IRole role = UMFactory.getRoleFactory().getRole( roleID );

                trace.infoT( methodName, "Getting members for role: " + roleID );
                Iterator tempIter = null;
                if ( type == GROUP_RETRIEVAL )
                {
                    tempIter = role.getGroupMembers( false );
                }
                else {
                    // default: USER_RETRIEVAL
                    tempIter = role.getUserMembers( false );
                }
                // create an iterator with no existence check
                memberIter = new PrincipalIterator(tempIter, PrincipalIterator.ITERATOR_TYPE_UNIQUEIDS_NOT_CHECKED);
                // if common members for more than ome role should be got
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
            // more than one role -> get common members of roles
            if (size > 1)
                memberIter = GroupAssignmentFactory.getMemberList( memberIDSets );

            if ( type == GROUP_RETRIEVAL )
            {
                return GroupAssignmentFactory.getVisibleGroupMembers( proxy, performer, memberIter, memberSF );
            }
            else {
                // default: USER_RETRIEVAL
                return GroupAssignmentFactory.getVisibleUserMembers( proxy, performer, memberIter, memberSF );
            }
        }
    }
    
    
	/**
	 *  Description of the Method
	 *
     *@param  proxy        Description of Parameter
	 *@param  members      Set of IPrincipal (are IUser or IGroup elements)
     *@param  roleList     Roles to be assigned to
     *@param  value        if 0, members are IUser, if 1 they are IGroup
	 *@return              Returns true if successfull, false otherwise
	 */
    protected boolean assignRoles( Set members, String[] roleList, int value )
	{
		final String methodName = "assignRoles(Set,Iterator,IRoleFactory,int)";

		boolean status = true;
		if ( !checkMember( value ) )
		{
			return false;
		}
        if ( (null == roleList) || (roleList.length == 0) || (null == members) )
		{
			trace.warningT( methodName, "No roles passed!" );
			return false;
		}
		else if ( ( members.isEmpty() ) && ( null != roleList ) )
		{
			trace.infoT( methodName, "No member is passed, returning." );
			return true;
		}
		else {
			try	
            {
                IRoleFactory roleFactory = UMFactory.getRoleFactory();
                int size = roleList.length;

                for (int nr=0; nr < size; nr++)
                {
                    String roleID = roleList[ nr ];
                    trace.infoT( methodName, "Assigning role " + roleID + " to the selected members" );
                    IRole role = roleFactory.getMutableRole( roleID.trim() );
                    if ( !addRoleToMembers( role, members, value ) )
                    {
                        trace.warningT( methodName, "Assignment of members wasn't successful for group: " 
                                        + role.getDisplayName());
                        status = false;
                    }
                }
			}
			catch ( UMException ex )
			{
				trace.errorT( methodName, "Could not retrieve role for the roleID passed" );
				status = false;
			}
		}
		trace.exiting( methodName, new Boolean(status) );
		return status;
	}


    /**
     *  Assigns the given members to a given role.
     *
     *@param  role         IRole object
     *@param  userList     Set containing IPrincipal Object
     *@param  value        0 for adding to group, 1 for adding to user
     *@return              true if successfull, false otherwise
     */
    private boolean addRoleToMembers( IRole role, Set memberList, int value )
    {
        final String methodName = "addRoleToMembers(IRole,Set,IRoleFactory,int)";
        trace.entering( methodName, new Object[]{role, memberList} );

        if ( !checkMember( value ) )
        {
            trace.infoT( methodName, value + " is no valid identifier" );
            return false;
        }
        boolean status = true;
        trace.infoT( methodName, "Adding " + memberList.size() + " members" );
        Iterator memberIter = memberList.iterator();
        IPrincipal member = null;
        try
        {
            memberIter = memberList.iterator();
            while ( memberIter.hasNext() )
            {
                member = ( (IPrincipal) memberIter.next() );
                String memberID = member.getUniqueID();
                String memberName = member.getDisplayName();

                if ( value == USER_RETRIEVAL )
                {
                    if ( !role.isUserMember( memberID, false ) )
                    {
                        trace.infoT( methodName, "Adding " + memberName + "( " +  memberID + " )" );
                        role.addUserMember( memberID );
                    }
                }
                else if ( value == GROUP_RETRIEVAL )
                {
                    if ( !role.isGroupMember(memberID, false ) )
                    {
                        trace.infoT( methodName, "Adding " + memberName + "( " +  memberID + " )" );
                        role.addGroupMember( memberID );
                    }
                }
            }
            role.save();
            role.commit();            
        }
        catch ( UMException ex )
        {
            role.rollback();
            trace.errorT( methodName, "Assignment of member(s) failed", ex );
            status = false;
        }
        trace.exiting( methodName, status ? Boolean.TRUE : Boolean.FALSE );
        return status;
    }


    /**
     *  Description of the Method
     *
     *@param  roleIDs          Description of Parameter
     *@param  memberList       Description of Parameter
     *@return                  Description of the Returned Value
     *@exception  UMException  Description of Exception
     */
    protected boolean removeRolesFromMembers( String[] roleIDs, Set memberList )
        throws UMException
    {
        final String methodName = "removeRolesFromMembers(String[],Set,int)";
        trace.entering( methodName, new Object[]{roleIDs, memberList} );

        if ((roleIDs == null) || (roleIDs.length == 0))
        {
            return false;
        }
        IRoleFactory rolefactory = UMFactory.getRoleFactory();
        int size = roleIDs.length;
        boolean status = true;
        for ( int i = 0; i < size; i++ )
        {
            String roleID = roleIDs[ i ];
            try {
                IRole role = rolefactory.getMutableRole( roleID.trim() );
                trace.infoT( methodName, "removing roleID " + roleID + " from selected members" );
                if (!removeRoleFromMembers( role, memberList ))
                {
                    trace.warningT( methodName, "Removing the assignment wasn't successful for role: " 
                                    + role.getDisplayName());
                    status = false;
                }
            }
            catch (UMException exc)
            {
                trace.warningT(methodName, "Couldn't get mutable role: " + roleID, exc);
            }
        }
        trace.exiting( methodName, status ? Boolean.TRUE : Boolean.FALSE );
        return status;
    }


    /**
     *  Description of the Method
     *
     *@param  memberList       Set of IUser or IGroup objects
     *@param  role             Description of Parameter
     *@return                  returns true if successfull, false otherwise
     *@exception  UMException  throws UMException if user or role can not be
     *                         retrieved from database
     */
    private boolean removeRoleFromMembers( IRole role, Set memberList )
    {
        final String methodName = "removeRoleFromMembers(IRole,Set)";
        trace.entering( methodName, new Object[]{role, memberList} );

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
                    role.removeMember( member.getUniqueID() );
                }
                else {
                    throw new UMException( "Member that should be removed was NULL!" );
                }
            }
            role.save();
            role.commit();            
        }
        catch (UMException exc)
        {
            role.rollback();
            trace.errorT( methodName, "Removing of assignments failed", exc );
            status = false;
        }
        
        trace.exiting( methodName, status ? Boolean.TRUE : Boolean.FALSE );
        return status;
    }


	/**
	 *@return    only one instance of this factory
	 */
	public static RoleAssignmentFactory getInstance()
	{
		if ( roleassignfactory == null )
		{
			roleassignfactory = new RoleAssignmentFactory();
		}
		return roleassignfactory;
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
			trace.infoT( "CheckMember(int)", "Not supported IPrincipal object" );
			return false;
		}
		return true;
	}
    
}
