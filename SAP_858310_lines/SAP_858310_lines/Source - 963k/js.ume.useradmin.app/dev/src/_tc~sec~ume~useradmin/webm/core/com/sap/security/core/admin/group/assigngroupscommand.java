package com.sap.security.core.admin.group;

import java.util.*;
import com.sap.security.api.*;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.IAccessToLogic;
import com.sap.security.core.admin.UserAdminCommonLogic;
import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.core.admin.ListBean;
import com.sap.security.core.admin.util;
import com.sap.security.core.admin.role.*;
import com.sap.security.core.admin.permissions.UMAdminPermissions;
import com.sap.security.core.imp.PrincipalFactory;
import com.sap.security.core.persistence.datasource.imp.CompanyGroups;
import com.sap.security.core.util.*;
import com.sap.security.core.util.IUMTrace;

/**
 *  This class is called before the group assignment screen is shown.
 *  It gets all common members of the selected group(s): 
 *
 *@author     Markus Liepold
 */
public class AssignGroupsCommand implements Command
{
	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/group/AssignGroupsCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);

    private String next;

	/**
	 *  Constructor for the AssignGroupsCommand object
	 *
	 *@param  next  Description of Parameters
	 */
	public AssignGroupsCommand( String next )
	{
		this.next = next;
	}


	/**
	 *  Constructor for the AssignGroupsCommand object
	 *
	 *@param  proxy  Description of Parameter
	 *@param  key    Description of Parameter
	 */
	public AssignGroupsCommand( IAccessToLogic proxy , String key )
	{
        final String methodName = "AssignGroupsCommand(proxy,key)";
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
				next = UserAdminCommonLogic.errorPage;
			}
		}
		catch ( MissingResourceException e )
		{
			trace.errorT( methodName, "Resource" + key + " not found", e );
			proxy.setRequestAttribute( "javax.servlet.jsp.jspException", e );
			next = UserAdminCommonLogic.errorPage;
		}
	}


	/**
	 *  This method expects the group name(s) that is used to get the common
	 *  members. Next it stores the members with common groups, the group names 
     *  and the ID into the session and then calls the appropriate jsp file
	 *  (group_assignment.jsp)
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
    		trace.infoT( methodName, "request parameters: " +HelperClass.reportRequestParameters(proxy) );

		//contains internalGroupnames(i.e groupIDs)
		String groupnames[] = util.getUniqueIDs( proxy, "groupname" );
        if (groupnames == null)
        {
            groupnames = (String[]) proxy.getSessionAttribute( "_groupIDs" );
        }
        String memberSF = HelperClass.getParam( proxy, "memberSF");
        String principal = HelperClass.setPrincipalRequestParam(proxy);
		String groupnamesReport = "";
		String groupnameIDs = "";

		// HelperClass.checkActiveUser throws UMRuntimeException if activeUser is null;
		IUser performer = HelperClass.checkActiveUser( proxy );
        // checkPermission throws java.security.AccessControlException if failed.
        performer.checkPermission( new UMAdminPermissions(UserAdminHelper.VIEW_GROUPS) );

        IGroupFactory groupFactory = UMFactory.getGroupFactory();
		try
		{
			if ( null != groupnames )
			{
				groupnamesReport = GroupUtil.makeGroupNames( groupFactory, groupnames );
				groupnameIDs = HelperClass.makeIDs( groupnames );
                if (trace.beInfo()) {
    				trace.infoT( methodName, "group names: " + groupnamesReport );
	       			trace.infoT( methodName, "group IDs: " + groupnameIDs );
                }
                if (errorCompanyGroupAssignment(proxy, performer, groupnames, principal))
                {
                    return GroupAdminLogic.groupManagementPage;
                }
			}
		}
		catch ( UMException ex )
		{
			trace.errorT( methodName, "Error while retrieving group ID's and names: ", ex );
			proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.EXCEPTION_GROUP_ASSIGNMENT ) ) );
            throw new RoleCommandException( "AssignGroupsCommand: Error while retrieving group ID's and names; " + ex.getMessage() );
		}

        // get all members of all selected groups
		Set membersList = null;
        try
		{
            GroupAssignmentFactory gaFactory = GroupAssignmentFactory.getInstance();
            if (principal.equalsIgnoreCase("user"))
            {
                membersList = gaFactory.getMembersWithTheseGroups( proxy, performer, groupnames, GroupAssignmentFactory.USER_RETRIEVAL, memberSF );

            }
            else if (principal.equalsIgnoreCase("group"))
            {
                membersList = gaFactory.getMembersWithTheseGroups( proxy, performer, groupnames, GroupAssignmentFactory.GROUP_RETRIEVAL, memberSF );
            }
            else
            {
                trace.infoT(methodName, "Not supported principal" );
            }
 		}
		catch ( UMException e )
		{
			trace.errorT( methodName, e );
			proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.EXCEPTION_SEARCH_FAILED ) ) );
			throw new RoleCommandException( "AssignGroupsCommand:execute " + e.getMessage() );
		}
        IPrincipal[] assignedMembers = null;

		if ((membersList != null) && (!membersList.isEmpty() ))
		{
            int size = membersList.size();
			assignedMembers = new IPrincipal[size];
            trace.infoT(methodName, size + " members found:");
			membersList.toArray( assignedMembers );
		}

        // check for read-only groups
        int readOnlyGroups = 0;
        try {
            IPrincipalFactory pf = UMFactory.getPrincipalFactory();
            for (int i=0; i < groupnames.length; i++)
            {
                if (!pf.isPrincipalAttributeModifiable(groupnames[i], 
                     IPrincipal.DEFAULT_RELATION_NAMESPACE, IPrincipal.PRINCIPAL_RELATION_MEMBER_ATTRIBUTE))
                {
                    readOnlyGroups++;
                }
            }
            // show info message, if one or more groups are read-only
            if (readOnlyGroups > 0)
            {
                trace.infoT( methodName, readOnlyGroups + " of the selected groups are read-only");
                if (groupnames.length > 1)
                    proxy.setRequestAttribute( InfoBean.beanId, new InfoBean( new Message( RoleAdminMessagesBean.READONLY_GROUPS_MODIFY ) ) );
                else                
                    proxy.setRequestAttribute( InfoBean.beanId, new InfoBean( new Message( RoleAdminMessagesBean.READONLY_GROUP_MODIFY ) ) );
            }
        }
        catch (UMException exc)
        {
            trace.errorT( methodName, "Check for read-only groups failed", exc );
            proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.EXCEPTION_GROUP_ASSIGNMENT ) ) );
            throw new RoleCommandException( "AssignGroupsCommand:execute " + exc.getMessage() );
        }

        String groupDescription = GroupUtil.getGroupDesciption(groupFactory, groupnames);
        ListBean list = HelperClass.setUpListBean(proxy, principal, assignedMembers);
        proxy.setRequestAttribute( ListBean.beanId, list );
		proxy.setRequestAttribute( "groupnamesReport", groupnamesReport );
		proxy.setSessionAttribute( "_groupIDs", groupnames);
		proxy.setRequestAttribute( "groupIDs", groupnameIDs );
		proxy.setRequestAttribute( "groupDescription", groupDescription );
        proxy.setRequestAttribute( "readOnlyGroups", new Boolean( readOnlyGroups>0 ) );
        proxy.setRequestAttribute( "memberSF", memberSF);

		trace.exiting( methodName, "returned next:" + next );
		return next;
	}
    
    
    /**************************************************************************/
    private boolean errorCompanyGroupAssignment(IAccessToLogic proxy, IUser performer, 
                                                String[] groupnames, String principal)
        throws UMException
    {
        final String methodName = "checkCompanyGroupAssignment(IAccessToLogic,String[],String)";

        // if company concept isn't enabled -> no need to check for company groups
        if ((groupnames == null) || (!UserAdminHelper.isCompanyConceptEnabled())) 
        {
            return false;
        }
        
        PrincipalFactory pf = ((PrincipalFactory) UMFactory.getPrincipalFactory());
        int amountCompGroups = 0;
        int groupNr = 0;

        while ( !((groupNr >= groupnames.length) || (amountCompGroups > 1)) )
        {
            String[] idParts = pf.getPrincipalIdParts( groupnames[ groupNr ] );
            if (CompanyGroups.DATASOURCE_ID.equals( idParts[1] ))
            {
                amountCompGroups++;
            }
            groupNr++;
        }

        // no company groups selected
        if (amountCompGroups == 0) return false;

        String messageID = null;
        if (principal.equalsIgnoreCase("user"))
        {
            if (amountCompGroups > 1)
            {
                messageID = RoleAdminMessagesBean.COMPANY_GROUP_SELECTION;
            }
            else {
                proxy.setRequestAttribute( InfoBean.beanId, new InfoBean( new Message( RoleAdminMessagesBean.COMPANY_GROUP_ASSIGN_USER ) ) );
                return false;
            }
        }
        else if (principal.equalsIgnoreCase("group"))
        {
            messageID = RoleAdminMessagesBean.COMPANY_GROUP_ASSIGN_GROUP;                
        }

        String searchFilter = (String)proxy.getSessionAttribute("groupSearchFilter");
        Iterator sortedGroups;
        try {
            // get the visible Groups for the user that matches the current search filter
            sortedGroups = GroupUtil.getVisibleGroups( proxy, performer, searchFilter, true );
        }
        catch (UMException ex)
        {
            trace.errorT(methodName, "No visible groups returned.", ex);
            sortedGroups = new HashSet().iterator();
        }
        proxy.setRequestAttribute( "list_groups", sortedGroups );
        proxy.setRequestAttribute( "searchFilter", searchFilter );
        proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( messageID ) ) );
        return true;
    }
}
