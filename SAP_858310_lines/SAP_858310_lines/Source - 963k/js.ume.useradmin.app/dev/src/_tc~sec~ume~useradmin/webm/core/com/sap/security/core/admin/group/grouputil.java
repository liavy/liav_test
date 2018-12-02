package com.sap.security.core.admin.group;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.sap.security.api.IGroup;
import com.sap.security.api.IGroupFactory;
import com.sap.security.api.IGroupSearchFilter;
import com.sap.security.api.IPrincipal;
import com.sap.security.api.IPrincipalSearchFilter;
import com.sap.security.api.ISearchResult;
import com.sap.security.api.IUser;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.api.UMRuntimeException;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.IAccessToLogic;
import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.core.admin.role.RoleAdminMessagesBean;
import com.sap.security.core.admin.util;
import com.sap.security.core.admin.role.HelperClass;
import com.sap.security.core.persistence.datasource.imp.CompanyGroups;
import com.sap.security.core.util.ErrorBean;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.Message;

/**
 *  Description of the Class
 *
 *@author     Fereidoon Samii
 *@created    May 16, 2001
 */
public class GroupUtil
{
	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/group/GroupUtil.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
	private final static IUMTrace trace = InternalUMFactory.getTrace( VERSIONSTRING );


	/**
	 *  Gets the Groups attribute of the GroupUtil class
	 *
	 *@param  allGroupIDs      Description of Parameter
	 *@return                  The Groups value
	 *@exception  UMException  Description of Exception
	 */
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
			trace.infoT( methodName, "Retrieving User Object for groupID= " + groupID );
			IGroup group = UMFactory.getGroupFactory().getGroup( groupID );
			if ( null == group )
			{
				trace.errorT( methodName, "UMFactory.getGroupFactory().getGroup returned null for groupID " + groupID );
			}
			else {
				trace.infoT( methodName, "getting " + group.getDisplayName() );
				groupSet.add( group );
			}
		}
		trace.exiting( methodName );
		return groupSet;
	}


    /**
     *  Gets all groups that are visible for the given user
     *
     *@param    performer      the user for that all groups should be returned
     *@return                  all visible groups
     *@exception  UMException  Description of Exception
     */
    protected static Iterator getVisibleGroups(IUser performer) 
        throws UMException
    {
        return getVisibleGroups( null, performer, "*", false );
    }

	/**
     *  Gets the groups that match the search filter and that are visible for 
     *  the given user 
	 *
     *@param    performer      the user for that all groups should be returned
     *@param    searchFilter   the group search filter string
     *@param    checkMaxHits   defines if only the defined max. hits should be returned
	 *@return                  the visible groups
	 *@exception  UMException  Description of Exception
	 */
	public static Iterator getVisibleGroups(IAccessToLogic proxy, IUser performer, 
                                               String searchFilter, boolean checkMaxHits) 
        throws UMException
	{
		final String methodName = "getVisibleGroups(IUser)";
		trace.entering( methodName );

		IGroupFactory groupfactory = UMFactory.getGroupFactory();
		IGroupSearchFilter sf = groupfactory.getGroupSearchFilter();
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
		ISearchResult groups = groupfactory.searchGroups( sf );

        boolean manageAllCompanies = UserAdminHelper.hasAccess(performer, UserAdminHelper.MANAGE_ALL_COMPANIES);
        boolean companyConcept = UserAdminHelper.isCompanyConceptEnabled();
        String companyID = performer.getCompany();

        if (companyConcept && !manageAllCompanies)
        {
            trace.infoT(methodName, "Company groups are not visible to this user!");
        }

        // sort found group IDs
        TreeSet sortedIDs = new TreeSet( new UniqueIDComparator() );
        while (groups.hasNext())
        {
            sortedIDs.add( (String) groups.next() );
        }
        String[] sortedIDsArray = new String[sortedIDs.size()];
        sortedIDs.toArray( sortedIDsArray );

        int maxHits = UMFactory.getProperties().getNumber("ume.admin.search_maxhits_warninglevel", 200);
        int size = groups.size();
        // don't return more groups as defined as max. search result
        if ((checkMaxHits) && (size > maxHits))
        {
            if (proxy != null)
            {
                // set error message for group admin
                proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.SEARCH_RESULT_BEYOND_MAXHITS, 
                        new Integer[]{new Integer(size), new Integer(maxHits)} ) ) );
            }
            size = maxHits;
        }
        
        TreeSet visibleGroups = new TreeSet( new GroupNameComparator() );
        for (int i = 0; i < size; i++)
        {
            String uniqueIdOfGroup = sortedIDsArray[i];
            try
            {
    			IGroup group = groupfactory.getGroup( uniqueIdOfGroup );
                // only show company groups if performer is allowed to manage all companies
                if (companyConcept && !manageAllCompanies)
                {
                    String[] values = group.getAttribute(IPrincipal.DEFAULT_NAMESPACE, CompanyGroups.COMPANY_ATTRIBUTE);
                    if ((values == null) || (values.length == 0) || 
                        util.checkNull(values[0]).equals( companyID ))
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
            catch (UMException exc)
            {
                trace.errorT(methodName, "Couldn't get group for ID: " + uniqueIdOfGroup);    
            }
        }
		trace.exiting( methodName );
		return visibleGroups.iterator();
	}


	/**
	 *  Gets the GroupDesciption attribute of the GroupUtil class
	 *
	 *@param  groupfactory  Description of Parameter
	 *@param  groupIDs      Description of Parameter
	 *@return               The GroupDesciption value
	 */
	protected static String getGroupDesciption( IGroupFactory groupfactory, String groupIDs[] )
	{
		final String methodName = "getGroupDesciption(IGroupFactory,String)";
		trace.entering( methodName, new Object[]{groupfactory, groupIDs} );

		if ( ( null == groupIDs ) || ( null == groupfactory ) )
		{
			return "";
		}

		StringBuffer groupDescription = new StringBuffer();
		String description = null;
		
		for (int i=0; i < groupIDs.length; i++) 
        {
			String groupID = groupIDs[i];
			try
			{
                IGroup group = groupfactory.getGroup( groupID.trim() );
                description = group.getDescription();
                if ( null != description )
                {
                    groupDescription.append( description );
                    groupDescription.append( ", " );
                }
			}
			catch ( UMException ex )
			{
				trace.warningT( methodName, "Couldn't get group for groupID= " + groupID );
			}
		}
		description = groupDescription.toString();
		trace.infoT( methodName, "description(before): " + description );
		if ( groupIDs.length > 1 )
		{
			return "MultiGroup";
		}
		int index = description.lastIndexOf( ',' );
		trace.infoT( methodName, "pos: " + index );
		if ( index <= 0 )
		{
			return "";
		}
		description = description.substring( 0, index );
		trace.infoT( methodName, "description(after): " + description );

		trace.exiting( methodName, description );
		return description;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  groupFactory     Description of Parameter
	 *@param  groupnames       Description of Parameter
	 *@return                  Description of the Returned Value
	 *@exception  UMException  Description of Exception
	 */
	protected static String makeGroupNames( IGroupFactory groupFactory, String[] groupnames )
			 throws UMException
	{
		final String methodName = "makeGroupNames(IGroupFactory,String[])";
		trace.entering( methodName );

		int count = groupnames.length;
		if ( 0 == count )
		{
			return "";
		}
		String[] groups = new String[count];
		for ( int i = 0; i < count; i++ )
		{
			IGroup group = groupFactory.getGroup( groupnames[i].trim() );
			if ( null == group )
			{
				trace.errorT( methodName, "group.getGroup(" + groupnames[i] + ") returned null" );
				throw new UMRuntimeException();
			}
			groups[i] = group.getDisplayName();
			trace.infoT( methodName, "groupID: " + groupnames[i] );
			trace.infoT( methodName, "groupName: " + groups[i] );
		}
		trace.exiting( methodName );
		return HelperClass.makeString( count, groups );
	}
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
